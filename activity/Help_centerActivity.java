package com.metime.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.metime.R;


public class Help_centerActivity extends Activity {

    private ImageButton img_backArrow;
    private TextView tv_hclink;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_center);

        img_backArrow = (ImageButton) findViewById(R.id.backarrow_hcenter);
        tv_hclink = (TextView) findViewById(R.id.tv_link);

        /**
         * Click and navigation to url which provide */
        tv_hclink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("https://www.google.com");
                Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                startActivity(intent);
            }
        });

            //tv_hclink.setMovementMethod(LinkMovementMethod.getInstance());




        img_backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}