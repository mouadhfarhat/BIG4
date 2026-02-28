package Controllers;

import Services.MealDbService;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class DishCardController {

    @FXML private Label lblName;
    @FXML private Label lblDesc;
    @FXML private Label lblPrice;
    @FXML private Label lblBadge;
    @FXML private ImageView ivDish;
    @FXML private Label lblStars;
    @FXML private Label lblPop;

    public void setRating(double rating, boolean popular) {
        // rating: 1.0 -> 5.0
        int full = (int)Math.round(rating);
        full = Math.max(1, Math.min(5, full));

        String stars = "★★★★★".substring(0, full) + "☆☆☆☆☆".substring(0, 5 - full);
        lblStars.setText(stars);

        lblPop.setVisible(popular);
    }

    private final MealDbService mealService = new MealDbService();

    public void setData(String name, String desc, String price, String imageUrl) {

        lblName.setText(name);
        lblDesc.setText(desc);
        lblPrice.setText(price);
        lblBadge.setVisible(false);

        if (imageUrl != null && !imageUrl.isBlank()) {
            ivDish.setImage(new Image(imageUrl, true));
        } else {
            loadImageFromApi(name);
        }
    }

    private void loadImageFromApi(String dishName) {

        Task<String> task = new Task<>() {
            @Override
            protected String call() {
                return mealService.fetchImageUrl(dishName);
            }
        };

        task.setOnSucceeded(e -> {
            String url = task.getValue();
            if (url != null) {
                ivDish.setImage(new Image(url, true));
            }
        });

        new Thread(task).start();
    }
}
