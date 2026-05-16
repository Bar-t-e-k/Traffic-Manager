#include <jni.h>
#include <string>
#include <stdexcept>
#include "packetParser.hpp"
#include "packet.hpp"

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_trafficmanagermobile_TrafficEngine_parsePacketNative(
        JNIEnv* env,
        jobject /* this */,
        jstring rawData) {

    const char* rawDataChars = env->GetStringUTFChars(rawData, nullptr);
    std::string cppString(rawDataChars);
    env->ReleaseStringUTFChars(rawData, rawDataChars);

    try {
        auto packet = PacketParser::parse(cppString);

        if (!packet) {
            return env->NewStringUTF("Error: It is not possible to decode the packet. Invalid format.");
        }

        std::string result = "Successfully decoded packet:\n"
                             "- ID: " + std::to_string(packet->getId()) + "\n" +
                "- Priority: " + packet->getPriorityString() + "\n" +
                "- Size: " + std::to_string(packet->getSize()) + " B";

        return env->NewStringUTF(result.c_str());

    } catch (const InvalidPacketException& e) {
        jclass exClass = env->FindClass("java/lang/IllegalArgumentException");
        if (exClass != nullptr) {
            env->ThrowNew(exClass, (std::string("C++ engine error: ") + e.what()).c_str());
        }
        return nullptr;
    } catch (const std::exception& e) {
        jclass exClass = env->FindClass("java/lang/RuntimeException");
        if (exClass != nullptr) {
            env->ThrowNew(exClass, e.what());
        }
        return nullptr;
    }
}