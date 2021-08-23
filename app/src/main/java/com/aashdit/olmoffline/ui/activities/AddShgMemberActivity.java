package com.aashdit.olmoffline.ui.activities;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
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
import androidx.appcompat.app.AppCompatActivity;

import com.aashdit.olmoffline.BuildConfig;
import com.aashdit.olmoffline.R;
import com.aashdit.olmoffline.adapters.AutoSuggestAdapter;
import com.aashdit.olmoffline.adapters.BranchSpnAdapter;
import com.aashdit.olmoffline.adapters.DisabilitySpnAdapter;
import com.aashdit.olmoffline.adapters.GenderSpnAdapter;
import com.aashdit.olmoffline.adapters.LeaderSpnAdapter;
import com.aashdit.olmoffline.adapters.PipCategorySpnAdapter;
import com.aashdit.olmoffline.adapters.ReligionSpnAdapter;
import com.aashdit.olmoffline.adapters.SocialCatSpnAdapter;
import com.aashdit.olmoffline.databinding.ActivityAddShgMember2Binding;
import com.aashdit.olmoffline.models.BankBranch;
import com.aashdit.olmoffline.models.Disability;
import com.aashdit.olmoffline.models.Gender;
import com.aashdit.olmoffline.models.Leader;
import com.aashdit.olmoffline.models.PipCategory;
import com.aashdit.olmoffline.models.Religion;
import com.aashdit.olmoffline.models.ShgMember;
import com.aashdit.olmoffline.models.SocialCategory;
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

public class AddShgMemberActivity extends AppCompatActivity {

    private static final String TAG = "AddShgMemberActivity";
    private static final int TRIGGER_AUTO_COMPLETE = 100;
    private static final long AUTO_COMPLETE_DELAY = 300;
    private final ArrayList<Disability> disabilities = new ArrayList<>();
    private final ArrayList<Gender> genders = new ArrayList<>();
    private final ArrayList<Leader> leaders = new ArrayList<>();
    private final ArrayList<PipCategory> pipCategories = new ArrayList<>();
    private final ArrayList<Religion> religions = new ArrayList<>();
    private final ArrayList<SocialCategory> socialCategories = new ArrayList<>();
    private final ArrayList<BankBranch> branchList = new ArrayList<>();
    private final ArrayList<String> bankList = new ArrayList<>();
    private final String signatureThumbFile = "iVBORw0KGgoAAAANSUhEUgAAABQAAAARCAIAAABSJhvpAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAADsMAAA7DAcdvqGQAAAATSURBVDhPYxgFo2AUjAIGBgYGAAQNAAHGKsbmAAAAAElFTkSuQmCC";
    private final String signatureThumbFileType = signatureThumbFile.concat("_png");
    private final String shgMemberSignature = signatureThumbFile.concat(signatureThumbFileType);
    private ActivityAddShgMember2Binding binding;
    private String shgMemberName, father_husbandName, memberCode, memberDob, memberDoj, memberEduLevel,
            aadharNumber, mobileNumber, ac_no, bankName;
    private Long memberReligionId = 0L;
    private Long memberGenderId = 0L;
    private Long memberPipCategoryId = 0L;
    private Long memberLeaderId = 0L;
    private Long memberDisabilityId = 0L;
    private Long memberSocialCatId = 0L;
    private Long bankBranchId = 0L;
    private Long shgId = 0L;
    private Long shgMemberId = 0L;
    private boolean insPMJJY = false;
    private boolean insPMSBY = false;
    private boolean insOther = false;
    private boolean insApy = false;
    private boolean aadharSeeded = false;
    private SharedPrefManager sp;
    private String token;
    private DisabilitySpnAdapter disabilitySpnAdapter;
    private GenderSpnAdapter genderSpnAdapter;
    private LeaderSpnAdapter leaderSpnAdapter;
    private PipCategorySpnAdapter pipCategorySpnAdapter;
    private ReligionSpnAdapter religionSpnAdapter;
    private SocialCatSpnAdapter socialCatSpnAdapter;
    private AutoSuggestAdapter autoSuggestAdapter;
    private Handler handler;
    /**
     * type = 0 for DOB
     * type = 1 for DOJ
     */
    private int type;
    private String intentType;
    private ShgMember member;
    private boolean isNotFirstLoad = false;
    private boolean isSocialCategory = false;
    private boolean isDisability = false;
    private boolean isPipCategory = false;
    private boolean isReligion = false;
    private boolean isGender = false;
    private boolean isLeader = false;
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
        binding = ActivityAddShgMember2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        resetTitles();

