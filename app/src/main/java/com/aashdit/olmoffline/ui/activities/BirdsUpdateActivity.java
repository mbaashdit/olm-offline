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
import com.aashdit.olmoffline.adapters.MonthSpnAdapter;
import com.aashdit.olmoffline.databinding.ActivityBirdsUpdateBinding;
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

public class BirdsUpdateActivity extends AppCompatActivity implements MonthListAdapter.MonthListener {

    private static final String TAG = "BirdsUpdateActivity";
    private final String noOfBuckSold = "";
    private final String incomeGeneratedBuck = "";
    private final String noOfBuckBuy = "";
    private final String expenditureCostBuck = "";
    private final String noOfBuckBorn = "";
    private final String noOfBuckDead = "";
    private final String noOfBuckVaccinated = "";
    private final ArrayList<String> yearsList = new ArrayList<>();
    private final ArrayList<Month> monthList = new ArrayList<>();
    String selectedYear = "";
    int monthNo;
    int monthForBlockFutureUpdate;
    int clickedPosition;
    private ActivityBirdsUpdateBinding binding;
    private String selectedMonth = "";
    private int entryFrom;
    private String noOfGoatSold = "";
    private String incomeGenerated = "";
    private String totalIncomeGenerated = "";
    private String incomeEggGenerated = "";
    private String noOfGoatBuy = "";
    private String expenditureCost = "";
    private String noOfEggSold = "";
    private String noOfGoatDead = "";
    private String noOfGoatVaccinated = "";
    private String remarks = "";
    private String token = "";
    private SharedPrefManager sp;
    private boolean regularVaccinated = false;
    private MonthListAdapter monthListAdapter;
    private Long birdsNo;
    private String schemeName, activityName, activityCode, schemeCode, entityName;
    private Long schemeId, entityId, activityId;
    private String entityCode = "";
    private boolean canEdit;
    private Realm realm;
    private boolean isFirst = true;
    private String taskUnique;
    private String taskItemUnique;
    private boolean isUpdate = false;
    private int Year = 0;
    private int Month = 0;

    private void disableInputs() {
        binding.etUpdateBirdSoldNo.setEnabled(false);
        binding.etUpdateBirdSoldNo.setClickable(false);
        binding.etUpdateBirdsIncome.setEnabled(false);
        binding.etUpdateBirdsIncome.setClickable(false);
        binding.etUpdateBirdBuy.setEnabled(false);
        binding.etUpdateBirdBuy.setClickable(false);
        binding.etUpdateGoatExpenditure.setEnabled(false);
        binding.etUpdateGoatExpenditure.setClickable(false);
        binding.etUpdateEggSold.setEnabled(false);
        binding.etUpdateEggSold.setClickable(false);
        binding.etUpdateTotalIncome.setEnabled(false);
        binding.etUpdateTotalIncome.setClickable(false);
        binding.etUpdateBirdDead.setEnabled(false);
        binding.etUpdateBirdDead.setClickable(false);
        binding.etUpdateGoatVaccinated.setEnabled(false);
        binding.etUpdateGoatVaccinated.setClickable(false);
        binding.etUpdateRemarks.setEnabled(false);
        binding.etUpdateRemarks.setClickable(false);
        binding.rlActivityUpdate.setEnabled(false);
        binding.rlActivityUpdate.setClickable(false);
//        binding.switchDeworming.setClickable(false);
        binding.etUpdateEggIncome.setEnabled(false);
        binding.etUpdateEggIncome.setClickable(false);

    }

