package com.github.cbryant02.skribblr;

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
    private final ProgressController controller;
    private final Stage stage;
    private int current;
    private final int target;

    public ProgressPopup(String message, int max) {
        this.current = 0;
        this.target = max;

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
        stage.show();

        // Get layout controller
        controller = loader.getController();

        // Position stage in top right corner
        stage.setX(screenBounds.getWidth() - stage.getWidth() - 10);
        stage.setY(10);

        // Set initial progress text
        update();
    }

    public void progress(int progress) {
        this.current = progress;
        if (current >= target)
            stage.close();
        update();
    }

    private void update() {
        controller.progressBar.setProgress(current/target);
        controller.progressLabel.setText(String.format("%d/%d", current, target));
    }

    private class ProgressController {
        @FXML private Label progressLabel;
        @FXML private ProgressBar progressBar;
    }
}
