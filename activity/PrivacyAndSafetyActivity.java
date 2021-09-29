package com.metime.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.metime.R;

public class PrivacyAndSafetyActivity extends Activity {

    private ImageButton btn_backarrow;
    private LinearLayout btn_blockUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_and_safety);

        btn_blockUser =(LinearLayout) findViewById(R.id.btnlay_blockuser);
        btn_backarrow = (ImageButton) findViewById(R.id.backarrow_ps);

        btn_backarrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btn_blockUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), BlockActivity.class));
            }
        });
    }
}