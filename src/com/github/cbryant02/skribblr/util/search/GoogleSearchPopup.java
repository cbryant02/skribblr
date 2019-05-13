package com.github.cbryant02.skribblr.util.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.github.cbryant02.skribblr.Main;
import com.github.cbryant02.skribblr.MainController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class GoogleSearchPopup {
    private static final String API_KEY = Main.getApiKey();
    private static final String ENGINE_ID = Main.getEngineId();
    private static final String REST_FORMAT = "https://www.googleapis.com/customsearch/v1?key=%s&cx=%s&q=%s&searchType=image&start=%d";
    private static final Map<String,GoogleSearchResult[]> resultsCache = new HashMap<>();
    private static ObjectMapper mapper;
    private String returnUrl;
    private int start = 1;
    private String currentSearch;
    private Stage stage;
    @FXML private Parent root;
    @FXML private GridPane grid;
    @FXML private TextField searchField;
    @FXML private Button prevButton;
    @FXML private Button nextButton;

    public GoogleSearchPopup() {
        // Set up the JSON object mapper and register our custom deserializer
        mapper = new ObjectMapper();
        SimpleModule dsModule = new SimpleModule();
        dsModule.addDeserializer(GoogleSearchResult[].class, new GoogleSearchResultDeserializer());
        mapper.registerModule(dsModule);

        // Load layout
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../../fxml/google.fxml"));
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
    }

    @FXML
    public void onNextButtonPressed() {
        start += 10;
        prevButton.setDisable(false);
        Platform.runLater(this::search);
    }

    @FXML
    public void onPrevButtonPressed() {
        start -= 10;
        if(start == 1)
            prevButton.setDisable(true);
        Platform.runLater(this::search);
    }

    @FXML
    public void onSearchFieldUpdate() {
        String term;
        try {
            term = URLEncoder.encode(searchField.getText(), StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) { return; }

        if(term == null || term.isEmpty())
            return;

        // Update current search term
        currentSearch = term;

        // Search
        Platform.runLater(this::search);

        // Enable next button
        nextButton.setDisable(false);
    }

    public void showAndWait() {
        stage.showAndWait();
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    private void search() {
        // Build query url
        String request = String.format(REST_FORMAT, API_KEY, ENGINE_ID, currentSearch, start);
        GoogleSearchResult[] results;

        // Skip API call if we've already fetched the results for this request
        // Saves both time and requests
        if(!resultsCache.containsKey(request)) {
            // Query results
            String json;
            try {
                json = query(request);
            } catch (IOException ex) {
                MainController.createExceptionAlert("An unexpected I/O error occurred while processing the search results. Please try again.", ex).showAndWait();
                return;
            }

            // Deserialize results
            try {
                results = mapper.readValue(json, GoogleSearchResult[].class);
            } catch (IOException ex) {
                MainController.createExceptionAlert("An unexpected I/O error occurred while processing the search results. Please try again.", ex).showAndWait();
                return;
            }
        } else {
            results = resultsCache.get(request);
            System.out.println("Cache already had results for \"" + request.substring(0,40) + "...\", using cached data");
        }

        // Clear GridPane
        if (grid.getChildren().size() > 0)
            grid.getChildren().removeAll(grid.getChildren());

        // Populate GridPane
        int resultIdx = 0;
        for (int y = 0; y < grid.getRowConstraints().size(); y++) {
            for (int x = 0; x < grid.getColumnConstraints().size(); x++) {
                if (resultIdx >= results.length)
                    break;

                GoogleSearchResult result = results[resultIdx];

                ImageView view = new ImageView(result.getImageMeta().getThumbnail());
                view.setCursor(Cursor.HAND);
                view.setOnMouseClicked(ignore -> {
                    returnUrl = result.getLink();
                    stage.close();
                });

                Label label = new Label();
                label.setText(String.format("%s\n(%dx%d)", result.getTitle(), result.getImageMeta().getWidth(), result.getImageMeta().getHeight()));
                label.setTextOverrun(OverrunStyle.ELLIPSIS);
                label.setTextAlignment(TextAlignment.CENTER);
                label.setAlignment(Pos.CENTER);
                GridPane.setValignment(label, VPos.BOTTOM);
                GridPane.setHgrow(label, Priority.ALWAYS);

                grid.add(view, x, y);
                grid.add(label, x, y);
                resultIdx++;
            }
        }

        resultsCache.put(request, results);
    }

    private String query(String request) throws IOException {
        HttpClientBuilder builder = HttpClientBuilder.create();
        builder.setUserAgent(String.format("Apache-HttpClient/4.5.8 (Java/%s)", System.getProperty("java.version")));

        HttpClient client = builder.build();
        HttpResponse response = client.execute(new HttpGet(request));

        int responseCode = response.getStatusLine().getStatusCode();
        System.out.println("Got response " + responseCode + " for " + request.substring(0,40) + "...");
        if(responseCode != HttpStatus.SC_OK)
            return "";

        return EntityUtils.toString(response.getEntity());
    }
}