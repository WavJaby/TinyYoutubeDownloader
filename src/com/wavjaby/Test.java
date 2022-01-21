package com.wavjaby;

import com.wavjaby.json.JsonObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Test {

    Test() {
        String url = "https://youtubei.googleapis.com/youtubei/v1/player?key=AIzaSyAO_FJ2SlqU8Q4STEHLGCilw_Y9_11qcW8";
        String payload = "{\"videoId\":\"qhTkYIQQKYk\",\"context\":{\"client\":{\"hl\":\"zh\",\"gl\":\"TW\",\"clientName\":\"WEB\",\"clientVersion\":\"2.20210330.08.00\"}}}";
        JsonObject jsonObject = new JsonObject(getUrl(url, payload));
        System.out.println(jsonObject.toStringBeauty());
        System.out.println(jsonObject.getJson("streamingData"));
        System.out.println(jsonObject.getJson("videoDetails"));
        System.out.println(jsonObject.getJson("playerConfig"));
    }

    public String getUrl(String input, String payload) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(input).openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setUseCaches(false);
            connection.setDoOutput(true);
            //post
            OutputStream payloadOut = connection.getOutputStream();
            payloadOut.write(payload.getBytes(StandardCharsets.UTF_8));
            payloadOut.flush();
            //get
            InputStream in;
            if (connection.getResponseCode() > 200)
                in = connection.getErrorStream();
            else
                in = connection.getInputStream();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buff = new byte[1024];
            int length;
            while ((length = in.read(buff)) > 0) {
                out.write(buff, 0, length);
            }
            return out.toString("UTF8");
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return null;
        }
    }

    public String getUrl(String input) {
        URL url;
        try {
            url = new URL(input);
            //get result
            InputStream in = url.openStream();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buff = new byte[1024];
            int length;
            while ((length = in.read(buff)) > 0) {
                out.write(buff, 0, length);
            }
            return out.toString("UTF8");
        } catch (IOException e) {
            return null;
        }
    }

    public static void main(String[] args) {
        new Test();
    }
}
