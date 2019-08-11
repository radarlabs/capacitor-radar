#import <Foundation/Foundation.h>
#import <Capacitor/Capacitor.h>

CAP_PLUGIN(RadarPlugin, "RadarPlugin",
    CAP_PLUGIN_METHOD(initialize, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(setUserId, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(setDescription, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(setMetadata, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(setPlacesProvider, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(getLocationPermissionsStatus, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(requestLocationPermissions, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(startTracking, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(stopTracking, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(trackOnce, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(updateLocation, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(acceptEvent, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(rejectEvent, CAPPluginReturnPromise);
)
