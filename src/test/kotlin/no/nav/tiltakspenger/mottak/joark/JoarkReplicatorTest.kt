package no.nav.tiltakspenger.mottak.joark

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import mu.KotlinLogging
import no.nav.tiltakspenger.mottak.Configuration
import no.nav.tiltakspenger.mottak.saf.SafService
import no.nav.tiltakspenger.mottak.søknad.DokumentInfoDTO
import no.nav.tiltakspenger.mottak.søknad.FraOgMedDatoSpmDTO
import no.nav.tiltakspenger.mottak.søknad.JaNeiSpmDTO
import no.nav.tiltakspenger.mottak.søknad.PeriodeSpmDTO
import no.nav.tiltakspenger.mottak.søknad.PersonopplysningerDTO
import no.nav.tiltakspenger.mottak.søknad.SpmSvarDTO
import no.nav.tiltakspenger.mottak.søknad.SøknadDTO
import no.nav.tiltakspenger.mottak.søknad.TiltakDTO
import no.nav.tiltakspenger.mottak.søknad.models.Periode
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
import java.time.LocalDate
import java.time.LocalDateTime

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
        """.trimIndent(),
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
        val mockIdentPublisher = mockk<IdentPublisher>(relaxed = true)
        val mockJournalpostIdPublisher = mockk<JournalpostIdPublisher>(relaxed = true)
        val joarkReplicator = JoarkReplicator(
            mockConsumer,
            mockProducer,
            mockIdentPublisher,
            mockJournalpostIdPublisher,
            mockk(),
            Configuration.tptsRapidName(),
        )
        val record = GenericData.Record(joarkjournalfoeringhendelserAvroSchema).apply {
            put("journalpostId", journalpostId)
            put("temaNytt", "IND")
        }
        repeat(offsets) {
            mockConsumer.addRecord(
                ConsumerRecord(topicName, partition.partition(), it.toLong(), "$journalpostId", record),
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
        val mockIdentPublisher = mockk<IdentPublisher>(relaxed = true)
        val mockJournalpostIdPublisher = mockk<JournalpostIdPublisher>(relaxed = true)

        try {
            runTest {
                JoarkReplicator(
                    mockConsumer,
                    mockProducer,
                    mockIdentPublisher,
                    mockJournalpostIdPublisher,
                    mockk(),
                    Configuration.tptsRapidName(),
                    this,
                ).start()
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
        val søknadDTO = SøknadDTO(
            versjon = "4",
            søknadId = "søknadId",
            dokInfo = DokumentInfoDTO(
                journalpostId = "noluisse",
                dokumentInfoId = "ac",
                filnavn = null,
            ),
            personopplysninger = PersonopplysningerDTO(
                ident = "dis",
                fornavn = "a",
                etternavn = "liber",
            ),
            tiltak = TiltakDTO(
                aktivitetId = "123",
                periode = Periode(fra = LocalDate.of(2023, 1, 1), til = LocalDate.of(2023, 3, 31)),
                arenaRegistrertPeriode = null,
                arrangør = "arrangør",
                type = "AMO",
                typeNavn = "AMO",

            ),
            barnetilleggPdl = emptyList(),
            barnetilleggManuelle = emptyList(),
            vedlegg = emptyList(),
            kvp = PeriodeSpmDTO(svar = SpmSvarDTO.Nei, fom = null, tom = null),
            intro = PeriodeSpmDTO(svar = SpmSvarDTO.Nei, fom = null, tom = null),
            institusjon = PeriodeSpmDTO(svar = SpmSvarDTO.Nei, fom = null, tom = null),
            etterlønn = JaNeiSpmDTO(svar = SpmSvarDTO.Nei),
            gjenlevendepensjon = PeriodeSpmDTO(svar = SpmSvarDTO.Nei, fom = null, tom = null),
            alderspensjon = FraOgMedDatoSpmDTO(svar = SpmSvarDTO.Nei, fom = null),
            sykepenger = PeriodeSpmDTO(svar = SpmSvarDTO.Nei, fom = null, tom = null),
            supplerendeStønadAlder = PeriodeSpmDTO(svar = SpmSvarDTO.Nei, fom = null, tom = null),
            supplerendeStønadFlyktning = PeriodeSpmDTO(svar = SpmSvarDTO.Nei, fom = null, tom = null),
            jobbsjansen = PeriodeSpmDTO(svar = SpmSvarDTO.Nei, fom = null, tom = null),
            trygdOgPensjon = PeriodeSpmDTO(svar = SpmSvarDTO.Nei, fom = null, tom = null),
            opprettet = LocalDateTime.now(),
        )
        val mockProducer = MockProducer(true, StringSerializer(), StringSerializer())
        val mockIdentPublisher = mockk<IdentPublisher>(relaxed = true)
        val mockJournalpostIdPublisher = mockk<JournalpostIdPublisher>(relaxed = true)
        val safService = mockk<SafService>()
        val joarkReplicator = JoarkReplicator(
            mockConsumer,
            mockProducer,
            mockIdentPublisher,
            mockJournalpostIdPublisher,
            safService,
            Configuration.tptsRapidName(),
        )
        coEvery { safService.hentSøknad(journalpostId.toString()) } returns søknadDTO
        val record = GenericData.Record(joarkjournalfoeringhendelserAvroSchema).apply {
            put("journalpostId", journalpostId)
            put("temaNytt", "IND")
            put("journalpostStatus", "MOTTATT")
        }
        mockConsumer.addRecord(
            ConsumerRecord(topicName, partition.partition(), 1L, "$journalpostId", record),
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
        val mockIdentPublisher = mockk<IdentPublisher>(relaxed = true)
        val mockJournalpostIdPublisher = mockk<JournalpostIdPublisher>(relaxed = true)
        val safService = mockk<SafService>()
        val joarkReplicator = JoarkReplicator(
            mockConsumer,
            mockProducer,
            mockIdentPublisher,
            mockJournalpostIdPublisher,
            safService,
            Configuration.tptsRapidName(),
        )
        coEvery { safService.hentSøknad(journalpostId.toString()) } returns null
        val record = GenericData.Record(joarkjournalfoeringhendelserAvroSchema).apply {
            put("journalpostId", journalpostId)
            put("temaNytt", "IND")
            put("journalpostStatus", "MOTTATT")
        }
        mockConsumer.addRecord(
            ConsumerRecord(topicName, partition.partition(), 1L, "$journalpostId", record),
        )
        runBlocking {
            joarkReplicator.start()
            delay(500L)
            assertEquals(0, mockProducer.history().size)
            joarkReplicator.stop()
        }
    }
}
