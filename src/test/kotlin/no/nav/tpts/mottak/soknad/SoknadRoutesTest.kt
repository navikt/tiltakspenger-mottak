package no.nav.tpts.mottak.soknad

import com.auth0.jwk.UrlJwkProvider
import io.ktor.application.Application
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.routing.routing
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import kotliquery.Session
import kotliquery.action.ListResultQueryAction
import kotliquery.action.NullableResultQueryAction
import no.nav.tpts.mottak.AuthConfig
import no.nav.tpts.mottak.acceptJson
import no.nav.tpts.mottak.appRoutes
import no.nav.tpts.mottak.db.DataSource
import no.nav.tpts.mottak.installAuth
import no.nav.tpts.mottak.soknad.soknadList.Soknad
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import java.time.LocalDateTime

class SoknadRoutesTest {

    private val mockSession = mockk<Session>(relaxed = false)
    private val mockSoknad = Soknad(
        fornavn = "Sigurd",
        etternavn = "Grøneng",
        id = "12312",
        opprettet = LocalDateTime.MAX,
        brukerRegistrertStartDato = null,
        brukerRegistrertSluttDato = null,
        ident = "123",
        systemRegistrertStartDato = null,
        systemRegistrertSluttDato = null,
    )
    /*
    private val mockSoknadDetails = SoknadDetails(
        fornavn = "Sigurd",
        etternavn = "Groneng",
        opprettet = LocalDateTime.MAX,
        brukerStartDato = null,
        brukerSluttDato = null,
        fnr = "12121221212"
    )*/

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

        withTestApplication({ soknadRoutes() }) {
            handleRequest(HttpMethod.Get, "/api/soknad").apply {
                // javaClass.getResource will read from the resources folder in main, not test
                val expectedJson = this::class.java.classLoader.getResource("soknadTest.json")!!.readText()
                JSONAssert.assertEquals(expectedJson, response.content, JSONCompareMode.LENIENT)
            }
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

        withTestApplication({ soknadRoutes() }) {
            handleRequest(HttpMethod.Get, "/api/soknad?pageSize=1&offset=1").apply {
                val expectedJson = this::class.java.classLoader.getResource("soknadPage2.json")!!.readText()
                JSONAssert.assertEquals(expectedJson, response.content, JSONCompareMode.LENIENT)
            }
            handleRequest(HttpMethod.Get, "/api/soknad?pageSize=1&offset=0").apply {
                val expectedJson = this::class.java.classLoader.getResource("soknadPage1.json")!!.readText()
                JSONAssert.assertEquals(expectedJson, response.content, JSONCompareMode.LENIENT)
            }
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

        withTestApplication({ soknadRoutes() }) {
            handleRequest(HttpMethod.Get, "/api/soknad?offset=20000").apply {
                val expectedJson = this::class.java.classLoader.getResource("emptyPage.json")!!.readText()
                JSONAssert.assertEquals(expectedJson, response.content, JSONCompareMode.LENIENT)
            }
        }
    }

    /*
    @Test
    fun `should get soknad by id`() {
        every { mockSession.run(any<NullableResultQueryAction<SoknadDetails>>()) } returns mockSoknadDetails

        withTestApplication({ soknadRoutes() }) {
            handleRequest(HttpMethod.Get, "/api/soknad/54123").apply {
                // javaClass.getResource will read from the resources folder in main, not test
                val expectedJson = this::class.java.classLoader.getResource("soknad.json")!!.readText()
                JSONAssert.assertEquals(expectedJson, response.content, JSONCompareMode.LENIENT)
            }
        }
    }
    */

    @Test
    fun `should return 404 when sokand not found`() {
        every { mockSession.run(any<NullableResultQueryAction<Soknad>>()) } returns null

        withTestApplication({ soknadRoutes() }) {
            handleRequest(HttpMethod.Get, "/api/soknad/54123").apply {
                Assertions.assertEquals(response.status(), HttpStatusCode.NotFound)
            }
        }
    }

    @Test
    fun `soknad endpoint should require authentication`() {
        mockkObject(AuthConfig)
        every { AuthConfig.issuer } returns "Issuer"
        val jwkProvider: UrlJwkProvider = mockk()

        withTestApplication({
            installAuth(jwkProvider)
            appRoutes(emptyList())
        }) {
            handleRequest(
                HttpMethod.Get,
                "/api/soknad"
            ).apply {
                Assertions.assertEquals(HttpStatusCode.Unauthorized, response.status())
            }
        }
    }
}

fun Application.soknadRoutes() {
    acceptJson()
    routing {
        soknadRoutes()
    }
}
