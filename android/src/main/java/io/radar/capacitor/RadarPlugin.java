package io.radar.capacitor;

import android.Manifest;
import android.location.Location;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import io.radar.sdk.Radar;
import io.radar.sdk.Radar.RadarTrackingOffline;
import io.radar.sdk.Radar.RadarTrackingPriority;
import io.radar.sdk.Radar.RadarTrackingSync;
import io.radar.sdk.RadarTrackingOptions;
import io.radar.sdk.model.RadarEvent;
import io.radar.sdk.model.RadarGeofence;
import io.radar.sdk.model.RadarPlace;
import io.radar.sdk.model.RadarRegion;
import io.radar.sdk.model.RadarUser;
import io.radar.sdk.model.RadarUserInsights;
import io.radar.sdk.model.RadarUserInsightsLocation;
import io.radar.sdk.model.RadarUserInsightsState;

@NativePlugin(
    permissions={
        Manifest.permission.ACCESS_FINE_LOCATION
    }
)
public class RadarPlugin extends Plugin {

    @PluginMethod()
    public void initialize(PluginCall call) {
        String publishableKey = call.getString("publishableKey");
        Radar.initialize(publishableKey);
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
        Radar.setMetadata(RadarPlugin.objectForJSONObject(metadata));
        call.success();
    }

    @PluginMethod()
    public void setPlacesProvider(PluginCall call) {
        String placesProviderStr = call.getString("placesProvider");
        if (placesProviderStr == null) {
            call.reject("placesProvider is required");
            return;
        }
        Radar.setPlacesProvider(RadarPlugin.placesProviderForString(placesProviderStr));
    }

    @PluginMethod()
    public void getLocationPermissionsStatus(PluginCall call) {
        boolean hasRequiredPermissions = hasRequiredPermissions();
        JSObject ret = new JSObject();
        ret.put("status", RadarPlugin.stringForPermissionsStatus(hasRequiredPermissions));
        call.success(ret);
    }

    @PluginMethod()
    public void requestLocationPermissions(PluginCall call) {
        pluginRequestAllPermissions();
        call.success();
    }

    @PluginMethod()
    public void startTracking(PluginCall call) {
        JSObject optionsObj = call.getObject("options");
        RadarTrackingOptions options = RadarPlugin.optionsForObject(optionsObj);
        if (options != null) {
            Radar.startTracking(options);
        } else {
            Radar.startTracking();
        }
    }

    @PluginMethod()
    public void stopTracking(PluginCall call) {
        Radar.stopTracking();
    }

    @PluginMethod()
    public void trackOnce(final PluginCall call) {
        Radar.trackOnce(new Radar.RadarCallback() {
            @Override
            public void onComplete(@NotNull Radar.RadarStatus status, @Nullable Location location, @Nullable RadarEvent[] events, @Nullable RadarUser user) {
                if (status == Radar.RadarStatus.SUCCESS) {
                    JSObject ret = new JSObject();
                    ret.put("status", RadarPlugin.stringForStatus(status));
                    ret.put("location", RadarPlugin.objectForLocation(location));
                    ret.put("events", RadarPlugin.arrayForEvents(events));
                    ret.put("user", RadarPlugin.objectForUser(user));
                    call.resolve(ret);
                } else {
                    call.reject(RadarPlugin.stringForStatus((status)));
                }
            }
        });
    }

