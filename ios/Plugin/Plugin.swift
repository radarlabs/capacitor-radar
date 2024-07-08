import Foundation
import CoreLocation
import Capacitor
import RadarSDK

@objc(RadarPlugin)
public class RadarPlugin: CAPPlugin, RadarDelegate, RadarVerifiedDelegate {
    
    let locationManager = CLLocationManager()

    public func didReceiveEvents(_ events: [RadarEvent], user: RadarUser?) {
        DispatchQueue.main.async {
            self.notifyListeners("events", data: [
                "events": RadarEvent.array(for: events) ?? [],
                "user": user?.dictionaryValue() ?? {}
            ])
        }
    }

    public func didUpdateLocation(_ location: CLLocation, user: RadarUser) {
        DispatchQueue.main.async {
            self.notifyListeners("location", data: [
                "location": Radar.dictionaryForLocation(location),
                "user": user.dictionaryValue()
            ])
        }
    }

    public func didUpdateClientLocation(_ location: CLLocation, stopped: Bool, source: RadarLocationSource) {
        DispatchQueue.main.async {
            self.notifyListeners("clientLocation", data: [
                "location": Radar.dictionaryForLocation(location),
                "stopped": stopped,
                "source": Radar.stringForLocationSource(source)
            ])
        }
    }

    public func didFail(status: RadarStatus) {
        DispatchQueue.main.async {
            self.notifyListeners("error", data: [
                "status": Radar.stringForStatus(status)
            ])
        }
    }
    
    public func didLog(message: String) {
        DispatchQueue.main.async {
            self.notifyListeners("log", data: [
                "message": message
            ])
        }
    }

    public func didUpdateToken(_ token: String) {
        DispatchQueue.main.async {
            self.notifyListeners("token", data: [
                "token": token
            ])
        }
    }

    // MARK: - CAPPlugin

