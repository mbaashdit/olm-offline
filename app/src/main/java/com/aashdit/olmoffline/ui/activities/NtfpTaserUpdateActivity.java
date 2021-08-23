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
import com.aashdit.olmoffline.databinding.ActivityNtftTaserUpdateBinding;
import com.aashdit.olmoffline.models.Month;
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

public class NtfpTaserUpdateActivity extends AppCompatActivity implements MonthListAdapter.MonthListener {

    private static final String TAG = "NtftTaserUpdateActivity";
    private final ArrayList<String> yearsList = new ArrayList<>();
    private final ArrayList<Month> monthList = new ArrayList<>();
    int monthNo;
    int monthForBlockFutureUpdate;
    String selectedYear = "";
    ArrayList<Long> selectedValueAdd = new ArrayList<>();
    int clickedPosition;
    String fundData = "";
    private ActivityNtftTaserUpdateBinding binding;
    private String remarks = "";
    private String token = "";
    private SharedPrefManager sp;
    private boolean isFirst = true;
    private Long schemeId, entityId, activityId;
    private String entityCode = "";
    private String quantitySoldBsr, quantitySoldCsr, quantityProducedBsr, quantityProducedCsr, seasonalIncomeBsr, seasonalIncomeCsr, totalInputCost, totalGrossIncome;
    private String salesUOM, prodUOM;
    //    private ValueAddAdapter valueAddAdapter;
    private boolean isUpdate;
    private int Year = 0;
    private int Month = 0;
    private MonthListAdapter monthListAdapter;
    private boolean canEdit;

    private ArrayList<ValueAdd> valueAdds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNtftTaserUpdateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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

        salesUOM = getIntent().getStringExtra("SUOM");
        prodUOM = getIntent().getStringExtra("PUOM");

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

        binding.tvSelectLbl.setVisibility(View.GONE);
        getValueAddedList();
//        getViewDetails();
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


                    quantitySoldBsr = binding.etUpdateSalesQtySoldBsr.getText().toString().trim();
                    quantitySoldCsr = binding.etUpdateSalesQtySoldCsr.getText().toString().trim();
                    quantityProducedBsr = binding.etUpdateProdQtySoldBsr.getText().toString().trim();
                    quantityProducedCsr = binding.etUpdateProdQtySoldCsr.getText().toString().trim();

                    totalInputCost = binding.etFarmerTotalInputCost.getText().toString().trim();
                    totalGrossIncome = binding.etFarmerTotalGrossIncome.getText().toString().trim();
                    seasonalIncomeBsr = binding.etSeasonalIncomeBsr.getText().toString().trim();
                    seasonalIncomeCsr = binding.etSeasonalIncomeCsr.getText().toString().trim();
                    remarks = binding.etUpdateRemarks.getText().toString().trim();

