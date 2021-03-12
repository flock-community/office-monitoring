<script lang="ts">
  import DoorOpen from "svelte-material-icons/DoorOpen.svelte";
  import DoorClosed from "svelte-material-icons/DoorClosed.svelte";
  import { deviceStateStore } from "../../../services/stores";
  import type { ContactSensorState } from "../../../services/StreamDtos";

  export let id: String;
  let isOpen: Boolean;
  let deviceStates = $deviceStateStore.filter((state) => state.deviceId === id);

  if (deviceStates.length > 0) {
    let latestState = (deviceStates[
      deviceStates.length - 1
    ] as unknown) as ContactSensorState;
    isOpen = latestState.contact;
  } else {
    console.warn("No latest state found for device: " + id);
    isOpen = true;
  }
</script>

{#if isOpen}
  <DoorOpen height="100%" width="100%" />
{:else}
  <DoorClosed height="100%" width="100%" />
{/if}
