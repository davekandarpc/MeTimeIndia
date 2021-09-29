package com.metime.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.metime.BaseActivity;
import com.metime.R;
import com.metime.adapter.AllCommentsAdepter;
import com.metime.model.AllCommentModel;
import com.metime.model.CommentsVideoPayloadModel;
import com.metime.retrofit.ApiManager;
import com.metime.utils.PreferenceHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddCommentActivity extends BaseActivity {


    private ImageView icClose;
    private Button btn_sendtocomment;
    private EditText edt_commentmessage;
    private RecyclerView recyclerView_comment;
    private AllCommentModel allComment;
    private TextView tv_commentcount;
   List<AllCommentModel> alldata;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_comment);
        // initToolbarWithBackButton("Post");

        this.icClose = (ImageView) findViewById(R.id.icClose);
        btn_sendtocomment = (Button) findViewById(R.id.btn_sendtocomment);
        edt_commentmessage = (EditText) findViewById(R.id.edtMessage);
        recyclerView_comment = (RecyclerView) findViewById(R.id.recyclerView_comment);
        tv_commentcount = (TextView) findViewById(R.id.tv_commentcount);
        GetAllComment();
        icClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.push_down_in, R.anim.push_down_out);
            }
        });
        btn_sendtocomment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoComment();
            }
        });
    }

    private void GetAllComment() {
        //Utils.showProgressDialog(this);
        String videoId = getIntent().getStringExtra("VideoId");

        Call<List<AllCommentModel>> call =ApiManager.shared(this).service.getAllComments(videoId);

        call.enqueue(new Callback<List<AllCommentModel>>() {
            @Override
            public void onResponse(Call<List<AllCommentModel>> call, Response<List<AllCommentModel>> response) {
                Log.e("Sanajay","Data from Comment"+response);
                // alldata = new List<AllCommentModel>();
                alldata = response.body();
                Log.e("Sanajay","Data from Comment"+alldata.size());
                tv_commentcount.setText(String.valueOf(alldata.size()) +" Comments");
                AllCommentsAdepter allCommentsAdepter =  new AllCommentsAdepter(alldata,getApplicationContext());
                recyclerView_comment.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                recyclerView_comment.setAdapter(allCommentsAdepter);
            }

            @Override
            public void onFailure(Call<List<AllCommentModel>> call, Throwable t) {

            }
        });
    }

    private void videoComment() {
        String videoId = getIntent().getStringExtra("VideoId");
        Log.e("Sanjay","VideoId====> "+videoId);
        Log.e("Sanjay","VideoId====> "+edt_commentmessage.getText().toString());
        CommentsVideoPayloadModel commentsmodel = new CommentsVideoPayloadModel();
        commentsmodel.setUserId(PreferenceHelper.getInstance(this).getObjectId());
        commentsmodel.setCreatedAt((new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")).format(Calendar.getInstance().getTime()));
        commentsmodel.setCommentText(edt_commentmessage.getText().toString());

        Call<ResponseBody> call = ApiManager.shared(this).service.commentVideo(videoId,commentsmodel);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.e("Sanjay","Response success"+response);
                if(response.code() == 200){
                    edt_commentmessage.setText(" ");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("Sanajay","Response in failur");
                t.printStackTrace();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.push_down_in, R.anim.push_down_out);
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

}
