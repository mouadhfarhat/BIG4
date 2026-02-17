package Entities;

/**
 * Fleet car. 1-to-1 with delivery man: at most one car per delivery man, at most one delivery man per car.
 */
public class Car {
    private Long carId;
    private String make;
    private String model;
    private String licensePlate;
    private String vehicleType;   // e.g. Sedan, Motorcycle, Van
    private Long deliveryManId;   // null when available

    public Car() {
    }

    public Car(String make, String model, String licensePlate, String vehicleType) {
        this.make = make;
        this.model = model;
        this.licensePlate = licensePlate;
        this.vehicleType = vehicleType;
    }

    public Long getCarId() { return carId; }
    public void setCarId(Long carId) { this.carId = carId; }

    public String getMake() { return make; }
    public void setMake(String make) { this.make = make; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public Long getDeliveryManId() { return deliveryManId; }
    public void setDeliveryManId(Long deliveryManId) { this.deliveryManId = deliveryManId; }

    public boolean isAssigned() { return deliveryManId != null; }

    @Override
    public String toString() {
        return make + " " + model + " (" + licensePlate + ")";
    }
}
