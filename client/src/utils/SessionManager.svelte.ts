import type { UserProfileResponse } from "../dtos/UserProfileResponse";
import type { SessionStorage }      from "./SessionStorage";
import type { AuthContext }         from "@data-types/AuthContext";
import type { SessionStatus }       from "@root/types/SessionStatus";
import type { Logger }              from "@loggers/Logger";
import      { ApiClient }           from "./ApiClient"



export class SessionManager
{
    public user:           UserProfileResponse | null = $state(null)
    public status:         SessionStatus              = $state("UNKNOWN")
    public server_address: string | null              = $state(null)
    public server_name:    string | null              = $state(null )
    public session_token:  string | null              = $state(null)


    public has_sever_context: boolean = $derived(this.server_address !== null)


    private getAuthContext(): AuthContext | null
    {
        if (this.server_address === null)
        {
            return null
        }

        return {
            base_url:      this.server_address,
            session_token: this.session_token
        }
    }


    constructor(
        private api: ApiClient, 
        private storage: SessionStorage,
        private logger: Logger,
    )
    {
        this.api.configureAuth(
            () => this.getAuthContext(),
            () => this.logout()
        )

        this.logger.info("set auth context")
    }


    async restoreSession(): Promise<void>
    {
        const persisted_session = await this.storage.load()

        if (persisted_session === null)
        {
            this.status = "NO_SERVER"

            return
        }

        this.server_address = persisted_session.base_url
        this.server_name = persisted_session.server_name
        this.session_token = persisted_session.session_token

        if (this.session_token === null)
        {
            this.status = "NEEDS_LOGIN"

            return
        }

        try
        {
            this.user = await this.api.getMe()
            this.status = "AUTHENTICATED"

            return
        }
        catch
        {
            this.status = "NEEDS_LOGIN"

            return
        }
    }


    public login(address: string, token: string, user: UserProfileResponse)
    {
        this.server_address = address
        this.session_token = token
        this.status = "AUTHENTICATED"
    }


    public logout()
    {
        this.storage.clear()

        this.session_token = null
        this.user = null
        this.status = this.server_address === null ? "NO_SERVER" : "NEEDS_LOGIN"
    }
}