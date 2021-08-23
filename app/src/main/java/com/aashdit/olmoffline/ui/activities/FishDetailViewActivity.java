package com.aashdit.olmoffline.ui.activities;

import android.content.Context;
import android.content.DialogInterface;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aashdit.olmoffline.BuildConfig;
import com.aashdit.olmoffline.R;
import com.aashdit.olmoffline.adapters.MonthListAdapter;
import com.aashdit.olmoffline.databinding.ActivityFishDetailViewBinding;
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

public class FishDetailViewActivity extends AppCompatActivity implements MonthListAdapter.MonthListener {

    private static final String TAG = "FishDetailViewActivity";
    String selectedYear = "2020";
    int month = 0;
    int year = 0;
    private ActivityFishDetailViewBinding binding;
    private SharedPrefManager sp;
    private String token;
    private Long lastUpdatedMonth;
    private Long lastUpdatedYear;
    private RecyclerView mRvMonthView;

    private ArrayList<Month> monthList;
    private MonthListAdapter monthListAdapter;
    private AppCompatSpinner spnYears;


    private final ArrayList<String> yearsList = new ArrayList<>();

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
        binding = ActivityFishDetailViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        resetTitles();

        sp = SharedPrefManager.getInstance(this);
        token = sp.getStringData(Constant.APP_TOKEN);

        setSupportActionBar(binding.toolbarDetailViewActivity);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.progreses.setVisibility(View.GONE);

        lastUpdatedMonth = getIntent().getLongExtra("LAST_MONTH", 0L);
        lastUpdatedYear = getIntent().getLongExtra("LAST_YEAR", 0L);


        selectedYear = String.valueOf(lastUpdatedYear);


        Long date = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date resultdate = new Date(date);
        System.out.println(sdf.format(resultdate));

        String s = sdf.format(resultdate);
        String[] parts = s.split("/");
        String yearNo = parts[2]; //sp.getStringData("YEAR");
        if (selectedYear.equals("0")) {
            selectedYear = yearNo;
        }
        int currentYear = Integer.parseInt(selectedYear);
        int currentOneDown = currentYear - 1;
        int currentTwoDown = currentYear - 2;
        int currentOneUp = currentYear + 1;
        int currentTwoUp = currentYear + 2;


        yearsList.add(String.valueOf(currentOneUp));
        yearsList.add(String.valueOf(currentTwoUp));
        yearsList.add(String.valueOf(currentYear));
        yearsList.add(String.valueOf(currentOneDown));
        yearsList.add(String.valueOf(currentTwoDown));

        monthList = new ArrayList<>();
        generateMonth(selectedYear);

