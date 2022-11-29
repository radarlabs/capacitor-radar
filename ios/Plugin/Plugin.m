#import <Foundation/Foundation.h>
#import <Capacitor/Capacitor.h>

CAP_PLUGIN(RadarPlugin, "Radar",
    CAP_PLUGIN_METHOD(initialize, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(setLogLevel, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(setUserId, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(getUserId, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(setDescription, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(getDescription, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(setMetadata, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(getMetadata, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(setAnonymousTrackingEnabled, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(setAdIdEnabled, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(getLocationPermissionsStatus, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(requestLocationPermissions, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(getLocation, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(trackOnce, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(startTrackingEfficient, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(startTrackingResponsive, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(startTrackingContinuous, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(startTrackingCustom, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(mockTracking, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(stopTracking, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(isTracking, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(setForegroundServiceOptions, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(startTrip, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(updateTrip, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(completeTrip, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(cancelTrip, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(acceptEvent, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(rejectEvent, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(getTripOptions, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(getContext, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(searchPlaces, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(searchGeofences, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(autocomplete, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(geocode, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(reverseGeocode, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(ipGeocode, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(getDistance, CAPPluginReturnPromise);
)
