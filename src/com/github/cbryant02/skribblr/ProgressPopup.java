package com.github.cbryant02.skribblr;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import java.io.IOException;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class ProgressPopup {
    private Label progressLabel;
    private ProgressBar progressBar;

    private ProgressController controller;

    private int current;
    private int target;

    public ProgressPopup(int max) throws IOException {
        this.current = 0;
        this.target = max;

        Rectangle2D screenBounds = Screen.getPrimary().getBounds();

        Parent root = FXMLLoader.load(getClass().getResource("fxml/progress.fxml"));
        Stage stage = new Stage();
        stage.setTitle("Drawing...");
        stage.setScene(new Scene(root, root.prefWidth(-1), root.prefHeight(-1)));
        stage.setResizable(false);
        stage.setAlwaysOnTop(true);
        stage.setX(screenBounds.getWidth() - root.prefWidth(-1));
        stage.setY(10);
        stage.show();
    }

    public void increment() {
        this.current += 1;
        updateText();
    }

    private void updateText() {
        progressLabel.setText(String.format("%d/%d", current, target));
    }
}
