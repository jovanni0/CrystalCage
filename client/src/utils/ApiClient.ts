import type { AuthContext }                    from "@data-types/AuthContext";
import type { AuthLoginRequest }               from "../dtos/AuthLoginRequest.ts";
import type { AuthorCreateRequest }            from "@dtos/AuthorCreateRequest";
import type { AuthorResponse }                 from "@dtos/AuthorResponse";
import type { AuthLoginResponse }              from "@dtos/AuthLoginResponse";
import type { PingResponse }                   from "@dtos/PingResponse";
import type { UserProfileResponse }            from "@dtos/UserProfileResponse";
import type { UniverseResponse }               from "@dtos/UniverseResponse";
import type { UniverseCreateRequest }          from "@dtos/UniverseCreateRequest";
import type { SeriesResponse }                 from "@dtos/SeriesResponse";
import type { SeriesCreateRequest }            from "@dtos/SeriesCreateRequest";
import type { Logger }                         from "@loggers/Logger";
import type { TagResponse }                    from "@dtos/TagResponse";
import type { TagCreateRequest }               from "@dtos/TagCreateRequest";
import type { BookCreateRequest }              from "@dtos/BookCreateRequest";
import type { BookSummaryResponse }            from "@dtos/BookSummaryResponse";
import type { SeriesDetailResponse }           from "@root/dtos/SeriesDetailResponse";
import type { BookDetailResponse }             from "@root/dtos/BookDetailResponse";
import type { CoverResponse }                  from "@root/dtos/CoverResponse";
import type { ReadRecordResponse }             from "@root/dtos/ReadRecordResponse";
import type { EditionResponse }                from "@root/dtos/EditionResponse";
import type { TagUpdateRequest }               from "@root/dtos/TagUpdateRequest";
import type { TagDetailResponse }              from "@root/dtos/TagDetailResponse";
import type { ReadRecordUpdateRequest }        from "@root/dtos/ReadRecordUpdateRequest";
import type { CoverCreateRequest }             from "@root/dtos/CoverCreateRequest";
import type { BookUpdateRequest }              from "@root/dtos/BookUpdateRequest";
import type { CoverUpdateRequest }             from "@root/dtos/CoverUpdateRequest";
import type { EditionCreateRequest }           from "@root/dtos/EditionCreateRequest";
import type { EditionUpdateRequest }           from "@root/dtos/EditionUpdateRequest";
import type { ReadRecordCreateRequest }        from "@root/dtos/ReadRecordCreateRequest";
import type { InviteCreateResponse }           from "@root/dtos/InviteCreateResponse";
import      { coverCreateRequestToFormData }   from "@root/dtos/CoverCreateRequest";
import      { editionCreateRequestToFormData } from "@root/dtos/EditionCreateRequest";
import      { isNullOrUndefined }              from "@root/utils/NullishChecks";
import      { ServerNotConfiguredError }       from "@root/errors/ServerNotConfiguredError";
import      { UnauthorizedError }              from "@root/errors/UnauthorizedError";
import      { toFormData }                     from "@dtos/BookCreateRequest";
import      { ApiError }                       from "@root/errors/ApiError";



export class ApiClient
{
    private getAuthContext: (() => AuthContext | null) | null = null
    private onUnauthorized: (() => void) | null = null


    constructor(private logger: Logger)
    { }


    configureAuth(getAuthContext: () => AuthContext | null, onUnauthorized: () => void)
    {
        this.getAuthContext = getAuthContext
        this.onUnauthorized = onUnauthorized
    }


    private async request(path: string, options: RequestInit): Promise<Response>
    {
        const context = this.getAuthContext?.()

        if (isNullOrUndefined(context))
        {
            throw new ServerNotConfiguredError(`request ${path} cannot be made because no context was configured`)
        }

        const headers = {
            ...options.headers,
            ...(context.session_token ? { Authorization: `Bearer ${context.session_token}`} : {}),
        }

        const response = await fetch(`${context.base_url}${path}`, { ...options, headers })

        if (response.status === 401)
        {
            this.onUnauthorized?.()
            throw new UnauthorizedError(`request ${path} returned 401 Unauthorized`)
        }

        if (!response.ok)
        {
            const message = await response.text()
            throw new ApiError(response.status, message)
        }

        return response
    }


