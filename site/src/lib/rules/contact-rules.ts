


export class ContactRules
{
    private static readonly EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    private static readonly PHONE_PATTERN = /^\+?[\d\s()-]{6,}$/

    public static readonly MESSAGE_MIN_LENGTH = 20


    public static checkNameForError(name: string): string | null
    {
        if (name.trim() === "")
        {
            return "Enter your name."
        }

        return null
    }


    public static checkEmailForError(email: string): string | null
    {
        if (email.trim() === "")
        {
            return "Enter your email address."
        }

        if (!this.EMAIL_PATTERN.test(email.trim()))
        {
            return "Enter a valid email address, like name@example.com."
        }

        return null
    }


    public static checkPhoneForError(phone: string): string | null
    {
        if (phone.trim() === "")
        {
            return "Enter your phone number."
        }

        if (!this.PHONE_PATTERN.test(phone.trim()))
        {
            return "Enter a valid phone number, or leave it empty."
        }

        return null
    }


    public static checkMessageForError(message: string): string | null
    {
        const message_length = message.trim().length

        if (message_length < this.MESSAGE_MIN_LENGTH)
        {
            return `Wite at least ${ this.MESSAGE_MIN_LENGTH } character (${ message_length } so far).`
        }

        return null
    }
}