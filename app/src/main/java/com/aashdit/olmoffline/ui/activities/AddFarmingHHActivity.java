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
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.aashdit.olmoffline.BuildConfig;
import com.aashdit.olmoffline.R;
import com.aashdit.olmoffline.adapters.CommencementSpnAdapter;
import com.aashdit.olmoffline.adapters.FundSourceAdapter;
import com.aashdit.olmoffline.adapters.HouseHoldSpnAdapter;
import com.aashdit.olmoffline.adapters.IrrigationAdapter;
import com.aashdit.olmoffline.adapters.SeedTypeAdapter;
import com.aashdit.olmoffline.adapters.SeedTypeSpnAdapter;
import com.aashdit.olmoffline.adapters.ShgSpnAdapter;
import com.aashdit.olmoffline.adapters.UOMSpnAdapter;
import com.aashdit.olmoffline.adapters.UOMSpnProdAdapter;
import com.aashdit.olmoffline.databinding.ActivityAddFarmingHHBinding;
import com.aashdit.olmoffline.models.CommencementSeason;
import com.aashdit.olmoffline.models.FundSource;
import com.aashdit.olmoffline.models.HHEntity;
import com.aashdit.olmoffline.models.IrrigationSource;
import com.aashdit.olmoffline.models.SHG;
import com.aashdit.olmoffline.models.SeedType;
import com.aashdit.olmoffline.models.UOM;
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

