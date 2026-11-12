


export namespace ReadTarget
{
    export class New
    {
        public readonly kind = "new"

        public readonly book_id: string


        constructor(book_id: string)
        {
            this.book_id = book_id
        }
    }



    export class Existing
    {
        public readonly kind = "existing"

        public readonly read_id: string


        constructor(read_id: string)
        {
            this.read_id = read_id
        }
    }
}



export type ReadTarget = ReadTarget.New | ReadTarget.Existing
