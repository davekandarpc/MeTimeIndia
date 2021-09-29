package com.metime.activity;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.media.MediaMuxer;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.metime.BaseActivity;
import com.metime.R;
import com.metime.adapter.AudioItemAdapter;
import com.metime.model.AudioModel;
import com.metime.model.AudioPayloadModel;
import com.metime.retrofit.ApiManager;
import com.metime.utils.Utils;
import com.metime.utils.VideoEditor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddMusicActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private TextView tvNext;
    AudioItemAdapter audioItemAdapter;
    private RelativeLayout rlSearch;
    private EditText edtSearch;
    private LinearLayout llDataNotFound;
    private ImageView ivback;
    MediaPlayer player = new MediaPlayer();

    AudioPayloadModel audioPayloadModel;

    LinearLayoutManager layoutManager;
    List<AudioModel> data = new ArrayList<AudioModel>();
    boolean isLoading = false;
    boolean isLastPage = false;
    boolean inCresePage = false;

    public static void openAddMusic(Context context, String data) {
        Intent intent = new Intent(context, AddMusicActivity.class);
        intent.putExtra("path", data);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_music);
        // initToolbarWithBackButton("Songs");

        this.recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        this.tvNext = (TextView) findViewById(R.id.tvNext);
        this.rlSearch = (RelativeLayout) findViewById(R.id.rlSearch);
        this.edtSearch = (EditText) findViewById(R.id.edtSearch);
        this.llDataNotFound = (LinearLayout) findViewById(R.id.llDataNotFound);
        this.ivback = (ImageView) findViewById(R.id.ivback);

        edtSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    data.clear();
                    performSearch();
                    return true;
                }
                return false;
            }
        });
        rlSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                data.clear();
                performSearch();
            }
        });

        ivback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        audioPayloadModel = new AudioPayloadModel();
        audioPayloadModel.setSearchTerm("");
        audioPayloadModel.setPageSize(10);
        audioPayloadModel.setPageNumber(1);
        setupRecycler();

        getAudioList();

        tvNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                AudioModel audioModel = audioItemAdapter.getSelectedModel();
                //finish();
                //AddVideoActivity.openAddVideo(AddMusicActivity.this, getIntent().getExtras().getString("path"));
                if (audioModel == null) {
                    Utils.showToast(AddMusicActivity.this, "No Music Selected");
                    finish();
                } else {
                    if (getIntent().getExtras().getString("path").isEmpty()) {
                        Intent data = new Intent();
                        data.putExtra("data", audioModel.getCompletePath());
                        setResult(RESULT_OK, data);
                        finish();
                    } else {
                        Log.i("TAG", "audiopath== " + audioModel.getCompletePath());
                        Log.i("TAG", "videopath== " + getIntent().getExtras().getString("path"));
                        if (audioModel.getAudioFile().contains(".aac")) {
                            mux(audioModel.getCompletePath(), getIntent().getExtras().getString("path"), VideoEditor.createVideoFile(AddMusicActivity.this, "merge").getAbsolutePath());
                            //muxVideoPlayer(getIntent().getExtras().getString("path"), audioModel.getCompletePath());
                        } else {
                            Utils.showToast(AddMusicActivity.this, "Music you select not supported at.");
                        }
                    }
                }
            }
        });
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

    private void getAudioList() {
        if (inCresePage == false) {
            Utils.showProgressDialog(this);
        } else {
            Utils.showProgressDialogBottom(this);
        }

        Call<List<AudioModel>> call = ApiManager.shared(this).service.getAudioList(audioPayloadModel);

        call.enqueue(new Callback<List<AudioModel>>() {
            @Override
            public void onResponse(Call<List<AudioModel>> call, Response<List<AudioModel>> response) {
                if (inCresePage == false) {
                    Utils.hideProgressDialog();
                    Log.e("Sanjay", "when increas false" + inCresePage);
                } else {
                    Utils.hideProgressDiloagBottom();
                    Log.e("Sanjay", "when increas true" + inCresePage);
                }
                isLoading = false;
                setData(response.body());
            }

            @Override
            public void onFailure(Call<List<AudioModel>> call, Throwable t) {
                if (inCresePage == false) {
                    Utils.hideProgressDialog();

                } else {
                    Utils.hideProgressDiloagBottom();
                }
                t.printStackTrace();
            }
        });
    }

    private void performSearch() {
        audioPayloadModel.setSearchTerm(edtSearch.getText().toString().trim());
        audioPayloadModel.setPageNumber(1);
        isLastPage = false;
        getAudioList();
    }

    void setupRecycler() {
        layoutManager = new LinearLayoutManager(AddMusicActivity.this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        audioItemAdapter = new AudioItemAdapter(data, AddMusicActivity.this, new AudioItemAdapter.AudioSelectListener() {
            @Override
            public void onAudioSelectListener(AudioModel audioModel) {
                reset();

                if (audioModel == null)
                    return;

                if (audioModel.getCompletePath().isEmpty())
                    Utils.showToast(AddMusicActivity.this, "Music file not exist.");
                else
                    playMusic(audioModel.getCompletePath());
            }
        });
        recyclerView.setAdapter(audioItemAdapter);

        // Pagination
        recyclerView.addOnScrollListener(recyclerViewOnScrollListener);
    }

    void setData(List<AudioModel> newData) {

        if (newData != null) {
            data.addAll(newData);
        }

        if (newData == null || newData.size() == 0)
            isLastPage = true;

        if (data != null && data.size() > 0) {
            llDataNotFound.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            if (audioItemAdapter == null)
                setupRecycler();
            else
                audioItemAdapter.notifyDataSetChanged();

        } else {
            llDataNotFound.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
    }


    void mux(String audioFile, String videoFile, String outFile) {
        try {
            // Init extractors which will get encoded frames
            MediaExtractor videoExtractor = new MediaExtractor();
            videoExtractor.setDataSource(videoFile);
            videoExtractor.selectTrack(0); // Assuming only one track per file. Adjust code if this is not the case.
            MediaFormat videoFormat = videoExtractor.getTrackFormat(0);

            long videoDuration = videoFormat.getLong(MediaFormat.KEY_DURATION);

            MediaExtractor audioExtractor = new MediaExtractor();
            audioExtractor.setDataSource(audioFile, null);
            audioExtractor.selectTrack(0); // Assuming only one track per file. Adjust code if this is not the case.
            MediaFormat audioFormat = audioExtractor.getTrackFormat(0);

            // Init muxer
            MediaMuxer muxer = new MediaMuxer(outFile, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

            MediaMetadataRetriever retrieverSrc = new MediaMetadataRetriever();
            retrieverSrc.setDataSource(videoFile);
            String degreesString = retrieverSrc.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
            if (degreesString != null) {
                int degrees = Integer.parseInt(degreesString);
                if (degrees >= 0) {
                    muxer.setOrientationHint(degrees);
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

            Toast.makeText(getApplicationContext(), "Merge Successfully", Toast.LENGTH_SHORT).show();

            Intent data = new Intent();
            data.putExtra("data", outFile);
            setResult(RESULT_OK, data);
            finish();

        } catch (Exception e) {
            Log.d("MSG", "Mixer Error 2 " + e.getMessage());
        }
    }

    /*private void mixingVideoAudio(String videoFileUri, String audioFileUri) {
        String outputFile = "";

        try {
            String path = getIntent().getExtras().getString("path");
            String FileName = path.substring(path.lastIndexOf("/") + 1);
            Log.i("TAG", "Converted File==== " + FileName);
            outputFile = getExternalFilesDir("video") + "/" + new SimpleDateFormat("yyyyMM_dd-HHmmss").format(new Date()) + "merge.mp4";

            MediaExtractor videoExtractor = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                videoExtractor = new MediaExtractor();
            }
            videoExtractor.setDataSource(videoFileUri);

            MediaExtractor audioExtractor = new MediaExtractor();
            audioExtractor.setDataSource(audioFileUri, null);

            Log.d("MSG", "Video Extractor Track Count " + videoExtractor.getTrackCount());
            Log.d("MSG", "Audio Extractor Track Count " + audioExtractor.getTrackCount());

            MediaMuxer muxer = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                muxer = new MediaMuxer(outputFile, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            }

            videoExtractor.selectTrack(0);
            MediaFormat videoFormat = videoExtractor.getTrackFormat(0);
            int videoTrack = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                videoTrack = muxer.addTrack(videoFormat);
            }

            audioExtractor.selectTrack(0);
            MediaFormat audioFormat = audioExtractor.getTrackFormat(0);
            int audioTrack = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                audioTrack = muxer.addTrack(audioFormat);
            }

            Log.d("MSG", "Video Format " + videoFormat.toString());
            Log.d("MSG", "Audio Format " + audioFormat.toString());

            boolean sawEOS = false;
            int frameCount = 0;
            int offset = 100;
            int sampleSize = 256 * 1024;
            ByteBuffer videoBuf = ByteBuffer.allocate(sampleSize);
            ByteBuffer audioBuf = ByteBuffer.allocate(sampleSize);
            MediaCodec.BufferInfo videoBufferInfo = new MediaCodec.BufferInfo();
            MediaCodec.BufferInfo audioBufferInfo = new MediaCodec.BufferInfo();


            videoExtractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
            audioExtractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                muxer.start();
            }

            while (!sawEOS) {
                videoBufferInfo.offset = offset;
                videoBufferInfo.size = videoExtractor.readSampleData(videoBuf, offset);

                if (videoBufferInfo.size < 0) {
                    Log.d("MSG", "saw input EOS.");
                    sawEOS = true;
                    videoBufferInfo.size = 0;

                } else {
                    videoBufferInfo.presentationTimeUs = videoExtractor.getSampleTime();
                    videoBufferInfo.flags = MediaCodec.BUFFER_FLAG_KEY_FRAME;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                        muxer.writeSampleData(videoTrack, videoBuf, videoBufferInfo);
                    }
                    videoExtractor.advance();

                    audioBufferInfo.offset = offset;
                    audioBufferInfo.size = audioExtractor.readSampleData(audioBuf, offset);

                    if (audioBufferInfo.size < 0) {
                        Log.d("MSG", "saw input EOS.");
                        audioBufferInfo.size = 0;
                    } else {
                        audioBufferInfo.presentationTimeUs = audioExtractor.getSampleTime();
                        audioBufferInfo.flags = MediaCodec.BUFFER_FLAG_KEY_FRAME;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                            muxer.writeSampleData(audioTrack, audioBuf, audioBufferInfo);
                        }
                        audioExtractor.advance();
                    }

                    frameCount++;
                    Log.d("MSG", "Frame (" + frameCount + ") Video PresentationTimeUs:" + videoBufferInfo.presentationTimeUs + " Flags:" + videoBufferInfo.flags + " Size(KB) " + videoBufferInfo.size / 1024);
                    Log.d("MSG", "Frame (" + frameCount + ") Audio PresentationTimeUs:" + audioBufferInfo.presentationTimeUs + " Flags:" + audioBufferInfo.flags + " Size(KB) " + audioBufferInfo.size / 1024);

                }
            }

            Toast.makeText(getApplicationContext(), "frame:" + frameCount, Toast.LENGTH_SHORT).show();


            *//*boolean sawEOS2 = false;
            int frameCount2 = 0;
            while (!sawEOS2) {
                audioBufferInfo.offset = offset;
                audioBufferInfo.size = audioExtractor.readSampleData(audioBuf, offset);

                if ( videoBufferInfo.size < 0 || audioBufferInfo.size < 0) {
                    Log.d("MSG", "saw input EOS.");
                    sawEOS2 = true;
                    audioBufferInfo.size = 0;
                } else {
                    audioBufferInfo.presentationTimeUs = audioExtractor.getSampleTime();
                    audioBufferInfo.flags = MediaCodec.BUFFER_FLAG_KEY_FRAME;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                        muxer.writeSampleData(audioTrack, audioBuf, audioBufferInfo);
                    }
                    audioExtractor.advance();
                    frameCount2++;
                    Log.d("MSG", "Frame (" + frameCount + ") Video PresentationTimeUs:" + videoBufferInfo.presentationTimeUs + " Flags:" + videoBufferInfo.flags + " Size(KB) " + videoBufferInfo.size / 1024);
                    Log.d("MSG", "Frame (" + frameCount + ") Audio PresentationTimeUs:" + audioBufferInfo.presentationTimeUs + " Flags:" + audioBufferInfo.flags + " Size(KB) " + audioBufferInfo.size / 1024);
                }
            }*//*

            Toast.makeText(getApplicationContext(), "Merge Successfully", Toast.LENGTH_SHORT).show();

            Intent data = new Intent();
            data.putExtra("data", outputFile);
            setResult(RESULT_OK, data);
            finish();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                muxer.stop();
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                muxer.release();
            }
        } catch (Exception e) {
            Log.d("MSG", "Mixer Error 2 " + e.getMessage());
        }
    }*/

    private void playMusic(String file) {
        try {
            reset();
            Uri uri = Uri.parse(file);
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.setDataSource(this, uri);
            player.prepare();
            player.setLooping(true);
            player.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                try {
                    mp.start();
                    mp.setLooping(true);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });*/
    }

    private void reset() {
        if (player.isPlaying()) {
            player.reset();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        reset();
    }

    private RecyclerView.OnScrollListener recyclerViewOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int visibleItemCount = layoutManager.getChildCount();
            int totalItemCount = layoutManager.getItemCount();
            int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

            if (!isLoading && !isLastPage) {
                if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                        && firstVisibleItemPosition >= 0
                        && totalItemCount >= 10) {

                    isLoading = true;
                    inCresePage = true;
                    audioPayloadModel.setPageNumber((audioPayloadModel.getPageNumber() + 1));
                    getAudioList();
                }
            }
        }
    };
}
