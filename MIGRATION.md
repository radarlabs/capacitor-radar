# Migration guides

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