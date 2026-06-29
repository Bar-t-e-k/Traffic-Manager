package com.example.trafficmanagermobile

import org.junit.Test

import org.junit.Assert.*

class TrafficLogUnitTest {

    @Test
    fun trafficLog_isCreatedCorrectly() {
        val id = 1
        val message = "Test Log from C++ engine"
        val isError = false
        val isCritical = true

        val log = TrafficLog(id, message, isError, isCritical)

        assertEquals(1, log.id)
        assertEquals("Test Log from C++ engine", log.message)
        assertFalse(log.isError)
        assertTrue(log.isCritical)
    }

    @Test
    fun trafficLog_equalityWorksCorrectly() {
        val log1 = TrafficLog(100, "Test Log from C++ engine", isError = false, isCritical = false)
        val log2 = TrafficLog(100, "Test Log from C++ engine", isError = false, isCritical = false)
        val log3 = TrafficLog(101, "Another Test Log", isError = true, isCritical = false)

        assertTrue("Objects with the same data should be equal", log1 == log2)
        assertFalse("Objects with different data should not be equal", log1 == log3)
    }
}