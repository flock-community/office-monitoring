package flock.community.office.monitoring.backend.alerting.service

import flock.community.office.monitoring.backend.alerting.AlertingConfigurationProperties
import flock.community.office.monitoring.backend.alerting.SignalAlertApiConfig
import flock.community.office.monitoring.backend.alerting.client.SignalAlertClient
import nl.wykorijnsburger.kminrandom.minRandomCached
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock

internal class AlertServiceTest{

    private val alertClient = mock(SignalAlertClient::class.java)
    private val alertService = AlertService(alertClient, AlertingConfigurationProperties::class.minRandomCached())

    @Test
    fun `testing it`(){
        val message = """
            Hallo {{name}}
        """.trimIndent()
        val interpolate = alertService.interpolate(message, mapOf("name" to "world"))
        println(interpolate)
        assertEquals("Hallo world", interpolate)
    }

    @Test
    fun `testing it 2`(){
        val message = """
            Hallo {{name}} {{otherName}}
        """.trimIndent()
        val interpolate = alertService.interpolate(message, mapOf("name" to "world"))
        println(interpolate)
        assertEquals("Hallo world {{otherName}}", interpolate)
    }
    @Test
    fun `testing it 3`(){
        val message = """
            Hallo {{name}} {{otherName}}
        """.trimIndent()
        val interpolate = alertService.interpolate(message, mapOf("name" to "world", "123" to "hello"))
        println(interpolate)
        assertEquals("Hallo world {{otherName}}", interpolate)
    }
}
