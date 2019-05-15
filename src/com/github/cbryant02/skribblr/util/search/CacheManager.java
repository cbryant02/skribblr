package com.github.cbryant02.skribblr.util.search;

import com.github.cbryant02.skribblr.MainController;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Simple class used to manage cached search results.
 * <p/>
 * Cache files are, on average, 3.3Kb. Each represents 10 results. This means 33,000 results can be stored in just 3.3Mb.
 * <br/>
 * Old (>1 month) and malformed cache files are automatically cleaned out.
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
final class CacheManager {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HHmmss");
    private static final File cacheFolder;

    private CacheManager() {}

    static {
        // Find and/or create cache folder
        cacheFolder = new File(".search_cache");
        if(!cacheFolder.exists()) {
            try {
                cacheFolder.mkdir();
            } catch (SecurityException ex) {
                MainController.createExceptionAlert("Couldn't create search cache folder", ex);
                System.exit(-1);
            }
        }

        // Clean up bad files
        clean();
    }

    /**
     * Check if the cache contains results for a given request
     * @param source Request URL string
     * @return True if cache contains results for {@code source}, otherwise false
     */
    static boolean has(String source) {
        File[] files = cacheFolder.listFiles();
        assert (files != null);

        // Iterate through files, return true if matching hash
        for(File file : files) {
            // Get source hash, delete file and continue if misformatted
            int hash;
            try {
                hash = Integer.valueOf(file.getName().split("_")[0]);
            } catch (NumberFormatException ex) {
                file.delete();
                continue;
            }

            if (hash == source.hashCode()) return true;
        }
        return false;
    }

    /**
     * Fetch cached results for a given request
     * @param source Request URL string
     * @return Cached {@link GoogleSearchResult} array, or null if no cached results
     */
    static GoogleSearchResult[] get(String source) {
        File[] files = cacheFolder.listFiles();
        assert (files != null);

        for(File file : files) {
            // Get source hash; delete file and continue if malformed
            int hash;
            try {
                hash = Integer.valueOf(file.getName().split("_")[0]);
            } catch (NumberFormatException ex) {
                file.delete();
                continue;
            }

            // Read object array if hash matches
            if (hash == source.hashCode()) {
                try(ObjectInputStream s = new ObjectInputStream(new FileInputStream(file))) {
                    // If cast fails, file is likely malformed; delete and continue
                    GoogleSearchResult[] results;
                    try {
                        results = (GoogleSearchResult[]) s.readObject();
                    } catch (ClassCastException ex) {
                        file.delete();
                        continue;
                    }

                    return results;
                } catch (IOException | ClassNotFoundException ex) {
                    MainController.createExceptionAlert("Failed to read cached result for " + source, ex);
                }
            }
        }
        return null;
    }

    /**
     * Save results to the cache
     * @param source Source URL for results
     * @param results Corresponding result array
     */
    static void save(String source, GoogleSearchResult[] results) {
        // Create cache file
        String filename = String.format("%d_%s.scache", source.hashCode(), DATE_FORMATTER.format(LocalDateTime.now()));
        File file = new File(cacheFolder.getAbsolutePath(), filename);

        try {
            // The filename is always unique, no need to check if it exists
            file.createNewFile();

            // Write array to file
            try(ObjectOutputStream s = new ObjectOutputStream(new FileOutputStream(file))) {
                s.writeObject(results);
            }

            // Log
            System.out.printf("CacheManager: Cached %s\n", source);
        } catch (IOException ex) {
            MainController.createExceptionAlert("Failed to save cache file for " + source, ex);
            file.delete();
        }
    }

    /**
     * Iterates through each cached result and deletes old and malformed files.
     * <br/>
     * Runs once every application startup.
     */
    private static void clean() {
        File[] files = cacheFolder.listFiles();
        assert (files != null);

        for(File file : files) {
            // Split filename into parts
            String[] parts = file.getName().split("_");

            // Get timestamp
            LocalDateTime date = LocalDateTime.parse(parts[1].substring(0, parts[1].lastIndexOf(".")), DATE_FORMATTER);

            try (ObjectInputStream s = new ObjectInputStream(new FileInputStream(file))) {
                // Read array from file
                // Delete file and continue if misformatted
                GoogleSearchResult[] results;
                try {
                    results = (GoogleSearchResult[]) s.readObject();
                } catch (ClassCastException ex) {
                    file.delete();
                    continue;
                }

                // Delete file if wrong array length
                if (results.length != 10) {
                    file.delete();
                    continue;
                }

                // Delete file if cached result is older than a month
                if (date.until(LocalDateTime.now(), ChronoUnit.DAYS) > 30L)
                    file.delete();
            } catch (Exception ignore) {}
        }
    }
}
