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

Radar.setUserId({ userId: 'capacitor' });

Radar.setMetadata({ metadata: {
  foo: 'bar',
}});

Radar.getLocationPermissionsStatus().then(result => {
  console.log('getLocationPermissionsStatus');
  console.log(result.status);
});

Radar.requestLocationPermissions({ background: true });

Radar.startTrip({
  options: {
    externalId: 'capacitor-chicken2',
    destinationGeofenceTag: 'tiny',
    destinationGeofenceExternalId: 'chicken2',
    metadata: {
      foo: 'bar',
      baz: true
    },
    mode: 'car'
  }
});

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
  steps: 20,
  interval: 1
});

setTimeout(() => {
  Radar.cancelTrip();
}, 30000);

const App: React.FC = () => (
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

export default App;
