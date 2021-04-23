<script lang="typescript">
    import LineChart from "./_components/LineChart.svelte";
    import {LineChartDateRecord, LineChartRecord} from "./model";
    import {get, writable} from "svelte/store";
    import {DeviceState, DeviceType, TemperatureSensorState,} from "../services/StreamDtos";
    import {devicesStore, deviceStateStore} from "../services/stores";

    let chartData: LineChartRecord[] = new Array();

    const chartRecordStore = writable<LineChartRecord[]>([]);

    const tempOptions = [18, 19, 20, 21, 22];
    const sensorNames = ["Bier", "Dakterras"];

    let lineChartDateRecords: LineChartDateRecord[] = [];

    let test = [];

    devicesStore.subscribe(devices => {

        const test = devices
            .filter(device => device.type === DeviceType.TEMPERATURE_SENSOR)
            .map(tempSensor => {
                get(deviceStateStore)
                    .get(tempSensor.id)
                    .map(tempRecord => {



                    })
            })
    })


    // Dummy data
    setInterval(() => {
        const temp = tempOptions[Math.floor(Math.random() * tempOptions.length)];
        const sensor = sensorNames[Math.floor(Math.random() * sensorNames.length)];

        const deviceState: DeviceState<TemperatureSensorState> = {
            id: sensor,
            type: DeviceType.TEMPERATURE_SENSOR,
            date: new Date(),
            deviceId: sensor,
            state: {
                lastSeen: new Date(),
                humidity: 90,
                pressure: 1000,
                temperature: temp,
                battery: 1,
                voltage: 1
            },
        };

        const eventsForDate = lineChartDateRecords.find(
            (item) => item.date === deviceState.date
        ) || {
            date: deviceState.date,
            events: [],
        };

        eventsForDate.events.push({
            name: deviceState.deviceId,
            value: 12, //deviceState.temperature,
            text: "hoi",
        });

        test[deviceState.date.toISOString()] = eventsForDate;

        let chartRecord = {
            name: sensor,
            date: new Date(),
            text: "humidity: 90",
            value: temp,
        };

        chartRecordStore.update((records) => [...records, chartRecord]);

        //console.log("chartData", get(chartRecordStore));
        //console.log("test", test);
    }, 1000);
</script>

<div class="p-3 h-full w-full">
    <LineChart {chartRecordStore}/>
</div>
