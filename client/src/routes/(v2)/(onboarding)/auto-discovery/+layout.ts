import { Capacitor } from "@capacitor/core"
import { redirect }  from "@sveltejs/kit"



export const load = () => 
{
    if (!Capacitor.isNativePlatform())
    {
        redirect(307, "/manual-discovery")
    }
}