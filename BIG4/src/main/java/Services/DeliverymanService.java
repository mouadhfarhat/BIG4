package Services;

import Entities.DeliveryMan;
import Utils.Mydatabase;

import java.sql.*;
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
        String sql = "insert into delivery_man(name, phone, email, vehicle_type, vehicle_number, status, address, salary, rating)" +
                "values(?,?,?,?,?,?,?,?,?)";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, deliveryMan.getName());
        ps.setString(2, deliveryMan.getPhone());
        ps.setString(3, deliveryMan.getEmail());
        ps.setString(4, deliveryMan.getVehicleType());
        ps.setString(5, deliveryMan.getVehicleNumber());
        ps.setString(6, deliveryMan.getStatus());
        ps.setString(7, deliveryMan.getAddress());
        ps.setDouble(8, deliveryMan.getSalary() != null ? deliveryMan.getSalary() : 0);
        ps.setDouble(9, deliveryMan.getRating() != null ? deliveryMan.getRating() : 0);
        ps.executeUpdate();
    }

    /**
     * Get all delivery men
     */
    public List<DeliveryMan> getAllDeliveryMen() throws SQLException {
        List<DeliveryMan> deliveryMen = new ArrayList<>();
        String sql = "select * from delivery_man";
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
            dm.setRating(rs.getDouble("rating"));
            deliveryMen.add(dm);
        }
        return deliveryMen;
    }

    /**
     * Get delivery man by ID
     */
    public DeliveryMan getDeliveryManById(Long id) throws SQLException {
        String sql = "select * from delivery_man where delivery_man_id = " + id;
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);
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
            dm.setRating(rs.getDouble("rating"));
            return dm;
        }
        return null;
    }

    /**
     * Get delivery men by status
     */
    public List<DeliveryMan> getDeliveryMenByStatus(String status) throws SQLException {
        List<DeliveryMan> deliveryMen = new ArrayList<>();
        String sql = "select * from delivery_man where status = '" + status + "'";
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
            dm.setRating(rs.getDouble("rating"));
            deliveryMen.add(dm);
        }
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
    public void updateDeliveryMan(DeliveryMan deliveryMan) throws SQLException {
        String sql = "update delivery_man set name='" + deliveryMan.getName() + "', phone='" + deliveryMan.getPhone() +
                "', email='" + deliveryMan.getEmail() + "', vehicle_type='" + deliveryMan.getVehicleType() +
                "', vehicle_number='" + deliveryMan.getVehicleNumber() + "', status='" + deliveryMan.getStatus() +
                "', address='" + deliveryMan.getAddress() + "', salary=" + (deliveryMan.getSalary() != null ? deliveryMan.getSalary() : 0) +
                ", rating=" + (deliveryMan.getRating() != null ? deliveryMan.getRating() : 0) +
                " where delivery_man_id=" + deliveryMan.getDeliveryManId();
        Statement st = cnx.createStatement();
        st.executeUpdate(sql);
    }

    /**
     * Update delivery man (using PreparedStatement - SAFER)
     */
    public void updateDeliveryMan2(DeliveryMan deliveryMan) throws SQLException {
        String sql = "update delivery_man set name=?, phone=?, email=?, vehicle_type=?, vehicle_number=?, " +
                "status=?, address=?, salary=?, rating=? where delivery_man_id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, deliveryMan.getName());
        ps.setString(2, deliveryMan.getPhone());
        ps.setString(3, deliveryMan.getEmail());
        ps.setString(4, deliveryMan.getVehicleType());
        ps.setString(5, deliveryMan.getVehicleNumber());
        ps.setString(6, deliveryMan.getStatus());
        ps.setString(7, deliveryMan.getAddress());
        ps.setDouble(8, deliveryMan.getSalary() != null ? deliveryMan.getSalary() : 0);
        ps.setDouble(9, deliveryMan.getRating() != null ? deliveryMan.getRating() : 0);
        ps.setLong(10, deliveryMan.getDeliveryManId());
        ps.executeUpdate();
    }

    /**
     * Update delivery man status
     */
    public void updateDeliveryManStatus(Long id, String status) throws SQLException {
        String sql = "update delivery_man set status='" + status + "' where delivery_man_id=" + id;
        Statement st = cnx.createStatement();
        st.executeUpdate(sql);
    }

    /**
     * Update delivery man rating
     */
    public void updateDeliveryManRating(Long id, Double rating) throws SQLException {
        String sql = "update delivery_man set rating=" + rating + " where delivery_man_id=" + id;
        Statement st = cnx.createStatement();
        st.executeUpdate(sql);
    }

    /**
     * Delete delivery man
     */
    public void deleteDeliveryMan(Long id) throws SQLException {
        String sql = "delete from delivery_man where delivery_man_id=" + id;
        Statement st = cnx.createStatement();
        st.executeUpdate(sql);
    }

    /**
     * Count all delivery men
     */
    public int countDeliveryMen() throws SQLException {
        String sql = "select count(*) as count from delivery_man";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);
        if (rs.next()) {
            return rs.getInt("count");
        }
        return 0;
    }
}
