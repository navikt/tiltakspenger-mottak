package no.nav.tpts.mottak.joark

import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.Consumer

fun joarkConsumer(): Consumer<String, GenericRecord> {
    throw NotImplementedError()
}

fun subscribeToTopic() {
    throw NotImplementedError()
}
