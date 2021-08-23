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
import com.aashdit.olmoffline.adapters.EGSpnAdapter;
import com.aashdit.olmoffline.adapters.FundSourceAdapter;
import com.aashdit.olmoffline.adapters.PGSpnAdapter;
import com.aashdit.olmoffline.adapters.ShgSpnAdapter;
import com.aashdit.olmoffline.adapters.UOMSpnAdapter;
import com.aashdit.olmoffline.adapters.UOMSpnProdAdapter;
import com.aashdit.olmoffline.adapters.VillageNfSpnAdapter;
import com.aashdit.olmoffline.databinding.ActivityNFSHGRegdBinding;
import com.aashdit.olmoffline.models.EG;
import com.aashdit.olmoffline.models.FundSource;
import com.aashdit.olmoffline.models.PG;
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

public class NFSHGRegdActivity extends AppCompatActivity {

    private static final String TAG = "NFSHGRegdActivity";
    private ActivityNFSHGRegdBinding binding;

    private String token, entityTypeCode,entityName;
    private SharedPrefManager sp;
    private Long selectedSchemeId, selectedActivityId;

    private String intentType;
    private boolean enableCheckBoxWhileUpdate = false;
    private Long registrationId;
    private String shgName;

    private final ArrayList<SHG> shgArrayList = new ArrayList<>();
    ArrayList<Long> selectedFundSources = new ArrayList<>();
    private ArrayList<UOM> uoms = new ArrayList<>();
    private ArrayList<UOM> uomsProd = new ArrayList<>();
    private ArrayList<VillageForNF> villageForNF = new ArrayList<>();
    private ArrayList<PG> pgList = new ArrayList<>();
    private ArrayList<EG> egList = new ArrayList<>();

    private Long selectedVillageId = 0L;
    private Long commencementSeasonId = 0L;
    private Long soldUomId = 0L;
    private Long productionUomId = 0L;
    private Long entityId = 0L;

    private PGSpnAdapter pgSpnAdapter;
    private EGSpnAdapter egSpnAdapter;
    private UOMSpnAdapter uomSpnAdapter;
    private UOMSpnProdAdapter uomSpnProdAdapter;
    private String noOfMembers, baseCapital, commencementDate, remarks;
    private Long selectedShgId = 0L;
    private FundSourceAdapter fundSourceAdapter;
    private ShgSpnAdapter shgSpnAdapter;
    private VillageNfSpnAdapter villageNfSpnAdapter;


    private ArrayList<FundSource> fundSources = new ArrayList<>();

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

