package com.metime.videofilter.utils;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.media.MediaMuxer;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

public class VideoInfoUtils {

    public static MediaFormat getVideoInfo(String path) {

        MediaExtractor audioExtractor = new MediaExtractor();
        try {
            audioExtractor.setDataSource(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int selectTrack = selectTrack(audioExtractor);
        if (selectTrack < 0) {
            throw new RuntimeException("No video track found in " + path);
        }
        MediaFormat format = audioExtractor.getTrackFormat(selectTrack);
        return format;

    }

    /**
     * Selects the video track, if any.
     *
     * @return the track index, or -1 if no video track is found.
     */
    private static int selectTrack(MediaExtractor mMediaExtractor) {
        // Select the first video track we find, ignore the rest.
        int numTracks = mMediaExtractor.getTrackCount();
        for (int i = 0; i < numTracks; i++) {
            MediaFormat format = mMediaExtractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("video/")) {
                return i;
            }
        }

        return -1;
    }




    public static void combineVideo(String videoPath, String newVideo, String output) {
        try {

            MediaExtractor videoExtractor = new MediaExtractor();
            videoExtractor.setDataSource(newVideo);
            videoExtractor.selectTrack(0); // Assuming only one track per file. Adjust code if this is not the case.
            MediaFormat videoFormat = videoExtractor.getTrackFormat(0);
            long videoDuration = videoFormat.getLong(MediaFormat.KEY_DURATION);

            MediaExtractor audioExtractor = new MediaExtractor();
            audioExtractor.setDataSource(videoPath, null);
            audioExtractor.selectTrack(1); // Assuming only one track per file. Adjust code if this is not the case.
            MediaFormat audioFormat = audioExtractor.getTrackFormat(1);

            // Init muxer
            MediaMuxer muxer = new MediaMuxer(output, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            // Set up the orientation and starting time for extractor.
            MediaMetadataRetriever retrieverSrc = new MediaMetadataRetriever();
            retrieverSrc.setDataSource(videoPath);
            String degreesString = retrieverSrc.extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
            if (degreesString != null) {
                int degrees = Integer.parseInt(degreesString);
                if (degrees >= 0) {
                    //we do not require to do anything here (KD) please remove this complete code
                   // muxer.setOrientationHint(degrees);
                }
            }
            int videoIndex = muxer.addTrack(videoFormat);
            int audioIndex = muxer.addTrack(audioFormat);
            muxer.start();

            // Prepare buffer for copying
            int maxChunkSize = 1024 * 1024;
            ByteBuffer buffer = ByteBuffer.allocate(maxChunkSize);
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();


            // Copy Video
            while (true) {
                int chunkSize = videoExtractor.readSampleData(buffer, 0);

                if (chunkSize > 0) {
                    bufferInfo.presentationTimeUs = videoExtractor.getSampleTime();
                    bufferInfo.flags = videoExtractor.getSampleFlags();
                    bufferInfo.size = chunkSize;

                    muxer.writeSampleData(videoIndex, buffer, bufferInfo);
                    videoExtractor.advance();
                } else {
                    break;
                }
            }

            // Copy audio
            while (true) {
                int chunkSize = audioExtractor.readSampleData(buffer, 0);

                if (chunkSize >= 0) {

                    if (bufferInfo.presentationTimeUs >= videoDuration)
                        break;

                    bufferInfo.presentationTimeUs = audioExtractor.getSampleTime();
                    bufferInfo.flags = audioExtractor.getSampleFlags();
                    bufferInfo.size = chunkSize;

                    muxer.writeSampleData(audioIndex, buffer, bufferInfo);
                    audioExtractor.advance();
                } else {
                    break;
                }
            }
            // Cleanup
            muxer.stop();
            muxer.release();

            videoExtractor.release();
            audioExtractor.release();

            /*MediaExtractor videoExtractor = new MediaExtractor();
            videoExtractor.setDataSource(newVideo);
            MediaFormat videoFormat = null;
            int videoTrackIndex = -1;
            int videoTrackCount = videoExtractor.getTrackCount();
            for (int i = 0; i < videoTrackCount; i++) {
                videoFormat = videoExtractor.getTrackFormat(i);
                String mimeType = videoFormat.getString(MediaFormat.KEY_MIME);
                if (mimeType.startsWith("video/")) {
                    videoTrackIndex = i;
                    break;
                }
            }
            videoExtractor.selectTrack(videoTrackIndex);


            MediaExtractor audioExtractor = new MediaExtractor();
            audioExtractor.setDataSource(videoPath);
            MediaFormat audioFormat = null;
            int audioTrackIndex = -1;
            int audioTrackCount = audioExtractor.getTrackCount();
            for (int i = 0; i < audioTrackCount; i++) {
                audioFormat = audioExtractor.getTrackFormat(i);
                String mimeType = audioFormat.getString(MediaFormat.KEY_MIME);
                if (mimeType.startsWith("audio/")) {
                    audioTrackIndex = i;
                    break;
                }
            }
            audioExtractor.selectTrack(audioTrackIndex);

            MediaCodec.BufferInfo videoBufferInfo = new MediaCodec.BufferInfo();
            MediaCodec.BufferInfo audioBufferInfo = new MediaCodec.BufferInfo();

            MediaMuxer mediaMuxer = new MediaMuxer(output, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            int writeVideoTrackIndex = mediaMuxer.addTrack(videoFormat);
            int writeAudioTrackIndex = mediaMuxer.addTrack(audioFormat);
            mediaMuxer.start();

            ByteBuffer byteBuffer = ByteBuffer.allocate(500 * 1024);
            long videoStampTime = 0;
            {
                videoExtractor.readSampleData(byteBuffer, 0);
                if (videoExtractor.getSampleFlags() == MediaExtractor.SAMPLE_FLAG_SYNC) {
                    videoExtractor.advance();
                }
                videoExtractor.readSampleData(byteBuffer, 0);
                long secondTime = videoExtractor.getSampleTime();
                videoExtractor.advance();
                long thirdTime = videoExtractor.getSampleTime();
                videoStampTime = Math.abs(thirdTime - secondTime);
            }
            videoExtractor.unselectTrack(videoTrackIndex);
            videoExtractor.selectTrack(videoTrackIndex);

            long audioStampTime = 0;
            //获取帧之间的间隔时间
            {
                audioExtractor.readSampleData(byteBuffer, 0);
                if (audioExtractor.getSampleFlags() == MediaExtractor.SAMPLE_FLAG_SYNC) {
                    audioExtractor.advance();
                }
                audioExtractor.readSampleData(byteBuffer, 0);
                long secondTime = audioExtractor.getSampleTime();
                audioExtractor.advance();
                audioExtractor.readSampleData(byteBuffer, 0);
                long thirdTime = audioExtractor.getSampleTime();
                audioStampTime = Math.abs(thirdTime - secondTime);
                Log.e("audioStampTime", audioStampTime + "");
            }

            audioExtractor.unselectTrack(audioTrackIndex);
            audioExtractor.selectTrack(audioTrackIndex);

            while (true) {
                int readVideoSampleSize = videoExtractor.readSampleData(byteBuffer, 0);
                if (readVideoSampleSize < 0) {
                    break;
                }
                videoBufferInfo.size = readVideoSampleSize;
                videoBufferInfo.presentationTimeUs += videoStampTime;
                videoBufferInfo.offset = 0;
                videoBufferInfo.flags = videoExtractor.getSampleFlags();
                mediaMuxer.writeSampleData(writeVideoTrackIndex, byteBuffer, videoBufferInfo);
                videoExtractor.advance();
            }

            while (true) {
                int readAudioSampleSize = audioExtractor.readSampleData(byteBuffer, 0);
                if (readAudioSampleSize < 0) {
                    break;
                }

                audioBufferInfo.size = readAudioSampleSize;
                audioBufferInfo.presentationTimeUs += audioStampTime;
                audioBufferInfo.offset = 0;
                audioBufferInfo.flags = videoExtractor.getSampleFlags();
                mediaMuxer.writeSampleData(writeAudioTrackIndex, byteBuffer, audioBufferInfo);
                audioExtractor.advance();
            }

            mediaMuxer.stop();
            mediaMuxer.release();
            videoExtractor.release();
            audioExtractor.release();*/
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
