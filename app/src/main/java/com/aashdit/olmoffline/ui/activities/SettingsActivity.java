package com.aashdit.olmoffline.ui.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.aashdit.olmoffline.BuildConfig;
import com.aashdit.olmoffline.databinding.ActivitySettingsBinding;
import com.aashdit.olmoffline.ui.MainActivity;
import com.aashdit.olmoffline.ui.dialog.ChangeDataDialog;
import com.aashdit.olmoffline.utils.Constant;
import com.aashdit.olmoffline.utils.LocaleManager;
import com.aashdit.olmoffline.utils.SharedPrefManager;
import com.aashdit.olmoffline.utils.Utility;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import static android.content.pm.PackageManager.GET_META_DATA;

public class SettingsActivity extends AppCompatActivity implements ChangeDataDialog.OnUpdateListener {

    private static final String TAG = "SettingsActivity";
    String defaultLang = "en";
    private ActivitySettingsBinding binding;
    private SharedPrefManager sp;
    private String token;
    private ChangeDataDialog dataDialog;

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
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        resetTitles();
        setSupportActionBar(binding.toolbarSetting);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        sp = SharedPrefManager.getInstance(this);
        token = sp.getStringData(Constant.APP_TOKEN);

        defaultLang = LocaleManager.getLanguagePref(this);

        binding.tvUserName.setText(sp.getStringData(Constant.USER_NAME));
        binding.etDesignation.setText(sp.getStringData(Constant.USER_ROLE));
        binding.etDesignation.setEnabled(false);
        binding.etGpName.setEnabled(false);

        binding.switchLanguage.setChecked(defaultLang.equals("or"));

        binding.switchLanguage.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
//                    sp.setStringData(Constant.APP_LANGUAGE, "or");
//                    showConfirmDialog();
                    setNewLocale(SettingsActivity.this, LocaleManager.ODIA);
                } else {
                    setNewLocale(SettingsActivity.this, LocaleManager.ENGLISH);
//                    showConfirmDialog();
//                    sp.setStringData(Constant.APP_LANGUAGE, "en");
                }
            }
        });

        binding.tvLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sp.clear();
                Intent intent = new Intent(SettingsActivity.this, MainActivity.class); //mContext.getIntent();
                startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                finish();
            }
        });

        if (sp.getStringData(Constant.USER_ROLE).equals("ROLE_CRPCM")) {
            binding.cvCrpcm.setVisibility(View.VISIBLE);
            binding.etGpTil.setVisibility(View.GONE);
            getDashboardData();
        } else {
            binding.cvCrpcm.setVisibility(View.GONE);
        }

        getProfileDetails();
        binding.tvCngPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dataDialog = new ChangeDataDialog(SettingsActivity.this, "PASSWORD");
                dataDialog.setOnUpdateListener(SettingsActivity.this);

            }
        });

        binding.tvCngMob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dataDialog = new ChangeDataDialog(SettingsActivity.this, "MOBILE");
                dataDialog.setOnUpdateListener(SettingsActivity.this);
            }
        });

        binding.tvCngMail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dataDialog = new ChangeDataDialog(SettingsActivity.this, "EMAIL");
                dataDialog.setOnUpdateListener(SettingsActivity.this);
            }
        });