    async ping(): Promise<PingResponse>
    {
        this.logger.debug("pinging server")

        const response = await this.request("/ping", {})

        return response.json()
    }


    async login(body: AuthLoginRequest): Promise<AuthLoginResponse>
    {
        const response = await this.request(
            "/auth/login",
            {
                method: "POST",
                body: JSON.stringify(body),
                headers: { "Content-Type": "application/json" }
            }
        )

        return response.json()
    }


    async getMe(): Promise<UserProfileResponse>
    {
        const response = await this.request(
            "/auth/me",
            {
                method: "GET",
            }
        )

        return response.json()
    }


    async getAuthors(): Promise<AuthorResponse[]>
    {
        const response = await this.request(
            "/authors",
            {
                method: "GET",
            }
        )

        return response.json()
    }


    async getAuthorById(id: string): Promise<AuthorResponse>
    {
        const response = await this.request(
            `/authors/${id}`,
            {
                method: "GET",
            }
        )

        return response.json()
    }


    public async createAuthor(request: AuthorCreateRequest): Promise<AuthorResponse>
    {
        const response = await this.request(
            "/authors",
            {
                method: "POST",
                body: JSON.stringify(request),
                headers: { "Content-Type": "application/json" }
            }
        )

        return response.json()
    }



    public async getUniverses(author_id: string | null): Promise<UniverseResponse[]>
    {
        const query = author_id !== null ? `?author_id=${author_id}` : ""

        const response = await this.request(
            `/universes${query}`,
            {
                method: "GET"
            }
        )

        return response.json()
    }


    public async createUniverse(request: UniverseCreateRequest): Promise<UniverseResponse>
    {
        const response = await this.request(
            `/universes`,
            {
                method: "POST",
                body: JSON.stringify(request),
                headers: { "Content-Type": "application/json" }
            }
        )

        return response.json()
    }


    public async getSeries(author_id: string | null, universe_id: string | null): Promise<SeriesResponse[]>
    {
        const author_filter = author_id !== null ? `author_id=${author_id}` : ""
        const universe_filter = universe_id !== null ? `&universe_id=${universe_id}` : ""
        
        let filter = ""

        if (author_id !== null && universe_id !== null)
        {
            filter = `?${author_filter}&${universe_filter}`
        }
        else if (universe_id !== null)
        {
            filter = `?${universe_filter}`
        }
        else if (author_id !== null)
        {
            filter = `?${author_filter}`
        }
        
        const response = await this.request(
            `/series${filter}`,
            {
                method: "GET"
            }
        )

        return response.json()
    }


    public async getSeriesById(id: string): Promise<SeriesDetailResponse>
    {
        const response = await this.request(
            `/series/${id}`,
            {
                method: "GET"
            }
        )

        return response.json()
    }


    public async createSeries(request: SeriesCreateRequest): Promise<SeriesResponse>
    {
        this.logger.debug(`sending series create request`, request)

        const response = await this.request(
            `/series`,
            {
                method: "POST",
                body: JSON.stringify(request),
                headers: { "Content-Type": "application/json" }
            }
        )

        return response.json()
    }


    public async getTags(): Promise<TagResponse[]>
    {
        this.logger.debug(`sending tags request`)

        const response = await this.request(
            `/tags`,
            {
                method: "GET",
            }
        )

        return response.json()
    }


    public async getTag(id: string): Promise<TagDetailResponse>
    {
        this.logger.debug(`sending GET /tags/${id}`)

        const response = await this.request(
            `/tags/${id}`,
            {
                method: "GET",
            }
        )

        return response.json()
    }


