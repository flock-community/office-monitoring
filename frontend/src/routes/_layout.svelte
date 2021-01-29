<script lang="ts">
    import Nav from "../components/Nav.svelte";
</script>

<div class="flex flex-col h-full">
    <Nav/>
    <main class="container mx-auto flex flex-grow">
        <slot/>
    </main>
</div>

<style>
</style>
