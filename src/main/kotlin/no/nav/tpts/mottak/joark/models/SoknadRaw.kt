package no.nav.tpts.mottak.joark.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SoknadRaw(
    @SerialName("soknadId") val soknadId: Int? = null,
    @SerialName("skjemaNummer") val skjemaNummer: String? = null,
    @SerialName("versjon") val versjon: String? = null,
    @SerialName("uuid") val uuid: String? = null,
    @SerialName("brukerBehandlingId") val brukerBehandlingId: String? = null,
    @SerialName("behandlingskjedeId") val behandlingskjedeId: String? = null,
    @SerialName("fakta") val fakta: List<Fakta> = arrayListOf(),
    @SerialName("status") val status: String? = null,
    @SerialName("aktoerId") val aktoerId: String? = null,
    @SerialName("opprettetDato") val opprettetDato: String? = null,
    @SerialName("sistLagret") val sistLagret: String? = null,
    @SerialName("delstegStatus") val delstegStatus: String? = null,
    @SerialName("vedlegg") val vedlegg: List<String> = arrayListOf(),
    @SerialName("journalforendeEnhet") val journalforendeEnhet: String? = null,
    @SerialName("soknadPrefix") val soknadPrefix: String? = null,
    @SerialName("soknadUrl") val soknadUrl: String? = null,
    @SerialName("fortsettSoknadUrl") val fortsettSoknadUrl: String? = null,
    @SerialName("erEttersending") val erEttersending: Boolean? = null
)
