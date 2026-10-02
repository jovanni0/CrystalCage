import type { AuthorResponse } from "@root/dtos/AuthorResponse";
import type { APIClient } from "./api-client.interface";
import type { UniverseResponse } from "@root/dtos/UniverseResponse";
import type { SeriesResponse } from "@root/dtos/SeriesResponse";
import type { AuthorCreateRequest } from "@root/dtos/AuthorCreateRequest";
import type { UniverseCreateRequest } from "@root/dtos/UniverseCreateRequest";
import type { SeriesCreateRequest } from "@root/dtos/SeriesCreateRequest";
import type { BookCreateRequest } from "@root/dtos/BookCreateRequest";
import type { BookSummaryResponse } from "@root/dtos/BookSummaryResponse";
import      { generateEphemeralId } from "@root/utils/IdGenerator";
import type { BookDetailResponse } from "@root/dtos/BookDetailResponse";
import type { BookUpdateRequest } from "@root/dtos/BookUpdateRequest";
import type { EditionResponse } from "@root/dtos/EditionResponse";
import type { EditionCreateRequest } from "@root/dtos/EditionCreateRequest";
import type { EditionUpdateRequest } from "@root/dtos/EditionUpdateRequest";
import type { TagCreateRequest, TagResponse } from "../dtos/tag.dto";
import type { TagCategoryResponse } from "../dtos/tag-category.dto";



export class MockApi implements APIClient
{
    private AUTHORS = [
        { id: "author-1", first_name: "Morton" , last_name: "Bird" },
        { id: "author-2", first_name: "Tammie" , last_name: "Hogan" },
        { id: "author-3", first_name: "Carlo" , last_name: "Howe" },
        { id: "author-4", first_name: "Darla" , last_name: "Rose" },
        { id: "author-5", first_name: "Jae" , last_name: "Vang" },
    ] as AuthorResponse[]

    private UNIVERSES = [
        { id: "universe-1", author_id: "author-3", name: "Unchained" },
    ] as UniverseResponse[]

    private SERIES = [
        { id: "series-1", author_id: "author-4", universe_id: null, name: "Beneth the Silicone Sky", is_finished: false },
        { id: "series-2", author_id: "author-3", universe_id: "universe-1", name: "Dark Depths", is_finished: false },
    ] as SeriesResponse[]

    private TAGS = [
        { id: "tag-1", category_id: "tag-category-genere",  name: "Sci-Fi",   description: null, book_count: 0 },
        { id: "tag-2", category_id: "tag-category-genere",  name: "Noir",     description: null, book_count: 0 },
        { id: "tag-3", category_id: "tag-category-genere",  name: "Military", description: null, book_count: 0 },
        { id: "tag-4", category_id: "tag-category-mood",    name: "Cozy",     description: null, book_count: 0 },
        { id: "tag-5", category_id: "tag-category-mood",    name: "Hopeful",  description: null, book_count: 0 },
        { id: "tag-6", category_id: "tag-category-warning", name: "Violence", description: null, book_count: 0 },
        { id: "tag-7", category_id: "tag-category-warning", name: "Gore",     description: null, book_count: 0 },
        { id: "tag-8", category_id: null,                   name: "Custom 1", description: null, book_count: 0 },
        { id: "tag-9", category_id: null,                   name: "Custom 2", description: null, book_count: 0 },
    ] as TagResponse[]


    private TAG_CATEGORIES = [
        { id: "tag-category-genere",  name: "Genre" },
        { id: "tag-category-mood",    name: "Mood" },
        { id: "tag-category-warning", name: "Content Warning" },
    ] as TagCategoryResponse[]

    private BOOKS = [
        { id: "book-1", author_id: "author-3", universe_id: "universe-1", series_id: "series-2", cover_id: null, volume_number: 1, title: "Ghosts of Sector Seven", read_status: "UNREAD", reread_count: 0 },
        { id: "book-2", author_id: "author-3", universe_id: "universe-1", series_id: "series-2", cover_id: null, volume_number: 2, title: "When things fall short", read_status: "UNREAD", reread_count: 0 },
        { id: "book-3", author_id: "author-3", universe_id: "universe-1", series_id: "series-2", cover_id: null, volume_number: 3, title: "Zero state",             read_status: "UNREAD", reread_count: 0 },
        { id: "book-4", author_id: "author-3", universe_id: "universe-1", series_id: "series-2", cover_id: null, volume_number: 4, title: "The Middle Eight",       read_status: "UNREAD", reread_count: 0 },
        { id: "book-5", author_id: "author-1", universe_id: null, series_id: null, cover_id: null, volume_number: null, title: "Bargans", read_status: "UNREAD", reread_count: 0 },
    ] as BookSummaryResponse[]

    private EDITIONS = [
        { id: "edition-1", book_id: "book-1", label: "Tor UK", release_date: null, format: "epub", added_at: "2000-01-01" },
        { id: "edition-2", book_id: "book-1", label: "Pass 1", release_date: null, format: "tome", added_at: "2001-10-12" },
        { id: "edition-3", book_id: "book-1", label: "Pass 2", release_date: null, format: "tome", added_at: "2004-05-22" },
    ] as EditionResponse[]

