import { Writable } from "stream";
import { writable } from "svelte/store";
import type { DeviceDto, DeviceStateDto } from "./StreamDtos";

const devicesStore = writable(new Array<DeviceDto>());
const deviceStateStore = writable(new Array<DeviceStateDto>());
