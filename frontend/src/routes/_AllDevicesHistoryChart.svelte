<script lang="ts">
    import {devicesStore, deviceStateStore} from "../services/stores";
    import type {ContactSensorState, DeviceState,} from "../services/StreamDtos";
    import type {TimelineChartRecord} from "./model";
    import DeviceHistoryChart from "./_DeviceHistoryChart.svelte";
    import {get} from "svelte/store";
    import {delay} from "./_utils";
    import type {Color} from "@amcharts/amcharts4/core";
    import * as am4core from "@amcharts/amcharts4/core";


    enum ChartUpdateStatus {
        IDLE,
        UPDATING,
        QUEUED
    }

    let chartData = []
    const door = "https://image.flaticon.com/icons/svg/59/59801.svg";
    const colorSet = new am4core.ColorSet();
    let _color = new Map<string, Color>();
    const getColor: (deviceId: string) => Color = (deviceId: string) => {
        if (!_color.has(deviceId)) {
            const encodedDeviceId = deviceId.split('').reduce((prev, cur) => prev + cur.charCodeAt(0), 0);
            const derivedColor = colorSet.getIndex(encodedDeviceId % 15);
            _color.set(deviceId, derivedColor);
        }

        return _color.get(deviceId)
    };

    let _deviceNames = new Map<string, string>();
    const getDeviceName: (deviceId: string) => string = (deviceId: string) => {
        if (!_deviceNames.has(deviceId)) {
            _deviceNames.set(deviceId, get(devicesStore).find(it => it.id == deviceId)?.name || "Roque device 42")
        }

        return _deviceNames.get(deviceId);
    }
    const convertToChartData = (deviceStates: DeviceState<ContactSensorState>[]) => {

        return deviceStates
            .map((state: DeviceState<ContactSensorState>) => {
                if (state.state.contact === false) {
                    let openedOnDate = state.date;

                    // The states are per device so the next one must be the close state, if it's not found the sensor is still open
                    let closedOnDate =
                        deviceStates.find((s) => s.date > state.date && s.state.contact)
                            ?.date || new Date();

                    let record: TimelineChartRecord = {
                        category: getDeviceName(state.deviceId),
                        start: openedOnDate,
                        end: closedOnDate,
                        icon: door,
                        text: `${getDeviceName(state.deviceId)} geopend van [bold]${openedOnDate}[/] tot [bold]${closedOnDate}[/]`,
                        color: getColor(state.deviceId)
                    };
                    return record;
                }
            })
            .filter((s) => s !== undefined);
    };

    let _updating: ChartUpdateStatus = ChartUpdateStatus.IDLE
    const updateChartData = async () => {
        if (_updating !== ChartUpdateStatus.IDLE) {
            _updating = ChartUpdateStatus.QUEUED;
            return;
        }

        _updating = ChartUpdateStatus.UPDATING;

        chartData = []
        for (let [deviceId, deviceStateArray] of get(deviceStateStore).entries()) {
            // console.debug(`Updating chart data for  contact sensor ${deviceId} (${deviceStateArray.length} entries`)
            // TODO: deal with all sorts of states (not only ContactSensor)
            const contactSensorStatesTyped = deviceStateArray as DeviceState<ContactSensorState>[];
            chartData = [...chartData, ...convertToChartData(contactSensorStatesTyped)];
        }

        await delay(250);
        if (_updating === ChartUpdateStatus.QUEUED) {
            _updating = ChartUpdateStatus.IDLE;
            await updateChartData()
        } else {
            _updating = ChartUpdateStatus.IDLE;
        }
    }

    deviceStateStore.subscribe(state => {
            updateChartData();
        }
    )
</script>

<DeviceHistoryChart {chartData}/>
<!--<pre>{JSON.stringify(chartData, null, 4)}</pre>-->
