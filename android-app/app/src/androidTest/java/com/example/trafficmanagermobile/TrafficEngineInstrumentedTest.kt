package com.example.trafficmanagermobile

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class TrafficEngineInstrumentedTest {

    @Test
    fun useAppContext() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.trafficmanagermobile", appContext.packageName)
    }

    @Test
    fun nativeEngine_parsesValidPacketCorrectly() {
        val engine = TrafficEngine()

        val result = engine.parsePacketNative("105,CRITICAL,1024,SecurePayload")

        assertNotNull(result)
        assertTrue("Wynik powinien zawierać priorytet", result.contains("CRITICAL"))
        assertTrue("Wynik powinien zawierać rozmiar", result.contains("1024"))
    }

    @Test
    fun nativeEngine_throwsExceptionOnInvalidPacket() {
        val engine = TrafficEngine()

        val result = engine.parsePacketNative("BAD_DATA")

        assertNotNull(result)
        assertTrue(
            "Wynik powinien zawierać informację o błędzie",
            result.contains("Error: It is not possible to decode the packet")
        )
    }
}