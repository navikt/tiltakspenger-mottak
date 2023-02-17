package no.nav.tiltakspenger.mottak.joark

import mu.KotlinLogging
import no.nav.tiltakspenger.mottak.Configuration
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord

private val LOG = KotlinLogging.logger {}

class JournalpostIdPublisher(
    private val producer: Producer<String, String> = createKafkaProducer(config = Configuration.KafkaConfig()),
    private val topicName: String,
) {
    fun publish(ident: String, journalpostId: String) {
        val metadata = producer.send(ProducerRecord(topicName, journalpostId, ident)).get()
        LOG.info { "Publiserte ident kl ${metadata.timestamp()} til topic ${metadata.topic()} på partition ${metadata.partition()} med offset ${metadata.offset()}" }
    }
}
