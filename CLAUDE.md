# CrystalCage – context for Claude

Personal book and read management app: Kotlin + PostgreSQL server (schema-per-user), SvelteKit + Capacitor client (Android, SPA, `ssr = false`).

Read first:
- `docs/scope.md` – goals, architecture, layers.
- `docs/coding-styles.md` – naming, spacing, formatting and git conventions. Follow them in every code suggestion (braces on new lines, no one-line `if` returns, explicit names, snake_case variables).
- `docs/client/required-information.md` – data and actions per screen.
- `docs/shared/endpoints.md`, `docs/server/*` – API and database.

Working agreement: give code in the chat; do not edit code files unless asked. This file is Claude's to keep current.



## Current state

- v1 client is done but is being redesigned. v2 lives inside the same client project; v1 will mostly be rewritten, so only review/refactor v2 code unless asked.
- v2 routes live under `client/src/routes/(v2)/` (`(app-proper)` for the app, `(onboarding)` for server discovery/login/signup, `dev/` for component previews).
- Add/manage book: `routes/(v2)/(app-proper)/add-book/[[id]]`. It saves books against the real server. Covers and editions are not added during creation; they get separate pages from the book profile.
- Temporary, unstyled cover and edition managers: `routes/(v1)/manage-covers/[id]` and `manage-editions/[id]` (stores in `v2/lib/stores/*-manager.store.svelte.ts`, picked files queue as drafts until uploaded).
- Library: `routes/(v2)/(app-proper)/(primary-pages)/(library)` (`LibStore` in `v2/lib/stores/library-store.svelte.ts`, grid of `components/covers/cover-wrapper.svelte`). Dev preview of cover states: `routes/(v2)/dev/cover-image`.
- Book profile redesign in progress: `routes/(v2)/(app-proper)/book-profile/[id]` with tabs Covers / Book (`(details)` group) / Reads / Editions. See "Book profile" below.
- Current phase: using the app with the real collection (adding books, covers, reads, historical reads) to collect UX issues; book profile is the main focus, then one polish pass across library + book profile, starting with shared components.
- Git: trunk-based, short branches off `main` (`v1.0.0` tag planned at the last pure-v1 commit).



## Client v2 architecture (`client/src/v2/lib`, imported as `@v2/lib/…`)

| folder | holds | network | reactive |
|---|---|---|---|
| `rules/` | pure static logic (`AuthorRules`, `ReadRecordRules`, `TabRules`, `ZoomRules`, …) | no | no |
| `clients/` | `HttpClient` (explicit base url, pre-login), `SessionHttp` (base url + bearer from `Session`, signs out on 401), one API class per resource (`ApiAuth`, …) | yes | no |
| `repositories/` | app-wide cached server data, created once in `CompositionRoot` | via clients | yes |
| `states/` | app-wide client state (`Session`, format colour registry) | no | yes |
| `stores/` | per-page/flow state (`ManageBookStore`, `LibStore`, `BookProfileStore`), created by a layout `load` | via repositories | yes |
| `caches/` | in-memory binary caches (`CoverImageCache`) | via clients | no |
| `storage/` | device persistence adapters (`SessionStorage`, `ReadPageCountStorage`) | no | no |
| `components/` | presentational building blocks | no | local only |
| `elements/` | composed UI pieces with behaviour (sheets, carousels, cover viewer) | via props | local only |

