import type { AuthorResponse }          from "@root/dtos/AuthorResponse"
import type { BookDetailResponse }      from "@root/dtos/BookDetailResponse"
import type { EditionResponse }         from "@root/dtos/EditionResponse"
import type { ReadRecordResponse }      from "@root/dtos/ReadRecordResponse"
import type { ReadRecordUpdateRequest } from "@root/dtos/ReadRecordUpdateRequest"
import type { Logger }                  from "@root/loggers/Logger"
import type { ApiClient }               from "@root/utils/ApiClient"
import      { toLocalDateString }       from "@root/utils/DataConversion"
import      { formatAuthorName }        from "@root/utils/LibraryGroupings"
import      { getIntervalInDays }       from "@root/utils/ReadRecordUtils"



type EditionEntry = {
    id:     string
    label:  string | null
    format: string
}



export class ReadManagementStore
{
    private read_record:         ReadRecordResponse | null = $state(null)
    private book:                BookDetailResponse | null = $state(null)
    private author:              AuthorResponse  | null    = $state(null)
    private editions:            EditionResponse[]         = $state([])
    
    public start_date:          string | null = $state(null)
    public end_date:            string | null = $state(null)
    public notes:               string | null = $state(null)
    public selected_edition_id: string | null = $state(null)
    
    private selected_edition: EditionEntry | null = $derived(this.editions.find(it => it.id === this.selected_edition_id) ?? null)
    
    public book_title:      string         = $derived(this.book?.title ?? "")
    public author_name:     string         = $derived(this.author ? formatAuthorName(this.author) : "")
    public total_time:      number | null  = $derived(getIntervalInDays(this.start_date, this.end_date))
    public pace:            number | null  = $derived(this.book?.word_count && this.total_time ? this.book?.word_count / this.total_time : null)
    public edition_options: EditionEntry[] = $derived(this.editions.map(it => ({ id: it.id, label: it.label, format: it.format}) ))
    public edition_label:   string         = $derived(this.selected_edition?.label ?? "")
    public edition_format:  string         = $derived(this.selected_edition?.format ?? "Unknown")


    constructor(
        private api: ApiClient,
        private logger: Logger,
        private id: string
    )
    {}


    public async initialize()
    {
        this.logger.info("initializing ReadManagementStore")

        try
        {
            this.read_record = await this.api.getReadRecord(this.id)
            this.book = await this.api.getBook(this.read_record.book_id)
    
            const [author, editions] = await Promise.all([
                this.api.getAuthorById(this.book.author_id),
                this.api.getEditions(this.book.id),
            ])
    
            this.author = author
            this.editions = editions

            this.selected_edition_id = this.read_record.edition_id
            this.start_date = this.read_record.start_date
            this.end_date = this.read_record.end_date
            this.notes = this.read_record.notes
        }
        catch (e)
        {
            this.logger.error("failed to fetch read info", { error: e })
        }
    }


    public async save(): Promise<boolean>
    {
        try
        {
            const record: ReadRecordUpdateRequest = {
                edition_id: this.selected_edition_id !== this.read_record?.edition_id ? this.selected_edition_id : undefined,
                start_date: this.start_date !== this.read_record?.start_date 
                    ? this.start_date ? toLocalDateString(new Date(this.start_date)) : undefined 
                    : undefined,
                end_date: this.end_date !== this.read_record?.end_date 
                    ? this.end_date ? toLocalDateString(new Date(this.end_date)) : undefined
                    : undefined,
                did_not_finish: undefined,
                is_historical: undefined,
                stop_position: undefined,
                notes: this.notes !== this.read_record?.notes ? this.notes : undefined
            }

            const has_changes = Object.values(record).some(value => value !== undefined)

            if (has_changes)
            {
                await this.api.updateReadRecord(this.id, record)
            }
            
            return true
        }
        catch (e)
        {
            this.logger.error("failed to update read info", { error: e })

            return false
        }
    }


    public async deleteRecord(): Promise<boolean>
    {
        if (!this.id)
        {
            this.logger.warn("cannot delete record with no record ID", { id: this.id })
            return false
        }

        try
        {
            this.api.deleteReadRecord(this.id)
            return true
        }
        catch (e)
        {
            this.logger.error("cannot delete read record", { id: this.id, error: e })
            return false
        }
    }
}
