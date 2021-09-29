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

public class Privacy_policyActivity extends Activity {

    private ImageButton img_backarrow;
    private WebView webView_privacypolicy;
    private ProgressDialog progressDialog ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);
        progressDialog = new ProgressDialog(this);
        img_backarrow = (ImageButton) findViewById(R.id.backarrow_privacpolicy);
        webView_privacypolicy = (WebView) findViewById(R.id.webview_privacpolicy);


        getDataTermOfUse();

        img_backarrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /**
     * to get all HTML tag Data APi from */
    private void getDataTermOfUse() {

        Utils.showProgressDialog(getApplicationContext());
        Call<ContentModel> call = ApiManager.shared(getApplicationContext()).service.getContent("privacy policy");

        call.enqueue(new Callback<ContentModel>() {
            @Override
            public void onResponse(Call<ContentModel> call, Response<ContentModel> response) {
                Log.e("Sanjay","string value===>"+response.body().getSettingValue());
                webView_privacypolicy.requestFocus();
                webView_privacypolicy.getSettings().setLightTouchEnabled(true);
                webView_privacypolicy.getSettings().setJavaScriptEnabled(true);
                webView_privacypolicy.getSettings().setGeolocationEnabled(true);
                webView_privacypolicy.setSoundEffectsEnabled(true);
                webView_privacypolicy.loadData(response.body().getSettingValue(),
                        "text/html", "UTF-8");

                webView_privacypolicy.setWebChromeClient(new WebChromeClient() {
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