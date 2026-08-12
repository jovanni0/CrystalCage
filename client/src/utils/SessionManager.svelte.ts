import type { UserProfileResponse } from "../dtos/UserProfileResponse";
import type { SessionStorage }      from "./SessionStorage";
import type { AuthContext }         from "@data-types/AuthContext";
import type { LoginResult }         from "@data-types/LoginResult";
import type { SessionStatus }       from "@root/types/SessionStatus";
import type { Logger }              from "@loggers/Logger";
import      { ApiClient }           from "./ApiClient"
import      { isNotNull, isNull }   from "@root/utils/NullishChecks";
import      { NotFoundError }       from "@root/errors/NotFoundError";
import      { UnauthorizedError }   from "@root/errors/UnauthorizedError";



export class SessionManager
{
    private base_url: string | null = null
    private session_token: string | null = null
    private server_name: string | null = null
    private current_user: UserProfileResponse | null = null

    status = $state<SessionStatus>("UNKNOWN")


    private getAuthContext(): AuthContext | null
    {
        if (this.base_url === null)
        {
            return null
        }

        return {
            base_url: this.base_url,
            session_token: this.session_token
        }
    }


    private handleInvalidSession()
    {
        this.session_token = null
        this.current_user = null
        this.storage.clear()
        this.status = isNull(this.base_url) ? "NO_SERVER" : "NEEDS_LOGIN"
    }


    constructor(
        private api: ApiClient, 
        private storage: SessionStorage,
        private logger: Logger,
    )
    {
        this.api.configureAuth(
            () => this.getAuthContext(),
            () => this.handleInvalidSession()
        )
    }


    getServerName()
    {
        return this.server_name
    }


    hasServerContext(): boolean
    {
        return isNotNull(this.base_url)
    }


    async restoreSession(): Promise<void>
    {
        const persisted_session = await this.storage.load()

        if (isNull(persisted_session))
        {
            this.status = "NO_SERVER"

            return
        }

        this.base_url = persisted_session.base_url
        this.session_token = persisted_session.session_token

        if (isNull(this.session_token))
        {
            this.status = "NEEDS_LOGIN"

            return
        }

        try
        {
            this.current_user = await this.api.getMe()
            this.status = "AUTHENTICATED"

            return
        }
        catch
        {
            this.status = "NEEDS_LOGIN"

            return
        }
    }


    /**
     * checks the connection with the server. returns `true` if the server was successfully pinged, else `false`
     * @param url
     */
    async connectToServer(url: string): Promise<boolean>
    {
        this.base_url = url

        try
        {
            const response = await this.api.ping()

            this.server_name = response.server_name

            await this.storage.save({
                base_url: this.base_url,
                server_name: this.server_name,
                session_token: null
            })

            this.status = "NEEDS_LOGIN"

            return true
        }
        catch (e)
        {
            this.logger.error("caught error while trying to ping server", { error: e })

            return false
        }
    }


    /**
     * performs the login process.
     * @param username
     * @param password
     */
    async login(username: string, password: string): Promise<LoginResult>
    {
        try
        {
            const response = await this.api.login({ username, password })

            this.session_token = response.token
            this.current_user = response.user

            await this.storage.save({
                base_url: this.base_url!,
                server_name: this.server_name!,
                session_token: this.session_token
            })

            this.status = "AUTHENTICATED"

            this.logger.info("received /login response", { response: response })
        }
        catch (e)
        {
            switch (true)
            {
                case e instanceof NotFoundError:        return { success: false, error_reason: "NETWORK_ERROR" }
                case e instanceof UnauthorizedError:    return { success: false, error_reason: "INVALID_CREDENTIALS" }
                default:                                return { success: false, error_reason: "UNKNOWN_ERROR" }
            }
        }

        return { success: true }
    }


    logout()
    {
        this.handleInvalidSession()
    }
}