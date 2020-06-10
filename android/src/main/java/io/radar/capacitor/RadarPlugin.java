package io.radar.capacitor;

import android.Manifest;
import android.location.Location;
import android.os.Build;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

import io.radar.sdk.Radar;
import io.radar.sdk.RadarTrackingOptions;
import io.radar.sdk.model.RadarAddress;
import io.radar.sdk.model.RadarContext;
import io.radar.sdk.model.RadarEvent;
import io.radar.sdk.model.RadarGeofence;
import io.radar.sdk.model.RadarPlace;
import io.radar.sdk.model.RadarPoint;
import io.radar.sdk.model.RadarRoutes;
import io.radar.sdk.model.RadarUser;

public class RadarPlugin extends Plugin {

    @PluginMethod()
    public void initialize(PluginCall call) {
        String publishableKey = call.getString("publishableKey");
        Radar.initialize(this.getContext(), publishableKey);
        call.success();
    }

    @PluginMethod()
    public void setUserId(PluginCall call) {
        String userId = call.getString("userId");
        Radar.setUserId(userId);
        call.success();
    }

    @PluginMethod()
    public void setDescription(PluginCall call) {
        String description = call.getString("description");
        Radar.setDescription(description);
        call.success();
    }

    @PluginMethod()
    public void setMetadata(PluginCall call) {
        JSObject metadata = call.getObject("metadata");
        Radar.setMetadata(RadarPlugin.jsonObjectForJSObject(metadata));
        call.success();
    }

    @PluginMethod()
    public void getLocationPermissionsStatus(PluginCall call) {
        boolean foreground = hasDefinedPermission(Manifest.permission.ACCESS_FINE_LOCATION);

        String status;
        if (Build.VERSION.SDK_INT >= 29) {
            if (foreground) {
                boolean background = hasDefinedPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
                status = background ? "GRANTED_BACKGROUND" : "GRANTED_FOREGROUND";
            } else {
                status = "DENIED";
            }
        } else {
            status = foreground ? "GRANTED_FOREGROUND" : "DENIED";
        }
        JSObject ret = new JSObject();
        ret.put("status", status);
        call.success(ret);
    }

    @PluginMethod()
    public void requestLocationPermissions(PluginCall call) {
        if (!call.hasOption("background")) {
            call.reject("background is required");

            return;
        }
        boolean background = call.getBoolean("background", false);

        if (Build.VERSION.SDK_INT >= 23) {
            int requestCode = 0;
            if (background && Build.VERSION.SDK_INT >= 29) {
                pluginRequestPermissions(new String[] { Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION }, requestCode);
            } else {
                pluginRequestPermissions(new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, requestCode);
            }
        }
        call.success();
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

        if (call.hasOption("latitude") && call.hasOption("longitude") && call.hasOption("accuracy")) {
            double latitude = call.getDouble("latitude");
            double longitude = call.getDouble("longitude");
            float accuracy = call.getDouble("accuracy").floatValue();
            Location location = new Location("RadarSDK");
            location.setLatitude(latitude);
            location.setLongitude(longitude);
            location.setAccuracy(accuracy);

            Radar.trackOnce(location, callback);
        } else {
            Radar.trackOnce(callback);
        }
    }

    @PluginMethod()
    public void startTrackingEfficient(PluginCall call) {
        Radar.startTracking(RadarTrackingOptions.EFFICIENT);
        call.success();
    }

    @PluginMethod()
    public void startTrackingResponsive(PluginCall call) {
        Radar.startTracking(RadarTrackingOptions.RESPONSIVE);
        call.success();
    }

    @PluginMethod()
    public void startTrackingContinuous(PluginCall call) {
        Radar.startTracking(RadarTrackingOptions.CONTINUOUS);
        call.success();
    }

    @PluginMethod()
    public void stopTracking(PluginCall call) {
        Radar.stopTracking();
        call.success();
    }

    @PluginMethod()
    public void acceptEvent(PluginCall call) {
        String eventId = call.getString("eventId");
        String verifiedPlaceId = call.getString("verifiedPlaceId");
        Radar.acceptEvent(eventId, verifiedPlaceId);
        call.success();
    }

