# CrystalCage – context for Claude

Personal book and read management app: Kotlin + PostgreSQL server (schema-per-user), SvelteKit + Capacitor client (Android, SPA, `ssr = false`).

Read first:
- `docs/scope.md` – goals, architecture, layers.
- `docs/coding-styles.md` – naming, spacing, formatting and git conventions. Follow them in every code suggestion (braces on new lines, no one-line `if` returns, explicit names, snake_case variables).
- `docs/client/required-information.md` – data and actions per screen.
- `docs/shared/endpoints.md`, `docs/server/*` – API and database.



## Current state

- v1 client is done but is being redesigned. v2 lives inside the same client project; v1 will mostly be rewritten, so only review/refactor v2 code unless asked.
- First v2 screen: add/manage book (`client/src/routes/(v2_redesign)/add-book`). It saves books against the real server. Covers and editions are no longer added during creation; they get separate pages from the book profile.
- Temporary, unstyled cover and edition managers: `routes/(v2_redesign)/manage-covers/[id]` and `manage-editions/[id]` (stores in `v2/lib/stores/*-manager.store.svelte.ts`, picked files queue as drafts until uploaded). The v1 book profile's edit button opens them from the Covers / Reads tabs; the Details tab opens `/add-book/[id]`.
- Next goal: wire the page into the app and import the whole book collection as a stress test.
- Git: trunk-based, short branches off `main` (`v1.0.0` tag planned at the last pure-v1 commit).



## Client v2 architecture (`client/src/v2`, alias `@v2`)

| folder | holds | network | reactive |
|---|---|---|---|
| `rules/` | pure static logic (`AuthorRules`: filter, nameOf, splitName, creatable name, toCreateRequest) | no | no |
| `clients/` | `HttpClient` (explicit base url, pre-login), `SessionHttp` (base url + bearer from `Session`, signs out on 401), one API class per resource (`ApiAuth`, …) | yes | no |
| `repositories/` | app-wide cached server data, created once in `CompositionRoot` | via clients | yes |
| `states/` | app-wide client state (`Session`, format colour registry) | no | yes |
| `stores/` | per-page/flow state (`ManageBookStore`), created by a layout `load` | via repositories | yes |
| `storage/` | device persistence adapters (`SessionStorage`, later a cache store) | no | no |

Decisions:
- Repositories never throw: they return `Result<T>` / `AsyncResult<T>` (`utils/result.ts`, `attempt()`, `toAppError()` with kinds `invalid | conflict | not_found | unauthorized | network | server | unknown`). Callers map kinds to messages.
- Full-list data (authors, universes, series, books, tags + tag categories) is loaded whole and filtered client-side with `$derived`; the server still validates everything. Per-book data (editions, covers, read records) uses a keyed cache (`GroupedListCache`, `SvelteMap` by book id). Shared bases: `ListCache<T>` / `GroupedListCache<T>` with load time, generation counter (discard results after `reset`/`forget`), shared in-flight requests, `ensure/load/invalidate/reset`.
- Repositories update their own lists after writes (always assign new arrays; `SvelteMap` values aren't deeply reactive). Related data is refreshed via explicit `invalidate()` calls after writes.
- Pages call `load()`/`ensure()` for what they need in their layout `load`; no fetching from setters.
- Stores are created through an async static factory (`ManageBookStore.create(...)`), never by calling async work in a constructor.
- Sheets keep a draft value derived from `is_open` + the bound value (writable `$derived`), committed only on confirm.
- Form text fields are `string` (empty = missing) and converted to `null` when building requests; ids and numbers stay `null`-able.
- Session: `Session` holds base url, server name, token, user, derived `status` (`UNKNOWN | NO_SERVER | NEEDS_LOGIN | AUTHENTICATED`), persists via `SessionStorage`, `onSignOut` resets repositories. Root `+layout.ts` restores + verifies (`me()` with timeout; network errors keep the token) and guards routes; `+layout.svelte` `$effect` redirects when the session ends mid-page.
- Markdown (blurb, thoughts) is rendered only through `utils/markdown.ts` (`marked` + DOMPurify allow-list).
- Types live next to the code that owns them; DTOs are one file per resource (`tag.dto.ts`).
- Edition format colours: built-ins + user overrides (`Record<id, Partial<…>>`), hue-only in oklch, muted variant derived in CSS.
- Markup uses custom element names instead of `div` on purpose.



## Open items

- Move remaining repositories from the old `ApiClient` to per-resource API classes on `HttpClient`/`SessionHttp`, then delete `ApiClient` and the old `APIClient`/`ApiClientV2` interfaces.
- Server: return 409 on signup when the username is taken; JWT expiry was ~23h (typo for 24h) – planned ~90 days plus a refresh endpoint.
- Server must validate that a book's series/universe belong to its author.
- Mock API: add latency and a failure switch; use UUIDs.
- Tag creation still needs a category choice (currently `null` → "uncategorized").
