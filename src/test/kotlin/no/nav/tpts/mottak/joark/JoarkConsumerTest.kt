package no.nav.tpts.mottak.joark

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import mu.KotlinLogging
import org.apache.avro.Schema
import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.MockConsumer
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import org.apache.kafka.common.KafkaException
import org.apache.kafka.common.TopicPartition
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

private val LOG = KotlinLogging.logger {}

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
    fun `konsumere fra en topic endrer offset`() {
        val topicName = "topic"
        val partition = TopicPartition(topicName, 0)
        val journalpostId = 1L
        val offsets = 3
        val mockConsumer = MockConsumer<String, GenericRecord>(OffsetResetStrategy.EARLIEST).apply {
            assign(listOf(partition))
            updateBeginningOffsets(mapOf(partition to 0L))
        }
        val joarkConsumer = JoarkConsumer(mockConsumer)
        val record = GenericData.Record(joarkjournalfoeringhendelserAvroSchema).apply {
            put("journalpostId", journalpostId)
            put("temaNytt", "IND")
        }
        repeat(offsets) {
            mockConsumer.addRecord(
                ConsumerRecord(topicName, partition.partition(), it.toLong(), "$journalpostId", record)
            )
        }
        runBlocking {
            joarkConsumer.start()
            // ikke optimalt med delay, men prod-koden har foreløpig ingen sideeffekter. Vil ha det når
            // SAF-integrasjonen er klar. Da kan vi sjekke for sideeffekten isteden
            delay(500L)
            assertEquals(offsets.toLong(), mockConsumer.committed(setOf(partition))[partition]?.offset())
            joarkConsumer.stop()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `exception i handteringen lukker konsumenten`() {
        val mockConsumer = MockConsumer<String, GenericRecord>(OffsetResetStrategy.EARLIEST).apply {
            setPollException(KafkaException())
        }
        try {
            runTest {
                JoarkConsumer(mockConsumer, this).start()
            }
        } catch (e: KafkaException) {
            LOG.debug(e) { "Fanget exception" }
        } finally {
            assertTrue(mockConsumer.closed())
        }
    }
}
