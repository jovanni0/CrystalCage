import type { Session }                  from "../states/session.svelte"
import type { HttpClient, 
              RequestOptions }           from "./http-client"
import      { ServerNotConfiguredError } from "@root/errors/ServerNotConfiguredError"
import      { UnauthorizedError }        from "@root/errors/UnauthorizedError"



export class SessionHttp
{
    constructor(private http: HttpClient, private session: Session) 
    {}


    public async send<type_T>(path: string, options: RequestOptions = {}): Promise<type_T>
    {
        const { base_url, token } = this.session

        if (!base_url) 
        {
            throw new ServerNotConfiguredError(`request ${path} needs a server`)
        }

        try
        {
            return await this.http.send<type_T>(base_url, path, {
                ...options,
                headers: { 
                    ...options.headers, 
                    ...(token ? { Authorization: `Bearer ${token}` } : {}) 
                },
            })
        }
        catch (e)
        {
            if (e instanceof UnauthorizedError) 
            {   
                void this.session.signOut()
            }
            
            throw e
        }
    }
}