package com.github.cbryant02.skribblr;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;

class ProgressPopup {
    private final Stage stage;

    ProgressPopup(String message, Task task) {
        Rectangle2D screenBounds = Screen.getPrimary().getBounds();

        // Load layout
        FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/progress.fxml"));
        loader.setController(new ProgressController());
        try {
            loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Configure and show stage
        Parent root = loader.getRoot();
        stage = new Stage();
        stage.setTitle(message);
        stage.setScene(new Scene(root, root.prefWidth(-1), root.prefHeight(-1)));
        stage.setResizable(false);
        stage.setAlwaysOnTop(true);

        // Get layout controller
        ProgressController controller = loader.getController();

        // Position stage in top right corner
        stage.setX(screenBounds.getWidth() - stage.getWidth() - 10);
        stage.setY(10);

        // Bind task progress to bar and set initial progress text
        controller.progressBar.progressProperty().bind(task.progressProperty());
    }

    void show() {
        stage.show();
    }

    void close() {
        stage.close();
    }

    private class ProgressController {
        @FXML private Label progressLabel;
        @FXML private ProgressBar progressBar;
    }
}
