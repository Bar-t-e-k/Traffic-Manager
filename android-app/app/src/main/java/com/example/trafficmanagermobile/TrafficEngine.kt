package com.example.trafficmanagermobile

class TrafficEngine {
    init {
        System.loadLibrary("trafficmanagermobile")
    }

    @Throws(IllegalArgumentException::class)
    external fun parsePacketNative(rawData: String): String
}