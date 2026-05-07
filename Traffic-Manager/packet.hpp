#ifndef PACKET_HPP
#define PACKET_HPP

#include <string>
#include <iostream>

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
    Packet(int p_id, Priority p_priority, std::string p_payload)
        : id(p_id), priority(p_priority), payload(std::move(p_payload)) {
        std::cout << "[CREATED] Packet ID: " << id << " created.\n";
    }

    ~Packet() {
        std::cout << "[DESTROYED] Packet ID: " << id << " is being deleted from memory.\n";
    }

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