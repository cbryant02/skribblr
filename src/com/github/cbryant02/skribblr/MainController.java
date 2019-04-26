package com.github.cbryant02.skribblr;

import com.github.cbryant02.skribblr.util.DrawUtils;
import java.io.FileNotFoundException;
import java.util.concurrent.ExecutionException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
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
    private int imageScale;

    private static final int SKIP_PIXELS_DEFAULT = 0;
    private static final int IMAGE_SCALE_DEFAULT = 100;

    MainController(Stage stage) {
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
    public void onLoadFileButtonPressed() throws ExecutionException, InterruptedException, FileNotFoundException {
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
    public void onLoadWebButtonPressed() {
        TextInputDialog prompt = new TextInputDialog();
        prompt.setTitle("Load image from URL");
        prompt.setHeaderText("Enter a URL:");
    }

    @FXML
    public void onTextFieldUpdate(ActionEvent e) {
        TextField field = (TextField)e.getSource();
        String input = field.getText();

        // Clean input string
        if(input.contains("."))
            input = input.split("\\.")[0].trim();
        input = input.replaceAll("[^\\d]", "");

        if(field.equals(skipPixelsInput)) {
            // Reset value to default and return if input is empty
            if(input.isEmpty()) {
                skipPixels = SKIP_PIXELS_DEFAULT;
                return;
            }
            skipPixels = formatSkipPixels(input);
            skipPixelsInput.setText(skipPixels + "px");
            skipPixelsInput.deselect();
        } else if (field.equals(imageScaleInput)) {
            // Reset value to default and return if input is empty
            if(input.isEmpty()) {
                imageScale = IMAGE_SCALE_DEFAULT;
                return;
            }
            imageScale = formatImageScale(input);
            imageScaleInput.setText(imageScale + "%");
        }
    }

    // Handles NumberFormatExceptions and bound checking for imageScale
    private int formatImageScale(String s) {
        int r;
        try {
            r = Integer.valueOf(s);
        } catch (NumberFormatException ex) {
            return imageScale;
        }
        if(r > 100 || r < 0)
            return imageScale;
        return r;
    }

    // Handles NumberFormatExceptions and bound checking for skipPixels
    private int formatSkipPixels(String s) {
        int r;
        try {
            r = Integer.valueOf(s);
        } catch (NumberFormatException ex) {
            return skipPixels;
        }
        if(r > 10 || r < 0)
            return skipPixels;
        return r;
    }
}