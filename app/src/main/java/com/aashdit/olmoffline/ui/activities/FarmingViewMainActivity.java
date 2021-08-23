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
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.aashdit.olmoffline.BuildConfig;
import com.aashdit.olmoffline.R;
import com.aashdit.olmoffline.databinding.ActivityFarmingViewMainBinding;
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

public class FarmingViewMainActivity extends AppCompatActivity {

    private static final String TAG = "FarmingViewMainActivity";
    int updatedMonth = 0;
    int updatedYear = 0;
    private ActivityFarmingViewMainBinding binding;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFarmingViewMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        resetTitles();
        setSupportActionBar(binding.toolbarViewMainFarm);
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

        navType = sp.getIntData("NAV_TYPE");
        if (entityCode.equals("HH")) {
            binding.rlToogle.setVisibility(View.GONE);
            binding.cvNoOfFarmers.setVisibility(View.GONE);
        } else {
            binding.rlToogle.setVisibility(View.VISIBLE);
            binding.cvNoOfFarmers.setVisibility(View.VISIBLE);
        }
        binding.progress.setVisibility(View.GONE);

        farmActivityDetails();
        binding.tvFarmActivityHistory.setEnabled(false);
        binding.tvFarmActivityHistory.setClickable(false);
        binding.swiperefresh.setOnRefreshListener(() -> {
            if (isGroup) {
                farmActivityIndividualsDetails();
            } else {
                farmActivityDetails();
            }
            binding.swiperefresh.setRefreshing(false);
        });
        binding.llGroups.setVisibility(View.VISIBLE);
        binding.llIndividuals.setVisibility(View.GONE);

