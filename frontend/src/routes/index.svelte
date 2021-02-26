<script lang="ts">
  import { connectSocket } from "../services/RSocketStore.svelte";
  import Device from "./devices/device.svelte";
  import AllDevicesHistoryChart from "./_AllDevicesHistoryChart.svelte";

  let devices = connectSocket("devices");
</script>

<svelte:head>
  <title>Flock.Office Monitor</title>
</svelte:head>

<div class="p-3 h-full">
  <div class="h-4/6">
    <AllDevicesHistoryChart />
  </div>
  <div class="h-2/6">
    <div class="grid grid-cols-3 gap-10">
      {#each $devices as { id, name, type }, i}
        <Device {name} />
      {/each}
    </div>
  </div>
</div>

<style>
</style>
