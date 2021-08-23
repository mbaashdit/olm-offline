package com.aashdit.olmoffline.ui.activities;

import android.app.DatePickerDialog;
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
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.aashdit.olmoffline.BuildConfig;
import com.aashdit.olmoffline.R;
import com.aashdit.olmoffline.adapters.FundSourceAdapter;
import com.aashdit.olmoffline.adapters.HouseHoldSpnAdapter;
import com.aashdit.olmoffline.adapters.ShgSpnAdapter;
import com.aashdit.olmoffline.adapters.UOMSpnAdapter;
import com.aashdit.olmoffline.adapters.UOMSpnProdAdapter;
import com.aashdit.olmoffline.adapters.VillageNfSpnAdapter;
import com.aashdit.olmoffline.databinding.ActivityNFHHRegdBinding;
import com.aashdit.olmoffline.models.FundSource;
import com.aashdit.olmoffline.models.HHEntity;
import com.aashdit.olmoffline.models.SHG;
import com.aashdit.olmoffline.models.UOM;
import com.aashdit.olmoffline.models.VillageForNF;
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

public class NFHHRegdActivity extends AppCompatActivity {

    private static final String TAG = "NFHHRegdActivity";
    private ActivityNFHHRegdBinding binding;

    private String token, entityTypeCode;
    private SharedPrefManager sp;
    private Long selectedSchemeId, selectedActivityId, entityId;

    private String intentType;
    private boolean enableCheckBoxWhileUpdate = false;
    private Long registrationId;
    private String shgName;

    private ArrayList<FundSource> fundSources = new ArrayList<>();
    private ArrayList<HHEntity> entriesArrayList;

    private ArrayList<UOM> uomsProd = new ArrayList<>();
    private ArrayList<VillageForNF> villageForNF = new ArrayList<>();
    private final ArrayList<SHG> shgArrayList = new ArrayList<>();
    ArrayList<Long> selectedFundSources = new ArrayList<>();
    private ArrayList<UOM> uoms = new ArrayList<>();


    private FundSourceAdapter fundSourceAdapter;
    private ShgSpnAdapter shgSpnAdapter;
    private VillageNfSpnAdapter villageNfSpnAdapter;

    private HouseHoldSpnAdapter houseHoldSpnAdapter;


    private Long selectedVillageId = 0L;
    private Long selectedHhId = 0L;
    private Long soldUomId = 0L;
    private Long productionUomId = 0L;


    private UOMSpnAdapter uomSpnAdapter;
    private UOMSpnProdAdapter uomSpnProdAdapter;
    private String noOfMembers, baseCapital, commencementDate, remarks, entityName;
    private Long selectedShgId = 0L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNFHHRegdBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        resetTitles();

        setSupportActionBar(binding.toolbarAddNonFarmHh);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        sp = SharedPrefManager.getInstance(this);
        token = sp.getStringData(Constant.APP_TOKEN);

        intentType = getIntent().getStringExtra(Constant.INTENT_TYPE);

        selectedSchemeId = getIntent().getLongExtra("SCHEME_ID", 0L);
        selectedActivityId = getIntent().getLongExtra("ACTIVITY_ID", 0L);
        entityTypeCode = getIntent().getStringExtra("ENTITY_TYPE_CODE");
        entityId = getIntent().getLongExtra("ENTITY_ID", 0L);
        entityName = getIntent().getStringExtra("ENTITY_NAME");
        if (entityTypeCode.equals("HH")) {
            binding.tvDiaryTitle.setText(getResources().getString(R.string.add_hh_activity));
        }

        binding.progreses.setVisibility(View.GONE);
        if (intentType != null) {
            if (intentType.equals(Constant.ADD)) {
                binding.ivEdit.setVisibility(View.GONE);
                binding.tvDiaryTitle.setText(getResources().getString(R.string.nf_add_enp));
//                binding.tvSelectShgLblUpdate.setVisibility(View.GONE);
                binding.spnAddShgSelectShg.setVisibility(View.VISIBLE);
//                binding.tvSelectShgLbl.setVisibility(View.VISIBLE);

                enableFields();
            } else {
                binding.ivEdit.setVisibility(View.VISIBLE);
                binding.tvDiaryTitle.setText(getResources().getString(R.string.nf_update_enp));
//                binding.tvSelectShgLblUpdate.setText(shgName);
//                binding.tvSelectShgLblUpdate.setVisibility(View.VISIBLE);
                binding.spnAddShgSelectShg.setVisibility(View.GONE);
//                binding.tvSelectShgLbl.setVisibility(View.GONE);

                binding.cvVillage.setVisibility(View.GONE);
                binding.tvSelectVillageLbl.setVisibility(View.GONE);
                binding.tvMemberShgSelect.setVisibility(View.GONE);
                binding.cvPg.setVisibility(View.GONE);
                binding.spnMemberHh.setVisibility(View.GONE);
                binding.tvSelectHhLblUpdate.setText(entityName);

                disableFields();
            }
        }


