package no.nav.tpts.mottak.saf.api

import kotlinx.serialization.Serializable

fun getDocumentsQuery(journalpostId: String) =
    """
    query {
      journalpost(journalpostId: "$journalpostId") {
        journalpostId
        journalposttype
        journalstatus
        tema
        temanavn
        behandlingstema
        behandlingstemanavn
        #sak
        #bruker
        #avsenderMottaker
        journalfoerendeEnhet
        journalfortAvNavn
        opprettetAvNavn
        kanal
        kanalnavn
        skjerming
        datoOpprettet
        #relevanteDatoer
        antallRetur
        eksternReferanseId
        #tilleggsopplysninger
        dokumenter {
          dokumentInfoId
          tittel
          brevkode
          dokumentstatus
          datoFerdigstilt
          originalJournalpostId
          skjerming
          logiskeVedlegg {
              logiskVedleggId
              tittel
          }
          dokumentvarianter {
              variantformat
              filnavn
              filtype
              filuuid
              saksbehandlerHarTilgang
              skjerming
          }
        }
      }
    }
""".trimIndent()


@Serializable
data class GraphqlQuery(
    val query: String
)