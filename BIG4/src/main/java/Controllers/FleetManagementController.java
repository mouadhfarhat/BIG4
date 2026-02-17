package Controllers;

import Entities.Car;
import Entities.DeliveryMan;
import Services.CarService;
import Services.DeliverymanService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class FleetManagementController {

    @FXML private TableView<Car> carsTable;
    @FXML private TableColumn<Car, Long> carIdCol;
    @FXML private TableColumn<Car, String> makeCol;
    @FXML private TableColumn<Car, String> modelCol;
    @FXML private TableColumn<Car, String> plateCol;
    @FXML private TableColumn<Car, String> typeCol;
    @FXML private TableColumn<Car, String> assignedToCol;
    @FXML private TableColumn<Car, Void> carActionsCol;

    @FXML private TextField makeField;
    @FXML private TextField modelField;
    @FXML private TextField plateField;
    @FXML private TextField typeField;
    @FXML private Button addCarBtn;
    @FXML private Button backBtn;
    @FXML private Label messageLabel;

    private final CarService carService = new CarService();
    private final DeliverymanService deliverymanService = new DeliverymanService();
    private Map<Long, String> deliveryManNames = new HashMap<>();

    @FXML
    public void initialize() {
        loadDeliveryManNames();
        carIdCol.setCellValueFactory(new PropertyValueFactory<>("carId"));
        makeCol.setCellValueFactory(new PropertyValueFactory<>("make"));
        modelCol.setCellValueFactory(new PropertyValueFactory<>("model"));
        plateCol.setCellValueFactory(new PropertyValueFactory<>("licensePlate"));
        typeCol.setCellValueFactory(new PropertyValueFactory<>("vehicleType"));
        assignedToCol.setCellValueFactory(cell -> {
            Long dmId = cell.getValue().getDeliveryManId();
            String name = dmId != null ? deliveryManNames.getOrDefault(dmId, "ID " + dmId) : "—";
            return new javafx.beans.property.SimpleStringProperty(name);
        });
        carActionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button assignBtn = new Button("Assign");
            private final Button unassignBtn = new Button("Unassign");
            private final Button deleteBtn = new Button("Delete");
            private final HBox box = new HBox(6, editBtn, assignBtn, unassignBtn, deleteBtn);

            {
                editBtn.setStyle("-fx-background-color: #0d9488; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 6 10;");
                assignBtn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 6 10;");
                unassignBtn.setStyle("-fx-background-color: #64748b; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 6 10;");
                deleteBtn.setStyle("-fx-background-color: #dc2626; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 6 10;");
                editBtn.setOnAction(e -> editCar(getTableRow().getItem()));
                assignBtn.setOnAction(e -> assignCar(getTableRow().getItem()));
                unassignBtn.setOnAction(e -> unassignCar(getTableRow().getItem()));
                deleteBtn.setOnAction(e -> deleteCar(getTableRow().getItem()));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }
                Car car = getTableRow().getItem();
                assignBtn.setVisible(!car.isAssigned());
                unassignBtn.setVisible(car.isAssigned());
                setGraphic(box);
            }
        });
        loadCars();
    }

    private void loadDeliveryManNames() {
        try {
            List<DeliveryMan> list = deliverymanService.getAllDeliveryMen();
            deliveryManNames.clear();
            for (DeliveryMan dm : list) deliveryManNames.put(dm.getDeliveryManId(), dm.getName() + " (" + dm.getEmail() + ")");
        } catch (SQLException e) {
            setMessage("Could not load delivery men: " + e.getMessage());
        }
    }

    private void loadCars() {
        try {
            List<Car> list = carService.getAllCars();
            carsTable.getItems().setAll(list);
            setMessage("");
        } catch (SQLException e) {
            setMessage("Could not load cars: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddCar() {
        setMessage("");
        String make = makeField.getText();
        String model = modelField.getText();
        String plate = plateField.getText();
        String type = typeField.getText();
        if (plate == null || plate.trim().isEmpty()) {
            setMessage("License plate is required.");
            return;
        }
        try {
            carService.createCar(make, model, plate, type != null ? type : "Sedan");
            makeField.clear();
            modelField.clear();
            plateField.clear();
            typeField.clear();
            loadCars();
        } catch (SQLException e) {
            setMessage("Failed to add car: " + e.getMessage());
        }
    }

    private void assignCar(Car car) {
        if (car == null || car.isAssigned()) return;
        try {
            List<DeliveryMan> drivers = deliverymanService.getAllDeliveryMen();
            if (drivers.isEmpty()) {
                setMessage("No delivery drivers. Add drivers first.");
                return;
            }
            DeliveryMan defaultDriver = drivers.get(0);
            DeliveryMan[] others = drivers.size() > 1 ? drivers.subList(1, drivers.size()).toArray(new DeliveryMan[0]) : new DeliveryMan[0];
            ChoiceDialog<DeliveryMan> dialog = new ChoiceDialog<>(defaultDriver, others);
            dialog.setTitle("Assign car");
            dialog.setHeaderText("Assign " + car.getMake() + " " + car.getModel() + " (" + car.getLicensePlate() + ")");
            dialog.setContentText("Choose delivery driver:");
            Optional<DeliveryMan> result = dialog.showAndWait();
            result.ifPresent(dm -> {
                try {
                    carService.assignCarToDeliveryMan(car.getCarId(), dm.getDeliveryManId());
                    loadDeliveryManNames();
                    loadCars();
                    setMessage("");
                } catch (SQLException e) {
                    setMessage("Assign failed: " + e.getMessage());
                }
            });
        } catch (SQLException e) {
            setMessage("Could not load drivers: " + e.getMessage());
        }
    }

    private void editCar(Car car) {
        if (car == null) return;
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Edit car");
        dialog.setHeaderText("Edit car #" + car.getCarId() + " — " + car.getLicensePlate());

        TextField makeInput = new TextField(car.getMake());
        TextField modelInput = new TextField(car.getModel());
        TextField plateInput = new TextField(car.getLicensePlate());
        TextField typeInput = new TextField(car.getVehicleType() != null ? car.getVehicleType() : "Sedan");
        makeInput.setPromptText("Make");
        modelInput.setPromptText("Model");
        plateInput.setPromptText("License plate");
        typeInput.setPromptText("Type (e.g. Sedan)");
        makeInput.setPrefWidth(220);
        modelInput.setPrefWidth(220);
        plateInput.setPrefWidth(220);
        typeInput.setPrefWidth(220);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Make:"), 0, 0);
        grid.add(makeInput, 1, 0);
        grid.add(new Label("Model:"), 0, 1);
        grid.add(modelInput, 1, 1);
        grid.add(new Label("License plate:"), 0, 2);
        grid.add(plateInput, 1, 2);
        grid.add(new Label("Type:"), 0, 3);
        grid.add(typeInput, 1, 3);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> btn == ButtonType.OK ? "ok" : null);
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(ok -> {
            String make = makeInput.getText();
            String model = modelInput.getText();
            String plate = plateInput.getText();
            String type = typeInput.getText();
            if (plate == null || plate.trim().isEmpty()) {
                setMessage("License plate is required.");
                return;
            }
            try {
                carService.updateCar(car.getCarId(), make, model, plate, type);
                loadCars();
                setMessage("");
            } catch (SQLException e) {
                setMessage("Update failed: " + e.getMessage());
            }
        });
    }

    private void deleteCar(Car car) {
        if (car == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete car");
        confirm.setHeaderText("Delete this car?");
        confirm.setContentText(car.getMake() + " " + car.getModel() + " (" + car.getLicensePlate() + ") will be removed from the fleet. If assigned, the driver will be unassigned. This cannot be undone.");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;
        try {
            carService.deleteCar(car.getCarId());
            loadCars();
            setMessage("");
        } catch (SQLException e) {
            setMessage("Delete failed: " + e.getMessage());
        }
    }

    private void unassignCar(Car car) {
        if (car == null || !car.isAssigned()) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Unassign car");
        confirm.setHeaderText("Unassign " + car.getMake() + " " + car.getModel() + "?");
        confirm.setContentText("The car will become available for another driver.");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;
        try {
            carService.unassignCar(car.getCarId());
            loadCars();
            setMessage("");
        } catch (SQLException e) {
            setMessage("Unassign failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/AdminDelivery.fxml"));
            Stage stage = (Stage) backBtn.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 800));
            stage.setTitle("Admin Delivery - Big4");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setMessage(String text) {
        messageLabel.setText(text);
    }
}
