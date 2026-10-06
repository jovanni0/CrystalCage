import { goto } from "$app/navigation"
import { App } from "@capacitor/app"
import type { AfterNavigate } from "@sveltejs/kit"



export class Nav
{
    private depth:           number = 0
    private is_next_replace: boolean = false


    /*
    *   Goes to the specified URL.
    */
    public goTo(url: string, replace_state: boolean = false)
    {
        this.is_next_replace = replace_state

        goto(url, { replaceState: replace_state })
    }


    /*
    *   Goes back one page in history. If there is no history, navigates to `fallback_url` and replaces the current state.
    */
    public goBack(fallback_url?: string)
    {
        if (this.depth > 0)
        {
            history.back()
        }
        else if (fallback_url)
        {
            this.goTo(fallback_url, true)
        }
    }


    /*
    *   Clears the hisotry stack, so there is nothing to go back to.
    */
    public resetTo(url: string)
    {
        this.goTo(url, true)
        this.depth = 0
    }


    /*
    *   Updates the nav stack depth after a navigation. Handles both `Nav.goTo()` and native `href` naigations.
    */
    public onNavigated(navigation: AfterNavigate)
    {
        switch(navigation.type)
        {
            case "enter":
            {
                this.depth = 0
                
                break
            }
            
            case "popstate":
            {
                const nav_delta = navigation.delta ?? -1
                this.depth = Math.max(0, this.depth + nav_delta)

                break
            }

            case "link":
            case "goto":
            {
                if (!this.is_next_replace)
                {
                    this.depth++
                }

                break
            }
        }

        this.is_next_replace = false
    }


    /*
    *   Handles native platforms (Android) back actions.
    */ 
    public onNativeBack()
    {
        if (this.depth === 0)
        {
            App.exitApp()
        }
        else
        {
            this.goBack()
        }
    }
}