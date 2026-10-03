import type { UserProfileResponse } from "@root/dtos/UserProfileResponse"
import type { SessionStatus } from "@root/types/SessionStatus"
import type { SessionStorage } from "../storage/session-storage"



export class Session
{
    private signout_listeners = new Set<() => void>()

    private is_restored: boolean = $state(false)

    public base_url:    string | null              = $state(null)
    public server_name: string | null              = $state(null)
    public token:       string | null              = $state(null)
    public user:        UserProfileResponse | null = $state(null)

    public status: SessionStatus = $derived(
        !this.is_restored ? "UNKNOWN"
        : !this.base_url ? "NO_SERVER"
        : !this.token ? "NEEDS_LOGIN"
        : "AUTHENTICATED"
    )


    constructor(private storage: SessionStorage)
    {}


    public async restore(): Promise<SessionStatus>
    {
        const saved = await this.storage.load()

        this.base_url    = saved?.base_url ?? null
        this.server_name = saved?.server_name ?? null
        this.token       = saved?.token ?? null
        this.is_restored = true

        return this.status
    }


    public async signIn(base_url: string, server_name: string, token: string, user: UserProfileResponse)
    {
        this.base_url    = base_url
        this.server_name = server_name
        this.token       = token
        this.user        = user

        await this.persist()
    }


    public async signOut()
    {
        if (this.token === null) return

        this.token = null
        this.user  = null

        await this.persist()

        this.signout_listeners.forEach(fn => fn())
    }


    public onSignOut(fn: () => void)
    {
        this.signout_listeners.add(fn)
    }


    private persist()
    {
        if (!this.base_url)
        {
            return this.storage.clear()
        }

        return this.storage.save({
            base_url: this.base_url,
            server_name: this.server_name,
            token: this.token
        })
    }
}