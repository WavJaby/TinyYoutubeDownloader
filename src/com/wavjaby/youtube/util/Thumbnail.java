package com.wavjaby.youtube.util;

import com.wavjaby.json.JsonObject;

public class Thumbnail {
    private final String url;
    private final int width;
    private final int height;

    public Thumbnail(JsonObject result) {
        this.url= result.getString("url");
        this.width = result.getInt("width");
        this.height = result.getInt("height");
    }

    public String getUrl() {
        return url;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
