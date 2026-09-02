


export type NetworkRequestOutcome =
    | { kind: "SUCCESS", server_name: string }
    | { kind: "ABORTED" }
    | { kind: "ERROR"; reason: "TIMEOUT" | "NOT-FOUND" | "REFUSED" | "TLS" | "UNKNOWN" }
    | { kind: "ERROR"; reason: "SERVER"; status: number; message?: string }
    | { kind: "ERROR"; reason: "WRONG-SERVER" }



const REASON_PRIORITY: Record<string, number> = {
    "WRONG-SERVER": 0, // most specific — something answered, just not Sanctum
    "SERVER":   0, // a real server answered — best possible signal
    "TLS":      1, // something is listening, handshake failed
    "REFUSED":  1, // something is listening, this port/service isn't it
    "TIMEOUT":  2, // ambiguous — filtered, or just slow, or nothing there
    "NOT-FOUND": 3, // hostname didn't resolve at all
    "UNKNOWN":  4,
}



export class Network
{
    /**
     * Best-effort classifier for network errors. Message-matching is inherently
     * fragile (undocumented platform strings) — always has a safe UNKNOWN fallback.
     */
    public static classifyError(e: unknown): NetworkRequestOutcome
    {
        const message = e instanceof Error ? e.message.toLowerCase() : String(e).toLowerCase()

        if (message.includes("timed out") || message.includes("timeout"))
        {
            return { kind: "ERROR", reason: "TIMEOUT" }
        }

        if (message.includes("hostname") || message.includes("nsurlerrordomain error -1003") || message.includes("could not be found"))
        {
            return { kind: "ERROR", reason: "NOT-FOUND" }
        }

        if (message.includes("connection refused") || message.includes("could not connect") || message.includes("nsurlerrordomain error -1004"))
        {
            return { kind: "ERROR", reason: "REFUSED" }
        }

        if (message.includes("ssl") || message.includes("certificate") || message.includes("cert"))
        {
            return { kind: "ERROR", reason: "TLS" }
        }

        return { kind: "ERROR", reason: "UNKNOWN" }
    }


    public static messageFor(outcome: NetworkRequestOutcome): string | undefined
    {
        switch (outcome.kind)
        {
            case "SUCCESS":
            case "ABORTED":
                return undefined

            case "ERROR":
                switch (outcome.reason)
                {
                    case "SERVER":       return outcome.message ?? `Server responded with an error (${outcome.status}).`
                    case "TLS":          return "Found a server, but couldn't establish a secure connection."
                    case "REFUSED":      return "Found a host, but the connection was refused — check the port."
                    case "TIMEOUT":      return "The server took too long to respond."
                    case "NOT-FOUND":    return "No server found at that address."
                    case "WRONG-SERVER": return "Found a server at that address, but it isn't a Sanctum server."
                    case "UNKNOWN":      return "Could not reach a server at this address."
                }
        }
    }


    /**
     * Picks the single most informative failure out of several parallel outcomes.
     * A real HTTP response beats a network-level guess; "not found" is the least
     * useful signal, so it only wins when nothing better is available.
     */
    public static pickMostRelevant(outcomes: NetworkRequestOutcome[]): NetworkRequestOutcome | undefined
    {
        const failures = outcomes.filter((o): o is Extract<NetworkRequestOutcome, { kind: "ERROR" }> => o.kind === "ERROR")

        if (failures.length === 0) return undefined

        return failures.reduce((best, current) =>
            REASON_PRIORITY[current.reason] < REASON_PRIORITY[best.reason] ? current : best
        )
    }
}