package com.metime;

import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


public abstract class BaseActivity extends AppCompatActivity {


    protected void initToolbarWithBackButton(String title) {
        initToolBar(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    protected void initToolBar(String title) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (!TextUtils.isEmpty(title)) {
            setScreenTitle(title);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_24dp);
        }
    }

    public void setScreenTitle(String title) {
        getSupportActionBar().setTitle(title);
        /*if (!TextUtils.isEmpty(PreferenceHelper.getInstance(this).getUserFullname())) {
            String fullName = PreferenceHelper.getInstance(this).getUserFullname();
            getSupportActionBar().setSubtitle(fullName);
        }*/
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.e("Sanjay", "Restart  App from base activety");


        /*Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        finishAffinity();
        startActivity(i);
        System.exit(0);*/
    }

}
