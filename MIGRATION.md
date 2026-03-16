# Migration guides

## 3.x to 4.0.0

### Breaking changes

**Capacitor 8 required.** This release upgrades from Capacitor 6 to Capacitor 8. You must upgrade your app to Capacitor 8 before using this version. See the [Capacitor 8 upgrade guide](https://capacitorjs.com/docs/updating/8-0).

**Node.js >=22 required.** The Capacitor 8 CLI requires Node.js 22 or later.

**Java 21 required.** Android builds now require JDK 21 (previously JDK 17).

**iOS minimum deployment target raised to 15.0** (previously 12.0).

**Android minSdkVersion raised to 24** (previously 21).

**`addListener` return type changed.** All `addListener` methods now return `Promise<PluginListenerHandle>` instead of `Promise<PluginListenerHandle> & PluginListenerHandle`. If you were using the synchronous handle pattern, switch to awaiting the promise:

Change:
```javascript
const handle = Radar.addListener('events', callback);
handle.remove(); // synchronous — no longer works
```
to:
```javascript
const handle = await Radar.addListener('events', callback);
handle.remove();
```

### New features

- **Tags management:** `setTags()`, `getTags()`, `addTags()`, `removeTags()`
- **Product:** `setProduct()`, `getProduct()`
- **In-app messaging:** `showInAppMessage()`, `loadImage()`, and listeners for `inAppMessage`, `inAppMessageDismissed`, `inAppMessageButtonClicked`
- **Initialize options:** `nativeSetup()`, `initialize()` now accepts an `options` parameter with `silentPush`, `autoLogNotificationConversions`, `autoHandleNotificationDeepLinks`
- **Motion permissions:** `requestMotionActivityPermission()`
- **Verified location extras:** `isTrackingVerified()`, `clearVerifiedLocationToken()`, `setExpectedJurisdiction()`, `startTrackingVerified()`, `stopTrackingVerified()`
- **Push notifications:** `setPushNotificationToken()`, `didReceivePushNotificationPayload()`
- **Other:** `initializeWithAppGroup()`, `setAppGroup()`, `setLocationExtensionToken()`, `stringForActivityType()`

### Android note

If you encounter a `desugar_jdk_libs` version error, ensure your app-level `build.gradle` uses version 2.1.5 or later:

```gradle
dependencies {
    coreLibraryDesugaring 'com.android.tools:desugar_jdk_libs:2.1.5'
}
```

## 3.7.1 to 3.7.2

- `Radar.getTripOptions()` and `Radar.getTrackingOptions` return typed `RadarTripOptions` and `RadarTrackingOptions` respectively rather than nesting within an `options` key.

Change
```
Radar.getTripOptions().then((result) => {
  Radar.updateTrip({ options: result.options, ...})
});

Radar.getTrackingOptions().then((result) => {
  Radar.starTrackingCustom({ options: result.options, ...})
});
```
to 
```
Radar.getTripOptions().then((result: RadarTripOptions) => {
  Radar.updateTrip({ options: result, ...})
});

Radar.getTrackingOptions().then((result: RadarTrackingOptions) => {
  Radar.startTrackingCustom({ options: result, ...})
});
```

## 3.5.0 to 3.5.2

- Exposes a new `Radar.startTrip()` function that accepts `trackingOptions` that apply while on a trip.

```javascript

// 3.5.2 (new)

Radar.startTrip({
  options: {
    tripOptions: {
      externalId: "1600",
      destinationGeofenceTag: "store",
      destinationGeofenceExternalId: "12345"
    },
    trackingOptions: {
      "desiredStoppedUpdateInterval": 30,
      "fastestStoppedUpdateInterval": 30,
      "desiredMovingUpdateInterval": 30,
      "fastestMovingUpdateInterval": 30,
      "desiredSyncInterval": 20,
      "desiredAccuracy": "high",
      "stopDuration": 0,
      "stopDistance": 0,
      "replay": "none",
      "sync": "all",
      "showBlueBar": true,
      "useStoppedGeofence": false,
      "stoppedGeofenceRadius": 0,
      "useMovingGeofence": false,
      "movingGeofenceRadius": 0,
      "syncGeofences": false,
      "syncGeofencesLimit": 0,
      "beacons": false,
      "foregroundServiceEnabled": false
    }
  }
});

// 3.5.0 (old)

Radar.startTrip({
  options: {
    externalId: "1600",
    destinationGeofenceTag: "store",
    destinationGeofenceExternalId: "12345"
  }
});

Radar.startTrackingContinuous();
