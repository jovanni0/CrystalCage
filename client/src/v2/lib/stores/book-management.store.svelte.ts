import type { SeriesResponse }          from "@root/dtos/SeriesResponse"
import type { UniverseResponse }        from "@root/dtos/UniverseResponse"
import type { Logger }                  from "@loggers/Logger"
import type { BookUpdateRequest }       from "@root/dtos/BookUpdateRequest"
import type { BookDetailResponse }      from "@root/dtos/BookDetailResponse"
import type { EditionResponse } from "@root/dtos/EditionResponse"
import type { TagResponse } from "../dtos/tag.dto"
import type { MockApi } from "../clients/mock-api"
import      { isNullOrUndefined }       from "@root/utils/NullishChecks"
import      { TagRules } from "../rules/tag-rules"
import      { AuthorRules } from "../rules/author-rules"
import      { UniverseRules } from "../rules/universe-rules"
import      { SeriesRules } from "../rules/series-rules"
import      { author_repository, 
              book_repository, 
              edition_repository, 
              series_repository, 
              tag_repository, 
              universe_repository } from "@root/CompositionRoot"
import      { BookRules } from "../rules/book-rules"



export class CreateBookStore
{
    private book: BookDetailResponse | null = $state(null)

    public is_new_book:    boolean = true

    // stuff filled/selected for the current book
    public author_id:        string | null  = $state(null)
    public universe_id:      string | null  = $state(null)
    public series_id:        string | null  = $state(null)
    public volume_number:    number | null  = $state(null)
    public title:            string         = $state("")
    public launch_date:      string         = $state("")
    public word_count:       number | null  = $state(null)
    public blurb:            string | null  = $state(null)
    public thoughts:         string | null  = $state(null)
    public tag_ids:          string[]       = $state([])
    
    public author_search:   string = $state("")
    public universe_search: string = $state("")
    public series_search:   string = $state("")
    public tag_search:      string = $state("")
    
    public author_results:      { id: string, label: string}[] = $derived(AuthorRules.filterByQuery(author_repository.all, this.author_search).map(it => ({ id: it.id, label: AuthorRules.nameOf(it) })))
    public author_create_label: string | undefined             = $derived(AuthorRules.getCreatableAuthorName(this.author_search, author_repository.all))
    public author_name:         string | null                  = $derived(AuthorRules.nameOfId(author_repository.all, this.author_id))
    
    public universe_options:      UniverseResponse[]             = $derived(universe_repository.all.filter(it => it.author_id === this.author_id))
    public universe_results:      { id: string, label: string}[] = $derived(UniverseRules.filterByQuery(this.universe_options, this.universe_search).map(it => ({ id: it.id, label: it.name }) ))
    public universe_create_label: string | undefined             = $derived(UniverseRules.getCreatableUniverse(this.universe_search, this.universe_options))
    public universe_name:         string | null                  = $derived(UniverseRules.nameOfId(this.universe_options, this.universe_id))
    
    public series_options:      SeriesResponse[]               = $derived(series_repository.all.filter(it => it.author_id === this.author_id && it.universe_id === this.universe_id))
    public series_results:      { id: string, label: string}[] = $derived(SeriesRules.filterByQuery(this.series_options, this.series_search).map(it => ({ id: it.id, label: it.name }) ))
    public series_create_label: string | undefined             = $derived(SeriesRules.getCreatableSeries(this.series_search, this.series_options))
    public series_name:         string | null                  = $derived(SeriesRules.nameOf(this.series_options, this.series_id))

    public other_volumes: { id: string, title: string, volume: number }[] = $derived(book_repository.all.map(it => ({ id: it.id, title: it.title, volume: it.volume_number ?? 0 }) ))
    public next_volume:   number                                          = $derived(Math.floor(Math.max(...this.other_volumes.map(it => it.volume), 0)) + 1)
    
    public edition_options: EditionResponse[] = $derived(this.book ? edition_repository.forBook(this.book.id) : [])
    public editions_wc: { id: string, label: string, format: string, added_at: string }[] = $derived(this.edition_options.toSorted((a, b) => b.added_at.localeCompare(a.added_at)).map(it => ({ id: it.id, label: it.label ?? "Unknown", format: it.format, added_at: it.added_at }) ))

    public tag_results:   TagResponse[]                                       = $derived(TagRules.filterByQuery(tag_repository.all, this.tag_search))
    public tag_groups:    { id: string, name: string, tags: TagResponse[] }[] = $derived(TagRules.groupTags(this.tag_results, tag_repository.categories))
    public selected_tags: { id: string, name: string, tags: TagResponse[] }[] = $derived(TagRules.groupTags(tag_repository.all.filter(t => this.tag_ids.includes(t.id)), tag_repository.categories))
    public should_create_tag: boolean = $derived(this.tag_search !== "" && this.tag_results.length === 0)

