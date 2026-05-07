#include "trafficManager.hpp"
#include <iostream>

int main() {
    TrafficManager manager;

    std::cout << "TRAFFIC SIMULATOR START:\n\n";

    auto p1 = std::make_unique<Packet>(1, Priority::LOW, "Background update", 500);
    auto p2 = std::make_unique<Packet>(2, Priority::CRITICAL, "Emergency Call", 200);
    auto p3 = std::make_unique<Packet>(3, Priority::HIGH, "Video Stream", 1400);
    auto p4 = std::make_unique<Packet>(4, Priority::MEDIUM, "Web Browsing", 800);
    auto p5 = std::make_unique<Packet>(5, Priority::HIGH, "Giant Video Stream", 9000);

    manager.addPacket(std::move(p1));
    manager.addPacket(std::move(p2));
    manager.addPacket(std::move(p3));
    manager.addPacket(std::move(p4));
    manager.addPacket(std::move(p5));

    std::cout << "\nBEFORE SORTING:\n";

	manager.processTraffic();

    std::cout << "\nDROPPING OVERSIZED PACKETS:\n";
    manager.dropOversizedPackets(1500);

    manager.processTraffic();

    manager.sortPackets();

    std::cout << "\nPROCESSING (AFTER SORTING):\n";
    manager.processTrafficWithClear();

    std::cout << "\nSIMULATION FINISHED\n";

    return 0;
}