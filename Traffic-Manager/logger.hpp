#ifndef LOGGER_HPP
#define LOGGER_HPP

#include <iostream>
#include <fstream>
#include <string>
#include <ctime>

class Logger {
public:
    static void log(const std::string& message) {
        std::cout << message << "\n";

        std::ofstream file("simulator_logs.txt", std::ios::app);
        if (file.is_open()) {
            std::time_t now = std::time(nullptr);
            char timeStr[26];

            #ifdef _WIN32
                ctime_s(timeStr, sizeof(timeStr), &now);
            #else
                ctime_r(&now, timeStr);
            #endif

            std::string t(timeStr);
            if (!t.empty() && t.back() == '\n') t.pop_back();

            file << "[" << t << "] " << message << "\n";
        }
    }
};

#endif