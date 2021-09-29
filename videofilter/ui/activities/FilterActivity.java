package com.metime.videofilter.ui.activities;

import android.content.Intent;
import android.graphics.Point;
import android.media.MediaFormat;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.metime.BaseActivity;
import com.metime.R;
import com.metime.utils.Utils;
import com.metime.videofilter.filter.helper.MagicFilterType;
import com.metime.videofilter.ui.adapters.FilterAdapter;
import com.metime.videofilter.ui.views.VideoFilterView;
import com.metime.videofilter.utils.ConfigUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class FilterActivity extends BaseActivity implements View.OnClickListener {

    private VideoFilterView mVideoFilterView;
    private View mFilterLayout;
    private FilterAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private boolean mIsPlaying = true;
    private View mSavingLayout;
    private ProgressBar mProgressBar;
    private static String VIDEO_PATH = "";

    private ImageView ivSave, ivCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        VIDEO_PATH = getIntent().getExtras().getString("path");

        loadVideoFile();
    }


    private void loadVideoFile() {
        ConfigUtils.getInstance().setVideoPath(VIDEO_PATH);
        ConfigUtils.getInstance().setOutPutVideoPath(getExternalFilesDir("video") + "/" + new SimpleDateFormat("yyyyMM_dd-HHmmss").format(new Date()) + "filter.mp4");

        MediaFormat format = ConfigUtils.getInstance().getMediaFormat();
        if (format == null) {
            Toast.makeText(this, "Video Parsing Error", Toast.LENGTH_SHORT).show();
            finish();
        }
        int videoFrameRate = format.getInteger(MediaFormat.KEY_FRAME_RATE);
        int frameInterval = 1000 / videoFrameRate;
        ConfigUtils.getInstance().setFrameInterval(frameInterval);
        setUpLayout();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mVideoFilterView != null)
            mVideoFilterView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mVideoFilterView != null)
            mVideoFilterView.onPause();
    }

    private void controlPlaying(boolean isPlaying) {
        if (isPlaying) {
            mVideoFilterView.resume();
        } else {
            mVideoFilterView.pause();
        }
        mIsPlaying = isPlaying;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancel:
                mVideoFilterView.stopRecord();
                mFilterLayout.setVisibility(View.VISIBLE);
                //mSavingLayout.setVisibility(View.GONE);
                break;

            case R.id.ivSave:
                if (mIsPlaying) {
                    controlPlaying(false);
                }
                mProgressBar.setProgress(0);
                mVideoFilterView.startRecord();
                //mSavingLayout.setVisibility(View.VISIBLE);
                Utils.showProgressDialog(this);
                break;

            case R.id.ivCancel:
                finish();
                break;
        }
    }


    /*@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_save:

                if (mIsPlaying) {
                    controlPlaying(false);
                }
                mProgressBar.setProgress(0);
                mVideoFilterView.startRecord();
                mControllerLayout.setVisibility(View.GONE);
                mSavingLayout.setVisibility(View.VISIBLE);

                return true;
        }

        return super.onOptionsItemSelected(item);
    }*/

    protected void setUpLayout() {
        mVideoFilterView = (VideoFilterView) findViewById(R.id.video_filter_view);
        Point screenSize = new Point();
        getWindowManager().getDefaultDisplay().getSize(screenSize);
        ViewGroup.LayoutParams params = mVideoFilterView.getLayoutParams();
        params.width = screenSize.x;
        params.height = screenSize.y;


        mVideoFilterView.setLayoutParams(params);
        mVideoFilterView.setOnSaveProgress(new VideoFilterView.OnSaveProgress() {
            @Override
            public void onProgress(int progress) {
                mProgressBar.setProgress(progress);
            }

            @Override
            public void onComplete() {
                Utils.hideProgressDialog();
                Intent data = new Intent();
                data.putExtra("data", ConfigUtils.getInstance().getOutPutVideoPath());
                setResult(RESULT_OK, data);
                finish();
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_filter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        mAdapter = new FilterAdapter(this);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnFilterChangeListener(new FilterAdapter.onFilterChangeListener() {
            @Override
            public void onFilterChanged(MagicFilterType filterType) {
                mVideoFilterView.getMovieRender().setFilter(filterType);
            }

            @Override
            public void onNoChanged(int pos) {
                // mSBFilter.setVisibility(mSBFilter.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE);
            }
        });

        mSavingLayout = findViewById(R.id.layout_saving);
        mFilterLayout = findViewById(R.id.layout_filter);
        mFilterLayout.setVisibility(View.VISIBLE);

        findViewById(R.id.cancel).setOnClickListener(this);
        mProgressBar = (ProgressBar) findViewById(R.id.progress);
        ivSave = (ImageView) findViewById(R.id.ivSave);
        ivSave.setOnClickListener(this);

        ivCancel = (ImageView) findViewById(R.id.ivCancel);
        ivCancel.setOnClickListener(this);
    }


}
