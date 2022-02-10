package no.nav.tpts.mottak.health

interface HealthCheck {
    val name: String
        get() = this.javaClass.simpleName

    fun status(): HealthStatus
}

enum class HealthStatus { TILFREDS, ULYKKELIG }
