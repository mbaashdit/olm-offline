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
import com.aashdit.olmoffline.databinding.ActivityFarmerUpdateBinding;
import com.aashdit.olmoffline.db.FarmReportLine;
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

public class FarmerUpdateActivity extends AppCompatActivity implements MonthListAdapter.MonthListener {

    private static final String TAG = "FarmerUpdateActivity";
    private final ArrayList<String> yearsList = new ArrayList<>();
    private final ArrayList<Month> monthList = new ArrayList<>();
    int monthNo;
    int monthForBlockFutureUpdate;
    String selectedYear = "";
    String selectedMonth = "";
    int clickedPosition;
    private ActivityFarmerUpdateBinding binding;
    private String remarks = "";
    private String token = "";
    private SharedPrefManager sp;
    private boolean isFirst = true;
    private Long schemeId, entityId, activityId;
    private String entityCode = "";
    private String quantitySold, quantityProduced, seasonalIncome, totalExpenditure, totalIncome;
    private String salesUOM, prodUOM;
    /**
     * offline properties
     */

    private Realm realm;
    private int entryFrom;
    private String schemeName, activityName, activityCode, schemeCode, entityName, selectedEntityCode;
    private String taskItemUnique;
    private String taskUnique;
    private boolean isUpdate;
    private int Year = 0;
    private int Month = 0;
    private MonthListAdapter monthListAdapter;
    private boolean canEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFarmerUpdateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sp = SharedPrefManager.getInstance(this);
        token = sp.getStringData(Constant.APP_TOKEN);
        setSupportActionBar(binding.toolbarUpdateActivity);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        if (realm == null) {
            realm = Realm.getDefaultInstance();
        }
        entryFrom = getIntent().getIntExtra("ENTRY_FROM", 0);
        selectedMonth = getIntent().getStringExtra("MONTH");


        entryFrom = getIntent().getIntExtra("ENTRY_FROM", 0);
        selectedMonth = getIntent().getStringExtra("MONTH");
        selectedYear = getIntent().getStringExtra("YEAR");
        schemeName = getIntent().getStringExtra("SCHEME_NAME");
        activityName = getIntent().getStringExtra("ACTIVITY_NAME");
        activityCode = getIntent().getStringExtra("ACTIVITY_CODE");
        schemeCode = getIntent().getStringExtra("SCHEME_CODE");
        entityName = getIntent().getStringExtra("ENTITY_NAME");
        selectedEntityCode = getIntent().getStringExtra("ENTITY_CODE");
        entityId = getIntent().getLongExtra("ENTITY_ID", 0L);
        schemeId = getIntent().getLongExtra("SCHEME_ID", 0L);
        activityId = getIntent().getLongExtra("ACTIVITY_ID", 0L);

        try {
            Month = Integer.parseInt(selectedMonth);
        } catch (Exception e) {
            Month = 0;
            e.printStackTrace();
        }
        if (entryFrom != 0) {
            binding.rvUpdateMonthList.setVisibility(View.GONE);
            binding.spnYear.setVisibility(View.GONE);

            binding.tvGupMonth.setText(Utility.convertMonthToWord(selectedMonth) + " " + selectedYear);
            viewReportLine();
        } else {
            binding.rvUpdateMonthList.setVisibility(View.VISIBLE);
            binding.spnYear.setVisibility(View.VISIBLE);
            Month = monthNo;
            binding.tvGupMonth.setVisibility(View.GONE);

        }


//        if (entryFrom == 1) {

//        }

        binding.progress.setVisibility(View.GONE);
//        try {
//            monthNo = Integer.parseInt(sp.getStringData(Constant.MONTH));
//        }catch (Exception e){
//            monthNo = 1;
//            e.printStackTrace();
//        }
//        schemeId = sp.getLongData(Constant.SCHEME_ID);
//        activityId = sp.getLongData(Constant.ACTIVITY_ID);
//        entityId = sp.getLongData(Constant.ENTITY_ID);
//        entityCode = getIntent().getStringExtra("ENTITY_CODE");
//        selectedYear = sp.getStringData(Constant.YEAR);

