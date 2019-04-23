package com.github.cbryant02.skribblr;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

import java.awt.image.BufferedImage;


public class MainController {
    @FXML
    private ImageView originalImageView;
    @FXML
    private ImageView skribblImageView;

    @FXML
    private Label originalImageLabel;
    @FXML
    private Label skribblImageLabel;

    public void initialize() {
        // Set image view attributes
        originalImageView.setPreserveRatio(true);
        skribblImageView.setPreserveRatio(true);

        // Get parent AnchorPanes
        AnchorPane originalPane = (AnchorPane)originalImageView.getParent();
        AnchorPane skribblPane  = (AnchorPane)skribblImageView.getParent();

        // Center image views
        originalImageView.setX(originalPane.getWidth()/2);
        originalImageView.setY(originalPane.getHeight()/2);

        skribblImageView.setX(originalPane.getWidth()/2);
        skribblImageView.setY(originalPane.getHeight()/2);
    }

    void loadImage(BufferedImage original, BufferedImage skribblified) {
        originalImageView.setImage(SwingFXUtils.toFXImage(original, null));
        skribblImageView.setImage(SwingFXUtils.toFXImage(skribblified, null));
    }
}
