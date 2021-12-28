package no.nav.tpts.mottak.soknad

import io.ktor.application.Application
import io.ktor.http.HttpMethod
import io.ktor.routing.routing
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import kotliquery.Session
import kotliquery.action.ListResultQueryAction
import no.nav.tpts.mottak.acceptJson
import no.nav.tpts.mottak.db.DataSource
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals

class SoknadRoutesTest {

    @Test
    fun shouldGetSoknad() {
        val mockSession = mockk<Session>(relaxed = false)
        every { mockSession.run(any<ListResultQueryAction<Soknad>>()) } returns listOf(
            Soknad(
                navn = "Sigurd",
                opprettetDato = LocalDateTime.MAX,
                brukerStartDato = null,
                brukerSluttDato = null
            )
        )

        mockkObject(DataSource)
        every { DataSource.hikariDataSource } returns mockk()
        every { DataSource.session } returns mockSession

        withTestApplication({ soknadRoutes() }) {
            handleRequest(HttpMethod.Get, "/api/soknad").apply {
                // javaClass.getResource will read from the resources folder in main, not test
                val expectedJson = this::class.java.classLoader.getResource("soknadTest.json")!!.readText()
                    .replace(Regex("[\n\t\\s]"), "")
                assertEquals(
                    expectedJson,
                    response.content
                )
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
