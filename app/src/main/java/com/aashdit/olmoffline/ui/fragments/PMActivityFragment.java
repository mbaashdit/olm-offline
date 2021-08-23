package com.aashdit.olmoffline.ui.fragments;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
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
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.aashdit.olmoffline.BuildConfig;
import com.aashdit.olmoffline.R;
import com.aashdit.olmoffline.adapters.ActivitySpnAdapter;
import com.aashdit.olmoffline.adapters.ActivityTypeSpnAdapter;
import com.aashdit.olmoffline.adapters.ClfListAdapter;
import com.aashdit.olmoffline.adapters.OffShgActivityListAdapter;
import com.aashdit.olmoffline.adapters.SchemeSpnAdapter;
import com.aashdit.olmoffline.adapters.ShgActivityListAdapter;
//import com.aashdit.olmoffline.adapters.ShgHouseHoldListAdapter;
import com.aashdit.olmoffline.adapters.ShgHouseHoldRealmListAdapter;
import com.aashdit.olmoffline.adapters.ShgListForHHSpnAdapter;
import com.aashdit.olmoffline.databinding.FragmentActivityBinding;
import com.aashdit.olmoffline.db.hh.ActivityHHSearch;
import com.aashdit.olmoffline.db.hh.HHSHGList;
import com.aashdit.olmoffline.db.hh.HhList;
import com.aashdit.olmoffline.db.hh.SchemeHHSearch;
import com.aashdit.olmoffline.db.shg.ActivitySHGSearch;
import com.aashdit.olmoffline.db.shg.SchemeSHGSearch;
import com.aashdit.olmoffline.db.shg.ShgList;
import com.aashdit.olmoffline.models.Activity;
import com.aashdit.olmoffline.models.ActivityType;
import com.aashdit.olmoffline.models.Entries;
import com.aashdit.olmoffline.models.SHG;
import com.aashdit.olmoffline.models.Schemes;
import com.aashdit.olmoffline.receiver.ConnectivityChangeReceiver;
import com.aashdit.olmoffline.ui.MainActivity;
import com.aashdit.olmoffline.ui.activities.AddBirdActivity;
import com.aashdit.olmoffline.ui.activities.AddBirdHouseHoldActivity;
import com.aashdit.olmoffline.ui.activities.AddDiaryActivity;
import com.aashdit.olmoffline.ui.activities.AddDiaryHouseHoldActivity;
import com.aashdit.olmoffline.ui.activities.AddFarmingHHActivity;
import com.aashdit.olmoffline.ui.activities.AddFarmingShgActivity;
import com.aashdit.olmoffline.ui.activities.AddFishActivity;
import com.aashdit.olmoffline.ui.activities.AddFishHouseHoldActivity;
import com.aashdit.olmoffline.ui.activities.AddHouseHoldActivity;
import com.aashdit.olmoffline.ui.activities.AddShgActivity;
import com.aashdit.olmoffline.ui.activities.BirdsUpdateActivity;
import com.aashdit.olmoffline.ui.activities.BirdsViewMainActivity;
import com.aashdit.olmoffline.ui.activities.DairyUpdateActivity;
import com.aashdit.olmoffline.ui.activities.DairyViewMainActivity;
import com.aashdit.olmoffline.ui.activities.FarmingViewMainActivity;
import com.aashdit.olmoffline.ui.activities.FishUpdateActivity;
import com.aashdit.olmoffline.ui.activities.FishViewMainActivity;
import com.aashdit.olmoffline.ui.activities.GoatryUpdateOfflineActivity;
import com.aashdit.olmoffline.ui.activities.GoatryViewMainActivity;
import com.aashdit.olmoffline.utils.Constant;
import com.aashdit.olmoffline.utils.SharedPrefManager;
import com.aashdit.olmoffline.utils.Utility;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import okhttp3.OkHttpClient;

import static android.app.Activity.RESULT_OK;

public class PMActivityFragment extends Fragment implements /*ShgActivityListAdapter.ActivityDetailsListener,
        ShgHouseHoldListAdapter.ActivityHHDetailsListener,*/ ClfListAdapter.ActivityCLFDetailsListener,
        ConnectivityChangeReceiver.ConnectivityReceiverListener,
        OffShgActivityListAdapter.ActivityOfflineDetailsListener,
        ShgHouseHoldRealmListAdapter.ActivityOfflineHHDetailsListener {

    private static final String TAG = "ActivityFragment";
    private static int AUTO_REFRESH_REQ_CODE = 100;
    String monthNo, yearNo;
    RealmList<Activity> pmActivitiesOfflines;
    private ImageView mIvAddShgActivity, mIvSearch, mIvAddHhActivity, mIvAddClfActivity;
    private RecyclerView mRecyclerView, mRvHhList, mRvClfList;
    private SharedPrefManager sp;
    private String token;
    private String activityCode, schemeCode;
    //    private ArrayList<PMSchemesOffline> schemesArrayList;
//    private ArrayList<PMActivitiesOffline> activityArrayList;
    private ArrayList<Schemes> schemesArrayList;
    private ArrayList<Activity> activityArrayList;
    private ArrayList<ActivityType> activityTypeArrayList;
    private ArrayList<Entries> entriesArrayList;
    private ArrayList<Entries> entriesHhList;
    private ArrayList<Entries> entriesClfList;
    private ArrayList<HHSHGList> shgArrayList;
    //    private ArrayList<SHG> shgArrayList;
    //    private SchemeOffSpnAdapter schemeSpnAdapter;
    private SchemeSpnAdapter schemeSpnAdapter;
    //    private ActivityOffSpnAdapter activitySpnAdapter;
    private ActivitySpnAdapter activitySpnAdapter;
    private ShgListForHHSpnAdapter shgSpnAdapter;
    //    private ShgSpnAdapter shgSpnAdapter;
    private Spinner mSpnSchemes, mSpnActivity, mSpnShgList;
    private Long selectedScheme = 0L;
    private Long selectedActivity = 0L;
    private Long selectedShg = 0L;
    //    private Long selectedActivityType = 0L;
    private TextView mTvTotal, mTvTotalHH, mTvTotalClf;
    private EditText mEtSearchTearm;
    private ShgActivityListAdapter activityListAdapter;
    private OffShgActivityListAdapter offShgActivityListAdapter;
    private ShgHouseHoldRealmListAdapter hhListSpnAdapter;
    private ActivityTypeSpnAdapter activityTypeSpnAdapter;
//    private ShgHouseHoldListAdapter houseHoldListAdapter;
    private ClfListAdapter clfListAdapter;
    private ProgressBar progressBar;
    private boolean shouldShow = false;
    private TextView mTvCluster;
    private TextView mTvHousehold;
    private TextView mTvShg;
    private ImageView mIvSettings;
    private SwipeRefreshLayout swipeLayout;
    private RelativeLayout mRlragBg;
    private Toolbar mToolbarActivityFragment;
    private RelativeLayout mRlShgContainer, mRlHouseHoldContainer, mRlClfContainer;
    private TextView mTvNoItemShg, mTVNoItemHh, mTvNoItemClf;
    private FirebaseAnalytics mFirebaseAnalytics;
    private String searchTerm = "";
    private String reportingLevelCode = "SHG";
    private String searchUrl = "";
    private String shgName = "";
    private FragmentActivityBinding binding;
    private Realm realm;
    //    private RealmResults<Schemes> schemesRealmResults;
    private String schemeName, activityName;
    private RealmResults<ShgList> shgListRealmResults;
    private RealmList<ShgList> shgListRealmList;
    private Long shgOrgId = 0L;
    /**
     * @param isConnected for checking network connectivity
     */
    private boolean isConnected = false;
    private ConnectivityChangeReceiver mConnectivityChangeReceiver;
    private RealmList<HHSHGList> hhshgLists;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentActivityBinding.inflate(inflater);
        realm = Realm.getDefaultInstance();
        return binding.getRoot(); /*inflater.inflate(R.layout.fragment_activity, container, false);*/
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());

        mConnectivityChangeReceiver = new ConnectivityChangeReceiver();
        mConnectivityChangeReceiver.setConnectivityReceiverListener(this);
        isConnected = mConnectivityChangeReceiver.getConnectionStatus(getContext());
        registerNetworkBroadcast();

        mRlShgContainer = view.findViewById(R.id.rl_shg_container);
        mRlHouseHoldContainer = view.findViewById(R.id.rl_hh_container);
        mRlClfContainer = view.findViewById(R.id.rl_clf_container);

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
        binding.progress.setVisibility(View.GONE);
        sp = SharedPrefManager.getInstance(getActivity());
        token = sp.getStringData(Constant.APP_TOKEN);
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.MINUTES)
                .readTimeout(10, TimeUnit.MINUTES)
                .writeTimeout(10, TimeUnit.MINUTES)
                .build();
        AndroidNetworking.initialize(getActivity(), okHttpClient);

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

        if (sp.getStringData(Constant.USER_ROLE).equals("ROLE_KMITRA")) {
            binding.spnActType.setVisibility(View.VISIBLE);
        } else {
            binding.spnActType.setVisibility(View.GONE);
        }

        mTVNoItemHh = view.findViewById(R.id.tv_no_hh_items);
        mTvNoItemClf = view.findViewById(R.id.tv_no_items);
        mTvNoItemShg = view.findViewById(R.id.tv_shg_no_items);

        mTvNoItemShg.setVisibility(View.GONE);
        mTVNoItemHh.setVisibility(View.GONE);
        mTvNoItemClf.setVisibility(View.GONE);

        swipeLayout = view.findViewById(R.id.swiperefresh);
        mIvSettings = view.findViewById(R.id.iv_setting);
        mSpnSchemes = view.findViewById(R.id.spn_scheme);
        mSpnActivity = view.findViewById(R.id.spn_activity);
        mSpnShgList = view.findViewById(R.id.spn_add_shg_select_shg);
        mIvSearch = view.findViewById(R.id.iv_search);
        progressBar = view.findViewById(R.id.progress);
        progressBar.setVisibility(View.GONE);

        mTvShg = view.findViewById(R.id.tv_shg);
        mTvCluster = view.findViewById(R.id.tv_cluster);
        mTvHousehold = view.findViewById(R.id.tv_household);
        mTvShg.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ic_selected));
        mTvShg.setTextColor(Color.parseColor("#DD084B"));
        mRlShgContainer.setVisibility(View.VISIBLE);
        mRlHouseHoldContainer.setVisibility(View.GONE);
        mRlClfContainer.setVisibility(View.GONE);
        mTvShg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reportingLevelCode = "SHG";
