import type { LogLevel }    from "@root/types/LogLevel";
import type { Logger }      from "./Logger";
import type { LogContext }  from "@root/types/LogContext";



export class ConsoleLogger implements Logger
{
    constructor(private min_level: LogLevel = "DEBUG")
    { }


    private shouldLog(level: LogLevel): boolean
    {
        const order: LogLevel[] = ["DEBUG", "INFO", "WARN", "ERROR"]

        return order.indexOf(level) >= order.indexOf(this.min_level)
    }


    private serializeContext(context?: LogContext): LogContext | undefined
    {
        if (!context)
        {
            return context
        }

        const result: LogContext = {}

        for (const [key, value] of Object.entries(context))
        {
            if (value instanceof Error)
            {
                result[key] = {
                    message: value.message, 
                    name: value.name, 
                    stack: value.stack
                }
            }
            else
            {
                result[key] = value
            }
        }

        return result
    }


    private write(log_level: LogLevel, message: string, context?: LogContext)
    {
        if (!this.shouldLog(log_level))
        {
            return
        }

        const serialied_context = this.serializeContext(context)

        const entry = 
        {
            timestamp: new Date().toISOString(),
            log_level,
            message,
            ...serialied_context
        }

        switch(log_level)
        {
            case "DEBUG": console.debug(entry); break;
            case "INFO": console.info(entry); break;
            case "WARN": console.warn(entry); break;
            case "ERROR": console.error(entry); break;
        }
    }


    debug(message: string, context?: LogContext): void
    {
        this.write("DEBUG", message, context)
    }


    info(message: string, context?: LogContext): void
    {
        this.write("INFO", message, context)
    }


    warn(message: string, context?: LogContext): void
    {
        this.write("WARN", message, context)
    }


    error(message: string, context?: LogContext): void
    {
        this.write("ERROR", message, context)
    }
}