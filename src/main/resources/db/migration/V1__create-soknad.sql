create table soknad (
    journalpost_id int primary key,
    aktoer_id int,
    navn varchar,
    fnr varchar,
    tiltak_start date,
    tiltak_slutt date,
    registrert date,
    dokument_info_id int,
    created_at timestamp,
    updated_at timestamp
)
