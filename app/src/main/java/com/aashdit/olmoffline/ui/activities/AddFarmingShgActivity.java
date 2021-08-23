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
import com.aashdit.olmoffline.adapters.IrrigationAdapter;
import com.aashdit.olmoffline.adapters.SeedTypeAdapter;
import com.aashdit.olmoffline.adapters.SeedTypeSpnAdapter;
import com.aashdit.olmoffline.adapters.ShgSpnAdapter;
import com.aashdit.olmoffline.adapters.UOMSpnAdapter;
import com.aashdit.olmoffline.adapters.UOMSpnProdAdapter;
import com.aashdit.olmoffline.databinding.ActivityAddFarmingShgBinding;
import com.aashdit.olmoffline.models.CommencementSeason;
import com.aashdit.olmoffline.models.FundSource;
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

public class AddFarmingShgActivity extends AppCompatActivity implements IrrigationAdapter.IrrigationSelectListener,
        FundSourceAdapter.FundSourceSelectListener, SeedTypeAdapter.SeedTypeSelectListener {

    private static final String TAG = "AddFarmingShgActivity";
    private final ArrayList<SHG> shgArrayList = new ArrayList<>();
    ArrayList<Long> selectedFundSources = new ArrayList<>();
    ArrayList<Long> selectedIrrigationSources = new ArrayList<>();
    ArrayList<Long> selectedSeedTypes = new ArrayList<>();
    //    ArrayList<Long> seedType = new ArrayList<>();
//    ArrayList<Long> irrigationSource = new ArrayList<>();
//    ArrayList<Long> fundSourcesAsLong = new ArrayList<>();
    //Seed Type
    private ActivityAddFarmingShgBinding binding;
    private String token, entityTypeCode;
    private SharedPrefManager sp;
    private Long selectedSchemeId, selectedActivityId, entityId;
    private ShgSpnAdapter shgSpnAdapter;
    private SeedTypeSpnAdapter seedTypeSpnAdapter;
    private FundSourceAdapter fundSourceAdapter;
    private SeedTypeAdapter seedTypeAdapter;
    private IrrigationAdapter irrigationAdapter;
    private CommencementSpnAdapter commencementSpnAdapter;
    private UOMSpnAdapter uomSpnAdapter;
    private UOMSpnProdAdapter uomSpnProdAdapter;
    private String noOfFarmer, baseCapital, commencementDate, remarks;
    private Long selectedShgId = 0L;
    //    private Long selectedSeedType = 0L;
    private Long selectedIrrigationSource = 0L;
    private Long commencementSeasonId = 0L;
    private Long soldUomId = 0L;
    private Long productionUomId = 0L;
    private ArrayList<FundSource> fundSources = new ArrayList<>();
    private ArrayList<SeedType> seedTypes = new ArrayList<>();
    private ArrayList<CommencementSeason> commencementSeasons = new ArrayList<>();
    private ArrayList<IrrigationSource> irrigationSources = new ArrayList<>();
    private ArrayList<UOM> uoms = new ArrayList<>();
    private ArrayList<UOM> uomsProd = new ArrayList<>();
    private String intentType;
    private boolean enableCheckBoxWhileUpdate = false;
    private Long registrationId;
    private String shgName;

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

    private void setupFundSourceAdapter(Context context, ArrayList<FundSource> fundSources) {
        fundSourceAdapter = new FundSourceAdapter(context, fundSources);
        fundSourceAdapter.setFundSourceSelectListener(this);
        binding.rvFundSource.setAdapter(fundSourceAdapter);
    }

    private void setupSeedTypeAdapter(Context context, ArrayList<SeedType> seedTypes) {
        seedTypeAdapter = new SeedTypeAdapter(context, seedTypes);
        seedTypeAdapter.setSeedTypeSelectListener(this);
        binding.rvSeedType.setAdapter(seedTypeAdapter);
    }

    private void setupIrrigationAdapter(Context context, ArrayList<IrrigationSource> irrigationSources) {
        irrigationAdapter = new IrrigationAdapter(context, irrigationSources);
        irrigationAdapter.setIrrigationSelectListener(this);
        binding.rvIrrigationSouce.setAdapter(irrigationAdapter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddFarmingShgBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        resetTitles();
        setSupportActionBar(binding.toolbarAddFarmShg);
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
        shgName = getIntent().getStringExtra("ENTITY_NAME");

        if (entityTypeCode.equals("SHG")) {
            binding.tvDiaryTitle.setText(getResources().getString(R.string.add_shg_activity));
        } else if (entityTypeCode.equals("HH")) {
            binding.tvDiaryTitle.setText(getResources().getString(R.string.add_hh_activity));
        } else if (entityTypeCode.equals("CLF")) {
            binding.tvDiaryTitle.setText(getResources().getString(R.string.add_clf_activity));
        }
        if (intentType != null) {
            if (intentType.equals(Constant.ADD)) {
                binding.ivEdit.setVisibility(View.GONE);
                binding.tvDiaryTitle.setText(getResources().getString(R.string.add_shg_activity));
                binding.tvSelectShgLblUpdate.setVisibility(View.GONE);
                binding.spnAddShgSelectShg.setVisibility(View.VISIBLE);
//                binding.tvSelectShgLbl.setVisibility(View.VISIBLE);

                enableFields();
            } else {
                binding.ivEdit.setVisibility(View.VISIBLE);
                binding.tvDiaryTitle.setText(getResources().getString(R.string.farming_update_shg));
                binding.tvSelectShgLblUpdate.setText(shgName);
                binding.tvSelectShgLblUpdate.setVisibility(View.VISIBLE);
                binding.spnAddShgSelectShg.setVisibility(View.GONE);
                binding.tvSelectShgLbl.setVisibility(View.GONE);

                disableFields();
            }
        }
        binding.ivEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.ivEdit.setColorFilter(ContextCompat.getColor(AddFarmingShgActivity.this, R.color.purple_500), android.graphics.PorterDuff.Mode.MULTIPLY);
                enableFields();
                enableCheckBoxWhileUpdate = true;
                seedTypeAdapter.setEnable(enableCheckBoxWhileUpdate);
                seedTypeAdapter.notifyDataSetChanged();
            }
        });

        binding.etCommenceDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDatePicker();
            }
        });
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
        binding.spnAddShgSelectShg.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    selectedShgId = 0L;
                } else {
                    selectedShgId = shgArrayList.get(position).getShgDetailsId();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        binding.rvSeedType.setLayoutManager(new LinearLayoutManager(this));
        setupSeedTypeAdapter(this, seedTypes);

        binding.rvFundSource.setLayoutManager(new LinearLayoutManager(this));
        setupFundSourceAdapter(this, fundSources);

        binding.rvIrrigationSouce.setLayoutManager(new LinearLayoutManager(this));
        setupIrrigationAdapter(this, irrigationSources);

        SHG shg = new SHG();
        shg.setShgName("Select SHG");
        shg.setShgDetailsId(0L);
        shg.setShgRegNumber("SHG");
        shg.setShgType(0L);
        shgArrayList.add(shg);
        shgSpnAdapter = new ShgSpnAdapter(this, shgArrayList);
        binding.spnAddShgSelectShg.setAdapter(shgSpnAdapter);

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

//        SeedType seedType = new SeedType();
//        seedType.valueEn = "Select SeedType";
//        seedType.valueId = 0L;
//        seedTypes.add(seedType);
//        seedTypeSpnAdapter = new SeedTypeSpnAdapter(this, seedTypes);
//        binding.spnAddShgTypeSeed.setAdapter(seedTypeSpnAdapter);

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

//        binding.spnFundSources.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
//                Toast.makeText(AddFarmingShgActivity.this, "Clicked", Toast.LENGTH_SHORT).show();
//                if (intentType.equals(Constant.UPDATE)){
//                    for (int i = 0; i < seedTypes.size(); i++) {
//                        if (selectedSeedTypes.get(i).equals(seedTypes.get(i).valueId)) {
//                            SeedType seedType = new SeedType();
//                            seedType.valueId = seedTypes.get(i).valueId;
//                            seedType.valueEn = seedTypes.get(i).valueEn;
//                            seedType.isSelected = true;
//
//                            seedTypes.set(i,seedType);
//                        }
//                    }
//                }
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> adapterView) {
//
//            }
//        });
        binding.spnFundSources.setSearchEnabled(true);
//        binding.spnFundSources.setHintText("Select");
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

        binding.progreses.setVisibility(View.GONE);
        binding.addShgSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                noOfFarmer = binding.etFarmersNo.getText().toString().trim();
                baseCapital = binding.etBaseCapital.getText().toString().trim();
                commencementDate = binding.etCommenceDate.getText().toString().trim();
                remarks = binding.etRemarks.getText().toString().trim();

                if (intentType.equals(Constant.ADD) && selectedShgId.equals(0L)) {
                    Toast.makeText(AddFarmingShgActivity.this, "Please select a SHG", Toast.LENGTH_SHORT).show();
                } else if (noOfFarmer.isEmpty()) {
                    Toast.makeText(AddFarmingShgActivity.this, "Please fill No of Farmer", Toast.LENGTH_SHORT).show();
                } else if (selectedSeedTypes.size() == 0) {
                    Toast.makeText(AddFarmingShgActivity.this, "Please Select SeedType", Toast.LENGTH_SHORT).show();
                } else if (baseCapital.isEmpty()) {
                    Toast.makeText(AddFarmingShgActivity.this, "Please fill Base Capital", Toast.LENGTH_SHORT).show();
                } else if (selectedIrrigationSources.size() == 0) {
                    Toast.makeText(AddFarmingShgActivity.this, "Please Select Irrigation Source", Toast.LENGTH_SHORT).show();
                } else if (soldUomId.equals(0L)) {
                    Toast.makeText(AddFarmingShgActivity.this, "Please Select Sales Unit", Toast.LENGTH_SHORT).show();
                } else if (productionUomId.equals(0L)) {
                    Toast.makeText(AddFarmingShgActivity.this, "Please Select Production Unit", Toast.LENGTH_SHORT).show();
                } else if (selectedFundSources.size() == 0) {
                    Toast.makeText(AddFarmingShgActivity.this, "Please Select Fund Source", Toast.LENGTH_SHORT).show();
                } else if (commencementSeasonId.equals(0L)) {
                    Toast.makeText(AddFarmingShgActivity.this, "Please Select Commencement Season", Toast.LENGTH_SHORT).show();
                } else if (commencementDate.isEmpty()) {
                    Toast.makeText(AddFarmingShgActivity.this, "Please fill Commencement Date", Toast.LENGTH_SHORT).show();
                } else {
                    showDialog();
                }
            }
        });


        getShgList();
        getSeedType();
        irrigationSource();
        fundSource();
        commencementSeason();
        uom();

