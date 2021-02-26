<script lang="ts">
  import { totalmem } from "os";

  import {
    ContactSensorState,
    DeviceState,
    DeviceType,
    TimelineChartRecord,
  } from "./model";

  import DeviceHistoryChart from "./_DeviceHistoryChart.svelte";
  import { groupBy } from "./_utils";

  const now = new Date();
  const before = new Date();
  before.setHours(before.getHours() - 2);

  const states: DeviceState[] = [
    {
      id: "1",
      type: DeviceType.CONTACT_SENSOR,
      deviceId: "door-1",
      date: before,
      body: {
        lastSeen: now,
        battery: 1,
        voltage: 1,
        contact: false,
      },
    },
    {
      id: "2",
      type: DeviceType.CONTACT_SENSOR,
      deviceId: "door-1",
      date: now,
      body: {
        lastSeen: now,
        battery: 1,
        voltage: 1,
        contact: true,
      },
    },
  ];

  const groupedPerDevice = groupBy(states, (state) => state.deviceId);

  const chartData: TimelineChartRecord[] = [];
</script>

<DeviceHistoryChart {chartData} />
