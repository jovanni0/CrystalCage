import { toSearchParams } from "@root/utils/SearchParams"
import type { DiscoveryResult } from "@root/utils/ServerDiscovery"



export function resultToSearchParams(result: DiscoveryResult): string
{
    const address = `${result.https ? "https" : "http"}://${result.host}:${result.port}`
    const search_params = toSearchParams({ "address": address, "name": result.name })

    return search_params.toString()
}