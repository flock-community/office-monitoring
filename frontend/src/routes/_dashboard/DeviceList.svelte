<script lang="ts">
  import Device from "../devices/device.svelte";
  import { devices as devices, hasData } from "../../services/stores";
</script>

<div class="grid md:grid-cols-3 sm:grid-cols-2 grid-cols-1gap-10">
  {#each $devices as { id, name, type }, i}
    <Device {id} {name} {type} />
  {/each}
</div>