        entriesArrayList = new ArrayList<>();
        HHEntity entries = new HHEntity();
        entries.entityName = "Select Entrepreneur";
        entries.entityId = 0L;
        entriesArrayList.add(entries);
        houseHoldSpnAdapter = new HouseHoldSpnAdapter(this, entriesArrayList);
        binding.spnMemberHh.setAdapter(houseHoldSpnAdapter);

        VillageForNF village = new VillageForNF();
        village.entityId = 0L;
        village.entityName = "Select Village";

        villageForNF.add(village);


        shgSpnAdapter = new ShgSpnAdapter(this, shgArrayList);
        binding.spnAddShgSelectShg.setAdapter(shgSpnAdapter);

        villageNfSpnAdapter = new VillageNfSpnAdapter(this, villageForNF);
        binding.spnAddShgSelectVillage.setAdapter(villageNfSpnAdapter);
        binding.spnAddShgSelectVillage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    selectedVillageId = 0L;
                    shgArrayList.clear();
                    SHG shg = new SHG();
                    shg.setShgName("Select SHG");
                    shg.setShgDetailsId(0L);
                    shg.setShgRegNumber("SHG");
                    shg.setShgType(0L);
                    shgArrayList.add(shg);
                    shgSpnAdapter = null;
                    shgSpnAdapter = new ShgSpnAdapter(NFHHRegdActivity.this, shgArrayList);
                    binding.spnAddShgSelectShg.setAdapter(shgSpnAdapter);