    private void enableInputs() {
        binding.etUpdateEggIncome.setEnabled(true);
        binding.etUpdateEggIncome.setClickable(true);
        binding.etUpdateTotalIncome.setEnabled(true);
        binding.etUpdateTotalIncome.setClickable(true);
        binding.etUpdateBirdSoldNo.setEnabled(true);
        binding.etUpdateBirdSoldNo.setClickable(true);
        binding.etUpdateBirdsIncome.setEnabled(true);
        binding.etUpdateBirdsIncome.setClickable(true);
        binding.etUpdateBirdBuy.setEnabled(true);
        binding.etUpdateBirdBuy.setClickable(true);
        binding.etUpdateGoatExpenditure.setEnabled(true);
        binding.etUpdateGoatExpenditure.setClickable(true);
        binding.etUpdateEggSold.setEnabled(true);
        binding.etUpdateEggSold.setClickable(true);
        binding.etUpdateBirdDead.setEnabled(true);
        binding.etUpdateBirdDead.setClickable(true);
        binding.etUpdateGoatVaccinated.setEnabled(true);
        binding.etUpdateGoatVaccinated.setClickable(true);
        binding.etUpdateRemarks.setEnabled(true);
        binding.etUpdateRemarks.setClickable(true);
        binding.rlActivityUpdate.setEnabled(true);
        binding.rlActivityUpdate.setClickable(true);
//        binding.switchDeworming.setClickable(true);

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBirdsUpdateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        resetTitles();
        sp = SharedPrefManager.getInstance(this);
        token = sp.getStringData(Constant.APP_TOKEN);
        setSupportActionBar(binding.toolbarUpdateActivity);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        realm = Realm.getDefaultInstance();

        birdsNo = getIntent().getLongExtra("BIRD_NOS", 0L);
        try {
            monthNo = Integer.parseInt(sp.getStringData(Constant.MONTH));
        } catch (Exception e) {
            monthNo = 1;
            e.printStackTrace();
        }
        schemeId = getIntent().getLongExtra("SCHEME_ID", 0L);
        activityId = getIntent().getLongExtra("ACTIVITY_ID", 0L);
        entityId = getIntent().getLongExtra("ENTITY_ID", 0L);
        entryFrom = getIntent().getIntExtra("ENTRY_FROM", 0);
        entityCode = getIntent().getStringExtra("ENTITY_CODE");
        schemeCode = getIntent().getStringExtra("SCHEME_CODE");
        entityName = getIntent().getStringExtra("ENTITY_NAME");
        schemeName = getIntent().getStringExtra("SCHEME_NAME");
        activityName = getIntent().getStringExtra("ACTIVITY_NAME");
        activityCode = getIntent().getStringExtra("ACTIVITY_CODE");
        activityName = getIntent().getStringExtra("ACTIVITY_NAME");
        selectedYear = getIntent().getStringExtra("YEAR");
        selectedMonth = getIntent().getStringExtra("MONTH");
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


        binding.progress.setVisibility(View.GONE);
        binding.llVaccinationView.setVisibility(View.GONE);
        binding.switchDeworming.setChecked(false);
        binding.switchDeworming.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    regularVaccinated = true;
                    binding.llVaccinationView.setVisibility(View.VISIBLE);
                } else {
                    regularVaccinated = false;
                    binding.llVaccinationView.setVisibility(View.GONE);
                }
            }
        });

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

        MonthSpnAdapter dataMonthAdapter = new MonthSpnAdapter(this, monthList);
        binding.spnMonth.setAdapter(dataMonthAdapter);
//        binding.spnMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                if (position == 0) {
//
//                } else {
//                    selectedMonth = monthList.get(position).getMonthName();
//                    Month = position;
//                }
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });


