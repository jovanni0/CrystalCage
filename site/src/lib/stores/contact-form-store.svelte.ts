import { ContactRules } from "#lib/rules/contact-rules.ts"



export type ContactTopic = "general" | "feedback" | "hosting" | "other"

export type SendStatus = "idle" | "sending" | "sent" | "failed"



export class ContactFormStore
{
    private was_submitted: boolean = $state(false)

    public status:  SendStatus   = $state("idle")
    public name:    string       = $state("")
    public email:   string       = $state("")
    public phone:   string       = $state("")
    public message: string       = $state("")
    public topic:   ContactTopic = $state("general")

    public readonly name_error:    string | null = $derived(this.was_submitted ? ContactRules.checkNameForError(this.name) : null)
    public readonly email_error:   string | null = $derived(this.was_submitted ? ContactRules.checkEmailForError(this.email) : null)
    public readonly phone_error:   string | null = $derived(this.was_submitted ? ContactRules.checkPhoneForError(this.phone) : null)
    public readonly message_error: string | null = $derived(this.was_submitted ? ContactRules.checkMessageForError(this.message) : null)
    public readonly sending_error: string | null = $derived(this.status === "failed" ? "Couldn't send your message. Try again." : null)

    public readonly is_form_valid: boolean = $derived(!this.name_error && !this.email_error && !this.phone_error && !this.message_error)

    public readonly topic_data = [
        { value: "general",  label: "General" },
        { value: "feedback", label: "Feedback" },
        { value: "hosting",  label: "Hosting" },
        { value: "other",    label: "Other" },
    ]


    public async submit(): Promise<boolean>
    {
        this.was_submitted = true

        if (!this.is_form_valid)
        {
            return false
        }

        this.status = "sending"

        const is_sent = Math.random() < 0.5

        if (is_sent)
        {
            await new Promise(resolve => setTimeout(resolve, 500))  

            this.status = "sent"

            return true
        }

        this.status = "failed"

        return false
    }
}