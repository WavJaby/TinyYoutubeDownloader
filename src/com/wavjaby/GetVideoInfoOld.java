//package com.wavjaby;
//
//import com.wavjaby.json.JsonArray;
//import com.wavjaby.json.JsonObject;
//import com.wavjaby.youtube.decrypt.YoutubeDecryptOld;
//import com.wavjaby.youtube.util.*;
//
//import java.io.*;
//import java.net.HttpURLConnection;
//import java.net.URL;
//import java.net.URLDecoder;
//import java.nio.charset.StandardCharsets;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.regex.Pattern;
//
//public class GetVideoInfoOld {
//    private Map<ITag, VideoObject> videoType;
//    private String videoID;
//    private JsonObject rawData;
//    private JsonObject videoDetails;
//    private JsonArray videoFormats;
//
//    public GetVideoInfoOld(String videoUrl) {
//        //get video ID
//        int videoIDIndex = videoUrl.indexOf("v=");
//        int nextQueryIndex = videoUrl.indexOf("&", videoIDIndex);
//        if (videoIDIndex != -1)
//            if (nextQueryIndex != -1)
//                videoID = videoUrl.substring(videoIDIndex + 2, nextQueryIndex);
//            else
//                videoID = videoUrl.substring(videoIDIndex + 2);
//        else {
//            videoIDIndex = videoUrl.lastIndexOf("/");
//            videoID = videoUrl.substring(videoIDIndex + 1);
//        }
//
//
//        //get video info
//        String url = "https://youtubei.googleapis.com/youtubei/v1/player?key=AIzaSyAO_FJ2SlqU8Q4STEHLGCilw_Y9_11qcW8";
//        String payload = "{\"videoId\":\"" + videoID + "\",\"context\":{\"client\":{\"hl\":\"zh\",\"gl\":\"TW\",\"clientName\":\"ANDROID\",\"clientVersion\":\"16.02\"}}}";
//        String result = getUrl(url, payload);
//        if (result == null) {
//            System.err.println("can't get video info");
//            return;
//        }
//
//        //get video format
//        rawData = new JsonObject(result);
//        videoFormats = rawData
//                .getJson("streamingData")
//                .getArray("adaptiveFormats");
//        //get details
//        videoDetails = rawData.getJson("videoDetails");
//        videoID = videoDetails.getString("videoId");
//    }
//
//    public Map<ITag, VideoObject> getVideoData() {
//        if (videoType != null)
//            return videoType;
//        //for decode signature
//        YoutubeDecryptOld decode = null;
//        videoType = new HashMap<>();
//        for (Object i : videoFormats) {
//            JsonObject data = ((JsonObject) i);
//            if (data.containsKey("url")) {
//                data.put("url", unicodeDecode(data.getString("url")));
//            } else {
//                if (decode == null)
//                    decode = new YoutubeDecryptOld(videoID, null);
//                String[] signatureWithUrl = unicodeDecode(data.getString("signatureCipher")).split("&");
//                Map<String, String> linkData = new HashMap<>();
//                for (String j : signatureWithUrl) {
//                    String[] k = j.split("=");
//                    linkData.put(k[0], k[1]);
//                }
//                String signature = decode.decode(urlDecode(linkData.get("s")));
//
//                data.put("url", urlDecode(urlDecode(linkData.get("url"))) + '&' + linkData.get("sp") + '=' + signature);
//            }
//            videoType.put(ITag.valueOf(data.getInt("itag")), new VideoObject(data));
//
//            //unknow
//            if (ITag.valueOf(data.getInt("itag")) == ITag.UNKNOWN) {
//                System.out.println("unknown itag:" + data.getInt("itag"));
//                System.out.println("mimeType:" + data.getString("mimeType"));
////                System.out.println("size:" + data.getInt("width") + "x" + data.getInt("height"));
//                System.out.println("qualityLabel:" + data.getString("qualityLabel"));
//                System.out.println("have audio:" + data.containsKey("mimeType"));
//            }
//        }
//
//        return videoType;
//    }
//
//    //getter
//    public String getTitle() {
//        return videoDetails.getString("title");
//    }
//
//    public String getDescription() {
//        return videoDetails.getString("shortDescription");
//    }
//
//    public String getViewCount() {
//        return videoDetails.getString("viewCount");
//    }
//
//    public String getChannelID() {
//        return videoDetails.getString("channelId");
//    }
//
//    public String getVideoID() {
//        return videoID;
//    }
//
//    public int getLengthSec() {
//        return Integer.parseInt(videoDetails.getString("lengthSeconds"));
//    }
//
//    public Thumbnail getLargeThumbnailUrl() {
//        int maxPix = 0;
//        JsonObject result = null;
//        for (Object i : videoDetails.getJson("thumbnail").getArray("thumbnails")) {
//            JsonObject data = ((JsonObject) i);
//            int pix = data.getInt("width") * data.getInt("height");
//            if (pix > maxPix) {
//                result = data;
//            }
//        }
//        if (result == null)
//            return null;
//        return new Thumbnail(result);
//    }
//
//    public VideoObject getVideoByQualityFpsType(VideoQuality videoQuality, int fps, Container container) {
//        for (VideoObject video : videoType.values()) {
//            if (!video.isVideo())
//                continue;
//            if (video.getContainer() == container && video.getVideoQuality() == videoQuality && video.getFps() == fps)
//                return video;
//        }
//        return null;
//    }
//
//    public List<VideoObject> getVideoByQualityType(VideoQuality videoQuality, Container container) {
//        List<VideoObject> list = new ArrayList<>();
//        for (VideoObject video : videoType.values()) {
//            if (video.getVideoQuality() == videoQuality && video.getContainer() == container)
//                list.add(video);
//        }
//        return list;
//    }
//
//    public VideoObject getBestAudioM4A() {
//        VideoObject audioObject = null;
//        int byteRate = 0;
//        for (Map.Entry<ITag, VideoObject> tag : videoType.entrySet()) {
//            if (!tag.getKey().hasAudio()) continue;
//            if (!(tag.getKey().getContainer() == Container.M4A)) continue;
//            VideoObject thisAudio = tag.getValue();
//            if (thisAudio.getBitrate() > byteRate) {
//                byteRate = thisAudio.getBitrate();
//                audioObject = thisAudio;
//            }
//        }
//        return audioObject;
//    }
//
//    //data getter
//    public String getUrl(String input) {
//        URL url;
//        try {
//            url = new URL(input);
//            //get result
//            InputStream in = url.openStream();
//            ByteArrayOutputStream out = new ByteArrayOutputStream();
//            byte[] buff = new byte[1024];
//            int length;
//            while ((length = in.read(buff)) > 0) {
//                out.write(buff, 0, length);
//            }
//            return out.toString("UTF8");
//        } catch (IOException e) {
//            return null;
//        }
//    }
//
//    public String getUrl(String input, String payload) {
//        try {
//            HttpURLConnection connection = (HttpURLConnection) new URL(input).openConnection();
//            connection.setRequestMethod("POST");
//            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
//            connection.setUseCaches(false);
//            connection.setDoOutput(true);
//            //post
//            OutputStream payloadOut = connection.getOutputStream();
//            payloadOut.write(payload.getBytes(StandardCharsets.UTF_8));
//            payloadOut.flush();
//            //get
//            InputStream in;
//            if (connection.getResponseCode() > 399)
//                in = connection.getErrorStream();
//            else
//                in = connection.getInputStream();
//            ByteArrayOutputStream out = new ByteArrayOutputStream();
//            byte[] buff = new byte[1024];
//            int length;
//            while ((length = in.read(buff)) > 0) {
//                out.write(buff, 0, length);
//            }
//            return out.toString("UTF8");
//        } catch (IOException e) {
//            System.err.println(e.getMessage());
//            return null;
//        }
//    }
//
//    private String urlDecode(String input) {
//        try {
//            return URLDecoder.decode(input, "UTF-8");
//        } catch (UnsupportedEncodingException e) {
//            return null;
//        }
//    }
//
//    private String unicodeDecode(String input) {
//        String[] arr = input.split(Pattern.quote("\\u"));
//        StringBuilder text = new StringBuilder();
//        text.append(arr[0]);
//        for (int i = 1; i < arr.length; i++) {
//            int hexVal = Integer.parseInt(arr[i].substring(0, 4), 16);
//            text.append((char) hexVal).append(arr[i].substring(4));
//        }
//        return text.toString();
//    }
//
//    public JsonObject getRawData() {
//        return rawData;
//    }
//}
