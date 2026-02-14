package test;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainFX extends Application {
    @Override
    public void start(Stage stage) throws Exception {

        Parent root= FXMLLoader.load(getClass().getResource("/homepage.fxml"));

        Scene scene=new Scene(root);

        Stage stage1=new Stage();
        stage1.setScene(scene);
        stage1.show();

    }
}
