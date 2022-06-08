CREATE TABLE IF NOT EXISTS barnetillegg
(
    journalpost_id  BIGINT,
    dokumentinfo_id BIGINT,
    ident           VARCHAR(11)              NOT NULL,
    fornavn         VARCHAR(255),
    etternavn       VARCHAR(255),
    alder           INT,
    bosted          VARCHAR(255),
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    FOREIGN KEY (journalpost_id, dokumentinfo_id) REFERENCES soknad (journalpost_id, dokumentinfo_id),
    PRIMARY KEY (journalpost_id, dokumentinfo_id, ident)
);
