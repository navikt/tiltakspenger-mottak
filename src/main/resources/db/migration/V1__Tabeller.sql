CREATE TABLE IF NOT EXISTS person
(
    id         BIGSERIAL PRIMARY KEY,
    ident      VARCHAR(11),
    fornavn    VARCHAR(255),
    etternavn  VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS soknad
(
    ident             VARCHAR(11) REFERENCES PERSON (ident),
    journalpost_id    BIGINT                   NOT NULL,
    dokumentinfo_id   BIGINT                   NOT NULL,
    data              JSONB                    NOT NULL,
    opprettet_dato    TIMESTAMP WITH TIME ZONE NOT NULL,
    bruker_start_dato DATE,
    bruker_slutt_dato DATE,
    system_start_dato DATE,
    system_slutt_dato DATE,
    created_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    PRIMARY KEY (journalpost_id, dokumentinfo_id)
);
