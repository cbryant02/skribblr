package com.github.cbryant02.skribblr.util.search;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.Serializable;

/**
 * Represents a single image search result.
 */
@JsonIgnoreProperties(value = {"url", "queries", "context", "searchInformation"})
@JsonDeserialize(using = GoogleSearchResultDeserializer.class)
final class GoogleSearchResult implements Serializable {
    @JsonProperty("title") private String title;
    @JsonProperty("link")  private String link;
    @JsonProperty("image") private ResultMeta meta;

    /**
     * @return Result title
     */
    String getTitle() {
        return title;
    }

    /**
     * @param title New result title
     */
    void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return Link to image
     */
    String getLink() {
        return link;
    }

    /**
     * @param link New image link
     */
    void setLink(String link) {
        this.link = link;
    }

    /**
     * @return Additional result metadata
     */
    ResultMeta getMeta() {
        return meta;
    }

    /**
     * @param meta New result metadata
     */
    void setMeta(ResultMeta meta) {
        this.meta = meta;
    }

    /**
     * Simple container class for extra image result metadata
     */
    static class ResultMeta implements Serializable {
        private final int height;
        private final int width;
        private final String thumbnail;

        ResultMeta(int height, int width, String thumbnail) {
            this.height = height;
            this.width = width;
            this.thumbnail = thumbnail;
        }

        int getHeight() {
            return height;
        }

        int getWidth() {
            return width;
        }

        String getThumbnail() {
            return thumbnail;
        }
    }
}
