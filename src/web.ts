import { WebPlugin } from '@capacitor/core';
import { RadarCallback, RadarLocationPermissionsCallback, RadarPlugin } from './definitions';
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

  setMetadata(): void {
    // not implemented
  }

  getLocationPermissionsStatus(): Promise<RadarLocationPermissionsCallback> {
    return new Promise(resolve => {
      // not implemented
      resolve({
        status: 'UNKNOWN'
      });
    });
  }

  requestLocationPermissions(): void {
    // not implemented
  }

  startTracking(): void {
    // not implemented
  }

  stopTracking(): void {
    // not implemented
  }

  async trackOnce(): Promise<RadarCallback> {
    return new Promise((resolve, reject) => {
      Radar.trackOnce((status, location, user, events) => {
        if (status === Radar.STATUS.SUCCESS) {
          resolve({
            status,
            location,
            user,
            events,
          });
        } else {
          reject(status);
        }
      });
    });
  }

  async updateLocation(): Promise<RadarCallback> {
    return new Promise(resolve => {
      // not implemented
      resolve();
    });
  }

  acceptEvent(): void {
    // not implemented
  }

  rejectEvent(): void {
    // not implemented
  }

}

const radarPluginWeb = new RadarPluginWeb();

export { radarPluginWeb };

import { registerWebPlugin } from '@capacitor/core';
registerWebPlugin(radarPluginWeb);
