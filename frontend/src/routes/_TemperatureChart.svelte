<script lang="typescript">
  import LineChart from "./_components/LineChart.svelte";
  import { LineChartDateRecord, LineChartRecord } from "./model";
  import { get, writable } from "svelte/store";
  import {
    DeviceState,
    DeviceType,
    TemperatureSensorState,
  } from "../services/StreamDtos";
  import { devicesStore, deviceStateStore } from "../services/stores";

  let chartData: LineChartRecord[] = new Array();

  const chartRecordStore = writable<LineChartRecord[]>([]);
  let tempSensorIds: string[] = [];

  const tempOptions = [18, 19, 20, 21, 22];
  const sensorNames = ["Bier", "Dakterras"];

  let lineChartDateRecords: LineChartDateRecord[] = [];

  let test = [];

  let testChartData = {};

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
        const test = recordsPerDate.get(event.date) || {
          date: new Date(event.date),
        };
        test[tempSensor.id] = event.state.temperature;

        recordsPerDate.set(event.date, test);

        const record = {
          name: tempSensor.name,
          date: event.date,
          text: `humidity: ${event.state.humidity}%`,
          value: event.state.temperature,
        } as LineChartRecord;
        return record;
      });
    });

    const testData = Array.from(recordsPerDate.values());
    chartRecordStore.update((existingRecords) => testData);
  });
</script>

<div class="p-3 flex-grow w-full">
  <LineChart {chartRecordStore} {tempSensorIds} />
</div>
