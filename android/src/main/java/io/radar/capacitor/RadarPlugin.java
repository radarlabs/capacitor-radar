package io.radar.capacitor;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.HashMap;

import io.radar.sdk.Radar;
import io.radar.sdk.RadarNotificationOptions;
import io.radar.sdk.RadarReceiver;
import io.radar.sdk.RadarTrackingOptions;
import io.radar.sdk.RadarTrackingOptions.RadarTrackingOptionsForegroundService;
import io.radar.sdk.RadarTripOptions;
import io.radar.sdk.RadarVerifiedReceiver;
import io.radar.sdk.model.RadarAddress;
import io.radar.sdk.Radar.RadarAddressVerificationStatus;
import io.radar.sdk.model.RadarContext;
import io.radar.sdk.model.RadarEvent;
import io.radar.sdk.model.RadarGeofence;
import io.radar.sdk.model.RadarPlace;
import io.radar.sdk.model.RadarRouteMatrix;
import io.radar.sdk.model.RadarRoutes;
import io.radar.sdk.model.RadarTrip;
import io.radar.sdk.model.RadarUser;
import io.radar.sdk.model.RadarVerifiedLocationToken;

@CapacitorPlugin(name = "Radar")
public class RadarPlugin extends Plugin {

    private static final String TAG = "RadarPlugin";
    protected static RadarPlugin sPlugin;

    @Override
    public void load() {
        sPlugin = this;

        Radar.setReceiver(new RadarReceiver() {
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
            public void onClientLocationUpdated(@NonNull Context context, @NonNull Location location, boolean stopped, @NonNull Radar.RadarLocationSource source) {
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
            public void onError(@NonNull Context context, @NonNull Radar.RadarStatus status) {
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
        });

        Radar.setVerifiedReceiver(new RadarVerifiedReceiver() {
            @Override
            public void onTokenUpdated(@NonNull Context context, @NonNull RadarVerifiedLocationToken token) {
                if (sPlugin == null) {
                    return;
                }

                try {
                    JSObject ret = new JSObject();
                    ret.put("token", RadarPlugin.jsObjectForJSONObject(token.toJson()));
                    sPlugin.notifyListeners("token", ret);
                } catch (Exception e) {
                    Log.e(TAG, "Exception", e);
                }
            }
        });
    }

    @PluginMethod()
    public void initialize(PluginCall call) {
        String publishableKey = call.getString("publishableKey");
        Boolean fraud = call.getBoolean("fraud", false);
        SharedPreferences.Editor editor = this.getContext().getSharedPreferences("RadarSDK", Context.MODE_PRIVATE).edit();
        editor.putString("x_platform_sdk_type", "Capacitor");
        editor.putString("x_platform_sdk_version", "3.12.0");
        editor.apply();
        Radar.initialize(this.getContext(), publishableKey, null, Radar.RadarLocationServicesProvider.GOOGLE, fraud);
        call.resolve();
    }

    @PluginMethod()
    public void setLogLevel(PluginCall call) {
        String level = call.getString("level");
        Radar.RadarLogLevel logLevel = Radar.RadarLogLevel.NONE;
        if (level == null) {
            call.reject("level is required");
            return;
        }

        level = level.toLowerCase();
        if (level.equals("error")) {
            logLevel = Radar.RadarLogLevel.ERROR;
        } else if (level.equals("warning")) {
            logLevel = Radar.RadarLogLevel.WARNING;
        } else if (level.equals("info")) {
            logLevel = Radar.RadarLogLevel.INFO;
        } else if (level.equals("debug")) {
            logLevel = Radar.RadarLogLevel.DEBUG;
        } else {
            call.reject("invalid level: " + level);
            return;
        }

        Radar.setLogLevel(logLevel);
        call.resolve();
    }

    @PluginMethod()
    public void setUserId(PluginCall call) {
        String userId = call.getString("userId");
        Radar.setUserId(userId);
        call.resolve();
    }

    @PluginMethod()
    public void getUserId(PluginCall call) {
        JSObject ret = new JSObject();
        ret.put("userId", Radar.getUserId());
        call.resolve(ret);
    }

    @PluginMethod()
    public void setDescription(PluginCall call) {
        String description = call.getString("description");
        Radar.setDescription(description);
        call.resolve();
    }

    @PluginMethod()
    public void getDescription(PluginCall call) {
        JSObject ret = new JSObject();
        ret.put("description", Radar.getDescription());
        call.resolve(ret);
    }

    @PluginMethod()
    public void setMetadata(PluginCall call) {
        JSObject metadata = call.getObject("metadata");
        Radar.setMetadata(RadarPlugin.jsonObjectForJSObject(metadata));
        call.resolve();
    }

    @PluginMethod()
    public void getMetadata(PluginCall call) {
        call.resolve(RadarPlugin.jsObjectForJSONObject(Radar.getMetadata()));
    }

    @PluginMethod()
    public void setAnonymousTrackingEnabled(PluginCall call) {
        boolean enabled = call.getBoolean("enabled");
        Radar.setAnonymousTrackingEnabled(enabled);
        call.resolve();
    }

    @PluginMethod()
    public void getLocationPermissionsStatus(PluginCall call) {
        boolean foreground = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) || hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION);

        String status;
        if (Build.VERSION.SDK_INT >= 29) {
            if (foreground) {
                boolean background = hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
                status = background ? "GRANTED_BACKGROUND" : "GRANTED_FOREGROUND";
            } else {
                status = "DENIED";
            }
        } else {
            status = foreground ? "GRANTED_FOREGROUND" : "DENIED";
        }
        JSObject ret = new JSObject();
        ret.put("status", status);
        call.resolve(ret);
    }

