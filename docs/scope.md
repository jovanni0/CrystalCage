# Scope

CrystalCage is a project aimed at creating a management software for your digital books. It handles the storage of digital formats - relevant because big tech will remove access to them when try feel like -, metadata storage and search and read history. You can think about lit like a combo between Goodreads and your physical bookshelf, that you can organize and customize at will without ever losing access.

## System architecture

Actors:
- mobile app
- desktop app
- web app
- server

Inputs:
- book profile (metadata)
- book file (any digital format)
- server query responses

Outputs:
- information about books in library
- book file (when you download a format)

## App requirements

Should have:
- Add/Remove a book, with relevant metadata:
  - one or more covers;
  - book identity (author name, universe, series, number, title);
  - details (release date, word count);
  - tags;
  - user opinion;
  - description (blurb);
  - editions (one or more digital formats).
- Add/Remove covers for each book.
- Set the cover to be displayed from the list associated with a book.
- Search by book name, author, series, universe.
- Display the books as gallery (all of them, no filter), filtered by series / by author.
- Add/Remove reads from a book version. Each read tracks start date, end date, notes.

Nice to have:
- Cover image cache for each book, to limit the information requested from the server on each load.
- Search by tag.
- A single codebase for mobile, desktop and browser.

Future ideas:
- Offline support. The app caches the latest information from the server and uses it if not able to connect to the server.
- Integrate reader and editor in app.
- Live sync of read/write progress with server.
- Auto pull book info from outside sources (Goodreads, Google Books).
- Import library from Goodreads/StoryGraph etc.

## Components

Server:
- binary asset storage = stores book files and covers on disk, at a specified location. owns filename/path scheme and sanitization. does not know about the author/series/title, uses the UUID for identifying the resources.
- metadata database = stores author, universe, series, book metadata, book version, read record, tag, cover metadata. the source of truth about the books, covers and versions. uses schema-per-user for logical separation. does not know about the location of files on disk.
- read status engine = derives a book's read state based on the read records in the database. knows only about the read records, nothing else.
- auth/user management = authenticates requests, maps user to schema.
- API = assembles responses from the above components for clients.

Client:
- API = sends requests to the server, receives responses. is separate from the UI and does not care about it.
- sync/offline controller = decides when to use the local cache and when to send a request to the server. handles cache updates on server requests or metadata change. does not know about the UI. (v1 stub, later implementation)
- local cache store = caches the last metadata and cover images on device. does not know if it's fresh or stale, just handles fetch/update requests.
- client-side state = keeps track of the states that are not sent to the server: book currently open, the state of an opened form, the current view of a page. doesn't know about the server.
- UI = the pages. read from client-side state and local cache via the sync/offline controller. does not know about the server.

## Tech Stack

Server:
- Kotlin = has modern language features, while retaining the compatibility with Java. runs on all the system that Java does.
- PostgreSQL = has support for schema-per-tenant.

Client:
- SvelteKit = easy to use Javascript framework, offers good performance.
- Capacitor = wrapper around a web app, gives it access to native Android features. allows a single codebase to be developed for web and mobile.

## Risks

- schema-per-tenant in PostgreSQL = while it is supported, if it does not work well enough, the change to a classic schema will require going trough all the queries and redesigning them.
- epub rendering in WebView = proper integration of renderer with Capacitor might lead to problems (fonts, pagination DRM)
- UUID storage schema = having both dynamic information and UUID in the filenames will lead to sync problems if a periodic reconciliation job is not properly implemented and run at a regular interval.

## Done for V1

in V1 the goal is to achieve a system that allows the user to upload and download ebooks from the server, organize them using the described metadata, manage covers for each of them and sort/search. the app does not need to have offline/deep cache support. it works well enough if I can use it for 2-3 weeks for managing my book collection without having major problems within the scope of this release.

things deferred to a later version:
- offline capability
- proper data caching
- UI analysis and redesign
- reader/editor integration directly in the app (FoliateJS and CodeMirror).
