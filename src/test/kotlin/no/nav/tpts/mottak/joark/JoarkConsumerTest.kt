package no.nav.tpts.mottak.joark

import io.mockk.every
import io.mockk.spyk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
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
import org.junit.jupiter.api.Disabled
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
        val partition0 = TopicPartition(topicName, 0)
        val journalpostId = 1L
        val mockConsumer = MockConsumer<String, GenericRecord>(OffsetResetStrategy.EARLIEST).apply {
            assign(listOf(partition0))
            updateBeginningOffsets(mapOf(partition0 to 0L))
        }
        val record = GenericData.Record(joarkjournalfoeringhendelserAvroSchema).apply {
            put("journalpostId", journalpostId)
            put("temaNytt", "IND")
        }
        val consumerRecord: ConsumerRecord<String, GenericRecord> =
            ConsumerRecord(topicName, partition0.partition(), 0, "$journalpostId", record)
        mockConsumer.addRecord(consumerRecord)
        runBlocking {
            val joarkConsumer = JoarkConsumer(mockConsumer)
            joarkConsumer.start()
            // ikke optimalt med delay, men prod-koden har foreløpig ingen sideeffekter. Vil ha det når
            // SAF-integrasjonen er klar. Da kan vi sjekke for sideeffekten isteden
            delay(100L)
            assertEquals(1, mockConsumer.committed(setOf(partition0))[partition0]?.offset())
            joarkConsumer.stop()
        }
        assertTrue(mockConsumer.closed())
    }

    @Disabled
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `exception i handteringen lukker konsumenten`() = runTest {
        val mockConsumer = MockConsumer<String, GenericRecord>(OffsetResetStrategy.EARLIEST).apply {
            setPollException(KafkaException())
        }
        val joarkConsumer = spyk(JoarkConsumer(mockConsumer))
        every { joarkConsumer.coroutineContext } returns this.coroutineContext
        try {
            withTimeout(10L) {
                launch {
                    joarkConsumer.start()
                }
            }
        } catch (ef: Exception) {
            LOG.error(ef) { "fanget exception" }
        }
        assertTrue(mockConsumer.closed())
    }
}
