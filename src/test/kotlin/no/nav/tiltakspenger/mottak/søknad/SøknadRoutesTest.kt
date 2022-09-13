package no.nav.tiltakspenger.mottak.søknad

import com.auth0.jwk.UrlJwkProvider
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import kotliquery.Session
import kotliquery.action.ListResultQueryAction
import kotliquery.action.NullableResultQueryAction
import no.nav.tiltakspenger.mottak.AuthConfig
import no.nav.tiltakspenger.mottak.acceptJson
import no.nav.tiltakspenger.mottak.appRoutes
import no.nav.tiltakspenger.mottak.db.DataSource
import no.nav.tiltakspenger.mottak.installAuth
import no.nav.tiltakspenger.mottak.søknad.søknadList.Søknad
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import java.time.LocalDateTime

internal class SøknadRoutesTest {

    private val mockSession = mockk<Session>(relaxed = false)
    private val mockSøknad = Søknad(
        søknadId = "12312",
        journalpostId = "23",
        dokumentInfoId = "45",
        fornavn = "Gøyal",
        etternavn = "Maskin",
        ident = "123",
        deltarKvp = false,
        deltarIntroduksjonsprogrammet = false,
        oppholdInstitusjon = false,
        typeInstitusjon = "",
        opprettet = LocalDateTime.MAX,
        barnetillegg = emptyList(),
        arenaTiltak = null,
        brukerregistrertTiltak = null
    )

    init {
        mockkObject(DataSource)
        every { DataSource.hikariDataSource } returns mockk()
        every { DataSource.session } returns mockSession
    }

    @BeforeEach
    fun clear() {
        clearAllMocks()
    }

    @Test
    @Disabled("to be deleted, the soknad routes should be moved to tiltakspenger-vedtak")
    fun `should get soknad list`() {
        every { mockSession.run(any<ListResultQueryAction<Søknad>>()) } returns listOf(
            mockSøknad
        )
        every { mockSession.run(any<NullableResultQueryAction<Int>>()) } returns 1

        mockkObject(DataSource)
        every { DataSource.hikariDataSource } returns mockk()
        every { DataSource.session } returns mockSession

        testApplication {
            soknadTestRoutes()
            val response = client.get("/api/soknad")
            val expectedJson = this::class.java.classLoader.getResource("soknadTest.json")!!.readText()
            JSONAssert.assertEquals(expectedJson, response.bodyAsText(), JSONCompareMode.LENIENT)
        }
    }

    @Test
    @Disabled("to be deleted, the soknad routes should be moved to tiltakspenger-vedtak")
    fun `should paginate using query params`() {
        every {
            mockSession.run(match<ListResultQueryAction<Søknad>> { it.query.paramMap["offset"] == 0 })
        } returns listOf(
            mockSøknad
        )
        every {
            mockSession.run(match<ListResultQueryAction<Søknad>> { it.query.paramMap["offset"] == 1 })
        } returns listOf(
            mockSøknad.copy(søknadId = "12313", fornavn = "Liten")
        )
        every { mockSession.run(any<NullableResultQueryAction<Int>>()) } returns 2

        mockkObject(DataSource)
        every { DataSource.hikariDataSource } returns mockk()
        every { DataSource.session } returns mockSession

        testApplication {
            soknadTestRoutes()
            var response = client.get("/api/soknad?pageSize=1&offset=1")
            var expectedJson = this::class.java.classLoader.getResource("soknadPage2.json")!!.readText()
            JSONAssert.assertEquals(expectedJson, response.bodyAsText(), JSONCompareMode.LENIENT)
            response = client.get("/api/soknad?pageSize=1&offset=0")
            expectedJson = this::class.java.classLoader.getResource("soknadPage1.json")!!.readText()
            JSONAssert.assertEquals(expectedJson, response.bodyAsText(), JSONCompareMode.LENIENT)
        }
    }

    @Test
    fun `should tolerate offset bigger than total in pagination`() {
        every {
            mockSession.run(any<ListResultQueryAction<Søknad>>())
        } returns emptyList()
        every { mockSession.run(any<NullableResultQueryAction<Int>>()) } returns 0

        mockkObject(DataSource)
        every { DataSource.hikariDataSource } returns mockk()
        every { DataSource.session } returns mockSession

        testApplication {
            soknadTestRoutes()
            val response = client.get("/api/soknad?offset=20000")
            val expectedJson = this::class.java.classLoader.getResource("emptyPage.json")!!.readText()
            JSONAssert.assertEquals(expectedJson, response.bodyAsText(), JSONCompareMode.LENIENT)
        }
    }

    @Test
    fun `should return 404 when soknad not found`() {
        every { mockSession.run(any<NullableResultQueryAction<Søknad>>()) } returns null

        testApplication {
            soknadTestRoutes()
            val client = createClient {
                expectSuccess = false
            }
            val response = client.get("/api/soknad/54123")
            assertEquals(HttpStatusCode.NotFound, response.status)
        }
    }

    @Test
    fun `soknad endpoint should require authentication`() {
        mockkObject(AuthConfig)
        every { AuthConfig.issuer } returns "Issuer"
        val jwkProvider: UrlJwkProvider = mockk()

        testApplication {
            application {
                installAuth(jwkProvider)
                appRoutes(emptyList())
            }
            val client = createClient {
                expectSuccess = false
            }
            val response = client.get("/api/soknad")
            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }
    }
}

fun ApplicationTestBuilder.soknadTestRoutes() {
    application { acceptJson() }
    routing {
        søknadRoutes()
    }
}
