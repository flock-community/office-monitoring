<script lang="ts">
  import Thermometer from "svelte-material-icons/Thermometer.svelte";
  import { deviceStates } from "../../../services/stores";
  import type {
    DeviceState,
    StateBody,
    TemperatureSensorState,
  } from "../../../services/StreamDtos";

  export let id: string;
  let temperature: string;

  deviceStates.subscribe((value: Map<string, DeviceState<StateBody>[]>) => {
    const deviceStates = value.get(id) || [];
    if (deviceStates.length > 0) {
      let latestState = (deviceStates[deviceStates.length - 1] as unknown)
        .state as TemperatureSensorState;
      temperature = latestState.temperature.toFixed(0);
    } else {
      // console.warn("No state found for device (yet): " + id);
      temperature = "?";
    }
  });
</script>

<Thermometer height="75%" width="100%" />
<div class="text-xl text-center">
  {temperature}°C
</div>