Decisions:
- Repositories never throw: they return `Result<T>` / `AsyncResult<T>` (`utils/result.ts`, `attempt()`, `toAppError()` with kinds `invalid | conflict | not_found | unauthorized | network | server | unknown`). Callers map kinds to messages.
- Full-list data (authors, universes, series, books, tags + tag categories) is loaded whole and filtered client-side with `$derived`; the server still validates everything. Per-book data (editions, covers, read records) and per-read data (read progress) use a keyed cache (`ListCacheGrouped`, `SvelteMap` by key). Shared bases: `ListCache<T>` / `ListCacheGrouped<T>` with load time, generation counter (discard results after `reset`/`forget`), shared in-flight requests, `ensure/load/invalidate/reset`.
- `BookRepository.details()` is not cached; whoever needs the full book holds its own copy and reloads it after its own writes.
- Repositories update their own lists after writes (always assign new arrays; `SvelteMap` values aren't deeply reactive). Related data is refreshed via explicit `invalidate()` calls after writes.
- Pages call `load()`/`ensure()` for what they need in their layout `load`; no fetching from setters.
- Stores are created through an async static factory (`ManageBookStore.create(...)`), never by calling async work in a constructor. Data the store cannot exist without is fetched in the factory and passed to the private constructor, so it is non-nullable inside the store; fields derived from constructor-assigned state use `$derived.by(() => …)` (plain `$derived(this.x)` trips TypeScript's "used before initialization"). Dependencies are passed as one named object (`…StoreDependencies`).
- Sheets keep a draft value derived from `is_open` + the bound value (writable `$derived`), committed only on confirm.
- Form text fields are `string` (empty = missing) and converted to `null` when building requests; ids and numbers stay `null`-able.
- Session: `Session` holds base url, server name, token, user, derived `status` (`UNKNOWN | NO_SERVER | NEEDS_LOGIN | AUTHENTICATED`), persists via `SessionStorage`, `onSignOut` resets repositories. Root `+layout.ts` restores + verifies (`me()` with timeout; network errors keep the token) and guards routes; `+layout.svelte` `$effect` redirects when the session ends mid-page.
- Navigation goes through `Nav` (`utils/nav.ts`), which tracks `depth` in `afterNavigate` and handles Android back (the root layout registers a `backButton` listener, so Capacitor's default WebView back is off). Replace-navigations (eg tab switches) must use `nav.goTo(href, true)`; a link with `data-sveltekit-replacestate` would be counted as a push. Overlays (cover viewer, later sheets) open with `nav.openOverlay(state)` (shallow `pushState`, typed in `App.PageState`) and close with `nav.closeOverlay()`; Android back closes an open overlay before anything else. In `nav.ts` Capacitor's `App` is imported as `CapacitorApp` so it does not shadow SvelteKit's `App` namespace.
- Tab bars derive the active tab with `TabRules` (longest path-prefix match, nothing active when no tab matches); the book profile top bar picks its action from the same result.
- Markdown (blurb, thoughts) is rendered only through `utils/markdown.ts` (`marked` + DOMPurify allow-list); stores expose the rendered HTML (`null` when empty) and pages use `{@html}` only on that output. The server calls thoughts `notes`.
- Types live next to the code that owns them; DTOs are one file per resource (`tag.dto.ts`).
- Edition format colours: built-ins + user overrides (`Record<id, Partial<…>>`), hue-only in oklch, muted variant derived in CSS.
- Markup uses custom element names instead of `div` on purpose (remember `display: block` where needed; custom elements are inline by default).
- Cover images: `GET /covers/{id}` needs the bearer token, so images are fetched as Blobs (`ApiCovers.getImage`), never via plain `<img src>`. Two `CoverImageCache` instances (LRU of Blobs by cover id with a byte budget, shared in-flight requests, generation counter, `peek/load/forget/reset`): `fullsize_cover_cache` (original, 48 MB) and `thumbnail_cover_cache` (16 MB); reset on sign out, `forget` after a cover is deleted. Cover files never change, so there is no time-based invalidation. Components create and revoke their own object urls, so eviction never breaks a visible image.
- Cover components (`components/covers/`), one job each: `cover-frame` only renders a state (`status: empty | loading | loaded | error` + `src`, placeholder icons, loading shimmer, `fit: cover | contain`, reports the image's natural size via `onImageLoad`); it fills its parent and has no size, border or radius. `cover-image` is the only place that loads covers (cache peek, optional cached-thumbnail preview, fetch when visible via the v1 `VisibilityObserver` unless `is_lazy` is false, object url lifecycle) and renders a `cover-frame` (the `{@attach}` goes through `cover-frame`'s rest-prop spread – attachments on components only work if the component spreads its rest props onto an element). `cover-tile` is the tile decoration (2:3 box, border, radius, shadow, clipping) wrapped around a `cover-image` (or a bare `cover-frame` on the dev page) wherever a cover is shown as a tile; `cover-wrapper` is the library tile (link + tile + title); `cover-viewer` (the viewer slide) wraps an eager, `contain`, thumbnail-first `cover-image` in a layer sized to the fitted image and transformed for zoom. Tile size is set by the parent (`width`); in a flex row the tile needs `flex: 0 0 <width>` or it shrinks.
- Overlapping tap targets (cover tile: open viewer + set main cover) are sibling buttons stacked by DOM order, never a button inside a button.
- Read progress: server keeps an append-only `read_progress` log (one row per saved update, `recorded_at TIMESTAMPTZ`, `progress NUMERIC(5,2)` 0–100, cascade on read delete); the latest row is the read's current progress. Page counts are media/device-specific, so they are stored only on the device (keyed by read record id), never on the server; the client derives pages from the percentage.
- Text truncation to N lines is pure CSS (`line-clamp` + `-webkit-box`), never JS; the full text stays in the DOM. In a tile with a visible title (or a button labelling the cover), the cover `alt` is empty to avoid double announcements.
- Design tokens (colours, `--text-body`, `--text-body-sm`, `--text-caption`, …) are defined in `routes/(v2)/+layout.svelte`; use only those names (eg there is no `--text-body-small`, `--dividers`).



## Book profile (v2)

- Layout `book-profile/[id]/+layout.ts` creates one `BookProfileStore` per book; `+layout.svelte` (top bar + tab bar) belongs in `[id]/` so tab hrefs and the title come from the store. A missing book maps to a 404 error page.
- `BookProfileStore` holds the book details (non-nullable, fetched in `create`, reloaded or patched from the server response after the profile's own writes, eg `setDefaultCover`) and derives the rest from repositories: author/universe/series names (series label `Name #n` with a non-breaking space), rendered blurb/thoughts, read records, current read, read summary. Covers and editions are loaded by the tabs and flows that need them. Multi-step flows (start/log read sheet) get their own store; view-only state (cover viewer index, chrome visibility) stays in components.
- Setting the main cover: `books.update(id, { cover_id })` (all `BookUpdateRequest` fields are optional), then `covers.markDefault` and patch `details.cover_id` – only after the server confirms; guarded against double taps.
- Read status: the in-progress read is shown by a banner; the stats cell shows history via `ReadRecordRules.summaryOf` – finished count if any (historical reads count unless DNF), else DNF, else unread. While a read is in progress the add-read action can only log a completed past read (end date required, must not overlap the current read).
- Top bar right slot = the active tab's main action with contextual icons (covers → manage covers, book → edit book, reads → add read, editions → manage editions); management pages are separate routes titled explicitly.
- Cover viewer, three layers: `elements/cover-viewer-overlay.svelte` owns the covers (current index, visible slides = current + neighbours, natural sizes, bars, close/set-main actions, keyboard); `utils/viewer-gestures.svelte.ts` (`ViewerGestures`, attached with `{@attach gestures.attach}`, sets `touch-action: none`) owns pointer handling and the on-screen state (zoom transform, strip offsets, settle animation) and reports `onPage/onDismiss/onTap`, asking `canPage`; `rules/gesture-rules.ts` holds the thresholds and pure decisions (axis decision after 10 px, double tap, page/dismiss commit by distance or flick velocity, edge resistance) and `rules/zoom-rules.ts` the zoom math. Slides are `components/covers/cover-viewer.svelte`. Gestures: one finger at 1× = page (sideways) / dismiss (down) / tap; zoomed one finger pans; two fingers pinch; double tap zooms; single tap (delayed for double-tap detection) toggles the bars. Bars overlay the image and respect safe areas; label wraps (max 4 lines) in the top bar, counter + added/size in the bottom bar.



## Open items

- Server: authors, universes and series only have `POST` (no `PATCH`/`DELETE`); tags have both. Delete semantics for entities still referenced by books are undefined (block with 409 vs reassign/merge).
- Server: add cover thumbnails (eg `GET /covers/{id}?size=thumbnail`) before the full-collection stress test; full-size covers in the library grid are heavy to decode on Android.
- Server: `GET /covers/{id}` finds the file with `AssetStorage.findByEmbeddedId`, which lists the whole covers directory on every request (O(n) per image), reads it fully into memory (`readAllBytes`) and sends no `Cache-Control`. Asset filenames are human-readable (`FilenameBuilder`), so derived files (thumbnails) should live outside the `covers/` folder, keyed by id only.
- Server: covers uploaded before the `sanctum_fs` volume was added are gone from disk but still in the DB (`exists in DB but no matching file found on disk`); delete or re-upload them.
- Move remaining repositories from the old `ApiClient` to per-resource API classes on `HttpClient`/`SessionHttp`, then delete `ApiClient` and the old `APIClient`/`ApiClientV2` interfaces.
- Server: return 409 on signup when the username is taken; JWT expiry was ~23h (typo for 24h) – planned ~90 days plus a refresh endpoint.
- Server must validate that a book's series/universe belong to its author.
- Mock API: add latency and a failure switch; use UUIDs.
- Tag creation still needs a category choice (currently `null` → "uncategorized").
- `ReadRecordRepository` passes the label "read progress" to its cache base (should be "read records"; affects log messages only).
