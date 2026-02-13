import Utils.Mydatabase;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Mydatabase.getInstance();

        FXMLLoader fxmlLoader = new FXMLLoader(
            Main.class.getResource("/main-view.fxml")
        );

        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        stage.setTitle("Waste & Stock Management");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}