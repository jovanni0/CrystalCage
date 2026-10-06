


export class DateRules
{
    /*
        convert an ISO date string to a RISO string.
    */
    public static ISOtoRISO(date: string): string
    {
        if (!date) 
            return ""

        const [year, month, day] = date.split("-")

        return `${day}-${month}-${year}`;
    }


    public static RISOtoISO(date: string): string
    {
        if (!date) 
        {
            return ""
        }

        const [day, month, year] = date.split("-")

        return `${year}-${month}-${day}`;
    }

    public static RISOinsertDashes(raw: string): string
    {
        const digits = raw
            .replace(/\D/g, "")
            .slice(0, 8)

        if (digits.length > 4)
        {
            return `${digits.slice(0, 2)}-${digits.slice(2, 4)}-${digits.slice(4)}`
        }

        if (digits.length > 2)
        {
            return `${digits.slice(0, 2)}-${digits.slice(2)}`
        }

        return digits
    }

    public static isDateValid(day: number, month: number, year: number)
    {
        const date = new Date(year, month - 1, day)

        return date.getFullYear() === year 
            && date.getMonth() === month - 1
            && date.getDate() === day

    }


    public static isRISOdateValid(date: string)
    {
        const digits = date
            .replace(/\D/g, "")
            .slice(0, 8)

        if (digits.length < 8)
        {
            return true
        }

        const day = Number(digits.slice(0, 2))
        const month = Number(digits.slice(2, 4))
        const year = Number(digits.slice(4, 8))

        return DateRules.isDateValid(day, month, year)
    }


    public static isISOdateValid(date: string)
    {
        const digits = date
            .replace(/\D/g, "")
            .slice(0, 8)

        if (digits.length < 8)
        {
            return true
        }

        const year = Number(digits.slice(0, 4))
        const month = Number(digits.slice(4, 6))
        const day = Number(digits.slice(6, 8))

        return DateRules.isDateValid(day, month, year)
    }
}