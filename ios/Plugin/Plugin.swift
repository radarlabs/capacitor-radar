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

    @objc func setPlacesProvider(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            let placesProviderStr = call.getString("placesProvider")
            let placesProvider = RadarPlugin.placesProviderForString(placesProviderStr)
            Radar.setPlacesProvider(placesProvider)
            call.success()
        }
    }

    @objc func getLocationPermissionsStatus(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            let authorizationStatus = CLLocationManager.authorizationStatus()
            let authorizationStatusStr = RadarPlugin.stringForAuthorizationStatus(authorizationStatus)
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
        }
    }

    @objc func startTracking(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            let optionsDict = call.getObject("options") ?? [:]
            let options = RadarPlugin.optionsForDictionary(optionsDict)
            Radar.startTracking(trackingOptions: options)
        }
    }

    @objc func stopTracking(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            Radar.stopTracking()
        }
    }

    @objc func trackOnce(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            Radar.trackOnce(completionHandler: { (status: RadarStatus, location: CLLocation?, events: [RadarEvent]?, user: RadarUser?) in
                if status == .success {
                    call.resolve([
                        "status": RadarPlugin.stringForStatus(status),
                        "location": RadarPlugin.dictionaryForLocation(location),
                        "events": RadarPlugin.arrayForEvents(events),
                        "user": RadarPlugin.dictionaryForUser(user)
                    ])
                } else {
                    call.reject(RadarPlugin.stringForStatus(status))
                }
            })
        }
    }

    @objc func updateLocation(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            guard let latitude = call.getDouble("latitude") else {
                call.reject("latitude is required")
                return
            }
            guard let longitude = call.getDouble("longitude") else {
                call.reject("longitude is required")
                return
            }
            guard let accuracy = call.getDouble("accuracy") else {
                call.reject("accuracy is required")
                return
            }
            let coordinate = CLLocationCoordinate2DMake(latitude, longitude)
            let location = CLLocation(coordinate: coordinate, altitude: -1, horizontalAccuracy: accuracy, verticalAccuracy: -1, timestamp: Date())
            Radar.updateLocation(location, completionHandler: { (status: RadarStatus, location: CLLocation?, events: [RadarEvent]?, user: RadarUser?) in
                if status == .success {
                    call.resolve([
                        "status": RadarPlugin.stringForStatus(status),
                        "location": RadarPlugin.dictionaryForLocation(location),
                        "events": RadarPlugin.arrayForEvents(events),
                        "user": RadarPlugin.dictionaryForUser(user)
                    ])
                } else {
                    call.reject(RadarPlugin.stringForStatus(status))
                }
            })
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

    static func stringForAuthorizationStatus(_ status: CLAuthorizationStatus?) -> String {
        guard let status = status else {
            return "UNKNOWN"
        }
        switch status {
        case .denied:
            return "DENIED"
        case .restricted:
            return "DENIED"
        case .authorizedAlways:
            return "GRANTED"
        case .authorizedWhenInUse:
            return "GRANTED"
        default:
            return "UNKNOWN"
        }
    }

    static func stringForStatus(_ status: RadarStatus?) -> String {
        guard let status = status else {
            return "ERROR_UNKNOWN"
        }
        switch status {
        case .success:
            return "SUCCESS"
        case .errorPublishableKey:
            return "ERROR_PUBLISHABLE_KEY"
        case .errorPermissions:
            return "ERROR_PERMISSIONS"
        case .errorLocation:
            return "ERROR_LOCATION"
        case .errorNetwork:
            return "ERROR_NETWORK"
        case .errorUnauthorized:
            return "ERROR_UNAUTHORIZED"
        case .errorRateLimit:
            return "ERROR_RATE_LIMIT"
        case .errorServer:
            return "ERROR_SERVER"
        default:
            return "ERROR_UNKNOWN"
        }
    }

    static func stringForEventType(_ type: RadarEventType?) -> String? {
        guard let type = type else {
            return nil
        }
        switch type {
        case .userEnteredGeofence:
            return "user.entered_geofence"
        case .userExitedGeofence:
            return "user.exited_geofence"
        case .userEnteredHome:
            return "user.entered_home"
        case .userExitedHome:
            return "user.exited_home"
        case .userEnteredOffice:
            return "user.entered_office"
        case .userExitedOffice:
            return "user.exited_office"
        case .userStartedTraveling:
            return "user.started_traveling"
        case .userStoppedTraveling:
            return "user.stopped_traveling"
        case .userEnteredPlace:
            return "user.entered_place"
        case .userExitedPlace:
            return "user.exited_place"
        case .userNearbyPlaceChain:
            return "user.nearby_place_chain"
        case .userEnteredRegionCountry:
            return "user.entered_region_country"
        case .userExitedRegionCountry:
            return "user.exited_region_country"
        case .userEnteredRegionState:
            return "user.entered_region_state"
        case .userExitedRegionState:
            return "user.exited_region_state"
        case .userEnteredRegionDMA:
            return "user.entered_region_dma"
        case .userExitedRegionDMA:
            return "user.exited_region_dma"
        default:
            return nil
        }
    }

    static func numberForEventConfidence(_ confidence: RadarEventConfidence?) -> Int {
        guard let confidence = confidence else {
            return 0
        }
        switch confidence {
        case .high:
            return 3
        case .medium:
            return 2
        case .low:
            return 1
        default:
            return 0
        }
    }

    static func stringForUserInsightsLocationType(_ type: RadarUserInsightsLocationType?) -> String? {
        guard let type = type else {
            return nil
        }
        switch type {
        case .home:
            return "home"
        case .office:
            return "office"
        default:
            return nil
        }
    }

    static func numberForUserInsightsLocationConfidence(_ confidence: RadarUserInsightsLocationConfidence?) -> Int {
        guard let confidence = confidence else {
            return 0
        }
        switch confidence {
        case .high:
            return 3
        case .medium:
            return 2
        case .low:
            return 1
        default:
            return 0
        }
    }

    static func placesProviderForString(_ str: String?) -> RadarPlacesProvider {
        guard let str = str else {
            return .none
        }
        switch str {
        case "facebook":
            return .facebook
        default:
            return .none
        }
    }

    static func dictionaryForUser(_ user: RadarUser?) -> [String: Any?] {
        var dict = [String: Any?]()
        dict["_id"] = user?._id
        if let userId = user?.userId {
            dict["userId"] = userId
        }
        if let deviceId = user?.deviceId {
            dict["deviceId"] = deviceId
        }
        if let description = user?._description {
            dict["description"] = description
        }
        if let geofences = user?.geofences {
            dict["geofences"] = RadarPlugin.arrayForGeofences(geofences)
        }
        if let insights = user?.insights {
            dict["insights"] = RadarPlugin.dictionaryForUserInsights(insights)
        }
        if let place = user?.place {
            dict["place"] = RadarPlugin.dictionaryForPlace(place)
        }
        if let country = user?.country {
            dict["country"] = RadarPlugin.dictionaryForRegion(country)
        }
        if let state = user?.state {
            dict["state"] = RadarPlugin.dictionaryForRegion(state)
        }
        if let dma = user?.dma {
            dict["dma"] = RadarPlugin.dictionaryForRegion(dma)
        }
        if let postalCode = user?.postalCode {
            dict["postalCode"] = RadarPlugin.dictionaryForRegion(postalCode)
        }
        return dict
    }

    static func dictionaryForUserInsights(_ insights: RadarUserInsights?) -> [String: Any?] {
        var dict = [String: Any?]()
        if let homeLocation = insights?.homeLocation {
            dict["homeLocation"] = RadarPlugin.dictionaryForUserInsightsLocation(homeLocation)
        }
        if let officeLocation = insights?.officeLocation {
            dict["officeLocation"] = RadarPlugin.dictionaryForUserInsightsLocation(officeLocation)
        }
        if let state = insights?.state {
            dict["state"] = RadarPlugin.dictionaryForUserInsightsState(state)
        }
        return dict
    }

    static func dictionaryForUserInsightsLocation(_ location: RadarUserInsightsLocation?) -> [String: Any?] {
        return [
            "type": RadarPlugin.stringForUserInsightsLocationType(location?.type),
            "location": RadarPlugin.dictionaryForLocation(location?.location),
            "confidence": RadarPlugin.numberForUserInsightsLocationConfidence(location?.confidence)
        ]
    }

    static func dictionaryForUserInsightsState(_ state: RadarUserInsightsState?) -> [String: Any?] {
        return [
            "home": state?.home,
            "office": state?.office,
            "traveling": state?.traveling
        ]
    }

    static func arrayForGeofences(_ geofences: [RadarGeofence]?) -> [[String: Any?]] {
        var arr: [[String: Any?]] = []
        guard let geofences = geofences else {
            return arr
        }
        for geofence in geofences {
            let dict = RadarPlugin.dictionaryForGeofence(geofence)
            arr.append(dict)
        }
        return arr
    }

    static func dictionaryForGeofence(_ geofence: RadarGeofence?) -> [String: Any?] {
        var dict = [String: Any?]()
        dict["_id"] = geofence?._id
        dict["description"] = geofence?.description
        if let tag = geofence?.tag {
            dict["tag"] = tag
        }
        if let externalId = geofence?.externalId {
            dict["externalId"] = externalId
        }
        return dict
    }

    static func arrayForPlaces(_ places: [RadarPlace]?) -> [[String: Any?]] {
        var arr: [[String: Any?]] = []
        guard let places = places else {
            return arr
        }
        for place in places {
            let dict = RadarPlugin.dictionaryForPlace(place)
            arr.append(dict)
        }
        return arr
    }

    static func dictionaryForPlace(_ place: RadarPlace?) -> [String: Any?] {
        var dict = [String: Any?]()
        dict["_id"] = place?._id
        dict["name"] = place?.name
        dict["categories"] = place?.categories
        if let chain = place?.chain {
          dict["chain"] = [
              "slug": chain.slug,
              "name": chain.name
          ]
        }
        return dict
    }

    static func dictionaryForRegion(_ region: RadarRegion?) -> [String: Any?] {
        return [
            "_id": region?._id,
            "type": region?.type,
            "code": region?.code,
            "name": region?.name
        ]
    }

    static func arrayForEvents(_ events: [RadarEvent]?) -> [[String: Any?]] {
        var arr: [[String: Any?]] = []
        guard let events = events else {
            return arr
        }
        for event in events {
            let dict = RadarPlugin.dictionaryForEvent(event)
            arr.append(dict)
        }
        return arr
    }

    static func dictionaryForEvent(_ event: RadarEvent?) -> [String: Any?] {
        var dict = [String: Any?]()
        dict["_id"] = event?._id
        dict["live"] = event?.live
        dict["type"] = RadarPlugin.stringForEventType(event?.type)
        if let geofence = event?.geofence {
            dict["geofence"] = RadarPlugin.dictionaryForGeofence(geofence)
        }
        if let place = event?.place {
            dict["place"] = RadarPlugin.dictionaryForPlace(place)
        }
        if let alternatePlaces = event?.alternatePlaces {
            dict["alternatePlaces"] = RadarPlugin.arrayForPlaces(alternatePlaces)
        }
        if let region = event?.region {
            dict["region"] = RadarPlugin.dictionaryForRegion(region)
        }
        dict["confidence"] = RadarPlugin.numberForEventConfidence(event?.confidence)
        return dict
    }

    static func dictionaryForLocation(_ location: CLLocation?) -> [String: Any?] {
        return [
            "latitude": location?.coordinate.latitude,
            "longitude": location?.coordinate.longitude,
            "accuracy": location?.horizontalAccuracy
        ]
    }

    static func optionsForDictionary(_ dict: [String: Any]?) -> RadarTrackingOptions {
        let options = RadarTrackingOptions()
        guard let dict = dict else {
            return options
        }
        if let sync = dict["sync"] as? String {
            if sync == "possibleStateChanges" {
                options.sync = .possibleStateChanges
            } else if sync == "all" {
                options.sync = .all
            }
        }
        if let offline = dict["offline"] as? String {
            if offline == "replayStopped" {
                options.offline = .replayStopped
            } else if offline == "replayOff" {
                options.offline = .replayOff
            }
        }
        return options
    }
}
