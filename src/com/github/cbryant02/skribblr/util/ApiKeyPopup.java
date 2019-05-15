package com.github.cbryant02.skribblr.util;

import com.github.cbryant02.skribblr.Main;
import com.github.cbryant02.skribblr.MainController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.ResourceBundle;

/**
 * A simple popup API key prompt.
 */
public class ApiKeyPopup implements Initializable {
    @FXML private Hyperlink apiKeyLink;
    @FXML private PasswordField keyField;

    private Stage stage;
    private final FXMLLoader loader;

    /**
     * Construct a new {@code ApiKeyPopup}.
     */
    public ApiKeyPopup() {
        // Load layout
        loader = new FXMLLoader(getClass().getClassLoader().getResource("com/github/cbryant02/skribblr/fxml/apikey.fxml"));
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException ex) {
            MainController.createExceptionAlert("Failed to load layout for API key prompt", ex);
            System.exit(-1);
        }
    }

    @Override
    @FXML
    public void initialize(URL location, ResourceBundle resources) {
        // Configure stage
        Parent root = loader.getRoot();
        stage = new Stage();
        stage.setTitle("Input API Key");
        stage.setScene(new Scene(root, root.prefWidth(-1), root.prefHeight(-1)));
        stage.setResizable(false);

        apiKeyLink.setOnMouseClicked(event -> {
            Main.getInstance().getHostServices().showDocument("https://developers.google.com/custom-search/v1/overview");
            event.consume();
        });
    }

    /**
     * Show this window and wait for the user to close it.
     */
    public void showAndWait() {
        stage.showAndWait();
    }

    @FXML
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void onKeyFieldUpdate() throws IOException {
        String value = keyField.getText();

        // Test the key before doing anything else
        if(!Main.testApiKey(value)) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Invalid API key");
            alert.setContentText("Your API key was either misformatted or rejected by the server. Please try again.");
            alert.showAndWait();
            return;
        }

        // Update the key file.
        // Writing a Base64-encoded String object to the file rather than plaintext makes it at least minimally difficult to obtain the API
        // key. We can't make full use of any good encryption algorithms in this context, and we really don't need to in the first place.
        File file = new File("api_key");
        if(file.exists())
            file.delete();
        file.createNewFile();

        try(ObjectOutputStream s = new ObjectOutputStream(new FileOutputStream(file))) {
            s.writeObject(new String(Base64.getEncoder().encode(value.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8));
        }

        Main.loadApiKey();
        stage.close();
    }
}