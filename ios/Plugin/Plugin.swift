import Foundation
import CoreLocation
import Capacitor
import RadarSDK

@objc(RadarPlugin)
public class RadarPlugin: CAPPlugin, RadarDelegate {
    
    let locationManager = CLLocationManager()

    override public func load() {
        Radar.setDelegate(self)
    }

    public func didReceiveEvents(_ events: [RadarEvent], user: RadarUser) {
        DispatchQueue.main.async {
            self.notifyListeners("events", data: [
                "events": RadarEvent.array(for: events) ?? [],
                "user": user.dictionaryValue()
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
                "source": Radar.stringForSource(source)
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

    @objc func initialize(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            guard let publishableKey = call.getString("publishableKey") else {
                call.reject("publishableKey is required")

                return
            }
            Radar.initialize(publishableKey: publishableKey)
            call.resolve()
        }
    }

    @objc func setUserId(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            let userId = call.getString("userId")
            Radar.setUserId(userId)
            call.resolve()
        }
    }

    @objc func setDescription(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            let description = call.getString("description")
            Radar.setDescription(description)
            call.resolve()
        }
    }

    @objc func setMetadata(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            let metadata = call.getObject("metadata")
            Radar.setMetadata(metadata)
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
            Radar.getLocation { (status: RadarStatus, location: CLLocation?, stopped: Bool) in
                if status == .success && location != nil {
                    call.resolve([
                        "status": Radar.stringForStatus(status),
                        "location": Radar.dictionaryForLocation(location!),
                        "stopped": stopped
                    ])
                } else {
                    call.reject(Radar.stringForStatus(status))
                }
            }
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
            let longitude = call.getDouble("latitude") ?? 0.0
            let accuracy = call.getDouble("accuracy") ?? 0.0
            let coordinate = CLLocationCoordinate2DMake(latitude, longitude)
            let location = CLLocation(coordinate: coordinate, altitude: -1, horizontalAccuracy: accuracy, verticalAccuracy: -1, timestamp: Date())

            if latitude != 0.0 && longitude != 0.0 && accuracy != 0.0 {
                Radar.trackOnce(location: location, completionHandler: completionHandler)
            } else {
                Radar.trackOnce(completionHandler: completionHandler)
            }
        }
    }

    @objc func startTrackingEfficient(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            Radar.startTracking(trackingOptions: RadarTrackingOptions.efficient)
            call.resolve()
        }
    }

    @objc func startTrackingResponsive(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            Radar.startTracking(trackingOptions: RadarTrackingOptions.responsive)
            call.resolve()
        }
    }

    @objc func startTrackingContinuous(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            Radar.startTracking(trackingOptions: RadarTrackingOptions.continuous)
            call.resolve()
        }
    }
    
    @objc func startTrackingCustom(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            let trackingOptionsDict = call.getObject("options") ?? [:]
            let trackingOptions = RadarTrackingOptions(from: trackingOptionsDict)
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

    @objc func startTrip(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            let optionsDict = call.getObject("options") ?? [:]
            let options = RadarTripOptions(from: optionsDict)
            Radar.startTrip(options: options) { (status: RadarStatus) in
                call.resolve([
                    "status": Radar.stringForStatus(status)
                ])
            }
        }
    }

    @objc func completeTrip(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            Radar.completeTrip() { (status: RadarStatus) in
                call.resolve([
                    "status": Radar.stringForStatus(status)
                ])
            }
        }
    }

    @objc func cancelTrip(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            Radar.cancelTrip() { (status: RadarStatus) in
                call.resolve([
                    "status": Radar.stringForStatus(status)
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
            let longitude = call.getDouble("latitude") ?? 0.0
            
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
            let categories = call.getArray("categories", String.self)
            let groups = call.getArray("groups", String.self)
            let limit = Int32(call.getInt("limit") ?? 10)

            let nearDict = call.options["near"] as? [String:Double] ?? nil
            
            if nearDict != nil {
                let latitude = nearDict?["latitude"] ?? 0.0
                let longitude = nearDict?["longitude"] ?? 0.0
                let near = CLLocation(coordinate: CLLocationCoordinate2DMake(latitude, longitude), altitude: -1, horizontalAccuracy: 5, verticalAccuracy: -1, timestamp: Date())
                
                Radar.searchPlaces(near: near, radius: radius, chains: chains, categories: categories, groups: groups, limit: limit, completionHandler: completionHandler)
            } else {
                Radar.searchPlaces(radius: radius, chains: chains, categories: categories, groups: groups, limit: limit, completionHandler: completionHandler)
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
            let limit = Int32(call.getInt("limit") ?? 10)

            let nearDict = call.options["near"] as? [String:Double] ?? nil
            if nearDict != nil {
                let latitude = nearDict?["latitude"] ?? 0.0
                let longitude = nearDict?["longitude"] ?? 0.0
                let near = CLLocation(coordinate: CLLocationCoordinate2DMake(latitude, longitude), altitude: -1, horizontalAccuracy: 5, verticalAccuracy: -1, timestamp: Date())

                Radar.searchGeofences(near: near, radius: radius, tags: tags, metadata: nil, limit: limit, completionHandler: completionHandler)
            } else {
                Radar.searchGeofences(radius: radius, tags: tags, metadata: nil, limit: limit, completionHandler: completionHandler)
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

            Radar.autocomplete(query: query, near: near, limit: limit) { (status: RadarStatus, addresses: [RadarAddress]?) in
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
            let longitude = call.getDouble("latitude") ?? 0.0
            
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

            if call.hasOption("origin") {
                let originDict = call.options["origin"] as! [String:Double]
                let originLatitude = originDict["latitude"] ?? 0.0
                let originLongitude = originDict["longitude"] ?? 0.0
                let origin = CLLocation(coordinate: CLLocationCoordinate2DMake(originLatitude, originLongitude), altitude: -1, horizontalAccuracy: 5, verticalAccuracy: -1, timestamp: Date())

                Radar.getDistance(origin: origin, destination: destination, modes: modes, units: units, completionHandler: completionHandler)
            } else {
                Radar.getDistance(destination: destination, modes: modes, units: units, completionHandler: completionHandler)
            }
        }
    }

    @objc func setLogLevel(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            guard let levelStr = call.getString("level") else {
                call.reject("level is required")
                return
            }
            var level = RadarLogLevel.RadarLogLevelInfo
            if levelStr == "NONE" || modeStr == "none" {
                level = RadarLogLevel.RadarLogLevelNone
            } else if levelStr == "ERROR" || modeStr == "error" {
                level = RadarLogLevel.RadarLogLevelError
            } else if levelStr == "WARNING" || modeStr == "warning" {
                level = RadarLogLevel.RadarLogLevelWarning
            } else if levelStr == "INFO" || modeStr == "info" {
                level = RadarLogLevel.RadarLogLevelInfo
            } else if levelStr == "DEBUG" || modeStr == "debug" {
                level = RadarLogLevel.RadarLogLevelDebug
            }
            Radar.setLogLevel(level: level)
            call.resolve()
        }
    }

}
