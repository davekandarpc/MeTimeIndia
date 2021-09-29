package com.metime.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.metime.BaseActivity;
import com.metime.R;
import com.metime.fragment.ActivityFragment;
import com.metime.fragment.EmptyFragment;
import com.metime.fragment.HomeFragment;
import com.metime.fragment.ProfileFragment;
import com.metime.fragment.SearchFragment;
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;


public class MainActivity extends BaseActivity {

    private boolean doubleBackToExitPressedOnce = false;

    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        AppCenter.start(getApplication(), "fe034c5d-295e-41c9-9454-2758d4508b53",
                Analytics.class, Crashes.class);
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavigation);
        bottomNavigationView.setItemIconTintList(null);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.navigation_home) {
                    showThisFragment(new HomeFragment(), HomeFragment.TAG);
                    Analytics.trackEvent("In home screen click");
                } else if (id == R.id.navigation_discover) {
                    showThisFragment(new SearchFragment(), SearchFragment.TAG);
                } else if (id == R.id.navigation_record) {
                    startActivity(new Intent(MainActivity.this, VideoCaptureActivity.class));
                    return false;
                } else if (id == R.id.navigation_activity) {
                    showThisFragment(new ActivityFragment(), ActivityFragment.TAG);
                } else if (id == R.id.navigation_account) {
                    showThisFragment(new ProfileFragment(), ProfileFragment.TAG);
                }

                return true;
            }
        });

        showThisFragment(new HomeFragment(), HomeFragment.TAG);
    }


    public void setBottomBG(boolean isHome){
        if(isHome){
            bottomNavigationView.setBackgroundColor(Color.parseColor("#4D000000"));
        }else {
            bottomNavigationView.setBackgroundColor(Color.parseColor("#43acf0"));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        /*Fragment frg = getSupportFragmentManager().findFragmentById(R.id.content);
        if(frg != null){
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.detach(frg);
            ft.attach(frg);
            ft.commitAllowingStateLoss();
        }else {
            showThisFragment(new HomeFragment(), HomeFragment.TAG);
        }*/
    }

    private void showThisFragment(Fragment fragment, String tag) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content, fragment);
        fragmentTransaction.addToBackStack(tag);
        fragmentTransaction.commit();
        //getSupportFragmentManager().executePendingTransactions();
    }


    /*@Override
    public void onBackPressed() {
        super.onBackPressed();

        if (doubleBackToExitPressedOnce) {
            finish();
        } else {
            pressAgain();
        }
    }

    private void pressAgain() {
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, getString(R.string.press_click_back_to_exit), Toast.LENGTH_LONG).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 1000);
    }*/




}
