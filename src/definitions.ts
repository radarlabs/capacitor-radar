import type { PluginListenerHandle } from '@capacitor/core';

export interface RadarPlugin {
  addListener(eventName: 'clientLocation', listenerFunc: (result: { location: Location, stopped: boolean, source: string }) => void): Promise<PluginListenerHandle> & PluginListenerHandle;
  addListener(eventName: 'location', listenerFunc: (result: { location: Location, user: RadarUser }) => void): Promise<PluginListenerHandle> & PluginListenerHandle;
  addListener(eventName: 'events', listenerFunc: (result: { events: RadarEvent[], user: RadarUser }) => void): Promise<PluginListenerHandle> & PluginListenerHandle;
  addListener(eventName: 'error', listenerFunc: (result: { status: string }) => void): Promise<PluginListenerHandle> & PluginListenerHandle;
  addListener(eventName: 'log', listenerFunc: (result: { message: string }) => void): Promise<PluginListenerHandle> & PluginListenerHandle;
  initialize(options: { publishableKey: string }): void;
  setUserId(options: { userId: string }): void;
  setDescription(options: { description: string }): void;
  setMetadata(options: { metadata: object }): void;
  getLocationPermissionsStatus(): Promise<RadarLocationPermissionsCallback>;
  requestLocationPermissions(options: { background: boolean }): void;
  getLocation(): Promise<RadarLocationCallback>;
  trackOnce(options?: { latitude?: number, longitude?: number, accuracy?: number }): Promise<RadarTrackCallback>;
  startTrackingEfficient(): void;
  startTrackingResponsive(): void;
  startTrackingContinuous(): void;
  startTrackingCustom(options: object): void;
  mockTracking(options: { origin: { latitude: number, longitude: number }, destination: { latitude: number, longitude: number }, mode: string, steps: number, interval: number }): void;
  stopTracking(): void;
  setForegroundServiceOptions(options: object): void;
  startTrip(options: object): Promise<RadarTripCallback>;
  updateTrip(options: {options: object, status?: string}): Promise<RadarTripCallback>;
  completeTrip(): Promise<RadarTripCallback>;
  cancelTrip(): Promise<RadarTripCallback>;
  acceptEvent(options: { eventId: string, verifiedPlaceId: string }): void;
  rejectEvent(options: { eventId: string }): void;
  getContext(options?: { latitude?: number, longitude?: number }): Promise<RadarContextCallback>;
  searchPlaces(options: { near?: { latitude: number, longitude: number }, radius: number, chains?: string[], categories?: string[], groups?: string[], limit: number }): Promise<RadarSearchPlacesCallback>;
  searchGeofences(options: { near?: { latitude: number, longitude: number }, radius: number, tags?: string[], limit: number }): Promise<RadarSearchGeofencesCallback>;
  autocomplete(options: { query: string, near?: { latitude: number, longitude: number }, limit: number }): Promise<RadarGeocodeCallback>;
  geocode(options: { query: string }): Promise<RadarGeocodeCallback>;
  reverseGeocode(options?: { latitude?: number, longitude?: number }): Promise<RadarGeocodeCallback>;
  ipGeocode(): Promise<RadarIPGeocodeCallback>;
  getDistance(options: { origin?: { latitude: number, longitude: number }, destination: { latitude: number, longitude: number }, modes: string[], units: string }): Promise<RadarRouteCallback>;
}

export interface RadarLocationCallback {
  status: string;
  location?: Location;
  stopped?: boolean;
}

export interface RadarTrackCallback {
  status: string;
  location?: Location;
  user?: RadarUser;
  events?: RadarEvent[];
}

export interface RadarTripCallback {
  status: string;
  trip?: RadarTrip;
  events?: RadarEvent[];
}

export interface RadarContextCallback {
  status: string;
  location?: Location;
  context?: RadarContext;
}

export interface RadarSearchPlacesCallback {
  status: string;
  location?: Location;
  places?: RadarPlace[];
}

export interface RadarSearchGeofencesCallback {
  status: string;
  location?: Location;
  geofences?: RadarGeofence[];
}

export interface RadarGeocodeCallback {
  status: string;
  addresses?: RadarAddress[];
}

export interface RadarIPGeocodeCallback {
  status: string;
  address?: RadarAddress;
}

export interface RadarRouteCallback {
  status: string;
  routes?: RadarRoutes;
}

export interface Location {
  latitude: number;
  longitude: number;
  accuracy?: number;
}

export interface RadarUser {
  _id: string;
  userId?: string;
  deviceId?: string;
  description?: string;
  metadata?: object;
  trip?: RadarTrip;
  geofences?: RadarGeofence[];
  insights?: RadarInsights;
  place?: RadarPlace;
  country?: RadarRegion;
  state?: RadarRegion;
  dma?: RadarRegion;
  postalCode?: RadarRegion;
  fraud?: RadarFraud;
}

export interface RadarTrip {
  _id: string;
  externalId: string;
  metadata?: object;
  destinationGeofenceTag?: string;
  destinationGeofenceExternalId?: string;
  mode?: string;
  eta?: RadarTripEta;
  status: string;
  scheduledArrivalAt?: Date;
}

export interface RadarContext {
  geofences?: RadarGeofence[];
  place?: RadarPlace;
  country?: RadarRegion;
  state?: RadarRegion;
  dma?: RadarRegion;
  postalCode?: RadarRegion;
}

export interface RadarEvent {
  _id: string;
  live: boolean;
  type: RadarEventType;
  geofence?: RadarGeofence;
  place?: RadarPlace;
  alternatePlaces?: RadarPlace;
  region?: RadarRegion;
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
  | 'user.stopped_traveling'
  | 'user.started_commuting'
  | 'user.stopped_commuting'
  | 'user.started_trip'
  | 'user.updated_trip'
  | 'user.approaching_trip_destination'
  | 'user.arrived_at_trip_destination'
  | 'user.stopped_trip';

export enum RadarEventVerification {
  accept = 1,
  unverify = 0,
  reject = -1
}

export interface RadarGeofence {
  _id: string;
  description: string;
  tag?: string;
  externalId?: string;
  metadata?: object;
}

export interface RadarPlace {
  _id: string;
  name: string;
  categories: string[];
  chain?: RadarChain;
}

export interface RadarChain {
  name: string;
  slug: string;
}

export interface RadarRegion {
  _id: string;
  type: string;
  code: string;
  name: string;
}

export interface RadarInsights {
  homeLocation?: RadarInsightsLocation;
  officeLocation?: RadarInsightsLocation;
  state?: {
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

export interface RadarAddress {
  latitude: number;
  longitude: number;
  placeLabel?: string;
  addressLabel?: string;
  formattedAddress?: string;
  country?: string;
  countryCode?: string;
  countryFlag?: string;
  state?: string;
  stateCode?: string;
  postalCode?: string;
  city?: string;
  borough?: string;
  county?: string;
  neighborhood?: string;
  number?: string;
  distance?: number;
  confidence?: string;
}

export interface RadarRoutes {
  geodesic: RadarRoute;
  foot?: RadarRoute;
  bike?: RadarRoute;
  car?: RadarRoute;
}

export interface RadarRoute {
  distance?: RadarRouteDistance;
  duration?: RadarRouteDuration;
}

export interface RadarRouteDistance {
  value: number;
  text: string;
}

export interface RadarRouteDuration {
  value: number;
  text: string;
}

export interface RadarTripEta {
  distance?: number;
  duration?: number;
}

export interface RadarFraud {
  proxy: boolean;
  mocked: boolean;
}
