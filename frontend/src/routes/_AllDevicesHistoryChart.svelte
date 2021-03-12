<script lang="ts">
    import {devicesStore, deviceStateStore} from "../services/stores";
    import type {
        ContactSensorState,
        DeviceState,
        TemperatureSensorState,
    } from "../services/StreamDtos";
    import {DeviceType} from "../services/StreamDtos";
    import type {TimelineChartRecord} from "./model";
    import DeviceHistoryChart from "./_DeviceHistoryChart.svelte";
    import {derived} from "svelte/store";

    const convertToChartData = (
        deviceStates: DeviceState<ContactSensorState>[]
    ) => {
        return deviceStates
            .map((state: DeviceState<ContactSensorState>) => {
                if (state.body.contact === false) {
                    let openedOnDate = state.date;

                    // The states are per device so the next one must be the close state, if it's not found the sensor is still open
                    let closedOnDate =
                        deviceStates.find((s) => s.date > state.date && s.body.contact)
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

    // const chartData = derived(devicesStore, store => {
    //     return store.map(x => {
    //         console.log("devicesStore ...")
    //         return x
    //     }).filter((device) => {
    //         console.log("Filtering chart data")
    //         return device.type === DeviceType.CONTACT_SENSOR;
    //     })
    //         .flatMap((contactSensor) => {
    //             const contactSensorStates = $deviceStateStore.filter(
    //                 (state) => state.deviceId === contactSensor.id
    //             );
    //             const contactSensorStatesTyped = contactSensorStates as DeviceState<ContactSensorState>[];
    //             return convertToChartData(contactSensorStatesTyped);
    //         })
    //         .filter((chartData) => chartData !== undefined);
    // })

    const chartData = $deviceStateStore.map(state => {
        console.log("Something deviceState")
        return $devicesStore
            .filter((device) => {
            console.log("Filtering chart data")
            return device.type === DeviceType.CONTACT_SENSOR;
        })
        .flatMap((contactSensor) => {
            const contactSensorStates = $deviceStateStore.filter(
                (state) => state.deviceId === contactSensor.id
            );
            const contactSensorStatesTyped = contactSensorStates as DeviceState<ContactSensorState>[];
            return convertToChartData(contactSensorStatesTyped);
        })
        .filter((chartData) => chartData !== undefined)
    }
    )
</script>

<DeviceHistoryChart {chartData}/>
