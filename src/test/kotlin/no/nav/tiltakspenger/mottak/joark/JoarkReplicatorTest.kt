package no.nav.tiltakspenger.mottak.joark

import io.mockk.coEvery
import io.mockk.mockkStatic
import java.time.LocalDateTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import mu.KotlinLogging
import no.nav.tiltakspenger.mottak.søknad.Søknad
import no.nav.tiltakspenger.mottak.søknad.handleSøknad
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

    @Disabled("konsumenten rekker å lukke før vi får sjekket offset. Kan øke delay, men kanskje ikke så lurt?")
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
            // ikke optimalt med delay, men prod-koden har foreløpig ingen sideeffekter. Vil ha det når
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
    fun `søknad funnet gir en publisert melding`() {
        val topicName = "topic"
        val partition = TopicPartition(topicName, 0)
        val journalpostId = 1L
        val mockConsumer = MockConsumer<String, GenericRecord>(OffsetResetStrategy.EARLIEST).apply {
            assign(listOf(partition))
            updateBeginningOffsets(mapOf(partition to 0L))
        }
        val søknad = Søknad(
            søknadId = "søknadid",
            journalpostId = "journalpostId",
            dokumentInfoId = "dokumentInfoId",
            fornavn = null,
            etternavn = null,
            ident = "ident",
            deltarKvp = false,
            deltarIntroduksjonsprogrammet = false,
            oppholdInstitusjon = false,
            typeInstitusjon = null,
            opprettet = LocalDateTime.now(),
            barnetillegg = emptyList(),
            arenaTiltak = null,
            brukerregistrertTiltak = null,
            trygdOgPensjon = emptyList(),
        )
        val mockProducer = MockProducer(true, StringSerializer(), StringSerializer())
        val joarkReplicator = JoarkReplicator(mockConsumer, mockProducer)
        mockkStatic("no.nav.tiltakspenger.mottak.soknad.SoknadMediatorKt")
        coEvery { handleSøknad(journalpostId.toString()) } returns søknad
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
    fun `ingen søknad funnet gir ingen feil`() {
        val topicName = "topic"
        val partition = TopicPartition(topicName, 0)
        val journalpostId = 1L
        val mockConsumer = MockConsumer<String, GenericRecord>(OffsetResetStrategy.EARLIEST).apply {
            assign(listOf(partition))
            updateBeginningOffsets(mapOf(partition to 0L))
        }
        val mockProducer = MockProducer(true, StringSerializer(), StringSerializer())
        val joarkReplicator = JoarkReplicator(mockConsumer, mockProducer)
        mockkStatic("no.nav.tiltakspenger.mottak.søknad.SøknadMediatorKt")
        coEvery { handleSøknad(journalpostId.toString()) } returns null
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
