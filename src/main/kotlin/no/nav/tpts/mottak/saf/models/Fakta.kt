package no.nav.tpts.mottak.saf.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Fakta(
    @SerialName("faktumId"         ) val faktumId         : Int?                   = null,
    @SerialName("soknadId"         ) val soknadId         : Int?                   = null,
    @SerialName("parrentFaktum"    ) val parrentFaktum    : Int?                   =  null,
    @SerialName("key"              ) val key              : String?                = null,
    @SerialName("value"            ) val value            : String?                = null,
    @SerialName("faktumEgenskaper" ) val faktumEgenskaper : List<FaktumEgenskaper> = arrayListOf(),
    @SerialName("properties"       ) val properties       : Map<String, String?>?  = emptyMap(),
    @SerialName("type"             ) val type             : String?                = null
)