//                getSchemes();
                mTvShg.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ic_selected));
                mTvShg.setTextColor(Color.parseColor("#DD084B"));
                mTvCluster.setBackgroundResource(0);
                mTvCluster.setTextColor(Color.parseColor("#000000"));
                mTvHousehold.setBackgroundResource(0);
                mTvHousehold.setTextColor(Color.parseColor("#000000"));
                mRlShgContainer.setVisibility(View.VISIBLE);
//                mRecyclerView.setAdapter(activityListAdapter);
                mRlHouseHoldContainer.setVisibility(View.GONE);
                mRlClfContainer.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
                mRvHhList.setVisibility(View.GONE);
                mRvClfList.setVisibility(View.GONE);


                if (selectedScheme.equals(0L)) {
                    Toast.makeText(getActivity(), "Please select a scheme", Toast.LENGTH_SHORT).show();
                } else if (selectedActivity.equals(0L)) {
                    Toast.makeText(getActivity(), "Please select an activity", Toast.LENGTH_SHORT).show();
                } else {
//                    loadList();
                }
            }
        });
        mTvCluster.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reportingLevelCode = "CLF";
                mTvShg.setBackgroundResource(0);
                mTvShg.setTextColor(Color.parseColor("#000000"));
                mTvCluster.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ic_selected));
                mTvCluster.setTextColor(Color.parseColor("#DD084B"));
                mTvHousehold.setBackgroundResource(0);
                mTvHousehold.setTextColor(Color.parseColor("#000000"));
                mRlShgContainer.setVisibility(View.GONE);
                mRlHouseHoldContainer.setVisibility(View.GONE);
                mRlClfContainer.setVisibility(View.VISIBLE);
                mRvClfList.setVisibility(View.VISIBLE);
                mRecyclerView.setVisibility(View.GONE);
                mRvHhList.setVisibility(View.GONE);

                if (selectedScheme.equals(0L)) {
                    Toast.makeText(getActivity(), "Please select a scheme", Toast.LENGTH_SHORT).show();
                } else if (selectedActivity.equals(0L)) {
                    Toast.makeText(getActivity(), "Please select an activity", Toast.LENGTH_SHORT).show();
                } else {
//                    loadList();
                }
            }
        });
        mTvHousehold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reportingLevelCode = "HH";
                mTvShg.setBackgroundResource(0);
                mTvShg.setTextColor(Color.parseColor("#000000"));
                mTvCluster.setBackgroundResource(0);
                mTvCluster.setTextColor(Color.parseColor("#000000"));
                mTvHousehold.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ic_selected));
                mTvHousehold.setTextColor(Color.parseColor("#DD084B"));
                mRlShgContainer.setVisibility(View.GONE);
                mRlClfContainer.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.GONE);
                mRvClfList.setVisibility(View.GONE);
                mRvHhList.setVisibility(View.VISIBLE);

                mRlHouseHoldContainer.setVisibility(View.VISIBLE);
                getShgList();
            }
        });


        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (selectedScheme.equals(0L)) {
                    Toast.makeText(getActivity(), "Please select a scheme", Toast.LENGTH_SHORT).show();
                    swipeLayout.setRefreshing(false);
                } /*else if (sp.getStringData(Constant.USER_ROLE).equals("ROLE_KMITRA") && selectedActivityType.equals(0L)) {
                    Toast.makeText(getActivity(), "Please select an activity type", Toast.LENGTH_SHORT).show();
                    swipeLayout.setRefreshing(false);
                }*/ else if (selectedActivity.equals(0L)) {
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

//        PMSchemesOffline pmSchemesOffline = new PMSchemesOffline();
//        pmSchemesOffline.schemeNameEn = "Select Scheme";


        schemesArrayList = new ArrayList<>();
//        schemesArrayList.add(pmSchemesOffline);
        schemesArrayList.add(schemes);


        Activity activity = new Activity();
        activity.setActivityCode("ACTIVITY");
        activity.setActivityId(0L);
        activity.setActivityNameEn("Select Activity");
        activity.setActivityNameHi("Select Activity");

        SHG shg = new SHG();
        shg.setShgDetailsId(0L);
        shg.setShgName("Select a SHG");
        shg.setShgRegNumber("0");

        shgArrayList = new ArrayList<>();
//        shgArrayList.add(shg);

//        PMActivitiesOffline pmActivitiesOffline = new PMActivitiesOffline();
//        pmActivitiesOffline.activityNameEn = "Select Activity";

        activityArrayList = new ArrayList<>();
//        activityArrayList.add(pmActivitiesOffline);
        activityArrayList.add(activity);

        entriesArrayList = new ArrayList<>();
        entriesHhList = new ArrayList<>();
        entriesClfList = new ArrayList<>();

        RealmResults<Schemes> schemesRealmList = realm.where(Schemes.class).findAll();
        RealmResults<Activity> activityRealmResults = realm.where(Activity.class).findAll();
        shgListRealmResults = realm.where(ShgList.class).findAll();


//
//        RealmResults<PMSchemesOffline> schemesRealmList = realm.where(PMSchemesOffline.class).findAll();
//        RealmResults<PMActivitiesOffline> activityRealmResults = realm.where(PMActivitiesOffline.class).findAll();

        if (schemesRealmList != null) {
            schemesArrayList = new ArrayList<>(schemesRealmList);
        }
        schemeSpnAdapter = new SchemeSpnAdapter(getActivity(), schemesArrayList);
        mSpnSchemes.setAdapter(schemeSpnAdapter);
        schemeSpnAdapter.notifyDataSetChanged();

        if (activityRealmResults != null) {
            activityArrayList = new ArrayList<>(activityRealmResults);
        }
        activitySpnAdapter = new ActivitySpnAdapter(getActivity(), activityArrayList);
        mSpnActivity.setAdapter(activitySpnAdapter);

//        shgSpnAdapter = new ShgSpnAdapter(getActivity(), shgArrayList);
//        mSpnShgList.setAdapter(shgSpnAdapter);
        mSpnShgList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                shgOrgId = hhshgLists.get(position).orgId;
                getHouseHoldList();

//                Toast.makeText(getActivity(), hhshgLists.get(position).orgName+"", Toast.LENGTH_SHORT).show();
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
                    schemeName = "SELECT";
                    schemeCode = "SCHEME";
//                    selectedActivityType = 0L;
                    Activity activity = new Activity();
                    activity.setActivityCode("ACTIVITY");
                    activity.setActivityId(0L);
                    activity.setActivityNameEn("Select Activity");
                    activity.setActivityNameHi("Select Activity");

                    activityArrayList = new ArrayList<>();
                    activityArrayList.clear();
//                    activityArrayList.add(activity);
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
                    selectedScheme = schemesArrayList.get(position).schemeId;
                    schemeName = schemesArrayList.get(position).schemeNameEn;
                    schemeCode = schemesArrayList.get(position).schemeCode;
//                    selectedScheme = schemesArrayList.get(position).getSchemeId();

                    selectedActivity = 0L;
//                    selectedActivityType = 0L;

                    Activity activity = new Activity();
                    activity.setActivityCode("ACTIVITY");
                    activity.setActivityId(0L);
                    activity.setActivityNameEn("Select Activity");
                    activity.setActivityNameHi("Select Activity");

                    activityArrayList = new ArrayList<>();
                    activityArrayList.clear();
//                    activityArrayList.add(activity);
                    activitySpnAdapter = new ActivitySpnAdapter(getActivity(), activityArrayList);
                    mSpnActivity.setAdapter(activitySpnAdapter);

                    entriesArrayList.clear();
                    if (activityListAdapter != null) {
                        activityListAdapter.notifyDataSetChanged();
                    }

                    getActivityFromRealmWithSchemeId(selectedScheme);


                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        mSpnActivity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    selectedActivity = 0L;
                    activityName = "SELECT";
                    entriesArrayList.clear();
                    if (activityListAdapter != null) {
                        activityListAdapter.notifyDataSetChanged();
                    }
                    mRecyclerView.setVisibility(View.GONE);
                } else {

                    shouldShow = true;
                    if (pmActivitiesOfflines != null) {
                        selectedActivity = pmActivitiesOfflines.get(position).getActivityId();
                        activityCode = pmActivitiesOfflines.get(position).getActivityCode();
                        activityName = pmActivitiesOfflines.get(position).getActivityNameEn();


                        SchemeSHGSearch schemeSearchesResult = realm.where(SchemeSHGSearch.class)
                                .equalTo("schemeId", selectedScheme)
                                .findFirst();

                        RealmList<ActivitySHGSearch> activityRealmList = schemeSearchesResult.activityRealmList;

                        for (int i = 0; i < activityRealmList.size(); i++) {
                            if (selectedActivity.equals(activityRealmList.get(i).activityId)) {
                                shgListRealmList = activityRealmList.get(i).shgList;
                                loadOfflineListList(shgListRealmList);
                            }
                        }


//                        loadOfflineListList(pmActivitiesOfflines.get(position).shgList);
//                        sp.setLongData("ACTIVITY_ID", selectedActivity);
//                        if (reportingLevelCode.equals("SHG")) {
////                            loadList();
//                        } else if (reportingLevelCode.equals("HH")) {
////                            loadList();
//                        } else if (reportingLevelCode.equals("CLF")) {
////                            loadList();
//                        }
//                        if (activityListAdapter != null) {
//                            activityListAdapter.notifyDataSetChanged();
//                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mEtSearchTearm = view.findViewById(R.id.et_search_tearm);
        mTvTotal = view.findViewById(R.id.tv_activity_total);
        mTvTotalHH = view.findViewById(R.id.tv_activity_hh_total);
        mTvTotalClf = view.findViewById(R.id.tv_activity_clf_total);
        mRecyclerView = view.findViewById(R.id.rv_activity_shg_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mRvHhList = view.findViewById(R.id.rv_activity_hh_list);
        mRvHhList.setLayoutManager(new LinearLayoutManager(getActivity()));

        mRvClfList = view.findViewById(R.id.rv_activity_clf_list);
        mRvClfList.setLayoutManager(new LinearLayoutManager(getActivity()));

//        loadOfflineListList(shgListRealmResults);
        mTvTotal.setVisibility(View.GONE);
        setUpShgDataAdapter();
        setUpHhDataAdapter();
        setUpClfDataAdapter();


        binding.ivSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shouldShow = true;
                searchTerm = mEtSearchTearm.getText().toString().trim();
                if (selectedScheme.equals(0L)) {
                    Toast.makeText(getActivity(), "Please select a scheme", Toast.LENGTH_SHORT).show();
                } else if (selectedActivity.equals(0L)) {
                    Toast.makeText(getActivity(), "Please select an activity", Toast.LENGTH_SHORT).show();
                } else {
//                    loadList();
                }
            }
        });

        binding.ivHhSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shouldShow = true;
                searchTerm = binding.etSearchHhTearm.getText().toString().trim();
                if (selectedScheme.equals(0L)) {
                    Toast.makeText(getActivity(), "Please select a scheme", Toast.LENGTH_SHORT).show();
                } else if (selectedActivity.equals(0L)) {
                    Toast.makeText(getActivity(), "Please select an activity", Toast.LENGTH_SHORT).show();
                } else {
//                    loadList();
                }
            }
        });

        binding.ivSearchClf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shouldShow = true;
                searchTerm = binding.etSearchTearmClf.getText().toString().trim();
                if (selectedScheme.equals(0L)) {
                    Toast.makeText(getActivity(), "Please select a scheme", Toast.LENGTH_SHORT).show();
                } else if (selectedActivity.equals(0L)) {
                    Toast.makeText(getActivity(), "Please select an activity", Toast.LENGTH_SHORT).show();
                } else {
//                    loadList();
                }
            }
        });

    }

    private void loadOfflineListList(RealmList<ShgList> shgListRealmResults) {
        binding.progress.setVisibility(View.GONE);
        mRvHhList.setVisibility(View.GONE);
        if (shgListRealmResults.size() == 0) {
            mRecyclerView.setVisibility(View.GONE);
        } else {
            mRecyclerView.setVisibility(View.VISIBLE);
        }
        if (shgListRealmResults != null && shgListRealmResults.size() > 0) {
            offShgActivityListAdapter = new OffShgActivityListAdapter(getContext(), shgListRealmResults);
            offShgActivityListAdapter.setActivityDetailsListener(this);
            offShgActivityListAdapter.notifyDataSetChanged();
            mRecyclerView.setAdapter(offShgActivityListAdapter);
        }
    }

    private void loadOfflineHHListList(RealmList<HhList> hhListRealmList) {
        binding.progress.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.GONE);
        if (hhListRealmList.size() == 0) {
            mRvHhList.setVisibility(View.GONE);
        } else {
            mRvHhList.setVisibility(View.VISIBLE);
        }
        if (hhListRealmList != null && hhListRealmList.size() > 0) {
            hhListSpnAdapter = new ShgHouseHoldRealmListAdapter(getContext(), hhListRealmList);
            hhListSpnAdapter.setActivityOfflineHhDetailsListener(this);
//            offShgActivityListAdapter.setActivityDetailsListener(this);
            hhListSpnAdapter.notifyDataSetChanged();
            mRvHhList.setAdapter(hhListSpnAdapter);
        }
    }

    private void getActivityFromRealmWithSchemeId(Long selectedScheme) {

        Schemes schemesOfflines = realm.where(Schemes.class)
                .equalTo("schemeId", selectedScheme).findFirst();

        Log.i(TAG, "getActivityFromRealmWithSchemeId: " + schemesOfflines.schemeId);

        if (schemesOfflines != null) {
            pmActivitiesOfflines = schemesOfflines.activityRealmList;
            Log.i(TAG, "getActivityFromRealmWithSchemeId: " + pmActivitiesOfflines.size());


            activityArrayList.clear();
            activityArrayList = new ArrayList<>(pmActivitiesOfflines);
//                    activityArrayList.add(activity);
            activitySpnAdapter = new ActivitySpnAdapter(getActivity(), activityArrayList);
            mSpnActivity.setAdapter(activitySpnAdapter);
            activitySpnAdapter.notifyDataSetChanged();
        }


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

    private void setUpClfDataAdapter() {
        clfListAdapter = new ClfListAdapter(getActivity(), entriesClfList);
        clfListAdapter.setActivityClfDetailsListener(this);
        mRvClfList.setAdapter(clfListAdapter);
    }

    private void setUpHhDataAdapter() {
//        houseHoldListAdapter = new ShgHouseHoldListAdapter(getActivity(), entriesHhList);
//        houseHoldListAdapter.setActivityHhDetailsListener(this);
//        mRvHhList.setAdapter(houseHoldListAdapter);
    }

    private void setUpShgDataAdapter() {
        activityListAdapter = new ShgActivityListAdapter(getActivity(), entriesArrayList);
//        activityListAdapter.setActivityDetailsListener(this);
        mRecyclerView.setAdapter(activityListAdapter);
    }

    private void getShgList() {


        SchemeHHSearch scheme = realm.where(SchemeHHSearch.class)
                .equalTo("schemeId", selectedScheme)
                .findFirst();

        RealmList<ActivityHHSearch> activity = scheme.activityRealmList;

        Log.i(TAG, "getShgList::::: activity :::" + activity.size());

        for (int i = 0; i < activity.size(); i++) {
            if (selectedActivity.equals(activity.get(i).activityId)) {
                if (activity.get(i).shgList.size() != 0) {
                    hhshgLists = activity.get(i).shgList;
                }
            }
        }

        if (hhshgLists != null) {
            shgArrayList = new ArrayList<>(hhshgLists);
            shgSpnAdapter = new ShgListForHHSpnAdapter(getActivity(), shgArrayList);
            mSpnShgList.setAdapter(shgSpnAdapter);

            getHouseHoldList();
        }
    }
    RealmList<HhList> householdsList;
    private void getHouseHoldList() {

        if (shgOrgId != 0) {
            HHSHGList hhshg = realm.where(HHSHGList.class)
                    .equalTo("orgId", shgOrgId)
                    .equalTo("schemeId",selectedScheme)
                    .equalTo("activityId",selectedActivity)
                    .findFirst();

            /*RealmList<HhList>*/ householdsList = hhshg.houseHold;

            loadOfflineHHListList(householdsList);

        }


    }

//    private void loadList() {
//
//        if (shouldShow) {
//            progressBar.setVisibility(View.VISIBLE);
//        } else {
//            progressBar.setVisibility(View.GONE);
//        }
//
//        JSONObject object = new JSONObject();
//
//        switch (reportingLevelCode) {
//            case "SHG":
//                try {
//                    object.put("searchTerm", searchTerm);
//                    object.put("schemeId", selectedScheme);
//                    object.put("activityId", selectedActivity);
//                    object.put("reportingLevelCode", reportingLevelCode);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//                break;
//            case "HH":
//                try {
//                    object.put("searchTerm", searchTerm);
//                    object.put("schemeId", selectedScheme);
//                    object.put("activityId", selectedActivity);
//                    object.put("entityId", selectedShg);
//                    object.put("reportingLevelCode", reportingLevelCode);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//                break;
//            case "CLF":
//                try {
//                    object.put("searchTerm", searchTerm);
//                    object.put("schemeId", selectedScheme);
//                    object.put("activityId", selectedActivity);
//                    object.put("reportingLevelCode", reportingLevelCode);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//                break;
//        }
//        if (selectedActivity.equals(1L)) {
//            searchUrl = "goatry-activity/search";
//        } else if (selectedActivity.equals(2L)) {
//            searchUrl = "poultry-activity/search";
//        } else if (selectedActivity.equals(3L)) {
//            searchUrl = "fishery-activity/search";
//        } else if (selectedActivity.equals(7L)) {
//            searchUrl = "dairy-activity/search";
//        }
//        if (sp.getStringData(Constant.USER_ROLE).equals("ROLE_KMITRA")) {
//            searchUrl = "farm-activity/search";
//            binding.spnActType.setVisibility(View.VISIBLE);
//        } else {
//            binding.spnActType.setVisibility(View.GONE);
//        }
//
//
//        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + searchUrl)
//                .addHeaders("Authorization", "Bearer " + token)
//                .setTag("Activity")
//                .addJSONObjectBody(object)
//                .setPriority(Priority.HIGH)
//                .build()
//                .getAsString(new StringRequestListener() {
//                    @Override
//                    public void onResponse(String response) {
//                        progressBar.setVisibility(View.GONE);
//                        if (Utility.isStringValid(response)) {
//                            try {
//                                JSONObject resObject = new JSONObject(response);
//                                if (resObject.optJSONObject("data") != null) {
//                                    String total = Objects.requireNonNull(resObject.optJSONObject("data")).optString("total");
//                                    switch (reportingLevelCode) {
//                                        case "SHG":
//                                            mTvTotal.setText(getResources().getString(R.string.total).concat(getResources().getString(R.string.Rs).concat(" ").concat(total)));
//                                            break;
//                                        case "HH":
//                                            mTvTotalHH.setText(getResources().getString(R.string.total).concat(getResources().getString(R.string.Rs)).concat(" ").concat(total));
//                                            break;
//                                        case "CLF":
//                                            mTvTotalClf.setText(getResources().getString(R.string.total).concat(getResources().getString(R.string.Rs)).concat(" ").concat(total));
//                                            break;
//                                    }
//                                    JSONArray array = Objects.requireNonNull(resObject.optJSONObject("data")).optJSONArray("entries");
//
//                                    if (array != null && array.length() > 0) {
//
//                                        switch (reportingLevelCode) {
//                                            case "SHG":
//                                                setUpShgDataAdapter();
//                                                entriesArrayList.clear();
//                                                for (int i = 0; i < array.length(); i++) {
//                                                    Entries e = Entries.parseEntries(array.optJSONObject(i), reportingLevelCode, searchUrl);
//                                                    entriesArrayList.add(e);
//                                                }
//
//
////                                                realm.executeTransaction(new Realm.Transaction() {
////                                                    @Override
////                                                    public void execute(Realm realm1) {
////                                                        realm1.insertOrUpdate(entriesArrayList);
////                                                    }
////                                                });
//
//
//                                                activityListAdapter.notifyDataSetChanged();
////                                                houseHoldListAdapter = null;
//                                                clfListAdapter = null;
//                                                break;
//                                            case "HH":
//                                                setUpHhDataAdapter();
//                                                entriesHhList.clear();
//                                                for (int i = 0; i < array.length(); i++) {
//                                                    Entries e = Entries.parseEntries(array.optJSONObject(i), reportingLevelCode, searchUrl);
//                                                    entriesHhList.add(e);
//                                                }
//
//
////                                                realm.executeTransaction(new Realm.Transaction() {
////                                                    @Override
////                                                    public void execute(Realm realm1) {
////                                                        realm1.insertOrUpdate(entriesHhList);
////                                                    }
////                                                });
//
////                                                houseHoldListAdapter.notifyDataSetChanged();
//                                                activityListAdapter = null;
//                                                clfListAdapter = null;
//                                                break;
//                                            case "CLF":
//                                                setUpClfDataAdapter();
//                                                entriesClfList.clear();
//                                                for (int i = 0; i < array.length(); i++) {
//                                                    Entries e = Entries.parseEntries(array.optJSONObject(i), reportingLevelCode, searchUrl);
//                                                    entriesClfList.add(e);
//                                                }
//                                                clfListAdapter.notifyDataSetChanged();
//                                                activityListAdapter = null;
////                                                houseHoldListAdapter = null;
//                                                break;
//                                        }
//                                    } else {
//                                        Toast.makeText(getActivity(), "No items found.", Toast.LENGTH_SHORT).show();
//                                        switch (reportingLevelCode) {
//                                            case "SHG":
//                                                entriesArrayList.clear();
//                                                setUpShgDataAdapter();
//                                                if (activityListAdapter != null) {
//                                                    activityListAdapter.notifyDataSetChanged();
//                                                }
//
//                                                break;
//                                            case "HH":
//                                                entriesHhList.clear();
//                                                setUpHhDataAdapter();
////                                                if (houseHoldListAdapter != null) {
//////                                                    houseHoldListAdapter.notifyDataSetChanged();
////                                                }
//
//                                                break;
//                                            case "CLF":
//                                                entriesClfList.clear();
//                                                setUpClfDataAdapter();
//                                                if (clfListAdapter != null) {
//                                                    clfListAdapter.notifyDataSetChanged();
//                                                }
//
//                                                break;
//                                        }
//                                    }
//                                }
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//
//                        }
//                    }
//
//                    @Override
//                    public void onError(ANError anError) {
//                        progressBar.setVisibility(View.GONE);
//                        Log.e(TAG, "onError: " + anError.getErrorDetail());
//                        try {
//                            JSONObject errObj = new JSONObject(anError.getErrorBody());
//                            int statusCode = errObj.optInt("status");
//                            if (statusCode == 500) {
//                                sp.clear();
//                                Intent intent = new Intent(getActivity(), MainActivity.class);
//                                startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
//                                        Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
//                                getActivity().finish();
//                            }
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                });
//
//    }


