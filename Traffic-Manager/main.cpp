#include "trafficManager.hpp"
#include "trafficGenerator.hpp"
#include <iostream>

int main() {
    TrafficManager manager;

    std::cout << "TRAFFIC SIMULATOR START:\n\n";

    int packetsToGenerate = 100;
    std::cout << "[SYSTEM] Generating " << packetsToGenerate << " random packets...\n";

    auto incomingTraffic = TrafficGenerator::generateTraffic(packetsToGenerate);

    for (auto& packet : incomingTraffic) {
        manager.addPacket(std::move(packet));
    }

    manager.dropOversizedPackets(1500);
    manager.sortPackets();
    manager.processTrafficWithClear();
    manager.printStatistics();

    std::cout << "\nSIMULATION FINISHED\n";

    return 0;
}