public class AddFarmingHHActivity extends AppCompatActivity implements IrrigationAdapter.IrrigationSelectListener,
        FundSourceAdapter.FundSourceSelectListener, SeedTypeAdapter.SeedTypeSelectListener {

    private static final String TAG = "AddFarmingHHActivity";
    private final String reportingLevelCode = "HH";
    ArrayList<Long> selectedFundSources = new ArrayList<>();
    ArrayList<Long> selectedIrrigationSources = new ArrayList<>();
    ArrayList<Long> selectedSeedTypes = new ArrayList<>();
    private ActivityAddFarmingHHBinding binding;
    private String token, entityTypeCode;
    private SharedPrefManager sp;
    private Long selectedSchemeId;
    private Long selectedActivityId, entityId;
    private ArrayList<FundSource> fundSources = new ArrayList<>();
    private ArrayList<SeedType> seedTypes = new ArrayList<>();
    private ArrayList<CommencementSeason> commencementSeasons = new ArrayList<>();
    private ArrayList<IrrigationSource> irrigationSources = new ArrayList<>();
    private ArrayList<UOM> uoms = new ArrayList<>();
    private ArrayList<UOM> uomsProd = new ArrayList<>();
    private ArrayList<SHG> shgArrayList = new ArrayList<>();
    private SeedTypeSpnAdapter seedTypeSpnAdapter;
    private FundSourceAdapter fundSourceAdapter;
    private SeedTypeAdapter seedTypeAdapter;
    private IrrigationAdapter irrigationAdapter;
    private CommencementSpnAdapter commencementSpnAdapter;
    private UOMSpnAdapter uomSpnAdapter;
    private UOMSpnProdAdapter uomSpnProdAdapter;
    private ShgSpnAdapter shgSpnAdapter;
    private ArrayList<HHEntity> entriesArrayList;
    private HouseHoldSpnAdapter houseHoldSpnAdapter;//ShgHHSpnAdapter

    private Long selectedShgId = 0L;
    private Long selectedHhId = 0L;
    //    private Long selectedSeedType = 0L;
    private Long selectedIrrigationSource = 0L;
    private Long commencementSeasonId = 0L;
    private Long soldUomId = 0L;
    private Long productionUomId = 0L;

    private String noOfHouseHold, noOfGoat, noOfBuck, baseCapital, commencementDate, remarks;
    private Long registrationId;
    private String intentType, shgName, memberName;
    private boolean enableCheckBoxWhileUpdate = false;

    private void setupFundSourceAdapter(Context context, ArrayList<FundSource> fundSources) {
        fundSourceAdapter = new FundSourceAdapter(context, fundSources);
        fundSourceAdapter.setFundSourceSelectListener(this);
        binding.rvFundSource.setAdapter(fundSourceAdapter);
    }

    private void setupIrrigationAdapter(Context context, ArrayList<IrrigationSource> irrigationSources) {
        irrigationAdapter = new IrrigationAdapter(context, irrigationSources);
        irrigationAdapter.setIrrigationSelectListener(this);
        binding.rvIrrigationSouce.setAdapter(irrigationAdapter);
    }

    private void setupSeedTypeAdapter(Context context, ArrayList<SeedType> seedTypes) {
        seedTypeAdapter = new SeedTypeAdapter(context, seedTypes);
        seedTypeAdapter.setSeedTypeSelectListener(this);
        binding.rvSeedType.setAdapter(seedTypeAdapter);
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddFarmingHHBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        resetTitles();
        setSupportActionBar(binding.toolbarAddFarmHh);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        intentType = getIntent().getStringExtra(Constant.INTENT_TYPE);
        sp = SharedPrefManager.getInstance(this);
        token = sp.getStringData(Constant.APP_TOKEN);
        selectedSchemeId = getIntent().getLongExtra("SCHEME_ID", 0L);
        selectedActivityId = getIntent().getLongExtra("ACTIVITY_ID", 0L);
        entityId = getIntent().getLongExtra("ENTITY_ID", 0L);
        shgName = getIntent().getStringExtra("SHG_NAME");
        memberName = getIntent().getStringExtra("ENTITY_NAME");
        binding.progreses.setVisibility(View.GONE);

        if (intentType != null) {
            if (intentType.equals(Constant.ADD)) {
                binding.ivEdit.setVisibility(View.GONE);
                binding.tvDiaryTitle.setText(getResources().getString(R.string.add_hh_activity));

                enableFields();
            } else {
                binding.ivEdit.setVisibility(View.VISIBLE);
                binding.tvDiaryTitle.setText(R.string.farming_update_member);
                binding.tvMemberShgSelect.setVisibility(View.GONE);
                binding.spnAddShgSelectShg.setVisibility(View.GONE);
                binding.tvMemberLbl.setVisibility(View.GONE);
                binding.spnMemberHh.setVisibility(View.GONE);

                binding.tvSelectHhShgLblUpdate.setText(shgName);
                binding.tvSelectHhLblUpdate.setText(memberName);

                disableFields();
            }
        }

        binding.ivEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.ivEdit.setColorFilter(ContextCompat.getColor(AddFarmingHHActivity.this, R.color.purple_500), android.graphics.PorterDuff.Mode.MULTIPLY);
                enableFields();
                enableCheckBoxWhileUpdate = true;
                seedTypeAdapter.setEnable(enableCheckBoxWhileUpdate);
                seedTypeAdapter.notifyDataSetChanged();
            }
        });


        binding.rvFundSource.setLayoutManager(new LinearLayoutManager(this));
        setupFundSourceAdapter(this, fundSources);

        binding.rvIrrigationSouce.setLayoutManager(new LinearLayoutManager(this));
        setupIrrigationAdapter(this, irrigationSources);

        binding.rvSeedType.setLayoutManager(new LinearLayoutManager(this));
        setupSeedTypeAdapter(this, seedTypes);

        SHG shg = new SHG();
        shg.setShgName("Select SHG");
        shg.setShgDetailsId(0L);
        shg.setShgRegNumber("SHG");
        shg.setShgType(0L);
        shgArrayList.add(shg);
        shgSpnAdapter = new ShgSpnAdapter(this, shgArrayList);
        binding.spnAddShgSelectShg.setAdapter(shgSpnAdapter);


        binding.spnAddShgSelectShg.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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


        entriesArrayList = new ArrayList<>();
        HHEntity entries = new HHEntity();
        entries.entityName = "Select Member";
        entries.entityId = 0L;
        entriesArrayList.add(entries);
        houseHoldSpnAdapter = new HouseHoldSpnAdapter(this, entriesArrayList);
        binding.spnMemberHh.setAdapter(houseHoldSpnAdapter);

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


