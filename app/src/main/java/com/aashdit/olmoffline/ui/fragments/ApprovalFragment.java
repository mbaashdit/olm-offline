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
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.aashdit.olmoffline.BuildConfig;
import com.aashdit.olmoffline.R;
import com.aashdit.olmoffline.adapters.ActivitySpnAdapter;
import com.aashdit.olmoffline.adapters.ActivityTypeSpnAdapter;
import com.aashdit.olmoffline.adapters.ApprovedAdapter;
import com.aashdit.olmoffline.adapters.PendingAdapter;
import com.aashdit.olmoffline.adapters.PendingOfflineAdapter;
import com.aashdit.olmoffline.adapters.RevertedAdapter;
import com.aashdit.olmoffline.adapters.SchemeSpnAdapter;
import com.aashdit.olmoffline.databinding.FragmentApprovalBinding;
import com.aashdit.olmoffline.db.shg.ActivitySHGSearch;
import com.aashdit.olmoffline.db.shg.SchemeSHGSearch;
import com.aashdit.olmoffline.db.shg.ShgList;
import com.aashdit.olmoffline.models.Activity;
import com.aashdit.olmoffline.models.ActivityType;
import com.aashdit.olmoffline.models.Approved;
import com.aashdit.olmoffline.models.Pending;
import com.aashdit.olmoffline.models.Reverted;
import com.aashdit.olmoffline.models.Schemes;
import com.aashdit.olmoffline.models.Task;
import com.aashdit.olmoffline.receiver.ConnectivityChangeReceiver;
import com.aashdit.olmoffline.ui.MainActivity;
import com.aashdit.olmoffline.ui.activities.PendingActivity;
import com.aashdit.olmoffline.utils.Constant;
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
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

