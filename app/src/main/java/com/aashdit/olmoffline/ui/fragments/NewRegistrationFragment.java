package com.aashdit.olmoffline.ui.fragments;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aashdit.olmoffline.BuildConfig;
import com.aashdit.olmoffline.R;
import com.aashdit.olmoffline.adapters.ActivitySpnAdapter;
import com.aashdit.olmoffline.adapters.ActivityTypeSpnAdapter;
import com.aashdit.olmoffline.adapters.SchemeSpnAdapter;
import com.aashdit.olmoffline.adapters.ShgSpnAdapter;
import com.aashdit.olmoffline.databinding.FragmentNewRegistrationBinding;
import com.aashdit.olmoffline.models.Activity;
import com.aashdit.olmoffline.models.ActivityType;
import com.aashdit.olmoffline.models.SHG;
import com.aashdit.olmoffline.models.Schemes;
import com.aashdit.olmoffline.receiver.ConnectivityChangeReceiver;
import com.aashdit.olmoffline.ui.activities.AddBirdActivity;
import com.aashdit.olmoffline.ui.activities.AddBirdClusterActivity;
import com.aashdit.olmoffline.ui.activities.AddBirdHouseHoldActivity;
import com.aashdit.olmoffline.ui.activities.AddClusterActivity;
import com.aashdit.olmoffline.ui.activities.AddDiaryActivity;
import com.aashdit.olmoffline.ui.activities.AddDiaryCLFActivity;
import com.aashdit.olmoffline.ui.activities.AddDiaryHouseHoldActivity;
import com.aashdit.olmoffline.ui.activities.AddFarmingHHActivity;
import com.aashdit.olmoffline.ui.activities.AddFarmingShgActivity;
import com.aashdit.olmoffline.ui.activities.AddFishActivity;
import com.aashdit.olmoffline.ui.activities.AddFishCLFActivity;
import com.aashdit.olmoffline.ui.activities.AddFishHouseHoldActivity;
import com.aashdit.olmoffline.ui.activities.AddHouseHoldActivity;
import com.aashdit.olmoffline.ui.activities.AddShgActivity;
import com.aashdit.olmoffline.ui.activities.NFHHRegdActivity;
import com.aashdit.olmoffline.ui.activities.NFSHGRegdActivity;
import com.aashdit.olmoffline.ui.activities.NTFPHHRegdActivity;
import com.aashdit.olmoffline.ui.activities.NTFPSHGRegdActivity;
import com.aashdit.olmoffline.ui.activities.NtfpTasarHHActivity;
import com.aashdit.olmoffline.ui.activities.NtfpTasarShgActivity;
import com.aashdit.olmoffline.utils.Constant;
import com.aashdit.olmoffline.utils.SharedPrefManager;
import com.aashdit.olmoffline.utils.Utility;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class NewRegistrationFragment extends Fragment implements ConnectivityChangeReceiver.ConnectivityReceiverListener {

    private static final String TAG = "NewRegistrationFragment";
    private FragmentNewRegistrationBinding binding;

    private Long selectedScheme = 0L;
    private Long selectedActivity = 0L;
    private Long selectedShg = 0L;
    private Long selectedActivityType = 0L;
    private Long selectedActivityByType = 0L;


    private SharedPrefManager sp;
    private String token;
    private String reportingLevelCode = "";


    private SchemeSpnAdapter schemeSpnAdapter;
    private ActivitySpnAdapter activitySpnAdapter;
    private ShgSpnAdapter shgSpnAdapter;
    private ActivityTypeSpnAdapter activityTypeSpnAdapter;

    private ArrayList<Schemes> schemesArrayList;
    private ArrayList<ActivityType> activityTypeArrayList;
    //    private ArrayList<ActivityByType> activityByTypeArrayList;
    private ArrayList<com.aashdit.olmoffline.models.Activity> activityArrayList;
    private ArrayList<SHG> shgArrayList;

    /**
     * @param isConnected for checking network connectivity
     */
    private boolean isConnected = false;
    private ConnectivityChangeReceiver mConnectivityChangeReceiver;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentNewRegistrationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sp = SharedPrefManager.getInstance(getActivity());
        token = sp.getStringData(Constant.APP_TOKEN);

        mConnectivityChangeReceiver = new ConnectivityChangeReceiver();
        mConnectivityChangeReceiver.setConnectivityReceiverListener(this);
        isConnected = mConnectivityChangeReceiver.getConnectionStatus(getContext());
        registerNetworkBroadcast();
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.MINUTES)
                .readTimeout(10, TimeUnit.MINUTES)
                .writeTimeout(10, TimeUnit.MINUTES)
                .build();
        AndroidNetworking.initialize(getActivity(), okHttpClient);


        Schemes schemes = new Schemes();
        schemes.setSchemeId(0L);
        schemes.setSchemeCode("Select");
        schemes.setSchemeNameEn("Select Scheme");
        schemes.setSchemeNameHi("Select Scheme");
        schemes.setActive(true);

        schemesArrayList = new ArrayList<>();
        schemesArrayList.add(schemes);


        Activity activity = new Activity();
        activity.setActivityCode("ACTIVITY");
        activity.setActivityId(0L);
        activity.setActivityNameEn("Select Activity");
        activity.setActivityNameHi("Select Activity");

        activityArrayList = new ArrayList<>();
        activityArrayList.add(activity);

        ActivityType activityType = new ActivityType();
        activityType.setActivityCode("ActivityType");
        activityType.setActivityId(0L);
        activityType.setActivityTypeDesc("Select ActivityType");
        activityType.setActive(true);

        activityTypeArrayList = new ArrayList<>();
        activityTypeArrayList.add(activityType);

