package com.metime.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.metime.R;
import com.metime.adapter.FollowFollowerAdepter;

public class FollowAndFollowerActivity extends FragmentActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private LinearLayout llSearchUsers;
    private TextView tvUserName;
    private ImageButton ibBack, ibprofile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow_and_follower);

        ibBack = (ImageButton) findViewById(R.id.ibBack);
        ibprofile = (ImageButton) findViewById(R.id.img_btn_profile);
        tvUserName = (TextView) findViewById(R.id.tvUserName);
        llSearchUsers = (LinearLayout) findViewById(R.id.llSearchUsers);
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        tabLayout.addTab(tabLayout.newTab().setText("FOLLOWING"));
        tabLayout.addTab(tabLayout.newTab().setText("FOLLOWERS"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        tvUserName.setText(getIntent().getExtras().getString("userName"));

        Log.e("Sanjay","from profile"+getIntent().getExtras().getInt("following"));
        FollowFollowerAdepter followFollowerAdepter = new FollowFollowerAdepter(getSupportFragmentManager(), this, tabLayout.getTabCount());
        viewPager.setAdapter(followFollowerAdepter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        viewPager.setCurrentItem(getIntent().getExtras().getInt("following"));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                Log.e("Sanjay","position"+tab.getPosition());
                Log.e("Sanjay","from profile"+getIntent().getExtras().getInt("following"));
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        llSearchUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FollowAndFollowerActivity.this, UserListActivity.class));
            }
        });

        ibprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FollowAndFollowerActivity.this, UserListActivity.class));
            }
        });

        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}