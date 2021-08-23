package com.aashdit.olmoffline.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aashdit.olmoffline.BuildConfig;
import com.aashdit.olmoffline.R;
import com.aashdit.olmoffline.adapters.MonthListAdapter;
import com.aashdit.olmoffline.adapters.SkillSpnAdapter;
import com.aashdit.olmoffline.databinding.ActivityNFUpdateBinding;
import com.aashdit.olmoffline.models.Month;
import com.aashdit.olmoffline.models.Skill;
import com.aashdit.olmoffline.ui.MainActivity;
import com.aashdit.olmoffline.utils.Constant;
import com.aashdit.olmoffline.utils.LocaleManager;
import com.aashdit.olmoffline.utils.SharedPrefManager;
import com.aashdit.olmoffline.utils.Utility;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static android.content.pm.PackageManager.GET_META_DATA;

public class NFUpdateActivity extends AppCompatActivity implements MonthListAdapter.MonthListener{

    private static final String TAG = "NFUpdateActivity";
    private ActivityNFUpdateBinding binding;

    private String remarks = "";
    private String token = "";

    private SharedPrefManager sp;

    int monthNo;
    int monthForBlockFutureUpdate;

    String selectedYear = "";
    private final ArrayList<Month> monthList = new ArrayList<>();
    private boolean isFirst = true;
    private boolean hasTrained = false;
    private Long schemeId, entityId, activityId, skillTypeId;
    private String entityCode = "";
    private final ArrayList<String> yearsList = new ArrayList<>();

    private String totalSales, totalInputCost, totalProfit;

    private final ArrayList<Skill> skills = new ArrayList<>();
    private SkillSpnAdapter skillSpnAdapter;

    private int Year = 0;
    private int Month = 0;
    private MonthListAdapter monthListAdapter;
    String salesAmount = "", inputAmount = "";

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
        binding = ActivityNFUpdateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        resetTitles();
        sp = SharedPrefManager.getInstance(this);
        token = sp.getStringData(Constant.APP_TOKEN);
        setSupportActionBar(binding.toolbarUpdateActivity);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.progress.setVisibility(View.GONE);
        try {
            monthNo = Integer.parseInt(sp.getStringData(Constant.MONTH));
        } catch (Exception e) {
            monthNo = 1;
            e.printStackTrace();
        }
        schemeId = sp.getLongData(Constant.SCHEME_ID);
        activityId = sp.getLongData(Constant.ACTIVITY_ID);
        entityId = sp.getLongData(Constant.ENTITY_ID);
        entityCode = getIntent().getStringExtra("ENTITY_CODE");
        selectedYear = sp.getStringData(Constant.YEAR);


        Long date = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date resultdate = new Date(date);
//         monthNo = resultdate.getMonth();
        System.out.println(sdf.format(resultdate));

        String s = sdf.format(resultdate);
        String[] parts = s.split("/");
        String monthForFutureUpdate = parts[1];
        String yearNo = parts[2]; //sp.getStringData("YEAR");
//        selectedYear = yearNo;
        monthForBlockFutureUpdate = Integer.parseInt(monthForFutureUpdate);
        if (selectedYear.equals("0")) {
            selectedYear = yearNo;
        }

        int currentYear = Integer.parseInt(selectedYear);
        int currentOneDown = currentYear - 1;
        int currentTwoDown = currentYear - 2;
        int currentThreeDown = currentYear - 3;
        int currentFourDown = currentYear - 4;
        int currentOneUp = currentYear + 1;
        int currentTwoUp = currentYear + 2;
        int currentThreeUp = currentYear + 3;
        int currentFourUp = currentYear + 4;

        yearsList.add(String.valueOf(currentFourDown));
        yearsList.add(String.valueOf(currentThreeDown));
        yearsList.add(String.valueOf(currentTwoDown));
        yearsList.add(String.valueOf(currentOneDown));
        yearsList.add(String.valueOf(currentYear));
        yearsList.add(String.valueOf(currentOneUp));
        yearsList.add(String.valueOf(currentTwoUp));
        yearsList.add(String.valueOf(currentThreeUp));
        yearsList.add(String.valueOf(currentFourUp));

        generateMonth(selectedYear);

        Skill leader = new Skill();
        leader.valueId = 0L;
        leader.valueEn = "Select Skill";
        skills.add(leader);

