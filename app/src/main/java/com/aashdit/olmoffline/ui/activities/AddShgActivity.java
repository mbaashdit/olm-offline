package com.aashdit.olmoffline.ui.activities;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.aashdit.olmoffline.BuildConfig;
import com.aashdit.olmoffline.R;
import com.aashdit.olmoffline.adapters.GoatshedSpnAdapter;
import com.aashdit.olmoffline.adapters.ShgSpnAdapter;
import com.aashdit.olmoffline.databinding.ActivityAddShgBinding;
import com.aashdit.olmoffline.models.GoatShed;
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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import static android.content.pm.PackageManager.GET_META_DATA;

public class AddShgActivity extends AppCompatActivity {

    private static final String TAG = "AddShgActivity";
    private final int PICK_IMAGE_REQUEST = 1;
    private final ArrayList<SHG> shgArrayList = new ArrayList<>();
    private final ArrayList<GoatShed> goatShedArrayList = new ArrayList<>();
    private ActivityAddShgBinding binding;
    private Uri filePath;
    private Long selectedSchemeId, selectedActivityId;
    private String noOfHouseHold, noOfGoat, noOfBuck, baseCapital, commencementDate, remarks;
    private String token, entityTypeCode;
    private SharedPrefManager sp;
    private ShgSpnAdapter shgSpnAdapter;
    private Spinner mSpnGoatShed;
    private GoatshedSpnAdapter goatshedSpnAdapter;
    private boolean isBucktied = false;
    private String intentType, entityName;
    private Long entityId = 0L;
    private Long goatryRegistrationId;
    private Long selectedShgId = 0L;
    private Long selectedGoatshedId = 0L;

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
        binding = ActivityAddShgBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        resetTitles();
        setSupportActionBar(binding.toolbarShgList);
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
        binding.tvToolbarTitleOne.setText(intentType.equals(Constant.ADD) ?
                getResources().getString(R.string.add_shg_activity) : "Update SHG Activity");
        binding.ivImage.setVisibility(View.GONE);
        binding.rlImagePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
            }
        });
        binding.etCommenceDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDatePicker();
            }
        });
//        binding.ivViewShg.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });

