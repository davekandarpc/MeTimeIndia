package com.metime.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.VideoView;

import androidx.annotation.Nullable;

import com.metime.BaseActivity;
import com.metime.R;
import com.metime.addtextutils.AddTextToVideoProcessor;
import com.metime.dialog.InfoDialog;
import com.metime.utils.Utils;
import com.metime.utils.VideoEditor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;

import static com.metime.utils.Constant.INTENT.VIDEO_TRIMMER;

public class AfterVideoCaptureActivity extends BaseActivity {

    private VideoView videoView;
    private int position = 0;

    SeekBar seekBar;
    String videoURL = "";
    String from = "1";
    double current_pos, total_duration;
    ImageView ivRecordAgain, ivDone;

    public static void openAfterVideoCapture(Context context, String data, String from) {
        Intent intent = new Intent(context, AfterVideoCaptureActivity.class);
        intent.putExtra("path", data);
        intent.putExtra("from", from);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_after_video_capture);

        Utils.showProgressDialog(this);

        videoURL = getIntent().getExtras().getString("path");
        from = getIntent().getExtras().getString("from");

        this.videoView = (VideoView) findViewById(R.id.videoView);
        seekBar = (SeekBar) findViewById(R.id.seekbar);
        ivRecordAgain = (ImageView) findViewById(R.id.ivRecordAgain);
        ivDone = (ImageView) findViewById(R.id.ivDone);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        ViewGroup.LayoutParams params = (ViewGroup.LayoutParams) videoView.getLayoutParams();
        params.width = metrics.widthPixels;
        params.height = metrics.heightPixels;
        videoView.setLayoutParams(params);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Utils.hideProgressDialog();
                startVideo();
            }
        }, 3000);

        ivRecordAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ivDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //if(from.equals("1")){
                    AddFilterActivity.openAddFilter(AfterVideoCaptureActivity.this, videoURL);
                    finish();
               /* }else {
                    new AsyncTextVideo().execute(VideoEditor.createVideoFile(AfterVideoCaptureActivity.this, "video").getAbsolutePath(), videoURL);
                }*/
            }
        });


    }

    private void startVideo() {

        // When the video file ready for playback.
        this.videoView.setOnPreparedListener(new OnPreparedListener() {

            public void onPrepared(MediaPlayer mediaPlayer) {

                videoView.seekTo(position);
                if (position == 0) {
                    videoView.start();
                    setVideoProgress();
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

        Uri uri = Uri.parse(videoURL);
        //Setting MediaController and URI, then starting the videoView
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

    // display video progress
    public void setVideoProgress() {
        //get the video duration
        current_pos = videoView.getCurrentPosition();
        total_duration = videoView.getDuration() - 1;

        seekBar.setMax((int) total_duration);
        final Handler handler = new Handler();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    current_pos = videoView.getCurrentPosition();
                    seekBar.setProgress((int) current_pos);
                    handler.postDelayed(this, 1000);
                } catch (IllegalStateException ed) {
                    ed.printStackTrace();
                }
            }
        };
        handler.postDelayed(runnable, 1000);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == VIDEO_TRIMMER) {
                position = 0;
                videoURL = data.getExtras().getString("data");
                startVideo();
            }
        }
    }
}
