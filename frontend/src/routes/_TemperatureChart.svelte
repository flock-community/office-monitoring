<script lang="typescript">
  import LineChart from "./_components/LineChart.svelte";
  import type { LineChartRecord } from "./model";
  import { get, writable } from "svelte/store";
  import {
    DeviceState,
    DeviceType,
    TemperatureSensorState,
  } from "../services/StreamDtos";
  import { devicesStore, deviceStateStore } from "../services/stores";

  const chartRecordStore = writable<LineChartRecord[]>([]);
  let tempSensorIds: string[] = [];

  const getTempsensors = () => {
    return get(devicesStore).filter(
      (device) => device.type === DeviceType.TEMPERATURE_SENSOR
    );
  };

  deviceStateStore.subscribe((deviceStates) => {
    let recordsPerDate = new Map<Date, any>();

    const chartRecords = getTempsensors().flatMap((tempSensor) => {
      tempSensorIds.push(tempSensor.id);

      const tempSensorData = deviceStates.get(
        tempSensor.id
      ) as DeviceState<TemperatureSensorState>[];

      console.log("Devices", deviceStates);

      return tempSensorData.map((event) => {
        const recordForDate = recordsPerDate.get(event.date) || {
          date: new Date(event.date),
        };
        recordForDate[tempSensor.id] = event.state.temperature;

        recordsPerDate.set(event.date, recordForDate);

        const record = {
          name: tempSensor.name,
          date: event.date,
          text: `humidity: ${event.state.humidity}%`,
          value: event.state.temperature,
        } as LineChartRecord;
        return record;
      });
    });

    chartRecordStore.update((existingRecords) =>
      Array.from(recordsPerDate.values())
    );
  });
</script>

<div class="p-3 flex-grow w-full">
  <LineChart {chartRecordStore} {tempSensorIds} />
</div>