//        binding.ivViewShgLbl.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//
//            }
//        });
        binding.switchBuckTied.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                isBucktied = b;
            }
        });

        SHG shg = new SHG();
        shg.setShgName("Select SHG");
        shg.setShgDetailsId(0L);
        shg.setShgRegNumber("SHG");
        shg.setShgType(0L);
        shgArrayList.add(shg);

        GoatShed goatShed = new GoatShed();
        goatShed.setActive(true);
        goatShed.setValueEn("Type of Goat Shed");
        goatShed.setValueHi("Type of Goat Shed ");
        goatShed.setValueId(0L);
        goatShedArrayList.add(goatShed);


        getGoatShed();
        getShgList();

        binding.rlAddShg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noOfHouseHold = binding.etHouseholdNo.getText().toString().trim();
                noOfBuck = binding.etBuckNo.getText().toString().trim();
                noOfGoat = binding.etGoatNo.getText().toString().trim();
                baseCapital = binding.etBaseCapital.getText().toString().trim();
                commencementDate = binding.etCommenceDate.getText().toString().trim();
                remarks = binding.etRemarks.getText().toString().trim();

            }
        });
        mSpnGoatShed = findViewById(R.id.spn_add_shg_type_of_goat_shed);
        goatshedSpnAdapter = new GoatshedSpnAdapter(this, goatShedArrayList);
        mSpnGoatShed.setAdapter(goatshedSpnAdapter);
        shgSpnAdapter = new ShgSpnAdapter(this, shgArrayList);
        binding.spnAddShgSelectShg.setAdapter(shgSpnAdapter);

        binding.spnAddShgSelectShg.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                selectedShgId = shgArrayList.get(position).getShgDetailsId();
                if (position == 0) {
                    selectedShgId = 0L;
//                    Toast.makeText(AddShgActivity.this, "Please select a SHG", Toast.LENGTH_SHORT).show();
                } else {
                    selectedShgId = shgArrayList.get(position).getShgDetailsId();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        binding.spnAddShgTypeOfGoatShed.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                selectedGoatshedId = goatShedArrayList.get(position).getValueId();
                if (position == 0) {
                    selectedGoatshedId = 0L;
//                    Toast.makeText(AddShgActivity.this, "Please select a Goat Shed Type", Toast.LENGTH_SHORT).show();
                } else {
                    selectedGoatshedId = goatShedArrayList.get(position).getValueId();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        binding.progreses.setVisibility(View.GONE);
        binding.addShgSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                noOfHouseHold = binding.etHouseholdNo.getText().toString().trim();
                noOfGoat = binding.etGoatNo.getText().toString().trim();
                noOfBuck = binding.etBuckNo.getText().toString().trim();
                baseCapital = binding.etBaseCapital.getText().toString().trim();
                commencementDate = binding.etCommenceDate.getText().toString().trim();
                remarks = binding.etRemarks.getText().toString().trim();

                if (intentType.equals(Constant.ADD) && selectedShgId.equals(0L)) {
                    Toast.makeText(AddShgActivity.this, "Please select a SHG", Toast.LENGTH_SHORT).show();
                } else if (noOfHouseHold.isEmpty()) {
                    Toast.makeText(AddShgActivity.this, "Please fill No of Members", Toast.LENGTH_SHORT).show();
                } else if (noOfGoat.isEmpty()) {
                    Toast.makeText(AddShgActivity.this, "Please fill No of Goats", Toast.LENGTH_SHORT).show();
                } else if (noOfBuck.isEmpty()) {
                    Toast.makeText(AddShgActivity.this, "Please fill No of Kids", Toast.LENGTH_SHORT).show();
                } else if (selectedGoatshedId.equals(0L)) {
                    Toast.makeText(AddShgActivity.this, "Please select GoatShed", Toast.LENGTH_SHORT).show();
                } else if (baseCapital.isEmpty()) {
                    Toast.makeText(AddShgActivity.this, "Please fill Base Capital", Toast.LENGTH_SHORT).show();
                } else if (commencementDate.isEmpty()) {
                    Toast.makeText(AddShgActivity.this, "Please fill Commencement Date", Toast.LENGTH_SHORT).show();
                } else {
                    doSubmitShgActivity();
                }
            }
        });

        if (intentType.equals(Constant.UPDATE)) {
            getViewReg();
            disableFields();

            binding.spnAddShgSelectShg.setVisibility(View.GONE);
            binding.tvGoatShgName.setVisibility(View.VISIBLE);

            binding.ivEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    binding.ivEdit.setColorFilter(ContextCompat.getColor(AddShgActivity.this, R.color.purple_500), android.graphics.PorterDuff.Mode.MULTIPLY);
                    enableFields();
                }
            });
        } else {
            binding.spnAddShgSelectShg.setVisibility(View.VISIBLE);
            binding.tvGoatShgName.setVisibility(View.GONE);
            binding.ivEdit.setVisibility(View.GONE);
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

        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "goatry-activity/view-reg")
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

                                    goatryRegistrationId = resObj.optLong("goatryRegistrationId");
                                    noOfHouseHold = String.valueOf(resObj.optLong("numHousehold"));
                                    baseCapital = resObj.optString("baseCapital").replace(",", "");

                                    commencementDate = resObj.optString("commencementDate");
                                    remarks = resObj.optString("remarks");
                                    selectedGoatshedId = resObj.optLong("goatShedTypeId");
                                    selectedShgId = resObj.optLong("entityId");
                                    noOfBuck = String.valueOf(resObj.optLong("numBuck"));
                                    noOfGoat = String.valueOf(resObj.optLong("numGoat"));
                                    isBucktied = resObj.optBoolean("bucksTied");

                                    binding.etBaseCapital.setText(baseCapital);
                                    binding.etCommenceDate.setText(commencementDate);
                                    binding.etRemarks.setText(remarks);
                                    binding.etHouseholdNo.setText(noOfHouseHold);
                                    binding.etBuckNo.setText(noOfBuck);
                                    binding.etGoatNo.setText(noOfGoat);
                                    binding.switchBuckTied.setChecked(isBucktied);
                                    binding.tvGoatShgName.setText(entityName);


                                    if (intentType.equals(Constant.UPDATE)) {
                                        for (int i = 0; i < goatShedArrayList.size(); i++) {
                                            if (selectedGoatshedId.equals(goatShedArrayList.get(i).getValueId())) {
                                                binding.spnAddShgTypeOfGoatShed.setSelection(i);
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

    private void doSubmitShgActivity() {
        binding.progreses.setVisibility(View.VISIBLE);
        JSONObject obj = new JSONObject();
        try {
            if (intentType.equals(Constant.UPDATE)) {
                obj.put("goatryRegistrationId", goatryRegistrationId);
            }
            obj.put("baseCapital", baseCapital);
            obj.put("commencementDate", commencementDate);
            obj.put("remarks", remarks);
            obj.put("goatShedTypeId", selectedGoatshedId);
            obj.put("numBuck", noOfBuck);
            obj.put("numGoat", noOfGoat);
            obj.put("entityTypeCode", entityTypeCode);
            obj.put("entityId", selectedShgId);
            obj.put("numHousehold", noOfHouseHold);
            obj.put("schemeId", selectedSchemeId);
            obj.put("activityId", selectedActivityId);
            obj.put("bucksTied", isBucktied);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "goatry-activity/save")
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

                                    Toast.makeText(AddShgActivity.this, resObj.optString("message"), Toast.LENGTH_LONG)
                                            .show();
                                } else {
                                    Toast.makeText(AddShgActivity.this, resObj.optString("message") + "", Toast.LENGTH_LONG)
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
        binding.etHouseholdNo.setEnabled(false);
        binding.etHouseholdNo.setClickable(false);
        binding.etGoatNo.setEnabled(false);
        binding.etGoatNo.setClickable(false);
        binding.etBuckNo.setEnabled(false);
        binding.etBuckNo.setClickable(false);
        binding.etRemarks.setEnabled(false);
        binding.etRemarks.setClickable(false);
        binding.etCommenceDate.setEnabled(false);
        binding.etCommenceDate.setClickable(false);
        binding.etBaseCapital.setEnabled(false);
        binding.etBaseCapital.setClickable(false);
        binding.switchBuckTied.setEnabled(false);
        binding.spnAddShgTypeOfGoatShed.setEnabled(false);
        binding.spnAddShgSelectShg.setEnabled(false);

        binding.addShgSubmit.setEnabled(false);
        binding.addShgSubmit.setClickable(false);
    }

    private void enableFields() {
        binding.etHouseholdNo.setEnabled(true);
        binding.etGoatNo.setEnabled(true);
        binding.etBuckNo.setEnabled(true);
        binding.etRemarks.setEnabled(true);
        binding.etCommenceDate.setEnabled(true);
        binding.etBaseCapital.setEnabled(true);
        binding.switchBuckTied.setEnabled(true);
        binding.spnAddShgTypeOfGoatShed.setEnabled(true);
        binding.spnAddShgSelectShg.setEnabled(true);

        binding.addShgSubmit.setEnabled(true);
        binding.addShgSubmit.setClickable(true);
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

        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "goatry-activity/shg/list")
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
                                        goatShed.setValueEn("Type of Goat Shed");
                                        goatShed.setValueHi("Type of Goat Shed ");
                                        goatShed.setValueId(0L);
                                        goatShedArrayList.add(goatShed);
                                        for (int i = 0; i < array.length(); i++) {
                                            GoatShed scheme = GoatShed.parseGoatShed(array.optJSONObject(i));
//                                            if (!scheme.isLotteryDone) {
                                            goatShedArrayList.add(scheme);
//                                            }
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
    public void onBackPressed() {
        Intent intent = getIntent();
        intent.putExtra("res", false);
        intent.putExtra("schemeId", selectedSchemeId);
        intent.putExtra("activityId", selectedActivityId);
        setResult(RESULT_OK, intent);
        finish();
        super.onBackPressed();
    }


    private void openDatePicker() {
        Calendar myCalendar = Calendar.getInstance();
        final DatePickerDialog datePickerDialog = new DatePickerDialog(AddShgActivity.this, new DatePickerDialog.OnDateSetListener() {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            filePath = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), filePath);
                binding.ivImage.setImageBitmap(bitmap);
                binding.ivImage.setVisibility(View.VISIBLE);
                String filename = filePath.getLastPathSegment();
                Log.i(TAG, "onActivityResult: filePath " + filename);
//                profileImageAsString = getEncoded64ImageStringFromBitmap(bitmap);
//                profileImageAsString = profileImageAsString.concat("_jpg");
//                mIvProfileImg.setVisibility(View.VISIBLE);
//                mTvProfileLbl.setVisibility(View.GONE);
//                dialog.dismiss();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}