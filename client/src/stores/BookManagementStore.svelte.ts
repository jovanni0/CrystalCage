import type { AuthorResponse }          from "@root/dtos/AuthorResponse"
import type { SeriesResponse }          from "@root/dtos/SeriesResponse"
import type { TagResponse }             from "@root/dtos/TagResponse"
import type { UniverseResponse }        from "@root/dtos/UniverseResponse"
import type { ApiClient }               from "@root/utils/ApiClient"
import type { Logger }                  from "@loggers/Logger"
import type { CoverManager }            from "@root/queries/CoverManager.interface"
import type { DisplayCover }            from "@root/types/DisplayCover"
import type { BookUpdateRequest }       from "@root/dtos/BookUpdateRequest"
import type { BookDetailResponse }      from "@root/dtos/BookDetailResponse"
import type { EditionManager }          from "@root/queries/EditionManager.interface"
import type { DisplayEdition }          from "@root/types/DisplayEdition"
import      { AuthorQuery }             from "@root/queries/AuthorQuery"
import      { SeriesQuery }             from "@root/queries/SeriesQuery"
import      { TagQuery }                from "@root/queries/TagQuery"
import      { UniverseQuery }           from "@root/queries/UniverseQuery"
import      { isNullOrUndefined }       from "@root/utils/NullishChecks"
import      { toBookCreateRequest2 }    from "@root/dtos/BookCreateRequest"
import      { PersistedCoverManager }   from "@root/queries/PersistedCoverManager.svelte"
import      { DraftCoverManager }       from "@root/queries/DraftCoverManager.svelte"
import      { PersistedEditionManager } from "@root/queries/PersistedEditionManager.svelte"
import      { DraftEditionManager }     from "@root/queries/DraftEditionManager"



export class BookManagementStore
{
    private author_query   = new AuthorQuery()
    private universe_query = new UniverseQuery()
    private series_query   = new SeriesQuery()
    private tag_query      = new TagQuery()

    
    private cover_manager:   CoverManager | null       = $state(null)
    private edition_manager: EditionManager | null     = $state(null)
    private book:            BookDetailResponse | null = $state(null)
    
    private authors:   AuthorResponse[]   = $state([])
    private universes: UniverseResponse[] = $state([])
    private series:    SeriesResponse[]   = $state([])
    private tags:      TagResponse[]      = $state([])

    public is_initialized: boolean = false

    public author_id:        string | null  = $state(null)
    public universe_id:      string | null  = $state(null)
    public series_id:        string | null  = $state(null)
    public volume_number:    number | null  = $state(null)
    public title:            string | null  = $state(null)
    public launch_date:      Date | null    = $state(null)
    public word_count:       number | null  = $state(null)
    public blurb:            string | null  = $state(null)
    public notes:            string | null  = $state(null)
    public tag_ids:          string[]       = $state([])
    
    public author_search:   string = $state("")
    public universe_search: string = $state("")
    public series_search:   string = $state("")
    public tag_search:      string = $state("")
    
    public author_results:   AuthorResponse[]     = $derived(this.author_query.filter(this.authors, this.author_search))
    public author_name:      string | null        = $derived(this.author_query.nameOf(this.authors, this.author_id))
    public universe_results: UniverseResponse[]   = $derived(this.universe_query.filter(this.universes, this.universe_search))
    public universe_name:    string | null        = $derived(this.universe_query.nameOf(this.universes, this.universe_id))
    public series_results:   SeriesResponse[]     = $derived(this.series_query.filter(this.series, this.series_search))
    public series_name:      string | null        = $derived(this.series_query.nameOf(this.series, this.series_id))
    public default_cover_id: string | null        = $derived(this.cover_manager?.default_id ?? null)
    public tag_results:      TagResponse[]        = $derived(this.tag_query.filter(this.tags, this.tag_search))
    public tag_names:        string[]             = $derived(this.tag_ids.map(it => this.tag_query.nameOf(this.tags, it)).filter(it => it !== null))
    public covers:           DisplayCover[]       = $derived(this.cover_manager?.covers ?? [])
    public default_cover:    DisplayCover | null  = $derived(this.default_cover_id !== null ? this.covers.find(it => it.id === this.default_cover_id) ?? null : null)
    public cover_count:      number               = $derived(this.covers.length)
    public cover_message:    string               = $derived(`${this.cover_count == 1 ? "No other options" : `${this.cover_count - 1} other option${this.cover_count == 2 ? "" : "s"}`} available. Tap to configure`)
    public editions:         DisplayEdition[]     = $derived(this.edition_manager?.editions ?? [])
    public edition_types:    string[]             = $derived([...new Set(this.editions.map(it => it.format))])
    public edition_count:    number               = $derived(this.editions.length)
    public edition_message:  string               = $derived(`${this.edition_count === 1 ? "1 type" : `${this.edition_count} types`} available`)

