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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.aashdit.olmoffline.BuildConfig;
import com.aashdit.olmoffline.R;
import com.aashdit.olmoffline.databinding.ActivityFishViewMainBinding;
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

public class FishViewMainActivity extends AppCompatActivity {

    private static final String TAG = "FishViewMainActivity";

    private ActivityFishViewMainBinding binding;
    private String month, year, entityCode;
    private Long entityId, schemeId, activityId;
    private SharedPrefManager sp;
    private String token;

    private int navType;

    private boolean canEdit;
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
        binding = ActivityFishViewMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        resetTitles();
        setSupportActionBar(binding.toolbarViewMainFish);
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

        binding.llContainerClfData.setVisibility(View.GONE);
        if (entityCode.equals("CLF")){
            binding.llContainerClfData.setVisibility(View.VISIBLE);
        }else{
            binding.llContainerClfData.setVisibility(View.GONE);
        }


        navType = sp.getIntData("NAV_TYPE");
        if (entityCode.equals("HH")) {
            binding.rlToogle.setVisibility(View.GONE);
            binding.cvNoOfFarmers.setVisibility(View.GONE);
        } else {
            binding.rlToogle.setVisibility(View.VISIBLE);
            binding.cvNoOfFarmers.setVisibility(View.VISIBLE);
        }
        binding.progress.setVisibility(View.GONE);
        fisheryActivityDetails();
        binding.swiperefresh.setOnRefreshListener(() -> {
            if (isGroups) {
                fisheryIndividualsDetails();
            } else {
                fisheryActivityDetails();
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
                    fisheryIndividualsDetails();
                    binding.llIndividuals.setVisibility(View.VISIBLE);
                    binding.llGroups.setVisibility(View.GONE);
                    binding.llBottomPanel.setVisibility(View.GONE);
                    binding.tvIndiLbl.setTextColor(getResources().getColor(R.color.purple_500));
                    binding.tvGroupLbl.setTextColor(getResources().getColor(R.color.black));
                    binding.tvFishActivityHistory.setVisibility(View.GONE);
                    binding.cvHhInd.setVisibility(View.GONE);
                } else {
                    fisheryActivityDetails();
                    binding.tvFishActivityHistory.setVisibility(View.VISIBLE);
                    binding.cvHhInd.setVisibility(View.VISIBLE);
                    binding.llGroups.setVisibility(View.VISIBLE);
                    binding.llIndividuals.setVisibility(View.GONE);
                    binding.llBottomPanel.setVisibility(View.VISIBLE);
                    binding.tvIndiLbl.setTextColor(getResources().getColor(R.color.black));
                    binding.tvGroupLbl.setTextColor(getResources().getColor(R.color.purple_500));
                }
            }
        });


        if (sp.getStringData(Constant.USER_ROLE).equals("ROLE_CRPCM")) {
            binding.llBottomPanel.setVisibility(View.GONE);
        } else {
            binding.llBottomPanel.setVisibility(View.VISIBLE);
        }

        binding.rlNewUpdateFish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent updateShgActivity = new Intent(FishViewMainActivity.this, FishUpdateActivity.class);
