package com.aashdit.olmoffline.ui.activities;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.aashdit.olmoffline.BuildConfig;
import com.aashdit.olmoffline.R;
import com.aashdit.olmoffline.adapters.AutoSuggestAdapter;
import com.aashdit.olmoffline.adapters.BlockSpnAdapter;
import com.aashdit.olmoffline.adapters.BookKeepersSpnAdapter;
import com.aashdit.olmoffline.adapters.BranchSpnAdapter;
import com.aashdit.olmoffline.adapters.DistrictSpnAdapter;
import com.aashdit.olmoffline.adapters.GpSpnAdapter;
import com.aashdit.olmoffline.adapters.MeetingFrequencySpnAdapter;
import com.aashdit.olmoffline.adapters.PromotedBySpnAdapter;
import com.aashdit.olmoffline.adapters.ShgTypeSpnAdapter;
import com.aashdit.olmoffline.adapters.VillageSpnAdapter;
import com.aashdit.olmoffline.databinding.ActivityCreateShgProfileBinding;
import com.aashdit.olmoffline.models.BankBranch;
import com.aashdit.olmoffline.models.Block;
import com.aashdit.olmoffline.models.BookKeepers;
import com.aashdit.olmoffline.models.District;
import com.aashdit.olmoffline.models.Frequency;
import com.aashdit.olmoffline.models.GP;
import com.aashdit.olmoffline.models.Promotor;
import com.aashdit.olmoffline.models.SHG;
import com.aashdit.olmoffline.models.ShgType;
import com.aashdit.olmoffline.models.Village;
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
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

import static android.content.pm.PackageManager.GET_META_DATA;

public class CreateShgProfileActivity extends AppCompatActivity {

