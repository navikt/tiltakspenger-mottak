package no.nav.tpts.mottak

import no.nav.tpts.mottak.joark.JournalfoeringHendelseRecord
import org.apache.avro.Schema
import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.MockConsumer
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import org.apache.kafka.common.TopicPartition
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.Duration

class AppTest {
    // From https://stash.adeo.no/projects/BOAF/repos/dok-avro/browse/dok-journalfoering-hendelse-v1/src/main/avro/schema/v1/JournalfoeringHendelse.avsc
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

    @Test
    fun `happy case`() {
        val TOPIC = "topic"
        val PARTITION = 0
        val journalfoeringHendelseRecord = JournalfoeringHendelseRecord(42L, "MOTTATT", "IND")

        val mockConsumer = MockConsumer<String, GenericRecord>(OffsetResetStrategy.EARLIEST).also {
            val topicPartition = TopicPartition(TOPIC, PARTITION)
            it.assign(listOf(topicPartition))
            it.updateBeginningOffsets(mapOf(topicPartition to 0L))
        }

        val record = GenericData.Record(joarkjournalfoeringhendelserAvroSchema).apply {
            put("journalpostId", 1)
            put("hendelsesId", 1)
            put("hendelsesType", 1)
            put("journalpostId", 1L)
        }
        val consumerRecord: ConsumerRecord<String, GenericRecord> = ConsumerRecord(TOPIC, PARTITION, 0, "key", record)
        mockConsumer.addRecord(consumerRecord)
        val poll = mockConsumer.poll(Duration.ofMillis(10))

        assertEquals("key", poll.records(TOPIC).first().key())
        assertEquals(1L, poll.records(TOPIC).first().value().get("journalpostId"))
        assertNull(poll.records(TOPIC).first().value().get("behandlingstema"))
        assertEquals(42L, journalfoeringHendelseRecord.journalpostId)
    }
}
