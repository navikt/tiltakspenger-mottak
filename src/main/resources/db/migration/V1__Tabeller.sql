CREATE TABLE IF NOT EXISTS person
(
    id         BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS soknad
(
    --soker             BIGINT REFERENCES PERSON,
    ident             TEXT NOT NULL,
    journalpost_id    BIGINT                   NOT NULL,
    dokumentinfo_id   BIGINT                   NOT NULL,
    data              JSONB                    NOT NULL,
    -- navn              VARCHAR(255),
    opprettet_dato    TIMESTAMP WITH TIME ZONE NOT NULL,
    bruker_start_dato DATE,
    bruker_slutt_dato DATE,
    system_start_dato DATE,
    system_slutt_dato DATE,
    created_at        TIMESTAMP WITH TIME ZONE NOT NULL,
    PRIMARY KEY (journalpost_id, dokumentinfo_id)
);

CREATE TABLE IF NOT EXISTS ident
(
    person_id  BIGSERIAL REFERENCES person,
    ident      VARCHAR(11),
    ident_type VARCHAR,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
)