<script lang="ts">
    import Button from "@root/components/button.svelte";
    import TopbarBack from "@root/components/topbar-back.svelte";
    import { api_client, session } from "@root/CompositionRoot";
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
    <TopbarBack />

    <content-wrapper>
        <h1>Generate invite code</h1>
    
        <p>{ code }</p>
    
        <div class="buttons">
            <Button text = "Sign out" onClick={ () => session.signOut() } type = "secondary"/>
            <Button text = "Generate" onClick={generate} />
        </div>
    </content-wrapper>
</div>


<style>
    .page
    {
        display: flex;
        flex-direction: column;
        height: 100%;
    }

    content-wrapper
    {
        display: flex;
        flex-direction: column;
        flex: 1;
        align-items: center;
        justify-content: center;
        padding: 24px;
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