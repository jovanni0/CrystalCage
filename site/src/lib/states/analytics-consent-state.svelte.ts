import { PUBLIC_GA_MEASUREMENT_ID } from "$app/env/public"
import { dev }                      from "$app/env"


type ConsentChoice = "granted" | "denied" | null

declare global
{
    interface Window
    {
        dataLayer: unknown[]
        gtag:      (...args: unknown[]) => void
    }
}


const STORAGE_KEY = "analytics-consent"



class AnalyticsConsent
{
    choice: ConsentChoice = $state(null)
    is_ready              = $state(false)   // false until the saved choice has been read

    private is_loaded = false


    restore()
    {
        this.choice = readChoice()
        this.is_ready = true

        if (this.choice === "granted")
        {
            this.load()
        }
    }

    accept()
    {
        this.choice = "granted"
        saveChoice("granted")
        this.load()
    }

    reject()
    {
        const was_granted = this.is_loaded

        this.choice = "denied"
        saveChoice("denied")

        if (was_granted)
        {
            window.gtag("consent", "update", { analytics_storage: "denied" })
            deleteAnalyticsCookies()
        }
    }

    // "Privacy settings" in the footer: show the card again
    reopen()
    {
        this.choice = null
        console.log("GA consent opened")
    }


    private load()
    {
        if (this.is_loaded)
        {
            return
        }

        this.is_loaded = true

        window.dataLayer = window.dataLayer || []
        window.gtag = function ()
        {
            // gtag needs the real `arguments` object, not an array
            window.dataLayer.push(arguments)
        }

        window.gtag("consent", "default", {
            analytics_storage:  "granted",
            ad_storage:         "denied",
            ad_user_data:       "denied",
            ad_personalization: "denied",
        })

        window.gtag("js", new Date())
        window.gtag("config", PUBLIC_GA_MEASUREMENT_ID, dev ? { debug_mode: true } : {})

        const script = document.createElement("script")
        script.async = true
        script.src = `https://www.googletagmanager.com/gtag/js?id=${ PUBLIC_GA_MEASUREMENT_ID }`

        document.head.append(script)

        console.log("GA loaded")
    }
}



function readChoice(): ConsentChoice
{
    try
    {
        const stored = localStorage.getItem(STORAGE_KEY)

        if (stored === "granted" || stored === "denied")
        {
            return stored
        }
    }
    catch
    {
    }

    return null
}


function saveChoice(choice: "granted" | "denied")
{
    try
    {
        localStorage.setItem(STORAGE_KEY, choice)
    }
    catch
    {
    }
}


function deleteAnalyticsCookies()
{
    const cookie_names = document.cookie
        .split(";")
        .map((cookie) => cookie.split("=")[0].trim())
        .filter((name) => name.startsWith("_ga"))

    for (const name of cookie_names)
    {
        document.cookie = `${ name }=; max-age=0; path=/; domain=.${ location.hostname }`
        document.cookie = `${ name }=; max-age=0; path=/`
    }
}



export const analytics_consent = new AnalyticsConsent()