<script lang="ts">
    import DoorSensor from "./device-types/DoorSensor.svelte";
    import {devicesStore} from "../../services/stores";
    import EventBus from "../../services/EventBus";
    import {DeviceStateSubscription} from "../../services/StreamDtos";

    export let id: String;
    export let name: String;


    const date = new Date()
    date.setHours(date.getHours() - 10)
    setTimeout(() => {
        console.log("Requesting deviceState for device:", id)
        EventBus.request(new DeviceStateSubscription(id, date));
    });

</script>

<div class="bg-gray-100 p-12 border rounded shadow">
    <div class="h-24 self-center">
        <DoorSensor {id}/>
    </div>
    <div class="text-xl text-center">
        {name}
    </div>
</div>
