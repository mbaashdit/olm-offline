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
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aashdit.olmoffline.BuildConfig;
import com.aashdit.olmoffline.R;
import com.aashdit.olmoffline.adapters.MonthListAdapter;
import com.aashdit.olmoffline.databinding.ActivityFishUpdateBinding;
import com.aashdit.olmoffline.db.DairyReportLine;
import com.aashdit.olmoffline.db.FisheryReportLine;
import com.aashdit.olmoffline.db.PoultryReportLine;
import com.aashdit.olmoffline.models.Month;
import com.aashdit.olmoffline.models.Task;
import com.aashdit.olmoffline.models.TaskListItem;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmResults;

import static android.content.pm.PackageManager.GET_META_DATA;

public class FishUpdateActivity extends AppCompatActivity implements MonthListAdapter.MonthListener {

    private static final String TAG = "FishUpdateActivity";


    private String remarks = "";
    private String token = "";

    private SharedPrefManager sp;

    private ActivityFishUpdateBinding binding;

    private String fingerlingHarvested = "0", tableSizeFishHarvested = "0", tableSizeFishSold = "0", fingerlingSold = "0",
            fryFeeding = "0", fingerlingFeeding = "0", yearlingFeeding = "0", frequency = "0", nettingFreqency = "0",
            fingerlingIncome = "",totalIncome = "",  tabFishIncome = "", fishExpenditure = "", fryStock = "0", fingerlingStock = "0", yearlingStock = "0";

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

    int monthNo;
    int monthForBlockFutureUpdate;
    private boolean isFirst = true;
//    private Long schemeId, entityId, activityId;
    private Long selectedSchemeId, selectedActivityId, selectedEntityId;
    private String entityCode = "";
    private final ArrayList<String> yearsList = new ArrayList<>();
    String selectedYear = "";
    private final ArrayList<Month> monthList = new ArrayList<>();


    private int entryFrom;
    private String selectedMonth,schemeName,activityName,activityCode,schemeCode,entityName,selectedEntityCode;
    private Realm realm;
    private String taskItemUnique;
    private String taskUnique;
    private int Year = 0;
    private int Month = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFishUpdateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        resetTitles();

        sp = SharedPrefManager.getInstance(this);
        token = sp.getStringData(Constant.APP_TOKEN);
        setSupportActionBar(binding.toolbarUpdateActivity);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

//        try {
//            monthNo = Integer.parseInt(sp.getStringData(Constant.MONTH));
//        } catch (Exception e) {
//            monthNo = 1;
//            e.printStackTrace();
//        }
//        schemeId = sp.getLongData(Constant.SCHEME_ID);
//        activityId = sp.getLongData(Constant.ACTIVITY_ID);
//        entityId = sp.getLongData(Constant.ENTITY_ID);
//        entityCode = getIntent().getStringExtra("ENTITY_CODE");
//        selectedYear = sp.getStringData(Constant.YEAR);

        if (realm == null) {
            realm = Realm.getDefaultInstance();
        }
        binding.progress.setVisibility(View.GONE);


        entryFrom = getIntent().getIntExtra("ENTRY_FROM", 0);
        selectedMonth = getIntent().getStringExtra("MONTH");
        selectedYear = getIntent().getStringExtra("YEAR");
        schemeName = getIntent().getStringExtra("SCHEME_NAME");
        activityName = getIntent().getStringExtra("ACTIVITY_NAME");
        activityCode = getIntent().getStringExtra("ACTIVITY_CODE");
        schemeCode = getIntent().getStringExtra("SCHEME_CODE");
        entityName = getIntent().getStringExtra("ENTITY_NAME");
        selectedEntityCode = getIntent().getStringExtra("ENTITY_CODE");
        selectedEntityId = getIntent().getLongExtra("ENTITY_ID", 0L);
        selectedSchemeId = getIntent().getLongExtra("SCHEME_ID", 0L);
        selectedActivityId = getIntent().getLongExtra("ACTIVITY_ID", 0L);

        try {
            Month = Integer.parseInt(selectedMonth);
        } catch (Exception e) {
            Month = 0;
            e.printStackTrace();
        }



