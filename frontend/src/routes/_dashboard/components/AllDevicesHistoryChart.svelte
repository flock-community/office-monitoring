<script lang="ts">
  import { deviceStates } from "../../../services/stores";
  import DeviceHistoryChart from "./DeviceHistoryChart.svelte";
  import { get } from "svelte/store";
  import { delay } from "../../_utils";

  import { resolveChartData } from "../../../services/ChartDataResolver";

  enum ChartUpdateStatus {
    IDLE,
    UPDATING,
    QUEUED,
  }

  let chartData = [];

  let _updating: ChartUpdateStatus = ChartUpdateStatus.IDLE;

  const updateChartData = async () => {
    if (_updating !== ChartUpdateStatus.IDLE) {
      _updating = ChartUpdateStatus.QUEUED;
      return;
    }

    _updating = ChartUpdateStatus.UPDATING;

    const newChartData = resolveChartData(get(deviceStates));
    chartData = [...newChartData];

    await delay(500);
    if (_updating === ChartUpdateStatus.QUEUED) {
      _updating = ChartUpdateStatus.IDLE;
      await updateChartData();
    } else {
      _updating = ChartUpdateStatus.IDLE;
    }
  };
  deviceStates.subscribe(async (state) => {
    if (state.size > 0) {
      await updateChartData();
    }
  });
</script>

<DeviceHistoryChart {chartData} />
