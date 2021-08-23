package com.aashdit.olmoffline.ui.activities;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.aashdit.olmoffline.BuildConfig;
import com.aashdit.olmoffline.R;
import com.aashdit.olmoffline.databinding.ActivityNewDashboardBinding;
import com.aashdit.olmoffline.receiver.ConnectivityChangeReceiver;
import com.aashdit.olmoffline.ui.fragments.ActivityFragment;
import com.aashdit.olmoffline.ui.fragments.ApprovalFragment;
import com.aashdit.olmoffline.ui.fragments.AttendanceFragment;
import com.aashdit.olmoffline.ui.fragments.FinancialFragment;
import com.aashdit.olmoffline.ui.fragments.FragmentNavigation;
import com.aashdit.olmoffline.ui.fragments.NewRegistrationFragment;
import com.aashdit.olmoffline.ui.fragments.NonFarmActivityFragment;
import com.aashdit.olmoffline.ui.fragments.PMActivityFragment;
import com.aashdit.olmoffline.ui.fragments.ProfilingFragment;
import com.aashdit.olmoffline.ui.fragments.TrainingFragment;
import com.aashdit.olmoffline.utils.Constant;
import com.aashdit.olmoffline.utils.LocaleManager;
import com.aashdit.olmoffline.utils.SharedPrefManager;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallState;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.OnSuccessListener;
import com.google.android.play.core.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import static android.content.pm.PackageManager.GET_META_DATA;
import static com.google.android.play.core.install.model.ActivityResult.RESULT_IN_APP_UPDATE_FAILED;

public class NewDashboardActivity extends AppCompatActivity implements FragmentNavigation.MenuClickListener,
        ConnectivityChangeReceiver.ConnectivityReceiverListener {

    private static final String TAG = "NewDashboardActivity";
    private static final int MY_REQUEST_CODE = 17326;
    public static DrawerLayout mDrawerLayout;
    final String appPackageName = "com.aashdit.olmmis";
    // Declare the UpdateManager
//    UpdateManager mUpdateManager;
    private ActivityNewDashboardBinding binding;
    private Fragment fragment = null;
    private ConnectivityChangeReceiver mConnectivityChangeReceiver;
    private boolean isConnected;
    private SharedPrefManager sp;
    private boolean doubleBackToExitPressedOnce;
    private AppUpdateManager appUpdateManager;
    InstallStateUpdatedListener listener = new InstallStateUpdatedListener() {
        @Override
        public void onStateUpdate(InstallState installState) {
            if (installState.installStatus() == InstallStatus.DOWNLOADED) {
                Log.d("InstallDownloded", "InstallStatus sucsses");
                notifyUser();
            }
        }
    };

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNewDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        resetTitles();
//        // Initialize the Update Manager with the Activity and the Update Mode
//        mUpdateManager = UpdateManager.Builder(this).mode(UpdateManagerConstant.IMMEDIATE);
////        mUpdateManager.start();
//
//        mUpdateManager.addUpdateInfoListener(new UpdateManager.UpdateInfoListener() {
//            @Override
//            public void onReceiveVersionCode(final int code) {
////                txtAvailableVersion.setText(String.valueOf(code));
//                if (code > BuildConfig.VERSION_CODE) {
//                    mUpdateManager.mode(UpdateManagerConstant.IMMEDIATE).start();
//                }
//            }
//
//            @Override
//            public void onReceiveStalenessDays(final int days) {
////                txtStalenessDays.setText(String.valueOf(days));
//            }
//        });

        sp = SharedPrefManager.getInstance(this);
        mDrawerLayout = binding.drawerLayout;
        mConnectivityChangeReceiver = new ConnectivityChangeReceiver();
        mConnectivityChangeReceiver.setConnectivityReceiverListener(this);
        isConnected = mConnectivityChangeReceiver.getConnectionStatus(this);
        registerNetworkBroadcast();
        FragmentNavigation navigationFragment = (FragmentNavigation) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        if (navigationFragment != null) {
            navigationFragment.setUp(mDrawerLayout, this);
            setDrawerListener();
        }
        checkUpdate();
        //checkAppUpdate();
        fragment = new ProfilingFragment();
        loadFragment(Constant.FRAG_PROFILING, fragment, "Profiling");
        binding.tvTitle.setText(getResources().getString(R.string.home));
    }

    private void registerNetworkBroadcast() {
        registerReceiver(mConnectivityChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    protected void unregisterNetworkChanges() {
        try {
            unregisterReceiver(mConnectivityChangeReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        unregisterNetworkChanges();
        appUpdateManager.unregisterListener(listener);
        super.onDestroy();
    }

    private void setDrawerListener() {
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, binding.toolbarDashboard, R.string.open, R.string.close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);

            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);

            }
        };
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerLayout.post(mDrawerToggle::syncState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onBackPressed() {

        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
//            super.onBackPressed();

            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }

            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        }


    }

    @Override
    public void onClickMenuItem(int position, String type) {
//        if (position == 1 &&  sp.getStringData(Constant.USER_ROLE).equals("ROLE_CRPCM")) {
//            binding.tvTitle.setVisibility(View.GONE);
//        }
        if (position == 0) {
            fragment = new ProfilingFragment();
            loadFragment(Constant.FRAG_PROFILING, fragment, "Profiling");
            binding.tvTitle.setText(getResources().getString(R.string.home));
        } else if (position == 2) {
            fragment = new TrainingFragment();
            loadFragment(Constant.FRAG_TRAINING, fragment, "Training");
            binding.tvTitle.setText(getResources().getString(R.string.training));
        } else if (position == 3) {
            fragment = new FinancialFragment();
            loadFragment(Constant.FRAG_FINANCIAL, fragment, "Finance");
            binding.tvTitle.setText(getResources().getString(R.string.financial));
        } else if (position == 4) {
            fragment = new AttendanceFragment();
            loadFragment(Constant.FRAG_ATTENDANCE, fragment, "Attendance");
            binding.tvTitle.setText(getResources().getString(R.string.attendance));
        } else if (position == 5) {
//            fragment = new AttendanceFragment();
//            loadFragment(Constant.FRAG_ATTENDANCE, fragment, "Attendance");
//            binding.tvTitle.setText("Attendance");
            Intent settingIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingIntent);
        }
        if (position != 1) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
