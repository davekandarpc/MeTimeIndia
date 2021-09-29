package com.metime.activity;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.gson.JsonObject;
import com.metime.R;
import com.metime.adapter.ScreenShotAdepter;
import com.metime.retrofit.ApiManager;
import com.metime.utils.Utils;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class Report_problemActivity extends Activity {
    private ImageButton img_btn_backarrow;
    private EditText edt_typeproblem;
    private TextView tv_addScreenShot;
    private RecyclerView recy_screenShot;
    LinearLayoutManager layoutManager;
    private Button btn_send;
    private ImageView imageView;
    private String imageEncoded;
    ArrayList<String> imagesEncodedList;
    private List<String> imagePathList = null;

    private  Uri uripath;

    private String imagePath;
    ArrayList<Uri> mArrayUri,simpleUri;
    ArrayList<String> mArrayFile;
    private static int RESULT_LOAD_IMAGE = 1;
    private  File file;
    String path ;
    ScreenShotAdepter screenShotAdepter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_problem);

        img_btn_backarrow =(ImageButton) findViewById(R.id.backarrow_reportproblem);
        //imageView = (ImageView) findViewById(R.id.iv_screenshot);
        edt_typeproblem = (EditText) findViewById(R.id.edt_report_problem);
        tv_addScreenShot = (TextView) findViewById(R.id.tv_addScreenShot);
        btn_send = (Button) findViewById(R.id.btn_send_report);
        recy_screenShot =(RecyclerView) findViewById(R.id.recyclerView_screenshot);






        img_btn_backarrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        tv_addScreenShot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPemission();

            }
        });

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //sendEmail();
                feedbackcall();
            }
        });



    }

    private void feedbackcall() {
        Utils.showProgressDialog(this);

        Log.e("Sanjay","File when click send=== "+file);

        File file1 = new File(path);

        RequestBody requestFile = RequestBody.create(
                MediaType.parse("image/jpg"), file1
        );
        MultipartBody.Part body = MultipartBody.Part.createFormData("file",file.getName(),requestFile);
        Call<ResponseBody> call = ApiManager.shared(this).service.feedBack(edt_typeproblem.getText().toString(),body);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utils.hideProgressDialog();
                Log.e("Sanjay","Success===> "+response.code());
                Toast.makeText(Report_problemActivity.this, "Your message send successfully", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utils.hideProgressDialog();
                Log.e("Sanjay","Message ="+ t.getMessage());
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 100:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent,"Select Picture"), RESULT_LOAD_IMAGE);
                }
                break;

            default:
                break;
        }
    }
    private void getPemission() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
        } else {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent,"Select Picture"), RESULT_LOAD_IMAGE);
        }
    }

    private void sendEmail() {
        mArrayFile = new ArrayList<String>();
        simpleUri = new ArrayList<Uri>();
        Intent sendEmail = new Intent(Intent.ACTION_SEND_MULTIPLE);
        sendEmail.setType("text/plain");
        sendEmail.putExtra(Intent.EXTRA_EMAIL,new String[]{"admin@metimeindia.com"});
        sendEmail.putExtra(Intent.EXTRA_SUBJECT, new String[]{"Reporting"});
        sendEmail.putExtra(Intent.EXTRA_TEXT, edt_typeproblem.getText());

        sendEmail.putParcelableArrayListExtra(Intent.EXTRA_STREAM,mArrayUri);
        this.startActivity(Intent.createChooser(sendEmail,"send mail....."));


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK  && data != null){

            imagePathList = new ArrayList<>();
            mArrayUri = new ArrayList<Uri>();
            if(data.getClipData() != null){

                int count = data.getClipData().getItemCount();
                for (int i=0; i<count; i++){

                    mArrayFile = new ArrayList<>();
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    path = getImageFilePath(imageUri);
                    mArrayUri.add(imageUri);
                    mArrayFile.add(path);


                    Log.e("Sanjay","Flie path "+path);

                    //
                }
                Log.e("Sanjay","Flie data"+mArrayUri);
               // displayImage();
                layoutManager = new GridLayoutManager(this,3);
                recy_screenShot.setLayoutManager(layoutManager);
                screenShotAdepter = new ScreenShotAdepter(mArrayUri,this);
                recy_screenShot.setAdapter(screenShotAdepter);
            }
            else if(data.getData() != null){
               // mArrayUri = new ArrayList<Uri>();
               // mArrayFile = new ArrayList<>();
                Uri imgUri = data.getData();
                path = getImageFilePath(imgUri);
                mArrayUri.add(imgUri);
                Log.e("Sanjay","Flie path "+path);
              //  mArrayUri.add("file://"+ Uri.parse(path));
                ////displayImage();
                layoutManager = new GridLayoutManager(this,3);
                recy_screenShot.setLayoutManager(layoutManager);
                screenShotAdepter = new ScreenShotAdepter(mArrayUri,this);
                recy_screenShot.setAdapter(screenShotAdepter);
            }
        }
    }

    private String getImageFilePath(Uri uri) {
        file = new File(uri.getPath());
        Log.e("Sanjay","File data=== "+file);
        String[] filePath = file.getPath().split(":");
        String image_id = filePath[filePath.length - 1];
            imagePathList = new ArrayList<>();
        Cursor cursor = getContentResolver().query(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Images.Media._ID + " = ? ", new String[]{image_id}, null);
        if (cursor!=null) {
            cursor.moveToFirst();
            imagePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            Log.e("LOG_TAG", "Selected Images if " + imagePathList);
            cursor.close();
        }
        return imagePath;
    }

    private void displayImage() {
        for(int i = 0; i < mArrayUri.size();i++){
            Glide.with(getApplicationContext())
                    .asBitmap()
                    .load(mArrayUri.get(i))
                    .into(imageView);
        }

    }



}