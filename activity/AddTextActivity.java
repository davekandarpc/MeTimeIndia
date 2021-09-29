package com.metime.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.media.MediaMuxer;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.metime.BaseActivity;
import com.metime.R;
import com.metime.adapter.TextColorAdapter;
import com.metime.addtextutils.AddTextToVideoProcessor;
import com.metime.utils.Utils;
import com.metime.utils.VideoEditor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class AddTextActivity extends BaseActivity implements View.OnTouchListener {

    private VideoView videoView;
    private int position = 0;
    private MediaController mediaController;
    RecyclerView recyclerView;
    String videoURL = "";

    ImageView ivAddText, ivAlignment;
    EditText edtText;
    TextView tvDone;
    String colorCode = "#FFFFFF";
    String fontName = "sansapro_normal.OTF";


    private ViewGroup mRrootLayout;
    private View flTextParent;
    private float _xDelta = 0;
    private float _yDelta = 0;

    private RadioGroup rdgpFont;
    private SeekBar sbFontSize;

    public static int CODE_PROCESSING_FINISHED = 6662;
    String outFile = "";
    Bitmap bitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_text);

        videoURL = getIntent().getExtras().getString("path");


        this.mRrootLayout = (ViewGroup) findViewById(R.id.mRrootLayout);
        this.flTextParent = (View) findViewById(R.id.flTextParent);
        this.rdgpFont = (RadioGroup) findViewById(R.id.rdgpFont);
        this.sbFontSize = (SeekBar) findViewById(R.id.sbFontSize);
        rdgpFont.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rdbtClassic) {
                    fontName = "sansapro_normal.OTF";
                } else if (checkedId == R.id.rdbtBold) {
                    fontName = "sansapro_bold.OTF";
                } else if (checkedId == R.id.rdbtCure) {
                    fontName = "sansapro_light.OTF";
                }
                setFontTypeface();
            }
        });
        this.recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        this.ivAddText = (ImageView) findViewById(R.id.ivAddText);
        this.ivAlignment = (ImageView) findViewById(R.id.ivAlignment);
        this.edtText = (EditText) findViewById(R.id.edtText);
        this.edtText.setOnTouchListener(this);
        edtText.setTextSize(30);

        this.tvDone = (TextView) findViewById(R.id.tvDone);

        this.videoView = (VideoView) findViewById(R.id.videoView);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        ViewGroup.LayoutParams params = (ViewGroup.LayoutParams) videoView.getLayoutParams();
        params.width = metrics.widthPixels;
        params.height = metrics.heightPixels;
        videoView.setLayoutParams(params);


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


        ArrayList<Integer> data = new ArrayList<>();
        data.add(Color.rgb(255, 255, 255));
        data.add(Color.rgb(35, 31, 32));
        data.add(Color.rgb(231, 57, 121));
        data.add(Color.rgb(155, 50, 181));
        data.add(Color.rgb(227, 27, 31));
        data.add(Color.rgb(252, 103, 50));
        data.add(Color.rgb(253, 197, 76));
        data.add(Color.rgb(116, 186, 89));
        data.add(Color.rgb(34, 201, 246));
        data.add(Color.rgb(119, 171, 246));

        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(new TextColorAdapter(data, new TextColorAdapter.OnColorListeners() {
            @Override
            public void onColorSelect(Integer color) {
                edtText.setTextColor(color);
                colorCode = String.format("#%06X", (0xFFFFFF & color));
            }
        }));


        ivAddText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sbFontSize.getVisibility() == View.VISIBLE)
                    sbFontSize.setVisibility(View.GONE);
                else
                    sbFontSize.setVisibility(View.VISIBLE);
            }
        });

        ivAlignment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*_xDelta = 0;
                _yDelta = 0;
                if (((FrameLayout.LayoutParams) edtText.getLayoutParams()).gravity == Gravity.LEFT)
                    ((FrameLayout.LayoutParams) edtText.getLayoutParams()).gravity = Gravity.CENTER;
                else if (((FrameLayout.LayoutParams) edtText.getLayoutParams()).gravity == Gravity.CENTER)
                    ((FrameLayout.LayoutParams) edtText.getLayoutParams()).gravity = Gravity.RIGHT;
                else
                    ((FrameLayout.LayoutParams) edtText.getLayoutParams()).gravity = Gravity.LEFT;
                edtText.invalidate();*/
            }
        });

        tvDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str = edtText.getText().toString();
                if (str.isEmpty()) {
                    Utils.showToast(AddTextActivity.this, "Please add text.");
                    return;
                }

                bitmap = loadBitmapFromView(flTextParent, flTextParent.getWidth(), flTextParent.getHeight());

                outFile = VideoEditor.createVideoFile(AddTextActivity.this, "text").getAbsolutePath();

                new AsyncTextVideo().execute(outFile, getIntent().getExtras().getString("path"));
            }
        });

        startVideo();

        sbFontSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                edtText.setTextSize(progress);
                edtText.invalidate();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void setFontTypeface() {
        Typeface face = Typeface.createFromAsset(getAssets(),
                "font/" + fontName);
        edtText.setTypeface(face);
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
    public boolean onTouch(View view, MotionEvent event) {
        boolean handle = false;
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                _xDelta = view.getX() - event.getRawX();
                _yDelta = view.getY() - event.getRawY();
                break;

            case MotionEvent.ACTION_MOVE:
                handle = true;
                view.animate()
                        .x(event.getRawX() + _xDelta)
                        .y(event.getRawY() + _yDelta)
                        .setDuration(0)
                        .start();
                break;
            default:
                return false;
        }
        return handle;
    }


    private class AsyncTextVideo extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {

            /**
             * 0 Output file
             * 1 Input file
             *
             */

            try {

                new AddTextToVideoProcessor().process(params[0], getContentResolver().openFileDescriptor(Uri.fromFile(new File(params[1])), "r").getFileDescriptor(), bitmap);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            return params[0];
        }


        @Override
        protected void onPostExecute(String result) {
            // execution of result of Long time consuming operation
            Utils.hideProgressDialog();
            Log.e("Path", result);

            new AsyncAudioVideo().execute(VideoEditor.createVideoFile(AddTextActivity.this, "text").getAbsolutePath(), result, getIntent().getExtras().getString("path"));
        }

        @Override
        protected void onPreExecute() {
            Utils.showProgressDialog(AddTextActivity.this);
        }


        @Override
        protected void onProgressUpdate(String... text) {
        }
    }

    private class AsyncAudioVideo extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            /**
             * 0 Output file
             * 1 Input Video file
             * 2 Input Audio File
             *
             */
            try {
                MediaExtractor videoExtractor = new MediaExtractor();
                videoExtractor.setDataSource(params[1]);
                MediaFormat videoFormat = null;
                for (int i = 0; i < videoExtractor.getTrackCount(); i++) {
                    videoFormat = videoExtractor.getTrackFormat(i);
                    if (videoFormat.getString(MediaFormat.KEY_MIME).startsWith("video/")) {
                        videoExtractor.selectTrack(i);
                        break;
                    }
                }

                //videoExtractor.selectTrack(0); // Assuming only one track per file. Adjust code if this is not the case.
                //MediaFormat videoFormat = videoExtractor.getTrackFormat(0);
                long videoDuration = videoFormat.getLong(MediaFormat.KEY_DURATION);

                MediaExtractor audioExtractor = new MediaExtractor();
                audioExtractor.setDataSource(params[2], null);
                MediaFormat audioFormat = null;
                for (int i = 0; i < audioExtractor.getTrackCount(); i++) {
                    audioFormat = audioExtractor.getTrackFormat(i);
                    if (audioFormat.getString(MediaFormat.KEY_MIME).startsWith("audio/")) {
                        audioExtractor.selectTrack(i);
                        break;
                    }
                }


                //audioExtractor.selectTrack(1); // Assuming only one track per file. Adjust code if this is not the case.
                //MediaFormat audioFormat = audioExtractor.getTrackFormat(1);

                // Init muxer
                MediaMuxer muxer = new MediaMuxer(params[0], MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
                MediaMetadataRetriever retrieverSrc = new MediaMetadataRetriever();
                retrieverSrc.setDataSource(params[1]);
                String degreesString = retrieverSrc.extractMetadata(
                        MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
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
            } catch (IOException e) {
                e.printStackTrace();
            }

            return params[0];

        }


        @Override
        protected void onPostExecute(String result) {
            // execution of result of Long time consuming operation
            Utils.hideProgressDialog();
            Log.e("Path", result);

            Intent dataNew = new Intent();
            dataNew.putExtra("data", result);
            setResult(RESULT_OK, dataNew);
            finish();
        }

        @Override
        protected void onPreExecute() {
            Utils.showProgressDialog(AddTextActivity.this);
        }


        @Override
        protected void onProgressUpdate(String... text) {
            Log.e("Sanjay", "Progress" + text[0]);
        }
    }

    public Bitmap loadBitmapFromView(View v, int width, int height) {

        edtText.setCursorVisible(false);
        edtText.setBackground(null);

        Bitmap b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        v.layout(0, 0, width, height);

        v.draw(c);
        return b;
    }
}
