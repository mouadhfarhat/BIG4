package test;

import Entities.Fooddonationevent;
import Entities.FoodDonationItem;
import Services.Fooddonationeventservice;
import Services.FoodDonationItemService;
import org.junit.jupiter.api.*;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit Test Suite for Food Donation Module
 * Tests CRUD operations for Events and Items
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Fooddonationtest {

    private static Fooddonationeventservice eventService;
    private static FoodDonationItemService itemService;
    private static Integer testEventId;
    private static final String TEST_CHARITY = "JUnit Test Charity";

    @BeforeAll
    static void setup() {
        System.out.println("========================================");
        System.out.println("  FOOD DONATION MODULE - JUNIT TESTS");
        System.out.println("========================================\n");

        eventService = new Fooddonationeventservice();
        itemService = new FoodDonationItemService();
    }

    @AfterAll
    static void cleanup() {
        System.out.println("\n========================================");
        System.out.println("  ALL JUNIT TESTS COMPLETED");
        System.out.println("========================================");
    }

    // ==================== EVENT TESTS ====================

    @Test
    @Order(1)
    @DisplayName("Test 1: Create Food Donation Event")
    void testCreateEvent() {
        System.out.println("\nTEST 1: CREATE FOOD DONATION EVENT");
        System.out.println("-----------------------------------");

        try {
            Fooddonationevent event = new Fooddonationevent();
            event.setEventDate(Date.valueOf(LocalDate.now().plusDays(7)));
            event.setTotalQuantity(150);
            event.setCharityName(TEST_CHARITY);
            event.setStatus("PENDING");

            eventService.addFoodDonationEvent(event);
            assertNotNull(event.getDonationEventId(), "Event ID should be generated");

            testEventId = event.getDonationEventId();
            System.out.println("✓ Event created with ID: " + testEventId);
            System.out.println("✓ Charity: " + event.getCharityName());
            System.out.println("✓ Status: " + event.getStatus());
            System.out.println("✓ TEST PASSED\n");

        } catch (SQLException e) {
            fail("Failed to create event: " + e.getMessage());
        }
    }

    @Test
    @Order(2)
    @DisplayName("Test 2: Read Food Donation Event by ID")
    void testReadEventById() {
        System.out.println("\nTEST 2: READ FOOD DONATION EVENT BY ID");
        System.out.println("---------------------------------------");

        try {
            Fooddonationevent event = eventService.getFoodDonationEventById(testEventId);

            assertNotNull(event, "Event should exist");
            assertEquals(TEST_CHARITY, event.getCharityName(), "Charity name should match");
            assertEquals("PENDING", event.getStatus(), "Status should be PENDING");
            assertEquals(150, event.getTotalQuantity(), "Quantity should be 150");

            System.out.println("✓ Event retrieved successfully");
            System.out.println("  - ID: " + event.getDonationEventId());
            System.out.println("  - Charity: " + event.getCharityName());
            System.out.println("  - Date: " + event.getEventDate());
            System.out.println("  - Quantity: " + event.getTotalQuantity());
            System.out.println("✓ TEST PASSED\n");

        } catch (SQLException e) {
            fail("Failed to read event: " + e.getMessage());
        }
    }

    @Test
    @Order(3)
    @DisplayName("Test 3: Read All Food Donation Events")
    void testReadAllEvents() {
        System.out.println("\nTEST 3: READ ALL FOOD DONATION EVENTS");
        System.out.println("--------------------------------------");

        try {
            List<Fooddonationevent> events = eventService.getAllFoodDonationEvents();

            assertNotNull(events, "Events list should not be null");
            assertFalse(events.isEmpty(), "Events list should not be empty");

            System.out.println("✓ Retrieved " + events.size() + " events:");
            for (Fooddonationevent event : events) {
                System.out.println("  - ID: " + event.getDonationEventId() +
                        ", Charity: " + event.getCharityName() +
                        ", Status: " + event.getStatus());
            }
            System.out.println("✓ TEST PASSED\n");

        } catch (SQLException e) {
            fail("Failed to read all events: " + e.getMessage());
        }
    }

    @Test
    @Order(4)
    @DisplayName("Test 4: Update Food Donation Event")
    void testUpdateEvent() {
        System.out.println("\nTEST 4: UPDATE FOOD DONATION EVENT");
        System.out.println("-----------------------------------");

        try {
            Fooddonationevent event = eventService.getFoodDonationEventById(testEventId);
            event.setStatus("SCHEDULED");
            event.setTotalQuantity(200);

            eventService.updateFoodDonationEvent(event);

            Fooddonationevent updated = eventService.getFoodDonationEventById(testEventId);
            assertEquals("SCHEDULED", updated.getStatus(), "Status should be updated to SCHEDULED");
            assertEquals(200, updated.getTotalQuantity(), "Quantity should be updated to 200");

            System.out.println("✓ Event updated successfully");
            System.out.println("  - New Status: " + updated.getStatus());
            System.out.println("  - New Quantity: " + updated.getTotalQuantity());
            System.out.println("✓ TEST PASSED\n");

        } catch (SQLException e) {
            fail("Failed to update event: " + e.getMessage());
        }
    }

    @Test
    @Order(5)
    @DisplayName("Test 5: Get Events by Status")
    void testGetEventsByStatus() {
        System.out.println("\nTEST 5: GET EVENTS BY STATUS");
        System.out.println("-----------------------------");

        try {
            List<Fooddonationevent> scheduledEvents = eventService.getFoodDonationEventsByStatus("SCHEDULED");

            assertNotNull(scheduledEvents, "Scheduled events list should not be null");
            assertTrue(scheduledEvents.stream()
                            .anyMatch(e -> e.getDonationEventId().equals(testEventId)),
                    "Test event should be in scheduled events");

            System.out.println("✓ Found " + scheduledEvents.size() + " SCHEDULED events");
            System.out.println("✓ TEST PASSED\n");

        } catch (SQLException e) {
            fail("Failed to get events by status: " + e.getMessage());
        }
    }

    @Test
    @Order(6)
    @DisplayName("Test 6: Event Statistics")
    void testEventStatistics() {
        System.out.println("\nTEST 6: EVENT STATISTICS");
        System.out.println("------------------------");

        try {
            int totalEvents = eventService.countAllEvents();
            int totalQuantity = eventService.getTotalQuantityDonated();

            assertTrue(totalEvents > 0, "Total events should be greater than 0");
            assertTrue(totalQuantity > 0, "Total quantity should be greater than 0");

            System.out.println("✓ Total Events: " + totalEvents);
            System.out.println("✓ Total Quantity Donated: " + totalQuantity);
            System.out.println("✓ TEST PASSED\n");

        } catch (SQLException e) {
            fail("Failed to get statistics: " + e.getMessage());
        }
    }

    // ==================== ITEM TESTS ====================

    @Test
    @Order(7)
    @DisplayName("Test 7: Create Food Donation Item")
    void testCreateItem() {
        System.out.println("\nTEST 7: CREATE FOOD DONATION ITEM");
        System.out.println("----------------------------------");

        try {
            FoodDonationItem item = new FoodDonationItem();
            item.setDonationEventId(testEventId);
            item.setItemId(1); // Assuming dish ID 1 exists
            item.setQuantity(50);

            itemService.addFoodDonationItem(item);

            System.out.println("✓ Item created successfully");
            System.out.println("  - Event ID: " + item.getDonationEventId());
            System.out.println("  - Dish ID: " + item.getItemId());
            System.out.println("  - Quantity: " + item.getQuantity());
            System.out.println("✓ TEST PASSED\n");

        } catch (SQLException e) {
            fail("Failed to create item: " + e.getMessage());
        }
    }

    @Test
    @Order(8)
    @DisplayName("Test 8: Read Food Donation Item")
    void testReadItem() {
        System.out.println("\nTEST 8: READ FOOD DONATION ITEM");
        System.out.println("--------------------------------");

        try {
            FoodDonationItem item = itemService.getItemByIds(testEventId, 1);

            assertNotNull(item, "Item should exist");
            assertEquals(testEventId, item.getDonationEventId(), "Event ID should match");
            assertEquals(1, item.getItemId(), "Dish ID should match");
            assertEquals(50, item.getQuantity(), "Quantity should be 50");

            System.out.println("✓ Item retrieved successfully");
            System.out.println("  - Event ID: " + item.getDonationEventId());
            System.out.println("  - Dish ID: " + item.getItemId());
            System.out.println("  - Quantity: " + item.getQuantity());
            System.out.println("✓ TEST PASSED\n");

        } catch (SQLException e) {
            fail("Failed to read item: " + e.getMessage());
        }
    }

    @Test
    @Order(9)
    @DisplayName("Test 9: Update Food Donation Item")
    void testUpdateItem() {
        System.out.println("\nTEST 9: UPDATE FOOD DONATION ITEM");
        System.out.println("----------------------------------");

        try {
            FoodDonationItem item = itemService.getItemByIds(testEventId, 1);
            item.setQuantity(75);

            itemService.updateFoodDonationItem(item);

            FoodDonationItem updated = itemService.getItemByIds(testEventId, 1);
            assertEquals(75, updated.getQuantity(), "Quantity should be updated to 75");

            System.out.println("✓ Item quantity updated successfully");
            System.out.println("  - New Quantity: " + updated.getQuantity());
            System.out.println("✓ TEST PASSED\n");

        } catch (SQLException e) {
            fail("Failed to update item: " + e.getMessage());
        }
    }

    @Test
    @Order(10)
    @DisplayName("Test 10: Get Items by Event")
    void testGetItemsByEvent() {
        System.out.println("\nTEST 10: GET ITEMS BY EVENT");
        System.out.println("----------------------------");

        try {
            List<FoodDonationItem> items = itemService.getItemsByEventId(testEventId);

            assertNotNull(items, "Items list should not be null");
            assertFalse(items.isEmpty(), "Items list should not be empty");

            System.out.println("✓ Found " + items.size() + " items for event " + testEventId);
            for (FoodDonationItem item : items) {
                System.out.println("  - Dish ID: " + item.getItemId() +
                        ", Quantity: " + item.getQuantity());
            }
            System.out.println("✓ TEST PASSED\n");

        } catch (SQLException e) {
            fail("Failed to get items by event: " + e.getMessage());
        }
    }

    @Test
    @Order(11)
    @DisplayName("Test 11: Item Statistics")
    void testItemStatistics() {
        System.out.println("\nTEST 11: ITEM STATISTICS");
        System.out.println("------------------------");

        try {
            int totalItems = itemService.countAllItems();
            int eventItemCount = itemService.countItemsForEvent(testEventId);
            int eventTotalQty = itemService.getTotalQuantityForEvent(testEventId);

            assertTrue(totalItems > 0, "Total items should be greater than 0");
            assertTrue(eventItemCount > 0, "Event items should be greater than 0");
            assertTrue(eventTotalQty > 0, "Event quantity should be greater than 0");

            System.out.println("✓ Total Items (all events): " + totalItems);
            System.out.println("✓ Items in test event: " + eventItemCount);
            System.out.println("✓ Total quantity in test event: " + eventTotalQty);
            System.out.println("✓ TEST PASSED\n");

        } catch (SQLException e) {
            fail("Failed to get item statistics: " + e.getMessage());
        }
    }

    @Test
    @Order(12)
    @DisplayName("Test 12: Check Item Exists")
    void testItemExists() {
        System.out.println("\nTEST 12: CHECK ITEM EXISTS");
        System.out.println("--------------------------");

        try {
            boolean exists = itemService.itemExists(testEventId, 1);
            assertTrue(exists, "Item should exist");

            boolean notExists = itemService.itemExists(testEventId, 999);
            assertFalse(notExists, "Non-existent item should return false");

            System.out.println("✓ Item existence check works correctly");
            System.out.println("✓ TEST PASSED\n");

        } catch (SQLException e) {
            fail("Failed to check item existence: " + e.getMessage());
        }
    }

    @Test
    @Order(13)
    @DisplayName("Test 13: Increment Item Quantity")
    void testIncrementQuantity() {
        System.out.println("\nTEST 13: INCREMENT ITEM QUANTITY");
        System.out.println("---------------------------------");

        try {
            FoodDonationItem before = itemService.getItemByIds(testEventId, 1);
            int originalQty = before.getQuantity();

            itemService.incrementItemQuantity(testEventId, 1, 25);

            FoodDonationItem after = itemService.getItemByIds(testEventId, 1);
            assertEquals(originalQty + 25, after.getQuantity(),
                    "Quantity should be incremented by 25");

            System.out.println("✓ Quantity incremented successfully");
            System.out.println("  - Before: " + originalQty);
            System.out.println("  - After: " + after.getQuantity());
            System.out.println("✓ TEST PASSED\n");

        } catch (SQLException e) {
            fail("Failed to increment quantity: " + e.getMessage());
        }
    }

    @Test
    @Order(14)
    @DisplayName("Test 14: Decrement Item Quantity")
    void testDecrementQuantity() {
        System.out.println("\nTEST 14: DECREMENT ITEM QUANTITY");
        System.out.println("---------------------------------");

        try {
            FoodDonationItem before = itemService.getItemByIds(testEventId, 1);
            int originalQty = before.getQuantity();

            itemService.decrementItemQuantity(testEventId, 1, 10);

            FoodDonationItem after = itemService.getItemByIds(testEventId, 1);
            assertEquals(originalQty - 10, after.getQuantity(),
                    "Quantity should be decremented by 10");

            System.out.println("✓ Quantity decremented successfully");
            System.out.println("  - Before: " + originalQty);
            System.out.println("  - After: " + after.getQuantity());
            System.out.println("✓ TEST PASSED\n");

        } catch (SQLException e) {
            fail("Failed to decrement quantity: " + e.getMessage());
        }
    }

    // ==================== CLEANUP TESTS ====================

    @Test
    @Order(15)
    @DisplayName("Test 15: Delete Food Donation Item")
    void testDeleteItem() {
        System.out.println("\nTEST 15: DELETE FOOD DONATION ITEM");
        System.out.println("-----------------------------------");

        try {
            itemService.deleteFoodDonationItem(testEventId, 1);

            FoodDonationItem deleted = itemService.getItemByIds(testEventId, 1);
            assertNull(deleted, "Item should be deleted");

            System.out.println("✓ Item deleted successfully");
            System.out.println("✓ Verified: Item no longer exists");
            System.out.println("✓ TEST PASSED\n");

        } catch (SQLException e) {
            fail("Failed to delete item: " + e.getMessage());
        }
    }

    @Test
    @Order(16)
    @DisplayName("Test 16: Delete Food Donation Event")
    void testDeleteEvent() {
        System.out.println("\nTEST 16: DELETE FOOD DONATION EVENT");
        System.out.println("------------------------------------");

        try {
            eventService.deleteFoodDonationEvent(testEventId);

            Fooddonationevent deleted = eventService.getFoodDonationEventById(testEventId);
            assertNull(deleted, "Event should be deleted");

            System.out.println("✓ Event deleted successfully");
            System.out.println("✓ Verified: Event no longer exists");
            System.out.println("✓ TEST PASSED\n");

        } catch (SQLException e) {
            fail("Failed to delete event: " + e.getMessage());
        }
    }
}