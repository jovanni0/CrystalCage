import type { AuthLoginResponse } from "@root/dtos/AuthLoginResponse"



export async function logIn(username: string, password: string, server_address: string): Promise<string>
{
    try
    {
        const request = { username, password }

        const response = await fetch(
            `${server_address}/auth/login`,
            {
                method: "POST",
                body: JSON.stringify(request),
                headers: { "Content-Type": "application/json" }
            }
        )

        if (response.status === 200)
        {
            const data = await response.json() as AuthLoginResponse
    
            // this data along with the server address should be stored inside the session manager upon integration.
            const session_token = data.token
            const current_user = data.user

            return ""
        }
        else if (response.status == 401)
        {
            return "Username or password are invalid."
        }
        else
        {
            return "Something went wrong."
        }
    }
    catch (e)
    {
        return "Something went wrong."
    }
}



export async function isInviteCodeValid(code: string, server_address: string): Promise<boolean>
{
    const response = await fetch(
        `${server_address}/auth/invite-code/${code}`,
        {
            method: "GET",
        }
    )

    return await response.json() as boolean
}


export async function signUp(code: string, username: string, password: string, server_address: string): Promise<number>
{
    const request = { 
        code:     code,
        username: username,
        password: password,
    }

    const response = await fetch(
        `${server_address}/auth/signup`,
        {
            method: "POST",
            body: JSON.stringify(request),
            headers: { "Content-Type": "application/json" }
        }
    )

    return response.status
}