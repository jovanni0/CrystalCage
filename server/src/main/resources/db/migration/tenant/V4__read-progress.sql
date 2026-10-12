CREATE TABLE read_progress(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    read_record_id UUID NOT NULL,
    recorded_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    progress NUMERIC(5, 2) NOT NULL,

    CONSTRAINT read_progress_record_fk FOREIGN KEY (read_record_id) REFERENCES read_record(id) ON DELETE CASCADE,
    CONSTRAINT read_progress_percent_range CHECK (progress >= 0 AND progress <= 100)
);


CREATE INDEX read_progress_record_latest ON read_progress (read_record_id, recorded_at DESC);