    public async updateTag(id: string, request: TagUpdateRequest): Promise<TagDetailResponse>
    {
        this.logger.debug(`sending PATCH /tags/${id}`, { request: request })

        const response = await this.request(
            `/tags/${id}`,
            {
                method: "PATCH",
                body: JSON.stringify(request),
                headers: { "Content-Type": "application/json" }
            }
        )

        return response.json()
    }


    public async deleteTag(id: string): Promise<boolean>
    {
        this.logger.debug(`sending DELETE /tags/${id}`)

        const response = await this.request(
            `/tags/${id}`,
            {
                method: "DELETE"
            }
        )

        return response.status === 204
    }


    public async createTag(request: TagCreateRequest): Promise<TagResponse>
    {
        this.logger.debug(`sending tag create request`, request)

        const response = await this.request(
            `/tags`,
            {
                method: "POST",
                body: JSON.stringify(request),
                headers: { "Content-Type": "application/json" }
            }
        )

        return response.json()
    }


    public async createBook(request: BookCreateRequest): Promise<BookSummaryResponse>
    {
        this.logger.debug(`sending book create request`, request)

        const response = await this.request(
            `/books`,
            {
                method: "POST",
                body: toFormData(request)
            }
        )

        return response.json()
    }


    // GET /books?author_id=&series_id=&universe_id=&tag_id=
    public async getBooks(
        author_id:   string | null = null, 
        universe_id: string | null = null,
        series_id:   string | null = null,
        tag_id:      string | null = null,
    ): Promise<BookSummaryResponse[]>
    {
        const query_components: string[] = []

        if (author_id)   query_components.push(`author_id=${author_id}`)
        if (universe_id) query_components.push(`universe_id=${universe_id}`)
        if (series_id)   query_components.push(`series_id=${series_id}`)
        if (tag_id)      query_components.push(`tag_id=${tag_id}`)

        const query = query_components.length > 0 ? `?${query_components.join("&")}` : ""
        
        this.logger.debug(`sending GET /books${query}`)

        const response = await this.request(
            `/books${query}`,
            {
                method: "GET",
            }
        )

        return response.json()
    }


    public async getBook(id: string): Promise<BookDetailResponse>
    {
        this.logger.debug(`sending GET /books/${id}`)

        const response = await this.request(
            `/books/${id}`,
            {
                method: "GET",
            }
        )

        return response.json()
    }


    public async updateBook(id: string, request: BookUpdateRequest): Promise<BookSummaryResponse>
    {
        this.logger.debug(`sending PATCH /books/${id}`)

        const response = await this.request(
            `/books/${id}`,
            {
                method: "PATCH",
                body: JSON.stringify(request),
                headers: { "Content-Type": "application/json" }
            }
        )

        return response.json()
    }


    public async deleteBook(id: string): Promise<void>
    {
        this.logger.debug(`sending DELETE /books/${id}`)

        await this.request(
            `/books/${id}`,
            {
                method: "DELETE"
            }
        )
    }


    public async getCoverBlob(id: string): Promise<Blob>
    {
        const response = await this.request(
            `/covers/${id}`,
            {
                method: "GET",
            }
        )

        return response.blob()
    }


    public async getCovers(book_id: string): Promise<CoverResponse[]>
    {
        const filter = `?book_id=${book_id}`

        this.logger.debug(`sending GET /covers${filter}`)

        const response = await this.request(
            `/covers${filter}`,
            {
                method: "GET",
            }
        )

        return response.json()
    }


    public async createCover(request: CoverCreateRequest): Promise<CoverResponse>
    {
        this.logger.debug(`sending POST /covers`, { request: request })

        const response = await this.request(
            `/covers`,
            {
                method: "POST",
                body: coverCreateRequestToFormData(request)
            }
        )

        return response.json()
    }