                    if (selectedYear.equals("Select a Year")) {
                        Toast.makeText(NtfpTaserUpdateActivity.this, "Please Select a Year", Toast.LENGTH_SHORT).show();
                    } else if (Month == 0/*selectedMonth.equals(getResources().getString(R.string.select_month))*/) {
                        Toast.makeText(NtfpTaserUpdateActivity.this, "Please Select a Month", Toast.LENGTH_SHORT).show();
                    } else if (quantitySoldBsr.equals("")) {
                        Toast.makeText(NtfpTaserUpdateActivity.this, "Please enter sold quantity BSR", Toast.LENGTH_SHORT).show();
                    } else if (quantitySoldCsr.equals("")) {
                        Toast.makeText(NtfpTaserUpdateActivity.this, "Please enter sold quantity CSR", Toast.LENGTH_SHORT).show();
                    } else if (quantityProducedBsr.equals("")) {
                        Toast.makeText(NtfpTaserUpdateActivity.this, "Please enter produced quantity BSR", Toast.LENGTH_SHORT).show();
                    } else if (quantityProducedCsr.equals("")) {
                        Toast.makeText(NtfpTaserUpdateActivity.this, "Please enter produced quantity CSR", Toast.LENGTH_SHORT).show();
                    } else if (selectedValueAdd.size() == 0) {
                        Toast.makeText(NtfpTaserUpdateActivity.this, "Please select Value Addition", Toast.LENGTH_SHORT).show();
                    } else if (totalInputCost.equals("")) {
                        Toast.makeText(NtfpTaserUpdateActivity.this, "Please enter input cost", Toast.LENGTH_SHORT).show();
                    } else if (seasonalIncomeBsr.equals("")) {
                        Toast.makeText(NtfpTaserUpdateActivity.this, "Please enter Seasonal Income (BSR)", Toast.LENGTH_SHORT).show();
                    } else if (seasonalIncomeCsr.equals("")) {
                        Toast.makeText(NtfpTaserUpdateActivity.this, "Please enter Seasonal Income (CSR)", Toast.LENGTH_SHORT).show();
                    } else if (totalGrossIncome.equals("")) {
                        Toast.makeText(NtfpTaserUpdateActivity.this, "Please enter Total Gross Income", Toast.LENGTH_SHORT).show();
                    } else {
                        doMonthlyReportForFarm();
                    }
                } else if (Integer.parseInt(selectedYear) > currentYear) {
                    Toast.makeText(NtfpTaserUpdateActivity.this, "Can't Update Future Date", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(NtfpTaserUpdateActivity.this, "Can't Update Future Date", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void doMonthlyReportForFarm() {
        binding.progress.setVisibility(View.VISIBLE);
        JSONObject object = new JSONObject();
        try {
            object.put("schemeId", sp.getLongData("SCHEME_ID"));
            object.put("activityId", sp.getLongData("ACTIVITY_ID"));
            object.put("monthId", Month);
            object.put("entityId", sp.getLongData("ENTITY_ID"));
            object.put("reportingYear", Year);
            object.put("reportingLevelCode", sp.getStringData("ENTITY_TYPE_CODE"));
            object.put("qtySoldBsr", quantitySoldBsr);
            object.put("qtySoldCsr", quantitySoldCsr);
            object.put("totalProductionBsr", quantityProducedBsr);
            object.put("totalProductionCsr", quantityProducedCsr);
            object.put("valueAddition", selectedValueAdd.toString().replace(" ", ""));
            object.put("totalInputCost", totalInputCost);
            object.put("seasonIncomeBsr", seasonalIncomeBsr);
            object.put("seasonIncomeCsr", seasonalIncomeCsr);
            object.put("totalGrossIncome", totalGrossIncome);
            object.put("remarks", remarks);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "ntfp-tasar/save-line")
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
                                    Toast.makeText(NtfpTaserUpdateActivity.this, getResources().getString(R.string.update_success), Toast.LENGTH_SHORT).show();

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
                                    Toast.makeText(NtfpTaserUpdateActivity.this, resObj.optString("message"), Toast.LENGTH_SHORT).show();
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
    public void onProfileItemClick(int position, String year) {
        clickedPosition = position;
        Month = position + 1;
//
//        for (int i = 0; i < valueAdds.size(); i++) {
//            valueAdds.get(i).isSelected = false;
//        }
//
//        getViewDetails();
        resetValueAddData();
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

    private void dairyActivityDetails() {
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

        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "ntfp-tasar/details")
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

    private void disableInputs() {

        binding.etUpdateSalesQtySoldBsr.setEnabled(false);
        binding.etUpdateSalesQtySoldBsr.setClickable(false);
        binding.etUpdateSalesQtySoldCsr.setEnabled(false);
        binding.etUpdateSalesQtySoldCsr.setClickable(false);
        binding.etUpdateProdQtySoldBsr.setEnabled(false);
        binding.etUpdateProdQtySoldBsr.setEnabled(false);
        binding.etUpdateProdQtySoldCsr.setClickable(false);
        binding.etUpdateProdQtySoldCsr.setEnabled(false);
        binding.etFarmerTotalInputCost.setEnabled(false);
        binding.etFarmerTotalInputCost.setClickable(false);
        binding.etFarmerTotalGrossIncome.setEnabled(false);
        binding.etFarmerTotalGrossIncome.setClickable(false);
        binding.etUpdateRemarks.setEnabled(false);
        binding.etUpdateRemarks.setClickable(false);
        binding.rlActivityUpdate.setEnabled(false);
        binding.rlActivityUpdate.setClickable(false);

        binding.etSeasonalIncomeBsr.setClickable(false);
        binding.etSeasonalIncomeBsr.setEnabled(false);
        binding.etSeasonalIncomeCsr.setClickable(false);
        binding.etSeasonalIncomeCsr.setEnabled(false);
    }

    private void enableInputs() {

        binding.etUpdateSalesQtySoldBsr.setEnabled(true);
        binding.etUpdateSalesQtySoldBsr.setClickable(true);
        binding.etUpdateSalesQtySoldCsr.setEnabled(true);
        binding.etUpdateSalesQtySoldCsr.setClickable(true);
        binding.etUpdateProdQtySoldBsr.setEnabled(true);
        binding.etUpdateProdQtySoldBsr.setClickable(true);
        binding.etUpdateProdQtySoldCsr.setEnabled(true);
        binding.etUpdateProdQtySoldCsr.setClickable(true);
        binding.etFarmerTotalInputCost.setEnabled(true);
        binding.etFarmerTotalInputCost.setClickable(true);
        binding.etFarmerTotalGrossIncome.setEnabled(true);
        binding.etFarmerTotalGrossIncome.setClickable(true);
        binding.etUpdateRemarks.setEnabled(true);
        binding.etUpdateRemarks.setClickable(true);
        binding.rlActivityUpdate.setEnabled(true);
        binding.rlActivityUpdate.setClickable(true);


        binding.etSeasonalIncomeBsr.setClickable(true);
        binding.etSeasonalIncomeBsr.setEnabled(true);
        binding.etSeasonalIncomeCsr.setClickable(true);
        binding.etSeasonalIncomeCsr.setEnabled(true);
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

        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "ntfp-tasar/view-line")
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
                                    binding.tvSelectLbl.setVisibility(View.GONE);
                                    /** tempMonth this variable is taken for to show previously selected month */
                                    int tempMonth = Month;
                                    monthListAdapter.restMonthSelected(tempMonth - 1);
                                    monthListAdapter.notifyDataSetChanged();
                                    dairyActivityDetails();
                                    if (obj != null) {
                                        entityId = obj.optLong("entityId");

                                        String qtySoldBsr = obj.optString("qtySoldBsr");
                                        String qtySoldCsr = obj.optString("qtySoldCsr");
                                        String totalProductionBsr = obj.optString("totalProductionBsr");
                                        String totalProductionCsr = obj.optString("totalProductionCsr");
                                        totalInputCost = obj.optString("totalInputCost");
                                        seasonalIncomeBsr = obj.optString("seasonIncomeBsr");
                                        seasonalIncomeCsr = obj.optString("seasonIncomeCsr");
                                        totalGrossIncome = obj.optString("totalGrossIncome");
                                        String totalProfit = obj.optString("totalProfit");
                                        String valueAddition = obj.optString("valueAddition");
                                        String remarks = obj.optString("remarks");
                                        String currentWFStatus = obj.optString("currentWFStatus");

                                        binding.tvWfStatus.setText(currentWFStatus);
                                        binding.etUpdateSalesQtySoldBsr.setText(qtySoldBsr);
                                        binding.etUpdateSalesQtySoldCsr.setText(qtySoldCsr);
                                        binding.etUpdateProdQtySoldBsr.setText(totalProductionBsr);
                                        binding.etUpdateProdQtySoldCsr.setText(totalProductionCsr);
                                        binding.etUpdateRemarks.setText(remarks);
                                        binding.etFarmerTotalInputCost.setText(totalInputCost.replace(",", ""));
                                        binding.etFarmerTotalGrossIncome.setText(totalGrossIncome.replace(",", ""));
                                        binding.etSeasonalIncomeBsr.setText(seasonalIncomeBsr.replace(",", ""));
                                        binding.etSeasonalIncomeCsr.setText(seasonalIncomeCsr.replace(",", ""));

                                        binding.etUpdateRemarks.setText(remarks);
                                        binding.rlActivityUpdate.setEnabled(true);
                                        binding.rlActivityUpdate.setClickable(true);

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

                                        binding.spnValueAdditions.setHintText("");
                                        binding.spnValueAdditions.refreshAdapterForUpdate();
                                        binding.tvSpnVaData.setVisibility(View.VISIBLE);
                                        if (fundData.endsWith(", ")) {
                                            binding.tvSpnVaData.setText(fundData.substring(0, fundData.length() - 2));
                                        } else {
                                            binding.tvSpnVaData.setText(fundData);
                                        }
                                    }
                                } else {
                                    binding.tvSelectLbl.setVisibility(View.VISIBLE);
//                                    binding.spnValueAdditions.setHintText("");
//                                    binding.spnValueAdditions.refreshAdapterForUpdate();
                                    binding.tvWfComments.setText("");

                                    binding.etUpdateSalesQtySoldBsr.setText("");
                                    binding.etUpdateSalesQtySoldCsr.setText("");
                                    binding.etUpdateProdQtySoldBsr.setText("");
                                    binding.etUpdateProdQtySoldCsr.setText("");
                                    binding.etUpdateRemarks.setText("");
                                    binding.etFarmerTotalInputCost.setText("");
                                    binding.etFarmerTotalGrossIncome.setText("");
                                    binding.etSeasonalIncomeBsr.setText("");
                                    binding.etSeasonalIncomeCsr.setText("");

                                    binding.tvSpnVaData.setVisibility(View.GONE);
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
}