//        if (intentType.equals(Constant.UPDATE)) {
//            viewReg();
//        }


    }

    private boolean isSeedType, isIrrigation, isFundSource, isCommencement, isUom;
    String seedData = "";
    String irriData = "";
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
                                    noOfFarmer = String.valueOf(resObj.optLong("numFarmers"));
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
                                                }
                                            }
                                        }
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
//                                    binding.tvSpnSeedData.setText(seedData);
                                    if (seedData.endsWith(", ")) {
                                        binding.tvSpnSeedData.setText(seedData.substring(0,seedData.length()-2));
                                    }else{
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
//                                    binding.tvSpnFsData.setText(fundData);
                                    if (fundData.endsWith(", ")) {
                                        binding.tvSpnFsData.setText(fundData.substring(0,fundData.length()-2));
                                    }else{
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
                                        binding.tvSpnIrriData.setText(irriData.substring(0,irriData.length()-2));
                                    }else{
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

                                    binding.etFarmersNo.setText(noOfFarmer);
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
        binding.etFarmersNo.setEnabled(false);
        binding.etRemarks.setEnabled(false);
        binding.addShgSubmit.setEnabled(false);

        binding.spnSeedtype.setEnabled(false);
        binding.spnFundSources.setEnabled(false);
        binding.multipleItemSelectionSpinner.setEnabled(false);
    }

    private void enableFields() {
        binding.spnAddShgUomSales.setEnabled(true);
        binding.spnAddShgCommencementSeason.setEnabled(true);
        binding.spnAddShgSelectShg.setEnabled(true);
        binding.spnAddShgUomProduction.setEnabled(true);
        binding.multipleItemSelectionSpinner.setEnabled(true);
        binding.rvFundSource.setEnabled(true);
        binding.rvIrrigationSouce.setEnabled(true);
        binding.rvSeedType.setEnabled(true);
        binding.etBaseCapital.setEnabled(true);
        binding.etCommenceDate.setEnabled(true);
        binding.etFarmersNo.setEnabled(true);
        binding.etRemarks.setEnabled(true);
        binding.addShgSubmit.setEnabled(true);

        binding.multipleItemSelectionSpinner.setEnabled(true);
        binding.spnFundSources.setEnabled(true);
        binding.spnSeedtype.setEnabled(true);
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

//                                        if (intentType.equals(Constant.UPDATE)) {
//                                            for (int i = 0; i < uoms.size(); i++) {
//                                                if (soldUomId.equals(uoms.get(i).valueId)) {
//                                                    binding.spnAddShgUomSales.setSelection(i);
//                                                }
//                                                uomSpnAdapter.notifyDataSetChanged();
//                                            }
//                                            for (int i = 0; i < uomsProd.size(); i++) {
//                                                if (productionUomId.equals(uomsProd.get(i).valueId)) {
//                                                    binding.spnAddShgUomProduction.setSelection(i);
//                                                }
//                                                uomSpnProdAdapter.notifyDataSetChanged();
//                                            }
//                                        }
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
                                        setupIrrigationAdapter(AddFarmingShgActivity.this, irrigationSources);
                                        irrigationAdapter.notifyDataSetChanged();
                                    }
                                    isIrrigation = true;
                                    if (isSeedType && isIrrigation && isFundSource && isCommencement && isUom) {
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
                                        commencementSpnAdapter.notifyDataSetChanged();

                                        isCommencement = true;
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
                                        setupFundSourceAdapter(AddFarmingShgActivity.this, fundSources);
                                        fundSourceAdapter.notifyDataSetChanged();

                                        isFundSource = true;
                                        if (isSeedType && isIrrigation && isFundSource && isCommencement && isUom) {
                                            viewReg();
                                        }
                                        if (intentType.equals(Constant.UPDATE)) {
//                                            for (int i = 0; i < fundSources.size(); i++) {
//                                                for (int j = 0; j < selectedFundSources.get(j); j++) {
//                                                    if (fundSources.get(i).valueId.equals(selectedFundSources.get(j))) {
//                                                        FundSource fs = new FundSource();
//                                                        fs.valueId = fundSources.get(i).valueId;
//                                                        fs.valueEn = fundSources.get(i).valueEn;
//                                                        fs.isSelected = true;
//
//                                                        fundSources.set(i,fs);
//                                                    }
//                                                }
//
//                                            }
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
//                                        SeedType shg = new SeedType();
//                                        shg.valueEn = "Select SeedType";
//                                        shg.valueId = 0L;
//                                        shg.setShgRegNumber("SHG");
//                                        shg.setShgType(0L);
//                                        seedTypes.add(shg);
                                        for (int i = 0; i < array.length(); i++) {
                                            SeedType scheme = SeedType.parseSeedType(array.optJSONObject(i));
                                            seedTypes.add(scheme);
                                        }
//                                        setupSeedTypeAdapter(AddFarmingShgActivity.this, seedTypes);
//                                        seedTypeAdapter.notifyDataSetChanged();
//                                        seedTypeSpnAdapter.notifyDataSetChanged();

                                        isSeedType = true;
                                        if (isSeedType && isIrrigation && isFundSource && isCommencement && isUom) {
                                            viewReg();
                                        }
                                        if (intentType.equals(Constant.UPDATE)) {
                                            for (int i = 0; i < seedTypes.size(); i++) {
//                                                if (selectedSeedTypes.get(i).equals(seedTypes.get(i).valueId)) {
//                                                    SeedType seedType = new SeedType();
//                                                    seedType.valueId = seedTypes.get(i).valueId;
//                                                    seedType.valueEn = seedTypes.get(i).valueEn;
//                                                    seedType.isSelected = true;
//
//                                                    seedTypes.set(i, seedType);
//                                                }
                                            }
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
            obj.put("reportingLevelCode", "SHG");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "farm-activity/shg/list")
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

                                        if (intentType.equals(Constant.UPDATE)) {
                                            for (int i = 0; i < shgArrayList.size(); i++) {
                                                if (entityId.equals(shgArrayList.get(i).getShgId())) {
                                                    binding.spnAddShgSelectShg.setSelection(i);
                                                }

                                                shgSpnAdapter.notifyDataSetChanged();
                                            }
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


    private void doSubmitShgActivity() {


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

        binding.progreses.setVisibility(View.VISIBLE);

        JSONObject reqObj = new JSONObject();
        try {
            if (intentType.equals(Constant.UPDATE)){
                reqObj.put("registrationId",registrationId);
            }
            reqObj.put("schemeId", selectedSchemeId);
            reqObj.put("activityId", selectedActivityId);
            reqObj.put("entityId", selectedShgId);
            reqObj.put("entityTypeCode", entityTypeCode);
            reqObj.put("numFarmers", noOfFarmer);
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

                                    Toast.makeText(AddFarmingShgActivity.this, resObj.optString("message"), Toast.LENGTH_LONG)
                                            .show();
                                } else {
                                    Toast.makeText(AddFarmingShgActivity.this, resObj.optString("message"), Toast.LENGTH_LONG)
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


    private void openDatePicker() {
        Calendar myCalendar = Calendar.getInstance();
        final DatePickerDialog datePickerDialog = new DatePickerDialog(AddFarmingShgActivity.this, new DatePickerDialog.OnDateSetListener() {
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

    @Override
    public void onIrrigationSelect(int pos, Long selectedId, boolean isSelect) {
        if (isSelect) {
            selectedIrrigationSources.add(selectedId);
        } else {
            selectedIrrigationSources.remove(selectedId);
        }
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
    public void onSeedTypeSelect(int pos, Long selectedId, boolean isSelect) {
        if (isSelect) {
            selectedSeedTypes.add(selectedId);
        } else {
            selectedSeedTypes.remove(selectedId);
        }
    }
}