#ifndef PACKET_PARSER_HPP
#define PACKET_PARSER_HPP

#include "packet.hpp"
#include "logger.hpp"
#include <memory>
#include <sstream>
#include <string>

class PacketParser {
private:
    static Priority stringToPriority(const std::string& str) {
        if (str == "CRITICAL") return Priority::CRITICAL;
        if (str == "HIGH") return Priority::HIGH;
        if (str == "MEDIUM") return Priority::MEDIUM;
        return Priority::LOW;
    }

public:
    static std::unique_ptr<Packet> parse(const std::string& rawData) {
        std::stringstream ss(rawData);
        std::string idStr, prioStr, sizeStr, payload;

        try {
            std::getline(ss, idStr, ',');
            std::getline(ss, prioStr, ',');
            std::getline(ss, sizeStr, ',');
            std::getline(ss, payload);

            int id = std::stoi(idStr);
            Priority prio = stringToPriority(prioStr);
            size_t size = std::stoull(sizeStr);

            return std::make_unique<Packet>(id, prio, payload, size);
        }
        catch (const std::exception& e) {
            Logger::log("[WARNING] Network Parse Error. Dropping corrupted packet: " + rawData);
            return nullptr;
        }
    }
};

#endif