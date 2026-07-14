# Endpoints

> for all endpoints that handle updates (PATCH), the server has to implement `Optional` data type for all fields that can be `null`, so to differentiate between set-to-null and missing values.

## /auth

`POST /auth/login`
logs in the user and returns the token and user info. this should be the only endpoint that does not require a token.
- request:  `AuthLoginRequest`
- response: `AuthLoginResponse`

`GET /auth/me`
retrieves the user details, based on the token in the header of the request.
- request:  none
- response: `UserProfileResponse`

## /library

`GET /library?query=...`
retrieve a list of responses based on a query - can be books, series, authors - containing a label (user-friendly identifier - title, author name, etc) and an image id (the ID of an image resource associated with the type of the object - cover ID for a book).
- request:  none
- response: `LibrarySearchResponse`

## /authors

`GET /authors`
retrieve a list of all existing authors, sorted alphabetically by `last_name-first_name`.
- request:  none
- response: `[ AuthorResponse ]`

`POST /authors`
create a new author.
- request:  `AuthorCreateRequest`
- response: `AuthorResponse`

## /universe

`GET /universes?author_id=...`
retrieve all universes associated with an author, sorted alphabetically by name.
- request:  none
- response: `[ UniverseResponse ]`

`POST /universes`
create a new universe.
- request:  `UniverseCreateRequest`
- response: `UniverseResponse`

## /series

`GET /series?author_id=...&universe_id=...`
retrieve all series names filtered by author and/or universe, sorted alphabetically by name.
- request:  none
- response: `SeriesSummaryResponse`

`GET /series?projection=full`
retrieve all series info, grouped by universe and author, alongside universe and author info.
- request:  none
- response: `[ SeriesGroupedFullResponse ]`

`GET /series/{id}`
retrieve series info (including info about books in the series).
- request:  none
- response: `SeriesDetailResponse`

`POST /series`
create a new series.
- request:  `SeriesCreateRequest`
- response: `SeriesResponse`

## /tags

`GET /tags`
retrieve a list of all existing tags, sorted alphabetically by name.
- request:  none
- response: `TagSummaryResponse`

`GET /tags/{id}?projection...`
> `projection`:
> - `usage` = tag info and info of all the books that use it
> - `links` = tag info and the number of books that use it
- request:  none
- response: `TagLinkProjectionResponse` / `TagUsageProjectionResponse`

`POST /tags`
creates a new tag.
- request:  `TagCreateRequest`
- response: `TagResponse`

`PATCH /tags/{id}/with-links`
updates the tag data. the server has to find all books that use that tag and compare them with the usage list. if a book does not match, remove the tag from it.
- request:  `TagUpdateRequest`
- response: `TagUsageProjectionResponse`

`DELETE /tags/{id}`
deletes a tag and all it's usage in all books.
- request:  none
- response: none

## /books

`GET /books?view=...(&author_id=...)`
retrieves a list or tree of books based on the requested view layout and/or filtered by author/series.
> `view`: 
> - `summary`   = ungrouped
> - `by-author` = grouped by author
> - `by-series` = grouped by series
- request:  none
- response: `[ BookSummaryResponse ]` / `[ BookGroupedByAuthorResponse ]` / `[ BookGroupedBySeriesResponse ]`

`GET /books/{id}?projection=...`
retrieve data about a book.
> `projection`:
> - `summary`   = info for displaying the cover
> - `details`   = info for Book/Details page
> - `full`      = full book info
- request:  none
- response: `BookSummaryResponse` / `BookDetailProjectionResponse` / `BookFullProjectionResponse`

`GET /books/{id}/covers`
get a list of cover data for a book. the cover image has to be loaded separately.
- request:  none
- response: `[ CoverResponse ]`

`POST /books`
create a new book entry based on the block of data collected in the app. multipart endpoint.
- request:  `BookCreateRequest`
- response: `BookSummaryResponse`

`PATCH /books/{id}`
update book information.
- request:  `BookUpdateRequest`
- response: none

## /covers

`GET /covers/{id}`
retrieve the cover image file for a book by the cover ID. needs the bearer token as part of the header, so normal `<img src>` will not work. the client fetches the images as blobs.
- request:  none
- response: image file as blob

`POST /covers`
add a new cover to a book. this request is multipart.
- request:  `CoverCreateRequest`
- response: `CoverResponse`

`PATCH /covers/{id}`
update cover information.
- request:  `CoverUpdateRequest`
- response: `CoverResponse`

`DELETE /covers/{id}`
delete a cover. also needs to make sure the cover is removed from the book record if it is a preferred cover; in this case, the latest remaining cover is set as preferred if any exists, else `null`.
- request:  none
- response: none

## /editions

`GET /editions?book_id=...&projection=...`
retrieve list of all editions for the book.
> `projection`:
> - `summary` = just id, label, format.
> - `full`    = all info about the edition and it's reads.
- request:  none
- response: `[ EditionSummaryResponse ]` / `[ EditionFullProjectionResponse ]`

`POST /editions`
create a new edition. this is a multipart request.
- request:  `EditionCreateRequest`
- response: `EditionResponse`

`PATCH /editions`
update edition information.
- request:  `EditionUpdateRequest`
- response: `EditionResponse`

`DELETE /editions/{id}`
delete an edition of the book. deleting an edition also deletes the read records associated to that edition.
- request:  none
- response: none

## /read-records

`GET /read-records?book_id=`
retrieves all read records for the book, ordered from newest to oldest.
- request:  none
- response: `[ ReadRecordBookListResponse ]`

`GET /read-records/{id}`
retrieve the information for a specific read record. the server has to compute the number of the read record in relation to the others (chronological order).
- request:  none
- response: `ReadRecordDetailResponse`

`PATCH /read-records/{id}`
edit the read record.
- request:  `ReadRecordUpdateRequest`
- response: `ReadRecordResponse`

`DELETE /read-records/{id}`
delete a read record.
- request:  none
- response: none
