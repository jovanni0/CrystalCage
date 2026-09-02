import type { NetworkRequestOutcome } from "@root/utils/Network"
import      { Capacitor, 
              CapacitorHttp }         from "@capacitor/core"
import      { Network }               from "@root/utils/Network"



type PingResponse = { type: "Sanctum"; server_name: string }


function isValidPingResponse(x: unknown): x is PingResponse
{
    return typeof x === "object" && x !== null
        && (x as any).type === "Sanctum"
        && typeof (x as any).server_name === "string"
}


export async function pingCandidate(url: string, signal: AbortSignal): Promise<{ outcome: NetworkRequestOutcome, name: string | null }>
{
    try
    {
        let status: number
        let parsed: unknown
        let server_name: string | null

        if (Capacitor.isNativePlatform())
        {
            const response = await CapacitorHttp.get({ 
                url: `${url}/ping`,
                connectTimeout: 800,
                readTimeout: 800,
            })

            status = response.status
            parsed = response.data
        }
        else
        {
            const response = await fetch(
                `${url}/ping`, 
                { 
                    signal: signal,
                }
            )

            status = response.status
            parsed = await response.json().catch(() => undefined)
        }

        server_name = (parsed as any).server_name ?? null

        if (signal.aborted) 
        {
            return { 
                outcome: { kind: "ABORTED" },
                name:    server_name
            }
        }

        if (status !== 200)
        {
            const message = (parsed && typeof parsed === "object") ? (parsed as any).message : undefined

            return { 
                outcome: { kind: "ERROR", reason: "SERVER", status, message },
                name:    server_name
            }
        }

        if (!isValidPingResponse(parsed))
        {
            return {
                outcome: { kind: "ERROR", reason: "WRONG-SERVER" },
                name:    server_name
            }
        }

        return {
            outcome: { kind: "SUCCESS", server_name: parsed.server_name },
            name:    server_name
        }
    }
    catch (e)
    {
        if (signal.aborted || (e instanceof DOMException && e.name === "AbortError")) 
        {
            return {
                outcome: { kind: "ABORTED" },
                name:    null
            }
        }

        return {
            outcome: Network.classifyError(e),
            name:    null
        }
    }
}