-- enforce a single historical read record per book, as per `docs/validation-rules##read-records`
CREATE UNIQUE INDEX read_record_one_historical_pr_book
    ON read_record (book_id)
    WHERE is_historical;

-- fix a missed case when a historical read can have an end date but not a start date
ALTER TABLE read_record DROP CONSTRAINT read_record_logic;
ALTER TABLE read_record
    ADD CONSTRAINT read_record_logic CHECK (
        (is_historical AND start_date IS NULL AND end_date IS NULL)
            OR (NOT is_historical AND start_date IS NOT NULL)
        );
