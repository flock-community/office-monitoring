<script lang="ts">
  import { Tabs, Tab } from "svelte-chota/cmp";
  import DeviceList from "./DeviceList.svelte";
  import Charts from "./components/Charts.svelte";

  let active_tab = 0;
</script>

<div class="flex flex-col h-4/6 h-min-500">
  <Tabs bind:active={active_tab}>
    <Tab>Alles</Tab>
    <Tab>Temperatuur</Tab>
  </Tabs>
  <Charts activeChart={active_tab} />
</div>
<div class="h-2/6">
  <DeviceList />
</div>
