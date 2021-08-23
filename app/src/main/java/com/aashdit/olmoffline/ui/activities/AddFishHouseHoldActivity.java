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
import com.aashdit.olmoffline.adapters.HouseHoldSpnAdapter;
import com.aashdit.olmoffline.adapters.ShgSpnAdapter;
import com.aashdit.olmoffline.databinding.ActivityAddFishHouseHoldBinding;
import com.aashdit.olmoffline.models.HHEntity;
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

public class AddFishHouseHoldActivity extends AppCompatActivity {

    private static final String TAG = "AddFishHouseHoldActivit";

    private ActivityAddFishHouseHoldBinding binding;


    private String token, entityTypeCode;
    private SharedPrefManager sp;
    private Long selectedSchemeId, selectedActivityId;

    private final ArrayList<SHG> shgArrayList = new ArrayList<>();
    private ShgSpnAdapter shgSpnAdapter;
    private Long selectedShgId = 0L;
    private Long selectedHhId = 0L;
    private Long fisheryRegistrationId;
    private String noOfHouseHold = "0";
    private String baseCapital = "";
    private String noOfPondsOwned = "0";
    private String totFryStock = "";
    private String totFingerlingStock = "";
    private String totYearlingStock = "";
    private String commencementDate = "";
    private String remarks = "";
    private final boolean isRegularNetting = false;
    private final boolean isFeedMgnt = false;

    private String intentType, entityName, shgName;

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
        binding = ActivityAddFishHouseHoldBinding.inflate(getLayoutInflater());
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
        intentType = getIntent().getStringExtra(Constant.INTENT_TYPE);
        selectedHhId = getIntent().getLongExtra("ENTITY_ID", 0L);
        entityName = getIntent().getStringExtra("ENTITY_NAME");
        shgName = getIntent().getStringExtra("SHG_NAME");
        binding.tvFishTitle.setText(intentType.equals(Constant.ADD) ?
                getResources().getString(R.string.add_hh_activity) : "Update Member Activity");

        binding.progreses.setVisibility(View.GONE);
//        switch (entityTypeCode) {
//            case "SHG":
//                binding.tvFishTitle.setText(getResources().getString(R.string.add_shg_activity));
//                break;
//            case "HH":
//                binding.tvFishTitle.setText(getResources().getString(R.string.add_hh_activity));
//                break;
//            case "CLF":
//                binding.tvFishTitle.setText(getResources().getString(R.string.add_clf_activity));
//                break;
//        }