//        if (dataDialog != null) {
//            dataDialog.setOnUpdateListener(new ChangeDataDialog.OnUpdateListener() {
//                @Override
//                public void updateDone(Dialog dialog, String type, String oldValue, String newValue) {
//                    Toast.makeText(SettingsActivity.this, "not null api needs to call", Toast.LENGTH_SHORT).show();
//                }
//            });
//        }
    }

    private void getProfileDetails() {
        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "myprofile")
                .addHeaders("Authorization", "Bearer " + token)
                .setTag("Activity")
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        if (Utility.isStringValid(response)) {
                            try {
                                JSONObject resObject = new JSONObject(response);
                                if (resObject.optBoolean("outcome")) {
                                    JSONObject dataObj = resObject.optJSONObject("data");
                                    if (dataObj != null) {
                                        String userName = dataObj.optString("userName");
                                        binding.tvUserName.setText(userName);
                                        String firstName = dataObj.optString("firstName");
                                        binding.etFirstName.setText(firstName);
                                        String lastName = dataObj.optString("lastName");
                                        binding.etLastName.setText(lastName);
                                        String email = dataObj.optString("email");
                                        binding.etEmail.setText(email);
                                        String mobileNumber = dataObj.optString("mobileNumber");
                                        binding.etMobile.setText(mobileNumber);
                                        String location = dataObj.optString("location");
                                        String locationType = dataObj.optString("locationType");
                                        binding.etGpName.setHint(locationType);
                                        binding.etGpName.setText(locationType+" - "+location);
                                        String designation = dataObj.optString("designation");
                                        binding.etDesignation.setText(designation);
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.e(TAG, "onError: " + anError.getErrorDetail());
                    }
                });
    }

    private void setNewLocale(AppCompatActivity mContext, @LocaleManager.LocaleDef String language) {
        LocaleManager.setNewLocale(this, language);
        Intent intent = new Intent(SettingsActivity.this, MainActivity.class); //mContext.getIntent();
        startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));

    }

    private void showConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Change Language ?")
                .setMessage("Are you sure you want to change language ?")

                // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Continue with delete operation

                        setNewLocale(SettingsActivity.this, LocaleManager.ODIA);

                    }
                })

                // A null listener allows the button to dismiss the dialog and take no further action.
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void getDashboardData() {
        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "dashboard")
                .addHeaders("Authorization", "Bearer " + token)
                .setTag("Activity")
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        if (Utility.isStringValid(response)) {
                            try {
                                JSONObject resObject = new JSONObject(response);
                                if (resObject.optBoolean("outcome")) {
                                    if (resObject.optJSONObject("data") != null) {
                                        String district = Objects.requireNonNull(resObject.optJSONObject("data")).optString("district");
                                        String block = Objects.requireNonNull(resObject.optJSONObject("data")).optString("block");
                                        String gp = Objects.requireNonNull(resObject.optJSONObject("data")).optString("gp");
                                        String villages = Objects.requireNonNull(resObject.optJSONObject("data")).optString("villages");
//                                        level = Objects.requireNonNull(resObject.optJSONObject("data")).optString("label");
//                                        levelName = Objects.requireNonNull(resObject.optJSONObject("data")).optString("labelValue");
                                        String userName = Objects.requireNonNull(resObject.optJSONObject("data")).optString("userName");
                                        String lastLogin = Objects.requireNonNull(resObject.optJSONObject("data")).optString("lastLogin");
                                        Long clfReported = Objects.requireNonNull(resObject.optJSONObject("data")).optLong("clfReported");
                                        Long pgReported = Objects.requireNonNull(resObject.optJSONObject("data")).optLong("pgReported");
                                        Long egReported = Objects.requireNonNull(resObject.optJSONObject("data")).optLong("egReported");
                                        Long shgReported = Objects.requireNonNull(resObject.optJSONObject("data")).optLong("shgReported");
                                        Long hhReported = Objects.requireNonNull(resObject.optJSONObject("data")).optLong("hhReported");
                                        Long activitiesCovered = Objects.requireNonNull(resObject.optJSONObject("data")).optLong("activitiesCovered");

                                        binding.etDist.setText(district);
                                        binding.etBlock.setText(block);
                                        binding.etGp.setText(gp);
                                        binding.etVillage.setText(villages);

                                    }
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.e(TAG, "onError: " + anError.getErrorDetail());
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void updateDone(Dialog dialog, String type, String oldValue, String newValue) {

        JSONObject reqObj = new JSONObject();
        try {
            reqObj.put("type",type);
            reqObj.put("oldValue",oldValue);
            reqObj.put("newValue",newValue);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "myprofile/update")
                .addHeaders("Authorization", "Bearer " + token)
                .setTag("Activity")
                .setPriority(Priority.HIGH)
                .addJSONObjectBody(reqObj)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        if (Utility.isStringValid(response)) {
                            try {
                                JSONObject resObject = new JSONObject(response);
                                if (resObject.optBoolean("outcome")) {
                                    Toast.makeText(SettingsActivity.this, resObject.optString("message"), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(SettingsActivity.this, resObject.optString("message"), Toast.LENGTH_SHORT).show();
                                }
                                dialog.dismiss();
                                if (type.equals("PASSWORD")){
                                    if (resObject.optBoolean("outcome")) {
                                        sp.clear();
                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                        startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                                Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                                        finish();
                                    }
                                }else {
                                    getProfileDetails();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.e(TAG, "onError: " + anError.getErrorDetail());
                    }
                });

    }
}