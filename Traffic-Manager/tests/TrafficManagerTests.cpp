#include <gtest/gtest.h>
#include "../packet.hpp"
#include "../trafficManager.hpp"

// TEST 1
TEST(PacketTest, ConstructorThrowsExceptionOnZeroSize) {
    EXPECT_THROW({
        Packet p(999, Priority::CRITICAL, "Corrupt payload", 0);
        }, InvalidPacketException);
}

// TEST 2
TEST(TrafficManagerTest, DropsOversizedPacketsCorrectly) {
    // Arrange 
    TrafficManager manager;

    manager.addPacket(std::make_unique<Packet>(1, Priority::LOW, "Good", 500));
    manager.addPacket(std::make_unique<Packet>(2, Priority::HIGH, "Bad", 2000));

    EXPECT_EQ(manager.getQueueSize(), 2);

    // Act 
    manager.dropOversizedPackets(1500); 

    // Assert
    EXPECT_EQ(manager.getQueueSize(), 1);
}