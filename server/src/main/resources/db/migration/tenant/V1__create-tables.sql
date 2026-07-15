CREATE TABLE author(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name  VARCHAR(255),
    last_name   VARCHAR(255) NOT NULL
);


CREATE TABLE universe(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    author_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,

    CONSTRAINT universe_author_id_fk FOREIGN KEY (author_id) REFERENCES author(id) ON DELETE CASCADE
);


CREATE TABLE series(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    author_id UUID NOT NULL,
    universe_id UUID,
    name VARCHAR(255) NOT NULL,
    is_finished BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT series_author_id_fk FOREIGN KEY (author_id) REFERENCES  author(id) ON DELETE CASCADE,
    CONSTRAINT series_universe_id_fk FOREIGN KEY (universe_id) REFERENCES universe(id) ON DELETE CASCADE
);


CREATE TABLE book(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    author_id UUID NOT NULL,
    universe_id UUID,
    series_id UUID,
    cover_id UUID,
    volume_number NUMERIC(3, 1),
    title VARCHAR(255) NOT NULL,
    launch_date DATE,
    word_count INTEGER,
    blurb TEXT,
    notes TEXT,

    CONSTRAINT book_author_id_fk FOREIGN KEY (author_id) REFERENCES author(id) ON DELETE CASCADE,
    CONSTRAINT book_universe_id_fk FOREIGN KEY (universe_id) REFERENCES universe(id) ON DELETE SET NULL,
    CONSTRAINT book_series_id_fk FOREIGN KEY (series_id) REFERENCES series(id) ON DELETE SET NULL
);


CREATE TABLE book_edition(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    book_id UUID NOT NULL,
    label VARCHAR(255),
    release_date DATE,
    added_at DATE NOT NULL,
    format VARCHAR(255) NOT NULL,

    CONSTRAINT edition_book_id_fk FOREIGN KEY (book_id) REFERENCES book(id) ON DELETE CASCADE
);


CREATE TABLE read_record(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    book_id UUID NOT NULL,
    edition_id UUID,
    start_date DATE,
    end_date DATE,
    did_not_finish BOOLEAN NOT NULL DEFAULT false,
    is_historical BOOLEAN NOT NULL DEFAULT false,
    stop_position VARCHAR(255),
    notes TEXT,

    CONSTRAINT record_book_id_fk FOREIGN KEY (book_id) REFERENCES book(id) ON DELETE CASCADE,
    CONSTRAINT record_edition_id_fk FOREIGN KEY (edition_id) REFERENCES book_edition(id) ON DELETE CASCADE
);


CREATE TABLE tag(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT
);


CREATE TABLE book_tag(
    book_id UUID NOT NULL,
    tag_id UUID NOT NULL,

    CONSTRAINT book_tab_pk PRIMARY KEY (book_id, tag_id),
    CONSTRAINT book_id_fk FOREIGN KEY (book_id) REFERENCES book(id) ON DELETE CASCADE,
    CONSTRAINT tag_id_fk FOREIGN KEY (tag_id) REFERENCES tag(id) ON DELETE CASCADE
);


CREATE TABLE cover(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    book_id UUID NOT NULL,
    label VARCHAR(255),
    added_at DATE NOT NULL,

    CONSTRAINT cover_book_id_fk FOREIGN KEY (book_id) REFERENCES book(id) ON DELETE CASCADE
)


-- because of circular dependency, the book-cover link has to be done separately.
ALTER TABLE book
    ADD CONSTRAINT book_cover_id_fk FOREIGN KEY (cover_id) REFERENCES cover(id);


-- enforce read record validation rules as per `docs/validation-rules##read-records`.
ALTER TABLE read_record
    ADD CONSTRAINT read_record_logic CHECK (
        (is_historical AND start_date IS NULL AND end_date IS NULL)
        OR (NOT is_historical AND NOT (start_date IS NULL AND end_date IS NOT NULL))
    );
