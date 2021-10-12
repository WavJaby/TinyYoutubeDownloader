package com.wavjaby.youtube.util;

import com.wavjaby.json.JsonObject;

public class VideoObject {
    private final ITag itag;
    private final Container container;
    private final String url;
    private final String mimeType;
    private final String quality;
    private final int bitrate;
    private final int averageBitrate;
    private final long contentLength;
    private final long lastModified;
    private final String projectionType;
    private final String approxDurationMs;
    //video
    private int width;
    private int height;
    private int fps;
    private VideoQuality videoQuality;
    //audio
    private String audioQuality;
    private int audioSampleRate;
    private int audioChannels;
    private float loudnessDb;

    public VideoObject(JsonObject data) {
        itag = ITag.valueOf(data.getInt("itag"));
        container = itag.getContainer();
        url = data.getString("url");
        mimeType = data.getString("mimeType");
        quality = data.getString("quality");
        bitrate = data.getInt("bitrate");
        averageBitrate = data.getInt("averageBitrate");
        contentLength = data.getLong("contentLength");
        lastModified = data.getLong("lastModified");
        projectionType = data.getString("projectionType");
        approxDurationMs = data.getString("approxDurationMs");

        if (itag.hasVideo()) {
            width = data.getInt("width");
            height = data.getInt("height");
            fps = data.getInt("fps");
            String qualityLabel = data.getString("qualityLabel");
            int index = qualityLabel.indexOf('p');
            qualityLabel = qualityLabel.substring(0, index);
            videoQuality = VideoQuality.getQuality(Integer.parseInt(qualityLabel));
        }

        if (itag.hasAudio()) {
            audioQuality = data.getString("audioQuality");
            audioSampleRate = data.getInt("audioSampleRate");
            audioChannels = data.getInt("audioChannels");
            loudnessDb = data.getFloat("loudnessDb");
        }
    }

    public boolean isVideo(){
        return videoQuality != null;
    }

    public Container getContainer(){
        return container;
    }

    public ITag getITag() {
        return itag;
    }

    public String getUrl() {
        return url;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getQuality() {
        return quality;
    }

    public int getBitrate() {
        return bitrate;
    }

    public int getAverageBitrate() {
        return averageBitrate;
    }

    public long getContentLength() {
        return contentLength;
    }

    public long getLastModified() {
        return lastModified;
    }

    public String getProjectionType() {
        return projectionType;
    }

    public String getApproxDurationMs() {
        return approxDurationMs;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getFps() {
        return fps;
    }

    public VideoQuality getVideoQuality() {
        return videoQuality;
    }

    public int getVideoQualityInt() {
        return videoQuality.quality;
    }

    public String getAudioQuality() {
        return audioQuality;
    }

    public int getAudioSampleRate() {
        return audioSampleRate;
    }

    public int getAudioChannels() {
        return audioChannels;
    }

    public float getLoudnessDb() {
        return loudnessDb;
    }
}
