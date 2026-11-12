import { Optional } from "./optional"



export class CalendarDate
{
    public readonly year:  number
    public readonly month: number
    public readonly day:   number


    private constructor(year: number, month: number, day: number)
    {
        this.year = year
        this.month = month
        this.day = day
    }


    public static parse(value: string): Optional<CalendarDate>
    {
        const match_fields = /^(\d{4})-(\d{2})-(\d{2})$/.exec(value)

        if (match_fields === null) 
        {
            return Optional.none()
        }

        const year  = Number(match_fields[1])
        const month = Number(match_fields[2])
        const day   = Number(match_fields[3])
        const date  = new Date(Date.UTC(year, month - 1, day))

        const is_real_date = date.getUTCFullYear() === year && date.getUTCMonth() === month - 1 && date.getUTCDate() === day

        return is_real_date 
            ? Optional.some(new CalendarDate(year, month, day)) 
            : Optional.none()
    }


    public isBefore(other: CalendarDate): boolean
    {
        return this.toUtcMs() < other.toUtcMs()
    }


    public daysUntil(other: CalendarDate): number
    {
        return Math.round((other.toUtcMs() - this.toUtcMs()) / 86_400_000)
    }


    public toString(): string
    {
        const month = String(this.month).padStart(2, '0')
        const day   = String(this.day).padStart(2, '0')

        return `${this.year}-${month}-${day}`
    }


    private toUtcMs()
    {
        return Date.UTC(this.year, this.month - 1, this.day)
    }
}