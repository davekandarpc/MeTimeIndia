package com.metime.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.metime.BaseActivity;
import com.metime.R;
import com.metime.model.VideoModel;

public class PlayServerVideoActivity extends BaseActivity {
    private PlayerView playerView;
    private SimpleExoPlayer simpleExoPlayer;

    VideoModel videoModel;
    ImageView ivClose;

    TextView tvTitle, tvDescription, tvHashTag;

    public static void newInstance(Context context, VideoModel videoModel) {
        Intent intentPlayVideo = new Intent(context, PlayServerVideoActivity.class);
        intentPlayVideo.putExtra("videomodel", videoModel);
        context.startActivity(intentPlayVideo);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_play_server_video);

        videoModel = (VideoModel) getIntent().getExtras().getSerializable("videomodel");


        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvTitle.setText(videoModel.getVideoTitle());

        tvDescription = (TextView) findViewById(R.id.tvDescription);
        tvDescription.setText(videoModel.getVideoDescription());

        tvHashTag = (TextView) findViewById(R.id.tvHashTag);
        tvHashTag.setText(videoModel.getHashTags());

        playerView = (PlayerView) findViewById(R.id.player_view);
        ivClose = (ImageView) findViewById(R.id.ivClose);

        /*playerView.showController();
        playerView.setControllerVisibilityListener(i -> {
            if (i == 0) {
                playerView.showController();
            }
        });*/

        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }




    private void initializePlayer() {

        if(simpleExoPlayer != null)
            return;

        simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(this);

        DataSource.Factory mediaDataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "KeriAndroid"));

        ProgressiveMediaSource mediaSource = new ProgressiveMediaSource.Factory(mediaDataSourceFactory).createMediaSource(Uri.parse(videoModel.getFullVideoPath()));

        simpleExoPlayer.prepare(mediaSource, false, false);
        simpleExoPlayer.setPlayWhenReady(true);
        simpleExoPlayer.setRepeatMode(Player.REPEAT_MODE_ONE);
        playerView.setShutterBackgroundColor(Color.TRANSPARENT);
        playerView.setPlayer(simpleExoPlayer);
        playerView.requestFocus();
    }

    private void releasePlayer() {

        playerView.onPause();

        if(simpleExoPlayer == null){
            return;
        }

        playerView.setPlayer(null);
        simpleExoPlayer.setRepeatMode(Player.REPEAT_MODE_OFF);
        simpleExoPlayer.setPlayWhenReady(false);
        simpleExoPlayer.stop();
        simpleExoPlayer.seekTo(0);

        simpleExoPlayer.stop();
        simpleExoPlayer.release();

        simpleExoPlayer = null;

    }


    @Override
    public void onResume() {
        super.onResume();
       initializePlayer();

    }

    @Override
    public void onPause() {
        super.onPause();
        releasePlayer();
    }

    @Override
    public void onStop() {
        super.onStop();
        releasePlayer();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