//        Month = monthNo;
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
                    noOfGoatSold = binding.etUpdateBirdSoldNo.getText().toString().trim();
                    noOfGoatBuy = binding.etUpdateBirdBuy.getText().toString().trim();
                    noOfGoatDead = binding.etUpdateBirdDead.getText().toString().trim();
                    noOfEggSold = binding.etUpdateEggSold.getText().toString().trim();
                    totalIncomeGenerated = binding.etUpdateTotalIncome.getText().toString().trim();
                    incomeGenerated = binding.etUpdateBirdsIncome.getText().toString().trim();
                    incomeEggGenerated = binding.etUpdateEggIncome.getText().toString().trim();
                    expenditureCost = binding.etUpdateGoatExpenditure.getText().toString().trim();


                    noOfGoatVaccinated = binding.etUpdateGoatVaccinated.getText().toString().trim();
                    if (!regularVaccinated) {
                        noOfGoatVaccinated = String.valueOf(0);
                    }
                    remarks = binding.etUpdateRemarks.getText().toString().trim();


                    if (selectedYear.equals("Select a Year")) {
                        Toast.makeText(BirdsUpdateActivity.this, "Please Select a Year", Toast.LENGTH_SHORT).show();
                    } else if (Month == 0/*selectedMonth.equals(getResources().getString(R.string.select_month))*/) {
                        Toast.makeText(BirdsUpdateActivity.this, "Please Select a Month", Toast.LENGTH_SHORT).show();
                    } else if (noOfGoatSold.equals("")) {
                        Toast.makeText(BirdsUpdateActivity.this, "Please Enter No. of Bird Sold", Toast.LENGTH_SHORT).show();
                    } /*else if (Integer.parseInt(noOfGoatSold) > birdsNo) {
                        Toast.makeText(BirdsUpdateActivity.this, "Please Enter valid Bird Sold", Toast.LENGTH_SHORT).show();
                    }*/ else if (noOfEggSold.equals("")) {
                        Toast.makeText(BirdsUpdateActivity.this, "Please Enter No. of Egg Sold", Toast.LENGTH_SHORT).show();
                    } else if (incomeGenerated.equals("")) {
                        Toast.makeText(BirdsUpdateActivity.this, "Please Enter Bird Income", Toast.LENGTH_SHORT).show();
                    } else if (incomeEggGenerated.equals("")) {
                        Toast.makeText(BirdsUpdateActivity.this, "Please Enter Egg Income", Toast.LENGTH_SHORT).show();
                    } else if (noOfGoatBuy.equals("")) {
                        Toast.makeText(BirdsUpdateActivity.this, "Please Enter No. of Bird Bought", Toast.LENGTH_SHORT).show();
                    } else if (expenditureCost.equals("")) {
                        Toast.makeText(BirdsUpdateActivity.this, "Please Enter Bird Expenditure Cost", Toast.LENGTH_SHORT).show();
                    } else if (noOfGoatDead.equals("")) {
                        Toast.makeText(BirdsUpdateActivity.this, "Please Enter No. of Bird Dead", Toast.LENGTH_SHORT).show();
                    } else {
                        updateActivityDetails();
                    }
                } else if (Integer.parseInt(selectedYear) > currentYear) {
                    Toast.makeText(BirdsUpdateActivity.this, "Can't Update Future Date", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(BirdsUpdateActivity.this, "Can't Update Future Date", Toast.LENGTH_SHORT).show();
                }
            }
        });