//        SeedType seedType = new SeedType();
//        seedType.valueEn = "Select SeedType";
//        seedType.valueId = 0L;
//        seedTypes.add(seedType);
//        seedTypeSpnAdapter = new SeedTypeSpnAdapter(this, seedTypes);
//        binding.spnAddShgTypeSeed.setAdapter(seedTypeSpnAdapter);
//        binding.spnAddShgTypeSeed.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                if (position == 0) {
//                    selectedSeedType = 0L;
//                } else {
//                    selectedSeedType = seedTypes.get(position).valueId;
//                }
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });
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

        binding.etRemarks.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (binding.etRemarks.hasFocus()) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    switch (event.getAction() & MotionEvent.ACTION_MASK) {
                        case MotionEvent.ACTION_SCROLL:
                            v.getParent().requestDisallowInterceptTouchEvent(false);
                            return true;
                    }
                }
                return false;
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


        binding.spnSeedtype.setSearchEnabled(true);
        if (intentType.equals(Constant.ADD)) {
            binding.tvSpnSeedData.setVisibility(View.GONE);
            binding.spnSeedtype.setHintText("Select");
        }else{
            binding.spnSeedtype.setHintText("");
        }
        binding.spnSeedtype.setClearText("Close & Clear");
        binding.spnSeedtype.setSearchHint("Select data");
        binding.spnSeedtype.setEmptyTitle("Not Data Found!");
        binding.spnSeedtype.setItems(seedTypes, items -> {
            binding.tvSpnSeedData.setVisibility(View.GONE);
            //The followings are selected items.
            selectedSeedTypes.clear();
            for (int i = 0; i < items.size(); i++) {
                Log.i(TAG, i + " : " + items.get(i).valueEn + " : " + items.get(i).isSelected + " ID " + items.get(i).valueId);
                if (!selectedSeedTypes.contains(items.get(i).valueId))
                    selectedSeedTypes.add(items.get(i).valueId);
            }
        });


        binding.spnFundSources.setSearchEnabled(true);
        if (intentType.equals(Constant.ADD)) {
            binding.tvSpnFsData.setVisibility(View.GONE);
            binding.spnFundSources.setHintText("Select");
        }else{
            binding.spnFundSources.setHintText("");
        }
        binding.spnFundSources.setClearText("Close & Clear");
        binding.spnFundSources.setSearchHint("Select data");
        binding.spnFundSources.setEmptyTitle("Not Data Found!");
        binding.spnFundSources.setItems(fundSources, items -> {
            binding.tvSpnFsData.setVisibility(View.GONE);
            //The followings are selected items.
            selectedFundSources.clear();
            for (int i = 0; i < items.size(); i++) {
                Log.i(TAG, i + " : " + items.get(i).valueEn + " : " + items.get(i).isSelected + " ID " + items.get(i).valueId);
                if (!selectedFundSources.contains(items.get(i).valueId))
                    selectedFundSources.add(items.get(i).valueId);
            }
        });

        // Pass true If you want searchView above the list. Otherwise false. default = true.
        binding.multipleItemSelectionSpinner.setSearchEnabled(true);

        if (intentType.equals(Constant.ADD)) {
            binding.tvSpnIrriData.setVisibility(View.GONE);
            binding.multipleItemSelectionSpinner.setHintText("Select");
        }else{
            binding.multipleItemSelectionSpinner.setHintText("");
        }

        //A text that will display in clear text button
        binding.multipleItemSelectionSpinner.setClearText("Close & Clear");

        // A text that will display in search hint.
        binding.multipleItemSelectionSpinner.setSearchHint("Select data");

        // Set text that will display when search result not found...
        binding.multipleItemSelectionSpinner.setEmptyTitle("Not Data Found!");

        // If you will set the limit, this button will not display automatically.
//        multiSelectSpinnerWithSearch.setShowSelectAllButton(true);

        // Removed second parameter, position. Its not required now..
        // If you want to pass preselected items, you can do it while making listArray,
        // pass true in setSelected of any item that you want to preselect
        binding.multipleItemSelectionSpinner.setItems(irrigationSources, items -> {
            binding.tvSpnIrriData.setVisibility(View.GONE);
            //The followings are selected items.
            selectedIrrigationSources.clear();
            for (int i = 0; i < items.size(); i++) {
                Log.i(TAG, i + " : " + items.get(i).valueEn + " : " + items.get(i).isSelected + " ID " + items.get(i).valueId);
                if (!selectedIrrigationSources.contains(items.get(i).valueId))
                    selectedIrrigationSources.add(items.get(i).valueId);
            }
        });


        getShgList();
        getSeedType();
        fundSource();
        commencementSeason();
        irrigationSource();
        uom();

