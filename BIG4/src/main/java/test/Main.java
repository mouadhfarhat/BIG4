package test;

import Entities.Delivery;
import Entities.DeliveryMan;
import Services.DeliveryService;
import Services.DeliverymanService;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

/**
 * Combined Test class for DeliveryMan and Delivery CRUD operations
 * This class provides a console-based menu to test all CRUD functionalities for both entities
 */
public class Main {

    private static DeliverymanService deliverymanService;
    private static DeliveryService deliveryService;
    private static Scanner scanner;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) {
        deliverymanService = new DeliverymanService();
        deliveryService = new DeliveryService();
        scanner = new Scanner(System.in);

        printHeader();

        boolean running = true;
        while (running) {
            displayMainMenu();
            int choice = getIntInput("Enter your choice: ");

            try {
                switch (choice) {
                    case 1:
                        deliveryManMenu();
                        break;
                    case 2:
                        deliveryMenu();
                        break;
                    case 3:
                        viewStatistics();
                        break;
                    case 4:
                        testFullWorkflow();
                        break;
                    case 0:
                        running = false;
                        System.out.println("\n" + "=".repeat(80));
                        System.out.println("Thank you for using the Delivery System Testing Platform!");
                        System.out.println("=".repeat(80) + "\n");
                        break;
                    default:
                        System.out.println("\n❌ Invalid choice. Please try again.\n");
                }
            } catch (SQLException e) {
                System.out.println("\n❌ Database Error: " + e.getMessage() + "\n");
                e.printStackTrace();
            } catch (Exception e) {
                System.out.println("\n❌ Error: " + e.getMessage() + "\n");
                e.printStackTrace();
            }

            if (running && choice != 0) {
                System.out.println("\nPress Enter to continue...");
                scanner.nextLine();
            }
        }

        scanner.close();
    }

    /**
     * Print welcome header
     */
    private static void printHeader() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("█████████████████████████████████████████████████████████████████████████████");
        System.out.println("█                                                                           █");
        System.out.println("█          DELIVERY SYSTEM COMPREHENSIVE CRUD TESTING PLATFORM             █");
        System.out.println("█                                                                           █");
        System.out.println("█          Testing: DeliveryMan & Delivery Entities                        █");
        System.out.println("█                                                                           █");
        System.out.println("█████████████████████████████████████████████████████████████████████████████");
        System.out.println("=".repeat(80) + "\n");
    }

    /**
     * Display main menu
     */
    private static void displayMainMenu() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("                              MAIN MENU");
        System.out.println("=".repeat(80));
        System.out.println("1. 🚴 Delivery Man Management (CRUD Operations)");
        System.out.println("2. 📦 Delivery Management (CRUD Operations)");
        System.out.println("3. 📊 View System Statistics");
        System.out.println("4. 🔄 Test Full Workflow (Add DeliveryMan → Create Delivery)");
        System.out.println("0. ❌ Exit");
        System.out.println("=".repeat(80));
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // DELIVERY MAN SECTION
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Delivery Man submenu
     */
    private static void deliveryManMenu() throws SQLException {
        boolean back = false;
        while (!back) {
            displayDeliveryManMenu();
            int choice = getIntInput("Enter your choice: ");

            switch (choice) {
                case 1:
                    testAddDeliveryMan();
                    break;
                case 2:
                    testGetAllDeliveryMen();
                    break;
                case 3:
                    testGetDeliveryManById();
                    break;
                case 4:
                    testGetActiveDeliveryMen();
                    break;
                case 5:
                    testGetDeliveryMenByStatus();
                    break;
                case 6:
                    testUpdateDeliveryMan();
                    break;
                case 7:
                    testUpdateDeliveryManStatus();
                    break;
                case 8:
                    testUpdateDeliveryManRating();
                    break;
                case 9:
                    testDeleteDeliveryMan();
                    break;
                case 10:
                    testCountDeliveryMen();
                    break;
                case 11:
                    testAddMultipleDeliveryMen();
                    break;
                case 0:
                    back = true;
                    break;
                default:
                    System.out.println("\n❌ Invalid choice. Please try again.\n");
            }

            if (!back && choice != 0) {
                System.out.println("\nPress Enter to continue...");
                scanner.nextLine();
            }
        }
    }

    /**
     * Display Delivery Man menu
     */
    private static void displayDeliveryManMenu() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("                      🚴 DELIVERY MAN MANAGEMENT");
        System.out.println("=".repeat(80));
        System.out.println("1.  ➕ Add New Delivery Man (CREATE)");
        System.out.println("2.  📋 Display All Delivery Men (READ)");
        System.out.println("3.  🔍 Find Delivery Man by ID (READ)");
        System.out.println("4.  ✅ Display Active Delivery Men (READ)");
        System.out.println("5.  🔎 Find Delivery Men by Status (READ)");
        System.out.println("6.  ✏️  Update Delivery Man (UPDATE)");
        System.out.println("7.  🔄 Update Delivery Man Status (UPDATE)");
        System.out.println("8.  ⭐ Update Delivery Man Rating (UPDATE)");
        System.out.println("9.  🗑️  Delete Delivery Man (DELETE)");
        System.out.println("10. 📊 Count All Delivery Men");
        System.out.println("11. 📦 Add Multiple Sample Delivery Men");
        System.out.println("0.  ⬅️  Back to Main Menu");
        System.out.println("=".repeat(80));
    }

    /**
     * Test: Add a new delivery man
     */
    private static void testAddDeliveryMan() throws SQLException {
        System.out.println("\n" + "-".repeat(80));
        System.out.println("                     ➕ ADD NEW DELIVERY MAN");
        System.out.println("-".repeat(80));

        DeliveryMan dm = new DeliveryMan();

        System.out.print("Enter name: ");
        dm.setName(scanner.nextLine());

        System.out.print("Enter phone (at least 8 digits): ");
        dm.setPhone(scanner.nextLine());

        System.out.print("Enter email: ");
        dm.setEmail(scanner.nextLine());

        System.out.print("Enter vehicle type (e.g., Motorcycle, Car, Bicycle): ");
        dm.setVehicleType(scanner.nextLine());

        System.out.print("Enter vehicle number: ");
        dm.setVehicleNumber(scanner.nextLine());

        System.out.print("Enter address: ");
        dm.setAddress(scanner.nextLine());

        System.out.print("Enter status (ACTIVE/INACTIVE/ON_LEAVE): ");
        dm.setStatus(scanner.nextLine());

        System.out.print("Enter salary: ");
        dm.setSalary(getDoubleInput());

        System.out.print("Enter rating (0-5): ");
        dm.setRating(getDoubleInput());

        dm.setDateOfJoining(LocalDate.now());

        deliverymanService.addDeliveryMan2(dm);
        System.out.println("\n✅ Delivery man added successfully!");
        System.out.println("-".repeat(80));
    }

    /**
     * Test: Get all delivery men
     */
    private static void testGetAllDeliveryMen() throws SQLException {
        System.out.println("\n" + "-".repeat(80));
        System.out.println("                     📋 ALL DELIVERY MEN");
        System.out.println("-".repeat(80));
        List<DeliveryMan> deliveryMen = deliverymanService.getAllDeliveryMen();

        if (deliveryMen.isEmpty()) {
            System.out.println("No delivery men found in the database.");
        } else {
            System.out.println("Found " + deliveryMen.size() + " delivery men:\n");
            displayDeliveryMenTable(deliveryMen);
        }
        System.out.println("-".repeat(80));
    }

    /**
     * Test: Get delivery man by ID
     */
    private static void testGetDeliveryManById() throws SQLException {
        System.out.println("\n" + "-".repeat(80));
        System.out.println("                     🔍 FIND DELIVERY MAN BY ID");
        System.out.println("-".repeat(80));
        Long id = getLongInput("Enter delivery man ID: ");

        DeliveryMan dm = deliverymanService.getDeliveryManById(id);

        if (dm != null) {
            System.out.println("\n✅ Delivery man found:");
            displayDeliveryManDetails(dm);
        } else {
            System.out.println("\n❌ No delivery man found with ID: " + id);
        }
        System.out.println("-".repeat(80));
    }

    /**
     * Test: Get active delivery men
     */
    private static void testGetActiveDeliveryMen() throws SQLException {
        System.out.println("\n" + "-".repeat(80));
        System.out.println("                     ✅ ACTIVE DELIVERY MEN");
        System.out.println("-".repeat(80));
        List<DeliveryMan> deliveryMen = deliverymanService.getActiveDeliveryMen();

        if (deliveryMen.isEmpty()) {
            System.out.println("No active delivery men found.");
        } else {
            System.out.println("Found " + deliveryMen.size() + " active delivery men:\n");
            displayDeliveryMenTable(deliveryMen);
        }
        System.out.println("-".repeat(80));
    }

    /**
     * Test: Get delivery men by status
     */
    private static void testGetDeliveryMenByStatus() throws SQLException {
        System.out.println("\n" + "-".repeat(80));
        System.out.println("                     🔎 FIND DELIVERY MEN BY STATUS");
        System.out.println("-".repeat(80));
        System.out.print("Enter status (ACTIVE/INACTIVE/ON_LEAVE): ");
        String status = scanner.nextLine();

        List<DeliveryMan> deliveryMen = deliverymanService.getDeliveryMenByStatus(status);

        if (deliveryMen.isEmpty()) {
            System.out.println("No delivery men found with status: " + status);
        } else {
            System.out.println("Found " + deliveryMen.size() + " delivery men with status '" + status + "':\n");
            displayDeliveryMenTable(deliveryMen);
        }
        System.out.println("-".repeat(80));
    }

    /**
     * Test: Update delivery man
     */
    private static void testUpdateDeliveryMan() throws SQLException {
        System.out.println("\n" + "-".repeat(80));
        System.out.println("                     ✏️ UPDATE DELIVERY MAN");
        System.out.println("-".repeat(80));
        Long id = getLongInput("Enter delivery man ID to update: ");

        DeliveryMan dm = deliverymanService.getDeliveryManById(id);

        if (dm == null) {
            System.out.println("\n❌ No delivery man found with ID: " + id);
            System.out.println("-".repeat(80));
            return;
        }

        System.out.println("\nCurrent details:");
        displayDeliveryManDetails(dm);

        System.out.println("\nEnter new details (press Enter to keep current value):");

        System.out.print("Name [" + dm.getName() + "]: ");
        String name = scanner.nextLine();
        if (!name.trim().isEmpty()) dm.setName(name);

        System.out.print("Phone [" + dm.getPhone() + "]: ");
        String phone = scanner.nextLine();
        if (!phone.trim().isEmpty()) dm.setPhone(phone);

        System.out.print("Email [" + dm.getEmail() + "]: ");
        String email = scanner.nextLine();
        if (!email.trim().isEmpty()) dm.setEmail(email);

        System.out.print("Vehicle Type [" + dm.getVehicleType() + "]: ");
        String vehicleType = scanner.nextLine();
        if (!vehicleType.trim().isEmpty()) dm.setVehicleType(vehicleType);

        System.out.print("Vehicle Number [" + dm.getVehicleNumber() + "]: ");
        String vehicleNumber = scanner.nextLine();
        if (!vehicleNumber.trim().isEmpty()) dm.setVehicleNumber(vehicleNumber);

        System.out.print("Address [" + dm.getAddress() + "]: ");
        String address = scanner.nextLine();
        if (!address.trim().isEmpty()) dm.setAddress(address);

        System.out.print("Status [" + dm.getStatus() + "]: ");
        String status = scanner.nextLine();
        if (!status.trim().isEmpty()) dm.setStatus(status);

        System.out.print("Salary [" + dm.getSalary() + "]: ");
        String salaryStr = scanner.nextLine();
        if (!salaryStr.trim().isEmpty()) dm.setSalary(Double.parseDouble(salaryStr));

        System.out.print("Rating [" + dm.getRating() + "]: ");
        String ratingStr = scanner.nextLine();
        if (!ratingStr.trim().isEmpty()) dm.setRating(Double.parseDouble(ratingStr));

        deliverymanService.updateDeliveryMan2(dm);
        System.out.println("\n✅ Delivery man updated successfully!");
        System.out.println("-".repeat(80));
    }

    /**
     * Test: Update delivery man status
     */
    private static void testUpdateDeliveryManStatus() throws SQLException {
        System.out.println("\n" + "-".repeat(80));
        System.out.println("                     🔄 UPDATE DELIVERY MAN STATUS");
        System.out.println("-".repeat(80));
        Long id = getLongInput("Enter delivery man ID: ");

        DeliveryMan dm = deliverymanService.getDeliveryManById(id);
        if (dm == null) {
            System.out.println("\n❌ No delivery man found with ID: " + id);
            System.out.println("-".repeat(80));
            return;
        }

        System.out.println("Current status: " + dm.getStatus());
        System.out.print("Enter new status (ACTIVE/INACTIVE/ON_LEAVE): ");
        String status = scanner.nextLine();

        deliverymanService.updateDeliveryManStatus(id, status);
        System.out.println("\n✅ Status updated successfully!");
        System.out.println("-".repeat(80));
    }

    /**
     * Test: Update delivery man rating
     */
    private static void testUpdateDeliveryManRating() throws SQLException {
        System.out.println("\n" + "-".repeat(80));
        System.out.println("                     ⭐ UPDATE DELIVERY MAN RATING");
        System.out.println("-".repeat(80));
        Long id = getLongInput("Enter delivery man ID: ");

        DeliveryMan dm = deliverymanService.getDeliveryManById(id);
        if (dm == null) {
            System.out.println("\n❌ No delivery man found with ID: " + id);
            System.out.println("-".repeat(80));
            return;
        }

        System.out.println("Current rating: " + dm.getRating());
        Double rating = getDoubleInput("Enter new rating (0-5): ");

        deliverymanService.updateDeliveryManRating(id, rating);
        System.out.println("\n✅ Rating updated successfully!");
        System.out.println("-".repeat(80));
    }

    /**
     * Test: Delete delivery man
     */
    private static void testDeleteDeliveryMan() throws SQLException {
        System.out.println("\n" + "-".repeat(80));
        System.out.println("                     🗑️ DELETE DELIVERY MAN");
        System.out.println("-".repeat(80));
        Long id = getLongInput("Enter delivery man ID to delete: ");

        DeliveryMan dm = deliverymanService.getDeliveryManById(id);
        if (dm == null) {
            System.out.println("\n❌ No delivery man found with ID: " + id);
            System.out.println("-".repeat(80));
            return;
        }

        System.out.println("\nDelivery man to delete:");
        displayDeliveryManDetails(dm);

        System.out.print("\nAre you sure you want to delete this delivery man? (yes/no): ");
        String confirmation = scanner.nextLine();

        if (confirmation.equalsIgnoreCase("yes")) {
            deliverymanService.deleteDeliveryMan(id);
            System.out.println("\n✅ Delivery man deleted successfully!");
        } else {
            System.out.println("\n❌ Deletion cancelled.");
        }
        System.out.println("-".repeat(80));
    }

    /**
     * Test: Count delivery men
     */
    private static void testCountDeliveryMen() throws SQLException {
        System.out.println("\n" + "-".repeat(80));
        System.out.println("                     📊 COUNT DELIVERY MEN");
        System.out.println("-".repeat(80));
        int count = deliverymanService.countDeliveryMen();
        System.out.println("Total number of delivery men: " + count);
        System.out.println("-".repeat(80));
    }

    /**
     * Test: Add multiple sample delivery men
     */
    private static void testAddMultipleDeliveryMen() throws SQLException {
        System.out.println("\n" + "-".repeat(80));
        System.out.println("                     📦 ADD SAMPLE DELIVERY MEN");
        System.out.println("-".repeat(80));

        DeliveryMan[] sampleDeliveryMen = {
                createSampleDeliveryMan("John Smith", "12345678", "john.smith@email.com", "Motorcycle", "MC-001", "ACTIVE", 1500.0, 4.5),
                createSampleDeliveryMan("Alice Johnson", "23456789", "alice.j@email.com", "Car", "CAR-002", "ACTIVE", 2000.0, 4.8),
                createSampleDeliveryMan("Bob Williams", "34567890", "bob.w@email.com", "Bicycle", "BIC-003", "ACTIVE", 1200.0, 4.2),
                createSampleDeliveryMan("Emma Brown", "45678901", "emma.b@email.com", "Motorcycle", "MC-004", "ON_LEAVE", 1550.0, 4.6),
                createSampleDeliveryMan("Michael Davis", "56789012", "michael.d@email.com", "Car", "CAR-005", "INACTIVE", 1800.0, 3.9)
        };

        int addedCount = 0;
        for (DeliveryMan dm : sampleDeliveryMen) {
            try {
                deliverymanService.addDeliveryMan2(dm);
                addedCount++;
                System.out.println("✅ Added: " + dm.getName());
            } catch (SQLException e) {
                System.out.println("❌ Failed to add: " + dm.getName() + " - " + e.getMessage());
            }
        }

        System.out.println("\n✅ Successfully added " + addedCount + " out of " + sampleDeliveryMen.length + " delivery men!");
        System.out.println("-".repeat(80));
    }

    /**
     * Create a sample delivery man
     */
    private static DeliveryMan createSampleDeliveryMan(String name, String phone, String email,
                                                       String vehicleType, String vehicleNumber,
                                                       String status, Double salary, Double rating) {
        DeliveryMan dm = new DeliveryMan();
        dm.setName(name);
        dm.setPhone(phone);
        dm.setEmail(email);
        dm.setVehicleType(vehicleType);
        dm.setVehicleNumber(vehicleNumber);
        dm.setAddress("123 Main Street");
        dm.setStatus(status);
        dm.setSalary(salary);
        dm.setRating(rating);
        dm.setDateOfJoining(LocalDate.now());
        return dm;
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // DELIVERY SECTION
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Delivery submenu
     */
    private static void deliveryMenu() throws SQLException {
        boolean back = false;
        while (!back) {
            displayDeliveryMenu();
            int choice = getIntInput("Enter your choice: ");

            switch (choice) {
                case 1:
                    testAddDelivery();
                    break;
                case 2:
                    testGetAllDeliveries();
                    break;
                case 3:
                    testGetDeliveryById();
                    break;
                case 4:
                    testGetDeliveriesByStatus();
                    break;
                case 5:
                    testGetDeliveriesByDeliveryMan();
                    break;
                case 6:
                    testUpdateDelivery();
                    break;
                case 7:
                    testUpdateDeliveryStatus();
                    break;
                case 8:
                    testSearchDeliveries();
                    break;
                case 9:
                    testDeleteDelivery();
                    break;
                case 10:
                    testAddMultipleDeliveries();
                    break;
                case 0:
                    back = true;
                    break;
                default:
                    System.out.println("\n❌ Invalid choice. Please try again.\n");
            }

            if (!back && choice != 0) {
                System.out.println("\nPress Enter to continue...");
                scanner.nextLine();
            }
        }
    }

    /**
     * Display Delivery menu
     */
    private static void displayDeliveryMenu() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("                      📦 DELIVERY MANAGEMENT");
        System.out.println("=".repeat(80));
        System.out.println("1.  ➕ Add New Delivery (CREATE)");
        System.out.println("2.  📋 Display All Deliveries (READ)");
        System.out.println("3.  🔍 Find Delivery by ID (READ)");
        System.out.println("4.  🔎 Find Deliveries by Status (READ)");
        System.out.println("5.  🚴 Find Deliveries by Delivery Man (READ)");
        System.out.println("6.  ✏️  Update Delivery (UPDATE)");
        System.out.println("7.  🔄 Update Delivery Status (UPDATE)");
        System.out.println("8.  🔎 Search Deliveries");
        System.out.println("9.  🗑️  Delete Delivery (DELETE)");
        System.out.println("10. 📦 Add Multiple Sample Deliveries");
        System.out.println("0.  ⬅️  Back to Main Menu");
        System.out.println("=".repeat(80));
    }

    /**
     * Test: Add a new delivery
     */
    private static void testAddDelivery() throws SQLException {
        System.out.println("\n" + "-".repeat(80));
        System.out.println("                     ➕ ADD NEW DELIVERY");
        System.out.println("-".repeat(80));

        Delivery delivery = new Delivery();

        Long orderId = getLongInput("Enter order ID: ");
        delivery.setOrderId(orderId);

        System.out.print("Enter recipient name: ");
        delivery.setRecipientName(scanner.nextLine());

        System.out.print("Enter recipient phone: ");
        delivery.setRecipientPhone(scanner.nextLine());

        System.out.print("Enter delivery address: ");
        delivery.setDeliveryAddress(scanner.nextLine());

        System.out.print("Enter pickup location (optional): ");
        String pickup = scanner.nextLine();
        if (!pickup.trim().isEmpty()) {
            delivery.setPickupLocation(pickup);
        }

        System.out.print("Enter delivery notes (optional): ");
        String notes = scanner.nextLine();
        if (!notes.trim().isEmpty()) {
            delivery.setDeliveryNotes(notes);
        }

        System.out.print("Assign to delivery man? (yes/no): ");
        String assignChoice = scanner.nextLine();
        if (assignChoice.equalsIgnoreCase("yes")) {
            Long deliveryManId = getLongInput("Enter delivery man ID: ");
            delivery.setDeliveryManId(deliveryManId);
        }

        delivery.setStatus("PENDING");
        delivery.setScheduledDate(LocalDateTime.now());

        boolean success = deliveryService.addDeliveryWithAutoAssignment(delivery);
        if (success) {
            System.out.println("\n✅ Delivery created successfully! ID: " + delivery.getDeliveryId());
        } else {
            System.out.println("\n❌ Failed to create delivery.");
        }
        System.out.println("-".repeat(80));
    }

    /**
     * Test: Get all deliveries
     */
    private static void testGetAllDeliveries() throws SQLException {
        System.out.println("\n" + "-".repeat(80));
        System.out.println("                     📋 ALL DELIVERIES");
        System.out.println("-".repeat(80));
        List<Delivery> deliveries = deliveryService.getAllDeliveries();

        if (deliveries.isEmpty()) {
            System.out.println("No deliveries found in the database.");
        } else {
            System.out.println("Found " + deliveries.size() + " deliveries:\n");
            displayDeliveriesTable(deliveries);
        }
        System.out.println("-".repeat(80));
    }

    /**
     * Test: Get delivery by ID
     */
    private static void testGetDeliveryById() throws SQLException {
        System.out.println("\n" + "-".repeat(80));
        System.out.println("                     🔍 FIND DELIVERY BY ID");
        System.out.println("-".repeat(80));
        Long id = getLongInput("Enter delivery ID: ");

        Delivery delivery = deliveryService.getDeliveryById(id);

        if (delivery != null) {
            System.out.println("\n✅ Delivery found:");
            displayDeliveryDetails(delivery);
        } else {
            System.out.println("\n❌ No delivery found with ID: " + id);
        }
        System.out.println("-".repeat(80));
    }

    /**
     * Test: Get deliveries by status
     */
    private static void testGetDeliveriesByStatus() throws SQLException {
        System.out.println("\n" + "-".repeat(80));
        System.out.println("                     🔎 FIND DELIVERIES BY STATUS");
        System.out.println("-".repeat(80));
        System.out.print("Enter status (PENDING/ON_DELIVERY/DELIVERED/CANCELED): ");
        String status = scanner.nextLine();

        List<Delivery> deliveries = deliveryService.getDeliveriesByStatus(status);

        if (deliveries.isEmpty()) {
            System.out.println("No deliveries found with status: " + status);
        } else {
            System.out.println("Found " + deliveries.size() + " deliveries with status '" + status + "':\n");
            displayDeliveriesTable(deliveries);
        }
        System.out.println("-".repeat(80));
    }

    /**
     * Test: Get deliveries by delivery man
     */
    private static void testGetDeliveriesByDeliveryMan() throws SQLException {
        System.out.println("\n" + "-".repeat(80));
        System.out.println("                     🚴 FIND DELIVERIES BY DELIVERY MAN");
        System.out.println("-".repeat(80));
        Long deliveryManId = getLongInput("Enter delivery man ID: ");

        List<Delivery> deliveries = deliveryService.getDeliveriesByDeliveryMan(deliveryManId);

        if (deliveries.isEmpty()) {
            System.out.println("No deliveries found for delivery man ID: " + deliveryManId);
        } else {
            System.out.println("Found " + deliveries.size() + " deliveries for delivery man ID " + deliveryManId + ":\n");
            displayDeliveriesTable(deliveries);
        }
        System.out.println("-".repeat(80));
    }

    /**
     * Test: Update delivery
     */
    private static void testUpdateDelivery() throws SQLException {
        System.out.println("\n" + "-".repeat(80));
        System.out.println("                     ✏️ UPDATE DELIVERY");
        System.out.println("-".repeat(80));
        Long id = getLongInput("Enter delivery ID to update: ");

        Delivery delivery = deliveryService.getDeliveryById(id);

        if (delivery == null) {
            System.out.println("\n❌ No delivery found with ID: " + id);
            System.out.println("-".repeat(80));
            return;
        }

        System.out.println("\nCurrent details:");
        displayDeliveryDetails(delivery);

        System.out.println("\nEnter new details (press Enter to keep current value):");

        System.out.print("Recipient Name [" + delivery.getRecipientName() + "]: ");
        String name = scanner.nextLine();
        if (!name.trim().isEmpty()) delivery.setRecipientName(name);

        System.out.print("Recipient Phone [" + delivery.getRecipientPhone() + "]: ");
        String phone = scanner.nextLine();
        if (!phone.trim().isEmpty()) delivery.setRecipientPhone(phone);

        System.out.print("Delivery Address [" + delivery.getDeliveryAddress() + "]: ");
        String address = scanner.nextLine();
        if (!address.trim().isEmpty()) delivery.setDeliveryAddress(address);

        System.out.print("Status [" + delivery.getStatus() + "]: ");
        String status = scanner.nextLine();
        if (!status.trim().isEmpty()) delivery.setStatus(status);

        boolean success = deliveryService.updateDelivery(delivery);
        if (success) {
            System.out.println("\n✅ Delivery updated successfully!");
        } else {
            System.out.println("\n❌ Failed to update delivery.");
        }
        System.out.println("-".repeat(80));
    }

    /**
     * Test: Update delivery status
     */
    private static void testUpdateDeliveryStatus() throws SQLException {
        System.out.println("\n" + "-".repeat(80));
        System.out.println("                     🔄 UPDATE DELIVERY STATUS");
        System.out.println("-".repeat(80));
        Long id = getLongInput("Enter delivery ID: ");

        Delivery delivery = deliveryService.getDeliveryById(id);
        if (delivery == null) {
            System.out.println("\n❌ No delivery found with ID: " + id);
            System.out.println("-".repeat(80));
            return;
        }

        System.out.println("Current status: " + delivery.getStatus());
        System.out.print("Enter new status (PENDING/ON_DELIVERY/DELIVERED/CANCELED): ");
        String status = scanner.nextLine();

        boolean success = deliveryService.updateDeliveryStatus(id, status);
        if (success) {
            System.out.println("\n✅ Status updated successfully!");
        } else {
            System.out.println("\n❌ Failed to update status.");
        }
        System.out.println("-".repeat(80));
    }

    /**
     * Test: Search deliveries
     */
    private static void testSearchDeliveries() throws SQLException {
        System.out.println("\n" + "-".repeat(80));
        System.out.println("                     🔎 SEARCH DELIVERIES");
        System.out.println("-".repeat(80));
        System.out.print("Enter search term (name, phone, or ID): ");
        String searchTerm = scanner.nextLine();

        List<Delivery> deliveries = deliveryService.searchDeliveries(searchTerm);

        if (deliveries.isEmpty()) {
            System.out.println("No deliveries found matching: " + searchTerm);
        } else {
            System.out.println("Found " + deliveries.size() + " deliveries matching '" + searchTerm + "':\n");
            displayDeliveriesTable(deliveries);
        }
        System.out.println("-".repeat(80));
    }

    /**
     * Test: Delete delivery
     */
    private static void testDeleteDelivery() throws SQLException {
        System.out.println("\n" + "-".repeat(80));
        System.out.println("                     🗑️ DELETE DELIVERY");
        System.out.println("-".repeat(80));
        Long id = getLongInput("Enter delivery ID to delete: ");

        Delivery delivery = deliveryService.getDeliveryById(id);
        if (delivery == null) {
            System.out.println("\n❌ No delivery found with ID: " + id);
            System.out.println("-".repeat(80));
            return;
        }

        System.out.println("\nDelivery to delete:");
        displayDeliveryDetails(delivery);

        System.out.print("\nAre you sure you want to delete this delivery? (yes/no): ");
        String confirmation = scanner.nextLine();

        if (confirmation.equalsIgnoreCase("yes")) {
            boolean success = deliveryService.deleteDelivery(id);
            if (success) {
                System.out.println("\n✅ Delivery deleted successfully!");
            } else {
                System.out.println("\n❌ Failed to delete delivery.");
            }
        } else {
            System.out.println("\n❌ Deletion cancelled.");
        }
        System.out.println("-".repeat(80));
    }

    /**
     * Test: Add multiple sample deliveries
     */
    private static void testAddMultipleDeliveries() throws SQLException {
        System.out.println("\n" + "-".repeat(80));
        System.out.println("                     📦 ADD SAMPLE DELIVERIES");
        System.out.println("-".repeat(80));

        Delivery[] sampleDeliveries = {
                createSampleDelivery(1001L, "Sarah Connor", "11111111", "123 Tech Street", "Restaurant A", "PENDING"),
                createSampleDelivery(1002L, "Kyle Reese", "22222222", "456 Future Ave", "Restaurant B", "ON_DELIVERY"),
                createSampleDelivery(1003L, "John Connor", "33333333", "789 Resistance Blvd", "Restaurant C", "DELIVERED"),
                createSampleDelivery(1004L, "Ellen Ripley", "44444444", "321 Space Station", "Restaurant D", "PENDING"),
                createSampleDelivery(1005L, "Rick Deckard", "55555555", "654 Blade Runner St", "Restaurant E", "CANCELED")
        };

        int addedCount = 0;
        for (Delivery d : sampleDeliveries) {
            try {
                boolean success = deliveryService.addDeliveryWithAutoAssignment(d);
                if (success) {
                    addedCount++;
                    System.out.println("✅ Added: Delivery for " + d.getRecipientName());
                }
            } catch (SQLException e) {
                System.out.println("❌ Failed to add delivery for: " + d.getRecipientName() + " - " + e.getMessage());
            }
        }

        System.out.println("\n✅ Successfully added " + addedCount + " out of " + sampleDeliveries.length + " deliveries!");
        System.out.println("-".repeat(80));
    }

    /**
     * Create a sample delivery
     */
    private static Delivery createSampleDelivery(Long orderId, String recipientName, String recipientPhone,
                                                 String deliveryAddress, String pickupLocation, String status) {
        Delivery delivery = new Delivery();
        delivery.setOrderId(orderId);
        delivery.setRecipientName(recipientName);
        delivery.setRecipientPhone(recipientPhone);
        delivery.setDeliveryAddress(deliveryAddress);
        delivery.setPickupLocation(pickupLocation);
        delivery.setStatus(status);
        delivery.setScheduledDate(LocalDateTime.now());
        delivery.setDeliveryNotes("Sample delivery");
        return delivery;
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // STATISTICS & WORKFLOW
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * View system statistics
     */
    private static void viewStatistics() throws SQLException {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("                     📊 SYSTEM STATISTICS");
        System.out.println("=".repeat(80));

        // Delivery Men Statistics
        int totalDeliveryMen = deliverymanService.countDeliveryMen();
        List<DeliveryMan> activeDeliveryMen = deliverymanService.getActiveDeliveryMen();
        List<DeliveryMan> allDeliveryMen = deliverymanService.getAllDeliveryMen();

        int inactiveCount = 0;
        int onLeaveCount = 0;
        double totalRating = 0;

        for (DeliveryMan dm : allDeliveryMen) {
            if ("INACTIVE".equals(dm.getStatus())) inactiveCount++;
            if ("ON_LEAVE".equals(dm.getStatus())) onLeaveCount++;
            if (dm.getRating() != null) totalRating += dm.getRating();
        }

        double avgRating = totalDeliveryMen > 0 ? totalRating / totalDeliveryMen : 0;

        // Delivery Statistics
        List<Delivery> allDeliveries = deliveryService.getAllDeliveries();
        int totalDeliveries = allDeliveries.size();
        int pendingDeliveries = (int) allDeliveries.stream().filter(d -> "PENDING".equals(d.getStatus())).count();
        int onDeliveryCount = (int) allDeliveries.stream().filter(d -> "ON_DELIVERY".equals(d.getStatus())).count();
        int deliveredCount = (int) allDeliveries.stream().filter(d -> "DELIVERED".equals(d.getStatus())).count();
        int canceledCount = (int) allDeliveries.stream().filter(d -> "CANCELED".equals(d.getStatus())).count();

        System.out.println("\n🚴 DELIVERY MEN:");
        System.out.println("   Total:         " + totalDeliveryMen);
        System.out.println("   Active:        " + activeDeliveryMen.size());
        System.out.println("   Inactive:      " + inactiveCount);
        System.out.println("   On Leave:      " + onLeaveCount);
        System.out.println("   Avg Rating:    " + String.format("%.2f", avgRating) + "/5.0");

        System.out.println("\n📦 DELIVERIES:");
        System.out.println("   Total:         " + totalDeliveries);
        System.out.println("   Pending:       " + pendingDeliveries);
        System.out.println("   In Transit:    " + onDeliveryCount);
        System.out.println("   Delivered:     " + deliveredCount);
        System.out.println("   Canceled:      " + canceledCount);

        if (totalDeliveries > 0) {
            double completionRate = (deliveredCount * 100.0) / totalDeliveries;
            System.out.println("   Completion:    " + String.format("%.1f", completionRate) + "%");
        }

        System.out.println("=".repeat(80));
    }

    /**
     * Test full workflow
     */
    private static void testFullWorkflow() throws SQLException {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("                     🔄 FULL WORKFLOW TEST");
        System.out.println("=".repeat(80));
        System.out.println("This will:");
        System.out.println("1. Create a new delivery man");
        System.out.println("2. Create a delivery and assign it to the new delivery man");
        System.out.println("3. Update the delivery status to 'DELIVERED'");
        System.out.println();

        System.out.print("Continue? (yes/no): ");
        String confirm = scanner.nextLine();
        if (!confirm.equalsIgnoreCase("yes")) {
            System.out.println("Workflow test cancelled.");
            System.out.println("=".repeat(80));
            return;
        }

        // Step 1: Create delivery man
        System.out.println("\n--- Step 1: Creating Delivery Man ---");
        DeliveryMan dm = createSampleDeliveryMan(
                "Test Driver " + System.currentTimeMillis(),
                "99999999",
                "test@email.com",
                "Motorcycle",
                "TEST-" + System.currentTimeMillis(),
                "ACTIVE",
                1500.0,
                4.5
        );
        deliverymanService.addDeliveryMan2(dm);
        System.out.println("✅ Created delivery man: " + dm.getName());

        // Get the ID (simulate by getting the last delivery man)
        List<DeliveryMan> allDM = deliverymanService.getAllDeliveryMen();
        DeliveryMan createdDM = allDM.get(allDM.size() - 1);
        System.out.println("✅ Delivery Man ID: " + createdDM.getDeliveryManId());

        // Step 2: Create delivery
        System.out.println("\n--- Step 2: Creating Delivery ---");
        Delivery delivery = createSampleDelivery(
                9999L,
                "Test Customer",
                "88888888",
                "Test Address 123",
                "Test Restaurant",
                "PENDING"
        );
        delivery.setDeliveryManId(createdDM.getDeliveryManId());

        boolean success = deliveryService.addDeliveryWithAutoAssignment(delivery);
        if (success) {
            System.out.println("✅ Created delivery ID: " + delivery.getDeliveryId());
        } else {
            System.out.println("❌ Failed to create delivery");
            System.out.println("=".repeat(80));
            return;
        }

        // Step 3: Update status
        System.out.println("\n--- Step 3: Updating Delivery Status ---");
        boolean statusUpdated = deliveryService.updateDeliveryStatus(delivery.getDeliveryId(), "ON_DELIVERY");
        if (statusUpdated) {
            System.out.println("✅ Status updated to: ON_DELIVERY");
        }

        // Wait a moment
        try { Thread.sleep(1000); } catch (InterruptedException e) {}

        statusUpdated = deliveryService.updateDeliveryStatus(delivery.getDeliveryId(), "DELIVERED");
        if (statusUpdated) {
            System.out.println("✅ Status updated to: DELIVERED");
        }

        System.out.println("\n✅ WORKFLOW COMPLETED SUCCESSFULLY!");
        System.out.println("=".repeat(80));
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // DISPLAY HELPERS
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Display delivery men in table format
     */
    private static void displayDeliveryMenTable(List<DeliveryMan> deliveryMen) {
        String line = "-".repeat(148);
        System.out.println(line);
        System.out.printf("| %-4s | %-20s | %-12s | %-25s | %-12s | %-15s | %-10s | %-8s | %-7s |%n",
                "ID", "Name", "Phone", "Email", "Vehicle Type", "Vehicle Number", "Status", "Salary", "Rating");
        System.out.println(line);

        for (DeliveryMan dm : deliveryMen) {
            System.out.printf("| %-4d | %-20s | %-12s | %-25s | %-12s | %-15s | %-10s | $%-7.2f | %-7.1f |%n",
                    dm.getDeliveryManId(),
                    truncate(dm.getName(), 20),
                    dm.getPhone(),
                    truncate(dm.getEmail(), 25),
                    truncate(dm.getVehicleType(), 12),
                    truncate(dm.getVehicleNumber(), 15),
                    dm.getStatus(),
                    dm.getSalary() != null ? dm.getSalary() : 0.0,
                    dm.getRating() != null ? dm.getRating() : 0.0
            );
        }
        System.out.println(line);
    }

    /**
     * Display detailed delivery man information
     */
    private static void displayDeliveryManDetails(DeliveryMan dm) {
        System.out.println("┌─────────────────────────────────────────────┐");
        System.out.println("│      DELIVERY MAN DETAILS                   │");
        System.out.println("└─────────────────────────────────────────────┘");
        System.out.println("  ID:              " + dm.getDeliveryManId());
        System.out.println("  Name:            " + dm.getName());
        System.out.println("  Phone:           " + dm.getPhone());
        System.out.println("  Email:           " + dm.getEmail());
        System.out.println("  Vehicle Type:    " + dm.getVehicleType());
        System.out.println("  Vehicle Number:  " + dm.getVehicleNumber());
        System.out.println("  Address:         " + (dm.getAddress() != null ? dm.getAddress() : "N/A"));
        System.out.println("  Status:          " + dm.getStatus());
        System.out.println("  Salary:          $" + (dm.getSalary() != null ? dm.getSalary() : 0.0));
        System.out.println("  Rating:          " + (dm.getRating() != null ? dm.getRating() : 0.0) + "/5.0");
        System.out.println("  Date of Joining: " + (dm.getDateOfJoining() != null ? dm.getDateOfJoining() : "N/A"));
        System.out.println("─────────────────────────────────────────────");
    }

    /**
     * Display deliveries in table format
     */
    private static void displayDeliveriesTable(List<Delivery> deliveries) {
        String line = "-".repeat(140);
        System.out.println(line);
        System.out.printf("| %-4s | %-8s | %-8s | %-20s | %-12s | %-30s | %-12s | %-19s |%n",
                "ID", "Order ID", "Man ID", "Recipient", "Phone", "Address", "Status", "Created At");
        System.out.println(line);

        for (Delivery d : deliveries) {
            System.out.printf("| %-4d | %-8d | %-8s | %-20s | %-12s | %-30s | %-12s | %-19s |%n",
                    d.getDeliveryId(),
                    d.getOrderId(),
                    d.getDeliveryManId() != null ? d.getDeliveryManId().toString() : "N/A",
                    truncate(d.getRecipientName(), 20),
                    truncate(d.getRecipientPhone(), 12),
                    truncate(d.getDeliveryAddress(), 30),
                    d.getStatus(),
                    d.getCreatedAt() != null ? d.getCreatedAt().format(DATE_TIME_FORMATTER) : "N/A"
            );
        }
        System.out.println(line);
    }

    /**
     * Display detailed delivery information
     */
    private static void displayDeliveryDetails(Delivery d) {
        System.out.println("┌─────────────────────────────────────────────┐");
        System.out.println("│      DELIVERY DETAILS                       │");
        System.out.println("└─────────────────────────────────────────────┘");
        System.out.println("  Delivery ID:        " + d.getDeliveryId());
        System.out.println("  Order ID:           " + d.getOrderId());
        System.out.println("  Delivery Man ID:    " + (d.getDeliveryManId() != null ? d.getDeliveryManId() : "N/A"));
        System.out.println("  Recipient Name:     " + d.getRecipientName());
        System.out.println("  Recipient Phone:    " + d.getRecipientPhone());
        System.out.println("  Delivery Address:   " + d.getDeliveryAddress());
        System.out.println("  Pickup Location:    " + (d.getPickupLocation() != null ? d.getPickupLocation() : "N/A"));
        System.out.println("  Status:             " + d.getStatus());
        System.out.println("  Scheduled Date:     " + (d.getScheduledDate() != null ? d.getScheduledDate().format(DATE_TIME_FORMATTER) : "N/A"));
        System.out.println("  Delivery Notes:     " + (d.getDeliveryNotes() != null ? d.getDeliveryNotes() : "N/A"));
        System.out.println("  Created At:         " + (d.getCreatedAt() != null ? d.getCreatedAt().format(DATE_TIME_FORMATTER) : "N/A"));
        System.out.println("  Updated At:         " + (d.getUpdatedAt() != null ? d.getUpdatedAt().format(DATE_TIME_FORMATTER) : "N/A"));
        System.out.println("─────────────────────────────────────────────");
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // UTILITY METHODS
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Truncate string to specified length
     */
    private static String truncate(String str, int length) {
        if (str == null) return "";
        return str.length() <= length ? str : str.substring(0, length - 3) + "...";
    }

    /**
     * Get integer input from user
     */
    private static int getIntInput(String prompt) {
        System.out.print(prompt);
        while (!scanner.hasNextInt()) {
            scanner.next();
            System.out.print("Invalid input. " + prompt);
        }
        int value = scanner.nextInt();
        scanner.nextLine();
        return value;
    }

    /**
     * Get long input from user
     */
    private static Long getLongInput(String prompt) {
        System.out.print(prompt);
        while (!scanner.hasNextLong()) {
            scanner.next();
            System.out.print("Invalid input. " + prompt);
        }
        Long value = scanner.nextLong();
        scanner.nextLine();
        return value;
    }

    /**
     * Get double input from user
     */
    private static Double getDoubleInput(String prompt) {
        System.out.print(prompt);
        while (!scanner.hasNextDouble()) {
            scanner.next();
            System.out.print("Invalid input. " + prompt);
        }
        Double value = scanner.nextDouble();
        scanner.nextLine();
        return value;
    }

    /**
     * Get double input (overloaded for no prompt)
     */
    private static Double getDoubleInput() {
        while (!scanner.hasNextDouble()) {
            scanner.next();
            System.out.print("Invalid input. Enter a number: ");
        }
        Double value = scanner.nextDouble();
        scanner.nextLine();
        return value;
    }
}