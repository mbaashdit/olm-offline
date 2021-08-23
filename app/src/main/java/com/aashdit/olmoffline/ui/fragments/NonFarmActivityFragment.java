package com.aashdit.olmoffline.ui.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.aashdit.olmoffline.BuildConfig;
import com.aashdit.olmoffline.R;
import com.aashdit.olmoffline.adapters.ActivitySpnAdapter;
import com.aashdit.olmoffline.adapters.ActivityTypeSpnAdapter;
import com.aashdit.olmoffline.adapters.OffShgActivityListAdapter;
import com.aashdit.olmoffline.adapters.SchemeSpnAdapter;
import com.aashdit.olmoffline.adapters.ShgHouseHoldRealmListAdapter;
import com.aashdit.olmoffline.adapters.ShgListForHHSpnAdapter;
import com.aashdit.olmoffline.adapters.ShgSpnAdapter;
import com.aashdit.olmoffline.adapters.UdyogShgActivityAdapter;
import com.aashdit.olmoffline.adapters.VillageNfSpnAdapter;
import com.aashdit.olmoffline.databinding.FragmentNonFarmActivityBinding;
import com.aashdit.olmoffline.db.hh.ActivityHHSearch;
import com.aashdit.olmoffline.db.hh.HHSHGList;
import com.aashdit.olmoffline.db.hh.HhList;
import com.aashdit.olmoffline.db.shg.ShgList;
import com.aashdit.olmoffline.models.Activity;
import com.aashdit.olmoffline.models.ActivityType;
import com.aashdit.olmoffline.models.Entries;
import com.aashdit.olmoffline.models.SHG;
import com.aashdit.olmoffline.models.Schemes;
import com.aashdit.olmoffline.models.VillageForNF;
import com.aashdit.olmoffline.ui.MainActivity;
import com.aashdit.olmoffline.ui.activities.NFHHRegdActivity;
import com.aashdit.olmoffline.ui.activities.NFSHGRegdActivity;
import com.aashdit.olmoffline.ui.activities.NFViewMainActivity;
import com.aashdit.olmoffline.ui.activities.NTFPHHRegdActivity;
import com.aashdit.olmoffline.ui.activities.NTFPSHGRegdActivity;
import com.aashdit.olmoffline.ui.activities.NTFTViewMainActivity;
import com.aashdit.olmoffline.ui.activities.NtfpTasarHHActivity;
import com.aashdit.olmoffline.ui.activities.NtfpTasarShgActivity;
import com.aashdit.olmoffline.ui.activities.NtfpTaserUpdateActivity;
import com.aashdit.olmoffline.ui.activities.SettingsActivity;
import com.aashdit.olmoffline.utils.Constant;
import com.aashdit.olmoffline.utils.SharedPrefManager;
import com.aashdit.olmoffline.utils.Utility;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import okhttp3.OkHttpClient;

import static android.app.Activity.RESULT_OK;

//import com.aashdit.olmmis.adapters.ShgActivityListAdapter;

public class NonFarmActivityFragment extends Fragment implements UdyogShgActivityAdapter.UdyogActivityDetailsListener /*ShgActivityListAdapter.ActivityDetailsListener,*/
        /* ShgHouseHoldListAdapter.ActivityHHDetailsListener, ClfListAdapter.ActivityCLFDetailsListener*/ {

    private static final String TAG = "NonFarmActivityFragment";
    private static int AUTO_REFRESH_REQ_CODE = 100;
    String monthNo, yearNo;
    Drawable droid;
    Display display;
    private ImageView mIvAddShgActivity, mIvSearch,/* mIvAddHhActivity,*/
            mIvAddClfActivity;
    //    private RecyclerView mRecyclerView, mRvHhList, mRvClfList;
    private SharedPrefManager sp;
    private String token;
    private ArrayList<Schemes> schemesArrayList;
    private ArrayList<Activity> activityArrayList;
    private ArrayList<ActivityType> activityTypeArrayList;
    private ArrayList<Entries> entriesArrayList;
    private ArrayList<Entries> entriesHhList;
    private ArrayList<Entries> entriesPgList;
    private ArrayList<Entries> entriesEgList;
    private ArrayList<SHG> shgArrayList;
    private ArrayList<VillageForNF> villageForNF = new ArrayList<>();
    private SchemeSpnAdapter schemeSpnAdapter;
    private ActivitySpnAdapter activitySpnAdapter;
    private ShgListForHHSpnAdapter shgSpnAdapter;
//    private ShgSpnAdapter shgSpnAdapter;
    private Spinner mSpnSchemes, mSpnActivity/*, mSpnShgList*/;
    private Long selectedScheme = 0L;
    private Long selectedActivity = 0L;
    private Long selectedShg = 0L;
    private Long selectedActivityType = 0L;
    private Long selectedVillageId = 0L;
    private TextView mTvTotal/*, mTvTotalHH, mTvTotalClf*/;
    private EditText mEtSearchTearm;
    private UdyogShgActivityAdapter udyogShgActivityAdapter;
    private ActivityTypeSpnAdapter activityTypeSpnAdapter;
    private VillageNfSpnAdapter villageNfSpnAdapter;
    //    private ShgHouseHoldListAdapter houseHoldListAdapter;
//    private ClfListAdapter clfListAdapter;
    private ProgressBar progressBar;
    private boolean shouldShow = false;
    private TextView mTvHousehold;
    private TextView mTvShg;
    private ImageView mIvSettings;
    private SwipeRefreshLayout swipeLayout;
    private RelativeLayout mRlragBg;
    private Toolbar mToolbarActivityFragment;
    private FirebaseAnalytics mFirebaseAnalytics;
    private String searchTerm = "";
    private String reportingLevelCode = "PG";
    private String searchUrl = "";
    private String shgName = "";
    private FragmentNonFarmActivityBinding binding;

    /**
     * offline
     * */

    private Realm realm;
    private RealmResults<Schemes> schemesRealmResults;
    private RealmList<ActivityType> activityTypeRealmResults;
    private RealmResults<ShgList> shgList;
    private List<HHSHGList> hhshgLists;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentNonFarmActivityBinding.inflate(inflater);
        realm = Realm.getDefaultInstance();
        return binding.getRoot(); /*inflater.inflate(R.layout.fragment_activity, container, false);*/
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(Objects.requireNonNull(getActivity()));

        mRlragBg = view.findViewById(R.id.rl_ac_fragment_bg);
        mToolbarActivityFragment = view.findViewById(R.id.toolbar_activity);
//        int nightModeFlags = Objects.requireNonNull(getContext()).getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
//        switch (nightModeFlags) {
//            case Configuration.UI_MODE_NIGHT_YES:
//                mRlragBg.setBackgroundColor(getResources().getColor(R.color.background_dark));
//                mToolbarActivityFragment.setBackgroundColor(getResources().getColor(R.color.d_toolbar));
//                break;
//
//            case Configuration.UI_MODE_NIGHT_NO:
//                mRlragBg.setBackgroundColor(getResources().getColor(R.color.background));
//                mToolbarActivityFragment.setBackgroundColor(getResources().getColor(R.color.white));
//                break;
//
//            case Configuration.UI_MODE_NIGHT_UNDEFINED:
//
//                break;
//        }

        sp = SharedPrefManager.getInstance(getActivity());
        token = sp.getStringData(Constant.APP_TOKEN);
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.MINUTES)
                .readTimeout(10, TimeUnit.MINUTES)
                .writeTimeout(10, TimeUnit.MINUTES)
                .build();
        AndroidNetworking.initialize(getActivity(), okHttpClient);
        if (realm != null) {
            schemesRealmResults = realm.where(Schemes.class).findAll();
        }
        Long date = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date resultdate = new Date(date);
        System.out.println(sdf.format(resultdate));

        String s = sdf.format(resultdate);
        String[] parts = s.split("/"); //returns an array with the 2 parts
        String firstPart = parts[0]; //14.015
        monthNo = parts[1];
        yearNo = parts[2];
        sp.setStringData("YEAR", yearNo);

        if (sp.getStringData(Constant.USER_ROLE).equals("ROLE_UMITRA")
                || sp.getStringData(Constant.USER_ROLE).equals("ROLE_CRPEP")) {
            binding.spnActType.setVisibility(View.VISIBLE);
        } else {
            binding.spnActType.setVisibility(View.GONE);
        }

        if (sp.getStringData(Constant.USER_ROLE).equals("ROLE_CRPEP")) {
            binding.tvPg.setVisibility(View.GONE);
            binding.tvEg.setVisibility(View.VISIBLE);
            binding.tvShg.setVisibility(View.VISIBLE);
            binding.tvHousehold.setVisibility(View.VISIBLE);


            reportingLevelCode = "EG";
            binding.tvEg.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ic_selected));
            binding.tvEg.setTextColor(Color.parseColor("#DD084B"));
        } else {
            binding.tvPg.setVisibility(View.VISIBLE);
            binding.tvEg.setVisibility(View.GONE);
            binding.tvShg.setVisibility(View.GONE);
            binding.tvHousehold.setVisibility(View.GONE);

            reportingLevelCode = "PG";
            binding.tvPg.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ic_selected));
            binding.tvPg.setTextColor(Color.parseColor("#DD084B"));
        }

        binding.tvShgNoItems.setVisibility(View.GONE);
        binding.cvSpnShg.setVisibility(View.GONE);

        swipeLayout = view.findViewById(R.id.swiperefresh);
        mIvSettings = view.findViewById(R.id.iv_setting);
        mSpnSchemes = view.findViewById(R.id.spn_scheme);
        mSpnActivity = view.findViewById(R.id.spn_activity);
        mIvSearch = view.findViewById(R.id.iv_search);
        progressBar = view.findViewById(R.id.progress);
        progressBar.setVisibility(View.GONE);

        mTvShg = view.findViewById(R.id.tv_shg);
        mTvHousehold = view.findViewById(R.id.tv_household);
