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
import com.aashdit.olmoffline.adapters.GoatshedSpnAdapter;
import com.aashdit.olmoffline.adapters.HouseHoldSpnAdapter;
import com.aashdit.olmoffline.adapters.ShgSpnAdapter;
import com.aashdit.olmoffline.databinding.ActivityAddDiaryHouseHoldBinding;
import com.aashdit.olmoffline.models.GoatShed;
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

public class AddDiaryHouseHoldActivity extends AppCompatActivity {

    private static final String TAG = "AddDiaryHouseHoldActivi";
    private final ArrayList<GoatShed> goatShedArrayList = new ArrayList<>();
    private final String searchTerm = "";
    private final String reportingLevelCode = "HH";
    private ActivityAddDiaryHouseHoldBinding binding;
    private Long selectedSchemeId, selectedActivityId;

    private String token, entityTypeCode;
    private SharedPrefManager sp;

    private String intentType, entityName, shgName;
    private Long selectedShgId = 0L;
    private Long selectedHhId = 0L;
    private Long selectedCowshedId = 0L;
    private Long dairyRegistrationId = 0L;
    private ArrayList<SHG> shgArrayList;
    private ShgSpnAdapter shgSpnAdapter;
    private ArrayList<HHEntity> entriesArrayList;
    private String noOfCows, noOfCalf, noOfBuffalo, noOfBuffaloCalf, baseCapital, commencementDate, remarks;
    private HouseHoldSpnAdapter houseHoldSpnAdapter;//ShgHHSpnAdapter
    private GoatshedSpnAdapter goatshedSpnAdapter;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
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

