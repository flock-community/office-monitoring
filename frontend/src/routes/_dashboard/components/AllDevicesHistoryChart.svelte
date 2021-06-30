<script lang="ts">
  import { deviceStates } from "../../../services/stores";
  import DeviceHistoryChart from "./DeviceHistoryChart.svelte";
  import { derived, get, writable } from "svelte/store";

  import { createChartRecords } from "../../../services/AllDevicesChartDataResolver";

  let chartData = derived(deviceStates, (states) => {
    return createChartRecords(states);
  });
</script>

<DeviceHistoryChart {chartData} />
