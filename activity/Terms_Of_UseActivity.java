package com.metime.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ImageButton;

import com.metime.R;
import com.metime.model.ContentModel;
import com.metime.retrofit.ApiManager;
import com.metime.utils.Utils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Terms_Of_UseActivity extends Activity {

    private ImageButton img_backarrow;
    private WebView web_terms_Of_Use;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms__of__use);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading Data...");

        getDataTermOfUse();

        img_backarrow = (ImageButton) findViewById(R.id.backarrow_touse);
        web_terms_Of_Use = (WebView) findViewById(R.id.webview_touse);


        img_backarrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


    }
    /**
     * to get All terms of use data from API */
    private void getDataTermOfUse() {
        Utils.showProgressDialog(getApplicationContext());
        Call<ContentModel> call = ApiManager.shared(getApplicationContext()).service.getContent("terms of use");

        call.enqueue(new Callback<ContentModel>() {
            @Override
            public void onResponse(Call<ContentModel> call, Response<ContentModel> response) {
                Log.e("Sanjay","string value===>"+response.body().getSettingValue());
                web_terms_Of_Use.requestFocus();
                web_terms_Of_Use.getSettings().setLightTouchEnabled(true);
                web_terms_Of_Use.getSettings().setJavaScriptEnabled(true);
                web_terms_Of_Use.getSettings().setGeolocationEnabled(true);
                web_terms_Of_Use.setSoundEffectsEnabled(true);
                web_terms_Of_Use.loadData(response.body().getSettingValue(),
                        "text/html", "UTF-8");

                web_terms_Of_Use.setWebChromeClient(new WebChromeClient() {
                    public void onProgressChanged(WebView view, int progress) {
                        if (progress < 100) {
                            progressDialog.show();
                        }
                        if (progress == 100) {
                            progressDialog.dismiss();
                        }
                    }
                });
            }

            @Override
            public void onFailure(Call<ContentModel> call, Throwable t) {

            }
        });

    }

}