//        getViewDetails();
    }

    private void updateActivityDetails() {


        PoultryReportLine poultryReportLine = new PoultryReportLine();
        poultryReportLine.schemeId = schemeId;
        poultryReportLine.activityId = activityId;
        poultryReportLine.year = Year;
        poultryReportLine.monthId = Month;
        poultryReportLine.entityId = entityId;
        poultryReportLine.entityTypeCode = entityCode;
        poultryReportLine.numBirdBought = noOfGoatBuy;
        poultryReportLine.numBirdSold = noOfGoatSold;
        poultryReportLine.numBirdDead = noOfGoatDead;
        poultryReportLine.numEggsSold = noOfEggSold;
        poultryReportLine.birdIncome = totalIncomeGenerated;
        poultryReportLine.birdExpenditure = expenditureCost;
        poultryReportLine.birdSalesIncome = incomeGenerated;
        poultryReportLine.eggSalesIncome = incomeEggGenerated;
        poultryReportLine.regularDeworming = regularVaccinated;
        poultryReportLine.dewormingFrequency = noOfGoatVaccinated;
        poultryReportLine.remarks = remarks;
        poultryReportLine.isSynced = false;
        poultryReportLine.unique = schemeId + "_" + activityId + "_" + Month + "_" + Year + "_" + entityId;


        taskUnique = schemeId + "_" + activityId + "_" + Month + "_" + Year + "_" + entityId;


        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm1) {


                realm1.insertOrUpdate(poultryReportLine);

                binding.tvWfStatus.setText(getResources().getString(R.string.status).concat(" ").concat("PENDING"));


                TaskListItem item = new TaskListItem();
                item.unique = taskItemUnique;
                item.month = Long.parseLong(String.valueOf(Month));
                item.year = Long.parseLong(selectedYear);
                item.schemeId = schemeId;
                item.activityId = activityId;
                item.entityId = entityId;
                item.levelCode = entityCode;
                item.wfStatus = "PENDING";
                item.monthName = Utility.convertMonthToWord(String.valueOf(Month));
                item.entityName = entityName;
                item.schemeName = schemeName;
                item.activityName = activityName;

                realm1.insertOrUpdate(item);

                RealmResults<PoultryReportLine> shgCount = realm.where(PoultryReportLine.class)
                        .equalTo("monthId", Month)
                        .equalTo("entityTypeCode", "SHG")
                        .findAll();
                RealmResults<PoultryReportLine> pgCount = realm.where(PoultryReportLine.class)
                        .equalTo("monthId", Month)
                        .equalTo("entityTypeCode", "PG")
                        .findAll();
                RealmResults<PoultryReportLine> egCount = realm.where(PoultryReportLine.class)
                        .equalTo("monthId", Month)
                        .equalTo("entityTypeCode", "EG")
                        .findAll();
                RealmResults<PoultryReportLine> hhCount = realm.where(PoultryReportLine.class)
                        .equalTo("monthId", Month)
                        .equalTo("entityTypeCode", "HH")
                        .findAll();
                RealmResults<PoultryReportLine> clfCount = realm.where(PoultryReportLine.class)
                        .equalTo("monthId", Month)
                        .equalTo("entityTypeCode", "CLF")
                        .findAll();


                Task _task = realm.where(Task.class)
                        .equalTo("unique", taskUnique)
                        .findFirst();


                Task task = new Task();//getMonthCode(selectedMonth);
//                if (_task ==null) {
                task.unique = taskUnique;

//                }
                task.month = Long.parseLong(String.valueOf(Month));
                task.year = Long.parseLong(Year + "");
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


            }
        });


//        binding.progress.setVisibility(View.VISIBLE);
//        JSONObject object = new JSONObject();
//
//        try {
//            object.put("schemeId", sp.getLongData("SCHEME_ID"));
//            object.put("activityId", sp.getLongData("ACTIVITY_ID"));
//            object.put("year", Year);
//            object.put("monthId", Month);
//            object.put("entityId", sp.getLongData("ENTITY_ID"));
//            object.put("entityTypeCode", sp.getStringData("ENTITY_TYPE_CODE"));
//            object.put("numBirdBought", noOfGoatBuy);
//            object.put("numBirdSold", noOfGoatSold);
//            object.put("numBirdDead", noOfGoatDead);
//            object.put("numEggsSold", noOfEggSold);
//            object.put("birdIncome", totalIncomeGenerated);
//            object.put("birdExpenditure", expenditureCost);
//            object.put("birdSalesIncome", incomeGenerated);
//            object.put("eggSalesIncome", incomeEggGenerated);
//            object.put("regularDeworming", regularVaccinated);
//            object.put("dewormingFrequency", noOfGoatVaccinated);
//            object.put("remarks", remarks);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }


