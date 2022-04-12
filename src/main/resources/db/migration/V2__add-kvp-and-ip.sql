ALTER TABLE soknad
    ADD COLUMN deltar_kvp                     boolean not null default false,
    ADD COLUMN deltar_introduksjonsprogrammet boolean not null default false
