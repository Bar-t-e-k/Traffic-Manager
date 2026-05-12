#include "trafficManager.hpp"
#include "trafficGenerator.hpp"
#include <iostream>

int main() {
    std::ofstream clearFile("simulator_logs.txt", std::ios::trunc);
    clearFile.close();

    TrafficManager manager;

    Logger::log("TRAFFIC SIMULATOR START:\n");

    int packetsToGenerate = 100;
    Logger::log("[SYSTEM] Generating " + std::to_string(packetsToGenerate) + " random packets...");

    try {
        auto incomingTraffic = TrafficGenerator::generateTraffic(packetsToGenerate);

        for (auto& packet : incomingTraffic) {
            manager.addPacket(std::move(packet));
        }
    } catch (const InvalidPacketException& ex) {
        Logger::log("[ERROR] " + std::string(ex.what()));
	} catch (const std::exception& ex) {
        Logger::log("[ERROR] Unexpected exception: " + std::string(ex.what()));
        return -1;
	}
    
    manager.dropOversizedPackets(1500);
    manager.sortPackets();
    manager.processTrafficWithClear();
    manager.printStatistics();

    Logger::log("\nSIMULATION FINISHED\n");

    return 0;
}