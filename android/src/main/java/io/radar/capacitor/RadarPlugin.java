package io.radar.capacitor;

import android.Manifest;
import android.content.Context;
import android.location.Location;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.PermissionState;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import io.radar.sdk.Radar;
import io.radar.sdk.RadarReceiver;
import io.radar.sdk.RadarTrackingOptions;
import io.radar.sdk.RadarTripOptions;
import io.radar.sdk.model.RadarAddress;
import io.radar.sdk.model.RadarContext;
import io.radar.sdk.model.RadarEvent;
import io.radar.sdk.model.RadarGeofence;
import io.radar.sdk.model.RadarPlace;
import io.radar.sdk.model.RadarRoutes;
import io.radar.sdk.model.RadarTrip;
import io.radar.sdk.model.RadarUser;

@CapacitorPlugin(name = "Radar")
public class RadarPlugin extends Plugin {

    private static final String TAG = "RadarPlugin";
    protected static RadarPlugin sPlugin;
    private final RadarPluginReceiver mReceiver = new RadarPluginReceiver();

    public static class RadarPluginReceiver extends RadarReceiver {
        @Override
        public void onEventsReceived(@NonNull Context context, @NonNull RadarEvent[] events, @Nullable RadarUser user) {
            if (sPlugin == null) {
                return;
            }

            try {
                JSObject ret = new JSObject();
                ret.put("events", RadarPlugin.jsArrayForJSONArray(RadarEvent.toJson(events)));
                if (user != null) {
                    ret.put("user", RadarPlugin.jsObjectForJSONObject(user.toJson()));
                }
                sPlugin.notifyListeners("events", ret);
            } catch (Exception e) {
                Log.e(TAG, "Exception", e);
            }
        }

        @Override
        public void onLocationUpdated(@NonNull Context context, @NonNull Location location, @NonNull RadarUser user) {
            if (sPlugin == null) {
                return;
            }

            try {
                JSObject ret = new JSObject();
                ret.put("location", RadarPlugin.jsObjectForJSONObject(Radar.jsonForLocation(location)));
                ret.put("user", RadarPlugin.jsObjectForJSONObject(user.toJson()));
                sPlugin.notifyListeners("location", ret);
            } catch (Exception e) {
                Log.e(TAG, "Exception", e);
            }
        }

        @Override
        public void onClientLocationUpdated(@NonNull Context context,
                                            @NonNull Location location,
                                            boolean stopped,
                                            @NonNull Radar.RadarLocationSource source) {
            if (sPlugin == null) {
                return;
            }

            try {
                JSObject ret = new JSObject();
                ret.put("location", RadarPlugin.jsObjectForJSONObject(Radar.jsonForLocation(location)));
                ret.put("stopped", stopped);
                ret.put("source", Radar.stringForSource(source));
                sPlugin.notifyListeners("clientLocation", ret);
            } catch (Exception e) {
                Log.e(TAG, "Exception", e);
            }
        }

        @Override
        public void onError(@NonNull Context context,
                            @NonNull Radar.RadarStatus status) {
            if (sPlugin == null) {
                return;
            }

            try {
                JSObject ret = new JSObject();
                ret.put("status", status.toString());
                sPlugin.notifyListeners("error", ret);
            } catch (Exception e) {
                Log.e(TAG, "Exception", e);
            }
        }

        @Override
        public void onLog(@NonNull Context context, @NonNull String message) {
            if (sPlugin == null) {
                return;
            }

            try {
                JSObject ret = new JSObject();
                ret.put("message", message);
                sPlugin.notifyListeners("log", ret);
            } catch (Exception e) {
                Log.e(TAG, "Exception", e);
            }
        }
    }