        skillSpnAdapter = new SkillSpnAdapter(this,skills);
        binding.spnNfSkill.setAdapter(skillSpnAdapter);
        binding.spnNfSkill.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0){
                    skillTypeId = 0L;
                }else{
                    skillTypeId = skills.get(position).valueId;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        getSkils();

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, yearsList);
        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        binding.spnYear.setAdapter(dataAdapter);
        binding.spnYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isFirst) {
                    if (selectedYear.equals("0")) {
                        selectedYear = yearsList.get(4);
                        binding.spnYear.setSelection(4);
                    }else{
                        selectedYear = yearsList.get(yearsList.indexOf(selectedYear));
                        binding.spnYear.setSelection(yearsList.indexOf(selectedYear));
                    }
                    generateMonth(selectedYear);
                    Year = Integer.parseInt(selectedYear);
                    monthListAdapter.notifyDataSetChanged();
                    getViewDetails();
                    isFirst = false;
                } else {
                    selectedYear = yearsList.get(position);
                    generateMonth(selectedYear);
                    Year = Integer.parseInt(selectedYear);
//                    Month = 0;
                    monthListAdapter.restMonthSelected(-1);
                    monthListAdapter.notifyDataSetChanged();
//                    getViewDetails();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        Month = monthNo;
        monthListAdapter = new MonthListAdapter(this, monthList, monthNo - 1);
        monthListAdapter.setMonthListener(this);
        binding.rvUpdateMonthList.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        binding.rvUpdateMonthList.setAdapter(monthListAdapter);
        binding.rvUpdateMonthList.getLayoutManager().scrollToPosition(monthNo - 1);

        binding.cvSkills.setVisibility(View.GONE);

        binding.switchHasTrained.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                hasTrained = isChecked;
                if (isChecked) {
                    binding.cvSkills.setVisibility(View.VISIBLE);
                } else {
//                    skillTypeId = 0L;
                    binding.cvSkills.setVisibility(View.GONE);
                }
            }
        });

        binding.etUpdateRemarks.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (binding.etUpdateRemarks.hasFocus()) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    switch (event.getAction() & MotionEvent.ACTION_MASK) {
                        case MotionEvent.ACTION_SCROLL:
                            v.getParent().requestDisallowInterceptTouchEvent(false);
                            return true;
                    }
                }
                return false;
            }
        });
