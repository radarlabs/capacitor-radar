declare module '@capacitor/core' {
  interface PluginRegistry {
    RadarPlugin: RadarPlugin;
  }
}

interface Location {
  accuracy: number;
  latitude: number;
  longitude: number;
}

/**
 * Recursive nullable object type
 */
type Nullable<Props extends object> = {
  [K in keyof Props]: Props[K] extends object ? Nullable<Props[K]> : Props[K] | null
};

export interface RadarCallback {
  status: string;
  location: Location;
  user: RadarUser;
  events: RadarEvent[];
}

interface RadarChain {
  name: string;
  slug: string;
  externalId: null | string;
  metadata: null | {
    [key: string]: string | boolean | number;
  };
}

interface RadarEvent {
  _id: string;
  actualCreatedAt: string;
  alternatePlaces: null | RadarPlace;
  confidence: RadarEventConfidence;
  createdAt: string;
  duration: number;
  geofence: null | RadarGeofence;
  live: boolean;
  place: null | RadarPlace;
  region: null | RadarRegion;
  type: RadarEventType;
  verification: RadarEventVerification;
  verifiedPlace: null | RadarPlace;
}

enum RadarEventConfidence {
  none = 0,
  low = 1,
  medium = 2,
  high = 3
}

type RadarEventType =
  | 'unknown'
  | 'user.entered_geofence'
  | 'user.entered_home'
  | 'user.entered_office'
  | 'user.entered_place'
  | 'user.entered_region_country'
  | 'user.entered_region_dma'
  | 'user.entered_region_state'
  | 'user.exited_geofence'
  | 'user.exited_home'
  | 'user.exited_office'
  | 'user.exited_place'
  | 'user.exited_region_country'
  | 'user.exited_region_dma'
  | 'user.exited_region_state'
  | 'user.nearby_place_chain'
  | 'user.started_traveling'
  | 'user.stopped_traveling';

enum RadarEventVerification {
  accept = 1,
  unverify = 0,
  reject = -1
}

interface RadarGeofence {
  _id: string;
  tag: null | string;
  externalId: null | string;
  metadata: null | {
    [key: string]: string | boolean | number;
  };
}

interface RadarInsights {
  homeLocation: Nullable<RadarInsightsLocation>;
  officeLocation: Nullable<RadarInsightsLocation>;
  state: {
    home: null | boolean;
    office: null | boolean;
    traveling: null | boolean;
  };
}

enum RadarInsightsConfidence {
  none = 0,
  low = 1,
  medium = 2,
  high = 3
}

interface RadarInsightsLocation {
  type: string;
  location: RadarInsightsLocation;
  confidence: RadarInsightsConfidence;
}

export interface RadarLocationPermissionsCallback {
  status: string;
}

interface RadarPlace {
  _id: string;
  name: string;
  chain: Nullable<RadarChain>;
  categories: string[];
  location?: Nullable<Location>;
}

export interface RadarPlugin {
  STATUS: any;
  PLACES_PROVIDER: any;
  initialize(options: { publishableKey: string }): void;
  setPlacesProvider(options: { placesProvider: string }): void;
  setUserId(options: { userId: string }): void;
  setDescription(options: { description: string }): void;
  setMetadata(options: { metadata: object }): void;
  getLocationPermissionsStatus(): Promise<RadarLocationPermissionsCallback>;
  requestLocationPermissions(options: { background: boolean }): void;
  startTracking(): void;
  stopTracking(): void;
  trackOnce(): Promise<RadarCallback>;
  updateLocation(options: { latitude: number, longitude: number, accuracy: number }): Promise<RadarCallback>;
  acceptEvent(options: { eventId: string, verifiedPlaceId: string }): void;
  rejectEvent(options: { eventId: string }): void;
}

interface RadarRegion {
  _id: string;
  type: string;
  code: string;
  name: string;
}

interface RadarUser {
  _id: string;
  userId: null | string;
  deviceId: null | string;
  description: null | string;
  metadata: null | {
    [key: string]: string | boolean | number;
  };
  location: Nullable<Location>;
  geofences: Nullable<RadarGeofence>[];
  insights: RadarInsights;
  place: Nullable<RadarPlace>;
  country: Nullable<RadarRegion>;
  state: Nullable<RadarRegion>;
  dma: Nullable<RadarRegion>;
  postalCode: Nullable<RadarRegion>;
}
