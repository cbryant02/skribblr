package com.github.cbryant02.skribblr;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;


public class MainController {
    @FXML
    private ImageView originalImageView;
    @FXML
    private ImageView skribblImageView;

    @FXML
    private Button loadImageButton;
    @FXML
    private Label imagePathLabel;

    private Stage stage;

    public MainController(Stage stage) {
        this.stage = stage;
    }

    @FXML
    public void initialize() {
        // Set image view attributes
        originalImageView.setPreserveRatio(true);
        skribblImageView.setPreserveRatio(true);
    }

    void loadImage(BufferedImage original, BufferedImage skribblified) {
        loadImage(SwingFXUtils.toFXImage(original, null), SwingFXUtils.toFXImage(skribblified, null));
    }

    void loadImage(Image original, Image skribblified) {
        originalImageView.setImage(original);
        skribblImageView.setImage(skribblified);
    }

    public void handleImageLoadButton() throws FileNotFoundException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose an image");

        // Filter extensions to images only
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Images", "*.png", "*.bmp", "*.jpg", "*.gif");
        fileChooser.getExtensionFilters().add(filter);

        String path = fileChooser.showOpenDialog(stage).toString();
        Image image = new Image(new BufferedInputStream(new FileInputStream(path)));
        imagePathLabel.setText(path);
        loadImage(image, image);
    }
}