//    private void getActivityByType(Long selectedActivityType) {
//
//        AndroidNetworking.get(BuildConfig.BASE_URL + Constant.API_PATH + "lov/activityByType/" + selectedActivityType)
//                .addHeaders("Authorization", "Bearer " + token)
//                .setTag("Activity")
//                .setPriority(Priority.HIGH)
//                .build()
//                .getAsString(new StringRequestListener() {
//                    @Override
//                    public void onResponse(String response) {
//                        if (Utility.isStringValid(response)) {
//                            try {
//                                JSONObject object = new JSONObject(response);
//                                if (object.optBoolean("outcome")) {
//                                    JSONArray array = object.optJSONArray("data");
//                                    if (array != null && array.length() > 0) {
//                                        activityArrayList.clear();
//
//                                        Activity activity = new Activity();
//                                        activity.setActivityCode("ACTIVITY");
//                                        activity.setActivityId(0L);
//                                        activity.setActivityNameEn("Select Activity");
//                                        activity.setActivityNameHi("Select Activity");
//                                        activityArrayList.add(activity);
//                                        for (int i = 0; i < array.length(); i++) {
//                                            Activity activity1 = Activity.parseActivity(array.optJSONObject(i));
//                                            activityArrayList.add(activity1);
//                                        }
//
//                                        activitySpnAdapter.notifyDataSetChanged();
//                                    }
//                                }
//
//
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }
//
//                    @Override
//                    public void onError(ANError anError) {
//                        Log.e(TAG, "onError: " + anError.getErrorDetail());
//                    }
//                });
//    }


