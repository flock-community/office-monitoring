import { writable } from "svelte/store";
import {
  ContactSensorState,
  DeviceDto,
  DeviceState,
  DeviceType,
  StateBody,
} from "./StreamDtos";

export const devicesStore = writable(new Array<DeviceDto>());
export const deviceStateStore = writable(new Array<DeviceState<StateBody>>());

// Dummy data
const deviceStateDummyData = () => {
  const now = new Date();
  const before = new Date();
  before.setHours(before.getHours() - 2);

  const device1States: DeviceState<ContactSensorState>[] = [
    {
      id: "1",
      type: DeviceType.CONTACT_SENSOR,
      deviceId: "door-1",
      date: before,
      body: {
        lastSeen: now,
        battery: 1,
        voltage: 1,
        contact: false,
      },
    },
    {
      id: "2",
      type: DeviceType.CONTACT_SENSOR,
      deviceId: "door-1",
      date: now,
      body: {
        lastSeen: now,
        battery: 1,
        voltage: 1,
        contact: true,
      },
    },
  ];

  return device1States;
};

const devicesDummyData = () => {
  const devices: DeviceDto[] = [
    {
      id: "door-1",
      name: "Linker deur",
      type: DeviceType.CONTACT_SENSOR,
    },
  ];
  return devices;
};

// deviceStateStore.update((stored) => [...stored, ...deviceStateDummyData()]);
// devicesStore.update((stored) => [...stored, ...devicesDummyData()]);
