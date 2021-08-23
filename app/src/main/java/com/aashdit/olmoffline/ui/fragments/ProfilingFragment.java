package com.aashdit.olmoffline.ui.fragments;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.aashdit.olmoffline.BuildConfig;
import com.aashdit.olmoffline.R;
import com.aashdit.olmoffline.adapters.ProfilingAdapter;
import com.aashdit.olmoffline.databinding.FragmentProfilingBinding;
import com.aashdit.olmoffline.db.Dashboard;
import com.aashdit.olmoffline.db.hh.ActivityHHSearch;
import com.aashdit.olmoffline.db.hh.HHSHGList;
import com.aashdit.olmoffline.db.hh.HhList;
import com.aashdit.olmoffline.db.hh.SchemeHHSearch;
import com.aashdit.olmoffline.db.shg.ActivitySHGSearch;
import com.aashdit.olmoffline.db.shg.SchemeSHGSearch;
import com.aashdit.olmoffline.db.shg.ShgList;
import com.aashdit.olmoffline.models.Activity;
import com.aashdit.olmoffline.models.ActivityType;
import com.aashdit.olmoffline.models.Profiling;
import com.aashdit.olmoffline.models.Schemes;
import com.aashdit.olmoffline.receiver.ConnectivityChangeReceiver;
import com.aashdit.olmoffline.ui.MainActivity;
import com.aashdit.olmoffline.ui.activities.SHGListActivity;
import com.aashdit.olmoffline.ui.dialog.CustomProgressDialogue;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import okhttp3.OkHttpClient;

public class ProfilingFragment extends Fragment implements ProfilingAdapter.ProfilingListener,
        ConnectivityChangeReceiver.ConnectivityReceiverListener {

    private static final String TAG = "ProfilingFragment";
    String level = "";
    String levelName = "";
    RealmResults<Schemes> schemesRealmList;
    RealmResults<Activity> activityRealmResults;

    //    private TextView mTvTitleProfiling;
    private RecyclerView mRvProfilingDashboard;
    private ProfilingAdapter profilingAdapter;
    private List<Profiling> profilingList;
    private RelativeLayout rlLabel;
    private SharedPrefManager sp;
    private String token;
    private FragmentProfilingBinding binding;
    private CustomProgressDialogue progressDialogue;
    private Dashboard dashboards;
    private Realm realm;
    private boolean isShgList, isHhLst;

    /**
     * for KM user isSchemeLoading,isShgListLoading,isHhListLoading
     */

    private boolean isSchemeLoading, isShgListLoading, isHhListLoading;

    /**
     * @param isConnected for checking network connectivity
     */
    private boolean isConnected = false;
    private ConnectivityChangeReceiver mConnectivityChangeReceiver;
    //    private ArrayList<Schemes> schemesArrayList = new ArrayList<>();
    private ArrayList<Activity> activityArrayList = new ArrayList<>();
    private ArrayList<ActivityType> activityTypeArrayList = new ArrayList<>();
    private ArrayList<SchemeSHGSearch> schemeSearches = new ArrayList<>();
    private ArrayList<SchemeHHSearch> schemeHHSearches = new ArrayList<>();
    private String userType;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfilingBinding.inflate(inflater);
        realm = Realm.getDefaultInstance();
//        return inflater.inflate(R.layout.fragment_profiling, container, false);


        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.MINUTES)
                .readTimeout(10, TimeUnit.MINUTES)
                .writeTimeout(10, TimeUnit.MINUTES)
                .build();
        AndroidNetworking.initialize(getContext(), okHttpClient);


        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        mTvTitleProfiling = view.findViewById(R.id.tv_title);

        mConnectivityChangeReceiver = new ConnectivityChangeReceiver();
        mConnectivityChangeReceiver.setConnectivityReceiverListener(this);
        isConnected = mConnectivityChangeReceiver.getConnectionStatus(getContext());
        registerNetworkBroadcast();
        binding.progressCircular.setVisibility(View.GONE);
        sp = SharedPrefManager.getInstance(getActivity());
        token = sp.getStringData(Constant.APP_TOKEN);
        userType = sp.getStringData(Constant.USER_ROLE);
        mRvProfilingDashboard = view.findViewById(R.id.rv_profiling_dashboard);
        mRvProfilingDashboard.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        rlLabel = view.findViewById(R.id.rl_label_pm);
        profilingList = new ArrayList<>();

        Profiling blf = new Profiling("BLF", "100000", "#B50003", "#FFE5E6");
        Profiling clf = new Profiling("CLF", "100000", "#08A0F6", "#DCF2FF");
        Profiling shg = new Profiling("SHG", "100000", "#7F62EA", "#F3F0FF");
        Profiling hh = new Profiling("HH", "100000", "#FF6937", "#FFF1EC");
