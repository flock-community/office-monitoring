<script lang="ts">
    import { deviceStates } from "../../../services/stores";
    import DeviceHistoryChart from "./DeviceHistoryChart.svelte";
    import { createChartRecords } from "../../../services/AllDevicesChartDataResolver";
    import { debounceMaxInterval } from "../../_utils";

    const updateChartData = debounceMaxInterval(states => {
        chartData = createChartRecords(states)
    }, 333);

    let chartData = [];
    deviceStates.subscribe(async (states) => {
        await updateChartData(states);
    });
</script>

<DeviceHistoryChart {chartData}/>
