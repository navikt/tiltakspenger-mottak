package no.nav.tpts.mottak.joark.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Fakta(
    @SerialName("faktumId") val faktumId: Int? = null,
    @SerialName("soknadId") val soknadId: Int? = null,
    @SerialName("key") val key: String? = null,
    @SerialName("value") val value: String? = null,
    @SerialName("properties") val properties: Properties? = Properties(),
    @SerialName("type") val type: String? = null
)