    public is_author_selected:   boolean = $derived(!isNullOrUndefined(this.author_id))
    public is_universe_selected: boolean = $derived(!isNullOrUndefined(this.universe_id))
    public is_series_selected:   boolean = $derived(!isNullOrUndefined(this.series_id))
    public has_notes:            boolean = $derived(!isNullOrUndefined(this.thoughts))
    public has_blurb:            boolean = $derived(!isNullOrUndefined(this.blurb))
    public can_submit:           boolean = $derived(!!this.author_id && this.title.trim() !== "")


    // SETTERE

    public setAuthorId(id: string): void
    {
        if (this.author_id === id) return

        this.author_id = id
        this.universe_id = null
        this.series_id = null
        this.volume_number = null
    }


    public setUniverseId(id: string): void
    {
        if (this.universe_id === id) return

        if (!this.author_id)
        {
            this.logger.warn("trying to set universe ID with unset author ID")
            return
        }

        this.universe_id = id
        this.series_id = null
        this.volume_number = null
    }


    public setSeriesId(id: string): void
    {
        if (this.series_id === id) return

        this.series_id = id
        this.volume_number = null
    }


    constructor(
        private logger: Logger,
        private api: MockApi,
        book_id: string | null,
    ) 
    {
        this.is_new_book = book_id === null

        this.initialize(book_id)
    }


    public async initialize(book_id: string | null)
    {
        await Promise.all([
            author_repository.load(), 
            universe_repository.load(),
            series_repository.load(),
            tag_repository.load(),
            book_repository.load(),
        ])

        if (book_id !== null)
        {
            this.book = await this.api.getBook(book_id)

            this.author_id     = this.book.author_id
            this.universe_id   = this.book.universe_id
            this.series_id     = this.book.series_id
            this.volume_number = this.book.volume_number
            this.title         = this.book.title
            this.launch_date   = this.book.launch_date ?? ""
            this.word_count    = this.book.word_count
            this.blurb         = this.book.blurb
            this.thoughts         = this.book.notes
            this.tag_ids       = [...this.book.tag_ids]

            await edition_repository.load(book_id)
        }
    }


    public async save()
    {
        if (!this.can_submit) return false

        if (this.book === null)
        {
            const request = BookRules.toCreateRequest(this.author_id!, this.universe_id, this.series_id, this.volume_number, this.title, this.launch_date, this.word_count, this.blurb, this.thoughts, this.tag_ids)
            const result = await book_repository.create(request)

            if (!result.ok)
            {
                return false
            }

            return result.value.id
        }
        else
        {
            const tag_ids_changed = this.tag_ids.length !== this.book.tag_ids.length || !this.tag_ids.every(id => this.book!.tag_ids.includes(id))
            const new_date_str = this.launch_date ? this.launch_date.slice(0, 10) : null
            const request: BookUpdateRequest = {
                author_id:     this.author_id !== this.book.author_id ? this.author_id! : undefined,
                universe_id:   this.universe_id !== this.book.universe_id ? this.universe_id : undefined,
                series_id:     this.series_id !== this.book.series_id ? this.series_id : undefined,
                cover_id:      undefined,
                volume_number: this.volume_number !== this.book.volume_number ? this.volume_number : undefined,
                title:         this.title !== this.book.title ? this.title! : undefined,
                launch_date:   new_date_str !== this.book.launch_date ? new_date_str : undefined,
                word_count:    this.word_count !== this.book.word_count ? this.word_count : undefined,
                blurb:         this.blurb != this.book.blurb ? this.blurb : undefined,
                notes:         this.thoughts !== this.book.notes ? this.thoughts : undefined,
                tag_ids:       tag_ids_changed ? this.tag_ids : undefined,
            }

            const result = await book_repository.update(this.book.id, request)

            if (!result.ok)
            {
                return false
            }

            return result.value.id
        }
    }


    public async delete(): Promise<boolean>
    {
        if (this.book == null) return false

        const result = await book_repository.delete(this.book.id)

        if (!result.ok)
        {
            return false
        }

        return result.value
    }


    public async createAndSetAuthor(): Promise<string | null>
    {
        if (!this.author_search) return null

        const request = AuthorRules.toCreateRequest(this.author_search)
        const result = await author_repository.create(request)

        if (!result.ok)
        {
            return null
        }

        this.setAuthorId(result.value.id)

        return result.value.id
    }


    public async createAndSetUniverse(): Promise<string | null>
    {
        if (!this.author_id || !this.universe_search) return null

        const request = UniverseRules.toCreateRequest(this.author_id, this.universe_search)
        const result = await universe_repository.create(request)

        if (!result.ok)
        {
            return null
        }

        this.setUniverseId(result.value.id)

        return result.value.id
    }


    public async createAndSetSeries(): Promise<string | null>
    {
        if (!this.author_id || !this.series_search) return null

        const request = SeriesRules.toCreateRequest(this.author_id, this.universe_id, this.series_search)
        const result = await series_repository.create(request)

        if (!result.ok)
        {
            return null
        }

        this.setSeriesId(result.value.id)

        return result.value.id
    }


    public async createTag(): Promise<string | null>
    {
        if (!this.tag_search) return null

        const request = TagRules.toCreateRequest(this.tag_search)
        const result = await tag_repository.create(request)

        if (!result.ok)
        {
            return null
        }

        return result.value.id
    }
}