    private static final String TAG = "CreateShgProfileActivit";
    private static final int TRIGGER_AUTO_COMPLETE = 100;

//    final Calendar myCalendar = Calendar.getInstance();
    private static final long AUTO_COMPLETE_DELAY = 300;
    private final int Year = 0;
    private final int Month = 0;
    private final ArrayList<District> districts = new ArrayList<>();
    private final ArrayList<Block> blocks = new ArrayList<>();
    private final ArrayList<GP> gps = new ArrayList<>();
    private final ArrayList<Village> villages = new ArrayList<>();
    private final ArrayList<ShgType> shgTypes = new ArrayList<>();
    private final ArrayList<Promotor> promotors = new ArrayList<>();
    private final ArrayList<Frequency> meetingFrequency = new ArrayList<>();
    private final ArrayList<BookKeepers> bookKeepers = new ArrayList<>();
    private final String branchName = "";
    private final boolean isBlockSelectedOnce = false;
    private final ArrayList<BankBranch> branchList = new ArrayList<>();
    private final ArrayList<String> bankList = new ArrayList<>();
    private final int type_0 = 0;//date of shg formation
    private final int type_1 = 1;//date of cooption into NRLM
    private final int type_2 = 2;//date of account opening
    private ActivityCreateShgProfileBinding binding;
    private Long selectedDistId = 0L;
    private Long selectedBlockId = 0L;
    private Long selectedGpId = 0L;
    private Long selectedVillageId = 0L;
    private Long selectedShgTypeId = 0L;
    private Long selectedPromotorId = 0L;
    private Long selectedMeetingFrequencyId = 0L;
    private Long selectedBookKeeperId = 0L;
    private Long selectedBranchId = 0L;
    private Long selectedShgId = 0L;
    private SharedPrefManager sp;
    private String token;
    private DistrictSpnAdapter districtSpnAdapter;
    private BlockSpnAdapter blockSpnAdapter;
    private GpSpnAdapter gpSpnAdapter;
    private VillageSpnAdapter villageSpnAdapter;
    private ShgTypeSpnAdapter shgTypeSpnAdapter;
    private PromotedBySpnAdapter promotedBySpnAdapter;
    private MeetingFrequencySpnAdapter meetingFrequencySpnAdapter;
    private BookKeepersSpnAdapter bookKeepersSpnAdapter;
    private boolean hasBasicTrainingReceived = false;
    private String shgName = "";
    private String shgCode = "";
    private String shgRegd = "";
    private String dateOfFormation = "";
    private String dateOfCooption = "";
    private String bankName = "";
    private String sbAcNo = "";
    private String dateOfAcOpening = "";
    private String monthlyAmountSaving = "";
    private String activeBankLoanAccountNumber = "";
    private String amountOfCapitalSubsidy = "";
    private String nameOfBookKeeper = "";
    private AutoSuggestAdapter autoSuggestAdapter;
    private Handler handler;
    private String intentType;
    private boolean isMeetingFreqFirst = false;
    private boolean isNotFirstLoad = false;
    private boolean isSelectedBookKeeper = false;
    private boolean isPromotor = false;
    private boolean isShgType = false;
    private BranchSpnAdapter branchSpnAdapter;

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
        binding = ActivityCreateShgProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        resetTitles();
        setSupportActionBar(binding.toolbarCreateShg);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        sp = SharedPrefManager.getInstance(this);
        token = sp.getStringData(Constant.APP_TOKEN);
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.MINUTES)
                .readTimeout(10, TimeUnit.MINUTES)
                .writeTimeout(10, TimeUnit.MINUTES)
                .build();
        AndroidNetworking.initialize(this, okHttpClient);

        intentType = getIntent().getStringExtra("INTENT_TYPE");
        binding.ivViewShgMember.setVisibility(View.GONE);
        if (intentType.equals("UPDATE")) {
            binding.tvToolbarTitleOne.setText(R.string.update_shg_profile);
            binding.shgLbl.setText(getResources().getString(R.string.update));
            binding.spnDist.setEnabled(false);
            binding.spnDist.setClickable(false);
            binding.spnBlock.setEnabled(false);
            binding.spnBlock.setClickable(false);
            binding.spnGp.setEnabled(false);
            binding.spnGp.setClickable(false);
            binding.spnVillage.setEnabled(false);
            binding.spnVillage.setClickable(false);
            binding.ivViewShgMember.setVisibility(View.VISIBLE);
            SHG shg = getIntent().getParcelableExtra("SHG_DETAILS");
            if (shg != null) {
                selectedShgId = shg.getShgId();
                selectedDistId = shg.getDistrictId();
                selectedBlockId = shg.getBlockId();
                selectedGpId = shg.getGpId();
                selectedVillageId = shg.getVillageId();
                selectedPromotorId = shg.getPromotedBy();
                selectedShgTypeId = shg.getShgType();
                selectedBranchId = shg.getBankBranchId();
                selectedMeetingFrequencyId = shg.getMeetingFrequency();
                selectedBookKeeperId = shg.getTrainedBookKeeperId();
                shgName = shg.getShgName();
                dateOfFormation = shg.getDateOfFormation();
                binding.etShgName.setText(shgName);
                binding.etShgFormationDate.setText(dateOfFormation);
                shgCode = shg.getShgCode();
                binding.etShgCode.setText(shgCode);
                dateOfCooption = shg.getDateOfRevival();
                binding.etShgCoopationDate.setText(dateOfCooption);
                bankName = shg.getBankName();
                getBranchListWRTBank();
                binding.etBankName.setText(bankName);
                sbAcNo = shg.getAccountNo();
                binding.etSbAcNo.setText(sbAcNo);
                dateOfAcOpening = shg.getAccountOpeningDate();
                binding.etShgAcOpeningDate.setText(dateOfAcOpening);
                monthlyAmountSaving = String.valueOf(shg.getMonthlySavingAmt());
                binding.etMonthlyAmountSavingPerMember.setText(monthlyAmountSaving);
                binding.switchBasicTrainingReceived.setChecked(shg.isBasicTrainingReceived());
                activeBankLoanAccountNumber = String.valueOf(shg.getActiveBankLoanAccountNumber());
                binding.etActiveLoanAcNo.setText(activeBankLoanAccountNumber);
                amountOfCapitalSubsidy = String.valueOf(shg.getAmountOfCapitalSubsidy());
                binding.etAmountToNrlm.setText(amountOfCapitalSubsidy);
                nameOfBookKeeper = shg.getNameOfBookKeeper();
                binding.etBookKeeperName.setText(nameOfBookKeeper);

            }


        }

        binding.progressCircular.setVisibility(View.GONE);
        binding.etShgFormationDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDatePicker(type_0);
            }
        });

        binding.etShgCoopationDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDatePicker(type_1);
            }
        });

        binding.etShgAcOpeningDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDatePicker(type_2);
            }
        });
        autoSuggestAdapter = new AutoSuggestAdapter(this,
                android.R.layout.simple_dropdown_item_1line);
        binding.etBankName.setThreshold(3);
        binding.etBankName.setAdapter(autoSuggestAdapter);
        binding.etBankName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.i(TAG, "beforeTextChanged::::: " + charSequence.toString());
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.i(TAG, "onTextChanged::::: " + charSequence.toString());
//
                handler.removeMessages(TRIGGER_AUTO_COMPLETE);
                handler.sendEmptyMessageDelayed(TRIGGER_AUTO_COMPLETE,
                        AUTO_COMPLETE_DELAY);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (binding.etBankName.getText().toString().contains(" ")) {
                    Log.i(TAG, "afterTextChanged: " + "Space found");
                }
                Log.i(TAG, "afterTextChanged:::: " + binding.etBankName.getText().toString());
            }
        });
        binding.etBankName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Toast.makeText(CreateShgProfileActivity.this,
//                        autoSuggestAdapter.getItem(position).toString(),
//                        Toast.LENGTH_LONG).show();
                bankName = autoSuggestAdapter.getItem(position);
                getBranchListWRTBank();
            }
        });

        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == TRIGGER_AUTO_COMPLETE) {
                    if (!TextUtils.isEmpty(binding.etBankName.getText())) {
                        makeApiCall(binding.etBankName.getText().toString());
                    }
                }
                return false;
            }
        });

        districtSpnAdapter = new DistrictSpnAdapter(this, districts);
        blockSpnAdapter = new BlockSpnAdapter(this, blocks);
        gpSpnAdapter = new GpSpnAdapter(this, gps);
        villageSpnAdapter = new VillageSpnAdapter(this, villages);
        shgTypeSpnAdapter = new ShgTypeSpnAdapter(this, shgTypes);


        bookKeepersSpnAdapter = new BookKeepersSpnAdapter(this, bookKeepers);
        binding.spnBookKeeper.setAdapter(bookKeepersSpnAdapter);
        binding.spnBookKeeper.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (intentType.equals("UPDATE")) {
                    selectedBookKeeperId = 0L;
                    if (isSelectedBookKeeper && position == 0) {
                        selectedBookKeeperId = 0L;
                    }
                    isSelectedBookKeeper = true;
                    if (isSelectedBookKeeper && position != 0) {
                        selectedBookKeeperId = bookKeepers.get(position).valueId;
                    }
                } else {
                    selectedBookKeeperId = bookKeepers.get(position).valueId;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        Frequency frequency = new Frequency();
        frequency.valueEn = "Select Meeting Frequency";
        frequency.valueId = 0L;
        meetingFrequency.add(frequency);

        meetingFrequencySpnAdapter = new MeetingFrequencySpnAdapter(this, meetingFrequency);
        binding.spnMeetingFreq.setAdapter(meetingFrequencySpnAdapter);
        binding.spnMeetingFreq.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (intentType.equals("UPDATE")) {
//                    selectedMeetingFrequencyId=0L;
                    if (isMeetingFreqFirst && position == 0) {
                        selectedMeetingFrequencyId = 0L;
                    }
                    isMeetingFreqFirst = true;
                    if (isMeetingFreqFirst && position != 0) {
                        selectedMeetingFrequencyId = meetingFrequency.get(position).valueId;
                    }
                } else {
                    selectedMeetingFrequencyId = meetingFrequency.get(position).valueId;
                }
                Log.i(TAG, "onItemSelected: selectedBranchId ~~ " + selectedMeetingFrequencyId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Promotor promotor = new Promotor();
        promotor.valueEn = "Select Promotor";
        promotor.valueId = 0L;
        promotors.add(promotor);

        promotedBySpnAdapter = new PromotedBySpnAdapter(this, promotors);
        binding.spnPromotor.setAdapter(promotedBySpnAdapter);
        binding.spnPromotor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (intentType.equals("UPDATE")) {
//                    selectedPromotorId = 0L;
                    if (isPromotor && position == 0) {
                        selectedPromotorId = 0L;
                    }
                    isPromotor = true;
                    if (isPromotor && position != 0) {
                        selectedPromotorId = promotors.get(position).valueId;
                    }
                } else {
                    selectedPromotorId = promotors.get(position).valueId;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        binding.spnShgType.setAdapter(shgTypeSpnAdapter);
        binding.spnShgType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (intentType.equals("UPDATE")) {
//                    selectedShgTypeId = 0L;
                    if (isShgType && position == 0) {
                        selectedShgTypeId = 0L;
                    }
                    isShgType = true;
                    if (isShgType && position != 0) {
                        selectedShgTypeId = shgTypes.get(position).valueId;
                    }
                } else {
                    selectedShgTypeId = shgTypes.get(position).valueId;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        District dis = new District();
        dis.districtName = "Select  District";
        dis.districtNameEN = "Select  District";
        dis.districtId = 0L;
        districts.add(dis);

        binding.spnDist.setAdapter(districtSpnAdapter);
        binding.spnDist.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
//                    selectedDistId = 0L;
//                    resetBlocks();
                } else {
                    selectedDistId = districts.get(position).districtId;
                    getBlockList();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        Block block = new Block();
        block.blockName = "Select Block";
        block.blockId = 0L;
        blocks.add(block);

        binding.spnBlock.setAdapter(blockSpnAdapter);
        binding.spnBlock.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
//                    if (isBlockSelectedOnce) {
//                        selectedBlockId = 0L;
//                        resetBlocks();
//                    }
                } else {
                    selectedBlockId = blocks.get(position).blockId;
                    getGpList();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        GP gp = new GP();
        gp.gpName = "Select Grampanchayat";
        gp.gpId = 0L;
        gps.add(gp);
        binding.spnGp.setAdapter(gpSpnAdapter);
        binding.spnGp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
//                    selectedGpId = 0L;
//                    resetGps();
                } else {
                    selectedGpId = gps.get(position).gpId;

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Village village = new Village();
        village.extraProps = "Select Village";
        village.village_id = 0L;
        villages.add(village);
        binding.spnVillage.setAdapter(villageSpnAdapter);
        binding.spnVillage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
//                    selectedVillageId = 0L;
                } else {
                    selectedVillageId = villages.get(position).village_id;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        getVillagList();
//        getDistrictList();
        getBookKeeperList();
        getShgTypes();
        getPromotors();
        getFrequencies();

        binding.switchBasicTrainingReceived.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                hasBasicTrainingReceived = isChecked;
            }
        });

        binding.addShgSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shgName = binding.etShgName.getText().toString().trim();
                shgCode = binding.etShgCode.getText().toString().trim();
                shgRegd = binding.etShgRegd.getText().toString().trim();
                sbAcNo = binding.etSbAcNo.getText().toString().trim();
                dateOfFormation = binding.etShgFormationDate.getText().toString().trim();
                dateOfCooption = binding.etShgCoopationDate.getText().toString().trim();
                bankName = binding.etBankName.getText().toString().trim();
                dateOfAcOpening = binding.etShgAcOpeningDate.getText().toString().trim();
                monthlyAmountSaving = binding.etMonthlyAmountSavingPerMember.getText().toString().trim();
                activeBankLoanAccountNumber = binding.etActiveLoanAcNo.getText().toString().trim();
                nameOfBookKeeper = binding.etBookKeeperName.getText().toString().trim();
                amountOfCapitalSubsidy = binding.etAmountToNrlm.getText().toString().trim();


                if (selectedVillageId.equals(0L)) {
                    Toast.makeText(CreateShgProfileActivity.this, "Please Select  Village", Toast.LENGTH_SHORT).show();
                } else if (shgName.isEmpty()) {
                    Toast.makeText(CreateShgProfileActivity.this, "Please Fill Shg Name", Toast.LENGTH_SHORT).show();
                } else if (dateOfFormation.isEmpty()) {
                    Toast.makeText(CreateShgProfileActivity.this, "Please select Date Of Formation", Toast.LENGTH_SHORT).show();
                } else if (shgCode.isEmpty()) {
                    Toast.makeText(CreateShgProfileActivity.this, "Please Fill SHG Code", Toast.LENGTH_SHORT).show();
                }/*  else if (shgRegd.isEmpty()) {
                    Toast.makeText(CreateShgProfileActivity.this, "Please Fill SHG Registration Number", Toast.LENGTH_SHORT).show();
                }*/ else if (selectedShgTypeId.equals(0L)) {
                    Toast.makeText(CreateShgProfileActivity.this, "Please Select  Shg Type", Toast.LENGTH_SHORT).show();
                } else if (dateOfCooption.isEmpty()) {
                    Toast.makeText(CreateShgProfileActivity.this, "Please select Date Of Cooption", Toast.LENGTH_SHORT).show();
                } else if (selectedPromotorId.equals(0L)) {
                    Toast.makeText(CreateShgProfileActivity.this, "Please Select  Promotor", Toast.LENGTH_SHORT).show();
                } else if (bankName.isEmpty()) {
                    Toast.makeText(CreateShgProfileActivity.this, "Please select Bank Name", Toast.LENGTH_SHORT).show();
                } else if (selectedBranchId.equals(0L)) {
                    Toast.makeText(CreateShgProfileActivity.this, "Please select Bank Branch", Toast.LENGTH_SHORT).show();
                } else if (sbAcNo.isEmpty()) {
                    Toast.makeText(CreateShgProfileActivity.this, "Please fill S/B Account Number", Toast.LENGTH_SHORT).show();
                } else if (sbAcNo.length() < 6) {
                    Toast.makeText(CreateShgProfileActivity.this, "Please fill a valid A/C number", Toast.LENGTH_SHORT).show();
                } else if (dateOfAcOpening.isEmpty()) {
                    Toast.makeText(CreateShgProfileActivity.this, "Please select Date Of Account Opening", Toast.LENGTH_SHORT).show();
                } else if (selectedMeetingFrequencyId.equals(0L)) {
                    Toast.makeText(CreateShgProfileActivity.this, "Please select Meeting Frequency", Toast.LENGTH_SHORT).show();
                } else if (monthlyAmountSaving.isEmpty()) {
                    Toast.makeText(CreateShgProfileActivity.this, "Please Fill Monthly amount saving", Toast.LENGTH_SHORT).show();
                } else if (monthlyAmountSaving.equals("null")) {
                    Toast.makeText(CreateShgProfileActivity.this, "Please Fill Monthly amount saving", Toast.LENGTH_SHORT).show();
                } else if (activeBankLoanAccountNumber.isEmpty()) {
                    Toast.makeText(CreateShgProfileActivity.this, "Please Fill Active Bank Loan A/C Number", Toast.LENGTH_SHORT).show();
                } else if (activeBankLoanAccountNumber.equals("null")) {
                    Toast.makeText(CreateShgProfileActivity.this, "Please Fill Active Bank Loan A/C Number", Toast.LENGTH_SHORT).show();
                } else if (amountOfCapitalSubsidy.isEmpty()) {
                    Toast.makeText(CreateShgProfileActivity.this, "Please Fill Subsidy amount", Toast.LENGTH_SHORT).show();
                } else if (amountOfCapitalSubsidy.equals("null")) {
                    Toast.makeText(CreateShgProfileActivity.this, "Please Fill Subsidy amount", Toast.LENGTH_SHORT).show();
                } else if (selectedBookKeeperId.equals(0L)) {
                    Toast.makeText(CreateShgProfileActivity.this, "Please Select  Book Keeper", Toast.LENGTH_SHORT).show();
                } else {
                    createShgApi();
                }
            }
        });

        binding.ivViewShgMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CreateShgProfileActivity.this, ShgMembersActivity.class);
                intent.putExtra("SHG_ID", selectedShgId);
                startActivity(intent);
            }
        });

        BankBranch br = new BankBranch();
        br.branchName = "Select  Branch";
        br.ifsc = "IFSC";
        br.bankBranchId = 0L;
        branchList.add(br);


        branchSpnAdapter = new BranchSpnAdapter(this, branchList);
        binding.spnBranchList.setAdapter(branchSpnAdapter);
        binding.spnBranchList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (intentType.equals("UPDATE")) {
                    if (isNotFirstLoad && position == 0) {
                        selectedBranchId = 0L;
                    }
                    isNotFirstLoad = true;
                    if (isNotFirstLoad && position != 0) {
                        selectedBranchId = branchList.get(position).bankBranchId;
                    }
                } else {
                    selectedBranchId = branchList.get(position).bankBranchId;
                }
                Log.i(TAG, "onItemSelected: selectedBranchId ~~ " + selectedBranchId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        if (intentType.equals("ADD")) {

        } else if (intentType.equals("UPDATE")) {

        }


    }

    private void getBranchListWRTBank() {

        AndroidNetworking.get(BuildConfig.BASE_URL + Constant.API_PATH + "lov/branchList/" + bankName)
                .addHeaders("Authorization", "Bearer " + token)
                .setTag("Create SHG Profile")
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(new StringRequestListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onResponse(String response) {
                        if (Utility.isStringValid(response)) {
                            try {
                                JSONObject resObj = new JSONObject(response);
                                if (resObj.optBoolean("outcome")) {
                                    JSONArray array = resObj.optJSONArray("data");
                                    if (array != null && array.length() > 0) {
                                        branchList.clear();
                                        BankBranch br = new BankBranch();
                                        br.branchName = "Select  Branch";
                                        br.ifsc = "IFSC";
                                        br.bankBranchId = 0L;
                                        branchList.add(br);
                                        binding.spnBranchList.setSelection(0);
                                        for (int i = 0; i < array.length(); i++) {
                                            BankBranch branch = BankBranch.parseBankBRanch(array.optJSONObject(i));
                                            branchList.add(branch);
                                        }
                                        binding.spnBranchList.setAdapter(branchSpnAdapter);
                                        branchSpnAdapter.notifyDataSetChanged();

                                        if (intentType.equals("UPDATE")) {
                                            for (int i = 0; i < branchList.size(); i++) {
                                                if (selectedBranchId.equals(branchList.get(i).bankBranchId)) {
                                                    binding.spnBranchList.setSelection(i);
                                                }
                                                branchSpnAdapter.notifyDataSetChanged();
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

    private void makeApiCall(String bankName) {
        AndroidNetworking.get(BuildConfig.BASE_URL + Constant.API_PATH + "lov/bankList/" + bankName)
                .addHeaders("Authorization", "Bearer " + token)
                .setTag("Create SHG Profile")
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        if (Utility.isStringValid(response)) {
                            try {
                                JSONObject resObj = new JSONObject(response);
                                if (resObj.optBoolean("outcome")) {
                                    JSONArray array = resObj.optJSONArray("data");
                                    if (array != null && array.length() > 0) {
                                        bankList.clear();
                                        for (int i = 0; i < array.length(); i++) {
                                            bankList.add(array.optString(i));
                                        }
                                        autoSuggestAdapter.setData(bankList);
                                        autoSuggestAdapter.notifyDataSetChanged();
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

    private void createShgApi() {
        if (intentType.equals("UPDATE")) {

            JSONObject vilReqObj = new JSONObject();
            try {
                vilReqObj.put("villageId", selectedVillageId);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            JSONObject bbReqObj = new JSONObject();
            try {
                bbReqObj.put("bankBranchId", selectedBranchId);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            JSONObject reqObj = new JSONObject();
            try {
                reqObj.put("shgDetailsId", selectedShgId);
                reqObj.put("dateOfFormation2", dateOfFormation);
                reqObj.put("village", vilReqObj);
                reqObj.put("shgName", shgName);
                reqObj.put("shgType", selectedShgTypeId);
                reqObj.put("shgCode", shgCode);
                reqObj.put("dateOfRevival2", dateOfCooption);
                reqObj.put("promotedBy", selectedPromotorId);
                reqObj.put("meetingFrequency", selectedMeetingFrequencyId);
                reqObj.put("monthlySavingAmt", monthlyAmountSaving);
                reqObj.put("isBasicTrainingRecv", hasBasicTrainingReceived);
                reqObj.put("loanAccountNo", activeBankLoanAccountNumber);
                reqObj.put("capitalSubsidyAmt", amountOfCapitalSubsidy);
                reqObj.put("trainedBookKeeper", selectedBookKeeperId);
                reqObj.put("bookKeeeperName", nameOfBookKeeper);
                reqObj.put("shgMemberDto", null);
                reqObj.put("IsActive", true);
                reqObj.put("shgBankBranch", bbReqObj);
                reqObj.put("accountNo", sbAcNo);
                reqObj.put("accountOpeningDate2", dateOfAcOpening);
                reqObj.put("bankName", bankName);

            } catch (JSONException e) {
                e.printStackTrace();
            }



            binding.progressCircular.setVisibility(View.VISIBLE);
            AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "shg/save")
                    .addHeaders("Authorization", "Bearer " + token)
                    .setTag("Create SHG Profile")
                    .addJSONObjectBody(reqObj)
                    .setPriority(Priority.HIGH)
                    .build()
                    .getAsString(new StringRequestListener() {
                        @Override
                        public void onResponse(String response) {
                            if (Utility.isStringValid(response)) {
                                binding.progressCircular.setVisibility(View.GONE);
                                try {
                                    JSONObject resObj = new JSONObject(response);
                                    if (resObj.optBoolean("outcome")) {

                                        Intent in = getIntent();
                                        in.putExtra("res", true);
                                        setResult(RESULT_OK, in);
                                        finish();

                                        Toast.makeText(CreateShgProfileActivity.this, resObj.optString("message"), Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(CreateShgProfileActivity.this, resObj.optString("message"), Toast.LENGTH_SHORT).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onError(ANError anError) {
                            binding.progressCircular.setVisibility(View.GONE);
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
        } else {
            binding.progressCircular.setVisibility(View.VISIBLE);

            JSONObject vilReqObj = new JSONObject();
            try {
                vilReqObj.put("villageId", selectedVillageId);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            JSONObject bbReqObj = new JSONObject();
            try {
                bbReqObj.put("bankBranchId", selectedBranchId);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            JSONObject reqObj = new JSONObject();
            try {
                reqObj.put("shgDetailsId", null);
                reqObj.put("dateOfFormation2", dateOfFormation);
                reqObj.put("village", vilReqObj);
                reqObj.put("shgName", shgName);
                reqObj.put("shgType", selectedShgTypeId);
                reqObj.put("shgCode", shgCode);
                reqObj.put("dateOfRevival2", dateOfCooption);
                reqObj.put("promotedBy", selectedPromotorId);
                reqObj.put("meetingFrequency", selectedMeetingFrequencyId);
                reqObj.put("monthlySavingAmt", monthlyAmountSaving);
                reqObj.put("isBasicTrainingRecv", hasBasicTrainingReceived);
                reqObj.put("loanAccountNo", activeBankLoanAccountNumber);
                reqObj.put("capitalSubsidyAmt", amountOfCapitalSubsidy);
                reqObj.put("trainedBookKeeper", selectedBookKeeperId);
                reqObj.put("bookKeeeperName", nameOfBookKeeper);
                reqObj.put("shgMemberDto", null);
                reqObj.put("IsActive", true);
                reqObj.put("shgBankBranch", bbReqObj);
                reqObj.put("accountNo", sbAcNo);
                reqObj.put("accountOpeningDate2", dateOfAcOpening);
                reqObj.put("bankName", bankName);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "shg/save")
                    .addHeaders("Authorization", "Bearer " + token)
                    .setTag("Create SHG Profile")
                    .addJSONObjectBody(reqObj)
                    .setPriority(Priority.HIGH)
                    .build()
                    .getAsString(new StringRequestListener() {
                        @Override
                        public void onResponse(String response) {
                            if (Utility.isStringValid(response)) {
                                binding.progressCircular.setVisibility(View.GONE);
                                try {
                                    JSONObject resObj = new JSONObject(response);
                                    if (resObj.optBoolean("outcome")) {

                                        Intent in = getIntent();
                                        in.putExtra("res", true);
                                        setResult(RESULT_OK, in);
                                        finish();

                                        Toast.makeText(CreateShgProfileActivity.this, resObj.optString("message"), Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(CreateShgProfileActivity.this, resObj.optString("message"), Toast.LENGTH_SHORT).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onError(ANError anError) {
                            binding.progressCircular.setVisibility(View.GONE);
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
    }

    private void resetGps() {
        gps.clear();
        selectedGpId = 0L;
        GP gp = new GP();
        gp.gpName = "Select Grampanchayat";
        gp.gpId = 0L;
        gps.add(gp);
        gpSpnAdapter.notifyDataSetChanged();

        resetVillages();
    }

    private void resetBlocks() {
        blocks.clear();
        selectedBlockId = 0L;
        Block block = new Block();
        block.blockName = "Select  Block";
        block.blockId = 0L;
        blocks.add(block);
        blockSpnAdapter.notifyDataSetChanged();

        resetGps();
    }

    private void resetVillages() {
        villages.clear();
        selectedVillageId = 0L;
        Village village = new Village();
        village.villageName = "Select  Village";
        village.village_id = 0L;
        villages.add(village);
        villageSpnAdapter.notifyDataSetChanged();
    }

    private void getFrequencies() {
        AndroidNetworking.get(BuildConfig.BASE_URL + Constant.API_PATH + "lov/meetingFrequency")
                .addHeaders("Authorization", "Bearer " + token)
                .setTag("GetAllPromoter")
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        if (Utility.isStringValid(response)) {
                            try {
                                JSONObject resObj = new JSONObject(response);
                                if (resObj.optBoolean("outcome")) {
                                    JSONArray array = resObj.optJSONArray("data");
                                    if (array != null && array.length() > 0) {
                                        meetingFrequency.clear();
                                        Frequency frequency = new Frequency();
                                        frequency.valueEn = "Select  Meeting Frequency";
                                        frequency.valueId = 0L;
                                        meetingFrequency.add(frequency);
                                        for (int i = 0; i < array.length(); i++) {
                                            Frequency promotor1 = Frequency.parseFrequency(array.optJSONObject(i));
                                            meetingFrequency.add(promotor1);
                                        }
                                        meetingFrequencySpnAdapter.notifyDataSetChanged();
                                        if (intentType.equals("UPDATE")) {
                                            for (int i = 0; i < meetingFrequency.size(); i++) {
                                                if (selectedMeetingFrequencyId.equals(meetingFrequency.get(i).valueId)) {
                                                    binding.spnMeetingFreq.setSelection(i);
                                                }
                                                meetingFrequencySpnAdapter.notifyDataSetChanged();
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

    private void getPromotors() {
        AndroidNetworking.get(BuildConfig.BASE_URL + Constant.API_PATH + "lov/promoter")
                .addHeaders("Authorization", "Bearer " + token)
                .setTag("GetAllPromoter")
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        if (Utility.isStringValid(response)) {
                            try {
                                JSONObject resObj = new JSONObject(response);
                                if (resObj.optBoolean("outcome")) {
                                    JSONArray array = resObj.optJSONArray("data");
                                    if (array != null && array.length() > 0) {
                                        promotors.clear();
                                        Promotor promotor = new Promotor();
                                        promotor.valueEn = "Select  Promotor";
                                        promotor.valueId = 0L;
                                        promotors.add(promotor);
                                        for (int i = 0; i < array.length(); i++) {
                                            Promotor promotor1 = Promotor.parsePromotor(array.optJSONObject(i));
                                            promotors.add(promotor1);
                                        }
                                        promotedBySpnAdapter.notifyDataSetChanged();
                                        if (intentType.equals("UPDATE")) {
                                            for (int i = 0; i < promotors.size(); i++) {
                                                if (selectedPromotorId.equals(promotors.get(i).valueId)) {
                                                    binding.spnPromotor.setSelection(i);
                                                }
                                                promotedBySpnAdapter.notifyDataSetChanged();
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

    private void getShgTypes() {
        AndroidNetworking.get(BuildConfig.BASE_URL + Constant.API_PATH + "lov/shgType")
                .addHeaders("Authorization", "Bearer " + token)
                .setTag("GetAllShgTypes")
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        if (Utility.isStringValid(response)) {
                            try {
                                JSONObject resObj = new JSONObject(response);
                                if (resObj.optBoolean("outcome")) {
                                    JSONArray array = resObj.optJSONArray("data");
                                    if (array != null && array.length() > 0) {
                                        shgTypes.clear();
                                        ShgType shgType = new ShgType();
                                        shgType.valueEn = "Select ShgType";
                                        shgType.valueId = 0L;
                                        shgTypes.add(shgType);
                                        for (int i = 0; i < array.length(); i++) {
                                            ShgType shgType1 = ShgType.parseShgType(array.optJSONObject(i));
                                            shgTypes.add(shgType1);
                                        }
                                        shgTypeSpnAdapter.notifyDataSetChanged();
                                        if (intentType.equals("UPDATE")) {
                                            for (int i = 0; i < shgTypes.size(); i++) {
                                                if (selectedShgTypeId.equals(shgTypes.get(i).valueId)) {
                                                    binding.spnShgType.setSelection(i);
                                                }
                                                shgTypeSpnAdapter.notifyDataSetChanged();
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

    private void getBookKeeperList() {
        AndroidNetworking.get(BuildConfig.BASE_URL + Constant.API_PATH + "lov/trainedBookKeepers")
                .addHeaders("Authorization", "Bearer " + token)
                .setTag("GetAllBookKeepers")
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        if (Utility.isStringValid(response)) {
                            try {
                                JSONObject resObj = new JSONObject(response);
                                if (resObj.optBoolean("outcome")) {
                                    JSONArray array = resObj.optJSONArray("data");
                                    if (array != null && array.length() > 0) {
                                        bookKeepers.clear();
                                        BookKeepers bookKeeper = new BookKeepers();
                                        bookKeeper.valueEn = "Select";
                                        bookKeeper.valueId = 0L;
                                        bookKeepers.add(bookKeeper);
                                        for (int i = 0; i < array.length(); i++) {
                                            BookKeepers bookKeepers1 = BookKeepers.parseBookKeepers(array.optJSONObject(i));
                                            bookKeepers.add(bookKeepers1);
                                        }
                                        bookKeepersSpnAdapter.notifyDataSetChanged();
                                        if (intentType.equals("UPDATE")) {
                                            for (int i = 0; i < bookKeepers.size(); i++) {
                                                if (selectedBookKeeperId.equals(bookKeepers.get(i).valueId)) {
                                                    binding.spnBookKeeper.setSelection(i);
                                                }
                                                bookKeepersSpnAdapter.notifyDataSetChanged();
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

    private void getGpList() {
        AndroidNetworking.get(BuildConfig.BASE_URL + Constant.API_PATH + "demography/gp/byBlock/" + selectedBlockId)
                .addHeaders("Authorization", "Bearer " + token)
                .setTag("GetAllGps")
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        if (Utility.isStringValid(response)) {
                            try {
                                JSONObject resObj = new JSONObject(response);
                                if (resObj.optBoolean("outcome")) {
                                    JSONArray array = resObj.optJSONArray("data");
                                    if (array != null && array.length() > 0) {
                                        gps.clear();
                                        GP gp = new GP();
                                        gp.gpName = "Select Grampanchayat";
                                        gp.gpId = 0L;
                                        gps.add(gp);
                                        for (int i = 0; i < array.length(); i++) {
                                            GP gp1 = GP.parseGp(array.optJSONObject(i));
                                            gps.add(gp1);
                                        }
                                        gpSpnAdapter.notifyDataSetChanged();
                                        if (intentType.equals("UPDATE")) {
                                            for (int i = 0; i < gps.size(); i++) {
                                                if (selectedGpId.equals(gps.get(i).gpId)) {
                                                    binding.spnGp.setSelection(i);
                                                }
                                                gpSpnAdapter.notifyDataSetChanged();
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

    private void getVillagList() {
        AndroidNetworking.get(BuildConfig.BASE_URL + Constant.API_PATH + "demography/village/list")
                .addHeaders("Authorization", "Bearer " + token)
                .setTag("GetAllBlock")
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        if (Utility.isStringValid(response)) {
                            try {
                                JSONObject resObj = new JSONObject(response);
                                if (resObj.optBoolean("outcome")) {
                                    JSONArray array = resObj.optJSONArray("data");
                                    if (array != null && array.length() > 0) {
                                        villages.clear();
                                        Village village = new Village();
                                        village.extraProps = "Select Village";
                                        village.village_id = 0L;
                                        villages.add(village);
                                        for (int i = 0; i < array.length(); i++) {
                                            Village village1 = Village.parseVillage(array.optJSONObject(i));
                                            villages.add(village1);
                                        }
                                        villageSpnAdapter.notifyDataSetChanged();
                                        if (intentType.equals("UPDATE")) {
                                            for (int i = 0; i < villages.size(); i++) {
                                                if (selectedVillageId.equals(villages.get(i).village_id)) {
                                                    binding.spnVillage.setSelection(i);
                                                }
                                                villageSpnAdapter.notifyDataSetChanged();
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

    private void getBlockList() {
        AndroidNetworking.get(BuildConfig.BASE_URL + Constant.API_PATH + "demography/block/byDistrict/" + selectedDistId)
                .addHeaders("Authorization", "Bearer " + token)
                .setTag("GetAllBlock")
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        if (Utility.isStringValid(response)) {
                            try {
                                JSONObject resObj = new JSONObject(response);
                                if (resObj.optBoolean("outcome")) {
                                    JSONArray array = resObj.optJSONArray("data");
                                    if (array != null && array.length() > 0) {
                                        blocks.clear();
                                        Block block = new Block();
                                        block.blockName = "Select  Block";
                                        block.blockId = 0L;
                                        blocks.add(block);
                                        for (int i = 0; i < array.length(); i++) {
                                            Block block1 = Block.parseBlock(array.optJSONObject(i));
                                            blocks.add(block1);
                                        }
                                        blockSpnAdapter.notifyDataSetChanged();
                                        if (intentType.equals("UPDATE")) {
                                            for (int i = 0; i < blocks.size(); i++) {
                                                if (selectedBlockId.equals(blocks.get(i).blockId)) {
                                                    binding.spnBlock.setSelection(i);
                                                }
                                                blockSpnAdapter.notifyDataSetChanged();
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

    private void getDistrictList() {
        AndroidNetworking.get(BuildConfig.BASE_URL + Constant.API_PATH + "demography/district/byState/1")
                .addHeaders("Authorization", "Bearer " + token)
                .setTag("GetAllShg")
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        if (Utility.isStringValid(response)) {
                            try {
                                JSONObject resObj = new JSONObject(response);
                                if (resObj.optBoolean("outcome")) {
                                    JSONArray array = resObj.optJSONArray("data");
                                    if (array != null && array.length() > 0) {
                                        districts.clear();
                                        District dis = new District();
                                        dis.districtName = "Select  District";
                                        dis.districtNameEN = "Select  District";
                                        dis.districtId = 0L;
                                        districts.add(dis);
                                        for (int i = 0; i < array.length(); i++) {
                                            District district = District.parseDistrict(array.optJSONObject(i));
                                            districts.add(district);
                                        }
                                        districtSpnAdapter.notifyDataSetChanged();
                                        if (intentType.equals("UPDATE")) {
                                            for (int i = 0; i < districts.size(); i++) {
                                                if (selectedDistId.equals(districts.get(i).districtId)) {
                                                    binding.spnDist.setSelection(i);
                                                }
                                                districtSpnAdapter.notifyDataSetChanged();
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

    private void openDatePicker(int type) {
        Calendar myCalendar = Calendar.getInstance();
        final DatePickerDialog datePickerDialog = new DatePickerDialog(CreateShgProfileActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, month);
                myCalendar.set(Calendar.DAY_OF_MONTH, day);

//                Year = year;
//                Month = month + 1;

                String myFormat = "dd/MM/yyyy"; //In which you need put here
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

                if (type == type_0) {
                    binding.etShgFormationDate.setText(sdf.format(myCalendar.getTime()));
                } else if (type == type_1) {
                    binding.etShgCoopationDate.setText(sdf.format(myCalendar.getTime()));
                } else if (type == type_2) {
                    binding.etShgAcOpeningDate.setText(sdf.format(myCalendar.getTime()));
                }
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