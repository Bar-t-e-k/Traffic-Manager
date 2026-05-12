#ifndef PACKET_HPP
#define PACKET_HPP

#include <string>
#include <iostream>

class InvalidPacketException : public std::runtime_error {
public:
    InvalidPacketException(const std::string& msg) : std::runtime_error(msg) {}
};

enum class Priority {
    LOW = 0,
    MEDIUM,
    HIGH,
    CRITICAL
};

class Packet {
private:
    int id;
    Priority priority;
    std::string payload;
    size_t size;

public:
    Packet(int p_id, Priority p_priority, std::string p_payload, size_t p_size)
        : id(p_id), priority(p_priority), payload(std::move(p_payload)), size(p_size) {

        if (p_size == 0) {
            throw InvalidPacketException("Packet size cannot be 0 bytes! ID: " + std::to_string(p_id));
        }
    }

    ~Packet() {}

    int getId() const { return id; }
    Priority getPriority() const { return priority; }
    size_t getSize() const { return size; }

    std::string getPriorityString() const {
        switch (priority) {
        case Priority::LOW: return "LOW";
        case Priority::MEDIUM: return "MEDIUM";
        case Priority::HIGH: return "HIGH";
        case Priority::CRITICAL: return "CRITICAL";
        default: return "UNKNOWN";
        }
    }
};

#endif