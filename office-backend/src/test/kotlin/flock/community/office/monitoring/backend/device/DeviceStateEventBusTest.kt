package flock.community.office.monitoring.backend.device

import flock.community.office.monitoring.backend.device.configuration.DeviceType
import flock.community.office.monitoring.backend.device.domain.ContactSensorStateBody
import flock.community.office.monitoring.backend.device.domain.DeviceState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class)
internal class DeviceStateEventBusTest {

    private val testBus = DeviceStateEventBus()

    private fun createTestMessages(amount: Int): List<DeviceState<ContactSensorStateBody>> {

        return (0L..amount).map {

            val contactSensorStateBody = ContactSensorStateBody(
                lastSeen = Instant.parse("2021-01-29T10:00:00.00Z"),
                battery = Random.nextInt(0, 100),
                voltage = Random.nextInt(0, 100),
                contact = Random.nextBoolean()
            )

            DeviceState(
                UUID.randomUUID().toString(),
                DeviceType.CONTACT_SENSOR,
                "eb11fe92-139f-4abd-874f-a622fc6ea050",
                "zigbee2mqtt/0x00158d000578385c",
                Instant.now().minus(it, ChronoUnit.MINUTES),
                contactSensorStateBody
            )

        }
    }


    @Test
    fun `When publishing a message to eventBus subscribe retrieves it`() = runBlockingTest {
        val testEntity = createTestMessages(1)[0]
        testBus.publish(testEntity)

        assertEquals(testEntity, testBus.subscribe(null).first())
    }

    @Test
    fun `When publishing multiple messages to eventBus subscribe retrieves the last one`() = runBlockingTest {
        val testEntities = createTestMessages(2)
        testBus.publish(testEntities[0])

        testBus.publish(testEntities[1])

        assertEquals(testEntities[1], testBus.subscribe(null).first())
    }
}
