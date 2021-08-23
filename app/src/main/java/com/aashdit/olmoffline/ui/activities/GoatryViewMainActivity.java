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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.aashdit.olmoffline.BuildConfig;
import com.aashdit.olmoffline.R;
import com.aashdit.olmoffline.databinding.ActivityViewMainBinding;
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

public class GoatryViewMainActivity extends AppCompatActivity {

    private static final String TAG = "ViewMainActivity";
    private Toolbar mToolbarViewMAinShg;
    private RelativeLayout mRlNewUpdate;

    private TextView mTvActivityHistory;
    private ActivityViewMainBinding binding;

    private String month, year, entityCode;
    private Long entityId, schemeId, activityId;
    private Long lastReportedMonth = 0L;
    private Long lastReportedYear = 0L;
    private SharedPrefManager sp;
    private String token;

    private boolean canEdit;

    private int navType;
    private boolean isGroups = false;


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
        binding = ActivityViewMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        resetTitles();

        sp = SharedPrefManager.getInstance(this);
        token = sp.getStringData(Constant.APP_TOKEN);
        mToolbarViewMAinShg = findViewById(R.id.toolbar_view_main_shg);
        setSupportActionBar(mToolbarViewMAinShg);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
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
        if(entityCode.equals("HH")){
            binding.rlToogle.setVisibility(View.GONE);
        }else {
            binding.rlToogle.setVisibility(View.VISIBLE);
        }

        binding.rlNewUpdateShg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent updateShgActivity = new Intent(GoatryViewMainActivity.this, UpdateShgActivity.class);
                updateShgActivity.putExtra("GOAT_NOS", numGoats);
                updateShgActivity.putExtra("BUCK_NOS", numBucks);
                updateShgActivity.putExtra("ENTITY_CODE", entityCode);
                sp.setLongData(Constant.ENTITY_ID, entityId);
                sp.setLongData(Constant.SCHEME_ID, schemeId);
                sp.setLongData(Constant.ACTIVITY_ID, activityId);
                sp.setStringData(Constant.MONTH, String.valueOf(lastReportedMonth));
                sp.setStringData(Constant.YEAR, String.valueOf(lastReportedYear));
//                sp.setBoolData(Constant.CAN_EDIT, canEdit);

                startActivityForResult(updateShgActivity, 99);
            }
        });

        mTvActivityHistory = findViewById(R.id.tv_activity_history);
        mTvActivityHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GoatryViewMainActivity.this, DetailViewActivity.class);
                intent.putExtra("LAST_MONTH",lastReportedMonth);
                intent.putExtra("LAST_YEAR",lastReportedYear);
                startActivity(intent);
            }
        });