//    private void getActivityAccordingToScheme(Long selectedScheme) {
//        AndroidNetworking.get(BuildConfig.BASE_URL + Constant.API_PATH + "lov/activity/" + selectedScheme)
//                .addHeaders("Authorization", "Bearer " + token)
//                .setTag("Activity")
//                .setPriority(Priority.HIGH)
//                .build()
//                .getAsString(new StringRequestListener() {
//                    @Override
//                    public void onResponse(String response) {
//                        if (Utility.isStringValid(response)) {
//                            try {
//                                JSONObject object = new JSONObject(response);
//                                if (object.optBoolean("outcome")) {
//                                    JSONArray array = object.optJSONArray("data");
//                                    if (array != null && array.length() > 0) {
//                                        activityArrayList.clear();
//
//                                        Activity activity = new Activity();
//                                        activity.setActivityCode("ACTIVITY");
//                                        activity.setActivityId(0L);
//                                        activity.setActivityNameEn("Select Activity");
//                                        activity.setActivityNameHi("Select Activity");
//                                        activityArrayList.add(activity);
//                                        for (int i = 0; i < array.length(); i++) {
//                                            Activity activity1 = Activity.parseActivity(array.optJSONObject(i));
//                                            activityArrayList.add(activity1);
//                                        }
//
//                                        activitySpnAdapter.notifyDataSetChanged();
//                                    }
//                                }
//
//
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }
//
//                    @Override
//                    public void onError(ANError anError) {
//                        Log.e(TAG, "onError: " + anError.getErrorDetail());
//                        try {
//                            JSONObject errObj = new JSONObject(anError.getErrorBody());
//                            int statusCode = errObj.optInt("status");
//                            if (statusCode == 500) {
//                                sp.clear();
//                                Intent intent = new Intent(getActivity(), MainActivity.class);
//                                startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
//                                        Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
//                                getActivity().finish();
//                            }
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                });
//    }
//
//    private void getSchemes() {
//
//        AndroidNetworking.get(BuildConfig.BASE_URL + Constant.API_PATH + "lov/schemes")
//                .addHeaders("Authorization", "Bearer " + token)
//                .setTag("Schemes")
//                .setPriority(Priority.HIGH)
//                .build()
//                .getAsString(new StringRequestListener() {
//                    @Override
//                    public void onResponse(String response) {
//                        if (Utility.isStringValid(response)) {
//                            try {
//                                JSONObject object = new JSONObject(response);
//                                if (object.optBoolean("outcome")) {
//                                    JSONArray array = object.optJSONArray("data");
//                                    if (array != null && array.length() > 0) {
//                                        schemesArrayList.clear();
//                                        Schemes schemes = new Schemes();
//                                        schemes.setSchemeId(0L);
//                                        schemes.setSchemeCode("Select");
//                                        schemes.setSchemeNameEn("Select Scheme");
//                                        schemes.setSchemeNameHi("Select Scheme");
//                                        schemes.setActive(true);
//                                        schemesArrayList.add(schemes);
//                                        for (int i = 0; i < array.length(); i++) {
//                                            Schemes scheme = Schemes.parseSchemes(array.optJSONObject(i));
//                                            if (scheme.isActive()) {
//                                                schemesArrayList.add(scheme);
//                                            }
//                                        }
//
//                                        schemeSpnAdapter.notifyDataSetChanged();
//                                    }
//                                }
//
//
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }
//
//                    @Override
//                    public void onError(ANError anError) {
//                        Log.e(TAG, "onError: " + anError.getErrorDetail());
//                        try {
//                            JSONObject errObj = new JSONObject(anError.getErrorBody());
//                            int statusCode = errObj.optInt("status");
//                            if (statusCode == 500) {
//                                sp.clear();
//                                Intent intent = new Intent(getActivity(), MainActivity.class);
//                                startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
//                                        Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
//                                getActivity().finish();
//                            }
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                });
//
//    }


