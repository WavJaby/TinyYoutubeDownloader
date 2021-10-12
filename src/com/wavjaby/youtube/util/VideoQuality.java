package com.wavjaby.youtube.util;

public enum VideoQuality {
    q144(144),
    q240(240),
    q360(360),
    q480(480),
    q720(720),
    q1080(1080),
    q1440(1440),
    q2160(2160),
    q3072(3720),
    q4320(4320),

    UNKNOWN(-1);

    final int quality;
    VideoQuality(int quality){
        this.quality = quality;
    }

    public static VideoQuality getQuality(int qualityInt) {
        for (VideoQuality quality : values()) {
            if (quality.quality == qualityInt)
                return quality;
        }
        return UNKNOWN;
    }
}