//        binding.tvActivityHistory.setEnabled(false);
//        binding.tvActivityHistory.setClickable(false);
        binding.progress.setVisibility(View.GONE);
        goatearyActivityDetails();
        binding.swiperefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (isGroups) {
                    goatryIndividualsDetails();
                }else{
                    goatearyActivityDetails();
                }
                binding.swiperefresh.setRefreshing(false);
            }
        });

        if (navType == 0){
            binding.cvHh.setVisibility(View.VISIBLE);
        }else {
            binding.cvHh.setVisibility(View.GONE);
        }
        binding.llGroups.setVisibility(View.VISIBLE);
        binding.llIndividuals.setVisibility(View.GONE);
        binding.swtGotary.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isGroups = isChecked;
                if (isChecked) {
                    goatryIndividualsDetails();
                    binding.llIndividuals.setVisibility(View.VISIBLE);
                    binding.llGroups.setVisibility(View.GONE);
                    binding.llBottomPanel.setVisibility(View.GONE);
                    binding.tvIndiLbl.setTextColor(getResources().getColor(R.color.purple_500));
                    binding.tvGroupLbl.setTextColor(getResources().getColor(R.color.black));
                    binding.tvActivityHistory.setVisibility(View.GONE);
                    binding.cvHhInd.setVisibility(View.GONE);
                } else {
                    goatearyActivityDetails();
                    binding.llGroups.setVisibility(View.VISIBLE);
                    binding.llIndividuals.setVisibility(View.GONE);
                    binding.llBottomPanel.setVisibility(View.VISIBLE);
                    binding.tvIndiLbl.setTextColor(getResources().getColor(R.color.black));
                    binding.tvGroupLbl.setTextColor(getResources().getColor(R.color.purple_500));
                    binding.tvActivityHistory.setVisibility(View.VISIBLE);
                    binding.cvHhInd.setVisibility(View.VISIBLE);
                }
            }
        });
        if (updatedMonth == 0 || updatedYear == 0) {
            binding.rlApproval.setVisibility(View.GONE);
        } else {
            binding.rlApproval.setVisibility(View.VISIBLE);
        }
        binding.rlApproval.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });

        if (sp.getStringData(Constant.USER_ROLE).equals("ROLE_CRPCM")){
            binding.llBottomPanel.setVisibility(View.GONE);
        }else {
            binding.llBottomPanel.setVisibility(View.VISIBLE);
        }
    }

    private void showDialog() {
        if (!GoatryViewMainActivity.this.isFinishing()) {
            new AlertDialog.Builder(this)
                    .setTitle("Are you sure ?")
                    .setMessage("Are you sure you want to send for approval ?")

                    // Specifying a listener allows you to take an action before dismissing the dialog.
                    // The dialog is automatically dismissed when a dialog button is clicked.
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Continue with delete operation
                            //TODO call send for approval api
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

        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "goatry-activity/submit")
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
//                                    Toast.makeText(GoatryViewMainActivity.this, resObj.optString("message"), Toast.LENGTH_SHORT).show();
                                    showDialog(resObj.optString("message"));
                                    binding.rlApproval.setVisibility(View.GONE);
//                                    goatearyActivityDetails();
                                }else {
//                                    Toast.makeText(GoatryViewMainActivity.this, resObj.optString("message"), Toast.LENGTH_SHORT).show();
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
                            if (statusCode==500){
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

    private void goatryIndividualsDetails() {

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

        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "goatry-activity/"+entityCode.toLowerCase()+"/rollup")
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
                                        binding.rlNewUpdateShg.setEnabled(true);
                                        binding.rlNewUpdateShg.setClickable(true);
                                        binding.tvActivityHistory.setEnabled(true);
                                        binding.tvActivityHistory.setClickable(true);
                                        String entityName = obj.optString("entityName");
                                        String commencementDate = obj.optString("commencementDate");
                                        String goatShedType = obj.optString("goatShedType");
                                        String regularVaccination = obj.optString("regularVaccination");
                                        String prevMonthIncome = obj.optString("prevMonthIncome");
                                        String currMonthIncome = obj.optString("currMonthIncome");
                                        String avgIncome = obj.optString("avgIncome");
                                        String prevMonthYear = obj.optString("prevMonthYear");
                                        String currMonthYear = obj.optString("currMonthYear");
                                        String arrow = obj.optString("arrow");
                                        Long entityId = obj.optLong("entityId");
                                        Long numGoats = obj.optLong("numGoats");
                                        Long numBucks = obj.getLong("numBucks");
                                        String baseCapital = obj.optString("baseCapital");
                                        Long numHouseHolds = obj.optLong("numHouseHolds");
                                        lastReportedMonth = obj.optLong("lastReportedMonth");
                                        lastReportedYear = obj.optLong("lastReportedYear");

                                        if (arrow.equals("D")) {
                                            binding.ivArrowType.setImageResource(R.drawable.down_arrow);
                                        } else {
                                            binding.ivArrowType.setImageResource(R.drawable.up_arrow);
                                        }

                                        binding.tvBaseCapitalInd.setText(getResources().getString(R.string.Rs).concat(" ").concat(baseCapital));
//                                        binding.tvCommenceDate.setText(commencementDate);
                                        binding.tvHouseholdsInd.setText(String.valueOf(numHouseHolds));
                                        binding.tvNoOfGoatsInd.setText(String.valueOf(numGoats));
                                        binding.tvNoOfBucksInd.setText(String.valueOf(numBucks));
//                                        binding.tvGoatShedStatus.setText(goatShedType);
//                                        binding.tvRegularVaccinationStatus.setText(regularVaccination);
                                        binding.tvIncomeMonthOneInd.setText(getResources().getString(R.string.Rs).concat(" ").concat(prevMonthIncome));
                                        binding.tvIncomeMonthTwoInd.setText(getResources().getString(R.string.Rs).concat(" ").concat(currMonthIncome));
                                        binding.tvIncomeMonthOneLblInd.setText(getResources().getString(R.string.income_in).concat(" ").concat(prevMonthYear));
                                        binding.tvIncomeMonthTwoLblInd.setText(getResources().getString(R.string.income_in).concat(" ").concat(currMonthYear));
                                        binding.tvAvgMonthIncomeInd.setText(getResources().getString(R.string.Rs).concat(" ").concat(avgIncome));
                                        binding.tvToolbarTitleViewMain.setText("");
                                    }
                                } else {

                                    binding.rlNewUpdateShg.setEnabled(false);
                                    binding.rlNewUpdateShg.setClickable(false);
                                    binding.tvActivityHistory.setEnabled(false);
                                    binding.tvActivityHistory.setClickable(false);
                                    Toast.makeText(GoatryViewMainActivity.this, resObj.optString("message"), Toast.LENGTH_SHORT).show();
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
                            if (statusCode==500){
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

    private Long numBucks;
    private Long numGoats;

    private void goatearyActivityDetails() {
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

        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "goatry-activity/details")
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
                                        binding.tvActivityHistory.setEnabled(true);
                                        binding.tvActivityHistory.setClickable(true);
                                        canEdit = obj.optBoolean("canEdit");
                                        String wfStatus = obj.optString("wfStatus");
                                        String comments = obj.optString("comments");
                                        String entityName = obj.optString("entityName");
                                        String commencementDate = obj.optString("commencementDate");
                                        String goatShedType = obj.optString("goatShedType");
                                        String regularVaccination = obj.optString("regularVaccination");
                                        String prevMonthIncome = obj.optString("prevMonthIncome");
                                        String currMonthIncome = obj.optString("currMonthIncome");
                                        String avgIncome = obj.optString("avgIncome");
                                        String prevMonthYear = obj.optString("prevMonthYear");
                                        String currMonthYear = obj.optString("currMonthYear");
                                        String arrow = obj.optString("arrow");
                                        String bucksTied = obj.optString("bucksTied");
                                        String regularDeworming = obj.optString("regularDeworming");
                                        Long entityId = obj.optLong("entityId");
                                        numGoats = obj.optLong("numGoats");
                                        numBucks = obj.getLong("numBucks");
                                        String baseCapital = obj.optString("baseCapital");
                                        Long numHouseHolds = obj.optLong("numHouseHolds");
                                        lastReportedMonth = obj.optLong("lastReportedMonth");
                                        lastReportedYear = obj.optLong("lastReportedYear");
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
                                        binding.tvRegularDewormingStatus.setText(regularDeworming);
                                        binding.tvBuckTied.setText(bucksTied);
                                        binding.tvBaseCapital.setText(getResources().getString(R.string.Rs) + " " + baseCapital);
                                        binding.tvCommenceDate.setText(commencementDate);
                                        binding.tvHouseholds.setText("" + numHouseHolds);
                                        binding.tvNoOfGoats.setText("" + numGoats);
                                        binding.tvNoOfBucks.setText("" + numBucks);
                                        binding.tvGoatShedStatus.setText(goatShedType);
                                        binding.tvRegularVaccinationStatus.setText(regularVaccination);
                                        binding.tvIncomeMonthOne.setText(getResources().getString(R.string.Rs) + " " + prevMonthIncome);
                                        binding.tvIncomeMonthTwo.setText(getResources().getString(R.string.Rs) + " " + currMonthIncome);
                                        binding.tvIncomeMonthOneLbl.setText(getResources().getString(R.string.income_in).concat(" ").concat(prevMonthYear));
                                        binding.tvIncomeMonthTwoLbl.setText(getResources().getString(R.string.income_in).concat(" ").concat(currMonthYear));
                                        binding.tvAvgMonthIncome.setText(getResources().getString(R.string.Rs) + " " + avgIncome);
                                        binding.tvToolbarTitleViewMain.setText(entityName);
                                    } else {

                                    }
                                } else {

                                    binding.rlNewUpdateShg.setEnabled(false);
                                    binding.rlNewUpdateShg.setClickable(false);
                                    binding.tvActivityHistory.setEnabled(false);
                                    binding.tvActivityHistory.setClickable(false);
//                                    Toast.makeText(GoatryViewMainActivity.this, resObj.optString("message"), Toast.LENGTH_SHORT).show();
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
                            if (statusCode==500){
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
        if (!GoatryViewMainActivity.this.isFinishing()) {
            new AlertDialog.Builder(this)
                    .setTitle("")
                    .setMessage(message)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Continue with delete operation

                        }
                    }).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }


    int updatedMonth = 0;
    int updatedYear = 0;

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
                goatearyActivityDetails();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = getIntent();
        setResult(RESULT_OK, intent);
        finish();
    }
}