<script context="module" lang="ts">
    import EventBus from "../../services/EventBus";
    import {DeviceSubscription} from "../../services/StreamDtos";


    export function preload() {
        EventBus.request(new DeviceSubscription())
    }
</script>

<script lang="ts">
    import {devicesStore} from "../../services/stores";
    import {derived} from "svelte/store";

    export const devices = derived(
        devicesStore,
        $devices => $devices.map(device => {
            return {...device, safeId: device.id.replace("/", "%2f")}
        })
    );
</script>

<style>
    ul {
        margin: 0 0 1em 0;
        line-height: 1.5;
    }
</style>

<svelte:head>
    <title>Devices</title>
</svelte:head>

<div>

    <h1 class="flex-cols-1">Available devices</h1>

    <div class="grid grid-cols-1 gap-10 ">
        {#each $devices as device}
            <!-- we're using the non-standard `rel=prefetch` attribute to
                    tell Sapper to load the data for the page as soon as
                    the user hovers over the link or taps it, instead of
                    waiting for the 'click' event -->
            <div class="bg-yellow-100 hover:bg-yellow-300 p-4">
                <a rel="prefetch" class="h-full w-full" href="devices/{device.safeId}">{device.id} - {device.name}</a>
            </div>
        {/each}
    </div>
</div>
