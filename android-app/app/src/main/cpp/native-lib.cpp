#include <jni.h>
#include <string>
#include <stdexcept>
#include "packetParser.hpp"
#include "packet.hpp"
#include "rudpServer.hpp"

std::unique_ptr<RudpServer> g_rudpServer = nullptr;

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

extern "C" JNIEXPORT jboolean JNICALL
Java_com_example_trafficmanagermobile_TrafficEngine_startRudpServerNative(
        JNIEnv* env,
        jobject /* this */,
        jint port) {

    if (!g_rudpServer) {
        g_rudpServer = std::make_unique<RudpServer>();
    }

    return g_rudpServer->start(port) ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_trafficmanagermobile_TrafficEngine_stopRudpServerNative(
        JNIEnv* env,
        jobject /* this */) {

    if (g_rudpServer) {
        g_rudpServer->stop();
        g_rudpServer.reset();
    }
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_trafficmanagermobile_TrafficEngine_receiveRudpPacketNative(
        JNIEnv* env,
        jobject /* this */) {

    if (!g_rudpServer) {
        return env->NewStringUTF("Error: RUDP Server not initialized");
    }

    std::string result = g_rudpServer->receiveAndProcess();

    return env->NewStringUTF(result.c_str());
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_trafficmanagermobile_TrafficEngine_sendRudpPacketNative(
        JNIEnv* env,
        jobject /* this */,
        jstring payload) {

    if (!g_rudpServer) return;

    const char* payloadChars = env->GetStringUTFChars(payload, nullptr);
    std::string cppPayload(payloadChars);
    env->ReleaseStringUTFChars(payload, payloadChars);

    g_rudpServer->sendReliable(cppPayload);
}