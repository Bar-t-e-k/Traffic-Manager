#ifndef RUDP_PACKET_HPP
#define RUDP_PACKET_HPP

#include <string>
#include <stdexcept>
#include "packet.hpp"

enum class RudpType {
    DATA, 
    ACK   
};

class RudpPacket {
private:
    RudpType type;
    uint32_t sequenceNumber;

    std::string internalPayload;

public:
    RudpPacket(RudpType t, uint32_t seq, std::string payload = "")
        : type(t), sequenceNumber(seq), internalPayload(std::move(payload)) {
    }

    [[nodiscard]] RudpType getType() const { return type; }
    [[nodiscard]] uint32_t getSequenceNumber() const { return sequenceNumber; }
    [[nodiscard]] std::string getInternalPayload() const { return internalPayload; }

    [[nodiscard]] std::string serialize() const {
        std::string typeStr = (type == RudpType::DATA) ? "DATA" : "ACK";

        if (type == RudpType::ACK) {
            return typeStr + "," + std::to_string(sequenceNumber);
        }
        else {
            return typeStr + "," + std::to_string(sequenceNumber) + "," + internalPayload;
        }
    }
};

#endif