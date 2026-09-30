# Validation Rules

## Read records

- a read record cannot have `start_date` set to null while `end_date` is set; you cannot claim to know when a read ended without knowing when it started (unless historical; see below).
- if `is_historical = true`, both `start_date` and `end_date` must be null. Tte UI enforces this by hiding the date pickers when the historical checkbox is active; the server must independently reject any request that violates this, regardless of client behavior.
- `did_not_finish` is independent of `is_historical`; a historical read may be marked DNF (the user remembers giving up on it but not when).
- at most one read record per edition may have `is_historical = true`. a second historical record for the same edition must be rejected.

## Series / Universe / Author consistency

- `Series.author_id` is mandatory. If `Series.universe_id` is also set, `Series.author_id` must equal `Universe.author_id` for that universe — a series cannot claim an author different from its parent universe's author.

## Books, covers, editions

- a `Cover` or `BookEdition` record must be created (and its ID generated) before the corresponding file is written to disk. the filename embeds the record's ID, so no ID means no valid filename.
- if the DB insert for a cover/edition succeeds but the file write fails, the DB insert must be rolled back. DB and disk must never disagree about what exists.
- deleting a `Cover` that is currently a book's preferred cover must reassign the book's preferred cover to another existing cover (eg the most recently added one), or to null if none remain.
- deleting a `BookEdition` cascades to delete all `ReadRecord`s associated with that edition.

## Filenames / filesystem

- filenames are sanitized to `a-z A-Z 0-9 - _ .` before writing; characters outside this set are normalized (closest ASCII) or stripped. reserved OS characters (`< > : " / \ | ? *`) are never permitted regardless of normalization outcome.
- if a composed filename would exceed 255 bytes, components are abbreviated in order: series name, universe name, author first name etc. the record ID and file extension are never abbreviated or dropped.
- the full file path must also be checked against a practical limit (260 characters, for Windows compatibility); if exceeded, the user should be notified to choose a shorter base directory rather than have the write silently fail or corrupt.

## Multi-tenancy

- every authenticated request must resolve to exactly one user schema (`user_{id}`) before any query runs; a request that cannot be resolved to a schema must be rejected before touching any tenant data.