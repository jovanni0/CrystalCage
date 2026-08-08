import type { AuthContext }                 from "@data-types/AuthContext";
import type { AuthLoginRequest }            from "../dtos/AuthLoginRequest.ts";
import type { AuthLoginResponse }           from "../dtos/AuthLoginResponse.ts";
import type { UserProfileResponse }         from "../dtos/UserProfileResponse.ts";
import type { PingResponse }                from "../dtos/PingResponse.ts";
import      { isNullOrUndefined }           from "@root/utils/NullishChecks";
import      { ServerNotConfiguredError }    from "@root/errors/ServerNotConfiguredError";
import      { UnauthorizedError }           from "@root/errors/UnauthorizedError";
import      { NotFoundError }               from "@root/errors/NotFoundError";



export class ApiClient
{
    private getAuthContext: (() => AuthContext | null) | null = null
    private onUnauthorized: (() => void) | null = null


    configureAuth(getAuthContext: () => AuthContext | null, onUnauthorized: () => void)
    {
        this.getAuthContext = getAuthContext
        this.onUnauthorized = onUnauthorized
    }


    private async request(path: string, options: RequestInit): Promise<Response>
    {
        const context = this.getAuthContext?.()

        if (isNullOrUndefined(context))
        {
            throw new ServerNotConfiguredError(`request ${path} cannot be made because no context was configured`)
        }

        const headers = {
            ...options.headers,
            ...(context.session_token ? { Authorization: `Bearer ${context.session_token}`} : {}),
        }

        const response = await fetch(`${context.base_url}${path}`, { ...options, headers })

        switch (response.status)
        {
            case 401:
                this.onUnauthorized?.()
                throw new UnauthorizedError(`request ${path} returned 401 Unauthorized`)
            case 404:
                throw new NotFoundError(`request ${path} returned 404 Not Found`)
        }

        return response
    }


    async ping(): Promise<PingResponse>
    {
        console.log(`sending ping to server...`)

        const response = await this.request("/ping", {})

        return response.json()
    }


    async login(body: AuthLoginRequest): Promise<AuthLoginResponse>
    {
        const response = await this.request(
            "/auth/login",
            {
                method: "POST",
                body: JSON.stringify(body),
                headers: { "Content-Type": "application/json" }
            }
        )

        return response.json()
    }


    async getMe(): Promise<UserProfileResponse>
    {
        const response = await this.request(
            "/auth/me",
            {
                method: "GET",
            }
        )

        return response.json()
    }
}