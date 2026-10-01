import type { AuthorResponse }          from "@root/dtos/AuthorResponse"
import type { SeriesResponse }          from "@root/dtos/SeriesResponse"
import type { UniverseResponse }        from "@root/dtos/UniverseResponse"
import type { Logger }                  from "@loggers/Logger"
import type { BookUpdateRequest }       from "@root/dtos/BookUpdateRequest"
import type { BookDetailResponse }      from "@root/dtos/BookDetailResponse"
import      { AuthorQuery }             from "@root/queries/AuthorQuery"
import      { SeriesQuery }             from "@root/queries/SeriesQuery"
import      { UniverseQuery }           from "@root/queries/UniverseQuery"
import      { isNullOrUndefined }       from "@root/utils/NullishChecks"
import      { toBookCreateRequest2 }    from "@root/dtos/BookCreateRequest"
import type { BookSummaryResponse } from "@root/dtos/BookSummaryResponse"
import type { EditionResponse } from "@root/dtos/EditionResponse"
import type { TagCategoryResponse } from "../dtos/tag-category.dto"
import type { TagResponse } from "../dtos/tag.dto"
import type { MockApi } from "../clients/mock-api"
import { TagQuery } from "../utils/tag-quert"



export class CreateBookStore
{
    private author_query   = new AuthorQuery()
    private universe_query = new UniverseQuery()
    private series_query   = new SeriesQuery()
    private tag_query      = new TagQuery()

    private book: BookDetailResponse | null = $state(null)
    
    private authors:        AuthorResponse[]      = $state([])
    private universes:      UniverseResponse[]    = $state([])
    private series:         SeriesResponse[]      = $state([])
    private volumes:        BookSummaryResponse[] = $state([])
    private editions:       EditionResponse[]     = $state([])
    private tags:           TagResponse[]         = $state([])
    private tag_categories: TagCategoryResponse[] = $state([])

    public is_initialized: boolean = false

    // stuff filled/selected for the current book
    public author_id:        string | null  = $state(null)
    public universe_id:      string | null  = $state(null)
    public series_id:        string | null  = $state(null)
    public volume_number:    number | null  = $state(null)
    public title:            string | null  = $state(null)
    public launch_date:      string         = $state("")
    public word_count:       number | null  = $state(null)
    public blurb:            string | null  = $state(null)
    public notes:            string | null  = $state(null)
    public tag_ids:          string[]       = $state([])
    
    public author_search:   string = $state("")
    public universe_search: string = $state("")
    public series_search:   string = $state("")
    public tag_search:      string = $state("")
    
    public author_results:      { id: string, label: string}[] = $derived(this.author_query.filter(this.authors, this.author_search).map(it => ({ id: it.id, label: this.author_query.nameOf(it) })))
    public author_create_label: string | undefined             = $derived(this.author_results.length === 0 ? this.getNameFromQuery() : undefined)
    public author_name:         string | null                  = $derived(this.author_query.nameOfId(this.authors, this.author_id))
    
    public universe_results:      { id: string, label: string}[] = $derived(this.universe_query.filter(this.universes, this.universe_search).map(it => ({ id: it.id, label: it.name }) ))
    public universe_create_label: string | undefined             = $derived(this.universe_results.length === 0 ? this.universe_search : undefined)
    public universe_name:         string | null                  = $derived(this.universe_query.nameOfId(this.universes, this.universe_id))
    
    public series_results:      { id: string, label: string}[] = $derived(this.series_query.filter(this.series, this.series_search).map(it => ({ id: it.id, label: it.name }) ))
    public series_create_label: string | undefined             = $derived(this.series_results.length === 0 ? this.series_search : undefined)
    public series_name:         string | null                  = $derived(this.series_query.nameOf(this.series, this.series_id))

    public other_volumes: { id: string, title: string, volume: number }[] = $derived(this.volumes.map(it => ({ id: it.id, title: it.title, volume: it.volume_number ?? 0 }) ))
    public next_volume:   number                                          = $derived(Math.floor(Math.max(...this.other_volumes.map(it => it.volume), 0)) + 1)
    
    public editions_wc: { id: string, label: string, format: string, added_at: string }[] = $derived(this.editions.toSorted((a, b) => b.added_at.localeCompare(a.added_at)).map(it => ({ id: it.id, label: it.label ?? "Unknown", format: it.format, added_at: it.added_at }) ))

    public tag_results:   TagResponse[]                  = $derived(this.tag_query.filter(this.tags, this.tag_search))
    public tag_groups:    { id: string, name: string, tags: TagResponse[] }[] = $derived(this.groupTagsByCategory())
    public selected_tags: { id: string, name: string, tags: TagResponse[] }[] = $derived(this.tag_groups.map(it => ({...it, tags: it.tags.filter(tag => this.tag_ids.includes(tag.id))})).filter(it => it.tags.length > 0) )
    public should_create_tag: boolean = $derived(this.tag_results.length === 0)

