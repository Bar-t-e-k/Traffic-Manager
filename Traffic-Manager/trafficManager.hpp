#ifndef TRAFFIC_MANAGER_HPP
#define TRAFFIC_MANAGER_HPP

#include "packet.hpp"
#include <vector>
#include <memory> 
#include <algorithm>

class TrafficManager {
private:
    std::vector<std::unique_ptr<Packet>> queue;

    int totalReceived = 0;
    int totalDropped = 0;
    int totalProcessed = 0;

public:
    void addPacket(std::unique_ptr<Packet> packet) {
        queue.push_back(std::move(packet));
        totalReceived++;
    }

    void dropOversizedPackets(size_t maxAllowedSize) {
        std::cout << "\n[POLICING] Dropping packets larger than " << maxAllowedSize << " bytes.\n";

        size_t initialSize = queue.size();

        queue.erase(
            std::remove_if(queue.begin(), queue.end(),
                [maxAllowedSize](const std::unique_ptr<Packet>& packet) {
                    return packet->getSize() > maxAllowedSize;
                }),
            queue.end()
        );

        int dropped = initialSize - queue.size();
        totalDropped += dropped;
        std::cout << "\n[POLICING] Dropped " << dropped << " oversized packets.\n";
    }

    void sortPackets() {
        std::cout << "\n[SCHEDULER] Sorting packets by priority...\n";

        std::sort(queue.begin(), queue.end(), [](const std::unique_ptr<Packet>& a, const std::unique_ptr<Packet>& b) {
            return static_cast<int>(a->getPriority()) > static_cast<int>(b->getPriority());
            });
    }

    void processTraffic() {
        std::cout << "\n[SYSTEM] Starting to process traffic...\n";

        for (auto& packet : queue) {
            std::cout << "[SENDING] ID: " << packet->getId()
                << " | Priority: " << packet->getPriorityString() << "\n";
        }

		totalProcessed += queue.size();
    }

    void processTrafficWithClear() {
		processTraffic();

        queue.clear();
    }

    void printStatistics() const {
        std::cout << "\n=====================================\n";
        std::cout << "      TRAFFIC MANAGER STATISTICS     \n";
        std::cout << "=====================================\n";
        std::cout << " Total Packets Received : " << totalReceived << "\n";
        std::cout << " Packets Dropped (MTU)  : " << totalDropped << "\n";
        std::cout << " Packets Processed      : " << totalProcessed << "\n";
        std::cout << "=====================================\n";
    }
};

#endif