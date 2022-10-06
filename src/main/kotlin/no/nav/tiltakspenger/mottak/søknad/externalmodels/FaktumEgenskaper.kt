package no.nav.tiltakspenger.mottak.søknad.externalmodels

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FaktumEgenskaper(
    @SerialName("faktumId") val faktumId: Int? = null,
    @SerialName("soknadId") val soknadId: Int? = null,
    @SerialName("key") val key: String? = null,
    @SerialName("value") val value: String? = null,
    @SerialName("systemEgenskap") val systemEgenskap: Int? = null
)
