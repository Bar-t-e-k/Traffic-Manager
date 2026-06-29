#ifndef RUDP_RETRANSMISSION_MANAGER_HPP
#define RUDP_RETRANSMISSION_MANAGER_HPP

#include "rudpPacket.hpp"
#include <map>
#include <chrono>
#include <thread>
#include <mutex>
#include <vector>
#include <functional>
#include <iostream>

struct UnackedPacket {
    RudpPacket packet;
    std::chrono::steady_clock::time_point lastSentTime;
    int retransmissionCount = 0;
};

class RudpRetransmissionManager {
private:
    std::map<uint32_t, UnackedPacket> unackedPackets; 
    std::mutex managerMutex; // Prevents race conditions between the main network thread and the timeout checker thread                 
    std::thread workerThread;                         
    bool isRunning = false;

    // Tuning parameters for reliable delivery
    const std::chrono::milliseconds TIMEOUT_DURATION{ 200 };
    const int MAX_RETRANSMISSIONS = 5;                     

    std::function<void(const RudpPacket&)> sendFunction;

    // Background worker loop. Periodically scans the unacked buffer for stale packets
    // and triggers retransmission if the ACK hasn't arrived within TIMEOUT_DURATION.
    void checkTimeoutsLoop() {
        while (isRunning) {
            std::this_thread::sleep_for(std::chrono::milliseconds(50));

            std::vector<RudpPacket> packetsToRetransmit;
            auto now = std::chrono::steady_clock::now();

            {
                std::lock_guard<std::mutex> lock(managerMutex);

                auto it = unackedPackets.begin();
                while (it != unackedPackets.end()) {
                    if (now - it->second.lastSentTime >= TIMEOUT_DURATION) {

                        if (it->second.retransmissionCount >= MAX_RETRANSMISSIONS) {
                            std::cout << "[RUDP] Packet " << it->first << " lost. Attempt limit exceeded!\n";
                            it = unackedPackets.erase(it);
                        }
                        else {
                            it->second.retransmissionCount++;
                            it->second.lastSentTime = now;
                            packetsToRetransmit.push_back(it->second.packet);
                            it++;
                        }
                    }
                    else {
                        it++;
                    }
                }
            }

            for (const auto& packet : packetsToRetransmit) {
                std::cout << "[RUDP] Timeout! Retransmission of a packet based on sequence: " << packet.getSequenceNumber() << "\n";
                if (sendFunction) {
                    sendFunction(packet);
                }
            }
        }
    }

    RudpRetransmissionManager(const RudpRetransmissionManager&) = delete;
    RudpRetransmissionManager& operator=(const RudpRetransmissionManager&) = delete;

public:
    RudpRetransmissionManager(std::function<void(const RudpPacket&)> sender)
        : sendFunction(std::move(sender)) {
    }

    ~RudpRetransmissionManager() {
        stop();
    }

    void start() {
        if (isRunning) return;
        isRunning = true;
        workerThread = std::thread(&RudpRetransmissionManager::checkTimeoutsLoop, this);
    }

    void stop() {
        if (!isRunning) return;
        isRunning = false;
        if (workerThread.joinable()) {
            workerThread.join();
        }
    }

    void trackPacket(const RudpPacket& packet) {
        std::lock_guard<std::mutex> lock(managerMutex);
        UnackedPacket unacked{ packet, std::chrono::steady_clock::now(), 0 };

        unackedPackets.insert_or_assign(packet.getSequenceNumber(), unacked);
    }

    void acknowledgePacket(uint32_t sequenceNumber) {
        std::lock_guard<std::mutex> lock(managerMutex);
        if (unackedPackets.erase(sequenceNumber) > 0) {
            std::cout << "[RUDP] ACK received for packet: " << sequenceNumber << ". Removed from buffer.\n";
        }
    }
};

#endif