//        if (sp.getStringData(Constant.USER_ROLE).equals("ROLE_PMITRA")) {
        if (type.equals("one") || type.equals("two") || type.equals("three")) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
            if (type.equals("one") && (sp.getStringData(Constant.USER_ROLE).equals("ROLE_PMITRA") ||
                    sp.getStringData(Constant.USER_ROLE).equals("ROLE_KMITRA")
                    || sp.getStringData(Constant.USER_ROLE).equals("ROLE_UMITRA")
                    || sp.getStringData(Constant.USER_ROLE).equals("ROLE_CRPEP"))) {
                fragment = new NewRegistrationFragment();
                loadFragment(Constant.FRAG_NEW_REGD, fragment, "NewRegd");
                binding.tvTitle.setText(R.string.new_registration);
            } else if (type.equals("two") && (sp.getStringData(Constant.USER_ROLE).equals("ROLE_PMITRA")
                    /* || sp.getStringData(Constant.USER_ROLE).equals("ROLE_KMITRA")*/)) {
                fragment = new PMActivityFragment();
                loadFragment(Constant.FRAG_ACTIVITY, fragment, "Activity");
                binding.tvTitle.setText(R.string.overview);
            } else if (type.equals("two") && (/*sp.getStringData(Constant.USER_ROLE).equals("ROLE_PMITRA") ||*/
                    sp.getStringData(Constant.USER_ROLE).equals("ROLE_KMITRA"))) {
                fragment = new ActivityFragment();
                loadFragment(Constant.FRAG_ACTIVITY, fragment, "Activity");
                binding.tvTitle.setText(R.string.overview);
            } else if (type.equals("two") && (sp.getStringData(Constant.USER_ROLE).equals("ROLE_UMITRA")
                    || sp.getStringData(Constant.USER_ROLE).equals("ROLE_CRPEP"))) {
                fragment = new NonFarmActivityFragment();
                loadFragment(Constant.FRAG_ACTIVITY, fragment, "Activity");
                binding.tvTitle.setText(R.string.overview);
            } else if (type.equals("three") && (sp.getStringData(Constant.USER_ROLE).equals("ROLE_PMITRA") ||
                    sp.getStringData(Constant.USER_ROLE).equals("ROLE_KMITRA")
                    || sp.getStringData(Constant.USER_ROLE).equals("ROLE_UMITRA")
                    || sp.getStringData(Constant.USER_ROLE).equals("ROLE_CRPEP"))) {
                fragment = new ApprovalFragment();
                loadFragment(Constant.FRAG_APPROVALS, fragment, "Approvals");
                binding.tvTitle.setText(R.string.approvals);
            } else {
                Toast.makeText(this, "Unauthorised Access", Toast.LENGTH_SHORT).show();
            }
        }