    @Override
    public void load() {
        sPlugin = this;
        String publishableKey = getContext().getString(R.string.radar_publishableKey);
        if (publishableKey == null || TextUtils.isEmpty(publishableKey)) {
            boolean ignoreWarning = getContext().getResources().getBoolean(R.bool.ignore_radar_initialize_warning);
            if (!ignoreWarning) {
                Log.w(TAG, "Radar could not initialize. Did you set string 'radar_publishableKey' in strings.xml?");
            }
        } else {
            Radar.initialize(getContext(), publishableKey, mReceiver);
        }
    }

    @PluginMethod
    public void initialize(PluginCall call) {
        String publishableKey = call.getString("publishableKey");
        Radar.initialize(this.getContext(), publishableKey, mReceiver);
        call.resolve();
    }

    @PluginMethod
    public void setUserId(PluginCall call) {
        String userId = call.getString("userId");
        Radar.setUserId(userId);
        call.resolve();
    }

    @PluginMethod
    public void setDescription(PluginCall call) {
        String description = call.getString("description");
        Radar.setDescription(description);
        call.resolve();
    }

    @PluginMethod
    public void setMetadata(PluginCall call) {
        JSObject metadata = call.getObject("metadata");
        Radar.setMetadata(RadarPlugin.jsonObjectForJSObject(metadata));
        call.resolve();
    }