public class ApprovalFragment extends Fragment implements PendingAdapter.OnPendingClickListener,
        RevertedAdapter.OnRevertClickListener, ApprovedAdapter.OnApprovedClickListener, PendingOfflineAdapter.OnOfflinePendingClickListener,
        ConnectivityChangeReceiver.ConnectivityReceiverListener {

    private static final String TAG = "ApprovalFragment";
    private final ArrayList<String> yearsList = new ArrayList<>();
    private final ArrayList<String> monthList = new ArrayList<>();
    String reqUrl = "";
    RealmList<Activity> activityRealmList;
    RealmResults<Task> taskRealmResults;
    RealmList<Activity> pmActivitiesOfflines;
    String month = "";
    String year = "";
    String activity = "";
    String scheme = "";
    private FragmentApprovalBinding binding;
    private List<Pending> pending;
    private List<Approved> approveds;
    private List<Reverted> reverteds;
    private List<Task> taskList;
    private PendingAdapter adapter;
    private PendingOfflineAdapter offlineAdapter;
    private ApprovedAdapter approvedAdapter;
    private RevertedAdapter revertedAdapter;
    private Long selectedScheme = 0L;
    private Long selectedActivity = 0L;
    private Long selectedActivityType = 0L;
    private String selectedYear = "ALL", selectedMonth = "ALL", selectedSchemeName = "", selectedActivityName = "";
    private SharedPrefManager sp;
    private String token;
    private ArrayList<Schemes> schemesArrayList;
    private ArrayList<com.aashdit.olmoffline.models.Activity> activityArrayList;
    private ArrayList<ActivityType> activityTypeArrayList;
    private SchemeSpnAdapter schemeSpnAdapter;
    private ActivitySpnAdapter activitySpnAdapter;
    private ActivityTypeSpnAdapter activityTypeSpnAdapter;
    private String status, userType;
    private Realm realm;
    /**
     * @param isConnected for checking network connectivity
     */
    private boolean isConnected = false;
    private ConnectivityChangeReceiver mConnectivityChangeReceiver;
    private RealmResults<Schemes> schemesRealmResults;
    private RealmResults<Activity> activityRealmResults;
    private RealmList<Activity> realmActivities;
    private RealmList<ShgList> shgListRealmList;
    private RealmResults<ActivityType> activityTypeRealmResults;
    private String schemeCode = "";
    private String activityCode = "";
    private String activityName = "";
    private String activityTypeName = "";
    private RealmResults<ActivityType> activityTypeOfflines;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentApprovalBinding.inflate(inflater, container, false);
        realm = Realm.getDefaultInstance();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sp = SharedPrefManager.getInstance(getActivity());
        token = sp.getStringData(Constant.APP_TOKEN);
        userType = sp.getStringData(Constant.USER_ROLE);

        if (realm == null) {
            realm = Realm.getDefaultInstance();
        } else {
            schemesRealmResults = realm.where(Schemes.class).findAll();
            activityRealmResults = realm.where(Activity.class).findAll();
            if (userType.equals(Constant.KMITRA)) {
                activityTypeRealmResults = realm.where(ActivityType.class).findAll();
            }
        }
        mConnectivityChangeReceiver = new ConnectivityChangeReceiver();
        mConnectivityChangeReceiver.setConnectivityReceiverListener(this);
        isConnected = mConnectivityChangeReceiver.getConnectionStatus(getContext());
        registerNetworkBroadcast();

        Long date = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date resultdate = new Date(date);

        String s = sdf.format(resultdate);
        String[] parts = s.split("/");
        String month = parts[1];
        String year = parts[2];

        int currentYear = Integer.parseInt(year);
        int currentOneDown = currentYear - 1;
        int currentTwoDown = currentYear - 2;
        int currentThreeDown = currentYear - 3;
        int currentFourDown = currentYear - 4;
        int currentOneUp = currentYear + 1;
        int currentTwoUp = currentYear + 2;
        int currentThreeUp = currentYear + 3;
        int currentFourUp = currentYear + 4;

//        yearsList.add("ALL");
        yearsList.add(String.valueOf(currentFourDown));
        yearsList.add(String.valueOf(currentThreeDown));
        yearsList.add(String.valueOf(currentTwoDown));
        yearsList.add(String.valueOf(currentOneDown));
        yearsList.add(String.valueOf(currentYear));
        yearsList.add(String.valueOf(currentOneUp));
        yearsList.add(String.valueOf(currentTwoUp));
        yearsList.add(String.valueOf(currentThreeUp));
        yearsList.add(String.valueOf(currentFourUp));

        generateMonth(year);

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, yearsList);
        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Creating adapter for spinner
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, monthList);
        // Drop down layout style - list view with radio button
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        binding.spnYears.setAdapter(dataAdapter);
        binding.spnYears.setSelection(4);
        binding.spnMonths.setAdapter(monthAdapter);


        schemesRealmResults = realm.where(Schemes.class).findAll();
        activityRealmResults = realm.where(Activity.class).findAll();
        if (userType.equals(Constant.KMITRA)) {
            activityTypeRealmResults = realm.where(ActivityType.class).findAll();
        }

        Schemes schemes = new Schemes();
        schemes.setSchemeId(0L);
        schemes.setSchemeCode("Scheme");
        schemes.setSchemeNameEn("Select");
        schemes.setSchemeNameHi("Select");
        schemes.setActive(true);

        schemesArrayList = new ArrayList<>();
        schemesArrayList.add(schemes);

        Activity activity = new Activity();
        activity.setActivityCode("Activity");
        activity.setActivityId(0L);
        activity.setActivityNameEn("Select");
        activity.setActivityNameHi("Select");

        activityArrayList = new ArrayList<>();
        activityArrayList.add(activity);

        taskList = new ArrayList<>();

        status = "PENDING";
        if (sp.getStringData(Constant.USER_ROLE).equals(Constant.UMITRA) ||
                sp.getStringData(Constant.USER_ROLE).equals(Constant.CRPEP) ||
                sp.getStringData(Constant.USER_ROLE).equals(Constant.KMITRA)) {
            binding.spnActType.setVisibility(View.VISIBLE);
            binding.llActivityType.setVisibility(View.VISIBLE);
        } else {
            binding.spnActType.setVisibility(View.GONE);
            binding.llActivityType.setVisibility(View.GONE);
            if (isConnected && schemesRealmResults.size() == 0) {
                getSchemesWithOfflineData();
            }
        }

        ActivityType activityType = new ActivityType();
        activityType.setActivityCode("ALL");
        activityType.setActivityId(0L);
        activityType.setActivityTypeDesc("Select");
        activityType.setActive(true);

        activityTypeArrayList = new ArrayList<>();
        activityTypeArrayList.add(activityType);


        if (schemesRealmResults != null) {
            schemesArrayList = new ArrayList<>(schemesRealmResults);
        }
        schemeSpnAdapter = new SchemeSpnAdapter(getActivity(), schemesArrayList);
        binding.spnSchemes.setAdapter(schemeSpnAdapter);
        schemeSpnAdapter.notifyDataSetChanged();

        if (activityRealmResults != null) {
            activityArrayList = new ArrayList<>(activityRealmResults);
        }
        activitySpnAdapter = new ActivitySpnAdapter(getActivity(), activityArrayList);
        binding.spnActivities.setAdapter(activitySpnAdapter);


        activityTypeSpnAdapter = new ActivityTypeSpnAdapter(getActivity(), activityTypeArrayList);
        binding.spnActType.setAdapter(activityTypeSpnAdapter);

        if (schemesRealmResults != null && schemesRealmResults.size() > 0) {
            schemesArrayList = new ArrayList<>(schemesRealmResults);
        }
        schemeSpnAdapter = new SchemeSpnAdapter(getActivity(), schemesArrayList);
        binding.spnSchemes.setAdapter(schemeSpnAdapter);


        if (activityRealmList != null && activityRealmList.size() > 0) {
            activityArrayList = new ArrayList<>(activityRealmList);
        }
        activitySpnAdapter = new ActivitySpnAdapter(getActivity(), activityArrayList);
        binding.spnActivities.setAdapter(activitySpnAdapter);


        binding.spnSchemes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    Activity activity = new Activity();
                    activity.setActivityCode("Activity");
                    activity.setActivityId(0L);
                    activity.setActivityNameEn("Select");
                    activity.setActivityNameHi("Select");
                    selectedScheme = 0L;
                    schemeCode = "";
                    activityArrayList = new ArrayList<>();
                    activityArrayList.clear();
                    activityArrayList.add(activity);
                    activitySpnAdapter = new ActivitySpnAdapter(getActivity(), activityArrayList);
                    binding.spnActivities.setAdapter(activitySpnAdapter);

                    if (sp.getStringData(Constant.USER_ROLE).equals(Constant.UMITRA) ||
                            sp.getStringData(Constant.USER_ROLE).equals(Constant.CRPEP) ||
                            sp.getStringData(Constant.USER_ROLE).equals(Constant.KMITRA)) {


                        ActivityType activityType = new ActivityType();
                        activityType.setActivityCode("ALL");
                        activityType.setActivityId(0L);
                        activityType.setActivityTypeDesc("Select");
                        activityType.setActive(true);


                        activityTypeArrayList.clear();
                        activityTypeArrayList.add(activityType);
                        selectedActivityType = 0L;
                        activityTypeSpnAdapter.notifyDataSetChanged();
                    }

                } else {
                    selectedScheme = schemesArrayList.get(position).getSchemeId();
                    schemeCode = schemesArrayList.get(position).getSchemeCode();
                    selectedSchemeName = schemesArrayList.get(position).getSchemeNameEn();


                    Activity activity = new Activity();
                    activity.setActivityCode("Activity");
                    activity.setActivityId(0L);
                    activity.setActivityNameEn("Select");
                    activity.setActivityNameHi("Select");

                    activityArrayList = new ArrayList<>();
                    activityArrayList.clear();
//                    activityArrayList.add(activity);
                    activitySpnAdapter = new ActivitySpnAdapter(getActivity(), activityArrayList);
                    binding.spnActivities.setAdapter(activitySpnAdapter);


                    if (sp.getStringData(Constant.USER_ROLE).equals(Constant.UMITRA) ||
                            sp.getStringData(Constant.USER_ROLE).equals(Constant.CRPEP) ||
                            sp.getStringData(Constant.USER_ROLE).equals(Constant.KMITRA)) {


                        ActivityType activityType = new ActivityType();
                        activityType.setActivityCode("ALL");
                        activityType.setActivityId(0L);
                        activityType.setActivityTypeDesc("Select");
                        activityType.setActive(true);


                        activityTypeArrayList.clear();
                        activityTypeArrayList.add(activityType);
                        selectedActivityType = 0L;
                        activityTypeSpnAdapter.notifyDataSetChanged();

                        getActivityTypeFromRealmWithSchemeId(selectedScheme);

//                        getActivityTypeByScheme();
                    } else {
//                        getActivityAccordingToScheme(selectedScheme);
                        getActivityFromRealmWithSchemeId(selectedScheme);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        binding.spnActType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    selectedActivityType = 0L;
                    selectedActivity = 0L;

                    Activity activity = new Activity();
                    activity.setActivityCode("ACTIVITY");
                    activity.setActivityId(0L);
                    activity.setActivityNameEn("Select");
                    activity.setActivityNameHi("Select");

                    activityArrayList = new ArrayList<>();
                    activityArrayList.clear();
                    activityArrayList.add(activity);
                    activitySpnAdapter = new ActivitySpnAdapter(getActivity(), activityArrayList);
                    binding.spnActivities.setAdapter(activitySpnAdapter);


                } else {

                    selectedActivityType = 0L;
                    selectedActivity = 0L;

//                    Activity activity = new Activity();
//                    activity.setActivityCode("ACTIVITY");
//                    activity.setActivityId(0L);
//                    activity.setActivityNameEn("Select");
//                    activity.setActivityNameHi("Select");
//
//                    activityArrayList = new ArrayList<>();
//                    activityArrayList.clear();
//                    activityArrayList.add(activity);
                    activitySpnAdapter = null;
                    activitySpnAdapter = new ActivitySpnAdapter(getActivity(), activityArrayList);
                    binding.spnActivities.setAdapter(activitySpnAdapter);


                    selectedActivityType = activityTypeArrayList.get(position).getActivityId();
//                    activityTypeName = activityTypeArrayList.get(position).getActivityId();
//                    getActivityAccordingToScheme(selectedActivityType);

                    getActivityByActivityType(selectedActivityType);

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        binding.spnActivities.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedActivity = activityArrayList.get(position).getActivityId();
                selectedActivityName = activityArrayList.get(position).getActivityNameEn();
                activityCode = activityArrayList.get(position).getActivityCode();


                if (userType.equals(Constant.PMITRA)) {
                    if (pmActivitiesOfflines != null) {
                        selectedActivity = pmActivitiesOfflines.get(position).getActivityId();
                        activityCode = pmActivitiesOfflines.get(position).getActivityCode();
                        activityName = pmActivitiesOfflines.get(position).getActivityNameEn();


                        SchemeSHGSearch schemeSearchesResult = realm.where(SchemeSHGSearch.class)
                                .equalTo("schemeId", selectedScheme)
                                .findFirst();

                        if (schemeSearchesResult != null) {
                            RealmList<ActivitySHGSearch> activityRealmList = schemeSearchesResult.activityRealmList;

                            for (int i = 0; i < activityRealmList.size(); i++) {
                                if (selectedActivity.equals(activityRealmList.get(i).activityId)) {
                                    shgListRealmList = activityRealmList.get(i).shgList;
                                    loadOfflineListList(shgListRealmList);
                                }
                            }
                        }
                    }
                } else {
                    getKMShgOfflineList();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        binding.spnYears.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedYear = yearsList.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        binding.spnMonths.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedMonth = monthList.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        pending = new ArrayList<>();

        approveds = new ArrayList<>();

        reverteds = new ArrayList<>();

        binding.progressBar.setVisibility(View.GONE);

        binding.tvPending.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ic_selected));
        binding.tvPending.setTextColor(Color.parseColor("#DD084B"));

        binding.tvNoItems.setVisibility(View.GONE);

        binding.tvPending.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConnected) {
                    status = "PENDING";
//                    getTask(status);
                    binding.tvPending.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ic_selected));
                    binding.tvPending.setTextColor(Color.parseColor("#DD084B"));

                    binding.tvApproved.setBackgroundResource(0);
                    binding.tvApproved.setTextColor(Color.parseColor("#000000"));
                    binding.tvReverted.setBackgroundResource(0);
                    binding.tvReverted.setTextColor(Color.parseColor("#000000"));
                }
                setupPendingAdapter();

            }
        });

        binding.tvApproved.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConnected) {
                    status = "APPROVED";
//                    getTask(status);
                    binding.tvApproved.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ic_selected));
                    binding.tvApproved.setTextColor(Color.parseColor("#DD084B"));

                    binding.tvPending.setBackgroundResource(0);
                    binding.tvPending.setTextColor(Color.parseColor("#000000"));
                    binding.tvReverted.setBackgroundResource(0);
                    binding.tvReverted.setTextColor(Color.parseColor("#000000"));

                    setupPendingAdapter();
                } else {
                    Toast.makeText(getActivity(), "You are Offline", Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.tvReverted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConnected) {
                    status = "REVERTED";
//                    getTask(status);
                    binding.tvReverted.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ic_selected));
                    binding.tvReverted.setTextColor(Color.parseColor("#DD084B"));

                    binding.tvPending.setBackgroundResource(0);
                    binding.tvPending.setTextColor(Color.parseColor("#000000"));
                    binding.tvApproved.setBackgroundResource(0);
                    binding.tvApproved.setTextColor(Color.parseColor("#000000"));

                    setupPendingAdapter();
                } else {
                    Toast.makeText(getActivity(), "You are Offline", Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.rvApprovals.setLayoutManager(new LinearLayoutManager(getActivity()));

        binding.swiperefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (isConnected) {
//                    getTask(status);
                    binding.swiperefresh.setRefreshing(false);
                } else {
                    binding.swiperefresh.setRefreshing(false);
//                    setupPendingAdapter();
                    getKMShgOfflineList();
                }
            }
        });

        setupPendingAdapter();