    @PluginMethod()
    public void rejectEvent(PluginCall call) {
        String eventId = call.getString("eventId");
        Radar.rejectEvent(eventId);
        call.success();
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

            Radar.searchPlaces(near, radius, chains, categories, groups, limit, callback);
        } else {
            Radar.searchPlaces(radius, chains, categories, groups, limit, callback);
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
        int limit = call.getInt("limit", 10);

        if (call.hasOption("near")) {
            JSObject nearObj = call.getObject("near");
            double latitude = nearObj.getDouble("latitude");
            double longitude = nearObj.getDouble("longitude");

            Location near = new Location("RadarSDK");
            near.setLatitude(latitude);
            near.setLongitude(longitude);
            near.setAccuracy(5);

            Radar.searchGeofences(near, radius, tags, limit, callback);
        } else {
            Radar.searchGeofences(radius, tags, limit, callback);
        }
    }

    @PluginMethod()
    public void searchPoints(final PluginCall call) throws JSONException {
        Radar.RadarSearchPointsCallback callback = new Radar.RadarSearchPointsCallback() {
            @Override
            public void onComplete(@NotNull Radar.RadarStatus status, @Nullable Location location, @Nullable RadarPoint[] points) {
                if (status == Radar.RadarStatus.SUCCESS && location != null && points != null) {
                    JSObject ret = new JSObject();
                    ret.put("status", status.toString());
                    ret.put("location", RadarPlugin.jsObjectForJSONObject(Radar.jsonForLocation(location)));
                    ret.put("points", RadarPlugin.jsArrayForJSONArray(RadarPoint.toJson(points)));
                    call.resolve(ret);
                } else {
                    call.reject(status.toString());
                }
            }
        };

        int radius = call.getInt("radius", 1000);
        String[] tags = RadarPlugin.stringArrayForJSArray(call.getArray("tags"));
        int limit = call.getInt("limit", 10);

        if (call.hasOption("near")) {
            JSObject nearObj = call.getObject("near");
            double latitude = nearObj.getDouble("latitude");
            double longitude = nearObj.getDouble("longitude");

            Location near = new Location("RadarSDK");
            near.setLatitude(latitude);
            near.setLongitude(longitude);
            near.setAccuracy(5);

            Radar.searchPoints(near, radius, tags, limit, callback);
        } else {
            Radar.searchPoints(radius, tags, limit, callback);
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

        Radar.autocomplete(query, near, limit, new Radar.RadarGeocodeCallback() {
            @Override
            public void onComplete(@NotNull Radar.RadarStatus status,@Nullable RadarAddress[] addresses) {
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
    public void geocode(final PluginCall call) throws JSONException {
        if (!call.hasOption("query")) {
            call.reject("query is required");

            return;
        }
        String query = call.getString("query");

        Radar.geocode(query, new Radar.RadarGeocodeCallback() {
            @Override
            public void onComplete(@NotNull Radar.RadarStatus status,@Nullable RadarAddress[] addresses) {
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
            public void onComplete(@NotNull Radar.RadarStatus status,@Nullable RadarAddress[] addresses) {
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
            double latitude = call.getDouble("latitude");
            double longitude = call.getDouble("longitude");
            Location location = new Location("RadarSDK");
            location.setLatitude(latitude);
            location.setLongitude(longitude);
            location.setAccuracy(5);

            Radar.reverseGeocode(location, callback);
        } else {
            Radar.reverseGeocode(callback);
        }
    }

    @PluginMethod()
    public void ipGeocode(final PluginCall call) throws JSONException {
        Radar.ipGeocode(new Radar.RadarIpGeocodeCallback() {
            @Override
            public void onComplete(@NotNull Radar.RadarStatus status,@Nullable RadarAddress address) {
                if (status == Radar.RadarStatus.SUCCESS && address != null) {
                    JSObject ret = new JSObject();
                    ret.put("status", status.toString());
                    ret.put("address", RadarPlugin.jsObjectForJSONObject(address.toJson()));
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
        if (modesList.contains("foot")) {
            modes.add(Radar.RadarRouteMode.FOOT);
        }
        if (modesList.contains("bike")) {
            modes.add(Radar.RadarRouteMode.BIKE);
        }
        if (modesList.contains("car")) {
            modes.add(Radar.RadarRouteMode.CAR);
        }

        if (!call.hasOption("units")) {
            call.reject("units is required");

            return;
        }
        Radar.RadarRouteUnits units = call.getString("units").equals("METRIC") ? Radar.RadarRouteUnits.METRIC : Radar.RadarRouteUnits.IMPERIAL;

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

    private static JSObject jsObjectForJSONObject(JSONObject jsonObj) {
        try {
            if (jsonObj == null) {
                return null;
            }

            JSObject obj = new JSObject();
            Iterator<String> keys = jsonObj.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                if (jsonObj.get(key) != null) {
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
