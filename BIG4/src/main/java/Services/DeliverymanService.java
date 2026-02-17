package Services;

import Entities.DeliveryMan;
import Utils.Mydatabase;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DeliverymanService {

    private Connection cnx;

    public DeliverymanService() {
        try {
            cnx = Mydatabase.getInstance().getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Add new delivery man (using PreparedStatement - SAFER)
     */
    public void addDeliveryMan2(DeliveryMan deliveryMan) throws SQLException {
        String sql = "INSERT INTO delivery_man(name, phone, email, vehicle_type, vehicle_number, status, address, salary, date_of_joining, rating) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, deliveryMan.getName());
        ps.setString(2, deliveryMan.getPhone());
        ps.setString(3, deliveryMan.getEmail());
        ps.setString(4, deliveryMan.getVehicleType());
        ps.setString(5, deliveryMan.getVehicleNumber());
        ps.setString(6, deliveryMan.getStatus());
        ps.setString(7, deliveryMan.getAddress());
        ps.setDouble(8, deliveryMan.getSalary() != null ? deliveryMan.getSalary() : 0);

        if (deliveryMan.getDateOfJoining() != null) {
            ps.setDate(9, java.sql.Date.valueOf(deliveryMan.getDateOfJoining()));
        } else {
            ps.setDate(9, java.sql.Date.valueOf(LocalDate.now()));
        }

        ps.setDouble(10, deliveryMan.getRating() != null ? deliveryMan.getRating() : 0);
        ps.executeUpdate();
        ps.close();
    }

    /**
     * Add new delivery man and return the generated delivery_man_id.
     * Works even when delivery_man_id has no AUTO_INCREMENT (generates ID in code).
     */
    public Long addDeliveryManAndGetId(DeliveryMan deliveryMan) throws SQLException {
        Long nextId;
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery("SELECT COALESCE(MAX(delivery_man_id), 0) + 1 FROM delivery_man")) {
            if (!rs.next()) return null;
            nextId = rs.getLong(1);
        }
        String sql = "INSERT INTO delivery_man(delivery_man_id, name, phone, email, vehicle_type, vehicle_number, status, address, salary, date_of_joining, rating) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setLong(1, nextId);
        ps.setString(2, deliveryMan.getName());
        ps.setString(3, deliveryMan.getPhone());
        ps.setString(4, deliveryMan.getEmail());
        ps.setString(5, deliveryMan.getVehicleType());
        ps.setString(6, deliveryMan.getVehicleNumber());
        ps.setString(7, deliveryMan.getStatus());
        ps.setString(8, deliveryMan.getAddress());
        ps.setDouble(9, deliveryMan.getSalary() != null ? deliveryMan.getSalary() : 0);
        if (deliveryMan.getDateOfJoining() != null) {
            ps.setDate(10, java.sql.Date.valueOf(deliveryMan.getDateOfJoining()));
        } else {
            ps.setDate(10, java.sql.Date.valueOf(LocalDate.now()));
        }
        ps.setDouble(11, deliveryMan.getRating() != null ? deliveryMan.getRating() : 0);
        ps.executeUpdate();
        ps.close();
        return nextId;
    }

    /**
     * Get delivery man by email
     */
    public DeliveryMan getDeliveryManByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM delivery_man WHERE email = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, email);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            DeliveryMan dm = new DeliveryMan();
            dm.setDeliveryManId(rs.getLong("delivery_man_id"));
            dm.setName(rs.getString("name"));
            dm.setPhone(rs.getString("phone"));
            dm.setEmail(rs.getString("email"));
            dm.setVehicleType(rs.getString("vehicle_type"));
            dm.setVehicleNumber(rs.getString("vehicle_number"));
            dm.setStatus(rs.getString("status"));
            dm.setAddress(rs.getString("address"));
            dm.setSalary(rs.getDouble("salary"));
            Date dateOfJoining = rs.getDate("date_of_joining");
            if (dateOfJoining != null) {
                dm.setDateOfJoining(dateOfJoining.toLocalDate());
            }
            dm.setRating(rs.getDouble("rating"));
            ps.close();
            return dm;
        }
        ps.close();
        return null;
    }

    /**
     * Get all delivery men
     */
    public List<DeliveryMan> getAllDeliveryMen() throws SQLException {
        List<DeliveryMan> deliveryMen = new ArrayList<>();
        String sql = "SELECT * FROM delivery_man";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            DeliveryMan dm = new DeliveryMan();
            dm.setDeliveryManId(rs.getLong("delivery_man_id"));
            dm.setName(rs.getString("name"));
            dm.setPhone(rs.getString("phone"));
            dm.setEmail(rs.getString("email"));
            dm.setVehicleType(rs.getString("vehicle_type"));
            dm.setVehicleNumber(rs.getString("vehicle_number"));
            dm.setStatus(rs.getString("status"));
            dm.setAddress(rs.getString("address"));
            dm.setSalary(rs.getDouble("salary"));

            Date dateOfJoining = rs.getDate("date_of_joining");
            if (dateOfJoining != null) {
                dm.setDateOfJoining(dateOfJoining.toLocalDate());
            }

            dm.setRating(rs.getDouble("rating"));
            deliveryMen.add(dm);
        }
        st.close();
        return deliveryMen;
    }

    /**
     * Get delivery man by ID
     */
    public DeliveryMan getDeliveryManById(Long id) throws SQLException {
        String sql = "SELECT * FROM delivery_man WHERE delivery_man_id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setLong(1, id);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            DeliveryMan dm = new DeliveryMan();
            dm.setDeliveryManId(rs.getLong("delivery_man_id"));
            dm.setName(rs.getString("name"));
            dm.setPhone(rs.getString("phone"));
            dm.setEmail(rs.getString("email"));
            dm.setVehicleType(rs.getString("vehicle_type"));
            dm.setVehicleNumber(rs.getString("vehicle_number"));
            dm.setStatus(rs.getString("status"));
            dm.setAddress(rs.getString("address"));
            dm.setSalary(rs.getDouble("salary"));

            Date dateOfJoining = rs.getDate("date_of_joining");
            if (dateOfJoining != null) {
                dm.setDateOfJoining(dateOfJoining.toLocalDate());
            }

            dm.setRating(rs.getDouble("rating"));
            ps.close();
            return dm;
        }
        ps.close();
        return null;
    }

    /**
     * Get delivery men by status
     */
    public List<DeliveryMan> getDeliveryMenByStatus(String status) throws SQLException {
        List<DeliveryMan> deliveryMen = new ArrayList<>();
        String sql = "SELECT * FROM delivery_man WHERE status = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, status);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            DeliveryMan dm = new DeliveryMan();
            dm.setDeliveryManId(rs.getLong("delivery_man_id"));
            dm.setName(rs.getString("name"));
            dm.setPhone(rs.getString("phone"));
            dm.setEmail(rs.getString("email"));
            dm.setVehicleType(rs.getString("vehicle_type"));
            dm.setVehicleNumber(rs.getString("vehicle_number"));
            dm.setStatus(rs.getString("status"));
            dm.setAddress(rs.getString("address"));
            dm.setSalary(rs.getDouble("salary"));

            Date dateOfJoining = rs.getDate("date_of_joining");
            if (dateOfJoining != null) {
                dm.setDateOfJoining(dateOfJoining.toLocalDate());
            }

            dm.setRating(rs.getDouble("rating"));
            deliveryMen.add(dm);
        }
        ps.close();
        return deliveryMen;
    }

    /**
     * Get active delivery men
     */
    public List<DeliveryMan> getActiveDeliveryMen() throws SQLException {
        return getDeliveryMenByStatus("ACTIVE");
    }

    /**
     * Update delivery man
     */
    public void updateDeliveryMan2(DeliveryMan deliveryMan) throws SQLException {
        String sql = "UPDATE delivery_man SET name=?, phone=?, email=?, vehicle_type=?, vehicle_number=?, " +
                "status=?, address=?, salary=?, date_of_joining=?, rating=? WHERE delivery_man_id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, deliveryMan.getName());
        ps.setString(2, deliveryMan.getPhone());
        ps.setString(3, deliveryMan.getEmail());
        ps.setString(4, deliveryMan.getVehicleType());
        ps.setString(5, deliveryMan.getVehicleNumber());
        ps.setString(6, deliveryMan.getStatus());
        ps.setString(7, deliveryMan.getAddress());
        ps.setDouble(8, deliveryMan.getSalary() != null ? deliveryMan.getSalary() : 0);

        if (deliveryMan.getDateOfJoining() != null) {
            ps.setDate(9, java.sql.Date.valueOf(deliveryMan.getDateOfJoining()));
        } else {
            ps.setDate(9, java.sql.Date.valueOf(LocalDate.now()));
        }

        ps.setDouble(10, deliveryMan.getRating() != null ? deliveryMan.getRating() : 0);
        ps.setLong(11, deliveryMan.getDeliveryManId());
        ps.executeUpdate();
        ps.close();
    }

    /**
     * Update delivery man status
     */
    public void updateDeliveryManStatus(Long id, String status) throws SQLException {
        String sql = "UPDATE delivery_man SET status=? WHERE delivery_man_id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, status);
        ps.setLong(2, id);
        ps.executeUpdate();
        ps.close();
    }

    /**
     * Update delivery man rating
     */
    public void updateDeliveryManRating(Long id, Double rating) throws SQLException {
        String sql = "UPDATE delivery_man SET rating=? WHERE delivery_man_id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setDouble(1, rating);
        ps.setLong(2, id);
        ps.executeUpdate();
        ps.close();
    }

    /**
     * Delete delivery man
     */
    public void deleteDeliveryMan(Long id) throws SQLException {
        String sql = "DELETE FROM delivery_man WHERE delivery_man_id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setLong(1, id);
        ps.executeUpdate();
        ps.close();
    }

    /**
     * Count all delivery men
     */
    public int countDeliveryMen() throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM delivery_man";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);
        if (rs.next()) {
            int count = rs.getInt("count");
            st.close();
            return count;
        }
        st.close();
        return 0;
    }
}