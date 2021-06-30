<script lang="ts">
  import AllDevicesHistoryChart from "./AllDevicesHistoryChart.svelte";
  import TemperatureChart from "./TemperatureChart.svelte";

  export let activeChart;
</script>

{#if activeChart === 0}
  <AllDevicesHistoryChart />
{/if}

{#if activeChart === 1}
  <TemperatureChart />
{/if}
