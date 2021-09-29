package com.metime.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.metime.R;
import com.metime.model.UserModel;
import com.metime.model.payload.EditProfilePayloadModel;
import com.metime.retrofit.ApiManager;
import com.metime.utils.BlobManager;
import com.metime.utils.PreferenceHelper;
import com.metime.utils.Utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends Activity {
    private EditText edt_name, edtLastName, edt_bio, edt_id, edt_instagram, edt_youtube;
    private Button btn_saveData;
    private ImageButton btn_backArrow;
    private ImageView iv_Camera;
    Bitmap selectedImage = null;
    UserModel userModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        edt_name = (EditText) findViewById(R.id.edt_name_e_edit);
        edtLastName = (EditText) findViewById(R.id.edtLastName);
        edt_id = (EditText) findViewById(R.id.edt_userid_e_edit);
        edt_bio = (EditText) findViewById(R.id.edt_bio_e_edit);
        edt_instagram = (EditText) findViewById(R.id.edt_insta_e_edit);
        edt_youtube = (EditText) findViewById(R.id.edt_youtub_e_edit);
        btn_saveData = (Button) findViewById(R.id.btnSaveData);
        btn_backArrow = (ImageButton) findViewById(R.id.backarrow_editprofile);
        iv_Camera = (ImageView) findViewById(R.id.iv_camera);


        btn_backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btn_saveData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedImage != null)
                    new AsyncTaskProfileImageUploadData().execute("");
                else
                    editUserProfile("");
            }
        });

        iv_Camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectUserImage();
            }
        });

        getUserProfile();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            if (requestCode == 1) {
                selectedImage = (Bitmap) data.getExtras().get("data");
                iv_Camera.setImageBitmap(selectedImage);
            } else if (requestCode == 2) {
                Uri selectedImageURI = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                if (selectedImageURI != null) {
                    Cursor cursor = getContentResolver().query(selectedImageURI,
                            filePathColumn, null, null, null);
                    if (cursor != null) {
                        cursor.moveToFirst();

                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        String picturePath = cursor.getString(columnIndex);
                        selectedImage = BitmapFactory.decodeFile(picturePath);
                        iv_Camera.setImageBitmap(selectedImage);
                        cursor.close();
                    }
                }
            }
        }
    }

    //Select User Image Method to choose image from gallery
    private void selectUserImage() {

        CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        AlertDialog.Builder al_builder = new AlertDialog.Builder(this);
        al_builder.setTitle("Add Photo");
        al_builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Take Photo")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    //  File imgFile = new File(android.os.Environment.getExternalStorageDirectory(),"temp.jpg");
                    // intent.putExtra(MediaStore.EXTRA_OUTPUT,Uri.fromFile(imgFile));
                    startActivityForResult(intent, 1);

                } else if (options[item].equals("Choose from Gallery")) {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, 2);
                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        al_builder.show();
    }

    //to edit profile image
    private void editUserProfile(String profileImage) {

        Utils.showProgressDialog(this);

        EditProfilePayloadModel editProfilePayloadModel = new EditProfilePayloadModel();

        editProfilePayloadModel.setFirstName(edt_name.getText().toString().trim());
        editProfilePayloadModel.setLastName(edtLastName.getText().toString().trim());
        editProfilePayloadModel.setBio(edt_bio.getText().toString().trim());
        editProfilePayloadModel.setInstagram(edt_instagram.getText().toString().trim());
        editProfilePayloadModel.setYouTube(edt_youtube.getText().toString().trim());
        if (profileImage.isEmpty()){
            editProfilePayloadModel.setProfilePicture(userModel.getProfilePicture());
        }else {
            editProfilePayloadModel.setProfilePicture(profileImage);
        }

        Call<UserModel> call = ApiManager.shared(this).service.editUserProfile(editProfilePayloadModel);

        call.enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                Utils.hideProgressDialog();
                Toast.makeText(EditProfileActivity.this, "Profile edited successfully.", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onFailure(Call<UserModel> call, Throwable t) {
                Utils.hideProgressDialog();
                t.printStackTrace();
            }
        });
    }

    private void getUserProfile() {

        Utils.showProgressDialog(EditProfileActivity.this);
        Call<UserModel> call = ApiManager.shared(EditProfileActivity.this).service.getProfile(PreferenceHelper.getInstance(this).getObjectId());

        call.enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                Utils.hideProgressDialog();

                userModel = (UserModel) response.body();
                if (userModel != null) {
                    edt_name.setText(userModel.getFirstName());
                    edtLastName.setText(userModel.getLastName());
                    edt_id.setText(userModel.getEmail());
                    edt_bio.setText(userModel.getBio());
                    edt_instagram.setText(userModel.getInstagram());
                    edt_youtube.setText(userModel.getYouTube());

                    if (!userModel.getProfileFullPath().isEmpty()) {
                        Glide.with(EditProfileActivity.this)
                                .load(userModel.getProfileFullPath())
                                .into(iv_Camera);
                    } else {
                        iv_Camera.setImageDrawable(getDrawable(R.drawable.ic_camera));
                    }
                }

            }

            @Override
            public void onFailure(Call<UserModel> call, Throwable t) {
                Utils.hideProgressDialog();
                t.printStackTrace();
            }
        });
    }

    private class AsyncTaskProfileImageUploadData extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            String fname = "";
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                selectedImage.compress(Bitmap.CompressFormat.PNG, 100, baos);
                InputStream is = new ByteArrayInputStream(baos.toByteArray());

                fname = BlobManager.uploadProfileImage(is, is.available());

            } catch (Exception e) {
                e.printStackTrace();
            }

            return fname;
        }


        @Override
        protected void onPostExecute(String result) {
            // execution of result of Long time consuming operation
            Utils.hideProgressDialog();
            editUserProfile(result);
        }


        @Override
        protected void onPreExecute() {
            Utils.showProgressDialog(EditProfileActivity.this);
        }


        @Override
        protected void onProgressUpdate(String... text) {
            Log.e("Sanjay", "Progress" + text[0]);
        }
    }
}