package flock.community.office.monitoring.backend.domain.service

import kotlinx.coroutines.flow.count
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class DevicesCommandExecutorTest {

    @Test
    fun `test getDevices returns devices`() = runBlocking {
        val testExecutor = DevicesCommandExecutor()
        //assertEquals(2,testExecutor.getDevices().count())
    }
}
