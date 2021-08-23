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
import com.aashdit.olmoffline.databinding.ActivityDairyUpdateBinding;
import com.aashdit.olmoffline.db.DairyReportLine;
import com.aashdit.olmoffline.db.GoatryReportLine;
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

public class DairyUpdateActivity extends AppCompatActivity implements MonthListAdapter.MonthListener {

    private static final String TAG = "DairyUpdateActivity";
    private ActivityDairyUpdateBinding binding;

    private String remarks = "";
    private String token = "";

    private SharedPrefManager sp;
    private Long selectedSchemeId, selectedActivityId, selectedEntityId;

    int monthNo;
    int monthForBlockFutureUpdate;
    private boolean isFirst = true;
    private Long schemeId, entityId, activityId;
    private String entityCode = "";
    private final ArrayList<String> yearsList = new ArrayList<>();
    String selectedYear = "";
    private final ArrayList<Month> monthList = new ArrayList<>();

    private String noOfCowSold = "0";
    private String noOfCalfBorn = "";
    private final String noOfCalfDead = "0";
    private String noOfCowDead = "";
    private String noOfCalfSold = "0";
    private final String noOfCalfBought = "0";
    private String noOfCowBought = "0";
    private String totalIncome = "0";
    private String totalExpenditure = "0";
    private final String milkProduced = "0";
    private String qtyOfMilkSold = "0";
    private String qtyOfMilkProduced = "0";
    private String numCalfDead = "";
    private final String numCalfBorn = "0";
    private String numCalfBought = "";
    private final String numCalfSold = "0";
    private final String buffaloMilkProduced = "0";
    private String milkSoldIncome = "0";
    private String vaccinationCount = "0";
    private String dewormingCount = "0";

    private boolean isVaccinated = false;
    private boolean isDeworming = false;


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
        binding = ActivityDairyUpdateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        resetTitles();

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


        binding.rlVaccinationInput.setVisibility(View.GONE);
        binding.rlDewormingInput.setVisibility(View.GONE);

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
        getViewDetails();



