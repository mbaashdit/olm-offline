package com.aashdit.olmoffline.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aashdit.olmoffline.BuildConfig;
import com.aashdit.olmoffline.R;
import com.aashdit.olmoffline.adapters.MonthListAdapter;
import com.aashdit.olmoffline.adapters.SkillSpnAdapter;
import com.aashdit.olmoffline.databinding.ActivityNTFTUpdateBinding;
import com.aashdit.olmoffline.models.Month;
import com.aashdit.olmoffline.models.Skill;
import com.aashdit.olmoffline.models.ValueAdd;
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

public class NTFTUpdateActivity extends AppCompatActivity implements MonthListAdapter.MonthListener {

    private static final String TAG = "NTFTUpdateActivity";
    private final ArrayList<Month> monthList = new ArrayList<>();
    private final ArrayList<String> yearsList = new ArrayList<>();
    private final ArrayList<Skill> skills = new ArrayList<>();
    int monthNo;
    int monthForBlockFutureUpdate;
    String selectedYear = "";
    ArrayList<Long> selectedValueAdd = new ArrayList<>();
    int clickedPosition;
    String fundData = "";
    private ActivityNTFTUpdateBinding binding;
    private String remarks = "";
    private String token = "";
    private SharedPrefManager sp;
    private boolean isFirst = true;
    private boolean hasTrained = false;
    private Long schemeId, entityId, activityId, skillTypeId = 0L;
    private String entityCode = "";
    private ArrayList<ValueAdd> valueAdds = new ArrayList<>();
    private String totalGrossIncome, totalInputCost, seasonalIncome, qtySold, qtyProduced;
    private SkillSpnAdapter skillSpnAdapter;
    private int Year = 0;
    private int Month = 0;
    private MonthListAdapter monthListAdapter;
    private boolean canEdit;
    private boolean isUpdate;
    private String puom, suom;

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
        binding = ActivityNTFTUpdateBinding.inflate(getLayoutInflater());
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

        puom = getIntent().getStringExtra("PUOM");
        suom = getIntent().getStringExtra("SUOM");
        if (suom != null) {
            binding.tvSalesUom.setText("( ".concat(suom).concat(" )"));
        }
        if (puom != null) {
            binding.tvProdUom.setText("( ".concat(puom).concat(" )"));
        }
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

        skillSpnAdapter = new SkillSpnAdapter(this, skills);


