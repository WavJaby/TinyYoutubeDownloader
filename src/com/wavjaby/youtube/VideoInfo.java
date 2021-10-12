package com.wavjaby.youtube;

import com.wavjaby.json.JsonArray;
import com.wavjaby.json.JsonObject;
import com.wavjaby.youtube.decrypt.ErrorType;
import com.wavjaby.youtube.decrypt.YoutubeDecrypt;
import com.wavjaby.youtube.util.*;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class VideoInfo {
    private Map<ITag, VideoObject> videoType;
    private String videoID;
    private JsonObject rawData;
    private JsonObject videoDetails;
    private JsonArray videoFormats;
    private String baseJsUrl;

    private GetVideoErrorEvent errorEvent;

    VideoInfo(String videoID, JsonObject rawData, JsonObject videoDetails, JsonArray videoFormats, String baseJsUrl) {
        this.videoID = videoID;
        this.rawData = rawData;
        this.videoDetails = videoDetails;
        this.videoFormats = videoFormats;
        this.baseJsUrl = baseJsUrl;
    }

    public void getVideoData(GetVideoEvent getVideoQualityEvent) {
        if (videoType != null) {
            getVideoQualityEvent.onQualityGet(videoType);
            return;
        }
        new Thread(() -> {
            //for decode signature
            YoutubeDecrypt decode = null;
            videoType = new HashMap<>();
            for (Object i : videoFormats) {
                JsonObject data = ((JsonObject) i);
                if (data.containsKey("url")) {
                    data.put("url", unicodeDecode(data.getString("url")));
                } else {
                    if (decode == null)
                        decode = new YoutubeDecrypt(baseJsUrl, errorEvent);
                    String[] signatureWithUrl = unicodeDecode(data.getString("signatureCipher")).split("&");
                    Map<String, String> linkData = new HashMap<>();
                    for (String j : signatureWithUrl) {
                        String[] k = j.split("=");
                        linkData.put(k[0], k[1]);
                    }
                    String signature = decode.decode(urlDecode(linkData.get("s")));

                    data.put("url", urlDecode(urlDecode(linkData.get("url"))) + '&' + linkData.get("sp") + '=' + signature);
                }
                videoType.put(ITag.valueOf(data.getInt("itag")), new VideoObject(data));

                //unknow
                if (ITag.valueOf(data.getInt("itag")) == ITag.UNKNOWN) {
                    System.out.println("unknown itag:" + data.getInt("itag"));
                    System.out.println("mimeType:" + data.getString("mimeType"));
                    if (data.containsKey("width"))
                        System.out.println("size:" + data.getInt("width") + "x" + data.getInt("height"));
                    System.out.println("qualityLabel:" + data.getString("qualityLabel"));
                    System.out.println("have audio:" + data.containsKey("mimeType"));
                }
            }
            getVideoQualityEvent.onQualityGet(videoType);
        }).start();
    }

    //getter
    public String getTitle() {
        return videoDetails.getString("title");
    }

    public String getDescription() {
        return videoDetails.getString("shortDescription");
    }

    public String getViewCount() {
        return videoDetails.getString("viewCount");
    }

    public String getChannelID() {
        return videoDetails.getString("channelId");
    }

    public String getVideoID() {
        return videoID;
    }

    public int getLengthSec() {
        return Integer.parseInt(videoDetails.getString("lengthSeconds"));
    }

    public Thumbnail getLargeThumbnail() {
        int maxPix = 0;
        JsonObject result = null;
        for (Object i : videoDetails.getJson("thumbnail").getArray("thumbnails")) {
            JsonObject data = ((JsonObject) i);
            int pix = data.getInt("width") * data.getInt("height");
            if (pix > maxPix) {
                result = data;
                maxPix = pix;
            }
        }
        if (result == null)
            return null;
        result.put("url", unicodeDecode(result.getString("url")));
        return new Thumbnail(result);
    }

    public Thumbnail getSmallThumbnail() {
        int minPix = -1;
        JsonObject result = null;
        for (Object i : videoDetails.getJson("thumbnail").getArray("thumbnails")) {
            JsonObject data = ((JsonObject) i);
            int pix = data.getInt("width") * data.getInt("height");
            if (pix < minPix || minPix == -1) {
                result = data;
                minPix = pix;
            }
        }
        if (result == null)
            return null;
        result.put("url", unicodeDecode(result.getString("url")));
        return new Thumbnail(result);
    }

    public VideoObject getVideoByQualityFpsType(VideoQuality videoQuality, int fps, Container container) {
        for (VideoObject video : videoType.values()) {
            if (!video.isVideo())
                continue;
            if (video.getContainer() == container && video.getVideoQuality() == videoQuality && video.getFps() == fps)
                return video;
        }
        return null;
    }

    public List<VideoObject> getVideoByQualityType(VideoQuality videoQuality, Container container) {
        List<VideoObject> list = new ArrayList<>();
        for (VideoObject video : videoType.values()) {
            if (video.getVideoQuality() == videoQuality && video.getContainer() == container)
                list.add(video);
        }
        return list;
    }

    public VideoObject getBestAudioM4A() {
        VideoObject audioObject = null;
        int byteRate = 0;
        for (Map.Entry<ITag, VideoObject> tag : videoType.entrySet()) {
            if (!tag.getKey().hasAudio()) continue;
            if (!(tag.getKey().getContainer() == Container.M4A)) continue;
            VideoObject thisAudio = tag.getValue();
            if (thisAudio.getBitrate() > byteRate) {
                byteRate = thisAudio.getBitrate();
                audioObject = thisAudio;
            }
        }
        return audioObject;
    }

    public JsonObject getRawData() {
        return rawData;
    }

    private String urlDecode(String input) {
        try {
            return URLDecoder.decode(input, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    private String unicodeDecode(String input) {
        String[] arr = input.split(Pattern.quote("\\u"));
        StringBuilder text = new StringBuilder();
        text.append(arr[0]);
        for (int i = 1; i < arr.length; i++) {
            int hexVal = Integer.parseInt(arr[i].substring(0, 4), 16);
            text.append((char) hexVal).append(arr[i].substring(4));
        }
        return text.toString();
    }

    public interface GetVideoEvent {
        void onQualityGet(Map<ITag, VideoObject> videoType);
    }

    public void setErrorEvent(GetVideoErrorEvent errorEvent) {
        this.errorEvent = errorEvent;
    }

    public interface GetVideoErrorEvent {
        void onError(ErrorType errorType, String message);
    }
}
