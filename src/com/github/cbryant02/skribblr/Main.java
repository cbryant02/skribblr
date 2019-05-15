package com.github.cbryant02.skribblr;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Main extends Application {
    public static final String ENGINE_ID = "001258445199890760318:mzcgmmiwzii";
    public static final String USER_AGENT = String.format("Apache-HttpClient/4.5.8 (Java/%s)", System.getProperty("java.version"));
    private static boolean enableSearch = true;
    private static String apiKey;
    private static Main instance;
    private static MainController controller;

    public static void main(String[] args) {
        instance = new Main();

        launch(args);
        System.exit(0);
    }

    @Override
    public void start(Stage stage) throws IOException, NativeHookException {
        controller = new MainController(stage);

        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("com/github/cbryant02/skribblr/fxml/main.fxml"));
        loader.setController(controller);
        loader.load();

        // Set up and show main stage
        Parent root = loader.getRoot();
        stage.setTitle("Skribblr");
        stage.setScene(new Scene(root, root.prefWidth(-1), root.prefHeight(-1)));
        stage.setResizable(false);
        stage.show();

        // Move stage out of the way
        stage.setX(Screen.getPrimary().getBounds().getWidth() - stage.getWidth() - 10);
        stage.setY(Screen.getPrimary().getBounds().getHeight() - stage.getHeight() - 50);

        // Disable JNativeHook logger because it nukes stdout from orbit
        Logger jnhLogger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        jnhLogger.setLevel(Level.OFF);
        jnhLogger.setUseParentHandlers(false);

        // Add F1 listener
        GlobalScreen.registerNativeHook();
        GlobalScreen.addNativeKeyListener(new HaltListener());

        // Check API key
        loadApiKey();
        if(apiKey == null) {
            enableSearch = false;
            Alert noSearchAlert = new Alert(Alert.AlertType.WARNING);
            noSearchAlert.setTitle("Warning");
            noSearchAlert.setContentText("You haven't provided a Google Custom Search API key. Web search will be disabled until you enter one.");
            noSearchAlert.showAndWait();
        }
    }

    /**
     * Load API key from disk
     */
    public static void loadApiKey() {
        File file = new File("api_key");

        // Read key
        try(ObjectInputStream s = new ObjectInputStream(new FileInputStream(file))) {
            apiKey = new String(Base64.getDecoder().decode((String)s.readObject()), StandardCharsets.UTF_8);
        } catch (Exception e) {
            apiKey = null;
        }

        // Update UI
        controller.checkApiKey();
    }

    /**
     * Test validity of a Google CSE API key
     * @param key Key to test
     * @return True if valid, otherwise false
     */
    public static boolean testApiKey(String key) {
        // Check length
        if(key.length() != 39)
            return false;

        // Check with server
        HttpClientBuilder builder = HttpClientBuilder.create();
        builder.setUserAgent(Main.USER_AGENT);
        try(CloseableHttpClient client = builder.build()) {
            HttpResponse response = client.execute(new HttpGet(String.format("https://www.googleapis.com/customsearch/v1?key=%s&cx=001258445199890760318:mzcgmmiwzii&q=test", key)));

            int responseCode = response.getStatusLine().getStatusCode();
            if(responseCode != HttpStatus.SC_OK) return false;
        } catch (IOException e) {
            e.printStackTrace();
        }

        // All good
        return true;
    }

    /**
     * @return Instance of Main
     */
    public static Main getInstance() {
        return instance;
    }

    public static String getApiKey() {
        return apiKey;
    }

    static boolean isSearchEnabled() {
        return enableSearch;
    }

    private class HaltListener implements NativeKeyListener {
        @Override
        public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) {}

        @Override
        public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {
            if(nativeKeyEvent.getKeyCode() == NativeKeyEvent.VC_F1) {
                try {
                    GlobalScreen.unregisterNativeHook();
                } catch (NativeHookException ex) { ex.printStackTrace(); }

                // If we were drawing, the mouse is probably still pressed down; release it
                Robot r = null;
                try {
                    r = new Robot();
                } catch (AWTException ex) { ex.printStackTrace(); }
                assert (r != null);
                r.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);

                System.exit(0);
            }
        }

        @Override
        public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) {}
    }
}