        binding.switchVaccination.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isVaccinated = isChecked;
                if (isChecked){
                    binding.rlVaccinationInput.setVisibility(View.VISIBLE);
                }else{
                    binding.rlVaccinationInput.setVisibility(View.GONE);
                }
            }
        });

        binding.switchDeworming.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isDeworming = isChecked;
                if (isChecked){
                    binding.rlDewormingInput.setVisibility(View.VISIBLE);
                }else{
                    binding.rlDewormingInput.setVisibility(View.GONE);
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


                    noOfCowSold = binding.etUpdateCowBuffSoldNo.getText().toString().trim();
                    noOfCalfSold = binding.etUpdateCalfSoldNo.getText().toString().trim();
                    noOfCowBought = binding.etUpdateCowBuffBought.getText().toString().trim();
                    numCalfBought = binding.etUpdateBuffCalfBought.getText().toString().trim();
                    noOfCalfBorn = binding.etUpdateCalvesBorn.getText().toString().trim();
                    noOfCowDead = binding.etUpdateCowsDead.getText().toString().trim();
                    numCalfDead = binding.etUpdateCalfDead.getText().toString().trim();


                    vaccinationCount = binding.etUpdateCowVaccinated.getText().toString().trim();
                    dewormingCount = binding.etUpdateCowDeworming.getText().toString().trim();

                    qtyOfMilkProduced = binding.etUpdateMilkProduced.getText().toString().trim();
                    qtyOfMilkSold = binding.etUpdateMilkSold.getText().toString().trim();

                    milkSoldIncome = binding.etUpdateMilkSoldIncome.getText().toString().trim();

                    totalIncome = binding.etUpdateGoatIncome.getText().toString().trim();
                    totalExpenditure = binding.etUpdateGoatExpenditure.getText().toString().trim();


                    if(!isVaccinated){
                        vaccinationCount = "0";
                    }
                    if(!isDeworming){
                        dewormingCount = "0";
                    }


                    remarks = binding.etUpdateRemarks.getText().toString().trim();

                    if (selectedYear.equals("Select a Year")) {
                        Toast.makeText(DairyUpdateActivity.this, "Please Select a Year", Toast.LENGTH_SHORT).show();
                    } else if (Month == 0/*selectedMonth.equals(getResources().getString(R.string.select_month))*/) {
                        Toast.makeText(DairyUpdateActivity.this, "Please Select a Month", Toast.LENGTH_SHORT).show();
                    } else if(noOfCowSold.equals("")){
                        Toast.makeText(DairyUpdateActivity.this, "Please enter no. of cows / buffaloes sold", Toast.LENGTH_SHORT).show();
                    } else if(noOfCalfSold.equals("")){
                        Toast.makeText(DairyUpdateActivity.this, "Please enter no. of claves sold", Toast.LENGTH_SHORT).show();
                    } else if(noOfCowBought.equals("")){
                        Toast.makeText(DairyUpdateActivity.this, "Please enter no. of cows / buffaloes bought", Toast.LENGTH_SHORT).show();
                    } else if(numCalfBought.equals("")){
                        Toast.makeText(DairyUpdateActivity.this, "Please enter no. of calves bought", Toast.LENGTH_SHORT).show();
                    } else if(noOfCalfBorn.equals("")){
                        Toast.makeText(DairyUpdateActivity.this, "Please enter no. of calves born", Toast.LENGTH_SHORT).show();
                    } else if(noOfCowDead.equals("")){
                        Toast.makeText(DairyUpdateActivity.this, "Please enter no. of cows / buffaloes dead", Toast.LENGTH_SHORT).show();
                    } else if(numCalfDead.equals("")){
                        Toast.makeText(DairyUpdateActivity.this, "Please enter no. of calves dead", Toast.LENGTH_SHORT).show();
                    } else if(totalIncome.equals("")){
                        Toast.makeText(DairyUpdateActivity.this, "Please enter Income", Toast.LENGTH_SHORT).show();
                    } else if(totalExpenditure.equals("")){
                        Toast.makeText(DairyUpdateActivity.this, "Please enter Input cost", Toast.LENGTH_SHORT).show();
                    } else if(qtyOfMilkProduced.equals("")){
                        Toast.makeText(DairyUpdateActivity.this, "Please enter Qty. of Milk Produced.", Toast.LENGTH_SHORT).show();
                    } else if(qtyOfMilkSold.equals("")){
                        Toast.makeText(DairyUpdateActivity.this, "Please enter Qty. of Milk Sold", Toast.LENGTH_SHORT).show();
                    } else if(milkSoldIncome.equals("")){
                        Toast.makeText(DairyUpdateActivity.this, "Please enter income from Milk Sales", Toast.LENGTH_SHORT).show();
                    } else {
                        doMonthlyReportForDairy();
                    }
                } else if (Integer.parseInt(selectedYear) > currentYear) {
                    Toast.makeText(DairyUpdateActivity.this, "Can't Update Future Date", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(DairyUpdateActivity.this, "Can't Update Future Date", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private boolean isUpdate;
    private void doMonthlyReportForDairy() {


        DairyReportLine reportLine = new DairyReportLine();
        reportLine.unique = selectedSchemeId + "_" + selectedActivityId + "_" + Month + "_" + selectedYear+"_"+selectedEntityId;
        reportLine.year = Integer.parseInt(selectedYear);
        reportLine.monthId = Month;
        reportLine.schemeId = selectedSchemeId;
        reportLine.activityId = selectedActivityId;
        reportLine.entityId = selectedEntityId;
        reportLine.entityTypeCode = selectedEntityCode;
        reportLine.remarks = remarks;
        reportLine.numCowDead = noOfCowDead;
        reportLine.numCowSold = noOfCowSold;
        reportLine.numCowBought = noOfCowBought;
        reportLine.monthlyCowMilkProduced = qtyOfMilkProduced;
        reportLine.monthlyCowMilkSold = qtyOfMilkSold;
        reportLine.regularVaccinationDone = isVaccinated;
        reportLine.numCowVaccinated = vaccinationCount;
        reportLine.totalExpenditure = Double.parseDouble(totalExpenditure);
        reportLine.totalIncome = Double.parseDouble(totalIncome);
        reportLine.milkIncome = milkSoldIncome;
        reportLine.regularDewormingDone = isDeworming;
        reportLine.numDewormed = dewormingCount;
        reportLine.numCalfBorn = noOfCalfBorn;
        reportLine.numCalfDead = numCalfDead;
        reportLine.numCalfBought = numCalfBought;
        reportLine.numCalfSold = numCalfSold;
        reportLine.isSynced = false;

        taskUnique = selectedSchemeId + "_" + selectedActivityId+"_"+Month + "_" + selectedYear + "_" + selectedEntityId;

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm1) {
                realm1.insertOrUpdate(reportLine);
                Toast.makeText(DairyUpdateActivity.this, "Saved in Local", Toast.LENGTH_SHORT).show();

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

                RealmResults<DairyReportLine> shgCount = realm.where(DairyReportLine.class)
                        .equalTo("monthId", Month)
                        .equalTo("entityTypeCode", "SHG")
                        .findAll();
                RealmResults<DairyReportLine> pgCount = realm.where(DairyReportLine.class)
                        .equalTo("monthId", Month)
                        .equalTo("entityTypeCode", "PG")
                        .findAll();
                RealmResults<DairyReportLine> egCount = realm.where(DairyReportLine.class)
                        .equalTo("monthId", Month)
                        .equalTo("entityTypeCode", "EG")
                        .findAll();
                RealmResults<DairyReportLine> hhCount = realm.where(DairyReportLine.class)
                        .equalTo("monthId", Month)
                        .equalTo("entityTypeCode", "HH")
                        .findAll();
                RealmResults<DairyReportLine> clfCount = realm.where(DairyReportLine.class)
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

        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "dairy-activity/details")
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

    private void disableInputs() {

        binding.etUpdateCowBuffSoldNo.setEnabled(false);
        binding.etUpdateCowBuffSoldNo.setClickable(false);
        binding.etUpdateCowBuffBought.setEnabled(false);
        binding.etUpdateCowBuffBought.setClickable(false);
        binding.etUpdateCalvesBorn.setEnabled(false);
        binding.etUpdateCalvesBorn.setClickable(false);
        binding.etUpdateCowsDead.setEnabled(false);
        binding.etUpdateCowsDead.setClickable(false);
        binding.etUpdateCalfSoldNo.setEnabled(false);
        binding.etUpdateCalfSoldNo.setClickable(false);
        binding.etUpdateBuffCalfBought.setEnabled(false);
        binding.etUpdateBuffCalfBought.setClickable(false);
        binding.etUpdateMilkSoldIncome.setEnabled(false);
        binding.etUpdateMilkSoldIncome.setClickable(false);
        binding.etUpdateCowVaccinated.setEnabled(false);
        binding.etUpdateCowVaccinated.setClickable(false);
        binding.etUpdateCowDeworming.setEnabled(false);
        binding.etUpdateCowDeworming.setClickable(false);
        binding.etUpdateCalfDead.setEnabled(false);
        binding.etUpdateCalfDead.setClickable(false);
        binding.etUpdateMilkProduced.setEnabled(false);
        binding.etUpdateMilkProduced.setClickable(false);
        binding.etUpdateMilkSold.setEnabled(false);
        binding.etUpdateMilkSold.setClickable(false);
        binding.etUpdateGoatIncome.setEnabled(false);
        binding.etUpdateGoatIncome.setClickable(false);
        binding.etUpdateGoatExpenditure.setEnabled(false);
        binding.etUpdateGoatExpenditure.setClickable(false);
        binding.etUpdateRemarks.setEnabled(false);
        binding.etUpdateRemarks.setClickable(false);
        binding.rlActivityUpdate.setEnabled(false);
        binding.rlActivityUpdate.setClickable(false);
        binding.switchVaccination.setEnabled(false);
        binding.switchDeworming.setEnabled(false);

    }

    private void enableInputs() {
        binding.etUpdateCowBuffSoldNo.setEnabled(true);
        binding.etUpdateCowBuffSoldNo.setClickable(true);
        binding.etUpdateCowBuffBought.setEnabled(true);
        binding.etUpdateCowBuffBought.setClickable(true);
        binding.etUpdateCalvesBorn.setEnabled(true);
        binding.etUpdateCalvesBorn.setClickable(true);
        binding.etUpdateCowsDead.setEnabled(true);
        binding.etUpdateCowsDead.setClickable(true);
        binding.etUpdateCalfSoldNo.setEnabled(true);
        binding.etUpdateCalfSoldNo.setClickable(true);
        binding.etUpdateBuffCalfBought.setEnabled(true);
        binding.etUpdateBuffCalfBought.setClickable(true);
        binding.etUpdateMilkSoldIncome.setEnabled(true);
        binding.etUpdateMilkSoldIncome.setClickable(true);
        binding.etUpdateCowVaccinated.setEnabled(true);
        binding.etUpdateCowVaccinated.setClickable(true);
        binding.etUpdateCowDeworming.setEnabled(true);
        binding.etUpdateCowDeworming.setClickable(true);
        binding.etUpdateCalfDead.setEnabled(true);
        binding.etUpdateCalfDead.setClickable(true);
        binding.etUpdateMilkProduced.setEnabled(true);
        binding.etUpdateMilkProduced.setClickable(true);
        binding.etUpdateMilkSold.setEnabled(true);
        binding.etUpdateMilkSold.setClickable(true);
        binding.etUpdateGoatIncome.setEnabled(true);
        binding.etUpdateGoatIncome.setClickable(true);
        binding.etUpdateGoatExpenditure.setEnabled(true);
        binding.etUpdateGoatExpenditure.setClickable(true);
        binding.etUpdateRemarks.setEnabled(true);
        binding.etUpdateRemarks.setClickable(true);
        binding.rlActivityUpdate.setEnabled(true);
        binding.rlActivityUpdate.setClickable(true);
        binding.switchVaccination.setEnabled(true);
        binding.switchDeworming.setEnabled(true);
    }

    private final boolean isFeeding = false;
    private final boolean isNetting = false;

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
//        getViewDetails();



        taskItemUnique =  selectedSchemeId + "_" + selectedActivityId + "_" +Month +"_"+selectedYear
                + "_" + selectedEntityId;

        viewReportLine();

    }


    private void viewReportLine() {
        String reportUniqueKey = selectedSchemeId + "_" + selectedActivityId + "_" + Month + "_" + selectedYear+"_"+selectedEntityId;
        DairyReportLine item = realm.where(DairyReportLine.class)
                .equalTo("unique", reportUniqueKey)
                .findFirst();


        if (item != null) {
            binding.etUpdateCowBuffSoldNo.setText(item.numCowSold);
            binding.etUpdateCowBuffBought.setText(item.numCalfBought);
            binding.etUpdateCalvesBorn.setText(item.numCalfBorn+"");
            binding.etUpdateCowsDead.setText(item.numCowDead+"");
            binding.etUpdateCalfSoldNo.setText(item.numCalfSold+"");
            binding.etUpdateBuffCalfBought.setText(item.numCalfBought+"");
            binding.etUpdateCalfDead.setText(item.numCalfDead+"");
            binding.etUpdateMilkProduced.setText(item.monthlyCowMilkProduced);
            binding.etUpdateMilkSold.setText(item.monthlyCowMilkSold);
            binding.etUpdateGoatIncome.setText(item.totalIncome+"");
            binding.etUpdateGoatExpenditure.setText(item.totalExpenditure+"");
            binding.etUpdateMilkSoldIncome.setText(item.milkIncome+"");
            binding.switchVaccination.setChecked(item.regularVaccinationDone);
            binding.switchDeworming.setChecked(item.regularDewormingDone);



            if(item.regularDewormingDone){
                binding.rlDewormingInput.setVisibility(View.VISIBLE);
                binding.etUpdateCowDeworming.setText(String.valueOf(item.numDewormed));
            }
            if (item.regularVaccinationDone){
                binding.rlVaccinationInput.setVisibility(View.VISIBLE);
                binding.etUpdateCowVaccinated.setText(String.valueOf(item.numCowVaccinated));
            }

            binding.tvAvgMilkProd.setText(item.monthlyCowMilkProduced);
            binding.tvAvgDailyMilkSold.setText(item.monthlyCowMilkSold);
            binding.etUpdateRemarks.setText(item.remarks);
            binding.rlActivityUpdate.setEnabled(true);
            binding.rlActivityUpdate.setClickable(true);


            if (!item.isSynced){
                binding.tvWfStatus.setText(getResources().getString(R.string.status).concat(" ").concat("PENDING"));
            }

        } else {
            binding.tvWfComments.setText("");
            binding.etUpdateCowBuffSoldNo.setText("");
            binding.etUpdateCowBuffBought.setText("");
            binding.etUpdateCalvesBorn.setText("");
            binding.etUpdateCowsDead.setText("");
            binding.etUpdateCalfSoldNo.setText("");
            binding.etUpdateBuffCalfBought.setText("");
            binding.etUpdateCalfDead.setText("");
            binding.etUpdateMilkProduced.setText("");
            binding.etUpdateMilkSold.setText("");
            binding.etUpdateGoatIncome.setText("");
            binding.etUpdateGoatExpenditure.setText("");
            binding.etUpdateMilkSoldIncome.setText("");
            binding.switchVaccination.setChecked(false);
            binding.switchDeworming.setChecked(false);
            binding.etUpdateCowVaccinated.setText("");
            binding.etUpdateCowDeworming.setText("");

            binding.tvAvgMilkProd.setText("");
            binding.tvAvgDailyMilkSold.setText("");
            binding.rlActivityUpdate.setEnabled(false);
            binding.rlActivityUpdate.setClickable(false);


            binding.etUpdateRemarks.setText("");
            enableInputs();

            binding.tvWfStatus.setText(getResources().getString(R.string.status).concat(" ").concat(""));


            binding.switchVaccination.setChecked(false);
            binding.switchDeworming.setChecked(false);
        }
    }

    private boolean canEdit;

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

        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "dairy-activity/view-line")
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
//                                        binding.tvBirdActivityHistory.setEnabled(true);
//                                        binding.tvBirdActivityHistory.setClickable(true);
//                                        String currentWFStatus = obj.optString("currentWFStatus");
//                                        String comments = obj.optString("comments");
                                        entityId = obj.optLong("entityId");

                                        Long numCowDead = obj.optLong("numCowDead");
                                        Long numCowSold = obj.optLong("numCowSold");
                                        Long numCowBought = obj.optLong("numCowBought");
                                        Long numCalfDead = obj.optLong("numCalfDead");
                                        Long numCalfBorn = obj.optLong("numCalfBorn");
                                        Long numCalfBought = obj.optLong("numCalfBought");
                                        Long numCalfSold = obj.optLong("numCalfSold");
                                        Long numCowVaccinated = obj.optLong("numCowVaccinated");
                                        Long numDewormed = obj.optLong("numDewormed");
                                        String totalIncome = obj.optString("totalIncome");
                                        String totalExpenditure = obj.optString("totalExpenditure");
                                        String milkIncome = obj.optString("milkIncome");
                                        String remarks = obj.optString("remarks");
                                        String monthlyCowMilkSold = obj.optString("monthlyCowMilkSold");
                                        String avgDailyCowMilkSold = obj.optString("avgDailyCowMilkSold");
                                        String monthlyCowMilkProduced = obj.optString("monthlyCowMilkProduced");
                                        String avgDailyCowMilkProduced = obj.optString("avgDailyCowMilkProduced");
                                        boolean regularVaccinationDone = obj.optBoolean("regularVaccinationDone");
                                        boolean regularDewormingDone = obj.optBoolean("regularDewormingDone");


                                        binding.etUpdateCowBuffSoldNo.setText(numCowSold+"");
                                        binding.etUpdateCowBuffBought.setText(numCowBought+"");
                                        binding.etUpdateCalvesBorn.setText(numCalfBorn+"");
                                        binding.etUpdateCowsDead.setText(numCowDead+"");
                                        binding.etUpdateCalfSoldNo.setText(numCalfSold+"");
                                        binding.etUpdateBuffCalfBought.setText(numCalfBought+"");
                                        binding.etUpdateCalfDead.setText(numCalfDead+"");
                                        binding.etUpdateMilkProduced.setText(monthlyCowMilkProduced);
                                        binding.etUpdateMilkSold.setText(monthlyCowMilkSold);
                                        binding.etUpdateGoatIncome.setText(totalIncome.replace(",",""));
                                        binding.etUpdateGoatExpenditure.setText(totalExpenditure.replace(",",""));
                                        binding.etUpdateMilkSoldIncome.setText(milkIncome.replace(",",""));
                                        binding.switchVaccination.setChecked(regularVaccinationDone);
                                        binding.switchDeworming.setChecked(regularDewormingDone);
                                        binding.etUpdateCowVaccinated.setText(String.valueOf(numCowVaccinated));
                                        binding.etUpdateCowDeworming.setText(String.valueOf(numDewormed));

                                        binding.tvAvgMilkProd.setText(avgDailyCowMilkProduced);
                                        binding.tvAvgDailyMilkSold.setText(avgDailyCowMilkSold);
                                        binding.etUpdateRemarks.setText(remarks);
                                        binding.rlActivityUpdate.setEnabled(true);
                                        binding.rlActivityUpdate.setClickable(true);


                                    }
                                } else {

                                    binding.tvWfComments.setText("");
                                    binding.etUpdateCowBuffSoldNo.setText("");
                                    binding.etUpdateCowBuffBought.setText("");
                                    binding.etUpdateCalvesBorn.setText("");
                                    binding.etUpdateCowsDead.setText("");
                                    binding.etUpdateCalfSoldNo.setText("");
                                    binding.etUpdateBuffCalfBought.setText("");
                                    binding.etUpdateCalfDead.setText("");
                                    binding.etUpdateMilkProduced.setText("");
                                    binding.etUpdateMilkSold.setText("");
                                    binding.etUpdateGoatIncome.setText("");
                                    binding.etUpdateGoatExpenditure.setText("");
                                    binding.etUpdateMilkSoldIncome.setText("");
                                    binding.switchVaccination.setChecked(false);
                                    binding.switchDeworming.setChecked(false);
                                    binding.etUpdateCowVaccinated.setText("");
                                    binding.etUpdateCowDeworming.setText("");

                                    binding.tvAvgMilkProd.setText("");
                                    binding.tvAvgDailyMilkSold.setText("");
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
}