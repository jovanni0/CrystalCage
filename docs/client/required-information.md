# Required Information

## AddBook

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
- [Select Author](#add-editbookselectauthor)
- [Select Universe](#add-editbookselectuniverse)
- [Select Series](#add-editbookselectseries)
- [Select Tags](#add-editbookselecttags)
- [Opinion](#addbookopinion)
- [Blurb](#addbookblurb)
- [Manage Covers](#addbookmanagecovers)
- [Manage Editions](#editbookmanageeditions)

> because some of the information external to the `Book` table is linked to the book's ID (which does not exist yet), it needs to be stored locally and sent as part of the create request, or dropped if canceled.

information stored locally:
- covers (per item):
  - cover image
  - cover label
  - is default
- editions (per item):
  - edition file
  - edition label
  - edition format

## AddBook/ManageCovers

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

> every modification is saved locally, see explanation on [AddBook](#addbook)

## Add-EditBook/SelectAuthor

data rendered:
- list of all authors available in the database

actions:
- create a new author
- select an author
- filter the list for an author (done locally)

## Add-EditBook/SelectUniverse

data rendered:
- list of all universes of the author

actions:
- create a new universe
- select an universe
- filter the list for an universe (done locally)

## Add-EditBook/SelectSeries

data rendered:
- list of all series of the author, part of the universe if one was selected.

actions:
- create a new series
- select an series
- filter the list for an series (done locally)

## Add-EditBook/SelectTags

data rendered:
- list of tags

actions:
- create a new tag
- select a tag
- filter the list for a tag (done locally)

## AddBook/Opinion

data rendered:
- opinion text

actions:
- modify the opinion

## AddBook/Blurb

data rendered:
- blurb text

actions:
- modify the blurb

## AddBook/ManageEditions

data rendered (for each edition):
- edition label
- added date
- format

actions:
- edit label (for each edition)
- delete edition (for each edition)
- add edition

> every modification is saved locally, see explanation on [AddBook](#addbook)

## Library/Gallery

data rendered (for each cover):
- cover image
- title

actions:
- search

navigation:
- [Add Book](#addbook)
- [Library/byAuthor](#librarybyauthor)
- [Library/bySeries](#librarybyseries)
- book page

> because the `GET /cover/{id}` will not work with `<img src>`, a wrapper should be implemented that creates an object URL and revokes it when unmounting. an `InteractionObserver` needs to be built to achieve the lazy-loading natively handled by `img`, and the HTTP-level cache needs to be implemented by the caching layer of the app.

## Library/byAuthor

data rendered (for each author):
- (for each book):
  - cover image
  - title

actions:
- search

navigation:
- [Add Book](#addbook)
- [Library/Gallery](#librarygallery)
- [Library/bySeries](#librarybyseries)
- author page
- book page

## Library/bySeries

data rendered (for each author):
- (for each universe):
  - series cover
  - series name
  - number of volume in series

actions:
- search

navigation:
- [Add Book](#addbook)
- [Library/Gallery](#librarygallery)
- [Library/byAuthor](#librarybyauthor)
- author page
- series page

## Author/Gallery

data rendered (for each book):
- cover image
- title

navigation:
- [Author/bySeries](#authorbyseries)
- [Add Book](#addbook)
- back
- book page

## Author/bySeries

data rendered (for each series):
- series name
- universe
- (for each book in the series):
  - cover image
  - title

navigation:
- [Add Book](#addbook)
- back
- series page
- book page

## SeriesProfile

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

## Book/Details

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
- [Book/Covers](#bookcovers)
- [Book/Reads](#bookreads)
- [Edit Book](#editbook)
- author page
- series page
- tag info page (for each tag)

## Book/Covers

data rendered (for each cover):
- cover image
- cover label
- added date
- preferred status
- full resolution cover (inspect)

navigation:
- [Book/Details](#bookdetails)
- [Book/Reads](#bookreads)
- [Edit Book](#editbook)
- back

actions:
- set cover as favorite

## Book/Reads

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
- [Book/Details](#bookdetails)
- [Book/Covers](#bookcovers)
- [Read Info](#readinfo) (for each read record)
- back

actions:
- start read
- finish read (if read in progress)

## TagProfile

data rendered:
- name
- description
- number of books that use the tag
- (for each book that uses the tag):
  - cover image (low-res)
  - title
  - series

navigation:
- [Edit Tag](#edittag)
- back
- book page

## EditTag

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

## ReadInfo

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
- [Edit Read](#editread)
- back

## EditRead

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
- [EditRead/Notes](#editreadnotes)

actions:
- save
- cancel
- delete
- edit start date
- edit end date
- edit edition

> because the page uses the cancel/save pattern, the modifications need to be stored locally and committed all at once to the server.

## EditRead/Notes

data rendered:
- read record notes

actions:
- edit notes

navigation:
- back

> modifications are saved locally, see [EditRead](#editread).

## Tags/byName

> ordering in done locally

data rendered (for each tag):
- name
- number of books that use the tag

navigation:
- [Tags/byUsage](#tagsbyusage)
- [TagProfile](#tagprofile)
- [AddTag](#addtag)

## Tags/byUsage

> ordering in done locally

data rendered (for each tag):
- name
- number of books that use the tag

navigation:
- [Tags/byName](#tagsbyname)
- [TagProfile](#tagprofile)
- [AddTag](#addtag)

## AddTag

data rendered:
- name
- description

actions:
- save
- cancel
- edit name
- edit description

> the data is set to the server when the user choses to save the tag.

## EditBook

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
- [Select Author](#add-editbookselectauthor)
- [Select Universe](#add-editbookselectuniverse)
- [Select Series](#add-editbookselectseries)
- [Select Tags](#add-editbookselecttags)
- [Opinion](#addbookopinion)
- [Blurb](#addbookblurb)
- [Manage Covers](#addbookmanagecovers)
- [Manage Editions](#editbookmanageeditions)
- back

actions:
- delete book
- change volume number
- change title
- change release date
- change word count

## EditBook/ManageCovers

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

## EditBook/ManageEditions

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

## EditBook/Opinion

data rendered:
- opinion text

actions:
- modify the opinion

## Blurb page

data rendered:
- blurb text

actions:
- modify the blurb