//        getViewDetails();


        binding.etNfTotSales.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                salesAmount = s.toString();

                if(salesAmount.equals("") || inputAmount.equals("")){
                    binding.etNfTotProfit.setText("");
                }
                try {
                    Double sa = Double.parseDouble(salesAmount);
                    Double ia = Double.parseDouble(inputAmount);
                    String amt = String.valueOf(sa-ia);
//                    if(amt.contains("E")){
//                        String[] splAmt = amt.split("E");
//                        binding.etNfTotProfit.setText(splAmt[0]);
//                    }else{
//                        binding.etNfTotProfit.setText(amt);
//                    }
                    binding.etNfTotProfit.setText(String.format("%.2f", Double.parseDouble(amt)));

                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        binding.etNfTotInputCost.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                inputAmount = s.toString();

                if(salesAmount.equals("") || inputAmount.equals("")){
                    binding.etNfTotProfit.setText("");
                }
                try {
                    Double sa = Double.parseDouble(salesAmount);
                    Double ia = Double.parseDouble(inputAmount);

                    String amt = String.valueOf(sa-ia);

                    binding.etNfTotProfit.setText(String.format("%.2f", Double.parseDouble(amt)));

                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        binding.rlActivityUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int curYear = Integer.parseInt(yearNo);
                /**
                 * old logic
                 * Integer.parseInt(yearNo) > Year && monthForBlockFutureUpdate > clickedPosition
                 *                         || Integer.parseInt(yearNo) > Year && (monthForBlockFutureUpdate == clickedPosition + 1)
                 * */
                if (Year == curYear && clickedPosition + 1 <= Integer.parseInt(monthForFutureUpdate) ||
                        (Year < curYear && clickedPosition + 1 <= 12)) {

                    totalSales = binding.etNfTotSales.getText().toString().trim();
                    totalInputCost = binding.etNfTotInputCost.getText().toString().trim();
                    totalProfit = binding.etNfTotProfit.getText().toString().trim();
                    remarks = binding.etUpdateRemarks.getText().toString().trim();

                    if (selectedYear.equals("Select a Year")) {
                        Toast.makeText(NFUpdateActivity.this, "Please Select a Year", Toast.LENGTH_SHORT).show();
                    } else if (Month == 0/*selectedMonth.equals(getResources().getString(R.string.select_month))*/) {
                        Toast.makeText(NFUpdateActivity.this, "Please Select a Month", Toast.LENGTH_SHORT).show();
                    } else if (hasTrained && skillTypeId.equals(0L)) {
                        Toast.makeText(NFUpdateActivity.this, "Please Select Skill", Toast.LENGTH_SHORT).show();
                    } else if (totalSales.equals("")) {
                        Toast.makeText(NFUpdateActivity.this, "Please enter Total Sales", Toast.LENGTH_SHORT).show();
                    } else if (totalInputCost.equals("")) {
                        Toast.makeText(NFUpdateActivity.this, "Please enter input cost", Toast.LENGTH_SHORT).show();
                    } else {
                        doMonthlyReportForNonFarm();
                    }
                } else if (Integer.parseInt(selectedYear) > currentYear) {
                    Toast.makeText(NFUpdateActivity.this, "Can't Update Future Date", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(NFUpdateActivity.this, "Can't Update Future Date", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }



    private void getSkils() {
        AndroidNetworking.get(BuildConfig.BASE_URL + Constant.API_PATH + "lov/skill")
                .addHeaders("Authorization", "Bearer " + token)
                .setTag("Activity")
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        if (Utility.isStringValid(response)) {
                            try {
                                JSONObject resObj = new JSONObject(response);
                                if (resObj.optBoolean("outcome")) {
                                    JSONArray array = resObj.optJSONArray("data");
                                    if (array != null && array.length() > 0) {
                                        skills.clear();
                                        Skill leader = new Skill();
                                        leader.valueId = 0L;
                                        leader.valueEn = "Select Skill";
                                        skills.add(leader);

                                        for (int i = 0; i < array.length(); i++) {
                                            Skill disability = Skill.parseSkills(array.optJSONObject(i));
                                            skills.add(disability);
                                        }
                                        skillSpnAdapter.notifyDataSetChanged();
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


    private boolean isUpdate;

    private void getViewDetails() {

        binding.progress.setVisibility(View.VISIBLE);
        JSONObject object = new JSONObject();
        try {
            object.put("month", Month);
            object.put("year", selectedYear);
            object.put("schemeId", String.valueOf(schemeId));
            object.put("activityId", String.valueOf(activityId));
            object.put("entityId", String.valueOf(entityId));
            object.put("entityTypeCode", entityCode);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "non-farm-activity/view-line")
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
                                    binding.tvWfStatus.setVisibility(View.VISIBLE);
                                    /** tempMonth this variable is taken for to show previously selected month */
                                    int tempMonth = Month;
                                    monthListAdapter.restMonthSelected(tempMonth - 1);
                                    monthListAdapter.notifyDataSetChanged();
                                    nonFarmActivityDetails();
                                    if (obj != null) {
                                        entityId = obj.optLong("entityId");

                                        String totalSales = obj.optString("totalSales");
                                        String totalExpenditure = obj.optString("totalExpenditure");
                                        String totalProfit = obj.optString("totalProfit");
                                        String remarks = obj.optString("remarks");
                                        String currentWFStatus = obj.optString("currentWFStatus");
                                        boolean hasAquiredTraining = obj.optBoolean("hasAquiredTraining");

                                        skillTypeId = obj.optLong("skillTypeId");

                                        binding.switchHasTrained.setChecked(hasAquiredTraining);
//
                                        binding.tvWfStatus.setText(currentWFStatus);

                                        binding.etNfTotSales.setText(totalSales.replace(",",""));
                                        binding.etNfTotProfit.setText(totalProfit.replace(",",""));
                                        binding.etUpdateRemarks.setText(remarks);
                                        binding.etNfTotInputCost.setText(totalExpenditure.replace(",",""));

                                        binding.etUpdateRemarks.setText(remarks);
                                        binding.rlActivityUpdate.setEnabled(true);
                                        binding.rlActivityUpdate.setClickable(true);

                                        for (int i = 0; i < skills.size(); i++) {
                                            if (skills.get(i).valueId.equals(skillTypeId)){
                                                binding.spnNfSkill.setSelection(i);
                                                skillSpnAdapter.notifyDataSetChanged();
                                            }
                                        }

                                    }
                                } else {

                                    binding.tvWfComments.setText("");

                                    binding.etUpdateRemarks.setText("");
                                    binding.etNfTotProfit.setText("");
                                    binding.etNfTotInputCost.setText("");
                                    binding.etNfTotSales.setText("");


                                    binding.rlActivityUpdate.setEnabled(false);
                                    binding.rlActivityUpdate.setClickable(false);


                                    binding.etUpdateRemarks.setText("");
                                    enableInputs();
                                    binding.tvWfStatus.setVisibility(View.GONE);
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

    private void nonFarmActivityDetails() {
        binding.progress.setVisibility(View.VISIBLE);
        JSONObject object = new JSONObject();
        try {
            object.put("month", Month);
            object.put("year", selectedYear);
            object.put("schemeId", String.valueOf(schemeId));
            object.put("activityId", String.valueOf(activityId));
            object.put("entityId", String.valueOf(entityId));
            object.put("entityTypeCode", entityCode);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "non-farm-activity/details")
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

                                        canEdit = obj.optBoolean("canEdit");
                                        String wfStatus = obj.optString("wfStatus");
                                        String comments = obj.optString("comments");
                                        String status = wfStatus.replace("_", " ");
                                        binding.tvWfStatus.setText(getResources().getString(R.string.status).concat(" ").concat(status));
                                        comments = comments.equals("null") ? "" : comments;
                                        binding.tvWfComments.setText(comments);
                                        if (canEdit) {
                                            enableInputs();
                                        } else {
                                            disableInputs();
                                        }
                                    }
                                } else {

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


    private void doMonthlyReportForNonFarm() {
        binding.progress.setVisibility(View.VISIBLE);
        JSONObject object = new JSONObject();
        try {
            object.put("schemeId", sp.getLongData("SCHEME_ID"));
            object.put("activityId", sp.getLongData("ACTIVITY_ID"));
            object.put("entityId", sp.getLongData("ENTITY_ID"));
            object.put("reportingLevelCode", sp.getStringData("ENTITY_TYPE_CODE"));
            object.put("monthId", Month);
            object.put("reportingYear", Year);
            object.put("hasAquiredTraining", hasTrained);
            object.put("skillTypeId", skillTypeId = hasTrained ? skillTypeId : 0);
            object.put("totalSales", Double.parseDouble(totalSales));
            object.put("totalExpenditure", Double.parseDouble(totalInputCost));
            object.put("totalProfit", Double.parseDouble("0.0"));
            object.put("remarks", remarks);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "non-farm-activity/save-line")
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
                                    Toast.makeText(NFUpdateActivity.this, getResources().getString(R.string.update_success), Toast.LENGTH_SHORT).show();

                                    sp.setStringData(Constant.YEAR, String.valueOf(Year));
                                    sp.setStringData(Constant.MONTH, String.valueOf(Month));
                                    binding.etUpdateRemarks.setText("");

                                    isUpdate = true;
                                    Intent intent = getIntent();
                                    intent.putExtra("res", isUpdate);
                                    intent.putExtra("updatedYear", Year);
                                    intent.putExtra("updatedMonth", Month);
                                    setResult(RESULT_OK, intent);
                                    finish();

                                } else {
                                    isUpdate = false;
                                    Toast.makeText(NFUpdateActivity.this, resObj.optString("message"), Toast.LENGTH_SHORT).show();
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        isUpdate = false;
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


    private void generateMonth(String selectedYear) {
        monthList.clear();
        monthList.add(new Month("JAN", selectedYear));
        monthList.add(new Month("FEB", selectedYear));
        monthList.add(new Month("MAR", selectedYear));
        monthList.add(new Month("APR", selectedYear));
        monthList.add(new Month("MAY", selectedYear));
        monthList.add(new Month("JUN", selectedYear));
        monthList.add(new Month("JUL", selectedYear));
        monthList.add(new Month("AUG", selectedYear));
        monthList.add(new Month("SEP", selectedYear));
        monthList.add(new Month("OCT", selectedYear));
        monthList.add(new Month("NOV", selectedYear));
        monthList.add(new Month("DEC", selectedYear));

    }


    int clickedPosition;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onProfileItemClick(int position, String year) {
        clickedPosition = position;
        Month = position + 1;
        binding.switchHasTrained.setChecked(false);
        binding.spnNfSkill.setSelection(0);
        getViewDetails();
    }


    private boolean canEdit;

    private void disableInputs() {

        binding.spnNfSkill.setEnabled(false);
        binding.switchHasTrained.setEnabled(false);
        binding.etNfTotSales.setEnabled(false);
        binding.etNfTotSales.setClickable(false);
        binding.etNfTotInputCost.setEnabled(false);
        binding.etNfTotInputCost.setClickable(false);
        binding.etNfTotProfit.setEnabled(false);
        binding.etNfTotProfit.setClickable(false);
        binding.etUpdateRemarks.setEnabled(false);
        binding.etUpdateRemarks.setClickable(false);
        binding.rlActivityUpdate.setEnabled(false);
        binding.rlActivityUpdate.setClickable(false);
    }

    private void enableInputs() {

        binding.spnNfSkill.setEnabled(true);
        binding.switchHasTrained.setEnabled(true);
        binding.etNfTotSales.setEnabled(true);
        binding.etNfTotSales.setClickable(true);
        binding.etNfTotInputCost.setEnabled(true);
        binding.etNfTotInputCost.setClickable(true);
        binding.etNfTotProfit.setEnabled(true);
        binding.etNfTotProfit.setClickable(true);
        binding.etUpdateRemarks.setEnabled(true);
        binding.etUpdateRemarks.setClickable(true);
        binding.rlActivityUpdate.setEnabled(true);
        binding.rlActivityUpdate.setClickable(true);
    }
}