        spnYears = findViewById(R.id.spn_years);
        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, yearsList);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spnYears.setAdapter(dataAdapter);
        spnYears.setSelection(2);
        spnYears.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedYear = yearsList.get(position); //parent.getItemAtPosition(position).toString();
                year = Integer.parseInt(selectedYear);
                generateMonth(selectedYear);
                monthListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        month = Integer.parseInt(String.valueOf(lastUpdatedMonth));
        year = Integer.parseInt(selectedYear);
        monthListAdapter = new MonthListAdapter(this, monthList, month - 1);
        monthListAdapter.setMonthListener(this);
        mRvMonthView = findViewById(R.id.rv_month_list);
        mRvMonthView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        mRvMonthView.setAdapter(monthListAdapter);
        mRvMonthView.getLayoutManager().scrollToPosition(month - 1);

        if(sp.getStringData("ENTITY_TYPE_CODE").equals("CLF")){
            binding.llContainerClfOne.setVisibility(View.VISIBLE);
            binding.llContainerClfTwo.setVisibility(View.VISIBLE);
        }else{
            binding.llContainerClfOne.setVisibility(View.GONE);
            binding.llContainerClfTwo.setVisibility(View.GONE);
        }

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
    }

    private void getViewDetails() {
        binding.progreses.setVisibility(View.VISIBLE);
        JSONObject object = new JSONObject();
        try {
            object.put("schemeId", sp.getLongData("SCHEME_ID"));
            object.put("activityId", sp.getLongData("ACTIVITY_ID"));
            object.put("year", year);
            object.put("month", month);
            object.put("entityId", sp.getLongData("ENTITY_ID"));
            object.put("entityTypeCode", sp.getStringData("ENTITY_TYPE_CODE"));
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
                        binding.progreses.setVisibility(View.GONE);
                        if (Utility.isStringValid(response)) {
                            try {
                                JSONObject resObj = new JSONObject(response);

                                if (resObj.optBoolean("outcome")) {
//                                    Toast.makeText(FishDetailViewActivity.this, getResources().getString(R.string.update_success), Toast.LENGTH_SHORT).show();

                                    JSONObject obj = resObj.optJSONObject("data");
                                    if (obj != null) {

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

                                        binding.tvUpdateFingIncome.setText(fingerlingIncome.replace(",",""));
                                        binding.tvUpdateTabFishIncome.setText(tableFishIncome.replace(",", ""));
                                        boolean regularNettingDone = obj.optBoolean("regularNettingDone");

                                        binding.swtNettingMgnt.setChecked(regularNettingDone);
                                        binding.swtNettingMgnt.setEnabled(false);

                                        boolean feedManagementDone = obj.optBoolean("feedManagementDone");
                                        if (feedManagementDone) {
                                            binding.cvQtyFood.setVisibility(View.VISIBLE);
                                        } else {
                                            binding.cvQtyFood.setVisibility(View.GONE);
                                        }

                                        if (regularNettingDone) {
                                            binding.cvNettingFreq.setVisibility(View.VISIBLE);
                                        } else {
                                            binding.cvNettingFreq.setVisibility(View.GONE);
                                        }

                                        binding.swtFeedingMgnt.setChecked(feedManagementDone);
                                        binding.swtFeedingMgnt.setEnabled(false);

                                        binding.tvDetailFrequency.setText(String.valueOf(nettingFrequency));
                                        binding.etFryFeedQty.setText(String.valueOf(fryFeedQty));
                                        binding.etFryFeedQty.setEnabled(false);
                                        binding.etFryFeedQty.setClickable(false);
                                        binding.etFingFeedQty.setText(String.valueOf(fingerlingFeedQty));
                                        binding.etFingFeedQty.setEnabled(false);
                                        binding.etFingFeedQty.setClickable(false);
                                        binding.etYearlingsFeedKg.setText(String.valueOf(yearlingFeedQty));
                                        binding.etYearlingsFeedKg.setEnabled(false);
                                        binding.etYearlingsFeedKg.setClickable(false);
                                        binding.tvDetailFingerlingHarvested.setText(String.valueOf(numFingerlingsHarvested));
                                        binding.tvDetailTabSizeFishHarvested.setText(String.valueOf(tableSizeFishHarvested));
                                        binding.tvDetailFingerlingSold.setText(String.valueOf(fingerlingsSoldQty));
                                        binding.tvDetailTabSizeFishSold.setText(String.valueOf(tableSizeFishSold));
                                        binding.tvDetailFryStock.setText(String.valueOf(numFryStocked));
                                        binding.tvDetailFingerlingStock.setText(String.valueOf(numFingerlingsStocked));
                                        binding.tvDetailYearlingStock.setText(String.valueOf(numYearlingsStocked));
                                        binding.tvDetailFishIncome.setText(totalIncome.replace(",", ""));
                                        binding.tvDetailFishExpenditure.setText(totalExpenditure.replace(",", ""));

                                        binding.etUpdateRemarks.setText(remarks);
                                        binding.etUpdateRemarks.setEnabled(false);
                                        binding.etUpdateRemarks.setClickable(false);

                                    }

                                } else {

                                    binding.swtFeedingMgnt.setChecked(false);
                                    binding.swtFeedingMgnt.setEnabled(false);

                                    binding.swtNettingMgnt.setChecked(false);
                                    binding.swtNettingMgnt.setEnabled(false);

                                    binding.tvDetailFrequency.setText("");
                                    binding.etFryFeedQty.setText("");
                                    binding.etFryFeedQty.setEnabled(false);
                                    binding.etFryFeedQty.setClickable(false);
                                    binding.etFingFeedQty.setText("");
                                    binding.etFingFeedQty.setEnabled(false);
                                    binding.etFingFeedQty.setClickable(false);
                                    binding.etYearlingsFeedKg.setText("");
                                    binding.etYearlingsFeedKg.setEnabled(false);
                                    binding.etYearlingsFeedKg.setClickable(false);
                                    binding.tvDetailFingerlingHarvested.setText("");
                                    binding.tvDetailTabSizeFishHarvested.setText("");
                                    binding.tvDetailFingerlingSold.setText("");
                                    binding.tvDetailTabSizeFishSold.setText("");
                                    binding.tvDetailFryStock.setText("");
                                    binding.tvDetailFingerlingStock.setText("");
                                    binding.tvDetailYearlingStock.setText("");
                                    binding.tvDetailFishIncome.setText("");
                                    binding.tvDetailFishExpenditure.setText("");

                                    binding.tvDetailFishIncome.setText("");
                                    binding.tvDetailFishExpenditure.setText("");

                                    binding.etUpdateRemarks.setText("");

                                    showDialog(resObj.optString("message"));
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        binding.progreses.setVisibility(View.GONE);
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

    private void showDialog(String message) {
        if (!FishDetailViewActivity.this.isFinishing()) {
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
        month = position + 1;

        getViewDetails();
    }
}