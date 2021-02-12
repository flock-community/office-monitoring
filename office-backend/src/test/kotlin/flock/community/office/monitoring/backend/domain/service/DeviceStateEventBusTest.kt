package flock.community.office.monitoring.backend.domain.service

import com.fasterxml.jackson.databind.ObjectMapper
import flock.community.office.monitoring.backend.configuration.DeviceType
import flock.community.office.monitoring.backend.domain.model.ContactSensorStateBody
import flock.community.office.monitoring.backend.domain.repository.entities.DeviceStateEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.stream.IntStream.range
import kotlin.random.Random

@SpringBootTest
internal class DeviceStateEventBusTest(@Autowired var objectMapper: ObjectMapper) {

    //Wat is het verschil tussen injectie en member autowiring? En wat doet de lateinit?
    @Autowired
    private final val testBus = DeviceStateEventBus()

    fun createTestMessages(amount: Int): List<DeviceStateEntity>{
        val contactSensorStateBody = ContactSensorStateBody(
                lastSeen = Instant.parse("2021-01-29T10:00:00.00Z"),
                battery = Random.nextInt(0, 100),
                voltage = Random.nextInt(0, 100),
                contact = Random.nextBoolean())

        val list: MutableList<DeviceStateEntity> = mutableListOf()
        for (i in range(0, amount)){
            val dse = DeviceStateEntity(UUID.randomUUID().toString(),
                    DeviceType.CONTACT_SENSOR,
                    "zigbee2mqtt/0x00158d000578385c",
                    Instant.now().minus(i.toLong(), ChronoUnit.MINUTES),
                    objectMapper.writeValueAsString(contactSensorStateBody))
            list.add(dse)
        }
        return list
    }

    @Test
    fun `test when publishing a message to eventBus subscribe retrieves it`() = runBlocking {
        val testEntity = createTestMessages(1)[0]
        testBus.publish(testEntity)

        assertEquals(testEntity, testBus.subscribe(null).first())
    }

    @Test
    fun `test when publishing multiple messages to eventBus subscribe retrieves the last one`() = runBlocking {
        val testEntities = createTestMessages(2)
        testBus.publish(testEntities[0])

        testBus.publish(testEntities[1])

        assertEquals(testEntities[1], testBus.subscribe(null).first())
    }
}