    @PluginMethod()
    public void updateLocation(final PluginCall call) {
        double latitude;
        Double latitudeDouble = call.getDouble("latitude");
        if (latitudeDouble == null) {
            Integer latitudeInteger = call.getInt("latitude");
            if (latitudeInteger == null) {
                call.reject("latitude is required");
                return;
            }
            latitude = latitudeInteger.doubleValue();
        } else {
            latitude = latitudeDouble.doubleValue();
        }
        double longitude;
        Double longitudeDouble = call.getDouble("longitude");
        if (longitudeDouble == null) {
            Integer longitudeInteger = call.getInt("longitude");
            if (longitudeInteger == null) {
                call.reject("longitude is required");
                return;
            }
            longitude = longitudeInteger.doubleValue();
        }
        else {
            longitude = longitudeDouble.doubleValue();
        }
        float accuracy;
        Float accuracyFloat = call.getFloat("accuracy");
        if (accuracyFloat == null) {
            Integer accuracyInteger = call.getInt("accuracy");
            if (accuracyInteger == null) {
                call.reject("accuracy is required");
                return;
            }
            accuracy = accuracyInteger.floatValue();
        } else {
            accuracy = accuracyFloat.floatValue();
        }
        Location location = new Location("RadarSDK");
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        location.setAccuracy(accuracy);
        Radar.updateLocation(location, new Radar.RadarCallback() {
            @Override
            public void onComplete(@NotNull Radar.RadarStatus status, @Nullable Location location, @Nullable RadarEvent[] events, @Nullable RadarUser user) {
                if (status == Radar.RadarStatus.SUCCESS) {
                    JSObject ret = new JSObject();
                    ret.put("status", RadarPlugin.stringForStatus(status));
                    ret.put("location", RadarPlugin.objectForLocation(location));
                    ret.put("events", RadarPlugin.arrayForEvents(events));
                    ret.put("user", RadarPlugin.objectForUser(user));
                    call.resolve(ret);
                } else {
                    call.reject(RadarPlugin.stringForStatus((status)));
                }
            }
        });
    }

    @PluginMethod()
    public void acceptEvent(PluginCall call) {
        String eventId = call.getString("eventId");
        String verifiedPlaceId = call.getString("verifiedPlaceId");
        Radar.acceptEvent(eventId, verifiedPlaceId);
    }

    @PluginMethod()
    public void rejectEvent(PluginCall call) {
        String eventId = call.getString("eventId");
        Radar.rejectEvent(eventId);
    }

    private static String stringForPermissionsStatus(boolean hasGrantedPermissions) {
        if (hasGrantedPermissions) {
            return "GRANTED";
        }
        return "DENIED";
    }

    private static String stringForStatus(Radar.RadarStatus status) {
        return status.toString();
    }

    private static String stringForEventType(RadarEvent.RadarEventType type) {
        switch (type) {
            case USER_ENTERED_GEOFENCE:
                return "user.entered_geofence";
            case USER_EXITED_GEOFENCE:
                return "user.exited_geofence";
            case USER_ENTERED_HOME:
                return "user.entered_home";
            case USER_EXITED_HOME:
                return "user.exited_home";
            case USER_ENTERED_OFFICE:
                return "user.entered_office";
            case USER_EXITED_OFFICE:
                return "user.exited_office";
            case USER_STARTED_TRAVELING:
                return "user.started_traveling";
            case USER_STOPPED_TRAVELING:
                return "user.stopped_traveling";
            case USER_ENTERED_PLACE:
                return "user.entered_place";
            case USER_EXITED_PLACE:
                return "user.exited_place";
            case USER_NEARBY_PLACE_CHAIN:
                return "user.nearby_place_chain";
            case USER_ENTERED_REGION_STATE:
                return "user.entered_region_state";
            case USER_EXITED_REGION_STATE:
                return "user.exited_region_state";
            case USER_ENTERED_REGION_COUNTRY:
                return "user.entered_region_country";
            case USER_EXITED_REGION_COUNTRY:
                return "user.exited_region_country";
            case USER_ENTERED_REGION_DMA:
                return "user.entered_region_dma";
            case USER_EXITED_REGION_DMA:
                return "user.exited_region_dma";
            default:
                return null;
        }
    }

    private static int numberForEventConfidence(RadarEvent.RadarEventConfidence confidence) {
        switch (confidence) {
            case HIGH:
                return 3;
            case MEDIUM:
                return 2;
            case LOW:
                return 1;
            default:
                return 0;
        }
    }

