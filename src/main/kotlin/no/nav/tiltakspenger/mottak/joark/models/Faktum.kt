package no.nav.tiltakspenger.mottak.joark.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Faktum(
    @SerialName("faktumId") val faktumId: Int,
    @SerialName("soknadId") val soknadId: Int,
    @SerialName("parrentFaktum") val parrentFaktum: Int? = null,
    @SerialName("key") val key: String,
    @SerialName("value") val value: String? = null,
    @SerialName("faktumEgenskaper") val faktumEgenskaper: List<FaktumEgenskaper> = arrayListOf(),
    @SerialName("properties") val properties: Properties = Properties(),
    @SerialName("type") val type: String
)
