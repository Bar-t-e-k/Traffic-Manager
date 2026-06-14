package com.example.trafficmanagermobile

import org.junit.Test

import org.junit.Assert.*

class TrafficLogUnitTest {

    @Test
    fun trafficLog_isCreatedCorrectly() {
        val id = 1
        val message = "Testowy Log z silnika C++"
        val isError = false
        val isCritical = true

        val log = TrafficLog(id, message, isError, isCritical)

        assertEquals(1, log.id)
        assertEquals("Testowy Log z silnika C++", log.message)
        assertFalse(log.isError)
        assertTrue(log.isCritical)
    }

    @Test
    fun trafficLog_equalityWorksCorrectly() {
        val log1 = TrafficLog(100, "Zdarzenie A", isError = false, isCritical = false)
        val log2 = TrafficLog(100, "Zdarzenie A", isError = false, isCritical = false)
        val log3 = TrafficLog(101, "Zdarzenie B", isError = true, isCritical = false)

        assertTrue("Obiekty o tych samych danych powinny być równe", log1 == log2)
        assertFalse("Obiekty o różnych danych nie są równe", log1 == log3)
    }
}