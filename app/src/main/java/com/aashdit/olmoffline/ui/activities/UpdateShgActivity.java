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
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aashdit.olmoffline.BuildConfig;
import com.aashdit.olmoffline.R;
import com.aashdit.olmoffline.adapters.MonthListAdapter;
import com.aashdit.olmoffline.adapters.MonthSpnAdapter;
import com.aashdit.olmoffline.databinding.ActivityUpdateShgBinding;
import com.aashdit.olmoffline.models.Month;
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

import static android.content.pm.PackageManager.GET_META_DATA;

public class UpdateShgActivity extends AppCompatActivity implements MonthListAdapter.MonthListener {

    private static final String TAG = "UpdateShgActivity";
    private Toolbar mToolbarUpdateActivity;


//    private LinearLayout mLinearVaccinationView;

    private ActivityUpdateShgBinding binding;

    private final String selectedMonth = "";

    private String noOfGoatSold = "";
    private String totalIncome = "";
    private String noOfGoatBuy = "";
    private String totalExpenditure = "";
//    private String noOfGoatBorn = "";
    private String noOfGoatDead = "";

    private String noOfBuckSold = "";
    private final String incomeGeneratedBuck = "";
    private String noOfBuckBuy = "";
    private final String expenditureCostBuck = "";
    private String noOfKidsBorn = "";
    private String noOfBuckDead = "";
    private String numVaccinated = "";
    private String numDewormed = "";

//    private String noOfBuckVaccinated = "";
//    private String noOfGoatVaccinated = "";

    private String remarks = "";
    private String token = "";

    private SharedPrefManager sp;

    private boolean regularVaccinated = false;
    private boolean regularDeworming = false;
    private boolean isBuckTied = false;
    private final ArrayList<String> yearsList = new ArrayList<>();
    String selectedYear = "";
    private final ArrayList<Month> monthList = new ArrayList<>();

    private MonthListAdapter monthListAdapter;

    private boolean canEdit;
    private String entityCode = "";

    private Long maxGoatAvailable, maxBuckAvailable;

    private Long schemeId, entityId, activityId;

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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUpdateShgBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        resetTitles();
        sp = SharedPrefManager.getInstance(this);
        token = sp.getStringData(Constant.APP_TOKEN);
        mToolbarUpdateActivity = findViewById(R.id.toolbar_update_activity);
        setSupportActionBar(mToolbarUpdateActivity);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        entityCode = getIntent().getStringExtra("ENTITY_CODE");
        maxGoatAvailable = getIntent().getLongExtra("GOAT_NOS", 0L);
        maxBuckAvailable = getIntent().getLongExtra("BUCK_NOS", 0L);
        schemeId = sp.getLongData(Constant.SCHEME_ID);
        activityId = sp.getLongData(Constant.ACTIVITY_ID);
        entityId = sp.getLongData(Constant.ENTITY_ID);
        try{
        monthNo = Integer.parseInt(sp.getStringData(Constant.MONTH));
        }catch (Exception e){
            monthNo = 1;
            e.printStackTrace();
        }

//        canEdit = sp.getBoolData(Constant.CAN_EDIT);
        selectedYear = sp.getStringData(Constant.YEAR);
        binding.progress.setVisibility(View.GONE);
//        mLinearVaccinationView = findViewById(R.id.ll_vaccination_view);
//        mLinearVaccinationView.setVisibility(View.GONE);
        binding.tvMf1.setVisibility(View.GONE);
        binding.tvMf2.setVisibility(View.GONE);
        binding.etUpdateGoatVaccinated.setVisibility(View.GONE);
        binding.etUpdateBuckVaccinated.setVisibility(View.GONE);
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
                if (b){
                    binding.tvMf2.setVisibility(View.VISIBLE);
                    binding.etUpdateBuckVaccinated.setVisibility(View.VISIBLE);
                    regularDeworming = true;
                }else{
                    binding.tvMf2.setVisibility(View.GONE);
                    binding.etUpdateBuckVaccinated.setVisibility(View.GONE);
                    regularDeworming = false;
                }
            }
        });

