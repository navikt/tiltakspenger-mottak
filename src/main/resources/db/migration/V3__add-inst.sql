ALTER TABLE soknad
    ADD COLUMN opphold_institusjon BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN type_institusjon    VARCHAR(255)