    public is_author_selected:   boolean = $derived(!isNullOrUndefined(this.author_id))
    public is_universe_selected: boolean = $derived(!isNullOrUndefined(this.universe_id))
    public has_notes:            boolean = $derived(!isNullOrUndefined(this.notes))
    public has_blurb:            boolean = $derived(!isNullOrUndefined(this.blurb))
    public has_covers:           boolean = $derived(this.covers.length > 0)
    public has_editions:         boolean = $derived(this.editions.length > 0)


    constructor(
        private logger: Logger,
        private api: ApiClient,
    ) 
    { }


    public async initialize(book_id: string | null)
    {
        this.book = null

        await Promise.all([
            this.fetchAuthors(), 
            this.fetchTags()
        ])

        if (book_id !== null)
        {
            this.book = await this.api.getBook(book_id)

            this.author_id = this.book.author_id
            this.universe_id = this.book.universe_id
            this.series_id = this.book.series_id
            this.volume_number = this.book.volume_number
            this.title = this.book.title
            this.launch_date = this.book.launch_date ? new Date(this.book.launch_date) : null
            this.word_count = this.book.word_count
            this.blurb = this.book.blurb
            this.notes = this.book.notes
            this.tag_ids = [...this.book.tag_ids]

            await Promise.all([
                this.fetchUniverses(this.book.author_id),
                this.fetchSeries(this.book.author_id, this.book.universe_id),
            ])

            const persisted_covers = new PersistedCoverManager(book_id, this.api)
            await persisted_covers.initialize()
            this.cover_manager = persisted_covers

            const persisted_editions = new PersistedEditionManager(book_id, this.api)
            await persisted_editions.initialize()
            this.edition_manager = persisted_editions
        }
        else
        {
            this.cover_manager = new DraftCoverManager()
            this.edition_manager = new DraftEditionManager()
        }

        this.is_initialized = true
    }


    public canSubmit(): boolean
    {
        return !!this.author_id && !!this.title?.trim()
    }


    public async save()
    {
        if (!this.canSubmit())
        {
            this.logger.warn("book cannot be submitted because either the title or author are missing")
            return false
        }

        if (this.book === null)
        {
            return await this.createBook()
        }
        else
        {
            return await this.updateBook()
        }
    }


    private async createBook()
    {
        if (!(this.cover_manager instanceof DraftCoverManager))
        {
            this.logger.error("createBook called without a DraftCoverManager")
            return
        }

        if (!(this.edition_manager instanceof DraftEditionManager))
        {
            this.logger.error("createBook called without a DraftEditionManager")
            return
        }

        try
        {
            this.logger.debug("preparing the book create request")

            const request = toBookCreateRequest2(
                {
                    author_id: this.author_id,
                    universe_id: this.universe_id,
                    series_id: this.series_id,
                    volume_number: this.volume_number,
                    title: this.title,
                    launch_date: this.launch_date,
                    word_count: this.word_count,
                    blurb: this.blurb,
                    notes: this.notes,
                    tag_ids: this.tag_ids,
                    default_cover_id: this.default_cover_id,
                }, 
                this.cover_manager.getDrafts(), 
                this.edition_manager.getDrafts()
            )

            await this.api.createBook(request)

            this.logger.debug("book created successfully")

            return true
        }
        catch (e)
        {
            this.logger.error("failed to save book", { error: e })

            return false
        }
    }


    private async updateBook()
    {
        if (!(this.cover_manager instanceof PersistedCoverManager))
        {
            this.logger.error("createBook called without a PersistedCoverManager")
            return
        }

        if (this.book === null)
        {
            this.logger.error("cannot update book if no original book was fetched")
            return
        }

        try
        {
            const tag_ids_changed = this.tag_ids.length !== this.book.tag_ids.length || !this.tag_ids.every(id => this.book!.tag_ids.includes(id))
            const new_date_str = this.launch_date ? this.launch_date.toISOString().slice(0, 10) : null

            const request: BookUpdateRequest = {
                author_id: this.author_id !== this.book.author_id ? this.author_id! : undefined,
                universe_id: this.universe_id !== this.book.universe_id ? this.universe_id : undefined,
                series_id: this.series_id !== this.book.series_id ? this.series_id : undefined,
                cover_id: this.default_cover_id !== this.book.cover_id ? this.default_cover_id : undefined,
                volume_number: this.volume_number !== this.book.volume_number ? this.volume_number : undefined,
                title: this.title !== this.book.title ? this.title! : undefined,
                launch_date: new_date_str !== this.book.launch_date ? new_date_str : undefined,
                word_count: this.word_count !== this.book.word_count ? this.word_count : undefined,
                blurb: this.blurb != this.book.blurb ? this.blurb : undefined,
                notes: this.notes !== this.book.notes ? this.notes : undefined,
                tag_ids: tag_ids_changed ? this.tag_ids : undefined,
            }

            await this.api.updateBook(this.book.id, request)

            this.logger.debug("book updated successfully")

            return true
        }
        catch (e)
        {
            this.logger.error("failed to update book", { error: e })

            return false
        }
    }


