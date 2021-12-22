CREATE TABLE IF NOT EXISTS person
(
    id        BIGSERIAL PRIMARY KEY,
    createdAt TIMESTAMP WITH TIME ZONE NOT NULL default (now() at time zone 'utc')
);

CREATE TABLE IF NOT EXISTS soknad
(
    soker          BIGINT REFERENCES PERSON,
    journalpostId  BIGINT                   NOT NULL,
    dokumentinfoId BIGINT                   NOT NULL,
    data           JSONB                    NOT NULL,
    opprettetDato  TIMESTAMP WITH TIME ZONE NOT NULL,
    createdAt      TIMESTAMP WITH TIME ZONE NOT NULL default (now() at time zone 'utc'),
    PRIMARY KEY (journalpostId, dokumentinfoId)
);
