package flock.community.office.monitoring.backend.domain.service

import flock.community.office.monitoring.backend.domain.repository.entities.DeviceStateEntity

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired

internal class DevicesFeedCommandExecutorTest (@Autowired val testExecutor: DevicesFeedCommandExecutor){

    fun setup(){

    }

    @Test
    fun getNewEventsForDevice() {
        val resultFlow = testExecutor.getNewEventsForDevice("1")

    }
}