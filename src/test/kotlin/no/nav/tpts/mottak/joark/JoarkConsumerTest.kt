package no.nav.tpts.mottak.joark

import org.apache.avro.Schema
import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.MockConsumer
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import org.apache.kafka.common.TopicPartition
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

internal class JoarkConsumerTest {

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
    @Disabled
    fun `konsumere fra en topic`() {
//        val log = spyk(LOG)
//        every { log.info { "starting JoarkConsumer" } } answers {callOriginal()}
        val topicName = "topic"
        val journalfoeringPartition = TopicPartition(topicName, 0)
        val partition = 0
        val journalpostId = 1L
        val mockConsumer = MockConsumer<String, GenericRecord>(OffsetResetStrategy.EARLIEST).also {
            val topicPartition = TopicPartition(topicName, partition)
            it.assign(listOf(topicPartition))
            it.updateBeginningOffsets(mapOf(topicPartition to 0L))
        }
        val joarkConsumer = JoarkConsumer(mockConsumer)
        joarkConsumer.start()

        val record = GenericData.Record(joarkjournalfoeringhendelserAvroSchema).apply {
            put("journalpostId", journalpostId)
            put("temaNytt", "IND")
        }
        val consumerRecord: ConsumerRecord<String, GenericRecord> =
            ConsumerRecord(topicName, partition, 0, "$journalpostId", record)
        mockConsumer.addRecord(consumerRecord)
        //val poll = mockConsumer.poll(Duration.ofMillis(1000))
        //verify { log.isInfoEnabled }
//        verify { log.info {"starting JoarkConsumer"} }
//        assertNotNull(log.entry())
//        assertEquals(1L, poll.records(topicName).first().value().get("journalpostId"))
//        assertNull(poll.records(topicName).first().value().get("behandlingstema"))
        joarkConsumer.stop()
        assertEquals(3, mockConsumer.committed(setOf(journalfoeringPartition))[journalfoeringPartition]?.offset())
        Thread.sleep(5L)
        assertTrue(mockConsumer.closed())
    }
}
