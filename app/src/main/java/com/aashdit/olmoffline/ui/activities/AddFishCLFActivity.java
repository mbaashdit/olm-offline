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
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.aashdit.olmoffline.BuildConfig;
import com.aashdit.olmoffline.R;
import com.aashdit.olmoffline.adapters.ClusterSpnAdapter;
import com.aashdit.olmoffline.databinding.ActivityAddFishCLFBinding;
import com.aashdit.olmoffline.models.Cluster;
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

public class AddFishCLFActivity extends AppCompatActivity {

    private static final String TAG = "AddFishCLFActivity";
    private ActivityAddFishCLFBinding binding;

    private String token, entityTypeCode;
    private SharedPrefManager sp;

    private Long selectedSchemeId, selectedActivityId;
    private final ArrayList<Cluster> shgArrayList = new ArrayList<>();
    private ClusterSpnAdapter shgSpnAdapter;

    private String noOfHouseHold = "";
    private String baseCapital = "";
    private String qtyFish;
    private String pondCleaningFreq = "";
    private final String noOfPondsOwned = "0";
    private String noOfPondsLeased = "";
    private String areaOfPonds = "";
    private String nettingFreq = "";
    private String totFryStock = "";
    private String totFingerlingStock = "";
    private String totYearlingStock = "";
    private String feedFry = "";
    private String feedFingerling = "";
    private String feedYearling = "";
    private String qtyOfFingerling = "";
    private String qtyOfYearling = "";
    private String qtyOfFry = "";
    private String commencementDate = "";
    private String remarks = "";
    private boolean isRegularNetting = false;
    private boolean isFeedMgnt = false;

    private Long selectedClfId = 0L;

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
        binding = ActivityAddFishCLFBinding.inflate(getLayoutInflater());
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
        binding.progreses.setVisibility(View.GONE);
        if (entityTypeCode.equals("SHG")) {
            binding.tvFishTitle.setText(getResources().getString(R.string.add_shg_activity));
        } else if (entityTypeCode.equals("HH")) {
            binding.tvFishTitle.setText(getResources().getString(R.string.add_hh_activity));
        } else if (entityTypeCode.equals("CLF")) {
            binding.tvFishTitle.setText(getResources().getString(R.string.add_clf_activity));
        }

