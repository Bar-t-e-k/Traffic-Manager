#ifndef LOGGER_HPP
#define LOGGER_HPP

#include <iostream>
#include <fstream>
#include <string>
#include <ctime>

#ifdef __ANDROID__
#include <android/log.h>
#define LOG_TAG "TrafficManager-C++"
#endif

class Logger {
public:
    static void log(const std::string& message) {

#ifdef __ANDROID__
        __android_log_print(ANDROID_LOG_INFO, LOG_TAG, "%s", message.c_str());
#else
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
#endif
    }
};

#endif