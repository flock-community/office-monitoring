import type { Color } from "@amcharts/amcharts4/core";
import * as am4core from "@amcharts/amcharts4/core";
import { get } from "svelte/store";
import { devicesStore } from "./stores";

const colorSet = new am4core.ColorSet();

const _color = new Map<string, Color>();
export const getColor: (deviceId: string) => Color = (deviceId: string) => {
  if (!_color.has(deviceId)) {
    const encodedDeviceId = deviceId
      .substr(12) // leave out the zigbee2mqtt/ prefix for every deviceId
      .split("")
      .reduce(
        (prev, cur, idx) => prev + (deviceId.length - idx) * cur.charCodeAt(0),
        0
      );
    const derivedColor = colorSet.getIndex(encodedDeviceId % 15);
    _color.set(deviceId, derivedColor);
  }

  return _color.get(deviceId);
};

let _deviceNames = new Map<string, string>();
export const getDeviceName: (deviceId: string) => string = (
  deviceId: string
) => {
  if (!_deviceNames.has(deviceId)) {
    _deviceNames.set(
      deviceId,
      get(devicesStore).find((it) => it.id == deviceId)?.name ||
        "Roque device 42"
    );
  }

  return _deviceNames.get(deviceId);
};
