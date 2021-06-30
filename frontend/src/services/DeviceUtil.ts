import type { Color } from "@amcharts/amcharts4/core";
import * as am4core from "@amcharts/amcharts4/core";
import { TEXT_CSS } from "rsocket-core";
import { derived, get, writable } from "svelte/store";
import { devices } from "./stores";
import type { DeviceDto } from "./StreamDtos";

const colorSet = new am4core.ColorSet();

const deviceColors = writable<Map<string, Color>>(new Map());
const deviceNames = writable<Map<string, string>>(new Map());

export const getColor: (deviceId: string) => Color = (deviceId: string) => {
  return get(deviceColors).get(deviceId) || colorSet.getIndex(0);
};

export const getDeviceName: (deviceId: string) => string = (
  deviceId: string
) => {
  return get(deviceNames).get(deviceId) || "Unknown";
};

const updateDeviceColors = (devices: DeviceDto[]) => {
  const colors = devices.reduce((map, current) => {
    const encodedDeviceId = current.id
      .split("")
      .reduce(
        (prev, cur, idx) =>
          prev + (current.id.length - idx) * cur.charCodeAt(0),
        0
      );
    const derivedColor = colorSet.getIndex(encodedDeviceId % 15);
    map.set(current.id, derivedColor);
    return map;
  }, new Map<string, Color>());
  deviceColors.set(colors);
};

const updateDeviceNames = (devices: DeviceDto[]) => {
  const names = devices.reduce((map, current) => {
    map.set(current.id, current.name);
    return map;
  }, new Map<string, string>());
  deviceNames.set(names);
};

devices.subscribe((devices) => {
  updateDeviceNames(devices);
  updateDeviceColors(devices);
});
