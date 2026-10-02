import type { Logger }              from "@root/loggers/Logger"
import      { ConflictError }       from "@root/errors/ConflictError"
import      { InternalServerError } from "@root/errors/InternalServerError"
import      { NotFoundError }       from "@root/errors/NotFoundError"
import      { UnauthorizedError }   from "@root/errors/UnauthorizedError"



export type ErrorKind = 
    | "conflict" 
    | "not_found" 
    | "unauthorized" 
    | "network" 
    | "server" 
    | "unknown"



export type AppError = { 
    kind: ErrorKind, 
    message: string, 
    cause?: unknown 
}


export type Result<T> =
    | { ok: true,  value: T }
    | { ok: false, error: AppError }



/* 
*  Alias so you can skip wrapping `Result` in `Promise` each time.
*/
export type AsyncResult<T> = Promise<Result<T>>



export function toAppError(e: unknown): AppError
{
    if (e instanceof ConflictError)       return { kind: "conflict",     message: e.message, cause: e }
    if (e instanceof NotFoundError)       return { kind: "not_found",    message: e.message, cause: e }
    if (e instanceof UnauthorizedError)   return { kind: "unauthorized", message: e.message, cause: e }
    if (e instanceof InternalServerError) return { kind: "server",       message: e.message, cause: e }
    if (e instanceof TypeError)           return { kind: "network",      message: "Can't reach the server", cause: e }
    return { kind: "unknown", message: String(e), cause: e }
}



/*
* Runs an async action, logs any failure once, and never throws.
*/
export async function attempt<T>(logger: Logger, action: string, fn: () => Promise<T>): Promise<Result<T>>
{
    try
    {
        return { 
            ok: true, 
            value: await fn() 
        }
    }
    catch (e)
    {
        const error = toAppError(e)

        logger.error(`failed to ${action}`, { kind: error.kind, error: e })

        return { 
            ok: false, 
            error 
        }
    }
}