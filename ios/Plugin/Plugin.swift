import Foundation
import CoreLocation
import Capacitor

@objc(RadarPlugin)
public class RadarPlugin: CAPPlugin {

    let locationManager = CLLocationManager()

    @objc func initialize(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            guard let publishableKey = call.getString("publishableKey") else {
                call.reject("publishableKey is required")
                return
            }
            Radar.initialize(publishableKey: publishableKey)
            call.success()
        }
    }

    @objc func setUserId(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            let userId = call.getString("userId")
            Radar.setUserId(userId)
            call.success()
        }
    }

    @objc func setDescription(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            let description = call.getString("description")
            Radar.setDescription(description)
            call.success()
        }
    }

    @objc func setMetadata(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            let metadata = call.getObject("metadata")
            Radar.setMetadata(metadata)
            call.success()
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
            call.success([
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
            call.success()
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
            
            let latitude = call.getDouble("latitude") ?? nil
            let longitude = call.getDouble("latitude") ?? nil
            let accuracy = call.getDouble("accuracy") ?? nil
            
            if latitude != nil && longitude != nil && accuracy != nil {
                let coordinate = CLLocationCoordinate2DMake(latitude!, longitude!)
                let location = CLLocation(coordinate: coordinate, altitude: -1, horizontalAccuracy: accuracy!, verticalAccuracy: -1, timestamp: Date())
                Radar.trackOnce(location: location, completionHandler: completionHandler)
            } else {
                Radar.trackOnce(completionHandler: completionHandler)
            }
        }
    }

    @objc func startTrackingEfficient(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            Radar.startTracking(trackingOptions: RadarTrackingOptions.efficient)
            call.success()
        }
    }
    
    @objc func startTrackingResponsive(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            Radar.startTracking(trackingOptions: RadarTrackingOptions.responsive)
            call.success()
        }
    }
    
    @objc func startTrackingContinuous(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            Radar.startTracking(trackingOptions: RadarTrackingOptions.continuous)
            call.success()
        }
    }
    
    @objc func startTrackingCustom(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            Radar.startTracking(trackingOptions: RadarTrackingOptions.efficient)
            call.success()
        }
    }

    @objc func stopTracking(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            Radar.stopTracking()
            call.success()
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
            call.success()
        }
    }

    @objc func rejectEvent(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            guard let eventId = call.getString("eventId") else {
                call.reject("eventId is required")
                return
            }
            Radar.rejectEventId(eventId)
            call.success()
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
            
            let latitude = call.getDouble("latitude") ?? nil
            let longitude = call.getDouble("latitude") ?? nil
            
            if latitude != nil && longitude != nil {
                let coordinate = CLLocationCoordinate2DMake(latitude!, longitude!)
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
            
            let near = call.get("near", [String:Double].self, [:])
            let latitude = near?["latitude"]
            let longitude = near?["longitude"]
            let radius = Int32(call.getInt("radius") ?? 1000)
            let chains = call.getArray("chains", String.self)
            let categories = call.getArray("categories", String.self)
            let groups = call.getArray("groups", String.self)
            let limit = Int32(call.getInt("limit") ?? 10)
            
            if latitude != nil && longitude != nil {
                let coordinate = CLLocationCoordinate2DMake(latitude!, longitude!)
                let location = CLLocation(coordinate: coordinate, altitude: -1, horizontalAccuracy: 5, verticalAccuracy: -1, timestamp: Date())
                Radar.searchPlaces(near: location, radius: radius, chains: chains, categories: categories, groups: groups, limit: limit, completionHandler: completionHandler)
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
            
            let near = call.get("near", [String:Double].self, [:])
            let latitude = near?["latitude"]
            let longitude = near?["longitude"]
            let radius = Int32(call.getInt("radius") ?? 1000)
            let tags = call.getArray("tags", String.self)
            let limit = Int32(call.getInt("limit") ?? 10)
            
            if latitude != nil && longitude != nil {
                let coordinate = CLLocationCoordinate2DMake(latitude!, longitude!)
                let location = CLLocation(coordinate: coordinate, altitude: -1, horizontalAccuracy: 5, verticalAccuracy: -1, timestamp: Date())
                Radar.searchGeofences(near: location, radius: radius, tags: tags, limit: limit, completionHandler: completionHandler)
            } else {
                Radar.searchGeofences(radius: radius, tags: tags, limit: limit, completionHandler: completionHandler)
            }
        }
    }
    
    @objc func searchPoints(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            let completionHandler: RadarSearchPointsCompletionHandler = { (status: RadarStatus, location: CLLocation?, points: [RadarPoint]?) in
                if status == .success && location != nil && points != nil {
                    call.resolve([
                        "status": Radar.stringForStatus(status),
                        "location": Radar.dictionaryForLocation(location!),
                        "points": RadarPoint.array(for: points!) ?? []
                    ])
                } else {
                    call.reject(Radar.stringForStatus(status))
                }
            }
            
            let near = call.get("near", [String:Double].self, [:])
            let latitude = near?["latitude"]
            let longitude = near?["longitude"]
            let radius = Int32(call.getInt("radius") ?? 1000)
            let tags = call.getArray("tags", String.self)
            let limit = Int32(call.getInt("limit") ?? 10)
            
            if latitude != nil && longitude != nil {
                let coordinate = CLLocationCoordinate2DMake(latitude!, longitude!)
                let location = CLLocation(coordinate: coordinate, altitude: -1, horizontalAccuracy: 5, verticalAccuracy: -1, timestamp: Date())
                Radar.searchPoints(near: location, radius: radius, tags: tags, limit: limit, completionHandler: completionHandler)
            } else {
                Radar.searchPoints(radius: radius, tags: tags, limit: limit, completionHandler: completionHandler)
            }
        }
    }
    
    @objc func autocomplete(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            guard let query = call.getString("query") else {
                call.reject("query is required")
                return
            }
            guard let near = call.get("near", [String:Double].self) else {
                call.reject("near is required")
                return
            }
            guard let latitude = near["latitude"] else {
                call.reject("near.latitude is required")
                return
            }
            guard let longitude = near["longitude"] else {
                call.reject("near.longitude is required")
                return
            }
            let limit = Int32(call.getInt("limit") ?? 10)
            
            let coordinate = CLLocationCoordinate2DMake(latitude, longitude)
            let location = CLLocation(coordinate: coordinate, altitude: -1, horizontalAccuracy: 5, verticalAccuracy: -1, timestamp: Date())
            Radar.autocomplete(query: query, near: location, limit: limit) { (status: RadarStatus, addresses: [RadarAddress]?) in
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
            
            let latitude = call.getDouble("latitude") ?? nil
            let longitude = call.getDouble("latitude") ?? nil
            
            if latitude != nil && longitude != nil {
                let coordinate = CLLocationCoordinate2DMake(latitude!, longitude!)
                let location = CLLocation(coordinate: coordinate, altitude: -1, horizontalAccuracy: 5, verticalAccuracy: -1, timestamp: Date())
                Radar.reverseGeocode(location: location, completionHandler: completionHandler)
            } else {
                Radar.reverseGeocode(completionHandler: completionHandler)
            }
        }
    }
    
    @objc func ipGeocode(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            Radar.ipGeocode { (status: RadarStatus, address: RadarAddress?) in
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
            
            let origin = call.get("origin", [String:Double].self, [:])
            let originLatitude = origin?["latitude"]
            let originLongitude = origin?["longitude"]
            guard let destination = call.get("destination", [String:Double].self) else {
                call.reject("destination is required")
                return
            }
            guard let destinationLatitude = destination["latitude"] else {
                call.reject("destination.latitude is required")
                return
            }
            guard let destinationLongitude = destination["longitude"] else {
                call.reject("destination.longitude is required")
                return
            }
            let destinationCoordinate = CLLocationCoordinate2DMake(destinationLatitude, destinationLongitude)
            let destinationLocation = CLLocation(coordinate: destinationCoordinate, altitude: -1, horizontalAccuracy: 5, verticalAccuracy: -1, timestamp: Date())
            let modesArr = call.getArray("modes", String.self) ?? []
            var modes: RadarRouteMode = []
            if modesArr.contains("FOOT") {
                modes.insert(.foot)
            }
            if modesArr.contains("BIKE") {
                modes.insert(.bike)
            }
            if modesArr.contains("CAR") {
                modes.insert(.car)
            }
            let units: RadarRouteUnits = call.getString("units") == "METRIC" ? .metric : .imperial;
            
            if originLatitude != nil && originLatitude != nil {
                let originCoordinate = CLLocationCoordinate2DMake(originLatitude!, originLongitude!)
                let originLocation = CLLocation(coordinate: originCoordinate, altitude: -1, horizontalAccuracy: 5, verticalAccuracy: -1, timestamp: Date())
                Radar.getDistance(origin: originLocation, destination: destinationLocation, modes: modes, units: units, completionHandler: completionHandler)
            } else {
                Radar.getDistance(destination: destinationLocation, modes: modes, units: units, completionHandler: completionHandler)
            }
        }
    }

}
