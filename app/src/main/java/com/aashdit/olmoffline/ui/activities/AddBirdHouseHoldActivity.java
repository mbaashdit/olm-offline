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
import com.aashdit.olmoffline.databinding.ActivityAddBirdHouseHoldBinding;
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

public class AddBirdHouseHoldActivity extends AppCompatActivity {

    private static final String TAG = "AddBirdHouseHoldActivit";
    private final ArrayList<GoatShed> goatShedArrayList = new ArrayList<>();
    private final String searchTerm = "";
    private final String reportingLevelCode = "HH";
    private ActivityAddBirdHouseHoldBinding binding;
    private SharedPrefManager sp;
    private String token, shgName, entityTypeCode;
    private Long selectedShgId = 0L;
    private Long selectedHhId = 0L;
    private Long selectedGoatshedId = 0L;
    private GoatshedSpnAdapter goatshedSpnAdapter;
    private String noOfHouseHold, noOfGoat, noOfBuck, baseCapital, commencementDate, remarks;

    private String intentType, entityName;
    private Long poultryRegistrationId;

    private Long selectedScheme;
    private Long selectedActivity;
    private ArrayList<SHG> shgArrayList;

    private ShgSpnAdapter shgSpnAdapter;
    private ArrayList<HHEntity> entriesArrayList;
    private HouseHoldSpnAdapter houseHoldSpnAdapter;//ShgHHSpnAdapter

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
        binding = ActivityAddBirdHouseHoldBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        resetTitles();
        setSupportActionBar(binding.toolbarHhList);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        sp = SharedPrefManager.getInstance(this);
        token = sp.getStringData(Constant.APP_TOKEN);

        selectedScheme = getIntent().getLongExtra("SCHEME_ID", 0L);
        selectedActivity = getIntent().getLongExtra("ACTIVITY_ID", 0L);
        binding.progreses.setVisibility(View.GONE);
        entityName = getIntent().getStringExtra("ENTITY_NAME");
        selectedHhId = getIntent().getLongExtra("ENTITY_ID", 0L);
        shgName = getIntent().getStringExtra("SHG_NAME");
        entityTypeCode = getIntent().getStringExtra("ENTITY_TYPE_CODE");
        intentType = getIntent().getStringExtra(Constant.INTENT_TYPE);

        if (entityTypeCode.equals("HH")) {
            binding.tvToolbarTitleOne.setText(intentType.equals(Constant.ADD) ?
                    getResources().getString(R.string.add_hh_activity) : "Update Member Activity");
        }

        SHG shg = new SHG();
        shg.setShgDetailsId(0L);
        shg.setShgName("Select a SHG");
        shg.setShgRegNumber("0");

        shgArrayList = new ArrayList<>();
        shgArrayList.add(shg);

