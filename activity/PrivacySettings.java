package com.metime.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Switch;

import com.metime.BaseActivity;
import com.metime.R;

public class PrivacySettings extends BaseActivity {

    private ImageButton imgBackArrow;
    private Switch swt_allow_comments, swt_allow_saving;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_settings);

        imgBackArrow = (ImageButton) findViewById(R.id.backarrow);
        swt_allow_comments = (Switch) findViewById(R.id.switch_allow_comment);
        swt_allow_saving = (Switch) findViewById(R.id.switch_allow_saving);

        imgBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

}