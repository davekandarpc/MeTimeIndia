package com.metime.activity;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.annotation.Nullable;

import com.metime.BaseActivity;
import com.metime.R;
import com.metime.videofilter.ui.activities.FilterActivity;

import java.util.HashMap;

import static com.metime.utils.Constant.INTENT.SELECT_MUSIC;
import static com.metime.utils.Constant.INTENT.VIDEO_FILTER;
import static com.metime.utils.Constant.INTENT.VIDEO_TEXT;
import static com.metime.utils.Constant.INTENT.VIDEO_TRIMMER;

public class AddFilterActivity extends BaseActivity {

    private VideoView videoView;
    private int position = 0;
    private MediaController mediaController;
    private ImageView ivNext;
    private LinearLayout llSoundEffect, llFilter, llAdjustClip, llAddTextParent;

    String videoURL = "";

    public static void openAddFilter(Context context, String data) {
        Intent intent = new Intent(context, AddFilterActivity.class);
        intent.putExtra("path", data);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_filter);
        //initToolbarWithBackButton("Add Filter");

        videoURL = getIntent().getExtras().getString("path");

        this.videoView = (VideoView) findViewById(R.id.videoView);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        ViewGroup.LayoutParams params = (ViewGroup.LayoutParams) videoView.getLayoutParams();
        params.width = metrics.widthPixels;
        params.height = metrics.heightPixels;
        videoView.setLayoutParams(params);

        this.ivNext = (ImageView) findViewById(R.id.ivNext);
        this.llSoundEffect = (LinearLayout) findViewById(R.id.llSoundEffect);
        this.llFilter = (LinearLayout) findViewById(R.id.llFilter);
        this.llAdjustClip = (LinearLayout) findViewById(R.id.llAdjustClip);
        this.llAddTextParent = (LinearLayout) findViewById(R.id.llAddTextParent);

        // Set the media controller buttons
        if (this.mediaController == null) {
            this.mediaController = new MediaController(this);

            // Set the videoView that acts as the anchor for the MediaController.
            this.mediaController.setAnchorView(videoView);

            // Set MediaController for VideoView
            this.videoView.setMediaController(mediaController);
        }


        // When the video file ready for playback.
        this.videoView.setOnPreparedListener(new OnPreparedListener() {

            public void onPrepared(MediaPlayer mediaPlayer) {

                videoView.seekTo(position);
                if (position == 0) {
                    videoView.start();
                }

                // When video Screen change size.
                mediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                    @Override
                    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {

                        // Re-Set the videoView that acts as the anchor for the MediaController
                        //mediaController.setAnchorView(videoView);
                    }
                });
            }
        });


        startVideo();
        //videoView.start();

        llSoundEffect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentMusic = new Intent(AddFilterActivity.this, AddMusicActivity.class);
                intentMusic.putExtra("path", videoURL);
                startActivityForResult(intentMusic, SELECT_MUSIC);
            }
        });

        llAddTextParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentMusic = new Intent(AddFilterActivity.this, AddTextActivity.class);
                intentMusic.putExtra("path", videoURL);
                startActivityForResult(intentMusic, VIDEO_TEXT);
            }
        });

        llAdjustClip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentMusic = new Intent(AddFilterActivity.this, VideoTrimmerActivity.class);
                intentMusic.putExtra("path", videoURL);
                startActivityForResult(intentMusic, VIDEO_TRIMMER);
            }
        });

        llFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentMusic = new Intent(AddFilterActivity.this, FilterActivity.class);
                intentMusic.putExtra("path", videoURL);
                startActivityForResult(intentMusic, VIDEO_FILTER);
            }
        });

        ivNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddVideoActivity.openAddVideo(AddFilterActivity.this, videoURL);
            }
        });

    }

    private void startVideo() {
        Uri uri = Uri.parse(videoURL);
        //Setting MediaController and URI, then starting the videoView
        videoView.setMediaController(mediaController);
        videoView.setVideoURI(uri);
        videoView.requestFocus();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return (super.onOptionsItemSelected(item));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_MUSIC) {
                videoURL = data.getExtras().getString("data");
                startVideo();
            } else if (requestCode == VIDEO_TRIMMER) {
                videoURL = data.getExtras().getString("data");
                startVideo();
            } else if (requestCode == VIDEO_FILTER) {
                videoURL = data.getExtras().getString("data");
                startVideo();
            } else if (requestCode == VIDEO_TEXT) {
                videoURL = data.getExtras().getString("data");
                startVideo();
            }
        }

    }



}
