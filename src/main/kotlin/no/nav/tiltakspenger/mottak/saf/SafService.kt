package no.nav.tiltakspenger.mottak.saf

import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import mu.KotlinLogging
import no.nav.tiltakspenger.mottak.saf.SafClient.Companion.FILNAVN_NY_SØKNAD
import no.nav.tiltakspenger.mottak.saf.SafClient.Companion.FILNAVN_SØKNAD
import no.nav.tiltakspenger.mottak.søknad.DokumentInfo
import no.nav.tiltakspenger.mottak.søknad.Søknad
import no.nav.tiltakspenger.mottak.søknad.Søknadv1

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

class SafService(private val safClient: SafClient) {
    suspend fun hentSøknad(journalpostId: String): Søknad? {
        LOG.info { "Henter metadata for journalpost med journalpostId $journalpostId" }
        val metadata = safClient.hentMetadataForJournalpost(journalpostId)
        if (metadata == null) {
            LOG.info { "Journalpost med id $journalpostId ble ikke håndtert" }
            return null
        }
        LOG.info { "Henter søknad med dokumentInfoId ${metadata.dokumentInfoId}" }
        val json = safClient.hentSoknad(metadata)
        LOG.info { "Hentet søknad med dokumentInfoId ${metadata.dokumentInfoId}, se secure-log for detaljer" }
        SECURELOG.info { "Hentet søknad $json" }

        if (metadata.filnavn == FILNAVN_NY_SØKNAD) {
            val mappedJson = jacksonObjectMapper().readTree(json) as ObjectNode
            return Søknad(
                ident = mappedJson.path("personopplysninger").path("ident").asText(),
                hoveddokument = DokumentInfo(
                    journalpostId = journalpostId,
                    dokumentInfoId = metadata.dokumentInfoId,
                    filnavn = metadata.filnavn,
                ),
                søknad = json,
                versjon = mappedJson.path("versjon").asText(),
                vedlegg = metadata.vedlegg.map {
                    DokumentInfo(
                        journalpostId = it.journalpostId,
                        dokumentInfoId = it.dokumentInfoId,
                        filnavn = it.filnavn,
                    )
                },
            )
        }

        if (metadata.filnavn == FILNAVN_SØKNAD) {
            return Søknadv1.toSøknad(
                json = json,
                journalpostId = journalpostId,
                dokumentInfoId = metadata.dokumentInfoId,
                filnavn = metadata.filnavn,
                vedleggMetadata = metadata.vedlegg,
            )
        }

        return null
    }
}
