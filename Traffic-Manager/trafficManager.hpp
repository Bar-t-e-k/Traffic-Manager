#ifndef TRAFFIC_MANAGER_HPP
#define TRAFFIC_MANAGER_HPP

#include "packet.hpp"
#include <vector>
#include <memory> 
#include <algorithm>

class TrafficManager {
private:
    std::vector<std::unique_ptr<Packet>> queue;

public:
    void addPacket(std::unique_ptr<Packet> packet) {
        queue.push_back(std::move(packet));
    }

    void dropOversizedPackets(size_t maxAllowedSize) {
        std::cout << "\n[POLICING] Dropping packets larger than " << maxAllowedSize << " bytes.\n";

        queue.erase(
            std::remove_if(queue.begin(), queue.end(),
                [maxAllowedSize](const std::unique_ptr<Packet>& packet) {
                    return packet->getSize() > maxAllowedSize;
                }),
            queue.end()
        );
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
    }

    void processTrafficWithClear() {
		processTraffic();

        queue.clear();
    }
};

#endif