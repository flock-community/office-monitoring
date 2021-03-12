<script lang="ts">
    import {devicesStore, deviceStateStore} from "../services/stores";
    import type {ContactSensorState, DeviceState,} from "../services/StreamDtos";
    import {DeviceType} from "../services/StreamDtos";
    import type {TimelineChartRecord} from "./model";
    import DeviceHistoryChart from "./_DeviceHistoryChart.svelte";
    import {get} from "svelte/store";

    const convertToChartData = (
        deviceStates: DeviceState<ContactSensorState>[]
    ) => {
        return deviceStates
            .map((state: DeviceState<ContactSensorState>) => {
                if (state.state.contact === false) {
                    let openedOnDate = state.date;

                    // The states are per device so the next one must be the close state, if it's not found the sensor is still open
                    let closedOnDate =
                        deviceStates.find((s) => s.date > state.date && s.state.contact)
                            ?.date || new Date();

                    let record: TimelineChartRecord = {
                        category: state.deviceId,
                        start: openedOnDate,
                        end: closedOnDate,
                    };

                    return record;
                }
            })
            .filter((s) => s !== undefined);
    };


    let chartData = []

    // TODO: Limit repaints to once every x msec?
    deviceStateStore.subscribe(state => {
            chartData = get(devicesStore)
                .filter((device) => device.type === DeviceType.CONTACT_SENSOR)
                .flatMap((contactSensor) => {
                    const contactSensorStates = get(deviceStateStore).filter(
                        (state) => state.deviceId === contactSensor.id
                    );
                    console.debug(`Updating chart data for  contact sensor ${contactSensor.id}:`, contactSensorStates)
                    const contactSensorStatesTyped = contactSensorStates as DeviceState<ContactSensorState>[];
                    return convertToChartData(contactSensorStatesTyped);
                })
                .filter((chartData) => chartData !== undefined)
        }
    )
</script>

<DeviceHistoryChart {chartData}/>
