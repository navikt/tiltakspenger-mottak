ALTER TABLE soknad
    DROP COLUMN navn;

ALTER TABLE person
    ADD COLUMN navn VARCHAR(255);