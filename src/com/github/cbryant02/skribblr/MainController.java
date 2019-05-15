package com.github.cbryant02.skribblr;

import com.github.cbryant02.skribblr.util.*;
import com.github.cbryant02.skribblr.util.search.GoogleSearchPopup;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.jnativehook.GlobalScreen;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

/**
 * Main layout controller. Handles most frontend application logic.
 */
public final class MainController {
    @FXML private ImageView  originalImageView;
    @FXML private ImageView  skribblImageView;
    @FXML private TextField  imageScaleInput;
    @FXML private TextField  drawSpeedInput;
    @FXML private Label      imagePathLabel;
    @FXML private Label      originalNoImageLabel;
    @FXML private Label      skribblNoImageLabel;
    @FXML private Label apiKeyLabel;
    @FXML private Button     drawButton;
    @FXML private Button     searchButton;
    @FXML private MenuButton bgColorMenu;
    @FXML private Rectangle  bgColorDisplay;

    private static final int IMAGE_SCALE_DEFAULT = 100;
    private final Stage stage;
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private Image currentImage;
    private Image currentImageConverted;
    private FutureTask<FileChooser> preloadFileChooserFuture;
    private int imageScale;

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
                event.consume();
            });

            bgColorMenu.getItems().add(item);
        }

        // Enable search button if search is enabled
        if(Main.isSearchEnabled())
            searchButton.setDisable(false);
    }

    /**
     * Prepares an error alert with a stacktrace and message
     * @param message Message to display
     * @param ex Exception to source stacktrace from
     * @return Prepared error alert
     */
    public static Alert createExceptionAlert(String message, Exception ex) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Oops!");
        alert.setContentText(message);

        StringWriter t = new StringWriter();
        ex.printStackTrace(new PrintWriter(t));
        String stackTrace = t.toString();

        Label label = new Label("Stack trace:");

        TextArea traceTextArea = new TextArea(stackTrace);
        traceTextArea.setEditable(false);
        traceTextArea.setWrapText(false);
        traceTextArea.setMaxWidth(Double.MAX_VALUE);
        traceTextArea.setMaxHeight(Double.MAX_VALUE);

        GridPane.setVgrow(traceTextArea, Priority.ALWAYS);
        GridPane.setHgrow(traceTextArea, Priority.ALWAYS);
        GridPane content = new GridPane();
        content.setMaxWidth(Double.MAX_VALUE);
        content.add(label, 0, 0);
        content.add(traceTextArea, 0, 1);

        alert.getDialogPane().setExpandableContent(content);

        return alert;
    }

    @FXML
    public void onLoadFileButtonPressed() throws ExecutionException, InterruptedException, IOException {
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

        // Load image
        loadImage(imageFile.getAbsolutePath(), true);

        // Reset FileChooser future
        preloadFileChooserFuture = new FutureTask<>(FileChooser::new);
        executor.execute(preloadFileChooserFuture);
    }

    @FXML
    public void onLoadWebButtonPressed() throws IOException {
        // Prompt for URL
        TextInputDialog prompt = new TextInputDialog();
        prompt.setTitle("Load image from URL");
        prompt.setHeaderText("Enter a URL:");
        Optional<String> input = prompt.showAndWait();

        // Return if user didn't input anything
        if(!input.isPresent())
            return;

        // Convert to URL object
        URL url;
        try {
            url = new URL(input.get());
        } catch (MalformedURLException ex) {
            createExceptionAlert("The text you typed in was not a valid URL. Please try again.", ex).showAndWait();
            return;
        }

        // Load image
        loadImage(url.toString(), false);
    }

    @FXML
    public void onSearchButtonPressed() throws IOException {
        GoogleSearchPopup popup = new GoogleSearchPopup();
        String url = popup.show();
        if(url != null)
            loadImage(url, false);
    }

    @FXML
    public void onDrawButtonPressed() {
        // Disable draw button while drawing
        drawButton.setDisable(true);

        // Move window out of the way
        stage.setIconified(true);

        // Get draw task
        Task<Void> drawTask = DrawUtils.draw(currentImageConverted);

        // Open progress indicator
        ProgressPopup p = new ProgressPopup("Drawing...", drawTask);
        p.show();

        // Start drawing
        executor.execute(drawTask);

        // Enable draw button again and close progress window when finished
        drawTask.setOnSucceeded(event -> {
            event.consume();
            p.close();
            drawButton.setDisable(false);
            stage.setIconified(false);
        });

        // Cancel draw task if ESC is pressed
        // This is JNativeHook's fault, not mine, I swear
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
            SkribblRobot.setDelay(SkribblRobot.getDefaultDelay());
            return;
        }

        // Update value and reprocess image
        long d = formatDrawDelay(input);
        if(d == -1L)
            d = SkribblRobot.getDefaultDelay();
        SkribblRobot.setDelay(d);
        drawSpeedInput.setText(d + "ms");
    }

    /**
     * Check presence of API key and update text field as needed
     */
    void checkApiKey() {
        if(Main.getApiKey() == null) {
            apiKeyLabel.setText("Enter API key!");
            apiKeyLabel.setFont(Font.font("System", FontWeight.BOLD, 18.0));
            apiKeyLabel.setTextFill(Color.RED);
            apiKeyLabel.setUnderline(true);
            apiKeyLabel.setOnMouseClicked(event -> new ApiKeyPopup().showAndWait());
            return;
        }
        apiKeyLabel.setText("API key valid");
        apiKeyLabel.setFont(Font.font("System", FontWeight.NORMAL, 12.0));
        apiKeyLabel.setTextFill(Color.GRAY);
        apiKeyLabel.setUnderline(false);
        apiKeyLabel.setOnMouseClicked(event -> {});
    }

    /**
     * Load, display, and process an image.
     * @param path Path to image
     * @param local True if this is a file on the local disk, false if otherwise
     */
    private void loadImage(String path, boolean local) throws IOException {
        Image image;

        // Need to load images differently based on file location (web/local)
        if (local) {
            try(BufferedInputStream s = new BufferedInputStream(new FileInputStream(path))) {
                image = new Image(s);
            }
        } else {
            HttpClientBuilder builder = HttpClientBuilder.create();
            builder.setUserAgent(Main.USER_AGENT);

            try (CloseableHttpClient client = builder.build()) {
                HttpResponse response = client.execute(new HttpGet(path));

                try (ByteArrayInputStream s = new ByteArrayInputStream(EntityUtils.toByteArray(response.getEntity()))) {
                    image = new Image(s);
                }
            }
        }

        // Show an exception dialog if the image ran into an error when loading
        // Usually this is just a 403
        Exception ex = image.getException();
        if(ex != null) {
            String message = "We ran into an error when getting that image from the server.\n" +
                    "Try again or try another image.";
            createExceptionAlert(message, ex).showAndWait();
            return;
        }

        // Update original view and process image for converted view
        originalImageView.setImage(image);
        currentImage = image;
        process(image);

        // Update image path
        imagePathLabel.setText(path);

        // Hide "no image" labels
        originalNoImageLabel.setVisible(false);
        skribblNoImageLabel.setVisible(false);

        // Enable draw button
        drawButton.setDisable(false);
    }

    /**
     * Process and display an image.
     * @param image Image to process
     */
    private void process(Image image) {
        Image converted = DrawUtils.skribblify(image);
        converted = DrawUtils.scaleImage(converted, imageScale/100.0);
        skribblImageView.setImage(DrawUtils.scaleImage(converted, 100.0/imageScale));
        currentImageConverted = converted;
    }

    /**
     * Handles number formatting for imageScale
     * @param s Input string
     * @return Formatted/bound-checked number
     */
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

    /**
     * Handles number formatting for draw delay
     * @param s Input string
     * @return Formatted/bound-checked number
     */
    private long formatDrawDelay(String s) {
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