        HHEntity entries = new HHEntity();
        entries.entityName="Select Member";
        entries.entityId=0L;
        entriesArrayList.add(entries);
        houseHoldSpnAdapter = new HouseHoldSpnAdapter(this,entriesArrayList);
        binding.spnFishHh.setAdapter(houseHoldSpnAdapter);

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
                    resetMembersData();
                }else {
                    selectedShgId = shgArrayList.get(position).getShgDetailsId();
                    resetMembersData();
                    getHouseHoldList();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        binding.spnFishHh.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    selectedHhId = 0L;
                }else {
                    selectedHhId = entriesArrayList.get(position).entityId;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        binding.etCommenceDate.setLongClickable(false);
        binding.etCommenceDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDatePicker();
            }
        });

        binding.addHhSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noOfPondsOwned = binding.etPondsOwned.getText().toString().trim();
                baseCapital = binding.etBaseCapital.getText().toString().trim();
                commencementDate = binding.etCommenceDate.getText().toString().trim();
                totFryStock = binding.etTotalFryStockNo.getText().toString().trim();
                totFingerlingStock = binding.etTotalFingerlingStockNo.getText().toString().trim();
                totYearlingStock = binding.etTotalYeerlingStockNo.getText().toString().trim();
                remarks = binding.etRemarks.getText().toString().trim();


                if (intentType.equals(Constant.ADD) && selectedShgId.equals(0L)) {
                    Toast.makeText(AddFishHouseHoldActivity.this, "Please select a SHG", Toast.LENGTH_SHORT).show();
                } else if (intentType.equals(Constant.ADD) && selectedHhId.equals(0L)) {
                    Toast.makeText(AddFishHouseHoldActivity.this, "Please select a Member", Toast.LENGTH_SHORT).show();
                }/* else if (noOfPondsOwned.isEmpty()) {
                    Toast.makeText(AddFishHouseHoldActivity.this, "Please fill no Of PondsOwned", Toast.LENGTH_SHORT).show();
                }*/else if (baseCapital.isEmpty()) {
                    Toast.makeText(AddFishHouseHoldActivity.this, "Please fill BaseCapital", Toast.LENGTH_SHORT).show();
                } else if (commencementDate.isEmpty()) {
                    Toast.makeText(AddFishHouseHoldActivity.this, "Please fill Commencement Date", Toast.LENGTH_SHORT).show();
                } else if (totFryStock.isEmpty()) {
                    Toast.makeText(AddFishHouseHoldActivity.this, "Please fill Total No of Fry Stock", Toast.LENGTH_SHORT).show();
                } else if (totFingerlingStock.isEmpty()) {
                    Toast.makeText(AddFishHouseHoldActivity.this, "Please fill Total No of Fingerling Stock", Toast.LENGTH_SHORT).show();
                } else if (totYearlingStock.isEmpty()) {
                    Toast.makeText(AddFishHouseHoldActivity.this, "Please fill Total No of Yearling Stock", Toast.LENGTH_SHORT).show();
                } else {
                    hhRegistrationForPisciculture();
                }

            }
        });

        if (intentType.equals(Constant.UPDATE)) {
            binding.tvSelectShgLbl.setVisibility(View.GONE);
            binding.tvSelectHhLblUpdate.setVisibility(View.GONE);

            binding.tvFishShgName.setText(shgName);
            binding.tvFishMemberName.setText(entityName);

            binding.spnAddShgFishSelectShg.setVisibility(View.GONE);
            binding.spnFishHh.setVisibility(View.GONE);
            binding.ivEdit.setVisibility(View.VISIBLE);
            getViewReg();
            disableFields();
            binding.ivEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    binding.ivEdit.setColorFilter(ContextCompat.getColor(AddFishHouseHoldActivity.this, R.color.purple_500), android.graphics.PorterDuff.Mode.MULTIPLY);
                    enableFields();
                }
            });
        }else{
            binding.ivEdit.setVisibility(View.GONE);
            binding.tvSelectShgLbl.setVisibility(View.GONE);
            binding.tvSelectHhLblUpdate.setVisibility(View.GONE);
            binding.spnAddShgFishSelectShg.setVisibility(View.VISIBLE);
            binding.spnFishHh.setVisibility(View.VISIBLE);
        }
    }


    private void disableFields() {
        binding.etRemarks.setEnabled(false);
        binding.etRemarks.setClickable(false);
        binding.etCommenceDate.setEnabled(false);
        binding.etCommenceDate.setClickable(false);
        binding.etBaseCapital.setEnabled(false);
        binding.etBaseCapital.setClickable(false);
        binding.etTotalFryStockNo.setEnabled(false);
        binding.etTotalFryStockNo.setClickable(false);
        binding.etTotalFingerlingStockNo.setEnabled(false);
        binding.etTotalFingerlingStockNo.setClickable(false);
        binding.etTotalYeerlingStockNo.setEnabled(false);
        binding.etTotalYeerlingStockNo.setClickable(false);

        binding.addHhSubmit.setEnabled(false);
        binding.addHhSubmit.setClickable(false);
    }

    private void enableFields() {
        binding.etRemarks.setEnabled(true);
        binding.etCommenceDate.setEnabled(true);
        binding.etBaseCapital.setEnabled(true);
        binding.etTotalFryStockNo.setEnabled(true);
        binding.etTotalFingerlingStockNo.setEnabled(true);
        binding.etTotalYeerlingStockNo.setEnabled(true);

        binding.addHhSubmit.setEnabled(true);
        binding.addHhSubmit.setClickable(true);
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
            obj.put("entityId", selectedHhId);
            obj.put("reportingLevelCode", "HH");

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

                                    binding.etBaseCapital.setText(baseCapital);
                                    binding.etCommenceDate.setText(commencementDate);
                                    binding.etRemarks.setText(remarks);
                                    binding.etTotalFryStockNo.setText(totFryStock);
                                    binding.etTotalFingerlingStockNo.setText(totFingerlingStock);
                                    binding.etTotalYeerlingStockNo.setText(totYearlingStock);

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

    private void resetMembersData() {
        entriesArrayList.clear();
        HHEntity entries = new HHEntity();
        entries.entityName = "Select Member";
        entries.entityId = 0L;
        entriesArrayList.add(entries);

        houseHoldSpnAdapter.notifyDataSetChanged();
    }

    private void hhRegistrationForPisciculture() {

        totFryStock = totFryStock.equals("")?"0":totFryStock;
        totFingerlingStock = totFingerlingStock.equals("")?"0":totFingerlingStock;
        totYearlingStock = totYearlingStock.equals("")?"0":totYearlingStock;

        binding.progreses.setVisibility(View.VISIBLE);
        JSONObject obj = new JSONObject();
        try {
            if (intentType.equals(Constant.UPDATE)) {
                obj.put("fisheryRegistrationId", fisheryRegistrationId);
            }
            obj.put("entityTypeCode", entityTypeCode);
            obj.put("entityId", selectedHhId);
            obj.put("numPondsOwned", 0);
            obj.put("baseCapital", Double.parseDouble(baseCapital));
            obj.put("commencementDate", commencementDate);
            obj.put("remarks", remarks);
            obj.put("schemeId", selectedSchemeId);
            obj.put("activityId", selectedActivityId);
            obj.put("regularNettingDone", isRegularNetting);
            obj.put("numFarmers", Integer.parseInt(noOfHouseHold));
            obj.put("numFryStocked", Integer.parseInt(totFryStock));
            obj.put("numFingerlingsStocked", Integer.parseInt(totFingerlingStock));
            obj.put("numYearlingsStocked", Integer.parseInt(totYearlingStock));
            obj.put("feedManagementDone", isFeedMgnt);
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

                                    Toast.makeText(AddFishHouseHoldActivity.this, resObj.optString("message"), Toast.LENGTH_LONG)
                                            .show();
                                } else {
                                    Toast.makeText(AddFishHouseHoldActivity.this, resObj.optString("message"), Toast.LENGTH_LONG)
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

    private void getShgList() {

        JSONObject obj = new JSONObject();
        try {
            obj.put("schemeId", selectedSchemeId);
            obj.put("activityId", selectedActivityId);
//            obj.put("reportingLevelCode", "SHG");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "fishery-activity/search-shg")
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
    private final ArrayList<HHEntity> entriesArrayList = new ArrayList<>();
    private HouseHoldSpnAdapter houseHoldSpnAdapter;
    private void getHouseHoldList() {

        JSONObject object = new JSONObject();

        try {
//            object.put("searchTerm", searchTerm);
            object.put("schemeId", selectedSchemeId);
            object.put("activityId", selectedActivityId);
            object.put("entityId", selectedShgId);
            object.put("reportingLevelCode", entityTypeCode);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "fishery-activity/hh/list")
                .addHeaders("Authorization", "Bearer " + token)
                .setTag("Activity")
                .addJSONObjectBody(object)
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        if (Utility.isStringValid(response)) {
                            try {
                                JSONObject resObject = new JSONObject(response);
                                if (resObject.optBoolean("outcome")){
                                    JSONArray array = resObject.optJSONArray("data");

                                    if (array != null && array.length() > 0) {
                                        entriesArrayList.clear();
                                        HHEntity entries = new HHEntity();
                                        entries.entityName="Select Member";
                                        entries.entityId=0L;
                                        entriesArrayList.add(entries);
                                        for (int i = 0; i < array.length(); i++) {
                                            HHEntity e = HHEntity.parseHHEntity(array.optJSONObject(i));
                                            entriesArrayList.add(e);
                                        }
                                        houseHoldSpnAdapter.notifyDataSetChanged();
                                    }else {
                                        entriesArrayList.clear();
                                        HHEntity entries = new HHEntity();
                                        entries.entityName="Select Member";
                                        entries.entityId=0L;
                                        entriesArrayList.add(entries);
                                        houseHoldSpnAdapter.notifyDataSetChanged();
                                    }
                                }
                                if (resObject.optJSONArray("data") != null) {
//                                    String total = Objects.requireNonNull(resObject.optJSONObject("data")).optString("total");
//                                    mTvTotal.setText(getResources().getString(R.string.total) + getResources().getString(R.string.Rs) + " " + total);

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

    private void openDatePicker() {
        Calendar myCalendar = Calendar.getInstance();
        final DatePickerDialog datePickerDialog = new DatePickerDialog(AddFishHouseHoldActivity.this, new DatePickerDialog.OnDateSetListener() {
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

}