// @ts-nocheck

import { WebPlugin } from '@capacitor/core';

import type {
  RadarLocationPermissionsCallback,
  RadarLocationCallback,
  RadarTrackCallback,
  RadarContextCallback,
  RadarSearchPlacesCallback,
  RadarSearchGeofencesCallback,
  RadarGeocodeCallback,
  RadarIPGeocodeCallback,
  RadarRouteCallback,
  RadarPlugin
} from './definitions';

import Radar from 'radar-sdk-js';

export class RadarPluginWeb extends WebPlugin implements RadarPlugin {
  initialize(options: { publishableKey: string }): void {
    Radar.initialize(options.publishableKey);
  }

  setUserId(options: { userId: string }): void {
    Radar.setUserId(options.userId);
  }

  getUserId(): Promise<object> {
    // not implemented
  }

  setDescription(options: { description: string }): void {
    Radar.setDescription(options.description);
  }

  getDescription(): Promise<object> {
    // not implemented
  }

  setMetadata(options: { metadata: object }): void {
    Radar.setMetadata(options.metadata);
  }

  getMetadata(): Promise<object> {
    // not implemented
  }

  setAnonymousTrackingEnabled(options: { enabled: boolean }): void {
    // not implemented
  }

  getLocationPermissionsStatus(): Promise<RadarLocationPermissionsCallback> {
    return new Promise(resolve => {
      const navigator = window.navigator as any;

      if (!navigator.permissions) {
        resolve({
          status: 'UNKNOWN'
        });
      } else {
        navigator.permissions.query({ name: 'geolocation' }).then((result) => {
          resolve({
            status: result.state === 'granted' ? 'GRANTED_FOREGROUND' : 'DENIED',
          });
        });
      }
    });
  }

  requestLocationPermissions(): void {
    // not implemented
  }

  async getLocation(): Promise<RadarLocationCallback> {
    return new Promise((resolve, reject) => {
      Radar.getLocation((err, { status, location, stopped }) => {
        if (err) {
          reject(err);
        } else {
          resolve({
            status,
            location,
            stopped,
          })
        }
      });
    });
  }

  async trackOnce(options?: { latitude?: number, longitude?: number, accuracy?: number } | { desiredAccuracy: string, beacons: boolean}): Promise<RadarTrackCallback> {
    return new Promise((resolve, reject) => {
      const callback = (err, { status, location, user, events }) => {
        if (err) {
          reject(err);
        } else {
          resolve({
            status,
            location,
            user,
            events,
          });
        }
      };

      if (options) {
        Radar.trackOnce(options, callback);
      } else {
        Radar.trackOnce(callback);
      }
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

  mockTracking(): void {
    // not implemented
  }

  stopTracking(): void {
    // not implemented
  }

  setForegroundServiceOptions(): void {
    // not implemented
  }

  startTrip(): void {
    // not implemented
  }

  updateTrip(): void {
    // not implemented
  }

  completeTrip(): void {
    // not implemented
  }

  cancelTrip(): void {
    // not implemented
  }

  acceptEvent(): void {
    // not implemented
  }

  rejectEvent(): void {
    // not implemented
  }

  async getContext(options?: { latitude?: number, longitude?: number }): Promise<RadarContextCallback> {
    return new Promise((resolve, reject) => {
      const callback = (err, { status, location, context }) => {
        if (err) {
          reject(err);
        } else {
          resolve({
            status,
            location,
            context,
          });
        }
      };

      if (options) {
        Radar.getContext(options, callback);
      } else {
        Radar.getContext(callback);
      }
    });
  }

  async searchPlaces(options: { near?: { latitude: number, longitude: number }, radius: number, chains?: string[], chainMetadata?: object, categories?: string[], groups?: string[], limit: number }): Promise<RadarSearchPlacesCallback> {
    return new Promise((resolve, reject) => {
      Radar.searchPlaces(options, (err, { status, location, places }) => {
        if (err) {
          reject(err);
        } else {
          resolve({
            status,
            location,
            places,
          });
        }
      });
    });
  }

  async searchGeofences(options: { near?: { latitude: number, longitude: number }, radius: number, tags?: string[], limit: number }): Promise<RadarSearchGeofencesCallback> {
    return new Promise((resolve, reject) => {
      Radar.searchGeofences(options, (err, { status, location, geofences }) => {
        if (err) {
          reject(err);
        } else {
          resolve({
            status,
            location,
            geofences,
          });
        }
      });
    });
  }

  async autocomplete(options: { query: string, near?: { latitude: number, longitude: number }, limit: number }): Promise<RadarGeocodeCallback> {
    return new Promise((resolve, reject) => {
      Radar.autocomplete(options, (err, { status, addresses }) => {
        if (err) {
          reject(err);
        } else {
          resolve({
            status,
            addresses,
          });
        }
      });
    });
  }

  async geocode(options: { query: string }): Promise<RadarGeocodeCallback> {
    return new Promise((resolve, reject) => {
      Radar.geocode(options, (err, { status, addresses }) => {
        if (err) {
          reject(err);
        } else {
          resolve({
            status,
            addresses,
          });
        }
      });
    });
  }

  async reverseGeocode(options?: { latitude?: number, longitude?: number }): Promise<RadarGeocodeCallback> {
    return new Promise((resolve, reject) => {
      const callback = (err, { status, addresses }) => {
        if (err) {
          reject(err);
        } else {
          resolve({
            status,
            addresses,
          });
        }
      };

      if (options) {
        Radar.reverseGeocode(options, callback);
      } else {
        Radar.reverseGeocode(callback);
      }
    });
  }

  async ipGeocode(): Promise<RadarIPGeocodeCallback> {
    return new Promise((resolve, reject) => {
      Radar.ipGeocode((err, { status, address }) => {
        if (err) {
          reject(err);
        } else {
          resolve({
            status,
            address,
          });
        }
      });
    });
  }

  async getDistance(options: { origin?: { latitude: number, longitude: number }, destination: { latitude: number, longitude: number }, modes: string[], units: string }): Promise<RadarRouteCallback> {
    return new Promise((resolve, reject) => {
      Radar.getDistance(options, (err, { status, routes }) => {
        if (err) {
          reject(err);
        } else {
          resolve({
            status,
            routes,
          });
        }
      });
    });
  }

}

const radarPluginWeb = new RadarPluginWeb();

export { radarPluginWeb };
