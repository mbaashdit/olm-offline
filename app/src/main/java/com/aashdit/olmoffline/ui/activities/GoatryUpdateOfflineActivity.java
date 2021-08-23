package com.aashdit.olmoffline.ui.activities;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
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

import com.aashdit.olmoffline.R;
import com.aashdit.olmoffline.adapters.MonthListAdapter;
import com.aashdit.olmoffline.adapters.MonthSpnAdapter;
import com.aashdit.olmoffline.databinding.ActivityGoatryUpdateOfflineBinding;
import com.aashdit.olmoffline.db.GoatryReportLine;
import com.aashdit.olmoffline.models.Month;
import com.aashdit.olmoffline.models.Task;
import com.aashdit.olmoffline.models.TaskListItem;
import com.aashdit.olmoffline.utils.LocaleManager;
import com.aashdit.olmoffline.utils.SharedPrefManager;
import com.aashdit.olmoffline.utils.Utility;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmResults;

import static android.content.pm.PackageManager.GET_META_DATA;

public class GoatryUpdateOfflineActivity extends AppCompatActivity implements MonthListAdapter.MonthListener {

    private static final String TAG = "GoatryUpdateOfflineActi";
    private final String incomeGeneratedBuck = "";
    private final String expenditureCostBuck = "";
    private final ArrayList<String> yearsList = new ArrayList<>();
    private final ArrayList<Month> monthList = new ArrayList<>();
    int monthNo = 0;
    int monthForBlockFutureUpdate;
    int clickedPosition;
    private ActivityGoatryUpdateOfflineBinding binding;
    private String selectedMonth, selectedYear, selectedEntityCode;
    private Long selectedSchemeId, selectedActivityId, selectedEntityId;
    private String noOfGoatSold = "";
    private String totalIncome = "";
    private String noOfGoatBuy = "";
    private String totalExpenditure = "";
    //    private String noOfGoatBorn = "";
    private String noOfGoatDead = "";
    private String noOfBuckSold = "";
    private String noOfBuckBuy = "";
    private String noOfKidsBorn = "";
    private String noOfBuckDead = "";
    private String numVaccinated = "";
    private String numDewormed = "";
    private String remarks = "";
    private String token = "";
    private SharedPrefManager sp;
    private boolean regularVaccinated = false;
    private boolean regularDeworming = false;
    private boolean isBuckTied = false;
    private Realm realm;
    private String taskItemUnique;
    private String taskUnique;
    private String schemeName, activityName, activityCode, schemeCode, entityName;
    private int entryFrom;
    private MonthListAdapter monthListAdapter;
    private int Year = 0;
    private int Month = 0;
    private boolean isFirst = true;
    private boolean isUpdate = false;

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
        binding = ActivityGoatryUpdateOfflineBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        resetTitles();

        setSupportActionBar(binding.toolbarUpdateGoatry);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (realm == null) {
            realm = Realm.getDefaultInstance();
        }
        entryFrom = getIntent().getIntExtra("ENTRY_FROM", 0);
//        if (entryFrom == 1) {
        selectedMonth = getIntent().getStringExtra("MONTH");
//        }
        try {
            Month = Integer.parseInt(selectedMonth);
        } catch (Exception e) {
            Month = 0;
            e.printStackTrace();
        }
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


        binding.tvGupMonth.setText(Utility.convertMonthToWord(selectedMonth) + " " + selectedYear);

        binding.etUpdateGoatVaccinated.setVisibility(View.GONE);
        binding.etUpdateBuckVaccinated.setVisibility(View.GONE);
        binding.tvMf1.setVisibility(View.GONE);
        binding.tvMf2.setVisibility(View.GONE);

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


        monthListAdapter = new MonthListAdapter(this, monthList, monthNo - 1);
        monthListAdapter.setMonthListener(this);
        binding.rvUpdateMonthList.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        binding.rvUpdateMonthList.setAdapter(monthListAdapter);
        binding.rvUpdateMonthList.getLayoutManager().scrollToPosition(monthNo - 1);


        if (entryFrom == 0) {
            binding.tvGupMonth.setVisibility(View.GONE);
            binding.rvUpdateMonthList.setVisibility(View.VISIBLE);
            Month = monthNo;
        } else {
            binding.tvGupMonth.setVisibility(View.VISIBLE);
            binding.rvUpdateMonthList.setVisibility(View.GONE);
        }


