package com.example.trafficmanagermobile

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TrafficViewModel : ViewModel() {
    private val engine = TrafficEngine()

    private val _resultState = MutableStateFlow("Waiting for packet...")
    val resultState: StateFlow<String> = _resultState.asStateFlow()

    fun processPacket(rawData: String) {
        if (rawData.isBlank()) {
            _resultState.value = "Insert packet data to parse."
            return
        }

        try {
            // Wywołujemy natywną metodę z C++
            val result = engine.parsePacketNative(rawData)
            _resultState.value = "SUCCESS (returned by C++):\n$result"
        } catch (e: IllegalArgumentException) {
            // Łapiemy wyjątek rzucony przez C++ (np. InvalidPacketException dla rozmiaru 0)
            _resultState.value = "CRITICAL ERROR (C++ Exception):\n${e.message}"
        } catch (e: Exception) {
            _resultState.value = "UNKNOWN ERROR:\n${e.message}"
        }
    }
}