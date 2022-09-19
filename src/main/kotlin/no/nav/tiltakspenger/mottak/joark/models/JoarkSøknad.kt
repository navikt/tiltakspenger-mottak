package no.nav.tiltakspenger.mottak.joark.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import no.nav.tiltakspenger.mottak.databind.LocalDateTimeSerializer
import java.time.LocalDateTime

@Serializable
data class JoarkSøknad(
    @SerialName("soknadId") val soknadId: Int,
    @SerialName("skjemaNummer") val skjemaNummer: String,
    @SerialName("versjon") val versjon: String? = null,
    @SerialName("uuid") val uuid: String,
    @SerialName("brukerBehandlingId") val brukerBehandlingId: String? = null,
    @SerialName("behandlingskjedeId") val behandlingskjedeId: String? = null,
    @SerialName("fakta") val fakta: List<Faktum> = arrayListOf(),
    @SerialName("status") val status: String,
    @SerialName("aktoerId") val aktoerId: String,
    @SerialName("opprettetDato") @Serializable(with = LocalDateTimeSerializer::class) val opprettetDato: LocalDateTime,
    @SerialName("sistLagret") @Serializable(with = LocalDateTimeSerializer::class) val sistLagret: LocalDateTime,
    @SerialName("delstegStatus") val delstegStatus: String,
    // @SerialName("vedlegg") val vedlegg: List<Vedlegg> = emptyList(),
    @SerialName("journalforendeEnhet") val journalforendeEnhet: String? = null,
    @SerialName("soknadPrefix") val soknadPrefix: String,
    @SerialName("soknadUrl") val soknadUrl: String,
    @SerialName("fortsettSoknadUrl") val fortsettSoknadUrl: String,
    @SerialName("erEttersending") val erEttersending: Boolean,
)
