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
import com.aashdit.olmoffline.adapters.ActDescriptionSpnAdapter;
import com.aashdit.olmoffline.adapters.CommencementSpnAdapter;
import com.aashdit.olmoffline.adapters.FundSourceAdapter;
import com.aashdit.olmoffline.adapters.HouseHoldSpnAdapter;
import com.aashdit.olmoffline.adapters.ShgSpnAdapter;
import com.aashdit.olmoffline.adapters.UOMSpnAdapter;
import com.aashdit.olmoffline.adapters.UOMSpnProdAdapter;
import com.aashdit.olmoffline.adapters.VillageNfSpnAdapter;
import com.aashdit.olmoffline.databinding.ActivityNTFTHHRegdBinding;
import com.aashdit.olmoffline.models.ActDescription;
import com.aashdit.olmoffline.models.CommencementSeason;
import com.aashdit.olmoffline.models.FundSource;
import com.aashdit.olmoffline.models.HHEntity;
import com.aashdit.olmoffline.models.IrrigationSource;
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

public class NTFPHHRegdActivity extends AppCompatActivity {

    private static final String TAG = "NTFTHHRegdActivity";
    private final ArrayList<SHG> shgArrayList = new ArrayList<>();
    String fundData = "";
    private ActivityNTFTHHRegdBinding binding;
    private ArrayList<Long> selectedFundSources = new ArrayList<>();
    private String token, entityTypeCode;
    private SharedPrefManager sp;
    private Long selectedSchemeId, selectedActivityId, entityId;
    private String intentType;
    private boolean isCommencement;
    private Long registrationId;
    private String shgName;
    private ArrayList<FundSource> fundSources = new ArrayList<>();
    private ArrayList<IrrigationSource> irrigationSources = new ArrayList<>();
    private ArrayList<HHEntity> entriesArrayList;
    private ArrayList<UOM> uomsProd = new ArrayList<>();
    private ArrayList<VillageForNF> villageForNF = new ArrayList<>();
    private ArrayList<UOM> uoms = new ArrayList<>();
    private ArrayList<ActDescription> actDescriptions = new ArrayList<>();
    private ArrayList<CommencementSeason> commencementSeasons = new ArrayList<>();
    private ArrayList<Long> selectedIrrigationSources = new ArrayList<>();
    private FundSourceAdapter fundSourceAdapter;
    private ShgSpnAdapter shgSpnAdapter;
    private VillageNfSpnAdapter villageNfSpnAdapter;
    private HouseHoldSpnAdapter houseHoldSpnAdapter;
    private Long selectedVillageId = 0L;
    private Long selectedHhId = 0L;
    private Long soldUomId = 0L;
    private Long productionUomId = 0L;
    private Long commencementSeasonId = 0L;
    private Long actDescId = 0L;
    private UOMSpnAdapter uomSpnAdapter;
    private UOMSpnProdAdapter uomSpnProdAdapter;
    private CommencementSpnAdapter commencementSpnAdapter;
    private ActDescriptionSpnAdapter descriptionSpnAdapter;
    private String noOfMembers, baseCapital, commencementDate, remarks, entityName;
    private Long selectedShgId = 0L;
    private boolean isFundSource, isUom;
    private boolean isDescription, isIrrigation;
    private boolean isCollection, isCultivation;

    private int noOfPlants = 0;
    private double cultivation = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNTFTHHRegdBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        resetTitles();

        setSupportActionBar(binding.toolbarAddNtfpHh);
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

