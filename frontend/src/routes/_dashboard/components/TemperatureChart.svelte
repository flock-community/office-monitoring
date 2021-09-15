<script lang="typescript">
    import LineChart from "./LineChart.svelte";
    import {derived, get} from "svelte/store";
    import type {DeviceState, TemperatureSensorState,} from "../../../services/StreamDtos";
    import {DeviceType} from "../../../services/StreamDtos";
    import {devices, deviceStates} from "../../../services/stores";

    let tempSensorIds: string[] = [];

    const getTempsensors = () => {
        return get(devices).filter(
            (device) => device.type === DeviceType.TEMPERATURE_SENSOR
        );
    };

    const chartRecords = derived(deviceStates, (states) => {
        return getTempsensors().flatMap((sensor) => {
            tempSensorIds.push(sensor.id);

            const eventsForSensor = states.get(
                sensor.id
            ) as DeviceState<TemperatureSensorState>[];

            return eventsForSensor.map((event) => {
                const recordForDate = {
                    date: new Date(event.date),
                };
                recordForDate[sensor.id] = event.state.temperature;
                return recordForDate;
            });
        });
    });

    // FIXME: de linechart subscribed zich op chartRecords maar blijkbaar is dat niet genoeg om de derived store te initialiseren, get(chartRecords) doet dat wel
    get(chartRecords);
</script>

<div class="p-3 flex-grow w-full">
    <LineChart {chartRecords} {tempSensorIds}/>
</div>
