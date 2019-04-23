package com.github.cbryant02.skribblr;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class ProgressPopup {
    @FXML
    private Label progressLabel;

    @FXML
    private ProgressBar progressBar;

    private int current;
    private int target;

    public ProgressPopup(int max) {
        this.current = 0;
        this.target = max;
    }

    public void initialize() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("progress.fxml"));
        Stage stage = new Stage();
        stage.setScene(new Scene(root, 300, 100));
        stage.show();
    }

    public void increment() {

    }
    private class ProgressIncrementFuture extends CompletableFuture<Void> {


    }
}