        if (entryFrom != 0) {
            binding.rvUpdateMonthList.setVisibility(View.GONE);
            binding.spnYear.setVisibility(View.GONE);

            binding.tvGupMonth.setText(Utility.convertMonthToWord(selectedMonth)  + " " + selectedYear);
            viewReportLine();
        } else {
            binding.rvUpdateMonthList.setVisibility(View.VISIBLE);
            binding.spnYear.setVisibility(View.VISIBLE);
            Month = monthNo;
            binding.tvGupMonth.setVisibility(View.GONE);

        }



        binding.llConditionalContainer.setVisibility(View.GONE);
        if (entityCode.equals("CLF")) {
            binding.llConditionalContainer.setVisibility(View.VISIBLE);
        } else {
            binding.llConditionalContainer.setVisibility(View.GONE);
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

//        Month = monthNo;
        monthListAdapter = new MonthListAdapter(this, monthList, monthNo - 1);
        monthListAdapter.setMonthListener(this);
        binding.rvUpdateMonthList.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        binding.rvUpdateMonthList.setAdapter(monthListAdapter);
        binding.rvUpdateMonthList.getLayoutManager().scrollToPosition(monthNo - 1);

        binding.cvQtyFood.setVisibility(View.GONE);
        binding.cvNetFreq.setVisibility(View.GONE);
        binding.swtFeedingMgnt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isFeeding = isChecked;
                if (isChecked) {
                    binding.cvQtyFood.setVisibility(View.VISIBLE);
                } else {
                    binding.cvQtyFood.setVisibility(View.GONE);
                }
            }
        });
        binding.swtNettingMgnt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isNetting = isChecked;
                if (isChecked) {
                    binding.cvNetFreq.setVisibility(View.VISIBLE);
                } else {
                    binding.cvNetFreq.setVisibility(View.GONE);
                }
            }
        });
        getViewDetails();

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
                    fingerlingHarvested = binding.etUpdateFingerlingHarvested.getText().toString().trim();
                    tableSizeFishHarvested = binding.etUpdateTabFishHarvested.getText().toString().trim();
                    fingerlingSold = binding.etUpdateFingerlingSold.getText().toString().trim();
                    tableSizeFishSold = binding.etUpdateTabFishSold.getText().toString().trim();
