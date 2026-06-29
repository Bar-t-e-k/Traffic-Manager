#ifndef RUDP_SERVER_HPP
#define RUDP_SERVER_HPP

#include "rudpPacket.hpp"
#include "rudpParser.hpp"
#include "rudpRetransmissionManager.hpp"
#include "packetParser.hpp"
#include "crypto.hpp"

#include <sys/socket.h>
#include <netinet/in.h>
#include <unistd.h>
#include <arpa/inet.h>

#include <iostream>
#include <string>
#include <vector>
#include <memory>

class RudpServer {
private:
    int serverFd = -1;                                
    bool isRunning = false;
    uint32_t nextSequenceNumber = 1;                  
    std::unique_ptr<RudpRetransmissionManager> retransmitManager;

    struct sockaddr_in clientAddr {};
    socklen_t clientAddrLen = sizeof(clientAddr);

    void sendRaw(const std::string& message, const struct sockaddr_in& targetAddr) const {
        if (serverFd < 0) return;
        sendto(serverFd, message.c_str(), message.length(), 0,
            (struct sockaddr*)&targetAddr, sizeof(targetAddr));
    }

public:
    RudpServer() {
        retransmitManager = std::make_unique<RudpRetransmissionManager>([this](const RudpPacket& packet) {
            this->sendRaw(packet.serialize(), this->clientAddr);
            });
    }

    ~RudpServer() {
        stop();
    }

    bool start(int port) {
        if (isRunning) return true;

        serverFd = socket(AF_INET, SOCK_DGRAM, 0);
        if (serverFd < 0) {
            std::cerr << "[RUDP Server] Failed to create socket.\n";
            return false;
        }

        struct sockaddr_in serverAddr {};
        serverAddr.sin_family = AF_INET;
        serverAddr.sin_addr.s_addr = INADDR_ANY;
        serverAddr.sin_port = htons(port);

        if (bind(serverFd, (struct sockaddr*)&serverAddr, sizeof(serverAddr)) < 0) {
            std::cerr << "[RUDP Server] Failed to bind socket to port " << port << ".\n";
            close(serverFd);
            serverFd = -1;
            return false;
        }

        isRunning = true;
        retransmitManager->start(); 
        std::cout << "[RUDP Server] Successfully started on port " << port << "...\n";
        return true;
    }

    void stop() {
        if (!isRunning) return;
        isRunning = false;
        retransmitManager->stop();
        if (serverFd >= 0) {
            close(serverFd);
            serverFd = -1;
        }
        std::cout << "[RUDP Server] Stopped.\n";
    }

    std::string receiveAndProcess() {
        if (serverFd < 0 || !isRunning) return "Error: Server not running";

        char buffer[2048];
        ssize_t bytesRead = recvfrom(serverFd, buffer, sizeof(buffer) - 1, 0,
            (struct sockaddr*)&clientAddr, &clientAddrLen);

        if (bytesRead < 0) {
            return "Error: Read socket failed";
        }

        buffer[bytesRead] = '\0';
        std::string rawData(buffer);

        try {
            RudpPacket rudpPacket = RudpParser::parse(rawData);

            if (rudpPacket.getType() == RudpType::ACK) {
                // Halt retransmission for this specific packet
                retransmitManager->acknowledgePacket(rudpPacket.getSequenceNumber());
                return "RUDP [ACK] Received for sequence: " + std::to_string(rudpPacket.getSequenceNumber());
            }
            else {
                // 1. Acknowledge the receipt immediately to stop sender's retransmissions
                RudpPacket ackResponse(RudpType::ACK, rudpPacket.getSequenceNumber());
                sendRaw(ackResponse.serialize(), clientAddr);

                // 2. Cryptographic layer: Peel off the AES encryption
                std::string decryptedPayload = decryptPayload(rudpPacket.getInternalPayload());

                // 3. Business layer: Parse the clear-text payload into a structured packet
                auto businessPacket = PacketParser::parse(decryptedPayload);

                if (!businessPacket) {
                    return "RUDP [DATA] Seq: " + std::to_string(rudpPacket.getSequenceNumber()) +
                        " | Error: Business decode failed.";
                }

                return "RUDP [DATA] Seq: " + std::to_string(rudpPacket.getSequenceNumber()) + "\n" +
                    "Successfully decoded packet:\n" +
                    "- ID: " + std::to_string(businessPacket->getId()) + "\n" +
                    "- Priority: " + businessPacket->getPriorityString() + "\n" +
                    "- Size: " + std::to_string(businessPacket->getSize()) + " B";
            }
        }
        catch (const std::exception& e) {
            return std::string("RUDP Parse Error: ") + e.what();
        }
    }

    void sendReliable(const std::string& businessPayload) {
        if (!isRunning) return;

        RudpPacket packet(RudpType::DATA, nextSequenceNumber, businessPayload);

        retransmitManager->trackPacket(packet);

        sendRaw(packet.serialize(), clientAddr);

        std::cout << "[RUDP Server] Sent reliable packet. Seq: " << nextSequenceNumber << "\n";
        nextSequenceNumber++;
    }
};

#endif