package com.aashdit.olmoffline.ui.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.aashdit.olmoffline.BuildConfig;
import com.aashdit.olmoffline.R;
import com.aashdit.olmoffline.databinding.ActivityNTFTViewMainBinding;
import com.aashdit.olmoffline.ui.MainActivity;
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

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

import static android.content.pm.PackageManager.GET_META_DATA;

public class NTFTViewMainActivity extends AppCompatActivity {

    private static final String TAG = "NTFTViewMainActivity";
    int updatedMonth = 0;
    int updatedYear = 0;
    private ActivityNTFTViewMainBinding binding;
    private String month, year, entityCode;
    private Long entityId, schemeId, activityId;
    private Long lastReportedMonth = 0L;
    private Long lastReportedYear = 0L;
    private SharedPrefManager sp;
    private String token;
    private int navType;
    private boolean isGroup = false;
    private boolean canEdit;
    private boolean isGroups = false;
    private String productionUOM, salesUOM;
    //as single screen used for both tasar and medicinal plant
    //so this property used to check which activity is requested.
    // 0 = tasar , 1 = medicinal plant
    private int requestedActivity;
    private String searchUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNTFTViewMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        resetTitles();
        setSupportActionBar(binding.toolbarViewMainNtft);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        sp = SharedPrefManager.getInstance(this);
        token = sp.getStringData(Constant.APP_TOKEN);
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.MINUTES)
                .readTimeout(10, TimeUnit.MINUTES)
                .writeTimeout(10, TimeUnit.MINUTES)
                .build();
        AndroidNetworking.initialize(this, okHttpClient);
        entityId = getIntent().getLongExtra("ENTITY_ID", 0L);
        schemeId = getIntent().getLongExtra("SCHEME_ID", 0L);
        activityId = getIntent().getLongExtra("ACTIVITY_ID", 0L);
        month = getIntent().getStringExtra("MONTH");
        year = getIntent().getStringExtra("YEAR");
        entityCode = getIntent().getStringExtra("ENTITY_TYPE_CODE");

        requestedActivity = getIntent().getIntExtra("REQ_ACT", -1);

        binding.tvAvgMonIncLbl.setText(getResources().getString(R.string.avg_monthly_income));//.concat(" (").concat(entityCode.equals("HH")?"All Member":entityCode).concat(" )")

        navType = sp.getIntData("NAV_TYPE");
        if (entityCode.equals("HH")) {
//            binding.rlToogle.setVisibility(View.GONE);
            binding.cvNoOfFarmers.setVisibility(View.GONE);
        } else {
//            binding.rlToogle.setVisibility(View.VISIBLE);
            binding.cvNoOfFarmers.setVisibility(View.VISIBLE);
        }
        binding.progress.setVisibility(View.GONE);
        binding.rlToogle.setVisibility(View.GONE);
        ntftActivityDetails();
        binding.tvFarmActivityHistory.setEnabled(false);
        binding.tvFarmActivityHistory.setClickable(false);
        binding.swiperefresh.setOnRefreshListener(() -> {
            if (isGroup) {
//                farmActivityIndividualsDetails();
            } else {
//                farmActivityDetails();
            }
            binding.swiperefresh.setRefreshing(false);
        });
        binding.llGroups.setVisibility(View.VISIBLE);
        binding.llIndividuals.setVisibility(View.GONE);

        binding.tvFarmActivityHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (requestedActivity == 0) {
                    Intent intent = new Intent(NTFTViewMainActivity.this, NtftTasarDetailsViewActivity.class);
                    intent.putExtra("LAST_MONTH", lastReportedMonth);
                    intent.putExtra("LAST_YEAR", lastReportedYear);
                    intent.putExtra("PUOM", productionUOM);
                    intent.putExtra("SUOM", salesUOM);
                    startActivity(intent);
                } else if (requestedActivity == 1) {
                    Intent intent = new Intent(NTFTViewMainActivity.this, NTFPDetailsViewActivity.class);
                    intent.putExtra("LAST_MONTH", lastReportedMonth);
                    intent.putExtra("LAST_YEAR", lastReportedYear);
                    intent.putExtra("PUOM", productionUOM);
                    intent.putExtra("SUOM", salesUOM);
                    startActivity(intent);
                }
            }
        });


        binding.rlNewUpdateNtft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (requestedActivity == 0) {
                    Intent updateShgActivity = new Intent(NTFTViewMainActivity.this, NtfpTaserUpdateActivity.class);
                    updateShgActivity.putExtra("ENTITY_CODE", entityCode);
                    updateShgActivity.putExtra("PUOM", productionUOM);
                    updateShgActivity.putExtra("SUOM", salesUOM);
                    sp.setLongData(Constant.ENTITY_ID, entityId);
                    sp.setLongData(Constant.SCHEME_ID, schemeId);
                    sp.setLongData(Constant.ACTIVITY_ID, activityId);
                    sp.setStringData(Constant.ENTITY_TYPE_CODE, entityCode);
                    sp.setStringData(Constant.MONTH, String.valueOf(lastReportedMonth));
                    sp.setStringData(Constant.YEAR, String.valueOf(lastReportedYear));

                    startActivityForResult(updateShgActivity, 99);
                } else if (requestedActivity == 1) {
                    Intent updateShgActivity = new Intent(NTFTViewMainActivity.this, NTFTUpdateActivity.class);
                    updateShgActivity.putExtra("ENTITY_CODE", entityCode);
                    updateShgActivity.putExtra("PUOM", productionUOM);
                    updateShgActivity.putExtra("SUOM", salesUOM);
                    sp.setLongData(Constant.ENTITY_ID, entityId);
                    sp.setLongData(Constant.SCHEME_ID, schemeId);
                    sp.setLongData(Constant.ACTIVITY_ID, activityId);
                    sp.setStringData(Constant.ENTITY_TYPE_CODE, entityCode);
                    sp.setStringData(Constant.MONTH, String.valueOf(lastReportedMonth));
                    sp.setStringData(Constant.YEAR, String.valueOf(lastReportedYear));

                    startActivityForResult(updateShgActivity, 99);
                }
            }
        });

        binding.rlApproval.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });

        binding.llBottomPanel.setVisibility(View.GONE);

    }

    private void showDialog() {
        if (!NTFTViewMainActivity.this.isFinishing()) {
            new AlertDialog.Builder(this)
                    .setTitle("Are you sure ?")
                    .setMessage("Are you sure you want to send for approval ?")

                    // Specifying a listener allows you to take an action before dismissing the dialog.
                    // The dialog is automatically dismissed when a dialog button is clicked.
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Continue with delete operation
                            sendForApproval();

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
    }

    private void sendForApproval() {
        binding.progress.setVisibility(View.VISIBLE);

        if (requestedActivity == 0) {
            searchUrl = "ntfp-tasar/submit";
        } else if (requestedActivity == 1) {
            searchUrl = "ntfp/submit";
        } else {
            searchUrl = "";
        }

        JSONObject object = new JSONObject();
        try {
            object.put("month", updatedMonth);
            object.put("year", updatedYear);
            object.put("schemeId", schemeId);
            object.put("activityId", activityId);
            object.put("entityId", entityId);
            object.put("entityTypeCode", entityCode);
            object.put("newStatus", "SUBMITTED_TO_MBK");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + searchUrl)
                .addHeaders("Authorization", "Bearer " + token)
                .setTag("Approval")
                .addJSONObjectBody(object)
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        binding.progress.setVisibility(View.GONE);
                        if (Utility.isStringValid(response)) {
                            try {
                                JSONObject resObj = new JSONObject(response);
                                if (resObj.optBoolean("outcome")) {
                                    showDialog(resObj.optString("message"));
                                    binding.rlApproval.setVisibility(View.GONE);
                                } else {
                                    showDialog(resObj.optString("message"));
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        binding.progress.setVisibility(View.GONE);
                        Log.e(TAG, "onError: " + anError.getErrorDetail());
                        try {
                            JSONObject errObj = new JSONObject(anError.getErrorBody());
                            int statusCode = errObj.optInt("status");
                            if (statusCode == 500) {
                                sp.clear();
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                        Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                                finish();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

    }

    private void showDialog(String message) {
        if (!NTFTViewMainActivity.this.isFinishing()) {
            new AlertDialog.Builder(this)
                    .setTitle("")
                    .setMessage(message)

                    // Specifying a listener allows you to take an action before dismissing the dialog.
                    // The dialog is automatically dismissed when a dialog button is clicked.
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Continue with delete operation

                        }
                    }).show();
        }
        // A null listener allows the button to dismiss the dialog and take no further action.
//                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                    }
//                })
//                .setIcon(android.R.drawable.ic_dialog_alert)
//                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 99 && resultCode == RESULT_OK && data != null) {
            if (data.getBooleanExtra("res", false)) {
                month = sp.getStringData(Constant.MONTH);
                year = sp.getStringData(Constant.YEAR);
                entityCode = sp.getStringData(Constant.ENTITY_TYPE_CODE);
                entityId = sp.getLongData(Constant.ENTITY_ID);
                schemeId = sp.getLongData(Constant.SCHEME_ID);
                activityId = sp.getLongData(Constant.ACTIVITY_ID);

                updatedMonth = data.getIntExtra("updatedMonth", 0);
                updatedYear = data.getIntExtra("updatedYear", 0);
                binding.rlApproval.setVisibility(View.VISIBLE);
                ntftActivityDetails();
            }
        }
    }

    private void ntftActivityDetails() {
        if (requestedActivity == 0) {
            searchUrl = "ntfp-tasar/details";
        } else if (requestedActivity == 1) {
            searchUrl = "ntfp/details";
        } else {
            searchUrl = "";
        }
        binding.progress.setVisibility(View.VISIBLE);
        JSONObject object = new JSONObject();
        try {
            object.put("month", month);
            object.put("year", year);
            object.put("schemeId", String.valueOf(schemeId));
            object.put("activityId", String.valueOf(activityId));
            object.put("entityId", String.valueOf(entityId));
            object.put("entityTypeCode", entityCode);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + searchUrl)
                .addHeaders("Authorization", "Bearer " + token)
                .setTag("Activity")
                .addJSONObjectBody(object)
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        binding.progress.setVisibility(View.GONE);
                        if (Utility.isStringValid(response)) {

                            try {
                                JSONObject resObj = new JSONObject(response);
                                if (resObj.optBoolean("outcome")) {
                                    JSONObject obj = resObj.optJSONObject("data");
                                    binding.llBottomPanel.setVisibility(View.VISIBLE);
                                    if (obj != null) {
                                        binding.tvFarmActivityHistory.setEnabled(true);
                                        binding.tvFarmActivityHistory.setClickable(true);
                                        canEdit = obj.optBoolean("canEdit");
                                        String wfStatus = obj.optString("wfStatus");
                                        String comments = obj.optString("comments");
                                        String entityName = obj.optString("entityName");
                                        String commencementDate = obj.optString("commencementDate");
                                        String goatShedType = obj.optString("shedType");
                                        String regularVaccination = obj.optString("regularDeworming");
                                        String prevMonthIncome = obj.optString("prevMonthIncome");
                                        String currMonthIncome = obj.optString("currMonthIncome");
                                        String avgIncome = obj.optString("avgIncome");
                                        String prevMonthYear = obj.optString("prevMonthYear");
                                        String currMonthYear = obj.optString("currMonthYear");
                                        String arrow = obj.optString("arrow");
                                        Long entityId = obj.optLong("entityId");
//                                        numGoats = obj.optLong("numBirds");
                                        String baseCapital = obj.optString("baseCapital");
                                        Long numMembers = obj.optLong("numMembers");
                                        lastReportedMonth = obj.optLong("lastReportedMonth");
                                        lastReportedYear = obj.optLong("lastReportedYear");
                                        String totalProduction = obj.optString("totalProduction");
                                        String totalSales = obj.optString("totalSales");
                                        salesUOM = obj.optString("salesUOM");
                                        productionUOM = obj.optString("productionUOM");
                                        String commencementSeason = obj.optString("commencementSeason");

//                                        boolean isRegisteredForSchemeActivity = obj.optBoolean("isRegisteredForSchemeActivity");

//                                        if (!isRegisteredForSchemeActivity){
//                                            showDialog(resObj.optString("message"));
//                                            binding.rlNewUpdateNtft.setEnabled(false);
//                                            binding.rlNewUpdateNtft.setClickable(false);
//                                            binding.tvFarmActivityHistory.setEnabled(false);
//                                            binding.tvFarmActivityHistory.setClickable(false);
//                                        }

                                        if (updatedMonth == 0 || updatedYear == 0) {
                                            binding.rlApproval.setVisibility(View.GONE);
                                        } else {
                                            binding.rlApproval.setVisibility(View.VISIBLE);
                                        }

                                        if (arrow.equals("D")) {
                                            binding.ivArrowType.setImageResource(R.drawable.down_arrow);
                                        } else {
                                            binding.ivArrowType.setImageResource(R.drawable.up_arrow);
                                        }

                                        commencementDate = commencementDate.equals("null") ? "N/A" : commencementDate;

                                        binding.tvMainUnitSales.setText(salesUOM);
                                        binding.tvMainUnitProd.setText(productionUOM);
                                        binding.tvBaseCapital.setText(getResources().getString(R.string.Rs).concat(" ").concat(baseCapital));
                                        binding.tvCommenceDate.setText(commencementDate);
                                        binding.tvNoOfMembers.setText(String.valueOf(numMembers));
                                        binding.tvIncomeMonthOne.setText(getResources().getString(R.string.Rs).concat(" ").concat(prevMonthIncome));
                                        binding.tvIncomeMonthTwo.setText(getResources().getString(R.string.Rs).concat(" ").concat(currMonthIncome));
                                        binding.tvIncomeMonthOneLbl.setText(getResources().getString(R.string.income_in).concat(" ").concat(prevMonthYear));
                                        binding.tvIncomeMonthTwoLbl.setText(getResources().getString(R.string.income_in).concat(" ").concat(currMonthYear));
                                        binding.tvAvgMonthIncome.setText(getResources().getString(R.string.Rs).concat(" ").concat(avgIncome));
                                        binding.tvToolbarTitleViewMain.setText(entityName);
                                        binding.tvTotSales.setText(totalSales.equals("NaN") ? "0" : totalSales);
                                        binding.tvTotProd.setText(totalProduction.equals("NaN") ? "0" : totalProduction);
                                        binding.tvCommenceSeason.setText(commencementSeason);
                                    }
                                } else {

                                    binding.rlNewUpdateNtft.setEnabled(false);
                                    binding.rlNewUpdateNtft.setClickable(false);
                                    binding.tvFarmActivityHistory.setEnabled(false);
                                    binding.tvFarmActivityHistory.setClickable(false);
//                                    Toast.makeText(BirdsViewMainActivity.this, resObj.optString("message"), Toast.LENGTH_SHORT).show();
                                    showDialog(resObj.optString("message"));
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        binding.progress.setVisibility(View.GONE);
                        Log.e(TAG, "onError: " + anError.getErrorDetail());
                        try {
                            JSONObject errObj = new JSONObject(anError.getErrorBody());
                            int statusCode = errObj.optInt("status");
                            if (statusCode == 500) {
                                sp.clear();
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                        Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                                finish();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

    }

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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = getIntent();
        setResult(RESULT_OK, intent);
        finish();
    }

}