<script lang="ts">
  import { DeviceSubscription } from "../services/StreamDtos";
  import { eventBus } from "../services/EventBus";
  import { hasData } from "../services/stores";
  import { onMount } from "svelte";
  import Dashboard from "./_dashboard/Dashboard.svelte";
  import LoadingIndicator from "./_LoadingIndicator.svelte";

  onMount(() => {
    eventBus.request(new DeviceSubscription());
  });
</script>

<svelte:head>
  <title>Flock.Office Monitor</title>
</svelte:head>

<div class="p-3 h-full w-full">
  {#if $hasData}
    <Dashboard />
  {:else}
    <LoadingIndicator />
  {/if}
</div>

<style>
  :global(:root) {
    --color-primary: #f8e008;
  }
</style>
