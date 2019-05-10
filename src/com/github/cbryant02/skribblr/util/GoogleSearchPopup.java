package com.github.cbryant02.skribblr.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.cbryant02.skribblr.Main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GoogleSearchPopup {
    private static final String REST_FORMAT = "https://www.googleapis.com/customsearch/v1?key=%s&cx=%s&q=%s&searchType=image";

    // Only for testing purposes
    public static void main(String[] args) {
        ObjectMapper mapper = new ObjectMapper();
        String apiKey = Main.getApiKey();
        String engineId = Main.getEngineId();

        String request = String.format(REST_FORMAT, apiKey, engineId, "dog");
        String json;
        try {
            json = getJson(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getJson(String request) throws IOException {
        URL url = new URL(request);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestMethod("GET");

        int responseCode = conn.getResponseCode();
        if(responseCode != 200)
            return "";

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        StringBuilder result = new StringBuilder();

        while((line = in.readLine()) != null)
            result.append(line);
        in.close();

        return result.toString();
    }
}