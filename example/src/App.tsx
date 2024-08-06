import React from 'react';
import { Redirect, Route } from 'react-router-dom';
import { IonApp, IonRouterOutlet } from '@ionic/react';
import { IonReactRouter } from '@ionic/react-router';
import Home from './pages/Home';
import { Radar, RadarGeocodeCallback } from 'capacitor-radar';
import { RadarTripOptions, RadarTrackingOptions } from 'capacitor-radar/src/definitions';
import { App as CapacitorApp } from '@capacitor/app'

/* Core CSS required for Ionic components to work properly */
import '@ionic/react/css/core.css';

/* Basic CSS for apps built with Ionic */
import '@ionic/react/css/normalize.css';
import '@ionic/react/css/structure.css';
import '@ionic/react/css/typography.css';

/* Optional CSS utils that can be commented out */
import '@ionic/react/css/padding.css';
import '@ionic/react/css/float-elements.css';
import '@ionic/react/css/text-alignment.css';
import '@ionic/react/css/text-transformation.css';
import '@ionic/react/css/flex-utils.css';
import '@ionic/react/css/display.css';

/* Theme variables */
import './theme/variables.css';

Radar.addListener('clientLocation', (result) => {
  console.log(`location: ${JSON.stringify(result)}`);
});

Radar.addListener('location', (result) => {
  console.log(`location: ${JSON.stringify(result)}`);
});

Radar.addListener('events', (result) => {
  console.log(`events: ${JSON.stringify(result)}`);
});

Radar.addListener('error', (result) => {
  console.log(`error: ${JSON.stringify(result)}`);
});

Radar.addListener('token', (result) => {
  console.log(`token: ${JSON.stringify(result)}`);
});

CapacitorApp.addListener('appStateChange', ({ isActive }) => {
  if (!isActive) {
    Radar.logResigningActive();
  }
});

// Available on @capacitor/app 4.1.0
// CapacitorApp.addListener('pause', () => {
//   Radar.logBackgrounding();
// })

// Add this to iOS callback for termination; Android calls this automatically
// Radar.logTermination();

interface AppProps {}

interface AppState {
  logs: string[]
}

class App extends React.Component<AppProps, AppState> {
  constructor(props: any) {
    super(props);
    this.state = {
      logs: []
    }
  }

  logOutput(...args: any) {
    this.setState((prevState: AppState) => ({
      logs: [...prevState.logs, args.join(" ")]
    }));
  }