    @PluginMethod()
    public void requestLocationPermissions(PluginCall call) {
        boolean foreground = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION);

        if (!call.hasOption("background")) {
            call.reject("background is required");

            return;
        }
        boolean background = call.getBoolean("background", false);

        if (Build.VERSION.SDK_INT >= 23) {
            int requestCode = 0;
            if (foreground && background && Build.VERSION.SDK_INT >= 29) {
                pluginRequestPermissions(new String[] { Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION }, requestCode);
            } else {
                pluginRequestPermissions(new String[] { Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION }, requestCode);
            }
        }
        call.resolve();
    }

    @PluginMethod()
    public void getLocation(final PluginCall call) throws JSONException {
        String desiredAccuracy = call.getString("desiredAccuracy");
        RadarTrackingOptions.RadarTrackingOptionsDesiredAccuracy accuracyLevel = RadarTrackingOptions.RadarTrackingOptionsDesiredAccuracy.MEDIUM;
        String accuracy = desiredAccuracy != null ? desiredAccuracy.toLowerCase()  : "medium";

        if (accuracy.equals("low")) {
            accuracyLevel = RadarTrackingOptions.RadarTrackingOptionsDesiredAccuracy.LOW;
        } else if (accuracy.equals("medium")) {
            accuracyLevel = RadarTrackingOptions.RadarTrackingOptionsDesiredAccuracy.MEDIUM;
        } else if (accuracy.equals("high")) {
            accuracyLevel = RadarTrackingOptions.RadarTrackingOptionsDesiredAccuracy.HIGH;
        } else {
            call.reject("invalid desiredAccuracy: " + desiredAccuracy);
            return;
        }

        Radar.getLocation(accuracyLevel, new Radar.RadarLocationCallback() {
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

    @PluginMethod()
    public void trackOnce(final PluginCall call) {
        Radar.RadarTrackCallback callback = new Radar.RadarTrackCallback() {
            @Override
            public void onComplete(@NotNull Radar.RadarStatus status, @Nullable Location location, @Nullable RadarEvent[] events, @Nullable RadarUser user) {
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

        if (call.hadOption("location")) {
            JSObject locationObj = call.getObject("location");
            double latitude = locationObj.getDouble("latitude");
            double longitude = locationObj.getDouble("longitude");
            float accuracy = locationObj.getDouble("accuracy").floatValue();
            Location location = new Location("RadarSDK");
            location.setLatitude(latitude);
            location.setLongitude(longitude);
            location.setAccuracy(accuracy);

            Radar.trackOnce(location, callback);
        } else if (call.hasOption("latitude") && call.hasOption("longitude") && call.hasOption("accuracy")) {
            double latitude = call.getDouble("latitude");
            double longitude = call.getDouble("longitude");
            float accuracy = call.getDouble("accuracy").floatValue();
            Location location = new Location("RadarSDK");
            location.setLatitude(latitude);
            location.setLongitude(longitude);
            location.setAccuracy(accuracy);

            Radar.trackOnce(location, callback);
        } else if (call.hasOption("desiredAccuracy")) {
            RadarTrackingOptions.RadarTrackingOptionsDesiredAccuracy accuracyLevel = RadarTrackingOptions.RadarTrackingOptionsDesiredAccuracy.MEDIUM;

            String desiredAccuracy = call.getString("desiredAccuracy").toLowerCase();
            if (desiredAccuracy.equals("none")) {
                accuracyLevel = RadarTrackingOptions.RadarTrackingOptionsDesiredAccuracy.NONE;
            } else if (desiredAccuracy.equals("low")) {
                accuracyLevel = RadarTrackingOptions.RadarTrackingOptionsDesiredAccuracy.LOW;
            } else if (desiredAccuracy.equals("medium")) {
                accuracyLevel = RadarTrackingOptions.RadarTrackingOptionsDesiredAccuracy.MEDIUM;
            } else if (desiredAccuracy.equals("high")) {
                accuracyLevel = RadarTrackingOptions.RadarTrackingOptionsDesiredAccuracy.HIGH;
            } else {
                call.reject("invalid desiredAccuracy: " + desiredAccuracy);
                return;
            }

            boolean beaconsTrackingOption = false;
            if (call.hasOption("beacons")) {
                beaconsTrackingOption = call.getBoolean("beacons");
            }
            Radar.trackOnce(accuracyLevel, beaconsTrackingOption, callback);
        } else {
            Radar.trackOnce(callback);
        }
    }

    @PluginMethod()
    public void trackVerified(final PluginCall call) {
        boolean beacons = call.getBoolean("beacons", false);
        
        Radar.trackVerified(beacons, new Radar.RadarTrackVerifiedCallback() {
            @Override
            public void onComplete(@NotNull Radar.RadarStatus status, @Nullable RadarVerifiedLocationToken token) {
                if (status == Radar.RadarStatus.SUCCESS && token != null) {
                    JSObject ret = new JSObject();
                    ret.put("status", status.toString());
                    ret.put("token", RadarPlugin.jsObjectForJSONObject(token.toJson()));
                    call.resolve(ret);
                } else {
                    call.reject(status.toString());
                }
            }
        });
    }

    @PluginMethod()
    public void getVerifiedLocationToken(final PluginCall call) {
        Radar.getVerifiedLocationToken(new Radar.RadarTrackVerifiedCallback() {
            @Override
            public void onComplete(@NotNull Radar.RadarStatus status, @Nullable RadarVerifiedLocationToken token) {
                if (status == Radar.RadarStatus.SUCCESS && token != null) {
                    JSObject ret = new JSObject();
                    ret.put("status", status.toString());
                    ret.put("token", RadarPlugin.jsObjectForJSONObject(token.toJson()));
                    call.resolve(ret);
                } else {
                    call.reject(status.toString());
                }
            }
        });
    }

    @PluginMethod()
    public void startTrackingVerified(PluginCall call) {
        int interval = call.getInt("interval", 1200);
        boolean beacons = call.getBoolean("beacons", false);

        Radar.startTrackingVerified(interval, beacons);
        call.resolve();
    }

    @PluginMethod()
    public void startTrackingEfficient(PluginCall call) {
        Radar.startTracking(RadarTrackingOptions.EFFICIENT);
        call.resolve();
    }

    @PluginMethod()
    public void startTrackingResponsive(PluginCall call) {
        Radar.startTracking(RadarTrackingOptions.RESPONSIVE);
        call.resolve();
    }

    @PluginMethod()
    public void startTrackingContinuous(PluginCall call) {
        Radar.startTracking(RadarTrackingOptions.CONTINUOUS);
        call.resolve();
    }

    @PluginMethod()
    public void startTrackingCustom(PluginCall call) {
        JSObject trackingOptionsObj = call.getObject("options");

        JSONObject trackingOptionsJson = RadarPlugin.jsonObjectForJSObject(trackingOptionsObj);
        RadarTrackingOptions trackingOptions = RadarTrackingOptions.fromJson(trackingOptionsJson);
        Radar.startTracking(trackingOptions);
        call.resolve();
    }

    @PluginMethod()
    public void mockTracking(final PluginCall call) throws JSONException {
        if (!call.hasOption("origin")) {
            call.reject("origin is required");

            return;
        }
        JSObject originObj = call.getObject("origin");
        double originLatitude = originObj.getDouble("latitude");
        double originLongitude = originObj.getDouble("longitude");
        Location origin = new Location("RadarSDK");
        origin.setLatitude(originLatitude);
        origin.setLongitude(originLongitude);
        origin.setAccuracy(5);

        if (!call.hasOption("destination")) {
            call.reject("destination is required");

            return;
        }
        JSObject destinationObj = call.getObject("destination");
        double destinationLatitude = destinationObj.getDouble("latitude");
        double destinationLongitude = destinationObj.getDouble("longitude");
        Location destination = new Location("RadarSDK");
        destination.setLatitude(destinationLatitude);
        destination.setLongitude(destinationLongitude);
        destination.setAccuracy(5);

        if (!call.hasOption("mode")) {
            call.reject("mode is required");

            return;
        }
        String modeStr = call.getString("mode");
        Radar.RadarRouteMode mode = Radar.RadarRouteMode.CAR;
        if (modeStr.equals("FOOT") || modeStr.equals("foot")) {
            mode = Radar.RadarRouteMode.FOOT;
        } else if (modeStr.equals("BIKE") || modeStr.equals("bike")) {
            mode = Radar.RadarRouteMode.BIKE;
        } else if (modeStr.equals("CAR") || modeStr.equals("car")) {
            mode = Radar.RadarRouteMode.CAR;
        }

        int steps = call.getInt("steps", 10);
        int interval = call.getInt("interval", 1);

        Radar.mockTracking(origin, destination, mode, steps, interval, new Radar.RadarTrackCallback() {
            @Override
            public void onComplete(@NotNull Radar.RadarStatus radarStatus, @Nullable Location location, @Nullable RadarEvent[] radarEvents, @Nullable RadarUser radarUser) {

            }
        });

        call.resolve();
    }

    @PluginMethod()
    public void stopTracking(PluginCall call) {
        Radar.stopTracking();
        call.resolve();
    }

    @PluginMethod()
    public void stopTrackingVerified(PluginCall call) {
        Radar.stopTrackingVerified();
        call.resolve();
    }

    @PluginMethod()
    public void isTracking(PluginCall call) {
        JSObject ret = new JSObject();
        ret.put("isTracking", Radar.isTracking());
        call.resolve(ret);
    }

    @PluginMethod()
    public void getTrackingOptions(PluginCall call) {
        RadarTrackingOptions options = Radar.getTrackingOptions();
        call.resolve(RadarPlugin.jsObjectForJSONObject(options.toJson()));
    }

    @PluginMethod()
    public void setForegroundServiceOptions(PluginCall call) {
        JSObject optionsObj = call.getObject("options");
        JSONObject optionsJson = RadarPlugin.jsonObjectForJSObject(optionsObj);
        RadarTrackingOptionsForegroundService options = RadarTrackingOptionsForegroundService.fromJson(optionsJson);
        Radar.setForegroundServiceOptions(options);
        call.resolve();
    }

    @PluginMethod()
    public void startTrip(PluginCall call) {
        JSObject optionsObj = call.getObject("options");
        JSONObject optionsJson = RadarPlugin.jsonObjectForJSObject(optionsObj);
        if (optionsJson == null) {
            call.reject("options is required");
            return;
        }
        // new format is { tripOptions, trackingOptions }
        JSONObject tripOptionsJson = optionsJson.optJSONObject("tripOptions");
        if (tripOptionsJson == null) {
            // legacy format
            tripOptionsJson = optionsJson;
        }
        RadarTripOptions options = RadarTripOptions.fromJson(tripOptionsJson);

        RadarTrackingOptions trackingOptions = null;
        JSONObject trackingOptionsJson = optionsJson.optJSONObject("trackingOptions");
        if (trackingOptionsJson != null) {
            trackingOptions = RadarTrackingOptions.fromJson(trackingOptionsJson);
        }
        Radar.startTrip(options, trackingOptions, new Radar.RadarTripCallback() {
            @Override
            public void onComplete(@NonNull Radar.RadarStatus status,
                                   @Nullable RadarTrip trip,
                                   @Nullable RadarEvent[] events) {
                JSObject ret = new JSObject();
                ret.put("status", status.toString());
                if (trip != null) {
                    ret.put("trip", RadarPlugin.jsObjectForJSONObject(trip.toJson()));
                }
                if (events != null) {
                    ret.put("events", RadarPlugin.jsArrayForArray(events));
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
        RadarTrip.RadarTripStatus status = RadarTrip.RadarTripStatus.UNKNOWN;
        if (call.hasOption("status")) {
            String statusStr = call.getString("status");
            if (statusStr.equals("STARTED") || statusStr.equals("started")) {
                status = RadarTrip.RadarTripStatus.STARTED;
            } else if (statusStr.equals("APPROACHING") || statusStr.equals("approaching")) {
                status = RadarTrip.RadarTripStatus.APPROACHING;
            } else if (statusStr.equals("ARRIVED") || statusStr.equals("arrived")) {
                status = RadarTrip.RadarTripStatus.ARRIVED;
            } else if (statusStr.equals("COMPLETED") || statusStr.equals("completed")) {
                status = RadarTrip.RadarTripStatus.COMPLETED;
            } else if (statusStr.equals("CANCELED") || statusStr.equals("canceled")) {
                status = RadarTrip.RadarTripStatus.CANCELED;
            }
        }

        Radar.updateTrip(options, status, new Radar.RadarTripCallback() {
            @Override
            public void onComplete(@NonNull Radar.RadarStatus status,
                                   @Nullable RadarTrip trip,
                                   @Nullable RadarEvent[] events) {
                JSObject ret = new JSObject();
                ret.put("status", status.toString());
                if (trip != null) {
                    ret.put("trip", RadarPlugin.jsObjectForJSONObject(trip.toJson()));
                }
                if (events != null) {
                    ret.put("events", RadarPlugin.jsArrayForArray(events));
                }
                call.resolve(ret);
            }
        });
    }

    @PluginMethod()
    public void completeTrip(PluginCall call) {
        Radar.completeTrip(new Radar.RadarTripCallback() {
            @Override
            public void onComplete(@NonNull Radar.RadarStatus status,
                                   @Nullable RadarTrip trip,
                                   @Nullable RadarEvent[] events) {
                JSObject ret = new JSObject();
                ret.put("status", status.toString());
                if (trip != null) {
                    ret.put("trip", RadarPlugin.jsObjectForJSONObject(trip.toJson()));
                }
                if (events != null) {
                    ret.put("events", RadarPlugin.jsArrayForArray(events));
                }
                call.resolve(ret);
            }
        });
    }

    @PluginMethod()
    public void cancelTrip(PluginCall call) {
        Radar.cancelTrip(new Radar.RadarTripCallback() {
            @Override
            public void onComplete(@NonNull Radar.RadarStatus status,
                                   @Nullable RadarTrip trip,
                                   @Nullable RadarEvent[] events) {
                JSObject ret = new JSObject();
                ret.put("status", status.toString());
                if (trip != null) {
                    ret.put("trip", RadarPlugin.jsObjectForJSONObject(trip.toJson()));
                }
                if (events != null) {
                    ret.put("events", RadarPlugin.jsArrayForArray(events));
                }
                call.resolve(ret);
            }
        });
    }

    @PluginMethod()
    public void acceptEvent(PluginCall call) {
        String eventId = call.getString("eventId");
        String verifiedPlaceId = call.getString("verifiedPlaceId");
        Radar.acceptEvent(eventId, verifiedPlaceId);
        call.resolve();
    }

    @PluginMethod()
    public void rejectEvent(PluginCall call) {
        String eventId = call.getString("eventId");
        Radar.rejectEvent(eventId);
        call.resolve();
    }

    @PluginMethod()
    public void getTripOptions(PluginCall call) {
        RadarTripOptions options = Radar.getTripOptions();
        if (options != null) {
            call.resolve(RadarPlugin.jsObjectForJSONObject(options.toJson()));
        } else {
            call.reject("No trip options available");
        }
    }

    @PluginMethod()
    public void getContext(final PluginCall call) {
        Radar.RadarContextCallback callback = new Radar.RadarContextCallback() {
            @Override
            public void onComplete(@NotNull Radar.RadarStatus status, @Nullable Location location, @Nullable RadarContext context) {
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
            double latitude = call.getDouble("latitude");
            double longitude = call.getDouble("longitude");
            Location location = new Location("RadarSDK");
            location.setLatitude(latitude);
            location.setLongitude(longitude);
            location.setAccuracy(5);

            Radar.getContext(location, callback);
        } else {
            Radar.getContext(callback);
        }
    }

    @PluginMethod()
    public void searchPlaces(final PluginCall call) throws JSONException {
        Radar.RadarSearchPlacesCallback callback = new Radar.RadarSearchPlacesCallback() {
            @Override
            public void onComplete(@NotNull Radar.RadarStatus status, @Nullable Location location, @Nullable RadarPlace[] places) {
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

        int radius = call.getInt("radius", 1000);
        String[] chains = RadarPlugin.stringArrayForJSArray(call.getArray("chains"));
        Map<String, String> chainMetadata = RadarPlugin.stringMapForJSObject(call.getObject("chainMetadata"));
        String[] categories = RadarPlugin.stringArrayForJSArray(call.getArray("categories"));
        String[] groups = RadarPlugin.stringArrayForJSArray(call.getArray("groups"));
        int limit = call.getInt("limit", 10);

        if (call.hasOption("near")) {
            JSObject nearObj = call.getObject("near");
            double latitude = nearObj.getDouble("latitude");
            double longitude = nearObj.getDouble("longitude");
            Location near = new Location("RadarSDK");
            near.setLatitude(latitude);
            near.setLongitude(longitude);
            near.setAccuracy(5);

            Radar.searchPlaces(near, radius, chains, chainMetadata, categories, groups, limit, callback);
        } else {
            Radar.searchPlaces(radius, chains, chainMetadata, categories, groups, limit, callback);
        }
    }

    @PluginMethod()
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

        int radius = call.getInt("radius", 1000);
        String[] tags = RadarPlugin.stringArrayForJSArray(call.getArray("tags"));
        JSONObject metadata = RadarPlugin.jsonObjectForJSObject(call.getObject("metadata"));
        int limit = call.getInt("limit", 10);
        boolean includeGeometry = call.getBoolean("includeGeometry", false);

        if (call.hasOption("near")) {
            JSObject nearObj = call.getObject("near");
            double latitude = nearObj.getDouble("latitude");
            double longitude = nearObj.getDouble("longitude");
            Location near = new Location("RadarSDK");
            near.setLatitude(latitude);
            near.setLongitude(longitude);
            near.setAccuracy(5);

            Radar.searchGeofences(near, radius, tags, metadata, limit, includeGeometry, callback);
        } else {
            Radar.searchGeofences(radius, tags, metadata, limit, includeGeometry, callback);
        }
    }

    @PluginMethod()
    public void autocomplete(final PluginCall call) throws JSONException {
        if (!call.hasOption("query")) {
            call.reject("query is required");

            return;
        }
        String query = call.getString("query");

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

        int limit = call.getInt("limit", 10);
        String country = call.getString("country");
        String[] layers = RadarPlugin.stringArrayForJSArray(call.getArray("layers"));
        boolean expandUnits = call.getBoolean("expandUnits", false);
        boolean mailable = call.getBoolean("mailable", false);

        Radar.autocomplete(query, near, layers, limit, country, expandUnits, mailable, new Radar.RadarGeocodeCallback() {
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

    @PluginMethod()
    public void validateAddress(final PluginCall call) throws JSONException {
        if (!call.hasOption("address")) {
            call.reject("address is required");

            return;
        }
        RadarAddress address = RadarAddress.fromJson(call.getObject("address"));


        Radar.validateAddress(address, new Radar.RadarValidateAddressCallback() {
            @Override
            public void onComplete(@NotNull Radar.RadarStatus status, @Nullable RadarAddress address, @Nullable RadarAddressVerificationStatus verificationStatus) {
                if (status == Radar.RadarStatus.SUCCESS && address != null) {
                    JSObject ret = new JSObject();
                    ret.put("status", status.toString());
                    ret.put("address", RadarPlugin.jsObjectForJSONObject(address.toJson()));
                    ret.put("verificationStatus", Radar.stringForVerificationStatus(verificationStatus));
                    call.resolve(ret);
                } else {
                    call.reject(status.toString());
                }
            }
        });
    }

    @PluginMethod()
    public void geocode(final PluginCall call) throws JSONException {
        if (!call.hasOption("query")) {
            call.reject("query is required");

            return;
        }
        String query = call.getString("query");
        String[] layers = RadarPlugin.stringArrayForJSArray(call.getArray("layers"));
        String[] countries = RadarPlugin.stringArrayForJSArray(call.getArray("countries"));

        Radar.geocode(query, layers, countries, new Radar.RadarGeocodeCallback() {
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

    @PluginMethod()
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

        String[] layers = RadarPlugin.stringArrayForJSArray(call.getArray("layers"));

        if (call.hasOption("location")) {
            JSObject locationObj = call.getObject("location");
            double latitude = locationObj.getDouble("latitude");
            double longitude = locationObj.getDouble("longitude");
            Location location = new Location("RadarSDK");
            location.setLatitude(latitude);
            location.setLongitude(longitude);
            location.setAccuracy(5);

            Radar.reverseGeocode(location, layers, callback);
        } else {
            Radar.reverseGeocode(layers, callback);
        }
    }

    @PluginMethod()
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

    @PluginMethod()
    public void getDistance(final PluginCall call) throws JSONException {
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

        if (!call.hasOption("destination")) {
            call.reject("destination is required");

            return;
        }
        JSObject destinationObj = call.getObject("destination");
        double destinationLatitude = destinationObj.getDouble("latitude");
        double destinationLongitude = destinationObj.getDouble("longitude");
        Location destination = new Location("RadarSDK");
        destination.setLatitude(destinationLatitude);
        destination.setLongitude(destinationLongitude);
        destination.setAccuracy(5);

        if (!call.hasOption("modes")) {
            call.reject("modes is required");

            return;
        }
        EnumSet<Radar.RadarRouteMode> modes = EnumSet.noneOf(Radar.RadarRouteMode.class);
        List<String> modesList = call.getArray("modes").toList();
        if (modesList.contains("FOOT") || modesList.contains("foot")) {
            modes.add(Radar.RadarRouteMode.FOOT);
        }
        if (modesList.contains("BIKE") || modesList.contains("bike")) {
            modes.add(Radar.RadarRouteMode.BIKE);
        }
        if (modesList.contains("CAR") || modesList.contains("car")) {
            modes.add(Radar.RadarRouteMode.CAR);
        }

        if (!call.hasOption("units")) {
            call.reject("units is required");

            return;
        }
        String unitsStr = call.getString("units");
        Radar.RadarRouteUnits units = unitsStr.equals("METRIC") || unitsStr.equals("metric") ? Radar.RadarRouteUnits.METRIC : Radar.RadarRouteUnits.IMPERIAL;

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
    public void getMatrix(final PluginCall call) throws JSONException {
        JSArray originsArr = call.getArray("origins");
        Location[] origins = new Location[originsArr.length()];
        for (int i = 0; i < originsArr.length(); i++) {
            JSONObject originObj = originsArr.getJSONObject(i);
            double latitude = originObj.getDouble("latitude");
            double longitude = originObj.getDouble("longitude");
            Location origin = new Location("RadarSDK");
            origin.setLatitude(latitude);
            origin.setLongitude(longitude);
            origins[i] = origin;
        }
        JSArray destinationsArr = call.getArray("destinations");
        Location[] destinations = new Location[destinationsArr.length()];
        for (int i = 0; i < destinationsArr.length(); i++) {
            JSONObject destinationObj = destinationsArr.getJSONObject(i);
            double latitude = destinationObj.getDouble("latitude");
            double longitude = destinationObj.getDouble("longitude");
            Location destination = new Location("RadarSDK");
            destination.setLatitude(latitude);
            destination.setLongitude(longitude);
            destinations[i] = destination;
        }
        String modeStr = call.getString("mode");
        Radar.RadarRouteMode mode = Radar.RadarRouteMode.CAR;
        if (modeStr == null) {
            call.reject("mode is required");
            return;
        }
        modeStr = modeStr.toLowerCase();
        if ( modeStr.equals("foot")) {
            mode = Radar.RadarRouteMode.FOOT;
        } else if (modeStr.equals("bike")) {
            mode = Radar.RadarRouteMode.BIKE;
        } else if (modeStr.equals("car")) {
            mode = Radar.RadarRouteMode.CAR;
        } else if (modeStr.equals("truck")) {
            mode = Radar.RadarRouteMode.TRUCK;
        } else if (modeStr.equals("motorbike")) {
            mode = Radar.RadarRouteMode.MOTORBIKE;
        } else {
            call.reject("invalid mode: " + mode);
            return;
        }

        String unitsStr = call.getString("units");
        Radar.RadarRouteUnits units = unitsStr.equals("METRIC") || unitsStr.equals("metric") ? Radar.RadarRouteUnits.METRIC : Radar.RadarRouteUnits.IMPERIAL;

        Radar.getMatrix(origins, destinations, mode, units, new Radar.RadarMatrixCallback() {
            @Override
            public void onComplete(@NonNull Radar.RadarStatus status, @Nullable RadarRouteMatrix matrix) {
                if (status == Radar.RadarStatus.SUCCESS) {
                    JSObject ret = new JSObject();
                    ret.put("status", status.toString());
                    if (matrix != null) {
                        ret.put("matrix", matrix.toJson());
                    }
                    call.resolve(ret);
                } else {
                    call.reject(status.toString());
                }
            }
        });
    }

    @PluginMethod()
    public void logConversion(final PluginCall call) throws JSONException  {
        if (!call.hasOption("name")) {
            call.reject("name is required");

            return;
        }

        String name = call.getString("name");
        double revenue = 0;
        if (call.hasOption("revenue")) {
            revenue = call.getDouble("revenue");
        }
        JSObject metadataObj = call.getObject("metadata");
        JSONObject metadataJson = RadarPlugin.jsonObjectForJSObject(metadataObj);

        if (revenue > 0) {
            Radar.logConversion(name, revenue, metadataJson, new Radar.RadarLogConversionCallback() {
                @Override
                public void onComplete(@NonNull Radar.RadarStatus status, @Nullable RadarEvent event) {
                    if (status == Radar.RadarStatus.SUCCESS) {
                        JSObject ret = new JSObject();
                        ret.put("status", status.toString());
                        if (event != null) {
                            ret.put("event", event.toJson());
                        }
                        call.resolve(ret);
                    } else {
                        call.reject(status.toString());
                    }
                }
            });
        } else {
            Radar.logConversion(name, metadataJson, new Radar.RadarLogConversionCallback() {
                @Override
                public void onComplete(@NonNull Radar.RadarStatus status, @Nullable RadarEvent event) {
                    if (status == Radar.RadarStatus.SUCCESS) {
                        JSObject ret = new JSObject();
                        ret.put("status", status.toString());
                        if (event != null) {
                            ret.put("event", event.toJson());
                        }
                        call.resolve(ret);
                    } else {
                        call.reject(status.toString());
                    }
                }
            });
        }
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
                if (jsObj.get(key) != null) {
                    obj.put(key, jsObj.get(key));
                }
            }
            return obj;
        } catch (JSONException j) {
            return null;
        }
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

    private static JSArray jsArrayForArray(Object[] array) {
        return jsArrayForJSONArray(new JSONArray(Arrays.asList(array)));
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

    private static Map<String, String> stringMapForJSObject(JSObject jsObj) {
        try {
            if (jsObj == null) {
                return null;
            }

            Map<String, String> stringMap = new HashMap<String, String>();
            Iterator<String> keys = jsObj.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                if (jsObj.get(key) != null) {
                    stringMap.put(key, jsObj.getString(key));
                }
            }
            return stringMap;
        } catch (JSONException j) {
            return null;
        }
    }

    @PluginMethod()
    public void logTermination(final PluginCall call) {
        // not implemented
        call.resolve();
    }

    @PluginMethod()
    public void logBackgrounding(final PluginCall call) {
        Radar.logBackgrounding();
        call.resolve();
    }

    @PluginMethod()
    public void logResigningActive(final PluginCall call) {
        Radar.logResigningActive();
        call.resolve();
    }

    @PluginMethod()
    public void setNotificationOptions(final PluginCall call) {
        JSObject notificationOptionsObj = call.getObject("options");

        JSONObject notificationOptionsJson = RadarPlugin.jsonObjectForJSObject(notificationOptionsObj);
        RadarNotificationOptions notificationOptions = RadarNotificationOptions.fromJson(notificationOptionsJson);
        Radar.setNotificationOptions(notificationOptions);
        call.resolve();
    }

    @PluginMethod()
    public void isUsingRemoteTrackingOptions(final PluginCall call) {
        JSObject ret = new JSObject();
        ret.put("isUsingRemoteTrackingOptions", Radar.isUsingRemoteTrackingOptions());
        call.resolve(ret);
    }

    @PluginMethod()
    public void getHost(final PluginCall call) {
        JSObject ret = new JSObject();
        ret.put("host", Radar.getHost());
        call.resolve(ret);
    }

    @PluginMethod()
    public void getPublishableKey(final PluginCall call) {
        JSObject ret = new JSObject();
        ret.put("publishableKey", Radar.getPublishableKey());
        call.resolve(ret);
    }
}
