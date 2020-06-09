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

  initialize(options: { publishableKey: string }): void {
    Radar.initialize(options.publishableKey);
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

  async getLocation(): Promise<RadarLocationCallback> {

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

  startTrackingEfficient(): void {
    // not implemented
  }

  startTrackingResponsive(): void {
    // not implemented
  }

  startTrackingContinuous(): void {
    // not implemented
  }

  startTrackingCustom(): void {
    // not implemented
  }

  stopTracking(): void {
    // not implemented
  }

  acceptEvent(): void {
    // not implemented
  }

  rejectEvent(): void {
    // not implemented
  }

  async getContext(): Promise<RadarContextCallback> {
    // not implemented
  }

  async searchPlaces(): Promise<RadarSearchPlacesCallback> {
    // not implemented
  }

  async searchGeofences(): Promise<RadarSearchGeofencesCallback> {
    // not implemented
  }

  async searchPoints(): Promise<RadarSearchPointsCallback> {
    // not implemented
  }

  async autocomplete(): Promise<RadarGeocodeCallback> {
    // not implemented
  }

  async geocode(): Promise<RadarGeocodeCallback> {
    // not implemented
  }

  async reverseGeocode(): Promise<RadarReverseGeocodeCallback> {
    // not implemented
  }

  async ipGeocode(): Promise<RadarIPGeocodeCallback> {
    // not implemented
  }

  async getDistance(): Promise<RadarDistanceCallback> {
    // not implemented
  }

}

const radarPluginWeb = new RadarPluginWeb();

export { radarPluginWeb };

import { registerWebPlugin } from '@capacitor/core';
registerWebPlugin(radarPluginWeb);
