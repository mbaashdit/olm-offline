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
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.aashdit.olmoffline.BuildConfig;
import com.aashdit.olmoffline.adapters.ClusterSpnAdapter;
import com.aashdit.olmoffline.adapters.GoatshedSpnAdapter;
import com.aashdit.olmoffline.databinding.ActivityAddBirdClusterBinding;
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

public class AddBirdClusterActivity extends AppCompatActivity {

    private static final String TAG = "AddBirdClusterActivity";

    private ActivityAddBirdClusterBinding binding;
    private SharedPrefManager sp;
    private String token, entityTypeCode;
    private String noOfHouseHold, noOfGoat, noOfBuck, baseCapital, commencementDate, remarks;


    private final ArrayList<Cluster> shgArrayList = new ArrayList<>();
    private ClusterSpnAdapter shgSpnAdapter;

    private Spinner mSpnGoatShed;
    private GoatshedSpnAdapter goatshedSpnAdapter;
    private final ArrayList<GoatShed> goatShedArrayList = new ArrayList<>();

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

    private Long selectedSchemeId, selectedActivityId;


    private Long selectedShgId = 0L;
    private Long selectedGoatshedId = 0L;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddBirdClusterBinding.inflate(getLayoutInflater());
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
        shg.entityName="Select Cluster";
        shg.entityId=0L;
        shgArrayList.add(shg);

        GoatShed goatShed = new GoatShed();
        goatShed.setActive(true);
        goatShed.setValueEn("Type of Bird Shed");
        goatShed.setValueHi("Type of Bird Shed ");
        goatShed.setValueId(0L);
        goatShedArrayList.add(goatShed);

        goatshedSpnAdapter = new GoatshedSpnAdapter(this, goatShedArrayList);
        binding.spnAddClfTypeOfBirdShed.setAdapter(goatshedSpnAdapter);
        shgSpnAdapter = new ClusterSpnAdapter(this, shgArrayList);
        binding.spnAddClfSelect.setAdapter(shgSpnAdapter);
        getGoatShed();
//        getShgList();
        getClfList();

        binding.etCommenceDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDatePicker();
            }
        });


        binding.rlAddBirdClf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noOfHouseHold = binding.etHouseholdNo.getText().toString().trim();
                noOfBuck = binding.etBirdNo.getText().toString().trim();
                baseCapital = binding.etBaseCapital.getText().toString().trim();
                commencementDate = binding.etCommenceDate.getText().toString().trim();
                remarks = binding.etRemarks.getText().toString().trim();

                if (selectedShgId.equals(0L)){
                    Toast.makeText(AddBirdClusterActivity.this, "Please select a Cluster", Toast.LENGTH_SHORT).show();
                }else  if (noOfHouseHold.isEmpty()){
                    Toast.makeText(AddBirdClusterActivity.this, "Please fill No of Members", Toast.LENGTH_SHORT).show();
                }else  if (noOfBuck.isEmpty()){
                    Toast.makeText(AddBirdClusterActivity.this, "Please fill No of Buck", Toast.LENGTH_SHORT).show();
                }else  if (selectedGoatshedId.equals(0L)){
                    Toast.makeText(AddBirdClusterActivity.this, "Please select BirdShed", Toast.LENGTH_SHORT).show();
                }else  if (baseCapital.isEmpty()){
                    Toast.makeText(AddBirdClusterActivity.this, "Please fill Base Capital", Toast.LENGTH_SHORT).show();
                }else  if (commencementDate.isEmpty()){
                    Toast.makeText(AddBirdClusterActivity.this, "Please fill Commencement Date", Toast.LENGTH_SHORT).show();
                }else {
                    doSubmitClfActivity();
                }
            }
        });

        binding.spnAddClfSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    selectedShgId = 0L;
                } else {
                    selectedShgId = shgArrayList.get(position).entityId;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        binding.spnAddClfTypeOfBirdShed.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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


    }

    private void doSubmitClfActivity() {
        binding.progreses.setVisibility(View.VISIBLE);
        JSONObject obj = new JSONObject();
        try {
            obj.put("baseCapital",baseCapital);
            obj.put("commencementDate",commencementDate);
            obj.put("remarks",remarks);
            obj.put("shedTypeId",selectedGoatshedId);
            obj.put("numBirds",noOfBuck);
            obj.put("entityTypeCode",entityTypeCode);
            obj.put("entityId",selectedShgId);
            obj.put("numHousehold",noOfHouseHold);
            obj.put("schemeId",selectedSchemeId);
            obj.put("activityId",selectedActivityId);
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

                                    Toast.makeText(AddBirdClusterActivity.this,resObj.optString("message"), Toast.LENGTH_LONG)
                                            .show();
                                }else{
                                    Toast.makeText(AddBirdClusterActivity.this,resObj.optString("message")+"", Toast.LENGTH_LONG)
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

//    final Calendar myCalendar = Calendar.getInstance();

    private void openDatePicker() {
        Calendar myCalendar = Calendar.getInstance();
        final DatePickerDialog datePickerDialog = new DatePickerDialog(AddBirdClusterActivity.this, new DatePickerDialog.OnDateSetListener() {
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

    private void getClfList() {

        JSONObject obj = new JSONObject();
        try {
            obj.put("schemeId", selectedSchemeId);
            obj.put("activityId", selectedActivityId);
            obj.put("reportingLevelCode", "CLF");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "poultry-activity/clf/list")
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

}