package com.aashdit.olmoffline.ui.activities;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.aashdit.olmoffline.BuildConfig;
import com.aashdit.olmoffline.R;
import com.aashdit.olmoffline.adapters.ShgSpnAdapter;
import com.aashdit.olmoffline.databinding.ActivityFishBinding;
import com.aashdit.olmoffline.models.SHG;
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
import java.util.Calendar;
import java.util.Locale;

import static android.content.pm.PackageManager.GET_META_DATA;

public class AddFishActivity extends AppCompatActivity {

    private static final String TAG = "FishActivity";
    private final ArrayList<SHG> shgArrayList = new ArrayList<>();
    private final String noOfPondsOwned = "0";
    private final boolean isRegularNetting = false;
    private final boolean isFeedMgnt = false;
    private ActivityFishBinding binding;
    private String token, entityTypeCode;
    private SharedPrefManager sp;
    private Long selectedSchemeId, selectedActivityId;
    private ShgSpnAdapter shgSpnAdapter;
    private String noOfHouseHold = "";
    private String baseCapital = "";
    private String totFryStock = "";
    private String totFingerlingStock = "";
    private String totYearlingStock = "";
    private String qtyOfFingerling = "";
    private String qtyOfYearling = "";
    private String qtyOfFry = "";
    private String commencementDate = "";
    private String remarks = "";
    private Long selectedShgId = 0L;
    private Long entityId = 0L;
    private String intentType, entityName;
    private Long fisheryRegistrationId;

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
        binding = ActivityFishBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        resetTitles();
        setSupportActionBar(binding.toolbarFishingActivity);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        sp = SharedPrefManager.getInstance(this);
        token = sp.getStringData(Constant.APP_TOKEN);
        selectedSchemeId = getIntent().getLongExtra("SCHEME_ID", 0L);
        selectedActivityId = getIntent().getLongExtra("ACTIVITY_ID", 0L);
        entityTypeCode = getIntent().getStringExtra("ENTITY_TYPE_CODE");
        entityId = getIntent().getLongExtra("ENTITY_ID", 0L);
        entityName = getIntent().getStringExtra("ENTITY_NAME");
        intentType = getIntent().getStringExtra(Constant.INTENT_TYPE);
        binding.progreses.setVisibility(View.GONE);
        if (entityTypeCode.equals("SHG")) {
            binding.tvFishTitle.setText(intentType.equals(Constant.ADD) ?
                    getResources().getString(R.string.add_shg_activity) : "Update SHG Activity");
        } else if (entityTypeCode.equals("HH")) {
            binding.tvFishTitle.setText(getResources().getString(R.string.add_hh_activity));
        } else if (entityTypeCode.equals("CLF")) {
            binding.tvFishTitle.setText(getResources().getString(R.string.add_clf_activity));
        }

        SHG shg = new SHG();
        shg.setShgName("Select SHG");
        shg.setShgDetailsId(0L);
        shg.setShgRegNumber("SHG");
        shg.setShgType(0L);
        shgArrayList.add(shg);
        getShgList();

        shgSpnAdapter = new ShgSpnAdapter(this, shgArrayList);
        binding.spnAddShgFishSelectShg.setAdapter(shgSpnAdapter);

