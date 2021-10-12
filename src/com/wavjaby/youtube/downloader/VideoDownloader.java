package com.wavjaby.youtube.downloader;

import com.wavjaby.youtube.util.VideoObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class VideoDownloader extends Thread {
    private final String url;
    private final File file;
    private final long fileSize;
    private ProgressListener listener;

    public VideoDownloader(VideoObject videoObject, File file) {
        this.url = videoObject.getUrl();
        this.fileSize = videoObject.getContentLength();
        this.file = file;
    }

    public void startDownload() {
        new Thread(this).start();
    }

    @Override
    public void run() {
        ThreadFileWriter fileWriter = new ThreadFileWriter(file);
        //download
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(20);
        //buff
        int buffSize = 1024 * 1024 * 10;
        //now byte pos
        int nowPos = 0;

        SpeedCalculator cal = new SpeedCalculator(fileSize);
        do {
            String rangeHeader = "bytes=" + nowPos + "-" + (nowPos + buffSize - 1);
            nowPos += buffSize;

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int threadID = fileWriter.add(out);
            executor.execute(() -> {
                try {
                    HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                    connection.setRequestProperty("Range", rangeHeader);
                    InputStream in = connection.getInputStream();
                    //10KB
                    byte[] buff = new byte[100000];
                    int length;
                    while ((length = in.read(buff)) > 0) {
                        out.write(buff, 0, length);
                        cal.add(length);
                        if (listener != null)
                            listener.progress(cal.getPercent(), cal.getPos(), cal.getSpeed(), executor);
                    }
                    fileWriter.threadDone(threadID);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            //wait some time
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (fileSize - nowPos > 0);
        //wait download
        try {
            executor.shutdown();
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        fileWriter.writeAll();
        fileWriter.close();
        cal.stop();

        if (listener != null)
            listener.done(cal);
    }

    public void setListener(ProgressListener listener) {
        this.listener = listener;
    }

    public interface ProgressListener {
        void progress(float percent, long pos, String speed, ThreadPoolExecutor executor);
        void done(SpeedCalculator calculator);
    }
}
