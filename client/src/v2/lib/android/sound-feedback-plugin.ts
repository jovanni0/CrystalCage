import { Capacitor, registerPlugin } from "@capacitor/core"



const SoundFeedback = registerPlugin<{ trigger(): Promise<void> }>("SoundFeedback")



export function triggerSoundEffect(): Promise<void>
{
    if (!Capacitor.isNativePlatform) 
    {
        return Promise.resolve()
    }

    return SoundFeedback.trigger()
}