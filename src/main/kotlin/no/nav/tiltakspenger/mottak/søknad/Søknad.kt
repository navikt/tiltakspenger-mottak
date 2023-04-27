package no.nav.tiltakspenger.mottak.søknad

data class Søknad(
    val ident: String,
    val journalpostId: String,
    val dokumentInfoId: String,
    val søknad: String,
    val vedlegg: List<Vedlegg>,
)
