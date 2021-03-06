package no.nav.tiltakspenger.mottak.soknad

import com.auth0.jwk.UrlJwkProvider
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
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
import no.nav.tiltakspenger.mottak.soknad.soknadList.Soknad
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import java.time.LocalDateTime

internal class SoknadRoutesTest {

    private val mockSession = mockk<Session>(relaxed = false)
    private val mockSoknad = Soknad(
        id = "12312",
        fornavn = "Sigurd",
        etternavn = "Grøneng",
        ident = "123",
        deltarKvp = false,
        deltarIntroduksjonsprogrammet = false,
        oppholdInstitusjon = false,
        tiltaksArrangoer = "JOBLEARN AS",
        tiltaksType = "Jobbklubb",
        typeInstitusjon = "",
        opprettet = LocalDateTime.MAX,
        brukerRegistrertStartDato = null,
        brukerRegistrertSluttDato = null,
        systemRegistrertStartDato = null,
        systemRegistrertSluttDato = null,
        barnetillegg = emptyList()
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
    fun `should get soknad list`() {
        every { mockSession.run(any<ListResultQueryAction<Soknad>>()) } returns listOf(
            mockSoknad
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
    fun `should paginate using query params`() {
        every {
            mockSession.run(match<ListResultQueryAction<Soknad>> { it.query.paramMap["offset"] == 0 })
        } returns listOf(
            mockSoknad
        )
        every {
            mockSession.run(match<ListResultQueryAction<Soknad>> { it.query.paramMap["offset"] == 1 })
        } returns listOf(
            mockSoknad.copy(id = "12313", fornavn = "Martin")
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
            mockSession.run(any<ListResultQueryAction<Soknad>>())
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
        every { mockSession.run(any<NullableResultQueryAction<Soknad>>()) } returns null

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
        soknadRoutes()
    }
}
