import type { PluginListenerHandle } from '@capacitor/core';
import {
  Location,
  RadarAddress,
  RadarAddressCallback,
  RadarAutocompleteOptions,
  RadarContextCallback,
  RadarEvent,
  RadarGetDistanceOptions,
  RadarGetMatrixOptions,
  RadarIPGeocodeCallback,
  RadarLocationCallback,
  RadarLogConversionCallback,
  RadarLogConversionOptions,
  RadarMockTrackingOptions,
  RadarNotificationOptions,
  RadarRouteCallback,
  RadarRouteMatrix,
  RadarSearchGeofencesCallback,
  RadarSearchGeofencesOptions,
  RadarSearchPlacesCallback,
  RadarSearchPlacesOptions,
  RadarStartTripOptions,
  RadarTrackCallback,
  RadarTrackingOptions,
  RadarTrackingOptionsDesiredAccuracy,
  RadarTrackingOptionsForegroundService,
  RadarTrackOnceOptions,
  RadarTrackVerifiedCallback,
  RadarTrackVerifiedOptions,
  RadarTripCallback,
  RadarTripOptions,
  RadarUpdateTripOptions,
  RadarUser,
  RadarValidateAddressCallback,
  RadarVerifiedLocationToken,
  RadarVerifiedTrackingOptions,
} from "./types"

export interface RadarPlugin {
  initialize(options: { publishableKey: string, fraud?: boolean }): void;
  setLogLevel(options: { level: string }): void;
  setUserId(options: { userId?: string }): void;
  getUserId(): Promise<object>,
  setDescription(options: { description?: string }): void;
  getDescription(): Promise<object>,
  setMetadata(options: { metadata?: object }): void;
  getMetadata(): Promise<object>,
  setAnonymousTrackingEnabled(options: { enabled: boolean }): void;
  getLocationPermissionsStatus(): Promise<RadarLocationPermissionsCallback>;
  requestLocationPermissions(options: { background: boolean }): void;
  getLocation(options: { desiredAccuracy: RadarTrackingOptionsDesiredAccuracy }): Promise<RadarLocationCallback>;
  trackOnce(options?: Location | RadarTrackOnceOptions): Promise<RadarTrackCallback>;
  trackVerified(options?: RadarTrackVerifiedOptions): Promise<RadarTrackVerifiedCallback>;
  getVerifiedLocationToken(): Promise<RadarTrackVerifiedCallback>;
  startTrackingEfficient(): void;
  startTrackingResponsive(): void;
  startTrackingContinuous(): void;
  startTrackingCustom(options: { options: RadarTrackingOptions}): void;
  startTrackingVerified(options: RadarVerifiedTrackingOptions): void;
  mockTracking(options: RadarMockTrackingOptions): void;
  stopTracking(): void;
  stopTrackingVerified(): void;
  getTrackingOptions(): Promise<RadarTrackingOptions>;
  isUsingRemoteTrackingOptions(): Promise<boolean>;
  isTracking(): Promise<RadarTrackingStatus>;
  setForegroundServiceOptions(options: { options: RadarTrackingOptionsForegroundService }): void;
  setNotificationOptions(options: RadarNotificationOptions): void; // Android only
  getTripOptions(): Promise<RadarTripOptions>;
  startTrip(options: { options: RadarTripOptions | RadarStartTripOptions}): Promise<RadarTripCallback>;
  completeTrip(): Promise<RadarTripCallback>;
  cancelTrip(): Promise<RadarTripCallback>;
  updateTrip(options: RadarUpdateTripOptions): Promise<RadarTripCallback>;
  acceptEvent(options: { eventId: string, verifiedPlaceId: string }): void;
  rejectEvent(options: { eventId: string }): void;
  getContext(options?: Location): Promise<RadarContextCallback>;
  searchPlaces(options: RadarSearchPlacesOptions): Promise<RadarSearchPlacesCallback>;
  searchGeofences(options: RadarSearchGeofencesOptions): Promise<RadarSearchGeofencesCallback>;
  autocomplete(options: RadarAutocompleteOptions): Promise<RadarAddressCallback>;
  geocode(options: { query: string, layers?: string[], countries?: string[] }): Promise<RadarAddressCallback>;
  reverseGeocode(options?: { location?: Location, layers?: string[] }): Promise<RadarAddressCallback>;
  ipGeocode(): Promise<RadarIPGeocodeCallback>;
  getDistance(options: RadarGetDistanceOptions): Promise<RadarRouteCallback>;
  getMatrix(options: RadarGetMatrixOptions): Promise<RadarRouteMatrix>;
  logConversion(options: RadarLogConversionOptions): Promise<RadarLogConversionCallback>;
  validateAddress(options: { address: RadarAddress }): Promise<RadarValidateAddressCallback>;
  logTermination(): void; // iOS only
  logBackgrounding(): void;
  logResigningActive(): void;
  isUsingRemoteTrackingOptions(): Promise<object>;
  getHost(): Promise<object>;
  getPublishableKey(): Promise<string>;

  addListener(eventName: 'clientLocation', listenerFunc: (result: { location: Location, stopped: boolean, source: string }) => void): Promise<PluginListenerHandle> & PluginListenerHandle;
  addListener(eventName: 'location', listenerFunc: (result: { location: Location, user: RadarUser }) => void): Promise<PluginListenerHandle> & PluginListenerHandle;
  addListener(eventName: 'events', listenerFunc: (result: { events: RadarEvent[], user: RadarUser }) => void): Promise<PluginListenerHandle> & PluginListenerHandle;
  addListener(eventName: 'error', listenerFunc: (result: { status: string }) => void): Promise<PluginListenerHandle> & PluginListenerHandle;
  addListener(eventName: 'log', listenerFunc: (result: { message: string }) => void): Promise<PluginListenerHandle> & PluginListenerHandle;
  addListener(eventName: 'token', listenerFunc: (result: { token: RadarVerifiedLocationToken }) => void): Promise<PluginListenerHandle> & PluginListenerHandle;
}

export interface RadarLocationPermissionsCallback {
  status: string;
}

export interface RadarTrackingStatus {
  isTracking: string;
}

export type RadarGeocodeCallback = RadarAddressCallback;

export * from "./types";
