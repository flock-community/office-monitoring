package flock.community.office.monitoring.backend.device

import com.fasterxml.jackson.databind.ObjectMapper
import flock.community.office.monitoring.backend.device.configuration.DeviceType
import flock.community.office.monitoring.backend.device.domain.ContactSensorStateBody
import flock.community.office.monitoring.backend.device.repository.DeviceStateRepository
import flock.community.office.monitoring.backend.device.repository.DeviceStateEntity
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.random.Random

@SpringBootTest
@Disabled
class DummyDataImporter {

    @Autowired
    lateinit var repository: DeviceStateRepository

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    fun `insert dummy data into DataStore`() {

        (5L downTo 0).forEach{ interval ->

            val contactSensorStateBody = ContactSensorStateBody(
                lastSeen = Instant.now().minus(interval, ChronoUnit.DAYS),
                battery = Random.nextInt(0, 100),
                voltage = Random.nextInt(0, 100),
                contact = Random.nextBoolean()
            )

            val deviceStateEntity = DeviceStateEntity(
                UUID.randomUUID().toString(),
                DeviceType.CONTACT_SENSOR,
                "zigbee2mqtt/0x00158d000578385c",
                Instant.now(),
                objectMapper.writeValueAsString(contactSensorStateBody)
            )

            repository.save(deviceStateEntity).also {
                println("Saved: $it")
            }
        }
    }
}