        getValueAddedList();

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
                    } else {
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

        binding.spnValueAdditions.setSearchEnabled(true);
        binding.tvSpnVaData.setVisibility(View.GONE);
        binding.spnValueAdditions.setHintText("");

        binding.spnValueAdditions.setClearText("Close & Clear");
        binding.spnValueAdditions.setSearchHint("Select data");
        binding.spnValueAdditions.setEmptyTitle("Not Data Found!");
        binding.spnValueAdditions.setItems(valueAdds, items -> {
            binding.tvSpnVaData.setVisibility(View.GONE);
            binding.tvSelectLbl.setVisibility(View.GONE);
            selectedValueAdd.clear();
            if (items.size() == 0) {
                binding.tvSelectLbl.setVisibility(View.VISIBLE);
            } else {
                for (int i = 0; i < items.size(); i++) {
                    Log.i(TAG, i + " : " + items.get(i).valueEn + " : " + items.get(i).isSelected + " ID " + items.get(i).valueId);
                    if (!selectedValueAdd.contains(items.get(i).valueId))
                        selectedValueAdd.add(items.get(i).valueId);
                }
                Log.i(TAG, "onCreate: ::: selected value " + selectedValueAdd.toString().trim());
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

                    qtyProduced = binding.etUpdateProdQtySold.getText().toString().trim();
                    qtySold = binding.etUpdateSalesQtySold.getText().toString().trim();
                    totalGrossIncome = binding.etNtfpTotalGrossIncome.getText().toString().trim();
                    totalInputCost = binding.etNtfpTotalInputCost.getText().toString().trim();
                    seasonalIncome = binding.etNtfpTotalSeasonalIncome.getText().toString().trim();
                    remarks = binding.etUpdateRemarks.getText().toString().trim();

                    if (selectedYear.equals("Select a Year")) {
                        Toast.makeText(NTFTUpdateActivity.this, "Please Select a Year", Toast.LENGTH_SHORT).show();
                    } else if (Month == 0/*selectedMonth.equals(getResources().getString(R.string.select_month))*/) {
                        Toast.makeText(NTFTUpdateActivity.this, "Please Select a Month", Toast.LENGTH_SHORT).show();
                    } else if (qtySold.equals("")) {
                        Toast.makeText(NTFTUpdateActivity.this, "Please enter Qty Sold", Toast.LENGTH_SHORT).show();
                    } else if (qtyProduced.equals("")) {
                        Toast.makeText(NTFTUpdateActivity.this, "Please enter Qty Produced", Toast.LENGTH_SHORT).show();
                    } else if (selectedValueAdd.size() == 0) {
                        Toast.makeText(NTFTUpdateActivity.this, "Please enter Value Addition", Toast.LENGTH_SHORT).show();
                    } else if (totalInputCost.equals("")) {
                        Toast.makeText(NTFTUpdateActivity.this, "Please enter input cost", Toast.LENGTH_SHORT).show();
                    } else if (seasonalIncome.equals("")) {
                        Toast.makeText(NTFTUpdateActivity.this, "Please enter seasonal income", Toast.LENGTH_SHORT).show();
                    } else if (totalGrossIncome.equals("")) {
                        Toast.makeText(NTFTUpdateActivity.this, "Please enter Total gross income", Toast.LENGTH_SHORT).show();
                    } else {
                        doMonthlyReportForNonFarm();
                    }
                } else if (Integer.parseInt(selectedYear) > currentYear) {
                    Toast.makeText(NTFTUpdateActivity.this, "Can't Update Future Date", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(NTFTUpdateActivity.this, "Can't Update Future Date", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void getValueAddedList() {
        AndroidNetworking.get(BuildConfig.BASE_URL + Constant.API_PATH + "lov/ntfpValueAdd")
                .addHeaders("Authorization", "Bearer " + token)
                .setTag("ntfpValueAdd")
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        if (Utility.isStringValid(response)) {
                            try {
                                JSONObject object = new JSONObject(response);
                                if (object.optBoolean("outcome")) {
                                    JSONArray array = object.optJSONArray("data");
                                    if (array != null && array.length() > 0) {
                                        valueAdds.clear();
                                        for (int i = 0; i < array.length(); i++) {
                                            ValueAdd scheme = ValueAdd.parseValueAddData(array.optJSONObject(i));
                                            valueAdds.add(scheme);
                                            Log.i(TAG, "onResponse: :: valueAdds " + valueAdds.get(i).valueId.toString());
                                        }
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
//        getViewDetails();
        resetValueAddData();
    }

    private void disableInputs() {

        binding.etUpdateSalesQtySold.setEnabled(false);
        binding.etUpdateSalesQtySold.setClickable(false);
        binding.etUpdateProdQtySold.setEnabled(false);
        binding.etUpdateProdQtySold.setClickable(false);
        binding.etNtfpTotalGrossIncome.setEnabled(false);
        binding.etNtfpTotalGrossIncome.setClickable(false);
        binding.etNtfpTotalSeasonalIncome.setEnabled(false);
        binding.etNtfpTotalSeasonalIncome.setClickable(false);
        binding.etNtfpTotalInputCost.setEnabled(false);
        binding.etNtfpTotalInputCost.setClickable(false);
        binding.etUpdateRemarks.setEnabled(false);
        binding.etUpdateRemarks.setClickable(false);
        binding.rlActivityUpdate.setEnabled(false);
        binding.rlActivityUpdate.setClickable(false);
    }

    private void enableInputs() {

        binding.etUpdateSalesQtySold.setEnabled(true);
        binding.etUpdateSalesQtySold.setClickable(true);
        binding.etUpdateProdQtySold.setEnabled(true);
        binding.etUpdateProdQtySold.setClickable(true);
        binding.etNtfpTotalGrossIncome.setEnabled(true);
        binding.etNtfpTotalGrossIncome.setClickable(true);
        binding.etNtfpTotalSeasonalIncome.setEnabled(true);
        binding.etNtfpTotalSeasonalIncome.setClickable(true);
        binding.etNtfpTotalInputCost.setEnabled(true);
        binding.etNtfpTotalInputCost.setClickable(true);
        binding.etUpdateRemarks.setEnabled(true);
        binding.etUpdateRemarks.setClickable(true);
        binding.rlActivityUpdate.setEnabled(true);
        binding.rlActivityUpdate.setClickable(true);
    }

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

        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "ntfp/view-line")
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

                                        String remarks = obj.optString("remarks");
                                        String currentWFStatus = obj.optString("currentWFStatus");
                                        String qtySold = obj.optString("qtySold");
                                        String qtyProduced = obj.optString("qtyProduced");
                                        String valueAddition = obj.optString("valueAddition");
                                        String totalInputCost = obj.optString("totalInputCost");
                                        String seasonalIncome = obj.optString("seasonalIncome");
                                        String totalGrossIncome = obj.optString("totalGrossIncome");
                                        String totalProfit = obj.optString("totalProfit");


                                        valueAddition = valueAddition.replace("[", "");
                                        valueAddition = valueAddition.replace("]", "");

                                        if (valueAddition.contains(",")) {
                                            String[] strFundSources = valueAddition.split(",");
                                            fundData = "";
                                            for (String s : strFundSources) {
                                                Long fs = Long.parseLong(s.trim());
                                                for (int i = 0; i < valueAdds.size(); i++) {
                                                    if (fs.equals(valueAdds.get(i).valueId)) {
                                                        selectedValueAdd.add(fs);
                                                        valueAdds.get(i).isSelected = true;
                                                        fundData = fundData.concat(valueAdds.get(i).valueEn).concat(", ");
                                                    }
                                                }
                                                binding.spnValueAdditions.refreshAdapterForUpdate();
                                            }
                                        } else {
                                            Long fs = Long.parseLong(valueAddition.trim());
                                            fundData = "";
                                            for (int i = 0; i < valueAdds.size(); i++) {
                                                if (fs.equals(valueAdds.get(i).valueId)) {
                                                    selectedValueAdd.add(fs);
                                                    valueAdds.get(i).isSelected = true;
                                                    fundData = fundData.concat(valueAdds.get(i).valueEn).concat(" ");
                                                }
                                            }
                                        }

                                        binding.tvSelectLbl.setVisibility(View.GONE);
                                        binding.spnValueAdditions.setHintText("");
                                        binding.spnValueAdditions.setEnabled(false);
                                        binding.spnValueAdditions.refreshAdapterForUpdate();
                                        binding.tvSpnVaData.setVisibility(View.VISIBLE);
                                        if (fundData.endsWith(", ")) {
                                            binding.tvSpnVaData.setText(fundData.substring(0, fundData.length() - 2));
                                        } else {
                                            binding.tvSpnVaData.setText(fundData);
                                        }


                                        binding.tvWfStatus.setText(currentWFStatus);

                                        binding.etNtfpTotalGrossIncome.setText(totalGrossIncome.replace(",", ""));
                                        binding.etNtfpTotalSeasonalIncome.setText(seasonalIncome.replace(",", ""));
                                        binding.etNtfpTotalInputCost.setText(totalInputCost.replace(",", ""));
                                        binding.etUpdateRemarks.setText(remarks);
                                        binding.etUpdateSalesQtySold.setText(qtySold.replace(",", ""));
                                        binding.etUpdateProdQtySold.setText(qtyProduced.replace(",", ""));

                                        binding.etUpdateRemarks.setText(remarks);
                                        binding.rlActivityUpdate.setEnabled(true);
                                        binding.rlActivityUpdate.setClickable(true);


                                    }
                                } else {

                                    binding.tvWfComments.setText("");

                                    binding.etUpdateRemarks.setText("");
                                    binding.etNtfpTotalGrossIncome.setText("");
                                    binding.etNtfpTotalSeasonalIncome.setText("");
                                    binding.etNtfpTotalInputCost.setText("");
                                    binding.etUpdateSalesQtySold.setText("");
                                    binding.etUpdateProdQtySold.setText("");


                                    binding.rlActivityUpdate.setEnabled(false);
                                    binding.rlActivityUpdate.setClickable(false);
                                    binding.spnValueAdditions.setEnabled(true);

                                    binding.tvSelectLbl.setVisibility(View.VISIBLE);
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

        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "ntfp/details")
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
                                            binding.spnValueAdditions.setEnabled(true);
                                        } else {
                                            disableInputs();
                                            binding.spnValueAdditions.setEnabled(false);
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
            object.put("monthId", Month);
            object.put("entityId", sp.getLongData("ENTITY_ID"));
            object.put("reportingYear", Year);
            object.put("reportingLevelCode", sp.getStringData("ENTITY_TYPE_CODE"));
            object.put("qtySold", qtySold);
            object.put("qtyProduced", qtyProduced);
            object.put("valueAddition", selectedValueAdd.toString().replace(" ", ""));
            object.put("totalInputCost", Double.parseDouble(totalInputCost));
            object.put("seasonalIncome", Double.parseDouble(seasonalIncome));
            object.put("totalGrossIncome", Double.parseDouble(totalGrossIncome));
            object.put("remarks", remarks);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "ntfp/save-line")
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
                                    Toast.makeText(NTFTUpdateActivity.this, getResources().getString(R.string.update_success), Toast.LENGTH_SHORT).show();

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
                                    Toast.makeText(NTFTUpdateActivity.this, resObj.optString("message"), Toast.LENGTH_SHORT).show();
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

    private void resetValueAddData() {
        selectedValueAdd.clear();
        binding.tvSpnVaData.setText("");
        for (int i = 0; i < valueAdds.size(); i++) {
            valueAdds.get(i).isSelected = false;
        }
        ArrayList<ValueAdd> tempValueAdded = valueAdds;
        binding.spnValueAdditions.setItems(tempValueAdded, items -> {
            binding.tvSpnVaData.setVisibility(View.GONE);
            binding.tvSelectLbl.setVisibility(View.GONE);
            selectedValueAdd.clear();

            if (items.size() == 0) {
                binding.tvSelectLbl.setVisibility(View.VISIBLE);
            } else {

                for (int i = 0; i < items.size(); i++) {
                    Log.i(TAG, i + " : " + items.get(i).valueEn + " : " + items.get(i).isSelected + " ID " + items.get(i).valueId);
                    if (!selectedValueAdd.contains(items.get(i).valueId))
                        selectedValueAdd.add(items.get(i).valueId);
                }
                Log.i(TAG, "onCreate: ::: selected value " + selectedValueAdd.toString().trim());
            }
        });
        getViewDetails();
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

}