package com.aashdit.olmoffline.ui.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aashdit.olmoffline.BuildConfig;
import com.aashdit.olmoffline.adapters.MonthListAdapter;
import com.aashdit.olmoffline.databinding.ActivityFarmingDetailsViewBinding;
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

public class FarmingDetailsViewActivity extends AppCompatActivity implements MonthListAdapter.MonthListener{

    private static final String TAG = "FarmingDetailsViewActiv";
    private ActivityFarmingDetailsViewBinding binding;

    private Long lastUpdatedMonth;
    private Long lastUpdatedYear;

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


    private boolean canEdit;

    private SharedPrefManager sp;
    private String token;
    private String salesUOM,prodUOM;
    String selectedYear = "2021";
    private final ArrayList<String> yearsList = new ArrayList<>();
    private ArrayList<Month> monthList;
    private MonthListAdapter monthListAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFarmingDetailsViewBinding.inflate(getLayoutInflater());
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

        salesUOM = getIntent().getStringExtra("SUOM");
        prodUOM = getIntent().getStringExtra("PUOM");
        lastUpdatedMonth = getIntent().getLongExtra("LAST_MONTH", 0L);
        lastUpdatedYear = getIntent().getLongExtra("LAST_YEAR", 0L);
        canEdit = getIntent().getBooleanExtra("EDIT", false);
        if (canEdit) {

        } else {
//            disableInputs();
        }

        binding.tvSalesUom.setText("(".concat(salesUOM).concat(") :"));
        binding.tvProdUom.setText("(".concat(prodUOM).concat(") :"));
        selectedYear = String.valueOf(lastUpdatedYear);

        Long date = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date resultdate = new Date(date);
        System.out.println(sdf.format(resultdate));

        String s = sdf.format(resultdate);
        String[] parts = s.split("/");
        String yearNo = parts[2]; //sp.getStringData("YEAR");
        if (selectedYear.equals("0")){
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

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, yearsList);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        binding.llVaccinationView.setVisibility(View.GONE);
        binding.spnYears.setAdapter(dataAdapter);
        binding.spnYears.setSelection(2);
        binding.spnYears.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedYear = yearsList.get(position); //parent.getItemAtPosition(position).toString();
                year = Integer.parseInt(selectedYear);
                generateMonth(selectedYear);
                monthListAdapter.notifyDataSetChanged();

//                Toast.makeText(getApplicationContext(), parent.getItemAtPosition(position).toString(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        month = Integer.parseInt(String.valueOf(lastUpdatedMonth));
        year = Integer.parseInt(selectedYear);
        monthListAdapter = new MonthListAdapter(this, monthList, month - 1);
        monthListAdapter.setMonthListener(this);
        binding.rvMonthList.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        binding.rvMonthList.setAdapter(monthListAdapter);
        binding.rvMonthList.getLayoutManager().scrollToPosition(month - 1);
        getViewDetails();
//        binding.tvUpdateRemarks.setOnTouchListener(new View.OnTouchListener() {
//
//            public boolean onTouch(View v, MotionEvent event) {
//                if (binding.tvUpdateRemarks.hasFocus()) {
//                    v.getParent().requestDisallowInterceptTouchEvent(true);
//                    switch (event.getAction() & MotionEvent.ACTION_MASK) {
//                        case MotionEvent.ACTION_SCROLL:
//                            v.getParent().requestDisallowInterceptTouchEvent(false);
//                            return true;
//                    }
//                }
//                return false;
//            }
//        });
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


        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "farm-activity/view-line")
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
//                                    Toast.makeText(BirdDetailViewActivity.this, getResources().getString(R.string.update_success), Toast.LENGTH_SHORT).show();

                                    JSONObject obj = resObj.optJSONObject("data");
                                    if (obj != null) {

                                        Double quantitySold = obj.optDouble("quantitySold");
                                        Double quantityProduced = obj.optDouble("quantityProduced");

                                        String totalIncome = obj.optString("totalIncome");
                                        String seasonalIncome = obj.optString("seasonalIncome");
                                        String totalExpenditure = obj.optString("totalExpenditure");
                                        String remarks = obj.optString("remarks");


                                        binding.etUpdateSalesQtySold.setText(String.valueOf(quantitySold));
                                        binding.etUpdateProdQtySold.setText(String.valueOf(quantityProduced));
                                        binding.etUpdateRemarks.setText(remarks);
                                        binding.tvTotalInputCost.setText(totalExpenditure.replace(",",""));
                                        binding.tvSeasonalIncome.setText(seasonalIncome.replace(",",""));
                                        binding.tvTotalGrossIncome.setText(totalIncome.replace(",",""));

                                        binding.etUpdateRemarks.setText(remarks);

                                    }

                                } else {

                                    binding.etUpdateSalesQtySold.setText("");
                                    binding.etUpdateProdQtySold.setText("");
                                    binding.etUpdateRemarks.setText("");
                                    binding.tvTotalInputCost.setText("");
                                    binding.tvSeasonalIncome.setText("");
                                    binding.tvTotalGrossIncome.setText("");

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
        if (!FarmingDetailsViewActivity.this.isFinishing()) {
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    int month = 0;
    int year = 0;

    @Override
    public void onProfileItemClick(int position, String year) {
        month = position + 1;

        getViewDetails();
    }
}