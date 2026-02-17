package Services;

import Entities.Car;
import Utils.Mydatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Fleet cars with 1-to-1 assignment to delivery men.
 * Uses dedicated table "fleet_car" with fixed schema so add/assign never fail.
 */
public class CarService {

    private static final String TABLE = "fleet_car";

    private static void ensureTable() throws SQLException {
        try (Connection conn = Mydatabase.getInstance().getConnection();
             Statement st = conn.createStatement()) {
            st.execute(
                    "CREATE TABLE IF NOT EXISTS " + TABLE + " (" +
                            "car_id BIGINT NOT NULL PRIMARY KEY," +
                            "make VARCHAR(128) NOT NULL DEFAULT ''," +
                            "model VARCHAR(128) NOT NULL DEFAULT ''," +
                            "license_plate VARCHAR(64) NOT NULL DEFAULT ''," +
                            "vehicle_type VARCHAR(64) NOT NULL DEFAULT 'Sedan'," +
                            "delivery_man_id BIGINT NULL," +
                            "UNIQUE KEY uk_fleet_delivery_man (delivery_man_id)" +
                            ")"
            );
        }
    }

    private long nextId(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COALESCE(MAX(car_id), 0) + 1 FROM " + TABLE)) {
            return rs.next() ? rs.getLong(1) : 1L;
        }
    }

    public Car createCar(String make, String model, String licensePlate, String vehicleType) throws SQLException {
        ensureTable();
        try (Connection conn = Mydatabase.getInstance().getConnection()) {
            long id = nextId(conn);
            String sql = "INSERT INTO " + TABLE + " (car_id, make, model, license_plate, vehicle_type, delivery_man_id) VALUES (?, ?, ?, ?, ?, NULL)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, id);
                ps.setString(2, make != null ? make.trim() : "");
                ps.setString(3, model != null ? model.trim() : "");
                ps.setString(4, licensePlate != null ? licensePlate.trim().toUpperCase() : "");
                ps.setString(5, vehicleType != null && !vehicleType.trim().isEmpty() ? vehicleType.trim() : "Sedan");
                ps.executeUpdate();
            }
            Car c = new Car(make, model, licensePlate, vehicleType);
            c.setCarId(id);
            return c;
        }
    }

    public List<Car> getAllCars() throws SQLException {
        ensureTable();
        List<Car> list = new ArrayList<>();
        String sql = "SELECT car_id, make, model, license_plate, vehicle_type, delivery_man_id FROM " + TABLE + " ORDER BY make, model";
        try (Connection conn = Mydatabase.getInstance().getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<Car> getAvailableCars() throws SQLException {
        List<Car> out = new ArrayList<>();
        for (Car c : getAllCars()) if (!c.isAssigned()) out.add(c);
        return out;
    }

    public Car getCarById(Long carId) throws SQLException {
        if (carId == null) return null;
        String sql = "SELECT car_id, make, model, license_plate, vehicle_type, delivery_man_id FROM " + TABLE + " WHERE car_id = ?";
        try (Connection conn = Mydatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, carId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        }
        return null;
    }

    public Car getCarByDeliveryManId(Long deliveryManId) throws SQLException {
        if (deliveryManId == null) return null;
        String sql = "SELECT car_id, make, model, license_plate, vehicle_type, delivery_man_id FROM " + TABLE + " WHERE delivery_man_id = ?";
        try (Connection conn = Mydatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, deliveryManId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        }
        return null;
    }

    public void assignCarToDeliveryMan(Long carId, Long deliveryManId) throws SQLException {
        ensureTable();
        try (Connection conn = Mydatabase.getInstance().getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement("UPDATE " + TABLE + " SET delivery_man_id = NULL WHERE delivery_man_id = ?")) {
                    ps.setLong(1, deliveryManId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement("UPDATE " + TABLE + " SET delivery_man_id = ? WHERE car_id = ?")) {
                    ps.setLong(1, deliveryManId);
                    ps.setLong(2, carId);
                    ps.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public void updateCar(Long carId, String make, String model, String licensePlate, String vehicleType) throws SQLException {
        String sql = "UPDATE " + TABLE + " SET make = ?, model = ?, license_plate = ?, vehicle_type = ? WHERE car_id = ?";
        try (Connection conn = Mydatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, make != null ? make.trim() : "");
            ps.setString(2, model != null ? model.trim() : "");
            ps.setString(3, licensePlate != null ? licensePlate.trim().toUpperCase() : "");
            ps.setString(4, vehicleType != null && !vehicleType.trim().isEmpty() ? vehicleType.trim() : "Sedan");
            ps.setLong(5, carId);
            ps.executeUpdate();
        }
    }

    public void unassignCar(Long carId) throws SQLException {
        String sql = "UPDATE " + TABLE + " SET delivery_man_id = NULL WHERE car_id = ?";
        try (Connection conn = Mydatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, carId);
            ps.executeUpdate();
        }
    }

    public void deleteCar(Long carId) throws SQLException {
        try (Connection conn = Mydatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM " + TABLE + " WHERE car_id = ?")) {
            ps.setLong(1, carId);
            ps.executeUpdate();
        }
    }

    private static Car map(ResultSet rs) throws SQLException {
        Car c = new Car();
        c.setCarId(rs.getLong("car_id"));
        c.setMake(rs.getString("make"));
        c.setModel(rs.getString("model"));
        c.setLicensePlate(rs.getString("license_plate"));
        c.setVehicleType(rs.getString("vehicle_type"));
        Object dm = rs.getObject("delivery_man_id");
        c.setDeliveryManId(dm != null ? ((Number) dm).longValue() : null);
        return c;
    }
}
