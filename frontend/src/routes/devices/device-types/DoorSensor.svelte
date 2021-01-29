<script>
  import DoorOpen from "svelte-material-icons/DoorOpen.svelte";
  import DoorClosed from "svelte-material-icons/DoorClosed.svelte";

  let isOpen = true;
</script>

{#if isOpen}
  <DoorOpen height="100%" width="100%" />
{:else}
  <DoorClosed height="100%" width="100%" />
{/if}