        salesUOM = getIntent().getStringExtra("SUOM");
        prodUOM = getIntent().getStringExtra("PUOM");

//        binding.tvSalesUom.setText("(".concat(salesUOM).concat(") :"));
//        binding.tvProdUom.setText("(".concat(prodUOM).concat(") :"));

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
//                    getViewDetails();
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

//        binding.etUpdateProdQtySold.setFilters(new InputFilter[] {new InputFilter.LengthFilter(9)});
//        binding.etUpdateSalesQtySold.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(3)});
//        DecimalDigitsInputFilter.setEditTextMaxLength(binding.etUpdateProdQtySold,9);
//        DecimalDigitsInputFilter.setEditTextMaxLength(binding.etUpdateSalesQtySold,9);

//        binding.etFarmerTotalGrossIncome.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(2)});
//        binding.etFarmerTotalSeasonalIncome.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(2)});
//        binding.etFarmerTotalInputCost.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(2)});

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


                    quantitySold = binding.etUpdateSalesQtySold.getText().toString().trim();
                    quantityProduced = binding.etUpdateProdQtySold.getText().toString().trim();

                    seasonalIncome = binding.etFarmerTotalSeasonalIncome.getText().toString().trim();
                    totalExpenditure = binding.etFarmerTotalInputCost.getText().toString().trim();
                    totalIncome = binding.etFarmerTotalGrossIncome.getText().toString().trim();
                    remarks = binding.etUpdateRemarks.getText().toString().trim();

