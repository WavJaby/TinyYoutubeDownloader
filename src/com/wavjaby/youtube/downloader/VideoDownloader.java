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
        //download
        int threadSize = 30;
        //buff (max 10 MB)
        long buffSize = Math.min(fileSize / threadSize, 1024 * 1024 * 10);
        //now byte pos
        long nowPos = 0;

        ThreadFileWriter fileWriter = new ThreadFileWriter(file);
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(threadSize);
        SpeedCalculator cal = new SpeedCalculator(fileSize);
        do {
            String rangeHeader = "bytes=" + nowPos + "-" + (nowPos + buffSize - 1);
            nowPos += buffSize;

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int threadID = fileWriter.add(out);
            executor.execute(() -> {
                while (true) {
                    try {
                        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                        connection.setRequestProperty("Range", rangeHeader);
                        InputStream in = connection.getInputStream();
                        //10KiB
                        byte[] buff = new byte[1024];
                        int length;
                        while ((length = in.read(buff)) > 0) {
                            out.write(buff, 0, length);
                            cal.add(length);

                            // calculate progress
                            String speed;
                            if (listener != null && (speed = cal.getSpeed()) != null)
                                listener.progress(cal.getPercent(), cal.getPos(), speed, executor);
                        }
                        fileWriter.threadDone(threadID);
                        break;
                    } catch (IOException e) {
                        System.err.println(e.getMessage());
                        System.err.println("Thread " + getId() + " error, retrying...");
                    }
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
            boolean success = executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
            if (!success) System.err.println("Download timeout, how is it possible?");
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
}