    public async updateCover(id: string, request: CoverUpdateRequest): Promise<CoverResponse>
    {
        this.logger.debug(`sending PATCH /covers/${id}`, { request: request })

        const response = await this.request(
            `/covers/${id}`,
            {
                method: "PATCH",
                body: JSON.stringify(request),
                headers: { "Content-Type": "application/json" }
            }
        )

        return response.json()
    }


    public async deleteCover(id: string): Promise<void>
    {
        this.logger.debug(`sending DELETE /covers/${id}`)

        await this.request(
            `/covers/${id}`,
            {
                method: "DELETE",
            }
        )
    }


    public async getReadRecords(book_id: string): Promise<ReadRecordResponse[]>
    {
        const filter = `?book_id=${book_id}`

        this.logger.debug(`sending GET /read-records${filter}`)

        const response = await this.request(
            `/read-records${filter}`,
            {
                method: "GET",
            }
        )

        return response.json()
    }


    public async getReadRecord(id: string): Promise<ReadRecordResponse>
    {
        this.logger.debug(`sending GET /read-records/${id}`)

        const response = await this.request(
            `/read-records/${id}`,
            {
                method: "GET",
            }
        )

        return response.json()
    }


    public async updateReadRecord(id: string, request: ReadRecordUpdateRequest): Promise<ReadRecordResponse>
    {
        this.logger.debug(`sending PATCH /read-records/${id}`, { request: request })

        const response = await this.request(
            `/read-records/${id}`,
            {
                method: "PATCH",
                body: JSON.stringify(request),
                headers: { "Content-Type": "application/json" }
            }
        )

        return response.json()
    }


    public async createReadRecord(request: ReadRecordCreateRequest): Promise<ReadRecordResponse>
    {
        this.logger.debug(`sending POST /read-records`, { request: request })

        const response = await this.request(
            `/read-records`,
            {
                method: "POST",
                body: JSON.stringify(request),
                headers: { "Content-Type": "application/json" }
            }
        )

        return response.json()
    }


    public async deleteReadRecord(id: string): Promise<void>
    {
        this.logger.debug(`sending DELETE /read-records/${id}`,)

        await this.request(
            `/read-records/${id}`,
            {
                method: "DELETE",
            }
        )
    }


    public async getEditions(book_id: string): Promise<EditionResponse[]>
    {
        const filter = `?book_id=${book_id}`

        this.logger.debug(`sending GET /editions${filter}`)

        const response = await this.request(
            `/editions${filter}`,
            {
                method: "GET",
            }
        )

        return response.json()
    }


    public async createEdition(request: EditionCreateRequest): Promise<EditionResponse>
    {
        this.logger.debug(`sending POST /editions`, { request: request })

        const response = await this.request(
            `/editions`,
            {
                method: "POST",
                body: editionCreateRequestToFormData(request)
            }
        )

        return response.json()
    }


    public async updateEdition(id: string, request: EditionUpdateRequest): Promise<EditionResponse>
    {
        this.logger.debug(`sending PATCH /editions/${id}`, { request: request })

        const response = await this.request(
            `/editions/${id}`,
            {
                method: "PATCH",
                body: JSON.stringify(request),
                headers: { "Content-Type": "application/json" }
            }
        )

        return response.json()
    }


    public async deleteEdition(id: string): Promise<void>
    {
        this.logger.debug(`sending DELETE /editions/${id}`)

        await this.request(
            `/editions/${id}`,
            {
                method: "DELETE",
            }
        )
    }


    public async fetchImageFromUrl(url: string): Promise<Blob>
    {
        this.logger.debug(`send GET /image-proxy?url=${url}`)

        const response = await this.request(
            `/image-proxy?url=${url}`,
            {
                method: "GET",
            }
        )

        return response.blob()
    }


    public async generateInviteCode(): Promise<InviteCreateResponse>
    {
        const response = await this.request(
            `/auth/invite-code`,
            {
                method: "POST",
            }
        )

        return response.json()
    }
}