//        binding.tvPg.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ic_selected));
//        binding.tvPg.setTextColor(Color.parseColor("#DD084B"));
        binding.rlShgContainer.setVisibility(View.VISIBLE);

        VillageForNF village = new VillageForNF();
        village.entityId = 0L;
        village.entityName = "Select Village";

        villageForNF.add(village);
        villageNfSpnAdapter = new VillageNfSpnAdapter(getActivity(), villageForNF);
        binding.spnSelectVillage.setAdapter(villageNfSpnAdapter);
        binding.spnSelectVillage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
                    shgSpnAdapter.notifyDataSetChanged();
                } else {
                    selectedVillageId = villageForNF.get(position).entityId;
                    shgArrayList.clear();
                    SHG shg = new SHG();
                    shg.setShgName("Select SHG");
                    shg.setShgDetailsId(0L);
                    shg.setShgRegNumber("SHG");
                    shg.setShgType(0L);
                    shgArrayList.add(shg);
                    shgSpnAdapter.notifyDataSetChanged();
//                    getShgList();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        entriesArrayList = new ArrayList<>();
        entriesHhList = new ArrayList<>();
        entriesPgList = new ArrayList<>();
        entriesEgList = new ArrayList<>();

        udyogShgActivityAdapter = new UdyogShgActivityAdapter(getActivity(), entriesArrayList);
        udyogShgActivityAdapter.setActivityDetailsListener(this);
        binding.tvShg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reportingLevelCode = "SHG";
                binding.cvSpnShg.setVisibility(View.GONE);
//                getSchemes();
                binding.tvShg.setBackground(ContextCompat.getDrawable(Objects.requireNonNull(getActivity()), R.drawable.ic_selected));
                binding.tvShg.setTextColor(Color.parseColor("#DD084B"));
                binding.tvPg.setBackgroundResource(0);
                binding.tvPg.setTextColor(Color.parseColor("#000000"));
                binding.tvEg.setBackgroundResource(0);
                binding.tvEg.setTextColor(Color.parseColor("#000000"));
                mTvHousehold.setBackgroundResource(0);
                mTvHousehold.setTextColor(Color.parseColor("#000000"));

                entriesArrayList.clear();
                udyogShgActivityAdapter.notifyDataSetChanged();
                if (selectedScheme.equals(0L)) {
                    Toast.makeText(getActivity(), "Please select a scheme", Toast.LENGTH_SHORT).show();
                } else if (selectedActivityType.equals(0L)) {
                    Toast.makeText(getActivity(), "Please select an activity type", Toast.LENGTH_SHORT).show();
                } else if (selectedActivity.equals(0L)) {
                    Toast.makeText(getActivity(), "Please select an activity", Toast.LENGTH_SHORT).show();
                } else {
//                    loadList();
                }
            }
        });

        binding.tvEg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reportingLevelCode = "EG";
                binding.cvSpnShg.setVisibility(View.GONE);
                mTvShg.setBackgroundResource(0);
                mTvShg.setTextColor(Color.parseColor("#000000"));
                binding.tvPg.setBackgroundResource(0);
                binding.tvPg.setTextColor(Color.parseColor("#000000"));
                binding.tvEg.setBackground(ContextCompat.getDrawable(Objects.requireNonNull(getActivity()), R.drawable.ic_selected));
                binding.tvEg.setTextColor(Color.parseColor("#DD084B"));
                mTvHousehold.setBackgroundResource(0);
                mTvHousehold.setTextColor(Color.parseColor("#000000"));

                entriesArrayList.clear();
                udyogShgActivityAdapter.notifyDataSetChanged();
                if (selectedScheme.equals(0L)) {
                    Toast.makeText(getActivity(), "Please select a scheme", Toast.LENGTH_SHORT).show();
                } else if (selectedActivityType.equals(0L)) {
                    Toast.makeText(getActivity(), "Please select an activity type", Toast.LENGTH_SHORT).show();
                } else if (selectedActivity.equals(0L)) {
                    Toast.makeText(getActivity(), "Please select an activity", Toast.LENGTH_SHORT).show();
                } else {
//                    loadList();
                }
            }
        });

        binding.tvPg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reportingLevelCode = "PG";
                binding.cvSpnShg.setVisibility(View.GONE);
                mTvShg.setBackgroundResource(0);
                mTvShg.setTextColor(Color.parseColor("#000000"));
                binding.tvEg.setBackgroundResource(0);
                binding.tvEg.setTextColor(Color.parseColor("#000000"));
                binding.tvPg.setBackground(ContextCompat.getDrawable(Objects.requireNonNull(getActivity()), R.drawable.ic_selected));
                binding.tvPg.setTextColor(Color.parseColor("#DD084B"));
                mTvHousehold.setBackgroundResource(0);
                mTvHousehold.setTextColor(Color.parseColor("#000000"));
