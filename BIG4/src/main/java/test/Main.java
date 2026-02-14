package test;

import Entities.DeliveryMan;
import Entities.Delivery;
import Services.DeliverymanService;
import Services.DeliveryService;

import java.sql.SQLException;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("   DELIVERY APP - DATABASE TEST");
        System.out.println("========================================\n");

        // Test 1: Database Connection
        testDatabaseConnection();

        // Test 2: DeliveryMan CRUD
        testDeliveryManCRUD();

        // Test 3: Delivery CRUD
        testDeliveryCRUD();

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
            System.out.println("  - Database 'delivery_db' exists");
            System.out.println("  - Credentials in MyDB.java are correct");
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
}