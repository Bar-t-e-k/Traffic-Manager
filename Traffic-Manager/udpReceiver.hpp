#ifndef UDP_RECEIVER_HPP
#define UDP_RECEIVER_HPP

#include "logger.hpp"
#include <string>
#include <stdexcept>
#include <vector>

#ifdef _WIN32
#include <winsock2.h>
#include <ws2tcpip.h>
#pragma comment(lib, "ws2_32.lib") 
#else
#include <sys/socket.h>
#include <arpa/inet.h>
#include <unistd.h>
#endif

class UdpReceiver {
private:
#ifdef _WIN32
    SOCKET sockfd;
#else
    int sockfd;
#endif

public:
    UdpReceiver(int port) {
#ifdef _WIN32
        WSADATA wsaData;
        if (WSAStartup(MAKEWORD(2, 2), &wsaData) != 0) {
            throw std::runtime_error("WSAStartup failed. Network initialization error.");
        }
#endif

        sockfd = socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP);

#ifdef _WIN32
        if (sockfd == INVALID_SOCKET)
#else
        if (sockfd < 0)
#endif
        {
            throw std::runtime_error("Failed to create UDP socket.");
        }

        sockaddr_in servaddr{};
        servaddr.sin_family = AF_INET;
        servaddr.sin_addr.s_addr = INADDR_ANY;
        servaddr.sin_port = htons(port);

        if (bind(sockfd, reinterpret_cast<sockaddr*>(&servaddr), sizeof(servaddr)) < 0) {
            throw std::runtime_error("Bind failed. Port " + std::to_string(port) + " might be in use.");
        }

        Logger::log("[NETWORK] UDP Server actively listening on port " + std::to_string(port));
    }

    ~UdpReceiver() {
#ifdef _WIN32
        if (sockfd != INVALID_SOCKET) closesocket(sockfd);
        WSACleanup();
#else
        if (sockfd >= 0) close(sockfd);
#endif
        Logger::log("[NETWORK] UDP Socket closed securely.");
    }

    UdpReceiver(const UdpReceiver&) = delete;
    UdpReceiver& operator=(const UdpReceiver&) = delete;

    std::string receivePacketData() {
        char buffer[1024];
        sockaddr_in cliaddr{};

#ifdef _WIN32
        int len = sizeof(cliaddr);
        int n = recvfrom(sockfd, buffer, sizeof(buffer) - 1, 0, reinterpret_cast<sockaddr*>(&cliaddr), &len);
#else
        socklen_t len = sizeof(cliaddr);
        int n = recvfrom(sockfd, buffer, sizeof(buffer) - 1, MSG_WAITALL, reinterpret_cast<sockaddr*>(&cliaddr), &len);
#endif

        if (n < 0) return "";

        buffer[n] = '\0';
        return std::string(buffer);
    }
};

#endif