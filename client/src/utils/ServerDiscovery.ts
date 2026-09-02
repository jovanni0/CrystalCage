import { Capacitor, registerPlugin } from "@capacitor/core";



type AndroidDiscoveryResponse = {
    servers: DiscoveryResult[]
}



export type DiscoveryResult = { 
    name:  string
    host:  string
    port:  number
    https: boolean 
}



export class ServerDiscovery
{
    private static AndroidServerDiscovery = registerPlugin<{ 
        discover(opts: { timeout_ms?: number }): Promise<AndroidDiscoveryResponse> 
    }>("AndroidServerDiscovery")



    public static async discoverServers(): Promise<DiscoveryResult[] | null>
    {
        if (!Capacitor.isNativePlatform())
            return null

        const response = await this.AndroidServerDiscovery.discover({ timeout_ms: 2000 })

        return response.servers
    }
}