//    private void getSchemesWithOfflineData() {
//
//        AndroidNetworking.get(BuildConfig.BASE_URL + Constant.API_PATH + "lov/offline/schemes/activity")
//                .addHeaders("Authorization", "Bearer " + token)
//                .setTag("Schemes")
//                .setPriority(Priority.HIGH)
//                .build()
//                .getAsString(new StringRequestListener() {
//                    @Override
//                    public void onResponse(String response) {
//                        if (Utility.isStringValid(response)) {
//                            try {
//                                JSONObject object = new JSONObject(response);
//                                if (object.optBoolean("outcome")) {
//                                    JSONArray array = object.optJSONArray("data");
//                                    if (array != null && array.length() > 0) {
//                                        schemesArrayList.clear();
//                                        Schemes schemes = new Schemes();
//                                        schemes.setSchemeId(0L);
//                                        schemes.setSchemeCode("Select");
//                                        schemes.setSchemeNameEn("Select Scheme");
//                                        schemes.setSchemeNameHi("Select Scheme");
//                                        RealmList<Activity> activityRealmList = new RealmList<>();
//                                        schemes.setActivityRealmList(activityRealmList);
//                                        schemes.setActive(true);
//
////                                        PMSchemesOffline schemes = new PMSchemesOffline();
////                                        schemes.schemeNameEn = "Select Scheme";
////                                        schemes.schemeId = 0L;
////                                        schemes.schemeNameHi = "SelectScheme";
////                                        schemes.schemeCode = "SELECT";
////                                        schemes.isActive = true;
//
//                                        schemesArrayList.add(schemes);
//                                        for (int i = 0; i < array.length(); i++) {
////                                            Schemes scheme = Schemes.parseSchemes(array.optJSONObject(i));
////                                            if (scheme.isActive()) {
////                                                schemesArrayList.add(scheme);
////                                            }
//                                            Schemes scheme = Schemes.parseSchemes(array.optJSONObject(i));
//                                            if (scheme.isActive) {
//                                                schemesArrayList.add(scheme);
//                                            }
//                                        }
//
//                                        realm.executeTransaction(new Realm.Transaction() {
//                                            @Override
//                                            public void execute(Realm realm1) {
//                                                realm1.delete(Schemes.class);
//                                                realm1.delete(Activity.class);
//                                                realm1.insertOrUpdate(schemesArrayList);
//                                            }
//                                        });
//
//                                        schemeSpnAdapter.notifyDataSetChanged();
//                                    }
//                                }
//
//
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }
//
//                    @Override
//                    public void onError(ANError anError) {
//                        Log.e(TAG, "onError: " + anError.getErrorDetail());
//                        try {
//                            JSONObject errObj = new JSONObject(anError.getErrorBody());
//                            int statusCode = errObj.optInt("status");
//                            if (statusCode == 500) {
//                                sp.clear();
//                                Intent intent = new Intent(getActivity(), MainActivity.class);
//                                startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
//                                        Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
//                                getActivity().finish();
//                            }
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                });
//
//    }

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
//            selectedScheme = data.getLongExtra("schemeId", 0L);
//            selectedActivity = data.getLongExtra("activityId", 0L);
//            loadList();
        }

    }

    /**
     * navigate type 0 = overview
     * navigate type 1 = pending
     */

