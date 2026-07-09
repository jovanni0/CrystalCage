# API Contract

## Endpoints

### /auth

- `POST /auth/login` = logs in the user and returns the token and user info, as `{ id, token }`. the request has the username and password in the body `{ username, password }`. this should be the only endpoint that does not require a token.
- `GET /auth/me` = retrieves the user details as `{ id, username, role, created_at }`, based on the token in the header of the request.

### /library

- `GET /library?query=...` = retrieve a list of responses based on a query - can be books, series, authors - as `[{ type: book | author | series }, label, id, image_id: string|null }]`, where label is the user-friendly identifier (title, author name etc) and `image_id` represents the ID of an image resource associated with the type of the object (cover ID for a book).

### /authors

- `GET /authors` = retrieve a list of all existing authors as `{ id, first_name, last_name }`. filtering is expected to be done on the client-side.
- `POST /authors` = create a new author, as `{ first_name, last_name }`.

### /universe

- `GET /universes?author_id=...` = retrieve all universes associated with an author as `{ id, name }`. filtering is expected to be done on the client-side.
- `POST /universes` = create a new universe, as `{ name, author_id }`.

### /series

- `GET /series?universe_id=...` = retrieve all series associated with an universe as `{ id, name }`. filtering is expected to be done on the client-side.
- `GET /series?author_id=...` = retrieve all series associated with an author as `{ id, name }`. necessary because not all series have a universe, some are free-floating.  filtering is expected to be done on the client-side.
- `POST /series` = create a new series, as `{ name, universe_id: number|null, author_id }`

### /tags

- `GET /tags` = retrieve a list of all existing tags, sorted alphabetically by name, as `[{ id, name }]`. filtering is expected to be done on the client-side.
- `GET /tags/{id}` = retrieve the tag info as `{ name, description, usage: [{ book_id, book_title, cover_id }]}`. the list of books that use the tag is derived server-side and returned as part of the response.
- `GET /tags/usage` = retrieve a list of all tags and the number of books that use the tag, as `[{ tag_id, name, number_or_books }]`.
- `POST /tag` = creates a new tag as: `{ name, description }`
- `PATCH /tag/{id}` = updates the tag data, by sending the body: `{ name, description, usage: [{ book_id }]}`. the server has to find all books that use that tag and compare them with the usage list. if a book does not match, remove the tag from it.
- `DELETE /tag/{id}` = deletes a tag and all it's usage in all books.

### /books