//        Profiling ccp = new Profiling("CCP", "100000", "#08A0F6", "#DCF2FF");

        profilingList.add(blf);
        profilingList.add(clf);
        profilingList.add(shg);
        profilingList.add(hh);
//        profilingList.add(ccp);

        if (sp.getStringData(Constant.USER_ROLE).equals("ROLE_CRPCM")) {

            profilingAdapter = new ProfilingAdapter(getActivity(), profilingList);
            profilingAdapter.setProfilingListener(this);
            mRvProfilingDashboard.setAdapter(profilingAdapter);
            rlLabel.setVisibility(View.GONE);
        } else {
            rlLabel.setVisibility(View.VISIBLE);
            binding.swipeRefreshDashboard.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    getDashboardData();
                }
            });
            if (sp.getStringData(Constant.USER_ROLE).equals("ROLE_UMITRA")) {
                binding.llTitlesContainer.setVisibility(View.GONE);
                binding.tvVillageLbl.setVisibility(View.GONE);
                binding.tvVillage.setVisibility(View.GONE);
                binding.llPgContainer.setVisibility(View.VISIBLE);

                binding.cvPg.setVisibility(View.VISIBLE);
                binding.cvEg.setVisibility(View.GONE);
                binding.cvShg.setVisibility(View.GONE);
                binding.cvHh.setVisibility(View.GONE);

            } else if (sp.getStringData(Constant.USER_ROLE).equals("ROLE_CRPEP")) {
                binding.llTitlesContainer.setVisibility(View.GONE);
                binding.tvVillageLbl.setVisibility(View.GONE);
                binding.tvVillage.setVisibility(View.GONE);
                binding.llPgContainer.setVisibility(View.VISIBLE);

                binding.cvPg.setVisibility(View.GONE);
                binding.cvEg.setVisibility(View.VISIBLE);
                binding.cvShg.setVisibility(View.VISIBLE);
                binding.cvHh.setVisibility(View.VISIBLE);
            } else {
                binding.llTitlesContainerUm.setVisibility(View.GONE);
                binding.llPgContainer.setVisibility(View.GONE);
            }

            if (isConnected && dashboards == null)
                getDashboardData();

            schemesRealmList = realm.where(Schemes.class).findAll();
            activityRealmResults = realm.where(Activity.class).findAll();
            progressDialogue = new CustomProgressDialogue(getContext());
            binding.tvSyncFromServer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isConnected) {
                        progressDialogue.show();
//                         getSchemesWithOfflineData();
                        if (sp.getStringData(Constant.USER_ROLE).equals(Constant.PMITRA)) {
                            getShgListData();
                            getHhListData();
                        } else if (sp.getStringData(Constant.USER_ROLE).equals(Constant.KMITRA)) {
                            //Todo call km shg list & km hh list
                            getSchemesWrtActivityByType();
                            getFarmingShgListData();
                            getFarmingHHListData();

                        } else if (sp.getStringData(Constant.USER_ROLE).equals(Constant.UMITRA)) {
                            //Todo call um shg list & km hh list
                            getSchemesWrtActivityByType();
                            getNonFarmingShgListData();
                            getNonFarmingHHListData();

                        }
                    } else {
                        Toast.makeText(getActivity(), "Make sure you have an active internet connection to sync from server", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            if (realm != null) {
                dashboards = realm.where(Dashboard.class).findFirst();

                if (dashboards != null) {
                    binding.tvTotPgReported.setText(String.valueOf(dashboards.pgReported));
                    binding.tvTotEgReported.setText(String.valueOf(dashboards.egReported));
                    binding.tvPgLbl.setText(dashboards.label.concat(" : "));
                    binding.tvPg.setText(dashboards.labelValue);
                    binding.tvDistrict.setText(dashboards.district);
                    binding.tvBlock.setText(dashboards.block);
                    binding.tvGp.setText(dashboards.gp);
                    binding.tvVillage.setText(dashboards.villages);
                    binding.tvUserName.setText(dashboards.userName);
                    binding.tvTotShgReported.setText(String.valueOf(dashboards.shgReported));
                    binding.tvTotShgReportedUm.setText(String.valueOf(dashboards.shgReported));
                    binding.tvTotHhReportedUm.setText(String.valueOf(dashboards.hhReported));
                    binding.tvTotHhReported.setText(String.valueOf(dashboards.hhReported));
                    binding.tvTotClfReported.setText(String.valueOf(dashboards.clfReported));
                    binding.tvTotActCovvered.setText(String.valueOf(dashboards.activitiesCovered));
                }
            }
        }
    }


    /**
     * @method to get HH List data for UM activities
     */
    private void getNonFarmingHHListData() {
        binding.progressCircular.setVisibility(View.VISIBLE);
        AndroidNetworking.get(BuildConfig.BASE_URL + Constant.API_PATH + "nonfarm/offline/search/hh")
                .addHeaders("Authorization", "Bearer " + token)
                .setTag("FarmingHhList")
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
//                        progressDialogue.hide();
                        binding.progressCircular.setVisibility(View.GONE);
                        if (Utility.isStringValid(response)) {

//                            isShgList = true;
                            isHhListLoading = true;
                            try {
                                JSONObject object = new JSONObject(response);
                                if (object.optBoolean("outcome")) {

                                    dismissProgressDialog();
                                    Toast.makeText(getActivity(), "Sync Successfully", Toast.LENGTH_SHORT).show();

                                    JSONArray array = object.optJSONArray("data");
                                    if (array != null && array.length() > 0) {
                                        schemeSearches.clear();

                                        for (int i = 0; i < array.length(); i++) {
                                            SchemeHHSearch scheme = SchemeHHSearch.parseSchemeSearch(array.optJSONObject(i), userType,"hh");
                                            if (scheme.isActive) {
                                                schemeHHSearches.add(scheme);
                                            }
                                        }

                                        realm.executeTransaction(new Realm.Transaction() {
                                            @Override
                                            public void execute(Realm realm1) {
                                                realm1.delete(SchemeHHSearch.class);
                                                realm1.delete(ActivityHHSearch.class);
                                                realm1.delete(HHSHGList.class);
                                                realm1.delete(HhList.class);
                                                realm1.insertOrUpdate(schemeHHSearches);
                                            }
                                        });
                                    }
                                }


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
//                        progressDialogue.hide();
                        binding.progressCircular.setVisibility(View.GONE);
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

    /**
     * @method to get HH List data for KM activities
     */
    private void getFarmingHHListData() {
        binding.progressCircular.setVisibility(View.VISIBLE);
        AndroidNetworking.get(BuildConfig.BASE_URL + Constant.API_PATH + "farming/offline/search/hh")
                .addHeaders("Authorization", "Bearer " + token)
                .setTag("FarmingHhList")
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
//                        progressDialogue.hide();
                        binding.progressCircular.setVisibility(View.GONE);
                        if (Utility.isStringValid(response)) {

//                            isShgList = true;
                            isHhListLoading = true;
                            try {
                                JSONObject object = new JSONObject(response);
                                if (object.optBoolean("outcome")) {

                                    dismissProgressDialog();
                                    Toast.makeText(getActivity(), "Sync Successfully", Toast.LENGTH_SHORT).show();

                                    JSONArray array = object.optJSONArray("data");
                                    if (array != null && array.length() > 0) {
                                        schemeSearches.clear();

                                        for (int i = 0; i < array.length(); i++) {
                                            SchemeHHSearch scheme = SchemeHHSearch.parseSchemeSearch(array.optJSONObject(i), userType,"hh");
                                            if (scheme.isActive) {
                                                schemeHHSearches.add(scheme);
                                            }
                                        }

                                        realm.executeTransaction(new Realm.Transaction() {
                                            @Override
                                            public void execute(Realm realm1) {
                                                realm1.delete(SchemeHHSearch.class);
                                                realm1.delete(ActivityHHSearch.class);
                                                realm1.delete(HHSHGList.class);
                                                realm1.delete(HhList.class);
                                                realm1.insertOrUpdate(schemeHHSearches);
                                            }
                                        });
                                    }
                                }


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
//                        progressDialogue.hide();
                        binding.progressCircular.setVisibility(View.GONE);
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


    /**
     * @method to get SHG List data for UM activities
     */
    private void getNonFarmingShgListData() {
        binding.progressCircular.setVisibility(View.VISIBLE);
        AndroidNetworking.get(BuildConfig.BASE_URL + Constant.API_PATH + "nonfarm/offline/search/shg")
                .addHeaders("Authorization", "Bearer " + token)
                .setTag("FarmingShgList")
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        binding.progressCircular.setVisibility(View.GONE);
                        if (Utility.isStringValid(response)) {

                            isShgListLoading = true;
                            try {
                                JSONObject object = new JSONObject(response);
                                if (object.optBoolean("outcome")) {

                                    dismissProgressDialog();
                                    Toast.makeText(getActivity(), "Sync Successfully", Toast.LENGTH_SHORT).show();

                                    JSONArray array = object.optJSONArray("data");
                                    if (array != null && array.length() > 0) {
                                        schemeSearches.clear();

                                        for (int i = 0; i < array.length(); i++) {
                                            SchemeSHGSearch scheme = SchemeSHGSearch.parseSchemeSearch(array.optJSONObject(i), userType, "shg");
                                            if (scheme.isActive) {
                                                schemeSearches.add(scheme);
                                            }
                                        }

                                        realm.executeTransaction(new Realm.Transaction() {
                                            @Override
                                            public void execute(Realm realm1) {
                                                realm1.delete(SchemeSHGSearch.class);
                                                realm1.delete(ActivitySHGSearch.class);
                                                realm1.delete(ShgList.class);
                                                realm1.insertOrUpdate(schemeSearches);

                                            }
                                        });
                                    }
                                }


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
//                        progressDialogue.hide();
                        binding.progressCircular.setVisibility(View.GONE);
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


    /**
     * @method to get SHG List data for KM activities
     */
    private void getFarmingShgListData() {
        binding.progressCircular.setVisibility(View.VISIBLE);
        AndroidNetworking.get(BuildConfig.BASE_URL + Constant.API_PATH + "farming/offline/search/shg")
                .addHeaders("Authorization", "Bearer " + token)
                .setTag("FarmingShgList")
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        binding.progressCircular.setVisibility(View.GONE);
                        if (Utility.isStringValid(response)) {

                            isShgListLoading = true;
                            try {
                                JSONObject object = new JSONObject(response);
                                if (object.optBoolean("outcome")) {

                                    dismissProgressDialog();
                                    Toast.makeText(getActivity(), "Sync Successfully", Toast.LENGTH_SHORT).show();

                                    JSONArray array = object.optJSONArray("data");
                                    if (array != null && array.length() > 0) {
                                        schemeSearches.clear();

                                        for (int i = 0; i < array.length(); i++) {
                                            SchemeSHGSearch scheme = SchemeSHGSearch.parseSchemeSearch(array.optJSONObject(i), userType, "shg");
                                            if (scheme.isActive) {
                                                schemeSearches.add(scheme);
                                            }
                                        }

                                        realm.executeTransaction(new Realm.Transaction() {
                                            @Override
                                            public void execute(Realm realm1) {
                                                realm1.delete(SchemeSHGSearch.class);
                                                realm1.delete(ActivitySHGSearch.class);
                                                realm1.delete(ShgList.class);
                                                realm1.insertOrUpdate(schemeSearches);

                                            }
                                        });
                                    }
                                }


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
//                        progressDialogue.hide();
                        binding.progressCircular.setVisibility(View.GONE);
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


    /**
     * @method to get Dropdown data for KM,UM,CRPEP user
     */
    private void getSchemesWrtActivityByType() {
        ArrayList<Schemes> schemesArrayList = new ArrayList<>();
        AndroidNetworking.get(BuildConfig.BASE_URL + Constant.API_PATH + "lov/offline/schemes/activityByType")
                .addHeaders("Authorization", "Bearer " + token)
                .setTag("Activity")
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        if (Utility.isStringValid(response)) {
                            isSchemeLoading = true;
                            try {
                                JSONObject resObject = new JSONObject(response);
                                if (resObject.optBoolean("outcome")) {
                                    JSONArray resDataArray = resObject.optJSONArray("data");
                                    if (resDataArray != null && resDataArray.length() > 0) {
                                        Schemes schemes = new Schemes();
                                        schemes.setSchemeId(0L);
                                        schemes.setSchemeCode("SELECT");
                                        schemes.setSchemeNameEn("Select a Scheme");
                                        schemes.setSchemeNameHi("");
                                        schemes.setActive(true);
                                        schemesArrayList.add(schemes);

                                        for (int i = 0; i < resDataArray.length(); i++) {
                                            schemesArrayList.add(Schemes.parseSchemes(resDataArray.optJSONObject(i), userType));
                                        }

                                        realm.executeTransaction(new Realm.Transaction() {
                                            @Override
                                            public void execute(Realm realm1) {
                                                realm1.delete(Schemes.class);
                                                realm1.delete(ActivityType.class);
                                                realm1.delete(Activity.class);

                                                realm1.insertOrUpdate(schemesArrayList);

                                                dismissProgressDialog();

                                            }
                                        });
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        progressDialogue.dismiss();
                        Log.e(TAG, "onError: " + anError.getErrorDetail());
                    }
                });
    }


    /**
     * @method to dismiss loader
     */
    private void dismissProgressDialog() {
        if (isSchemeLoading && isShgListLoading && isHhListLoading) {
            progressDialogue.dismiss();
        }
    }


    /**
     * @method to get Dashboard data
     */

    private void getDashboardData() {
        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "dashboard")
                .addHeaders("Authorization", "Bearer " + token)
                .setTag("Activity")
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        if (Utility.isStringValid(response)) {
                            binding.swipeRefreshDashboard.setRefreshing(false);
                            try {
                                JSONObject resObject = new JSONObject(response);
                                if (resObject.optBoolean("outcome")) {
                                    if (resObject.optJSONObject("data") != null) {
                                        String district = Objects.requireNonNull(resObject.optJSONObject("data")).optString("district");
                                        String block = Objects.requireNonNull(resObject.optJSONObject("data")).optString("block");
                                        String gp = Objects.requireNonNull(resObject.optJSONObject("data")).optString("gp");
                                        String villages = Objects.requireNonNull(resObject.optJSONObject("data")).optString("villages").equals("null") ?
                                                "N/A" : Objects.requireNonNull(resObject.optJSONObject("data")).optString("villages");
                                        level = Objects.requireNonNull(resObject.optJSONObject("data")).optString("label");
                                        levelName = Objects.requireNonNull(resObject.optJSONObject("data")).optString("labelValue");
                                        String userName = Objects.requireNonNull(resObject.optJSONObject("data")).optString("userName");
                                        String lastLogin = Objects.requireNonNull(resObject.optJSONObject("data")).optString("lastLogin");
                                        Long clfReported = Objects.requireNonNull(resObject.optJSONObject("data")).optLong("clfReported");
                                        Long pgReported = Objects.requireNonNull(resObject.optJSONObject("data")).optLong("pgReported");
                                        Long egReported = Objects.requireNonNull(resObject.optJSONObject("data")).optLong("egReported");
                                        Long shgReported = Objects.requireNonNull(resObject.optJSONObject("data")).optLong("shgReported");
                                        Long hhReported = Objects.requireNonNull(resObject.optJSONObject("data")).optLong("hhReported");
                                        Long activitiesCovered = Objects.requireNonNull(resObject.optJSONObject("data")).optLong("activitiesCovered");

                                        Dashboard dashboard = new Dashboard();
                                        dashboard.district = district;
                                        dashboard.block = block;
                                        dashboard.gp = gp;
                                        dashboard.villages = villages;
                                        dashboard.label = level;
                                        dashboard.labelValue = levelName;
                                        dashboard.userName = userName;
                                        dashboard.lastLogin = lastLogin;
                                        dashboard.clfReported = clfReported;
                                        dashboard.pgReported = pgReported;
                                        dashboard.egReported = egReported;
                                        dashboard.shgReported = shgReported;
                                        dashboard.hhReported = hhReported;
                                        dashboard.activitiesCovered = activitiesCovered;

                                        binding.tvTotPgReported.setText(String.valueOf(pgReported));
                                        binding.tvTotEgReported.setText(String.valueOf(egReported));
                                        binding.tvPgLbl.setText(level.concat(" : "));
                                        binding.tvPg.setText(levelName);
                                        binding.tvDistrict.setText(district);
                                        binding.tvBlock.setText(block);
                                        binding.tvGp.setText(gp);
                                        binding.tvVillage.setText(villages);
                                        binding.tvUserName.setText(userName);
                                        binding.tvTotShgReported.setText(String.valueOf(shgReported));
                                        binding.tvTotShgReportedUm.setText(String.valueOf(shgReported));
                                        binding.tvTotHhReportedUm.setText(String.valueOf(hhReported));
                                        binding.tvTotHhReported.setText(String.valueOf(hhReported));
                                        binding.tvTotClfReported.setText(String.valueOf(clfReported));
                                        binding.tvTotActCovvered.setText(String.valueOf(activitiesCovered));

                                        realm.executeTransaction(new Realm.Transaction() {
                                            @Override
                                            public void execute(Realm realm1) {
                                                realm1.delete(Dashboard.class);
                                                realm1.insertOrUpdate(dashboard);
                                            }
                                        });

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


    /**
     * @method to get SHG List data for Livestock activities
     */
    private void getShgListData() {

        ArrayList<Schemes> schemesArrayList = new ArrayList<>();
        binding.progressCircular.setVisibility(View.VISIBLE);
        AndroidNetworking.get(BuildConfig.BASE_URL + Constant.API_PATH + "livestock/offline/search/shg")
                .addHeaders("Authorization", "Bearer " + token)
                .setTag("Schemes")
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
//                        progressDialogue.hide();
                        binding.progressCircular.setVisibility(View.GONE);
                        if (Utility.isStringValid(response)) {

                            isShgList = true;
                            try {
                                JSONObject object = new JSONObject(response);
                                if (object.optBoolean("outcome")) {
                                    if (isShgList && isHhLst) {
                                        progressDialogue.hide();
                                        Toast.makeText(getActivity(), "Sync Successfully", Toast.LENGTH_SHORT).show();
                                    }
                                    JSONArray array = object.optJSONArray("data");
                                    if (array != null && array.length() > 0) {
                                        schemeSearches.clear();

                                        for (int i = 0; i < array.length(); i++) {
                                            SchemeSHGSearch scheme = SchemeSHGSearch.parseSchemeSearch(array.optJSONObject(i), userType, "shg");
                                            if (scheme.isActive) {
                                                schemeSearches.add(scheme);
                                            }
                                        }


                                        schemesArrayList.clear();
                                        Schemes schemes = new Schemes();
                                        schemes.setSchemeId(0L);
                                        schemes.setSchemeCode("Select");
                                        schemes.setSchemeNameEn("Select Scheme");
                                        schemes.setSchemeNameHi("Select Scheme");
                                        RealmList<Activity> activityRealmList = new RealmList<>();
                                        schemes.setActivityRealmList(activityRealmList);
                                        schemes.setActive(true);

//                                        PMSchemesOffline schemes = new PMSchemesOffline();
//                                        schemes.schemeNameEn = "Select Scheme";
//                                        schemes.schemeId = 0L;
//                                        schemes.schemeNameHi = "SelectScheme";
//                                        schemes.schemeCode = "SELECT";
//                                        schemes.isActive = true;

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
                                                realm1.delete(SchemeSHGSearch.class);
                                                realm1.delete(ActivitySHGSearch.class);
                                                realm1.delete(ShgList.class);
                                                realm1.insertOrUpdate(schemesArrayList);
                                                realm1.insertOrUpdate(schemeSearches);
                                            }
                                        });
                                    }
                                }


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
//                        progressDialogue.hide();
                        binding.progressCircular.setVisibility(View.GONE);
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


    /**
     * @method to get HH List data for Livestock activities
     */
    private void getHhListData() {

        ArrayList<Schemes> schemesArrayList = new ArrayList<>();
        binding.progressCircular.setVisibility(View.VISIBLE);
        AndroidNetworking.get(BuildConfig.BASE_URL + Constant.API_PATH + "livestock/offline/search/hh")
                .addHeaders("Authorization", "Bearer " + token)
                .setTag("Schemes")
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
//                        progressDialogue.hide();
                        binding.progressCircular.setVisibility(View.GONE);
                        if (Utility.isStringValid(response)) {
                            isHhLst = true;
                            try {
                                JSONObject object = new JSONObject(response);
                                if (object.optBoolean("outcome")) {

                                    if (isShgList && isHhLst) {
                                        progressDialogue.hide();
                                        Toast.makeText(getActivity(), "Sync Successfully", Toast.LENGTH_SHORT).show();
                                    }

                                    JSONArray array = object.optJSONArray("data");
                                    if (array != null && array.length() > 0) {
                                        schemeHHSearches.clear();

                                        for (int i = 0; i < array.length(); i++) {
                                            SchemeHHSearch scheme = SchemeHHSearch.parseSchemeSearch(array.optJSONObject(i), userType,"hh");
                                            if (scheme.isActive) {
                                                schemeHHSearches.add(scheme);
                                            }
                                        }

                                        schemesArrayList.clear();
                                        Schemes schemes = new Schemes();
                                        schemes.setSchemeId(0L);
                                        schemes.setSchemeCode("Select");
                                        schemes.setSchemeNameEn("Select Scheme");
                                        schemes.setSchemeNameHi("Select Scheme");
                                        RealmList<Activity> activityRealmList = new RealmList<>();
                                        schemes.setActivityRealmList(activityRealmList);
                                        schemes.setActive(true);

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
                                                realm1.delete(SchemeHHSearch.class);
                                                realm1.delete(ActivityHHSearch.class);
                                                realm1.delete(HHSHGList.class);
                                                realm1.delete(HhList.class);
                                                realm1.insertOrUpdate(schemesArrayList);
                                                realm1.insertOrUpdate(schemeHHSearches);
                                            }
                                        });
                                    }
                                }


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
//                        progressDialogue.hide();
                        binding.progressCircular.setVisibility(View.GONE);
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
    public void onProfileItemClick(int position) {
        if (sp.getStringData(Constant.USER_ROLE).equals("ROLE_CRPCM")) {
            if (position == 2) {
                Intent intent = new Intent(getActivity(), SHGListActivity.class);
                startActivity(intent);
            }
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
}
