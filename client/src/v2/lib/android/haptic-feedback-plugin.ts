import { Capacitor, registerPlugin } from "@capacitor/core"



export type HapticType = "tick" | "segment" | "key" | "confirm" | "reject" | "none"



const HapticFeedback = registerPlugin<{
    trigger(o?: { haptic?: HapticType }): Promise<boolean>
}>("HapticFeedback")



export function triggerHapticEffect(type: HapticType): Promise<boolean>
{
    if (!Capacitor.isNativePlatform) 
    {
        return Promise.resolve(false)
    }

    return HapticFeedback.trigger({ haptic: type })
}