    private void getShgList() {
        binding.progreses.setVisibility(View.VISIBLE);
        JSONObject object = new JSONObject();
        try {
            object.put("schemeId", selectedSchemeId);
            object.put("activityId", selectedActivityId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "dairy-activity/search-shg")
                .addHeaders("Authorization", "Bearer " + token)
                .addJSONObjectBody(object)
                .setTag("Schemes")
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        if (Utility.isStringValid(response)) {
                            binding.progreses.setVisibility(View.GONE);
                            try {
                                JSONObject object = new JSONObject(response);
                                if (object.optBoolean("outcome")) {
                                    JSONArray array = object.optJSONArray("data");
                                    if (array != null && array.length() > 0) {
                                        shgArrayList.clear();
                                        SHG shg = new SHG();
                                        shg.setShgDetailsId(0L);
                                        shg.setShgName("Select a SHG");
                                        shg.setShgRegNumber("0");
                                        shgArrayList.add(shg);

                                        for (int i = 0; i < array.length(); i++) {
                                            SHG shg1 = SHG.parseShg(array.optJSONObject(i));
                                            shgArrayList.add(shg1);
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

    private void getHouseHoldList() {

        JSONObject object = new JSONObject();

        try {
//            object.put("searchTerm", searchTerm);
            object.put("schemeId", selectedSchemeId);
            object.put("activityId", selectedActivityId);
            object.put("entityId", selectedShgId);
            object.put("reportingLevelCode", reportingLevelCode);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "dairy-activity/hh/list")
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
                                if (resObject.optBoolean("outcome")) {
                                    JSONArray array = resObject.optJSONArray("data");

                                    if (array != null && array.length() > 0) {
                                        entriesArrayList.clear();
                                        HHEntity entries = new HHEntity();
                                        entries.entityName = "Select Member";
                                        entries.entityId = 0L;
                                        entriesArrayList.add(entries);
                                        for (int i = 0; i < array.length(); i++) {
                                            HHEntity e = HHEntity.parseHHEntity(array.optJSONObject(i));
                                            entriesArrayList.add(e);
                                        }
                                        houseHoldSpnAdapter.notifyDataSetChanged();
                                    } else {
                                        entriesArrayList.clear();
                                        HHEntity entries = new HHEntity();
                                        entries.entityName = "Select Member";
                                        entries.entityId = 0L;
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

    private void getGoatShed() {
        AndroidNetworking.get(BuildConfig.BASE_URL + Constant.API_PATH + "lov/goatShed")
                .addHeaders("Authorization", "Bearer " + token)
                .setTag("goatShed")
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
//                                        schemes.clear();
                                        goatShedArrayList.clear();
                                        GoatShed goatShed = new GoatShed();
                                        goatShed.setActive(true);
                                        goatShed.setValueEn("Type of Cow Shed");
                                        goatShed.setValueHi("Type of Cow Shed ");
                                        goatShed.setValueId(0L);
                                        goatShedArrayList.add(goatShed);
                                        for (int i = 0; i < array.length(); i++) {
                                            GoatShed scheme = GoatShed.parseGoatShed(array.optJSONObject(i));
                                            goatShedArrayList.add(scheme);
                                        }

                                        goatshedSpnAdapter.notifyDataSetChanged();

                                        if (intentType.equals(Constant.UPDATE)) {
                                            getViewReg();
                                        }
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddDiaryHouseHoldBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        resetTitles();
        setSupportActionBar(binding.toolbarHhList);
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
        binding.progreses.setVisibility(View.GONE);
        binding.tvDairyShgName.setText(shgName);
        binding.tvToolbarTitleOne.setText(intentType.equals(Constant.ADD) ?
                getResources().getString(R.string.add_member_activity) : "Update Member Activity");

        SHG shg = new SHG();
        shg.setShgDetailsId(0L);
        shg.setShgName("Select a SHG");
        shg.setShgRegNumber("0");

        shgArrayList = new ArrayList<>();
        shgArrayList.add(shg);

        entriesArrayList = new ArrayList<>();
        getShgList();
        getGoatShed();
        shgSpnAdapter = new ShgSpnAdapter(this, shgArrayList);
        binding.spnSelectShg.setAdapter(shgSpnAdapter);
        binding.spnSelectShg.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    resetMembersData();
                    selectedShgId = 0L;
                } else {
                    selectedShgId = shgArrayList.get(position).getShgDetailsId();
                    resetMembersData();
                    getHouseHoldList();
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        HHEntity entries = new HHEntity();
        entries.entityName = "Select Member";
        entries.entityId = 0L;
        entriesArrayList.add(entries);

        houseHoldSpnAdapter = new HouseHoldSpnAdapter(this, entriesArrayList);
        binding.spnDairyHh.setAdapter(houseHoldSpnAdapter);
        binding.spnDairyHh.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    selectedHhId = 0L;
                } else {
                    selectedHhId = entriesArrayList.get(position).entityId;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        GoatShed goatShed = new GoatShed();
        goatShed.setActive(true);
        goatShed.setValueEn("Type of Cow Shed");
        goatShed.setValueHi("Type of Cow Shed ");
        goatShed.setValueId(0L);
        goatShedArrayList.add(goatShed);

        goatshedSpnAdapter = new GoatshedSpnAdapter(this, goatShedArrayList);
        binding.spnAddCowShed.setAdapter(goatshedSpnAdapter);
        binding.spnAddCowShed.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    selectedCowshedId = 0L;
                } else {
                    selectedCowshedId = goatShedArrayList.get(position).getValueId();
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
        binding.addHhSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                noOfCows = binding.etCowsNo.getText().toString().trim();
                noOfCalf = binding.etCalfNo.getText().toString().trim();
                baseCapital = binding.etBaseCapital.getText().toString().trim();
                commencementDate = binding.etCommenceDate.getText().toString().trim();
                remarks = binding.etRemarks.getText().toString().trim();

                if (intentType.equals(Constant.ADD) && selectedShgId.equals(0L)) {
                    Toast.makeText(AddDiaryHouseHoldActivity.this, "Please select a SHG", Toast.LENGTH_SHORT).show();
                } else if (intentType.equals(Constant.ADD) && selectedHhId.equals(0L)) {
                    Toast.makeText(AddDiaryHouseHoldActivity.this, "Please select a Member", Toast.LENGTH_SHORT).show();
                } else if (noOfCows.isEmpty()) {
                    Toast.makeText(AddDiaryHouseHoldActivity.this, "Please fill No of Cows / Buffaloes", Toast.LENGTH_SHORT).show();
                } else if (noOfCalf.isEmpty()) {
                    Toast.makeText(AddDiaryHouseHoldActivity.this, "Please fill No of Calf", Toast.LENGTH_SHORT).show();
                } else if (selectedCowshedId.equals(0L)) {
                    Toast.makeText(AddDiaryHouseHoldActivity.this, "Please select Cow Shed", Toast.LENGTH_SHORT).show();
                } else if (baseCapital.isEmpty()) {
                    Toast.makeText(AddDiaryHouseHoldActivity.this, "Please fill Base Capital", Toast.LENGTH_SHORT).show();
                } else if (commencementDate.isEmpty()) {
                    Toast.makeText(AddDiaryHouseHoldActivity.this, "Please fill Commencement Date", Toast.LENGTH_SHORT).show();
                } else {
                    doSubmitHHActivity();
                }
            }
        });

        if (intentType.equals(Constant.UPDATE)) {
            getViewReg();
            disableFields();

            binding.spnSelectShg.setVisibility(View.GONE);
            binding.tvSelectShgLbl.setVisibility(View.GONE);
            binding.tvDairyShgName.setVisibility(View.VISIBLE);
            binding.tvDairyShgName.setText(shgName);

            binding.spnDairyHh.setVisibility(View.GONE);
            binding.tvHhLbl.setVisibility(View.GONE);

            binding.tvDairyMemberName.setText(entityName);


            binding.ivEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    binding.ivEdit.setColorFilter(ContextCompat.getColor(AddDiaryHouseHoldActivity.this, R.color.purple_500), android.graphics.PorterDuff.Mode.MULTIPLY);
                    enableFields();
                }
            });
        } else {
//            binding.spnAddShgSelectShg.setVisibility(View.VISIBLE);
            binding.tvDairyShgName.setVisibility(View.GONE);
            binding.ivEdit.setVisibility(View.GONE);
        }
    }


    private void disableFields() {
//        binding.etHouseholdNo.setEnabled(false);
//        binding.etHouseholdNo.setClickable(false);
        binding.etCowsNo.setEnabled(false);
        binding.etCowsNo.setClickable(false);
        binding.etCalfNo.setEnabled(false);
        binding.etCalfNo.setClickable(false);
        binding.etRemarks.setEnabled(false);
        binding.etRemarks.setClickable(false);
        binding.etCommenceDate.setEnabled(false);
        binding.etCommenceDate.setClickable(false);
        binding.etBaseCapital.setEnabled(false);
        binding.etBaseCapital.setClickable(false);
        binding.spnAddCowShed.setEnabled(false);
//        binding.spnAddShgSelectShg.setEnabled(false);

        binding.addHhSubmit.setEnabled(false);
        binding.addHhSubmit.setClickable(false);
    }

    private void enableFields() {
//        binding.etHouseholdNo.setEnabled(true);
        binding.etCowsNo.setEnabled(true);
        binding.etCalfNo.setEnabled(true);
        binding.etRemarks.setEnabled(true);
        binding.etCommenceDate.setEnabled(true);
        binding.etBaseCapital.setEnabled(true);
        binding.spnAddCowShed.setEnabled(true);
//        binding.spnAddShgSelectShg.setEnabled(true);

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

        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "dairy-activity/view-reg")
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

                                    dairyRegistrationId = resObj.optLong("dairyRegistrationId");
//                                    noOfHouseHold = String.valueOf(resObj.optLong("numHousehold"));
                                    baseCapital = resObj.optString("baseCapital").replace(",", "");

                                    commencementDate = resObj.optString("commencementDate");
                                    remarks = resObj.optString("remarks");
                                    selectedCowshedId = resObj.optLong("shedTypeId");
//                                    selectedShgId = resObj.optLong("entityId");
                                    noOfCows = String.valueOf(resObj.optLong("numCow"));
                                    noOfCalf = String.valueOf(resObj.optLong("numCalf"));
//                                    isBucktied = resObj.optBoolean("bucksTied");
//
                                    binding.etBaseCapital.setText(baseCapital);
                                    binding.etCommenceDate.setText(commencementDate);
                                    binding.etRemarks.setText(remarks);
//                                    binding.etHouseholdNo.setText(noOfHouseHold);
                                    binding.etCowsNo.setText(noOfCows);
                                    binding.etCalfNo.setText(noOfCalf);
//                                    binding.tvDairyShgName.setText(entityName);
//
//
                                    if (intentType.equals(Constant.UPDATE)) {
                                        for (int i = 0; i < goatShedArrayList.size(); i++) {
                                            if (selectedCowshedId.equals(goatShedArrayList.get(i).getValueId())) {
                                                binding.spnAddCowShed.setSelection(i);
                                            }
                                            goatshedSpnAdapter.notifyDataSetChanged();
                                        }
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

    private void resetMembersData() {
        entriesArrayList.clear();
        HHEntity entries = new HHEntity();
        entries.entityName = "Select Member";
        entries.entityId = 0L;
        entriesArrayList.add(entries);

        houseHoldSpnAdapter.notifyDataSetChanged();
    }

    private void openDatePicker() {
        Calendar myCalendar = Calendar.getInstance();
        final DatePickerDialog datePickerDialog = new DatePickerDialog(AddDiaryHouseHoldActivity.this, new DatePickerDialog.OnDateSetListener() {
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


    private void doSubmitHHActivity() {
        binding.progreses.setVisibility(View.VISIBLE);
        JSONObject obj = new JSONObject();
        try {
            if (intentType.equals(Constant.UPDATE)){
                obj.put("dairyRegistrationId",dairyRegistrationId);
            }
            obj.put("baseCapital", baseCapital);
            obj.put("commencementDate", commencementDate);
            obj.put("remarks", remarks);
            obj.put("shedTypeId", selectedCowshedId);
            obj.put("numCow", noOfCows);
            obj.put("numCalf", noOfCalf);
            obj.put("entityTypeCode", entityTypeCode);
            obj.put("entityId", selectedHhId);
            obj.put("numHousehold", "0");
            obj.put("schemeId", selectedSchemeId);
            obj.put("activityId", selectedActivityId);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "dairy-activity/save")
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

                                    Toast.makeText(AddDiaryHouseHoldActivity.this, resObj.optString("message"), Toast.LENGTH_LONG)
                                            .show();
                                } else {
                                    Toast.makeText(AddDiaryHouseHoldActivity.this, resObj.optString("message") + "", Toast.LENGTH_LONG)
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
                    }
                });


    }
}