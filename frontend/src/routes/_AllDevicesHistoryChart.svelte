<script lang="ts">
    import {deviceStateStore} from "../services/stores";
    import DeviceHistoryChart from "./_DeviceHistoryChart.svelte";
    import {get} from "svelte/store";
    import {delay} from "./_utils";
    import {Pulse} from 'svelte-loading-spinners'
    import {resolveChartData} from "../services/ChartDataResolver";

    enum ChartUpdateStatus {
        IDLE,
        UPDATING,
        QUEUED
    }

    let chartData = [];

    let _updating: ChartUpdateStatus = ChartUpdateStatus.IDLE

    const updateChartData = async () => {
        if (_updating !== ChartUpdateStatus.IDLE) {
            _updating = ChartUpdateStatus.QUEUED;
            return;
        }

        _updating = ChartUpdateStatus.UPDATING;

        const newChartData = resolveChartData(get(deviceStateStore));
        chartData = [...newChartData]

        await delay(500);
        if (_updating === ChartUpdateStatus.QUEUED) {
            _updating = ChartUpdateStatus.IDLE;
            await updateChartData()
        } else {
            _updating = ChartUpdateStatus.IDLE;
        }
    }
    deviceStateStore.subscribe(async state => {
            if (state.size > 0) {
                await updateChartData();
            }
        }
    )
</script>

{#if chartData.length === 0}
    <div class="h-full flex justify-center items-center flex-col">
        <Pulse size="200" color="#f8e008" unit="px" duration="1.5s"/>
    </div>
{:else }
    <DeviceHistoryChart {chartData}/>
{/if}
<!--<pre>{JSON.stringify(chartData, null, 4)}</pre>-->
