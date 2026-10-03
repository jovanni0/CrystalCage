import { api_auth, 
         logger, 
         session } from "@root/CompositionRoot"
import { attempt } from "@v2/lib/utils/result"



export async function logIn(base_url: string, server_name: string, username: string, password: string): Promise<string | null>
{
    const result = await attempt(logger, "log in", () => api_auth.login(base_url, { username, password }))

    if (!result.ok)
    {
        return result.error.kind === "unauthorized" ? "Username or password are invalid."
             : result.error.kind === "network"      ? "Can't reach the server."
             : "Something went wrong."
    }

    await session.signIn(base_url, server_name, result.value.token, result.value.user)
    
    return null
}