    @PluginMethod
    public void getLocationPermissionsStatus(PluginCall call) {
        boolean foreground = false;
        String locationAuthorization = "NOT_DETERMINED";
        PermissionState gpsPermissionState = getPermissionState(Manifest.permission.ACCESS_FINE_LOCATION);
        PermissionState wifiPermissionState = getPermissionState(Manifest.permission.ACCESS_COARSE_LOCATION);
        if (PermissionState.GRANTED == gpsPermissionState || PermissionState.GRANTED == wifiPermissionState) {
            locationAuthorization = "GRANTED_FOREGROUND";
            foreground = true;
        } else if (PermissionState.DENIED == gpsPermissionState
                || PermissionState.DENIED == wifiPermissionState
                || PermissionState.PROMPT_WITH_RATIONALE == gpsPermissionState
                || PermissionState.PROMPT_WITH_RATIONALE == wifiPermissionState) {
            locationAuthorization = "DENIED";
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (foreground) {
                PermissionState backgroundState = getPermissionState(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
                if (PermissionState.GRANTED == backgroundState) {
                    locationAuthorization = "GRANTED_BACKGROUND";
                }
            }
        }
        JSObject ret = new JSObject();
        ret.put("status", locationAuthorization);
        call.resolve(ret);
    }

    @PluginMethod
    public void requestLocationPermissions(PluginCall call) {
        if (!call.hasOption("background")) {
            call.reject("background is required");
            return;
        }
        //noinspection ConstantConditions
        boolean background = call.getBoolean("background", false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int requestCode = 0;
            if (background && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                pluginRequestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                }, requestCode);
            } else {
                pluginRequestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, requestCode);
            }
        }
        call.resolve();
    }


    @PluginMethod
    public void getLocation(final PluginCall call) throws JSONException {
        Radar.getLocation(new Radar.RadarLocationCallback() {
            @Override
            public void onComplete(@NotNull Radar.RadarStatus status, @Nullable Location location, boolean stopped) {
                if (status == Radar.RadarStatus.SUCCESS && location != null) {
                    JSObject ret = new JSObject();
                    ret.put("status", status.toString());
                    ret.put("location", RadarPlugin.jsObjectForJSONObject(Radar.jsonForLocation(location)));
                    ret.put("stopped", stopped);
                    call.resolve(ret);
                } else {
                    call.reject(status.toString());
                }
            }
        });
    }

    @PluginMethod
    public void trackOnce(final PluginCall call) {
        Radar.RadarTrackCallback callback = new Radar.RadarTrackCallback() {
            @Override
            public void onComplete(@NotNull Radar.RadarStatus status,
                                   @Nullable Location location,
                                   @Nullable RadarEvent[] events,
                                   @Nullable RadarUser user) {
                if (status == Radar.RadarStatus.SUCCESS && location != null && events != null && user != null) {
                    JSObject ret = new JSObject();
                    ret.put("status", status.toString());
                    ret.put("location", RadarPlugin.jsObjectForJSONObject(Radar.jsonForLocation(location)));
                    ret.put("events", RadarPlugin.jsArrayForJSONArray(RadarEvent.toJson(events)));
                    ret.put("user", RadarPlugin.jsObjectForJSONObject(user.toJson()));
                    call.resolve(ret);
                } else {
                    call.reject(status.toString());
                }
            }
        };

        if (call.hasOption("latitude") && call.hasOption("longitude") && call.hasOption("accuracy")) {
            Double latitude = call.getDouble("latitude");
            Double longitude = call.getDouble("longitude");
            Double accuracy = call.getDouble("accuracy");
            if (latitude != null && longitude != null && accuracy != null) {
                Location location = new Location("RadarSDK");
                location.setLatitude(latitude);
                location.setLongitude(longitude);
                location.setAccuracy(accuracy.floatValue());

                Radar.trackOnce(location, callback);
            } else {
                Radar.trackOnce(callback);
            }
        } else {
            Radar.trackOnce(callback);
        }
    }

    @PluginMethod
    public void startTrackingEfficient(PluginCall call) {
        Radar.startTracking(RadarTrackingOptions.EFFICIENT);
        call.resolve();
    }

    @PluginMethod
    public void startTrackingResponsive(PluginCall call) {
        Radar.startTracking(RadarTrackingOptions.RESPONSIVE);
        call.resolve();
    }

    @PluginMethod
    public void startTrackingContinuous(PluginCall call) {
        Radar.startTracking(RadarTrackingOptions.CONTINUOUS);
        call.resolve();
    }

    @PluginMethod
    public void startTrackingCustom(PluginCall call) {
        JSObject trackingOptionsObj = call.getObject("options");
        JSONObject trackingOptionsJson = RadarPlugin.jsonObjectForJSObject(trackingOptionsObj);
        if (trackingOptionsJson != null) {
            RadarTrackingOptions trackingOptions = RadarTrackingOptions.fromJson(trackingOptionsJson);
            Radar.startTracking(trackingOptions);
            call.resolve();
        } else {
            call.reject("options is required");
        }
    }

    @PluginMethod
    public void mockTracking(final PluginCall call) throws JSONException {
        if (!call.hasOption("origin")) {
            call.reject("origin is required");
            return;
        }
        if (!call.hasOption("destination")) {
            call.reject("destination is required");
            return;
        }
        if (!call.hasOption("mode")) {
            call.reject("mode is required");
            return;
        }
        JSObject originObj = call.getObject("origin");
        double originLatitude = originObj.getDouble("latitude");
        double originLongitude = originObj.getDouble("longitude");
        Location origin = new Location("RadarSDK");
        origin.setLatitude(originLatitude);
        origin.setLongitude(originLongitude);
        origin.setAccuracy(5);

        JSObject destinationObj = call.getObject("destination");
        double destinationLatitude = destinationObj.getDouble("latitude");
        double destinationLongitude = destinationObj.getDouble("longitude");
        Location destination = new Location("RadarSDK");
        destination.setLatitude(destinationLatitude);
        destination.setLongitude(destinationLongitude);
        destination.setAccuracy(5);

        String modeStr = call.getString("mode");
        Radar.RadarRouteMode mode = Radar.RadarRouteMode.CAR;
        if (modeStr != null) {
            switch (modeStr) {
                case "FOOT":
                case "foot":
                    mode = Radar.RadarRouteMode.FOOT;
                    break;
                case "BIKE":
                case "bike":
                    mode = Radar.RadarRouteMode.BIKE;
                    break;
                case "CAR":
                case "car":
                    mode = Radar.RadarRouteMode.CAR;
                    break;
                default:
                    Log.e(TAG, "No mode for string " + modeStr);
                    break;
            }
        }

        //noinspection ConstantConditions
        int steps = call.getInt("steps", 10);
        //noinspection ConstantConditions
        int interval = call.getInt("interval", 1);

        Radar.mockTracking(origin, destination, mode, steps, interval, (Radar.RadarTrackCallback) null);
        call.resolve();
    }

    @PluginMethod
    public void stopTracking(PluginCall call) {
        Radar.stopTracking();
        call.resolve();
    }

    @PluginMethod
    public void startTrip(PluginCall call) {
        JSObject optionsObj = call.getObject("options");
        JSONObject optionsJson = RadarPlugin.jsonObjectForJSObject(optionsObj);
        if (optionsJson == null) {
            call.reject("options is required");
            return;
        }
        RadarTripOptions options = RadarTripOptions.fromJson(optionsJson);
        Radar.startTrip(options, new Radar.RadarTripCallback() {
            @Override
            public void onComplete(@NonNull Radar.RadarStatus radarStatus,
                                   @Nullable RadarTrip radarTrip,
                                   @Nullable RadarEvent[] radarEvents) {
                JSObject ret = new JSObject();
                ret.put("status", radarStatus.toString());
                if (radarTrip != null) {
                    ret.put("trip", RadarPlugin.jsObjectForJSONObject(radarTrip.toJson()));
                }
                if (radarEvents != null) {
                    ret.put("events", RadarPlugin.jsArrayForArray(radarEvents));
                }
                call.resolve(ret);
            }
        });
    }

    @PluginMethod
    public void completeTrip(PluginCall call) {
        Radar.completeTrip(new Radar.RadarTripCallback() {
            @Override
            public void onComplete(@NonNull Radar.RadarStatus radarStatus,
                                   @Nullable RadarTrip radarTrip,
                                   @Nullable RadarEvent[] radarEvents) {
                JSObject ret = new JSObject();
                ret.put("status", radarStatus.toString());
                if (radarTrip != null) {
                    ret.put("trip", RadarPlugin.jsObjectForJSONObject(radarTrip.toJson()));
                }
                if (radarEvents != null) {
                    ret.put("events", RadarPlugin.jsArrayForArray(radarEvents));
                }
                call.resolve(ret);
            }
        });
    }

    @PluginMethod
    public void cancelTrip(PluginCall call) {
        Radar.cancelTrip(new Radar.RadarTripCallback() {
            @Override
            public void onComplete(@NonNull Radar.RadarStatus radarStatus,
                                   @Nullable RadarTrip radarTrip,
                                   @Nullable RadarEvent[] radarEvents) {
                JSObject ret = new JSObject();
                ret.put("status", radarStatus.toString());
                if (radarTrip != null) {
                    ret.put("trip", RadarPlugin.jsObjectForJSONObject(radarTrip.toJson()));
                }
                if (radarEvents != null) {
                    ret.put("events", RadarPlugin.jsArrayForArray(radarEvents));
                }
                call.resolve(ret);
            }
        });
    }

    @PluginMethod
    public void updateTrip(PluginCall call) {
        JSObject optionsObj = call.getObject("options");
        JSONObject optionsJson = RadarPlugin.jsonObjectForJSObject(optionsObj);
        if (optionsJson == null) {
            call.reject("options is required");
            return;
        }
        RadarTripOptions options = RadarTripOptions.fromJson(optionsJson);
        RadarTrip.RadarTripStatus status = null;
        if (call.hasOption("status")) {
            String statusStr = call.getString("status");
            if (statusStr != null) {
                for (RadarTrip.RadarTripStatus tripStatus : RadarTrip.RadarTripStatus.values()) {
                    if (tripStatus.name().equalsIgnoreCase(statusStr)) {
                        status = tripStatus;
                        break;
                    }
                }
                if (status == null) {
                    call.reject(Radar.RadarStatus.ERROR_BAD_REQUEST.toString());
                    return;
                }
            }
        }
        status = status == null ? RadarTrip.RadarTripStatus.UNKNOWN : status;
        Radar.updateTrip(options, status, new Radar.RadarTripCallback() {
            @Override
            public void onComplete(@NonNull Radar.RadarStatus radarStatus,
                                   @Nullable RadarTrip radarTrip,
                                   @Nullable RadarEvent[] radarEvents) {
                JSObject ret = new JSObject();
                ret.put("status", radarStatus.toString());
                if (radarTrip != null) {
                    ret.put("trip", RadarPlugin.jsObjectForJSONObject(radarTrip.toJson()));
                }
                if (radarEvents != null) {
                    ret.put("events", RadarPlugin.jsArrayForArray(radarEvents));
                }
                call.resolve(ret);
            }
        });
    }

    @PluginMethod
    public void acceptEvent(PluginCall call) {
        String eventId = call.getString("eventId");
        String verifiedPlaceId = call.getString("verifiedPlaceId");
        if (eventId == null) {
            call.reject("eventId is required");
            return;
        }
        Radar.acceptEvent(eventId, verifiedPlaceId);
        call.resolve();
    }

    @PluginMethod
    public void rejectEvent(PluginCall call) {
        String eventId = call.getString("eventId");
        if (eventId == null) {
            call.reject("eventId is required");
            return;
        }
        Radar.rejectEvent(eventId);
        call.resolve();
    }

    @PluginMethod
    public void getContext(final PluginCall call) {
        Radar.RadarContextCallback callback = new Radar.RadarContextCallback() {
            @Override
            public void onComplete(@NotNull Radar.RadarStatus status,
                                   @Nullable Location location,
                                   @Nullable RadarContext context) {
                if (status == Radar.RadarStatus.SUCCESS && location != null && context != null) {
                    JSObject ret = new JSObject();
                    ret.put("status", status.toString());
                    ret.put("location", RadarPlugin.jsObjectForJSONObject(Radar.jsonForLocation(location)));
                    ret.put("context", RadarPlugin.jsObjectForJSONObject(context.toJson()));
                    call.resolve(ret);
                } else {
                    call.reject(status.toString());
                }
            }
        };

        if (call.hasOption("latitude") && call.hasOption("longitude")) {
            Double latitude = call.getDouble("latitude");
            Double longitude = call.getDouble("longitude");
            if (latitude == null || longitude == null) {
                Radar.getContext(callback);
            } else {
                Location location = new Location("RadarSDK");
                location.setLatitude(latitude);
                location.setLongitude(longitude);
                location.setAccuracy(5);

                Radar.getContext(location, callback);
            }
        } else {
            Radar.getContext(callback);
        }
    }

    @PluginMethod
    public void searchPlaces(final PluginCall call) throws JSONException {
        Radar.RadarSearchPlacesCallback callback = new Radar.RadarSearchPlacesCallback() {
            @Override
            public void onComplete(@NotNull Radar.RadarStatus status,
                                   @Nullable Location location,
                                   @Nullable RadarPlace[] places) {
                if (status == Radar.RadarStatus.SUCCESS && location != null && places != null) {
                    JSObject ret = new JSObject();
                    ret.put("status", status.toString());
                    ret.put("location", RadarPlugin.jsObjectForJSONObject(Radar.jsonForLocation(location)));
                    ret.put("places", RadarPlugin.jsArrayForJSONArray(RadarPlace.toJson(places)));
                    call.resolve(ret);
                } else {
                    call.reject(status.toString());
                }
            }
        };

        //noinspection ConstantConditions
        int radius = call.getInt("radius", 1000);
        String[] chains = RadarPlugin.stringArrayForJSArray(call.getArray("chains"));
        String[] categories = RadarPlugin.stringArrayForJSArray(call.getArray("categories"));
        String[] groups = RadarPlugin.stringArrayForJSArray(call.getArray("groups"));
        //noinspection ConstantConditions
        int limit = call.getInt("limit", 10);

        if (call.hasOption("near")) {
            JSObject nearObj = call.getObject("near");
            double latitude = nearObj.getDouble("latitude");
            double longitude = nearObj.getDouble("longitude");
            Location near = new Location("RadarSDK");
            near.setLatitude(latitude);
            near.setLongitude(longitude);
            near.setAccuracy(5);

            Radar.searchPlaces(near, radius, chains, categories, groups, limit, callback);
        } else {
            Radar.searchPlaces(radius, chains, categories, groups, limit, callback);
        }
    }

    @PluginMethod
    public void searchGeofences(final PluginCall call) throws JSONException {
        Radar.RadarSearchGeofencesCallback callback = new Radar.RadarSearchGeofencesCallback() {
            @Override
            public void onComplete(@NotNull Radar.RadarStatus status, @Nullable Location location, @Nullable RadarGeofence[] geofences) {
                if (status == Radar.RadarStatus.SUCCESS && location != null && geofences != null) {
                    JSObject ret = new JSObject();
                    ret.put("status", status.toString());
                    ret.put("location", RadarPlugin.jsObjectForJSONObject(Radar.jsonForLocation(location)));
                    ret.put("geofences", RadarPlugin.jsArrayForJSONArray(RadarGeofence.toJson(geofences)));
                    call.resolve(ret);
                } else {
                    call.reject(status.toString());
                }
            }
        };

        //noinspection ConstantConditions
        int radius = call.getInt("radius", 1000);
        String[] tags = RadarPlugin.stringArrayForJSArray(call.getArray("tags"));
        //noinspection ConstantConditions
        int limit = call.getInt("limit", 10);

        if (call.hasOption("near")) {
            JSObject nearObj = call.getObject("near");
            double latitude = nearObj.getDouble("latitude");
            double longitude = nearObj.getDouble("longitude");
            Location near = new Location("RadarSDK");
            near.setLatitude(latitude);
            near.setLongitude(longitude);
            near.setAccuracy(5);

            Radar.searchGeofences(near, radius, tags, null, limit, callback);
        } else {
            Radar.searchGeofences(radius, tags, null, limit, callback);
        }
    }

    @PluginMethod
    public void autocomplete(final PluginCall call) throws JSONException {
        if (!call.hasOption("query")) {
            call.reject("query is required");
            return;
        }
        String query = call.getString("query");
        if (query == null) {
            call.reject("query is required");
            return;
        }

        if (!call.hasOption("near")) {
            call.reject("near is required");
            return;
        }
        JSObject nearObj = call.getObject("near");
        double latitude = nearObj.getDouble("latitude");
        double longitude = nearObj.getDouble("longitude");
        Location near = new Location("RadarSDK");
        near.setLatitude(latitude);
        near.setLongitude(longitude);
        near.setAccuracy(5);

        //noinspection ConstantConditions
        int limit = call.getInt("limit", 10);

        Radar.autocomplete(query, near, limit, new Radar.RadarGeocodeCallback() {
            @Override
            public void onComplete(@NotNull Radar.RadarStatus status, @Nullable RadarAddress[] addresses) {
                if (status == Radar.RadarStatus.SUCCESS && addresses != null) {
                    JSObject ret = new JSObject();
                    ret.put("status", status.toString());
                    ret.put("addresses", RadarPlugin.jsArrayForJSONArray(RadarAddress.toJson(addresses)));
                    call.resolve(ret);
                } else {
                    call.reject(status.toString());
                }
            }
        });
    }

    @PluginMethod
    public void geocode(final PluginCall call) throws JSONException {
        if (!call.hasOption("query")) {
            call.reject("query is required");
            return;
        }
        String query = call.getString("query");
        if (query == null) {
            call.reject("query is required");
            return;
        }

        Radar.geocode(query, new Radar.RadarGeocodeCallback() {
            @Override
            public void onComplete(@NotNull Radar.RadarStatus status, @Nullable RadarAddress[] addresses) {
                if (status == Radar.RadarStatus.SUCCESS && addresses != null) {
                    JSObject ret = new JSObject();
                    ret.put("status", status.toString());
                    ret.put("addresses", RadarPlugin.jsArrayForJSONArray(RadarAddress.toJson(addresses)));
                    call.resolve(ret);
                } else {
                    call.reject(status.toString());
                }
            }
        });
    }

    @PluginMethod
    public void reverseGeocode(final PluginCall call) throws JSONException {
        Radar.RadarGeocodeCallback callback = new Radar.RadarGeocodeCallback() {
            @Override
            public void onComplete(@NotNull Radar.RadarStatus status, @Nullable RadarAddress[] addresses) {
                if (status == Radar.RadarStatus.SUCCESS && addresses != null) {
                    JSObject ret = new JSObject();
                    ret.put("status", status.toString());
                    ret.put("addresses", RadarPlugin.jsArrayForJSONArray(RadarAddress.toJson(addresses)));
                    call.resolve(ret);
                } else {
                    call.reject(status.toString());
                }
            }
        };

        if (call.hasOption("latitude") && call.hasOption("longitude")) {
            Double latitude = call.getDouble("latitude");
            Double longitude = call.getDouble("longitude");
            if (latitude == null || longitude == null) {
                Radar.reverseGeocode(callback);
            } else {
                Location location = new Location("RadarSDK");
                location.setLatitude(latitude);
                location.setLongitude(longitude);
                location.setAccuracy(5);

                Radar.reverseGeocode(location, callback);
            }
        } else {
            Radar.reverseGeocode(callback);
        }
    }

    @PluginMethod
    public void ipGeocode(final PluginCall call) throws JSONException {
        Radar.ipGeocode(new Radar.RadarIpGeocodeCallback() {
            @Override
            public void onComplete(@NotNull Radar.RadarStatus status, @Nullable RadarAddress address, boolean proxy) {
                if (status == Radar.RadarStatus.SUCCESS && address != null) {
                    JSObject ret = new JSObject();
                    ret.put("status", status.toString());
                    ret.put("address", RadarPlugin.jsObjectForJSONObject(address.toJson()));
                    ret.put("proxy", proxy);
                    call.resolve(ret);
                } else {
                    call.reject(status.toString());
                }
            }
        });
    }

    @PluginMethod
    public void getDistance(final PluginCall call) throws JSONException {
        if (!call.hasOption("destination")) {
            call.reject("destination is required");
            return;
        }
        if (!call.hasOption("modes")) {
            call.reject("modes is required");
            return;
        }
        if (!call.hasOption("units")) {
            call.reject("units is required");
            return;
        }
        Radar.RadarRouteCallback callback = new Radar.RadarRouteCallback() {
            @Override
            public void onComplete(@NotNull Radar.RadarStatus status, @Nullable RadarRoutes routes) {
                if (status == Radar.RadarStatus.SUCCESS && routes != null) {
                    JSObject ret = new JSObject();
                    ret.put("status", status.toString());
                    ret.put("points", RadarPlugin.jsObjectForJSONObject(routes.toJson()));
                    call.resolve(ret);
                } else {
                    call.reject(status.toString());
                }
            }
        };
        JSObject destinationObj = call.getObject("destination");
        double destinationLatitude = destinationObj.getDouble("latitude");
        double destinationLongitude = destinationObj.getDouble("longitude");
        Location destination = new Location("RadarSDK");
        destination.setLatitude(destinationLatitude);
        destination.setLongitude(destinationLongitude);
        destination.setAccuracy(5);

        EnumSet<Radar.RadarRouteMode> modes = EnumSet.noneOf(Radar.RadarRouteMode.class);
        List<String> modesList = new ArrayList<>(call.getArray("modes").toList());
        for (Radar.RadarRouteMode mode : Radar.RadarRouteMode.values()) {
            int index = modesList.indexOf(mode.name());
            if (index >= 0) {
                modes.add(mode);
                modesList.remove(index);
            } else {
                index = modesList.indexOf(mode.name().toLowerCase(Locale.ROOT));
                if (index >= 0) {
                    modes.add(mode);
                    modesList.remove(index);
                }
            }
        }

        String unitsStr = call.getString("units");
        if (unitsStr == null) {
            call.reject("units is required");
            return;
        }
        Radar.RadarRouteUnits units = unitsStr.equalsIgnoreCase("METRIC")
                ? Radar.RadarRouteUnits.METRIC : Radar.RadarRouteUnits.IMPERIAL;

        if (call.hasOption("origin")) {
            JSObject originObj = call.getObject("origin");
            double originLatitude = originObj.getDouble("latitude");
            double originLongitude = originObj.getDouble("longitude");

            Location origin = new Location("RadarSDK");
            origin.setLatitude(originLatitude);
            origin.setLongitude(originLongitude);

            Radar.getDistance(origin, destination, modes, units, callback);
        } else {
            Radar.getDistance(destination, modes, units, callback);
        }
    }

    @PluginMethod
    public void setLogLevel(final PluginCall call) {
        if (!call.hasOption("level")) {
            call.reject("level is required");
            return;
        }
        String levelStr = call.getString("level");
        Radar.RadarLogLevel level = Radar.RadarLogLevel.INFO;
        if (levelStr != null) {
            switch (levelStr) {
                case "NONE":
                case "none":
                    level = Radar.RadarLogLevel.NONE;
                    break;
                case "ERROR":
                case "error":
                    level = Radar.RadarLogLevel.ERROR;
                    break;
                case "WARNING":
                case "warning":
                    level = Radar.RadarLogLevel.WARNING;
                    break;
                case "INFO":
                case "info":
                    level = Radar.RadarLogLevel.INFO;
                    break;
                case "DEBUG":
                case "debug":
                    level = Radar.RadarLogLevel.DEBUG;
                    break;
                default:
                    Log.e(TAG, "No level for string " + levelStr);
                    break;
            }
        }
        Radar.setLogLevel(level);
        call.resolve();
    }

    private static JSObject jsObjectForJSONObject(JSONObject jsonObj) {
        try {
            if (jsonObj == null) {
                return null;
            }

            JSObject obj = new JSObject();
            Iterator<String> keys = jsonObj.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                if (jsonObj.opt(key) != null) {
                    obj.put(key, jsonObj.get(key));
                }
            }
            return obj;
        } catch (JSONException j) {
            return null;
        }
    }

    private static JSONObject jsonObjectForJSObject(JSObject jsObj) {
        try {
            if (jsObj == null) {
                return null;
            }

            JSONObject obj = new JSONObject();
            Iterator<String> keys = jsObj.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                jsObj.get(key);
                obj.put(key, jsObj.get(key));
            }
            return obj;
        } catch (JSONException j) {
            return null;
        }
    }

    private static JSArray jsArrayForArray(Object[] array) {
        return jsArrayForJSONArray(new JSONArray(Arrays.asList(array)));
    }

    private static JSArray jsArrayForJSONArray(JSONArray jsonArr) {
        try {
            if (jsonArr == null) {
                return null;
            }

            JSArray arr = new JSArray();
            for (int i = 0; i < jsonArr.length(); i++) {
                arr.put(RadarPlugin.jsObjectForJSONObject(jsonArr.getJSONObject(i)));
            }
            return arr;
        } catch (JSONException j) {
            return null;
        }
    }

    private static String[] stringArrayForJSArray(JSArray jsArr) {
        try {
            if (jsArr == null) {
                return null;
            }

            String[] arr = new String[jsArr.length()];
            for (int i = 0; i < jsArr.length(); i++) {
                arr[i] = jsArr.getString(i);
            }
            return arr;
        } catch (JSONException j) {
            return null;
        }
    }

}