        setSupportActionBar(binding.toolbarShgAddMember);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        sp = SharedPrefManager.getInstance(this);
        token = sp.getStringData(Constant.APP_TOKEN);

        shgId = getIntent().getLongExtra("SHG_ID", 0L);
        intentType = getIntent().getStringExtra("INTENT_TYPE");
        member = getIntent().getParcelableExtra("SHG_MEMBER");

        if (intentType.equals("UPDATE")) {
            binding.memberLbl.setText(getResources().getString(R.string.update));
            binding.tvToolbarTitleOne.setText(R.string.update_member);
            if (member != null) {
                shgMemberName = member.memberName;
                shgId = member.shgId;
                shgMemberId = member.memberId;
                memberReligionId = member.religion;
                memberSocialCatId = member.socialCategory;
                memberDisabilityId = member.disabilityType;
                memberGenderId = member.gender;
                memberPipCategoryId = member.pipCategory;
                memberLeaderId = member.leader;
                bankBranchId = member.bankBranchId;
                binding.etShgMemberName.setText(shgMemberName);
                binding.etFatherHusName.setText(member.fatherHusbName);
                binding.etShgMemberDob.setText(member.dateOfBirth);
                binding.etShgMemberJoining.setText(member.dateOfJoin);
                binding.etShgMemberEducationLevel.setText(member.eduLevel);
                binding.etMemAcNo.setText(member.accountNo);
                binding.actMemBankName.setText(member.bankName);
                bankName = member.bankName;
                getBranchListWRTBank();
                binding.etMemAadhar.setText(member.aadharNo);
                binding.etMemMobile.setText(member.mobileNo);
                binding.etMemberCode.setText(member.memberCode);

                insPMJJY = member.enrolledInPmjjy;
                insPMSBY = member.enrolledInPmsby;
                insApy = member.enrolledInApy;
                insOther = member.enrolledInOther;
                aadharSeeded = member.isAadharSeeded;
                binding.switchPmjjy.setChecked(member.enrolledInPmjjy);
                binding.switchPmsby.setChecked(member.enrolledInPmsby);
                binding.switchApy.setChecked(member.enrolledInApy);
                binding.switchOther.setChecked(member.enrolledInOther);
                binding.switchSbac.setChecked(member.isAadharSeeded);
            }
        }

        Religion religion = new Religion();
        religion.valueId = 0L;
        religion.valueEn = "Select Religion";
        religions.add(religion);


        Gender gender = new Gender();
        gender.valueId = 0L;
        gender.valueEn = "Select Gender";
        genders.add(gender);


        Leader leader = new Leader();
        leader.valueId = 0L;
        leader.valueEn = "Select Leader";
        leaders.add(leader);

        PipCategory pipCategory = new PipCategory();
        pipCategory.valueId = 0L;
        pipCategory.valueEn = "Select PipCategory";
        pipCategories.add(pipCategory);

        Disability disability = new Disability();
        disability.valueId = 0L;
        disability.valueEn = "Select Disability";
        disabilities.add(disability);

        SocialCategory socialCategory = new SocialCategory();
        socialCategory.valueId = 0L;
        socialCategory.valueEn = "Select SocialCategory";
        socialCategories.add(socialCategory);

