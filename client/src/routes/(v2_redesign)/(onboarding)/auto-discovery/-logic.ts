import type { DiscoveryResult } from "@root/utils/ServerDiscovery"



export function resultToSearchParams(result: DiscoveryResult): string
{
    const address = `${result.https ? "https" : "http"}://${result.host}:${result.port}`
    const search_params = new URLSearchParams({ "address": address, "name": result.name })

    return search_params.toString()
}