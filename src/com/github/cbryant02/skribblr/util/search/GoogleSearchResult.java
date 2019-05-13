package com.github.cbryant02.skribblr.util.search;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonIgnoreProperties(value = {"url", "queries", "context", "searchInformation"})
@JsonDeserialize(using = GoogleSearchResultDeserializer.class)
class GoogleSearchResult {
    @JsonProperty("title") private String title;
    @JsonProperty("link")  private String link;
    @JsonProperty("image") private ImageMeta meta;

    String getTitle() {
        return title;
    }

    void setTitle(String title) {
        this.title = title;
    }

    String getLink() {
        return link;
    }

    void setLink(String link) {
        this.link = link;
    }

    ImageMeta getImageMeta() {
        return meta;
    }

    void setImageMeta(ImageMeta image) {
        this.meta = image;
    }

    static class ImageMeta {
        private final int height;
        private final int width;
        private final String thumbnail;

        ImageMeta(int height, int width, String thumbnail) {
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