    //    private SeedTypeAdapter seedTypeAdapter;
    private void setupFundSourceAdapter(Context context, ArrayList<FundSource> fundSources) {
        fundSourceAdapter = new FundSourceAdapter(context, fundSources);
//        fundSourceAdapter.setFundSourceSelectListener(this);
        binding.rvFundSource.setAdapter(fundSourceAdapter);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNFSHGRegdBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        resetTitles();

        setSupportActionBar(binding.toolbarAddNonFarmShg);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        sp = SharedPrefManager.getInstance(this);
        token = sp.getStringData(Constant.APP_TOKEN);

        intentType = getIntent().getStringExtra(Constant.INTENT_TYPE);

        selectedSchemeId = getIntent().getLongExtra("SCHEME_ID", 0L);
        selectedActivityId = getIntent().getLongExtra("ACTIVITY_ID", 0L);
        entityId = getIntent().getLongExtra("ENTITY_ID",0L);
        entityTypeCode = getIntent().getStringExtra("ENTITY_TYPE_CODE");
        entityName = getIntent().getStringExtra("ENTITY_NAME");


        if (entityTypeCode.equals("SHG")) {
            binding.tvDiaryTitle.setText("Add "+entityTypeCode+" Activity");
            binding.tvSelectShgLbl.setText("Select "+entityTypeCode);
            binding.spnAddShgSelectShg.setHint("Select "+entityTypeCode);
            shgSpnAdapter = new ShgSpnAdapter(this, shgArrayList);
            binding.spnAddShgSelectShg.setAdapter(shgSpnAdapter);
        } else if (entityTypeCode.equals("HH")) {
            binding.tvDiaryTitle.setText("Add "+entityTypeCode+" Activity");
        } else if (entityTypeCode.equals("CLF")) {
            binding.tvDiaryTitle.setText(getResources().getString(R.string.add_clf_activity));
        }else if (entityTypeCode.equals("EG")) {
            EG shg = new EG();
            shg.entityName="Select "+entityTypeCode;
            shg.entityId=0L;
            egList.add(shg);
            binding.tvDiaryTitle.setText("Add "+entityTypeCode+" Activity");
            binding.tvSelectShgLbl.setText("Select "+entityTypeCode);
            binding.spnAddShgSelectShg.setHint("Select "+entityTypeCode);
            binding.tvSelectVillageLbl.setVisibility(View.GONE);
            binding.cvVillageContainer.setVisibility(View.GONE);
            egSpnAdapter = new EGSpnAdapter(this,egList);
            binding.spnAddShgSelectShg.setAdapter(egSpnAdapter);
            getEgList();
        } else if (entityTypeCode.equals("PG")) {
            PG shg = new PG();
            shg.entityName="Select "+entityTypeCode;
            shg.entityId=0L;
            pgList.add(shg);
            binding.tvDiaryTitle.setText("Add "+entityTypeCode+" Activity");
            binding.tvSelectShgLbl.setText("Select "+entityTypeCode);
            binding.spnAddShgSelectShg.setHint("Select "+entityTypeCode);
            binding.tvSelectVillageLbl.setVisibility(View.GONE);
            binding.cvVillageContainer.setVisibility(View.GONE);
            pgSpnAdapter = new PGSpnAdapter(this,pgList);
            binding.spnAddShgSelectShg.setAdapter(pgSpnAdapter);
            getPgList();
        }

        binding.progreses.setVisibility(View.GONE);
        if (intentType != null) {
            if (intentType.equals(Constant.ADD)) {
                binding.ivEdit.setVisibility(View.GONE);
                binding.tvDiaryTitle.setText("Add "+entityTypeCode+" Activity");
                binding.tvSelectShgLblUpdate.setVisibility(View.GONE);
                binding.spnAddShgSelectShg.setVisibility(View.VISIBLE);
                binding.tvSelectShgLbl.setVisibility(View.GONE);

                enableFields();
            } else {
                if (entityTypeCode.equals("SHG")){
                    binding.tvSelectVillageLbl.setVisibility(View.GONE);
                    binding.cvVillageContainer.setVisibility(View.GONE);
                }
                binding.ivEdit.setVisibility(View.VISIBLE);
                binding.tvDiaryTitle.setText("Update "+entityTypeCode+" Activity");
                binding.tvSelectShgLblUpdate.setText(shgName);
                binding.tvSelectShgLblUpdate.setVisibility(View.VISIBLE);
                binding.spnAddShgSelectShg.setVisibility(View.GONE);
                binding.tvSelectShgLbl.setVisibility(View.GONE);

                disableFields();
            }
        }

        VillageForNF village = new VillageForNF();
        village.entityId = 0L;
        village.entityName = "Select Village";

        villageForNF.add(village);

        villageNfSpnAdapter = new VillageNfSpnAdapter(this,villageForNF);
        binding.spnAddShgSelectVillage.setAdapter(villageNfSpnAdapter);
        binding.spnAddShgSelectVillage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    selectedVillageId = 0L;
                    selectedShgId = 0L;
                    shgArrayList.clear();
                    SHG shg = new SHG();
                    shg.setShgName("Select "+entityTypeCode);
                    shg.setShgDetailsId(0L);
                    shg.setShgRegNumber("SHG");
                    shg.setShgType(0L);
                    shgArrayList.add(shg);
//                    shgSpnAdapter.notifyDataSetChanged();

                    shgSpnAdapter = null;
                    shgSpnAdapter = new ShgSpnAdapter(NFSHGRegdActivity.this, shgArrayList);
                    binding.spnAddShgSelectShg.setAdapter(shgSpnAdapter);

                }else{
                    selectedShgId = 0L;
                    selectedVillageId = villageForNF.get(position).entityId;
                    shgArrayList.clear();
                    SHG shg = new SHG();
                    shg.setShgName("Select "+entityTypeCode);
                    shg.setShgDetailsId(0L);
                    shg.setShgRegNumber("SHG");
                    shg.setShgType(0L);
                    shgArrayList.add(shg);
//                    shgSpnAdapter.notifyDataSetChanged();

                    shgSpnAdapter = null;
                    shgSpnAdapter = new ShgSpnAdapter(NFSHGRegdActivity.this, shgArrayList);
                    binding.spnAddShgSelectShg.setAdapter(shgSpnAdapter);
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
                } else {
                    if (entityTypeCode.equals("SHG")) {
                        selectedShgId = shgArrayList.get(position).getShgDetailsId();
                    }else if (entityTypeCode.equals("PG")) {
                        selectedShgId = pgList.get(position).entityId;
                    }else if (entityTypeCode.equals("EG")) {
                        selectedShgId = egList.get(position).entityId;
                    }
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

        binding.ivEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.ivEdit.setColorFilter(ContextCompat.getColor(NFSHGRegdActivity.this, R.color.purple_500), android.graphics.PorterDuff.Mode.MULTIPLY);
                enableFields();
                enableCheckBoxWhileUpdate = true;
            }
        });

        binding.etCommenceDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDatePicker();
            }
        });

        binding.addShgSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noOfMembers = binding.etFarmersNo.getText().toString().trim();
                baseCapital = binding.etBaseCapital.getText().toString().trim();
                remarks = binding.etRemarks.getText().toString().trim();
                commencementDate = binding.etCommenceDate.getText().toString().trim();

                if (intentType.equals(Constant.ADD) && entityTypeCode.equals("SHG") && selectedVillageId.equals(0L)) {
                    Toast.makeText(NFSHGRegdActivity.this, "Please select a Village", Toast.LENGTH_SHORT).show();
                } else if (intentType.equals(Constant.ADD) && selectedShgId.equals(0L)) {
                    Toast.makeText(NFSHGRegdActivity.this, "Please select a "+entityTypeCode, Toast.LENGTH_SHORT).show();
                } else if (noOfMembers.isEmpty()) {
                    Toast.makeText(NFSHGRegdActivity.this, "Please fill No of Members", Toast.LENGTH_SHORT).show();
                } else if (baseCapital.isEmpty()) {
                    Toast.makeText(NFSHGRegdActivity.this, "Please fill Base Capital", Toast.LENGTH_SHORT).show();
                } else if (selectedFundSources.size() == 0) {
                    Toast.makeText(NFSHGRegdActivity.this, "Please Select Fund Source", Toast.LENGTH_SHORT).show();
                } else if (soldUomId.equals(0L)) {
                    Toast.makeText(NFSHGRegdActivity.this, "Please Select Sales Unit", Toast.LENGTH_SHORT).show();
                } else if (productionUomId.equals(0L)) {
                    Toast.makeText(NFSHGRegdActivity.this, "Please Select Production Unit", Toast.LENGTH_SHORT).show();
                } else if (commencementDate.isEmpty()) {
                    Toast.makeText(NFSHGRegdActivity.this, "Please fill Commencement Date", Toast.LENGTH_SHORT).show();
                } else {
                    showDialog();
                }

            }
        });

        fundSource();
        uom();
        getVilageList();
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
            selectedFundSources.clear();
            for (int i = 0; i < items.size(); i++) {
                Log.i(TAG, i + " : " + items.get(i).valueEn + " : " + items.get(i).isSelected + " ID " + items.get(i).valueId);
                if (!selectedFundSources.contains(items.get(i).valueId))
                    selectedFundSources.add(items.get(i).valueId);
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
                                    noOfMembers = String.valueOf(resObj.optLong("numMembers"));
                                    baseCapital = String.valueOf(resObj.optLong("baseCapital"));

                                    commencementSeasonId = resObj.optLong("commencementSeasonId");
                                    commencementDate = resObj.optString("commencementDate");
                                    remarks = resObj.optString("remarks");
                                    soldUomId = resObj.optLong("salesUomId");
                                    productionUomId = resObj.optLong("productionUomId");
                                    selectedShgId = resObj.optLong("entityId");

                                    binding.tvSelectShgLblUpdate.setText(entityName);
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

                                    binding.etFarmersNo.setText(noOfMembers);
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
                                        setupFundSourceAdapter(NFSHGRegdActivity.this, fundSources);
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

    private void getPgList() {

        JSONObject obj = new JSONObject();
        try {
            obj.put("schemeId", selectedSchemeId);
            obj.put("activityId", selectedActivityId);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "non-farm-activity/pg/list")
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
                                        pgList.clear();
                                        PG shg = new PG();
                                        shg.entityName="Select "+entityTypeCode;
                                        shg.entityId=0L;
                                        pgList.add(shg);
                                        for (int i = 0; i < array.length(); i++) {
                                            PG scheme = PG.parsePG(array.optJSONObject(i));
                                            pgList.add(scheme);
                                        }
                                        pgSpnAdapter.notifyDataSetChanged();
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

    private void getEgList() {


        JSONObject obj = new JSONObject();
        try {
            obj.put("schemeId", selectedSchemeId);
            obj.put("activityId", selectedActivityId);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "non-farm-activity/eg/list")
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
                                        egList.clear();
                                        EG shg = new EG();
                                        shg.entityName="Select "+entityTypeCode;
                                        shg.entityId=0L;
                                        egList.add(shg);
                                        for (int i = 0; i < array.length(); i++) {
                                            EG scheme = EG.parseEG(array.optJSONObject(i));
                                            egList.add(scheme);
                                        }
                                        egSpnAdapter.notifyDataSetChanged();
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

        String listUrl =  "";
        String rlc =  "";
        if (entityTypeCode.equals("SHG")){
            listUrl = "non-farm-activity/shg/list";
            rlc = "SHG";
        }
        JSONObject obj = new JSONObject();
        try {
            obj.put("schemeId", selectedSchemeId);
            obj.put("activityId", selectedActivityId);
            obj.put("villageId", selectedVillageId);
            obj.put("reportingLevelCode", rlc);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + listUrl)
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
                                        shg.setShgName("Select "+entityTypeCode);
                                        shg.setShgDetailsId(0L);
                                        shg.setShgRegNumber("SHG");
                                        shg.setShgType(0L);
                                        shgArrayList.add(shg);
                                        for (int i = 0; i < array.length(); i++) {
                                            SHG scheme = SHG.parseShg(array.optJSONObject(i));
                                            shgArrayList.add(scheme);
                                        }

                                        shgSpnAdapter.notifyDataSetChanged();

//                                        if (intentType.equals(Constant.UPDATE)) {
//                                            for (int i = 0; i < shgArrayList.size(); i++) {
//                                                if (entityId.equals(shgArrayList.get(i).getShgId())) {
//                                                    binding.spnAddShgSelectShg.setSelection(i);
//                                                }
//
//                                                shgSpnAdapter.notifyDataSetChanged();
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


    private void showDialog() {
        if (!NFSHGRegdActivity.this.isFinishing()) {
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
            if (intentType.equals(Constant.UPDATE)) {
                reqObj.put("registrationId", registrationId);
            }
            reqObj.put("schemeId", selectedSchemeId);
            reqObj.put("activityId", selectedActivityId);
            reqObj.put("entityId", selectedShgId);
            reqObj.put("entityTypeCode", entityTypeCode);
            reqObj.put("numMembers", noOfMembers);
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

                                    Toast.makeText(NFSHGRegdActivity.this, resObj.optString("message"), Toast.LENGTH_LONG)
                                            .show();
                                } else {
                                    Toast.makeText(NFSHGRegdActivity.this, resObj.optString("message"), Toast.LENGTH_LONG)
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


    private void disableFields() {
        binding.spnAddShgUomSales.setEnabled(false);
//        binding.spnAddShgCommencementSeason.setEnabled(false);
        binding.spnAddShgSelectShg.setEnabled(false);
        binding.spnAddShgUomProduction.setEnabled(false);
//        binding.multipleItemSelectionSpinner.setEnabled(false);
        binding.rvFundSource.setEnabled(false);
//        binding.rvIrrigationSouce.setEnabled(false);
//        binding.rvSeedType.setEnabled(false);
        binding.etBaseCapital.setEnabled(false);
        binding.etCommenceDate.setEnabled(false);
        binding.etFarmersNo.setEnabled(false);
        binding.etRemarks.setEnabled(false);
        binding.addShgSubmit.setEnabled(false);

//        binding.spnSeedtype.setEnabled(false);
        binding.spnFundSources.setEnabled(false);
//        binding.multipleItemSelectionSpinner.setEnabled(false);
    }

    private void enableFields() {
        binding.spnAddShgUomSales.setEnabled(true);
//        binding.spnAddShgCommencementSeason.setEnabled(true);
        binding.spnAddShgSelectShg.setEnabled(true);
        binding.spnAddShgUomProduction.setEnabled(true);
//        binding.multipleItemSelectionSpinner.setEnabled(true);
        binding.rvFundSource.setEnabled(true);
//        binding.rvIrrigationSouce.setEnabled(true);
//        binding.rvSeedType.setEnabled(true);
        binding.etBaseCapital.setEnabled(true);
        binding.etCommenceDate.setEnabled(true);
        binding.etFarmersNo.setEnabled(true);
        binding.etRemarks.setEnabled(true);
        binding.addShgSubmit.setEnabled(true);

//        binding.multipleItemSelectionSpinner.setEnabled(true);
        binding.spnFundSources.setEnabled(true);
//        binding.spnSeedtype.setEnabled(true);
    }

    private void openDatePicker() {
        Calendar myCalendar = Calendar.getInstance();
        final DatePickerDialog datePickerDialog = new DatePickerDialog(NFSHGRegdActivity.this, new DatePickerDialog.OnDateSetListener() {
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