//        ActivityByType activityByType = new ActivityByType();
//        activityByType.setActivityCode("ActivityType");
//        activityByType.setActivityId(0L);
//        activityByType.setActivityNameEn("Select ActivityByType");
//        activityByType.setActivityNameHi("Select ActivityByType");

//        activityByTypeArrayList = new ArrayList<>();
//        activityByTypeArrayList.add(activityByType);

        SHG shg = new SHG();
        shg.setShgDetailsId(0L);
        shg.setShgName("Select a SHG");
        shg.setShgRegNumber("0");

        shgArrayList = new ArrayList<>();
        shgArrayList.add(shg);

        activityTypeSpnAdapter = new ActivityTypeSpnAdapter(getActivity(), activityTypeArrayList);
        binding.spnActType.setAdapter(activityTypeSpnAdapter);

        schemeSpnAdapter = new SchemeSpnAdapter(getActivity(), schemesArrayList);
        binding.spnSchemes.setAdapter(schemeSpnAdapter);

        activitySpnAdapter = new ActivitySpnAdapter(getActivity(), activityArrayList);
        binding.spnActivity.setAdapter(activitySpnAdapter);

        shgSpnAdapter = new ShgSpnAdapter(getActivity(), shgArrayList);
        binding.spnShg.setAdapter(shgSpnAdapter);

        binding.spnSchemes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    selectedActivityType = 0L;
                    selectedActivity = 0L;
                    selectedScheme = 0L;
                    Activity activity = new Activity();
                    activity.setActivityCode("ACTIVITY");
                    activity.setActivityId(0L);
                    activity.setActivityNameEn("Select Activity");
                    activity.setActivityNameHi("Select Activity");

                    activityArrayList = new ArrayList<>();
                    activityArrayList.clear();
                    activityArrayList.add(activity);
                    activitySpnAdapter = new ActivitySpnAdapter(getActivity(), activityArrayList);
                    binding.spnActivity.setAdapter(activitySpnAdapter);

                    if (sp.getStringData(Constant.USER_ROLE).equals("ROLE_KMITRA") ||
                            sp.getStringData(Constant.USER_ROLE).equals("ROLE_UMITRA") ||
                            sp.getStringData(Constant.USER_ROLE).equals("ROLE_CRPEP")) {
                        ActivityType activityType = new ActivityType();
                        activityType.setActivityCode("ActivityType");
                        activityType.setActivityId(0L);
                        activityType.setActivityTypeDesc("Select ActivityType");
                        activityType.setActive(true);

                        activityTypeArrayList = new ArrayList<>();
                        activityTypeArrayList.clear();
                        activityTypeArrayList.add(activityType);
                        activityTypeSpnAdapter = new ActivityTypeSpnAdapter(getActivity(), activityTypeArrayList);
                        binding.spnActType.setAdapter(activityTypeSpnAdapter);
                    }

                } else {
                    selectedScheme = schemesArrayList.get(position).getSchemeId();

                    Activity activity = new Activity();
                    activity.setActivityCode("ACTIVITY");
                    activity.setActivityId(0L);
                    activity.setActivityNameEn("Select Activity");
                    activity.setActivityNameHi("Select Activity");

                    activityArrayList = new ArrayList<>();
                    activityArrayList.clear();
                    activityArrayList.add(activity);
                    activitySpnAdapter = new ActivitySpnAdapter(getActivity(), activityArrayList);
                    binding.spnActivity.setAdapter(activitySpnAdapter);

                    selectedActivityType = 0L;
                    selectedActivity = 0L;

                    if (sp.getStringData(Constant.USER_ROLE).equals("ROLE_PMITRA")) {
                        getActivityAccordingToScheme(selectedScheme);
                    } else if (sp.getStringData(Constant.USER_ROLE).equals("ROLE_KMITRA") ||
                            sp.getStringData(Constant.USER_ROLE).equals("ROLE_UMITRA") ||
                            sp.getStringData(Constant.USER_ROLE).equals("ROLE_CRPEP")) {
                        resetActivityType();
                        getActivityTypeByScheme();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        binding.spnActType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                if (position == 0) {
//                    selectedActivityType = 0L;
//                    selectedActivity = 0L;
//
//                    Activity activity = new Activity();
//                    activity.setActivityCode("ACTIVITY");
//                    activity.setActivityId(0L);
//                    activity.setActivityNameEn("Select Activity");
//                    activity.setActivityNameHi("Select Activity");
//
//                    activityArrayList = new ArrayList<>();
//                    activityArrayList.clear();
//                    activityArrayList.add(activity);
//                    activitySpnAdapter = new ActivitySpnAdapter(getActivity(), activityArrayList);
//                    binding.spnActivity.setAdapter(activitySpnAdapter);
                    resetActivityList();
                } else {
                    resetActivityList();
                    selectedActivityType = activityTypeArrayList.get(position).getActivityId();
                    getActivityByType(selectedActivityType);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        binding.spnActivity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    selectedActivity = 0L;
                } else {
                    selectedActivity = activityArrayList.get(position).getActivityId();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        if (sp.getStringData(Constant.USER_ROLE).equals(Constant.KMITRA)
                || sp.getStringData(Constant.USER_ROLE).equals(Constant.UMITRA)
                || sp.getStringData(Constant.USER_ROLE).equals(Constant.CRPEP)) {
            binding.llSelectType.setVisibility(View.VISIBLE);
        } else {
            binding.llSelectType.setVisibility(View.GONE);
        }

        if (sp.getStringData(Constant.USER_ROLE).equals(Constant.PMITRA)) {
            binding.llCategory.setVisibility(View.VISIBLE);
            binding.llCategory2.setVisibility(View.GONE);
        }
        if (sp.getStringData(Constant.USER_ROLE).equals(Constant.KMITRA)) {
            binding.llCategory.setVisibility(View.VISIBLE);
            binding.llCategory2.setVisibility(View.GONE);
        }

        if (sp.getStringData(Constant.USER_ROLE).equals(Constant.UMITRA)) {
            binding.llCategory.setVisibility(View.GONE);
            binding.llCategory2.setVisibility(View.VISIBLE);
            binding.cvShg.setVisibility(View.GONE);
            binding.cvHh.setVisibility(View.GONE);
            binding.cvEg.setVisibility(View.GONE);
        } else if (sp.getStringData(Constant.USER_ROLE).equals(Constant.CRPEP)) {
            binding.llCategory.setVisibility(View.VISIBLE);
            binding.cvPg.setVisibility(View.GONE);
            binding.cvShg.setVisibility(View.VISIBLE);
            binding.cvHh.setVisibility(View.VISIBLE);
            binding.cvEg.setVisibility(View.VISIBLE);
        }

        binding.spnShg.setVisibility(View.GONE);
        binding.progressBar.setVisibility(View.GONE);

        binding.spnShg.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {

                } else {
                    selectedShg = shgArrayList.get(position).getShgDetailsId();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        binding.cvHh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                binding.spnShg.setVisibility(View.VISIBLE);
                reportingLevelCode = "HH";
                binding.tvClf.setTextColor(getResources().getColor(R.color.black));
                binding.tvHh.setTextColor(getResources().getColor(R.color.purple_500));
                binding.tvShg.setTextColor(getResources().getColor(R.color.black));
                binding.tvPg.setTextColor(getResources().getColor(R.color.black));
                binding.tvEg.setTextColor(getResources().getColor(R.color.black));
//                getShgList();
            }
        });

        binding.cvClf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                binding.spnShg.setVisibility(View.GONE);
                reportingLevelCode = "CLF";
                binding.tvClf.setTextColor(getResources().getColor(R.color.purple_500));
                binding.tvHh.setTextColor(getResources().getColor(R.color.black));
                binding.tvShg.setTextColor(getResources().getColor(R.color.black));
                binding.tvPg.setTextColor(getResources().getColor(R.color.black));
                binding.tvEg.setTextColor(getResources().getColor(R.color.black));
            }
        });

        binding.cvShg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                binding.spnShg.setVisibility(View.GONE);
                reportingLevelCode = "SHG";
                binding.tvClf.setTextColor(getResources().getColor(R.color.black));
                binding.tvHh.setTextColor(getResources().getColor(R.color.black));
                binding.tvShg.setTextColor(getResources().getColor(R.color.purple_500));
                binding.tvPg.setTextColor(getResources().getColor(R.color.black));
                binding.tvEg.setTextColor(getResources().getColor(R.color.black));
            }
        });

        binding.cvPg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                binding.spnShg.setVisibility(View.GONE);
                reportingLevelCode = "PG";
                binding.tvClf.setTextColor(getResources().getColor(R.color.black));
                binding.tvHh.setTextColor(getResources().getColor(R.color.black));
                binding.tvEg.setTextColor(getResources().getColor(R.color.black));
                binding.tvShg.setTextColor(getResources().getColor(R.color.black));
                binding.tvPg.setTextColor(getResources().getColor(R.color.purple_500));
            }
        });
        binding.cvEg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                binding.spnShg.setVisibility(View.GONE);
                reportingLevelCode = "EG";
                binding.tvClf.setTextColor(getResources().getColor(R.color.black));
                binding.tvHh.setTextColor(getResources().getColor(R.color.black));
                binding.tvPg.setTextColor(getResources().getColor(R.color.black));
                binding.tvShg.setTextColor(getResources().getColor(R.color.black));
                binding.tvEg.setTextColor(getResources().getColor(R.color.purple_500));
            }
        });


        getSchemes();