//                mRlShgContainer.setVisibility(View.GONE);
//                mRlHouseHoldContainer.setVisibility(View.GONE);
//                mRlClfContainer.setVisibility(View.VISIBLE);
//                mRvClfList.setVisibility(View.VISIBLE);
//                mRecyclerView.setVisibility(View.GONE);
//                mRvHhList.setVisibility(View.GONE);
                entriesArrayList.clear();
                udyogShgActivityAdapter.notifyDataSetChanged();
                if (selectedScheme.equals(0L)) {
                    Toast.makeText(getActivity(), "Please select a scheme", Toast.LENGTH_SHORT).show();
                } else if (selectedActivityType.equals(0L)) {
                    Toast.makeText(getActivity(), "Please select an activity type", Toast.LENGTH_SHORT).show();
                } else if (selectedActivity.equals(0L)) {
                    Toast.makeText(getActivity(), "Please select an activity", Toast.LENGTH_SHORT).show();
                } else {
//                    loadList();
                }
            }
        });

        binding.tvHousehold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reportingLevelCode = "HH";
                binding.cvSpnShg.setVisibility(View.VISIBLE);
                mTvShg.setBackgroundResource(0);
                mTvShg.setTextColor(Color.parseColor("#000000"));
                binding.tvPg.setBackgroundResource(0);
                binding.tvPg.setTextColor(Color.parseColor("#000000"));
                binding.tvEg.setBackgroundResource(0);
                binding.tvEg.setTextColor(Color.parseColor("#000000"));
                mTvHousehold.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ic_selected));
                mTvHousehold.setTextColor(Color.parseColor("#DD084B"));

                mTvTotal.setText(getResources().getString(R.string.total).concat(getResources().getString(R.string.Rs).concat(" ").concat("0.0")));

                binding.spnSelectVillage.setSelection(0);
                binding.spnSelectShg.setSelection(0);
                selectedVillageId = 0L;
                selectedShg = 0L;
//                getShgList();

                entriesArrayList.clear();
                udyogShgActivityAdapter.notifyDataSetChanged();
            }
        });
        getVilageList();
        mIvSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent settings = new Intent(getActivity(), SettingsActivity.class);
                startActivity(settings);
            }
        });

        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (selectedScheme.equals(0L)) {
                    Toast.makeText(getActivity(), "Please select a scheme", Toast.LENGTH_SHORT).show();
                    swipeLayout.setRefreshing(false);
                } else if (sp.getStringData(Constant.USER_ROLE).equals("ROLE_UMITRA") && selectedActivityType.equals(0L)) {
                    Toast.makeText(getActivity(), "Please select an activity type", Toast.LENGTH_SHORT).show();
                    swipeLayout.setRefreshing(false);
                } else if (selectedActivity.equals(0L)) {
                    Toast.makeText(getActivity(), "Please select an activity", Toast.LENGTH_SHORT).show();
                    swipeLayout.setRefreshing(false);
                } else {
//                    loadList();
                    swipeLayout.setRefreshing(false);
                }
            }
        });


        ActivityType activityType = new ActivityType();
        activityType.setActivityCode("ActivityType");
        activityType.setActivityId(0L);
        activityType.setActivityTypeDesc("Select ActivityType");
        activityType.setActive(true);

        activityTypeArrayList = new ArrayList<>();
        activityTypeArrayList.add(activityType);

        activityTypeSpnAdapter = new ActivityTypeSpnAdapter(getActivity(), activityTypeArrayList);
        binding.spnActType.setAdapter(activityTypeSpnAdapter);

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

        SHG shg = new SHG();
        shg.setShgDetailsId(0L);
        shg.setShgName("Select SHG");
        shg.setShgRegNumber("0");

        shgArrayList = new ArrayList<>();
        shgArrayList.add(shg);

        activityArrayList = new ArrayList<>();
        activityArrayList.add(activity);


        schemeSpnAdapter = new SchemeSpnAdapter(getActivity(), schemesArrayList);
        mSpnSchemes.setAdapter(schemeSpnAdapter);

        activitySpnAdapter = new ActivitySpnAdapter(getActivity(), activityArrayList);
        mSpnActivity.setAdapter(activitySpnAdapter);

        shgSpnAdapter = new ShgListForHHSpnAdapter(getActivity(), new ArrayList<>());
        binding.spnSelectShg.setAdapter(shgSpnAdapter);
        binding.spnSelectShg.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                selectedShg = shgArrayList.get(position).getShgDetailsId();
                shgName = shgArrayList.get(position).getShgName();

                entriesArrayList.clear();
                udyogShgActivityAdapter.notifyDataSetChanged();
