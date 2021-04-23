<script lang="ts">
  import Device from "./devices/device.svelte";
  import { DeviceSubscription } from "../services/StreamDtos";
  import EventBus from "../services/EventBus";
  import { devicesStore } from "../services/stores";
  import { Tabs, Tab } from "svelte-chota/cmp";
  import AllDevicesHistoryChart from "./_AllDevicesHistoryChart.svelte";
  import TemperatureChart from "./_TemperatureChart.svelte";

  const devices = devicesStore;
  EventBus.request(new DeviceSubscription());

  let active_tab = 1;
</script>

<svelte:head>
  <title>Flock.Office Monitor</title>
</svelte:head>

<div class="p-3 h-full w-full">
  <div class="h-4/6 h-min-500">
    <Tabs bind:active={active_tab}>
      <Tab>Deuren</Tab>
      <Tab>Temperatuur</Tab>
    </Tabs>

    {#if active_tab === 0}
      <AllDevicesHistoryChart />
    {/if}

    {#if active_tab === 1}
      <TemperatureChart />
    {/if}
  </div>
  <div class="h-2/6">
    <div class="grid md:grid-cols-3 sm:grid-cols-2 grid-cols-1gap-10">
      {#each $devices as { id, name, type }, i}
        <Device {id} {name} {type}/>
      {/each}
    </div>
  </div>
</div>

<style>
  :global(:root) {
    --color-primary: #f8e008;
  }
</style>
