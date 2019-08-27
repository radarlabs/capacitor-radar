declare module '@capacitor/core' {
  interface PluginRegistry {
    RadarPlugin: RadarPlugin;
  }
}

export interface Location {
  latitude: number;
  longitude: number;
  accuracy: number;
}

export interface RadarCallback {
  status: string;
  location: Location;
  user: RadarUser;
  events: RadarEvent[];
}

export interface RadarChain {
  name: string;
  slug: string;
}

export interface RadarEvent {
  _id: string;
  live: boolean;
  type: RadarEventType;
  geofence: null | RadarGeofence;
  place: null | RadarPlace;
  alternatePlaces: null | RadarPlace;
  region: null | RadarRegion;
  confidence: RadarEventConfidence;
}

export enum RadarEventConfidence {
  none = 0,
  low = 1,
  medium = 2,
  high = 3
}

export type RadarEventType =
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

export enum RadarEventVerification {
  accept = 1,
  unverify = 0,
  reject = -1
}

export interface RadarGeofence {
  _id: string;
  description: string;
  tag: null | string;
  externalId: null | string;
}

export interface RadarInsights {
  homeLocation: null | RadarInsightsLocation;
  officeLocation: null | RadarInsightsLocation;
  state: null | {
    home: boolean;
    office: boolean;
    traveling: boolean;
  };
}

export enum RadarInsightsConfidence {
  none = 0,
  low = 1,
  medium = 2,
  high = 3
}

export interface RadarInsightsLocation {
  type: string;
  location: RadarInsightsLocation;
  confidence: RadarInsightsConfidence;
}

export interface RadarLocationPermissionsCallback {
  status: string;
}

export interface RadarPlace {
  _id: string;
  name: string;
  categories: string[];
  chain: null | RadarChain;
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

export interface RadarRegion {
  _id: string;
  type: string;
  code: string;
  name: string;
}

export interface RadarUser {
  _id: string;
  userId: null | string;
  deviceId: null | string;
  description: null | string;
  geofences: null | RadarGeofence[];
  insights: null | RadarInsights;
  place: null | RadarPlace;
  country: null | RadarRegion;
  state: null | RadarRegion;
  dma: null | RadarRegion;
  postalCode: null | RadarRegion;
}