    private static String stringForUserInsightsLocationType(RadarUserInsightsLocation.RadarUserInsightsLocationType type) {
        switch (type) {
            case HOME:
                return "home";
            case OFFICE:
                return "office";
            default:
                return null;
        }
    }

    private static int numberForUserInsightsLocationConfidence(RadarUserInsightsLocation.RadarUserInsightsLocationConfidence confidence) {
        switch (confidence) {
            case HIGH:
                return 3;
            case MEDIUM:
                return 2;
            case LOW:
                return 1;
            default:
                return 0;
        }
    }

    private static Radar.RadarPlacesProvider placesProviderForString(String providerStr) {
        if (providerStr.equals("facebook")) {
            return Radar.RadarPlacesProvider.FACEBOOK;
        }
        return Radar.RadarPlacesProvider.NONE;
    }

    private static JSObject objectForUser(RadarUser user) {
        if (user == null) {
            return null;
        }

        JSObject obj = new JSObject();
        obj.put("_id", user.getId());
        obj.put("userId", user.getUserId());
        obj.put("description", user.getDescription());
        obj.put("geofences", RadarPlugin.arrayForGeofences(user.getGeofences()));
        obj.put("insights", RadarPlugin.objectForUserInsights(user.getInsights()));
        obj.put("place", RadarPlugin.objectForPlace(user.getPlace()));
        obj.put("country", RadarPlugin.objectForRegion(user.getCountry()));
        obj.put("state", RadarPlugin.objectForRegion(user.getState()));
        obj.put("dma", RadarPlugin.objectForRegion(user.getDma()));
        obj.put("postalCode", RadarPlugin.objectForRegion(user.getPostalCode()));
        return obj;
    }

    private static JSObject objectForUserInsights(RadarUserInsights insights) {
        if (insights == null) {
            return null;
        }

        JSObject obj = new JSObject();
        obj.put("homeLocation", RadarPlugin.objectForUserInsightsLocation(insights.getHomeLocation()));
        obj.put("officeLocation", RadarPlugin.objectForUserInsightsLocation(insights.getOfficeLocation()));
        obj.put("state", RadarPlugin.objectForUserInsightsState(insights.getState()));
        return obj;
    }

    private static JSObject objectForUserInsightsLocation(RadarUserInsightsLocation location) {
        if (location == null) {
            return null;
        }

        JSObject obj = new JSObject();
        obj.put("type", RadarPlugin.stringForUserInsightsLocationType(location.getType()));
        obj.put("location", RadarPlugin.objectForLocation(location.getLocation()));
        obj.put("confidence", RadarPlugin.numberForUserInsightsLocationConfidence(location.getConfidence()));
        return obj;
    }

    private static JSObject objectForUserInsightsState(RadarUserInsightsState state) {
        if (state == null) {
            return null;
        }

        JSObject obj = new JSObject();
        obj.put("home", state.getHome());
        obj.put("office", state.getOffice());
        obj.put("traveling", state.getTraveling());
        return obj;
    }

    private static JSArray arrayForGeofences(RadarGeofence[] geofences) {
        if (geofences == null) {
            return null;
        }

        JSArray arr = new JSArray();
        for (RadarGeofence geofence : geofences) {
            arr.put(RadarPlugin.objectForGeofence(geofence));
        }
        return arr;
    }

    private static JSObject objectForGeofence(RadarGeofence geofence) {
        if (geofence == null) {
            return null;
        }

        JSObject obj = new JSObject();
        obj.put("_id", geofence.getId());
        obj.put("tag", geofence.getTag());
        obj.put("externalId", geofence.getExternalId());
        obj.put("description", geofence.getDescription());
        return obj;
    }

    private static JSArray arrayForPlaces(RadarPlace[] places) {
        if (places == null) {
            return null;
        }

        JSArray arr = new JSArray();
        for (RadarPlace place : places) {
            arr.put(RadarPlugin.objectForPlace(place));
        }
        return arr;
    }

