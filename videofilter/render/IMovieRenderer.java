package com.metime.videofilter.render;


public interface IMovieRenderer {
    void surfaceCreated();
    void surfaceChanged(int width, int height);
    void doFrame();
    void surfaceDestroy();
}