    private BOOK: BookDetailResponse = {
        id: "book-1",
        author_id: "author-4",
        series_id: "series-1",
        universe_id: null,
        cover_id: null,
        volume_number: null,
        title: "Sitting in a Place of Honor",
        launch_date: "2020-10-09",
        word_count: 347,
        blurb: null,
        notes: null,
        tag_ids: [],
        read_status: "UNREAD",
        reread_count: 0,
    }




    public getAuthors(): Promise<AuthorResponse[]>
    {
        return Promise.resolve(structuredClone(this.AUTHORS))
    }

    public createAuthor(request: AuthorCreateRequest): Promise<AuthorResponse>
    {
        const new_record: AuthorResponse = {
            id: generateEphemeralId(),
            first_name: request.first_name,
            last_name: request.last_name,
        }

        this.AUTHORS.push(new_record)

        return Promise.resolve(structuredClone(new_record))
    }

    public getUniverses(author_id: string | null): Promise<UniverseResponse[]>
    {
        return Promise.resolve(structuredClone(this.UNIVERSES))
    }

    public createUniverse(request: UniverseCreateRequest): Promise<UniverseResponse>
    {
        const new_record: UniverseResponse = {
            id: generateEphemeralId(),
            author_id: request.author_id,
            name: request.name,
        }

        this.UNIVERSES.push(new_record)

        return Promise.resolve(structuredClone(new_record))
    }

    public getSeries(author_id: string | null, universe_id: string | null): Promise<SeriesResponse[]>
    {
        let filtered = this.SERIES

        if (author_id)
        {
            filtered = filtered.filter(it => it.author_id === author_id)
        }

        if (universe_id)
        {
            filtered = filtered.filter(it => it.universe_id === universe_id)
        }

        return Promise.resolve(structuredClone(filtered))
    }

    public createSeries(request: SeriesCreateRequest): Promise<SeriesResponse>
    {
        const new_record: SeriesResponse = {
            id: generateEphemeralId(),
            author_id: request.author_id,
            universe_id: request.universe_id,
            name: request.name,
            is_finished: request.is_finished ?? false,
        }

        this.SERIES.push(new_record)

        return Promise.resolve(structuredClone(new_record))
    }

    public getTags(): Promise<TagResponse[]>
    {
        return Promise.resolve(structuredClone(this.TAGS))
    }

    public createTag(request: TagCreateRequest): Promise<TagResponse>
    {
        const new_record: TagResponse = {
            id: generateEphemeralId(),
            category_id: request.category_id,
            name: request.name,
            description: request.description,
            book_count: 0,
        }

        this.TAGS.push(new_record)

        return Promise.resolve(structuredClone(new_record))
    }


    public async getBooks(
        author_id:   string | null = null, 
        universe_id: string | null = null,
        series_id:   string | null = null,
        tag_id:      string | null = null,
    ): Promise<BookSummaryResponse[]>
    {
        return this.BOOKS.filter(book =>
            (author_id === null || book.author_id === author_id) &&
            (universe_id === null || book.universe_id === universe_id) &&
            (series_id === null || book.series_id === series_id)
        )
    }

    public async getBook(id: string): Promise<BookDetailResponse>
    {
        return Promise.resolve({...this.BOOK, id: id})
    }

    public createBook(request: BookCreateRequest): Promise<BookSummaryResponse>
    {
        const new_record: BookSummaryResponse = {
            id: generateEphemeralId(),
            author_id: request.author_id,
            universe_id: request.universe_id ?? null,
            series_id: request.series_id ?? null,
            cover_id: null,
            volume_number: request.volume_number ?? null,
            title: request.title,
            read_status: "UNREAD",
            reread_count: 0
        }

        return Promise.resolve(structuredClone(new_record))
    }

    public updateBook(id: string, request: BookUpdateRequest): Promise<BookSummaryResponse>
    {
        return this.getBook(id)
    }

    public deleteBook(id: string): Promise<void>
    {
        return Promise.resolve()
    }

    public getEditions(book_id: string): Promise<EditionResponse[]>
    {
        return Promise.resolve(structuredClone(this.EDITIONS.filter(it => it.book_id === book_id)))
    }

    public createEdition(request: EditionCreateRequest): Promise<EditionResponse>
    {
        const new_edition = {
            id: generateEphemeralId(),
            book_id: request.book_id,
            label: request.label,
            release_date: request.release_date,
            format: request.format,
            added_at: new Date().toISOString().split('T')[0]
        }

        this.EDITIONS.push(new_edition)

        return Promise.resolve(structuredClone(new_edition))
    }

    public updateEdition(id: string, request: EditionUpdateRequest): Promise<EditionResponse>
    {
        const target_edition = this.EDITIONS.find(it => it.id === id)

        if (!target_edition)
        {
            return Promise.reject()
        }

        const updated_edition = {
            ...target_edition,
            ...Object.fromEntries(Object.entries(request).filter(([, value]) => value !== undefined))
        }

        this.EDITIONS = [...this.EDITIONS, updated_edition]

        return Promise.resolve(structuredClone(updated_edition))
    }

    public deleteEdition(id: string): Promise<void>
    {
        this.EDITIONS = this.EDITIONS.filter(it => it.id !== id)

        return Promise.resolve()
    }


    public getTagCategories(): Promise<TagCategoryResponse[]> 
    {
        return Promise.resolve(structuredClone(this.TAG_CATEGORIES))
    }
}