        Cluster shg = new Cluster();
        shg.entityName = "Select Cluster";
        shg.entityId = 0L;
        shgArrayList.add(shg);
        getClfList();
        shgSpnAdapter = new ClusterSpnAdapter(this, shgArrayList);
        binding.spnAddShgFishSelectShg.setAdapter(shgSpnAdapter);
        binding.spnAddShgFishSelectShg.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    selectedClfId = 0L;
                } else {
                    selectedClfId = shgArrayList.get(position).entityId;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        binding.cvNettingContainer.setVisibility(View.GONE);
        binding.swtNettingMgnt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isRegularNetting = isChecked;
                if (isChecked) {
                    binding.cvNettingContainer.setVisibility(View.VISIBLE);
                } else {
                    binding.cvNettingContainer.setVisibility(View.GONE);
                }
            }
        });

        binding.cvFeedContainer.setVisibility(View.GONE);
        binding.swtFeedMgnt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isFeedMgnt = isChecked;
                if (isChecked){
                    binding.cvFeedContainer.setVisibility(View.VISIBLE);
                }else{
                    binding.cvFeedContainer.setVisibility(View.GONE);
                }
            }
        });


        binding.etCommenceDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDatePicker();
            }
        });

        binding.addClfSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noOfHouseHold = binding.etHhNo.getText().toString().trim();
                baseCapital = binding.etBaseCapital.getText().toString().trim();
                noOfPondsLeased = binding.etPondsNoLeased.getText().toString().trim();
                areaOfPonds = binding.etPondsTotalArea.getText().toString().trim();
                qtyOfFingerling = binding.etFingerlingsQty.getText().toString().trim();
                qtyOfYearling = binding.etYearlingQty.getText().toString().trim();
                qtyOfFry = binding.etFryAppliedQty.getText().toString().trim();
                nettingFreq = binding.etNettingFreq.getText().toString().trim();
                feedFry = binding.etFryQty.getText().toString().trim();
                feedFingerling = binding.etFingQty.getText().toString().trim();
                feedYearling = binding.etYearlQty.getText().toString().trim();
                totFryStock = binding.etTotalFryStockNo.getText().toString().trim();
                totFingerlingStock = binding.etTotalFingerlingStockNo.getText().toString().trim();
                totYearlingStock = binding.etTotalYeerlingStockNo.getText().toString().trim();
                pondCleaningFreq = binding.etPondCleaningFrequency.getText().toString().trim();
                commencementDate = binding.etCommenceDate.getText().toString().trim();
                remarks = binding.etRemarks.getText().toString().trim();

                if (selectedClfId.equals(0L)) {
                    Toast.makeText(AddFishCLFActivity.this, "Please select a Cluster", Toast.LENGTH_SHORT).show();
                } else if (noOfHouseHold.isEmpty()) {
                    Toast.makeText(AddFishCLFActivity.this, "Please fill No of Members", Toast.LENGTH_SHORT).show();
                } else if (baseCapital.isEmpty()) {
                    Toast.makeText(AddFishCLFActivity.this, "Please fill BaseCapital", Toast.LENGTH_SHORT).show();
                } else if (commencementDate.isEmpty()) {
                    Toast.makeText(AddFishCLFActivity.this, "Please fill Commencement Date", Toast.LENGTH_SHORT).show();
                } else if (noOfPondsLeased.isEmpty()) {
                    Toast.makeText(AddFishCLFActivity.this, "Please fill No of Ponds Leased", Toast.LENGTH_SHORT).show();
                } else if (areaOfPonds.isEmpty()) {
                    Toast.makeText(AddFishCLFActivity.this, "Please fill Pond area in hectares", Toast.LENGTH_SHORT).show();
                } /*else if (qtyOfFry.isEmpty()) {
                    Toast.makeText(AddFishCLFActivity.this, "Please fill Quantity of Fry", Toast.LENGTH_SHORT).show();
                } else if (qtyOfFingerling.isEmpty()) {
                    Toast.makeText(AddFishCLFActivity.this, "Please fill Quantity of Fingerlings", Toast.LENGTH_SHORT).show();
                } else if (qtyOfYearling.isEmpty()) {
                    Toast.makeText(AddFishCLFActivity.this, "Please fill Quantity of Yearlings", Toast.LENGTH_SHORT).show();
                }*/ else if (pondCleaningFreq.isEmpty()) {
                    Toast.makeText(AddFishCLFActivity.this, "Please fill Pond cleaning frequency", Toast.LENGTH_SHORT).show();
                } else if (Integer.parseInt(pondCleaningFreq)> 12) {
                    Toast.makeText(AddFishCLFActivity.this, "Please fill valid Pond cleaning frequency", Toast.LENGTH_SHORT).show();
                } else if (isRegularNetting && nettingFreq.isEmpty()) {
                    Toast.makeText(AddFishCLFActivity.this, "Please fill Netting frequency", Toast.LENGTH_SHORT).show();
                } else if (isFeedMgnt && (feedFry.isEmpty() || feedFingerling.isEmpty() || feedYearling.isEmpty())) {
                    Toast.makeText(AddFishCLFActivity.this, "Please fill Feeding Management", Toast.LENGTH_SHORT).show();
                } else if (totFryStock.isEmpty()) {
                    Toast.makeText(AddFishCLFActivity.this, "Please fill Total No of Fry Stock", Toast.LENGTH_SHORT).show();
                } else if (totFingerlingStock.isEmpty()) {
                    Toast.makeText(AddFishCLFActivity.this, "Please fill Total No of Fingerling Stock", Toast.LENGTH_SHORT).show();
                } else if (totYearlingStock.isEmpty()) {
                    Toast.makeText(AddFishCLFActivity.this, "Please fill Total No of Yearling Stock", Toast.LENGTH_SHORT).show();
                } else {
                    clfRegistrationForPisciculture();
                }

            }
        });

    }

    private void clfRegistrationForPisciculture() {


        nettingFreq = nettingFreq.equals("") ? "0" : nettingFreq;
        pondCleaningFreq = pondCleaningFreq.equals("") ? "0" : pondCleaningFreq;
        totFryStock = totFryStock.equals("")?"0":totFryStock;
        totFingerlingStock = totFingerlingStock.equals("")?"0":totFingerlingStock;
        totYearlingStock = totYearlingStock.equals("")?"0":totYearlingStock;
        qtyOfFry = qtyOfFry.equals("")?"0":qtyOfFry;
        qtyOfFingerling = qtyOfFingerling.equals("")?"0":qtyOfFingerling;
        qtyOfYearling = qtyOfYearling.equals("")?"0":qtyOfYearling;
        noOfPondsLeased = noOfPondsLeased.equals("")?"0":noOfPondsLeased;

        if(!isRegularNetting){
            nettingFreq = "0";
        }
        if(!isFeedMgnt){
            feedFry = "0";
            feedFingerling = "0";
            feedYearling = "0";
        }


        binding.progreses.setVisibility(View.VISIBLE);
        JSONObject obj = new JSONObject();
        try {
            obj.put("entityTypeCode", entityTypeCode);
            obj.put("entityId", selectedClfId);
            obj.put("numPondsOwned", Integer.parseInt(noOfPondsOwned));
            obj.put("numPondsLeased", Integer.parseInt(noOfPondsLeased));
            obj.put("areaOfPond", Double.parseDouble(areaOfPonds));
            obj.put("baseCapital", Double.parseDouble(baseCapital));
            obj.put("commencementDate", commencementDate);
            obj.put("remarks", remarks);
            obj.put("schemeId", selectedSchemeId);
            obj.put("activityId", selectedActivityId);
            obj.put("pondCleaningFrequency", Integer.parseInt(pondCleaningFreq));
            obj.put("regularNettingDone", isRegularNetting);
            obj.put("nettingFrequency", Integer.parseInt(nettingFreq));
            obj.put("numFarmers", Integer.parseInt(noOfHouseHold));
            obj.put("numFryStocked", Integer.parseInt(totFryStock));
            obj.put("numFingerlingsStocked", Integer.parseInt(totFingerlingStock));
            obj.put("numYearlingsStocked", Integer.parseInt(totYearlingStock));
            obj.put("feedManagementDone", isFeedMgnt);
            obj.put("fryFeedQty", Integer.parseInt(/*qtyOfFry*/feedFry));
            obj.put("fingerlingFeedQty", Integer.parseInt(/*qtyOfFingerling*/feedFingerling));
            obj.put("yearlingFeedQty", Integer.parseInt(/*qtyOfYearling*/feedYearling));
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

                                    Toast.makeText(AddFishCLFActivity.this, resObj.optString("message"), Toast.LENGTH_LONG)
                                            .show();
                                } else {
                                    Toast.makeText(AddFishCLFActivity.this, resObj.optString("message"), Toast.LENGTH_LONG)
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

    private void getClfList() {

        JSONObject obj = new JSONObject();
        try {
            obj.put("schemeId", selectedSchemeId);
            obj.put("activityId", selectedActivityId);
            obj.put("reportingLevelCode", "CLF");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "fishery-activity/clf/list")
                .addHeaders("Authorization", "Bearer " + token)
                .addJSONObjectBody(obj)
                .setTag("clflist")
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

    private void openDatePicker() {
        Calendar myCalendar = Calendar.getInstance();
        final DatePickerDialog datePickerDialog = new DatePickerDialog(AddFishCLFActivity.this, new DatePickerDialog.OnDateSetListener() {
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