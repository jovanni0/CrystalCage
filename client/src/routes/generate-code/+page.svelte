<script lang="ts">
    import Button from "@root/components/button.svelte";
    import { api_client } from "@root/CompositionRoot";
    import { goBack } from "@root/utils/go-back";




    let code = $state("")


    async function generate()
    {
        try
        {
            const result = await api_client.generateInviteCode()
            code = result.code
        }
        catch(e)
        {
            code = `Error: ${e?.toString()}`
        }
    }
</script>



<div class="page">
    <h1>Generate invite code</h1>

    <p>{ code }</p>

    <div class="buttons">
        <Button text = "Back" onClick={goBack} type = "secondary"/>
        <Button text = "Generate" onClick={generate} />
    </div>
</div>


<style>
    .page
    {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        padding: 24px;
        height: 100%;
        box-sizing: border-box;
        gap: 64px;
    }

    .buttons
    {
        display: flex;
        flex-direction: row;
        gap: 16px;
    }
</style>