//        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "poultry-activity/save-line")
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
//
//                            try {
//                                JSONObject resObj = new JSONObject(response);
//
//                                if (resObj.optBoolean("outcome")) {
//                                    Toast.makeText(BirdsUpdateActivity.this, getResources().getString(R.string.update_success), Toast.LENGTH_SHORT).show();
//
//
//                                    sp.setStringData(Constant.YEAR, String.valueOf(Year));
//                                    sp.setStringData(Constant.MONTH, String.valueOf(Month));
//                                    binding.tvSelectedMonth.setText(getResources().getString(R.string.select_month));
//
//                                    binding.etUpdateBirdSoldNo.setText("");
//                                    binding.etUpdateBirdsIncome.setText("");
//                                    binding.etUpdateBirdBuy.setText("");
//                                    binding.etUpdateGoatExpenditure.setText("");
//                                    binding.etUpdateEggIncome.setText("");
//                                    binding.etUpdateBirdDead.setText("");
//
//                                    binding.etUpdateGoatVaccinated.setText("");
//
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
//                                    Toast.makeText(BirdsUpdateActivity.this, resObj.optString("message"), Toast.LENGTH_SHORT).show();
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
    public void onProfileItemClick(int position, String year) {
        clickedPosition = position;
        Month = position + 1;


        taskItemUnique = schemeId + "_" + activityId + "_" + Month + "_" + Year
                + "_" + entityId;

        viewReportLine();
//        getViewDetails();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void getViewDetails() {
        binding.progress.setVisibility(View.VISIBLE);
        JSONObject object = new JSONObject();
        try {
            object.put("schemeId", sp.getLongData("SCHEME_ID"));
            object.put("activityId", sp.getLongData("ACTIVITY_ID"));
            object.put("year", selectedYear);
            object.put("month", Month);
            object.put("entityId", sp.getLongData("ENTITY_ID"));
            object.put("entityTypeCode", entityCode /*sp.getStringData("ENTITY_TYPE_CODE")*/);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "poultry-activity/view-line")
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
                                    binding.tvWfStatus.setVisibility(View.VISIBLE);
                                    /** tempMonth this variable is taken for to show previously selected month */
                                    int tempMonth = Month;
                                    monthListAdapter.restMonthSelected(tempMonth - 1);
                                    monthListAdapter.notifyDataSetChanged();
                                    birdsActivityDetails();
                                    JSONObject obj = resObj.optJSONObject("data");
                                    if (obj != null) {

                                        Long numGoatSold = obj.optLong("numBirdSold");
                                        Long numGoatBought = obj.optLong("numBirdBought");
                                        Long numGoatBorn = obj.optLong("numBirdBorn");
                                        Long numGoatDead = obj.optLong("numBirdDead");
                                        Long goatVaccinated = obj.optLong("birdVaccinated");
                                        Long numEggsSold = obj.optLong("numEggsSold");
                                        Long dewormingFrequency = obj.optLong("dewormingFrequency");
                                        String goatIncome = obj.optString("birdIncome");
                                        String goatExpenditure = obj.optString("birdExpenditure");
                                        String birdSalesIncome = obj.optString("birdSalesIncome");
                                        String eggSalesIncome = obj.optString("eggSalesIncome");


                                        String remarks = obj.optString("remarks");

                                        boolean regularVaccinated = obj.optBoolean("regularDeworming");

                                        binding.etUpdateBirdSoldNo.setText(numGoatSold + "");
                                        binding.etUpdateBirdsIncome.setText(birdSalesIncome.replace(",", ""));
                                        binding.etUpdateBirdBuy.setText(numGoatBought + "");
                                        binding.etUpdateGoatExpenditure.setText(goatExpenditure.replace(",", ""));
                                        binding.etUpdateEggSold.setText(numEggsSold + "");
                                        binding.etUpdateEggIncome.setText(eggSalesIncome.replace(",", ""));
                                        binding.etUpdateBirdDead.setText(numGoatDead + "");
                                        binding.etUpdateTotalIncome.setText(goatIncome.replace(",", ""));

                                        binding.switchDeworming.setChecked(regularVaccinated);
                                        binding.etUpdateGoatVaccinated.setText(dewormingFrequency + "");
                                        binding.etUpdateRemarks.setText(remarks);

                                    }

                                } else {
                                    binding.tvWfComments.setText("");
                                    binding.etUpdateBirdSoldNo.setText("");
                                    binding.etUpdateBirdsIncome.setText("");
                                    binding.etUpdateBirdBuy.setText("");
                                    binding.etUpdateGoatExpenditure.setText("");
                                    binding.etUpdateEggSold.setText("");
                                    binding.etUpdateEggIncome.setText("");
                                    binding.etUpdateBirdDead.setText("");

                                    binding.switchDeworming.setChecked(regularVaccinated);
                                    binding.etUpdateGoatVaccinated.setText("");
                                    binding.etUpdateRemarks.setText("");
                                    binding.etUpdateTotalIncome.setText("");
                                    binding.switchDeworming.setChecked(false);
                                    enableInputs();
                                    binding.tvWfStatus.setVisibility(View.GONE);
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            binding.tvWfComments.setText("");
                            binding.etUpdateBirdSoldNo.setText("");
                            binding.etUpdateBirdsIncome.setText("");
                            binding.etUpdateBirdBuy.setText("");
                            binding.etUpdateGoatExpenditure.setText("");
                            binding.etUpdateEggSold.setText("");
                            binding.etUpdateEggIncome.setText("");
                            binding.etUpdateBirdDead.setText("");

                            binding.switchDeworming.setChecked(regularVaccinated);
                            binding.etUpdateGoatVaccinated.setText("");
                            binding.etUpdateRemarks.setText("");
                            binding.etUpdateTotalIncome.setText("");
                            binding.switchDeworming.setChecked(false);

                            enableInputs();
                            binding.tvWfStatus.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.e(TAG, "onError: " + anError.getErrorDetail());
                        binding.progress.setVisibility(View.GONE);
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


    private void birdsActivityDetails() {
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

        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "poultry-activity/details")
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
//                                        binding.tvBirdActivityHistory.setEnabled(true);
//                                        binding.tvBirdActivityHistory.setClickable(true);
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


    private void viewReportLine() {
        String reportUniqueKey = schemeId + "_" + activityId + "_" + Month + "_" + selectedYear+"_"+entityId;
        PoultryReportLine item = realm.where(PoultryReportLine.class)
                .equalTo("unique", reportUniqueKey)
                .findFirst();


        if (item != null) {
            binding.etUpdateBirdSoldNo.setText(item.numBirdSold + "");
            binding.etUpdateBirdsIncome.setText(item.birdIncome);
            binding.etUpdateBirdBuy.setText(item.numBirdBought + "");
            binding.etUpdateGoatExpenditure.setText(item.birdExpenditure);
            binding.etUpdateEggSold.setText(item.numEggsSold + "");
            binding.etUpdateEggIncome.setText(item.eggSalesIncome);
            binding.etUpdateBirdDead.setText(item.numBirdDead + "");
            binding.etUpdateTotalIncome.setText(item.birdIncome);

            binding.switchDeworming.setChecked(regularVaccinated);
            binding.etUpdateGoatVaccinated.setText(item.dewormingFrequency + "");
            binding.etUpdateRemarks.setText(remarks);


            if (item.regularDeworming) {
                binding.switchDeworming.setChecked(true);
            } else {
                binding.switchDeworming.setChecked(false);
            }


            if (!item.isSynced) {
                binding.tvWfStatus.setText(getResources().getString(R.string.status).concat(" ").concat("PENDING"));
            }

        } else {


            binding.etUpdateBirdSoldNo.setText("");
            binding.etUpdateBirdsIncome.setText("");
            binding.etUpdateBirdBuy.setText("");
            binding.etUpdateGoatExpenditure.setText("");
            binding.etUpdateEggSold.setText("");
            binding.etUpdateEggIncome.setText("");
            binding.etUpdateBirdDead.setText("");
            binding.etUpdateTotalIncome.setText("");

            binding.switchDeworming.setChecked(false);
            binding.etUpdateGoatVaccinated.setText("");
            binding.etUpdateRemarks.setText("");

            binding.tvWfStatus.setText(getResources().getString(R.string.status).concat(" ").concat(""));


            binding.switchDeworming.setChecked(false);
        }
    }
}