- `GET /books` = retrieve a list of all books in the database, their ID, title and cover ID as `[{ id, title, cover_id }]`.
- `GET /books?author=...` = retrieves the books by a specific author as `[{ id, title, cover_id }]`
- `GET /books/by-author` = retrieve a list of books sorted by author as `[{ author: { id, last_name, first_name }, books: [{ id, title }] }]`.
- `GET /books/as-series` = retrieve a list of series sorted by author and universe as `[{ author: {id, first_name, last_name }, universes: [{ universe: { id, name }, series: [{ id, name, number_of_volumes}] }] }]`. the required sorting happens server-side and the client should trust it and not sort it further.
- `GET /books/by-series?author=...` = retrieves a list of books of a specific author, sorted by series, as `[{ series: { id, name}, universe: { id, name }, books: [{ id, title, cover_id }] }]`
- `GET /books/series?id=...` = retrieve series info as `{ series_name, author: { id, first_name, last_name }, books: [{ id, title, release_date, word_count, read_status }] }`.
- `GET /books/{id}/details` = retrieve the book details as `{ author: { id, first_name, last_name }, universe: { id, name }, series: { id, name }, volume_number, title, cover_id, read_status, number_or_reads, release_date, word_count, tags: [{ id, name }], opinion, blurb }`. the number of reads and read is calculated server-side based on the read records for the book, using the algorithm described [here](database-model.md#read-status).
- `GET /books/{id}/covers` = get a list of cover IDs and data for a book as `[{ id, label, first_added }]`. the cover image has to be loaded separately.
- `GET /books/{id}/full` = retrieves full information about a book as `{ author: { id, first_name, last_name }, universe: { id, name } | null, series: { id, name } | null, volume_number: int|null, title, preferred_cover: { id, name }, release_date, word_count, tags: [{ id, name }], opinion, blurb, number_or_covers }`.
- `GET /books/{id}/read-records` = retrieves all read records for the book, ordered from newest to oldest server-side as `[{ record_id, start_date, end_date: date|null, notes, edition: { label, format, first_added } }]`.
- `GET /books/{id}/editions` = retrieve list of all editions for the book, as `[{ edition_id, label, format }]`.
- `GET /books/{id}/editions/full` = retrieve a list of full information about editions, as `[{ id, label, format, first_added, reads: [{ id, red_number, start_date, end_date }] }]`
- `GET /books/{book_id}/read-records/{record_id}` = retrieve the information for a specific read record as `{ book: { book_id, author_first_name, author_last_name, title, word_count }, edition: { edition_id, label, format }, read_number, start_date, end_date, notes}`. the server has to compute the number of the read record in relation to the others (chronological order).
- `POST /books` = create a new book entry based on the block of data collected in the app. multipart endpoint.
- `POST /books/{id}/editions` = create a new edition, as `{ file, label, format }`. this is a multipart request.
- `PATCH /books/{id}/read-records` = edit the read record with the body: `{ record_id, start_date, end_date, edition_id, notes }`.
- `PATCH /books/{id}/editions` = update edition information, as `{ edition_id, label, format }`.
- `PATCH /books/{id}/opinion` = update the opinion of a book, as `{ opinion }`.
- `PATCH /books/{id}/preferred-cover` = set the preferred cover ID for a book, where the body of the query is `{ cover_id }`.
- `PATCH /books/{id}/blurb` = update the blurb of a book, as `{ blurb }`.
- `DELETE /books/{book_id}/read-records/{record_id}` = delete a read record.
- `DELETE /books/{book_id}/editions/{edition_id}` = delete an edition of the book. deleting an edition also deletes the read records associated to that edition.

### /covers

- `GET /covers/{id}` = retrieve the cover image file for a book by the cover ID. needs the bearer token as part of the header, so normal `<img src>` will not work. the client fetches the images as blobs.
- `GET /covers/{id}/full-resolution` = get a cover at full resolution.
- `GET /covers/{id}/low-resolution` = get a cover at low resolution, for small images that do not need details.
- `POST /covers` = add a new cover to a book, with body `{ book_id, cover_file }`. this request is multipart.
- `PATCH /covers/{id}` = update cover information, as `{ label }`.
- `DELETE /covers/{id}` = delete a cover. also needs to make sure the cover is removed from the book record if it is a preferred cover; in this case, the latest remaining cover is set as preferred if any exists.

## Add Book flow

### Add Book

data rendered:
- default cover image
- default cover label
- number of other cover options
- author first name + last name
- universe
- series
- volume number
- title
- release date
- word count
- tags
- user opinion
- blurb
- number of editions
- edition formats (epub, mobi)
- latest added edition

actions:
- delete book
- change volume number
- change title
- change release date
- change word count
- save edits
- cancel edits

navigation:
- author selection page
- universe selection page
- series selection page
- tags selection page
- opinion page
- blurb page
- covers page
- editions page

> because when adding a new book most of the information is linked to the book's ID (which does not yet exist), all the data added during the process should be stored locally and sent to the server when the user has decided to save the book, or dropped if canceled.

information stored locally:
- author
  - author ID / null
  - last name
  - first name
- universe / null
  - universe ID / null
  - universe name
- series name
  - series ID / null
  - series name
- volume number
- title
- release date
- word count
- tags list (per item):
  - tag ID / null
  - tag name
  - description null (because the description is added on the tag page)
- opinion
- blurb
- covers list (per item):
  - cover ID / null
  - cover image
  - cover label
  - is default
- editions list (per item):
  - edition file
  - edition label
  - edition format
  
> read records are added on a separate page, so new edition records will not contain any read record information at this point.

```
book: {
  author: { new: { first_name, last_name } | existing: { id } },
  universe: { new: { universe_name } | existing: { id } } | null,
  series: { new: { series_name } | existing: { id } } | null,
  volume_number: int | null,
  title,
  release_date: date | null,
  word_count: int | null,
  tags: [{ new: { tag_name } | existing: { id } }],
  opinion: string | null,
  blurb: string | null,
  covers_list: [{ cover_image, cover_label, is_default }],
  editions_list: [{ edition_file, edition_label, edition_format }]
}
```

endpoints:
- `POST /books`

> because the server is hit only then the user confirms saving the book on the Add Book page, no other page uses the server to save/update data. for the Edit Book flow, this is not true and every page updates the data directly on the server.

### Covers page

data rendered (for each cover):
- cover image
- cover label
- cover added date
- if the cover is the default one

actions: 
- add cover
- change default cover
- edit cover label (for each cover)
- delete cover (for each cover)


### Editions page

data rendered (for each edition):
- edition label
- added date
- format

actions:
- edit label (for each edition)
- delete edition (for each edition)
- add edition

### Author selection page

data rendered:
- a list of all authors available in the database

actions:
- create a new author
- select an author
- filter the list for an author

endpoints:
- `GET /authors`

### Universe selection page

data rendered:
- a list of all universes of the previously selected author

actions:
- create a new universe
- select an universe
- filter the list for an universe

endpoints:
- `GET /universes?author_id=...`

### Series selection page

data rendered:
- a list of all series related to the previously selected universe

actions:
- create a new series
- select an series
- filter the list for an series

endpoints:
- `GET /series?universe_id=...`
- `GET /series?author_id=...`

### Tag selection page

data rendered:
- a list of tags available in the database

actions:
- create a new tag
- select a tag
- filter the list for a tag

endpoints:
- `GET /tags`

### Opinion page

data rendered:
- the opinion text

actions:
- modify the opinion text

### Blurb page

data rendered:
- the blurb text

actions:
- modify the blurb text

## Library Flow

### Library-Gallery page

data rendered (for each cover):
- cover image
- title

actions:
- search

navigation:
- Add Book
- Library-Author
- Library-Series
- book page

endpoints:
- `GET /books`
- `GET /cover/{id}`
- `GET /library?query=...`

> because the `GET /cover/{id}` will not work with `<img src>`, a wrapper should be implemented that creates an object URL and revokes it when unmounting. an `InteractionObserver` needs to be built to achieve the lazy-loading natively handled by `img`, and the HTTP-level cache needs to be implemented by the caching layer of the app.

### Library-Author page

data rendered (for each author):
- (for each book):
  - cover image
  - title

actions:
- search

navigation:
- Add Book
- Library-Gallery
- Library-Series
- author page
- book page

endpoints:
- `GET /books/by-author`

### Library-Series page

data rendered (for each author):
- (for each universe):
  - series cover
  - series name
  - number of volume in series

actions:
- search

navigation:
- Add Book
- Library-Gallery
- Library-Author
- author page
- series page

endpoints:
- `GET /books/as-series`

## Author Flow

### Author-Gallery page

data rendered (for each book):
- cover image
- title

navigation:
- back
- Author-Series
- book page
- Add Book

endpoints:
- `GET /books?author=...`
- `GET /covers/{id}`

### Author-Series page

data rendered (for each series):
- series name
- universe
- (for each book in the series):
  - cover image
  - title

navigation:
- back
- series page
- book page
- Add Book

endpoints:
- `GET /books/by-series?author=...`
- `GET /covers/{id}`

## Series Page

data rendered:
- series name
- author name
- (for each book):
  - title
  - cover image
  - release date
  - word count
  - read status

navigation:
- author page
- book page
- back

endpoints:
- `GET /books/series?id=...`

## Book flow

### Book-Details

data rendered:
- title
- series
- universe
- author
- cover image
- read status
- release date
- word count
- number of (re)reads
- tags
- opinion
- blurb

navigation:
- Book-Covers
- Book-Reads
- author page
- series page
- tag info page (for each tag)
- book edit page

endpoints:
- `GET /books/{id}/details`
- `GET /covers/{id}`

### Book-Covers

data rendered (for each cover):
- cover image
- cover label
- added date
- preferred status
- full resolution cover (inspect)

navigation:
- Book-Details
- Book-Reads
- back
- book edit page

actions:
- set cover as favorite

endpoints:
- `GET /books/{id}/covers`
- `GET /covers/{id}`
- `GET /covers/{id}/full-resolution`
- `PATCH /books/{id}/preferred-cover`

### Book-Reads

data rendered 
- total read time
- (for each read):
  - read number (the n-th read of the book, index+1 from the record list, determined client-side, assuming the list is ordered server-side first)
  - start date
  - end date or read duration (if read in progress)
  - edition label
  - edition format
  - edition first added date
  - notes

navigation:
- Book-Details
- Book-Covers
- Read-Details (for each read record)
- back

actions:
- start read
- finish read (if read in progress)

endpoints:
- `GET /books/{id}/read-records`

## Tag Info flow

### Tag page

data rendered:
- name
- description
- number of books that use the tag
- (for each book that uses the tag):
  - cover image (low-res)
  - title
  - series

navigation:
- back
- Tag Edit
- book page

endpoints:
- `GET /tags/{id}`
- `GET /covers/{id}/low-resolution`

### Edit Tag page

data rendered:
- name
- description
- number of books that use the tag
- (for each book that uses the tag):
  - cover image (low-res)
  - title
  - series

actions:
- edit name
- edit description
- (for each book that uses that tag):
  - remove tag from book
- cancel
- save
- delete

> because the page uses the cancel/save pattern, the modifications need to be stored locally and committed all at once to the server.

endpoints:
- `GET /tags/{id}`
- `GET /covers/{id}/low-resolution`
- `PATCH /tag/{id}`
- `DELETE /tag/{id}`

## Read Info flow

### Read Info page

data rendered:
- book title
- book word count
- book edition label
- book edition format
- author name
- read number (the n-th read of the book)
- start date
- end date
- total time (can be computed client-side)
- pace (words/day) (can be computes client-side)
- notes

navigation:
- back
- Edit Read Info

endpoints:
- `GET /books/{book_id}/read-records/{record_id}`

### Edit Read Info page

data rendered:
- book title
- book word count
- book edition label
- book edition format
- author name
- read number (the n-th read of the book)
- start date
- end date
- total time (can be computed client-side)
- pace (words/day) (can be computes client-side)
- notes

navigation:
- Edit Read Notes

actions:
- save
- cancel
- delete
- edit start date
- edit end date
- edit edition

> because the page uses the cancel/save pattern, the modifications need to be stored locally and committed all at once to the server.

endpoints:
- `GET /books/{id}/editions`
- `PATCH /books/{id}/read-records`
- `DELETE /books/{book_id}/read-records/{record_id}`

### Edit Read Notes

data rendered:
- read record notes

actions:
- edit notes

navigation:
- back

> the edits to the notes are saved client-side until the edit is complete, so no endpoint is used for updating the notes specifically.

## Tags flow

### Tags-Name page

data rendered (for each tag):
- name
- number of books that use the tag

navigation:
- Tags-Usage
- Tag Info
- Add Tag

endpoints:
- `GET /tags/usage`

> the sorting alphabetically is done client-side.

### Tag-Usage page

data rendered (for each tag):
- name
- number of books that use the tag

navigation:
- Tags-Name
- Tag Info
- Add Tag

endpoints:
- `GET /tag/usage`

> the sorting by number of books that use it is done client-side.

### Add Tag page

data rendered:
- name
- description

actions:
- save
- cancel
- edit name
- edit description

> the data is set to the server when the user choses to save the tag.

endpoints:
- `POST /tag`

## Edit Book flow

### Edit Book

data rendered:
- preferred cover image
- preferred cover label
- number of cover options
- author name
- universe
- series
- volume number
- title
- release date
- word count
- tags list
- opinion
- description

navigation:
- back
- author selection page
- universe selection page
- series selection page
- tags selection page
- opinion page
- blurb page
- covers page
- editions page

actions:
- delete book
- change volume number
- change title
- change release date
- change word count

endpoints:
- `GET /books/{id}/full`

### Covers page

data rendered (for each cover):
- cover image
- cover label
- cover added date
- if the cover is the default one

actions: 
- add cover
- change default cover
- edit cover label (for each cover)
- delete cover (for each cover)

endpoints:
- `GET /books/{id}/covers`
- `GET /covers/{id}`
- `POST /covers`
- `PATCH /books/{id}/preferred-cover`
- `PATCH /covers/{id}`
- `DELETE /covers/{id}`

### Editions page

data rendered (for each edition):
- edition label
- added date
- format
- (for each read for that edition):
  - read number
  - start date
  - end date

actions:
- back
- edit label (for each edition)
- delete edition (for each edition)
- add edition

endpoints:
- `GET /books/{id}/editions/full`
- `PATCH /books/{id}/editions`
- `DELETE /books/{book_id}/editions/{edition_id}`
- `POST /books/{id}/editions`

### Author selection page

data rendered:
- a list of all authors available in the database

actions:
- create a new author
- select an author
- filter the list for an author

endpoints:
- `GET /authors`
- `POST /authors`

### Universe selection page

data rendered:
- a list of all universes of the previously selected author

actions:
- create a new universe
- select an universe
- filter the list for an universe

endpoints:
- `GET /universes?author_id=...`
- `POST /universes`

### Series selection page

data rendered:
- a list of all series related to the previously selected universe

actions:
- create a new series
- select an series
- filter the list for an series

endpoints:
- `GET /series?universe_id=...`
- `GET /series?author_id=...`
- `POST /series`

### Tag selection page

data rendered:
- a list of tags available in the database

actions:
- create a new tag
- select a tag
- filter the list for a tag

endpoints:
- `GET /tags`
- `POST /tag`

### Opinion page

data rendered:
- the opinion text

actions:
- modify the opinion text

endpoints:
- `PATCH /books/{id}/opinion`

### Blurb page

data rendered:
- the blurb text

actions:
- modify the blurb text

endpoints:
- `PATCH /books/{id}/blurb`
