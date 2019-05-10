package com.github.cbryant02.skribblr.util;

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

/**
 * A simple popup with a Task-tracking progress bar.
 */
public class ProgressPopup {
    @FXML private Label titleLabel;
    @FXML private ProgressBar progressBar;
    private final Stage stage;

    /**
     * Construct a new ProgressPopup.
     * @param message Message to display (i.e. task description)
     * @param task Task to track
     */
    public ProgressPopup(String message, Task task) {
        Rectangle2D screenBounds = Screen.getPrimary().getBounds();

        // Load layout
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../fxml/progress.fxml"));
        loader.setController(this);
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

        // Position stage in top right corner
        stage.setX(screenBounds.getWidth() - root.prefWidth(-1) - 20);
        stage.setY(10);

        // Bind task progress to bar and set initial progress text
        progressBar.progressProperty().bind(task.progressProperty());

        // Change the title to the message
        titleLabel.setText(message);
    }

    /**
     * Show this ProgressPopup.
     */
    public void show() {
        stage.show();
    }

    /**
     * Close this ProgressPopup.
     */
    public void close() {
        stage.close();
    }
}
