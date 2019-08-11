declare module '@capacitor/core' {
  interface PluginRegistry {
    RadarPlugin: RadarPlugin;
  }
}

export interface RadarCallback {
  status: string;
  location: any;
  user: any;
  events: any;
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
