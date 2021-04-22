<script lang="ts">
    import Device from "./devices/device.svelte";
    import {DeviceSubscription} from "../services/StreamDtos";
    import EventBus from "../services/EventBus"
    import {devicesStore} from "../services/stores";
    import AllDevicesHistoryChart from "./_AllDevicesHistoryChart.svelte";

    const devices = devicesStore
    EventBus.request(new DeviceSubscription())
</script>

<svelte:head>
    <title>Flock.Office Monitor</title>
</svelte:head>

<div class="p-3 h-full w-full">
    <div class="h-4/6 h-min-500">
        <AllDevicesHistoryChart/>
    </div>
    <div class="h-2/6">
        <div class="grid md:grid-cols-3 sm:grid-cols-2 grid-cols-1 gap-10">
            {#each $devices as {id, name, type}, i}
                <Device {id} {name} {type}/>
            {/each}
        </div>
    </div>
</div>

<style>
</style>
