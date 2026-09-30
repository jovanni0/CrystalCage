


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


    // SHOULD BE IMPROVED TO TRY TO INSERT THE STANDARD PORT AND DO NOT WAIT UNTILL ALL FETCH SUCCES, BECAUSE WAITING AFTER A FIREWALL/REVERSE PROXY MAY TAKE
    // UP TO 30 SEC.
    private static buildCandidates(raw: string): string[]
    {
        const input = raw.trim();
        const protocol_match = input.match(/^(https?):\/\//i);
        const without_protocol = protocol_match ? input.slice(protocol_match[0].length) : input;
        const host_and_port = without_protocol.split("/")[0];
        // 1. If user already typed the protocol (e.g. "https://example.com" or "http://192.168.1.5:9710")
        if (protocol_match)
        {
            return [`${protocol_match[1].toLowerCase()}://${host_and_port}`];
        }
        // 2. If no protocol was typed, try HTTPS first, then HTTP
        return [
            `https://${host_and_port}`,
            `http://${host_and_port}`
        ];
    }


    private static isSyntacticallyValid(candidate: string): boolean
    {
        return URL.canParse(candidate)
    }
}