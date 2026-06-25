#ifndef RUDP_PARSER_HPP
#define RUDP_PARSER_HPP

#include "rudpPacket.hpp"
#include <string>
#include <stdexcept>

class RudpParser {
public:
    static RudpPacket parse(const std::string& rawData) {
        size_t firstComma = rawData.find(',');
        if (firstComma == std::string::npos) {
            throw std::invalid_argument("No comma found");
        }

        std::string typeStr = rawData.substr(0, firstComma);
        size_t secondComma = rawData.find(',', firstComma + 1);

        RudpType type = (typeStr == "ACK") ? RudpType::ACK : RudpType::DATA;

        if (type == RudpType::ACK) {
            uint32_t seq = std::stoul(rawData.substr(firstComma + 1));
            return {type, seq};
        }
        else {
            if (secondComma == std::string::npos) {
                throw std::invalid_argument("No payload found in DATA packet");
            }

            uint32_t seq = std::stoul(rawData.substr(firstComma + 1, secondComma - firstComma - 1));
            std::string payload = rawData.substr(secondComma + 1);

            return {type, seq, payload};
        }
    }
};

#endif