  componentDidMount() {
    Radar.initialize({ publishableKey: 'prj_test_pk_26da2abbdc9c6ffc86afcbc3df0bbde4f8d4e1f8' });
    Radar.setLogLevel({level: 'debug'});
    Radar.setUserId({ userId: 'capacitor' });
    Radar.setDescription({ description: 'capacitor example'});
    Radar.setMetadata({ metadata: {
      foo: 'bar',
    }});
    Radar.setAnonymousTrackingEnabled({ enabled: false});
    
    Radar.getLocationPermissionsStatus().then((result) => {
      this.logOutput(JSON.stringify(result));
    });

    Radar.requestLocationPermissions({ background: false });
    Radar.getUserId().then((result) => {
      this.logOutput(`getUserId: ${JSON.stringify(result)}\n`)
    }).catch((error) => {
      this.logOutput(`getUserId: error ${JSON.stringify(error)}\n`);
    });
    Radar.getDescription().then((result) => {
      this.logOutput(`getDescription: ${JSON.stringify(result)}\n`)
    }).catch((error) => {
      this.logOutput(`getDescription: error ${JSON.stringify(error)}\n`);
    });
    Radar.getMetadata().then((result) => {
      this.logOutput(`getMetadata: ${JSON.stringify(result)}\n`)
    }).catch((error) => {
      this.logOutput(`getMetadata: error ${JSON.stringify(error)}\n`);
    });
    Radar.getLocation({desiredAccuracy: 'high'}).then((result) => {
      this.logOutput(`getLocation: ${JSON.stringify(result)}\n`)
    }).catch((error) => {
      this.logOutput(`getLocation: error ${JSON.stringify(error)}\n`);
    });
    Radar.trackOnce({
      desiredAccuracy: 'high',
      beacons: false
    }).then((result) => {
      this.logOutput(`trackOnce: ${JSON.stringify(result)}\n`);
    }).catch((error) => {
      this.logOutput(`trackOnce: error ${JSON.stringify(error)}\n`);
    });
    Radar.trackOnce({
      location: {
        latitude: 40,
        longitude: -73,
        accuracy: 0,
      }
    }).then((result) => {
      this.logOutput(`trackOnce with location: ${JSON.stringify(result)}\n`);
    }).catch((error) => {
      this.logOutput(`trackOnce with location: error ${JSON.stringify(error)}\n`);
    });
    Radar.isTracking().then((result) => {
      this.logOutput(`isTracking: ${JSON.stringify(result)}\n`);
    }).catch((error) => {
      this.logOutput(`isTracking: error ${JSON.stringify(error)}\n`);
    });
    Radar.getTrackingOptions().then((result: RadarTrackingOptions) => {
      this.logOutput(`getTrackingOptions: ${JSON.stringify(result)}\n`);
      Radar.startTrackingCustom({ options: result });
    }).catch((error) => {
      this.logOutput(`getTrackingOptions: error ${JSON.stringify(error)}\n`);
    });
    Radar.logConversion({
      name: "viewed_product",
      metadata: {"sku": "tshirt"}
    }).then((result) => {
      this.logOutput(`logConversion: ${JSON.stringify(result)}\n`);
    }).catch((error) => {
      this.logOutput(`logConversion: error ${JSON.stringify(error)}\n`);
    });
    Radar.logConversion({
      name: "in_app_purchase",
      revenue: 150,
      metadata: {"sku": "tshirt"}
    }).then((result) => {
      this.logOutput(`logConversion: ${JSON.stringify(result)}\n`);
    }).catch((error) => {
      this.logOutput(`logConversion: error ${JSON.stringify(error)}\n`);
    });
    // Radar.setNotificationOptions({
    //   iconString: 'icon'
    // });
    Radar.getTripOptions().then((result) => {
      this.logOutput(`getTripOptions: ${JSON.stringify(result)}\n`);
    }).catch((error) => {
      this.logOutput(`getTripOptions: error ${JSON.stringify(error)}\n`);
    });
    Radar.searchPlaces({
      near: {
        'latitude': 40.783826,
        'longitude': -73.975363,
      },
      radius: 1000,
      chains: ["starbucks"],
      chainMetadata: {
        "customFlag": "true"
      },
      limit: 10,
    }).then((result) => {
      this.logOutput(`searchPlaces: ${JSON.stringify(result)}\n`);
    }).catch((error) => {
      this.logOutput(`searchPlaces: error ${JSON.stringify(error)}\n`);
    });
    Radar.searchGeofences({
      near: {
        'latitude': 40.783826,
        'longitude': -73.975363,
      },
      radius: 1000,
      metadata: {
        "customFlag": "true"
      },
      limit: 10,
      includeGeometry: false
    }).then((result) => {
      this.logOutput(`searchGeofences: ${JSON.stringify(result)}\n`);
    }).catch((error) => {
      this.logOutput(`searchGeofences: error ${JSON.stringify(error)}\n`);
    });
    Radar.autocomplete({
      query: '841 broadway new york new york',
      near: {
        'latitude': 40.783826,
        'longitude': -73.975363,
      },
      limit: 10,
      layers: ['address', 'street'],
      country: 'US'
    }).then((result) => {
      this.logOutput(`autocomplete: ${JSON.stringify(result)}\n`);
      const address = (result && result.addresses && result.addresses[0]);

      if (address) {
        // Radar.validateAddress({address: address}).then((result) => {
        //   this.logOutput(`validateAddress: ${JSON.stringify(result)}\n`);
        // }).catch((error) => {
        //   this.logOutput(`validateAddress: error ${JSON.stringify(error)}\n`);
        // });
      }
    }).catch((error) => {
      this.logOutput(`autocomplete: error ${JSON.stringify(error)}\n`);
    });
    Radar.getMatrix({
      origins: [
        {
          'latitude': 40.78382,
          'longitude': -73.97536,
        },
        {
          'latitude': 40.70390,
          'longitude': -73.98670,
        },
      ],
      destinations: [
        {
          'latitude': 40.64189,
          'longitude': -73.78779,
        },
        {
          'latitude': 35.99801,
          'longitude': -78.94294,
        },
      ],
      mode: 'car',
      units: 'imperial',
    }).then((result) => {
      this.logOutput(`getMatrix: ${JSON.stringify(result)}\n`);
    }).catch((error) => {
      this.logOutput(`getMatrix: error ${JSON.stringify(error)}\n`);
    });
    
    Radar.trackVerified().then((result) => {
      this.logOutput(`trackVerified: ${JSON.stringify(result)}\n`);
      const { token } = result;
      if (token?.passed) {
        // allow access to feature
      } else {
        // deny access to feature, show error message
      }
    }).catch((error) => {
      this.logOutput(`trackVerified: error ${JSON.stringify(error)}\n`);
    });
    Radar.getVerifiedLocationToken().then((result) => {
      this.logOutput(`getVerifiedLocationToken: ${JSON.stringify(result)}\n`);
      const { token } = result;
      if (token) {
        // send token to server
      }
    }).catch((error) => {
      this.logOutput(`getVerifiedLocationToken: error ${JSON.stringify(error)}\n`);
    });

    Radar.isUsingRemoteTrackingOptions().then((result) => {
      this.logOutput(`isUsingRemoteTrackingOptions: ${JSON.stringify(result)}\n`);
    });

    Radar.getHost().then((result) => {
      this.logOutput(`getHost: ${JSON.stringify(result)}\n`);
    });

    Radar.getPublishableKey().then((result) => {
      this.logOutput(`getPublishableKey: ${JSON.stringify(result)}\n`);
    });

    // var stopTrackingTime = new Date()
    // stopTrackingTime.setMinutes(stopTrackingTime.getMinutes() + 1)
    // Radar.startTrackingCustom({
    //   options: {
    //     stopTrackingAfter: stopTrackingTime
    //   }
    // });

    var scheduledArrivalAt = new Date()
    scheduledArrivalAt.setMinutes(scheduledArrivalAt.getMinutes() + 9)
    /*Radar.startTrip({
      options: {
        externalId: "1601",
        destinationGeofenceTag: "foo",
        destinationGeofenceExternalId: "bamly",
        scheduledArrivalAt: scheduledArrivalAt
      }
    }).then((result) => {
      this.logOutput(`trip ${JSON.stringify(result.trip)}`)

      Radar.startTrackingResponsive()

      if (result.status == "SUCCESS") {
        Radar.startTrackingResponsive()
      } else {
        this.logOutput(`failed to start trip ${JSON.stringify(result)}`);
      }
    }).catch((error) => {
      this.logOutput(`error ${JSON.stringify(error)}`);
    });*/

    /*Radar.startTrip({
      options: {
        tripOptions: {
          externalId: "1597",
          destinationGeofenceTag: "foo",
          destinationGeofenceExternalId: "bamly",
          scheduledArrivalAt: scheduledArrivalAt
        }
      }
    }).then((result) => {
      this.logOutput(`trip ${JSON.stringify(result.trip)}`)

      if (result.status == "SUCCESS") {
        Radar.startTrackingResponsive()
      } else {
        this.logOutput(`failed to start trip ${JSON.stringify(result)}`);
      }
    }).catch((error) => {
      this.logOutput(`error ${JSON.stringify(error)}`);
    });*/

    Radar.startTrip({
      options: {
        tripOptions: {
          externalId: "299",
          destinationGeofenceTag: "store",
          destinationGeofenceExternalId: "123",
          mode: "car",
          scheduledArrivalAt: scheduledArrivalAt.toISOString(),
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
    }).then((result) => {
      this.logOutput(`trip ${JSON.stringify(result.trip)}`)

      Radar.getTripOptions().then((result: RadarTripOptions) => {
        this.logOutput(`getTripOptions: ${JSON.stringify(result)}\n`);

        Radar.updateTrip({ options: result, status: "arrived"}).then((updateTripResult) => {
          this.logOutput(`updateTrip: ${JSON.stringify(updateTripResult)}\n`);
        }).catch((error) => {
          this.logOutput(`updateTrip: error ${JSON.stringify(error)}\n`);
        });
      }).catch((error) => {
        this.logOutput(`getTripOptions: error ${JSON.stringify(error)}\n`);
      });

      if (result.status === "SUCCESS") {

      } else {
        this.logOutput(`failed to start trip ${JSON.stringify(result)}`);
      }
    }).catch((error) => {
      this.logOutput(`error ${JSON.stringify(error)}`);
    });

    Radar.geocode({ query: "20 jay st brooklyn" }).then((result: RadarGeocodeCallback) => {
      this.logOutput(`geocode: ${JSON.stringify(result.addresses)}`);
    }).catch((error) => {
      this.logOutput(`geocode: error ${error}`);
    });
    Radar.reverseGeocode({ location: {
      latitude: 40.783826,
      longitude: -73.975363,
    } }).then((result: RadarGeocodeCallback) => {
      this.logOutput(`geocode: ${JSON.stringify(result.addresses)}`);
    }).catch((error) => {
      this.logOutput(`geocode: error ${error}`);
    })

    /*
    Radar.mockTracking({
      origin: {
        latitude: 40.717122,
        longitude: -74.035504
      },
      destination: {
        latitude: 40.7120678,
        longitude: -74.0396181
      },
      mode: 'car',
      steps: 5,
      interval: 1
    });
    
    setTimeout(() => {
      Radar.cancelTrip().then((result) => {
        this.logOutput(JSON.stringify(result));
      });
    }, 30000);

    /*
    setTimeout(() => {
      Radar.stopTracking();
    }, 30000);
    */
  }

  render() {
    return (
      <IonApp>
        <IonReactRouter>
          <IonRouterOutlet>
            <Route exact path="/home">
              <Home logs={this.state.logs}/>
            </Route>
            <Route exact path="/">
              <Redirect to="/home" />
            </Route>
          </IonRouterOutlet>
        </IonReactRouter>
      </IonApp>
    );
  }
}

export default App;