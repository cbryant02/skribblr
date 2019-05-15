package com.github.cbryant02.skribblr.util;

import com.github.cbryant02.skribblr.MainController;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * A simple popup with a Task-tracking progress bar.
 */
public class ProgressPopup implements Initializable {
    @FXML private Label titleLabel;
    @FXML private ProgressBar progressBar;

    private final FXMLLoader loader;
    private Stage stage;
    private final String message;
    private final Task task;

    /**
     * Construct a new ProgressPopup.
     * @param message Message to display (i.e. task description)
     * @param task Task to track
     */
    public ProgressPopup(String message, Task task) {
        this.message = message;
        this.task = task;

        // Load layout
        loader = new FXMLLoader(getClass().getClassLoader().getResource("com/github/cbryant02/skribblr/fxml/progress.fxml"));
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException ex) {
            MainController.createExceptionAlert("Failed to load layout for progress popup", ex);
            System.exit(-1);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configure stage
        Parent root = loader.getRoot();
        stage = new Stage();
        stage.setTitle(message);
        stage.setScene(new Scene(root, root.prefWidth(-1), root.prefHeight(-1)));
        stage.setResizable(false);
        stage.setAlwaysOnTop(true);

        // Position stage in top right corner
        Rectangle2D screenBounds = Screen.getPrimary().getBounds();
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
