<script lang="ts">
  import PowerSocketEu from "svelte-material-icons/PowerSocketEu.svelte";
  import { deviceStates } from "../../../services/stores";
  import type {
    DeviceState,
    StateBody,
    SwitchState,
  } from "../../../services/StreamDtos";

  export let id: string;
  let state: string;

  deviceStates.subscribe((value: Map<string, DeviceState<StateBody>[]>) => {
    const deviceStates = value.get(id) || [];
    if (deviceStates.length > 0) {
      let latestState = (deviceStates[deviceStates.length - 1] as unknown)
        .state as SwitchState;
      state = latestState.state;
    } else {
      // console.warn("No state found for device (yet): " + id);
      state = "?";
    }
  });
</script>

<PowerSocketEu height="75%" width="100%" />
<div class="text-xl text-center">
  {state}
</div>
