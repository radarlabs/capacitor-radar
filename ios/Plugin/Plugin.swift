import Foundation
import CoreLocation
import Capacitor

@objc(RadarPlugin)
public class RadarPlugin: CAPPlugin, RadarDelegate {

    let locationManager = CLLocationManager()

    public func didReceiveEvents(_ events: [RadarEvent], user: RadarUser) {
        self.notifyListeners("events", data: [
            "events": RadarPlugin.arrayForEvents(events),
            "user": RadarPlugin.dictionaryForUser(user)
        ])
    }

    public func didUpdateLocation(_ location: CLLocation, user: RadarUser) {
        self.notifyListeners("location", data: [
            "location": RadarPlugin.dictionaryForLocation(location),
            "user": RadarPlugin.dictionaryForUser(user)
        ])
    }

    public func didFail(status: RadarStatus) {
        self.notifyListeners("error", data: [
            "status": RadarPlugin.stringForStatus(status)
        ])
    }

    @objc func initialize(_ call: CAPPluginCall) {
        guard let publishableKey = call.getString("publishableKey") else {
            call.reject("publishableKey is required")
            return
        }
        Radar.initialize(publishableKey: publishableKey)
        Radar.setDelegate(self)
        call.success()
    }

    @objc func setUserId(_ call: CAPPluginCall) {
        let userId = call.getString("userId")
        Radar.setUserId(userId)
        call.success()
    }

    @objc func setDescription(_ call: CAPPluginCall) {
        let description = call.getString("description")
        Radar.setDescription(description)
        call.success()
    }

    @objc func setMetadata(_ call: CAPPluginCall) {
        let metadata = call.getObject("metadata")
        Radar.setMetadata(metadata)
        call.success()
    }

    @objc func setPlacesProvider(_ call: CAPPluginCall) {
        let placesProviderStr = call.getString("placesProvider")
        let placesProvider = RadarPlugin.placesProviderForString(placesProviderStr)
        Radar.setPlacesProvider(placesProvider)
        call.success()
    }

    @objc func getLocationPermissionsStatus(_ call: CAPPluginCall) {
        let authorizationStatus = CLLocationManager.authorizationStatus()
        let authorizationStatusStr = RadarPlugin.stringForAuthorizationStatus(authorizationStatus)
        call.success([
            "status": authorizationStatusStr
        ])
    }

    @objc func requestLocationPermissions(_ call: CAPPluginCall) {
        guard let background = call.getBool("background") else {
            call.reject("background is required")
            return
        }
        if background {
            locationManager.requestAlwaysAuthorization()
        } else {
            locationManager.requestWhenInUseAuthorization()
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
        DispatchQueue.main.async {
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
        guard let eventId = call.getString("eventId") else {
            call.reject("eventId is required")
            return
        }
        let verifiedPlaceId = call.getString("verifiedPlaceId") ?? nil
        Radar.acceptEventId(eventId, verifiedPlaceId: verifiedPlaceId)
        call.success()
    }

    @objc func rejectEvent(_ call: CAPPluginCall) {
        guard let eventId = call.getString("eventId") else {
            call.reject("eventId is required")
            return
        }
        Radar.rejectEventId(eventId)
        call.success()
    }

    static func stringForAuthorizationStatus(_ status: CLAuthorizationStatus?) -> String {
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
        switch str {
        case "facebook":
            return .facebook
        default:
            return .none
        }
    }

    static func dictionaryForUser(_ user: RadarUser?) -> [String: Any?] {
        return [
            "_id": user?._id,
            "userId": user?.userId,
            "description": user?._description,
            "geofences": RadarPlugin.arrayForGeofences(user?.geofences),
            "insights": RadarPlugin.dictionaryForUserInsights(user?.insights),
            "place": RadarPlugin.dictionaryForPlace(user?.place),
            "country": RadarPlugin.dictionaryForRegion(user?.country),
            "state": RadarPlugin.dictionaryForRegion(user?.state),
            "dma": RadarPlugin.dictionaryForRegion(user?.dma),
            "postalCode": RadarPlugin.dictionaryForRegion(user?.postalCode)
        ]
    }

    static func dictionaryForUserInsights(_ insights: RadarUserInsights?) -> [String: Any?] {
        return [
            "homeLocation": RadarPlugin.dictionaryForUserInsightsLocation(insights?.homeLocation),
            "officeLocation": RadarPlugin.dictionaryForUserInsightsLocation(insights?.officeLocation),
            "state": RadarPlugin.dictionaryForUserInsightsState(insights?.state)
        ]
    }

    static func dictionaryForUserInsightsLocation(_ location: RadarUserInsightsLocation?) -> [String: Any?] {
        return [
            "type": location?.type,
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
        return [
            "_id": geofence?._id,
            "tag": geofence?.tag,
            "externalId": geofence?.externalId,
            "description": geofence?._description,
            "metadata": geofence?.metadata
        ]
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
        return [
            "_id": place?._id,
            "name": place?.name,
            "categories": place?.categories,
            "chain": [
                "slug": place?.chain?.slug,
                "name": place?.chain?.name
            ]
        ]
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
        return [
            "_id": event?._id,
            "live": event?.live,
            "type": RadarPlugin.stringForEventType(event?.type),
            "geofence": RadarPlugin.dictionaryForGeofence(event?.geofence),
            "place": RadarPlugin.dictionaryForPlace(event?.place),
            "alternatePlaces": RadarPlugin.arrayForPlaces(event?.alternatePlaces),
            "region": RadarPlugin.dictionaryForRegion(event?.region),
            "confidence": RadarPlugin.numberForEventConfidence(event?.confidence)
        ]
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
