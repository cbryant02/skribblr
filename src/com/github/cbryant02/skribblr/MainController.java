package com.github.cbryant02.skribblr;

import com.github.cbryant02.skribblr.util.DrawUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;


public class MainController {
    @FXML private ImageView originalImageView;
    @FXML private ImageView skribblImageView;
    @FXML private Label imagePathLabel;
    @FXML private TextField skipPixelsInput;
    @FXML private TextField imageScaleInput;
    @FXML private Button drawButton;

    private final Stage stage;
    private FutureTask<FileChooser> preloadFileChooserFuture;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private int skipPixels;
    private double imageScale;

    private static final int SKIP_PIXELS_DEFAULT = 0;
    private static final double IMAGE_SCALE_DEFAULT = 1.0;

    public MainController(Stage stage) {
        this.stage = stage;
    }

    @FXML
    public void initialize() {
        // Set image view attributes
        originalImageView.setPreserveRatio(true);
        skribblImageView.setPreserveRatio(true);

        // Asynchronously preload file chooser because for some reason it's stupidly slow
        preloadFileChooserFuture = new FutureTask<>(FileChooser::new);
        executor.execute(preloadFileChooserFuture);

        // Default image scale and pixel skipping values
        imageScale = IMAGE_SCALE_DEFAULT;
        skipPixels = SKIP_PIXELS_DEFAULT;
    }

    private void loadImage(Image original) {
        Image converted = DrawUtils.scaleImage(DrawUtils.skribblify(original), imageScale);

        originalImageView.setImage(original);
        skribblImageView.setImage(converted);
    }

    @FXML
    public void onLoadButtonPressed() throws Exception {
        FileChooser fileChooser = preloadFileChooserFuture.get();
        fileChooser.setTitle("Choose an image");

        // Indicate loading
        stage.getScene().setCursor(Cursor.WAIT);

        // Filter extensions to images only
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Images", "*.png", "*.bmp", "*.jpg", "*.gif");
        fileChooser.getExtensionFilters().add(filter);

        // Get image file and check that it exists (in case user pressed cancel)
        File imageFile = fileChooser.showOpenDialog(stage);

        if(imageFile == null || !imageFile.exists())
            return;

        String path = imageFile.toString();
        Image image = new Image(new BufferedInputStream(new FileInputStream(path)));
        imagePathLabel.setText(path);
        loadImage(image);

        // Reset FileChooser future
        preloadFileChooserFuture = new FutureTask<>(FileChooser::new);
        executor.execute(preloadFileChooserFuture);

        // Reset cursor
        stage.getScene().setCursor(Cursor.DEFAULT);

        // Enable drawing
        drawButton.setDisable(false);
    }

    @FXML
    public void onTextFieldUpdate(ActionEvent e) {
        TextField field = (TextField)e.getSource();
        if(field.equals(skipPixelsInput)) {
            if(skipPixelsInput.getText().isEmpty())
                skipPixels = SKIP_PIXELS_DEFAULT;
        } else {

        }
    }

    private double formatImageScale(String s) {
        return -1;
    }

    private int formatSkipPixels(String s) {
        return -1;
    }
}