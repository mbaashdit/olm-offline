package com.aashdit.olmoffline.ui.activities;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.aashdit.olmoffline.R;
import com.aashdit.olmoffline.databinding.ActivityDashboardBinding;
import com.aashdit.olmoffline.ui.fragments.ActivityFragment;
import com.aashdit.olmoffline.ui.fragments.AttendanceFragment;
import com.aashdit.olmoffline.ui.fragments.FinancialFragment;
import com.aashdit.olmoffline.ui.fragments.FragmentNavigation;
import com.aashdit.olmoffline.ui.fragments.ProfilingFragment;
import com.aashdit.olmoffline.ui.fragments.TrainingFragment;
import com.aashdit.olmoffline.utils.Constant;
import com.aashdit.olmoffline.utils.LocaleManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.analytics.FirebaseAnalytics;

import static android.content.pm.PackageManager.GET_META_DATA;

public class DashboardActivity extends AppCompatActivity {

    private static final String TAG = "DashboardActivity";

    private Fragment fragment = null;
    private RelativeLayout mRlDashboardBackground;
    private FirebaseAnalytics mFirebaseAnalytics;

    private ActivityDashboardBinding binding;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
    }

    protected void resetTitles() {
        try {
            ActivityInfo info = getPackageManager().getActivityInfo(getComponentName(), GET_META_DATA);
            if (info.labelRes != 0) {
                setTitle(info.labelRes);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
    public static DrawerLayout mDrawerLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        resetTitles();

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mRlDashboardBackground = findViewById(R.id.rl_dashboard_background);
//        int nightModeFlags =  getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
//        switch (nightModeFlags) {
//            case Configuration.UI_MODE_NIGHT_YES:
//                mRlDashboardBackground.setBackgroundColor(getResources().getColor(R.color.background_dark));
//                break;
//
//            case Configuration.UI_MODE_NIGHT_NO:
//                mRlDashboardBackground.setBackgroundColor(getResources().getColor(R.color.background));
//                break;
//
//            case Configuration.UI_MODE_NIGHT_UNDEFINED:
//
//                break;
//        }

//        mDrawerLayout = binding.drawerLayout; /*(DrawerLayout) findViewById(R.id.drawer_layout);*/


        FragmentNavigation navigationFragment = (FragmentNavigation) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        if (navigationFragment != null) {
//            navigationFragment.setUp(mDrawerLayout/*, this*/);
//            setDrawerListener();
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.navigation_bottom);
        bottomNavigationView.setSelectedItemId(R.id.action_activity);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment = null;
                String tag = Constant.FRAG_PROFILING;
                String title = "";
                switch (item.getItemId()) {
                    case R.id.action_profiling:
                        fragment = new ProfilingFragment();
                        tag = Constant.FRAG_PROFILING;
                        title = "Dashboard";
                        break;
                    case R.id.action_activity:
                        fragment = new ActivityFragment();
                        tag = Constant.FRAG_ACTIVITY;
                        title = "Student";
                        break;
                    case R.id.action_training:
                        fragment = new TrainingFragment();
                        tag = Constant.FRAG_TRAINING;
                        title = "CCH";
                        break;
                    case R.id.action_financial:
                        fragment = new FinancialFragment();
                        tag = Constant.FRAG_FINANCIAL;
                        title = "Attendance";
                        break;
                    case R.id.action_attendance:
                        fragment = new AttendanceFragment();
                        tag = Constant.FRAG_ATTENDANCE;
                        title  = "Report";
                        break;

                }
                return loadFragment(tag, fragment,title);
            }
        });

//        ProfilingFragment dashboardFragment = new ProfilingFragment();
//        dashboardFragment.setMenuViewListener(this);
        fragment = new ActivityFragment();
        loadFragment(Constant.FRAG_ACTIVITY, fragment,"Dashboard");

    }

    private boolean loadFragment(String tag, Fragment fragment, String dashboard) {

        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();

        if (manager.findFragmentByTag(tag) == null) {
            ft.add(R.id.frame_layout, fragment, tag);
        }

        Fragment fragmentProfiling = manager.findFragmentByTag(Constant.FRAG_PROFILING);
        Fragment fragmentActivity = manager.findFragmentByTag(Constant.FRAG_ACTIVITY);
        Fragment fragmentTraining = manager.findFragmentByTag(Constant.FRAG_TRAINING);
        Fragment fragmentFinancial = manager.findFragmentByTag(Constant.FRAG_FINANCIAL);
        Fragment fragmentAttendance = manager.findFragmentByTag(Constant.FRAG_ATTENDANCE);


        if (fragment != null) {
            // Hide all Fragment
            if (fragmentProfiling != null) {
                ft.hide(fragmentProfiling);
            }
            if (fragmentActivity != null) {
                ft.hide(fragmentActivity);
            }
            if (fragmentTraining != null) {
                ft.hide(fragmentTraining);
            }

            if (fragmentFinancial != null) {
                ft.hide(fragmentFinancial);
            }

            if (fragmentAttendance != null) {
                ft.hide(fragmentAttendance);
            }

            // Show  current Fragment

//            mTvTitle.setText(title);
            if (tag.equals(Constant.FRAG_PROFILING)) {
                if (fragmentProfiling != null) {
                    ft.show(fragmentProfiling);
                }
            }
            if (tag.equals(Constant.FRAG_ACTIVITY)) {
                if (fragmentActivity != null) {
                    ft.show(fragmentActivity);
                }
            }
            if (tag.equals(Constant.FRAG_TRAINING)) {
                if (fragmentTraining != null) {
                    ft.show(fragmentTraining);
                }
            }

            if (tag.equals(Constant.FRAG_FINANCIAL)) {
                if (fragmentFinancial != null) {
                    ft.show(fragmentFinancial);
                }
            }
            if (tag.equals(Constant.FRAG_ATTENDANCE)) {
                if (fragmentAttendance != null) {
                    ft.show(fragmentAttendance);
                }
            }
            ft.commitAllowingStateLoss();
            return true;
        }

        return false;
    }


    private boolean doubleBackToExitPressedOnce;

    public void setUp(DrawerLayout drawerLayout/*, MenuClickListener listener*/) {

        mDrawerLayout = drawerLayout;
//        this.listener = listener;
    }

//    private void setDrawerListener() {
//        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, binding.toolbarDashboard, R.string.open, R.string.close) {
//            @Override
//            public void onDrawerOpened(View drawerView) {
//                super.onDrawerOpened(drawerView);
//
//            }
//
//            @Override
//            public void onDrawerSlide(View drawerView, float slideOffset) {
//                super.onDrawerSlide(drawerView, slideOffset);
//
//            }
//
//            @Override
//            public void onDrawerClosed(View drawerView) {
//                super.onDrawerClosed(drawerView);
//
//            }
//        };
//        mDrawerLayout.addDrawerListener(mDrawerToggle);
//        mDrawerLayout.post(mDrawerToggle::syncState);
//        mDrawerToggle.syncState();
//    }

    @Override
    public void onBackPressed() {

        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }
}