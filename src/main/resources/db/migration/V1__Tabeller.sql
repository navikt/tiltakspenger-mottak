CREATE TABLE IF NOT EXISTS person
(
    id        BIGSERIAL PRIMARY KEY,
    createdAt TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS soknad
(
    soker           BIGINT REFERENCES PERSON,
    journalpostId   BIGINT                   NOT NULL,
    dokumentinfoId  BIGINT                   NOT NULL,
    data            JSONB                    NOT NULL,
    navn            VARCHAR(255),
    opprettetDato   TIMESTAMP WITH TIME ZONE NOT NULL,
    brukerStartDato DATE,
    brukerSluttDato DATE,
    systemStartDato DATE,
    systemSluttDato DATE,
    createdAt       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (NOW() AT TIME ZONE 'utc'),
    PRIMARY KEY (journalpostId, dokumentinfoId)
);

CREATE TABLE IF NOT EXISTS ident
(
    personId  BIGSERIAL REFERENCES person,
    ident     VARCHAR(11),
    identType VARCHAR,
    createdAt TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (NOW() AT TIME ZONE 'utc')
)