    @objc func initialize(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            guard let publishableKey = call.getString("publishableKey") else {
                call.reject("publishableKey is required")

                return
            }
            UserDefaults.standard.set("Capacitor", forKey: "radar-xPlatformSDKType")
            UserDefaults.standard.set("3.10.0", forKey: "radar-xPlatformSDKVersion")
            Radar.initialize(publishableKey: publishableKey)
            call.resolve()
        }
    }

    override public func load() {
        // By default, Capacitor passes JavaScript Dates to native code as
        // ISO8601 strings. Setting this to false keeps them as Date objects,
        // so no further string -> Date parsing needs to take place in iOS.
        // (Android doesn't have this property, so it _does_ need to do the
        // conversion manually.)
        // (https://capacitorjs.com/docs/core-apis/data-types#dates)
        shouldStringifyDatesInCalls = false
        Radar.setDelegate(self)
        Radar.setVerifiedDelegate(self)
    }

    @objc func setUserId(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            let userId = call.getString("userId")
            Radar.setUserId(userId)
            call.resolve()
        }
    }

    @objc func setLogLevel(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            let level = call.getString("level") ?? ""
            if (level == "") {                
                call.reject("level is required")
                return
            }
            var logLevel = RadarLogLevel.none
            switch level.lowercased() {
            case "error":
                logLevel = RadarLogLevel.error
            case "warning":
                logLevel = RadarLogLevel.warning
            case "info":
                logLevel = RadarLogLevel.info
            case "debug":
                logLevel = RadarLogLevel.debug
            default:                
                call.reject("invalid level: " + level)
                return
            }
            Radar.setLogLevel(logLevel)
            call.resolve()
        }
    }

    @objc func getUserId(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            call.resolve([
                "userId": Radar.getUserId()!
            ]);
        }
    }

    @objc func setDescription(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            let description = call.getString("description")
            Radar.setDescription(description)
            call.resolve()
        }
    }

    @objc func getDescription(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            call.resolve([
                "description": Radar.getDescription()!
            ]);
        }
    }

    @objc func setMetadata(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            let metadata = call.getObject("metadata")
            Radar.setMetadata(metadata)
            call.resolve()
        }
    }

    @objc func getMetadata(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            call.resolve(Radar.getMetadata() as? [String:String] ?? [:]);
        }
    }

    @objc func setAnonymousTrackingEnabled(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            guard let enabled = call.getBool("enabled") else {
                call.reject("enabled is required")

                return
            };
            Radar.setAnonymousTrackingEnabled(enabled)
            call.resolve()
        }
    }

    @objc func getLocationPermissionsStatus(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            let authorizationStatus = CLLocationManager.authorizationStatus()
            var authorizationStatusStr = "DENIED"
            switch authorizationStatus {
            case .authorizedAlways:
                authorizationStatusStr = "GRANTED_BACKGROUND"
            case .authorizedWhenInUse:
                authorizationStatusStr = "GRANTED_FOREGROUND"
            default:
                authorizationStatusStr = "DENIED"
            }
            call.resolve([
                "status": authorizationStatusStr
            ])
        }
    }

    @objc func requestLocationPermissions(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            guard let background = call.getBool("background") else {
                call.reject("background is required")

                return
            }
            if background {
                self.locationManager.requestAlwaysAuthorization()
            } else {
                self.locationManager.requestWhenInUseAuthorization()
            }
            call.resolve()
        }
    }

    @objc func getLocation(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            let desiredAccuracy = call.getString("desiredAccuracy") ?? "medium";
            var accuracy = RadarTrackingOptions.desiredAccuracy(for:"medium");
            
            switch desiredAccuracy.lowercased() {
            case "high":
                accuracy = RadarTrackingOptions.desiredAccuracy(for:"high")
            case "medium":
                accuracy = RadarTrackingOptions.desiredAccuracy(for:"medium")
            case "low":
                accuracy = RadarTrackingOptions.desiredAccuracy(for:"low")
            default:
                call.reject("invalid desiredAccuracy: " + desiredAccuracy)
                return
            }

            Radar.getLocation(desiredAccuracy: accuracy,  completionHandler: { (status: RadarStatus, location: CLLocation?, stopped: Bool) in
                if status == .success && location != nil {
                    call.resolve([
                        "status": Radar.stringForStatus(status),
                        "location": Radar.dictionaryForLocation(location!),
                        "stopped": stopped
                    ])
                } else {
                    call.reject(Radar.stringForStatus(status))
                }
            })
        }
    }

    @objc func trackOnce(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            let completionHandler: RadarTrackCompletionHandler = { (status: RadarStatus, location: CLLocation?, events: [RadarEvent]?, user: RadarUser?) in
                if status == .success && location != nil && events != nil && user != nil {
                    call.resolve([
                        "status": Radar.stringForStatus(status),
                        "location": Radar.dictionaryForLocation(location!),
                        "events": RadarEvent.array(for: events!) ?? [],
                        "user": user!.dictionaryValue()
                    ])
                } else {
                    call.reject(Radar.stringForStatus(status))
                }
            }

            let latitude = call.getDouble("latitude") ?? 0.0
            let longitude = call.getDouble("longitude") ?? 0.0
            let accuracy = call.getDouble("accuracy") ?? 0.0
            let coordinate = CLLocationCoordinate2DMake(latitude, longitude)
            let location = CLLocation(coordinate: coordinate, altitude: -1, horizontalAccuracy: accuracy, verticalAccuracy: -1, timestamp: Date())
            var accuracyLevel = RadarTrackingOptions.desiredAccuracy(for:"medium")
            let beaconsTrackingOption = call.getBool("beacons") ?? false
            var desiredAccuracy = call.getString("desiredAccuracy") ?? "medium"
            desiredAccuracy = desiredAccuracy.lowercased()

            if desiredAccuracy == "high" {
                accuracyLevel = RadarTrackingOptions.desiredAccuracy(for:"high")
            } else if desiredAccuracy == "medium" {
                accuracyLevel = RadarTrackingOptions.desiredAccuracy(for:"medium")
            } else if desiredAccuracy == "low" {
                accuracyLevel = RadarTrackingOptions.desiredAccuracy(for:"low")
            } else {
                call.reject("invalid desiredAccuracy: " + desiredAccuracy)

                return
            }

            if latitude != 0.0 && longitude != 0.0 && accuracy != 0.0 {
                Radar.trackOnce(location: location, completionHandler: completionHandler)
            } else {
                Radar.trackOnce(desiredAccuracy: accuracyLevel, beacons: beaconsTrackingOption, completionHandler: completionHandler)
            }
        }
    }

    @objc func trackVerified(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            let beacons = call.getBool("beacons") ?? false

            Radar.trackVerified(beacons: beacons) { (status: RadarStatus, location: CLLocation?, events: [RadarEvent]?, user: RadarUser?) in
                if status == .success && location != nil && events != nil && user != nil {
                    call.resolve([
                        "status": Radar.stringForStatus(status),
                        "location": Radar.dictionaryForLocation(location!),
                        "events": RadarEvent.array(for: events!) ?? [],
                        "user": user!.dictionaryValue()
                    ])
                } else {
                    call.reject(Radar.stringForStatus(status))
                }
            }
        }
    }

    @objc func trackVerifiedToken(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            let beacons = call.getBool("beacons") ?? false

            Radar.trackVerifiedToken(beacons: beacons) { (status: RadarStatus, token: String?) in
                if status == .success && token != nil {
                    call.resolve([
                        "status": Radar.stringForStatus(status),
                        "token": token ?? ""
                    ])
                } else {
                    call.reject(Radar.stringForStatus(status))
                }
            }
        }
    }

    @objc func startTrackingVerified(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            let token = call.getBool("token") ?? false
            let interval = call.getDouble("interval") ?? 300.0
            let beacons = call.getBool("beacons") ?? false

            Radar.startTrackingVerified(token: token, interval: interval, beacons: beacons)
        }
    }

    @objc func startTrackingEfficient(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            Radar.startTracking(trackingOptions: RadarTrackingOptions.presetEfficient)
            call.resolve()
        }
    }

    @objc func startTrackingResponsive(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            Radar.startTracking(trackingOptions: RadarTrackingOptions.presetResponsive)
            call.resolve()
        }
    }

    @objc func startTrackingContinuous(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            Radar.startTracking(trackingOptions: RadarTrackingOptions.presetContinuous)
            call.resolve()
        }
    }
    
    @objc func startTrackingCustom(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            let trackingOptionsDict = call.getObject("options") ?? [:]
            guard let trackingOptions = RadarTrackingOptions(from: trackingOptionsDict) else {
                call.reject("options is required")

                return
            }
            Radar.startTracking(trackingOptions: trackingOptions)
            call.resolve()
        }
    }

    @objc func mockTracking(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            guard let originDict = call.options["origin"] as? [String:Double] else {
                call.reject("origin is required")

                return
            }
            let originLatitude = originDict["latitude"] ?? 0.0
            let originLongitude = originDict["longitude"] ?? 0.0
            let origin = CLLocation(coordinate: CLLocationCoordinate2DMake(originLatitude, originLongitude), altitude: -1, horizontalAccuracy: 5, verticalAccuracy: -1, timestamp: Date())

            guard let destinationDict = call.options["destination"] as? [String:Double] else {
                call.reject("destination is required")

                return
            }
            let destinationLatitude = destinationDict["latitude"] ?? 0.0
            let destinationLongitude = destinationDict["longitude"] ?? 0.0
            let destination = CLLocation(coordinate: CLLocationCoordinate2DMake(destinationLatitude, destinationLongitude), altitude: -1, horizontalAccuracy: 5, verticalAccuracy: -1, timestamp: Date())

            guard let modeStr = call.getString("mode") else {
                call.reject("mode is required")

                return
            }
            var mode = RadarRouteMode.car
            if modeStr == "FOOT" || modeStr == "foot" {
                mode = .foot
            } else if modeStr == "BIKE" || modeStr == "bike" {
                mode = .bike
            } else if modeStr == "CAR" || modeStr == "car" {
                mode = .car
            }

            let steps = Int32(call.getInt("steps") ?? 10)
            let interval = TimeInterval(call.getInt("interval") ?? 1)

            Radar.mockTracking(origin: origin, destination: destination, mode: mode, steps: steps, interval: interval, completionHandler: nil)
            
            call.resolve()
        }
    }

    @objc func stopTracking(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            Radar.stopTracking()
            call.resolve()
        }
    }

    @objc func isTracking(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            call.resolve([
                "isTracking": Radar.isTracking()
            ]);
        }
    }

    @objc func getTrackingOptions(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            let options = Radar.getTrackingOptions()
            call.resolve(options.dictionaryValue() as? [String:Any] ?? [:])
            
        }
    }

    @objc func setForegroundServiceOptions(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            // not implemented
            call.resolve()
        }
    }

    @objc func startTrip(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            let optionsDict = call.getObject("options") ?? [:]
            // { tripOptions, trackingOptions } is the new req format.
            // fallback to reading trip options from the top level options.
            let tripOptionsDict = optionsDict["tripOptions"] as? [String:Any] ?? optionsDict
            guard let options = RadarTripOptions(from: tripOptionsDict) else {
                call.reject("tripOptions is required")

                return
            }
            let trackingOptionsDict = optionsDict["trackingOptions"] as? [String:Any]
            var trackingOptions: RadarTrackingOptions?
            if (trackingOptionsDict != nil) {
                trackingOptions = RadarTrackingOptions(from: trackingOptionsDict!)
            }
            Radar.startTrip(options: options, trackingOptions: trackingOptions) { (status: RadarStatus, trip: RadarTrip?, events: [RadarEvent]?) in
                call.resolve([
                    "status": Radar.stringForStatus(status),
                    "trip": trip?.dictionaryValue() ?? {},
                    "events": RadarEvent.array(for: events) ?? []
                ])
            }
        }
    }

    @objc func updateTrip(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            let optionsDict = call.getObject("options") ?? [:]
            guard let options = RadarTripOptions(from: optionsDict) else {
                call.reject("options is required")

                return
            }
            let statusStr = call.getString("status")
            var status = RadarTripStatus.unknown
            if statusStr == "STARTED" || statusStr == "started" {
                status = .started
            } else if statusStr == "APPROACHING" || statusStr == "approaching" {
                status = .approaching
            } else if statusStr == "ARRIVED" || statusStr == "arrived" {
                status = .arrived
            } else if statusStr == "COMPLETED" || statusStr == "completed" {
                status = .completed
            } else if statusStr == "CANCELED" || statusStr == "canceled" {
                status = .canceled
            }

            Radar.updateTrip(options: options, status: status) { (status: RadarStatus, trip: RadarTrip?, events: [RadarEvent]?) in
                call.resolve([
                    "status": Radar.stringForStatus(status),
                    "trip": trip?.dictionaryValue() ?? {},
                    "events": RadarEvent.array(for: events) ?? []
                ])
            }
        }
    }

    @objc func completeTrip(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            Radar.completeTrip() { (status: RadarStatus, trip: RadarTrip?, events: [RadarEvent]?) in
                call.resolve([
                    "status": Radar.stringForStatus(status),
                    "trip": trip?.dictionaryValue() ?? {},
                    "events": RadarEvent.array(for: events) ?? []
                ])
            }
        }
    }

    @objc func cancelTrip(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            Radar.cancelTrip() { (status: RadarStatus, trip: RadarTrip?, events: [RadarEvent]?) in
                call.resolve([
                    "status": Radar.stringForStatus(status),
                    "trip": trip?.dictionaryValue() ?? {},
                    "events": RadarEvent.array(for: events) ?? []
                ])
            }
        }
    }

    @objc func acceptEvent(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            guard let eventId = call.getString("eventId") else {
                call.reject("eventId is required")

                return
            }
            let verifiedPlaceId = call.getString("verifiedPlaceId") ?? nil
            Radar.acceptEventId(eventId, verifiedPlaceId: verifiedPlaceId)
            call.resolve()
        }
    }

    @objc func rejectEvent(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            guard let eventId = call.getString("eventId") else {
                call.reject("eventId is required")

                return
            }
            Radar.rejectEventId(eventId)
            call.resolve()
        }
    }

     @objc func getTripOptions(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            let options = Radar.getTripOptions()
            call.resolve(options?.dictionaryValue() as? [String:Any] ?? [:])
        }
    }

    @objc func getContext(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            let completionHandler: RadarContextCompletionHandler = { (status: RadarStatus, location: CLLocation?, context: RadarContext?) in
                if status == .success && location != nil && context != nil {
                    call.resolve([
                        "status": Radar.stringForStatus(status),
                        "location": Radar.dictionaryForLocation(location!),
                        "context": context!.dictionaryValue()
                    ])
                } else {
                    call.reject(Radar.stringForStatus(status))
                }
            }

            let latitude = call.getDouble("latitude") ?? 0.0
            let longitude = call.getDouble("longitude") ?? 0.0
            
            if latitude != 0.0 && longitude != 0.0 {
                let coordinate = CLLocationCoordinate2DMake(latitude, longitude)
                let location = CLLocation(coordinate: coordinate, altitude: -1, horizontalAccuracy: 5, verticalAccuracy: -1, timestamp: Date())
                
                Radar.getContext(location: location, completionHandler: completionHandler)
            } else {
                Radar.getContext(completionHandler: completionHandler)
            }
        }
    }

    @objc func searchPlaces(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            let completionHandler: RadarSearchPlacesCompletionHandler = { (status: RadarStatus, location: CLLocation?, places: [RadarPlace]?) in
                if status == .success && location != nil && places != nil {
                    call.resolve([
                        "status": Radar.stringForStatus(status),
                        "location": Radar.dictionaryForLocation(location!),
                        "places": RadarPlace.array(for: places!) ?? []
                    ])
                } else {
                    call.reject(Radar.stringForStatus(status))
                }
            }

            let radius = Int32(call.getInt("radius") ?? 1000)
            let chains = call.getArray("chains", String.self)
            let chainMetadata = call.options["chainMetadata"] as? [String:String] ?? nil
            let categories = call.getArray("categories", String.self)
            let groups = call.getArray("groups", String.self)
            let limit = Int32(call.getInt("limit") ?? 10)

            let nearDict = call.options["near"] as? [String:Double] ?? nil
            
            if nearDict != nil {
                let latitude = nearDict?["latitude"] ?? 0.0
                let longitude = nearDict?["longitude"] ?? 0.0
                let near = CLLocation(coordinate: CLLocationCoordinate2DMake(latitude, longitude), altitude: -1, horizontalAccuracy: 5, verticalAccuracy: -1, timestamp: Date())
                
                Radar.searchPlaces(near: near, radius: radius, chains: chains, chainMetadata: chainMetadata, categories: categories, groups: groups, limit: limit, completionHandler: completionHandler)
            } else {
                Radar.searchPlaces(radius: radius, chains: chains, chainMetadata: chainMetadata, categories: categories, groups: groups, limit: limit, completionHandler: completionHandler)
            }
        }
    }

    @objc func searchGeofences(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            let completionHandler: RadarSearchGeofencesCompletionHandler = { (status: RadarStatus, location: CLLocation?, geofences: [RadarGeofence]?) in
                if status == .success && location != nil && geofences != nil {
                    call.resolve([
                        "status": Radar.stringForStatus(status),
                        "location": Radar.dictionaryForLocation(location!),
                        "geofences": RadarGeofence.array(for: geofences!) ?? []
                    ])
                } else {
                    call.reject(Radar.stringForStatus(status))
                }
            }

            let radius = Int32(call.getInt("radius") ?? 1000)
            let tags = call.getArray("tags", String.self)
            let metadata = call.getObject("metadata") ?? nil
            let limit = Int32(call.getInt("limit") ?? 10)

            let nearDict = call.options["near"] as? [String:Double] ?? nil
            if nearDict != nil {
                let latitude = nearDict?["latitude"] ?? 0.0
                let longitude = nearDict?["longitude"] ?? 0.0
                let near = CLLocation(coordinate: CLLocationCoordinate2DMake(latitude, longitude), altitude: -1, horizontalAccuracy: 5, verticalAccuracy: -1, timestamp: Date())

                Radar.searchGeofences(near: near, radius: radius, tags: tags, metadata: metadata, limit: limit, completionHandler: completionHandler)
            } else {
                Radar.searchGeofences(radius: radius, tags: tags, metadata: metadata, limit: limit, completionHandler: completionHandler)
            }
        }
    }

    @objc func autocomplete(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            guard let query = call.getString("query") else {
                call.reject("query is required")

                return
            }

            guard let nearDict = call.options["near"] as? [String:Double] else {
                call.reject("near is required")

                return
            }
            let latitude = nearDict["latitude"] ?? 0.0
            let longitude = nearDict["longitude"] ?? 0.0
            let near = CLLocation(coordinate: CLLocationCoordinate2DMake(latitude, longitude), altitude: -1, horizontalAccuracy: 5, verticalAccuracy: -1, timestamp: Date())

            let limit = Int32(call.getInt("limit") ?? 10)
            let country = call.getString("country")
            let layers = call.getArray("layers", String.self)
            let mailable = call.getBool("mailable") ?? false

            Radar.autocomplete(query: query, near: near, layers: layers, limit: limit, country: country, mailable: mailable) { (status: RadarStatus, addresses: [RadarAddress]?) in
                if status == .success && addresses != nil {
                    call.resolve([
                        "status": Radar.stringForStatus(status),
                        "addresses": RadarAddress.array(forAddresses: addresses!) ?? []
                    ])
                } else {
                    call.reject(Radar.stringForStatus(status))
                }
            }
        }
    }

    @objc func validateAddress(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            guard let address = RadarAddress.init(object: call.getObject("address")) else {
                call.reject("address is required")

                return
            }

            Radar.validateAddress(address: address) { (status: RadarStatus, address: RadarAddress?, verificationStatus: RadarAddressVerificationStatus) in
                if status == .success && address != nil {

                    call.resolve([
                        "status": Radar.stringForStatus(status),
                        "address": address?.dictionaryValue() ?? [:],
                        "verificationStatus": Radar.stringForVerificationStatus(verificationStatus)
                    ])
                } else {
                    call.reject(Radar.stringForStatus(status))
                }
            }
        }
    }

    @objc func geocode(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            guard let query = call.getString("query") else {
                call.reject("query is required")

                return
            }

            Radar.geocode(address: query) { (status: RadarStatus, addresses: [RadarAddress]?) in
                if status == .success && addresses != nil {
                    call.resolve([
                        "status": Radar.stringForStatus(status),
                        "addresses": RadarAddress.array(forAddresses: addresses!) ?? []
                    ])
                } else {
                    call.reject(Radar.stringForStatus(status))
                }
            }
        }
    }

    @objc func reverseGeocode(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            let completionHandler: RadarGeocodeCompletionHandler = { (status: RadarStatus, addresses: [RadarAddress]?) in
                if status == .success && addresses != nil {
                    call.resolve([
                        "status": Radar.stringForStatus(status),
                        "addresses": RadarAddress.array(forAddresses: addresses!) ?? []
                    ])
                } else {
                    call.reject(Radar.stringForStatus(status))
                }
            }

            let latitude = call.getDouble("latitude") ?? 0.0
            let longitude = call.getDouble("longitude") ?? 0.0
            
            if latitude != 0.0 && longitude != 0.0 {
                let coordinate = CLLocationCoordinate2DMake(latitude, longitude)
                let location = CLLocation(coordinate: coordinate, altitude: -1, horizontalAccuracy: 5, verticalAccuracy: -1, timestamp: Date())

                Radar.reverseGeocode(location: location, completionHandler: completionHandler)
            } else {
                Radar.reverseGeocode(completionHandler: completionHandler)
            }
        }
    }

    @objc func ipGeocode(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            Radar.ipGeocode { (status: RadarStatus, address: RadarAddress?, proxy: Bool) in
                if status == .success && address != nil {
                    call.resolve([
                        "status": Radar.stringForStatus(status),
                        "address": address!.dictionaryValue()
                    ])
                } else {
                    call.reject(Radar.stringForStatus(status))
                }
            }
        }
    }

    @objc func getDistance(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            let completionHandler: RadarRouteCompletionHandler = { (status: RadarStatus, routes: RadarRoutes?) in
                if status == .success && routes != nil {
                    call.resolve([
                        "status": Radar.stringForStatus(status),
                        "routes": routes!.dictionaryValue()
                    ])
                } else {
                    call.reject(Radar.stringForStatus(status))
                }
            }

            guard let destinationDict = call.options["destination"] as? [String:Double] else {
                call.reject("destination is required")

                return
            }
            let destinationLatitude = destinationDict["latitude"] ?? 0.0
            let destinationLongitude = destinationDict["longitude"] ?? 0.0
            let destination = CLLocation(coordinate: CLLocationCoordinate2DMake(destinationLatitude, destinationLongitude), altitude: -1, horizontalAccuracy: 5, verticalAccuracy: -1, timestamp: Date())

            guard let modesArr = call.getArray("modes", String.self) else {
                call.reject("modes is required")

                return
            }
            var modes: RadarRouteMode = []
            if modesArr.contains("FOOT") || modesArr.contains("foot") {
                modes.insert(.foot)
            }
            if modesArr.contains("BIKE") || modesArr.contains("bike") {
                modes.insert(.bike)
            }
            if modesArr.contains("CAR") || modesArr.contains("car") {
                modes.insert(.car)
            }

            guard let unitsStr = call.getString("units") else {
                call.reject("units is required")

                return
            }
            let units: RadarRouteUnits = unitsStr == "METRIC" || unitsStr == "metric" ? .metric : .imperial;

            if let originDict = call.options["origin"] as? [String: Double] {
                let originLatitude = originDict["latitude"] ?? 0.0
                let originLongitude = originDict["longitude"] ?? 0.0
                let origin = CLLocation(coordinate: CLLocationCoordinate2DMake(originLatitude, originLongitude), altitude: -1, horizontalAccuracy: 5, verticalAccuracy: -1, timestamp: Date())

                Radar.getDistance(origin: origin, destination: destination, modes: modes, units: units, completionHandler: completionHandler)
            } else {
                Radar.getDistance(destination: destination, modes: modes, units: units, completionHandler: completionHandler)
            }
        }
    }

    @objc func getMatrix(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            let completionHandler: RadarRouteMatrixCompletionHandler  = { (status: RadarStatus, matrix: RadarRouteMatrix?) in
                if status == .success && matrix != nil {
                    call.resolve([
                        "status": Radar.stringForStatus(status),
                        "matrix": matrix!.arrayValue()
                    ])
                } else {
                    call.reject(Radar.stringForStatus(status))
                }
            }
            let originsArr = call.getArray("origins", JSObject.self) ?? []
            let origins: [CLLocation] = originsArr.map{ (originDict) -> CLLocation in
                let originLatitude = originDict["latitude"] as? Double ?? 0.0
                let originLongitude = originDict["longitude"] as? Double ?? 0.0
                let origin = CLLocation(coordinate: CLLocationCoordinate2DMake(originLatitude, originLongitude), altitude: -1, horizontalAccuracy: 5, verticalAccuracy: -1, timestamp: Date())
                return origin
            }

            let destinationsArr = call.getArray("destinations", JSObject.self) ?? []
            let destinations: [CLLocation] = destinationsArr.map{ (destinationDict) -> CLLocation in
                let destinationLatitude = destinationDict["latitude"] as? Double ?? 0.0
                let destinationLongitude = destinationDict["longitude"] as? Double ?? 0.0
                let destination = CLLocation(coordinate: CLLocationCoordinate2DMake(destinationLatitude, destinationLongitude), altitude: -1, horizontalAccuracy: 5, verticalAccuracy: -1, timestamp: Date())

                return destination
            }

            guard let modeStr = call.getString("mode") else {
                call.reject("mode is required")

                return
            }
            var mode: RadarRouteMode = .car
            switch modeStr.lowercased() {
            case "foot":
                mode = .foot
            case "bike":
                mode = .bike
            case "car":
                mode = .car
            case "truck":
                mode = .truck
            case "motorbike":
                mode = .motorbike
            default:                
                call.reject("invalid mode: " + modeStr)
                return
            }

            guard let unitsStr = call.getString("units") else {
                call.reject("units is required")

                return
            }
            let units: RadarRouteUnits = unitsStr == "METRIC" || unitsStr == "metric" ? .metric : .imperial;

            Radar.getMatrix(origins: origins, destinations: destinations, mode: mode, units: units, completionHandler: completionHandler)            
        }
    }

    @objc func logConversion(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            let completionHandler: RadarLogConversionCompletionHandler  = { (status: RadarStatus, event: RadarEvent? ) in
                if status == .success { 
                    call.resolve([
                        "status": Radar.stringForStatus(status),
                        "event": event?.dictionaryValue() ?? [:]
                    ])
                } else {
                    call.reject(Radar.stringForStatus(status))
                }
            } 
            
            guard let name = call.getString("name") else {
                call.reject("name is required")

                return
            }
            let revenue = call.getString("revenue") != nil ? NSNumber(value: Double(call.getString("revenue")!)!) : nil
            let metadata = call.getObject("metadata")
            
            if revenue != nil {
                Radar.logConversion(name: name, revenue: revenue!, metadata: metadata, completionHandler: completionHandler)
            } else {
                Radar.logConversion(name: name, metadata: metadata, completionHandler: completionHandler)
            }
        }
    }

    @objc func logTermination(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            Radar.logTermination()
            call.resolve()
        }
    }

    @objc func logBackgrounding(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            Radar.logBackgrounding()
            call.resolve()
        }
    }

    @objc func logResigningActive(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            Radar.logResigningActive()
            call.resolve()
        }
    }

    @objc func setNotificationOptions(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            // Not implemented
            call.resolve()
        }
    }

    @objc func isUsingRemoteTrackingOptions(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            call.resolve([
                "isUsingRemoteTrackingOptions": Radar.isUsingRemoteTrackingOptions()
            ]);
        }
    }

    @objc func getHost(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            call.resolve([
                "host": RadarSettings.host()
            ]);
        }
    }

    @objc func getPublishableKey(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            call.resolve([
                "publishableKey": RadarSettings.publishableKey()
            ]);
        }
    }


}