    private static JSObject objectForPlace(RadarPlace place) {
        if (place == null) {
            return null;
        }

        JSObject obj = new JSObject();
        obj.put("_id", place.getId());
        obj.put("name", place.getName());
        obj.put("categories", place.getCategories());
        if (place.getChain() != null) {
            JSObject chainObj = new JSObject();
            chainObj.put("slug", place.getChain().getSlug());
            chainObj.put("name", place.getChain().getName());
        }
        return obj;
    }

    private static JSObject objectForRegion(RadarRegion region) {
        if (region == null) {
            return null;
        }

        JSObject obj = new JSObject();
        obj.put("_id", region.getId());
        obj.put("type", region.getType());
        obj.put("code", region.getCode());
        obj.put("name", region.getName());
        return obj;
    }

    private static JSArray arrayForEvents(RadarEvent[] events) {
        if (events == null) {
            return null;
        }

        JSArray arr = new JSArray();
        for (RadarEvent event : events) {
            arr.put(RadarPlugin.objectForEvent(event));
        }
        return arr;
    }

    private static JSObject objectForEvent(RadarEvent event) {
        if (event == null) {
            return null;
        }

        JSObject obj = new JSObject();
        obj.put("_id", event.getId());
        obj.put("live", event.getLive());
        obj.put("type", RadarPlugin.stringForEventType(event.getType()));
        obj.put("geofence", RadarPlugin.objectForGeofence(event.getGeofence()));
        obj.put("place", RadarPlugin.objectForPlace(event.getPlace()));
        obj.put("alternatePlaces", RadarPlugin.arrayForPlaces(event.getAlternatePlaces()));
        obj.put("region", RadarPlugin.objectForRegion(event.getRegion()));
        obj.put("confidence", RadarPlugin.numberForEventConfidence(event.getConfidence()));
        obj.put("duration", event.getDuration());
        return obj;
    }

    private static JSObject objectForLocation(Location location) {
        if (location == null) {
            return null;
        }

        JSObject obj = new JSObject();
        obj.put("latitude", location.getLatitude());
        obj.put("longitude", location.getLongitude());
        obj.put("accuracy", location.getAccuracy());
        return obj;
    }

    private static JSObject objectForJSONObject(JSONObject jsonObj) {
        try {
            if (jsonObj == null) {
                return null;
            }

            JSObject obj = new JSObject();
            Iterator<String> keys = jsonObj.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                obj.put(key, jsonObj.get(key));
            }
            return obj;
        } catch (JSONException j) {
            return null;
        }
    }

    private static RadarTrackingOptions optionsForObject(JSObject optionsObj) {
        try {
            RadarTrackingOptions.Builder options = new RadarTrackingOptions.Builder();
            if (optionsObj != null) {
                if (optionsObj.get("sync") != null) {
                    switch (optionsObj.getString("sync")) {
                        case "possibleStateChanges":
                            options.sync(RadarTrackingSync.POSSIBLE_STATE_CHANGES);
                            break;
                        case "all":
                            options.sync(RadarTrackingSync.ALL);
                            break;
                    }
                }
                if (optionsObj.get("offline") != null) {
                    switch (optionsObj.getString("offline")) {
                        case "replayStopped":
                            options.offline(RadarTrackingOffline.REPLAY_STOPPED);
                            break;
                        case "replayOff":
                            options.offline(RadarTrackingOffline.REPLAY_OFF);
                            break;
                    }
                }
                if (optionsObj.get("priority") != null) {
                    switch (optionsObj.getString("priority")) {
                        case "efficiency":
                            options.priority(RadarTrackingPriority.EFFICIENCY);
                            break;
                        case "responsiveness":
                            options.priority(RadarTrackingPriority.RESPONSIVENESS);
                            break;
                    }
                }
            }
            return options.build();
        } catch (JSONException j) {
            return null;
        }
    }

}
