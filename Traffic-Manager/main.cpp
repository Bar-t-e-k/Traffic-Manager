#include "trafficManager.hpp"
#include <iostream>

int main() {
    TrafficManager manager;

    std::cout << "TRAFFIC SIMULATOR START:\n\n";

    auto p1 = std::make_unique<Packet>(1, Priority::LOW, "Background update");
    auto p2 = std::make_unique<Packet>(2, Priority::CRITICAL, "Emergency Call");
    auto p3 = std::make_unique<Packet>(3, Priority::HIGH, "Video Stream");
    auto p4 = std::make_unique<Packet>(4, Priority::MEDIUM, "Web Browsing");

    manager.addPacket(std::move(p1));
    manager.addPacket(std::move(p2));
    manager.addPacket(std::move(p3));
    manager.addPacket(std::move(p4));

    std::cout << "\nBEFORE SORTING:\n";

	manager.processTraffic();

    manager.sortPackets();

    std::cout << "\nPROCESSING (AFTER SORTING):\n";
    manager.processTrafficWithClear();

    std::cout << "\nSIMULATION FINISHED\n";

    return 0;
}