//                    fryStock = binding.etUpdateFryStockNo.getText().toString().trim();
//                    fingerlingStock = binding.etUpdateFingerlingStocked.getText().toString().trim();
//                    yearlingStock = binding.etUpdateYearlingStocked.getText().toString().trim();

                    fryFeeding = binding.etFryFeedQty.getText().toString().trim();
                    fingerlingFeeding = binding.etFingFeedQty.getText().toString().trim();
                    yearlingFeeding = binding.etYearlingsFeedKg.getText().toString().trim();

                    frequency = binding.etUpdateFrequency.getText().toString().trim();
                    nettingFreqency = binding.etUpdateNettingFrequency.getText().toString().trim();
                    tabFishIncome = binding.etUpdateTabFishIncome.getText().toString().trim();
                    fingerlingIncome = binding.etUpdateFingIncome.getText().toString().trim();
                    totalIncome = binding.etUpdateTotalIncome.getText().toString().trim();
                    fishExpenditure = binding.etUpdateFishExpenditure.getText().toString().trim();

                    remarks = binding.etUpdateRemarks.getText().toString().trim();

                    if (selectedYear.equals("Select a Year")) {
                        Toast.makeText(FishUpdateActivity.this, "Please Select a Year", Toast.LENGTH_SHORT).show();
                    } else if (Month == 0/*selectedMonth.equals(getResources().getString(R.string.select_month))*/) {
                        Toast.makeText(FishUpdateActivity.this, "Please Select a Month", Toast.LENGTH_SHORT).show();
                    } else if (fingerlingHarvested.equals("")) {
                        Toast.makeText(FishUpdateActivity.this, "Please Enter Fingerling Harvested", Toast.LENGTH_SHORT).show();
                    } else if (tableSizeFishHarvested.equals("")) {
                        Toast.makeText(FishUpdateActivity.this, "Please Enter TableSize Fish Harvested", Toast.LENGTH_SHORT).show();
                    } else if (fingerlingSold.equals("")) {
                        Toast.makeText(FishUpdateActivity.this, "Please Enter Fingerling Sold", Toast.LENGTH_SHORT).show();
                    } else if (tableSizeFishSold.equals("")) {
                        Toast.makeText(FishUpdateActivity.this, "Please Enter TableSize Fish Sold", Toast.LENGTH_SHORT).show();
                    } else if (isFeeding && (fryFeeding.equals("") || fingerlingFeeding.equals("") || yearlingFeeding.equals(""))) {
                        Toast.makeText(FishUpdateActivity.this, "Please Enter Feeding Quantity", Toast.LENGTH_SHORT).show();
                    } else if (isNetting && nettingFreqency.equals("")) {
                        Toast.makeText(FishUpdateActivity.this, "Please Enter Netting Frequency", Toast.LENGTH_SHORT).show();
                    } else if (!frequency.equals("") && Integer.parseInt(frequency) > 12) {
                        Toast.makeText(FishUpdateActivity.this, "Please Enter Valid Pond Cleaning Frequency", Toast.LENGTH_SHORT).show();
                    } else if (fingerlingIncome.equals("")) {
                        Toast.makeText(FishUpdateActivity.this, "Please Enter Total Income Generated", Toast.LENGTH_SHORT).show();
                    } else if (fishExpenditure.equals("")) {
                        Toast.makeText(FishUpdateActivity.this, "Please Enter Total Expenditure Cost", Toast.LENGTH_SHORT).show();
                    } else {
                        doMonthlyReportForFishery();
                    }
                } else if (Integer.parseInt(selectedYear) > currentYear) {
                    Toast.makeText(FishUpdateActivity.this, "Can't Update Future Date", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(FishUpdateActivity.this, "Can't Update Future Date", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    private void fisheryActivityDetails() {
        binding.progress.setVisibility(View.VISIBLE);
        JSONObject object = new JSONObject();
        try {
            object.put("month", Month);
            object.put("year", selectedYear);
            object.put("schemeId", String.valueOf(selectedSchemeId));
            object.put("activityId", String.valueOf(selectedActivityId));
            object.put("entityId", String.valueOf(selectedEntityId));
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

    private void doMonthlyReportForFishery() {


        fryStock = fryStock.equals("") ? "0" : fryStock;
        fingerlingStock = fingerlingStock.equals("") ? "0" : fingerlingStock;
        yearlingStock = yearlingStock.equals("") ? "0" : yearlingStock;
        if (!isFeeding) {
            fryFeeding = "";
            fingerlingFeeding = "";
            yearlingFeeding = "";
        }
        fryFeeding = fryFeeding.equals("") ? "0" : fryFeeding;
        fingerlingFeeding = fingerlingFeeding.equals("") ? "0" : fingerlingFeeding;
        yearlingFeeding = yearlingFeeding.equals("") ? "0" : yearlingFeeding;
        frequency = frequency.equals("") ? "0" : frequency;
        if (!isNetting) {
            nettingFreqency = "";
        }
        nettingFreqency = nettingFreqency.equals("") ? "0" : nettingFreqency;







        FisheryReportLine reportLine = new FisheryReportLine();
        reportLine.unique = selectedSchemeId + "_" + selectedActivityId + "_" + Month + "_" + selectedYear+"_"+selectedEntityId;
        reportLine.year = Integer.parseInt(selectedYear);
        reportLine.monthId = Month;
        reportLine.schemeId = selectedSchemeId;
        reportLine.activityId = selectedActivityId;
        reportLine.entityId = selectedEntityId;
        reportLine.entityTypeCode = selectedEntityCode;
        reportLine.remarks = remarks;
        reportLine.numFingerlingsHarvested = fingerlingHarvested;
        reportLine.tableSizeFishHarvested = tableSizeFishHarvested;
        reportLine.fingerlingsSoldQty = fingerlingSold;
        reportLine.tableSizeFishSold = tableSizeFishSold;
        reportLine.totalExpenditure = Double.parseDouble(fishExpenditure);
        reportLine.totalIncome = Double.parseDouble(totalIncome);
        reportLine.fingerlingIncome = Double.parseDouble(fingerlingIncome);
        reportLine.tableFishIncome = Double.parseDouble(tabFishIncome);
        reportLine.isSynced = false;

        taskUnique = selectedSchemeId + "_" + selectedActivityId+"_"+Month + "_" + selectedYear + "_" + selectedEntityId;




        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm1) {
                realm1.insertOrUpdate(reportLine);
                Toast.makeText(FishUpdateActivity.this, "Saved in Local", Toast.LENGTH_SHORT).show();

                binding.tvWfStatus.setText(getResources().getString(R.string.status).concat(" ").concat("PENDING"));


                TaskListItem item = new TaskListItem();
                item.unique = taskItemUnique;
                item.month = Long.parseLong(String.valueOf(Month));
                item.year = Long.parseLong(selectedYear);
                item.schemeId = selectedSchemeId;
                item.activityId = selectedActivityId;
                item.entityId = selectedEntityId;
                item.levelCode = selectedEntityCode;
                item.wfStatus = "PENDING";
                item.monthName = Utility.convertMonthToWord(String.valueOf(Month));
                item.entityName = entityName;
                item.schemeName = schemeName;
                item.activityName = activityName;

                realm1.insertOrUpdate(item);

                RealmResults<FisheryReportLine> shgCount = realm.where(FisheryReportLine.class)
                        .equalTo("monthId", Month)
                        .equalTo("entityTypeCode", "SHG")
                        .findAll();
                RealmResults<FisheryReportLine> pgCount = realm.where(FisheryReportLine.class)
                        .equalTo("monthId", Month)
                        .equalTo("entityTypeCode", "PG")
                        .findAll();
                RealmResults<FisheryReportLine> egCount = realm.where(FisheryReportLine.class)
                        .equalTo("monthId", Month)
                        .equalTo("entityTypeCode", "EG")
                        .findAll();
                RealmResults<FisheryReportLine> hhCount = realm.where(FisheryReportLine.class)
                        .equalTo("monthId", Month)
                        .equalTo("entityTypeCode", "HH")
                        .findAll();
                RealmResults<FisheryReportLine> clfCount = realm.where(FisheryReportLine.class)
                        .equalTo("monthId", Month)
                        .equalTo("entityTypeCode", "CLF")
                        .findAll();




                Task task = new Task();//getMonthCode(selectedMonth);
//                if (_task ==null) {
                task.unique = taskUnique;

//                }
                task.month = Long.parseLong(String.valueOf(Month));
                task.year = Long.parseLong(selectedYear);
                task.schemeId = selectedSchemeId;
                task.activityId = selectedActivityId;
                task.taskStatus = "PENDING";
                task.schemeName = schemeName;
                task.activityName = activityName;
                task.clfCount = clfCount.size();
                task.shgCount = shgCount.size();
                task.pgCount = pgCount.size();
                task.egCount = egCount.size();
                task.householdCount = hhCount.size();
                task.monthCode = Utility.convertMonthToWord(String.valueOf(Month));//getMonthCode(selectedMonth);
                task.hashu = schemeCode.concat("#").concat(activityCode).concat("#").concat(Utility.convertMonthToWord(String.valueOf(Month)));
                realm1.insertOrUpdate(task);

            }
        });






















//        binding.progress.setVisibility(View.VISIBLE);
//        JSONObject object = new JSONObject();
//        fryStock = fryStock.equals("") ? "0" : fryStock;
//        fingerlingStock = fingerlingStock.equals("") ? "0" : fingerlingStock;
//        yearlingStock = yearlingStock.equals("") ? "0" : yearlingStock;
//        if (!isFeeding) {
//            fryFeeding = "";
//            fingerlingFeeding = "";
//            yearlingFeeding = "";
//        }
//        fryFeeding = fryFeeding.equals("") ? "0" : fryFeeding;
//        fingerlingFeeding = fingerlingFeeding.equals("") ? "0" : fingerlingFeeding;
//        yearlingFeeding = yearlingFeeding.equals("") ? "0" : yearlingFeeding;
//        frequency = frequency.equals("") ? "0" : frequency;
//        if (!isNetting) {
//            nettingFreqency = "";
//        }
//        nettingFreqency = nettingFreqency.equals("") ? "0" : nettingFreqency;

//        if (entityCode.equals("CLF")){
//            try {
//                object.put("schemeId", sp.getLongData("SCHEME_ID"));
//                object.put("activityId", sp.getLongData("ACTIVITY_ID"));
//                object.put("year", Year);
//                object.put("monthId", Month);
//                object.put("entityTypeCode", sp.getStringData("ENTITY_TYPE_CODE"));
//                object.put("entityId", sp.getLongData("ENTITY_ID"));
//                object.put("numFingerlingsHarvested", fingerlingHarvested);
//                object.put("tableSizeFishHarvested", tableSizeFishHarvested);
//                object.put("fingerlingsSoldQty", fingerlingSold);
//                object.put("tableSizeFishSold", tableSizeFishSold);
//                object.put("totalIncome", Double.parseDouble(totalIncome));
//                object.put("totalExpenditure", Double.parseDouble(fishExpenditure));
//                object.put("fingerlingIncome", Double.parseDouble(fingerlingIncome));
//                object.put("tableFishIncome", Double.parseDouble(tabFishIncome));
//                object.put("numFryStocked", fryStock);
//                object.put("numFingerlingsStocked", fingerlingStock);
//                object.put("numYearlingsStocked", yearlingStock);
//                object.put("fryFeedQty", fryFeeding);
//                object.put("fingerlingFeedQty", fingerlingFeeding);
//                object.put("yearlingFeedQty", yearlingFeeding);
//                object.put("pondCleaningFrequency", frequency);
//                object.put("regularNettingDone", isNetting);
//                object.put("nettingFrequency", nettingFreqency);
//                object.put("feedManagementDone", isFeeding);
//                object.put("remarks", remarks);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }else {


//            try {
//                object.put("schemeId", sp.getLongData("SCHEME_ID"));
//                object.put("activityId", sp.getLongData("ACTIVITY_ID"));
//                object.put("year", Year);
//                object.put("monthId", Month);
//                object.put("entityTypeCode", sp.getStringData("ENTITY_TYPE_CODE"));
//                object.put("entityId", sp.getLongData("ENTITY_ID"));
//                object.put("numFingerlingsHarvested", fingerlingHarvested);
//                object.put("tableSizeFishHarvested", tableSizeFishHarvested);
//                object.put("fingerlingsSoldQty", fingerlingSold);
//                object.put("tableSizeFishSold", tableSizeFishSold);
//
//                object.put("totalExpenditure", Double.parseDouble(fishExpenditure));
//                object.put("totalIncome", Double.parseDouble(totalIncome));
//                object.put("fingerlingIncome", Double.parseDouble(fingerlingIncome));
//                object.put("tableFishIncome", Double.parseDouble(tabFishIncome));
//
////                object.put("numFryStocked", fryStock);
////                object.put("numFingerlingsStocked", fingerlingStock);
////                object.put("numYearlingsStocked", yearlingStock);
////                object.put("fryFeedQty", fryFeeding);
////                object.put("fingerlingFeedQty", fingerlingFeeding);
////                object.put("yearlingFeedQty", yearlingFeeding);
////                object.put("pondCleaningFrequency", frequency);
////                object.put("regularNettingDone", isNetting);
////                object.put("nettingFrequency", nettingFreqency);
////                object.put("feedManagementDone", isFeeding);
//                object.put("remarks", remarks);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
//
//        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "fishery-activity/save-line")
//                .addHeaders("Authorization", "Bearer " + token)
//                .setTag("Activity")
//                .addJSONObjectBody(object)
//                .setPriority(Priority.HIGH)
//                .build()
//                .getAsString(new StringRequestListener() {
//                    @Override
//                    public void onResponse(String response) {
//                        binding.progress.setVisibility(View.GONE);
//                        if (Utility.isStringValid(response)) {
//                            try {
//                                JSONObject resObj = new JSONObject(response);
//                                if (resObj.optBoolean("outcome")) {
//                                    Toast.makeText(FishUpdateActivity.this, getResources().getString(R.string.update_success), Toast.LENGTH_SHORT).show();
//
//                                    sp.setStringData(Constant.YEAR, String.valueOf(Year));
//                                    sp.setStringData(Constant.MONTH, String.valueOf(Month));
//                                    binding.etUpdateRemarks.setText("");
//
//                                    isUpdate = true;
//                                    Intent intent = getIntent();
//                                    intent.putExtra("res", isUpdate);
//                                    intent.putExtra("updatedYear", Year);
//                                    intent.putExtra("updatedMonth", Month);
//                                    setResult(RESULT_OK, intent);
//                                    finish();
//
//                                } else {
//                                    isUpdate = false;
//                                    Toast.makeText(FishUpdateActivity.this, resObj.optString("message"), Toast.LENGTH_SHORT).show();
//                                }
//
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }
//
//                    @Override
//                    public void onError(ANError anError) {
//                        isUpdate = false;
//                        binding.progress.setVisibility(View.GONE);
//                        Log.e(TAG, "onError: " + anError.getErrorDetail());
//                        try {
//                            JSONObject errObj = new JSONObject(anError.getErrorBody());
//                            int statusCode = errObj.optInt("status");
//                            if (statusCode==500){
//                                sp.clear();
//                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//                                startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
//                                        Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
//                                finish();
//                            }
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                });
    }


    private void disableInputs() {

        binding.etUpdateFingerlingHarvested.setEnabled(false);
        binding.etUpdateFingerlingHarvested.setClickable(false);
        binding.etUpdateTabFishHarvested.setEnabled(false);
        binding.etUpdateTabFishHarvested.setClickable(false);
        binding.etUpdateFingerlingSold.setEnabled(false);
        binding.etUpdateFingerlingSold.setClickable(false);
        binding.etUpdateTabFishSold.setEnabled(false);
        binding.etUpdateTabFishSold.setClickable(false);
        binding.etUpdateTotalIncome.setEnabled(false);
        binding.etUpdateTotalIncome.setClickable(false);
        binding.etUpdateTabFishIncome.setEnabled(false);
        binding.etUpdateTabFishIncome.setClickable(false);
//        binding.etUpdateFryStockNo.setEnabled(false);
//        binding.etUpdateFryStockNo.setClickable(false);
//        binding.etUpdateFingerlingStocked.setEnabled(false);
//        binding.etUpdateFingerlingStocked.setClickable(false);
//        binding.etUpdateYearlingStocked.setEnabled(false);
//        binding.etUpdateYearlingStocked.setClickable(false);
        binding.etFryFeedQty.setEnabled(false);
        binding.etFryFeedQty.setClickable(false);
        binding.etFingFeedQty.setEnabled(false);
        binding.etFingFeedQty.setClickable(false);
        binding.etYearlingsFeedKg.setEnabled(false);
        binding.etYearlingsFeedKg.setClickable(false);
        binding.etUpdateFrequency.setEnabled(false);
        binding.etUpdateFrequency.setClickable(false);
        binding.etUpdateNettingFrequency.setEnabled(false);
        binding.etUpdateNettingFrequency.setClickable(false);
        binding.etUpdateFingIncome.setEnabled(false);
        binding.etUpdateFingIncome.setClickable(false);
        binding.etUpdateFishExpenditure.setEnabled(false);
        binding.etUpdateFishExpenditure.setClickable(false);
        binding.etUpdateRemarks.setEnabled(false);
        binding.etUpdateRemarks.setClickable(false);
        binding.rlActivityUpdate.setEnabled(false);
        binding.rlActivityUpdate.setClickable(false);

        binding.swtFeedingMgnt.setEnabled(false);

        binding.swtNettingMgnt.setEnabled(false);

    }

    private void enableInputs() {
        binding.etUpdateFingerlingHarvested.setEnabled(true);
        binding.etUpdateFingerlingHarvested.setClickable(true);
        binding.etUpdateTabFishHarvested.setEnabled(true);
        binding.etUpdateTabFishHarvested.setClickable(true);
        binding.etUpdateFingerlingSold.setEnabled(true);
        binding.etUpdateFingerlingSold.setClickable(true);
        binding.etUpdateTabFishSold.setEnabled(true);
        binding.etUpdateTabFishSold.setClickable(true);
        binding.etUpdateTotalIncome.setEnabled(true);
        binding.etUpdateTotalIncome.setClickable(true);
        binding.etUpdateTabFishIncome.setEnabled(true);
        binding.etUpdateTabFishIncome.setClickable(true);
//        binding.etUpdateFryStockNo.setEnabled(true);
//        binding.etUpdateFryStockNo.setClickable(true);
//        binding.etUpdateFingerlingStocked.setEnabled(true);
//        binding.etUpdateFingerlingStocked.setClickable(true);
//        binding.etUpdateYearlingStocked.setEnabled(true);
//        binding.etUpdateYearlingStocked.setClickable(true);
        binding.etFryFeedQty.setEnabled(true);
        binding.etFryFeedQty.setClickable(true);
        binding.etFingFeedQty.setEnabled(true);
        binding.etFingFeedQty.setClickable(true);
        binding.etYearlingsFeedKg.setEnabled(true);
        binding.etYearlingsFeedKg.setClickable(true);
        binding.etUpdateFrequency.setEnabled(true);
        binding.etUpdateFrequency.setClickable(true);
        binding.etUpdateNettingFrequency.setEnabled(true);
        binding.etUpdateNettingFrequency.setClickable(true);
        binding.etUpdateFingIncome.setEnabled(true);
        binding.etUpdateFingIncome.setClickable(true);
        binding.etUpdateFishExpenditure.setEnabled(true);
        binding.etUpdateFishExpenditure.setClickable(true);
        binding.etUpdateRemarks.setEnabled(true);
        binding.etUpdateRemarks.setClickable(true);
        binding.rlActivityUpdate.setEnabled(true);
        binding.rlActivityUpdate.setClickable(true);

        binding.swtFeedingMgnt.setEnabled(true);
        binding.swtFeedingMgnt.setClickable(true);

        binding.swtNettingMgnt.setEnabled(true);
        binding.swtNettingMgnt.setClickable(true);

    }

    private boolean isFeeding = false;
    private boolean isNetting = false;
