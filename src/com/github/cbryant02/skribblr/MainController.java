package com.github.cbryant02.skribblr;

import com.github.cbryant02.skribblr.util.DrawUtils;
import javafx.concurrent.Task;
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
import java.io.FileNotFoundException;
import java.util.concurrent.ExecutionException;
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
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private Image currentImage;
    private Image currentImageConverted;
    private FutureTask<FileChooser> preloadFileChooserFuture;
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

    private void loadImage(Image image) {
        originalImageView.setImage(image);
        currentImage = image;

        process(image);
    }

    // Process and display 'skribblified' image
    private void process(Image image) {
        Image converted = DrawUtils.skribblify(image);
        converted = DrawUtils.scaleImage(converted, imageScale/100.0);
        skribblImageView.setImage(DrawUtils.scaleImage(converted, 100.0/imageScale));
        currentImageConverted = converted;
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
    public void onDrawButtonPressed() {
        drawButton.setDisable(true);                                    // Disable draw button while drawing
        Task<Void> drawTask = DrawUtils.draw(currentImageConverted, skipPixels);
        executor.execute(drawTask);

        // Open progress indicator
        ProgressPopup p = new ProgressPopup("Drawing...", drawTask);
        p.show();

        // Enable draw button again and close progress window when finished
        drawTask.setOnSucceeded(event -> {
            drawButton.setDisable(false);
            event.consume();
            p.close();
        });
    }

    @FXML
    public void onTextFieldUpdate(ActionEvent e) {
        TextField field = (TextField)e.getSource();
        String input = field.getText();

        // Sanitize input string
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
        } else if (field.equals(imageScaleInput)) {
            // Reset value to default and return if input is empty
            if(input.isEmpty()) {
                imageScale = IMAGE_SCALE_DEFAULT;
                return;
            }
            imageScale = formatImageScale(input);
            imageScaleInput.setText(imageScale + "%");
            if(currentImageConverted != null)
                process(currentImage);
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