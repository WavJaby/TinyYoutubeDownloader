# TinyYoutubeDownloader

## Get Youtube video info
```java
VideoInfoGetter videoInfoGetter = new VideoInfoGetter();
videoInfoGetter.getVideo(VIDEO_URL, (videoInfo) -> {
    //get video info
    System.out.println("Video url: https://youtu.be/" + videoInfo.getVideoID());
    System.out.println("Channel url: https://www.youtube.com/channel/" + videoInfo.getChannelID());
    System.out.println("Channel title: " + videoInfo.getTitle());
    System.out.println("Channel description: " + videoInfo.getDescription());
    System.out.println("Length(sec): " + videoInfo.getLengthSec());
    Thumbnail thumbnail = videoInfo.getLargeThumbnail();
    System.out.println("Thumbnail: " + thumbnail.getUrl());
    System.out.println("Thumbnail size: " + thumbnail.getWidth() + "x" + thumbnail.getHeight());
    System.out.println("View count: " + videoInfo.getViewCount());
});
```
## Get Youtube video
```java
videoInfoGetter.getVideo(VIDEO_URL, (videoInfo) -> {
    //get video
    videoInfo.setErrorEvent((errorType, message) -> {
        System.err.println(errorType.name() + ": " + message);
    });
    videoInfo.getVideoData(videoData -> {
        videoData.forEach((iTag, video) -> {
            if (iTag.hasVideo())
                System.out.println("Video: " + video.getVideoQuality() + "," + video.getFps() + "FPS");
            if (iTag.hasAudio())
                System.out.println("Audio: " + video.getBitrate() + "bps");
            System.out.println("Size: " + video.getContentLength() / 1024 + "KB");
            System.out.println(video.getMimeType());
        });

        VideoQuality videoQuality = VideoQuality.getQuality(QUALITY);
        VideoObject videoObject = videoInfo.getVideoByQualityFpsType(videoQuality, FPS, Container.Mp4);

        String videoUrl = videoObject.getUrl();
        System.out.println(videoUrl);
        System.out.println(videoObject.getContentLength());
        System.out.println(videoObject.getMimeType());
        System.out.println(videoInfo.getLengthSec());


        String outputDir = "PATH";
        File tempVideo = new File(outputDir, "download.mp4");
        File tempAudio = new File(outputDir, "audio.m4a");

        CountDownLatch count = new CountDownLatch(2);
        VideoDownloader videoDownloader = new VideoDownloader(videoObject, tempVideo);
        VideoDownloader audioDownloader = new VideoDownloader(videoInfo.getBestAudioM4A(), tempAudio);
        videoDownloader.setListener(new VideoDownloader.ProgressListener() {
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
        audioDownloader.setListener(new VideoDownloader.ProgressListener() {
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
```

## Example
[Example File](src/com/wavjaby/Main.java)
