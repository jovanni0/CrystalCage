import type { EditionResponse } from "@root/dtos/EditionResponse"
import type { EditionRepository } from "../repositories/edition-repository.svelte"
import type { ReadDraft } from "../types/read-draft"
import type { EditionData } from "../data-models/edition-data"
import { edition_format_color_registry } from "@root/CompositionRoot"
import { ReadTarget } from "../data-models/read-target"
import { Optional } from "../_other/optional"
import { ReadDraftRules } from "../rules/read-draft-rules"
import type { ReadRecordRepository } from "../repositories/read-record-repository.svelte"
import { describeError } from "../utils/result"



export class ReadManageStore
{
    private edition_repo: EditionRepository
    private reads_repo:   ReadRecordRepository
    private book_id:      string
    
    public draft: ReadDraft

    public is_open: boolean          = $state(false)
    public error:   Optional<string> = $state(Optional.none())
    
    public edition_options = $derived(this.getAvailableEditions())


    constructor(edition_repo: EditionRepository, reads_repo: ReadRecordRepository, book_id: string)
    {
        this.edition_repo = edition_repo
        this.reads_repo   = reads_repo
        this.book_id      = book_id

        this.draft = $state(ReadDraftRules.empty(new ReadTarget.New(book_id)))
    }


    public startNew()
    {
        this.draft   = ReadDraftRules.empty(new ReadTarget.New(this.book_id))
        this.error   = Optional.none()
        this.is_open = true
    }


    public editExisting(read_id: string)
    {
        const reads = this.reads_repo.get(this.book_id)
        const read_target = reads.find(read => read.id === read_id)

        if (!read_target)
        {
            return
        }

        this.draft   = ReadDraftRules.fromReadRecordResponse(read_target)
        this.error   = Optional.none()
        this.is_open = true
    }


    public async save()
    {
        if (this.draft.target instanceof ReadTarget.New)
        {
            console.log(this.draft)

            const create_request = ReadDraftRules.toReadRecordCreateRequest(this.draft, this.book_id)

            if (create_request.isNone())
            {
                console.log("create none")
                return
            }

            console.log(create_request)

            const create_response = await this.reads_repo.create(create_request.value)

            if (!create_response.ok)
            {
                console.log("error")
                this.error = Optional.from(`Could not save: ${describeError(create_response.error)}`)
                return
            }

            this.is_open = false
            return
        }
        else
        {
            console.log("update")

            const update_request = ReadDraftRules.toReadRecordUpdateRequest(this.draft)

            if (update_request.isNone())
            {
                return
            }

            const update_response = await this.reads_repo.update(this.draft.target.read_id, update_request.value)

            if (!update_response.ok)
            {
                this.error = Optional.from(`Could not update: ${describeError(update_response.error)}`)
                return
            }

            this.is_open = false
            return
        }
    }


    private getAvailableEditions()
    {
        const editions: EditionResponse[] = this.edition_repo.get(this.book_id)

        const edition_info: EditionData[] = editions.map(edition => ({
            id:    edition.id,
            label: edition.label ?? "",
            format: edition_format_color_registry.resolve(edition.format)
        }))

        return edition_info
    }
}