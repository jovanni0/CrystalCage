import { edition_format_color_registry } from "@root/CompositionRoot";
import { EditionData } from "../data-models/edition-data";
import { ReadData } from "../data-models/read-data";
import type { EditionRepository } from "../repositories/edition-repository.svelte";
import type { ReadRecordRepository } from "../repositories/read-record-repository.svelte";
import { Optional } from "../_other/optional";
import type { EditionFileFormatDefinition } from "../utils/format-color-registry.svelte";




export class ReadDetailsStore
{
    private read_repo:    ReadRecordRepository
    private edition_repo: EditionRepository
    private book_id:      string

    public is_open:        boolean       = $state(false)
    public read_id:        string | null = $state(null)
    public start_date:     string | null = $state(null)
    public end_date:       string | null = $state(null)
    public total_time:     number | null = $state(null)
    public edition_label:  string | null = $state(null)
    public edition_format: EditionFileFormatDefinition | null = $state(null)
    public state:          "In progress" | "DNF" | null = $state(null)
    public is_historical:  boolean = $state(false)
    public notes:         string | null = $state(null)


    constructor(read_repo: ReadRecordRepository, edition_repo: EditionRepository, book_id: string)
    {
        this.read_repo = read_repo
        this.edition_repo   = edition_repo
        this.book_id   = book_id
    }


    public openFor(read_id: string)
    {
        const reads = this.read_repo.get(this.book_id)
        const target_read = reads.find(read => read.id === read_id)
        
        if (!target_read)
        {
            return
        }
        
        if (target_read.edition_id !== null)
        {
            const editions = this.edition_repo.get(this.book_id)
            const target_edition = editions.find(edition => edition.id === target_read.edition_id)

            if (!target_edition)
            {
                return
            }

            const edition_data = new EditionData(
                target_edition.id, 
                target_edition.label ?? "Unnamed Edition", 
                edition_format_color_registry.resolve(target_edition.format)
            )

            const read_target_data = ReadData.fromResponse(target_read, Optional.from(edition_data))

            if (read_target_data.ok)
            {
                this.dataToView(read_target_data.value)
                this.is_open = true
            }
        }
        else
        {
            
            const read_target_data = ReadData.fromResponse(target_read, Optional.none())
            
            if (read_target_data.ok)
            {
                this.dataToView(read_target_data.value)
                this.is_open = true
            }
        }
    }


    private dataToView(read_data: ReadData)
    {
        switch (read_data.kind)
        {
            case "finished":
                this.read_id        = read_data.id
                this.start_date     = read_data.start_date.toString()
                this.end_date       = read_data.end_date.toString()
                this.total_time     = read_data.start_date.daysUntil(read_data.end_date)
                this.edition_label  = read_data.edition.getOrNull()?.label ?? null
                this.edition_format = read_data.edition.getOrNull()?.format ?? null
                this.state          = null
                this.is_historical  = false
                this.notes          = read_data.notes.getOrNull()
                break;

            case "in progress":
                this.read_id        = read_data.id
                this.start_date     = read_data.start_date.toString()
                this.end_date       = null
                this.total_time     = null
                this.edition_label  = read_data.edition.getOrNull()?.label ?? null
                this.edition_format = read_data.edition.getOrNull()?.format ?? null
                this.state          = "In progress"
                this.is_historical  = false
                this.notes          = read_data.notes.getOrNull()
                break;

            case "abandoned":
                this.read_id        = read_data.id
                this.start_date     = read_data.start_date.toString()
                this.end_date       = read_data.end_date.toString()
                this.total_time     = read_data.start_date.daysUntil(read_data.end_date)
                this.edition_label  = read_data.edition.getOrNull()?.label ?? null
                this.edition_format = read_data.edition.getOrNull()?.format ?? null
                this.state          = "DNF"
                this.is_historical  = false
                this.notes          = read_data.notes.getOrNull()
                break;

            case "historical":
                this.read_id        = read_data.id
                this.start_date     = null
                this.end_date       = null
                this.total_time     = null
                this.edition_label  = read_data.edition.getOrNull()?.label ?? null
                this.edition_format = read_data.edition.getOrNull()?.format ?? null
                this.state          = read_data.is_abandoned ? "DNF" : null
                this.is_historical  = true
                this.notes          = read_data.notes.getOrNull()
                break;
        }
    }
}