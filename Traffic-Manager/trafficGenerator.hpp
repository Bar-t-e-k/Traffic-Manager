#ifndef TRAFFIC_GENERATOR_HPP
#define TRAFFIC_GENERATOR_HPP

#include "packet.hpp"
#include <memory>
#include <vector>
#include <random>

class TrafficGenerator {
public:
    static std::vector<std::unique_ptr<Packet>> generateTraffic(int numPackets) {
        std::vector<std::unique_ptr<Packet>> generatedPackets;

        std::random_device rd;
        std::mt19937 gen(rd());

        std::uniform_int_distribution<> priorityDist(0, 3);
        std::uniform_int_distribution<> sizeDist(50, 2000);

        for (int i = 1; i <= numPackets; ++i) {
            Priority randPriority = static_cast<Priority>(priorityDist(gen));
            size_t randSize = sizeDist(gen);

            generatedPackets.push_back(std::make_unique<Packet>(i, randPriority, "Auto-generated payload", randSize));
        }

        return generatedPackets;
    }
};

#endif