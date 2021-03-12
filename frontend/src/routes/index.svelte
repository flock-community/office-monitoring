<script lang="ts">
  import Device from "./devices/device.svelte";
  import {DeviceSubscription} from "../services/StreamDtos";
  import EventBus from "../services/EventBus"
  import {devicesStore} from "../services/stores";
  let devices = devicesStore
  EventBus.request(new DeviceSubscription())
</script>

<svelte:head>
  <title>Flock.Office Monitor</title>
</svelte:head>

<div class="p-3">
  <div class="grid grid-cols-3 gap-10">
    {#each $devices as { id, name, type }, i}
      <Device {name} />
    {/each}
  </div>
</div>

<style>
</style>
