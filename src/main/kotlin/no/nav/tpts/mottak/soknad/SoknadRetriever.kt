package no.nav.tpts.mottak.soknad

import mu.KotlinLogging

val LOG = KotlinLogging.logger {}

suspend fun retrieveSoknad(journalPostId: String) {
    LOG.debug { "Retrieving søknad with journalPostId $journalPostId" }
}