        autoSuggestAdapter = new AutoSuggestAdapter(this,
                android.R.layout.simple_dropdown_item_1line);
        binding.actMemBankName.setThreshold(3);
        binding.actMemBankName.setAdapter(autoSuggestAdapter);
        binding.actMemBankName.addTextChangedListener(new TextWatcher() {
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
                if (binding.actMemBankName.getText().toString().contains(" ")) {
                    Log.i(TAG, "afterTextChanged: " + "Space found");
                }
                Log.i(TAG, "afterTextChanged:::: " + binding.actMemBankName.getText().toString());
            }
        });


        binding.actMemBankName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Toast.makeText(AddShgMemberActivity.this,
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
                    if (!TextUtils.isEmpty(binding.actMemBankName.getText())) {
                        makeApiCall(binding.actMemBankName.getText().toString());
                    }
                }
                return false;
            }
        });


        binding.switchPmjjy.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                insPMJJY = isChecked;
            }
        });
        binding.switchPmsby.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                insPMSBY = isChecked;
            }
        });
        binding.switchOther.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                insOther = isChecked;
            }
        });
        binding.switchApy.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                insApy = isChecked;
            }
        });

        binding.switchSbac.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                aadharSeeded = b;
            }
        });

        getReligion();
        getGender();
        getLeader();
        getPipCategory();
        getDisability();
        getSocialCategory();

        binding.etShgMemberDob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDatePicker(0);
            }
        });
        binding.etShgMemberJoining.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDatePicker(1);
            }
        });

        BankBranch br = new BankBranch();
        br.branchName = "Select a Branch";
        br.ifsc = "IFSC";
        br.bankBranchId = 0L;
        branchList.add(br);

        branchSpnAdapter = new BranchSpnAdapter(this, branchList);
        binding.spnMemBranchList.setAdapter(branchSpnAdapter);

        binding.spnMemBranchList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (intentType.equals("UPDATE")) {
//                    bankBranchId = 0L;
                    if (isNotFirstLoad && position == 0) {
                        bankBranchId = 0L;
                    }
                    isNotFirstLoad = true;
                    if (isNotFirstLoad && position != 0) {
                        bankBranchId = branchList.get(position).bankBranchId;
                    }
                } else {
                    bankBranchId = branchList.get(position).bankBranchId;
                    Log.i(TAG, "onItemSelected: selectedBranchId ~~ " + bankBranchId);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        binding.addShgMemberSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shgMemberName = binding.etShgMemberName.getText().toString().trim();
                father_husbandName = binding.etFatherHusName.getText().toString().trim();
                memberDob = binding.etShgMemberDob.getText().toString().trim();
                memberDoj = binding.etShgMemberJoining.getText().toString().trim();
                memberEduLevel = binding.etShgMemberEducationLevel.getText().toString().trim();
                bankName = binding.actMemBankName.getText().toString().trim();
                ac_no = binding.etMemAcNo.getText().toString().trim();
                aadharNumber = binding.etMemAadhar.getText().toString().trim();
                mobileNumber = binding.etMemMobile.getText().toString().trim();
                memberCode = binding.etMemberCode.getText().toString().trim();

                if (shgMemberName.isEmpty()) {
                    Toast.makeText(AddShgMemberActivity.this, "Please Fill SHG Member Name", Toast.LENGTH_SHORT).show();
                } else if (father_husbandName.isEmpty()) {
                    Toast.makeText(AddShgMemberActivity.this, "Please Fill Father/Husband Name", Toast.LENGTH_SHORT).show();
                } else if (memberCode.isEmpty()) {
                    Toast.makeText(AddShgMemberActivity.this, "Please Fill Member code", Toast.LENGTH_SHORT).show();
                } else if (memberSocialCatId == 0L) {
                    Toast.makeText(AddShgMemberActivity.this, "Please Select a Social Category", Toast.LENGTH_SHORT).show();
                } else if (memberDob.isEmpty()) {
                    Toast.makeText(AddShgMemberActivity.this, "Please Fill Date of Birth", Toast.LENGTH_SHORT).show();
                } else if (memberDisabilityId == 0L) {
                    Toast.makeText(AddShgMemberActivity.this, "Please Select a Disability", Toast.LENGTH_SHORT).show();
                } else if (memberReligionId == 0L) {
                    Toast.makeText(AddShgMemberActivity.this, "Please Select a Religion", Toast.LENGTH_SHORT).show();
                } else if (memberGenderId == 0L) {
                    Toast.makeText(AddShgMemberActivity.this, "Please Select a Gender", Toast.LENGTH_SHORT).show();
                } else if (memberPipCategoryId == 0L) {
                    Toast.makeText(AddShgMemberActivity.this, "Please Select a PIP Category", Toast.LENGTH_SHORT).show();
                } else if (memberLeaderId == 0L) {
                    Toast.makeText(AddShgMemberActivity.this, "Please Select a Leader", Toast.LENGTH_SHORT).show();
                } else if (memberDoj.isEmpty()) {
                    Toast.makeText(AddShgMemberActivity.this, "Please Fill Date of Joining", Toast.LENGTH_SHORT).show();
                } else if (memberEduLevel.isEmpty()) {
                    Toast.makeText(AddShgMemberActivity.this, "Please Fill Education Level", Toast.LENGTH_SHORT).show();
                } else if (ac_no.isEmpty()) {
                    Toast.makeText(AddShgMemberActivity.this, "Please Fill A/C number", Toast.LENGTH_SHORT).show();
                } else if (ac_no.length() < 6) {
                    Toast.makeText(AddShgMemberActivity.this, "Please Fill a valid A/C number", Toast.LENGTH_SHORT).show();
                } else if (bankName.isEmpty()) {
                    Toast.makeText(AddShgMemberActivity.this, "Please Fill Bank Name", Toast.LENGTH_SHORT).show();
                } else if (bankBranchId == 0L) {
                    Toast.makeText(AddShgMemberActivity.this, "Please Select a Bank Branch", Toast.LENGTH_SHORT).show();
                } else if (aadharNumber.isEmpty()) {
                    Toast.makeText(AddShgMemberActivity.this, "Please Fill Aadhar Number", Toast.LENGTH_SHORT).show();
                } else if (mobileNumber.isEmpty()) {
                    Toast.makeText(AddShgMemberActivity.this, "Please Fill Mobile Number", Toast.LENGTH_SHORT).show();
                } else if (mobileNumber.length() < 10) {
                    Toast.makeText(AddShgMemberActivity.this, "Invalid Mobile Number", Toast.LENGTH_SHORT).show();
                } else {
                    doShgMemberRegistration();
                }
            }
        });

        socialCatSpnAdapter = new SocialCatSpnAdapter(this, socialCategories);
        binding.spnSocialCategory.setAdapter(socialCatSpnAdapter);
        binding.spnSocialCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (intentType.equals("UPDATE")) {
                    if (isSocialCategory && position == 0) {
                        memberSocialCatId = 0L;
                    }
                    isSocialCategory = true;
                    if (isSocialCategory && position != 0) {
                        memberSocialCatId = socialCategories.get(position).valueId;
                    }
                } else {
                    memberSocialCatId = socialCategories.get(position).valueId;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        disabilitySpnAdapter = new DisabilitySpnAdapter(this, disabilities);
        genderSpnAdapter = new GenderSpnAdapter(this, genders);
        binding.spnAddShgMemberGender.setAdapter(genderSpnAdapter);
        binding.spnAddShgMemberGender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (intentType.equals("UPDATE")) {
                    if (isGender && position == 0) {
                        memberGenderId = 0L;
                    }
                    isGender = true;
                    if (isGender && position != 0) {
                        memberGenderId = genders.get(position).valueId;
                    }
                } else {
                    memberGenderId = genders.get(position).valueId;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        religionSpnAdapter = new ReligionSpnAdapter(this, religions);
        binding.spnAddShgMemberReligion.setAdapter(religionSpnAdapter);
        binding.spnAddShgMemberReligion.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (intentType.equals("UPDATE")) {
                    if (isReligion && position == 0) {
                        memberReligionId = 0L;
                    }
                    isReligion = true;
                    if (isReligion && position != 0) {
                        memberReligionId = religions.get(position).valueId;
                    }
                } else {
                    memberReligionId = religions.get(position).valueId;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        pipCategorySpnAdapter = new PipCategorySpnAdapter(this, pipCategories);
        binding.spnAddShgMemberPipCategory.setAdapter(pipCategorySpnAdapter);
        binding.spnAddShgMemberPipCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (intentType.equals("UPDATE")) {
                    if (isPipCategory && position == 0) {
                        memberPipCategoryId = 0L;
                    }
                    isPipCategory = true;
                    if (isPipCategory && position != 0) {
                        memberPipCategoryId = pipCategories.get(position).valueId;
                    }
                } else {
                    memberPipCategoryId = pipCategories.get(position).valueId;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        leaderSpnAdapter = new LeaderSpnAdapter(this, leaders);
        binding.spnAddShgMemberLeader.setAdapter(leaderSpnAdapter);
        binding.spnAddShgMemberLeader.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (intentType.equals("UPDATE")) {
                    if (isLeader && position == 0) {
                        memberLeaderId = 0L;
                    }
                    isLeader = true;
                    if (isLeader && position != 0) {
                        memberLeaderId = leaders.get(position).valueId;
                    }
                } else {
                    memberLeaderId = leaders.get(position).valueId;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        binding.spnAddShgMemberDisability.setAdapter(disabilitySpnAdapter);
        binding.spnAddShgMemberDisability.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (intentType.equals("UPDATE")) {
                    if (isDisability && position == 0) {
                        memberDisabilityId = 0L;
                    }
                    isDisability = true;
                    if (isDisability && position != 0) {
                        memberDisabilityId = disabilities.get(position).valueId;
                    }
                } else {
                    memberDisabilityId = disabilities.get(position).valueId;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        binding.progressCircular.setVisibility(View.GONE);

    }

    private void getBranchListWRTBank() {

        AndroidNetworking.get(BuildConfig.BASE_URL + Constant.API_PATH + "lov/branchList/" + bankName)
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
                                        branchList.clear();
                                        BankBranch br = new BankBranch();
                                        br.branchName = "Select a Branch";
                                        br.ifsc = "IFSC";
                                        br.bankBranchId = 0L;
                                        branchList.add(br);
                                        binding.spnMemBranchList.setSelection(0);
                                        for (int i = 0; i < array.length(); i++) {
                                            BankBranch branch = BankBranch.parseBankBRanch(array.optJSONObject(i));
                                            branchList.add(branch);
                                        }
                                        branchSpnAdapter.notifyDataSetChanged();
                                        if (intentType.equals("UPDATE")) {
                                            for (int i = 0; i < branchList.size(); i++) {
                                                if (bankBranchId.equals(branchList.get(i).bankBranchId)) {
                                                    binding.spnMemBranchList.setSelection(i);
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

    private void doShgMemberRegistration() {
        binding.progressCircular.setVisibility(View.VISIBLE);
        if (intentType.equals("UPDATE")) {
//            if (memberPipCategoryId != null && memberPipCategoryId == 0){
//                memberPipCategoryId = null;
//            }

            JSONObject bbReqObj = new JSONObject();
            try {
                bbReqObj.put("bankBranchId", bankBranchId);
            } catch (JSONException e) {
                e.printStackTrace();
            }


            JSONObject reqObj = new JSONObject();
            try {
                reqObj.put("memberId", shgMemberId);
                reqObj.put("memberName", shgMemberName);
                reqObj.put("shgId", shgId);
                reqObj.put("fatherHusbName", father_husbandName);
                reqObj.put("memberCode", memberCode);
                reqObj.put("socialCategory", memberSocialCatId);
                reqObj.put("memberDob2", memberDob);
                reqObj.put("disabilityType", memberDisabilityId);
                reqObj.put("religion", memberReligionId);
                reqObj.put("gender", memberGenderId);
                reqObj.put("pipCategory", memberPipCategoryId);
                reqObj.put("leader", memberLeaderId);
                reqObj.put("dateOfJoin2", memberDoj);
                reqObj.put("eduLevel", memberEduLevel);
                reqObj.put("enrolledInPmjjy", insPMJJY);
                reqObj.put("enrolledInPmsby", insPMSBY);
                reqObj.put("enrolledInOther", insOther);
                reqObj.put("enrolledInApy", insApy);
                reqObj.put("aadharNo", aadharNumber);
                reqObj.put("mobileNo", mobileNumber);
                reqObj.put("bankBranch", bbReqObj);
                reqObj.put("accountNo", ac_no);
                reqObj.put("isAadharSeeded", aadharSeeded);
                reqObj.put("signatureAttachPath1", null);

            } catch (JSONException e) {
                e.printStackTrace();
            }


            AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "shg/member/save")
                    .addHeaders("Authorization", "Bearer " + token)
                    .addJSONObjectBody(reqObj)
                    .setTag("Update Member Profile")
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

                                        Toast.makeText(AddShgMemberActivity.this, resObj.optString("message"), Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(AddShgMemberActivity.this, resObj.optString("message"), Toast.LENGTH_SHORT).show();
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

            JSONObject bbReqObj = new JSONObject();
            try {
                bbReqObj.put("bankBranchId", bankBranchId);
            } catch (JSONException e) {
                e.printStackTrace();
            }


            JSONObject reqObj = new JSONObject();
            try {
                reqObj.put("memberId", null);
                reqObj.put("memberName", shgMemberName);
                reqObj.put("shgId", shgId);
                reqObj.put("fatherHusbName", father_husbandName);
                reqObj.put("memberCode", memberCode);
                reqObj.put("socialCategory", memberSocialCatId);
                reqObj.put("memberDob2", memberDob);
                reqObj.put("disabilityType", memberDisabilityId);
                reqObj.put("religion", memberReligionId);
                reqObj.put("gender", memberGenderId);
                reqObj.put("pipCategory", memberPipCategoryId);
                reqObj.put("leader", memberLeaderId);
                reqObj.put("dateOfJoin2", memberDoj);
                reqObj.put("eduLevel", memberEduLevel);
                reqObj.put("enrolledInPmjjy", insPMJJY);
                reqObj.put("enrolledInPmsby", insPMSBY);
                reqObj.put("enrolledInOther", insOther);
                reqObj.put("enrolledInApy", insApy);
                reqObj.put("aadharNo", aadharNumber);
                reqObj.put("mobileNo", mobileNumber);
                reqObj.put("bankBranch", bbReqObj);
                reqObj.put("accountNo", ac_no);
                reqObj.put("isAadharSeeded", aadharSeeded);
                reqObj.put("signatureAttachPath1", null);

            } catch (JSONException e) {
                e.printStackTrace();
            }


            AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "shg/member/save")
                    .addHeaders("Authorization", "Bearer " + token)
                    .setTag("Create Member Profile")
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

                                        Toast.makeText(AddShgMemberActivity.this, resObj.optString("message"), Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(AddShgMemberActivity.this, resObj.optString("message"), Toast.LENGTH_SHORT).show();
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

    private void getReligion() {
        AndroidNetworking.get(BuildConfig.BASE_URL + Constant.API_PATH + "lov/religion")
                .addHeaders("Authorization", "Bearer " + token)
                .setTag("Activity")
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
                                        religions.clear();
                                        Religion religion = new Religion();
                                        religion.valueId = 0L;
                                        religion.valueEn = "Select Religion";
                                        religions.add(religion);
                                        for (int i = 0; i < array.length(); i++) {
                                            Religion disability = Religion.parseReligion(array.optJSONObject(i));
                                            religions.add(disability);
                                        }
                                        religionSpnAdapter.notifyDataSetChanged();

                                        if (intentType.equals("UPDATE")) {
                                            for (int i = 0; i < religions.size(); i++) {
                                                if (memberReligionId.equals(religions.get(i).valueId)) {
                                                    binding.spnAddShgMemberReligion.setSelection(i);
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

    private void getGender() {
        AndroidNetworking.get(BuildConfig.BASE_URL + Constant.API_PATH + "lov/gender")
                .addHeaders("Authorization", "Bearer " + token)
                .setTag("Activity")
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
                                        genders.clear();
                                        Gender gender = new Gender();
                                        gender.valueId = 0L;
                                        gender.valueEn = "Select Gender";
                                        genders.add(gender);

                                        for (int i = 0; i < array.length(); i++) {
                                            Gender disability = Gender.parseGender(array.optJSONObject(i));
                                            genders.add(disability);
                                        }
                                        genderSpnAdapter.notifyDataSetChanged();


                                        if (intentType.equals("UPDATE")) {
                                            for (int i = 0; i < genders.size(); i++) {
                                                if (memberGenderId.equals(genders.get(i).valueId)) {
                                                    binding.spnAddShgMemberGender.setSelection(i);
                                                }
                                                genderSpnAdapter.notifyDataSetChanged();
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

    private void getLeader() {
        AndroidNetworking.get(BuildConfig.BASE_URL + Constant.API_PATH + "lov/leader")
                .addHeaders("Authorization", "Bearer " + token)
                .setTag("Activity")
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
                                        leaders.clear();
                                        Leader leader = new Leader();
                                        leader.valueId = 0L;
                                        leader.valueEn = "Select Leader";
                                        leaders.add(leader);

                                        for (int i = 0; i < array.length(); i++) {
                                            Leader disability = Leader.parseLeader(array.optJSONObject(i));
                                            leaders.add(disability);
                                        }
                                        leaderSpnAdapter.notifyDataSetChanged();


                                        if (intentType.equals("UPDATE")) {
                                            for (int i = 0; i < leaders.size(); i++) {
                                                if (memberLeaderId.equals(leaders.get(i).valueId)) {
                                                    binding.spnAddShgMemberLeader.setSelection(i);
                                                }
                                                leaderSpnAdapter.notifyDataSetChanged();
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

    private void getPipCategory() {
        AndroidNetworking.get(BuildConfig.BASE_URL + Constant.API_PATH + "lov/pip")
                .addHeaders("Authorization", "Bearer " + token)
                .setTag("Activity")
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
                                        pipCategories.clear();

                                        PipCategory pipCategory = new PipCategory();
                                        pipCategory.valueId = 0L;
                                        pipCategory.valueEn = "Select PipCategory";
                                        pipCategories.add(pipCategory);

                                        for (int i = 0; i < array.length(); i++) {
                                            PipCategory disability = PipCategory.parsePipCategory(array.optJSONObject(i));
                                            pipCategories.add(disability);
                                        }
                                        pipCategorySpnAdapter.notifyDataSetChanged();

                                        if (intentType.equals("UPDATE")) {
                                            for (int i = 0; i < pipCategories.size(); i++) {
                                                if (memberPipCategoryId.equals(pipCategories.get(i).valueId)) {
                                                    binding.spnAddShgMemberPipCategory.setSelection(i);
                                                }
                                                pipCategorySpnAdapter.notifyDataSetChanged();
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

    private void getDisability() {
        AndroidNetworking.get(BuildConfig.BASE_URL + Constant.API_PATH + "lov/disability")
                .addHeaders("Authorization", "Bearer " + token)
                .setTag("Activity")
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
                                        disabilities.clear();

                                        Disability disability = new Disability();
                                        disability.valueId = 0L;
                                        disability.valueEn = "Select Disability";
                                        disabilities.add(disability);
                                        for (int i = 0; i < array.length(); i++) {
                                            Disability disability1 = Disability.parseDisability(array.optJSONObject(i));
                                            disabilities.add(disability1);
                                        }
                                        disabilitySpnAdapter.notifyDataSetChanged();

                                        if (intentType.equals("UPDATE")) {
                                            for (int i = 0; i < disabilities.size(); i++) {
                                                if (memberDisabilityId.equals(disabilities.get(i).valueId)) {
                                                    binding.spnAddShgMemberDisability.setSelection(i);
                                                }
                                                disabilitySpnAdapter.notifyDataSetChanged();
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

    private void getSocialCategory() {
        AndroidNetworking.get(BuildConfig.BASE_URL + Constant.API_PATH + "lov/socialCategory")
                .addHeaders("Authorization", "Bearer " + token)
                .setTag("Activity")
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
                                        socialCategories.clear();

                                        SocialCategory socialCategory = new SocialCategory();
                                        socialCategory.valueId = 0L;
                                        socialCategory.valueEn = "Select SocialCategory";
                                        socialCategories.add(socialCategory);
                                        for (int i = 0; i < array.length(); i++) {
                                            SocialCategory disability = SocialCategory.parseSocialCategory(array.optJSONObject(i));
                                            socialCategories.add(disability);
                                        }
                                        socialCatSpnAdapter.notifyDataSetChanged();
                                        if (intentType.equals("UPDATE")) {
                                            for (int i = 0; i < socialCategories.size(); i++) {
                                                if (memberSocialCatId.equals(socialCategories.get(i).valueId)) {
                                                    binding.spnSocialCategory.setSelection(i);
                                                }
                                                socialCatSpnAdapter.notifyDataSetChanged();
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

//    final Calendar myCalendar = Calendar.getInstance();

    private void openDatePicker(int type) {
        Calendar myCalendar = Calendar.getInstance();
        final DatePickerDialog datePickerDialog = new DatePickerDialog(AddShgMemberActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, month);
                myCalendar.set(Calendar.DAY_OF_MONTH, day);

//                Year = year;
//                Month = month + 1;

                String myFormat = "dd/MM/yyyy"; //In which you need put here
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

                if (type == 0) {
                    binding.etShgMemberDob.setText(sdf.format(myCalendar.getTime()));
                }
                if (type == 1) {
                    binding.etShgMemberJoining.setText(sdf.format(myCalendar.getTime()));
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