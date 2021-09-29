package com.metime.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import com.metime.BaseActivity;
import com.metime.R;
import com.videotrimmer.VideoTrimmer;
import com.videotrimmer.interfaces.OnTrimVideoListener;
import com.videotrimmer.view.RangeSeekBarView;

public class VideoTrimmerActivity extends BaseActivity implements OnTrimVideoListener {

    private VideoTrimmer mVideoTrimmer;
    TextView textSize;
    RangeSeekBarView timeLineBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_trimmer);

        Intent extraIntent = getIntent();
        String path = "";

        if (extraIntent != null) {
            path = extraIntent.getStringExtra("path");
        }

        mVideoTrimmer = ((VideoTrimmer) findViewById(R.id.timeLine));
        timeLineBar = (RangeSeekBarView) findViewById(R.id.timeLineBar);
        textSize = (TextView) findViewById(R.id.textSize);


        if (mVideoTrimmer != null && path != null) {
            mVideoTrimmer.setMaxDuration(30);
            mVideoTrimmer.setOnTrimVideoListener(this);
            mVideoTrimmer.setVideoURI(Uri.parse(path));
            mVideoTrimmer.setDestinationPath(getExternalFilesDir("video") + "/");
        }
    }

    @Override
    public void getResult(final Uri uri) {
        Intent data = new Intent();
        data.putExtra("data", uri.toString());
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    public void cancelAction() {
        mVideoTrimmer.destroy();
        finish();
    }
}
