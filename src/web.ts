import { WebPlugin } from '@capacitor/core';
import { RadarCallback, RadarPlugin } from './definitions';
import Radar from 'radar-sdk-js';

export class RadarPluginWeb extends WebPlugin implements RadarPlugin {
  constructor() {
    super({
      name: 'RadarPlugin',
      platforms: ['web']
    });
  }

  STATUS = Radar.STATUS

  PLACES_PROVIDER = Radar.PLACES_PROVIDER

  initialize(options: { publishableKey: string }): void {
    Radar.initialize(options.publishableKey);
  }

  setPlacesProvider(options: { placesProvider: string }): void {
    Radar.setPlacesProvider(options.placesProvider);
  }

  setUserId(options: { userId: string }): void {
    Radar.setUserId(options.userId);
  }

  setDescription(options: { description: string }): void {
    Radar.setDescription(options.description);
  }

  async trackOnce(): Promise<RadarCallback> {
    return new Promise(resolve => {
      Radar.trackOnce((status, location, user, events) => {
        resolve({
          status,
          location,
          user,
          events
        });
      });
    });
  }

}

const radarPluginWeb = new RadarPluginWeb();

export { radarPluginWeb };

import { registerWebPlugin } from '@capacitor/core';
registerWebPlugin(radarPluginWeb);
