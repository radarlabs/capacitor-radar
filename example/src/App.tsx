import React, { useEffect, useState } from 'react';
import { Redirect, Route } from 'react-router-dom';
import { IonApp, IonRouterOutlet } from '@ionic/react';
import { IonReactRouter } from '@ionic/react-router';
import Home from './pages/Home';
import { Radar } from 'capacitor-radar';
import { App as CapacitorApp } from '@capacitor/app'
import { StatusBar, Style } from '@capacitor/status-bar';

import '@ionic/react/css/core.css';
import '@ionic/react/css/normalize.css';
import '@ionic/react/css/structure.css';
import '@ionic/react/css/typography.css';
import '@ionic/react/css/padding.css';
import '@ionic/react/css/float-elements.css';
import '@ionic/react/css/text-alignment.css';
import '@ionic/react/css/text-transformation.css';
import '@ionic/react/css/flex-utils.css';
import '@ionic/react/css/display.css';

import './theme/variables.css';

const stringify = (obj: any) => JSON.stringify(obj, null, 2);

const App: React.FC = () => {
  const [displayText, setDisplayText] = useState<string>('');

  useEffect(() => {
    StatusBar.setOverlaysWebView({ overlay: false });
    
    Radar.initialize({'publishableKey': 'prj_test_pk_000000000000000000000000000000000000'});
    Radar.setLogLevel({ level: 'debug' });
    Radar.setUserId({ userId: 'capacitor_test'});
    Radar.setDescription({ description: 'capacitor example' });
    Radar.setMetadata({ metadata: { foo: 'bar'} });
    Radar.setProduct({ product: 'test-product' });
    Radar.setAnonymousTrackingEnabled( { enabled: false });

    Radar.addListener('clientLocation', (result) => {
      console.log('clientLocation', stringify(result));
    });
    Radar.addListener('location', (result) => {
      console.log('location', stringify(result));
    });
    Radar.addListener('events', (result) => {
      console.log('events', stringify(result));
    });
    Radar.addListener('error', (result) => {
      console.log('error', stringify(result));
    });
    Radar.addListener('token', (result) => {
      console.log('token', stringify(result));
    });
    Radar.addListener('inAppMessage', (result) => {
      console.log('inAppMessage', stringify(result));
    });
    Radar.addListener('inAppMessageDismissed', (result) => {
      console.log('inAppMessageDismissed', stringify(result));
    });
    Radar.addListener('inAppMessageButtonClicked', (result) => {
      console.log('inAppMessageButtonClicked', stringify(result));
    });

    CapacitorApp.addListener('appStateChange', ({ isActive }) => {
      if (!isActive) {
        Radar.logResigningActive();
      }
    });

    Radar.requestLocationPermissions({ background: false});
  }, []);

  return (
    <IonApp>
      <IonReactRouter>
        <IonRouterOutlet>
          <Route exact path="/home">
            <Home displayText={displayText} setDisplayText={setDisplayText} />
          </Route>
          <Route exact path="/">
            <Redirect to="/home" />
          </Route>
        </IonRouterOutlet>
      </IonReactRouter>
    </IonApp>
  )
};

export default App;