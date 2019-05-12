package com.github.cbryant02.skribblr.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.github.cbryant02.skribblr.Main;
import com.github.cbryant02.skribblr.MainController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GoogleSearchPopup {
    private static final String API_KEY = Main.getApiKey();
    private static final String ENGINE_ID = Main.getEngineId();
    private static final String REST_FORMAT = "https://www.googleapis.com/customsearch/v1?key=%s&cx=%s&q=%s&searchType=image";
    private Stage stage;
    @FXML private Parent root;
    @FXML private GridPane grid;
    @FXML private TextField searchField;

    public GoogleSearchPopup() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../fxml/google.fxml"));
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        // Configure stage
        stage = new Stage();
        stage.setTitle("Search");
        stage.setScene(new Scene(root, root.prefWidth(-1), root.prefHeight(-1)));
        stage.setResizable(false);
        stage.setAlwaysOnTop(true);
    }

    @FXML
    public void onSearchFieldUpdate() {
        String term = searchField.getText();
    }

    public void showAndWait() {
        stage.showAndWait();
    }

    private void search(String term) {
        // Format request
        String request = String.format(REST_FORMAT, API_KEY, ENGINE_ID, "dog");

        // Query results
        String json;
        try {
            json = query(request);
        } catch (IOException ex) {
            MainController.createExceptionAlert("An unexpected I/O error occurred while processing the search results. Please try again.", ex).showAndWait();
            return;
        }

        // Set up the object mapper and register our custom deserializer
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule dsModule = new SimpleModule();
        dsModule.addDeserializer(GoogleSearchResult[].class, new GoogleSearchResultDeserializer());
        mapper.registerModule(dsModule);

        GoogleSearchResult[] results;
        try {
            results = mapper.readValue(json, GoogleSearchResult[].class);
        } catch (IOException ex) {
            MainController.createExceptionAlert("An unexpected I/O error occurred while processing the search results. Please try again.", ex).showAndWait();
            return;
        }

        // Populate GridPane
        int resultIdx = 0;
        for(int y = 0; y < grid.getRowConstraints().size(); y++) {
            for(int x = 0; x < grid.getColumnConstraints().size(); x++) {
                if(resultIdx >= results.length)
                    break;

                GoogleSearchResult result = results[resultIdx];
                grid.add(new ImageView(result.getImageMeta().getThumbnail()), x, y);
                resultIdx++;
            }
        }
    }

    private String query(String request) throws IOException {
        URL url = new URL(request);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestMethod("GET");

        int responseCode = conn.getResponseCode();
        System.out.println("Got response " + responseCode + " from " + url.getHost());
        if(responseCode != 200) {
            return "";
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        StringBuilder result = new StringBuilder();

        while((line = in.readLine()) != null)
            result.append(line).append("\n");
        in.close();

        return result.toString();
    }
}