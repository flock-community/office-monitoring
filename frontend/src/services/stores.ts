import { derived, writable } from "svelte/store";
import { eventBus } from "./EventBus";
import {
  DeviceDto,
  DeviceState,
  DeviceStateSubscription,
  StateBody,
} from "./StreamDtos";

export const devicesStore = writable(new Array<DeviceDto>());
export const deviceStateStore = writable(
  new Map<string, DeviceState<StateBody>[]>()
);

devicesStore.subscribe((devices) => {
  const date = new Date();
  date.setHours(date.getHours() - 10);
  devices.forEach((device) =>
    // TODO kan dit weg?
    setTimeout(() => {
      eventBus.request(new DeviceStateSubscription(device.id, date));
    })
  );
});

export const hasData = derived(
  deviceStateStore,
  ($deviceStateStore) => $deviceStateStore.size > 0
);
