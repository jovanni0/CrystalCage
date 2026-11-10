import { Optional } from "./optional"



export namespace CoverSheetData
{
    export type Type = CoverSheetData.Empty | CoverSheetData.Fetching | CoverSheetData.Error | CoverSheetData.Create | CoverSheetData.Update

    export class Empty
    {
        public readonly kind = "empty"
    }


    export class Fetching
    {
        public readonly kind = "fetching"
    }


    export class Error
    {
        public readonly kind = "error"

        public readonly error: string


        constructor(error: string)
        {
            this.error = error
        }
    }


    export class Create
    {
        public readonly kind = "create"

        public readonly file:     File
        public readonly size:     string
        public readonly format:   string
        public readonly label:    Optional<string>
        


        constructor(file: File, size_px: string, format: string, label: Optional<string>)
        {
            this.file = file
            this.size = size_px
            this.format = format
            this.label = label
        }


        public setLabel(value: Optional<string>): CoverSheetData.Create
        {
            return new CoverSheetData.Create(this.file, this.size, this.format, value)
        }
    }


    export class Update
    {
        public readonly kind = "update"

        public readonly cover_id:       string
        public readonly label:          Optional<string>
        public readonly original_label: Optional<string>
        public readonly size:           string
        public readonly format:         string



        constructor(cover_id: string, label: Optional<string>, original_label: Optional<string>, size_px: string, format: string)
        {
            this.cover_id = cover_id
            this.label = label
            this.original_label = original_label
            this.size = size_px
            this.format = format
        }


        public setLabel(value: Optional<string>): CoverSheetData.Update
        {
            return new CoverSheetData.Update(this.cover_id, value, this.original_label, this.size, this.format)
        }
    }
}