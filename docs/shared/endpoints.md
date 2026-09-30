# DTOs

> for all `PATCH` request DTOs, optional fields use the convention "present but null = clear the field, absent = don't touch it".

> foreign keys are IDs only. the client will make further requests if it needs additional data.

## /auth

`POST /auth/login`
logs in the user and returns the token and user info. this should be the only endpoint that does not require a token.
- request:  `LoginRequest`
- response: `LoginResponse`
```ts
AuthLoginRequest {
    username: string
    password: string
}

AuthLoginResponse {
    token: string
    user:  UserProfileResponse
}
```

`POST /auth/signup`
creates a new user account.
- request:  `LoginRequest`
- response: `LoginResponse`

`GET /auth/me`
retrieves the user details, based on the token in the header of the request.
- request:  none
- response: `UserProfileResponse`
```ts
UserProfileResponse {
    id:         string
    username:   string
    role:       string
    created_at: string
}
```

## /library

`GET /library?query=...`
retrieve a list of responses based on a query - can be books, series, authors - containing a label (user-friendly identifier - title, author name, etc) and an image id (the ID of an image resource associated with the type of the object - cover ID for a book).
- request:  none
- response: `LibrarySearchResponse[]`
```ts
LibrarySearchResponse {
    id:       string
    type:     "BOOK" | "AUTHOR" | "SERIES"
    label:    string
    image_id: string | null
}
```

## /authors

`GET /authors`
retrieve a list of all existing authors, sorted alphabetically by last_name-first_name.
- request:  none
- response: `AuthorResponse[]`
```ts
AuthorResponse {
    id:         string
    first_name: string | null
    last_name:  string
}
```

`GET /authors/{id}`
retrieve the specified author record by ID.
- request:  none
- response: `AuthorResponse`

`POST /authors`
create a new author.
- request:  `AuthorCreateRequest`
- response: `AuthorResponse`
```ts
AuthorCreateRequest {
    first_name: string | null
    last_name:  string
}
```

## /universes

`GET /universes(?author_id=...)`
retrieve all universes (maybe) associated with an author, sorted alphabetically by name.
- request:  none
- response: `UniverseResponse[]`
```ts
UniverseResponse {
    id:        string
    author_id: string
    name:      string
}
```

`POST /universes`
create a new universe.
- request:  `UniverseCreateRequest`
- response: `UniverseResponse`
```ts
UniverseCreateRequest {
  author_id: string
  name:      string
}
```

## /series

`GET /series?author_id=...&universe_id=...`
retrieve all series filtered by author and/or universe, sorted alphabetically by name.
- request:  none
- response: `SeriesResponse[]`
```ts
SeriesResponse {
    id:          string
    author_id:   string
    universe_id: string | null
    name:        string
    is_finished: boolean
}
```

`GET /series/{id}`
retrieve series info (including info about books in the series).
- request:  none
- response: `SeriesDetailResponse`
```ts
SeriesDetailResponse {
    id:          string
    author_id:   string
    universe_id: string | null
    name:        string
    is_finished: boolean
    books:       SeriesBookItem[]
}


SeriesBookItem {
    id:            string
    cover_id:      string | null
    volume_number: number | null
    title:         string
    launch_date:   string | null
    word_count:    number | null
    read_status:   "UNREAD" | "IN_PROGRESS" | "DNF" | "READ"
    reread_count:  number
}
```

`POST /series`
create a new series.
- request:  `SeriesCreateRequest`
- response: `SeriesResponse`
```ts
SeriesCreateRequest {
    author_id:   string
    universe_id: string | null
    name:        string
    is_finished: boolean | null
}
```

## /tags

`GET /tags`
retrieve a list of all existing tags, sorted alphabetically by name.
- request:  none
- response: `TagResponse[]`
```ts
TagResponse {
    id:          string
    name:        string
    description: string | null
    book_count:  number
}
```

`GET /tags/{id}`
- request:  none
- response: `TagDetailResponse`
```ts
TagDetailResponse {
    id:          string
    name:        string
    description: string | null
    books:       TagBookItem[]
}

TagBookItem {
    id:        string
    title:     string
    series_id: string | null
    cover_id:  string | null
}
```

`POST /tags`
creates a new tag.
- request:  `TagCreateRequest`
- response: `TagResponse`
```ts
TagCreateRequest {
    name:        string
    description: string | null
}
```

`PATCH /tags/{id}`
updates the tag data. the server has to add/remove links between tags and books based on the ID arrays.
- request:  `TagUpdateRequest`
- response: `TagDetailResponse`
```ts
TagUpdateRequest {
    name?:             undefined | string
    description?:      undefined | string | null
    added_book_ids?:   undefined | string[]
    removed_book_ids?: undefined | string[]
}
```

`DELETE /tags/{id}`
deletes a tag and all it's usage in all books.
- request:  none
- response: none

## /books

`GET /books?author_id=&series_id=&universe_id=&tag_id=`
retrieves all books filtered by author/universe/series/tag, ordered by last_name-first_name-universe_name-series_name-volume_number-title.
- request:  none
- response: `BookSummaryResponse[]`
```ts
BookSummaryResponse {
    id:            string
    author_id:     string
    universe_id:   string | null
    series_id:     string | null
    cover_id:      string | null
    volume_number: number | null
    title:         string
    read_status:   "UNREAD" | "IN_PROGRESS" | "DNF" | "READ"
    reread_count:  number
}
```

`GET /books/{id}`
retrieve data about a book.
- request:  none
- response: `BookDetailResponse`
```ts
BookDetailResponse {
    id:            string
    author_id:     string
    series_id:     string | null
    universe_id:   string | null
    cover_id:      string | null
    volume_number: number | null
    title:         string
    launch_date:   string | null
    word_count:    number | null
    blurb:         string | null
    notes:         string | null
    tag_ids:       string[]
    read_status:   "UNREAD" | "IN_PROGRESS" | "DNF" | "READ"
    reread_count:  number
}
```

