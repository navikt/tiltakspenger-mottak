ALTER TABLE IF EXISTS soknad
    DROP CONSTRAINT soknad_soker_fkey;

ALTER TABLE IF EXISTS soknad
    RENAME COLUMN soker TO ident;

ALTER TABLE IF EXISTS soknad
    ALTER COLUMN ident TYPE VARCHAR(11);

ALTER TABLE IF EXISTS soknaddata
    ADD CONSTRAINT soknad_person_fk FOREIGN KEY (ident) REFERENCES person (ident);