        binding.swtFisher.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isGroups = isChecked;
                if (isChecked) {
                    farmActivityIndividualsDetails();
                    binding.llIndividuals.setVisibility(View.VISIBLE);
                    binding.llGroups.setVisibility(View.GONE);
                    binding.llBottomPanel.setVisibility(View.GONE);
                    binding.tvIndiLbl.setTextColor(getResources().getColor(R.color.purple_500));
                    binding.tvGroupLbl.setTextColor(getResources().getColor(R.color.black));
                    binding.tvFarmActivityHistory.setVisibility(View.GONE);
//                    binding.cvHhInd.setVisibility(View.GONE);
                } else {
                    farmActivityDetails();
                    binding.llGroups.setVisibility(View.VISIBLE);
                    binding.llIndividuals.setVisibility(View.GONE);
//                    binding.llBottomPanel.setVisibility(View.VISIBLE);
                    binding.tvIndiLbl.setTextColor(getResources().getColor(R.color.black));
                    binding.tvGroupLbl.setTextColor(getResources().getColor(R.color.purple_500));
                    binding.tvFarmActivityHistory.setVisibility(View.VISIBLE);
//                    binding.cvHhInd.setVisibility(View.VISIBLE);
                }
            }
        });

        binding.tvFarmActivityHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FarmingViewMainActivity.this, FarmingDetailsViewActivity.class);
                intent.putExtra("LAST_MONTH", lastReportedMonth);
                intent.putExtra("LAST_YEAR", lastReportedYear);
                intent.putExtra("PUOM", productionUOM);
                intent.putExtra("SUOM", salesUOM);
                startActivity(intent);
            }
        });

        binding.rlNewUpdateFarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent updateShgActivity = new Intent(FarmingViewMainActivity.this, FarmerUpdateActivity.class);
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
        });

        binding.rlApproval.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });

        binding.llBottomPanel.setVisibility(View.GONE);
    }

    private String salesUOM,productionUOM;

    private void farmActivityDetails() {
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

        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "farm-activity/details")
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
                                        Long numHouseHolds = obj.optLong("numFarmers");
                                        lastReportedMonth = obj.optLong("lastReportedMonth");
                                        lastReportedYear = obj.optLong("lastReportedYear");
                                        double totalProduction = obj.optDouble("totalProduction");
                                        double totalSales = obj.optDouble("totalSales");
                                        salesUOM = obj.optString("salesUOM");
                                        productionUOM = obj.optString("productionUOM");

                                        boolean isRegisteredForSchemeActivity = obj.optBoolean("isRegisteredForSchemeActivity");

                                        if (!isRegisteredForSchemeActivity){
                                            showDialog(resObj.optString("message"));
                                            binding.rlNewUpdateFarm.setEnabled(false);
                                            binding.rlNewUpdateFarm.setClickable(false);
                                            binding.tvFarmActivityHistory.setEnabled(false);
                                            binding.tvFarmActivityHistory.setClickable(false);
                                        }

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

                                        commencementDate = commencementDate.equals("null")?"N/A":commencementDate;

                                        binding.tvMainUnitSales.setText(salesUOM);
                                        binding.tvMainUnitProd.setText(productionUOM);
                                        binding.tvBaseCapital.setText(getResources().getString(R.string.Rs).concat(" ").concat(baseCapital));
                                        binding.tvCommenceDate.setText(commencementDate);
                                        binding.tvNoOfFarmers.setText(String.valueOf(numHouseHolds));
                                        binding.tvIncomeMonthOne.setText(getResources().getString(R.string.Rs).concat(" ").concat(prevMonthIncome));
                                        binding.tvIncomeMonthTwo.setText(getResources().getString(R.string.Rs).concat(" ").concat(currMonthIncome));
                                        binding.tvIncomeMonthOneLbl.setText(getResources().getString(R.string.income_in).concat(" ").concat(prevMonthYear));
                                        binding.tvIncomeMonthTwoLbl.setText(getResources().getString(R.string.income_in).concat(" ").concat(currMonthYear));
                                        binding.tvAvgMonthIncome.setText(getResources().getString(R.string.Rs).concat(" ").concat(avgIncome));
                                        binding.tvToolbarTitleViewMain.setText(entityName);
                                        binding.tvTotSales.setText(String.valueOf(totalSales));
                                        binding.tvTotProd.setText(String.valueOf(totalProduction));
                                    }
                                } else {

                                    binding.rlNewUpdateFarm.setEnabled(false);
                                    binding.rlNewUpdateFarm.setClickable(false);
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

    private void farmActivityIndividualsDetails() {
        binding.progress.setVisibility(View.VISIBLE);
        JSONObject object = new JSONObject();
        try {
            object.put("month", month);
            object.put("year", year);
            object.put("schemeId", String.valueOf(schemeId));
            object.put("activityId", String.valueOf(activityId));
            object.put("entityId", String.valueOf(entityId));
            object.put("reportingLevelCode", entityCode);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "farm-activity/" + entityCode.toLowerCase() + "/rollup")
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

                                    if (obj != null) {
                                        binding.rlNewUpdateFarm.setEnabled(true);
                                        binding.rlNewUpdateFarm.setClickable(true);
                                        binding.tvFarmActivityHistory.setEnabled(true);
                                        binding.tvFarmActivityHistory.setClickable(true);

                                        String entityName = obj.optString("entityName");
                                        String commencementDate = obj.optString("commencementDate");
                                        String prevMonthIncome = obj.optString("prevMonthIncome");
                                        String currMonthIncome = obj.optString("currMonthIncome");
                                        String avgIncome = obj.optString("avgIncome");
                                        String prevMonthYear = obj.optString("prevMonthYear");
                                        String currMonthYear = obj.optString("currMonthYear");
                                        String arrow = obj.optString("arrow");
                                        String baseCapital = obj.optString("baseCapital");
                                        Long numHouseHolds = obj.optLong("numHouseHolds");
                                        lastReportedMonth = obj.optLong("lastReportedMonth");
                                        lastReportedYear = obj.optLong("lastReportedYear");
                                        double totalProduction = obj.optDouble("totalProduction");
                                        double totalSales = obj.optDouble("totalSales");

                                        String salesUOM = obj.optString("salesUOM");
                                        String productionUOM = obj.optString("productionUOM");

                                        binding.tvMainUnitSalesInd.setText(salesUOM);
                                        binding.tvMainUnitProdInd.setText(productionUOM);

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

                                        binding.tvBaseCapitalInd.setText(getResources().getString(R.string.Rs).concat(" ").concat(baseCapital));
                                        binding.tvCommenceDate.setText(commencementDate);
                                        binding.tvNoOfFarmers.setText(String.valueOf(numHouseHolds));
                                        binding.tvIncomeMonthOneInd.setText(getResources().getString(R.string.Rs).concat(" ").concat(prevMonthIncome));
                                        binding.tvIncomeMonthTwoInd.setText(getResources().getString(R.string.Rs).concat(" ").concat(currMonthIncome));
                                        binding.tvIncomeMonthOneLblInd.setText(getResources().getString(R.string.income_in).concat(" ").concat(prevMonthYear));
                                        binding.tvIncomeMonthTwoLblInd.setText(getResources().getString(R.string.income_in).concat(" ").concat(currMonthYear));
                                        binding.tvAvgMonthIncomeInd.setText(getResources().getString(R.string.Rs).concat(" ").concat(avgIncome));
                                        binding.tvToolbarTitleViewMain.setText("");
                                        binding.tvTotSalesInd.setText(String.valueOf(totalSales));
                                        binding.tvTotProdInd.setText(String.valueOf(totalProduction));
                                    }
                                } else {

                                    binding.rlNewUpdateFarm.setEnabled(false);
                                    binding.rlNewUpdateFarm.setClickable(false);
                                    binding.tvFarmActivityHistory.setEnabled(false);
                                    binding.tvFarmActivityHistory.setClickable(false);
                                    Toast.makeText(FarmingViewMainActivity.this, resObj.optString("message"), Toast.LENGTH_SHORT).show();
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
                farmActivityDetails();
            }
        }
    }


    private void showDialog() {
        if (!FarmingViewMainActivity.this.isFinishing()) {
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

        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "farm-activity/submit")
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