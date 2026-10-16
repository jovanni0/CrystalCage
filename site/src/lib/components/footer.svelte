<script lang="ts">
    import Fdroid      from "./icons/fdroid.svelte";
    import Gitea       from "./icons/gitea.svelte";
    import Github      from "./icons/github.svelte";
    import Obtainium   from "./icons/obtainium.svelte";
    import SanctumLogo from "./icons/sanctum-logo.svelte";
</script>



<footer>
    <top-wrapper>
        <brand-wrapper>
            <brand-top>
                <SanctumLogo 
                    width  = { 48 }
                    height = { 48 }
                />
        
                <p class = "brand">Sanctum</p>
            </brand-top>
    
            <p class = "tag-line">Your library. Your rules.</p>
        </brand-wrapper>
    
        <nav aria-label = "footer">
            <nav-group>
                <p class = "group-header">Docs</p>
    
                <ul>
                    <li><a href = "/app">App</a></li>
                    <li><a href = "/hosting">Hosting</a></li>
                </ul>
            </nav-group>

            <nav-group>
                <p class = "group-header">Project</p>
    
                <ul>
                    <li><a href = "/roadmap">Roadmap</a></li>
                    <li><a href = "/contact">Contact</a></li>
                    <li><a href = "https://github.com/jovanni0/CrystalCage/issues">Report an issue</a></li>
                    <li><a href = "https://github.com/jovanni0/CrystalCage">Source code</a></li>
                </ul>
            </nav-group>

            <nav-group>
                <p class = "group-header">Get the app</p>
    
                <ul>
                    <li><a 
                        href = "..."
                        class = "icon-text"
                    >
                        <Github />
                        GitHub
                    </a></li>
                    <li><a 
                        href = "..."
                        class = "icon-text"
                    >
                        <Gitea />
                        Gitea
                    </a></li>
                    <li><a 
                        href = "..."
                        class = "icon-text"
                    >
                        <Fdroid />
                        F-Droid
                    </a></li>
                    <li><a 
                        href = "..."
                        class = "icon-text"
                    >
                        <Obtainium />
                        Obtainium
                    </a></li>
                </ul>
            </nav-group>
        </nav>
    </top-wrapper>


    <bottom-wrapper>
        <left-wrapper>
            <p>© 2026 jovanni0 · AGPL 3.0 · </p>
            <button
                type = "button"
                class = "link-button"
            >
                Privacy settings
            </button>
        </left-wrapper>

        <p class = "made-with">Built with SvelteKit</p>
    </bottom-wrapper>
</footer>



<style>
    footer
    {
        display: flex;
        flex-direction: column;

        padding: 32px 32px 16px 32px;
        gap: 48px;

        background-color: var(--text);
        color: var(--text-inverted-muted);

        top-wrapper
        {
            display: flex;
            flex-direction: row;
            justify-content: space-between;

            brand-wrapper
            {
                display: flex;
                flex-direction: column;
                align-items: center;
                gap: 16px;

                font-family: Lora;

                brand-top
                {
                    display: flex;
                    flex-direction: row;
                    align-items: center;
                    gap: 12px;

                    font-size: var(--text-display-sm);
                }

                p.tag-line
                {
                    font-size: var(--text-body-lg);
                }
            }

            p.group-header
            {
                font-weight: 800;
            }

            nav
            {
                display: flex;
                flex-direction: row;
                gap: 128px;

                nav-group
                {
                    display: flex;
                    flex-direction: column;
                    gap: 24px;
    
                    ul
                    {
                        margin: 0;
                        padding: 0;
                        list-style: none;
    
                        display: flex;
                        flex-direction: column;
                        gap: 12px;
        
                        a
                        {
                            color: currentColor;
                            text-decoration: none;
    
                            &:hover
                            {
                                text-decoration: underline;
                            }

                            &.icon-text
                            {
                                display: flex;
                                flex-direction: row;
                                align-items: center;
                                gap: 8px;
                            }
                        }
                    }
                }
            }
        }

        bottom-wrapper
        {
            display: flex;
            flex-direction: row;
            justify-content: space-between;

            left-wrapper
            {
                display: flex;
                flex-direction: row;
                gap: 4px;
            }
        }
    }
</style>