<script lang="ts">
    import DoorSensor from "./device-types/DoorSensor.svelte";
    import {devicesStore} from "../../services/stores";
    import EventBus from "../../services/EventBus";
    import {DeviceStateSubscription} from "../../services/StreamDtos";

    export let id: String;
    export let name: String;


    console.log("Requesting stuff for device:", id)
    const date = new Date()
    date.setHours(date.getHours() - 10)
    setTimeout(() => EventBus.request(new DeviceStateSubscription(id, date)),500);

</script>

<div class="bg-gray-100 p-12 border rounded shadow">
    <div class="h-24 self-center">
        <DoorSensor {id}/>
    </div>
    <div class="text-xl text-center">
        {name}
    </div>
</div>
