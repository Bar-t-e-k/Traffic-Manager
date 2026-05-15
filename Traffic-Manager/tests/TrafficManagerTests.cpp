#include <gtest/gtest.h>
#include "../packet.hpp"
#include "../trafficManager.hpp"
#include "../packetParser.hpp"

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

// TEST 3
TEST(TrafficManagerTest, SortsPacketsByPriority) {
    TrafficManager manager;

	// Wrong order: LOW, CRITICAL, HIGH
    manager.addPacket(std::make_unique<Packet>(1, Priority::LOW, "A", 100));
    manager.addPacket(std::make_unique<Packet>(2, Priority::CRITICAL, "B", 100));
    manager.addPacket(std::make_unique<Packet>(3, Priority::HIGH, "C", 100));

    manager.sortPackets();

	// Expected order after sorting: CRITICAL, HIGH, LOW
    EXPECT_EQ(manager.getPacketPriorityAt(0), Priority::CRITICAL);
    EXPECT_EQ(manager.getPacketPriorityAt(1), Priority::HIGH);
    EXPECT_EQ(manager.getPacketPriorityAt(2), Priority::LOW);
}

// TEST 4
TEST(PacketParserTest, ParsesValidNetworkString) {
	// Simulate receiving a well-formed packet string from the network
    std::string rawData = "105,HIGH,1500,VideoData";

    auto packet = PacketParser::parse(rawData);

    ASSERT_NE(packet, nullptr);
    EXPECT_EQ(packet->getId(), 105);
    EXPECT_EQ(packet->getPriority(), Priority::HIGH);
    EXPECT_EQ(packet->getSize(), 1500);
}

// TEST 5
TEST(PacketParserTest, ReturnsNullOnInvalidString) {
	// Corrupted packet string that doesn't follow the expected format
    std::string badData = "To_nie_jest_pakiet";

    auto packet = PacketParser::parse(badData);

    EXPECT_EQ(packet, nullptr);
}