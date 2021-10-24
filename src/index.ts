import { registerPlugin } from '@capacitor/core';

import type { RadarPlugin } from './definitions';

const Radar = registerPlugin<RadarPlugin>('Radar', {
  web: () => import('./web').then(m => new m.RadarPluginWeb()),
});

export * from './definitions';
export { Radar };