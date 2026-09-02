


export type UrlValidationResult =
    | { valid: true; candidates: string[] }
    | { valid: false; reason: "invalid-ip" | "invalid-url" }


    
export class UrlResolver
{
    private static readonly DEFAULT_PORT = 9710
    private static readonly IPV4_REGEX = /^(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}$/
    private static readonly IP_SHAPE_REGEX = /^\d{1,3}(\.\d{1,3}){3}$/


    public static validate(raw: string): UrlValidationResult
    {
        const input = raw.trim()
        const host_part = this.extractHost(input)

        if (this.IP_SHAPE_REGEX.test(host_part) && !this.IPV4_REGEX.test(host_part))
        {
            return { valid: false, reason: "invalid-ip" }
        }

        const candidates = this.buildCandidates(input).filter(this.isSyntacticallyValid)

        if (candidates.length === 0)
        {
            return { valid: false, reason: "invalid-url" }
        }

        return { valid: true, candidates }
    }


    private static extractHost(input: string): string
    {
        const protocol_match = input.match(/^(https?):\/\//i)
        const without_protocol = protocol_match ? input.slice(protocol_match[0].length) : input
        return without_protocol.split("/")[0].split(":")[0].replace(/^\[|\]$/g, "")
    }


    private static buildCandidates(raw: string): string[]
    {
        const input = raw.trim()
        const protocol_match = input.match(/^(https?):\/\//i)
        const address = protocol_match ? input.slice(protocol_match[0].length) : input
        const host_part = address.split("/")[0]
        const has_port = /:\d+$/.test(host_part.replace(/^\[.*\]/, ""))
        const host_with_port = has_port ? host_part : `${host_part}:${this.DEFAULT_PORT}`

        if (protocol_match)
        {
            return [`${protocol_match[1].toLowerCase()}://${host_with_port}`]
        }

        return [`http://${host_with_port}`, `https://${host_with_port}`]
    }


    private static isSyntacticallyValid(candidate: string): boolean
    {
        return URL.canParse(candidate)
    }
}