//        getSchemes();
//        getTask(status);
    }

    private void getActivityByActivityType(Long selectedActivityType) {
//        RealmResults<Activity> activityRealmResults = realm.where(Activity.class)
//                .equalTo("activityId",selectedActivityType)
//                .findAll();

        activityTypeRealmResults.where().equalTo("activityId", selectedActivityType)
                .findAll();

        ActivityType activityType = realm.where(ActivityType.class)
                .equalTo("activityId", selectedActivityType)
                .findFirst();
//        activityType.activityRealmList

        activityArrayList = new ArrayList<>(activityType.activityRealmList);
        activitySpnAdapter = new ActivitySpnAdapter(getActivity(), activityArrayList);
        binding.spnActivities.setAdapter(activitySpnAdapter);
        activitySpnAdapter.notifyDataSetChanged();

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

                                        ActivityType activityType = new ActivityType();
                                        activityType.setActivityCode("ALL");
                                        activityType.setActivityId(0L);
                                        activityType.setActivityTypeDesc("Select");
                                        activityType.setActive(true);
                                        activityTypeArrayList.add(activityType);
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

    private void getTask(String status) {

        if (selectedScheme.equals(0L)) {
            taskList.clear();
            binding.tvNoItems.setVisibility(View.VISIBLE);
            binding.rvApprovals.setVisibility(View.GONE);
            setupPendingAdapter();
            Toast.makeText(getActivity(), "Please Select a Scheme", Toast.LENGTH_SHORT).show();
        } else if ((sp.getStringData(Constant.USER_ROLE).equals("ROLE_UMITRA") ||
                sp.getStringData(Constant.USER_ROLE).equals("ROLE_CRPEP") ||
                sp.getStringData(Constant.USER_ROLE).equals("ROLE_KMITRA")) && selectedActivityType.equals(0L)) {
            taskList.clear();
            binding.tvNoItems.setVisibility(View.VISIBLE);
            binding.rvApprovals.setVisibility(View.GONE);
            setupPendingAdapter();
            Toast.makeText(getActivity(), "Please Select an Activity type", Toast.LENGTH_SHORT).show();
        } else if (selectedActivity.equals(0L)) {
            taskList.clear();
            binding.tvNoItems.setVisibility(View.VISIBLE);
            binding.rvApprovals.setVisibility(View.GONE);
            setupPendingAdapter();
            Toast.makeText(getActivity(), "Please Select an activity", Toast.LENGTH_SHORT).show();
        } else {


            if (selectedMonth.equals("ALL")) {
                month = "";
            } else if (selectedMonth.equals("JAN")) {
                month = "1";
            } else if (selectedMonth.equals("FEB")) {
                month = "2";
            } else if (selectedMonth.equals("MAR")) {
                month = "3";
            } else if (selectedMonth.equals("APR")) {
                month = "4";
            } else if (selectedMonth.equals("MAY")) {
                month = "5";
            } else if (selectedMonth.equals("JUN")) {
                month = "6";
            } else if (selectedMonth.equals("JUL")) {
                month = "7";
            } else if (selectedMonth.equals("AUG")) {
                month = "8";
            } else if (selectedMonth.equals("SEP")) {
                month = "9";
            } else if (selectedMonth.equals("OCT")) {
                month = "10";
            } else if (selectedMonth.equals("NOV")) {
                month = "11";
            } else if (selectedMonth.equals("DEC")) {
                month = "12";
            }

            if (selectedYear.equals("ALL")) {
                year = yearsList.get(4);
            } else {
                year = selectedYear;
            }

//        year = yearsList.get(5);

            if (selectedActivity.equals(0L)) {
                activity = "";
            } else {
                activity = String.valueOf(selectedActivity);
            }
//        if (selectedActivity.equals(0L)) {
//            activity = "";
//        } else {
//            activity = String.valueOf(selectedActivity);
//        }
            if (selectedScheme.equals(0L)) {
                scheme = "";
            } else {
                scheme = String.valueOf(selectedScheme);
            }

            binding.progressBar.setVisibility(View.VISIBLE);

            JSONObject object = new JSONObject();
            try {
                object.put("month", month);
                object.put("year", year);
                object.put("schemeId", scheme);
                object.put("activityId", activity);
                object.put("taskStatus", status);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            String url = "";

            if (sp.getStringData(Constant.USER_ROLE).equals(Constant.PMITRA)) {
                if (scheme.equals("") || activity.equals("")) {
                    Toast.makeText(getActivity(), "The data fetching may take sometime depending on the network strength.", Toast.LENGTH_LONG).show();
                }
                url = "tasks/search";
            } else if (sp.getStringData(Constant.USER_ROLE).equals(Constant.KMITRA)) {
                if (scheme.equals("") || activity.equals("")) {
                    Toast.makeText(getActivity(), "The data fetching may take sometime depending on the network strength.", Toast.LENGTH_LONG).show();
                }
                url = "farming/tasks/search";
            } else if (sp.getStringData(Constant.USER_ROLE).equals(Constant.UMITRA)
                    || sp.getStringData(Constant.USER_ROLE).equals(Constant.CRPEP)) {
                if (selectedActivityType.equals(2L)) {
                    if (scheme.equals("") || activity.equals("")) {
                        Toast.makeText(getActivity(), "The data fetching may take sometime depending on the network strength.", Toast.LENGTH_LONG).show();
                    }
                    url = "ntfp-tasar/tasks/search";
                } else if (selectedActivityType.equals(13L)) {
                    if (scheme.equals("") || activity.equals("")) {
                        Toast.makeText(getActivity(), "The data fetching may take sometime depending on the network strength.", Toast.LENGTH_LONG).show();
                    }
                    url = "ntfp/tasks/search";
                } else {
                    if (scheme.equals("") || activity.equals("")) {
                        Toast.makeText(getActivity(), "The data fetching may take sometime depending on the network strength.", Toast.LENGTH_LONG).show();
                    }
                    url = "nonfarming/tasks/search";
                }
            }

            AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + url)
                    .addHeaders("Authorization", "Bearer " + token)
                    .setTag("Activity")
                    .addJSONObjectBody(object)
                    .setPriority(Priority.HIGH)
                    .build()
                    .getAsString(new StringRequestListener() {
                        @Override
                        public void onResponse(String response) {
                            binding.progressBar.setVisibility(View.GONE);
                            if (Utility.isStringValid(response)) {
                                try {
                                    JSONObject resObj = new JSONObject(response);
                                    if (resObj.optBoolean("outcome")) {
                                        JSONArray resArray = resObj.optJSONArray("data");
                                        if (resArray != null && resArray.length() > 0) {
                                            binding.tvNoItems.setVisibility(View.GONE);
                                            binding.rvApprovals.setVisibility(View.VISIBLE);
                                            taskList.clear();
                                            for (int i = 0; i < resArray.length(); i++) {
                                                Task task = Task.parseTask(resArray.optJSONObject(i));
                                                taskList.add(task);
                                            }

                                            realm.executeTransaction(new Realm.Transaction() {
                                                @Override
                                                public void execute(Realm realm1) {
//                                                    realm1.delete(Task.class);
                                                    realm1.insertOrUpdate(taskList);
                                                }
                                            });

                                            setupPendingAdapter();
                                        } else {
                                            taskList.clear();
                                            binding.tvNoItems.setVisibility(View.VISIBLE);
                                            binding.rvApprovals.setVisibility(View.GONE);
                                            setupPendingAdapter();
                                        }
                                    } else {
                                        taskList.clear();
                                        binding.tvNoItems.setVisibility(View.VISIBLE);
                                        binding.rvApprovals.setVisibility(View.GONE);
                                        setupPendingAdapter();
                                        Toast.makeText(getActivity(), resObj.optString("message"), Toast.LENGTH_SHORT).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onError(ANError anError) {
                            binding.progressBar.setVisibility(View.GONE);
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
    }

    private void setupRevertedAdapter() {
        revertedAdapter = new RevertedAdapter(getActivity(), taskList);
        revertedAdapter.setOnRevertClickListener(this);
        binding.rvApprovals.setAdapter(revertedAdapter);
        revertedAdapter.notifyDataSetChanged();
    }

    private void setupApprovedAdapter() {
        approvedAdapter = new ApprovedAdapter(getActivity(), taskList);
        approvedAdapter.setOnApprovedClickListener(this);
        binding.rvApprovals.setAdapter(approvedAdapter);
        approvedAdapter.notifyDataSetChanged();
    }

    private void setupPendingAdapter() {
        if (isConnected) {
            adapter = new PendingAdapter(getActivity(), taskList, userType);
            adapter.setOnPendingClickListener(this);
            binding.rvApprovals.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        } else {
            binding.swiperefresh.setRefreshing(false);
            taskRealmResults = realm.where(Task.class)
                    .equalTo("schemeId", selectedScheme)
                    .equalTo("activityId", selectedActivity)
                    .findAll();
            offlineAdapter = new PendingOfflineAdapter(getActivity(), taskRealmResults, userType);
            offlineAdapter.setOnPendingClickListener(this);
            binding.rvApprovals.setAdapter(offlineAdapter);
            offlineAdapter.notifyDataSetChanged();
        }
    }

    private void generateMonth(String selectedYear) {
        monthList.clear();
        monthList.add("ALL");
        monthList.add("JAN");
        monthList.add("FEB");
        monthList.add("MAR");
        monthList.add("APR");
        monthList.add("MAY");
        monthList.add("JUN");
        monthList.add("JUL");
        monthList.add("AUG");
        monthList.add("SEP");
        monthList.add("OCT");
        monthList.add("NOV");
        monthList.add("DEC");

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
                                        schemes.setSchemeCode("Scheme");
                                        schemes.setSchemeNameEn("Select");
                                        schemes.setSchemeNameHi("ALL");
                                        schemes.setActive(true);
                                        schemesArrayList.add(schemes);
                                        for (int i = 0; i < array.length(); i++) {
                                            Schemes scheme = Schemes.parseSchemes(array.optJSONObject(i), userType);
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

    private void getActivityAccordingToScheme(Long selectedDataId) {

        if (isConnected) {
            if (userType.equals(Constant.PMITRA)) {
                reqUrl = "lov/activity/";
            } else {
                reqUrl = "lov/activityByType/";
            }
            AndroidNetworking.get(BuildConfig.BASE_URL + Constant.API_PATH + reqUrl + selectedDataId)
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
                                            activity.setActivityCode("Activity");
                                            activity.setActivityId(0L);
                                            activity.setActivityNameEn("Select");
                                            activity.setActivityNameHi("Select");
                                            activityArrayList.add(activity);
                                            for (int i = 0; i < array.length(); i++) {
//                                                Activity activity1 = Activity.parseActivity(array.optJSONObject(i));
//                                                activityArrayList.add(activity1);
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
        } else {
            Schemes schemesOfflines = realm.where(Schemes.class)
                    .equalTo("schemeId", selectedScheme).findFirst();

            if (schemesOfflines != null) {
                realmActivities = schemesOfflines.activityRealmList;


                activityArrayList.clear();
                activityArrayList = new ArrayList<>(realmActivities);

                activitySpnAdapter = new ActivitySpnAdapter(getActivity(), activityArrayList);
                binding.spnActivities.setAdapter(activitySpnAdapter);
                activitySpnAdapter.notifyDataSetChanged();

            }
        }
    }

    @Override
    public void onClfClick(int position) {
        if (status.equals("PENDING")) {
            Intent pendingIntent = new Intent(getActivity(), PendingActivity.class);
            pendingIntent.putExtra("YEAR", String.valueOf(taskList.get(position).year));
            pendingIntent.putExtra("MONTH", String.valueOf(taskList.get(position).month));
            pendingIntent.putExtra("SCHEME", taskList.get(position).schemeName);
            pendingIntent.putExtra("ACTIVITY", taskList.get(position).activityName);
            pendingIntent.putExtra("SCHEME_ID", taskList.get(position).schemeId);
            pendingIntent.putExtra("ACTIVITY_ID", taskList.get(position).activityId);
            pendingIntent.putExtra("STATUS", status);
            pendingIntent.putExtra("ACT_TYPE", selectedActivityType);
            pendingIntent.putExtra("ACT_CODE", activityCode);
            pendingIntent.putExtra("TYPE", "CLF");
            startActivity(pendingIntent);
        }
    }

    @Override
    public void onShgClick(int position) {
        if (status.equals("PENDING")) {
            Intent pendingIntent = new Intent(getActivity(), PendingActivity.class);
            pendingIntent.putExtra("YEAR", String.valueOf(taskList.get(position).year));
            pendingIntent.putExtra("MONTH", String.valueOf(taskList.get(position).month));
            pendingIntent.putExtra("SCHEME", taskList.get(position).schemeName);
            pendingIntent.putExtra("ACTIVITY", taskList.get(position).activityName);
            pendingIntent.putExtra("SCHEME_ID", taskList.get(position).schemeId);
            pendingIntent.putExtra("ACTIVITY_ID", taskList.get(position).activityId);
            pendingIntent.putExtra("STATUS", status);
            pendingIntent.putExtra("ACT_TYPE", selectedActivityType);
            pendingIntent.putExtra("ACT_CODE", activityCode);
            pendingIntent.putExtra("TYPE", "SHG");
            startActivity(pendingIntent);
        }
    }

    @Override
    public void onHhClick(int position) {
        if (status.equals("PENDING")) {
            Intent pendingIntent = new Intent(getActivity(), PendingActivity.class);
            pendingIntent.putExtra("YEAR", String.valueOf(taskList.get(position).year));
            pendingIntent.putExtra("MONTH", String.valueOf(taskList.get(position).month));
            pendingIntent.putExtra("SCHEME", taskList.get(position).schemeName);
            pendingIntent.putExtra("ACTIVITY", taskList.get(position).activityName);
            pendingIntent.putExtra("SCHEME_ID", taskList.get(position).schemeId);
            pendingIntent.putExtra("ACTIVITY_ID", taskList.get(position).activityId);
            pendingIntent.putExtra("STATUS", status);
            pendingIntent.putExtra("ACT_TYPE", selectedActivityType);
            pendingIntent.putExtra("ACT_CODE", activityCode);
            pendingIntent.putExtra("TYPE", "HH");
            startActivity(pendingIntent);
        }
    }

    @Override
    public void onPgClick(int position) {
        if (status.equals("PENDING")) {
            Intent pendingIntent = new Intent(getActivity(), PendingActivity.class);
            pendingIntent.putExtra("YEAR", String.valueOf(taskList.get(position).year));
            pendingIntent.putExtra("MONTH", String.valueOf(taskList.get(position).month));
            pendingIntent.putExtra("SCHEME", taskList.get(position).schemeName);
            pendingIntent.putExtra("ACTIVITY", taskList.get(position).activityName);
            pendingIntent.putExtra("SCHEME_ID", taskList.get(position).schemeId);
            pendingIntent.putExtra("ACTIVITY_ID", taskList.get(position).activityId);
            pendingIntent.putExtra("STATUS", status);
            pendingIntent.putExtra("ACT_TYPE", selectedActivityType);
            pendingIntent.putExtra("ACT_CODE", activityCode);
            pendingIntent.putExtra("TYPE", "PG");
            startActivity(pendingIntent);
        }
    }

    @Override
    public void onEgClick(int position) {
        if (status.equals("PENDING")) {
            Intent pendingIntent = new Intent(getActivity(), PendingActivity.class);
            pendingIntent.putExtra("YEAR", String.valueOf(taskList.get(position).year));
            pendingIntent.putExtra("MONTH", String.valueOf(taskList.get(position).month));
            pendingIntent.putExtra("SCHEME", taskList.get(position).schemeName);
            pendingIntent.putExtra("ACTIVITY", taskList.get(position).activityName);
            pendingIntent.putExtra("SCHEME_ID", taskList.get(position).schemeId);
            pendingIntent.putExtra("ACTIVITY_ID", taskList.get(position).activityId);
            pendingIntent.putExtra("STATUS", status);
            pendingIntent.putExtra("ACT_TYPE", selectedActivityType);
            pendingIntent.putExtra("ACT_CODE", activityCode);
            pendingIntent.putExtra("TYPE", "EG");
            startActivity(pendingIntent);
        }
    }

    @Override
    public void onRevertClick(int position) {
        // decide what to do
    }

    @Override
    public void onApprovedClick(int position) {
        // decide what to do
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

    private void getSchemesWithOfflineData() {

        AndroidNetworking.get(BuildConfig.BASE_URL + Constant.API_PATH + "lov/offline/schemes/activity")
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
//                                        Schemes schemes = new Schemes();
//                                        schemes.setSchemeId(0L);
//                                        schemes.setSchemeCode("Select");
//                                        schemes.setSchemeNameEn("Select Scheme");
//                                        schemes.setSchemeNameHi("Select Scheme");
//                                        RealmList<Activity> activityRealmList = new RealmList<>();
//                                        schemes.setActivityRealmList(activityRealmList);
//                                        schemes.setActive(true);

                                        Schemes schemes = new Schemes();
                                        schemes.schemeNameEn = "Select Scheme";
                                        schemes.schemeId = 0L;
                                        schemes.schemeNameHi = "SelectScheme";
                                        schemes.schemeCode = "SELECT";
                                        schemes.isActive = true;

                                        schemesArrayList.add(schemes);
                                        for (int i = 0; i < array.length(); i++) {
//                                            Schemes scheme = Schemes.parseSchemes(array.optJSONObject(i));
//                                            if (scheme.isActive()) {
//                                                schemesArrayList.add(scheme);
//                                            }
                                            Schemes scheme = Schemes.parseSchemes(array.optJSONObject(i), userType);
                                            if (scheme.isActive) {
                                                schemesArrayList.add(scheme);
                                            }
                                        }

                                        realm.executeTransaction(new Realm.Transaction() {
                                            @Override
                                            public void execute(Realm realm1) {
                                                realm1.delete(Schemes.class);
                                                realm1.delete(Activity.class);
                                                realm1.insertOrUpdate(schemesArrayList);
                                            }
                                        });

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
    public void onOfflineClfClick(int position) {
        if (status.equals("PENDING") && taskRealmResults != null) {
            Intent pendingIntent = new Intent(getActivity(), PendingActivity.class);
            pendingIntent.putExtra("YEAR", String.valueOf(taskRealmResults.get(position).year));
            pendingIntent.putExtra("MONTH", String.valueOf(taskRealmResults.get(position).month));
            pendingIntent.putExtra("SCHEME", taskRealmResults.get(position).schemeName);
            pendingIntent.putExtra("ACTIVITY_NAME", taskRealmResults.get(position).activityName);
            pendingIntent.putExtra("SCHEME_ID", taskRealmResults.get(position).schemeId);
            pendingIntent.putExtra("ACTIVITY_ID", taskRealmResults.get(position).activityId);
            pendingIntent.putExtra("STATUS", status);
            pendingIntent.putExtra("ACT_TYPE", selectedActivityType);
            pendingIntent.putExtra("ACTIVITY_CODE", activityCode);
            pendingIntent.putExtra("SCHEME_CODE", schemeCode);
            pendingIntent.putExtra("TYPE", "CLF");
            startActivity(pendingIntent);
        }
    }

    @Override
    public void onOfflineShgClick(int position)
    {
        if (status.equals("PENDING") && taskRealmResults != null) {
            Intent pendingIntent = new Intent(getActivity(), PendingActivity.class);
            pendingIntent.putExtra("YEAR", String.valueOf(taskRealmResults.get(position).year));
            pendingIntent.putExtra("MONTH", String.valueOf(taskRealmResults.get(position).month));
            pendingIntent.putExtra("SCHEME", taskRealmResults.get(position).schemeName);
            pendingIntent.putExtra("ACTIVITY_NAME", taskRealmResults.get(position).activityName);
            pendingIntent.putExtra("SCHEME_ID", taskRealmResults.get(position).schemeId);
            pendingIntent.putExtra("ACTIVITY_ID", taskRealmResults.get(position).activityId);
            pendingIntent.putExtra("STATUS", status);
            pendingIntent.putExtra("ACT_TYPE", selectedActivityType);
            pendingIntent.putExtra("ACTIVITY_CODE", activityCode);
            pendingIntent.putExtra("SCHEME_CODE", schemeCode);
            pendingIntent.putExtra("ENTITY_CODE", "SHG");
            startActivity(pendingIntent);
        }
    }

    @Override
    public void onOfflineHhClick(int position) {
        if (status.equals("PENDING") && taskRealmResults != null) {
            Intent pendingIntent = new Intent(getActivity(), PendingActivity.class);
            pendingIntent.putExtra("YEAR", String.valueOf(taskRealmResults.get(position).year));
            pendingIntent.putExtra("MONTH", String.valueOf(taskRealmResults.get(position).month));
            pendingIntent.putExtra("SCHEME", taskRealmResults.get(position).schemeName);
            pendingIntent.putExtra("ACTIVITY_NAME", taskRealmResults.get(position).activityName);
            pendingIntent.putExtra("SCHEME_ID", taskRealmResults.get(position).schemeId);
            pendingIntent.putExtra("ACTIVITY_ID", taskRealmResults.get(position).activityId);
            pendingIntent.putExtra("STATUS", status);
            pendingIntent.putExtra("ACT_TYPE", selectedActivityType);
            pendingIntent.putExtra("ACTIVITY_CODE", activityCode);
            pendingIntent.putExtra("SCHEME_CODE", schemeCode);
            pendingIntent.putExtra("ENTITY_CODE", "HH");
            startActivity(pendingIntent);
        }
    }

    @Override
    public void onOfflinePgClick(int position) {
        if (status.equals("PENDING") && taskRealmResults != null) {
            Intent pendingIntent = new Intent(getActivity(), PendingActivity.class);
            pendingIntent.putExtra("YEAR", String.valueOf(taskRealmResults.get(position).year));
            pendingIntent.putExtra("MONTH", String.valueOf(taskRealmResults.get(position).month));
            pendingIntent.putExtra("SCHEME", taskRealmResults.get(position).schemeName);
            pendingIntent.putExtra("ACTIVITY_NAME", taskRealmResults.get(position).activityName);
            pendingIntent.putExtra("SCHEME_ID", taskRealmResults.get(position).schemeId);
            pendingIntent.putExtra("ACTIVITY_ID", taskRealmResults.get(position).activityId);
            pendingIntent.putExtra("STATUS", status);
            pendingIntent.putExtra("ACT_TYPE", selectedActivityType);
            pendingIntent.putExtra("ACTIVITY_CODE", activityCode);
            pendingIntent.putExtra("SCHEME_CODE", schemeCode);
            pendingIntent.putExtra("ENTITY_CODE", "PG");
            startActivity(pendingIntent);
        }
    }

    @Override
    public void onOfflineEgClick(int position) {
        if (status.equals("PENDING") && taskRealmResults != null) {
            Intent pendingIntent = new Intent(getActivity(), PendingActivity.class);
            pendingIntent.putExtra("YEAR", String.valueOf(taskRealmResults.get(position).year));
            pendingIntent.putExtra("MONTH", String.valueOf(taskRealmResults.get(position).month));
            pendingIntent.putExtra("SCHEME", taskRealmResults.get(position).schemeName);
            pendingIntent.putExtra("ACTIVITY_NAME", taskRealmResults.get(position).activityName);
            pendingIntent.putExtra("SCHEME_ID", taskRealmResults.get(position).schemeId);
            pendingIntent.putExtra("ACTIVITY_ID", taskRealmResults.get(position).activityId);
            pendingIntent.putExtra("STATUS", status);
            pendingIntent.putExtra("ACT_TYPE", selectedActivityType);
            pendingIntent.putExtra("ACTIVITY_CODE", activityCode);
            pendingIntent.putExtra("SCHEME_CODE", schemeCode);
            pendingIntent.putExtra("ENTITY_CODE", "EG");
            startActivity(pendingIntent);
        }
    }

    private void getActivityFromRealmWithSchemeId(Long selectedScheme) {

        Schemes schemesOfflines = realm.where(Schemes.class)
                .equalTo("schemeId", selectedScheme).findFirst();

        Log.i(TAG, "getActivityFromRealmWithSchemeId: " + schemesOfflines.schemeId);

        if (userType.equals(Constant.PMITRA)) {
            if (schemesOfflines != null) {
                pmActivitiesOfflines = schemesOfflines.activityRealmList;
                Log.i(TAG, "getActivityFromRealmWithSchemeId: " + pmActivitiesOfflines.size());


                activityArrayList.clear();
                activityArrayList = new ArrayList<>(pmActivitiesOfflines);
//                    activityArrayList.add(activity);
                activitySpnAdapter = new ActivitySpnAdapter(getActivity(), activityArrayList);
                binding.spnActivities.setAdapter(activitySpnAdapter);
                activitySpnAdapter.notifyDataSetChanged();
            }
        }

    }

    private void getActivityTypeFromRealmWithSchemeId(Long selectedScheme) {

        Schemes schemesOfflines = realm.where(Schemes.class)
                .equalTo("schemeId", selectedScheme).findFirst();

        Log.i(TAG, "getActivityFromRealmWithSchemeId: " + schemesOfflines.schemeId);

        activityTypeOfflines = realm.where(ActivityType.class).findAll();
        activityTypeArrayList = new ArrayList<>(activityTypeOfflines);
        activityTypeSpnAdapter = new ActivityTypeSpnAdapter(getActivity(), activityTypeArrayList);
        binding.spnActType.setAdapter(activityTypeSpnAdapter);
        activityTypeSpnAdapter.notifyDataSetChanged();


//        if (schemesOfflines != null) {
//            pmActivitiesOfflines = schemesOfflines.activityRealmList;
//            Log.i(TAG, "getActivityFromRealmWithSchemeId: " + pmActivitiesOfflines.size());
//
//
//            activityArrayList.clear();
//            activityArrayList = new ArrayList<>(pmActivitiesOfflines);
////                    activityArrayList.add(activity);
//            activitySpnAdapter = new ActivitySpnAdapter(getActivity(), activityArrayList);
//            binding.spnActivities.setAdapter(activitySpnAdapter);
//            activitySpnAdapter.notifyDataSetChanged();
//        }


    }

    private void loadOfflineListList(RealmList<ShgList> shgListRealmResults) {
//        binding.progress.setVisibility(View.GONE);
        binding.rvApprovals.setVisibility(View.GONE);
        if (shgListRealmResults.size() == 0) {
            binding.rvApprovals.setVisibility(View.GONE);
        } else {
            binding.rvApprovals.setVisibility(View.VISIBLE);
        }
        if (shgListRealmResults != null && shgListRealmResults.size() > 0) {
//            offShgActivityListAdapter = new OffShgActivityListAdapter(getContext(), shgListRealmResults);
//            offShgActivityListAdapter.setActivityDetailsListener(this);
//            offShgActivityListAdapter.notifyDataSetChanged();
//            mRecyclerView.setAdapter(offShgActivityListAdapter);


            taskRealmResults = realm.where(Task.class)
                    .equalTo("schemeId", selectedScheme)
                    .equalTo("activityId", selectedActivity)
                    .findAll();


            offlineAdapter = new PendingOfflineAdapter(getActivity(), taskRealmResults, userType);
            offlineAdapter.setOnPendingClickListener(this);
            binding.rvApprovals.setAdapter(offlineAdapter);
            offlineAdapter.notifyDataSetChanged();


        }
    }


    private void getKMShgOfflineList() {


        if (selectedMonth.equals("ALL")) {
            taskRealmResults = realm.where(Task.class)
                    .equalTo("schemeId", selectedScheme)
                    .equalTo("activityId", selectedActivity)
                    .findAll();
        } else {

            switch (selectedMonth) {
                case "JAN":
                    month = "1";
                    break;
                case "FEB":
                    month = "2";
                    break;
                case "MAR":
                    month = "3";
                    break;
                case "APR":
                    month = "4";
                    break;
                case "MAY":
                    month = "5";
                    break;
                case "JUN":
                    month = "6";
                    break;
                case "JUL":
                    month = "7";
                    break;
                case "AUG":
                    month = "8";
                    break;
                case "SEP":
                    month = "9";
                    break;
                case "OCT":
                    month = "10";
                    break;
                case "NOV":
                    month = "11";
                    break;
                case "DEC":
                    month = "12";
                    break;
            }

            taskRealmResults = realm.where(Task.class)
                    .equalTo("schemeId", selectedScheme)
                    .equalTo("activityId", selectedActivity)
                    .equalTo("month", Integer.parseInt(month))
//                    .equalTo("year", Integer.parseInt(year))
                    .findAll();
        }

        if (taskRealmResults != null) {
            offlineAdapter = new PendingOfflineAdapter(getActivity(), taskRealmResults, userType);
            offlineAdapter.setOnPendingClickListener(this);
            binding.rvApprovals.setAdapter(offlineAdapter);
            offlineAdapter.notifyDataSetChanged();


            binding.rvApprovals.setVisibility(View.GONE);
            if (taskRealmResults.size() == 0) {
                binding.rvApprovals.setVisibility(View.GONE);
            } else {
                binding.rvApprovals.setVisibility(View.VISIBLE);
            }
        }
    }
}