//        }
//        else {
//            Toast.makeText(this, "Unauthorised Access", Toast.LENGTH_SHORT).show();
//        }
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
        Fragment fragmentNewRegd = manager.findFragmentByTag(Constant.FRAG_NEW_REGD);
        Fragment fragmentApproved = manager.findFragmentByTag(Constant.FRAG_APPROVALS);


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
            if (fragmentNewRegd != null) {
                ft.hide(fragmentNewRegd);
            }
            if (fragmentApproved != null) {
                ft.hide(fragmentApproved);
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
            if (tag.equals(Constant.FRAG_NEW_REGD)) {
                if (fragmentNewRegd != null) {
                    ft.show(fragmentNewRegd);
                }
            }
            if (tag.equals(Constant.FRAG_APPROVALS)) {
                if (fragmentApproved != null) {
                    ft.show(fragmentApproved);
                }
            }
            ft.commitAllowingStateLoss();
            return true;
        }

        return false;
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        this.isConnected = isConnected;
    }

    private void checkUpdate() {
        appUpdateManager = AppUpdateManagerFactory.create(this);
        appUpdateManager.registerListener(listener);

        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();
        appUpdateInfoTask.addOnSuccessListener(new OnSuccessListener<AppUpdateInfo>() {
            @Override
            public void onSuccess(AppUpdateInfo appUpdateInfo) {
                Log.d("appUpdateInfo :", "packageName :" + appUpdateInfo.packageName() + ", " + "availableVersionCode :" + appUpdateInfo.availableVersionCode() + ", " + "updateAvailability :" + appUpdateInfo.updateAvailability() + ", " + "installStatus :" + appUpdateInfo.installStatus());

                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                        && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                    requestUpdate(appUpdateInfo);
                    Log.d("UpdateAvailable", "update is there ");
                } else if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                    Log.d("Update", "3");
                    notifyUser();
                } else {
//                    Toast.makeText(NewDashboardActivity.this, "No Update Available", Toast.LENGTH_SHORT).show();
                    Log.d("NoUpdateAvailable", "update is not there ");
                }
            }
        });
    }

    private void requestUpdate(AppUpdateInfo appUpdateInfo) {
        try {
            appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.IMMEDIATE, NewDashboardActivity.this, MY_REQUEST_CODE);
            resume();
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MY_REQUEST_CODE) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    if (resultCode != RESULT_OK) {
                        Toast.makeText(this, "RESULT_OK" + resultCode, Toast.LENGTH_LONG).show();
                        Log.d("RESULT_OK  :", "" + resultCode);
                    }
                    break;
                case Activity.RESULT_CANCELED:

                    if (resultCode != RESULT_CANCELED) {
                        Toast.makeText(this, "RESULT_CANCELED" + resultCode, Toast.LENGTH_LONG).show();
                        Log.d("RESULT_CANCELED  :", "" + resultCode);
                    }
                    break;
                case RESULT_IN_APP_UPDATE_FAILED:

                    if (resultCode != RESULT_IN_APP_UPDATE_FAILED) {

                        Toast.makeText(this, "RESULT_IN_APP_UPDATE_FAILED" + resultCode, Toast.LENGTH_LONG).show();
                        Log.d("RESULT_IN_APP_FAILED:", "" + resultCode);
                    }
            }
        }
    }

    private void notifyUser() {

        /**
         * Fatal Exception: java.lang.IllegalArgumentException
         * No suitable parent found from the given view. Please provide a valid view.
         * */

//        Snackbar snackbar =
//                Snackbar.make(findViewById(R.id.message),
//                        "An update has just been downloaded.",
//                        Snackbar.LENGTH_INDEFINITE);
//        snackbar.setAction("RESTART", new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                appUpdateManager.completeUpdate();
//            }
//        });
//        snackbar.setActionTextColor(
//                getResources().getColor(R.color.purple_500));
//        snackbar.show();
    }

    private void resume() {
        appUpdateManager.getAppUpdateInfo().addOnSuccessListener(new OnSuccessListener<AppUpdateInfo>() {
            @Override
            public void onSuccess(AppUpdateInfo appUpdateInfo) {
                if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                    notifyUser();
                }

            }
        });
    }

    private void checkAppUpdate() {
        AndroidNetworking.get(BuildConfig.BASE_URL + Constant.BASE_PATH + "assets/appJs/encrypt/olm_app_update.json")
                .setTag("AppUpdate")
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject resObj = new JSONObject(response);
                            int vCode = resObj.getInt("versionCode");
                            if (vCode > BuildConfig.VERSION_CODE) {
                                showUpdateNotification();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.e(TAG, "onError: " + anError.getErrorDetail());
                    }
                });
    }

    private void showUpdateNotification() {

        if (!NewDashboardActivity.this.isFinishing()) {

            new AlertDialog.Builder(this)
                    .setTitle("App Update")
                    .setMessage("New Version of APP available !!! ")
                    .setCancelable(false)

                    // Specifying a listener allows you to take an action before dismissing the dialog.
                    // The dialog is automatically dismissed when a dialog button is clicked.
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                            } catch (android.content.ActivityNotFoundException anfe) {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                            }
                        }
                    })

                    // A null listener allows the button to dismiss the dialog and take no further action.
//                .setNegativeButton(android.R.string.no, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    }


}