        entriesArrayList = new ArrayList<>();
        getShgList();
        shgSpnAdapter = new ShgSpnAdapter(this, shgArrayList);
        binding.spnAddHhSelectShg.setAdapter(shgSpnAdapter);
        binding.spnAddHhSelectShg.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    selectedShgId = 0L;
                    resetMembersData();
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
        binding.spnAddHhTypeOfBirdShed.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    selectedGoatshedId = 0L;
                } else {
                    selectedGoatshedId = goatShedArrayList.get(position).getValueId();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        binding.spnAddHhSelectHh.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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

        HHEntity entries = new HHEntity();
        entries.entityName = "Select Member";
        entries.entityId = 0L;
        entriesArrayList.add(entries);
        houseHoldSpnAdapter = new HouseHoldSpnAdapter(this, entriesArrayList);
        binding.spnAddHhSelectHh.setAdapter(houseHoldSpnAdapter);

        GoatShed goatShed = new GoatShed();
        goatShed.setActive(true);
        goatShed.setValueEn("Type of Bird Shed");
        goatShed.setValueHi("Type of Bird Shed ");
        goatShed.setValueId(0L);
        goatShedArrayList.add(goatShed);

        goatshedSpnAdapter = new GoatshedSpnAdapter(this, goatShedArrayList);
        binding.spnAddHhTypeOfBirdShed.setAdapter(goatshedSpnAdapter);
        getGoatShed();


        binding.etCommenceDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDatePicker();
            }
        });

        binding.addHhSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                noOfHouseHold = binding.etHouseholdNo.getText().toString().trim();
                noOfBuck = binding.etBirdNo.getText().toString().trim();
                baseCapital = binding.etBaseCapital.getText().toString().trim();
                commencementDate = binding.etCommenceDate.getText().toString().trim();
                remarks = binding.etRemarks.getText().toString().trim();


                if (intentType.equals(Constant.ADD) && selectedShgId.equals(0L)) {
                    Toast.makeText(AddBirdHouseHoldActivity.this, "Please select a SHG", Toast.LENGTH_SHORT).show();
                } else if (intentType.equals(Constant.ADD) && selectedHhId.equals(0L)) {
                    Toast.makeText(AddBirdHouseHoldActivity.this, "Please select a Member", Toast.LENGTH_SHORT).show();
                } else if (noOfBuck.isEmpty()) {
                    Toast.makeText(AddBirdHouseHoldActivity.this, "Please fill No of Birds", Toast.LENGTH_SHORT).show();
                } else if (selectedGoatshedId.equals(0L)) {
                    Toast.makeText(AddBirdHouseHoldActivity.this, "Please select BirdShed", Toast.LENGTH_SHORT).show();
                } else if (baseCapital.isEmpty()) {
                    Toast.makeText(AddBirdHouseHoldActivity.this, "Please fill Base Capital", Toast.LENGTH_SHORT).show();
                } else if (commencementDate.isEmpty()) {
                    Toast.makeText(AddBirdHouseHoldActivity.this, "Please fill Commencement Date", Toast.LENGTH_SHORT).show();
                } else {
                    doSubmitHHActivity();
                }
            }
        });

        if (intentType.equals(Constant.UPDATE)) {
            binding.ivEdit.setVisibility(View.VISIBLE);
            binding.tvHhSelectShgLbl.setVisibility(View.GONE);
            binding.tvBirdShgName.setText(shgName);
            binding.spnAddHhSelectShg.setVisibility(View.GONE);
            binding.spnAddHhSelectHh.setVisibility(View.GONE);

            binding.tvHhSelectHhLbl.setVisibility(View.GONE);
            binding.tvBirdHhName.setText(entityName);

            disableFields();
            getViewReg();

            binding.ivEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    binding.ivEdit.setColorFilter(ContextCompat.getColor(AddBirdHouseHoldActivity.this, R.color.purple_500), android.graphics.PorterDuff.Mode.MULTIPLY);
                    enableFields();
                }
            });
        } else {
            binding.ivEdit.setVisibility(View.GONE);
        }

    }


    private void disableFields() {
        binding.etBirdNo.setEnabled(false);
        binding.etBirdNo.setClickable(false);
        binding.etRemarks.setEnabled(false);
        binding.etRemarks.setClickable(false);
        binding.etCommenceDate.setEnabled(false);
        binding.etCommenceDate.setClickable(false);
        binding.etBaseCapital.setEnabled(false);
        binding.etBaseCapital.setClickable(false);

        binding.spnAddHhSelectHh.setEnabled(false);
        binding.spnAddHhSelectShg.setEnabled(false);
        binding.spnAddHhTypeOfBirdShed.setEnabled(false);
        binding.addHhSubmit.setEnabled(false);
        binding.addHhSubmit.setClickable(false);

    }

    private void enableFields() {
        binding.etBirdNo.setEnabled(true);
        binding.etRemarks.setEnabled(true);
        binding.etCommenceDate.setEnabled(true);
        binding.etBaseCapital.setEnabled(true);

        binding.spnAddHhSelectHh.setEnabled(true);
        binding.spnAddHhSelectShg.setEnabled(true);
        binding.spnAddHhTypeOfBirdShed.setEnabled(true);
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
            obj.put("schemeId", selectedScheme);
            obj.put("activityId", selectedActivity);
            obj.put("entityId", selectedHhId);
            obj.put("reportingLevelCode", "HH");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "poultry-activity/view-reg")
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

                                    poultryRegistrationId = resObj.optLong("poultryRegistrationId");
                                    noOfHouseHold = String.valueOf(resObj.optLong("numHousehold"));
                                    baseCapital = resObj.optString("baseCapital").replace(",", "");

                                    commencementDate = resObj.optString("commencementDate");
                                    remarks = resObj.optString("remarks");
                                    selectedGoatshedId = resObj.optLong("shedTypeId");
                                    noOfBuck = String.valueOf(resObj.optLong("numBirds"));
