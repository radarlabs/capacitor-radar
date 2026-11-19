# Migration guides

## 4.x to 5.0.0

This release updates the native SDKs to Radar Android SDK 5.0.0 and Radar iOS SDK 4.0.0.

### Breaking changes

**Android: declare the foreground service in your app.** Radar Android SDK 4.x and later does not declare `RadarForegroundService` in its manifest. Add the service and the `FOREGROUND_SERVICE_LOCATION` permission to `android/app/src/main/AndroidManifest.xml` if one of these is true:

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

**Some token and event fields can be missing.** Check for these fields before you use them:

- `RadarVerifiedLocationToken`: `user`, `events`, `token`, `expiresAt`, `expiresIn`, `passed`, and `failureReasons`.
- `RadarRevealRiskToken`: `risk`, `network`, and `device`.
- `RadarEvent`: `location` and `actualCreatedAt`. For example, conversion events from `logConversion()` do not have them.

The TypeScript types now mark these fields as optional. Change:
```javascript
const { token } = await Radar.trackVerified();
const userId = token.user.userId;
```
to:
```javascript
const { token } = await Radar.trackVerified();
const userId = token?.user?.userId;
```

**Verified tracking and reveal risk can return new response formats.** You set the response format on your Radar project. The tokens have a new `format` field:

| `format` | Response | What to do on your backend |
| --- | --- | --- |
| `jws`, or no `format` | The full legacy response. It has a signed token if you configured signing. | No change. Verify the signed token. |
| `jwe` | An encrypted token and the response metadata. | Decrypt and verify the token. |
| `id` | The evaluation `_id` only. | Get the result with a secret API key. |

- For `jwe` and `id`, the verified token does not have `user`, `events`, or `failureReasons`, and the reveal risk token does not have `risk`, `network`, or `device`.
- For `id`, the token does not have `token`, `expiresAt`, `expiresIn`, or `passed`. Use `format` to find the response type.
- Send `token` and `_id` to your backend. Do not use `passed` in the app to allow or deny access without a check on your backend.

If your project uses the legacy response, you only have to handle the optional fields.

**TypeScript type fixes.** These types now match the values that the plugin returns:

- `RadarRevealRiskToken.risk.level` is lowercase: `'none'`, `'low'`, `'medium'`, or `'high'`. Before, the type used `'High'` and other capitalized values, but the plugin always returned lowercase values. Update your comparisons.
- `RadarVerifiedLocationToken.expiresAt` is a `string` (ISO 8601). Before, the type was `Date`, but the plugin always returned a string. Use `new Date(token.expiresAt)` if you need a `Date`.
- `RadarVerifiedLocationToken` now has `warningReasons`, `nearbyGeofences`, and `meta`. `RadarRevealRiskToken` now has `meta`.

**`autocomplete()` no longer accepts `expandUnits`.** The native SDKs removed this option. Remove it from your calls.

**`validateAddress()` takes the address fields directly.** It no longer accepts `{ address }`. Pass `street` and `number`, or `addressLabel`, but not both. `city`, `stateCode`, `postalCode`, and `countryCode` are required, and `unit` is optional. If a required field is empty, `validateAddress()` returns `ERROR_BAD_REQUEST`.

Change:
```javascript
Radar.validateAddress({
  address: { number: '841', street: 'Broadway', city: 'New York', stateCode: 'NY', postalCode: '10003', countryCode: 'US' },
});
```
to:
```javascript
Radar.validateAddress({
  street: 'Broadway',
  number: '841',
  city: 'New York',
  stateCode: 'NY',
  postalCode: '10003',
  countryCode: 'US',
});

// or, with the street address on one line:
Radar.validateAddress({
  addressLabel: '841 Broadway',
  city: 'New York',
  stateCode: 'NY',
  postalCode: '10003',
  countryCode: 'US',
});
```

To validate an address from `autocomplete()` or `geocode()`, pass its `street` and `number` when both are set, and its `addressLabel` otherwise.

### Behavior changes

These come from the native SDKs. You do not have to change code for them, but your app can behave differently.

- `trackVerified()` now retries against a backup host by default when the response from the primary host does not come from Radar, for example when a proxy or captive portal returns its own page. To turn this off, use the `trackVerifiedAutoFailover` option (see [New options](#new-options)).
- On Android, `trackVerified()` now uses the `beacons` and `desiredAccuracy` values that you pass, and it no longer leaves beacon monitoring running after it completes.
- On Android, background location updates now report the correct location time.
- The SDKs can now record traces for `trackOnce()`, `trackVerified()`, and location updates from `startTracking()`. They upload traces only when Radar turns on tracing for your project.

### Other changes

- Fraud SDKs are updated to Android `io.radar:sdk-fraud` 1.3.1 and iOS `RadarSDKFraud` 1.6.0. Verified tracking and reveal risk now encrypt the full request body and need these versions. The plugin includes them.
- On iOS, `logConversion()` now sends `revenue` when you pass it as a number. Before, iOS dropped a number `revenue`, and the conversion had no revenue.
- Radar Android SDK 5.x requires Android API 23 or later and Android Gradle Plugin 7.0 or later. Capacitor 8 apps already meet these requirements.

### New options

Pass these in the `options` of `initialize()`:

- `trackVerifiedAutoFailover` (default `true`): set it to `false` to turn off the `trackVerified()` failover described in [Behavior changes](#behavior-changes).
- `fraud` (Android only, default `false`): turns on additional fraud detection signals for location verification. On iOS, fraud detection turns on when `RadarSDKFraud` is linked.

On Android, `initialize()` from JS applies its options again, and an option that you do not pass goes back to its default. If you set `fraud = true` or `trackVerifiedAutoFailover = false` in `MainActivity`, also pass the same value to the JS `initialize()`.

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