//        if (sp.getStringData(Constant.USER_ROLE).equals("ROLE_KMITRA"))
        binding.rlNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConnected) {
                    if (selectedScheme.equals(0L)) {
                        Toast.makeText(getActivity(), "Please select a scheme", Toast.LENGTH_SHORT).show();
                    } else if (sp.getStringData(Constant.USER_ROLE).equals("ROLE_KMITRA") && selectedActivityType.equals(0L) ||
                            sp.getStringData(Constant.USER_ROLE).equals("ROLE_UMITRA") && selectedActivityType.equals(0L)
                            || sp.getStringData(Constant.USER_ROLE).equals("ROLE_CRPEP") && selectedActivityType.equals(0L)) {
                        Toast.makeText(getActivity(), "Please select an activity type", Toast.LENGTH_SHORT).show();
                    } else if (selectedActivity.equals(0L)) {
                        Toast.makeText(getActivity(), "Please select an activity", Toast.LENGTH_SHORT).show();
                    } else if (reportingLevelCode.equals("")) {
                        Toast.makeText(getActivity(), "Please select Reporting Level", Toast.LENGTH_SHORT).show();
                    } /*else  if (reportingLevelCode.equals("HH") && selectedShg.equals(0L)){
                    Toast.makeText(getActivity(), "Please select a SHG", Toast.LENGTH_SHORT).show();
                }*/ else {

                        if (reportingLevelCode.equals("SHG")) {
                            if (selectedActivity.equals(1L)) {
                                Intent addShgActivity = new Intent(getActivity(), AddShgActivity.class);
                                addShgActivity.putExtra("SCHEME_ID", selectedScheme);
                                addShgActivity.putExtra("ACTIVITY_ID", selectedActivity);
                                addShgActivity.putExtra("ENTITY_TYPE_CODE", reportingLevelCode);
                                addShgActivity.putExtra(Constant.INTENT_TYPE, Constant.ADD);
                                startActivity(addShgActivity);
                            } else if (selectedActivity.equals(2L)) {
                                Intent addShgActivity = new Intent(getActivity(), AddBirdActivity.class);
                                addShgActivity.putExtra("SCHEME_ID", selectedScheme);
                                addShgActivity.putExtra("ACTIVITY_ID", selectedActivity);
                                addShgActivity.putExtra("ENTITY_TYPE_CODE", reportingLevelCode);
                                addShgActivity.putExtra(Constant.INTENT_TYPE, Constant.ADD);
                                startActivity(addShgActivity);
                            } else if (selectedActivity.equals(3L)) {
                                Intent addShgActivity = new Intent(getActivity(), AddFishActivity.class);
                                addShgActivity.putExtra("SCHEME_ID", selectedScheme);
                                addShgActivity.putExtra("ACTIVITY_ID", selectedActivity);
                                addShgActivity.putExtra("ENTITY_TYPE_CODE", reportingLevelCode);
                                addShgActivity.putExtra(Constant.INTENT_TYPE, Constant.ADD);
                                startActivity(addShgActivity);
                            } else if (selectedActivity.equals(7L)) {
                                Intent addShgActivity = new Intent(getActivity(), AddDiaryActivity.class);
                                addShgActivity.putExtra("SCHEME_ID", selectedScheme);
                                addShgActivity.putExtra("ACTIVITY_ID", selectedActivity);
                                addShgActivity.putExtra("ENTITY_TYPE_CODE", reportingLevelCode);
                                addShgActivity.putExtra(Constant.INTENT_TYPE, Constant.ADD);
                                startActivity(addShgActivity);
                            }
                            if (sp.getStringData(Constant.USER_ROLE).equals("ROLE_KMITRA")) {
                                Intent addShgActivity = new Intent(getActivity(), AddFarmingShgActivity.class);
                                addShgActivity.putExtra("SCHEME_ID", selectedScheme);
                                addShgActivity.putExtra("ACTIVITY_ID", selectedActivity);
                                addShgActivity.putExtra("ENTITY_TYPE_CODE", reportingLevelCode);
                                addShgActivity.putExtra(Constant.INTENT_TYPE, Constant.ADD);
                                startActivity(addShgActivity);
                            }
                            if (/*sp.getStringData(Constant.USER_ROLE).equals("ROLE_UMITRA") ||*/ sp.getStringData(Constant.USER_ROLE).equals("ROLE_CRPEP")) {
                                if (selectedActivityType.equals(13L)) {
                                    Intent addShgActivity = new Intent(getActivity(), NTFPSHGRegdActivity.class);
                                    addShgActivity.putExtra("SCHEME_ID", selectedScheme);
                                    addShgActivity.putExtra("ACTIVITY_ID", selectedActivity);
                                    addShgActivity.putExtra("ENTITY_TYPE_CODE", reportingLevelCode);
                                    addShgActivity.putExtra(Constant.INTENT_TYPE, Constant.ADD);
                                    startActivity(addShgActivity);
                                } else if (selectedActivityType.equals(2L)) {

                                    Intent addShgActivity = new Intent(getActivity(), NtfpTasarShgActivity.class);
                                    addShgActivity.putExtra("SCHEME_ID", selectedScheme);
                                    addShgActivity.putExtra("ACTIVITY_ID", selectedActivity);
                                    addShgActivity.putExtra("ENTITY_TYPE_CODE", reportingLevelCode);
                                    addShgActivity.putExtra(Constant.INTENT_TYPE, Constant.ADD);
                                    startActivity(addShgActivity);
                                } else {
                                    Intent addShgActivity = new Intent(getActivity(), NFSHGRegdActivity.class);
                                    addShgActivity.putExtra("SCHEME_ID", selectedScheme);
                                    addShgActivity.putExtra("ACTIVITY_ID", selectedActivity);
                                    addShgActivity.putExtra("ENTITY_TYPE_CODE", reportingLevelCode);
                                    addShgActivity.putExtra(Constant.INTENT_TYPE, Constant.ADD);
                                    startActivity(addShgActivity);
                                }
                            }
                        } else if (reportingLevelCode.equals("CLF")) {
                            if (selectedActivity.equals(1L)) {
                                Intent addShgActivity = new Intent(getActivity(), AddClusterActivity.class);
                                addShgActivity.putExtra("SCHEME_ID", selectedScheme);
                                addShgActivity.putExtra("ACTIVITY_ID", selectedActivity);
                                addShgActivity.putExtra("ENTITY_TYPE_CODE", reportingLevelCode);
                                startActivity(addShgActivity);
                            } else if (selectedActivity.equals(2L)) {
                                Intent addShgActivity = new Intent(getActivity(), AddBirdClusterActivity.class);
                                addShgActivity.putExtra("SCHEME_ID", selectedScheme);
                                addShgActivity.putExtra("ACTIVITY_ID", selectedActivity);
                                addShgActivity.putExtra("ENTITY_TYPE_CODE", reportingLevelCode);
                                startActivity(addShgActivity);
                            } else if (selectedActivity.equals(3L)) {
                                Intent addShgActivity = new Intent(getActivity(), AddFishCLFActivity.class);
                                addShgActivity.putExtra("SCHEME_ID", selectedScheme);
                                addShgActivity.putExtra("ACTIVITY_ID", selectedActivity);
                                addShgActivity.putExtra("ENTITY_TYPE_CODE", reportingLevelCode);
                                startActivity(addShgActivity);
                            } else if (selectedActivity.equals(7L)) {
                                Intent addShgActivity = new Intent(getActivity(), AddDiaryCLFActivity.class);
                                addShgActivity.putExtra("SCHEME_ID", selectedScheme);
                                addShgActivity.putExtra("ACTIVITY_ID", selectedActivity);
                                addShgActivity.putExtra("ENTITY_TYPE_CODE", reportingLevelCode);
                                startActivity(addShgActivity);
                            }
                        } else if (reportingLevelCode.equals("HH")) {
                            if (selectedActivity.equals(1L)) {
                                Intent addShgActivity = new Intent(getActivity(), AddHouseHoldActivity.class);
                                addShgActivity.putExtra("SCHEME_ID", selectedScheme);
                                addShgActivity.putExtra("ACTIVITY_ID", selectedActivity);
                                addShgActivity.putExtra("ENTITY_TYPE_CODE", reportingLevelCode);
                                addShgActivity.putExtra(Constant.INTENT_TYPE, Constant.ADD);
                                startActivity(addShgActivity);
                            } else if (selectedActivity.equals(2L)) {
                                Intent addShgActivity = new Intent(getActivity(), AddBirdHouseHoldActivity.class);
                                addShgActivity.putExtra("SCHEME_ID", selectedScheme);
                                addShgActivity.putExtra("ACTIVITY_ID", selectedActivity);
                                addShgActivity.putExtra("ENTITY_TYPE_CODE", reportingLevelCode);
                                addShgActivity.putExtra(Constant.INTENT_TYPE, Constant.ADD);
                                startActivity(addShgActivity);
                            } else if (selectedActivity.equals(3L)) {
                                Intent addShgActivity = new Intent(getActivity(), AddFishHouseHoldActivity.class);
                                addShgActivity.putExtra("SCHEME_ID", selectedScheme);
                                addShgActivity.putExtra("ACTIVITY_ID", selectedActivity);
                                addShgActivity.putExtra("ENTITY_TYPE_CODE", reportingLevelCode);
                                addShgActivity.putExtra(Constant.INTENT_TYPE, Constant.ADD);
                                startActivity(addShgActivity);
                            } else if (selectedActivity.equals(7L)) {
                                Intent addShgActivity = new Intent(getActivity(), AddDiaryHouseHoldActivity.class);
                                addShgActivity.putExtra("SCHEME_ID", selectedScheme);
                                addShgActivity.putExtra("ACTIVITY_ID", selectedActivity);
                                addShgActivity.putExtra("ENTITY_TYPE_CODE", reportingLevelCode);
                                addShgActivity.putExtra(Constant.INTENT_TYPE, Constant.ADD);
                                startActivity(addShgActivity);
                            }
                            if (sp.getStringData(Constant.USER_ROLE).equals("ROLE_KMITRA")) {
                                Intent addShgActivity = new Intent(getActivity(), AddFarmingHHActivity.class);
                                addShgActivity.putExtra("SCHEME_ID", selectedScheme);
                                addShgActivity.putExtra("ACTIVITY_ID", selectedActivity);
                                addShgActivity.putExtra("ENTITY_TYPE_CODE", reportingLevelCode);
                                addShgActivity.putExtra(Constant.INTENT_TYPE, Constant.ADD);
                                startActivity(addShgActivity);
                            }
                            if (/*sp.getStringData(Constant.USER_ROLE).equals("ROLE_UMITRA") ||*/ sp.getStringData(Constant.USER_ROLE).equals("ROLE_CRPEP")) {
                                if (selectedActivityType.equals(13L)) {
                                    Intent addShgActivity = new Intent(getActivity(), NTFPHHRegdActivity.class);
                                    addShgActivity.putExtra("SCHEME_ID", selectedScheme);
                                    addShgActivity.putExtra("ACTIVITY_ID", selectedActivity);
                                    addShgActivity.putExtra("ENTITY_TYPE_CODE", reportingLevelCode);
                                    addShgActivity.putExtra(Constant.INTENT_TYPE, Constant.ADD);
                                    startActivity(addShgActivity);
                                } else if (selectedActivityType.equals(2L)) {

                                    Intent addShgActivity = new Intent(getActivity(), NtfpTasarHHActivity.class);
                                    addShgActivity.putExtra("SCHEME_ID", selectedScheme);
                                    addShgActivity.putExtra("ACTIVITY_ID", selectedActivity);
                                    addShgActivity.putExtra("ENTITY_TYPE_CODE", reportingLevelCode);
                                    addShgActivity.putExtra(Constant.INTENT_TYPE, Constant.ADD);
                                    startActivity(addShgActivity);
                                } else {
                                    Intent addShgActivity = new Intent(getActivity(), NFHHRegdActivity.class);
                                    addShgActivity.putExtra("SCHEME_ID", selectedScheme);
                                    addShgActivity.putExtra("ACTIVITY_ID", selectedActivity);
                                    addShgActivity.putExtra("ENTITY_TYPE_CODE", reportingLevelCode);
                                    addShgActivity.putExtra(Constant.INTENT_TYPE, Constant.ADD);
                                    startActivity(addShgActivity);
                                }
                            }
                        } else if (reportingLevelCode.equals("EG")) {
                            if (/*sp.getStringData(Constant.USER_ROLE).equals("ROLE_UMITRA") || */sp.getStringData(Constant.USER_ROLE).equals("ROLE_CRPEP")) {
                                if (selectedActivityType.equals(13L)) {
                                    Intent addShgActivity = new Intent(getActivity(), NTFPSHGRegdActivity.class);
                                    addShgActivity.putExtra("SCHEME_ID", selectedScheme);
                                    addShgActivity.putExtra("ACTIVITY_ID", selectedActivity);
                                    addShgActivity.putExtra("ENTITY_TYPE_CODE", reportingLevelCode);
                                    addShgActivity.putExtra(Constant.INTENT_TYPE, Constant.ADD);
                                    startActivity(addShgActivity);
                                } else if (selectedActivityType.equals(2L)) {

                                    Intent addShgActivity = new Intent(getActivity(), NtfpTasarShgActivity.class);
                                    addShgActivity.putExtra("SCHEME_ID", selectedScheme);
                                    addShgActivity.putExtra("ACTIVITY_ID", selectedActivity);
                                    addShgActivity.putExtra("ENTITY_TYPE_CODE", reportingLevelCode);
                                    addShgActivity.putExtra(Constant.INTENT_TYPE, Constant.ADD);
                                    startActivity(addShgActivity);
                                } else {
                                    Intent addShgActivity = new Intent(getActivity(), NFSHGRegdActivity.class);
                                    addShgActivity.putExtra("SCHEME_ID", selectedScheme);
                                    addShgActivity.putExtra("ACTIVITY_ID", selectedActivity);
                                    addShgActivity.putExtra("ENTITY_TYPE_CODE", reportingLevelCode);
                                    addShgActivity.putExtra(Constant.INTENT_TYPE, Constant.ADD);
                                    startActivity(addShgActivity);
                                }
                            }
                        } else if (reportingLevelCode.equals("PG")) {
                            if (sp.getStringData(Constant.USER_ROLE).equals("ROLE_UMITRA")) {
                                if (selectedActivityType.equals(13L)) {
                                    Intent addShgActivity = new Intent(getActivity(), NTFPSHGRegdActivity.class);
                                    addShgActivity.putExtra("SCHEME_ID", selectedScheme);
                                    addShgActivity.putExtra("ACTIVITY_ID", selectedActivity);
                                    addShgActivity.putExtra("ENTITY_TYPE_CODE", reportingLevelCode);
                                    addShgActivity.putExtra(Constant.INTENT_TYPE, Constant.ADD);
                                    startActivity(addShgActivity);
                                } else if (selectedActivityType.equals(2L)) {

                                    Intent addShgActivity = new Intent(getActivity(), NtfpTasarShgActivity.class);
                                    addShgActivity.putExtra("SCHEME_ID", selectedScheme);
                                    addShgActivity.putExtra("ACTIVITY_ID", selectedActivity);
                                    addShgActivity.putExtra("ENTITY_TYPE_CODE", reportingLevelCode);
                                    addShgActivity.putExtra(Constant.INTENT_TYPE, Constant.ADD);
                                    startActivity(addShgActivity);
                                } else {
                                    Intent addShgActivity = new Intent(getActivity(), NFSHGRegdActivity.class);
                                    addShgActivity.putExtra("SCHEME_ID", selectedScheme);
                                    addShgActivity.putExtra("ACTIVITY_ID", selectedActivity);
                                    addShgActivity.putExtra("ENTITY_TYPE_CODE", reportingLevelCode);
                                    addShgActivity.putExtra(Constant.INTENT_TYPE, Constant.ADD);
                                    startActivity(addShgActivity);
                                }
                            }
                        }
                    }
                }else {
                    Snackbar.make(binding.rlRootView,"No Internet Connection", Snackbar.LENGTH_LONG).show();
                }
            }
        });

    }

    private void resetActivityList() {
        selectedActivityType = 0L;
        selectedActivity = 0L;

        Activity activity = new Activity();
        activity.setActivityCode("ACTIVITY");
        activity.setActivityId(0L);
        activity.setActivityNameEn("Select Activity");
        activity.setActivityNameHi("Select Activity");

        activityArrayList = new ArrayList<>();
        activityArrayList.clear();
        activityArrayList.add(activity);
        activitySpnAdapter = new ActivitySpnAdapter(getActivity(), activityArrayList);
        binding.spnActivity.setAdapter(activitySpnAdapter);
    }

    private void resetActivityType() {
        ActivityType activityType = new ActivityType();
        activityType.setActivityCode("ActivityType");
        activityType.setActivityId(0L);
        activityType.setActivityTypeDesc("Select ActivityType");
        activityType.setActive(true);

        activityTypeArrayList = new ArrayList<>();
        activityTypeArrayList.clear();
        activityTypeArrayList.add(activityType);
        activityTypeSpnAdapter = new ActivityTypeSpnAdapter(getActivity(), activityTypeArrayList);
        binding.spnActType.setAdapter(activityTypeSpnAdapter);
    }

    private void getActivityByType(Long selectedActivityType) {

        AndroidNetworking.get(BuildConfig.BASE_URL + Constant.API_PATH + "lov/activityByType/" + selectedActivityType)
                .addHeaders("Authorization", "Bearer " + token)
                .setTag("Activity")
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
                                        activityArrayList.clear();

                                        Activity activity = new Activity();
                                        activity.setActivityCode("ACTIVITY");
                                        activity.setActivityId(0L);
                                        activity.setActivityNameEn("Select Activity");
                                        activity.setActivityNameHi("Select Activity");
                                        activityArrayList.add(activity);
                                        for (int i = 0; i < array.length(); i++) {
                                            Activity activity1 = Activity.parseActivity(array.optJSONObject(i),0L,0L);
                                            activityArrayList.add(activity1);
                                        }

                                        activitySpnAdapter.notifyDataSetChanged();
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
                    }
                });
    }

    private void getActivityTypeByScheme() {
        AndroidNetworking.get(BuildConfig.BASE_URL + Constant.API_PATH + "lov/activityType/" + selectedScheme)
                .addHeaders("Authorization", "Bearer " + token)
                .setTag("Activity")
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
                                        activityTypeArrayList.clear();

                                        ActivityType activity = new ActivityType();
                                        activity.setActivityCode("ActivityType");
                                        activity.setActivityId(0L);
                                        activity.setActivityTypeDesc("Select ActivityType");
                                        activity.setActive(true);
                                        activityTypeArrayList.add(activity);
                                        for (int i = 0; i < array.length(); i++) {
                                            ActivityType activity1 = ActivityType.parseActivityType(array.optJSONObject(i),null,"","");
                                            activityTypeArrayList.add(activity1);
                                        }

                                        activityTypeSpnAdapter.notifyDataSetChanged();
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
                    }
                });
    }

    private void getSchemes() {

        AndroidNetworking.get(BuildConfig.BASE_URL + Constant.API_PATH + "lov/schemes")
                .addHeaders("Authorization", "Bearer " + token)
                .setTag("Schemes")
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
                                        schemesArrayList.clear();
                                        Schemes schemes = new Schemes();
                                        schemes.setSchemeId(0L);
                                        schemes.setSchemeCode("Select");
                                        schemes.setSchemeNameEn("Select Scheme");
                                        schemes.setSchemeNameHi("Select Scheme");
                                        schemes.setActive(true);
                                        schemesArrayList.add(schemes);
                                        for (int i = 0; i < array.length(); i++) {
                                            Schemes scheme = Schemes.parseSchemes(array.optJSONObject(i),sp.getStringData(Constant.USER_ROLE));
                                            if (scheme.isActive()) {
                                                schemesArrayList.add(scheme);
                                            }
                                        }

                                        schemeSpnAdapter.notifyDataSetChanged();
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
                    }
                });

    }

    private void getActivityAccordingToScheme(Long selectedScheme) {
        AndroidNetworking.get(BuildConfig.BASE_URL + Constant.API_PATH + "lov/activity/" + selectedScheme)
                .addHeaders("Authorization", "Bearer " + token)
                .setTag("Activity")
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
                                        activityArrayList.clear();

                                        Activity activity = new Activity();
                                        activity.setActivityCode("ACTIVITY");
                                        activity.setActivityId(0L);
                                        activity.setActivityNameEn("Select Activity");
                                        activity.setActivityNameHi("Select Activity");
                                        activityArrayList.add(activity);
                                        for (int i = 0; i < array.length(); i++) {
                                            Activity activity1 = Activity.parseActivity(array.optJSONObject(i),0L,0L);
                                            activityArrayList.add(activity1);
                                        }

                                        activitySpnAdapter.notifyDataSetChanged();
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
                    }
                });
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        this.isConnected = isConnected;
    }

    private void registerNetworkBroadcast() {
        getActivity().registerReceiver(mConnectivityChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    protected void unregisterNetworkChanges() {
        try {
            getActivity().unregisterReceiver(mConnectivityChangeReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        unregisterNetworkChanges();
        super.onDestroy();
    }
}
