package com.metime.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.metime.BaseActivity;
import com.metime.R;


public class ViewPermissionActivity extends BaseActivity {

    private LinearLayout llPublicParent, llFollowersParent;
    private ImageButton imgBackarrow;
    private Button btn_done;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_permission);

        imgBackarrow = (ImageButton) findViewById(R.id.backarrow_pr);
        llPublicParent = (LinearLayout) findViewById(R.id.llPublicParent);
        llFollowersParent = (LinearLayout) findViewById(R.id.llFollowersParent);
        btn_done = (Button) findViewById(R.id.btnDone);

        imgBackarrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        llPublicParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent data = new Intent();
                data.putExtra("data", true);
                setResult(RESULT_OK, data);
                finish();
            }
        });

        llFollowersParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent data = new Intent();
                data.putExtra("data", false);
                setResult(RESULT_OK, data);
                finish();
            }
        });

    }
}