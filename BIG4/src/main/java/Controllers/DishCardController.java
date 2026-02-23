package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

public class DishCardController {

    @FXML private Label lblName;
    @FXML private Label lblDesc;
    @FXML private Label lblPrice;
    @FXML private Label lblBadge;
    @FXML private ImageView ivDish;

    public void setData(String name, String desc, String price) {
        lblName.setText(name);
        lblDesc.setText(desc);
        lblPrice.setText(price);

        // optional
        lblBadge.setVisible(false);

        // if you have an Image:
        // ivDish.setImage(new Image("file:..."));
    }
}
