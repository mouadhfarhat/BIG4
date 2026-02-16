package test;

import Entities.DeliveryMan;
import Entities.Delivery;
import Entities.Fooddonationevent;
import Entities.FoodDonationItem;
import Services.DeliverymanService;
import Services.DeliveryService;
import Services.Fooddonationeventservice;
import Services.FoodDonationItemService;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("   BIG4 RESTAURANT - DATABASE TEST");
        System.out.println("========================================\n");

        // Test 1: Database Connection
        testDatabaseConnection();

        // Test 2: DeliveryMan CRUD
        testDeliveryManCRUD();

        // Test 3: Delivery CRUD
        testDeliveryCRUD();

        // Test 4: Food Donation Events CRUD
        testFoodDonationEventCRUD();

        // Test 5: Food Donation Items CRUD
        testFoodDonationItemCRUD();

        System.out.println("\n========================================");
        System.out.println("   ALL TESTS COMPLETED");
        System.out.println("========================================");
    }

    /**
     * Test 1: Test database connection
     */
    private static void testDatabaseConnection() {
        System.out.println("TEST 1: DATABASE CONNECTION");
        System.out.println("---------------------------");
        try {
            DeliverymanService service = new DeliverymanService();
            int count = service.countDeliveryMen();
            System.out.println("✓ Database connected successfully!");
            System.out.println("✓ Current delivery men count: " + count);
            System.out.println("✓ Connection test PASSED\n");
        } catch (SQLException e) {
            System.out.println("✗ Database connection FAILED!");
            System.out.println("✗ Error: " + e.getMessage());
            System.out.println("✗ Please check:");
            System.out.println("  - MySQL is running");
            System.out.println("  - Database 'project' exists");
            System.out.println("  - Credentials in Mydatabase.java are correct");
            System.out.println();
            return;
        }
    }

    /**
     * Test 2: Test DeliveryMan CRUD operations
     */
    private static void testDeliveryManCRUD() {
        System.out.println("TEST 2: DELIVERY MAN CRUD OPERATIONS");
        System.out.println("------------------------------------");

        DeliverymanService service = new DeliverymanService();
        Long testId = null;

        try {
            // CREATE - Test addDeliveryMan2 (PreparedStatement - safer)
            System.out.println("\n2.1 CREATE - Adding new delivery man...");
            DeliveryMan newDM = new DeliveryMan();
            newDM.setName("Test Delivery Man");
            newDM.setPhone("21699999999");
            newDM.setEmail("test@example.com");
            newDM.setVehicleType("Motorcycle");
            newDM.setVehicleNumber("TN-TEST-001");
            newDM.setStatus("ACTIVE");
            newDM.setAddress("Test Address");
            newDM.setSalary(1500.0);
            newDM.setRating(4.5);

            service.addDeliveryMan2(newDM);
            System.out.println("✓ Delivery man created successfully");

            // READ - Get all delivery men
            System.out.println("\n2.2 READ - Getting all delivery men...");
            List<DeliveryMan> allDeliveryMen = service.getAllDeliveryMen();
            System.out.println("✓ Retrieved " + allDeliveryMen.size() + " delivery men:");
            for (DeliveryMan dm : allDeliveryMen) {
                System.out.println("  - ID: " + dm.getDeliveryManId() + ", Name: " + dm.getName() + ", Phone: " + dm.getPhone() + ", Status: " + dm.getStatus());
                if (dm.getPhone().equals("21699999999")) {
                    testId = dm.getDeliveryManId();
                }
            }

            // READ - Get by ID
            System.out.println("\n2.3 READ - Getting delivery man by ID...");
            if (testId != null) {
                DeliveryMan dm = service.getDeliveryManById(testId);
                if (dm != null) {
                    System.out.println("✓ Found delivery man:");
                    System.out.println("  - Name: " + dm.getName());
                    System.out.println("  - Phone: " + dm.getPhone());
                    System.out.println("  - Vehicle: " + dm.getVehicleType() + " (" + dm.getVehicleNumber() + ")");
                    System.out.println("  - Status: " + dm.getStatus());
                    System.out.println("  - Rating: " + dm.getRating());
                }
            }

            // READ - Get by status
            System.out.println("\n2.4 READ - Getting active delivery men...");
            List<DeliveryMan> activeDM = service.getActiveDeliveryMen();
            System.out.println("✓ Found " + activeDM.size() + " active delivery men");

            // UPDATE - Update delivery man
            System.out.println("\n2.5 UPDATE - Updating delivery man...");
            if (testId != null) {
                DeliveryMan dmToUpdate = service.getDeliveryManById(testId);
                dmToUpdate.setStatus("INACTIVE");
                dmToUpdate.setRating(4.8);
                service.updateDeliveryMan2(dmToUpdate);
                System.out.println("✓ Delivery man updated successfully");

                DeliveryMan updated = service.getDeliveryManById(testId);
                System.out.println("  - New Status: " + updated.getStatus());
                System.out.println("  - New Rating: " + updated.getRating());
            }

            // UPDATE - Update status only
            System.out.println("\n2.6 UPDATE - Updating status only...");
            if (testId != null) {
                service.updateDeliveryManStatus(testId, "ACTIVE");
                DeliveryMan updated = service.getDeliveryManById(testId);
                System.out.println("✓ Status updated to: " + updated.getStatus());
            }

            // DELETE - Delete delivery man
            System.out.println("\n2.7 DELETE - Deleting delivery man...");
            if (testId != null) {
                service.deleteDeliveryMan(testId);
                System.out.println("✓ Delivery man deleted successfully");

                DeliveryMan deleted = service.getDeliveryManById(testId);
                if (deleted == null) {
                    System.out.println("✓ Verified: Delivery man no longer exists");
                }
            }

            System.out.println("\n✓ DeliveryMan CRUD tests PASSED\n");

        } catch (SQLException e) {
            System.out.println("✗ DeliveryMan CRUD tests FAILED!");
            System.out.println("✗ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Test 3: Test Delivery CRUD operations
     */
    private static void testDeliveryCRUD() {
        System.out.println("TEST 3: DELIVERY CRUD OPERATIONS");
        System.out.println("--------------------------------");

        DeliveryService service = new DeliveryService();
        Long testId = null;

        try {
            // CREATE - Add delivery
            System.out.println("\n3.1 CREATE - Adding new delivery...");
            Delivery newDelivery = new Delivery();
            newDelivery.setOrderId(99999L);
            newDelivery.setDeliveryAddress("Test Delivery Address");
            newDelivery.setRecipientName("Test Recipient");
            newDelivery.setRecipientPhone("21699999999");
            newDelivery.setPickupLocation("Test Restaurant");
            newDelivery.setStatus("PENDING");
            newDelivery.setEstimatedTime(45);
            newDelivery.setDeliveryNotes("Test notes");

            service.addDelivery2(newDelivery);
            System.out.println("✓ Delivery created successfully");

            // READ - Get all deliveries
            System.out.println("\n3.2 READ - Getting all deliveries...");
            List<Delivery> allDeliveries = service.getAllDeliveries();
            System.out.println("✓ Retrieved " + allDeliveries.size() + " deliveries:");
            for (Delivery d : allDeliveries) {
                System.out.println("  - ID: " + d.getDeliveryId() + ", Order: " + d.getOrderId() + ", Status: " + d.getStatus());
                if (d.getOrderId() == 99999L) {
                    testId = d.getDeliveryId();
                }
            }

            // READ - Get by ID
            System.out.println("\n3.3 READ - Getting delivery by ID...");
            if (testId != null) {
                Delivery d = service.getDeliveryById(testId);
                if (d != null) {
                    System.out.println("✓ Found delivery:");
                    System.out.println("  - Recipient: " + d.getRecipientName());
                    System.out.println("  - Address: " + d.getDeliveryAddress());
                    System.out.println("  - Status: " + d.getStatus());
                    System.out.println("  - Estimated Time: " + d.getEstimatedTime() + " minutes");
                }
            }

            // READ - Get by order ID
            System.out.println("\n3.4 READ - Getting delivery by order ID...");
            Delivery d = service.getDeliveryByOrderId(99999L);
            if (d != null) {
                System.out.println("✓ Found delivery for order 99999");
            }

            // READ - Get by status
            System.out.println("\n3.5 READ - Getting pending deliveries...");
            List<Delivery> pendingDeliveries = service.getDeliveriesByStatus("PENDING");
            System.out.println("✓ Found " + pendingDeliveries.size() + " pending deliveries");

            // UPDATE - Update delivery
            System.out.println("\n3.6 UPDATE - Updating delivery...");
            if (testId != null) {
                Delivery dToUpdate = service.getDeliveryById(testId);
                dToUpdate.setStatus("ACCEPTED");
                dToUpdate.setEstimatedTime(50);
                service.updateDelivery2(dToUpdate);
                System.out.println("✓ Delivery updated successfully");

                Delivery updated = service.getDeliveryById(testId);
                System.out.println("  - New Status: " + updated.getStatus());
                System.out.println("  - New Estimated Time: " + updated.getEstimatedTime());
            }

            // UPDATE - Update status
            System.out.println("\n3.7 UPDATE - Updating status to ON_THE_WAY...");
            if (testId != null) {
                service.updateDeliveryStatus(testId, "ON_THE_WAY");
                Delivery updated = service.getDeliveryById(testId);
                System.out.println("✓ Status updated to: " + updated.getStatus());
            }

            // UPDATE - Update location
            System.out.println("\n3.8 UPDATE - Updating delivery location...");
            if (testId != null) {
                service.updateDeliveryLocation(testId, 36.8065, 10.1699);
                System.out.println("✓ Location updated (Latitude: 36.8065, Longitude: 10.1699)");
            }

            // UPDATE - Rate delivery
            System.out.println("\n3.9 UPDATE - Rating delivery...");
            if (testId != null) {
                service.rateDelivery(testId, 5);
                Delivery rated = service.getDeliveryById(testId);
                System.out.println("✓ Delivery rated: " + rated.getRating() + " stars");
            }

            // DELETE - Delete delivery
            System.out.println("\n3.10 DELETE - Deleting delivery...");
            if (testId != null) {
                service.deleteDelivery(testId);
                System.out.println("✓ Delivery deleted successfully");

                Delivery deleted = service.getDeliveryById(testId);
                if (deleted == null) {
                    System.out.println("✓ Verified: Delivery no longer exists");
                }
            }

            System.out.println("\n✓ Delivery CRUD tests PASSED\n");

        } catch (SQLException e) {
            System.out.println("✗ Delivery CRUD tests FAILED!");
            System.out.println("✗ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Test 4: Test Food Donation Event CRUD operations
     */
    private static void testFoodDonationEventCRUD() {
        System.out.println("TEST 4: FOOD DONATION EVENT CRUD OPERATIONS");
        System.out.println("-------------------------------------------");

        Fooddonationeventservice service = new Fooddonationeventservice();
        Integer testId = null;

        try {
            // CREATE - Add food donation event
            System.out.println("\n4.1 CREATE - Adding new donation event...");
            Fooddonationevent newEvent = new Fooddonationevent();
            newEvent.setEventDate(Date.valueOf(LocalDate.now().plusDays(7)));
            newEvent.setTotalQuantity(100);
            newEvent.setCharityName("Test Charity Organization");
            newEvent.setStatus("PENDING");
            newEvent.setCalendarEventId("TEST-CAL-001");

            service.addFoodDonationEvent(newEvent);
            System.out.println("✓ Donation event created successfully");

            // READ - Get all donation events
            System.out.println("\n4.2 READ - Getting all donation events...");
            List<Fooddonationevent> allEvents = service.getAllFoodDonationEvents();
            System.out.println("✓ Retrieved " + allEvents.size() + " donation events:");
            for (Fooddonationevent event : allEvents) {
                System.out.println("  - ID: " + event.getDonationEventId() +
                        ", Charity: " + event.getCharityName() +
                        ", Date: " + event.getEventDate() +
                        ", Status: " + event.getStatus());
                if ("Test Charity Organization".equals(event.getCharityName())) {
                    testId = event.getDonationEventId();
                }
            }

            // READ - Get by ID
            System.out.println("\n4.3 READ - Getting donation event by ID...");
            if (testId != null) {
                Fooddonationevent event = service.getFoodDonationEventById(testId);
                if (event != null) {
                    System.out.println("✓ Found donation event:");
                    System.out.println("  - Charity: " + event.getCharityName());
                    System.out.println("  - Date: " + event.getEventDate());
                    System.out.println("  - Total Quantity: " + event.getTotalQuantity());
                    System.out.println("  - Status: " + event.getStatus());
                    System.out.println("  - Calendar ID: " + event.getCalendarEventId());
                }
            }

            // READ - Get by status
            System.out.println("\n4.4 READ - Getting pending donation events...");
            List<Fooddonationevent> pendingEvents = service.getFoodDonationEventsByStatus("PENDING");
            System.out.println("✓ Found " + pendingEvents.size() + " pending donation events");

            // UPDATE - Update donation event
            System.out.println("\n4.5 UPDATE - Updating donation event...");
            if (testId != null) {
                Fooddonationevent eventToUpdate = service.getFoodDonationEventById(testId);
                eventToUpdate.setStatus("SCHEDULED");
                eventToUpdate.setTotalQuantity(150);
                service.updateFoodDonationEvent(eventToUpdate);
                System.out.println("✓ Donation event updated successfully");

                Fooddonationevent updated = service.getFoodDonationEventById(testId);
                System.out.println("  - New Status: " + updated.getStatus());
                System.out.println("  - New Total Quantity: " + updated.getTotalQuantity());
            }

            // UPDATE - Update status only
            System.out.println("\n4.6 UPDATE - Updating status to COMPLETED...");
            if (testId != null) {
                service.updateEventStatus(testId, "COMPLETED");
                Fooddonationevent updated = service.getFoodDonationEventById(testId);
                System.out.println("✓ Status updated to: " + updated.getStatus());
            }

            // STATISTICS
            System.out.println("\n4.7 STATISTICS - Getting donation statistics...");
            int totalEvents = service.countAllEvents();
            int totalQuantity = service.getTotalQuantityDonated();
            System.out.println("✓ Total Events: " + totalEvents);
            System.out.println("✓ Total Quantity Donated: " + totalQuantity);

            // DELETE - Delete donation event
            System.out.println("\n4.8 DELETE - Deleting donation event...");
            if (testId != null) {
                service.deleteFoodDonationEvent(testId);
                System.out.println("✓ Donation event deleted successfully");

                Fooddonationevent deleted = service.getFoodDonationEventById(testId);
                if (deleted == null) {
                    System.out.println("✓ Verified: Donation event no longer exists");
                }
            }

            System.out.println("\n✓ Food Donation Event CRUD tests PASSED\n");

        } catch (SQLException e) {
            System.out.println("✗ Food Donation Event CRUD tests FAILED!");
            System.out.println("✗ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Test 5: Test Food Donation Item CRUD operations
     */
    private static void testFoodDonationItemCRUD() {
        System.out.println("TEST 5: FOOD DONATION ITEM CRUD OPERATIONS");
        System.out.println("------------------------------------------");

        FoodDonationItemService itemService = new FoodDonationItemService();
        Fooddonationeventservice eventService = new Fooddonationeventservice();
        Integer testEventId = null;
        Integer testItemId = 1;

        try {
            // SETUP - Create test event
            System.out.println("\n5.0 SETUP - Creating test event for items...");
            Fooddonationevent testEvent = new Fooddonationevent();
            testEvent.setEventDate(Date.valueOf(LocalDate.now().plusDays(5)));
            testEvent.setTotalQuantity(200);
            testEvent.setCharityName("Test Items Charity");
            testEvent.setStatus("PENDING");

            eventService.addFoodDonationEvent(testEvent);

            List<Fooddonationevent> events = eventService.getAllFoodDonationEvents();
            for (Fooddonationevent e : events) {
                if ("Test Items Charity".equals(e.getCharityName())) {
                    testEventId = e.getDonationEventId();
                    break;
                }
            }
            System.out.println("✓ Test event created with ID: " + testEventId);

            if (testEventId == null) {
                System.out.println("✗ Could not create test event. Skipping item tests.");
                return;
            }

            // CREATE - Add donation item
            System.out.println("\n5.1 CREATE - Adding donation item...");
            FoodDonationItem newItem = new FoodDonationItem();
            newItem.setDonationEventId(testEventId);
            newItem.setItemId(testItemId);
            newItem.setQuantity(50);

            itemService.addFoodDonationItem(newItem);
            System.out.println("✓ Donation item added successfully");

            // CREATE - Add multiple items
            System.out.println("\n5.2 CREATE - Adding multiple items...");
            FoodDonationItem item2 = new FoodDonationItem(testEventId, 2, 75);
            FoodDonationItem item3 = new FoodDonationItem(testEventId, 3, 100);

            itemService.addFoodDonationItem(item2);
            itemService.addFoodDonationItem(item3);
            System.out.println("✓ Additional items added successfully");

            // READ - Get all items
            System.out.println("\n5.3 READ - Getting all donation items...");
            List<FoodDonationItem> allItems = itemService.getAllFoodDonationItems();
            System.out.println("✓ Retrieved " + allItems.size() + " donation items:");
            for (FoodDonationItem item : allItems) {
                System.out.println("  - Event: " + item.getDonationEventId() +
                        ", Item: " + (item.getItemName() != null ? item.getItemName() : "ID " + item.getItemId()) +
                        ", Quantity: " + item.getQuantity());
            }

            // READ - Get items by event
            System.out.println("\n5.4 READ - Getting items for test event...");
            List<FoodDonationItem> eventItems = itemService.getItemsByEventId(testEventId);
            System.out.println("✓ Found " + eventItems.size() + " items for event " + testEventId);

            // READ - Get specific item
            System.out.println("\n5.5 READ - Getting specific item...");
            FoodDonationItem specificItem = itemService.getItemByIds(testEventId, testItemId);
            if (specificItem != null) {
                System.out.println("✓ Found item:");
                System.out.println("  - Event ID: " + specificItem.getDonationEventId());
                System.out.println("  - Item ID: " + specificItem.getItemId());
                System.out.println("  - Quantity: " + specificItem.getQuantity());
            }

            // READ - Check if item exists
            System.out.println("\n5.6 READ - Checking if item exists...");
            boolean exists = itemService.itemExists(testEventId, testItemId);
            System.out.println("✓ Item exists: " + exists);

            // UPDATE - Update item quantity
            System.out.println("\n5.7 UPDATE - Updating item quantity...");
            FoodDonationItem itemToUpdate = itemService.getItemByIds(testEventId, testItemId);
            if (itemToUpdate != null) {
                itemToUpdate.setQuantity(80);
                itemService.updateFoodDonationItem(itemToUpdate);
                System.out.println("✓ Item quantity updated successfully");

                FoodDonationItem updated = itemService.getItemByIds(testEventId, testItemId);
                System.out.println("  - New Quantity: " + updated.getQuantity());
            }

            // UPDATE - Increment quantity
            System.out.println("\n5.8 UPDATE - Incrementing item quantity...");
            itemService.incrementItemQuantity(testEventId, testItemId, 20);
            FoodDonationItem incremented = itemService.getItemByIds(testEventId, testItemId);
            System.out.println("✓ Quantity incremented to: " + incremented.getQuantity());

            // UPDATE - Decrement quantity
            System.out.println("\n5.9 UPDATE - Decrementing item quantity...");
            itemService.decrementItemQuantity(testEventId, testItemId, 10);
            FoodDonationItem decremented = itemService.getItemByIds(testEventId, testItemId);
            System.out.println("✓ Quantity decremented to: " + decremented.getQuantity());

            // STATISTICS
            System.out.println("\n5.10 STATISTICS - Getting item statistics...");
            int totalItems = itemService.countAllItems();
            int eventItemCount = itemService.countItemsForEvent(testEventId);
            int eventTotalQty = itemService.getTotalQuantityForEvent(testEventId);

            System.out.println("✓ Total Items (all events): " + totalItems);
            System.out.println("✓ Items in test event: " + eventItemCount);
            System.out.println("✓ Total quantity in test event: " + eventTotalQty);

            // DELETE - Delete specific item
            System.out.println("\n5.11 DELETE - Deleting specific item...");
            itemService.deleteFoodDonationItem(testEventId, testItemId);
            System.out.println("✓ Item deleted successfully");

            FoodDonationItem deletedItem = itemService.getItemByIds(testEventId, testItemId);
            if (deletedItem == null) {
                System.out.println("✓ Verified: Item no longer exists");
            }

            // DELETE - Delete all items for event
            System.out.println("\n5.12 DELETE - Deleting all items for test event...");
            itemService.deleteItemsByEventId(testEventId);
            System.out.println("✓ All items for event deleted successfully");

            int remainingItems = itemService.countItemsForEvent(testEventId);
            System.out.println("✓ Verified: " + remainingItems + " items remaining for event");

            // CLEANUP - Delete test event
            System.out.println("\n5.13 CLEANUP - Deleting test event...");
            eventService.deleteFoodDonationEvent(testEventId);
            System.out.println("✓ Test event cleaned up successfully");

            System.out.println("\n✓ Food Donation Item CRUD tests PASSED\n");

        } catch (SQLException e) {
            System.out.println("✗ Food Donation Item CRUD tests FAILED!");
            System.out.println("✗ Error: " + e.getMessage());
            e.printStackTrace();

            if (testEventId != null) {
                try {
                    eventService.deleteFoodDonationEvent(testEventId);
                    System.out.println("✓ Cleanup completed");
                } catch (SQLException cleanupError) {
                    System.out.println("✗ Cleanup failed: " + cleanupError.getMessage());
                }
            }
        }
    }
}