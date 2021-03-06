package no.nav.tiltakspenger.mottak.joark

import io.mockk.coEvery
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import mu.KotlinLogging
import no.nav.tiltakspenger.mottak.soknad.handleSoknad
import no.nav.tiltakspenger.mottak.soknad.soknadList.Soknad
import org.apache.avro.Schema
import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.MockConsumer
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import org.apache.kafka.clients.producer.MockProducer
import org.apache.kafka.common.KafkaException
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.StringSerializer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

private val LOG = KotlinLogging.logger {}

internal class JoarkReplicatorTest {
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

    @Disabled("konsumenten rekker ?? lukke f??r vi f??r sjekket offset. Kan ??ke delay, men kanskje ikke s?? lurt?")
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
        val mockProducer = MockProducer(false, StringSerializer(), StringSerializer())
        val joarkReplicator = JoarkReplicator(mockConsumer, mockProducer)
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
            joarkReplicator.start()
            // ikke optimalt med delay, men prod-koden har forel??pig ingen sideeffekter. Vil ha det n??r
            // SAF-integrasjonen er klar. Da kan vi sjekke for sideeffekten isteden
            delay(1000L)
            assertEquals(offsets.toLong(), mockConsumer.committed(setOf(partition))[partition]?.offset())
            joarkReplicator.stop()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `exception i handteringen lukker konsument og produsent`() {
        val mockConsumer = MockConsumer<String, GenericRecord>(OffsetResetStrategy.EARLIEST).apply {
            setPollException(KafkaException())
        }
        val mockProducer = MockProducer(false, StringSerializer(), StringSerializer())
        try {
            runTest {
                JoarkReplicator(mockConsumer, mockProducer, this).start()
            }
        } catch (e: KafkaException) {
            LOG.debug(e) { "Fanget exception" }
        } finally {
            assertTrue(mockConsumer.closed())
            assertTrue(mockProducer.closed())
        }
    }

    @Test
    @Disabled("Feiler pga manglende delay?")
    fun `s??knad funnet gir en publisert melding`() {
        val topicName = "topic"
        val partition = TopicPartition(topicName, 0)
        val journalpostId = 1L
        val mockConsumer = MockConsumer<String, GenericRecord>(OffsetResetStrategy.EARLIEST).apply {
            assign(listOf(partition))
            updateBeginningOffsets(mapOf(partition to 0L))
        }
        val soknad = Soknad(
            "id", null, null, "ident", false, false,
            false, null, null, null, null, null, null, null, null, emptyList()
        )
        val mockProducer = MockProducer(true, StringSerializer(), StringSerializer())
        val joarkReplicator = JoarkReplicator(mockConsumer, mockProducer)
        mockkStatic("no.nav.tiltakspenger.mottak.soknad.SoknadMediatorKt")
        coEvery { handleSoknad(journalpostId.toString()) } returns soknad
        val record = GenericData.Record(joarkjournalfoeringhendelserAvroSchema).apply {
            put("journalpostId", journalpostId)
            put("temaNytt", "IND")
            put("journalpostStatus", "MOTTATT")
        }
        mockConsumer.addRecord(
            ConsumerRecord(topicName, partition.partition(), 1L, "$journalpostId", record)
        )
        runBlocking {
            joarkReplicator.start()
            delay(500L)
            assertEquals(1, mockProducer.history().size)
            joarkReplicator.stop()
        }
    }

    @Test
    fun `ingen s??knad funnet gir ingen feil`() {
        val topicName = "topic"
        val partition = TopicPartition(topicName, 0)
        val journalpostId = 1L
        val mockConsumer = MockConsumer<String, GenericRecord>(OffsetResetStrategy.EARLIEST).apply {
            assign(listOf(partition))
            updateBeginningOffsets(mapOf(partition to 0L))
        }
        val mockProducer = MockProducer(true, StringSerializer(), StringSerializer())
        val joarkReplicator = JoarkReplicator(mockConsumer, mockProducer)
        mockkStatic("no.nav.tiltakspenger.mottak.soknad.SoknadMediatorKt")
        coEvery { handleSoknad(journalpostId.toString()) } returns null
        val record = GenericData.Record(joarkjournalfoeringhendelserAvroSchema).apply {
            put("journalpostId", journalpostId)
            put("temaNytt", "IND")
            put("journalpostStatus", "MOTTATT")
        }
        mockConsumer.addRecord(
            ConsumerRecord(topicName, partition.partition(), 1L, "$journalpostId", record)
        )
        runBlocking {
            joarkReplicator.start()
            delay(500L)
            assertEquals(0, mockProducer.history().size)
            joarkReplicator.stop()
        }
    }
}
