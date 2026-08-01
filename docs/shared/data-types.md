# Data Types

## /auth

<!-- request to log in -->
AuthLoginRequest    = `{ username, password }`

<!-- response to a login -->
<!-- TODO: is this necessary or just use the UserProfileResponse -->
AuthLoginResponse   = `{ id, token }` 

<!-- response to user profile request -->
UserProfileResponse  = `{ id, username, role, created_at }`

## /library

<!-- response to a search; on Library -->
LibrarySearchResponse = `{ type, label, id, image_id? }`

## /authors

<!-- request to create an author; on Add-EditBook/SelectAuthor -->
AuthorCreateRequest = `{ first_name?, last_name }`

<!-- response to creating an author -->
AuthorResponse      = `{ id, first_name?, last_name }`

## /universes

<!-- request to create a universe; on Add-EditBook/SelectUniverse -->
UniverseCreateRequest   = `{ author_id, name }`

<!-- response to creating a universe and to all universes for an author; on Add-EditBook/SelectUniverse -->
UniverseResponse = `{ id, author_id, name }`

<!-- created because of nesting requirements -->
UniverseSummaryResponse = `{ id, name }`

## /series

<!-- request to create a new series; on Add-EditBook/SelectSeries -->
SeriesCreateRequest     = `{ author_id, universe_id?, name }`

<!-- response to creating a new series -->
SeriesResponse    = `{ id, author_id, universe_id?, name, is_finished }`

<!-- response to all series associated with a universe/author; on Add-EditBook/SelectSeries -->
SeriesSummaryResponse   = `{ id, name }`

<!-- response to series details; SeriesProfile -->
SeriesDetailResponse  = `{ id, name, author: { id, first_name?, last_name }, books: [{ id, title, release_date?, word_count?, read_status }] }`
SeriesDetailResponse  = `{ id, name, author: AuthorResponse, books: [ BookForSeriesResponse ] }`

<!-- response to all books by an author, grouped by series; on Author/bySeries -->
SeriesGroupedFullResponse = `{ universe: { id, name }, series: { id, name }, books: [{ id, title, cover_id? }] }`
SeriesGroupedFullResponse = `{ universe: UniverseSummaryResponse, series: SeriesSummaryResponse, books: [ BookSummaryResponse ] }`

<!-- created because of nesting requirements -->
SeriesVolumeNumberResponse  = `{ id, name, number_of_volumes }`

## /tags

<!-- request to create a new tag; on Add-EditBook/SelectTags -->
TagCreateRequest    = `{ name, description }`

<!-- request to update a tag and the books that link to it; on EditTag -->
TagUpdateRequest    = `{ name?, description?, books: [ id ]? }`

<!-- response to creating a tag -->
TagResponse         = `{ id, name, description }`



<!-- response to find all tags; on Add-EditBook/SelectTags -->
TagSummaryResponse  = `{ id, name }`

<!-- response to all tags and the number of links; on AllTags/byName or AllTags/byUsage -->
TagLinkProjectionResponse   = `{ id, name, links }`

<!-- response to tag info and all books that use it; on TagProfile -->
TagUsageProjectionResponse  = `{ id, name, description?, books: [{ id, title, cover_id? }] }`
TagUsageProjectionResponse  = `{ id, name, description?, books: [ BookSummaryResponse ] }`

## /covers

<!-- request to create a new cover; on AddBook/ManageCovers or EditBook/ManageCovers -->
CoverCreateRequest  = `{ book_id, cover_file }` - multipart

<!-- request to update cover information; on AddBook/ManageCovers or EditBook/ManageCovers -->
CoverUpdateRequest  = `{ label? }`

<!-- response to creating a new cover and to all covers of a book; on BookProfile/Covers-->
CoverResponse       = `{ id, book_id, label?, added_at }`

## /editions

<!-- request to create a new edition; on EditBook/ManageEditions -->
EditionCreateRequest    = `{ book_id, format, file }`

<!-- request to update an edition; on EditBook/ManageEditions -->
EditionUpdateRequest    = `{ label? }`

<!-- response to all edition for a book; on EditRead -->
EditionSummaryResponse  = `{ id, label?, format }`

<!-- response to all information about editions of a book; on EditBook/ManageEditions -->
EditionFullProjectionResponse   = `{ id, label?, format, added_at, reads: [{ id, start_date?, end_date?, read_number }] }`
EditionFullProjectionResponse   = `{ id, label?, format, added_at, reads: [ ReadRecordSummaryResponse ] }`

<!-- response to creating/updating an edition-->
EditionResponse = `{ id, book_id, label?, format, added_at }`

## /read-records