                    if (selectedYear.equals("Select a Year")) {
                        Toast.makeText(FarmerUpdateActivity.this, "Please Select a Year", Toast.LENGTH_SHORT).show();
                    } else if (Month == 0/*selectedMonth.equals(getResources().getString(R.string.select_month))*/) {
                        Toast.makeText(FarmerUpdateActivity.this, "Please Select a Month", Toast.LENGTH_SHORT).show();
                    } else if (quantitySold.equals("")) {
                        Toast.makeText(FarmerUpdateActivity.this, "Please enter sold quantity", Toast.LENGTH_SHORT).show();
                    } else if (quantityProduced.equals("")) {
                        Toast.makeText(FarmerUpdateActivity.this, "Please enter produced quantity", Toast.LENGTH_SHORT).show();
                    } else if (totalExpenditure.equals("")) {
                        Toast.makeText(FarmerUpdateActivity.this, "Please enter input cost", Toast.LENGTH_SHORT).show();
                    } else if (seasonalIncome.equals("")) {
                        Toast.makeText(FarmerUpdateActivity.this, "Please enter seasonal income", Toast.LENGTH_SHORT).show();
                    } else if (totalIncome.equals("")) {
                        Toast.makeText(FarmerUpdateActivity.this, "Please enter total income", Toast.LENGTH_SHORT).show();
                    } else {
                        doMonthlyReportForFarm();
                    }
                } else if (Integer.parseInt(selectedYear) > currentYear) {
                    Toast.makeText(FarmerUpdateActivity.this, "Can't Update Future Date", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(FarmerUpdateActivity.this, "Can't Update Future Date", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    private void viewReportLine() {
        String reportUniqueKey = schemeId + "_" + activityId + "_" + Month + "_" + selectedYear + "_" + entityId;
        FarmReportLine item = realm.where(FarmReportLine.class)
                .equalTo("unique", reportUniqueKey)
                .findFirst();


        if (item != null) {
            binding.etUpdateSalesQtySold.setText(String.valueOf(item.quantitySold));
            binding.etUpdateProdQtySold.setText(String.valueOf(item.quantityProduced));
            binding.etUpdateRemarks.setText(item.remarks);
            binding.etFarmerTotalInputCost.setText(item.totalExpenditure);
            binding.etFarmerTotalSeasonalIncome.setText(item.seasonalIncome);
            binding.etFarmerTotalGrossIncome.setText(item.totalIncome);

            binding.rlActivityUpdate.setEnabled(true);
            binding.rlActivityUpdate.setClickable(true);

            if (!item.isSynced) {
                binding.tvWfStatus.setText(getResources().getString(R.string.status).concat(" ").concat("PENDING"));
            }
        } else {
            binding.tvWfStatus.setText(getResources().getString(R.string.status).concat(" "));
            binding.etUpdateSalesQtySold.setText("");
            binding.etUpdateProdQtySold.setText("");
            binding.etUpdateRemarks.setText("");
            binding.etFarmerTotalInputCost.setText("");
            binding.etFarmerTotalSeasonalIncome.setText("");
            binding.etFarmerTotalGrossIncome.setText("");

//            binding.rlActivityUpdate.setEnabled(false);
//            binding.rlActivityUpdate.setClickable(false);
        }
    }

    private void doMonthlyReportForFarm() {

        FarmReportLine reportLine = new FarmReportLine();
        reportLine.unique = schemeId + "_" + activityId + "_" + Month + "_" + selectedYear + "_" + entityId;
        reportLine.year = Integer.parseInt(selectedYear);
        reportLine.monthId = Month;
        reportLine.schemeId = schemeId;
        reportLine.activityId = activityId;
        reportLine.entityId = entityId;
        reportLine.entityTypeCode = selectedEntityCode;
        reportLine.remarks = remarks;
        reportLine.quantitySold = quantitySold;
        reportLine.quantityProduced = quantityProduced;
        reportLine.searchTerm = "";
        reportLine.seasonalIncome = seasonalIncome;
        reportLine.totalExpenditure = totalExpenditure;
        reportLine.totalIncome = totalIncome;
        reportLine.isSynced = false;

        taskUnique = schemeId + "_" + activityId + "_" + Month + "_" + selectedYear + "_" + entityId;

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm1) {
                realm1.insertOrUpdate(reportLine);
                Toast.makeText(FarmerUpdateActivity.this, "Saved in Local", Toast.LENGTH_SHORT).show();

                binding.tvWfStatus.setText(getResources().getString(R.string.status).concat(" ").concat("PENDING"));

                if (taskItemUnique==null){
                    taskItemUnique = schemeId + "_" + activityId + "_" + Month + "_" + selectedYear
                            + "_" + entityId;
                }

                TaskListItem item = new TaskListItem();
                item.unique = taskItemUnique;
                item.month = Long.parseLong(String.valueOf(Month));
                item.year = Long.parseLong(selectedYear);
                item.schemeId = schemeId;
                item.activityId = activityId;
                item.entityId = entityId;
                item.levelCode = selectedEntityCode;
                item.wfStatus = "PENDING";
                item.monthName = Utility.convertMonthToWord(String.valueOf(Month));
                item.entityName = entityName;
                item.schemeName = schemeName;
                item.activityName = activityName;

                realm1.insertOrUpdate(item);

                RealmResults<FarmReportLine> shgCount = realm.where(FarmReportLine.class)
                        .equalTo("monthId", Month)
                        .equalTo("entityTypeCode", "SHG")
                        .findAll();
                RealmResults<FarmReportLine> pgCount = realm.where(FarmReportLine.class)
                        .equalTo("monthId", Month)
                        .equalTo("entityTypeCode", "PG")
                        .findAll();
                RealmResults<FarmReportLine> egCount = realm.where(FarmReportLine.class)
                        .equalTo("monthId", Month)
                        .equalTo("entityTypeCode", "EG")
                        .findAll();
                RealmResults<FarmReportLine> hhCount = realm.where(FarmReportLine.class)
                        .equalTo("monthId", Month)
                        .equalTo("entityTypeCode", "HH")
                        .findAll();
                RealmResults<FarmReportLine> clfCount = realm.where(FarmReportLine.class)
                        .equalTo("monthId", Month)
                        .equalTo("entityTypeCode", "CLF")
                        .findAll();


                Task task = new Task();//getMonthCode(selectedMonth);
//                if (_task ==null) {
                task.unique = taskUnique;

//                }
                task.month = Long.parseLong(String.valueOf(Month));
                task.year = Long.parseLong(selectedYear);
                task.schemeId = schemeId;
                task.activityId = activityId;
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

                finish();
            }
        });


//        binding.progress.setVisibility(View.VISIBLE);
//        JSONObject object = new JSONObject();
//        try {
//            object.put("schemeId", sp.getLongData("SCHEME_ID"));
//            object.put("activityId", sp.getLongData("ACTIVITY_ID"));
//            object.put("reportingYear", Year);
//            object.put("monthId", Month);
//            object.put("quantitySold", quantitySold);
//            object.put("quantityProduced", quantityProduced);
//            object.put("reportingLevelCode", sp.getStringData("ENTITY_TYPE_CODE"));
//            object.put("entityId", sp.getLongData("ENTITY_ID"));
//            object.put("remarks", remarks);
//            object.put("searchTerm", "");
//            object.put("seasonalIncome", Double.parseDouble(seasonalIncome));
//            object.put("totalExpenditure", Double.parseDouble(totalExpenditure));
//            object.put("totalIncome", Double.parseDouble(totalIncome));
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "farm-activity/save-line")
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
//                                    Toast.makeText(FarmerUpdateActivity.this, getResources().getString(R.string.update_success), Toast.LENGTH_SHORT).show();
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
//                                    Toast.makeText(FarmerUpdateActivity.this, resObj.optString("message"), Toast.LENGTH_SHORT).show();
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

        binding.etUpdateSalesQtySold.setEnabled(false);
        binding.etUpdateSalesQtySold.setClickable(false);
        binding.etUpdateProdQtySold.setEnabled(false);
        binding.etUpdateProdQtySold.setClickable(false);
        binding.etFarmerTotalInputCost.setEnabled(false);
        binding.etFarmerTotalInputCost.setClickable(false);
        binding.etFarmerTotalSeasonalIncome.setEnabled(false);
        binding.etFarmerTotalSeasonalIncome.setClickable(false);
        binding.etFarmerTotalGrossIncome.setEnabled(false);
        binding.etFarmerTotalGrossIncome.setClickable(false);
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
        binding.etFarmerTotalInputCost.setEnabled(true);
        binding.etFarmerTotalInputCost.setClickable(true);
        binding.etFarmerTotalSeasonalIncome.setEnabled(true);
        binding.etFarmerTotalSeasonalIncome.setClickable(true);
        binding.etFarmerTotalGrossIncome.setEnabled(true);
        binding.etFarmerTotalGrossIncome.setClickable(true);
        binding.etUpdateRemarks.setEnabled(true);
        binding.etUpdateRemarks.setClickable(true);
        binding.rlActivityUpdate.setEnabled(true);
        binding.rlActivityUpdate.setClickable(true);
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
//        getViewDetails();


        taskItemUnique = schemeId + "_" + activityId + "_" + Month + "_" + selectedYear
                + "_" + entityId;

        viewReportLine();

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

        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "farm-activity/view-line")
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
                                    dairyActivityDetails();
                                    if (obj != null) {
                                        entityId = obj.optLong("entityId");

                                        Double quantitySold = obj.optDouble("quantitySold");
                                        Double quantityProduced = obj.optDouble("quantityProduced");

                                        String totalIncome = obj.optString("totalIncome");
                                        String seasonalIncome = obj.optString("seasonalIncome");
                                        String totalExpenditure = obj.optString("totalExpenditure");
                                        String remarks = obj.optString("remarks");
                                        String currentWFStatus = obj.optString("currentWFStatus");
//
                                        binding.tvWfStatus.setText(currentWFStatus);

                                        binding.etUpdateSalesQtySold.setText(String.valueOf(quantitySold));
                                        binding.etUpdateProdQtySold.setText(String.valueOf(quantityProduced));
                                        binding.etUpdateRemarks.setText(remarks);
                                        binding.etFarmerTotalInputCost.setText(totalExpenditure.replace(",", ""));
                                        binding.etFarmerTotalSeasonalIncome.setText(seasonalIncome.replace(",", ""));
                                        binding.etFarmerTotalGrossIncome.setText(totalIncome.replace(",", ""));

                                        binding.etUpdateRemarks.setText(remarks);
                                        binding.rlActivityUpdate.setEnabled(true);
                                        binding.rlActivityUpdate.setClickable(true);

                                    }
                                } else {

                                    binding.tvWfComments.setText("");

                                    binding.etUpdateSalesQtySold.setText("");
                                    binding.etUpdateProdQtySold.setText("");
                                    binding.etUpdateRemarks.setText("");
                                    binding.etFarmerTotalInputCost.setText("");
                                    binding.etFarmerTotalSeasonalIncome.setText("");
                                    binding.etFarmerTotalGrossIncome.setText("");


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