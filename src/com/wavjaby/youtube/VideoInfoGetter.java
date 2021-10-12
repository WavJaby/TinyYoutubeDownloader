package com.wavjaby.youtube;

import com.wavjaby.json.JsonArray;
import com.wavjaby.json.JsonObject;
import com.wavjaby.youtube.decrypt.ErrorType;

import static com.wavjaby.youtube.URLDataGetter.getUrlData;
import static com.wavjaby.youtube.decrypt.ErrorType.*;

public class VideoInfoGetter {

    private GetVideoInfoErrorEvent errorEvent;

    public VideoInfoGetter() {
    }

    public void getVideo(String videoUrl, GetVideoInfoEvent event) {
        //get video ID
        int videoIDIndex = videoUrl.indexOf("v=");
        int nextQueryIndex = videoUrl.indexOf("&", videoIDIndex);
        String videoID;
        if (videoIDIndex != -1)
            if (nextQueryIndex != -1)
                videoID = videoUrl.substring(videoIDIndex + 2, nextQueryIndex);
            else
                videoID = videoUrl.substring(videoIDIndex + 2);
        else {
            videoIDIndex = videoUrl.lastIndexOf("/");
            if (videoIDIndex != -1)
                videoID = videoUrl.substring(videoIDIndex + 1);
            else {
                errorEvent.onError(CANT_GET_VIDEO_ID, "Could not get video ID");
                return;
            }
        }
        final String getVideoID = videoID;
        //get video info
        new Thread(() -> {
            String url = "https://youtu.be/" + getVideoID;
            String result = getUrlData(url);
            if (result == null) {
                errorEvent.onError(CANT_GET_VIDEO_INFO, "Could not get video info");
                return;
            }

            //get base.js url
            String baseJsUrl = null;
            String configKey = "\"PLAYER_JS_URL\"";
            int configStart = result.indexOf(configKey);
            if (configStart == -1)
                errorEvent.onError(CANT_FIND_BASE_JS_URL, "Could not find base.js URL");
            else {
                configStart = result.indexOf('\"', configStart + configKey.length() + 1);
                int configEnd = -1;
                if (configStart == -1)
                    errorEvent.onError(CANT_FIND_BASE_JS_URL, "Could not find the start of base.js url");
                else
                    configEnd = result.indexOf('\"', configStart + 1);
                if (configEnd == -1)
                    errorEvent.onError(CANT_FIND_BASE_JS_URL, "Could not find the end of base.js url");
                else
                    baseJsUrl = "https://www.youtube.com" + result.substring(configStart + 1, configEnd);
            }

            //get video data
            int startIndex = result.indexOf("ytInitialPlayerResponse");
            if (startIndex == -1) {
                errorEvent.onError(CANT_FIND_VIDEO_INFO, "Could not find video info");
                return;
            }
            //get video format
            JsonObject rawData = new JsonObject(result.substring(startIndex));
            JsonArray videoFormats = rawData
                    .getJson("streamingData")
                    .getArray("adaptiveFormats");
            //get details
            JsonObject videoDetails = rawData.getJson("videoDetails");
            event.onInfoGet(new VideoInfo(getVideoID, rawData, videoDetails, videoFormats, baseJsUrl));
        }).start();
    }

    public interface GetVideoInfoEvent {
        void onInfoGet(VideoInfo videoInfo);
    }


    public void setErrorEvent(GetVideoInfoErrorEvent errorEvent) {
        this.errorEvent = errorEvent;
    }

    public interface GetVideoInfoErrorEvent {
        void onError(ErrorType errorType, String message);
    }
}