                    entriesArrayList.clear();
                    HHEntity entries = new HHEntity();
                    entries.entityName = "Select Entrepreneur";
                    entries.entityId = 0L;
                    entriesArrayList.add(entries);
                    houseHoldSpnAdapter = null;
                    houseHoldSpnAdapter = new HouseHoldSpnAdapter(NFHHRegdActivity.this, entriesArrayList);
                    binding.spnMemberHh.setAdapter(houseHoldSpnAdapter);

//                    resetShgs();
                } else {
//                    resetShgs();
                    shgArrayList.clear();
                    SHG shg = new SHG();
                    shg.setShgName("Select SHG");
                    shg.setShgDetailsId(0L);
                    shg.setShgRegNumber("SHG");
                    shg.setShgType(0L);
                    shgArrayList.add(shg);
                    shgSpnAdapter.notifyDataSetChanged();

                    selectedVillageId = villageForNF.get(position).entityId;
                    getShgList();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        binding.spnAddShgSelectShg.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    selectedShgId = 0L;
                    selectedHhId = 0L;

                    entriesArrayList.clear();
                    HHEntity entries = new HHEntity();
                    entries.entityName = "Select Entrepreneur";
                    entries.entityId = 0L;
                    entriesArrayList.add(entries);
                    houseHoldSpnAdapter = null;
                    houseHoldSpnAdapter = new HouseHoldSpnAdapter(NFHHRegdActivity.this, entriesArrayList);
                    binding.spnMemberHh.setAdapter(houseHoldSpnAdapter);

                } else {
                    selectedShgId = shgArrayList.get(position).getShgDetailsId();
                    getHouseHoldList();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        binding.spnMemberHh.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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

        UOM uom = new UOM();
        uom.valueEn = "Select Unit Of Measurement";
        uom.valueId = 0L;
        uoms.add(uom);
        uomSpnAdapter = new UOMSpnAdapter(this, uoms);
        binding.spnAddShgUomSales.setAdapter(uomSpnAdapter);

        binding.spnAddShgUomSales.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0) {
                    soldUomId = 0L;
                } else {
                    soldUomId = uoms.get(i).valueId;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        UOM uomp = new UOM();
        uomp.valueEn = "Select Unit Of Measurement";
        uomp.valueId = 0L;
        uomsProd.add(uomp);
        uomSpnProdAdapter = new UOMSpnProdAdapter(this, uomsProd);
        binding.spnAddShgUomProduction.setAdapter(uomSpnProdAdapter);

        binding.spnAddShgUomProduction.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0) {
                    productionUomId = 0L;
                } else {
                    productionUomId = uomsProd.get(i).valueId;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        binding.etCommenceDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDatePicker();
            }
        });
        fundSourceAdapter = new FundSourceAdapter(this, fundSources);
        binding.spnFundSources.setSearchEnabled(true);
        if (intentType.equals(Constant.ADD)) {
            binding.tvSpnFsData.setVisibility(View.GONE);
            binding.spnFundSources.setHintText("Select");
        } else {
            binding.spnFundSources.setHintText("");
        }
        binding.spnFundSources.setClearText("Close & Clear");
        binding.spnFundSources.setSearchHint("Select data");
        binding.spnFundSources.setEmptyTitle("Not Data Found!");
        binding.spnFundSources.setItems(fundSources, items -> {
            binding.tvSpnFsData.setVisibility(View.GONE);
            selectedFundSources.clear();
            for (int i = 0; i < items.size(); i++) {
                Log.i(TAG, i + " : " + items.get(i).valueEn + " : " + items.get(i).isSelected + " ID " + items.get(i).valueId);
                if (!selectedFundSources.contains(items.get(i).valueId))
                    selectedFundSources.add(items.get(i).valueId);
            }
        });

        binding.addHhSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                baseCapital = binding.etBaseCapital.getText().toString().trim();
                remarks = binding.etRemarks.getText().toString().trim();
                commencementDate = binding.etCommenceDate.getText().toString().trim();

                if (intentType.equals(Constant.ADD) && selectedVillageId.equals(0L)) {
                    Toast.makeText(NFHHRegdActivity.this, "Please select a Village", Toast.LENGTH_SHORT).show();
                } else if (intentType.equals(Constant.ADD) && selectedShgId.equals(0L)) {
                    Toast.makeText(NFHHRegdActivity.this, "Please select a SHG", Toast.LENGTH_SHORT).show();
                } else if (intentType.equals(Constant.ADD) && selectedHhId.equals(0L)) {
                    Toast.makeText(NFHHRegdActivity.this, "Please Select an Entrepreneur", Toast.LENGTH_SHORT).show();
                } else if (baseCapital.isEmpty()) {
                    Toast.makeText(NFHHRegdActivity.this, "Please fill Base Capital", Toast.LENGTH_SHORT).show();
                } else if (selectedFundSources.size() == 0) {
                    Toast.makeText(NFHHRegdActivity.this, "Please Select Fund Source", Toast.LENGTH_SHORT).show();
                } else if (soldUomId.equals(0L)) {
                    Toast.makeText(NFHHRegdActivity.this, "Please Select Sales Unit", Toast.LENGTH_SHORT).show();
                } else if (productionUomId.equals(0L)) {
                    Toast.makeText(NFHHRegdActivity.this, "Please Select Production Unit", Toast.LENGTH_SHORT).show();
                } else if (commencementDate.isEmpty()) {
                    Toast.makeText(NFHHRegdActivity.this, "Please fill Commencement Date", Toast.LENGTH_SHORT).show();
                } else {
                    showDialog();
                }
            }
        });
        binding.ivEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.ivEdit.setColorFilter(ContextCompat.getColor(NFHHRegdActivity.this, R.color.purple_500), android.graphics.PorterDuff.Mode.MULTIPLY);
                enableFields();
                enableCheckBoxWhileUpdate = true;
            }
        });
        fundSource();
        uom();
        getVilageList();
    }

    private void resetShgs(){
//        shgArrayList.clear();
//        SHG shg = new SHG();
//        shg.setShgName("Select SHG");
//        shg.setShgDetailsId(0L);
//        shg.setShgRegNumber("SHG");
//        shg.setShgType(0L);
//        shgArrayList.add(shg);
//        shgSpnAdapter.notifyDataSetChanged();

        selectedShgId = 0L;
        shgSpnAdapter = null;
        shgSpnAdapter = new ShgSpnAdapter(NFHHRegdActivity.this, shgArrayList);
        binding.spnAddShgSelectShg.setAdapter(shgSpnAdapter);
    }
    private void showDialog() {
        if (!NFHHRegdActivity.this.isFinishing()) {
            String msg = intentType.equals(Constant.ADD) ? "Are you sure for registration ?" : "Are you sure for update ?";
            new AlertDialog.Builder(this)
                    .setTitle(intentType.equals(Constant.ADD) ? "Registration " : "Update ")
                    .setMessage(msg)
                    // Specifying a listener allows you to take an action before dismissing the dialog.
                    // The dialog is automatically dismissed when a dialog button is clicked.
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Continue with delete operation
                            doSubmitShgActivity();
                        }
                    }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).show();
        }
    }

    private void doSubmitShgActivity() {

        binding.progreses.setVisibility(View.VISIBLE);

        JSONObject reqObj = new JSONObject();
        try {
            if (intentType.equals(Constant.UPDATE)) {
                reqObj.put("registrationId", registrationId);
            }
            reqObj.put("schemeId", selectedSchemeId);
            reqObj.put("activityId", selectedActivityId);
            if (intentType.equals(Constant.UPDATE)) {
                reqObj.put("entityId", entityId);
            } else {
                reqObj.put("entityId", selectedHhId);
            }
            reqObj.put("entityTypeCode", entityTypeCode);
            reqObj.put("numMembers", 1);
            reqObj.put("fundSource", selectedFundSources.toString());
            reqObj.put("baseCapital", baseCapital);
            reqObj.put("commencementDate", commencementDate);
            reqObj.put("remarks", remarks);
            reqObj.put("salesUomId", soldUomId);
            reqObj.put("productionUomId", productionUomId);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "non-farm-activity/save")
                .addHeaders("Authorization", "Bearer " + token)
                .addJSONObjectBody(reqObj)
                .setTag("nfshgregd")
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

                                    Toast.makeText(NFHHRegdActivity.this, resObj.optString("message"), Toast.LENGTH_LONG)
                                            .show();
                                } else {
                                    Toast.makeText(NFHHRegdActivity.this, resObj.optString("message"), Toast.LENGTH_LONG)
                                            .show();
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.e(TAG, "onError: " + anError.getErrorDetail());
                        binding.progreses.setVisibility(View.GONE);
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


    private void fundSource() {


        AndroidNetworking.get(BuildConfig.BASE_URL + Constant.API_PATH + "lov/fundSource")
                .addHeaders("Authorization", "Bearer " + token)
                .setTag("fundSource")
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
                                        fundSources.clear();
                                        for (int i = 0; i < array.length(); i++) {
                                            FundSource scheme = FundSource.parseFundSource(array.optJSONObject(i));
                                            fundSources.add(scheme);
                                        }
                                        fundSourceAdapter.notifyDataSetChanged();

                                        isFundSource = true;
                                        if (isFundSource && isUom) {
                                            viewReg();
                                        }
                                        if (intentType.equals(Constant.UPDATE)) {

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

    private void uom() {
        AndroidNetworking.get(BuildConfig.BASE_URL + Constant.API_PATH + "lov/uom")
                .addHeaders("Authorization", "Bearer " + token)
                .setTag("irrigationSource")
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
                                        uoms.clear();
                                        UOM uom = new UOM();
                                        uom.valueEn = "Select Unit Of Measurement";
                                        uom.valueId = 0L;
                                        uoms.add(uom);

                                        uomsProd.clear();
                                        UOM uomp = new UOM();
                                        uomp.valueEn = "Select Unit Of Measurement";
                                        uomp.valueId = 0L;
                                        uomsProd.add(uomp);

                                        for (int i = 0; i < array.length(); i++) {
                                            UOM scheme = UOM.parseUOM(array.optJSONObject(i));
                                            uoms.add(scheme);
                                            uomsProd.add(scheme);
                                        }

                                        uomSpnAdapter.notifyDataSetChanged();
                                        uomSpnProdAdapter.notifyDataSetChanged();

                                        isUom = true;
                                        if (isFundSource && isUom) {
                                            viewReg();
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

        JSONObject obj = new JSONObject();
        try {
            obj.put("schemeId", selectedSchemeId);
            obj.put("activityId", selectedActivityId);
            obj.put("villageId", selectedVillageId);
            obj.put("reportingLevelCode", "HH");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "non-farm-activity/search-shg")
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
                                        selectedShgId = 0L;
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

//                                        shgSpnAdapter.notifyDataSetChanged();

                                        shgSpnAdapter = null;
                                        shgSpnAdapter = new ShgSpnAdapter(NFHHRegdActivity.this, shgArrayList);
                                        binding.spnAddShgSelectShg.setAdapter(shgSpnAdapter);
                                        if (intentType.equals(Constant.UPDATE)) {
                                            for (int i = 0; i < shgArrayList.size(); i++) {
                                                if (entityId.equals(shgArrayList.get(i).getShgId())) {
                                                    binding.spnAddShgSelectShg.setSelection(i);
                                                }

//                                                shgSpnAdapter.notifyDataSetChanged();
                                                shgSpnAdapter = null;
                                                shgSpnAdapter = new ShgSpnAdapter(NFHHRegdActivity.this, shgArrayList);
                                                binding.spnAddShgSelectShg.setAdapter(shgSpnAdapter);
                                            }
                                        }
                                    }else {
                                        resetShgs();
//                                        shgArrayList.clear();
//                                        SHG shg = new SHG();
//                                        shg.setShgName("Select SHG");
//                                        shg.setShgDetailsId(0L);
//                                        shg.setShgRegNumber("SHG");
//                                        shg.setShgType(0L);
//                                        shgArrayList.add(shg);
//                                        shgSpnAdapter = null;
//                                        shgSpnAdapter = new ShgSpnAdapter(NFHHRegdActivity.this, shgArrayList);
//                                        binding.spnAddShgSelectShg.setAdapter(shgSpnAdapter);
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

    private void getVilageList() {
        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "non-farm-activity/village/list")
                .addHeaders("Authorization", "Bearer " + token)
                .setTag("fundSource")
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
                                        villageForNF.clear();
                                        VillageForNF village = new VillageForNF();
                                        village.entityId = 0L;
                                        village.entityName = "Select Village";

                                        villageForNF.add(village);
                                        for (int i = 0; i < array.length(); i++) {
                                            VillageForNF scheme = VillageForNF.parseVillage(array.optJSONObject(i));
                                            villageForNF.add(scheme);
                                        }
                                        villageNfSpnAdapter.notifyDataSetChanged();

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

    private void getHouseHoldList() {

        JSONObject object = new JSONObject();
        try {
            object.put("schemeId", selectedSchemeId);
            object.put("activityId", selectedActivityId);
            object.put("entityId", selectedShgId);
            object.put("reportingLevelCode", entityTypeCode);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "non-farm-activity/hh/list")
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
                                        selectedHhId = 0L;
                                        entriesArrayList.clear();
                                        HHEntity entries = new HHEntity();
                                        entries.entityName = "Select Entrepreneur";
                                        entries.entityId = 0L;
                                        entriesArrayList.add(entries);
                                        for (int i = 0; i < array.length(); i++) {
                                            HHEntity e = HHEntity.parseHHEntity(array.optJSONObject(i));
                                            entriesArrayList.add(e);
                                        }
//                                        houseHoldSpnAdapter.notifyDataSetChanged();

                                        houseHoldSpnAdapter = null;
                                        houseHoldSpnAdapter = new HouseHoldSpnAdapter(NFHHRegdActivity.this, entriesArrayList);
                                        binding.spnMemberHh.setAdapter(houseHoldSpnAdapter);


                                    } else {
                                        entriesArrayList.clear();
                                        HHEntity entries = new HHEntity();
                                        entries.entityName = "Select Entrepreneur";
                                        entries.entityId = 0L;
                                        entriesArrayList.add(entries);
//                                        houseHoldSpnAdapter.notifyDataSetChanged();
                                        houseHoldSpnAdapter = null;
                                        houseHoldSpnAdapter = new HouseHoldSpnAdapter(NFHHRegdActivity.this, entriesArrayList);
                                        binding.spnMemberHh.setAdapter(houseHoldSpnAdapter);
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

    private boolean isFundSource, isUom;

    String fundData = "";


    private void viewReg() {

        /**
         * {
         *     "schemeId" : 2,
         *     "activityId":22,
         *     "entityId": 265241,
         *     "entityTypeCode": "SHG"
         * }
         * */

        JSONObject reqObj = new JSONObject();
        try {
            reqObj.put("schemeId", selectedSchemeId);
            reqObj.put("activityId", selectedActivityId);
            reqObj.put("entityId", entityId);
            reqObj.put("entityTypeCode", entityTypeCode);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "non-farm-activity/view-reg")
                .addHeaders("Authorization", "Bearer " + token)
                .addJSONObjectBody(reqObj)
                .setTag("irrigationSource")
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
                                    registrationId = resObj.optLong("registrationId");
                                    noOfMembers = String.valueOf(resObj.optLong("numFarmers"));
                                    baseCapital = String.valueOf(resObj.optLong("baseCapital"));

                                    commencementDate = resObj.optString("commencementDate");
                                    remarks = resObj.optString("remarks");
                                    soldUomId = resObj.optLong("salesUomId");
                                    productionUomId = resObj.optLong("productionUomId");
                                    selectedShgId = resObj.optLong("entityId");

                                    String strFundSource = resObj.optString("fundSource");
                                    strFundSource = strFundSource.replace("[", "");
                                    strFundSource = strFundSource.replace("]", "");

                                    if (strFundSource.contains(",")) {
                                        String[] strFundSources = strFundSource.split(",");
                                        fundData = "";
                                        for (String s : strFundSources) {
                                            Long fs = Long.parseLong(s.trim());
                                            for (int i = 0; i < fundSources.size(); i++) {
                                                if (fs.equals(fundSources.get(i).valueId)) {
                                                    selectedFundSources.add(fs);
                                                    fundSources.get(i).isSelected = true;
                                                    fundData = fundData.concat(fundSources.get(i).valueEn).concat(", ");
                                                }
                                            }
                                            binding.spnFundSources.refreshAdapterForUpdate();
                                        }
                                    } else {
                                        Long fs = Long.parseLong(strFundSource.trim());
//                                        selectedFundSources .add(fs);
                                        fundData = "";
                                        for (int i = 0; i < fundSources.size(); i++) {
                                            if (fs.equals(fundSources.get(i).valueId)) {
                                                selectedFundSources.add(fs);
                                                fundSources.get(i).isSelected = true;
                                                fundData = fundData.concat(fundSources.get(i).valueEn).concat(" ");
                                            }
                                            binding.spnFundSources.refreshAdapterForUpdate();
                                        }
                                    }
                                    binding.tvSpnFsData.setText(fundData);
                                    if (fundData.endsWith(", ")) {
                                        binding.tvSpnFsData.setText(fundData.substring(0, fundData.length() - 2));
                                    } else {
                                        binding.tvSpnFsData.setText(fundData);
                                    }
                                    if (intentType.equals(Constant.UPDATE)) {
                                        for (int i = 0; i < uoms.size(); i++) {
                                            if (soldUomId.equals(uoms.get(i).valueId)) {
                                                binding.spnAddShgUomSales.setSelection(i);
                                            }
                                            uomSpnAdapter.notifyDataSetChanged();
                                        }

                                        for (int i = 0; i < uomsProd.size(); i++) {
                                            if (productionUomId.equals(uomsProd.get(i).valueId)) {
                                                binding.spnAddShgUomProduction.setSelection(i);
                                            }
                                            uomSpnProdAdapter.notifyDataSetChanged();
                                        }

                                    }

//                                    binding.etFarmersNo.setText(noOfMembers);
                                    binding.etBaseCapital.setText(baseCapital);
                                    binding.etCommenceDate.setText(commencementDate);
                                    binding.etRemarks.setText(remarks);
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

    private void disableFields() {
        binding.spnAddShgUomSales.setEnabled(false);
        binding.spnAddShgSelectShg.setEnabled(false);
        binding.spnAddShgUomProduction.setEnabled(false);
        binding.etBaseCapital.setEnabled(false);
        binding.etCommenceDate.setEnabled(false);
        binding.etRemarks.setEnabled(false);
        binding.spnFundSources.setEnabled(false);
        binding.addHhSubmit.setEnabled(false);
        binding.addHhSubmit.setClickable(false);
    }

    private void enableFields() {
        binding.spnAddShgUomSales.setEnabled(true);
        binding.spnAddShgSelectShg.setEnabled(true);
        binding.spnAddShgUomProduction.setEnabled(true);
        binding.etBaseCapital.setEnabled(true);
        binding.etCommenceDate.setEnabled(true);
        binding.etRemarks.setEnabled(true);
        binding.spnFundSources.setEnabled(true);
        binding.addHhSubmit.setEnabled(true);
        binding.addHhSubmit.setClickable(true);
    }

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

    private void openDatePicker() {
        Calendar myCalendar = Calendar.getInstance();
        final DatePickerDialog datePickerDialog = new DatePickerDialog(NFHHRegdActivity.this, new DatePickerDialog.OnDateSetListener() {
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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

}