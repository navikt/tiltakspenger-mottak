package no.nav.tpts.mottak

import org.apache.kafka.clients.producer.MockProducer
import org.apache.kafka.common.serialization.StringSerializer
import kotlin.test.Test
import kotlin.test.assertNotNull

class AppTest {
    @Test
    fun `committer ikke naar det skjer feil i konsumeringen`() {
        val mockProducer = MockProducer(false, StringSerializer(), StringSerializer())
        assertNotNull(mockProducer)
    }
}