<!-- request to create a read record; on Book/Reads -->
ReadRecordCreateRequest = `{ book_id }`

<!-- request to update read record; on EditRead -->
ReadRecordUpdateRequest = `{ start_date?, end_date?, edition_id?, notes? }`

<!-- response to all read records for a book; on Book/Reads -->
ReadRecordBookListResponse  = `{ id, start_date?, end_date?, notes?, read_number, edition: { id, label?, format, added_at } }`
ReadRecordBookListResponse  = `{ id, start_date?, end_date?, notes?, read_number, edition: EditionResponse }`

<!-- response to all information about a read record; on ReadInfo -->
ReadRecordDetailResponse    = `{ book: { id, title, word_count?, author: { first_name?, last_name } }, edition: { id, label?, format }, read_number, start_date?, end_date?, notes? }`
ReadRecordDetailResponse    = `{ book: BookForReadRecordResponse, edition: EditionSummaryResponse, read_number, start_date?, end_date?, notes? }`

<!-- response to create/update a read record -->
ReadRecordResponse  = `{ id, book_id, edition_id?, start_date?, end_date?, did_not_finish, is_historical, stop_position?, notes? }`

<!-- created because of nesting requirements -->
ReadRecordSummaryResponse   = `{ id, start_date?, end_date?, read_number }`

## /books

<!-- request to create a book; on AddBook -->
BookCreateRequest   = `{ author_id, universe_id?, series_id?, volume_number?, title, release_date?, word_count?, tags: [ id ], opinion?, blurb?, covers: [{ label?, file, is_default }], editions: [{ label?, format, file }] }`
BookCreateRequest   = `{ author_id, universe_id?, series_id?, volume_number?, title, release_date?, word_count?, tags: [ id ], opinion?, blurb?, covers: [ BookCoverCreateResponse ], editions: [ BookEditionCreateResponse ] }`

<!-- request to update book info; on EditBook/... -->
BookUpdateRequest   = `{ title?, author_id?, universe_id?, series_id?, cover_id?, volume_number?, title?, launch_date?, word_count?, blurb?, opinion? }`

<!-- response to all books; could be filtered by author; on Library/Gallery or Author/Gallery -->
BookSummaryResponse = `{ id, title, cover_id? }`

<!-- response to all books grouped by author; on Library/byAuthor -->
BookGroupedByAuthorResponse = `{ author: { id, first_name?, last_name }, books: [{ id, title, cover_id? }] }`
BookGroupedByAuthorResponse = `{ author: AuthorResponse, books: [ BookSummaryResponse ] }`

<!-- response to all books grouped by author and universe; on Library/bySeries -->
BookGroupedBySeriesResponse = `{ author: { id, first_name?, last_name }, universes: [{ universe: { id, name }, series: [{ id, name, number_of_volumes }] }] }`
BookGroupedBySeriesResponse = `{ author: AuthorResponse, universes: [ BookUniverseGroupResponse ] }`

<!-- response to book details; on BookProfile/Details -->
BookDetailProjectionResponse = `{ author: { id, first_name?, last_name }, universe: { id, name }?, series: { id, name }?, volume_number?, title, cover_id?, read_status, number_or_reads, release_date?, word_count?, tags: [{ id, name }], opinion?, blurb? }`
BookDetailProjectionResponse = `{ author: AuthorResponse, universe: UniverseSummaryResponse?, series: SeriesSummaryResponse?, volume_number?, title, cover_id?, read_status, number_or_reads, release_date?, word_count?, tags: [ TagSummaryResponse ], opinion?, blurb? }`

<!-- response to book full details; on EditBook -->
BookFullProjectionResponse  = `{ author: { id, first_name?, last_name }, universe: { id, name }?, series: { id, name }?, volume_number?, title, cover: { id, label }?, release_date?, word_count?, tags: [{ id, name }], opinion?, blurb?, number_or_covers }`
BookFullProjectionResponse  = `{ author: AuthorSummaryResponse, universe: UniverseSummaryResponse?, series: SeriesSummaryResponse?, volume_number?, title, cover: CoverResponse?, release_date?, word_count?, tags: [ TagSummaryResponse ], opinion?, blurb?, number_or_covers }`

<!-- created because of nesting requirements -->
BookCoverCreateResponse     = `{ label?, file, is_default }`
BookEditionCreateResponse   = `{ label?, format, file }`
BookUniverseGroupResponse   = `{ universe: UniverseSummaryResponse, series: [ SeriesVolumeNumberResponse ] }`
BookForSeriesResponse       = `{ id, title, release_date?, word_count?, read_status }`
BookForReadRecordResponse   = `{ id, title, word_count?, author: AuthorResponse }`