<script lang="ts">
  import Device from "./devices/device.svelte";
  import * as rxjs from 'rxjs';
  import {DeviceSubscription, DeviceX} from "../services/StreamDtos";
  import {connectSocket} from "../services/RSocket.svelte"
  import {map} from "rxjs/operators";

  // TODO: Set up rxjs nicely here. Consider a store instead maybe? Or some centralised 'eventbus' - like something
  let devices = connectSocket("devices", rxjs.interval(10000).pipe(map(_ => new DeviceSubscription())));
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
