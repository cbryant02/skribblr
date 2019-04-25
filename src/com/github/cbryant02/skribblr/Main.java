package com.github.cbryant02.skribblr;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.awt.image.BufferedImage;
import java.io.IOException;

public class Main extends Application {
    private BufferedImage currentImageOrig;
    private BufferedImage currentImageConverted;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/main.fxml"));
        loader.setController(new MainController(stage));
        loader.load();

        Parent root = loader.getRoot();
        stage.setTitle("Skribblr");
        stage.setScene(new Scene(root, root.prefWidth(-1), root.prefHeight(-1)));
        stage.setResizable(false);
        stage.show();
    }
}
