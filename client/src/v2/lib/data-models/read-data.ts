import type { ReadRecordResponse } from "@root/dtos/ReadRecordResponse"
import { CalendarDate } from "../_other/calendar-date"
import { Optional }     from "../_other/optional"
import type { EditionData }  from "./edition-data"
import type { Result } from "../utils/result"



export namespace ReadData
{
    export function fromResponse(response: ReadRecordResponse, edition: Optional<EditionData>): Result<ReadData>
    {
        const notes = Optional.from(response.notes)

        if (response.is_historical)
        {
            return {
                ok: true,
                value: ReadData.Historical.create(response.id, edition, notes, response.did_not_finish)
            }
        }

        const start_date = Optional.from(response.start_date).flatMap(CalendarDate.parse)

        if (start_date.isNone())
        {
            return {
                ok: false,
                error: { kind: "invalid", message: `read ${response.id} has no valid start date` }
            }
        }

        const end_date = Optional.from(response.end_date).flatMap(CalendarDate.parse)

        if (end_date.isNone())
        {
            if (response.did_not_finish)
            {
                return {
                    ok: false,
                    error: { kind: "invalid", message: `read ${response.id} is abandoned but has no end date` }
                }
            }

            return { 
                ok: true, 
                value: ReadData.InProgress.create(response.id, start_date.value, edition, notes)
            }
        }

        if (response.did_not_finish)
        {
            const abandoned = ReadData.Abandoned.create(response.id, start_date.value, end_date.value, edition, notes)

            if (abandoned.isNone())
            {
                return {
                    ok: false,
                    error: { kind: "invalid", message: `read ${response.id} has end date before start date` }
                }
            }

            return {
                ok: true,
                value: abandoned.value
            }
        }

        const finished = ReadData.Finished.create(response.id, start_date.value, end_date.value, edition, notes)

        if (finished.isNone())
        {
            return {
                ok: false,
                error: { kind: "invalid", message: `read ${response.id} has end date before start date` }
            }
        }

        return {
            ok: true,
            value: finished.value
        }
    }



    export class Finished
    {
        public readonly kind = "finished"

        public readonly id:         string
        public readonly start_date: CalendarDate
        public readonly end_date:   CalendarDate
        public readonly edition:    Optional<EditionData>
        public readonly notes:      Optional<string>


        constructor(id: string, start_date: CalendarDate, end_date: CalendarDate, edition: Optional<EditionData>, notes: Optional<string>)
        {
            this.id         = id
            this.start_date = start_date
            this.end_date   = end_date
            this.edition    = edition
            this.notes      = notes
        }


        public static create(id: string, start_date: CalendarDate, end_date: CalendarDate, edition: Optional<EditionData>, notes: Optional<string>): Optional<ReadData>
        {
            return start_date.isBefore(end_date)
                ? Optional.from(new ReadData.Finished(id, start_date, end_date, edition, notes))
                : Optional.none()
        }
    }



    export class InProgress
    {
        public readonly kind = "in progress"

        public readonly id:         string
        public readonly start_date: CalendarDate
        public readonly edition:    Optional<EditionData>
        public readonly notes:      Optional<string>


        private constructor(id: string, start_date: CalendarDate, edition: Optional<EditionData>, notes: Optional<string>)
        {
            this.id         = id
            this.start_date = start_date
            this.edition    = edition
            this.notes      = notes
        }


        public static create(id: string, start_date: CalendarDate, edition: Optional<EditionData>, notes: Optional<string>): ReadData
        {
            return new ReadData.InProgress(id, start_date, edition, notes)
        }
    }



    export class Abandoned
    {
        public readonly kind = "abandoned"

        public readonly id:         string
        public readonly start_date: CalendarDate
        public readonly end_date:   CalendarDate
        public readonly edition:    Optional<EditionData>
        public readonly notes:      Optional<string>


        private constructor(id: string, start_date: CalendarDate, end_date: CalendarDate, edition: Optional<EditionData>, notes: Optional<string>)
        {
            this.id         = id
            this.start_date = start_date
            this.end_date   = end_date
            this.edition    = edition
            this.notes      = notes
        }


        public static create(id: string, start_date: CalendarDate, end_date: CalendarDate, edition: Optional<EditionData>, notes: Optional<string>): Optional<ReadData>
        {
            return start_date.isBefore(end_date)
                ? Optional.from(new ReadData.Abandoned(id, start_date, end_date, edition, notes))
                : Optional.none()
        }
    }



    export class Historical
    {
        public readonly kind = "historical"

        public readonly id:           string
        public readonly edition:      Optional<EditionData>
        public readonly notes:        Optional<string>
        public readonly is_abandoned: boolean


        private constructor(id: string, edition: Optional<EditionData>, notes: Optional<string>, is_abandoned: boolean)
        {
            this.id           = id
            this.edition      = edition
            this.notes        = notes
            this.is_abandoned = is_abandoned
        }


        public static create(id: string, edition: Optional<EditionData>, notes: Optional<string>, is_abandoned: boolean): ReadData
        {
            return new ReadData.Historical(id, edition, notes, is_abandoned)
        }
    }
}



export type ReadData = ReadData.Finished | ReadData.InProgress | ReadData.Abandoned | ReadData.Historical
