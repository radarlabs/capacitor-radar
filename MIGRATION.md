# Migration guides

## 4.x to 5.0.0

This release updates the native SDKs to Radar Android SDK 4.3.0 and Radar iOS SDK 3.46.0.

### Breaking changes

**Android: declare the foreground service in your app.** Radar Android SDK 4.x does not declare `RadarForegroundService` in its manifest. Add the service and the `FOREGROUND_SERVICE_LOCATION` permission to `android/app/src/main/AndroidManifest.xml` if one of these is true:

- You use continuous tracking.
- You set `foregroundServiceEnabled: true` in your tracking options.
- Your remote tracking options enable the foreground service.

If you do not declare the service, the SDK logs an error and tracks without a foreground service.

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_LOCATION" />
    <application>
        <service
            android:name="io.radar.sdk.RadarForegroundService"
            android:foregroundServiceType="location"
            tools:targetApi="26" />
    </application>
</manifest>
```

**`autocomplete()` no longer accepts `expandUnits`.** The native SDKs removed this option. Remove it from your calls.

### Behavior changes

These come from the native SDKs. You do not have to change code for them, but your app can behave differently.

- `trackVerified()` now retries against a backup host by default when the primary host does not respond. To turn this off, pass `trackVerifiedAutoFailover: false` in the `options` of `initialize()`.
- On Android, `trackVerified()` now uses the `beacons` and `desiredAccuracy` values that you pass, and it no longer leaves beacon monitoring running after it completes.
- The SDKs can now record traces for `trackOnce()`, `trackVerified()`, and location updates from `startTracking()`. They upload traces only when Radar turns on tracing for your project.
- `validateAddress()` now sends the street and number when both are set, and the address label otherwise. Addresses from `autocomplete()` and `geocode()` no longer return `ERROR_BAD_REQUEST`.

### Other changes

- Fraud SDKs are updated to Android `io.radar:sdk-fraud` 1.3.1 and iOS `RadarSDKFraud` 1.6.0. Verified tracking and reveal risk now encrypt the full request body and need these versions. The plugin includes them.
- Radar Android SDK 4.x requires Android API 23 or later and Android Gradle Plugin 7.0 or later. Capacitor 8 apps already meet these requirements.

See the native migration guides for more details: [Android](https://github.com/radarlabs/radar-sdk-android/blob/master/MIGRATION.md) and [iOS](https://github.com/radarlabs/radar-sdk-ios/blob/master/MIGRATION.md).

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
