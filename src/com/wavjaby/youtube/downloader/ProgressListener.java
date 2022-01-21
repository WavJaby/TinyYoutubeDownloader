package com.wavjaby.youtube.downloader;

import java.util.concurrent.ThreadPoolExecutor;

public interface ProgressListener {
    void progress(float percent, long pos, String speed, ThreadPoolExecutor executor);
    void done(SpeedCalculator calculator);
}