        binding.spnAddShgFishSelectShg.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    selectedShgId = 0L;
                } else {
                    selectedShgId = shgArrayList.get(position).getShgDetailsId();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        binding.etCommenceDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDatePicker();
            }
        });

        binding.addShgSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noOfHouseHold = binding.etNoOfHh.getText().toString().trim();
                baseCapital = binding.etBaseCapital.getText().toString().trim();
                qtyOfFingerling = binding.etFingerlingsQty.getText().toString().trim();
                qtyOfYearling = binding.etYearlingsQty.getText().toString().trim();
                qtyOfFry = binding.etFryQty.getText().toString().trim();
                totFryStock = binding.etTotalFryStockNo.getText().toString().trim();
                totFingerlingStock = binding.etTotalFingerlingStockNo.getText().toString().trim();
                totYearlingStock = binding.etTotalYeerlingStockNo.getText().toString().trim();
                commencementDate = binding.etCommenceDate.getText().toString().trim();
                remarks = binding.etRemarks.getText().toString().trim();

                if (intentType.equals(Constant.ADD) && selectedShgId.equals(0L)) {
                    Toast.makeText(AddFishActivity.this, "Please select a SHG", Toast.LENGTH_SHORT).show();
                } else if (noOfHouseHold.isEmpty()) {
                    Toast.makeText(AddFishActivity.this, "Please fill No of Members", Toast.LENGTH_SHORT).show();
                } else if (baseCapital.isEmpty()) {
                    Toast.makeText(AddFishActivity.this, "Please fill BaseCapital", Toast.LENGTH_SHORT).show();
                } else if (commencementDate.isEmpty()) {
                    Toast.makeText(AddFishActivity.this, "Please fill Commencement Date", Toast.LENGTH_SHORT).show();
                } /*else if (qtyOfFry.isEmpty()) {
                    Toast.makeText(AddFishActivity.this, "Please fill Quantity of Fry", Toast.LENGTH_SHORT).show();
                } else if (qtyOfFingerling.isEmpty()) {
                    Toast.makeText(AddFishActivity.this, "Please fill Quantity of Fingerlings", Toast.LENGTH_SHORT).show();
                } else if (qtyOfYearling.isEmpty()) {
                    Toast.makeText(AddFishActivity.this, "Please fill Quantity of Yearlings", Toast.LENGTH_SHORT).show();
                }*/ else if (totFryStock.isEmpty()) {
                    Toast.makeText(AddFishActivity.this, "Please fill Total No of Fry Stock", Toast.LENGTH_SHORT).show();
                } else if (totFingerlingStock.isEmpty()) {
                    Toast.makeText(AddFishActivity.this, "Please fill Total No of Fingerling Stock", Toast.LENGTH_SHORT).show();
                } else if (totYearlingStock.isEmpty()) {
                    Toast.makeText(AddFishActivity.this, "Please fill Total No of Yearling Stock", Toast.LENGTH_SHORT).show();
                } else {
                    shgRegistrationForPisciculture();
                }
            }
        });

        if (intentType.equals(Constant.UPDATE)) {
            binding.ivEdit.setVisibility(View.VISIBLE);
            binding.tvFishShgName.setText(entityName);
            binding.tvFishShgName.setVisibility(View.VISIBLE);
            binding.spnAddShgFishSelectShg.setVisibility(View.GONE);
            binding.tvSelectShgLbl.setVisibility(View.GONE);
            getViewReg();
            disableFields();

            binding.ivEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    binding.ivEdit.setColorFilter(ContextCompat.getColor(AddFishActivity.this, R.color.purple_500), android.graphics.PorterDuff.Mode.MULTIPLY);
                    enableFields();
                }
            });
        } else {
            binding.ivEdit.setVisibility(View.GONE);
            binding.tvFishShgName.setVisibility(View.GONE);
            binding.spnAddShgFishSelectShg.setVisibility(View.VISIBLE);
            enableFields();
//            binding.tvSelectShgLbl.setVisibility(View.VISIBLE);
        }
    }

    private void getViewReg() {

        /**
         * {
         *     "schemeId" : 4,
         *     "activityId":2,
         *     "entityId":423815,
         *     "reportingLevelCode": "SHG"
         * }
         * */

        JSONObject obj = new JSONObject();
        try {
            obj.put("schemeId", selectedSchemeId);
            obj.put("activityId", selectedActivityId);
            obj.put("entityId", entityId);
            obj.put("reportingLevelCode", "SHG");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "fishery-activity/view-reg")
                .addHeaders("Authorization", "Bearer " + token)
                .addJSONObjectBody(obj)
                .setTag("view-regd")
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        if (Utility.isStringValid(response)) {
                            try {
                                JSONObject object = new JSONObject(response);
                                if (object.optBoolean("outcome")) {
                                    JSONObject resObj = object.optJSONObject("data");

                                    fisheryRegistrationId = resObj.optLong("fisheryRegistrationId");
                                    noOfHouseHold = String.valueOf(resObj.optLong("numFarmers"));
                                    baseCapital = resObj.optString("baseCapital").replace(",", "");

                                    commencementDate = resObj.optString("commencementDate");
                                    remarks = resObj.optString("remarks");
                                    totFryStock = String.valueOf(resObj.optLong("numFryStocked"));
                                    totFingerlingStock = String.valueOf(resObj.optLong("numFingerlingsStocked"));
                                    totYearlingStock = String.valueOf(resObj.optLong("numYearlingsStocked"));
//
                                    binding.etBaseCapital.setText(baseCapital);
                                    binding.etCommenceDate.setText(commencementDate);
                                    binding.etRemarks.setText(remarks);
                                    binding.etNoOfHh.setText(noOfHouseHold);
                                    binding.etTotalFryStockNo.setText(totFryStock);
                                    binding.etTotalFingerlingStockNo.setText(totFingerlingStock);
                                    binding.etTotalYeerlingStockNo.setText(totYearlingStock);
//

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


    private void shgRegistrationForPisciculture() {

        totFryStock = totFryStock.equals("") ? "0" : totFryStock;
        totFingerlingStock = totFingerlingStock.equals("") ? "0" : totFingerlingStock;
        totYearlingStock = totYearlingStock.equals("") ? "0" : totYearlingStock;
        qtyOfFry = qtyOfFry.equals("") ? "0" : qtyOfFry;
        qtyOfFingerling = qtyOfFingerling.equals("") ? "0" : qtyOfFingerling;
        qtyOfYearling = qtyOfYearling.equals("") ? "0" : qtyOfYearling;


        binding.progreses.setVisibility(View.VISIBLE);
        JSONObject obj = new JSONObject();
        try {
            if (intentType.equals(Constant.UPDATE)) {
                obj.put("fisheryRegistrationId", fisheryRegistrationId);
            }
            obj.put("entityTypeCode", entityTypeCode);
            obj.put("entityId", intentType.equals(Constant.UPDATE) ? entityId : selectedShgId);
            obj.put("numPondsOwned", Integer.parseInt(noOfPondsOwned));
//            obj.put("numPondsLeased", Integer.parseInt(noOfPondsLeased));
//            obj.put("areaOfPond", Double.parseDouble(areaOfPonds));
            obj.put("baseCapital", Double.parseDouble(baseCapital));
            obj.put("commencementDate", commencementDate);
            obj.put("remarks", remarks);
            obj.put("schemeId", selectedSchemeId);
            obj.put("activityId", selectedActivityId);
//            obj.put("pondCleaningFrequency", Integer.parseInt(pondCleaningFreq));
            obj.put("regularNettingDone", isRegularNetting);
//            obj.put("nettingFrequency", Integer.parseInt(nettingFreq));
            obj.put("numFarmers", Integer.parseInt(noOfHouseHold));
            obj.put("numFryStocked", Integer.parseInt(totFryStock));
            obj.put("numFingerlingsStocked", Integer.parseInt(totFingerlingStock));
            obj.put("numYearlingsStocked", Integer.parseInt(totYearlingStock));
            obj.put("feedManagementDone", isFeedMgnt);
//            obj.put("fryFeedQty", Integer.parseInt(feedFry/*qtyOfFry*/));
//            obj.put("fingerlingFeedQty", Integer.parseInt(feedFingerling/*qtyOfFingerling*/));
//            obj.put("yearlingFeedQty", Integer.parseInt(feedYearling/*qtyOfYearling*/));
        } catch (JSONException e) {
            e.printStackTrace();
        }


        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "fishery-activity/save")
                .addHeaders("Authorization", "Bearer " + token)
                .addJSONObjectBody(obj)
                .setTag("save")
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

                                    Intent intent = getIntent();
                                    intent.putExtra("res", true);
                                    intent.putExtra("schemeId", selectedSchemeId);
                                    intent.putExtra("activityId", selectedActivityId);
                                    setResult(RESULT_OK, intent);
                                    finish();

                                    Toast.makeText(AddFishActivity.this, resObj.optString("message"), Toast.LENGTH_LONG)
                                            .show();
                                } else {
                                    Toast.makeText(AddFishActivity.this, resObj.optString("message"), Toast.LENGTH_LONG)
                                            .show();
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

    private void disableFields() {
        binding.etNoOfHh.setEnabled(false);
        binding.etNoOfHh.setClickable(false);
        binding.etTotalFryStockNo.setEnabled(false);
        binding.etTotalFryStockNo.setClickable(false);
        binding.etTotalYeerlingStockNo.setEnabled(false);
        binding.etTotalYeerlingStockNo.setClickable(false);
        binding.etTotalFingerlingStockNo.setEnabled(false);
        binding.etTotalFingerlingStockNo.setClickable(false);
        binding.etRemarks.setEnabled(false);
        binding.etRemarks.setClickable(false);
        binding.etCommenceDate.setEnabled(false);
        binding.etCommenceDate.setClickable(false);
        binding.etBaseCapital.setEnabled(false);
        binding.etBaseCapital.setClickable(false);

        binding.addShgSubmit.setEnabled(false);
        binding.addShgSubmit.setClickable(false);
    }

    private void enableFields() {
        binding.etNoOfHh.setEnabled(true);
        binding.etTotalFryStockNo.setEnabled(true);
        binding.etTotalFingerlingStockNo.setEnabled(true);
        binding.etTotalYeerlingStockNo.setEnabled(true);
        binding.etRemarks.setEnabled(true);
        binding.etCommenceDate.setEnabled(true);
        binding.etBaseCapital.setEnabled(true);

        binding.addShgSubmit.setEnabled(true);
        binding.addShgSubmit.setClickable(true);
    }


    private void openDatePicker() {
        Calendar myCalendar = Calendar.getInstance();
        final DatePickerDialog datePickerDialog = new DatePickerDialog(AddFishActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, month);
                myCalendar.set(Calendar.DAY_OF_MONTH, day);

                String myFormat = "dd/MM/yyyy"; //In which you need put here
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

                binding.etCommenceDate.setText(sdf.format(myCalendar.getTime()));
            }
        }, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void getShgList() {

        JSONObject obj = new JSONObject();
        try {
            obj.put("schemeId", selectedSchemeId);
            obj.put("activityId", selectedActivityId);
            obj.put("reportingLevelCode", "SHG");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "fishery-activity/shg/list")
                .addHeaders("Authorization", "Bearer " + token)
                .addJSONObjectBody(obj)
                .setTag("shglist")
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
                                        shgArrayList.clear();
                                        SHG shg = new SHG();
                                        shg.setShgName("Select SHG");
                                        shg.setShgDetailsId(0L);
                                        shg.setShgRegNumber("SHG");
                                        shg.setShgType(0L);
                                        shgArrayList.add(shg);
                                        for (int i = 0; i < array.length(); i++) {
                                            SHG scheme = SHG.parseShg(array.optJSONObject(i));
                                            shgArrayList.add(scheme);
                                        }

                                        shgSpnAdapter.notifyDataSetChanged();
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
}