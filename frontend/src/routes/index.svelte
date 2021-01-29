<script lang="ts">
  import { connectSocket } from "../services/RSocket.svelte";
  import Device from "./_devices/device.svelte";

  let devices = connectSocket("devices");
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
