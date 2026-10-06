import type { SessionStatus }  from "@root/types/SessionStatus"
import      { toSearchParams } from "@root/utils/SearchParams"



export class SessionRules
{
    /*
    * Where a session in `status` must go when it is on `route_id`, or `null` if it may stay.
    */
    public static redirectFor(status: SessionStatus, route_id: string | null, base_url: string | null, server_name: string | null): string | null
    {
        const is_onboarding = route_id?.includes("onboarding") ?? false

        if (status === "UNKNOWN")
        {
            return null
        }

        if (status === "AUTHENTICATED")
        {
            return is_onboarding ? "/" : null
        }

        if (is_onboarding)
        {
            return null
        }

        if (status === "NEEDS_LOGIN")
        {
            const search_params = toSearchParams({ address: base_url, name: server_name })

            return search_params ? `/login?${search_params}` : "/auto-discovery"
        }

        return "/welcome"
    }
}
