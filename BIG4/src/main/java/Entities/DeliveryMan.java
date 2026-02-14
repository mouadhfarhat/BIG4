package Entities;

public class DeliveryMan {
    private Long deliveryManId;
    private String name;
    private String phone;
    private String email;
    private String vehicleType;
    private String vehicleNumber;
    private String status;
    private String address;
    private Double salary;
    private Double rating;

    // Constructors
    public DeliveryMan() {
    }

    public DeliveryMan(String name, String phone, String email, String vehicleType, String vehicleNumber) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.vehicleType = vehicleType;
        this.vehicleNumber = vehicleNumber;
        this.status = "ACTIVE";
        this.rating = 0.0;
    }

    // Getters and Setters
    public Long getDeliveryManId() {
        return deliveryManId;
    }

    public void setDeliveryManId(Long deliveryManId) {
        this.deliveryManId = deliveryManId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Double getSalary() {
        return salary;
    }

    public void setSalary(Double salary) {
        this.salary = salary;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    @Override
    public String toString() {
        return "DeliveryMan{" +
                "deliveryManId=" + deliveryManId +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