//                                    bucksTied = resObj.optBoolean("bucksTied");

                                    binding.etBaseCapital.setText(baseCapital);
                                    binding.etCommenceDate.setText(commencementDate);
                                    binding.etRemarks.setText(remarks);
                                    binding.etBirdNo.setText(noOfBuck);
//                                    binding.etHouseholdNo.setText(noOfHouseHold);


                                    if (intentType.equals(Constant.UPDATE)) {
                                        for (int i = 0; i < goatShedArrayList.size(); i++) {
                                            if (selectedGoatshedId.equals(goatShedArrayList.get(i).getValueId())) {
                                                binding.spnAddHhTypeOfBirdShed.setSelection(i);
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

//    final Calendar myCalendar = Calendar.getInstance();

    private void resetMembersData() {
        entriesArrayList.clear();
        HHEntity entries = new HHEntity();
        entries.entityName = "Select Member";
        entries.entityId = 0L;
        entriesArrayList.add(entries);

        houseHoldSpnAdapter.notifyDataSetChanged();
    }

    private void doSubmitHHActivity() {
        binding.progreses.setVisibility(View.VISIBLE);
        JSONObject obj = new JSONObject();
        try {
            if (intentType.equals(Constant.UPDATE)) {
                obj.put("poultryRegistrationId", poultryRegistrationId);
            }
            obj.put("baseCapital", baseCapital);
            obj.put("commencementDate", commencementDate);
            obj.put("remarks", remarks);
            obj.put("shedTypeId", selectedGoatshedId);
            obj.put("numBirds", noOfBuck);
            obj.put("entityTypeCode", "HH");
            obj.put("entityId", selectedHhId);
            obj.put("numHousehold", "0");
            obj.put("schemeId", selectedScheme);
            obj.put("activityId", selectedActivity);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "poultry-activity/save")
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
                                    intent.putExtra("schemeId", selectedScheme);
                                    intent.putExtra("activityId", selectedActivity);
                                    setResult(RESULT_OK, intent);
                                    finish();

                                    Toast.makeText(AddBirdHouseHoldActivity.this, resObj.optString("message"), Toast.LENGTH_LONG)
                                            .show();
                                } else {
                                    Toast.makeText(AddBirdHouseHoldActivity.this, resObj.optString("message") + "", Toast.LENGTH_LONG)
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
                                        goatShed.setValueEn("Type of Bird Shed");
                                        goatShed.setValueHi("Type of Bird Shed ");
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

    private void getShgList() {
        binding.progreses.setVisibility(View.VISIBLE);
        JSONObject object = new JSONObject();
        try {
            object.put("schemeId", selectedScheme);
            object.put("activityId", selectedActivity);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "poultry-activity/search-shg")
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

    private void openDatePicker() {
        Calendar myCalendar = Calendar.getInstance();
        final DatePickerDialog datePickerDialog = new DatePickerDialog(AddBirdHouseHoldActivity.this, new DatePickerDialog.OnDateSetListener() {
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

    private void getHouseHoldList() {

        JSONObject object = new JSONObject();

        try {
//            object.put("searchTerm", searchTerm);
            object.put("schemeId", selectedScheme);
            object.put("activityId", selectedActivity);
            object.put("entityId", selectedShgId);
            object.put("reportingLevelCode", reportingLevelCode);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "poultry-activity/hh/list")
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