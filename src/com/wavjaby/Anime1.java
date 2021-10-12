package com.wavjaby;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class Anime1 {
    Anime1(){
        String siteUrl = "https://anime1.cc/208507646-01-0000";
        System.out.println(getDataFromUrl(siteUrl));
    }

    public static String getDataFromUrl(String urlString) {
        try {
            //connection api
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setConnectTimeout(6000);

            int code = connection.getResponseCode();
            if (code > 299)
                return null;

            //read result
            StringBuilder result = new StringBuilder();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));
            String readLine;
            while ((readLine = in.readLine()) != null) {
                result.append(readLine);
            }
            //close connection
            in.close();
            connection.disconnect();
            return result.toString();
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return null;
        }
    }

    public static void main(String[] args) {
        new Anime1();
    }
}
