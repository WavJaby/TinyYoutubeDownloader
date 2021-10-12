package com.wavjaby.youtube;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class URLDataGetter {
    public static String getUrlData(String input) {
        try {
            URL url = new URL(input);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.159");
            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setConnectTimeout(Integer.MAX_VALUE);

            if(conn.getResponseCode() > 399)
                return null;

            //get result
            InputStream in = conn.getInputStream();
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

}