//    @Override
//    public void onActivityClick(int position) {
//
//    }
    @Override
    public void onOffActivityClick(int position) {
        if (shgListRealmList != null && shgListRealmList.get(position) != null) {
            if (sp.getStringData(Constant.USER_ROLE).equals(Constant.PMITRA)) {
                if (/*selectedActivityId.equals(1L)*/activityCode.equals("GOATRY")) {
                    Intent goatryViewIntent = new Intent(getActivity(), GoatryUpdateOfflineActivity.class/*GoatryViewMainActivity.class*/);
                    goatryViewIntent.putExtra("ENTITY_CODE", reportingLevelCode);
                    goatryViewIntent.putExtra("MONTH", monthNo);
                    goatryViewIntent.putExtra("YEAR", yearNo);
                    goatryViewIntent.putExtra("ENTITY_ID", shgListRealmList.get(position).orgId);
                    goatryViewIntent.putExtra("SCHEME_ID", selectedScheme);
                    goatryViewIntent.putExtra("ACTIVITY_ID", selectedActivity);
                    goatryViewIntent.putExtra("ENTITY_NAME", shgListRealmList.get(position).orgName);
                    goatryViewIntent.putExtra("SCHEME_NAME", schemeName);
                    goatryViewIntent.putExtra("ACTIVITY_NAME", activityName);
                    goatryViewIntent.putExtra("ACTIVITY_CODE", activityCode);
                    goatryViewIntent.putExtra("SCHEME_CODE", schemeCode);
                    goatryViewIntent.putExtra("ENTRY_FROM", 0);
                    startActivity(goatryViewIntent);
                } else if (/*selectedActivityId.equals(2L)*/activityCode.equals("POULTRY")) {
                    Intent poultryViewIntent = new Intent(getActivity(), BirdsUpdateActivity.class);
                    poultryViewIntent.putExtra("ENTITY_CODE", reportingLevelCode);
                    poultryViewIntent.putExtra("MONTH", monthNo);
                    poultryViewIntent.putExtra("YEAR", yearNo);
                    poultryViewIntent.putExtra("ENTITY_ID", shgListRealmList.get(position).orgId);
                    poultryViewIntent.putExtra("ENTITY_NAME", shgListRealmList.get(position).orgName);
                    poultryViewIntent.putExtra("SCHEME_ID", selectedScheme);
                    poultryViewIntent.putExtra("ACTIVITY_ID", selectedActivity);
                    poultryViewIntent.putExtra("SCHEME_NAME", schemeName);
                    poultryViewIntent.putExtra("ACTIVITY_NAME", activityName);
                    poultryViewIntent.putExtra("ACTIVITY_CODE", activityCode);
                    poultryViewIntent.putExtra("SCHEME_CODE", schemeCode);
                    poultryViewIntent.putExtra("ENTRY_FROM", 0);
                    startActivity(poultryViewIntent);
                } else if (/*selectedActivityId.equals(3L)*/activityCode.equals("FISHERY")) {
                    Intent fisheryViewIntent = new Intent(getActivity(), FishUpdateActivity.class);
                    fisheryViewIntent.putExtra("ENTITY_CODE", reportingLevelCode);
                    fisheryViewIntent.putExtra("MONTH", monthNo);
                    fisheryViewIntent.putExtra("YEAR", yearNo);
                    fisheryViewIntent.putExtra("ENTITY_ID", shgListRealmList.get(position).orgId);
                    fisheryViewIntent.putExtra("ENTITY_NAME", shgListRealmList.get(position).orgName);
                    fisheryViewIntent.putExtra("SCHEME_ID", selectedScheme);
                    fisheryViewIntent.putExtra("ACTIVITY_ID", selectedActivity);
                    fisheryViewIntent.putExtra("SCHEME_NAME", schemeName);
                    fisheryViewIntent.putExtra("ACTIVITY_NAME", activityName);
                    fisheryViewIntent.putExtra("ACTIVITY_CODE", activityCode);
                    fisheryViewIntent.putExtra("SCHEME_CODE", schemeCode);
                    fisheryViewIntent.putExtra("ENTRY_FROM", 0);
                    startActivity(fisheryViewIntent);
                } else if (/*selectedActivityId.equals(7L*/activityCode.equals("DAIRY")) {
                    Intent fisheryViewIntent = new Intent(getActivity(), DairyUpdateActivity.class);
                    fisheryViewIntent.putExtra("ENTITY_CODE", reportingLevelCode);
                    fisheryViewIntent.putExtra("MONTH", monthNo);
                    fisheryViewIntent.putExtra("YEAR", yearNo);
                    fisheryViewIntent.putExtra("ENTITY_ID", shgListRealmList.get(position).orgId);
                    fisheryViewIntent.putExtra("ENTITY_NAME", shgListRealmList.get(position).orgName);
                    fisheryViewIntent.putExtra("SCHEME_ID", selectedScheme);
                    fisheryViewIntent.putExtra("ACTIVITY_ID", selectedActivity);
                    fisheryViewIntent.putExtra("SCHEME_NAME", schemeName);
                    fisheryViewIntent.putExtra("ACTIVITY_NAME", activityName);
                    fisheryViewIntent.putExtra("ACTIVITY_CODE", activityCode);
                    fisheryViewIntent.putExtra("SCHEME_CODE", schemeCode);
                    fisheryViewIntent.putExtra("ENTRY_FROM", 0);
                    startActivity(fisheryViewIntent);
                }
            }
        }
    }

    @Override
    public void onEditDetails(int position) {
        if (isConnected) {
            if (sp.getStringData(Constant.USER_ROLE).equals("ROLE_KMITRA")) {
                if (reportingLevelCode.equals("SHG")) {
                    Intent intent = new Intent(getActivity(), AddFarmingShgActivity.class);

                    intent.putExtra("ENTITY_TYPE_CODE", reportingLevelCode);
                    intent.putExtra("SCHEME_ID", selectedScheme);
                    intent.putExtra("ACTIVITY_ID", selectedActivity);
                    intent.putExtra("ENTITY_ID", entriesArrayList.get(position).getOrgId());
                    intent.putExtra("ENTITY_NAME", entriesArrayList.get(position).getOrgName());
                    intent.putExtra(Constant.INTENT_TYPE, Constant.UPDATE);


                    startActivityForResult(intent, AUTO_REFRESH_REQ_CODE);
                } else if (reportingLevelCode.equals("HH")) {
                    Intent intent = new Intent(getActivity(), AddFarmingHHActivity.class);

                    intent.putExtra("ENTITY_TYPE_CODE", reportingLevelCode);
                    intent.putExtra("SCHEME_ID", selectedScheme);
                    intent.putExtra("ACTIVITY_ID", selectedActivity);
                    intent.putExtra("ENTITY_ID", entriesHhList.get(position).getOrgId());
                    intent.putExtra("ENTITY_NAME", entriesHhList.get(position).getOrgName());
                    intent.putExtra("SHG_NAME", shgName);
                    intent.putExtra(Constant.INTENT_TYPE, Constant.UPDATE);


                    startActivityForResult(intent, AUTO_REFRESH_REQ_CODE);
                }

            } else if (sp.getStringData(Constant.USER_ROLE).equals("ROLE_PMITRA")) {
                if (reportingLevelCode.equals("SHG")) {
                    if (selectedActivity.equals(1L)) {
                        Intent intent = new Intent(getActivity(), AddShgActivity.class);

                        intent.putExtra("ENTITY_TYPE_CODE", reportingLevelCode);
                        intent.putExtra("SCHEME_ID", selectedScheme);
                        intent.putExtra("ACTIVITY_ID", selectedActivity);
                        intent.putExtra("ENTITY_ID", entriesArrayList.get(position).getOrgId());
                        intent.putExtra("ENTITY_NAME", entriesArrayList.get(position).getOrgName());
                        intent.putExtra(Constant.INTENT_TYPE, Constant.UPDATE);

                        startActivityForResult(intent, AUTO_REFRESH_REQ_CODE);
                    } else if (selectedActivity.equals(2L)) {
                        Intent intent = new Intent(getActivity(), AddBirdActivity.class);

                        intent.putExtra("ENTITY_TYPE_CODE", reportingLevelCode);
                        intent.putExtra("SCHEME_ID", selectedScheme);
                        intent.putExtra("ACTIVITY_ID", selectedActivity);
                        intent.putExtra("ENTITY_ID", entriesArrayList.get(position).getOrgId());
                        intent.putExtra("ENTITY_NAME", entriesArrayList.get(position).getOrgName());
                        intent.putExtra(Constant.INTENT_TYPE, Constant.UPDATE);

                        startActivityForResult(intent, AUTO_REFRESH_REQ_CODE);
                    } else if (selectedActivity.equals(3L)) {
                        Intent intent = new Intent(getActivity(), AddFishActivity.class);

                        intent.putExtra("ENTITY_TYPE_CODE", reportingLevelCode);
                        intent.putExtra("SCHEME_ID", selectedScheme);
                        intent.putExtra("ACTIVITY_ID", selectedActivity);
                        intent.putExtra("ENTITY_ID", entriesArrayList.get(position).getOrgId());
                        intent.putExtra("ENTITY_NAME", entriesArrayList.get(position).getOrgName());
                        intent.putExtra(Constant.INTENT_TYPE, Constant.UPDATE);


                        startActivityForResult(intent, AUTO_REFRESH_REQ_CODE);
                    } else if (selectedActivity.equals(7L)) {
                        Intent intent = new Intent(getActivity(), AddDiaryActivity.class);

                        intent.putExtra("ENTITY_TYPE_CODE", reportingLevelCode);
                        intent.putExtra("SCHEME_ID", selectedScheme);
                        intent.putExtra("ACTIVITY_ID", selectedActivity);
                        intent.putExtra("ENTITY_ID", entriesArrayList.get(position).getOrgId());
                        intent.putExtra("ENTITY_NAME", entriesArrayList.get(position).getOrgName());
                        intent.putExtra(Constant.INTENT_TYPE, Constant.UPDATE);


                        startActivityForResult(intent, AUTO_REFRESH_REQ_CODE);
                    }
                } else if (reportingLevelCode.equals("HH")) {
                    if (selectedActivity.equals(1L)) {
                        Intent intent = new Intent(getActivity(), AddHouseHoldActivity.class);

                        intent.putExtra("ENTITY_TYPE_CODE", reportingLevelCode);
                        intent.putExtra("SCHEME_ID", selectedScheme);
                        intent.putExtra("ACTIVITY_ID", selectedActivity);
                        intent.putExtra(Constant.INTENT_TYPE, Constant.UPDATE);
                        intent.putExtra("ENTITY_ID", entriesHhList.get(position).getOrgId());
                        intent.putExtra("ENTITY_NAME", entriesHhList.get(position).getOrgName());
                        intent.putExtra("SHG_NAME", shgName);

                        startActivityForResult(intent, AUTO_REFRESH_REQ_CODE);
                    } else if (selectedActivity.equals(2L)) {
                        Intent intent = new Intent(getActivity(), AddBirdHouseHoldActivity.class);

                        intent.putExtra("ENTITY_TYPE_CODE", reportingLevelCode);
                        intent.putExtra("SCHEME_ID", selectedScheme);
                        intent.putExtra("ACTIVITY_ID", selectedActivity);
                        intent.putExtra(Constant.INTENT_TYPE, Constant.UPDATE);
                        intent.putExtra("ENTITY_ID", entriesHhList.get(position).getOrgId());
                        intent.putExtra("ENTITY_NAME", entriesHhList.get(position).getOrgName());
                        intent.putExtra("SHG_NAME", shgName);

                        startActivityForResult(intent, AUTO_REFRESH_REQ_CODE);
                    } else if (selectedActivity.equals(3L)) {
                        Intent intent = new Intent(getActivity(), AddFishHouseHoldActivity.class);

                        intent.putExtra("ENTITY_TYPE_CODE", reportingLevelCode);
                        intent.putExtra("SCHEME_ID", selectedScheme);
                        intent.putExtra("ACTIVITY_ID", selectedActivity);
                        intent.putExtra(Constant.INTENT_TYPE, Constant.UPDATE);
                        intent.putExtra("ENTITY_ID", entriesHhList.get(position).getOrgId());
                        intent.putExtra("ENTITY_NAME", entriesHhList.get(position).getOrgName());
                        intent.putExtra("SHG_NAME", shgName);


                        startActivityForResult(intent, AUTO_REFRESH_REQ_CODE);
                    } else if (selectedActivity.equals(7L)) {
                        Intent intent = new Intent(getActivity(), AddDiaryHouseHoldActivity.class);

                        intent.putExtra("ENTITY_TYPE_CODE", reportingLevelCode);
                        intent.putExtra("SCHEME_ID", selectedScheme);
                        intent.putExtra("ACTIVITY_ID", selectedActivity);
                        intent.putExtra(Constant.INTENT_TYPE, Constant.UPDATE);
                        intent.putExtra("ENTITY_ID", entriesHhList.get(position).getOrgId());
                        intent.putExtra("ENTITY_NAME", entriesHhList.get(position).getOrgName());
                        intent.putExtra("SHG_NAME", shgName);

                        startActivityForResult(intent, AUTO_REFRESH_REQ_CODE);
                    }
                }
            }
        } else {
            Toast.makeText(getActivity(), "You are Offline", Toast.LENGTH_SHORT).show();
        }
    }