//    private int Year = 0;
//    private int Month = 0;
    private MonthListAdapter monthListAdapter;

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
//        fisheryActivityDetails();
        getViewDetails();
    }

    private boolean canEdit;

    //fisheryActivityDetails
    private void getViewDetails() {

        binding.progress.setVisibility(View.VISIBLE);
        JSONObject object = new JSONObject();
        try {
            object.put("month", Month);
            object.put("year", selectedYear);
            object.put("schemeId", String.valueOf(selectedSchemeId));
            object.put("activityId", String.valueOf(selectedActivityId));
            object.put("entityId", String.valueOf(selectedEntityId));
            object.put("entityTypeCode", entityCode);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "fishery-activity/view-line")
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
                                    fisheryActivityDetails();
                                    if (obj != null) {
//                                        binding.tvBirdActivityHistory.setEnabled(true);
//                                        binding.tvBirdActivityHistory.setClickable(true);
//                                        String currentWFStatus = obj.optString("currentWFStatus");
//                                        String comments = obj.optString("comments");
                                        selectedEntityId = obj.optLong("entityId");

                                        Long numFingerlingsHarvested = obj.optLong("numFingerlingsHarvested");
                                        Long tableSizeFishHarvested = obj.optLong("tableSizeFishHarvested");
                                        Long fingerlingsSoldQty = obj.optLong("fingerlingsSoldQty");
                                        Long tableSizeFishSold = obj.optLong("tableSizeFishSold");
                                        Long numFryStocked = obj.optLong("numFryStocked");
                                        Long numFingerlingsStocked = obj.optLong("numFingerlingsStocked");
                                        Long numYearlingsStocked = obj.optLong("numYearlingsStocked");
                                        Long fryFeedQty = obj.optLong("fryFeedQty");
                                        Long fingerlingFeedQty = obj.optLong("fingerlingFeedQty");
                                        Long yearlingFeedQty = obj.optLong("yearlingFeedQty");
                                        Long pondCleaningFrequency = obj.optLong("pondCleaningFrequency");
                                        Long nettingFrequency = obj.optLong("nettingFrequency");
                                        String totalIncome = obj.optString("totalIncome");
                                        String totalExpenditure = obj.optString("totalExpenditure");
                                        String fingerlingIncome = obj.optString("fingerlingIncome");
                                        String tableFishIncome = obj.optString("tableFishIncome");
                                        String remarks = obj.optString("remarks");

                                        boolean regularNettingDone = obj.optBoolean("regularNettingDone");

                                        binding.swtNettingMgnt.setChecked(regularNettingDone);

                                        boolean feedManagementDone = obj.optBoolean("feedManagementDone");

                                        binding.swtFeedingMgnt.setChecked(feedManagementDone);
//                                        binding.tvWfComments.setText(comments);
                                        binding.etUpdateFingerlingHarvested.setText("" + numFingerlingsHarvested);
                                        binding.etUpdateTabFishHarvested.setText("" + tableSizeFishHarvested);
                                        binding.etUpdateFingerlingSold.setText("" + fingerlingsSoldQty);
                                        binding.etUpdateTabFishSold.setText("" + tableSizeFishSold);

                                        binding.etFryFeedQty.setText("" + fryFeedQty);
                                        binding.etFingFeedQty.setText("" + fingerlingFeedQty);
                                        binding.etYearlingsFeedKg.setText("" + yearlingFeedQty);

                                        binding.etUpdateFrequency.setText("" + pondCleaningFrequency);
                                        binding.etUpdateNettingFrequency.setText("" + nettingFrequency);
                                        binding.etUpdateTabFishIncome.setText(tableFishIncome.replace(",", ""));
                                        binding.etUpdateFingIncome.setText(fingerlingIncome.replace(",", ""));
                                        binding.etUpdateTotalIncome.setText(totalIncome.replace(",", ""));
                                        binding.etUpdateFishExpenditure.setText(totalExpenditure.replace(",", ""));

                                        binding.etUpdateRemarks.setText(remarks);
                                    }
                                } else {

                                    binding.tvWfComments.setText("");
                                    binding.etUpdateFingerlingHarvested.setText("");
                                    binding.etUpdateTabFishHarvested.setText("");
                                    binding.etUpdateFingerlingSold.setText("");
                                    binding.etUpdateTabFishSold.setText("");

                                    binding.etFryFeedQty.setText("");
                                    binding.etFingFeedQty.setText("");
                                    binding.etYearlingsFeedKg.setText("");

                                    binding.etUpdateFrequency.setText("");
                                    binding.etUpdateNettingFrequency.setText("");
                                    binding.etUpdateFingIncome.setText("");
                                    binding.etUpdateFishExpenditure.setText("");
                                    binding.etUpdateTabFishIncome.setText("");
                                    binding.etUpdateFingIncome.setText("");
                                    binding.etUpdateTotalIncome.setText("");
                                    binding.etUpdateRemarks.setText("");
                                    binding.swtNettingMgnt.setChecked(false);
                                    binding.swtFeedingMgnt.setChecked(false);

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

    private void viewReportLine() {
        String reportUniqueKey = selectedSchemeId + "_" + selectedActivityId + "_" + Month + "_" + selectedYear+"_"+selectedEntityId;
        FisheryReportLine item = realm.where(FisheryReportLine.class)
                .equalTo("unique", reportUniqueKey)
                .findFirst();


        if (item != null) {

            binding.etUpdateFingerlingHarvested.setText(item.numFingerlingsHarvested+"");
            binding.etUpdateTabFishHarvested.setText(item.tableSizeFishHarvested+"");
            binding.etUpdateFingerlingSold.setText(item.fingerlingsSoldQty+"");
            binding.etUpdateTabFishSold.setText(item.tableSizeFishSold+"");

            binding.etFryFeedQty.setText("");
            binding.etFingFeedQty.setText("");
            binding.etYearlingsFeedKg.setText("");

            binding.etUpdateFrequency.setText("");
            binding.etUpdateNettingFrequency.setText("");
            binding.etUpdateFingIncome.setText(item.fingerlingIncome+"");
            binding.etUpdateFishExpenditure.setText(item.totalExpenditure+"");
            binding.etUpdateTabFishIncome.setText(item.tableFishIncome+"");
            binding.etUpdateFingIncome.setText(item.fingerlingIncome+"");
            binding.etUpdateTotalIncome.setText(item.totalIncome+"");
            binding.etUpdateRemarks.setText(item.remarks);
            binding.swtNettingMgnt.setChecked(false);
            binding.swtFeedingMgnt.setChecked(false);

            if (!item.isSynced) {
                binding.tvWfStatus.setText(getResources().getString(R.string.status).concat(" ").concat("PENDING"));
            }

        } else {


            binding.etUpdateFingerlingHarvested.setText("");
            binding.etUpdateTabFishHarvested.setText("");
            binding.etUpdateFingerlingSold.setText("");
            binding.etUpdateTabFishSold.setText("");

            binding.etFryFeedQty.setText("");
            binding.etFingFeedQty.setText("");
            binding.etYearlingsFeedKg.setText("");

            binding.etUpdateFrequency.setText("");
            binding.etUpdateNettingFrequency.setText("");
            binding.etUpdateFingIncome.setText("");
            binding.etUpdateFishExpenditure.setText("");
            binding.etUpdateTabFishIncome.setText("");
            binding.etUpdateFingIncome.setText("");
            binding.etUpdateTotalIncome.setText("");
            binding.etUpdateRemarks.setText("");
            binding.swtNettingMgnt.setChecked(false);
            binding.swtFeedingMgnt.setChecked(false);
        }
    }
}