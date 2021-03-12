import { writable } from "svelte/store";
import type { DeviceDto, DeviceStateDto } from "./StreamDtos";

export const devicesStore = writable(new Array<DeviceDto>());
export const deviceStateStore = writable(new Array<DeviceStateDto>());
