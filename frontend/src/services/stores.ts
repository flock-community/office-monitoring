import { derived, writable } from "svelte/store";
import { eventBus } from "./EventBus";
import {
  DeviceDto,
  DeviceState,
  DeviceStateSubscription,
  StateBody,
} from "./StreamDtos";

export const devices = writable(new Array<DeviceDto>());
export const deviceStates = writable(
  new Map<string, DeviceState<StateBody>[]>()
);

// TODO Verplaatsen naar Dashboard? Hierdoor gebeurt dit niet wanneer we niet alle states nodig
devices.subscribe((devices) => {
  const date = new Date();
  date.setHours(date.getHours() - 10);
  devices.forEach(
    (device) => eventBus.request(new DeviceStateSubscription(device.id, date))
    // TODO kan dit weg
    // setTimeout(
    //   () => eventBus.request(new DeviceStateSubscription(device.id, date)),
    //   500
    // )
  );
});

export const hasData = derived(
  deviceStates,
  ($deviceStates) => $deviceStates.size > 0
);
