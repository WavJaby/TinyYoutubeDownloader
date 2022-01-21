package com.wavjaby.youtube.downloader;

public class SpeedCalculator {
    private final long startTime = System.currentTimeMillis();
    private long endTime;

    private final long size;
    private long pos = 0;

    public SpeedCalculator(long size) {
        this.size = size;
    }

    public synchronized void add(long length) {
        pos += length;
    }

    private long lastTime = startTime;
    private double lastSpeed = 0;
    private long lastPos = 0;

    public String getSpeed() {
        long nowTime = System.currentTimeMillis();
        double timePass = (double) (nowTime - lastTime) / 1000;
        if (timePass == 0) return null;
        // byte per second
        double speedByteS = (double) (pos - lastPos) / timePass;

        // second average
        double avg = (double) (nowTime - lastTime) / 1000;
        speedByteS *= avg;
        speedByteS += lastSpeed * (1 - avg);
        if (nowTime - lastTime >= 1000) {
            lastPos = pos;
            lastTime = nowTime;
            lastSpeed = speedByteS;
        }

        String unit = "Byte/s";
        if (speedByteS > 1000) {
            unit = "KB/s";
            speedByteS /= 1000;
        }
        if (speedByteS > 1000) {
            unit = "MB/s";
            speedByteS /= 1000;
        }
        if (speedByteS > 1000) {
            unit = "GB/s";
            speedByteS /= 1000;
        }
        return String.format("%.1f", speedByteS) + unit;
    }

    public String getAvgSpeed(String format) {
        double result = size / ((endTime - startTime) / 1000d);
        String unit = "Byte/s";
        if (result > 1000) {
            unit = "KB/s";
            result /= 1000;
        }
        if (result > 1000) {
            unit = "MB/s";
            result /= 1000;
        }
        if (result > 1000) {
            unit = "GB/s";
            result /= 1000;
        }
        return String.format(format, result) + unit;
    }

    public float getPercent() {
        return (float) ((double) pos / size) * 100;
    }

    public long getTimePass() {
        return endTime - startTime;
    }

    public long getPos() {
        return pos;
    }

    public void stop() {
        endTime = System.currentTimeMillis();
    }
}
