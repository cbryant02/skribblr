package com.github.cbryant02.skribblr.util;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonIgnoreProperties(value = {"url", "queries", "context", "searchInformation"})
@JsonDeserialize(using = GoogleSearchResultDeserializer.class)
public class GoogleSearchResult {
    @JsonProperty("title") private String title;
    @JsonProperty("link")  private String link;
    @JsonProperty("image") private ImageMeta meta;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public ImageMeta getImageMeta() {
        return meta;
    }

    public void setImageMeta(ImageMeta image) {
        this.meta = image;
    }

    static class ImageMeta {
        private int height;
        private int width;
        private String thumbnail;

        public ImageMeta(int height, int width, String thumbnail) {
            this.height = height;
            this.width = width;
            this.thumbnail = thumbnail;
        }

        public int getHeight() {
            return height;
        }

        public int getWidth() {
            return width;
        }

        public String getThumbnail() {
            return thumbnail;
        }
    }

}