//    @Override
//    public void onActivityHHClick(int position) {
//        if (isConnected) {
//            if (sp.getStringData(Constant.USER_ROLE).equals("ROLE_PMITRA")) {
//                if (selectedActivity.equals(1L)) {
//                    Intent intent = new Intent(getActivity(), GoatryViewMainActivity.class);
//
//                    intent.putExtra("MONTH", monthNo);
//                    intent.putExtra("YEAR", yearNo);
//                    intent.putExtra("ENTITY_ID", entriesHhList.get(position).getOrgId());
//                    intent.putExtra("ENTITY_TYPE_CODE", reportingLevelCode);
//                    intent.putExtra("SCHEME_ID", selectedScheme);
//                    intent.putExtra("ACTIVITY_ID", selectedActivity);
//                    intent.putExtra("SCHEME_NAME", schemeName);
//                    intent.putExtra("ACTIVITY_NAME", activityName);
//
//                    sp.setLongData("SCHEME_ID", selectedScheme);
//                    sp.setLongData("ACTIVITY_ID", selectedActivity);
//                    sp.setLongData("ENTITY_ID", entriesHhList.get(position).getOrgId());
//                    sp.setStringData("ENTITY_TYPE_CODE", reportingLevelCode);
//                    sp.setStringData("MONTH", monthNo);
//                    sp.setStringData("YEAR", yearNo);
//                    sp.setIntData("NAV_TYPE", 1);
//
//                    startActivityForResult(intent, AUTO_REFRESH_REQ_CODE);
//                } else if (selectedActivity.equals(2L)) {
//                    Intent intent = new Intent(getActivity(), BirdsViewMainActivity.class);
//
//                    intent.putExtra("MONTH", monthNo);
//                    intent.putExtra("YEAR", yearNo);
//                    intent.putExtra("ENTITY_ID", entriesHhList.get(position).getOrgId());
//                    intent.putExtra("ENTITY_TYPE_CODE", reportingLevelCode);
//                    intent.putExtra("SCHEME_ID", selectedScheme);
//                    intent.putExtra("ACTIVITY_ID", selectedActivity);
//                    intent.putExtra("SCHEME_NAME", schemeName);
//                    intent.putExtra("ACTIVITY_NAME", activityName);
//
//                    sp.setLongData("SCHEME_ID", selectedScheme);
//                    sp.setLongData("ACTIVITY_ID", selectedActivity);
//                    sp.setLongData("ENTITY_ID", entriesHhList.get(position).getOrgId());
//                    sp.setStringData("ENTITY_TYPE_CODE", reportingLevelCode);
//                    sp.setStringData("MONTH", monthNo);
//                    sp.setStringData("YEAR", yearNo);
//                    sp.setIntData("NAV_TYPE", 1);
//
//                    startActivityForResult(intent, AUTO_REFRESH_REQ_CODE);
//                } else if (selectedActivity.equals(3L)) {
//                    Intent intent = new Intent(getActivity(), FishViewMainActivity.class);
//
//                    intent.putExtra("MONTH", monthNo);
//                    intent.putExtra("YEAR", yearNo);
//                    intent.putExtra("ENTITY_ID", entriesHhList.get(position).getOrgId());
//                    intent.putExtra("ENTITY_TYPE_CODE", reportingLevelCode);
//                    intent.putExtra("SCHEME_ID", selectedScheme);
//                    intent.putExtra("ACTIVITY_ID", selectedActivity);
//                    intent.putExtra("SCHEME_NAME", schemeName);
//                    intent.putExtra("ACTIVITY_NAME", activityName);
//
//                    sp.setLongData("SCHEME_ID", selectedScheme);
//                    sp.setLongData("ACTIVITY_ID", selectedActivity);
//                    sp.setLongData("ENTITY_ID", entriesHhList.get(position).getOrgId());
//                    sp.setStringData("ENTITY_TYPE_CODE", reportingLevelCode);
//                    sp.setStringData("MONTH", monthNo);
//                    sp.setStringData("YEAR", yearNo);
//                    sp.setIntData("NAV_TYPE", 1);
//
//                    startActivityForResult(intent, AUTO_REFRESH_REQ_CODE);
//                } else if (selectedActivity.equals(7L)) {
//                    Intent intent = new Intent(getActivity(), DairyViewMainActivity.class);
//
//                    intent.putExtra("MONTH", monthNo);
//                    intent.putExtra("YEAR", yearNo);
//                    intent.putExtra("ENTITY_ID", entriesHhList.get(position).getOrgId());
//                    intent.putExtra("ENTITY_TYPE_CODE", reportingLevelCode);
//                    intent.putExtra("SCHEME_ID", selectedScheme);
//                    intent.putExtra("ACTIVITY_ID", selectedActivity);
//                    intent.putExtra("SCHEME_NAME", schemeName);
//                    intent.putExtra("ACTIVITY_NAME", activityName);
//
//                    sp.setLongData("SCHEME_ID", selectedScheme);
//                    sp.setLongData("ACTIVITY_ID", selectedActivity);
//                    sp.setLongData("ENTITY_ID", entriesHhList.get(position).getOrgId());
//                    sp.setStringData("ENTITY_TYPE_CODE", reportingLevelCode);
//                    sp.setStringData("MONTH", monthNo);
//                    sp.setStringData("YEAR", yearNo);
//                    sp.setIntData("NAV_TYPE", 1);
//
//                    startActivityForResult(intent, AUTO_REFRESH_REQ_CODE);
//                }
//            } else if (sp.getStringData(Constant.USER_ROLE).equals("ROLE_KMITRA")) {
//                Intent intent = new Intent(getActivity(), FarmingViewMainActivity.class);
//
//                intent.putExtra("MONTH", monthNo);
//                intent.putExtra("YEAR", yearNo);
//                intent.putExtra("ENTITY_ID", entriesHhList.get(position).getOrgId());
//                intent.putExtra("ENTITY_TYPE_CODE", reportingLevelCode);
//                intent.putExtra("SCHEME_ID", selectedScheme);
//                intent.putExtra("ACTIVITY_ID", selectedActivity);
//                intent.putExtra("SCHEME_NAME", schemeName);
//                intent.putExtra("ACTIVITY_NAME", activityName);
//
//                sp.setLongData("SCHEME_ID", selectedScheme);
//                sp.setLongData("ACTIVITY_ID", selectedActivity);
//                sp.setLongData("ENTITY_ID", entriesHhList.get(position).getOrgId());
//                sp.setStringData("ENTITY_TYPE_CODE", reportingLevelCode);
//                sp.setStringData("MONTH", monthNo);
//                sp.setStringData("YEAR", yearNo);
//                sp.setIntData("NAV_TYPE", 1);
//
////                startActivity(intent);
//                startActivityForResult(intent, AUTO_REFRESH_REQ_CODE);
//            }
//        } else {
//            Toast.makeText(getActivity(), "You are Offline", Toast.LENGTH_SHORT).show();
//        }
//    }

    @Override
    public void onActivityCLFClick(int position) {
        if (isConnected) {
            if (sp.getStringData(Constant.USER_ROLE).equals("ROLE_PMITRA")) {
                if (selectedActivity.equals(1L)) {
                    Intent intent = new Intent(getActivity(), GoatryViewMainActivity.class);

                    intent.putExtra("MONTH", monthNo);
                    intent.putExtra("YEAR", yearNo);
                    intent.putExtra("ENTITY_ID", entriesClfList.get(position).getOrgId());
                    intent.putExtra("ENTITY_TYPE_CODE", reportingLevelCode);
                    intent.putExtra("SCHEME_ID", selectedScheme);
                    intent.putExtra("ACTIVITY_ID", selectedActivity);

                    sp.setLongData("SCHEME_ID", selectedScheme);
                    sp.setLongData("ACTIVITY_ID", selectedActivity);
                    sp.setLongData("ENTITY_ID", entriesClfList.get(position).getOrgId());
                    sp.setStringData("ENTITY_TYPE_CODE", reportingLevelCode);
                    sp.setStringData("MONTH", monthNo);
                    sp.setStringData("YEAR", yearNo);
                    sp.setIntData("NAV_TYPE", 2);

                    startActivityForResult(intent, AUTO_REFRESH_REQ_CODE);
                } else if (selectedActivity.equals(2L)) {
                    Intent intent = new Intent(getActivity(), BirdsViewMainActivity.class);

                    intent.putExtra("MONTH", monthNo);
                    intent.putExtra("YEAR", yearNo);
                    intent.putExtra("ENTITY_ID", entriesClfList.get(position).getOrgId());
                    intent.putExtra("ENTITY_TYPE_CODE", reportingLevelCode);
                    intent.putExtra("SCHEME_ID", selectedScheme);
                    intent.putExtra("ACTIVITY_ID", selectedActivity);

                    sp.setLongData("SCHEME_ID", selectedScheme);
                    sp.setLongData("ACTIVITY_ID", selectedActivity);
                    sp.setLongData("ENTITY_ID", entriesClfList.get(position).getOrgId());
                    sp.setStringData("ENTITY_TYPE_CODE", reportingLevelCode);
                    sp.setStringData("MONTH", monthNo);
                    sp.setStringData("YEAR", yearNo);
                    sp.setIntData("NAV_TYPE", 2);

                    startActivityForResult(intent, AUTO_REFRESH_REQ_CODE);
                } else if (selectedActivity.equals(3L)) {
                    Intent intent = new Intent(getActivity(), FishViewMainActivity.class);

                    intent.putExtra("MONTH", monthNo);
                    intent.putExtra("YEAR", yearNo);
                    intent.putExtra("ENTITY_ID", entriesClfList.get(position).getOrgId());
                    intent.putExtra("ENTITY_TYPE_CODE", reportingLevelCode);
                    intent.putExtra("SCHEME_ID", selectedScheme);
                    intent.putExtra("ACTIVITY_ID", selectedActivity);

                    sp.setLongData("SCHEME_ID", selectedScheme);
                    sp.setLongData("ACTIVITY_ID", selectedActivity);
                    sp.setLongData("ENTITY_ID", entriesClfList.get(position).getOrgId());
                    sp.setStringData("ENTITY_TYPE_CODE", reportingLevelCode);
                    sp.setStringData("MONTH", monthNo);
                    sp.setStringData("YEAR", yearNo);
                    sp.setIntData("NAV_TYPE", 2);

                    startActivityForResult(intent, AUTO_REFRESH_REQ_CODE);
                } else if (selectedActivity.equals(7L)) {
                    Intent intent = new Intent(getActivity(), DairyViewMainActivity.class);

                    intent.putExtra("MONTH", monthNo);
                    intent.putExtra("YEAR", yearNo);
                    intent.putExtra("ENTITY_ID", entriesClfList.get(position).getOrgId());
                    intent.putExtra("ENTITY_TYPE_CODE", reportingLevelCode);
                    intent.putExtra("SCHEME_ID", selectedScheme);
                    intent.putExtra("ACTIVITY_ID", selectedActivity);

                    sp.setLongData("SCHEME_ID", selectedScheme);
                    sp.setLongData("ACTIVITY_ID", selectedActivity);
                    sp.setLongData("ENTITY_ID", entriesClfList.get(position).getOrgId());
                    sp.setStringData("ENTITY_TYPE_CODE", reportingLevelCode);
                    sp.setStringData("MONTH", monthNo);
                    sp.setStringData("YEAR", yearNo);
                    sp.setIntData("NAV_TYPE", 2);

                    startActivityForResult(intent, AUTO_REFRESH_REQ_CODE);
                }
            } else if (sp.getStringData(Constant.USER_ROLE).equals("ROLE_KMITRA")) {
            }
        } else {
            Toast.makeText(getActivity(), "You are Offline", Toast.LENGTH_SHORT).show();
        }
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

    @Override
    public void onActivityOfflineHHClick(int position) {
//        Toast.makeText(getActivity(), ""+position, Toast.LENGTH_SHORT).show();

//        hhshgLists


        if (householdsList != null && householdsList.get(position) != null) {
            if (sp.getStringData(Constant.USER_ROLE).equals(Constant.PMITRA)) {
                if (/*selectedActivityId.equals(1L)*/activityCode.equals("GOATRY")) {
                    Intent goatryViewIntent = new Intent(getActivity(), GoatryUpdateOfflineActivity.class/*GoatryViewMainActivity.class*/);
                    goatryViewIntent.putExtra("ENTITY_CODE", reportingLevelCode);
                    goatryViewIntent.putExtra("MONTH", monthNo);
                    goatryViewIntent.putExtra("YEAR", yearNo);
                    goatryViewIntent.putExtra("ENTITY_ID", householdsList.get(position).orgId);
                    goatryViewIntent.putExtra("ENTITY_NAME", householdsList.get(position).orgName);
                    goatryViewIntent.putExtra("SCHEME_ID", selectedScheme);
                    goatryViewIntent.putExtra("ACTIVITY_ID", selectedActivity);
                    goatryViewIntent.putExtra("SCHEME_NAME", schemeName);
                    goatryViewIntent.putExtra("ACTIVITY_NAME", activityName);
                    goatryViewIntent.putExtra("ACTIVITY_CODE", activityCode);
                    goatryViewIntent.putExtra("SCHEME_CODE", schemeCode);
                    goatryViewIntent.putExtra("ENTRY_FROM", 0);
                    startActivity(goatryViewIntent);
                } else if (/*selectedActivityId.equals(2L)*/activityCode.equals("POULTRY")) {
                    Intent poultryViewIntent = new Intent(getActivity(), BirdsUpdateActivity.class);
                    poultryViewIntent.putExtra("ENTITY_CODE", reportingLevelCode);
                    poultryViewIntent.putExtra("MONTH", monthNo);
                    poultryViewIntent.putExtra("YEAR", yearNo);
                    poultryViewIntent.putExtra("ENTITY_ID", householdsList.get(position).orgId);
                    poultryViewIntent.putExtra("ENTITY_NAME", householdsList.get(position).orgName);
                    poultryViewIntent.putExtra("SCHEME_ID", selectedScheme);
                    poultryViewIntent.putExtra("ACTIVITY_ID", selectedActivity);
                    poultryViewIntent.putExtra("SCHEME_NAME", schemeName);
                    poultryViewIntent.putExtra("ACTIVITY_NAME", activityName);
                    poultryViewIntent.putExtra("ACTIVITY_CODE", activityCode);
                    poultryViewIntent.putExtra("SCHEME_CODE", schemeCode);
                    poultryViewIntent.putExtra("ENTRY_FROM", 0);
                    startActivity(poultryViewIntent);
                } else if (/*selectedActivityId.equals(3L)*/activityCode.equals("FISHERY")) {
                    Intent fisheryViewIntent = new Intent(getActivity(), FishUpdateActivity.class);
                    fisheryViewIntent.putExtra("ENTITY_CODE", reportingLevelCode);
                    fisheryViewIntent.putExtra("MONTH", monthNo);
                    fisheryViewIntent.putExtra("YEAR", yearNo);
                    fisheryViewIntent.putExtra("ENTITY_ID", householdsList.get(position).orgId);
                    fisheryViewIntent.putExtra("ENTITY_NAME", householdsList.get(position).orgName);
                    fisheryViewIntent.putExtra("SCHEME_ID", selectedScheme);
                    fisheryViewIntent.putExtra("ACTIVITY_ID", selectedActivity);
                    fisheryViewIntent.putExtra("SCHEME_NAME", schemeName);
                    fisheryViewIntent.putExtra("ACTIVITY_NAME", activityName);
                    fisheryViewIntent.putExtra("ACTIVITY_CODE", activityCode);
                    fisheryViewIntent.putExtra("SCHEME_CODE", schemeCode);
                    fisheryViewIntent.putExtra("ENTRY_FROM", 0);
                    startActivity(fisheryViewIntent);
                } else if (/*selectedActivityId.equals(7L*/activityCode.equals("DAIRY")) {
                    Intent fisheryViewIntent = new Intent(getActivity(), DairyUpdateActivity.class);
                    fisheryViewIntent.putExtra("ENTITY_CODE", reportingLevelCode);
                    fisheryViewIntent.putExtra("MONTH", monthNo);
                    fisheryViewIntent.putExtra("YEAR", yearNo);
                    fisheryViewIntent.putExtra("ENTITY_ID", householdsList.get(position).orgId);
                    fisheryViewIntent.putExtra("ENTITY_NAME", householdsList.get(position).orgName);
                    fisheryViewIntent.putExtra("SCHEME_ID", selectedScheme);
                    fisheryViewIntent.putExtra("ACTIVITY_ID", selectedActivity);
                    fisheryViewIntent.putExtra("SCHEME_NAME", schemeName);
                    fisheryViewIntent.putExtra("ACTIVITY_NAME", activityName);
                    fisheryViewIntent.putExtra("ACTIVITY_CODE", activityCode);
                    fisheryViewIntent.putExtra("SCHEME_CODE", schemeCode);
                    fisheryViewIntent.putExtra("ENTRY_FROM", 0);
                    startActivity(fisheryViewIntent);
                }
            }
        }
    }
}