    public is_new_book:          boolean | undefined = $derived(this.is_initialized ? this.book === null : undefined)
    public is_author_selected:   boolean             = $derived(!isNullOrUndefined(this.author_id))
    public is_universe_selected: boolean             = $derived(!isNullOrUndefined(this.universe_id))
    public is_series_selected:   boolean             = $derived(!isNullOrUndefined(this.series_id))
    public has_notes:            boolean             = $derived(!isNullOrUndefined(this.notes))
    public has_blurb:            boolean             = $derived(!isNullOrUndefined(this.blurb))


    private getNameFromQuery(): string | undefined
    {
        const name = this.author_query.splitName(this.author_search)
        const target = this.authors.find(it => it.first_name === name.first_name && it.last_name === name.last_name)
        
        if (target)
        {
            return undefined
        }

        if (name.first_name)
        {
            return `${name.last_name}, ${name.first_name}`
        }
        
        return name.last_name
    }

    constructor(
        private logger: Logger,
        private api: MockApi,
    ) 
    { }


    public async initialize(book_id: string | null)
    {
        this.book = null

        await Promise.all([
            this.fetchAuthors(), 
            this.fetchTags(),
            this.fetchTagCategories(),
            this.fetchEditions("book-1"),    // TODO: remove after testing
        ])

        if (book_id !== null)
        {
            this.book = await this.api.getBook(book_id)

            this.author_id = this.book.author_id
            this.universe_id = this.book.universe_id
            this.series_id = this.book.series_id
            this.volume_number = this.book.volume_number
            this.title = this.book.title
            this.launch_date = this.book.launch_date ?? ""
            this.word_count = this.book.word_count
            this.blurb = this.book.blurb
            this.notes = this.book.notes
            this.tag_ids = [...this.book.tag_ids]

            await Promise.all([
                this.fetchUniverses(this.book.author_id),
                this.fetchSeries(this.book.author_id, this.book.universe_id),
                this.fetchEditions(book_id)
            ])
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
                    default_cover_id: null,
                }, 
                [], 
                []
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
        if (this.book === null)
        {
            this.logger.error("cannot update book if no original book was fetched")
            return
        }

        try
        {
            const tag_ids_changed = this.tag_ids.length !== this.book.tag_ids.length || !this.tag_ids.every(id => this.book!.tag_ids.includes(id))
            const new_date_str = this.launch_date ? this.launch_date.slice(0, 10) : null

            const request: BookUpdateRequest = {
                author_id: this.author_id !== this.book.author_id ? this.author_id! : undefined,
                universe_id: this.universe_id !== this.book.universe_id ? this.universe_id : undefined,
                series_id: this.series_id !== this.book.series_id ? this.series_id : undefined,
                cover_id: undefined,
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


    public async deleteBook()
    {
        if (this.book == null)
        {
            if (this.is_initialized === false)
            {
                this.logger.error("trying to delete a book when store is not initialized")
            }
            else
            {
                this.logger.error("trying to delete the book when the store is in create mode")
            }

            return
        }

        try
        {
            await this.api.deleteBook(this.book.id)
            return true
        }
        catch (e)
        {
            this.logger.error("while trying to delete book", { error: e })
            return false
        }
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


    private async fetchBooksInSeries(series_id: string): Promise<void>
    {
        this.volumes = await this.api.getBooks(null, null, series_id, null)
    }


    private async fetchTags(): Promise<void>
    {
        this.tags = await this.api.getTags()
    }


    private async fetchTagCategories(): Promise<void>
    {
        this.tag_categories = await this.api.getTagCategories()
    }

    
    private async fetchEditions(book_id: string): Promise<void>
    {
        this.editions = await this.api.getEditions(book_id)
    }


    public async createAuthor(): Promise<boolean>
    {
        const { first_name, last_name } = this.author_query.splitName(this.author_search)

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


    public async createUniverse(): Promise<boolean>
    {
        if (!this.author_id || !this.universe_query)
        {
            return false
        }

        try
        {
            const universe = await this.api.createUniverse({ name: this.universe_search, author_id: this.author_id })
            
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


    public async createSeriesFromSearch(): Promise<boolean>
    {
        if (!this.author_id || !this.series_search)
        {
            return false
        }

        try
        {
            const series = await this.api.createSeries({ 
                name: this.series_search, 
                author_id: this.author_id, 
                universe_id: this.universe_id, 
                is_finished: false,
            })
            
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
        this.fetchBooksInSeries(id)
    }


    public async createTag(): Promise<boolean>
    {
        if (!this.tag_search.trim())
        {
            return false
        }

        try
        {
            const tag = await this.api.createTag({ 
                name: this.tag_search.trim(), 
                description: null,
                category_id: null,
            })
            
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


    private groupTagsByCategory()
    {
        const categories = new Set(this.tag_categories.map(it => it.id))

        // tags with no category, or a category that no longer exists, go to the fallback group
        const by_category = Map.groupBy(
            this.tag_results,
            tag => tag.category_id && categories.has(tag.category_id) ? tag.category_id : null
        )

        const groups = this.tag_categories.map(category => ({
            id:   category.id,
            name: category.name,
            tags: by_category.get(category.id) ?? [],
        }))

        groups.push({ id: "uncategorized", name: "", tags: by_category.get(null) ?? [] })
        groups.sort((a, b) => a.name.localeCompare(b.name))

        return groups.filter(group => group.tags.length > 0)
    }
}