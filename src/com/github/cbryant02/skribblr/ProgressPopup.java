package com.github.cbryant02.skribblr;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import java.io.IOException;

public class ProgressPopup {
    private Label progressLabel;
    private ProgressBar progressBar;

    private ProgressController controller;

    private int current;
    private int target;

    public ProgressPopup(int max) throws IOException {
        this.current = 0;
        this.target = max;
        Parent root = FXMLLoader.load(getClass().getResource("fxml/progress.fxml"));

    }

    public void increment() {
        this.current += 1;
        updateText();
    }

    private void updateText() {
        progressLabel.setText(String.format("%d/%d", current, target));
    }
}
