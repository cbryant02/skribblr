package com.github.cbryant02.skribblr.util.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.github.cbryant02.skribblr.Main;
import com.github.cbryant02.skribblr.MainController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
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
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;

/**
 * A search popup.
 * "Returns" a URL upon selection
 */
public final class GoogleSearchPopup implements Initializable {
    @FXML private GridPane grid;
    @FXML private TextField searchField;
    @FXML private Button prevButton;
    @FXML private Button nextButton;

    private static final String API_KEY = Main.getApiKey();
    private static final String ENGINE_ID = Main.ENGINE_ID;
    private static final String REST_FORMAT = "https://www.googleapis.com/customsearch/v1?key=%s&cx=%s&q=%s&searchType=image&start=%d";
    private static ObjectMapper mapper;
    private final FXMLLoader loader;
    private String returnUrl;
    private String currentSearch;
    private Stage stage;
    private int start = 1;

    /**
     * Construct a new {@code GoogleSearchPopup}.
     */
    public GoogleSearchPopup() {
        // Set up the JSON object mapper and register our custom deserializer
        mapper = new ObjectMapper();
        SimpleModule dsModule = new SimpleModule();
        dsModule.addDeserializer(GoogleSearchResult[].class, new GoogleSearchResultDeserializer());
        mapper.registerModule(dsModule);

        // Load layout
        loader = new FXMLLoader(getClass().getClassLoader().getResource("com/github/cbryant02/skribblr/fxml/google.fxml"));
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException ex) {
            MainController.createExceptionAlert("Failed to load layout for search popup", ex);
            System.exit(-1);
        }
    }

    @FXML
    public void initialize(URL location, ResourceBundle resources) {
        // Configure stage
        Parent root = loader.getRoot();
        stage = new Stage();
        stage.setTitle("Search");
        stage.setScene(new Scene(root, root.prefWidth(-1), root.prefHeight(-1)));
        stage.setResizable(false);
    }

    /**
     * Show the popup and return the result.
     * @return URL for selected image, or null if none selected
     */
    public String show() {
        stage.showAndWait();
        return returnUrl;
    }

    @FXML
    private void onNextButtonPressed() {
        start += 10;
        prevButton.setDisable(false);
        Platform.runLater(this::search);
    }

    @FXML
    private void onPrevButtonPressed() {
        start -= 10;
        if(start == 1)
            prevButton.setDisable(true);
        Platform.runLater(this::search);
    }

    @FXML
    private void onSearchFieldUpdate() {
        String term;
        try {
            term = URLEncoder.encode(searchField.getText(), StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ex) { return; }

        if(term == null || term.isEmpty())
            return;

        // Update current search term
        currentSearch = term;

        // Search
        Platform.runLater(this::search);

        // Enable next button
        nextButton.setDisable(false);
    }

    /**
     * Performs a search using the current search term and start value, then shows the results
     */
    private void search() {
        // Build query url
        String request = String.format(REST_FORMAT, API_KEY, ENGINE_ID, currentSearch, start);
        GoogleSearchResult[] results;

        // Skip API call if the cache has results for this request
        // Saves both time and API calls
        if(CacheManager.has(request)) {
            results = CacheManager.get(request);
            System.out.printf("Using cached data for %s\n", request);
        } else {
            // Query results
            String json;
            try {
                json = get(request);
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

            // Cache results
            CacheManager.save(request, results);
        }
        assert (results != null);

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

                ImageView view = new ImageView(result.getMeta().getThumbnail());
                view.setCursor(Cursor.HAND);
                view.setOnMouseClicked(ignore -> {
                    returnUrl = result.getLink();
                    stage.close();
                });

                Label label = new Label();
                label.setText(String.format("%s\n(%dx%d)", result.getTitle(), result.getMeta().getWidth(), result.getMeta().getHeight()));
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
    }

    /**
     * Perform a GET request and return the result.
     * @param request Request URL
     * @return JSON result
     * @throws IOException If a general connection problem or I/O error occurs
     */
    private String get(String request) throws IOException {
        HttpClientBuilder builder = HttpClientBuilder.create();
        builder.setUserAgent(Main.USER_AGENT);

        try(CloseableHttpClient client = builder.build()) {
            HttpResponse response = client.execute(new HttpGet(request));

            int responseCode = response.getStatusLine().getStatusCode();
            System.out.printf("Got response %d for %s\n", responseCode, request);
            if (responseCode != HttpStatus.SC_OK)
                return "";

            return EntityUtils.toString(response.getEntity());
        }
    }
}