//        if (intentType.equals(Constant.UPDATE)) {
//            viewReg();
//        }

        binding.etCommenceDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDatePicker();
            }
        });

        binding.addHhSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                baseCapital = binding.etBaseCapital.getText().toString().trim();
                commencementDate = binding.etCommenceDate.getText().toString().trim();
                remarks = binding.etRemarks.getText().toString().trim();


                if (intentType.equals(Constant.ADD) && selectedShgId.equals(0L)) {
                    Toast.makeText(AddFarmingHHActivity.this, "Please select a SHG", Toast.LENGTH_SHORT).show();
                } else if (intentType.equals(Constant.ADD) && selectedHhId.equals(0L)) {
                    Toast.makeText(AddFarmingHHActivity.this, "Please select a Member", Toast.LENGTH_SHORT).show();
                } else if (selectedSeedTypes.size() == 0) {
                    Toast.makeText(AddFarmingHHActivity.this, "Please Select SeedType", Toast.LENGTH_SHORT).show();
                } else if (baseCapital.isEmpty()) {
                    Toast.makeText(AddFarmingHHActivity.this, "Please fill Base Capital", Toast.LENGTH_SHORT).show();
                } else if (selectedIrrigationSources.size() == 0) {
                    Toast.makeText(AddFarmingHHActivity.this, "Please Select Irrigation Source", Toast.LENGTH_SHORT).show();
                } else if (soldUomId.equals(0L)) {
                    Toast.makeText(AddFarmingHHActivity.this, "Please Select Sales Unit", Toast.LENGTH_SHORT).show();
                } else if (productionUomId.equals(0L)) {
                    Toast.makeText(AddFarmingHHActivity.this, "Please Select Production Unit", Toast.LENGTH_SHORT).show();
                } else if (selectedFundSources.size() == 0) {
                    Toast.makeText(AddFarmingHHActivity.this, "Please Select Fund Source", Toast.LENGTH_SHORT).show();
                } else if (commencementSeasonId.equals(0L)) {
                    Toast.makeText(AddFarmingHHActivity.this, "Please Select Commencement Season", Toast.LENGTH_SHORT).show();
                } else if (commencementDate.isEmpty()) {
                    Toast.makeText(AddFarmingHHActivity.this, "Please fill Commencement Date", Toast.LENGTH_SHORT).show();
                } else {
                    showDialog();
                }
            }
        });

    }


    private boolean isSeedType, isIrrigation, isFundSource, isCommencement, isUom;
    String seedData = "";
    String irriData = "";
    String fundData = "";
    private void viewReg() {

        /**
         * {
         *     "schemeId": 2,
         *     "activityId": 28,
         *     "entityId": 2911724,
         *     "entityTypeCode": "HH"
         * }
         * */

        JSONObject reqObj = new JSONObject();
        try {
            reqObj.put("schemeId", selectedSchemeId);
            reqObj.put("activityId", selectedActivityId);
            reqObj.put("entityId", entityId);
            reqObj.put("entityTypeCode", "HH");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "farm-activity/view-reg")
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
//                                    noOfFarmer = String.valueOf(resObj.optLong("numFarmers"));
                                    baseCapital = String.valueOf(resObj.optLong("baseCapital"));

                                    commencementSeasonId = resObj.optLong("commencementSeasonId");
                                    commencementDate = resObj.optString("commencementDate");
                                    remarks = resObj.optString("remarks");
                                    soldUomId = resObj.optLong("soldUomId");
                                    productionUomId = resObj.optLong("productionUomId");
                                    selectedShgId = resObj.optLong("entityId");

                                    String strSeedType = resObj.optString("seedType");
                                    strSeedType = strSeedType.replace("[", "");
                                    strSeedType = strSeedType.replace("]", "");

                                    if (strSeedType.contains(",")) {
                                        String[] strSeed = strSeedType.split(",");
                                        seedData = "";
                                        for (String s : strSeed) {
                                            Long asd = Long.parseLong(s.trim());
                                            for (int i = 0; i < seedTypes.size(); i++) {
                                                if (asd.equals(seedTypes.get(i).valueId)) {
                                                    selectedSeedTypes.add(asd);
                                                    seedTypes.get(i).isSelected = true;
                                                    seedData = seedData.concat(seedTypes.get(i).valueEn).concat(", ");
//                                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
//                                                        seedData = seedTypes
//                                                                .stream()
//                                                                .map(a -> String.valueOf(a.valueEn))
//                                                                .collect(Collectors.joining(","));
//                                                    } else {
//                                                        if (i != seedTypes.size() - 1) {
//                                                            seedData = seedData.concat(seedTypes.get(i).valueEn).concat(", ");
//                                                        } else {
//                                                            seedData = seedData.concat(seedTypes.get(i).valueEn).concat(" ");
//                                                        }
//                                                    }
                                                }
                                            }
                                        }
//                                    }
                                    } else {
                                        Long asd = Long.parseLong(strSeedType.trim());
                                        seedData = "";
                                        for (int i = 0; i < seedTypes.size(); i++) {
                                            if (asd.equals(seedTypes.get(i).valueId)) {
                                                selectedSeedTypes.add(asd);
                                                seedTypes.get(i).isSelected = true;
                                                seedData = seedData.concat(seedTypes.get(i).valueEn).concat(" ");
                                            }
                                        }
                                    }
                                    binding.tvSpnSeedData.setText(seedData);
                                    if (seedData.endsWith(", ")) {
                                        binding.tvSpnSeedData.setText(seedData.substring(0, seedData.length() - 2));
                                    } else {
                                        binding.tvSpnSeedData.setText(seedData);
                                    }
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
                                                irriData = irriData.concat(irrigationSources.get(i).valueEn).concat(" ");
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
                                    }

