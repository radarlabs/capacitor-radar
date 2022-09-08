import React from 'react';
import { Redirect, Route } from 'react-router-dom';
import { IonApp, IonRouterOutlet } from '@ionic/react';
import { IonReactRouter } from '@ionic/react-router';
import Home from './pages/Home';
import { Radar } from 'capacitor-radar';

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
  alert(`location: ${JSON.stringify(result)}`);
});

Radar.addListener('location', (result) => {
  alert(`location: ${JSON.stringify(result)}`);
});

Radar.addListener('events', (result) => {
  alert(`events: ${JSON.stringify(result)}`);
});

Radar.addListener('error', (result) => {
  alert(`error: ${JSON.stringify(result)}`);
});

class App extends React.Component {
  componentDidMount() {
    Radar.setUserId({ userId: 'capacitor' });

    Radar.setMetadata({ metadata: {
      foo: 'bar',
    }});

    Radar.getLocationPermissionsStatus().then((result) => {
      alert(JSON.stringify(result));
    });

    Radar.requestLocationPermissions({ background: false });

    Radar.sendEvent({ customType: 'capacitorCustomEvent' })
    .then((callback) => {
      alert(`Received events ${callback.events}`);
  
      // Calling sendEvent() twice in a row triggers a rate-limit error,
      // so don't send the second one until the first one is finished.
      Radar.sendEvent({
        customType: 'capacitorCustomEventWithLocation',
        location: { latitude: 42.0585, longitude: -87.6834 }
      })
      .then((callback) => {
        alert(`Received events ${callback.events}`);
      })
      .catch((reason) => {
        alert('Failed to send a custom event with a specific location')
      });
    })
    .catch((reason) => {
      alert('Failed to send a custom event with no location')
    });

    Radar.setForegroundServiceOptions({
      options: {
        title: 'Foreground service title',
        text: 'Foreground service text'
      }
    });

    Radar.startTrackingContinuous();

    Radar.trackOnce().then((result) => {
      alert(JSON.stringify(result));
    });

    /*
    Radar.startTrip({
      options: {
        externalId: '299',
        destinationGeofenceTag: 'store',
        destinationGeofenceExternalId: '123',
        metadata: {
          foo: 'bar',
          baz: true
        },
        mode: 'car'
      }
    });
    */

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
        alert(JSON.stringify(result));
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
              <Home />
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
