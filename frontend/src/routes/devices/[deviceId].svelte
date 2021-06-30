<script context="module" lang="ts">
  export async function preload({ params }) {
    return {
      deviceId: params.deviceId,
    };
  }
</script>

<script lang="ts">
  import { eventBus } from "../../services/EventBus";
  import type { DeviceDto } from "../../services/StreamDtos";
  import {
    DeviceStateSubscription,
    DeviceSubscription,
  } from "../../services/StreamDtos";
  import { derived } from "svelte/store";
  import { devices, deviceStates } from "../../services/stores";

  export let deviceId;

  const date = new Date();
  date.setHours(date.getHours() - 10);
  setTimeout(() => {
    console.log("Requesting deviceState for device:", deviceId);
    eventBus.request(new DeviceStateSubscription(deviceId, date));
  }, 5000);

  setTimeout(() => {
    eventBus.request(new DeviceSubscription());
  }, 500);

  const unknownDevice: DeviceDto = {
    id: "unknown",
    name: "Onbekend",
    type: undefined,
  };

  const device = derived(
    devices,
    ($devices) => $devices.find((s) => s.id === deviceId) || unknownDevice
  );

  const state = derived(
    deviceStates,
    ($deviceStates) => $deviceStates.get(deviceId) || []
  );
</script>

<div>
  <h1>Device: {deviceId}</h1>

  <div>
    <h2>Metadata</h2>
    <pre>{JSON.stringify($device, null, 4)}</pre>
  </div>
  <div>
    <h2>State</h2>
    <pre>{JSON.stringify($state, null, 4)}</pre>
  </div>
</div>