//                                    binding.etFarmersNo.setText(noOfFarmer);
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
        binding.spnAddShgCommencementSeason.setEnabled(false);
        binding.spnAddShgSelectShg.setEnabled(false);
        binding.spnAddShgUomProduction.setEnabled(false);
        binding.multipleItemSelectionSpinner.setEnabled(false);
        binding.rvFundSource.setEnabled(false);
        binding.rvIrrigationSouce.setEnabled(false);
        binding.rvSeedType.setEnabled(false);
        binding.etBaseCapital.setEnabled(false);
        binding.etCommenceDate.setEnabled(false);
        binding.etRemarks.setEnabled(false);
        binding.addHhSubmit.setEnabled(false);
        binding.spnMemberHh.setEnabled(false);

        binding.spnSeedtype.setEnabled(false);
        binding.spnFundSources.setEnabled(false);
        binding.multipleItemSelectionSpinner.setEnabled(false);
    }

    private void enableFields() {
        binding.spnAddShgUomSales.setEnabled(true);
        binding.spnAddShgCommencementSeason.setEnabled(true);
        binding.spnAddShgSelectShg.setEnabled(true);
        binding.spnMemberHh.setEnabled(true);
        binding.spnAddShgUomProduction.setEnabled(true);
        binding.multipleItemSelectionSpinner.setEnabled(true);
        binding.rvFundSource.setEnabled(true);
        binding.rvIrrigationSouce.setEnabled(true);
        binding.rvSeedType.setEnabled(true);
        binding.etBaseCapital.setEnabled(true);
        binding.etCommenceDate.setEnabled(true);
        binding.etRemarks.setEnabled(true);
        binding.addHhSubmit.setEnabled(true);

        binding.multipleItemSelectionSpinner.setEnabled(true);
        binding.spnFundSources.setEnabled(true);
        binding.spnSeedtype.setEnabled(true);
    }

    private void doSubmitShgActivity() {
        binding.progreses.setVisibility(View.GONE);


        /**
         * {
         *     "schemeId":2,
         *     "activityId":18,
         *     "entityId":147637,
         *     "entityTypeCode": "SHG",
         *     "numFarmers" : 15,
         *     "seedTypeId" : 106,
         *     "irrigationSource" : "101,104" ,
         *     "fundSource" : "105" ,
         *     "baseCapital" : 25000.00 ,
         *     "commencementSeasonId" : 109,
         *     "commencementDate" : "26/03/2019" ,
         *     "remarks" : "blyat" ,
         *     "soldUomId" : 114,
         *     "productionUomId" : 114
         * }
         * */

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
            reqObj.put("entityTypeCode", "HH");
            reqObj.put("numFarmers", 0);
            reqObj.put("seedType", selectedSeedTypes.toString());
            reqObj.put("irrigationSource", selectedIrrigationSources.toString());
            reqObj.put("fundSource", selectedFundSources.toString());
            reqObj.put("baseCapital", baseCapital);
            reqObj.put("commencementSeasonId", commencementSeasonId);
            reqObj.put("commencementDate", commencementDate);
            reqObj.put("remarks", remarks);
            reqObj.put("soldUomId", soldUomId);
            reqObj.put("productionUomId", productionUomId);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "farm-activity/save")
                .addHeaders("Authorization", "Bearer " + token)
                .addJSONObjectBody(reqObj)
                .setTag("shglist")
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

                                    Toast.makeText(AddFarmingHHActivity.this, resObj.optString("message"), Toast.LENGTH_LONG)
                                            .show();
                                } else {
                                    Toast.makeText(AddFarmingHHActivity.this, resObj.optString("message"), Toast.LENGTH_LONG)
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

    private void showDialog() {
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
        final DatePickerDialog datePickerDialog = new DatePickerDialog(AddFarmingHHActivity.this, new DatePickerDialog.OnDateSetListener() {
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
            object.put("schemeId", selectedSchemeId);
            object.put("activityId", selectedActivityId);
            object.put("entityId", selectedShgId);
            object.put("reportingLevelCode", reportingLevelCode);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "farm-activity/hh/list")
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

    private void getShgList() {

        JSONObject obj = new JSONObject();
        try {
            obj.put("schemeId", selectedSchemeId);
            obj.put("activityId", selectedActivityId);
            obj.put("reportingLevelCode", "HH");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "farm-activity/search-shg")
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
                                        if (isSeedType && isIrrigation && isFundSource && isCommencement && isUom) {
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
                                        setupIrrigationAdapter(AddFarmingHHActivity.this, irrigationSources);
                                        irrigationAdapter.notifyDataSetChanged();

                                        isIrrigation = true;
                                        if (isSeedType && isIrrigation && isFundSource && isCommencement && isUom) {
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
                                        if (isSeedType && isIrrigation && isFundSource && isCommencement && isUom) {
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
                                        setupFundSourceAdapter(AddFarmingHHActivity.this, fundSources);
                                        fundSourceAdapter.notifyDataSetChanged();
                                        isFundSource = true;
                                        if (isSeedType && isIrrigation && isFundSource && isCommencement && isUom) {
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

    private void getSeedType() {
        AndroidNetworking.get(BuildConfig.BASE_URL + Constant.API_PATH + "lov/seedType")
                .addHeaders("Authorization", "Bearer " + token)
                .setTag("seedType")
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
                                        seedTypes.clear();
                                        for (int i = 0; i < array.length(); i++) {
                                            SeedType scheme = SeedType.parseSeedType(array.optJSONObject(i));
                                            seedTypes.add(scheme);
                                        }
                                        setupSeedTypeAdapter(AddFarmingHHActivity.this, seedTypes);
                                        seedTypeAdapter.notifyDataSetChanged();
                                        isSeedType = true;
                                        if (isSeedType && isIrrigation && isFundSource && isCommencement && isUom) {
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFundSourceSelect(int pos, Long selectedId, boolean isSelect) {
        if (isSelect) {
            selectedFundSources.add(selectedId);
        } else {
            selectedFundSources.remove(selectedId);
        }
    }

    @Override
    public void onIrrigationSelect(int pos, Long selectedId, boolean isSelect) {
        if (isSelect) {
            selectedIrrigationSources.add(selectedId);
        } else {
            selectedIrrigationSources.remove(selectedId);
        }
    }

    @Override
    public void onSeedTypeSelect(int pos, Long selectedId, boolean isSelect) {
        if (isSelect) {
            selectedSeedTypes.add(selectedId);
        } else {
            selectedSeedTypes.remove(selectedId);
        }
    }
}