    public reset()
    {
        this.author_id        = null
        this.universe_id      = null
        this.series_id        = null
        this.volume_number    = null
        this.title            = null
        this.launch_date      = null
        this.word_count       = null
        this.blurb            = null
        this.notes            = null
        this.tag_ids          = []

        this.author_search   = ""
        this.universe_search = ""
        this.series_search   = ""
        this.tag_search      = ""
    }


    private async fetchAuthors()
    {
        this.authors = await this.api.getAuthors()
    }


    private async fetchUniverses(author_id: string): Promise<void>
    {
        this.universes = await this.api.getUniverses(author_id)
    }


    private async fetchSeries(author_id: string, universe_id: string | null): Promise<void>
    {
        this.series = await this.api.getSeries(author_id, universe_id)
    }


    private async fetchTags(): Promise<void>
    {
        this.tags = await this.api.getTags()
    }


    public async createAuthor(full_name: string): Promise<boolean>
    {
        const { first_name, last_name } = this.author_query.splitName(full_name)

        try
        {
            const author = await this.api.createAuthor({ first_name, last_name })

            this.authors.push(author)
            this.author_id = author.id

            return true
        }
        catch (e)
        {
            this.logger.error(`could not create author`, { error: e })

            return false
        }
    }


    public setAuthorId(id: string): void
    {
        this.author_id = id
        this.fetchUniverses(id)
        this.fetchSeries(id, null)
    }


    public async createUniverse(author_id: string, name: string): Promise<boolean>
    {
        try
        {
            const universe = await this.api.createUniverse({ name, author_id })
            
            this.logger.info(`created new universe`, universe)

            this.universes.push(universe)
            this.universe_id = universe.id

            return true
        }
        catch(e)
        {
            this.logger.error(`could not create universe`, { error: e })

            return false
        }
    }


    public setUniverseId(id: string): void
    {
        this.universe_id = id

        if (!this.author_id)
        {
            this.logger.warn("trying to set universe ID with unset author ID")
            return
        }
        
        this.fetchSeries(this.author_id, id)
    }


    public async createSeries(name: string, author_id: string, universe_id: string | null, is_finished: boolean = false): Promise<boolean>
    {
        try
        {
            const series = await this.api.createSeries({ name, author_id, universe_id, is_finished })
            
            this.logger.info(`created new series`, series)

            this.series = [series, ...this.series]
            this.series_id = series.id

            return true
        }
        catch(e)
        {
            this.logger.error(`could not create series`, { error: e })

            return false
        }
    }


    public setSeriesId(id: string): void
    {
        this.series_id = id
    }


    public async createTag(name: string, description: string | null): Promise<boolean>
    {
        try
        {
            const tag = await this.api.createTag({ name, description })
            
            this.logger.info(`created new tag`, tag)

            this.tags.push(tag)
            this.tag_ids.push(tag.id)

            return true
        }
        catch(e)
        {
            this.logger.error(`could not create tag`, { error: e })

            return false
        }
    }


    public toggleTagId(id: string, selected: boolean): void
    {
        if (selected && !this.tag_ids.includes(id))
        {
            this.tag_ids.push(id)
        }
        else
        {
            this.tag_ids = this.tag_ids.filter(it => it !== id)
        }
    }


    public addCover(file: File): void
    {
        this.cover_manager?.addCover(file)
    }


    public removeCover(id: string): void
    {
        this.cover_manager?.removeCover(id)
    }


    public setCoverLabel(id: string, label: string): void
    {
        this.cover_manager?.setCoverLabel(id, label)
    }


    public setDefaultCoverId(id: string): void
    {
        this.cover_manager?.setDefaultCoverId(id)
    }


    public addEdition(file: File): void
    {
        this.edition_manager?.addEdition(file)
    }


    public removeEdition(id: string): void
    {
        this.edition_manager?.removeEdition(id)
    }


    public setEditionLabel(id: string, label: string): void
    {
        this.edition_manager?.setEditionLabel(id, label)
    }
}