        binding.progreses.setVisibility(View.GONE);
        if (intentType != null) {
            if (intentType.equals(Constant.ADD)) {
                binding.ivEdit.setVisibility(View.GONE);
                binding.tvDiaryTitle.setText(getResources().getString(R.string.nf_add_enp));
                binding.spnAddShgSelectShg.setVisibility(View.VISIBLE);

                enableFields();
            } else {
                binding.ivEdit.setVisibility(View.VISIBLE);
                binding.tvDiaryTitle.setText(getResources().getString(R.string.nf_update_enp));
                binding.spnAddShgSelectShg.setVisibility(View.GONE);
                binding.tvSelectVillageLbl.setVisibility(View.GONE);
                binding.tvMemberShgSelect.setVisibility(View.GONE);
                binding.spnMemberHh.setVisibility(View.GONE);
                binding.cvShgContainer.setVisibility(View.GONE);
                binding.tvMemberLbl.setVisibility(View.GONE);
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
                    shgSpnAdapter = new ShgSpnAdapter(NTFPHHRegdActivity.this, shgArrayList);
                    binding.spnAddShgSelectShg.setAdapter(shgSpnAdapter);


                    entriesArrayList.clear();
                    HHEntity entries = new HHEntity();
                    entries.entityName = "Select Entrepreneur";
                    entries.entityId = 0L;
                    entriesArrayList.add(entries);
                    houseHoldSpnAdapter = null;
                    houseHoldSpnAdapter = new HouseHoldSpnAdapter(NTFPHHRegdActivity.this, entriesArrayList);
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
                    houseHoldSpnAdapter = new HouseHoldSpnAdapter(NTFPHHRegdActivity.this, entriesArrayList);
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

        ActDescription actDescription = new ActDescription();
        actDescription.valueEn = "Select Activity Description";
        actDescription.valueId = 0L;
        actDescriptions.add(actDescription);

        descriptionSpnAdapter = new ActDescriptionSpnAdapter(this, actDescriptions);
        binding.spnAddShgSelectActDesc.setAdapter(descriptionSpnAdapter);
        binding.spnAddShgSelectActDesc.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0) {
                    actDescId = 0L;
                    binding.cvArea.setVisibility(View.GONE);
                    binding.cvPlants.setVisibility(View.GONE);
                    isCollection = false;
                    isCultivation = false;
                } else {
                    actDescId = actDescriptions.get(i).valueId;
                    if (actDescriptions.get(i).valueCode.equals("CULTIVATION")) {
                        binding.cvArea.setVisibility(View.VISIBLE);
                        binding.cvPlants.setVisibility(View.GONE);
                        isCollection = false;
                        isCultivation = true;
                    } else if (actDescriptions.get(i).valueCode.equals("COLLECTION")) {
                        binding.cvPlants.setVisibility(View.VISIBLE);
                        binding.cvArea.setVisibility(View.GONE);
                        isCollection = true;
                        isCultivation = false;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        CommencementSeason commencementSeason = new CommencementSeason();
        commencementSeason.valueEn = "Select Commencement Season";
        commencementSeason.valueId = 0L;
        commencementSeasons.add(commencementSeason);
        commencementSpnAdapter = new CommencementSpnAdapter(this, commencementSeasons);
        binding.spnAddShgCommencementSeason.setAdapter(commencementSpnAdapter);
        binding.spnAddShgCommencementSeason.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0) {
                    commencementSeasonId = 0L;
                } else {
                    commencementSeasonId = commencementSeasons.get(i).valueId;
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
            binding.cvVillageContainer.setVisibility(View.GONE);

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


        binding.multipleItemSelectionSpinner.setSearchEnabled(true);
        if (intentType.equals(Constant.ADD)) {
            binding.tvSpnIrriData.setVisibility(View.GONE);
            binding.multipleItemSelectionSpinner.setHintText("Select");
        } else {
            binding.multipleItemSelectionSpinner.setHintText("");
        }
        binding.multipleItemSelectionSpinner.setClearText("Close & Clear");
        binding.multipleItemSelectionSpinner.setSearchHint("Select data");
        binding.multipleItemSelectionSpinner.setEmptyTitle("Not Data Found!");
        binding.multipleItemSelectionSpinner.setItems(irrigationSources, items -> {
            binding.tvSpnIrriData.setVisibility(View.GONE);
            selectedIrrigationSources.clear();
            for (int i = 0; i < items.size(); i++) {
                Log.i(TAG, i + " : " + items.get(i).valueEn + " : " + items.get(i).isSelected + " ID " + items.get(i).valueId);
                if (!selectedIrrigationSources.contains(items.get(i).valueId))
                    selectedIrrigationSources.add(items.get(i).valueId);
            }
        });


        binding.addHhSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                baseCapital = binding.etBaseCapital.getText().toString().trim();
                remarks = binding.etRemarks.getText().toString().trim();
                commencementDate = binding.etCommenceDate.getText().toString().trim();
                noOfPlants = binding.etNoOfPlants.getText().toString().trim().equals("") ? 0 : Integer.parseInt(binding.etNoOfPlants.getText().toString().trim());
                cultivation = binding.etAreaOfCultivation.getText().toString().trim().equals("") ? 0.0 : Double.parseDouble(binding.etAreaOfCultivation.getText().toString().trim());

                if (intentType.equals(Constant.ADD) && selectedVillageId.equals(0L)) {
                    Toast.makeText(NTFPHHRegdActivity.this, "Please select a Village", Toast.LENGTH_SHORT).show();
                } else if (intentType.equals(Constant.ADD) && selectedShgId.equals(0L)) {
                    Toast.makeText(NTFPHHRegdActivity.this, "Please select a SHG", Toast.LENGTH_SHORT).show();
                } else if (intentType.equals(Constant.ADD) && selectedHhId.equals(0L)) {
                    Toast.makeText(NTFPHHRegdActivity.this, "Please Select an Entrepreneur", Toast.LENGTH_SHORT).show();
                } else if (actDescId.equals(0L)) {
                    Toast.makeText(NTFPHHRegdActivity.this, "Please select Activity Description", Toast.LENGTH_SHORT).show();
                } else if (isCollection && noOfPlants == 0) {
                    Toast.makeText(NTFPHHRegdActivity.this, "Please fill no of plants", Toast.LENGTH_SHORT).show();
                } else if (isCultivation && cultivation == 0.0) {
                    Toast.makeText(NTFPHHRegdActivity.this, "Please fill area of cultivation", Toast.LENGTH_SHORT).show();
                } else if (baseCapital.isEmpty()) {
                    Toast.makeText(NTFPHHRegdActivity.this, "Please fill Base Capital", Toast.LENGTH_SHORT).show();
                } else if (selectedIrrigationSources.size() == 0) {
                    Toast.makeText(NTFPHHRegdActivity.this, "Please Select Irrigation Source", Toast.LENGTH_SHORT).show();
                } else if (soldUomId.equals(0L)) {
                    Toast.makeText(NTFPHHRegdActivity.this, "Please Select Sales Unit", Toast.LENGTH_SHORT).show();
                } else if (productionUomId.equals(0L)) {
                    Toast.makeText(NTFPHHRegdActivity.this, "Please Select Production Unit", Toast.LENGTH_SHORT).show();
                } else if (selectedFundSources.size() == 0) {
                    Toast.makeText(NTFPHHRegdActivity.this, "Please Select Fund Source", Toast.LENGTH_SHORT).show();
                } else if (commencementSeasonId.equals(0L)) {
                    Toast.makeText(NTFPHHRegdActivity.this, "Please fill Commencement Season", Toast.LENGTH_SHORT).show();
                } else if (commencementDate.isEmpty()) {
                    Toast.makeText(NTFPHHRegdActivity.this, "Please fill Commencement Date", Toast.LENGTH_SHORT).show();
                } else {
                    showDialog();
                }
            }
        });
        binding.ivEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.ivEdit.setColorFilter(ContextCompat.getColor(NTFPHHRegdActivity.this, R.color.purple_500), android.graphics.PorterDuff.Mode.MULTIPLY);
                enableFields();
            }
        });
        getVilageList();
        activityDescription();
        fundSource();
        irrigationSource();
        commencementSeason();
        uom();

    }

    private void activityDescription() {
        AndroidNetworking.get(BuildConfig.BASE_URL + Constant.API_PATH + "lov/ntfpActivityDesc")
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
                                        actDescriptions.clear();
                                        ActDescription description = new ActDescription();
                                        description.valueEn = "Select Activity Description";
                                        description.valueId = 0L;
                                        actDescriptions.add(description);


                                        for (int i = 0; i < array.length(); i++) {
                                            ActDescription scheme = ActDescription.parseActDescription(array.optJSONObject(i));
                                            actDescriptions.add(scheme);
                                        }
                                        descriptionSpnAdapter.notifyDataSetChanged();
                                        isDescription = true;


//                                        uomSpnAdapter.notifyDataSetChanged();
//                                        uomSpnProdAdapter.notifyDataSetChanged();

//                                        isUom = true;
                                        if (isCommencement && isFundSource && isUom) {
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

    private void irrigationSource() {
        AndroidNetworking.get(BuildConfig.BASE_URL + Constant.API_PATH + "lov/irrigationSource")
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
                                        irrigationSources.clear();
                                        for (int i = 0; i < array.length(); i++) {
                                            IrrigationSource scheme = IrrigationSource.parseCommencementSeason(array.optJSONObject(i));
                                            irrigationSources.add(scheme);
                                        }
//                                        setupIrrigationAdapter(NTFPSHGRegdActivity.this, irrigationSources);
//                                        irrigationAdapter.notifyDataSetChanged();
                                    }
                                    isIrrigation = true;
                                    if (isIrrigation && isFundSource && isCommencement && isUom) {
                                        viewReg();
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

    private void resetShgs() {

        selectedShgId = 0L;
        shgSpnAdapter = null;
        shgSpnAdapter = new ShgSpnAdapter(NTFPHHRegdActivity.this, shgArrayList);
        binding.spnAddShgSelectShg.setAdapter(shgSpnAdapter);
    }

    private void showDialog() {
        if (!NTFPHHRegdActivity.this.isFinishing()) {
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
            reqObj.put("activityDescriptionId", actDescId);
            reqObj.put("numPlants", noOfPlants);
            reqObj.put("areaofCultivation", cultivation);
            reqObj.put("fundSource", selectedFundSources.toString());
            reqObj.put("irrigationSource", selectedIrrigationSources.toString());
            reqObj.put("baseCapital", baseCapital);
            reqObj.put("commencementDate", commencementDate);
            reqObj.put("commencementSeasonId", commencementSeasonId);
            reqObj.put("remarks", remarks);
            reqObj.put("salesUOMId", soldUomId);
            reqObj.put("productionUOMId", productionUomId);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "ntfp/save")
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

                                    Toast.makeText(NTFPHHRegdActivity.this, resObj.optString("message"), Toast.LENGTH_LONG)
                                            .show();
                                } else {
                                    Toast.makeText(NTFPHHRegdActivity.this, resObj.optString("message"), Toast.LENGTH_LONG)
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
                                        if (isIrrigation && isCommencement && isFundSource && isUom) {
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

        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "ntfp/search-shg")
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
                                        shgSpnAdapter = new ShgSpnAdapter(NTFPHHRegdActivity.this, shgArrayList);
                                        binding.spnAddShgSelectShg.setAdapter(shgSpnAdapter);
                                        if (intentType.equals(Constant.UPDATE)) {
                                            for (int i = 0; i < shgArrayList.size(); i++) {
                                                if (entityId.equals(shgArrayList.get(i).getShgId())) {
                                                    binding.spnAddShgSelectShg.setSelection(i);
                                                }

//                                                shgSpnAdapter.notifyDataSetChanged();
                                                shgSpnAdapter = null;
                                                shgSpnAdapter = new ShgSpnAdapter(NTFPHHRegdActivity.this, shgArrayList);
                                                binding.spnAddShgSelectShg.setAdapter(shgSpnAdapter);
                                            }
                                        }
                                    } else {
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
        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "ntfp/village/list")
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

    private void commencementSeason() {
        AndroidNetworking.get(BuildConfig.BASE_URL + Constant.API_PATH + "lov/commencementSeason")
                .addHeaders("Authorization", "Bearer " + token)
                .setTag("commencementSeason")
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
                                        commencementSeasons.clear();
                                        CommencementSeason shg = new CommencementSeason();
                                        shg.valueEn = "Select Commencement Season";
                                        shg.valueId = 0L;
//                                        shg.setShgRegNumber("SHG");
//                                        shg.setShgType(0L);
                                        commencementSeasons.add(shg);
                                        for (int i = 0; i < array.length(); i++) {
                                            CommencementSeason scheme = CommencementSeason.parseCommencementSeason(array.optJSONObject(i));
                                            commencementSeasons.add(scheme);
                                        }
                                        isCommencement = true;
                                        if (isIrrigation && isFundSource && isCommencement && isUom) {
                                            viewReg();
                                        }

                                        commencementSpnAdapter.notifyDataSetChanged();
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
        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "ntfp/hh/list")
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
                                        houseHoldSpnAdapter = new HouseHoldSpnAdapter(NTFPHHRegdActivity.this, entriesArrayList);
                                        binding.spnMemberHh.setAdapter(houseHoldSpnAdapter);


                                    } else {
                                        entriesArrayList.clear();
                                        HHEntity entries = new HHEntity();
                                        entries.entityName = "Select Entrepreneur";
                                        entries.entityId = 0L;
                                        entriesArrayList.add(entries);
//                                        houseHoldSpnAdapter.notifyDataSetChanged();
                                        houseHoldSpnAdapter = null;
                                        houseHoldSpnAdapter = new HouseHoldSpnAdapter(NTFPHHRegdActivity.this, entriesArrayList);
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

    private String irriData = "";

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

        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "ntfp/view-reg")
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
                                    baseCapital = resObj.optString("baseCapital").replace(",", "");

                                    commencementSeasonId = resObj.optLong("commencementSeasonId");
                                    commencementDate = resObj.optString("commencementDate");
                                    remarks = resObj.optString("remarks");
                                    soldUomId = resObj.optLong("salesUOMId");
                                    productionUomId = resObj.optLong("productionUOMId");
                                    selectedShgId = resObj.optLong("entityId");
                                    actDescId = resObj.optLong("activityDescriptionId");
                                    noOfPlants = resObj.optInt("numPlants");
                                    cultivation = Double.parseDouble(resObj.optString("areaofCultivation"));

                                    binding.etAreaOfCultivation.setText(String.valueOf(cultivation));
                                    binding.etNoOfPlants.setText(String.valueOf(noOfPlants));
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



                                    String strIrrigation = resObj.optString("irrigationSource");
                                    strIrrigation = strIrrigation.replace("[", "");
                                    strIrrigation = strIrrigation.replace("]", "");

                                    if (strIrrigation.contains(",")) {
                                        String[] strIrrigations = strIrrigation.split(",");
                                        irriData = "";
                                        for (String s : strIrrigations) {
                                            Long fs = Long.parseLong(s.trim());
                                            for (int i = 0; i < irrigationSources.size(); i++) {
                                                if (fs.equals(irrigationSources.get(i).valueId)) {
                                                    selectedIrrigationSources.add(fs);
                                                    irrigationSources.get(i).isSelected = true;
                                                    irriData = irriData.concat(irrigationSources.get(i).valueEn).concat(", ");
                                                }
                                            }
                                        }
                                    } else {
                                        Long fs = Long.parseLong(strIrrigation.trim());
                                        irriData = "";
                                        for (int i = 0; i < irrigationSources.size(); i++) {
                                            if (fs.equals(irrigationSources.get(i).valueId)) {
                                                selectedIrrigationSources.add(fs);
                                                irrigationSources.get(i).isSelected = true;
                                                irriData = irriData.concat(irrigationSources.get(i).valueEn).concat("");
                                            }
                                        }
                                    }
                                    if (irriData.endsWith(", ")) {
                                        binding.tvSpnIrriData.setText(irriData.substring(0, irriData.length() - 2));
                                    } else {
                                        binding.tvSpnIrriData.setText(irriData);
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

                                        for (int i = 0; i < commencementSeasons.size(); i++) {
                                            if (commencementSeasonId.equals(commencementSeasons.get(i).valueId)) {
                                                binding.spnAddShgCommencementSeason.setSelection(i);
                                            }
                                            commencementSpnAdapter.notifyDataSetChanged();
                                        }

                                        for (int i = 0; i < actDescriptions.size(); i++) {
                                            if (actDescId.equals(actDescriptions.get(i).valueId)) {
                                                binding.spnAddShgSelectActDesc.setSelection(i);
                                            }
                                            descriptionSpnAdapter.notifyDataSetChanged();
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
        binding.spnAddShgSelectActDesc.setEnabled(false);
        binding.etBaseCapital.setEnabled(false);
        binding.etCommenceDate.setEnabled(false);
        binding.etRemarks.setEnabled(false);
        binding.spnFundSources.setEnabled(false);
        binding.addHhSubmit.setEnabled(false);
        binding.addHhSubmit.setClickable(false);
        binding.etNoOfPlants.setEnabled(false);
        binding.etAreaOfCultivation.setEnabled(false);

        binding.spnFundSources.setEnabled(false);
        binding.multipleItemSelectionSpinner.setEnabled(false);
        binding.spnAddShgCommencementSeason.setEnabled(false);
    }

    private void enableFields() {
        binding.spnAddShgUomSales.setEnabled(true);
        binding.spnAddShgSelectShg.setEnabled(true);
        binding.spnAddShgUomProduction.setEnabled(true);
        binding.spnAddShgSelectActDesc.setEnabled(true);
        binding.etBaseCapital.setEnabled(true);
        binding.etCommenceDate.setEnabled(true);
        binding.etRemarks.setEnabled(true);
        binding.spnFundSources.setEnabled(true);
        binding.addHhSubmit.setEnabled(true);
        binding.addHhSubmit.setClickable(true);
        binding.etNoOfPlants.setEnabled(true);
        binding.etAreaOfCultivation.setEnabled(true);

        binding.spnFundSources.setEnabled(true);
        binding.multipleItemSelectionSpinner.setEnabled(true);
        binding.spnAddShgCommencementSeason.setEnabled(true);
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
        final DatePickerDialog datePickerDialog = new DatePickerDialog(NTFPHHRegdActivity.this, new DatePickerDialog.OnDateSetListener() {
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