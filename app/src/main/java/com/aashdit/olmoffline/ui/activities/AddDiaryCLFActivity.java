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

import com.aashdit.olmoffline.BuildConfig;
import com.aashdit.olmoffline.adapters.ClusterSpnAdapter;
import com.aashdit.olmoffline.adapters.GoatshedSpnAdapter;
import com.aashdit.olmoffline.databinding.ActivityAddDiaryCLFBinding;
import com.aashdit.olmoffline.models.Cluster;
import com.aashdit.olmoffline.models.GoatShed;
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

public class AddDiaryCLFActivity extends AppCompatActivity {

    private static final String TAG = "AddDiaryCLFActivity";
    private ActivityAddDiaryCLFBinding binding;

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

    private Long selectedSchemeId, selectedActivityId;

    private String token, entityTypeCode;
    private SharedPrefManager sp;
    private GoatshedSpnAdapter goatshedSpnAdapter;
    private final ArrayList<GoatShed> goatShedArrayList = new ArrayList<>();
    private final ArrayList<Cluster> shgArrayList = new ArrayList<>();
    private ClusterSpnAdapter shgSpnAdapter;

    private String noOfHouseHold, noOfCow, noOfCalf, noOfBuffaloes, noOfBuffaloesCalf, baseCapital,
            commencementDate, remarks;
    private Long selectedClusterId = 0L;
    private Long selectedDairyshedId = 0L;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddDiaryCLFBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        resetTitles();

        setSupportActionBar(binding.toolbarClfList);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        sp = SharedPrefManager.getInstance(this);
        token = sp.getStringData(Constant.APP_TOKEN);

        selectedSchemeId = getIntent().getLongExtra("SCHEME_ID", 0L);
        selectedActivityId = getIntent().getLongExtra("ACTIVITY_ID", 0L);
        entityTypeCode = getIntent().getStringExtra("ENTITY_TYPE_CODE");
        binding.progreses.setVisibility(View.GONE);

        Cluster shg = new Cluster();
        shg.entityName = "Select Cluster";
        shg.entityId = 0L;
        shgArrayList.add(shg);

        GoatShed goatShed = new GoatShed();
        goatShed.setActive(true);
        goatShed.setValueEn("Type of Cow Shed");
        goatShed.setValueHi("Type of Cow Shed ");
        goatShed.setValueId(0L);
        goatShedArrayList.add(goatShed);

        goatshedSpnAdapter = new GoatshedSpnAdapter(this, goatShedArrayList);
        binding.spnAddClfSelect.setAdapter(goatshedSpnAdapter);
        binding.spnAddClfSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    selectedDairyshedId = 0L;
                } else {
                    selectedDairyshedId = goatShedArrayList.get(position).getValueId();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        shgSpnAdapter = new ClusterSpnAdapter(this, shgArrayList);
        binding.spnAddShgSelectClf.setAdapter(shgSpnAdapter);
        binding.spnAddShgSelectClf.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    selectedClusterId = 0L;
                } else {
                    selectedClusterId = shgArrayList.get(position).entityId;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        getCowShed();
        getClfList();
        binding.etCommenceDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDatePicker();
            }
        });

        binding.addClfSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noOfHouseHold = binding.etHouseholdNo.getText().toString().trim();
                noOfCow = binding.etCowsNo.getText().toString().trim();
                noOfCalf = binding.etCalfNo.getText().toString().trim();
                baseCapital = binding.etBaseCapital.getText().toString().trim();
                commencementDate = binding.etCommenceDate.getText().toString().trim();
                remarks = binding.etRemarks.getText().toString().trim();

                if (selectedClusterId.equals(0L)){
                    Toast.makeText(AddDiaryCLFActivity.this, "Please select a Cluster", Toast.LENGTH_SHORT).show();
                }else  if (noOfHouseHold.isEmpty()){
                    Toast.makeText(AddDiaryCLFActivity.this, "Please fill No of Members", Toast.LENGTH_SHORT).show();
                }else  if (noOfCow.isEmpty()){
                    Toast.makeText(AddDiaryCLFActivity.this, "Please fill No of Cows", Toast.LENGTH_SHORT).show();
                }else  if (noOfCalf.isEmpty()){
                    Toast.makeText(AddDiaryCLFActivity.this, "Please fill No of Calf", Toast.LENGTH_SHORT).show();
                }else  if (selectedDairyshedId.equals(0L)){
                    Toast.makeText(AddDiaryCLFActivity.this, "Please select Cow Shed", Toast.LENGTH_SHORT).show();
                }else  if (baseCapital.isEmpty()){
                    Toast.makeText(AddDiaryCLFActivity.this, "Please fill Base Capital", Toast.LENGTH_SHORT).show();
                }else  if (commencementDate.isEmpty()){
                    Toast.makeText(AddDiaryCLFActivity.this, "Please fill Commencement Date", Toast.LENGTH_SHORT).show();
                }else {
                    doSubmitClfActivity();
                }
            }
        });
    }

    private void doSubmitClfActivity() {


        binding.progreses.setVisibility(View.VISIBLE);
        JSONObject obj = new JSONObject();
        try {
            obj.put("baseCapital", baseCapital);
            obj.put("commencementDate", commencementDate);
            obj.put("remarks", remarks);
            obj.put("shedTypeId", selectedDairyshedId);
            obj.put("numCow", noOfCow);
            obj.put("numCalf", noOfCalf);
            obj.put("entityTypeCode", entityTypeCode);
            obj.put("entityId", selectedClusterId);
            obj.put("numHousehold", noOfHouseHold);
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
                        if (Utility.isStringValid(response)){
                            try {
                                JSONObject resObj = new JSONObject(response);

                                if (resObj.optBoolean("outcome")){

                                    Intent intent = getIntent();
                                    intent.putExtra("res", true);
                                    intent.putExtra("schemeId", selectedSchemeId);
                                    intent.putExtra("activityId", selectedActivityId);
                                    setResult(RESULT_OK, intent);
                                    finish();

                                    Toast.makeText(AddDiaryCLFActivity.this,resObj.optString("message"), Toast.LENGTH_LONG)
                                            .show();
                                }else{
                                    Toast.makeText(AddDiaryCLFActivity.this,resObj.optString("message")+"", Toast.LENGTH_LONG)
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
                        Log.e(TAG, "onError: "+anError.getErrorDetail() );
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

    private void getCowShed() {
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
//                                            if (!scheme.isLotteryDone) {
                                            goatShedArrayList.add(scheme);
//                                            }
                                        }

                                        goatshedSpnAdapter.notifyDataSetChanged();
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

    private void getClfList() {

        JSONObject obj = new JSONObject();
        try {
            obj.put("schemeId", selectedSchemeId);
            obj.put("activityId", selectedActivityId);
            obj.put("reportingLevelCode", "CLF");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "dairy-activity/clf/list")
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
                                        Cluster shg = new Cluster();
                                        shg.entityName = "Select Cluster";
                                        shg.entityId = 0L;
                                        shgArrayList.add(shg);
                                        for (int i = 0; i < array.length(); i++) {
                                            Cluster scheme = Cluster.parseClusterData(array.optJSONObject(i));
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

    private void openDatePicker() {
        Calendar myCalendar = Calendar.getInstance();
        final DatePickerDialog datePickerDialog = new DatePickerDialog(AddDiaryCLFActivity.this, new DatePickerDialog.OnDateSetListener() {
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