//                loadList();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mSpnSchemes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    selectedScheme = 0L;
                    selectedActivity = 0L;
                    selectedActivityType = 0L;
                    Activity activity = new Activity();
                    activity.setActivityCode("ACTIVITY");
                    activity.setActivityId(0L);
                    activity.setActivityNameEn("Select Activity");
                    activity.setActivityNameHi("Select Activity");

                    activityArrayList = new ArrayList<>();
                    activityArrayList.clear();
                    activityArrayList.add(activity);
                    activitySpnAdapter = new ActivitySpnAdapter(getActivity(), activityArrayList);
                    mSpnActivity.setAdapter(activitySpnAdapter);


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

                    mTvTotal.setText(getResources().getString(R.string.total) + getResources().getString(R.string.Rs));
                } else {
                    selectedScheme = schemesArrayList.get(position).getSchemeId();

                    selectedActivity = 0L;
                    selectedActivityType = 0L;

                    Activity activity = new Activity();
                    activity.setActivityCode("ACTIVITY");
                    activity.setActivityId(0L);
                    activity.setActivityNameEn("Select Activity");
                    activity.setActivityNameHi("Select Activity");

                    activityArrayList = new ArrayList<>();
                    activityArrayList.clear();
                    activityArrayList.add(activity);
                    activitySpnAdapter = new ActivitySpnAdapter(Objects.requireNonNull(getActivity()), activityArrayList);
                    mSpnActivity.setAdapter(activitySpnAdapter);


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


                    entriesArrayList.clear();
                    if (udyogShgActivityAdapter != null) {
                        udyogShgActivityAdapter.notifyDataSetChanged();
                    }
                    if (sp.getStringData(Constant.USER_ROLE).equals("ROLE_PMITRA")) {
//                        getActivityAccordingToScheme(selectedScheme);
                    } else if (sp.getStringData(Constant.USER_ROLE).equals("ROLE_KMITRA") ||
                            sp.getStringData(Constant.USER_ROLE).equals("ROLE_UMITRA") ||
                            sp.getStringData(Constant.USER_ROLE).equals("ROLE_CRPEP")) {
//                        getActivityTypeByScheme();
                    }

                    mTvTotal.setText(getResources().getString(R.string.total) + getResources().getString(R.string.Rs));
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
//                    mSpnActivity.setAdapter(activitySpnAdapter);
                    resetActivityList();
                } else {
                    resetActivityList();

//                    selectedActivity = 0L;
//                    activityArrayList.clear();
//                    Activity activity = new Activity();
//                    activity.setActivityCode("ACTIVITY");
//                    activity.setActivityId(0L);
//                    activity.setActivityNameEn("Select Activity");
//                    activity.setActivityNameHi("Select Activity");
//                    activityArrayList.add(activity);
                    selectedActivityType = activityTypeArrayList.get(position).getActivityId();
                    activitySpnAdapter.notifyDataSetChanged();
//                    getActivityByType(selectedActivityType);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mSpnActivity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    selectedActivity = 0L;
                    entriesArrayList.clear();
                    if (udyogShgActivityAdapter != null) {
                        udyogShgActivityAdapter.notifyDataSetChanged();
                    }
                } else {
                    shouldShow = true;
                    selectedActivity = activityArrayList.get(position).getActivityId();
                    sp.setLongData("ACTIVITY_ID", selectedActivity);
                    if (reportingLevelCode.equals("SHG")) {
//                        loadList();
                    } else if (reportingLevelCode.equals("HH")) {
//                        loadList();
                    } else if (reportingLevelCode.equals("CLF")) {
//                        loadList();
                    } else if (reportingLevelCode.equals("PG")) {
//                        loadList();
                    } else if (reportingLevelCode.equals("EG")) {
//                        loadList();
                    }
                    if (udyogShgActivityAdapter != null) {
                        udyogShgActivityAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mEtSearchTearm = view.findViewById(R.id.et_search_tearm);
        mTvTotal = view.findViewById(R.id.tv_activity_total);

        binding.rvActivityEntityList.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.rvActivityEntityList.setAdapter(udyogShgActivityAdapter);

//        getSchemes();

        binding.ivSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shouldShow = true;
                searchTerm = mEtSearchTearm.getText().toString().trim();
                if (selectedScheme.equals(0L)) {
                    Toast.makeText(getActivity(), "Please select a scheme", Toast.LENGTH_SHORT).show();
                } else if (selectedActivityType.equals(0L)) {
                    Toast.makeText(getActivity(), "Please select an activity type", Toast.LENGTH_SHORT).show();
                } else if (selectedActivity.equals(0L)) {
                    Toast.makeText(getActivity(), "Please select an activity", Toast.LENGTH_SHORT).show();
                } else {
//                    loadList();
                }
            }
        });
        /**
         * intro guide
         * */
        // We load a drawable and create a location to show a tap target here
        // We need the display to get the width and height at this point in time
        display = getActivity().getWindowManager().getDefaultDisplay();
        // Load our little droid guy
        droid = ContextCompat.getDrawable(getActivity(), R.drawable.ic_chevron);
        // Tell our droid buddy where we want him to appear
        final Rect droidTarget = new Rect(0, 0, droid.getIntrinsicWidth() * 2, droid.getIntrinsicHeight() * 2);
        // Using deprecated methods makes you look way cool
        droidTarget.offset(display.getWidth() / 2, display.getHeight() / 2);

        if (!sp.getBoolData(Constant.IS_FIRST_TIME)) {
            introView();
        }
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

        entriesArrayList.clear();
        udyogShgActivityAdapter.notifyDataSetChanged();
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

    private void introView() {
        // Tell our droid buddy where we want him to appear
        final Rect droidTarget = new Rect(0, 0, droid.getIntrinsicWidth() * 2, droid.getIntrinsicHeight() * 2);
        // Using deprecated methods makes you look way cool
        droidTarget.offset(display.getWidth() / 2, display.getHeight() / 2);

        if (sp.getStringData(Constant.USER_ROLE).equals("ROLE_UMITRA")) {
            new TapTargetSequence(getActivity())
                    .targets(
                            TapTarget.forView(binding.tvPg, "Reporting Level")
                                    .dimColor(android.R.color.black)
                                    .outerCircleColor(R.color.color1)
                                    .targetCircleColor(R.color.white)
                                    .textColor(android.R.color.white),
                            TapTarget.forView(binding.ivSearch, "Search")
                                    .dimColor(android.R.color.black)
                                    .outerCircleColor(R.color.color1)
                                    .targetCircleColor(R.color.white)
                                    .textColor(android.R.color.white))
                    .listener(new TapTargetSequence.Listener() {
                        // This listener will tell us when interesting(tm) events happen in regards
                        // to the sequence
                        @Override
                        public void onSequenceFinish() {
                            // Yay
                            sp.setBoolData(Constant.IS_FIRST_TIME, true);
                        }

                        @Override
                        public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
                            // Perform action for the current target
                            sp.setBoolData(Constant.IS_FIRST_TIME, true);
                        }

                        @Override
                        public void onSequenceCanceled(TapTarget lastTarget) {
                            // Boo
                            sp.setBoolData(Constant.IS_FIRST_TIME, true);
                        }
                    }).start();
        } else {
            new TapTargetSequence(getActivity())
                    .targets(
                            TapTarget.forView(binding.tvEg, "Reporting Level")
                                    .dimColor(android.R.color.black)
                                    .outerCircleColor(R.color.color1)
                                    .targetCircleColor(R.color.white)
                                    .textColor(android.R.color.white),
                            TapTarget.forView(binding.ivSearch, "Search")
                                    .dimColor(android.R.color.black)
                                    .outerCircleColor(R.color.color1)
                                    .targetCircleColor(R.color.white)
                                    .textColor(android.R.color.white))
                    .listener(new TapTargetSequence.Listener() {
                        // This listener will tell us when interesting(tm) events happen in regards
                        // to the sequence
                        @Override
                        public void onSequenceFinish() {
                            // Yay
                            sp.setBoolData(Constant.IS_FIRST_TIME, true);
                        }

                        @Override
                        public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
                            // Perform action for the current target
                            sp.setBoolData(Constant.IS_FIRST_TIME, true);
                        }

                        @Override
                        public void onSequenceCanceled(TapTarget lastTarget) {
                            // Boo
                            sp.setBoolData(Constant.IS_FIRST_TIME, true);
                        }
                    }).start();
        }

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
//                                            ActivityType activity1 = ActivityType.parseActivityType(array.optJSONObject(i));
//                                            activityTypeArrayList.add(activity1);
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

    private void getShgList() {
        progressBar.setVisibility(View.VISIBLE);
        JSONObject obj = new JSONObject();
        try {
            obj.put("schemeId", selectedScheme);
            obj.put("activityId", selectedActivity);
            obj.put("villageId", selectedVillageId);
            obj.put("reportingLevelCode", "HH");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "non-farm-activity/search-shg")
//                .addHeaders("Authorization", "Bearer " + token)
//                .addJSONObjectBody(obj)
                .setTag("Schemes")
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        if (Utility.isStringValid(response)) {
                            progressBar.setVisibility(View.GONE);
                            try {
                                JSONObject object = new JSONObject(response);
                                if (object.optBoolean("outcome")) {
                                    JSONArray array = object.optJSONArray("data");
                                    if (array != null && array.length() > 0) {
                                        shgArrayList.clear();
                                        SHG shg = new SHG();
                                        shg.setShgDetailsId(0L);
                                        shg.setShgName("Select SHG");
                                        shg.setShgRegNumber("0");
                                        shgArrayList.add(shg);

                                        for (int i = 0; i < array.length(); i++) {
                                            SHG shg1 = SHG.parseShg(array.optJSONObject(i));
                                            shgArrayList.add(shg1);
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
                        progressBar.setVisibility(View.GONE);
                        Log.e(TAG, "onError: " + anError.getErrorDetail());
                        try {
                            JSONObject errObj = new JSONObject(anError.getErrorBody());
                            int statusCode = errObj.optInt("status");
                            if (statusCode == 500) {
                                sp.clear();
                                Intent intent = new Intent(getActivity(), MainActivity.class);
                                startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                        Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                                getActivity().finish();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private void loadList() {

        if (shouldShow) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
        }

        JSONObject object = new JSONObject();

        switch (reportingLevelCode) {
            case "SHG":
            case "EG":
            case "PG":
            case "CLF":
                try {
                    object.put("searchTerm", searchTerm);
                    object.put("schemeId", selectedScheme);
                    object.put("activityId", selectedActivity);
                    object.put("reportingLevelCode", reportingLevelCode);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case "HH":
                try {
                    object.put("searchTerm", searchTerm);
                    object.put("schemeId", selectedScheme);
                    object.put("activityId", selectedActivity);
                    object.put("entityId", selectedShg);
                    object.put("reportingLevelCode", reportingLevelCode);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
        }
        if (sp.getStringData(Constant.USER_ROLE).equals("ROLE_UMITRA")
                || sp.getStringData(Constant.USER_ROLE).equals("ROLE_CRPEP")) {
            searchUrl = "non-farm-activity/search";
            if (selectedActivityType.equals(2L)) {
                searchUrl = "ntfp-tasar/search";
            }
            if (selectedActivityType.equals(13L)) {
                searchUrl = "ntfp/search";
            }
            binding.spnActType.setVisibility(View.VISIBLE);
        } else {
            binding.spnActType.setVisibility(View.GONE);
        }


        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + searchUrl)
                .addHeaders("Authorization", "Bearer " + token)
                .setTag("Activity")
                .addJSONObjectBody(object)
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        progressBar.setVisibility(View.GONE);
                        if (Utility.isStringValid(response)) {
                            try {
                                JSONObject resObject = new JSONObject(response);
                                if (resObject.optJSONObject("data") != null) {
                                    String total = Objects.requireNonNull(resObject.optJSONObject("data")).optString("total");
                                    mTvTotal.setText(getResources().getString(R.string.total).concat(getResources().getString(R.string.Rs).concat(" ").concat(total)));

                                    JSONArray array = Objects.requireNonNull(resObject.optJSONObject("data")).optJSONArray("entries");

                                    if (array != null && array.length() > 0) {

                                        switch (reportingLevelCode) {
                                            case "SHG":
                                            case "EG":
                                            case "PG":
//                                                setUpShgDataAdapter();
                                                entriesArrayList.clear();
                                                for (int i = 0; i < array.length(); i++) {
                                                    Entries e = Entries.parseEntries(array.optJSONObject(i),reportingLevelCode,searchUrl);
                                                    entriesArrayList.add(e);
                                                }
                                                udyogShgActivityAdapter.notifyDataSetChanged();
//                                                houseHoldListAdapter = null;
//                                                clfListAdapter = null;
                                                break;
                                            case "HH":
//                                                setUpHhDataAdapter();
                                                entriesArrayList.clear();
                                                for (int i = 0; i < array.length(); i++) {
                                                    Entries e = Entries.parseEntries(array.optJSONObject(i),reportingLevelCode,searchUrl);
                                                    entriesArrayList.add(e);
                                                }
                                                udyogShgActivityAdapter.notifyDataSetChanged();

                                                break;

                                        }
                                    } else {
                                        Toast.makeText(getActivity(), "No items found.", Toast.LENGTH_SHORT).show();
                                        switch (reportingLevelCode) {
                                            case "SHG":
                                            case "PG":
                                            case "EG":
                                            case "HH":
                                                entriesArrayList.clear();
                                                if (udyogShgActivityAdapter != null) {
                                                    udyogShgActivityAdapter.notifyDataSetChanged();
                                                }
                                                break;
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
                        progressBar.setVisibility(View.GONE);
                        Log.e(TAG, "onError: " + anError.getErrorDetail());
                        try {
                            JSONObject errObj = new JSONObject(anError.getErrorBody());
                            int statusCode = errObj.optInt("status");
                            if (statusCode == 500) {
                                sp.clear();
                                Intent intent = new Intent(getActivity(), MainActivity.class);
                                startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                        Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                                getActivity().finish();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

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
//                                            Activity activity1 = Activity.parseActivity(array.optJSONObject(i));
//                                            activityArrayList.add(activity1);
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
                                Intent intent = new Intent(getActivity(), MainActivity.class);
                                startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                        Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                                getActivity().finish();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
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
//                                            Activity activity1 = Activity.parseActivity(array.optJSONObject(i));
//                                            activityArrayList.add(activity1);
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
                        try {
                            JSONObject errObj = new JSONObject(anError.getErrorBody());
                            int statusCode = errObj.optInt("status");
                            if (statusCode == 500) {
                                sp.clear();
                                Intent intent = new Intent(getActivity(), MainActivity.class);
                                startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                        Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                                getActivity().finish();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
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
                                            Schemes scheme = Schemes.parseSchemes(array.optJSONObject(i),"");
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
                        try {
                            JSONObject errObj = new JSONObject(anError.getErrorBody());
                            int statusCode = errObj.optInt("status");
                            if (statusCode == 500) {
                                sp.clear();
                                Intent intent = new Intent(getActivity(), MainActivity.class);
                                startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                        Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                                getActivity().finish();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 10 && resultCode == RESULT_OK && data != null) {
            if (data.getBooleanExtra("res", false)) {
                selectedScheme = data.getLongExtra("schemeId", 0L);
                selectedActivity = data.getLongExtra("activityId", 0L);
//                loadList();
            }
        } else if (requestCode == AUTO_REFRESH_REQ_CODE) {
//            loadList();
        }

    }

    /**
     * navigate type 0 = SHG
     * navigate type 1 = HH
     * navigate type 2 = CLF
     */

    @Override
    public void onUdyogActivityClick(int position) {
        if (reportingLevelCode.equals("SHG")) {
            if (selectedActivityType.equals(2L)/**Tasar*/) {
                Intent intent = new Intent(getActivity(), NTFTViewMainActivity.class);

                intent.putExtra("ENTITY_TYPE_CODE", reportingLevelCode);
                intent.putExtra("SCHEME_ID", selectedScheme);
                intent.putExtra("ACTIVITY_ID", selectedActivity);
                intent.putExtra("ENTITY_ID", entriesArrayList.get(position).getOrgId());
                intent.putExtra("ENTITY_NAME", entriesArrayList.get(position).getOrgName());
                intent.putExtra("MONTH", monthNo);
                intent.putExtra("YEAR", yearNo);
                intent.putExtra("REQ_ACT", 0);
                intent.putExtra(Constant.INTENT_TYPE, Constant.UPDATE);

                startActivityForResult(intent, AUTO_REFRESH_REQ_CODE);
            } else if (selectedActivityType.equals(13L)/**Medicinal Plant*/) {
                Intent intent = new Intent(getActivity(), NTFTViewMainActivity.class);

                intent.putExtra("ENTITY_TYPE_CODE", reportingLevelCode);
                intent.putExtra("SCHEME_ID", selectedScheme);
                intent.putExtra("ACTIVITY_ID", selectedActivity);
                intent.putExtra("ENTITY_ID", entriesArrayList.get(position).getOrgId());
                intent.putExtra("ENTITY_NAME", entriesArrayList.get(position).getOrgName());
                intent.putExtra("MONTH", monthNo);
                intent.putExtra("YEAR", yearNo);
                intent.putExtra("REQ_ACT", 1);
                intent.putExtra(Constant.INTENT_TYPE, Constant.UPDATE);

                startActivityForResult(intent, AUTO_REFRESH_REQ_CODE);
            } else {
                Intent intent = new Intent(getActivity(), NFViewMainActivity.class);

                intent.putExtra("ENTITY_TYPE_CODE", reportingLevelCode);
                intent.putExtra("SCHEME_ID", selectedScheme);
                intent.putExtra("ACTIVITY_ID", selectedActivity);
                intent.putExtra("ENTITY_ID", entriesArrayList.get(position).getOrgId());
                intent.putExtra("ENTITY_NAME", entriesArrayList.get(position).getOrgName());
                intent.putExtra("MONTH", monthNo);
                intent.putExtra("YEAR", yearNo);
                intent.putExtra(Constant.INTENT_TYPE, Constant.UPDATE);

                startActivityForResult(intent, AUTO_REFRESH_REQ_CODE);
            }
        } else if (reportingLevelCode.equals("HH")) {
            if (selectedActivityType.equals(2L)) {
                Intent intent = new Intent(getActivity(), NTFTViewMainActivity.class);

                intent.putExtra("ENTITY_TYPE_CODE", reportingLevelCode);
                intent.putExtra("SCHEME_ID", selectedScheme);
                intent.putExtra("ACTIVITY_ID", selectedActivity);
                intent.putExtra("ENTITY_ID", entriesArrayList.get(position).getOrgId());
                intent.putExtra("ENTITY_NAME", entriesArrayList.get(position).getOrgName());
                intent.putExtra("MONTH", monthNo);
                intent.putExtra("YEAR", yearNo);
                intent.putExtra("REQ_ACT", 0);
                intent.putExtra(Constant.INTENT_TYPE, Constant.UPDATE);

                startActivityForResult(intent, AUTO_REFRESH_REQ_CODE);
            } else if (selectedActivityType.equals(13L)) {
                Intent intent = new Intent(getActivity(), NTFTViewMainActivity.class);

                intent.putExtra("ENTITY_TYPE_CODE", reportingLevelCode);
                intent.putExtra("SCHEME_ID", selectedScheme);
                intent.putExtra("ACTIVITY_ID", selectedActivity);
                intent.putExtra("ENTITY_ID", entriesArrayList.get(position).getOrgId());
                intent.putExtra("ENTITY_NAME", entriesArrayList.get(position).getOrgName());
                intent.putExtra("MONTH", monthNo);
                intent.putExtra("YEAR", yearNo);
                intent.putExtra("REQ_ACT", 1);
                intent.putExtra(Constant.INTENT_TYPE, Constant.UPDATE);

                startActivityForResult(intent, AUTO_REFRESH_REQ_CODE);
            } else {
                Intent intent = new Intent(getActivity(), NFViewMainActivity.class);

                intent.putExtra("ENTITY_TYPE_CODE", reportingLevelCode);
                intent.putExtra("SCHEME_ID", selectedScheme);
                intent.putExtra("ACTIVITY_ID", selectedActivity);
                intent.putExtra("ENTITY_ID", entriesArrayList.get(position).getOrgId());
                intent.putExtra("ENTITY_NAME", entriesArrayList.get(position).getOrgName());
                intent.putExtra("MONTH", monthNo);
                intent.putExtra("YEAR", yearNo);
                intent.putExtra(Constant.INTENT_TYPE, Constant.UPDATE);

                startActivityForResult(intent, AUTO_REFRESH_REQ_CODE);
            }
        } else if (reportingLevelCode.equals("PG")) {
            if (selectedActivityType.equals(2L)/**Tasar*/) {
                Intent intent = new Intent(getActivity(), NtfpTaserUpdateActivity.class);

                intent.putExtra("ENTITY_TYPE_CODE", reportingLevelCode);
                intent.putExtra("SCHEME_ID", selectedScheme);
                intent.putExtra("ACTIVITY_ID", selectedActivity);
                intent.putExtra("ENTITY_ID", entriesArrayList.get(position).getOrgId());
                intent.putExtra("ENTITY_NAME", entriesArrayList.get(position).getOrgName());
                intent.putExtra("MONTH", monthNo);
                intent.putExtra("YEAR", yearNo);
                intent.putExtra("REQ_ACT", 0);
                intent.putExtra(Constant.INTENT_TYPE, Constant.UPDATE);

                startActivityForResult(intent, AUTO_REFRESH_REQ_CODE);
            } else if (selectedActivityType.equals(13L)/**Medicinal Plant*/) {
                Intent intent = new Intent(getActivity(), NTFTViewMainActivity.class);

                intent.putExtra("ENTITY_TYPE_CODE", reportingLevelCode);
                intent.putExtra("SCHEME_ID", selectedScheme);
                intent.putExtra("ACTIVITY_ID", selectedActivity);
                intent.putExtra("ENTITY_ID", entriesArrayList.get(position).getOrgId());
                intent.putExtra("ENTITY_NAME", entriesArrayList.get(position).getOrgName());
                intent.putExtra("MONTH", monthNo);
                intent.putExtra("YEAR", yearNo);
                intent.putExtra("REQ_ACT", 1);
                intent.putExtra(Constant.INTENT_TYPE, Constant.UPDATE);

                startActivityForResult(intent, AUTO_REFRESH_REQ_CODE);
            } else {
                Intent intent = new Intent(getActivity(), NFViewMainActivity.class);

                intent.putExtra("ENTITY_TYPE_CODE", reportingLevelCode);
                intent.putExtra("SCHEME_ID", selectedScheme);
                intent.putExtra("ACTIVITY_ID", selectedActivity);
                intent.putExtra("ENTITY_ID", entriesArrayList.get(position).getOrgId());
                intent.putExtra("ENTITY_NAME", entriesArrayList.get(position).getOrgName());
                intent.putExtra("MONTH", monthNo);
                intent.putExtra("YEAR", yearNo);
                intent.putExtra(Constant.INTENT_TYPE, Constant.UPDATE);

                startActivityForResult(intent, AUTO_REFRESH_REQ_CODE);
            }
        } else if (reportingLevelCode.equals("EG")) {
            if (selectedActivityType.equals(2L)) {
                Intent intent = new Intent(getActivity(), NTFTViewMainActivity.class);

                intent.putExtra("ENTITY_TYPE_CODE", reportingLevelCode);
                intent.putExtra("SCHEME_ID", selectedScheme);
                intent.putExtra("ACTIVITY_ID", selectedActivity);
                intent.putExtra("ENTITY_ID", entriesArrayList.get(position).getOrgId());
                intent.putExtra("ENTITY_NAME", entriesArrayList.get(position).getOrgName());
                intent.putExtra("MONTH", monthNo);
                intent.putExtra("YEAR", yearNo);
                intent.putExtra("REQ_ACT", 0);
                intent.putExtra(Constant.INTENT_TYPE, Constant.UPDATE);

                startActivityForResult(intent, AUTO_REFRESH_REQ_CODE);
            } else if (selectedActivityType.equals(13L)) {
                Intent intent = new Intent(getActivity(), NTFTViewMainActivity.class);

                intent.putExtra("ENTITY_TYPE_CODE", reportingLevelCode);
                intent.putExtra("SCHEME_ID", selectedScheme);
                intent.putExtra("ACTIVITY_ID", selectedActivity);
                intent.putExtra("ENTITY_ID", entriesArrayList.get(position).getOrgId());
                intent.putExtra("ENTITY_NAME", entriesArrayList.get(position).getOrgName());
                intent.putExtra("MONTH", monthNo);
                intent.putExtra("YEAR", yearNo);
                intent.putExtra("REQ_ACT", 1);
                intent.putExtra(Constant.INTENT_TYPE, Constant.UPDATE);

                startActivityForResult(intent, AUTO_REFRESH_REQ_CODE);
            } else {
                Intent intent = new Intent(getActivity(), NFViewMainActivity.class);

                intent.putExtra("ENTITY_TYPE_CODE", reportingLevelCode);
                intent.putExtra("SCHEME_ID", selectedScheme);
                intent.putExtra("ACTIVITY_ID", selectedActivity);
                intent.putExtra("ENTITY_ID", entriesArrayList.get(position).getOrgId());
                intent.putExtra("ENTITY_NAME", entriesArrayList.get(position).getOrgName());
                intent.putExtra("MONTH", monthNo);
                intent.putExtra("YEAR", yearNo);
                intent.putExtra(Constant.INTENT_TYPE, Constant.UPDATE);

                startActivityForResult(intent, AUTO_REFRESH_REQ_CODE);
            }
        }
    }

    @Override
    public void onUdyogEditDetails(int position) {
        if (sp.getStringData(Constant.USER_ROLE).equals("ROLE_UMITRA") || sp.getStringData(Constant.USER_ROLE).equals("ROLE_CRPEP")) {
            if (reportingLevelCode.equals("SHG")) {
                if (selectedActivityType.equals(2L)) {
                    Intent intent = new Intent(getActivity(), NtfpTasarShgActivity.class);

                    intent.putExtra("ENTITY_TYPE_CODE", reportingLevelCode);
                    intent.putExtra("SCHEME_ID", selectedScheme);
                    intent.putExtra("ACTIVITY_ID", selectedActivity);
                    intent.putExtra("ENTITY_ID", entriesArrayList.get(position).getOrgId());
                    intent.putExtra("ENTITY_NAME", entriesArrayList.get(position).getOrgName());
                    intent.putExtra(Constant.INTENT_TYPE, Constant.UPDATE);

                    startActivityForResult(intent, AUTO_REFRESH_REQ_CODE);
                } else if (selectedActivityType.equals(13L)) {
                    Intent intent = new Intent(getActivity(), NTFPSHGRegdActivity.class);

                    intent.putExtra("ENTITY_TYPE_CODE", reportingLevelCode);
                    intent.putExtra("SCHEME_ID", selectedScheme);
                    intent.putExtra("ACTIVITY_ID", selectedActivity);
                    intent.putExtra("ENTITY_ID", entriesArrayList.get(position).getOrgId());
                    intent.putExtra("ENTITY_NAME", entriesArrayList.get(position).getOrgName());
                    intent.putExtra(Constant.INTENT_TYPE, Constant.UPDATE);

                    startActivityForResult(intent, AUTO_REFRESH_REQ_CODE);
                } else {
                    Intent intent = new Intent(getActivity(), NFSHGRegdActivity.class);

                    intent.putExtra("ENTITY_TYPE_CODE", reportingLevelCode);
                    intent.putExtra("SCHEME_ID", selectedScheme);
                    intent.putExtra("ACTIVITY_ID", selectedActivity);
                    intent.putExtra("ENTITY_ID", entriesArrayList.get(position).getOrgId());
                    intent.putExtra("ENTITY_NAME", entriesArrayList.get(position).getOrgName());
                    intent.putExtra(Constant.INTENT_TYPE, Constant.UPDATE);

                    startActivityForResult(intent, AUTO_REFRESH_REQ_CODE);
                }
            } else if (reportingLevelCode.equals("HH")) {
                if (selectedActivityType.equals(2L)) {
                    Intent intent = new Intent(getActivity(), NtfpTasarHHActivity.class);

                    intent.putExtra("ENTITY_TYPE_CODE", reportingLevelCode);
                    intent.putExtra("SCHEME_ID", selectedScheme);
                    intent.putExtra("ACTIVITY_ID", selectedActivity);
                    intent.putExtra("ENTITY_ID", entriesArrayList.get(position).getOrgId());
                    intent.putExtra("ENTITY_NAME", entriesArrayList.get(position).getOrgName());
                    intent.putExtra("SHG_NAME", shgName);
                    intent.putExtra(Constant.INTENT_TYPE, Constant.UPDATE);

                    startActivityForResult(intent, AUTO_REFRESH_REQ_CODE);
                } else if (selectedActivityType.equals(13L)) {
                    Intent intent = new Intent(getActivity(), NTFPHHRegdActivity.class);

                    intent.putExtra("ENTITY_TYPE_CODE", reportingLevelCode);
                    intent.putExtra("SCHEME_ID", selectedScheme);
                    intent.putExtra("ACTIVITY_ID", selectedActivity);
                    intent.putExtra("ENTITY_ID", entriesArrayList.get(position).getOrgId());
                    intent.putExtra("ENTITY_NAME", entriesArrayList.get(position).getOrgName());
                    intent.putExtra(Constant.INTENT_TYPE, Constant.UPDATE);

                    startActivityForResult(intent, AUTO_REFRESH_REQ_CODE);
                } else {
                    Intent intent = new Intent(getActivity(), NFHHRegdActivity.class);

                    intent.putExtra("ENTITY_TYPE_CODE", reportingLevelCode);
                    intent.putExtra("SCHEME_ID", selectedScheme);
                    intent.putExtra("ACTIVITY_ID", selectedActivity);
                    intent.putExtra("ENTITY_ID", entriesArrayList.get(position).getOrgId());
                    intent.putExtra("ENTITY_NAME", entriesArrayList.get(position).getOrgName());
                    intent.putExtra("SHG_NAME", shgName);
                    intent.putExtra(Constant.INTENT_TYPE, Constant.UPDATE);

                    startActivityForResult(intent, AUTO_REFRESH_REQ_CODE);
                }
            } else if (reportingLevelCode.equals("PG")) {
                if (selectedActivityType.equals(2L)) {
                    Intent intent = new Intent(getActivity(), NtfpTasarShgActivity.class);

                    intent.putExtra("ENTITY_TYPE_CODE", reportingLevelCode);
                    intent.putExtra("SCHEME_ID", selectedScheme);
                    intent.putExtra("ACTIVITY_ID", selectedActivity);
                    intent.putExtra("ENTITY_ID", entriesArrayList.get(position).getOrgId());
                    intent.putExtra("ENTITY_NAME", entriesArrayList.get(position).getOrgName());
                    intent.putExtra("SHG_NAME", shgName);
                    intent.putExtra(Constant.INTENT_TYPE, Constant.UPDATE);

                    startActivityForResult(intent, AUTO_REFRESH_REQ_CODE);
                } else if (selectedActivityType.equals(13L)) {
                    Intent intent = new Intent(getActivity(), NTFPSHGRegdActivity.class);

                    intent.putExtra("ENTITY_TYPE_CODE", reportingLevelCode);
                    intent.putExtra("SCHEME_ID", selectedScheme);
                    intent.putExtra("ACTIVITY_ID", selectedActivity);
                    intent.putExtra("ENTITY_ID", entriesArrayList.get(position).getOrgId());
                    intent.putExtra("ENTITY_NAME", entriesArrayList.get(position).getOrgName());
                    intent.putExtra(Constant.INTENT_TYPE, Constant.UPDATE);

                    startActivityForResult(intent, AUTO_REFRESH_REQ_CODE);
                } else {
                    Intent intent = new Intent(getActivity(), NFSHGRegdActivity.class);

                    intent.putExtra("ENTITY_TYPE_CODE", reportingLevelCode);
                    intent.putExtra("SCHEME_ID", selectedScheme);
                    intent.putExtra("ACTIVITY_ID", selectedActivity);
                    intent.putExtra("ENTITY_ID", entriesArrayList.get(position).getOrgId());
                    intent.putExtra("ENTITY_NAME", entriesArrayList.get(position).getOrgName());
                    intent.putExtra("SHG_NAME", shgName);
                    intent.putExtra(Constant.INTENT_TYPE, Constant.UPDATE);

                    startActivityForResult(intent, AUTO_REFRESH_REQ_CODE);
                }
            } else if (reportingLevelCode.equals("EG")) {
                if (selectedActivityType.equals(2L)) {
                    Intent intent = new Intent(getActivity(), NtfpTasarShgActivity.class);

                    intent.putExtra("ENTITY_TYPE_CODE", reportingLevelCode);
                    intent.putExtra("SCHEME_ID", selectedScheme);
                    intent.putExtra("ACTIVITY_ID", selectedActivity);
                    intent.putExtra("ENTITY_ID", entriesArrayList.get(position).getOrgId());
                    intent.putExtra("ENTITY_NAME", entriesArrayList.get(position).getOrgName());
                    intent.putExtra("SHG_NAME", shgName);
                    intent.putExtra(Constant.INTENT_TYPE, Constant.UPDATE);

                    startActivityForResult(intent, AUTO_REFRESH_REQ_CODE);
                } else if (selectedActivityType.equals(13L)) {
                    Intent intent = new Intent(getActivity(), NTFPSHGRegdActivity.class);

                    intent.putExtra("ENTITY_TYPE_CODE", reportingLevelCode);
                    intent.putExtra("SCHEME_ID", selectedScheme);
                    intent.putExtra("ACTIVITY_ID", selectedActivity);
                    intent.putExtra("ENTITY_ID", entriesArrayList.get(position).getOrgId());
                    intent.putExtra("ENTITY_NAME", entriesArrayList.get(position).getOrgName());
                    intent.putExtra(Constant.INTENT_TYPE, Constant.UPDATE);

                    startActivityForResult(intent, AUTO_REFRESH_REQ_CODE);
                } else {
                    Intent intent = new Intent(getActivity(), NFSHGRegdActivity.class);

                    intent.putExtra("ENTITY_TYPE_CODE", reportingLevelCode);
                    intent.putExtra("SCHEME_ID", selectedScheme);
                    intent.putExtra("ACTIVITY_ID", selectedActivity);
                    intent.putExtra("ENTITY_ID", entriesArrayList.get(position).getOrgId());
                    intent.putExtra("ENTITY_NAME", entriesArrayList.get(position).getOrgName());
                    intent.putExtra("SHG_NAME", shgName);
                    intent.putExtra(Constant.INTENT_TYPE, Constant.UPDATE);

                    startActivityForResult(intent, AUTO_REFRESH_REQ_CODE);
                }
            }
        }
    }


    RealmList<HHSHGList> hhshgListRealmResults;
    RealmResults<ActivityHHSearch> activityHHSearchRealmResults;
    RealmResults<HHSHGList> realmShgList;
    RealmList<HhList> houseHoldsRealmList;
    private void loadShgListForMembers() {

//        HHSHGList hhshgList = new HHSHGList();
//        hhshgList.orgName = "Select";
//        hhshgList.orgId = 0L;
        activityHHSearchRealmResults = realm.where(ActivityHHSearch.class)
                .equalTo("schemeId", selectedScheme)
                .equalTo("activityId", selectedActivity)
                .findAll();
        for (int i = 0; i < activityHHSearchRealmResults.size(); i++) {
            if (selectedActivity.equals(activityHHSearchRealmResults.get(i).activityId)) {
                hhshgListRealmResults = activityHHSearchRealmResults.get(i).shgList;
            }
        }

        if (hhshgListRealmResults != null && hhshgListRealmResults.size() > 0) {
            hhshgLists = new ArrayList<>(hhshgListRealmResults);
            shgSpnAdapter = null;
            shgSpnAdapter = new ShgListForHHSpnAdapter(getActivity(), hhshgLists);
//            mSpnShgList.setAdapter(shgSpnAdapter);
            shgSpnAdapter.notifyDataSetChanged();


        }

    }

    private void getMembersOfOrg() {

        realmShgList = realm.where(HHSHGList.class)
                .equalTo("orgId", selectedShg)
                .findAll();
//        houseHoldsRealmList = realmShgList.

        for (int i = 0; i < realmShgList.size(); i++) {
            if (selectedShg.equals(realmShgList.get(i).orgId)) {
                houseHoldsRealmList = realmShgList.get(i).houseHold;
                setupHouseHoldData();
                break;
            }
        }

    }

    private void setupHouseHoldData() {
        /*if (houseHoldsRealmList != null && houseHoldsRealmList.size() > 0) {
            mRvHhList.setVisibility(View.VISIBLE);
            hhListSpnAdapter = new ShgHouseHoldRealmListAdapter(getContext(), houseHoldsRealmList);
            hhListSpnAdapter.setActivityOfflineHhDetailsListener(this);
            mRvHhList.setAdapter(hhListSpnAdapter);
            hhListSpnAdapter.notifyDataSetChanged();
        }else {
            mRvHhList.setVisibility(View.GONE);
        }*/
    }

    private void loadShgListOffline() {
        shgList = realm.where(ShgList.class)
                .equalTo("schemeId", selectedScheme)
                .equalTo("activityTypeId", selectedActivityType)
                .equalTo("activityId", selectedActivity)
                .findAll();

//        if (shgList != null && shgList.size() > 0) {
//            mRecyclerView.setVisibility(View.VISIBLE);
//            offShgActivityListAdapter = new OffShgActivityListAdapter(getContext(), shgList);
//            offShgActivityListAdapter.setActivityDetailsListener(this);
//            offShgActivityListAdapter.notifyDataSetChanged();
//            mRecyclerView.setAdapter(offShgActivityListAdapter);
//        } else {
//            mRecyclerView.setVisibility(View.GONE);
//        }


    }
}
