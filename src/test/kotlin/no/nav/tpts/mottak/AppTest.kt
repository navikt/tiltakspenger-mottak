package no.nav.tpts.mottak

import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.MockConsumer
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import org.apache.kafka.clients.producer.MockProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class AppTest {
    // From https://stash.adeo.no/projects/BOAF/repos/dok-avro/browse/dok-journalfoering-hendelse-v1/src/main/avro/schema/v1/JournalfoeringHendelse.avsc
    /*
    private val joarkjournalfoeringhendelserAvroSchema = Schema.Parser().parse(
        """{ 
              "namespace" : "no.nav.joarkjournalfoeringhendelser",
              "type" : "record",
              "name" : "JournalfoeringHendelseRecord",
              "fields" : [
                {"name": "hendelsesId", "type": "string"},
                {"name": "versjon", "type": "int"},
                {"name": "hendelsesType", "type": "string"},
                {"name": "journalpostId", "type": "long"},
                {"name": "journalpostStatus", "type": "string"},
                {"name": "temaGammelt", "type": "string"},
                {"name": "temaNytt", "type": "string"},
                {"name": "mottaksKanal", "type": "string"},
                {"name": "kanalReferanseId", "type": "string"},
                {"name": "behandlingstema", "type": "string", "default": ""}
              ]
            }
        """.trimIndent()
    )
       */
    @Test
    fun `happy case`() {
        val mockProducer = MockProducer(false, StringSerializer(), StringSerializer())
        val mockConsumer = MockConsumer<String, GenericRecord>(OffsetResetStrategy.EARLIEST)
        mockConsumer.subscribe(listOf("topic"))
        /*
        val record = GenericData.Record(joarkjournalfoeringhendelserAvroSchema).apply {
            put("journalpostId", 1)
        }*/
        // val consumerRecord: ConsumerRecord<String, GenericRecord> = ConsumerRecord("topic", 0, 0, "key", record)
        val producerRecord: ProducerRecord<String, String> = ProducerRecord("topic", "key", "value")
        mockProducer.send(producerRecord)
        // mockConsumer.addRecord(consumerRecord)
        assertNotNull(mockProducer)
    }
}
