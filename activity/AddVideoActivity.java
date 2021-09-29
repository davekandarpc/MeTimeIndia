package com.metime.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.metime.BaseActivity;
import com.metime.R;
import com.metime.model.CreateVideoPayloadModel;
import com.metime.model.HashTagModel;
import com.metime.model.HashTagPayloadModel;
import com.metime.model.VideoModel;
import com.metime.retrofit.ApiManager;
import com.metime.utils.BlobManager;
import com.metime.utils.Constant;
import com.metime.utils.PreferenceHelper;
import com.metime.utils.SpaceTokenizer;
import com.metime.utils.Utils;
import com.metime.utils.VideoEditor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.List;

import pyxis.uzuki.live.mediaresizer.MediaResizer;
import pyxis.uzuki.live.mediaresizer.MediaResizerGlobal;
import pyxis.uzuki.live.mediaresizer.data.ResizeOption;
import pyxis.uzuki.live.mediaresizer.data.VideoResizeOption;
import pyxis.uzuki.live.mediaresizer.model.MediaType;
import pyxis.uzuki.live.mediaresizer.model.ScanRequest;
import pyxis.uzuki.live.mediaresizer.model.VideoResolutionType;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddVideoActivity extends BaseActivity implements BlobManager.OnVideoUploadListener {

    private TextView tvVideoPermission;
    private Button btnUpload, btnPrivacySettings;
    private ImageButton btnBackArrow;
    private LinearLayout btnpublicllayout;
    private Switch swt_saveBtn;
    private ImageView ivCoverPhoto;

    EditText edtTitle, edtDescription;
    MultiAutoCompleteTextView multiAutoCompleteTextView;
    ProgressBar progressBar;
    HashTagPayloadModel hashTagPayloadModel = new HashTagPayloadModel();

    boolean isPublicVideo = true;

    public static void openAddVideo(Context context, String data) {
        Intent intent = new Intent(context, AddVideoActivity.class);
        intent.putExtra("path", data);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_video);
        // initToolbarWithBackButton("Post");

        this.multiAutoCompleteTextView = (MultiAutoCompleteTextView) findViewById(R.id.multiAutoCompleteTextView);
        this.multiAutoCompleteTextView.setThreshold(1);
        this.multiAutoCompleteTextView.setTokenizer(new SpaceTokenizer());//(new MultiAutoCompleteTextView.CommaTokenizer());
        this.progressBar = (ProgressBar) findViewById(R.id.progressBar);

        this.btnUpload = (Button) findViewById(R.id.btnUpload);
        this.btnPrivacySettings = (Button) findViewById(R.id.btn_privacy_settings);

        this.tvVideoPermission = (TextView) findViewById(R.id.tvVideoPermission);
        this.btnpublicllayout = (LinearLayout) findViewById(R.id.btnlayout);
        this.btnBackArrow = (ImageButton) findViewById(R.id.backarrow_pr);
        this.swt_saveBtn = (Switch) findViewById(R.id.switch_saving);
        this.edtTitle = (EditText) findViewById(R.id.edtTitle);
        this.edtDescription = (EditText) findViewById(R.id.edtDescription);
        this.ivCoverPhoto = (ImageView) findViewById(R.id.ivCoverPhoto);


        Uri uri = Uri.parse(getIntent().getExtras().getString("path"));

        File f = new File(getIntent().getExtras().getString("path"));

        Glide.with(this)
                .load(f)
                .into(ivCoverPhoto);

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.e("hash", getHashTagIds());

                //validation Here
                if (edtTitle.getText().toString().trim().isEmpty()) {
                    edtTitle.setError("Please enter title.");
                    edtTitle.requestFocus();
                    return;
                }

                if (edtDescription.getText().toString().trim().isEmpty()) {
                    edtDescription.setError("Please enter description.");
                    edtDescription.requestFocus();
                    return;
                }


               /* int outputWidth = 720;
                int outputHeight = 1280;
               int bitrate =  (int)0.25 * 30 * outputWidth * outputHeight;

                new Mp4Composer(getIntent().getExtras().getString("path"), VideoEditor.createVideoFile(AddVideoActivity.this, "_compressing").getAbsolutePath())
                        .size(outputWidth, outputHeight)
                        .fillMode(FillMode.PRESERVE_ASPECT_FIT)
                        .videoBitrate(bitrate)
                        .listener(new Mp4Composer.Listener() {
                            @Override
                            public void onProgress(double progress) {
                                Log.d("TAG", "onProgress = " + progress);
                            }

                            @Override
                            public void onCompleted() {
                                Log.d("TAG", "onCompleted()");
                                runOnUiThread(() -> {
                                    Toast.makeText(AddVideoActivity.this, "codec complete path", Toast.LENGTH_SHORT).show();
                                });
                            }

                            @Override
                            public void onCanceled() {
                                Log.d("TAG", "onCanceled");
                            }

                            @Override
                            public void onFailed(Exception exception) {
                                Log.e("TAG", "onFailed()", exception);
                            }
                        })
                        .start();*/

                VideoResizeOption resizeOption = new VideoResizeOption.Builder()
                        .setVideoResolutionType(VideoResolutionType.AS480)
                        .setVideoBitrate(2000 * 1000)
                        .setAudioBitrate(-1)
                        .setAudioChannel(-1)
                        .setScanRequest(ScanRequest.TRUE)
                        .build();

                ResizeOption option = new ResizeOption.Builder()
                        .setMediaType(MediaType.VIDEO)
                        .setVideoResizeOption(resizeOption)
                        .setTargetPath(getIntent().getExtras().getString("path"))
                        .setOutputPath(VideoEditor.createVideoFile(AddVideoActivity.this, "_compressing").getAbsolutePath())
                        .setCallback((code, output) -> {
                            Utils.hideProgressDialog();
                            Log.e("file path", output);
                            new AsyncTaskUploadData().execute(output);
                        }).build();

                MediaResizer.process(option);
                Utils.showProgressDialog(AddVideoActivity.this);
            }
        });


        btnPrivacySettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.i("TAG==", "Here when you click button");
                startActivity(new Intent(AddVideoActivity.this, PrivacySettings.class));
            }
        });

        btnpublicllayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent videoPermission = new Intent(AddVideoActivity.this, ViewPermissionActivity.class);
                startActivityForResult(videoPermission, Constant.INTENT.VIDEO_VIEW_PERMISSION);
            }
        });
        btnBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        multiAutoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String str = multiAutoCompleteTextView.getText().toString();
                String[] data = str.split(" ");
                String searchQuery = data[data.length - 1].trim();

                if (searchQuery.isEmpty())
                    return;

                hashTagPayloadModel.setSearchTerm(searchQuery);
                getHashTagList();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        MediaResizerGlobal.initializeApplication(this);

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

    private void addVideo(String fileName) {

        if (fileName.isEmpty())
            return;

        Utils.showProgressDialog(this);

        CreateVideoPayloadModel createVideoPayloadModel = new CreateVideoPayloadModel();
        createVideoPayloadModel.setVideoTitle(edtTitle.getText().toString());
        createVideoPayloadModel.setVideoDescription(edtDescription.getText().toString());
        createVideoPayloadModel.setVideoFileName(fileName);
        createVideoPayloadModel.setApproved(false);
        createVideoPayloadModel.setPublic(isPublicVideo);
        createVideoPayloadModel.setHashTags(getHashTagIds());
        createVideoPayloadModel.setObjectId(PreferenceHelper.getInstance(this).getObjectId());


        Call<VideoModel> call = ApiManager.shared(this).service.AddVideo(createVideoPayloadModel);

        call.enqueue(new Callback<VideoModel>() {
            @Override
            public void onResponse(Call<VideoModel> call, Response<VideoModel> response) {
                Utils.hideProgressDialog();
                Toast.makeText(AddVideoActivity.this, "Video added successfully.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }

            @Override
            public void onFailure(Call<VideoModel> call, Throwable t) {
                Utils.hideProgressDialog();
                t.printStackTrace();
            }
        });
    }


    private String getHashTagIds() {
        String[] data = multiAutoCompleteTextView.getText().toString().split(" ");

        if(data == null || data.length == 0)
            return "";

        String tagStr = "";

        for(int i = 0; i < data.length; i++ ){
            String s = data[i].trim();
            if(s.isEmpty())
                continue;

            if(tagStr.isEmpty())
                tagStr = s;
            else
                tagStr = tagStr +", "+ s;
        }

        return tagStr;
    }


    @Override
    public void onOnVideoUploaded(String filename) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //addVideo(filename);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            /*if (requestCode == Constant.INTENT.SELECT_HASH_TAG) {
                selectedHashTagData = data.getParcelableArrayListExtra("data");
                showSelectedData();
            } else */
            if (requestCode == Constant.INTENT.VIDEO_VIEW_PERMISSION) {
                isPublicVideo = data.getBooleanExtra("data", true);
                if (isPublicVideo)
                    tvVideoPermission.setText("Public");
                else
                    tvVideoPermission.setText("Followers");
            }
        }
    }

    private class AsyncTaskUploadData extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            String fname = "";
            try {
                File f = new File(params[0]);
                if (f != null) {

                    Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(f.getAbsolutePath(), MediaStore.Video.Thumbnails.MINI_KIND);

                    //Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(f, new Size(500, 500), null);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    thumbnail.compress(Bitmap.CompressFormat.PNG, 100, baos);
                    InputStream is = new ByteArrayInputStream(baos.toByteArray());


                    fname = BlobManager.UploadVideo(params[0], is, is.available(), AddVideoActivity.this);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return fname;
        }


        @Override
        protected void onPostExecute(String result) {
            // execution of result of Long time consuming operation
            Utils.hideProgressDialog();
            addVideo(result);
        }


        @Override
        protected void onPreExecute() {
            Utils.showProgressDialog(AddVideoActivity.this);
        }


        @Override
        protected void onProgressUpdate(String... text) {
            Log.e("Sanjay", "Progress" + text[0]);

        }
    }

    private void getHashTagList() {
        // display a progress dialog

        progressBar.setVisibility(View.VISIBLE);
        Call<List<HashTagModel>> call = ApiManager.shared(this).service.getHashTagList(hashTagPayloadModel);

        call.enqueue(new Callback<List<HashTagModel>>() {
            @Override
            public void onResponse(Call<List<HashTagModel>> call, Response<List<HashTagModel>> response) {
                progressBar.setVisibility(View.GONE);

                String[] data = new String[response.body().size()];
                for (int i = 0; i < response.body().size(); i++) {
                    //data.add(hash.getTagName());
                    data[i] = response.body().get(i).getTagName();
                }

                ArrayAdapter tagArray = new ArrayAdapter(AddVideoActivity.this, R.layout.row_hash_tag_item_select, data);
                multiAutoCompleteTextView.setAdapter(tagArray);
                tagArray.notifyDataSetChanged();
                //setData(response.body());
            }

            @Override
            public void onFailure(Call<List<HashTagModel>> call, Throwable t) {
                t.printStackTrace();
                progressBar.setVisibility(View.GONE);
            }
        });
    }


}
