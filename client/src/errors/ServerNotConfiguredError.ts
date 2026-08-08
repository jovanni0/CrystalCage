


export class ServerNotConfiguredError extends Error
{
    constructor(message: string)
    {
        super(message);
        this.name = "ServerNotConfiguredError"
        Object.setPrototypeOf(this, ServerNotConfiguredError.prototype)
    }
}