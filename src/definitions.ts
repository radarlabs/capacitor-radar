declare module '@capacitor/core' {
  interface PluginRegistry {
    RadarPlugin: RadarPlugin;
  }
}

export interface RadarCallback {
  status: string;
  location: NativeLocation;
  user: RadarUser;
  events: RadarEvent[];
}

interface Chain {
  name: string;
  slug: string;
}

interface CodedLocation {
  _id: string;
  code: number;
  name: string;
  type: string;
}

type Country = CodedLocation;

type DMA = CodedLocation;

interface Geofence {
  _id: string;
  externalId: string;
  geometry: NativeLocation;
  tag: string;
  metadata: {
    [key: string]: string;
  };
}

interface Insights {
  homeLocation: Nullable<Location>;
  officeLocation: Nullable<Location>;
  state: {
    home: null | boolean;
    office: null | boolean;
    traveling: null | boolean;
  };
}

interface Location {
  confidence: RadarEventConfidence;
  location: NativeLocation;
  type: string;
}

interface NativeLocation {
  accuracy: number;
  latitude: number;
  longitude: number;
}

interface Place {
  _id: string;
  categories: string[];
  chain: Nullable<Chain>;
  location?: Nullable<Location>;
  name: string;
}

type PostalCode = CodedLocation;

enum RadarEventConfidence {
  none = 0,
  low = 1,
  medium = 2,
  high = 3
}

interface RadarEvent {
  _id: string;
  actualCreatedAt: string;
  alternatePlaces: null | Place;
  confidence: RadarEventConfidence;
  createdAt: string;
  duration: number;
  geofence: null | Geofence;
  live: boolean;
  place: null | Place;
  region: null | Region;
  type: RadarEventType;
  verification: RadarEventVerification;
  verifiedPlace: null | Place;
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

export interface RadarLocationPermissionsCallback {
  status: string;
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

interface RadarUser {
  _id: string;
  country: Nullable<Country>;
  dma: Nullable<DMA>;
  geofences: Nullable<Geofence>[];
  insights: Insights;
  location: Nullable<NativeLocation>;
  place: Nullable<Place>;
  postalCode: Nullable<PostalCode>;
  state: Nullable<State>;
  userId: null | string;
}

interface Region {
  country: Nullable<Country>;
  dma: Nullable<DMA>;
  postalCode: Nullable<PostalCode>;
  state: Nullable<State>;
}

type State = CodedLocation;

/**
 * Recursive nullable object type
 */
type Nullable<Props extends object> = {
  [K in keyof Props]: Props[K] extends object ? Nullable<Props[K]> : Props[K] | null
};
