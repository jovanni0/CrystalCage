import { PUBLIC_OLLAMA_MODEL, PUBLIC_OLLAMA_URL } from "$app/env/public"



export type ChatMessage = {
    role:      "user" | "assistant"
    content:   string
    is_error?: boolean
}

export type ChatStatus = "idle" | "waiting" | "streaming"


const SYSTEM_PROMPT = `You are the assistant on the documentation site of Sanctum, a self-hosted book manager.
Answer only from the documentation below. If the documentation does not cover the question, say so plainly.
Keep answers short. Use Markdown lists and inline code for commands, file names and settings.
End every answer that uses the documentation with one line: "Sources: Page › Section", listing the sections you used.

Documentation:
`

// only the latest messages are sent, so the docs at the start of the request always fit in num_ctx
const MAX_HISTORY_MESSAGES = 8



export class ChatStore
{
    public is_open:  boolean       = $state(false)
    public messages: ChatMessage[] = $state([])
    public status:   ChatStatus    = $state("idle")

    public readonly is_empty = $derived(this.messages.length === 0)
    public readonly is_busy  = $derived(this.status !== "idle")

    private docs_context:     string | null          = null
    private abort_controller: AbortController | null = null


    public async send(text: string)
    {
        const question = text.trim()

        if (question === "" || this.is_busy)
        {
            return
        }

        this.messages.push({ role: "user", content: question })
        this.status = "waiting"
        this.abort_controller = new AbortController()

        try
        {
            const docs_context = await this.loadDocsContext()

            const history = this.messages
                .filter((message) => !message.is_error)
                .slice(-MAX_HISTORY_MESSAGES)
                .map((message) => ({ role: message.role, content: message.content }))

            const response = await fetch(`${ PUBLIC_OLLAMA_URL }/api/chat`, {
                method:  "POST",
                headers: { "Content-Type": "application/json" },
                signal:  this.abort_controller.signal,
                body:    JSON.stringify({
                    model:    PUBLIC_OLLAMA_MODEL,
                    stream:   true,
                    options:  { num_ctx: 8192, temperature: 0.2 },
                    messages: [{ role: "system", content: SYSTEM_PROMPT + docs_context }, ...history],
                }),
            })

            if (!response.ok || response.body === null)
            {
                throw new Error(`Ollama responded with ${ response.status }`)
            }

            this.messages.push({ role: "assistant", content: "" })
            const answer = this.messages[this.messages.length - 1]

            this.status = "streaming"

            await readChatStream(response.body, (chunk) =>
            {
                answer.content += chunk
            })
        }
        catch (error)
        {
            const was_stopped = error instanceof DOMException && error.name === "AbortError"

            if (!was_stopped)
            {
                this.messages.push({ role: "assistant", content: "Sorry, could not get a response.", is_error: true })
            }
        }
        finally
        {
            this.status = "idle"
            this.abort_controller = null
        }
    }

    public stop()
    {
        this.abort_controller?.abort()
    }

    public reset()
    {
        this.stop()
        this.messages = []
    }


    private async loadDocsContext(): Promise<string>
    {
        if (this.docs_context === null)
        {
            const response = await fetch("/ai-context.txt")
            this.docs_context = await response.text()
        }

        return this.docs_context
    }
}



async function readChatStream(body: ReadableStream<Uint8Array<ArrayBuffer>>, onText: (text: string) => void)
{
    const reader = body.pipeThrough(new TextDecoderStream()).getReader()
    let buffer = ""

    while (true)
    {
        const { value, done } = await reader.read()

        if (done)
        {
            break
        }

        buffer += value

        const lines = buffer.split("\n")
        buffer = lines.pop() ?? ""

        for (const line of lines)
        {
            if (line.trim() === "")
            {
                continue
            }

            const data = JSON.parse(line) as { message?: { content: string } }

            if (data.message?.content)
            {
                onText(data.message.content)
            }
        }
    }
}



export const chat_store = new ChatStore()