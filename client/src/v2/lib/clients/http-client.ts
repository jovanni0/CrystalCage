import type { Logger }              from "@root/loggers/Logger"
import      { ApiError }            from "@root/errors/ApiError"
import      { ConflictError }       from "@root/errors/ConflictError"
import      { InternalServerError } from "@root/errors/InternalServerError"
import      { NotFoundError }       from "@root/errors/NotFoundError"
import      { UnauthorizedError }   from "@root/errors/UnauthorizedError"



export type RequestOptions = {
    method?:  "GET" | "POST" | "PATCH" | "PUT" | "DELETE"
    body?:    unknown
    headers?: Record<string, string>
    signal?:  AbortSignal
}



export class HttpClient
{
    constructor(private logger: Logger) 
    {}


    public async send<type_T>(base_url: string, path: string, options: RequestOptions = {}): Promise<type_T>
    {
        const is_form = options.body instanceof FormData
        const headers = { 
            ...(options.body && !is_form ? { "Content-Type": "application/json" } : {}), 
            ...options.headers 
        }

        const response = await fetch(`${base_url}${path}`, {
            method:  options.method ?? "GET",
            headers,
            body:    options.body === undefined ? undefined : is_form ? options.body as FormData : JSON.stringify(options.body),
            signal:  options.signal,
        })


        if (!response.ok)
        {
            const message = await response.text()
            
            if (response.status === 401) throw new UnauthorizedError(message)
            if (response.status === 404) throw new NotFoundError(message)
            if (response.status === 409) throw new ConflictError(message)
            if (response.status >= 500)  throw new InternalServerError(message)

            throw new ApiError(response.status, message)
        }

        if (response.status === 204) 
        {
            return undefined as type_T
        }

        return response.headers.get("Content-Type")?.includes("json") 
            ? response.json() 
            : response.blob() as type_T
    }
}