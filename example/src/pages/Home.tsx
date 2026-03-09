import React, { useRef, useEffect } from 'react';
import {
  IonContent,
  IonHeader,
  IonPage,
  IonTitle,
  IonToolbar,
  IonButton,
  IonList,
  IonListHeader,
  IonLabel,
} from '@ionic/react';
import { Radar } from 'capacitor-radar';

const stringify = (obj: any) => JSON.stringify(obj, null, 2);

interface HomeProps {
  displayText: string;
  setDisplayText: (text: string) => void;
}

const Home: React.FC<HomeProps> = ({ displayText, setDisplayText }) => {
  const outputRef = useRef<HTMLPreElement>(null);

  useEffect(() => {
    if (outputRef.current) {
      outputRef.current.scrollTop = outputRef.current.scrollHeight;
    }
  }, [displayText]);

  const getUserId = async () => {
    try {
      const result = await Radar.getUserId();
      setDisplayText('getUserId: ' + stringify(result));
    } catch (err) {
      setDisplayText('getUserId error: ' + err);
    }
  };

  const getDescription = async () => {
    try {
      const result = await Radar.getDescription();
      setDisplayText('getDescription: ' + stringify(result));
    } catch (err) {
      setDisplayText('getDescription error: ' + err);
    }
  };

  const getMetadata = async () => {
    try {
      const result = await Radar.getMetadata();
      setDisplayText('getMetadata: ' + stringify(result));
    } catch (err) {
      setDisplayText('getMetadata error: ' + err);
    }
  };

  const setTags = async () => {
    try {
      await Radar.setTags({ tags: ['tag1', 'tag2'] });
      setDisplayText('setTags: success');
    } catch (err) {
      setDisplayText('setTags error: ' + err);
    }
  };

  const getTags = async () => {
    try {
      const result = await Radar.getTags();
      setDisplayText('getTags: ' + stringify(result));
    } catch (err) {
      setDisplayText('getTags error: ' + err);
    }
  };

  const addTags = async () => {
    try {
      await Radar.addTags({ tags: ['tag3', 'tag4'] });
      setDisplayText('addTags: success');
    } catch (err) {
      setDisplayText('addTags error: ' + err);
    }
  };

  const removeTags = async () => {
    try {
      await Radar.removeTags({ tags: ['tag1', 'tag2'] });
      setDisplayText('removeTags: success');
    } catch (err) {
      setDisplayText('removeTags error: ' + err);
    }
  };

  const getProduct = async () => {
    try {
      const result = await Radar.getProduct();
      setDisplayText('getProduct: ' + stringify(result));
    } catch (err) {
      setDisplayText('getProduct error: ' + err);
    }
  };

  const getLocationPermissionsStatus = async () => {
    try {
      const result = await Radar.getLocationPermissionsStatus();
      setDisplayText('getLocationPermissionsStatus: ' + stringify(result));
    } catch (err) {
      setDisplayText('getLocationPermissionsStatus error: ' + err);
    }
  };

  const requestPermissionsForeground = () => {
    Radar.requestLocationPermissions({ background: false });
    setDisplayText('requestLocationPermissions (foreground) called');
  };

  const requestPermissionsBackground = () => {
    Radar.requestLocationPermissions({ background: true });
    setDisplayText('requestLocationPermissions (background) called');
  };

  const requestMotionActivityPermission = () => {
    Radar.requestMotionActivityPermission();
    setDisplayText('requestMotionActivityPermission called');
  };

  const getLocation = async () => {
    try {
      const result = await Radar.getLocation({ desiredAccuracy: 'high' });
      setDisplayText('getLocation: ' + stringify(result));
    } catch (err) {
      setDisplayText('getLocation error: ' + err);
    }
  };

  const trackOnce = async () => {
    try {
      const result = await Radar.trackOnce();
      setDisplayText('trackOnce: ' + stringify(result));
    } catch (err) {
      setDisplayText('trackOnce error: ' + err);
    }
  };

  const trackOnceWithOptions = async () => {
    try {
      const result = await Radar.trackOnce({ desiredAccuracy: 'high', beacons: true });
      setDisplayText('trackOnce (with options): ' + stringify(result));
    } catch (err) {
      setDisplayText('trackOnce (with options) error: ' + err);
    }
  };

  const trackVerified = async () => {
    try {
      const result = await Radar.trackVerified({
        beacons: true,
        desiredAccuracy: 'high',
        reason: 'test reason',
        transactionId: '123'
      });
      setDisplayText('trackVerified: ' + stringify(result));
    } catch (err) {
      setDisplayText('trackVerified error: ' + err);
    }
  };

  const getVerifiedLocationToken = async () => {
    try {
      const result = await Radar.getVerifiedLocationToken();
      setDisplayText('getVerifiedLocationToken: ' + stringify(result));
    } catch (err) {
      setDisplayText('getVerifiedLocationToken error: ' + err);
    }
  };

  const clearVerifiedLocationToken = () => {
    Radar.clearVerifiedLocationToken();
    setDisplayText('clearVerifiedLocationToken called');
  };

  const startTrackingVerified = () => {
    Radar.startTrackingVerified({  interval: 100, beacons: true });
    setDisplayText('startTrackingVerified called');
  };

  const isTrackingVerified = async () => {
    try {
      const result = await Radar.isTrackingVerified();
      setDisplayText('isTrackingVerified: ' + stringify(result));
    } catch (err) {
      setDisplayText('isTrackingVerified error: ' + err);
    }
  };

  const stopTrackingVerified = () => {
    Radar.stopTrackingVerified();
    setDisplayText('stopTrackingVerified called');
  };

  const setExpectedJurisdiction = () => {
    Radar.setExpectedJurisdiction({ countryCode: 'US', stateCode: 'NY' });
    setDisplayText('setExpectedJurisdiction called');
  };

  const startTrackingEfficient = () => {
    Radar.startTrackingEfficient();
    setDisplayText('startTrackingEfficient called');
  };

  const startTrackingResponsive = () => {
    Radar.startTrackingResponsive();
    setDisplayText('startTrackingResponsive called');
  };
  
  const startTrackingContinuous = () => {
    Radar.startTrackingContinuous();
    setDisplayText('startTrackingContinuous called');
  };

  const startTrackingCustom = () => {
    Radar.startTrackingCustom({
      options: {
        desiredStoppedUpdateInterval: 30,
        fastestStoppedUpdateInterval: 30,
        desiredMovingUpdateInterval: 30,
        fastestMovingUpdateInterval: 30,
        desiredSyncInterval: 20,
        desiredAccuracy: 'high',
        stopDuration: 0,
        stopDistance: 0,
        replay: 'none',
        sync: 'all',
        showBlueBar: true,
        useStoppedGeofence: false,
        stoppedGeofenceRadius: 0,
        useMovingGeofence: false,
        movingGeofenceRadius: 0,
        syncGeofences: false,
        syncGeofencesLimit: 0,
        beacons: false,
        foregroundServiceEnabled: false,
      },
    });
    setDisplayText('startTrackingCustom called');
  };

  const mockTracking = () => {
    Radar.mockTracking({
      origin: { latitude: 40.78382, longitude: -73.97536 },
      destination: { latitude: 40.7039, longitude: -73.9867 },
      mode: 'car',
      steps: 5,
      interval: 1,
    });
    setDisplayText('mockTracking called');
  };

  const stopTracking = () => {
    Radar.stopTracking();
    setDisplayText('stopTracking called');
  };

  const getTrackingOptions = async () => {
    try {
      const result = await Radar.getTrackingOptions();
      setDisplayText('getTrackingOptions: ' + stringify(result));
    } catch (err) {
      setDisplayText('getTrackingOptions error: ' + err);
    }
  };

  const isTracking = async () => {
    try {
      const result = await Radar.isTracking();
      setDisplayText('isTracking: ' + stringify(result));
    } catch (err) {
      setDisplayText('isTracking error: ' + err);
    }
  };

  const isUsingRemoteTrackingOptions = async () => {
    try {
      const result = await Radar.isUsingRemoteTrackingOptions();
      setDisplayText('isUsingRemoteTrackingOptions: ' + stringify(result));
    } catch (err) {
      setDisplayText('isUsingRemoteTrackingOptions error: ' + err);
    }
  };

  const setForegroundServiceOptions = () => {
    Radar.setForegroundServiceOptions({
      options: {
        text: 'Tracking in progress',
        title: 'Radar',
        iconString: 'icon',
        iconColor: '#FF0000',
        updatesOnly: true,
        activity: 'io.ionic.starter.MainActivity',
        importance: 2,
        id: 123,
        channelName: 'Radar Foreground Service',
      },
    });
    setDisplayText('setForegroundServiceOptions called');
  };

  const setNotificationOptions = () => {
    Radar.setNotificationOptions({
      iconString: 'icon',
      iconColor: '#FF0000',
      foregroundServiceIconString: 'icon',
      foregroundServiceIconColor: '#00FF00',
      eventIconString: 'icon',
      eventIconColor: '#0000FF',
    });
    setDisplayText('setNotificationOptions called');
  };


  const startTrip = async () => {
    try {
      const result = await Radar.startTrip({
        options: {
          externalId: '300',
          destinationGeofenceTag: 'store',
          destinationGeofenceExternalId: '123',
          mode: 'car',
          scheduledArrivalAt: new Date(Date.now() + 9 * 60000),
        },
      });
      setDisplayText('startTrip: ' + stringify(result));
    } catch (err) {
      setDisplayText('startTrip error: ' + err);
    }
  };

  const startTripWithTrackingOptions = async () => {
    try {
      const result = await Radar.startTrip({
        options: {
          tripOptions: {
            externalId: '302',
            destinationGeofenceTag: 'store',
            destinationGeofenceExternalId: '123',
            mode: 'car',
            scheduledArrivalAt: new Date(Date.now() + 9 * 60000),
          },
          trackingOptions: {
            desiredStoppedUpdateInterval: 30,
            fastestStoppedUpdateInterval: 30,
            desiredMovingUpdateInterval: 30,
            fastestMovingUpdateInterval: 30,
            desiredSyncInterval: 20,
            desiredAccuracy: 'high',
            stopDuration: 0,
            stopDistance: 0,
            replay: 'none',
            sync: 'all',
            showBlueBar: true,
            useStoppedGeofence: false,
            stoppedGeofenceRadius: 0,
            useMovingGeofence: false,
            movingGeofenceRadius: 0,
            syncGeofences: false,
            syncGeofencesLimit: 0,
            beacons: false,
            foregroundServiceEnabled: false,
          },
        },
      });
      setDisplayText('startTrip (with tracking options): ' + stringify(result));
    } catch (err) {
      setDisplayText('startTrip (with tracking options) error: ' + err);
    }
  };

  const getTripOptions = async () => {
    try {
      const result = await Radar.getTripOptions();
      setDisplayText('getTripOptions: ' + stringify(result));
    } catch (err) {
      setDisplayText('getTripOptions error: ' + err);
    }
  };

  const updateTrip = async () => {
    try {
      const result = await Radar.updateTrip({
        options: {
          externalId: '300',
          destinationGeofenceTag: 'store',
          destinationGeofenceExternalId: '123',
          mode: 'car',
        },
        status: 'approaching',
      });
      setDisplayText('updateTrip: ' + stringify(result));
    } catch (err) {
      setDisplayText('updateTrip error: ' + err);
    }
  };

  const completeTrip = async () => {
    try {
      const result = await Radar.completeTrip();
      setDisplayText('completeTrip: ' + stringify(result));
    } catch (err) {
      setDisplayText('completeTrip error: ' + err);
    }
  };

  const cancelTrip = async () => {
    try {
      const result = await Radar.cancelTrip();
      setDisplayText('cancelTrip: ' + stringify(result));
    } catch (err) {
      setDisplayText('cancelTrip error: ' + err);
    }
  };

  const acceptEvent = () => {
    Radar.acceptEvent({ eventId: 'event-001', verifiedPlaceId: 'place-001' });
    setDisplayText('acceptEvent called');
  };

  const rejectEvent = () => {
    Radar.rejectEvent({ eventId: 'event-001' });
    setDisplayText('rejectEvent called');
  };

  const getContext = async () => {
    try {
      const result = await Radar.getContext({ latitude: 40.78382, longitude: -73.97536 });
      setDisplayText('getContext: ' + stringify(result));
    } catch (err) {
      setDisplayText('getContext error: ' + err);
    }
  };

  const searchPlaces = async () => {
    try {
      const result = await Radar.searchPlaces({
        near: { latitude: 40.783826, longitude: -73.975363 },
        radius: 1000,
        chains: ['starbucks'],
        chainMetadata: { customFlag: 'true' },
        limit: 10,
      });
      setDisplayText('searchPlaces: ' + stringify(result));
    } catch (err) {
      setDisplayText('searchPlaces error: ' + err);
    }
  };

  const searchGeofences = async () => {
    try {
      const result = await Radar.searchGeofences({
        radius: 1000,
        tags: ['venue'],
        limit: 10,
        includeGeometry: true,
      });
      setDisplayText('searchGeofences: ' + stringify(result));
    } catch (err) {
      setDisplayText('searchGeofences error: ' + err);
    }
  };

  const autocomplete = async () => {
    try {
      const result = await Radar.autocomplete({
        query: 'brooklyn roasting',
        near: { latitude: 40.7342, longitude: -73.9911 },
        layers: ['locality'],
        limit: 10,
        country: 'US',
      });
      setDisplayText('autocomplete: ' + stringify(result));
    } catch (err) {
      setDisplayText('autocomplete error: ' + err);
    }
  };

  const geocode = async () => {
    try {
      const result = await Radar.geocode({ query: '40.78382,-73.97536' });
      setDisplayText('geocode: ' + stringify(result));
    } catch (err) {
      setDisplayText('geocode error: ' + err);
    }
  };

  const reverseGeocode = async () => {
    try {
        const result = await Radar.reverseGeocode({
        location: { latitude: 40.783826, longitude: -73.975363 },
      });
      setDisplayText('reverseGeocode: ' + stringify(result));
    } catch (err) {
      setDisplayText('reverseGeocode error: ' + err);
    }
  };

  const ipGeocode = async () => {
    try {
      const result = await Radar.ipGeocode();
      setDisplayText('ipGeocode: ' + stringify(result));
    } catch (err) {
      setDisplayText('ipGeocode error: ' + err);
    }
  };

  const validateAddress = async () => {
    try {
      const result = await Radar.validateAddress({
        address: {
          latitude: 0,
          longitude: 0,
          city: 'New York',
          stateCode: 'NY',
          postalCode: '10003',
          countryCode: 'US',
          number: '841',
        },
      });
      setDisplayText('validateAddress: ' + stringify(result));
    } catch (err) {
      setDisplayText('validateAddress error: ' + err);
    }
  };

  const getDistance = async () => {
    try {
      const result = await Radar.getDistance({
        origin: { latitude: 40.78382, longitude: -73.97536 },
        destination: { latitude: 40.7039, longitude: -73.9867 },
        modes: ['foot', 'car'],
        units: 'imperial',
      });
      setDisplayText('getDistance: ' + stringify(result));
    } catch (err) {
      setDisplayText('getDistance error: ' + err);
    }
  };

  const getMatrix = async () => {
    try {
      const result = await Radar.getMatrix({
        origins: [
          { latitude: 40.78382, longitude: -73.97536 },
          { latitude: 40.7039, longitude: -73.9867 },
        ],
        destinations: [
          { latitude: 40.64189, longitude: -73.78779 },
          { latitude: 35.99801, longitude: -78.94294 },
        ],
        mode: 'car',
        units: 'imperial',
      });
      setDisplayText('getMatrix: ' + stringify(result));
    } catch (err) {
      setDisplayText('getMatrix error: ' + err);
    }
  };

  const logConversion = async () => {
    try {
      const result = await Radar.logConversion({
        name: 'in_app_purchase',
        metadata: { sku: '123456789' },
      });
      setDisplayText('logConversion: ' + stringify(result));
    } catch (err) {
      setDisplayText('logConversion error: ' + err);
    }
  };

  const logConversionWithRevenue = async () => {
    try {
      const result = await Radar.logConversion({
        name: 'in_app_purchase',
        revenue: 150,
        metadata: { sku: '123456789' },
      });
      setDisplayText('logConversion (with revenue): ' + stringify(result));
    } catch (err) {
      setDisplayText('logConversion (with revenue) error: ' + err);
    }
  };

  const startIndoorScan = async () => {
    try {
      const result = await Radar.startIndoorScan({
        geofenceId: 'some-geofence-id',
        scanLengthSeconds: 10,
      });
      setDisplayText('startIndoorScan: ' + stringify(result));
    } catch (err) {
      setDisplayText('startIndoorScan error: ' + err);
    }
  };

  const getHost = async () => {
    try {
      const result = await Radar.getHost();
      setDisplayText('getHost: ' + stringify(result));
    } catch (err) {
      setDisplayText('getHost error: ' + err);
    }
  };

  const getPublishableKey = async () => {
    try {
      const result = await Radar.getPublishableKey();
      setDisplayText('getPublishableKey: ' + stringify(result));
    } catch (err) {
      setDisplayText('getPublishableKey error: ' + err);
    }
  };

const showTestInAppMessage = () => {
  Radar.showInAppMessage({
    message: {
      title: {
        text: 'This is the title',
        color: '#ff0000',
      },
      body: {
        text: 'This is a demo message.',
        color: '#00ff00',
      },
      button: {
        text: 'Buy it',
        color: '#0000ff',
        backgroundColor: '#EB0083',
      },
      image: {
        url: 'https://images.pexels.com/photos/949587/pexels-photo-949587.jpeg',
        name: 'image.jpeg',
      },
      metadata: {
        campaignId: '1234',
      },
    },
  });
  setDisplayText('showInAppMessage called');
};


  const runAll = async () => {
    const fns = [
      getUserId, getDescription, getMetadata,
      setTags, getTags, addTags, removeTags, getProduct,
      getLocationPermissionsStatus,
      getLocation, trackOnce,
      trackVerified, getVerifiedLocationToken, isTrackingVerified,
      startTrackingCustom, isTracking, getTrackingOptions,
      isUsingRemoteTrackingOptions, stopTracking,
      startTrip, getTripOptions, updateTrip, completeTrip, cancelTrip,
      getContext, searchPlaces, searchGeofences,
      autocomplete, geocode, reverseGeocode, ipGeocode,
      validateAddress, getDistance, getMatrix,
      logConversion, logConversionWithRevenue,
      getHost, getPublishableKey
    ];
    for (const fn of fns) {
      try {
        await fn();
      } catch (err) {
        setDisplayText(fn.name + ' error: ' + err);
      }
      await new Promise((r) => setTimeout(r, 500));
    }
  };

  return (
    <IonPage>
      <IonHeader>
        <IonToolbar>
          <IonTitle>Radar Capacitor Example</IonTitle>
        </IonToolbar>
      </IonHeader>
      <IonContent fullscreen>
        <pre
          ref={outputRef}
          style={{
            background: '#1e1e1e',
            color: '#d4d4d4',
            fontFamily: "'Courier New', monospace",
            fontSize: '13px',
            padding: '12px',
            borderRadius: '8px',
            margin: '12px',
            height: '200px',
            overflowY: 'auto',
            whiteSpace: 'pre-wrap',
            wordBreak: 'break-all',
          }}
        >
          {displayText || 'Tap a button to test...'}
        </pre>
        <IonList style={{ paddingBottom: '40px' }}>
          <IonListHeader>
            <IonLabel>Run All</IonLabel>
          </IonListHeader>
          <IonButton expand="block" style={{ margin: '4px 12px' }} onClick={runAll}>
            Run All
          </IonButton>
          <IonListHeader>
            <IonLabel>User &amp; Identity</IonLabel>
          </IonListHeader>
          <IonButton expand="block" style={{ margin: '6px 12px' }} onClick={getUserId}>getUserId</IonButton>
          <IonButton expand="block" style={{ margin: '6px 12px' }} onClick={getDescription}>getDescription</IonButton>
          <IonButton expand="block" style={{ margin: '6px 12px' }} onClick={getMetadata}>getMetadata</IonButton>
          <IonButton expand="block" style={{ margin: '6px 12px' }} onClick={setTags}>setTags</IonButton>
          <IonButton expand="block" style={{ margin: '6px 12px' }} onClick={getTags}>getTags</IonButton>
          <IonButton expand="block" style={{ margin: '6px 12px' }} onClick={addTags}>addTags</IonButton>
          <IonButton expand="block" style={{ margin: '6px 12px' }} onClick={removeTags}>removeTags</IonButton>
          <IonButton expand="block" style={{ margin: '6px 12px' }} onClick={getProduct}>getProduct</IonButton>
          <IonListHeader>
            <IonLabel>Permissions</IonLabel>
          </IonListHeader>
          <IonButton expand="block" style={{ margin: '6px 12px' }} onClick={getLocationPermissionsStatus}>getLocationPermissionsStatus</IonButton>
          <IonButton expand="block" style={{ margin: '6px 12px' }} onClick={requestPermissionsForeground}>requestPermissions (foreground)</IonButton>
          <IonButton expand="block" style={{ margin: '6px 12px' }} onClick={requestPermissionsBackground}>requestPermissions (background)</IonButton>
          <IonButton expand="block" style={{ margin: '6px 12px' }} onClick={requestMotionActivityPermission}>requestMotionActivityPermission</IonButton>
          <IonListHeader>
            <IonLabel>Location</IonLabel>
          </IonListHeader>
          <IonButton expand="block" style={{ margin: '6px 12px' }} onClick={getLocation}>getLocation</IonButton>
          <IonButton expand="block" style={{ margin: '6px 12px' }} onClick={trackOnce}>trackOnce</IonButton>
          <IonButton expand="block" style={{ margin: '6px 12px' }} onClick={trackOnceWithOptions}>trackOnce (with options)</IonButton>
          <IonListHeader>
            <IonLabel>Verified Location</IonLabel>
          </IonListHeader>
          <IonButton expand="block" style={{ margin: '6px 12px' }} onClick={trackVerified}>trackVerified</IonButton>
          <IonButton expand="block" style={{ margin: '6px 12px' }} onClick={getVerifiedLocationToken}>getVerifiedLocationToken</IonButton>
          <IonButton expand="block" style={{ margin: '6px 12px' }} onClick={clearVerifiedLocationToken}>clearVerifiedLocationToken</IonButton>
          <IonButton expand="block" style={{ margin: '6px 12px' }} onClick={startTrackingVerified}>startTrackingVerified</IonButton>
          <IonButton expand="block" style={{ margin: '6px 12px' }} onClick={isTrackingVerified}>isTrackingVerified</IonButton>
          <IonButton expand="block" style={{ margin: '6px 12px' }} onClick={stopTrackingVerified}>stopTrackingVerified</IonButton>
          <IonButton expand="block" style={{ margin: '6px 12px' }} onClick={setExpectedJurisdiction}>setExpectedJurisdiction</IonButton>
          <IonListHeader>
            <IonLabel>Tracking</IonLabel>
          </IonListHeader>
          <IonButton expand="block" style={{ margin: '6px 12px' }} onClick={startTrackingEfficient}>startTrackingEfficient</IonButton>
          <IonButton expand="block" style={{ margin: '6px 12px' }} onClick={startTrackingResponsive}>startTrackingResponsive</IonButton>
          <IonButton expand="block" style={{ margin: '6px 12px' }} onClick={startTrackingContinuous}>startTrackingContinuous</IonButton>
          <IonButton expand="block" style={{ margin: '6px 12px' }} onClick={startTrackingCustom}>startTrackingCustom</IonButton>
          <IonButton expand="block" style={{ margin: '6px 12px' }} onClick={mockTracking}>mockTracking</IonButton>
          <IonButton expand="block" style={{ margin: '6px 12px' }} onClick={stopTracking}>stopTracking</IonButton>
          <IonButton expand="block" style={{ margin: '6px 12px' }} onClick={getTrackingOptions}>getTrackingOptions</IonButton>
          <IonButton expand="block" style={{ margin: '6px 12px' }} onClick={isTracking}>isTracking</IonButton>
          <IonButton expand="block" style={{ margin: '6px 12px' }} onClick={isUsingRemoteTrackingOptions}>isUsingRemoteTrackingOptions</IonButton>
          <IonListHeader>
            <IonLabel>Android Service Options</IonLabel>
          </IonListHeader>
          <IonButton expand="block" style={{ margin: '6px 12px' }} onClick={setForegroundServiceOptions}>setForegroundServiceOptions</IonButton>
          <IonButton expand="block" style={{ margin: '6px 12px' }} onClick={setNotificationOptions}>setNotificationOptions</IonButton>
          <IonListHeader>
            <IonLabel>Trips</IonLabel>
          </IonListHeader>
          <IonButton expand="block" style={{ margin: '6px 12px' }} onClick={startTrip}>startTrip</IonButton>
          <IonButton expand="block" style={{ margin: '6px 12px' }} onClick={startTripWithTrackingOptions}>startTrip (with tracking options)</IonButton>
          <IonButton expand="block" style={{ margin: '6px 12px' }} onClick={getTripOptions}>getTripOptions</IonButton>
          <IonButton expand="block" style={{ margin: '6px 12px' }} onClick={updateTrip}>updateTrip</IonButton>
          <IonButton expand="block" style={{ margin: '6px 12px' }} onClick={completeTrip}>completeTrip</IonButton>
          <IonButton expand="block" style={{ margin: '6px 12px' }} onClick={cancelTrip}>cancelTrip</IonButton>
          <IonListHeader>
            <IonLabel>Events</IonLabel>
          </IonListHeader>
          <IonButton expand="block" style={{ margin: '6px 12px' }} onClick={acceptEvent}>acceptEvent</IonButton>
          <IonButton expand="block" style={{ margin: '6px 12px' }} onClick={rejectEvent}>rejectEvent</IonButton>
          <IonListHeader>
            <IonLabel>Geo / Search</IonLabel>
          </IonListHeader>
          <IonButton expand="block" style={{ margin: '6px 12px' }} onClick={getContext}>getContext</IonButton>
          <IonButton expand="block" style={{ margin: '6px 12px' }} onClick={searchPlaces}>searchPlaces</IonButton>
          <IonButton expand="block" style={{ margin: '6px 12px' }} onClick={searchGeofences}>searchGeofences</IonButton>
          <IonButton expand="block" style={{ margin: '6px 12px' }} onClick={autocomplete}>autocomplete</IonButton>
          <IonButton expand="block" style={{ margin: '6px 12px' }} onClick={geocode}>geocode</IonButton>
          <IonButton expand="block" style={{ margin: '6px 12px' }} onClick={reverseGeocode}>reverseGeocode</IonButton>
          <IonButton expand="block" style={{ margin: '6px 12px' }} onClick={ipGeocode}>ipGeocode</IonButton>
          <IonButton expand="block" style={{ margin: '6px 12px' }} onClick={validateAddress}>validateAddress</IonButton>
          <IonButton expand="block" style={{ margin: '6px 12px' }} onClick={getDistance}>getDistance</IonButton>
          <IonButton expand="block" style={{ margin: '6px 12px' }} onClick={getMatrix}>getMatrix</IonButton>
          <IonListHeader>
            <IonLabel>Conversions</IonLabel>
          </IonListHeader>
          <IonButton expand="block" style={{ margin: '6px 12px' }} onClick={logConversion}>logConversion</IonButton>
          <IonButton expand="block" style={{ margin: '6px 12px' }} onClick={logConversionWithRevenue}>logConversion (with revenue)</IonButton>
          <IonListHeader>
            <IonLabel>Indoor</IonLabel>
          </IonListHeader>
          <IonButton expand="block" style={{ margin: '6px 12px' }} onClick={startIndoorScan}>startIndoorScan</IonButton>
          <IonListHeader>
            <IonLabel>Misc</IonLabel>
          </IonListHeader>
          <IonButton expand="block" style={{ margin: '6px 12px' }} onClick={showTestInAppMessage}>showInAppMessage (test)</IonButton>
          <IonButton expand="block" style={{ margin: '6px 12px' }} onClick={getHost}>getHost</IonButton>
          <IonButton expand="block" style={{ margin: '6px 12px' }} onClick={getPublishableKey}>getPublishableKey</IonButton>
        </IonList>
      </IonContent>
    </IonPage>
  );
};

export default Home;