`POST /books` (multipart)
create a new book entry based on the block of data collected in the app.
- request:  `BookCreateRequest`
- response: `BookSummaryResponse`
```ts
BookCreateRequest
{
    author_id:     string
    universe_id:   string | null
    series_id:     string | null
    volume_number: number | null
    title:         string
    launch_date:   string | null
    word_count:    number | null
    blurb:         string | null
    notes:         string | null
    tag_ids:       string[] | null
    covers:        EmbeddedCover[] | null
    editions:      EmbeddedEdition[] | null
}

EmbeddedCover = {
    file:       File
    label?:     string
    is_default: boolean
}

EmbeddedEdition = {
    file:          File
    label?:        string
    format:        string
    release_date?: string
}
```
> for `covers[]` and `editions[]`, each array entry's fields are sent as separate multipart parts named `covers[N].field` (e.g. `covers[0].label`, `covers[0].file`, `covers[0].is_default`), where N is the zero-based index of the entry.

> at most one cover can be set as default. if more then one is set, the server return `400 Bad Request`.

`PATCH /books/{id}`
update book information.
- request:  `BookUpdateRequest`
- response: none
```ts
BookUpdateRequest {
    author_id?:     undefined | string
    universe_id?:   undefined | string | null
    series_id?:     undefined | string | null
    cover_id?:      undefined | string | null
    volume_number?: undefined | number | null
    title?:         undefined | string
    launch_date?:   undefined | string | null
    word_count?:    undefined | number | null
    blurb?:         undefined | string | null
    notes?:         undefined | string | null
    tag_ids?:       undefined | string[]
}
```

`DELETE /books/{id}`
delete a book by id.
- request:  none
- response: none

## /covers

`GET /covers?book_id=...`
retrieves information about all covers of a book. the cover image has to be loaded separately.
- request:  none
- response: `CoverResponse[]`
```ts
CoverResponse {
    id:         string
    book_id:    string
    label:      string | null
    added_at:   string
    is_default: boolean
}
```

`GET /covers/{id}`
retrieve the cover image file for a book by the cover ID. needs the bearer token as part of the header, so normal `<img src>` will not work. the client fetches the images as blobs.
- request:  none
- response: image file as blob

`POST /covers` (multipart)
add a new cover to a book.
- request:  `CoverCreateRequest`
- response: `CoverResponse`
```ts
CoverCreateRequest {
    book_id: string
    label:   string | null
    file:    File
}
```

`PATCH /covers/{id}`
update cover information.
- request:  `CoverUpdateRequest`
- response: `CoverResponse`
```ts
CoverUpdateRequest {
    label?: undefined | string | null
}
```

`DELETE /covers/{id}`
delete a cover. also needs to make sure the cover is removed from the book record if it is a preferred cover; in this case, the latest remaining cover is set as preferred if any exists, else `null`.
- request:  none
- response: none

## /editions

`GET /editions?book_id=...`
retrieve list of all editions for the book, ordered by `added_at` asc.
- request:  none
- response: `EditionResponse[]`
```ts
EditionResponse {
    id:           string
    book_id:      string
    label:        string | null
    release_date: string | null
    format:       string
    added_at:     string
}
```

`POST /editions` (multipart)
create a new edition.
- request:  `EditionCreateRequest`
- response: `EditionResponse`
```ts
EditionCreateRequest {
    book_id:      string
    label:        string | null
    release_date: string | null
    format:       string
    file:         File
}
```

`PATCH /editions/{id}`
update edition information.
- request:  `EditionUpdateRequest`
- response: `EditionResponse`
```ts
EditionUpdateRequest {
    label?:        undefined | string | null
    release_date?: undefined | string | null
    format?:       undefined | string
}
```

`DELETE /editions/{id}`
delete an edition of the book. deleting an edition also deletes the read records associated to that edition.
- request:  none
- response: none

## /read-records

`GET /read-records?book_id=...`
retrieves all read records for the book, ordered from newest to oldest.
- request:  none
- response: `ReadRecordResponse[]`
```ts
ReadRecordResponse {
    id:             string
    book_id:        string
    edition_id:     string | null
    start_date:     string | null
    end_date:       string | null
    did_not_finish: boolean
    is_historical:  boolean
    stop_position:  string | null
    notes:          string | null
}
```

`GET /read-records/{id}`
retrieve the information for a specific read record.the read number is computed based on the index of the read in the response from the `GET /read-records?book_id=...` endpoint.
- request:  none
- response: `ReadRecordResponse`

`POST /read-records`
create a new read record.
- request: `ReadRecordCreateRequest`
- response: `ReadRecordResponse`
```ts
ReadRecordCreateRequest {
    book_id:        string,
    edition_id:     string|null,
    start_date:     string | null
    end_date:       string | null
    did_not_finish: boolean
    is_historical:  boolean
    stop_position:  string | null
    notes:          string | null
}
```

`PATCH /read-records/{id}`
 edit the read record.
- request:  `ReadRecordUpdateRequest`
- response: `ReadRecordResponse`
```ts
ReadRecordUpdateRequest {
    edition_id?:     undefined | string | null
    start_date?:     undefined | LocalDate | null
    end_date?:       undefined | LocalDate | null
    did_not_finish?: undefined | boolean
    is_historical?:  undefined | boolean
    stop_position?:  undefined | string | null
    notes?:          undefined | string | null
}
```

`DELETE /read-records/{id}`
delete a read record.
- request:  none
- response: none

## /image-proxy

`GET /image-proxy?url=...`
download the image from the specified URL and return it as a blob.
- request:  none
- response: none