        binding.switchVaccination.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    binding.tvMf1.setVisibility(View.VISIBLE);
                    binding.etUpdateGoatVaccinated.setVisibility(View.VISIBLE);
                    regularVaccinated = true;
//                    mLinearVaccinationView.setVisibility(View.VISIBLE);
                } else {
                    binding.tvMf1.setVisibility(View.GONE);
                    binding.etUpdateGoatVaccinated.setVisibility(View.GONE);
                    regularVaccinated = false;
//                    mLinearVaccinationView.setVisibility(View.GONE);
                }
            }
        });


        binding.switchDeworming.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    binding.tvMf2.setVisibility(View.VISIBLE);
                    binding.etUpdateBuckVaccinated.setVisibility(View.VISIBLE);
                    regularDeworming = true;
                } else {
                    binding.tvMf2.setVisibility(View.GONE);
                    binding.etUpdateBuckVaccinated.setVisibility(View.GONE);
                    regularDeworming = false;
                }
            }
        });

        binding.switchBuckTied.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                isBuckTied = b;
            }
        });


        viewReportLine();

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

                    noOfGoatSold = binding.etUpdateGoatSoldNo.getText().toString().trim();
                    noOfGoatBuy = binding.etUpdateGoatBuy.getText().toString().trim();
                    noOfGoatDead = binding.etUpdateGoatDead.getText().toString().trim();

                    noOfBuckSold = binding.etUpdateBuckSold.getText().toString().trim();
                    totalIncome = binding.etActivityBuckIncome.getText().toString().trim();
                    noOfBuckBuy = binding.etUpdateBuckBuy.getText().toString().trim();
                    totalExpenditure = binding.etActivityBuckExpenditure.getText().toString().trim();
                    noOfKidsBorn = binding.etUpdateKidsBorn.getText().toString().trim();
                    noOfBuckDead = binding.etUpdateBuckDead.getText().toString().trim();

                    numDewormed = binding.etUpdateBuckVaccinated.getText().toString().trim();
                    numVaccinated = binding.etUpdateGoatVaccinated.getText().toString().trim();


                    if (!regularVaccinated) {
                        numVaccinated = String.valueOf(0);
                    }

                    if (!regularDeworming) {
                        numDewormed = String.valueOf(0);
                    }

                    remarks = binding.etUpdateRemarks.getText().toString().trim();

                    if (selectedYear.equals("Select a Year")) {
                        Toast.makeText(GoatryUpdateOfflineActivity.this, "Please Select a Year", Toast.LENGTH_SHORT).show();
                    } else if (Month == 0/*selectedMonth.equals(getResources().getString(R.string.select_month))*/) {
                        Toast.makeText(GoatryUpdateOfflineActivity.this, "Please Select a Month", Toast.LENGTH_SHORT).show();
                    } else if (noOfGoatSold.equals("")) {
                        Toast.makeText(GoatryUpdateOfflineActivity.this, "Please Enter No. of Goat Sold", Toast.LENGTH_SHORT).show();
                    } else if (totalIncome.equals("")) {
                        Toast.makeText(GoatryUpdateOfflineActivity.this, "Please Enter Total Income", Toast.LENGTH_SHORT).show();
                    } else if (noOfGoatBuy.equals("")) {
                        Toast.makeText(GoatryUpdateOfflineActivity.this, "Please Enter No. of Goat Bought", Toast.LENGTH_SHORT).show();
                    } else if (totalExpenditure.equals("")) {
                        Toast.makeText(GoatryUpdateOfflineActivity.this, "Please Enter Total Input Cost", Toast.LENGTH_SHORT).show();
                    } else if (noOfGoatDead.equals("")) {
                        Toast.makeText(GoatryUpdateOfflineActivity.this, "Please Enter No. of Goat Dead", Toast.LENGTH_SHORT).show();
                    } else if (noOfBuckSold.equals("")) {
                        Toast.makeText(GoatryUpdateOfflineActivity.this, "Please Enter No. of Kids Sold", Toast.LENGTH_SHORT).show();
                    } else if (noOfBuckBuy.equals("")) {
                        Toast.makeText(GoatryUpdateOfflineActivity.this, "Please Enter No. of Kids Bought", Toast.LENGTH_SHORT).show();
                    } else if (noOfKidsBorn.equals("")) {
                        Toast.makeText(GoatryUpdateOfflineActivity.this, "Please Enter No. of Kids Born", Toast.LENGTH_SHORT).show();
                    } else if (noOfBuckDead.equals("")) {
                        Toast.makeText(GoatryUpdateOfflineActivity.this, "Please Enter No. of Kids Dead", Toast.LENGTH_SHORT).show();
                    } else if (regularVaccinated && numVaccinated.isEmpty()) {
                        Toast.makeText(GoatryUpdateOfflineActivity.this, "Please Enter No. of Goat vaccinated", Toast.LENGTH_SHORT).show();
                    } else if (regularDeworming && numDewormed.isEmpty()) {
                        Toast.makeText(GoatryUpdateOfflineActivity.this, "Please Enter No. of Goat Dewormed", Toast.LENGTH_SHORT).show();
                    } else {
                        updateActivityDetails();
                    }
                } else if (Integer.parseInt(selectedYear) > currentYear) {
                    Toast.makeText(GoatryUpdateOfflineActivity.this, "Can't Update Future Date", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(GoatryUpdateOfflineActivity.this, "Can't Update Future Date", Toast.LENGTH_SHORT).show();
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
    }

    private void viewReportLine() {
        String reportUniqueKey = selectedSchemeId + "_" + selectedActivityId + "_" + Month + "_" + selectedYear + "_" + selectedEntityId;
        GoatryReportLine item = realm.where(GoatryReportLine.class)
                .equalTo("unique", reportUniqueKey)
                .findFirst();


        if (item != null) {
            binding.etUpdateGoatSoldNo.setText(item.numGoatSold);
            binding.etUpdateGoatBuy.setText(item.numGoatBought);
            binding.etUpdateGoatDead.setText(item.numGoatDead);

            binding.etUpdateBuckSold.setText(item.numBuckSold);
            binding.etActivityBuckIncome.setText(item.totalIncome);
            binding.etUpdateBuckBuy.setText(item.numBuckBought);
            binding.etActivityBuckExpenditure.setText(item.totalExpenditure);
            binding.etUpdateKidsBorn.setText(item.numBuckBorn);
            binding.etUpdateBuckDead.setText(item.numBuckDead);
            binding.etUpdateBuckVaccinated.setText(item.numDewormed);
            binding.etUpdateGoatVaccinated.setText(item.numVaccinated);
            binding.etUpdateRemarks.setText(item.remarks);

            if (item.regularVaccinated) {
                binding.switchVaccination.setChecked(true);
            } else {
                binding.switchVaccination.setChecked(false);
            }
            if (item.regularDeworming) {
                binding.switchDeworming.setChecked(true);
            } else {
                binding.switchDeworming.setChecked(false);
            }
            if (item.bucksTied) {
                binding.switchBuckTied.setChecked(true);
            } else {
                binding.switchBuckTied.setChecked(false);
            }

            if (!item.isSynced) {
                binding.tvWfStatus.setText(getResources().getString(R.string.status).concat(" ").concat("PENDING"));
            }

        } else {
            binding.etUpdateGoatSoldNo.setText("");
            binding.etUpdateGoatBuy.setText("");
            binding.etUpdateGoatDead.setText("");

            binding.etUpdateBuckSold.setText("");
            binding.etActivityBuckIncome.setText("");
            binding.etUpdateBuckBuy.setText("");
            binding.etActivityBuckExpenditure.setText("");
            binding.etUpdateKidsBorn.setText("");
            binding.etUpdateBuckDead.setText("");
            binding.etUpdateBuckVaccinated.setText("");
            binding.etUpdateGoatVaccinated.setText("");
            binding.etUpdateRemarks.setText("");

            binding.tvWfStatus.setText(getResources().getString(R.string.status).concat(" ").concat(""));


            binding.switchVaccination.setChecked(false);
            binding.switchDeworming.setChecked(false);
            binding.switchBuckTied.setChecked(false);
        }
    }

    private void updateActivityDetails() {
        GoatryReportLine goatryReportLine = new GoatryReportLine();
        goatryReportLine.unique = selectedSchemeId + "_" + selectedActivityId + "_" + Month + "_" + selectedYear + "_" + selectedEntityId;
        goatryReportLine.year = Integer.parseInt(selectedYear);
        goatryReportLine.monthId = Month;
        goatryReportLine.schemeId = selectedSchemeId;
        goatryReportLine.activityId = selectedActivityId;
        goatryReportLine.entityId = selectedEntityId;
        goatryReportLine.entityTypeCode = selectedEntityCode;
        goatryReportLine.numGoatSold = noOfGoatSold;
        goatryReportLine.totalIncome = totalIncome;
        goatryReportLine.numGoatBought = noOfGoatBuy;
        goatryReportLine.totalExpenditure = totalExpenditure;
        goatryReportLine.numGoatBorn = "0";
        goatryReportLine.numGoatDead = noOfGoatDead;
        goatryReportLine.numBuckSold = noOfBuckSold;
        goatryReportLine.numBuckBought = noOfBuckBuy;
        goatryReportLine.numBuckBorn = noOfKidsBorn;
        goatryReportLine.numBuckDead = noOfBuckDead;
        goatryReportLine.regularVaccinated = regularVaccinated;
        goatryReportLine.numVaccinated = numVaccinated;
        goatryReportLine.regularDeworming = regularDeworming;
        goatryReportLine.numDewormed = numDewormed;
        goatryReportLine.bucksTied = isBuckTied;
        goatryReportLine.isSynced = false;
        goatryReportLine.remarks = remarks;


//        Long goatsCount = Long.parseLong(noOfGoatBuy) - (Long.parseLong(noOfGoatDead) + Long.parseLong(noOfGoatSold));
//        Long kidsCount = Long.parseLong(noOfBuckBuy) + Long.parseLong(noOfKidsBorn) - (Long.parseLong(noOfBuckSold) + Long.parseLong(noOfBuckDead));
//
//
//        JSONObject updateProperties = new JSONObject();
//        try {
//            updateProperties.put("Goats", goatsCount);
//            updateProperties.put("Kids", kidsCount);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }

        //item.schemeId+"_"+item.activityId+item.month+"_"+item.year+"_"+item.entityId;

        taskUnique = selectedSchemeId + "_" + selectedActivityId + "_" + Month + "_" + selectedYear + "_" + selectedEntityId;


        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm1) {
                realm1.insertOrUpdate(goatryReportLine);

                Toast.makeText(GoatryUpdateOfflineActivity.this, "Saved in Local", Toast.LENGTH_SHORT).show();

                binding.tvWfStatus.setText(getResources().getString(R.string.status).concat(" ").concat("PENDING"));

//                TaskListItem item = realm.where(TaskListItem.class)
//                        .equalTo("unique", taskItemUnique)
//                        .findFirst();
//                if (item != null) {
////                    item.properties = updateProperties.toString();
//                    item.isUpdated = true;
//                    realm1.insertOrUpdate(item);
//                }


                if (taskItemUnique == null){

                    taskItemUnique = selectedSchemeId + "_" + selectedActivityId + "_" + Month + "_" + selectedYear
                            + "_" + selectedEntityId;
                }

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

                if (taskItemUnique != null) {
                    realm1.insertOrUpdate(item);
                }
                RealmResults<GoatryReportLine> shgCount = realm.where(GoatryReportLine.class)
                        .equalTo("monthId", Month)
                        .equalTo("entityTypeCode", "SHG")
                        .findAll();
                RealmResults<GoatryReportLine> pgCount = realm.where(GoatryReportLine.class)
                        .equalTo("monthId", Month)
                        .equalTo("entityTypeCode", "PG")
                        .findAll();
                RealmResults<GoatryReportLine> egCount = realm.where(GoatryReportLine.class)
                        .equalTo("monthId", Month)
                        .equalTo("entityTypeCode", "EG")
                        .findAll();
                RealmResults<GoatryReportLine> hhCount = realm.where(GoatryReportLine.class)
                        .equalTo("monthId", Month)
                        .equalTo("entityTypeCode", "HH")
                        .findAll();
                RealmResults<GoatryReportLine> clfCount = realm.where(GoatryReportLine.class)
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

                finish();
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


        taskItemUnique = selectedSchemeId + "_" + selectedActivityId + "_" + Month + "_" + selectedYear
                + "_" + selectedEntityId;

        viewReportLine();
    }
}