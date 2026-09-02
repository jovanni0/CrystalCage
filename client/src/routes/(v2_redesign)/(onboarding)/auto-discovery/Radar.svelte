<script lang="ts">
    import type { HTMLAttributes } from "svelte/elements";
    import      Logo               from "@root/components/v2_redesign/Logo.svelte";



    type DotInfo = {
        left:   number
        top:    number
        radius: number
    }

    // values are in percentages.
    const WAVE_START_POINT     = 25
    const MIN_DOT_SPAWN_RADIUS = 30
    const MAX_DOT_SPAWN_RADIUS = 85

    let {
        servers        = [],
        cycle_duration = 3000,
        style,
        ...rest
    } : HTMLAttributes<HTMLDivElement> & {
        servers?:        { id: string, name: string}[],
        cycle_duration?: number
        style?:          string
    } = $props()


    const dot_positions = new Map<string, DotInfo>()

    const dots = $derived(servers.map(it => ({ id: it.id, name: it.name, ...assingDotPosition(it.id) })))


    function assingDotPosition(id: string): DotInfo
    {
        let dot_info = dot_positions.get(id)

        if (dot_info)
        {
            return dot_info
        }

        const spawn_angle = Math.random() * Math.PI * 2
        const spawn_radius = MIN_DOT_SPAWN_RADIUS + Math.random() * (MAX_DOT_SPAWN_RADIUS - MIN_DOT_SPAWN_RADIUS)

        const center_distance = spawn_radius / 2
        const left = 50 + center_distance * Math.cos(spawn_angle)
        const top = 50 + center_distance *Math.sin(spawn_angle)


        dot_info = { left, top, radius: spawn_radius }
        dot_positions.set(id, dot_info)

        return dot_info
    }
</script>



<div
    class = "radar"
    style = "--cycle-duration: { cycle_duration }ms; { style }"
    { ...rest }
>
    <div class = "ring" style = "width: 30%; height: 30%;"></div>
    <div class = "ring" style = "width: 60%; height: 60%;"></div>
    <div class = "ring" style = "width: 90%; height: 90%;"></div>

    <div class = "pulse"></div>

    {#each dots as dot (dot.id)}
        <div
            class = "dot"
            style = "left: { dot.left }%; top: { dot.top }%; --delay-factor: {(dot.radius - WAVE_START_POINT) / (100 - WAVE_START_POINT)};"
            title = { dot.name }
        ></div>
    {/each}

    <Logo class = "logo" style = "width: 20%; height: 20%" />
</div>



<style>
    @property --wave-progress 
    {
        syntax: '<number>';
        inherits: true;
        initial-value: 0;
    }

    .radar
    {
        position: relative;

        width:  100%;
        max-width: 400px;
        aspect-ratio: 1;

        margin: 0 auto;
        
        transition: width 0.4s ease;
        animation: radar-sweep var(--cycle-duration) linear infinite;
    }
    
    .ring
    {
        position: absolute;
        top: 50%;
        left: 50%;

        border-radius: 50%;
        border: 1px solid var(--text-inverted-muted);
        
        transform: translate(-50%, -50%);
    }

    .pulse
    {
        position: absolute;
        top: 50%;
        left: 50%;
        
        border-radius: 50%;
        border: 2px solid var(--bg-brand-secondary);
        
        transform: translate(-50%, -50%);
        
        width: calc(25% + var(--wave-progress) * 75%);
        height: calc(25% + var(--wave-progress) * 75%);
        opacity: calc(1 - var(--wave-progress));
    }

    .dot
    {
        --diff: calc(var(--wave-progress) - var(--delay-factor));
        --flash: max(0, 1 - abs(var(--diff) * 20));

        position: absolute;
        width: 6px;
        height: 6px;

        border-radius: 50%;
        
        background-color: var(--bg-brand-secondary);
        
        transform: translate(-50%, -50%) scale(calc(1 + 0.4 * max(0, 1 - abs(var(--diff) * 20))));
        filter: drop-shadow(0 0 calc(2px * var(--flash)) var(--bg-brand-secondary));
    }

    :global(.logo)  
    {
        position: absolute;
        top: 50%;
        left: 50%;

        transform: translate(-50%, -50%);
    }

    @keyframes radar-sweep 
    {
        from 
        { 
            --wave-progress: 0; 
        }
        
        to 
        { 
            --wave-progress: 1; 
        }
    }
</style>