<script lang="ts">
  import DoorSensor from "./device-types/DoorSensor.svelte";

  export let name: String;
</script>

<div class="bg-gray-100 p-12 border rounded shadow">
  <div class="h-24 self-center">
    <DoorSensor />
  </div>
  <div class="text-xl text-center">
    {name}
  </div>
</div>
