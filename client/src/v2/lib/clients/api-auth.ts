import type { PingResponse }         from "@root/dtos/PingResponse"
import type { HttpClient }           from "./http-client"
import type { SessionHttp }          from "./session-http"
import type { AuthLoginRequest }     from "@root/dtos/AuthLoginRequest"
import type { AuthLoginResponse }    from "@root/dtos/AuthLoginResponse"
import type { InviteCreateResponse } from "@root/dtos/InviteCreateResponse"
import type { UserProfileResponse }  from "@root/dtos/UserProfileResponse"
import type { SignupRequest } from "../dtos/signup.dto"



export class ApiAuth
{
    constructor(private http: HttpClient, private session_http: SessionHttp) 
    {}



    /*------ BEFORE LOGIN ------*/



    public ping(base_url: string)
    { 
        return this.http.send<PingResponse>(base_url, "/ping", { 
            signal: AbortSignal.timeout(3000) 
        }) 
    }
    

    public login(base_url: string, body: AuthLoginRequest)
    { 
        return this.http.send<AuthLoginResponse>(base_url, "/auth/login", { 
            method: "POST", body 
        }) 
    }
    

    public signUp(base_url: string, body: SignupRequest)
    { 
        return this.http.send<void>(base_url, "/auth/signup", { 
            method: "POST", body 
        }) 
    }


    public isInviteCodeValid(base_url: string, code: string)
    { 
        return this.http.send<boolean>(base_url, `/auth/invite-code/${encodeURIComponent(code)}`) 
    }



    /*------ AFTER LOGIN ------*/



    public me()
    { 
        return this.session_http.send<UserProfileResponse>("/auth/me", { 
            signal: AbortSignal.timeout(4000) 
        }) 
    }


    public createInviteCode()
    { 
        return this.session_http.send<InviteCreateResponse>("/auth/invite-code", { 
            method: "POST" 
        }) 
    }
}