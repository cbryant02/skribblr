package com.github.cbryant02.skribblr;

import com.github.cbryant02.skribblr.util.DrawUtils;
import com.github.cbryant02.skribblr.util.Skribbl;
import com.github.cbryant02.skribblr.util.SkribblRobot;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.jnativehook.GlobalScreen;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;


public class MainController {
    @FXML private ImageView originalImageView;
    @FXML private ImageView skribblImageView;
    @FXML private Label imagePathLabel;
    @FXML private TextField imageScaleInput;
    @FXML private TextField drawSpeedInput;
    @FXML private Button drawButton;
    @FXML private MenuButton bgColorMenu;
    @FXML private Rectangle bgColorDisplay;

    private final Stage stage;
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private Image currentImage;
    private Image currentImageConverted;
    private FutureTask<FileChooser> preloadFileChooserFuture;
    private int imageScale;

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

        // Set up background color picker
        for(Skribbl.Color color : Skribbl.Color.values()) {
            HBox box = new HBox();
            CustomMenuItem item = new CustomMenuItem(box);

            box.setSpacing(5);
            box.getChildren().add(new Rectangle(15, 15, color.getFxColor()));
            box.getChildren().add(new Label(color.toString()));

            item.setOnAction(event -> {
                bgColorMenu.setText(color.toString());
                bgColorDisplay.setFill(color.getFxColor());
                DrawUtils.setBgColor(color);
            });

            bgColorMenu.getItems().add(item);
        }
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

        // Filter extensions to images only
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Images", "*.png", "*.bmp", "*.jpg", "*.gif");
        fileChooser.getExtensionFilters().add(filter);

        // Get image from user
        File imageFile = fileChooser.showOpenDialog(stage);

        // Return if user didn't input anything or file is invalid
        if(imageFile == null || !imageFile.exists())
            return;

        String path = imageFile.toString();
        imagePathLabel.setText(path);
        loadImage(new Image(new BufferedInputStream(new FileInputStream(path))));

        // Reset FileChooser future
        preloadFileChooserFuture = new FutureTask<>(FileChooser::new);
        executor.execute(preloadFileChooserFuture);

        // Enable drawing
        drawButton.setDisable(false);
    }

    @FXML
    public void onLoadWebButtonPressed() {
        // Prompt for URL
        TextInputDialog prompt = new TextInputDialog();
        prompt.setTitle("Load image from URL");
        prompt.setHeaderText("Enter a URL:");
        Optional<String> input = prompt.showAndWait();

        // Return if user didn't input anything
        if(!input.isPresent())
            return;

        URL url;
        try {
            url = new URL(input.get());
        } catch (MalformedURLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Invalid URL");
            alert.setContentText("The text you typed in was not a valid URL. Please try again.");
            alert.showAndWait();
            return;
        }

        loadImage(new Image(url.toString()));

        // Enable drawing
        drawButton.setDisable(false);
    }

    @FXML
    public void onDrawButtonPressed() {
        drawButton.setDisable(true);                                            // Disable draw button while drawing
        Task<Void> drawTask = DrawUtils.draw(currentImageConverted);
        executor.execute(drawTask);

        // Move window out of the way
        stage.setIconified(true);

        // Open progress indicator
        ProgressPopup p = new ProgressPopup("Drawing...", drawTask);
        p.show();

        // Enable draw button again and close progress window when finished
        drawTask.setOnSucceeded(event -> {
            event.consume();
            p.close();
            drawButton.setDisable(false);
            stage.setIconified(false);
        });

        // Cancel draw task if ESC is pressed
        GlobalScreen.addNativeKeyListener(new NativeKeyListener() {
            @Override
            public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) {}

            @Override
            public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {
                if (nativeKeyEvent.getKeyCode() == NativeKeyEvent.VC_ESCAPE) {
                    Platform.runLater(() -> {
                        drawTask.cancel(true);
                        p.close();
                        drawButton.setDisable(false);
                        stage.setIconified(false);
                    });
                }
            }

            @Override
            public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) {}
        });
    }

    @FXML
    public void onImageScaleUpdate() {
        String input = imageScaleInput.getText();

        // Sanitize input string
        if(input.contains("."))
            input = input.split("\\.")[0].trim();
        input = input.replaceAll("[^\\d]", "");

        // Reset value to default and return if input is empty
        if(input.isEmpty()) {
            imageScale = IMAGE_SCALE_DEFAULT;
            return;
        }

        // Update value and reprocess image
        imageScale = formatImageScale(input);
        imageScaleInput.setText(imageScale + "%");
        if(currentImageConverted != null)
            process(currentImage);
    }

    @FXML
    public void onDrawSpeedUpdate() {
        String input = drawSpeedInput.getText();

        // Sanitize input string
        if(input.contains("ms"))
            input = input.replaceAll("ms", "");

        // Reset value to default and return if input is empty
        if(input.isEmpty()) {
            SkribblRobot.setBaseDelay(SkribblRobot.getDefaultBaseDelay());
            return;
        }

        // Update value and reprocess image
        long d = formatDrawSpeed(input);
        if(d == -1L)
            d = SkribblRobot.getDefaultBaseDelay();
        SkribblRobot.setBaseDelay(d);
        drawSpeedInput.setText(d + "ms");
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

    // Handles NumberFormatExceptions and bound checking for draw speed
    private long formatDrawSpeed(String s) {
        long r;
        try {
            r = Long.valueOf(s);
        } catch (NumberFormatException ex) {
            return -1L;
        }
        if(r > 100) return 100L;
        if(r < 1) return 1L;
        return r;
    }
}