package flock.community.office.monitoring.backend.domain.service

import com.fasterxml.jackson.databind.ObjectMapper
import flock.community.office.monitoring.backend.configuration.DeviceType
import flock.community.office.monitoring.backend.domain.model.ContactSensorStateBody
import flock.community.office.monitoring.backend.domain.repository.entities.DeviceStateEntity
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.Instant
import java.util.*
import kotlin.random.Random

@SpringBootTest
internal class DeviceStateEventBusTest() {

    private final val testBus = DeviceStateEventBus()

    //Wat is het verschil tussen injectie en member autowiring? En wat doet de lateinit?
    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    fun `test when publishing messages to eventBus subscribe retrieves them`() = runBlocking {
        val contactSensorStateBody = ContactSensorStateBody(
                lastSeen = Instant.parse("2021-01-29T10:00:00.00Z"),
                battery = Random.nextInt(0, 100),
                voltage = Random.nextInt(0, 100),
                contact = Random.nextBoolean())

//        Wat is het verschil tussen deviceId en topic?
        val testEntity = DeviceStateEntity(
                UUID.randomUUID().toString(),
                DeviceType.CONTACT_SENSOR,
                "zigbee2mqtt/0x00158d000578385c",
                Instant.now(),
                objectMapper.writeValueAsString(contactSensorStateBody)
        )

        testBus.publish(testEntity)

        assertEquals(testEntity, testBus.subscribe(null).first())
    }
}