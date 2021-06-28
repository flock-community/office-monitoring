import { writable } from "svelte/store";
import type { DeviceDto, DeviceState, StateBody } from "./StreamDtos";

export const devicesStore = writable(new Array<DeviceDto>());
export const deviceStateStore = writable(
  new Map<string, DeviceState<StateBody>[]>()
);
