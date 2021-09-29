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

public class Copyright_PolicyActivity extends Activity {


    private ImageButton img_backarrow;
    private WebView webView_crpolicy;

    private ProgressDialog progressDialog ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_copyright__policy);
         progressDialog = new ProgressDialog(this);
        img_backarrow = (ImageButton) findViewById(R.id.backarrow_crpolicy);
        webView_crpolicy = (WebView) findViewById(R.id.webview_crpolicy);

        getDataTermOfUse();

        img_backarrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void getDataTermOfUse() {
        Utils.showProgressDialog(getApplicationContext());
        Call<ContentModel> call = ApiManager.shared(getApplicationContext()).service.getContent("Copyright Policy");

        call.enqueue(new Callback<ContentModel>() {
            @Override
            public void onResponse(Call<ContentModel> call, Response<ContentModel> response) {
                Log.e("Sanjay","string value===>"+response.body().getSettingValue());
                webView_crpolicy.requestFocus();
                webView_crpolicy.getSettings().setLightTouchEnabled(true);
                webView_crpolicy.getSettings().setJavaScriptEnabled(true);
                webView_crpolicy.getSettings().setGeolocationEnabled(true);
                webView_crpolicy.setSoundEffectsEnabled(true);
                webView_crpolicy.loadData(response.body().getSettingValue(),
                        "text/html", "UTF-8");

                webView_crpolicy.setWebChromeClient(new WebChromeClient() {
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