import type { AuthorCreateRequest } from "@root/dtos/AuthorCreateRequest";
import type { AuthorResponse } from "@root/dtos/AuthorResponse";
import type { BookCreateRequest } from "@root/dtos/BookCreateRequest";
import type { BookDetailResponse } from "@root/dtos/BookDetailResponse";
import type { BookSummaryResponse } from "@root/dtos/BookSummaryResponse";
import type { BookUpdateRequest } from "@root/dtos/BookUpdateRequest";
import type { EditionCreateRequest } from "@root/dtos/EditionCreateRequest";
import type { EditionResponse } from "@root/dtos/EditionResponse";
import type { EditionUpdateRequest } from "@root/dtos/EditionUpdateRequest";
import type { SeriesCreateRequest } from "@root/dtos/SeriesCreateRequest";
import type { SeriesDetailResponse } from "@root/dtos/SeriesDetailResponse";
import type { SeriesResponse } from "@root/dtos/SeriesResponse";
import type { TagCreateRequest } from "@root/dtos/TagCreateRequest";
import type { TagDetailResponse } from "@root/dtos/TagDetailResponse";
import type { TagResponse } from "@root/dtos/TagResponse";
import type { UniverseCreateRequest } from "@root/dtos/UniverseCreateRequest";
import type { UniverseResponse } from "@root/dtos/UniverseResponse";
import type { TagCategoryResponse } from "../dtos/tag-category.dto";



export interface APIClient 
{
    getAuthors():                               Promise<AuthorResponse[]>
    // getAuthorById(id: string):                  Promise<AuthorResponse>
    createAuthor(request: AuthorCreateRequest): Promise<AuthorResponse>

    getUniverses(author_id: string | null):         Promise<UniverseResponse[]>
    createUniverse(request: UniverseCreateRequest): Promise<UniverseResponse>

    getSeries(author_id: string | null, universe_id: string | null): Promise<SeriesResponse[]>
    // getSeriesById(id: string):                                       Promise<SeriesDetailResponse>
    createSeries(request: SeriesCreateRequest):                      Promise<SeriesResponse>

    getTags():                            Promise<TagResponse[]>
    // getTagById(id: string):               Promise<TagDetailResponse>
    createTag(request: TagCreateRequest): Promise<TagResponse>
    getTagCategories(): Promise<TagCategoryResponse[]>

    getBook(id: string): Promise<BookDetailResponse>
    getBooks(author_id: string | null, universe_id: string | null,series_id: string | null, tag_id: string | null): Promise<BookSummaryResponse[]>
    createBook(request: BookCreateRequest): Promise<BookSummaryResponse>
    updateBook(id: string, request: BookUpdateRequest): Promise<BookSummaryResponse>
    deleteBook(id: string): Promise<void>

    getEditions(book_id: string): Promise<EditionResponse[]>
    createEdition(request: EditionCreateRequest): Promise<EditionResponse>
    updateEdition(id: string, request: EditionUpdateRequest): Promise<EditionResponse>
    deleteEdition(id: string): Promise<void>
}