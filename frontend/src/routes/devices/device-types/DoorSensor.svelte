<script lang="ts">
    import DoorOpen from "svelte-material-icons/DoorOpen.svelte";
    import DoorClosed from "svelte-material-icons/DoorClosed.svelte";
    import {deviceStateStore} from "../../../services/stores";
    import type {ContactSensorState} from "../../../services/StreamDtos";

    export let id: String;
    let isOpen: Boolean;

    deviceStateStore.subscribe(value => {
        const deviceStates = value.filter((state) => state.deviceId === id)
        console.log("Device state:", deviceStates.map(it => ({date: it.date, contact: (it.state as ContactSensorState).contact})));
        if (deviceStates.length > 0) {
            let latestState = (deviceStates[
            deviceStates.length - 1
                ] as unknown).state as ContactSensorState;
            isOpen = latestState.contact;
        } else {
            console.warn("No state found for device (yet): " + id);
            isOpen = true;
        }
    });
</script>

{#if isOpen}
    <DoorOpen height="100%" width="100%"/>
{:else}
    <DoorClosed height="100%" width="100%"/>
{/if}
