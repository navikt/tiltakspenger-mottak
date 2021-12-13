package no.nav.tpts.mottak.models

import no.nav.tpts.mottak.saf.models.SoknadRaw

data class SoknadField(
    val key: String?,
    val value: String?,
    val type: String?,
    val properties: Map<String, String?>?
)

data class Soknad(
    val aktorId: String,
    val fields: List<SoknadField>
)

fun SoknadRaw.toSoknad() {
    this.fakta.map { fakta -> SoknadField(
        key = fakta.key,
        value = fakta.value,
        type = fakta.type,
        properties = fakta.properties
    ) }
}
