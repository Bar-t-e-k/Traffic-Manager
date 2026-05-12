#ifndef TRAFFIC_MANAGER_HPP
#define TRAFFIC_MANAGER_HPP

#include "packet.hpp"
#include "logger.hpp"
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
        Logger::log("[POLICING] Dropping packets larger than " + std::to_string(maxAllowedSize) + " bytes.");

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
        Logger::log("[POLICING] Dropped " + std::to_string(dropped) + " oversized packets.");
    }

    void sortPackets() {
        Logger::log("[SCHEDULER] Sorting packets by priority...");

        std::sort(queue.begin(), queue.end(), [](const std::unique_ptr<Packet>& a, const std::unique_ptr<Packet>& b) {
            return static_cast<int>(a->getPriority()) > static_cast<int>(b->getPriority());
            });
    }

    size_t getQueueSize() const { return queue.size(); }

    void processTraffic() {
        Logger::log("[SYSTEM] Starting to process traffic...");

        for (auto& packet : queue) {
            Logger::log("[SENDING] ID: " + std::to_string(packet->getId()) +
                        " | Priority: " + packet->getPriorityString());
        }

		totalProcessed += queue.size();
    }

    void processTrafficWithClear() {
		processTraffic();

        queue.clear();
    }

    void printStatistics() const {
        Logger::log("\n=====================================");
        Logger::log("      TRAFFIC MANAGER STATISTICS     ");
        Logger::log("=====================================");
        Logger::log(" Total Packets Received : " + std::to_string(totalReceived));
        Logger::log(" Packets Dropped (MTU)  : " + std::to_string(totalDropped));
        Logger::log(" Packets Processed      : " + std::to_string(totalProcessed));
        Logger::log("=====================================");
    }
};

#endif