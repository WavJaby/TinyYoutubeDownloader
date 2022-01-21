package com.wavjaby;

import com.wavjaby.convert.ConvertVideo;
import com.wavjaby.youtube.VideoInfoGetter;
import com.wavjaby.youtube.downloader.ProgressListener;
import com.wavjaby.youtube.downloader.SpeedCalculator;
import com.wavjaby.youtube.downloader.VideoDownloader;
import com.wavjaby.youtube.util.Container;
import com.wavjaby.youtube.util.Thumbnail;
import com.wavjaby.youtube.util.VideoObject;
import com.wavjaby.youtube.util.VideoQuality;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;

public class Main {
    Main() {
//        System.setIn(new ByteArrayInputStream("https://www.youtube.com/watch?v=dQw4w9WgXcQ\n720".getBytes()));
        Scanner userInput = new Scanner(System.in);
        VideoInfoGetter videoInfoGetter = new VideoInfoGetter();
        videoInfoGetter.setErrorEvent((errorType, message) -> {
            System.err.println(errorType.name() + ": " + message);
        });
        System.out.print("url: ");
        String url = userInput.nextLine();
        System.out.println("getting video from: " + url);
        videoInfoGetter.getVideo(url, (videoInfo) -> {
            // get video info
            System.out.println("Video url: https://youtu.be/" + videoInfo.getVideoID());
            System.out.println("Channel url: https://www.youtube.com/channel/" + videoInfo.getChannelID());
            System.out.println("Channel title: " + videoInfo.getTitle());
            System.out.println("Channel description: " + videoInfo.getDescription());
            System.out.println("Length(sec): " + videoInfo.getLengthSec());
            Thumbnail thumbnail = videoInfo.getLargeThumbnail();
            System.out.println("Thumbnail: " + thumbnail.getUrl());
            System.out.println("Thumbnail size: " + thumbnail.getWidth() + "x" + thumbnail.getHeight());
            System.out.println("View count: " + videoInfo.getViewCount());

            // get video
            videoInfo.setErrorEvent((errorType, message) -> {
                System.err.println(errorType.name() + ": " + message);
            });
            videoInfo.getVideoData(videoData -> {
                // video format
                videoData.forEach((iTag, video) -> {
                    if (iTag.hasVideo())
                        System.out.println("Video: " + video.getVideoQuality() + "," + video.getFps() + "FPS");
                    if (iTag.hasAudio())
                        System.out.println("Audio: " + video.getBitrate() + "bps");
                    System.out.println("Size: " + video.getContentLength() / 1024 + "KB");
                    System.out.println(video.getMimeType());
                });

                // get quality
                String[] input = userInput.nextLine().split("p");
                VideoQuality videoQuality = VideoQuality.getQuality(Integer.parseInt(input[0]));
                VideoObject videoObject;
                if (input.length > 1)
                    videoObject = videoInfo.getVideoByQualityFpsType(videoQuality, Integer.parseInt(input[1]), Container.Mp4);
                else
                    videoObject = videoInfo.getVideoByQualityType(videoQuality, Container.Mp4).get(0);
                VideoObject audioObject = videoInfo.getBestAudioM4A();


                // raw data
//                System.out.println(videoInfo.getRawData().toStringBeauty());
                // print video info
                System.out.println(videoObject.getUrl());
                System.out.println(videoObject.getMimeType());
                System.out.println(videoObject.getContentLength() / 1024 / 1024 + "MB");
                // print audio info
                System.out.println(audioObject.getUrl());
                System.out.println(audioObject.getMimeType());
                System.out.println(audioObject.getContentLength() / 1024 / 1024 + "MB");


                String outputDir = "C:\\Users\\Eason\\Desktop";
                File tempVideo = new File(outputDir, "download.mp4");
                File tempAudio = new File(outputDir, "audio.m4a");

                CountDownLatch count = new CountDownLatch(2);
                VideoDownloader videoDownloader = new VideoDownloader(videoObject, tempVideo);
                VideoDownloader audioDownloader = new VideoDownloader(audioObject, tempAudio);
                videoDownloader.setListener(new ProgressListener() {
                    @Override
                    public void progress(float percent, long pos, String speed, ThreadPoolExecutor executor) {
                        System.out.print("\rdone: " + executor.getCompletedTaskCount() +
                                ", inThread: " + executor.getActiveCount() +
                                ", Speed: " + speed +
                                ", Percent: " + percent
                        );
                    }

                    @Override
                    public void done(SpeedCalculator calculator) {
                        long timePass = calculator.getTimePass() / 1000;
                        System.out.println();
                        System.out.println("Used " + timePass + "sec, average speed " + calculator.getAvgSpeed("%.1f"));
                        count.countDown();
                    }
                });
                audioDownloader.setListener(new ProgressListener() {
                    @Override
                    public void progress(float percent, long pos, String speed, ThreadPoolExecutor executor) {

                    }

                    @Override
                    public void done(SpeedCalculator calculator) {
                        long timePass = calculator.getTimePass() / 1000;
                        System.out.println();
                        System.out.println("Used " + timePass + "sec, average speed " + calculator.getAvgSpeed("%.1f"));
                        count.countDown();
                    }
                });
                videoDownloader.startDownload();
                audioDownloader.startDownload();

                try {
                    count.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                String title = videoInfo.getTitle();
                title = title.replaceAll("[/:*?\"><|]", "_");
                new ConvertVideo(title + ".mp4", tempVideo, tempAudio, outputDir);
                System.out.println("done");
            });
        });
    }


    public static void main(String[] args) {
        new Main();
    }
}
