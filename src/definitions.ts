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

export interface RadarPlugin {
  STATUS: any;
  PLACES_PROVIDER: any;
  initialize(options: { publishableKey: string }): void;
  setPlacesProvider(options: { placesProvider: string }): void;
  setUserId(options: { userId: string }): void;
  setDescription(options: { description: string }): void;
  trackOnce(): Promise<RadarCallback>;
}
