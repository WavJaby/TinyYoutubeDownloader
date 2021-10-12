package com.wavjaby.convert;


import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

public class ConvertVideo {
    public ConvertVideo(String fileName, File videoFile, File audioFile, String outputDir) {
        Movie video;
        Movie audio;

        try {
            video = MovieCreator.build(videoFile.getPath());
            audio = MovieCreator.build(audioFile.getPath());

        } catch (RuntimeException | IOException e) {
            e.printStackTrace();
            return;
        }

        Movie movie = new Movie();
        try {
            movie.addTrack(new AppendTrack(video.getTracks().get(0)));
            movie.addTrack(new AppendTrack(audio.getTracks().get(0)));
            Container out = new DefaultMp4Builder().build(movie);
            FileChannel fc = new FileOutputStream(outputDir + "/" + fileName).getChannel();
            out.writeContainer(fc);
            fc.close();

            videoFile = new File(videoFile.getAbsolutePath());
            audioFile = new File(audioFile.getAbsolutePath());
            videoFile.delete();
            audioFile.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }



    }
}
