package com.example.trafficmanagermobile

class TrafficEngine {
    init {
        System.loadLibrary("trafficmanagermobile")
    }

    @Throws(IllegalArgumentException::class)
    external fun parsePacketNative(rawData: String): String
    external fun startRudpServerNative(port: Int): Boolean
    external fun stopRudpServerNative()
    external fun receiveRudpPacketNative(): String
    external fun sendRudpPacketNative(payload: String)
}