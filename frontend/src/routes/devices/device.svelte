<script lang="ts">
  import DoorSensor from "./device-types/DoorSensor.svelte";
  import { eventBus } from "../../services/EventBus";
  import {
    DeviceStateSubscription,
    DeviceType,
  } from "../../services/StreamDtos";
  import TemperatureSensor from "./device-types/TemperatureSensor.svelte";
  import Socket from "./device-types/Socket.svelte";

  export let id: String;
  export let name: String;
  export let type: DeviceType;
</script>

<a href="/devices/{id}" class="bg-gray-100 p-12 border rounded shadow ">
  <div class="h-24 self-center">
    {#if type === DeviceType.CONTACT_SENSOR}
      <DoorSensor {id} />
    {:else if type === DeviceType.TEMPERATURE_SENSOR}
      <TemperatureSensor {id} />
    {:else if type === DeviceType.SWITCH}
      <Socket {id} />
    {/if}
  </div>
  <div class="text-xl text-center">
    {name}
  </div>
</a>