//        binding.cvSelectMonth.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                openDatePicker();
//            }
//        });

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
                }else {
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


        Month = monthNo;
        monthListAdapter = new MonthListAdapter(this, monthList, monthNo-1);
        monthListAdapter.setMonthListener(this);
        binding.rvUpdateMonthList.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        binding.rvUpdateMonthList.setAdapter(monthListAdapter);
        binding.rvUpdateMonthList.getLayoutManager().scrollToPosition(monthNo-1);

        binding.switchBuckTied.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                isBuckTied = b;
            }
        });

        binding.rlActivityUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                selectedMonth = binding.tvSelectedMonth.getText().toString();

                int curYear = Integer.parseInt(yearNo);

                /**
                 * old logic
                 * Integer.parseInt(yearNo) > Year && monthForBlockFutureUpdate > clickedPosition
                 *                         || Integer.parseInt(yearNo) > Year && (monthForBlockFutureUpdate == clickedPosition + 1)
                 * */
                if (Year == curYear && clickedPosition+1 <= Integer.parseInt(monthForFutureUpdate) ||
                        (Year < curYear && clickedPosition+1 <= 12) ) {
                    noOfGoatSold = binding.etUpdateGoatSoldNo.getText().toString().trim();
//                    totalIncome = binding.etUpdateGoatIncome.getText().toString().trim();
                    noOfGoatBuy = binding.etUpdateGoatBuy.getText().toString().trim();
//                    totalExpenditure = binding.etUpdateGoatExpenditure.getText().toString().trim();
//                    noOfGoatBorn = binding.etUpdateGoatBorn.getText().toString().trim();
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

                    if (!regularDeworming){
                        numDewormed = String.valueOf(0);
                    }

                    remarks = binding.etUpdateRemarks.getText().toString().trim();

                    if (selectedYear.equals("Select a Year")) {
                        Toast.makeText(UpdateShgActivity.this, "Please Select a Year", Toast.LENGTH_SHORT).show();
                    } else if (Month == 0/*selectedMonth.equals(getResources().getString(R.string.select_month))*/) {
                        Toast.makeText(UpdateShgActivity.this, "Please Select a Month", Toast.LENGTH_SHORT).show();
                    } else if (noOfGoatSold.equals("")) {
                        Toast.makeText(UpdateShgActivity.this, "Please Enter No. of Goat Sold", Toast.LENGTH_SHORT).show();
                    } else if (totalIncome.equals("")) {
                        Toast.makeText(UpdateShgActivity.this, "Please Enter Total Income", Toast.LENGTH_SHORT).show();
                    } else if (noOfGoatBuy.equals("")) {
                        Toast.makeText(UpdateShgActivity.this, "Please Enter No. of Goat Bought", Toast.LENGTH_SHORT).show();
                    } else if (totalExpenditure.equals("")) {
                        Toast.makeText(UpdateShgActivity.this, "Please Enter Total Input Cost", Toast.LENGTH_SHORT).show();
                    } else if (noOfGoatDead.equals("")) {
                        Toast.makeText(UpdateShgActivity.this, "Please Enter No. of Goat Dead", Toast.LENGTH_SHORT).show();
                    } else if (noOfBuckSold.equals("")) {
                        Toast.makeText(UpdateShgActivity.this, "Please Enter No. of Kids Sold", Toast.LENGTH_SHORT).show();
                    } else if (noOfBuckBuy.equals("")) {
                        Toast.makeText(UpdateShgActivity.this, "Please Enter No. of Kids Bought", Toast.LENGTH_SHORT).show();
                    } else if (noOfKidsBorn.equals("")) {
                        Toast.makeText(UpdateShgActivity.this, "Please Enter No. of Kids Born", Toast.LENGTH_SHORT).show();
                    } else if (noOfBuckDead.equals("")) {
                        Toast.makeText(UpdateShgActivity.this, "Please Enter No. of Kids Dead", Toast.LENGTH_SHORT).show();
                    }  else if (regularVaccinated && numVaccinated.isEmpty()) {
                        Toast.makeText(UpdateShgActivity.this, "Please Enter No. of Goat vaccinated", Toast.LENGTH_SHORT).show();
                    }else if (regularDeworming && numDewormed.isEmpty()) {
                        Toast.makeText(UpdateShgActivity.this, "Please Enter No. of Goat Dewormed", Toast.LENGTH_SHORT).show();
                    } else {
                        updateActivityDetails();
                    }
                } else if (Integer.parseInt(selectedYear) > currentYear) {
                    Toast.makeText(UpdateShgActivity.this, "Can't Update Future Date", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(UpdateShgActivity.this, "Can't Update Future Date", Toast.LENGTH_SHORT).show();
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
    }

    private void disableInputs() {
        binding.etUpdateGoatSoldNo.setEnabled(false);
        binding.etUpdateGoatSoldNo.setClickable(false);
//        binding.etUpdateGoatIncome.setEnabled(false);
//        binding.etUpdateGoatIncome.setClickable(false);
        binding.etUpdateGoatBuy.setEnabled(false);
        binding.etUpdateGoatBuy.setClickable(false);
//        binding.etUpdateGoatExpenditure.setEnabled(false);
//        binding.etUpdateGoatExpenditure.setClickable(false);
//        binding.etUpdateGoatBorn.setEnabled(false);
//        binding.etUpdateGoatBorn.setClickable(false);
        binding.etUpdateGoatDead.setEnabled(false);
        binding.etUpdateGoatDead.setClickable(false);
        binding.etUpdateGoatVaccinated.setEnabled(false);
        binding.etUpdateGoatVaccinated.setClickable(false);

        binding.etUpdateBuckSold.setEnabled(false);
        binding.etUpdateBuckSold.setClickable(false);
        binding.etActivityBuckIncome.setEnabled(false);
        binding.etActivityBuckIncome.setClickable(false);
        binding.etUpdateBuckBuy.setEnabled(false);
        binding.etUpdateBuckBuy.setClickable(false);
        binding.etActivityBuckExpenditure.setEnabled(false);
        binding.etActivityBuckExpenditure.setClickable(false);
        binding.etUpdateKidsBorn.setEnabled(false);
        binding.etUpdateKidsBorn.setClickable(false);
        binding.etUpdateBuckDead.setEnabled(false);
        binding.etUpdateBuckDead.setClickable(false);
        binding.etUpdateBuckVaccinated.setEnabled(false);
        binding.etUpdateBuckVaccinated.setClickable(false);

        binding.rlActivityUpdate.setClickable(false);
        binding.rlActivityUpdate.setEnabled(false);
        binding.etUpdateRemarks.setClickable(false);
        binding.etUpdateRemarks.setEnabled(false);


        binding.switchVaccination.setEnabled(false);
        binding.switchDeworming.setEnabled(false);
        binding.switchBuckTied.setEnabled(false);
    }

    private void enableInputs() {
        binding.etUpdateGoatSoldNo.setEnabled(true);
        binding.etUpdateGoatSoldNo.setClickable(true);
//        binding.etUpdateGoatIncome.setEnabled(true);
//        binding.etUpdateGoatIncome.setClickable(true);
        binding.etUpdateGoatBuy.setEnabled(true);
        binding.etUpdateGoatBuy.setClickable(true);
//        binding.etUpdateGoatExpenditure.setEnabled(true);
//        binding.etUpdateGoatExpenditure.setClickable(true);
//        binding.etUpdateGoatBorn.setEnabled(true);
//        binding.etUpdateGoatBorn.setClickable(true);
        binding.etUpdateGoatDead.setEnabled(true);
        binding.etUpdateGoatDead.setClickable(true);
        binding.etUpdateGoatVaccinated.setEnabled(true);
        binding.etUpdateGoatVaccinated.setClickable(true);

        binding.etUpdateBuckSold.setEnabled(true);
        binding.etUpdateBuckSold.setClickable(true);
        binding.etActivityBuckIncome.setEnabled(true);
        binding.etActivityBuckIncome.setClickable(true);
        binding.etUpdateBuckBuy.setEnabled(true);
        binding.etUpdateBuckBuy.setClickable(true);
        binding.etActivityBuckExpenditure.setEnabled(true);
        binding.etActivityBuckExpenditure.setClickable(true);
        binding.etUpdateKidsBorn.setEnabled(true);
        binding.etUpdateKidsBorn.setClickable(true);
        binding.etUpdateBuckDead.setEnabled(true);
        binding.etUpdateBuckDead.setClickable(true);
        binding.etUpdateBuckVaccinated.setEnabled(true);
        binding.etUpdateBuckVaccinated.setClickable(true);

        binding.rlActivityUpdate.setClickable(true);
        binding.rlActivityUpdate.setEnabled(true);
        binding.etUpdateRemarks.setClickable(true);
        binding.etUpdateRemarks.setEnabled(true);


        binding.switchVaccination.setEnabled(true);
        binding.switchDeworming.setEnabled(true);
        binding.switchBuckTied.setEnabled(true);
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
            object.put("entityTypeCode",entityCode /*sp.getStringData("ENTITY_TYPE_CODE")*/);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "goatry-activity/view-line")
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
                                    monthListAdapter.restMonthSelected(tempMonth - 1);//removed -1
                                    monthListAdapter.notifyDataSetChanged();
//                                    Toast.makeText(UpdateShgActivity.this, getResources().getString(R.string.update_success), Toast.LENGTH_SHORT).show();
                                    goatearyActivityDetails();
                                    JSONObject obj = resObj.optJSONObject("data");
                                    if (obj != null) {

                                        Long numGoatSold = obj.optLong("numGoatSold");
                                        Long numBuckSold = obj.optLong("numBuckSold");
                                        Long numGoatBought = obj.optLong("numGoatBought");
                                        Long numBuckBought = obj.optLong("numBuckBought");
                                        Long numGoatBorn = obj.optLong("numGoatBorn");
                                        Long numBuckBorn = obj.optLong("numBuckBorn");
                                        Long numGoatDead = obj.optLong("numGoatDead");
                                        Long numBuckDead = obj.optLong("numBuckDead");
                                        Long goatVaccinated = obj.optLong("numVaccinated");
                                        Long buckVaccinated = obj.optLong("numDewormed");
                                        String buckIncome = obj.optString("totalIncome");
                                        String buckExpenditure = obj.optString("totalExpenditure");

                                        String remarks = obj.optString("remarks");

                                        boolean regularVaccinated = obj.optBoolean("regularVaccinated");
                                        boolean regularDeworming = obj.optBoolean("regularDeworming");
                                        boolean bucksTied = obj.optBoolean("bucksTied");

                                        binding.switchBuckTied.setChecked(bucksTied);
                                        binding.switchDeworming.setChecked(regularDeworming);

                                        binding.etUpdateGoatSoldNo.setText(String.valueOf(numGoatSold));
                                        binding.etUpdateGoatBuy.setText(String.valueOf(numGoatBought));
                                        binding.etUpdateGoatDead.setText(String.valueOf(numGoatDead));

                                        binding.etUpdateBuckSold.setText(String.valueOf(numBuckSold));
                                        binding.etActivityBuckIncome.setText(buckIncome.replace(",",""));
                                        binding.etUpdateBuckBuy.setText(String.valueOf(numBuckBought));
                                        binding.etActivityBuckExpenditure.setText(buckExpenditure.replace(",",""));
                                        binding.etUpdateKidsBorn.setText(String.valueOf(numBuckBorn));
                                        binding.etUpdateBuckDead.setText(String.valueOf(numBuckDead));

                                        binding.switchVaccination.setChecked(regularVaccinated);

                                        binding.etUpdateGoatVaccinated.setText(String.valueOf(goatVaccinated));
                                        binding.etUpdateBuckVaccinated.setText(String.valueOf(buckVaccinated));

                                        binding.etUpdateRemarks.setText(remarks);

                                    }

                                } else {
                                    binding.tvWfComments.setText("");
                                    binding.etUpdateGoatSoldNo.setText("");
//                                    binding.etUpdateGoatIncome.setText("");
                                    binding.etUpdateGoatBuy.setText("");
//                                    binding.etUpdateGoatExpenditure.setText("");
//                                    binding.etUpdateGoatBorn.setText("");
                                    binding.etUpdateGoatDead.setText("");

                                    binding.etUpdateBuckSold.setText("");
                                    binding.etActivityBuckIncome.setText("");
                                    binding.etUpdateBuckBuy.setText("");
                                    binding.etActivityBuckExpenditure.setText("");
                                    binding.etUpdateKidsBorn.setText("");
                                    binding.etUpdateBuckDead.setText("");

                                    binding.switchVaccination.setChecked(false);
                                    binding.switchDeworming.setChecked(false);
                                    binding.switchBuckTied.setChecked(false);

                                    binding.etUpdateGoatVaccinated.setText("");
                                    binding.etUpdateBuckVaccinated.setText("");

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
                        Log.e(TAG, "onError: " + anError.getErrorDetail());
                        binding.progress.setVisibility(View.GONE);
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

    private boolean isUpdate = false;

    private void updateActivityDetails() {

        binding.progress.setVisibility(View.VISIBLE);
        JSONObject object = new JSONObject();

        try {
            object.put("schemeId", sp.getLongData("SCHEME_ID"));
            object.put("activityId", sp.getLongData("ACTIVITY_ID"));
            object.put("year", Year);
            object.put("monthId", Month);
            object.put("entityId", sp.getLongData("ENTITY_ID"));
            object.put("entityTypeCode", sp.getStringData("ENTITY_TYPE_CODE"));
            object.put("numGoatSold", noOfGoatSold);
            object.put("totalIncome", totalIncome);
            object.put("numGoatBought", noOfGoatBuy);
            object.put("totalExpenditure", totalExpenditure);
            object.put("numGoatBorn", 0);
            object.put("numGoatDead", noOfGoatDead);
            object.put("numBuckSold", noOfBuckSold);
            object.put("numBuckBought", noOfBuckBuy);
            object.put("numBuckBorn", noOfKidsBorn);
            object.put("numBuckDead", noOfBuckDead);
            object.put("regularVaccinated", regularVaccinated);
            object.put("numVaccinated", numVaccinated);
            object.put("regularDeworming", regularDeworming);
            object.put("numDewormed", numDewormed);
            object.put("bucksTied", isBuckTied);
            object.put("remarks", remarks);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "goatry-activity/save-line")
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
                                    Toast.makeText(UpdateShgActivity.this, getResources().getString(R.string.update_success), Toast.LENGTH_SHORT).show();


                                    sp.setStringData(Constant.YEAR, String.valueOf(Year));
                                    sp.setStringData(Constant.MONTH, String.valueOf(Month));
                                    sp.setStringData(Constant.ENTITY_TYPE_CODE,entityCode);
                                    binding.tvSelectedMonth.setText(getResources().getString(R.string.select_month));

                                    binding.etUpdateGoatSoldNo.setText("");
//                                    binding.etUpdateBuckIncome.setText("");
                                    binding.etUpdateGoatBuy.setText("");
//                                    binding.etUpdateGoatExpenditure.setText("");
//                                    binding.etUpdateGoatBorn.setText("");
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

                                    isUpdate = true;
                                    Intent intent = getIntent();
                                    intent.putExtra("res", isUpdate);
                                    intent.putExtra("updatedYear", Year);
                                    intent.putExtra("updatedMonth", Month);
                                    setResult(RESULT_OK, intent);
                                    finish();

                                } else {
                                    isUpdate = false;
                                    Toast.makeText(UpdateShgActivity.this, resObj.optString("message"), Toast.LENGTH_SHORT).show();
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

//    final Calendar myCalendar = Calendar.getInstance();

    private int Year = 0;
    private int Month = 0;

//    private void openDatePicker() {
//        Calendar myCalendar = Calendar.getInstance();
//        final DatePickerDialog datePickerDialog = new DatePickerDialog(UpdateShgActivity.this, new DatePickerDialog.OnDateSetListener() {
//            @Override
//            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
//                myCalendar.set(Calendar.YEAR, year);
//                myCalendar.set(Calendar.MONTH, month);
//                myCalendar.set(Calendar.DAY_OF_MONTH, day);
//
//                Year = year;
//                Month = month + 1;
//
//                String myFormat = "MMM-yyyy"; //In which you need put here
//                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
//
//                binding.tvSelectedMonth.setText(sdf.format(myCalendar.getTime()));
//            }
//        }, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
//                myCalendar.get(Calendar.DAY_OF_MONTH));
//        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
//        datePickerDialog.show();
//    }

    @Override
    public void onBackPressed() {

//        Intent intent = getIntent();
//        intent.putExtra("res", isUpdate);
//        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }

    int clickedPosition;

    @Override
    public void onProfileItemClick(int position, String year) {
        clickedPosition = position;
        Month = position + 1;
        getViewDetails();

    }


    private void goatearyActivityDetails() {
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
                                        canEdit = obj.optBoolean("canEdit");
                                        String wfStatus = obj.optString("wfStatus");
                                        String comments = obj.optString("comments");
                                        String status = wfStatus.replace("_", " ");
                                        binding.tvWfStatus.setText(getResources().getString(R.string.status).concat(" ").concat(status));
                                        comments = comments.equals("null") ? "":comments;
                                        binding.tvWfComments.setText(comments);
                                        if (canEdit) {
                                            enableInputs();
                                        } else {
                                            disableInputs();
                                        }

                                    } else {

                                    }
                                } else {
//
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