//                updateShgActivity.putExtra("BIRD_NOS",numGoats);
                updateShgActivity.putExtra("ENTITY_CODE", entityCode);
                sp.setLongData(Constant.ENTITY_ID, entityId);
                sp.setLongData(Constant.SCHEME_ID, schemeId);
                sp.setLongData(Constant.ACTIVITY_ID, activityId);
                sp.setStringData(Constant.ENTITY_TYPE_CODE, entityCode);

                startActivityForResult(updateShgActivity, 99);
            }
        });
        binding.rlApproval.setVisibility(View.GONE);
        binding.rlApproval.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });

        binding.cvQtyFood.setVisibility(View.GONE);
        binding.cvNettingFreq.setVisibility(View.GONE);
        binding.tvFishActivityHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FishViewMainActivity.this, FishDetailViewActivity.class);
                intent.putExtra("LAST_MONTH",lastReportedMonth);
                intent.putExtra("LAST_YEAR",lastReportedYear);
                startActivity(intent);
            }
        });
    }

    private void showDialog() {
        if (!FishViewMainActivity.this.isFinishing()) {
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

    private void showDialog(String message) {
        if (!FishViewMainActivity.this.isFinishing()) {
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
            object.put("reportingLevelCode", entityCode);
            object.put("newStatus", "SUBMITTED_TO_MBK");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "fishery-activity/submit")
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
//                                    binding.rlNewUpdateShg.setEnabled(false);//false
//                                    binding.rlNewUpdateShg.setClickable(false);//false
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

    private Long lastReportedMonth = 0L, lastReportedYear = 0L;

    private void fisheryActivityDetails() {

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

        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "fishery-activity/details")
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
                                        binding.tvFishActivityHistory.setEnabled(true);
                                        binding.tvFishActivityHistory.setClickable(true);
                                        canEdit = obj.optBoolean("canEdit");
                                        String wfStatus = obj.optString("wfStatus");
                                        String comments = obj.optString("comments");
                                        String entityName = obj.optString("entityName");
                                        String commencementDate = obj.optString("commencementDate");
                                        String prevMonthIncome = obj.optString("prevMonthIncome");
                                        String currMonthIncome = obj.optString("currMonthIncome");
                                        String avgIncome = obj.optString("avgIncome");
                                        String prevMonthYear = obj.optString("prevMonthYear");
                                        String currMonthYear = obj.optString("currMonthYear");
                                        String arrow = obj.optString("arrow");
                                        entityId = obj.optLong("entityId");
                                        Long numFrys = obj.optLong("numFrys");
                                        Long numFingerlings = obj.optLong("numFingerlings");
                                        Long numYearlings = obj.optLong("numYearlings");
                                        Long numFishHarvested = obj.optLong("numFishHarvested");
                                        Long numFingerlingsHarvested = obj.optLong("numFingerlingsHarvested");
                                        String areaOfPond = obj.optString("areaOfPond");
                                        String baseCapital = obj.optString("baseCapital");
                                        Long numHouseHolds = obj.optLong("numFarmers");
                                        Long nettingFrequency = obj.optLong("nettingFrequency");

                                        Long fryFeedQty = obj.optLong("fryFeedQty");
                                        Long fingerlingFeedQty = obj.optLong("fingerlingFeedQty");
                                        Long yearlingFeedQty = obj.optLong("yearlingFeedQty");
                                        lastReportedMonth = obj.optLong("lastReportedMonth");
                                        lastReportedYear = obj.optLong("lastReportedYear");

                                        boolean regularNettingDone = obj.optBoolean("regularNettingDone");
                                        binding.swtNettingMgnt.setEnabled(false);
                                        if (regularNettingDone) {
                                            binding.swtNettingMgnt.setChecked(true);
                                            binding.cvNettingFreq.setVisibility(View.VISIBLE);
                                        }else {
                                            binding.swtNettingMgnt.setChecked(false);
                                            binding.cvNettingFreq.setVisibility(View.GONE);
                                        }


                                        boolean feedManagementDone = obj.optBoolean("feedManagementDone");
                                        binding.swtFeedingMgnt.setEnabled(false);
                                        if(feedManagementDone){
                                            binding.swtFeedingMgnt.setChecked(true);
                                            binding.cvQtyFood.setVisibility(View.VISIBLE);
                                        }else{
                                            binding.swtFeedingMgnt.setChecked(false);
                                            binding.cvQtyFood.setVisibility(View.GONE);
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

                                        binding.etFryFeedQty.setText(String.valueOf(fryFeedQty));
                                        binding.etFryFeedQty.setEnabled(false);
                                        binding.etFryFeedQty.setClickable(false);
                                        binding.etFingFeedQty.setText(String.valueOf(fingerlingFeedQty));
                                        binding.etFingFeedQty.setEnabled(false);
                                        binding.etFingFeedQty.setClickable(false);
                                        binding.etYearlingsFeedKg.setText(String.valueOf(yearlingFeedQty));
                                        binding.etYearlingsFeedKg.setEnabled(false);
                                        binding.etYearlingsFeedKg.setClickable(false);
                                        binding.tvFrequencyOfNeeting.setText(String.valueOf(nettingFrequency));
                                        binding.tvNoOfFingerling.setText(""+numFingerlingsHarvested);
                                        binding.tvNoOfTabSizeFish.setText(""+numFishHarvested);
                                        binding.tvBaseCapital.setText(getResources().getString(R.string.Rs).concat(" ").concat(baseCapital));
                                        binding.tvCommenceDate.setText(commencementDate);
                                        binding.tvAreaOfPonds.setText(areaOfPond.concat(" Hec"));
                                        binding.tvNoOfFarmers.setText(String.valueOf(numHouseHolds));
                                        binding.tvIncomeMonthOne.setText(getResources().getString(R.string.Rs).concat(" ").concat(prevMonthIncome));
                                        binding.tvIncomeMonthTwo.setText(getResources().getString(R.string.Rs).concat(" ").concat(currMonthIncome));
                                        binding.tvIncomeMonthOneLbl.setText(getResources().getString(R.string.income_in).concat(" ").concat(prevMonthYear));
                                        binding.tvIncomeMonthTwoLbl.setText(getResources().getString(R.string.income_in).concat(" ").concat(currMonthYear));
                                        binding.tvAvgMonthIncome.setText(getResources().getString(R.string.Rs).concat(" ").concat(avgIncome));
                                        binding.tvToolbarTitleViewMain.setText(entityName);
                                    }
                                } else {
                                    showDialog(resObj.optString("message"));
                                    binding.rlNewUpdateFish.setEnabled(false);
                                    binding.rlNewUpdateFish.setClickable(false);
                                    binding.tvFishActivityHistory.setEnabled(false);
                                    binding.tvFishActivityHistory.setClickable(false);
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

    private void fisheryIndividualsDetails() {
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

        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "fishery-activity/"+entityCode.toLowerCase()+"/rollup")
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
                                        binding.tvFishActivityHistory.setEnabled(true);
                                        binding.tvFishActivityHistory.setClickable(true);
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
                                        Long numFrys = obj.optLong("numFrys");
                                        Long numFingerlings = obj.optLong("numFingerlings");
                                        Long numFingerlingsHarvested = obj.optLong("numFingerlingsHarvested");
                                        Long numYearlings = obj.optLong("numYearlings");
                                        Long numFishHarvested = obj.optLong("numFishHarvested");
                                        Double areaOfPond = obj.optDouble("areaOfPond");
                                        String baseCapital = obj.optString("baseCapital");
                                        Long numHouseHolds = obj.optLong("numFarmers");
                                        lastReportedMonth = obj.optLong("lastReportedMonth");
                                        lastReportedYear = obj.optLong("lastReportedYear");

                                        if (arrow.equals("D")) {
                                            binding.ivArrowType.setImageResource(R.drawable.down_arrow);
                                        } else {
                                            binding.ivArrowType.setImageResource(R.drawable.up_arrow);
                                        }

                                        binding.tvNoOfFingerlingInd.setText(String.valueOf(numFingerlingsHarvested));
                                        binding.tvNoOfYearlingInd.setText(String.valueOf(numFishHarvested));
                                        binding.tvBaseCapitalInd.setText(getResources().getString(R.string.Rs).concat(" ").concat(baseCapital));
                                        binding.tvHouseholdsInd.setText(String.valueOf(numHouseHolds));
                                        binding.tvIncomeMonthOneInd.setText(getResources().getString(R.string.Rs).concat(" ").concat(prevMonthIncome));
                                        binding.tvIncomeMonthTwoInd.setText(getResources().getString(R.string.Rs).concat(" ").concat(currMonthIncome));
                                        binding.tvIncomeMonthOneLblInd.setText(getResources().getString(R.string.income_in).concat(" ").concat(prevMonthYear));
                                        binding.tvIncomeMonthTwoLblInd.setText(getResources().getString(R.string.income_in).concat(" ").concat(currMonthYear));
                                        binding.tvAvgMonthIncomeInd.setText(getResources().getString(R.string.Rs).concat(" ").concat(avgIncome));
                                        binding.tvToolbarTitleViewMain.setText("");
                                    }
                                } else {
                                    binding.tvFishActivityHistory.setEnabled(false);
                                    binding.tvFishActivityHistory.setClickable(false);
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
                fisheryActivityDetails();
            }
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