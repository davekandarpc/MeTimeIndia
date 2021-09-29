package com.metime.videofilter.utils;

import android.media.MediaFormat;

import  com.metime.videofilter.filter.helper.MagicFilterType;

public class ConfigUtils {
    String LOG_TAG = "ConfigUtils";
    private static ConfigUtils mInstance;

    public static ConfigUtils getInstance() {
        if (mInstance == null) {
            synchronized (ConfigUtils.class) {
                if (mInstance == null) {
                    mInstance = new ConfigUtils();
                }
            }
        }
        return mInstance;
    }

    private ConfigUtils() {
    }

    private String mVideoPath;
    private String mOutputFile;


    private MediaFormat mMediaFormat;

    public MediaFormat getMediaFormat() {
        return mMediaFormat;
    }

    public MagicFilterType getMagicFilterType() {
        return mMagicFilterType;
    }

    public void setMagicFilterType(MagicFilterType magicFilterType) {
        mMagicFilterType = magicFilterType;
    }

    private MagicFilterType mMagicFilterType = MagicFilterType.FILTER_1;

    public int getFrameInterval() {
        return mFrameInterval;
    }

    public void setFrameInterval(int frameInterval) {
        mFrameInterval = frameInterval;
    }

    private int mFrameInterval;

    public String getVideoPath() {
        return mVideoPath;
    }

    public void setVideoPath(String videoPath) {
        mVideoPath = videoPath;
        mMediaFormat = VideoInfoUtils.getVideoInfo(mVideoPath);
    }

    public String getOutPutVideoPath() {
        return mOutputFile;
    }

    public void setOutPutVideoPath(String videoPath) {
        mOutputFile = videoPath;
    }


}