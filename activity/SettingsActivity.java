package com.metime.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.metime.R;
import com.metime.SplashActivity;
import com.metime.utils.PreferenceHelper;


public class SettingsActivity extends Activity {

private LinearLayout btnlay_manageyraccount, btnlay_prisefatyoption, btnlay_contpreferences, btnlay_notimanage, btnlay_shrmyprofile, btnlay_reportproblem, btnlay_helpcenter, btnlay_termofused, btnlay_communitiguide, btnlay_privacypolicy, btnlay_copyrightp;
private TextView tv_logout;
    private ImageButton btn_backarrow;
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

       // btnlay_manageyraccount = (LinearLayout) findViewById(R.id.btnlay_manageyraccount);
        btnlay_prisefatyoption = (LinearLayout) findViewById(R.id.btnlay_prisefatyoption);
        //btnlay_contpreferences = (LinearLayout) findViewById(R.id.btnlay_contpreferences);
       // btnlay_notimanage = (LinearLayout) findViewById(R.id.btnlay_notimanage);
       // btnlay_shrmyprofile = (LinearLayout) findViewById(R.id.btnlay_shrmyprofile);
        btnlay_reportproblem =(LinearLayout) findViewById(R.id.btnlay_reportproblem);
        btnlay_helpcenter=(LinearLayout) findViewById(R.id.btnlay_helpcenter);
        btnlay_termofused=(LinearLayout) findViewById(R.id.btnlay_termofused);
        btnlay_communitiguide=(LinearLayout) findViewById(R.id.btnlay_communitiguide);
        btnlay_privacypolicy=(LinearLayout) findViewById(R.id.btnlay_privacypolicy);
        btnlay_copyrightp=(LinearLayout) findViewById(R.id.btnlay_copyrightp);
        btn_backarrow = (ImageButton) findViewById(R.id.backarrow_editprofile);
        tv_logout = (TextView) findViewById(R.id.tv_logout);

        /*btnlay_manageyraccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ManageAcoountActivity.class));
            }
        });*/


        tv_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreferenceHelper.getInstance(SettingsActivity.this).logout();
                Intent intent = new Intent(getApplicationContext(), SplashActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("from", "1");
                startActivity(intent);
            }
        });

        btnlay_prisefatyoption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext() ,PrivacyAndSafetyActivity.class));
            }
        });

        btn_backarrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        /*btnlay_contpreferences.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),ContentPreferencesActivity.class));
            }
        });*/

       /* btnlay_notimanage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //(new Intent(getApplicationContext(), Notification_ManagmentActivity.class));
            }
        });
        btnlay_shrmyprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // startActivity(new Intent(getApplicationContext(), Share_MyprofileActivity.class));
            }
        });*/
        btnlay_reportproblem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), Report_problemActivity.class));
            }
        });
        btnlay_helpcenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),Help_centerActivity.class));
            }
        });

        btnlay_termofused.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), Terms_Of_UseActivity.class));
            }
        });

        btnlay_communitiguide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), Community_GuidelineActivity.class));
            }
        });

        btnlay_privacypolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), Privacy_policyActivity.class));
            }
        });

        btnlay_copyrightp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),Copyright_PolicyActivity.class));
            }
        });
    }
}