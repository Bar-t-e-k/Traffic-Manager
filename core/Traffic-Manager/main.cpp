#include "trafficManager.hpp"
#include "trafficGenerator.hpp"
#include "udpReceiver.hpp"
#include "packetParser.hpp"
#include "logger.hpp"
#include <iostream>
#include <string>

// Mode 1: Simulation with packet generator
void runSimulationMode(TrafficManager& manager) {
    int packetsToGenerate = 100;
    Logger::log("[SYSTEM] Running in SIMULATION mode. Generating " + std::to_string(packetsToGenerate) + " packets...");

    auto incomingTraffic = TrafficGenerator::generateTraffic(packetsToGenerate);
    for (auto& packet : incomingTraffic) {
        manager.addPacket(std::move(packet));
    }

    manager.dropOversizedPackets(1500);
    manager.sortPackets();
    manager.processTrafficWithClear();
    manager.printStatistics();
}

// Mode 2: Server mode with UDP listener
void runServerMode(TrafficManager& manager) {
    UdpReceiver receiver(8080);
    int batchSize = 5;

    Logger::log("[SYSTEM] Running in SERVER mode. Waiting for " + std::to_string(batchSize) + " packets via UDP...");

    int receivedCount = 0;
    while (receivedCount < batchSize) {
        std::string incomingData = receiver.receivePacketData();

        if (!incomingData.empty() && incomingData.back() == '\n') incomingData.pop_back();
        if (!incomingData.empty() && incomingData.back() == '\r') incomingData.pop_back();

        if (!incomingData.empty()) {
            Logger::log("[NETWORK] Received raw packet: " + incomingData);
            auto packet = PacketParser::parse(incomingData);
            if (packet) {
                manager.addPacket(std::move(packet));
                receivedCount++;
            }
        }
    }

    Logger::log("[SYSTEM] Batch full. Applying MTU filter...");
    manager.dropOversizedPackets(1500);
    manager.sortPackets();
    manager.processTrafficWithClear();
    manager.printStatistics();
}

int main(int argc, char* argv[]) {
    std::ofstream clearFile("simulator_logs.txt", std::ios::trunc);
    clearFile.close();

    Logger::log("--- 5G L3 TRAFFIC ROUTER BOOTING ---");

	// Help menu when no arguments are provided
    if (argc < 2) {
        std::cout << "Usage: " << argv[0] << " [mode]\n";
        std::cout << "Available modes:\n";
        std::cout << "  --simulate   Run the internal random traffic generator\n";
        std::cout << "  --server     Start the UDP server and listen for network packets\n";
        Logger::log("[FATAL ERROR] No execution mode provided.");
        return 1; 
    }

    std::string mode = argv[1];
    TrafficManager manager;

    try {
        if (mode == "--simulate") {
            runSimulationMode(manager);
        }
        else if (mode == "--server") {
            runServerMode(manager);
        }
        else {
            std::cout << "Unknown mode: " << mode << "\n";
            Logger::log("[FATAL ERROR] Unknown execution mode: " + mode);
            return 1;
        }
    }
    catch (const std::exception& e) {
        Logger::log(std::string("[FATAL ERROR] System crash: ") + e.what());
        return -1;
    }

    Logger::log("--- SYSTEM SHUTDOWN ---");
    return 0;
}