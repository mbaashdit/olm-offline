package com.aashdit.olmoffline.ui.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.aashdit.olmoffline.BuildConfig;
import com.aashdit.olmoffline.R;
import com.aashdit.olmoffline.adapters.PendingItemsAdapter;
import com.aashdit.olmoffline.adapters.PendingOfflineItemsAdapter;
import com.aashdit.olmoffline.databinding.ActivityPendingBinding;
import com.aashdit.olmoffline.db.DairyReportLine;
import com.aashdit.olmoffline.db.FarmReportLine;
import com.aashdit.olmoffline.db.FisheryReportLine;
import com.aashdit.olmoffline.db.GoatryReportLine;
import com.aashdit.olmoffline.db.PoultryReportLine;
import com.aashdit.olmoffline.models.Task;
import com.aashdit.olmoffline.models.TaskListItem;
import com.aashdit.olmoffline.receiver.ConnectivityChangeReceiver;
import com.aashdit.olmoffline.ui.MainActivity;
import com.aashdit.olmoffline.ui.dialog.CustomProgressDialogue;
import com.aashdit.olmoffline.utils.Constant;
import com.aashdit.olmoffline.utils.LocaleManager;
import com.aashdit.olmoffline.utils.SharedPrefManager;
import com.aashdit.olmoffline.utils.Utility;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

import static android.content.pm.PackageManager.GET_META_DATA;

public class PendingActivity extends AppCompatActivity implements /*PendingItemsAdapter.PendingDataListener,*/
        PendingOfflineItemsAdapter.PendingOfflineDataListener,
        ConnectivityChangeReceiver.ConnectivityReceiverListener {

    private static final String TAG = "PendingActivity";
    private final List<TaskListItem> taskListItems = new ArrayList<>();
    private ActivityPendingBinding binding;
    private String selectedYear, selectedMonth, selectedScheme, selectedActivity, type, status, schemeCode;
    private Long selectedSchemeId, selectedActType, selectedActivityId;
    private String month = "";
    private String token, userType, activityCode;
    private SharedPrefManager sp;

    private PendingItemsAdapter pendingItemsAdapter;
    private PendingOfflineItemsAdapter pendingOfflineItemsAdapter;

    private Realm realm;
    private RealmResults<TaskListItem> taskListItemRealmResults;

    private CustomProgressDialogue progressDialogue;
    /**
     * @param isConnected for checking network connectivity
     */
    private boolean isConnected = false;
    private ConnectivityChangeReceiver mConnectivityChangeReceiver;
    private GoatryReportLine reportLine;
    private boolean canSend;
    private PoultryReportLine poultryReportLine;
    private DairyReportLine dairyReportLine;
    private FisheryReportLine fisheryReportLine;
    private FarmReportLine farmReportLine;

//    @Override
//    public void onPendingClick(int position) {
//
//        Long entityId = taskListItems.get(position).entityId;
//        if (userType.equals(Constant.PMITRA)) {
//            if (/*selectedActivityId.equals(1L)*/activityCode.equals("GOATRY")) {
//                Intent goatryViewIntent = new Intent(this, UpdateShgActivity.class/*GoatryViewMainActivity.class*/);
//                goatryViewIntent.putExtra("ENTITY_TYPE_CODE", type);
//                goatryViewIntent.putExtra("MONTH", selectedMonth);
//                goatryViewIntent.putExtra("YEAR", selectedYear);
//                goatryViewIntent.putExtra("ENTITY_ID", entityId);
//                goatryViewIntent.putExtra("SCHEME_ID", selectedSchemeId);
//                goatryViewIntent.putExtra("ACTIVITY_ID", selectedActivityId);
//                startActivity(goatryViewIntent);
//            } else if (/*selectedActivityId.equals(2L)*/activityCode.equals("POULTRY")) {
//                Intent poultryViewIntent = new Intent(this, BirdsViewMainActivity.class);
//                poultryViewIntent.putExtra("ENTITY_TYPE_CODE", type);
//                poultryViewIntent.putExtra("MONTH", selectedMonth);
//                poultryViewIntent.putExtra("YEAR", selectedYear);
//                poultryViewIntent.putExtra("ENTITY_ID", entityId);
//                poultryViewIntent.putExtra("SCHEME_ID", selectedSchemeId);
//                poultryViewIntent.putExtra("ACTIVITY_ID", selectedActivityId);
//                startActivity(poultryViewIntent);
//            } else if (/*selectedActivityId.equals(3L)*/activityCode.equals("FISHERY")) {
//                Intent fisheryViewIntent = new Intent(this, FishViewMainActivity.class);
//                fisheryViewIntent.putExtra("ENTITY_TYPE_CODE", type);
//                fisheryViewIntent.putExtra("MONTH", selectedMonth);
//                fisheryViewIntent.putExtra("YEAR", selectedYear);
//                fisheryViewIntent.putExtra("ENTITY_ID", entityId);
//                fisheryViewIntent.putExtra("SCHEME_ID", selectedSchemeId);
//                fisheryViewIntent.putExtra("ACTIVITY_ID", selectedActivityId);
//                startActivity(fisheryViewIntent);
//            } else if (/*selectedActivityId.equals(7L)*/activityCode.equals("DAIRY")) {
//                Intent fisheryViewIntent = new Intent(this, DairyViewMainActivity.class);
//                fisheryViewIntent.putExtra("ENTITY_TYPE_CODE", type);
//                fisheryViewIntent.putExtra("MONTH", selectedMonth);
//                fisheryViewIntent.putExtra("YEAR", selectedYear);
//                fisheryViewIntent.putExtra("ENTITY_ID", entityId);
//                fisheryViewIntent.putExtra("SCHEME_ID", selectedSchemeId);
//                fisheryViewIntent.putExtra("ACTIVITY_ID", selectedActivityId);
//                startActivity(fisheryViewIntent);
//            }
//        } else if (userType.equals(Constant.KMITRA)) {
//            Intent fisheryViewIntent = new Intent(this, FarmingViewMainActivity.class);
//            fisheryViewIntent.putExtra("ENTITY_TYPE_CODE", type);
//            fisheryViewIntent.putExtra("MONTH", selectedMonth);
//            fisheryViewIntent.putExtra("YEAR", selectedYear);
//            fisheryViewIntent.putExtra("ENTITY_ID", entityId);
//            fisheryViewIntent.putExtra("SCHEME_ID", selectedSchemeId);
//            fisheryViewIntent.putExtra("ACTIVITY_ID", selectedActivityId);
//            startActivity(fisheryViewIntent);
//        } else if (userType.equals(Constant.UMITRA) || userType.equals(Constant.CRPEP)) {
//            if (selectedActType.equals(2L)) {
//                Intent fisheryViewIntent = new Intent(this, NTFTViewMainActivity.class);
//                fisheryViewIntent.putExtra("ENTITY_TYPE_CODE", type);
//                fisheryViewIntent.putExtra("MONTH", selectedMonth);
//                fisheryViewIntent.putExtra("YEAR", selectedYear);
//                fisheryViewIntent.putExtra("ENTITY_ID", entityId);
//                fisheryViewIntent.putExtra("SCHEME_ID", selectedSchemeId);
//                fisheryViewIntent.putExtra("ACTIVITY_ID", selectedActivityId);
//                fisheryViewIntent.putExtra("REQ_ACT", 0);
//                startActivity(fisheryViewIntent);
//            } else if (selectedActType.equals(13L)) {
//                Intent fisheryViewIntent = new Intent(this, NTFTViewMainActivity.class);
//                fisheryViewIntent.putExtra("ENTITY_TYPE_CODE", type);
//                fisheryViewIntent.putExtra("MONTH", selectedMonth);
//                fisheryViewIntent.putExtra("YEAR", selectedYear);
//                fisheryViewIntent.putExtra("ENTITY_ID", entityId);
//                fisheryViewIntent.putExtra("SCHEME_ID", selectedSchemeId);
//                fisheryViewIntent.putExtra("ACTIVITY_ID", selectedActivityId);
//                fisheryViewIntent.putExtra("REQ_ACT", 1);
//                startActivity(fisheryViewIntent);
//            } else {
//                Intent fisheryViewIntent = new Intent(this, NFViewMainActivity.class);
//                fisheryViewIntent.putExtra("ENTITY_TYPE_CODE", type);
//                fisheryViewIntent.putExtra("MONTH", selectedMonth);
//                fisheryViewIntent.putExtra("YEAR", selectedYear);
//                fisheryViewIntent.putExtra("ENTITY_ID", entityId);
//                fisheryViewIntent.putExtra("SCHEME_ID", selectedSchemeId);
//                fisheryViewIntent.putExtra("ACTIVITY_ID", selectedActivityId);
//                startActivity(fisheryViewIntent);
//            }
//        }
//    }

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
        binding = ActivityPendingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        resetTitles();
        setSupportActionBar(binding.toolbarPendingActivity);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        sp = SharedPrefManager.getInstance(this);
        token = sp.getStringData(Constant.APP_TOKEN);
        userType = sp.getStringData(Constant.USER_ROLE);

        if (realm == null) {
            realm = Realm.getDefaultInstance();
        }
        taskListItemRealmResults = realm.where(TaskListItem.class).findAll();
        mConnectivityChangeReceiver = new ConnectivityChangeReceiver();
        mConnectivityChangeReceiver.setConnectivityReceiverListener(this);
        isConnected = mConnectivityChangeReceiver.getConnectionStatus(this);
        registerNetworkBroadcast();

        progressDialogue = new CustomProgressDialogue(this);

        binding.progress.setVisibility(View.GONE);

        selectedYear = getIntent().getStringExtra("YEAR");
        selectedMonth = getIntent().getStringExtra("MONTH");
        selectedScheme = getIntent().getStringExtra("SCHEME");
        selectedActivity = getIntent().getStringExtra("ACTIVITY_NAME");
        selectedSchemeId = getIntent().getLongExtra("SCHEME_ID", 0L);
        selectedActivityId = getIntent().getLongExtra("ACTIVITY_ID", 0L);
        selectedActType = getIntent().getLongExtra("ACT_TYPE", 0L);
        activityCode = getIntent().getStringExtra("ACTIVITY_CODE");
        type = getIntent().getStringExtra("ENTITY_CODE");
        status = getIntent().getStringExtra("STATUS");
        schemeCode = getIntent().getStringExtra("SCHEME_CODE");


        switch (selectedMonth) {
            case "1":
                month = "JAN";
                break;
            case "2":
                month = "FEB";
                break;
            case "3":
                month = "MAR";
                break;
            case "4":
                month = "APR";
                break;
            case "5":
                month = "MAY";
                break;
            case "6":
                month = "JUN";
                break;
            case "7":
                month = "JUL";
                break;
            case "8":
                month = "AUG";
                break;
            case "9":
                month = "SEP";
                break;
            case "10":
                month = "OCT";
                break;
            case "11":
                month = "NOV";
                break;
            case "12":
                month = "DEC";
                break;
        }

        binding.tvSelectedYear.setText(selectedYear);
        binding.tvMonth.setText(month);
        binding.tvScheme.setText(selectedScheme);
        binding.tvActivity.setText(selectedActivity);

        switch (type) {
            case "HH":
                binding.tvPendingType.setText(getString(R.string.pending_).concat("Member"));
                break;
            case "SHG":
                binding.tvPendingType.setText(getString(R.string.pending_).concat("SHG"));
                break;
            case "CLF":
                binding.tvPendingType.setText(getString(R.string.pending_).concat("Cluster"));
                break;
        }


        if (isConnected) {
            pendingItemsAdapter = new PendingItemsAdapter(this, taskListItems, selectedActivityId, userType);
//            pendingItemsAdapter.setPendingDataListener(this);
            binding.rvPendingList.setLayoutManager(new LinearLayoutManager(this));
            binding.rvPendingList.setAdapter(pendingItemsAdapter);
        }

        if (sp.getStringData(Constant.USER_ROLE).equals(Constant.CRPEP)
                || sp.getStringData(Constant.USER_ROLE).equals(Constant.UMITRA)) {
//            binding.llActivityType.setVisibility(View.VISIBLE);
        } else {
            binding.llActivityType.setVisibility(View.GONE);
        }

        taskListItemRealmResults = realm.where(TaskListItem.class)
                .equalTo("activityId", selectedActivityId)
                .equalTo("month", Integer.parseInt(selectedMonth))
                .equalTo("year", Integer.parseInt(selectedYear))
                .equalTo("levelCode", type)
                .findAll();

        if (taskListItemRealmResults.size() > 0) {
            binding.tvNoItems.setVisibility(View.GONE);
        } else {
            binding.tvNoItems.setVisibility(View.VISIBLE);
        }
        pendingOfflineItemsAdapter = new PendingOfflineItemsAdapter(this, taskListItemRealmResults,
                selectedActivityId, userType);

        pendingOfflineItemsAdapter.setPendingDataListener(this);
        binding.rvPendingList.setLayoutManager(new LinearLayoutManager(this));
        binding.rvPendingList.setAdapter(pendingOfflineItemsAdapter);
        if (isConnected) {
//            getPendingTaskList();
        }
        /*else {
            pendingOfflineItemsAdapter = new PendingOfflineItemsAdapter(this,taskListItemRealmResults,
                    selectedActivityId,userType);
            pendingOfflineItemsAdapter.setPendingDataListener(this);
            binding.rvPendingList.setLayoutManager(new LinearLayoutManager(this));
            binding.rvPendingList.setAdapter(pendingOfflineItemsAdapter);

        }*/
    }

    private void getPendingTaskList() {
        binding.progress.setVisibility(View.VISIBLE);


        JSONObject obj = new JSONObject();
        try {
            obj.put("month", selectedMonth);
            obj.put("year", selectedYear);
            obj.put("schemeId", String.valueOf(selectedSchemeId));
            obj.put("activityId", String.valueOf(selectedActivityId));
            obj.put("wfStatus", status);
            obj.put("levelCode", type);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String url = "";

        if (userType.equals(Constant.PMITRA)) {
            url = "tasks/list";
        } else if (userType.equals(Constant.KMITRA)) {
            url = "farming/tasks/list";
        } else if (userType.equals(Constant.UMITRA) || sp.getStringData(Constant.USER_ROLE).equals(Constant.CRPEP)) {
            if (selectedActType.equals(2L)) {
                url = "ntfp-tasar/tasks/list";
            } else if (selectedActType.equals(13L)) {
                url = "ntfp/tasks/list";
            } else {
                url = "nonfarming/tasks/list";
            }
        }

        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + url)
                .addHeaders("Authorization", "Bearer " + token)
                .setTag("Activity")
                .addJSONObjectBody(obj)
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        if (Utility.isStringValid(response)) {
                            binding.progress.setVisibility(View.GONE);
                            try {
                                JSONObject resObj = new JSONObject(response);
                                if (resObj.optBoolean("outcome")) {
                                    JSONArray resArray = resObj.optJSONArray("data");
                                    if (resArray != null && resArray.length() > 0) {
                                        taskListItems.clear();
                                        binding.tvNoItems.setVisibility(View.GONE);
                                        binding.rvPendingList.setVisibility(View.VISIBLE);
                                        for (int i = 0; i < resArray.length(); i++) {
                                            TaskListItem item = TaskListItem.parseTaskList(resArray.optJSONObject(i));
                                            taskListItems.add(item);
                                        }

                                        realm.executeTransaction(new Realm.Transaction() {
                                            @Override
                                            public void execute(Realm realm1) {
//                                                realm1.delete(TaskListItem.class);
                                                realm1.insertOrUpdate(taskListItems);
                                            }
                                        });

                                        pendingItemsAdapter.notifyDataSetChanged();
                                    } else {
                                        binding.tvNoItems.setVisibility(View.VISIBLE);
                                        binding.rvPendingList.setVisibility(View.GONE);
                                    }

                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        binding.progress.setVisibility(View.GONE);
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
    public void onNetworkConnectionChanged(boolean isConnected) {
        this.isConnected = isConnected;
    }

    private void registerNetworkBroadcast() {
        registerReceiver(mConnectivityChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    protected void unregisterNetworkChanges() {
        try {
            unregisterReceiver(mConnectivityChangeReceiver);
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
    public void onOfflinePendingClick(int position) {
        Long entityId = taskListItemRealmResults.get(position).entityId;
        if (userType.equals(Constant.PMITRA)) {
            if (/*selectedActivityId.equals(1L)*/activityCode.equals("GOATRY")) {
                Intent goatryViewIntent = new Intent(this, GoatryUpdateOfflineActivity.class/*GoatryViewMainActivity.class*/);
                goatryViewIntent.putExtra("ENTITY_CODE", type);
                goatryViewIntent.putExtra("MONTH", selectedMonth);
                goatryViewIntent.putExtra("YEAR", selectedYear);
                goatryViewIntent.putExtra("ENTITY_ID", entityId);
                goatryViewIntent.putExtra("ENTRY_FROM", 1);
                goatryViewIntent.putExtra("SCHEME_ID", selectedSchemeId);
                goatryViewIntent.putExtra("ACTIVITY_ID", selectedActivityId);

                goatryViewIntent.putExtra("ENTITY_NAME", taskListItemRealmResults.get(position).entityName);
                goatryViewIntent.putExtra("SCHEME_NAME", taskListItemRealmResults.get(position).schemeName);
                goatryViewIntent.putExtra("ACTIVITY_NAME", taskListItemRealmResults.get(position).activityName);
                goatryViewIntent.putExtra("SCHEME_CODE", schemeCode);
                goatryViewIntent.putExtra("ACTIVITY_CODE", activityCode);

//                sp.setLongData(Constant.ENTITY_ID, entityId);
//                sp.setLongData(Constant.SCHEME_ID, selectedSchemeId);
//                sp.setLongData(Constant.ACTIVITY_ID, selectedActivityId);
//                sp.setStringData(Constant.MONTH, String.valueOf(selectedMonth));
//                sp.setStringData(Constant.YEAR, String.valueOf(selectedYear));

                startActivity(goatryViewIntent);
            } else if (/*selectedActivityId.equals(2L)*/activityCode.equals("POULTRY")) {
                Intent poultryViewIntent = new Intent(this, BirdsUpdateActivity.class);
                poultryViewIntent.putExtra("ENTITY_TYPE_CODE", type);
                poultryViewIntent.putExtra("MONTH", selectedMonth);
                poultryViewIntent.putExtra("YEAR", selectedYear);
                poultryViewIntent.putExtra("ENTITY_ID", entityId);
                poultryViewIntent.putExtra("ENTRY_FROM", 1);
                poultryViewIntent.putExtra("SCHEME_ID", selectedSchemeId);
                poultryViewIntent.putExtra("ACTIVITY_ID", selectedActivityId);


                poultryViewIntent.putExtra("ENTITY_NAME", taskListItemRealmResults.get(position).entityName);
                poultryViewIntent.putExtra("SCHEME_NAME", taskListItemRealmResults.get(position).schemeName);
                poultryViewIntent.putExtra("ACTIVITY_NAME", taskListItemRealmResults.get(position).activityName);
                poultryViewIntent.putExtra("SCHEME_CODE", schemeCode);
                poultryViewIntent.putExtra("ACTIVITY_CODE", activityCode);

                startActivity(poultryViewIntent);
            } else if (/*selectedActivityId.equals(3L)*/activityCode.equals("FISHERY")) {
                Intent fisheryViewIntent = new Intent(this, FishUpdateActivity.class);
                fisheryViewIntent.putExtra("ENTITY_TYPE_CODE", type);
                fisheryViewIntent.putExtra("MONTH", selectedMonth);
                fisheryViewIntent.putExtra("YEAR", selectedYear);
                fisheryViewIntent.putExtra("ENTITY_ID", entityId);
                fisheryViewIntent.putExtra("ENTRY_FROM", 1);
                fisheryViewIntent.putExtra("SCHEME_ID", selectedSchemeId);
                fisheryViewIntent.putExtra("ACTIVITY_ID", selectedActivityId);


                fisheryViewIntent.putExtra("ENTITY_NAME", taskListItemRealmResults.get(position).entityName);
                fisheryViewIntent.putExtra("SCHEME_NAME", taskListItemRealmResults.get(position).schemeName);
                fisheryViewIntent.putExtra("ACTIVITY_NAME", taskListItemRealmResults.get(position).activityName);
                fisheryViewIntent.putExtra("SCHEME_CODE", schemeCode);
                fisheryViewIntent.putExtra("ACTIVITY_CODE", activityCode);

                startActivity(fisheryViewIntent);
            } else if (/*selectedActivityId.equals(7L)*/activityCode.equals("DAIRY")) {
                Intent fisheryViewIntent = new Intent(this, DairyUpdateActivity.class);
                fisheryViewIntent.putExtra("ENTITY_TYPE_CODE", type);
                fisheryViewIntent.putExtra("MONTH", selectedMonth);
                fisheryViewIntent.putExtra("YEAR", selectedYear);
                fisheryViewIntent.putExtra("ENTITY_ID", entityId);
                fisheryViewIntent.putExtra("SCHEME_ID", selectedSchemeId);
                fisheryViewIntent.putExtra("ACTIVITY_ID", selectedActivityId);
                fisheryViewIntent.putExtra("ENTRY_FROM", 1);


                fisheryViewIntent.putExtra("ENTITY_NAME", taskListItemRealmResults.get(position).entityName);
                fisheryViewIntent.putExtra("SCHEME_NAME", taskListItemRealmResults.get(position).schemeName);
                fisheryViewIntent.putExtra("ACTIVITY_NAME", taskListItemRealmResults.get(position).activityName);
                fisheryViewIntent.putExtra("SCHEME_CODE", schemeCode);
                fisheryViewIntent.putExtra("ACTIVITY_CODE", activityCode);

                startActivity(fisheryViewIntent);
            }
        } else if (userType.equals(Constant.KMITRA)) {
            Intent fisheryViewIntent = new Intent(this, FarmerUpdateActivity.class);
            fisheryViewIntent.putExtra("ENTITY_CODE", type);
            fisheryViewIntent.putExtra("MONTH", selectedMonth);
            fisheryViewIntent.putExtra("YEAR", selectedYear);
            fisheryViewIntent.putExtra("ENTITY_ID", entityId);
            fisheryViewIntent.putExtra("SCHEME_ID", selectedSchemeId);
            fisheryViewIntent.putExtra("ACTIVITY_ID", selectedActivityId);
            fisheryViewIntent.putExtra("ENTRY_FROM", 1);

            fisheryViewIntent.putExtra("ENTITY_NAME", taskListItemRealmResults.get(position).entityName);
            fisheryViewIntent.putExtra("SCHEME_NAME", taskListItemRealmResults.get(position).schemeName);
            fisheryViewIntent.putExtra("ACTIVITY_NAME", taskListItemRealmResults.get(position).activityName);
            fisheryViewIntent.putExtra("SCHEME_CODE", schemeCode);
            fisheryViewIntent.putExtra("ACTIVITY_CODE", activityCode);

            startActivity(fisheryViewIntent);
        } else if (userType.equals(Constant.UMITRA) || userType.equals(Constant.CRPEP)) {
            if (selectedActType.equals(2L)) {
                Intent fisheryViewIntent = new Intent(this, NTFTViewMainActivity.class);
                fisheryViewIntent.putExtra("ENTITY_TYPE_CODE", type);
                fisheryViewIntent.putExtra("MONTH", selectedMonth);
                fisheryViewIntent.putExtra("YEAR", selectedYear);
                fisheryViewIntent.putExtra("ENTITY_ID", entityId);
                fisheryViewIntent.putExtra("SCHEME_ID", selectedSchemeId);
                fisheryViewIntent.putExtra("ACTIVITY_ID", selectedActivityId);
                fisheryViewIntent.putExtra("REQ_ACT", 0);
                startActivity(fisheryViewIntent);
            } else if (selectedActType.equals(13L)) {
                Intent fisheryViewIntent = new Intent(this, NTFTViewMainActivity.class);
                fisheryViewIntent.putExtra("ENTITY_TYPE_CODE", type);
                fisheryViewIntent.putExtra("MONTH", selectedMonth);
                fisheryViewIntent.putExtra("YEAR", selectedYear);
                fisheryViewIntent.putExtra("ENTITY_ID", entityId);
                fisheryViewIntent.putExtra("SCHEME_ID", selectedSchemeId);
                fisheryViewIntent.putExtra("ACTIVITY_ID", selectedActivityId);
                fisheryViewIntent.putExtra("REQ_ACT", 1);
                startActivity(fisheryViewIntent);
            } else {
                Intent fisheryViewIntent = new Intent(this, NFViewMainActivity.class);
                fisheryViewIntent.putExtra("ENTITY_TYPE_CODE", type);
                fisheryViewIntent.putExtra("MONTH", selectedMonth);
                fisheryViewIntent.putExtra("YEAR", selectedYear);
                fisheryViewIntent.putExtra("ENTITY_ID", entityId);
                fisheryViewIntent.putExtra("SCHEME_ID", selectedSchemeId);
                fisheryViewIntent.putExtra("ACTIVITY_ID", selectedActivityId);
                startActivity(fisheryViewIntent);
            }
        }
    }

    @Override
    public void onSendForApproval(int position, String uniqueId, String actName) {

        if (isConnected) {
            if (actName.equals("Goatry Farming") || actName.equals("Goat Farming")) {

                reportLine = realm.where(GoatryReportLine.class)
                        .equalTo("unique", uniqueId)
                        .findFirst();


                progressDialogue.show();

                if (reportLine != null) {


                    String taskUnique = reportLine.schemeId + "_" + reportLine.activityId + "_" + reportLine.monthId + "_" + reportLine.year + "_" + reportLine.entityId;
                    Task _task = realm.where(Task.class)
                            .equalTo("unique", taskUnique)
                            .findFirst();


                    JSONObject reqCheckObj = new JSONObject();
                    try {
                        reqCheckObj.put("month", reportLine.monthId);
                        reqCheckObj.put("year", reportLine.year);
                        reqCheckObj.put("schemeId", reportLine.schemeId);
                        reqCheckObj.put("activityId", reportLine.activityId);
                        reqCheckObj.put("entityId", reportLine.entityId);
                        reqCheckObj.put("entityTypeCode", reportLine.entityTypeCode);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    getDetailsAboutTheRequest(reqCheckObj, uniqueId, _task, position, taskUnique, "goatry-activity/details");


                }
            } else if (actName.equals("Poultry Farming")) {
                poultryReportLine = realm.where(PoultryReportLine.class)
                        .equalTo("unique", uniqueId)
                        .findFirst();


                progressDialogue.show();

                if (poultryReportLine != null) {


                    String taskUnique = poultryReportLine.schemeId + "_" + poultryReportLine.activityId + "_" + poultryReportLine.monthId + "_" + poultryReportLine.year + "_" + poultryReportLine.entityId;
                    Task _task = realm.where(Task.class)
                            .equalTo("unique", taskUnique)
                            .findFirst();


                    JSONObject reqCheckObj = new JSONObject();
                    try {
                        reqCheckObj.put("month", poultryReportLine.monthId);
                        reqCheckObj.put("year", poultryReportLine.year);
                        reqCheckObj.put("schemeId", poultryReportLine.schemeId);
                        reqCheckObj.put("activityId", poultryReportLine.activityId);
                        reqCheckObj.put("entityId", poultryReportLine.entityId);
                        reqCheckObj.put("entityTypeCode", poultryReportLine.entityTypeCode);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    getDetailsAboutTheRequest(reqCheckObj, uniqueId, _task, position, taskUnique, "poultry-activity/details");


                }
            } else if (actName.equals("Dairy Farming")) {
                dairyReportLine = realm.where(DairyReportLine.class)
                        .equalTo("unique", uniqueId)
                        .findFirst();

                progressDialogue.show();

                if (dairyReportLine != null) {

                    String taskUnique = dairyReportLine.schemeId + "_" + dairyReportLine.activityId + "_" + dairyReportLine.monthId + "_" + dairyReportLine.year + "_" + dairyReportLine.entityId;
                    Task _task = realm.where(Task.class)
                            .equalTo("unique", taskUnique)
                            .findFirst();

                    JSONObject reqCheckObj = new JSONObject();
                    try {
                        reqCheckObj.put("month", dairyReportLine.monthId);
                        reqCheckObj.put("year", dairyReportLine.year);
                        reqCheckObj.put("schemeId", dairyReportLine.schemeId);
                        reqCheckObj.put("activityId", dairyReportLine.activityId);
                        reqCheckObj.put("entityId", dairyReportLine.entityId);
                        reqCheckObj.put("entityTypeCode", dairyReportLine.entityTypeCode);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    getDetailsAboutTheRequest(reqCheckObj, uniqueId, _task, position, taskUnique, "dairy-activity/details");


                }
            } else if (actName.equals("Pisciculture")) {

                fisheryReportLine = realm.where(FisheryReportLine.class)
                        .equalTo("unique", uniqueId)
                        .findFirst();

                progressDialogue.show();

                if (fisheryReportLine != null) {


                    String taskUnique = fisheryReportLine.schemeId + "_" + fisheryReportLine.activityId + "_" + fisheryReportLine.monthId + "_" + fisheryReportLine.year + "_" + fisheryReportLine.entityId;
                    Task _task = realm.where(Task.class)
                            .equalTo("unique", taskUnique)
                            .findFirst();

                    JSONObject reqCheckObj = new JSONObject();
                    try {
                        reqCheckObj.put("month", fisheryReportLine.monthId);
                        reqCheckObj.put("year", fisheryReportLine.year);
                        reqCheckObj.put("schemeId", fisheryReportLine.schemeId);
                        reqCheckObj.put("activityId", fisheryReportLine.activityId);
                        reqCheckObj.put("entityId", fisheryReportLine.entityId);
                        reqCheckObj.put("entityTypeCode", fisheryReportLine.entityTypeCode);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    getDetailsAboutTheRequest(reqCheckObj, uniqueId, _task, position, taskUnique, "fishery-activity/details");

                }
            }else if (userType.equals(Constant.KMITRA)){
                farmReportLine = realm.where(FarmReportLine.class)
                        .equalTo("unique",uniqueId)
                        .findFirst();

                progressDialogue.show();
                if (farmReportLine != null){

                    String taskUnique = farmReportLine.schemeId + "_" + farmReportLine.activityId + "_" + farmReportLine.monthId + "_" + farmReportLine.year + "_" + farmReportLine.entityId;
                    Task _task = realm.where(Task.class)
                            .equalTo("unique", taskUnique)
                            .findFirst();

                    JSONObject reqCheckObj = new JSONObject();
                    try {
                        reqCheckObj.put("month", farmReportLine.monthId);
                        reqCheckObj.put("year", farmReportLine.year);
                        reqCheckObj.put("schemeId", farmReportLine.schemeId);
                        reqCheckObj.put("activityId", farmReportLine.activityId);
                        reqCheckObj.put("entityId", farmReportLine.entityId);
                        reqCheckObj.put("entityTypeCode", farmReportLine.entityTypeCode);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    getDetailsAboutTheRequest(reqCheckObj, uniqueId, _task, position, taskUnique, "farm-activity/details");


                }
            }
        } else {
            Toast.makeText(this, "Please enable internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    private void getDetailsAboutTheRequest(JSONObject object, String uniqueId, Task _task, int position,
                                           String taskUnique, String detailsUrl) {

        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + detailsUrl)
                .addHeaders("Authorization", "Bearer " + token)
                .setTag("Activity")
                .addJSONObjectBody(object)
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        binding.progress.setVisibility(View.GONE);
                        if (Utility.isStringValid(response)) {

                            try {
                                JSONObject resObj = new JSONObject(response);
                                if (resObj.optBoolean("outcome")) {
                                    JSONObject obj = resObj.optJSONObject("data");

                                    if (obj != null) {
                                        String wfStatus = obj.optString("wfStatus");
                                        canSend = obj.optBoolean("canEdit");


                                        if (canSend && reportLine != null && detailsUrl.equals("goatry-activity/details")) {

                                            saveLineForGoatry(position, taskUnique, _task, uniqueId);

                                        } else if (canSend && poultryReportLine != null && detailsUrl.equals("poultry-activity/details")) {

                                            saveLineForPoultry(position, taskUnique, _task, uniqueId);

                                        } else if (canSend && dairyReportLine != null && detailsUrl.equals("dairy-activity/details")) {

                                            saveLineForDairy(position, taskUnique, _task, uniqueId);

                                        } else if (canSend && fisheryReportLine != null && detailsUrl.equals("fishery-activity/details")) {

                                            saveLineForFishery(position, taskUnique, _task, uniqueId);

                                        }else if (canSend && farmReportLine != null && detailsUrl.equals("farm-activity/details")){

                                            saveLineForFarming(position,taskUnique,_task,uniqueId);

                                        } else {
                                            realm.executeTransaction(new Realm.Transaction() {
                                                @Override
                                                public void execute(Realm realm1) {

//                                                    RealmResults<TaskListItem> taskListItems = realm.where(TaskListItem.class)
//                                                            .equalTo("unique", uniqueId)
//                                                            .findAll();
//
//                                                    taskListItems.deleteAllFromRealm();
//                                                    if (detailsUrl.equals("goatry-activity/details")) {
//                                                        RealmResults<GoatryReportLine> goatryReportLines = realm.where(GoatryReportLine.class)
//                                                                .equalTo("unique", uniqueId).findAll();
//                                                        goatryReportLines.deleteAllFromRealm();
//                                                    } else if (detailsUrl.equals("poultry-activity/details")) {
//                                                        RealmResults<PoultryReportLine> goatryReportLines = realm.where(PoultryReportLine.class)
//                                                                .equalTo("unique", uniqueId).findAll();
//                                                        goatryReportLines.deleteAllFromRealm();
//                                                    } else if (detailsUrl.equals("dairy-activity/details")) {
//                                                        RealmResults<DairyReportLine> goatryReportLines = realm.where(DairyReportLine.class)
//                                                                .equalTo("unique", uniqueId).findAll();
//                                                        goatryReportLines.deleteAllFromRealm();
//                                                    } else if (detailsUrl.equals("fishery-activity/details")) {
//                                                        RealmResults<FisheryReportLine> goatryReportLines = realm.where(FisheryReportLine.class)
//                                                                .equalTo("unique", uniqueId).findAll();
//                                                        goatryReportLines.deleteAllFromRealm();
//                                                    }
//                                                    pendingOfflineItemsAdapter.notifyItemRemoved(position);

                                                    Toast.makeText(PendingActivity.this, "", Toast.LENGTH_SHORT).show();
                                                    progressDialogue.dismiss();

                                                }
                                            });

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
//                        canSend = false;
                        binding.progress.setVisibility(View.GONE);
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

    private void saveLineForFarming(int position, String taskUnique, Task _task, String uniqueId) {


        //        binding.progress.setVisibility(View.VISIBLE);
        JSONObject object = new JSONObject();
        try {
            object.put("schemeId", farmReportLine.schemeId);
            object.put("activityId", farmReportLine.activityId);
            object.put("reportingYear", farmReportLine.year);
            object.put("monthId", farmReportLine.monthId);
            object.put("quantitySold", farmReportLine.quantitySold);
            object.put("quantityProduced", farmReportLine.quantityProduced);
            object.put("reportingLevelCode", farmReportLine.entityTypeCode);
            object.put("entityId", farmReportLine.entityId);
            object.put("remarks", farmReportLine.remarks);
            object.put("searchTerm", "");
            object.put("seasonalIncome", Double.parseDouble(farmReportLine.seasonalIncome));
            object.put("totalExpenditure", Double.parseDouble(farmReportLine.totalExpenditure));
            object.put("totalIncome", Double.parseDouble(farmReportLine.totalIncome));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "farm-activity/save-line")
                .addHeaders("Authorization", "Bearer " + token)
                .setTag("Activity")
                .addJSONObjectBody(object)
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        binding.progress.setVisibility(View.GONE);
                        if (Utility.isStringValid(response)) {
                            try {
                                JSONObject resObj = new JSONObject(response);
                                if (resObj.optBoolean("outcome")) {
                                    Toast.makeText(PendingActivity.this, getResources().getString(R.string.update_success), Toast.LENGTH_SHORT).show();


                                    try {
                                        realm.executeTransaction(new Realm.Transaction() {
                                            @Override
                                            public void execute(@NotNull Realm realm1) {
                                                FarmReportLine reportLine = realm.where(FarmReportLine.class)
                                                        .equalTo("unique", uniqueId)
                                                        .findFirst();
//
                                                if (reportLine != null) {

                                                    sendForApproval("farm-activity/submit", reportLine.monthId, reportLine.year,
                                                            reportLine.schemeId, reportLine.activityId, reportLine.entityId,
                                                            reportLine.entityTypeCode, uniqueId, taskUnique, _task, position);


//                                                    if (reportLine.entityTypeCode.equals("SHG")) {
//                                                        _task.shgCount = _task.shgCount - 1;
//                                                    } else if (reportLine.entityTypeCode.equals("CLF")) {
//                                                        _task.clfCount = _task.clfCount - 1;
//                                                    } else if (reportLine.entityTypeCode.equals("PG")) {
//                                                        _task.pgCount = _task.pgCount - 1;
//                                                    } else if (reportLine.entityTypeCode.equals("EG")) {
//                                                        _task.egCount = _task.egCount - 1;
//                                                    } else if (reportLine.entityTypeCode.equals("HH")) {
//                                                        _task.householdCount = _task.householdCount - 1;
//                                                    }
////
//                                                    Task task = new Task();
//                                                    task.unique = taskUnique;
//                                                    task.householdCount = _task.householdCount;
//                                                    task.shgCount = _task.shgCount;
//                                                    task.clfCount = _task.clfCount;
//                                                    task.egCount = _task.egCount;
//                                                    task.pgCount = _task.pgCount;
//                                                    task.month = _task.month;
//                                                    task.year = _task.year;
//                                                    task.schemeId = _task.schemeId;
//                                                    task.activityId = _task.activityId;
//                                                    task.taskStatus = "PENDING";
//                                                    task.schemeName = _task.schemeName;
//                                                    task.activityName = _task.activityName;
//                                                    task.monthCode = _task.monthCode;
//                                                    task.hashu = _task.hashu;
////
//                                                    realm1.insertOrUpdate(task);
////
//                                                    RealmResults<Task> taskRealmResults = realm.where(Task.class)
//                                                            .equalTo("clfCount", 0)
//                                                            .equalTo("shgCount", 0)
//                                                            .equalTo("pgCount", 0)
//                                                            .equalTo("egCount", 0)
//                                                            .equalTo("householdCount", 0)
//                                                            .findAll();
//
//                                                    taskRealmResults.deleteAllFromRealm();
//
//
//                                                    RealmResults<FisheryReportLine> goatryReportLines = realm.where(FisheryReportLine.class)
//                                                            .equalTo("unique", uniqueId).findAll();
//                                                    goatryReportLines.deleteAllFromRealm();
//
//                                                    RealmResults<TaskListItem> taskListItems = realm.where(TaskListItem.class)
//                                                            .equalTo("unique", uniqueId)
//                                                            .findAll();
//
//                                                    taskListItems.deleteAllFromRealm();
//
//                                                    pendingOfflineItemsAdapter.notifyItemRemoved(position);
//
//                                                    progressDialogue.dismiss();

                                                }
                                            }
                                        });
                                    } finally {
                                        if (realm != null) {
                                            realm.close();
                                        }
                                    }

                                } else {
                                    Toast.makeText(PendingActivity.this, resObj.optString("message"), Toast.LENGTH_SHORT).show();
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        binding.progress.setVisibility(View.GONE);
                        Log.e(TAG, "onError: " + anError.getErrorDetail());
                        try {
                            JSONObject errObj = new JSONObject(anError.getErrorBody());
                            int statusCode = errObj.optInt("status");
                            if (statusCode==500){
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

    private void saveLineForFishery(int position, String taskUnique, Task _task, String uniqueId) {

        JSONObject object = new JSONObject();
        try {
            object.put("schemeId", fisheryReportLine.schemeId);
            object.put("activityId", fisheryReportLine.activityId);
            object.put("year", fisheryReportLine.year);
            object.put("monthId", fisheryReportLine.monthId);
            object.put("entityTypeCode", fisheryReportLine.entityTypeCode);
            object.put("entityId", fisheryReportLine.entityId);
            object.put("numFingerlingsHarvested", fisheryReportLine.numFingerlingsHarvested);
            object.put("tableSizeFishHarvested", fisheryReportLine.tableSizeFishHarvested);
            object.put("fingerlingsSoldQty", fisheryReportLine.fingerlingsSoldQty);
            object.put("tableSizeFishSold", fisheryReportLine.tableSizeFishSold);
            object.put("totalExpenditure", String.valueOf(fisheryReportLine.totalExpenditure));
            object.put("totalIncome", String.valueOf(fisheryReportLine.totalIncome));
            object.put("fingerlingIncome", String.valueOf(fisheryReportLine.fingerlingIncome));
            object.put("tableFishIncome", String.valueOf(fisheryReportLine.tableFishIncome));
        } catch (JSONException e) {
            e.printStackTrace();
        }


        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "fishery-activity/save-line")
                .addHeaders("Authorization", "Bearer " + token)
                .setTag("Activity")
                .addJSONObjectBody(object)
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        binding.progress.setVisibility(View.GONE);
                        if (Utility.isStringValid(response)) {
                            try {
                                JSONObject resObj = new JSONObject(response);
                                if (resObj.optBoolean("outcome")) {
                                    Toast.makeText(PendingActivity.this, getResources().getString(R.string.update_success), Toast.LENGTH_SHORT).show();

                                    try {
                                        realm.executeTransaction(new Realm.Transaction() {
                                            @Override
                                            public void execute(@NotNull Realm realm1) {
                                                FisheryReportLine reportLine = realm.where(FisheryReportLine.class)
                                                        .equalTo("unique", uniqueId)
                                                        .findFirst();
//
                                                if (reportLine != null) {

                                                    sendForApproval("fishery-activity/submit", reportLine.monthId, reportLine.year,
                                                            reportLine.schemeId, reportLine.activityId, reportLine.entityId,
                                                            reportLine.entityTypeCode, uniqueId, taskUnique, _task, position);


//                                                    if (reportLine.entityTypeCode.equals("SHG")) {
//                                                        _task.shgCount = _task.shgCount - 1;
//                                                    } else if (reportLine.entityTypeCode.equals("CLF")) {
//                                                        _task.clfCount = _task.clfCount - 1;
//                                                    } else if (reportLine.entityTypeCode.equals("PG")) {
//                                                        _task.pgCount = _task.pgCount - 1;
//                                                    } else if (reportLine.entityTypeCode.equals("EG")) {
//                                                        _task.egCount = _task.egCount - 1;
//                                                    } else if (reportLine.entityTypeCode.equals("HH")) {
//                                                        _task.householdCount = _task.householdCount - 1;
//                                                    }
////
//                                                    Task task = new Task();
//                                                    task.unique = taskUnique;
//                                                    task.householdCount = _task.householdCount;
//                                                    task.shgCount = _task.shgCount;
//                                                    task.clfCount = _task.clfCount;
//                                                    task.egCount = _task.egCount;
//                                                    task.pgCount = _task.pgCount;
//                                                    task.month = _task.month;
//                                                    task.year = _task.year;
//                                                    task.schemeId = _task.schemeId;
//                                                    task.activityId = _task.activityId;
//                                                    task.taskStatus = "PENDING";
//                                                    task.schemeName = _task.schemeName;
//                                                    task.activityName = _task.activityName;
//                                                    task.monthCode = _task.monthCode;
//                                                    task.hashu = _task.hashu;
////
//                                                    realm1.insertOrUpdate(task);
////
//                                                    RealmResults<Task> taskRealmResults = realm.where(Task.class)
//                                                            .equalTo("clfCount", 0)
//                                                            .equalTo("shgCount", 0)
//                                                            .equalTo("pgCount", 0)
//                                                            .equalTo("egCount", 0)
//                                                            .equalTo("householdCount", 0)
//                                                            .findAll();
//
//                                                    taskRealmResults.deleteAllFromRealm();
//
//
//                                                    RealmResults<FisheryReportLine> goatryReportLines = realm.where(FisheryReportLine.class)
//                                                            .equalTo("unique", uniqueId).findAll();
//                                                    goatryReportLines.deleteAllFromRealm();
//
//                                                    RealmResults<TaskListItem> taskListItems = realm.where(TaskListItem.class)
//                                                            .equalTo("unique", uniqueId)
//                                                            .findAll();
//
//                                                    taskListItems.deleteAllFromRealm();
//
//                                                    pendingOfflineItemsAdapter.notifyItemRemoved(position);
//
//                                                    progressDialogue.dismiss();

                                                }
                                            }
                                        });
                                    } finally {
                                        if (realm != null) {
                                            realm.close();
                                        }
                                    }
                                } else {
                                    Toast.makeText(PendingActivity.this, resObj.optString("message"), Toast.LENGTH_SHORT).show();
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        binding.progress.setVisibility(View.GONE);
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

    private void saveLineForDairy(int position, String taskUnique, Task _task, String uniqueId) {


        JSONObject object = new JSONObject();
        try {
            object.put("month", dairyReportLine.monthId);
            object.put("year", dairyReportLine.year);
            object.put("schemeId", String.valueOf(dairyReportLine.schemeId));
            object.put("activityId", String.valueOf(dairyReportLine.activityId));
            object.put("entityId", String.valueOf(dairyReportLine.entityId));
            object.put("entityTypeCode", dairyReportLine.entityTypeCode);
            object.put("remarks", dairyReportLine.remarks);
            object.put("numCowDead", dairyReportLine.numCowDead);
            object.put("numCowSold", dairyReportLine.numCowSold);
            object.put("numCowBought", dairyReportLine.numCowBought);
            object.put("monthlyCowMilkProduced", dairyReportLine.monthlyCowMilkProduced);
            object.put("monthlyCowMilkSold", dairyReportLine.monthlyCowMilkSold);
            object.put("regularVaccinationDone", dairyReportLine.regularVaccinationDone);
            object.put("numCowVaccinated", dairyReportLine.numCowVaccinated);
            object.put("totalExpenditure", String.valueOf(dairyReportLine.totalExpenditure));
            object.put("totalIncome", String.valueOf(dairyReportLine.totalIncome));
            object.put("milkIncome", dairyReportLine.milkIncome);
            object.put("regularDewormingDone", dairyReportLine.regularDewormingDone);
            object.put("numDewormed", dairyReportLine.numDewormed);
            object.put("numCalfBorn", dairyReportLine.numCalfBorn);
            object.put("numCalfDead", dairyReportLine.numCowDead);
            object.put("numCalfBought", dairyReportLine.numCalfBought);
            object.put("numCalfSold", dairyReportLine.numCalfSold);


        } catch (JSONException e) {
            e.printStackTrace();
        }

        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "dairy-activity/save-line")
                .addHeaders("Authorization", "Bearer " + token)
                .setTag("Activity")
                .addJSONObjectBody(object)
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        binding.progress.setVisibility(View.GONE);
                        if (Utility.isStringValid(response)) {
                            try {
                                JSONObject resObj = new JSONObject(response);
                                if (resObj.optBoolean("outcome")) {
                                    Toast.makeText(PendingActivity.this, getResources().getString(R.string.update_success), Toast.LENGTH_SHORT).show();

                                    try {
                                        realm.executeTransaction(new Realm.Transaction() {
                                            @Override
                                            public void execute(@NotNull Realm realm1) {
                                                DairyReportLine reportLine = realm.where(DairyReportLine.class)
                                                        .equalTo("unique", uniqueId)
                                                        .findFirst();
//
                                                if (reportLine != null) {

                                                    sendForApproval("dairy-activity/submit", reportLine.monthId, reportLine.year,
                                                            reportLine.schemeId, reportLine.activityId, reportLine.entityId,
                                                            reportLine.entityTypeCode, uniqueId, taskUnique, _task, position);

//                                                    if (reportLine.entityTypeCode.equals("SHG")) {
//                                                        _task.shgCount = _task.shgCount - 1;
//                                                    } else if (reportLine.entityTypeCode.equals("CLF")) {
//                                                        _task.clfCount = _task.clfCount - 1;
//                                                    } else if (reportLine.entityTypeCode.equals("PG")) {
//                                                        _task.pgCount = _task.pgCount - 1;
//                                                    } else if (reportLine.entityTypeCode.equals("EG")) {
//                                                        _task.egCount = _task.egCount - 1;
//                                                    } else if (reportLine.entityTypeCode.equals("HH")) {
//                                                        _task.householdCount = _task.householdCount - 1;
//                                                    }
////
//                                                    Task task = new Task();
//                                                    task.unique = taskUnique;
//                                                    task.householdCount = _task.householdCount;
//                                                    task.shgCount = _task.shgCount;
//                                                    task.clfCount = _task.clfCount;
//                                                    task.egCount = _task.egCount;
//                                                    task.pgCount = _task.pgCount;
//                                                    task.month = _task.month;
//                                                    task.year = _task.year;
//                                                    task.schemeId = _task.schemeId;
//                                                    task.activityId = _task.activityId;
//                                                    task.taskStatus = "PENDING";
//                                                    task.schemeName = _task.schemeName;
//                                                    task.activityName = _task.activityName;
//                                                    task.monthCode = _task.monthCode;
//                                                    task.hashu = _task.hashu;
////
//                                                    realm1.insertOrUpdate(task);
////
//                                                    RealmResults<Task> taskRealmResults = realm.where(Task.class)
//                                                            .equalTo("clfCount", 0)
//                                                            .equalTo("shgCount", 0)
//                                                            .equalTo("pgCount", 0)
//                                                            .equalTo("egCount", 0)
//                                                            .equalTo("householdCount", 0)
//                                                            .findAll();
//
//                                                    taskRealmResults.deleteAllFromRealm();
//
//
//                                                    RealmResults<DairyReportLine> goatryReportLines = realm.where(DairyReportLine.class)
//                                                            .equalTo("unique", uniqueId).findAll();
//                                                    goatryReportLines.deleteAllFromRealm();
//
//                                                    RealmResults<TaskListItem> taskListItems = realm.where(TaskListItem.class)
//                                                            .equalTo("unique", uniqueId)
//                                                            .findAll();
//
//                                                    taskListItems.deleteAllFromRealm();
//
//                                                    pendingOfflineItemsAdapter.notifyItemRemoved(position);
//
//                                                    progressDialogue.dismiss();

                                                }
                                            }
                                        });
                                    } finally {
                                        if (realm != null) {
                                            realm.close();
                                        }
                                    }
                                } else {
                                    Toast.makeText(PendingActivity.this, resObj.optString("message"), Toast.LENGTH_SHORT).show();
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        binding.progress.setVisibility(View.GONE);
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

    private void saveLineForPoultry(int position, String taskUnique, Task _task, String uniqueId) {

        JSONObject object = new JSONObject();

        try {
            object.put("schemeId", poultryReportLine.schemeId);
            object.put("activityId", poultryReportLine.activityId);
            object.put("year", poultryReportLine.year);
            object.put("monthId", poultryReportLine.monthId);
            object.put("entityId", poultryReportLine.entityId);
            object.put("entityTypeCode", poultryReportLine.entityTypeCode);
            object.put("numBirdBought", poultryReportLine.numBirdBought);
            object.put("numBirdSold", poultryReportLine.numBirdSold);
            object.put("numBirdDead", poultryReportLine.numBirdDead);
            object.put("numEggsSold", poultryReportLine.numEggsSold);
            object.put("birdIncome", poultryReportLine.birdIncome);
            object.put("birdExpenditure", poultryReportLine.birdExpenditure);
            object.put("birdSalesIncome", poultryReportLine.birdSalesIncome);
            object.put("eggSalesIncome", poultryReportLine.eggSalesIncome);
            object.put("regularDeworming", poultryReportLine.regularDeworming);
            object.put("dewormingFrequency", poultryReportLine.dewormingFrequency);
            object.put("remarks", poultryReportLine.remarks);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "poultry-activity/save-line")
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
                                JSONObject resObj = new JSONObject(response);

                                if (resObj.optBoolean("outcome")) {
                                    Toast.makeText(PendingActivity.this, getResources().getString(R.string.update_success), Toast.LENGTH_SHORT).show();

                                    try {
                                        realm.executeTransaction(new Realm.Transaction() {
                                            @Override
                                            public void execute(Realm realm1) {

                                                PoultryReportLine reportLine = realm.where(PoultryReportLine.class)
                                                        .equalTo("unique", uniqueId)
                                                        .findFirst();
//
                                                if (reportLine != null) {

                                                    sendForApproval("poultry-activity/submit", reportLine.monthId, reportLine.year,
                                                            reportLine.schemeId, reportLine.activityId, reportLine.entityId,
                                                            reportLine.entityTypeCode, uniqueId, taskUnique, _task, position);


//                                                    if (reportLine.entityTypeCode.equals("SHG")) {
//                                                        _task.shgCount = _task.shgCount - 1;
//                                                    } else if (reportLine.entityTypeCode.equals("CLF")) {
//                                                        _task.clfCount = _task.clfCount - 1;
//                                                    } else if (reportLine.entityTypeCode.equals("PG")) {
//                                                        _task.pgCount = _task.pgCount - 1;
//                                                    } else if (reportLine.entityTypeCode.equals("EG")) {
//                                                        _task.egCount = _task.egCount - 1;
//                                                    } else if (reportLine.entityTypeCode.equals("HH")) {
//                                                        _task.householdCount = _task.householdCount - 1;
//                                                    }
////
//                                                    Task task = new Task();
//                                                    task.unique = taskUnique;
//                                                    task.householdCount = _task.householdCount;
//                                                    task.shgCount = _task.shgCount;
//                                                    task.clfCount = _task.clfCount;
//                                                    task.egCount = _task.egCount;
//                                                    task.pgCount = _task.pgCount;
//                                                    task.month = _task.month;
//                                                    task.year = _task.year;
//                                                    task.schemeId = _task.schemeId;
//                                                    task.activityId = _task.activityId;
//                                                    task.taskStatus = "PENDING";
//                                                    task.schemeName = _task.schemeName;
//                                                    task.activityName = _task.activityName;
//                                                    task.monthCode = _task.monthCode;
//                                                    task.hashu = _task.hashu;
////
//                                                    realm1.insertOrUpdate(task);
////
//                                                    RealmResults<Task> taskRealmResults = realm.where(Task.class)
//                                                            .equalTo("clfCount", 0)
//                                                            .equalTo("shgCount", 0)
//                                                            .equalTo("pgCount", 0)
//                                                            .equalTo("egCount", 0)
//                                                            .equalTo("householdCount", 0)
//                                                            .findAll();
//
//                                                    taskRealmResults.deleteAllFromRealm();

                                                }

//                                                RealmResults<PoultryReportLine> goatryReportLines = realm.where(PoultryReportLine.class)
//                                                        .equalTo("unique", uniqueId).findAll();
//                                                goatryReportLines.deleteAllFromRealm();
//
//                                                RealmResults<TaskListItem> taskListItems = realm.where(TaskListItem.class)
//                                                        .equalTo("unique", uniqueId)
//                                                        .findAll();
//
//                                                taskListItems.deleteAllFromRealm();
//
//                                                pendingOfflineItemsAdapter.notifyItemRemoved(position);
//
//                                                progressDialogue.dismiss();

                                            }
                                        });
                                    } finally {
                                        if (realm != null) {
                                            realm.close();
                                        }
                                    }

                                } else {
                                    Toast.makeText(PendingActivity.this, resObj.optString("message"), Toast.LENGTH_SHORT).show();
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

    private void saveLineForGoatry(int position, String taskUnique, Task _task, String uniqueId) {

        JSONObject object = new JSONObject();

        try {
            object.put("schemeId", reportLine.schemeId);
            object.put("activityId", reportLine.activityId);
            object.put("year", reportLine.year);
            object.put("monthId", reportLine.monthId);
            object.put("entityId", reportLine.entityId);
            object.put("entityTypeCode", reportLine.entityTypeCode);
            object.put("numGoatSold", reportLine.numGoatSold);
            object.put("totalIncome", reportLine.totalIncome);
            object.put("numGoatBought", reportLine.numGoatBought);
            object.put("totalExpenditure", reportLine.totalExpenditure);
            object.put("numGoatBorn", 0);
            object.put("numGoatDead", reportLine.numGoatDead);
            object.put("numBuckSold", reportLine.numBuckSold);
            object.put("numBuckBought", reportLine.numBuckBought);
            object.put("numBuckBorn", reportLine.numBuckBorn);
            object.put("numBuckDead", reportLine.numBuckDead);
            object.put("regularVaccinated", reportLine.regularVaccinated);
            object.put("numVaccinated", reportLine.numVaccinated);
            object.put("regularDeworming", reportLine.regularDeworming);
            object.put("numDewormed", reportLine.numDewormed);
            object.put("bucksTied", reportLine.bucksTied);
            object.put("remarks", reportLine.remarks);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "goatry-activity/save-line")
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
                                JSONObject resObj = new JSONObject(response);

                                if (resObj.optBoolean("outcome")) {
                                    Toast.makeText(PendingActivity.this, getResources().getString(R.string.update_success), Toast.LENGTH_SHORT).show();

                                    try {
                                        realm.executeTransaction(new Realm.Transaction() {
                                            @Override
                                            public void execute(Realm realm1) {


                                                GoatryReportLine reportLine = realm.where(GoatryReportLine.class)
                                                        .equalTo("unique", uniqueId)
                                                        .findFirst();

                                                if (reportLine != null) {
                                                    sendForApproval("goatry-activity/submit", reportLine.monthId, reportLine.year,
                                                            reportLine.schemeId, reportLine.activityId, reportLine.entityId,
                                                            reportLine.entityTypeCode, uniqueId, taskUnique, _task, position);


//                                                    if (reportLine.entityTypeCode.equals("SHG")) {
//                                                        _task.shgCount = _task.shgCount - 1;
//                                                    } else if (reportLine.entityTypeCode.equals("CLF")) {
//                                                        _task.clfCount = _task.clfCount - 1;
//                                                    } else if (reportLine.entityTypeCode.equals("PG")) {
//                                                        _task.pgCount = _task.pgCount - 1;
//                                                    } else if (reportLine.entityTypeCode.equals("EG")) {
//                                                        _task.egCount = _task.egCount - 1;
//                                                    } else if (reportLine.entityTypeCode.equals("HH")) {
//                                                        _task.householdCount = _task.householdCount - 1;
//                                                    }
//
//                                                    Task task = new Task();
//                                                    task.unique = taskUnique;
//                                                    task.householdCount = _task.householdCount;
//                                                    task.shgCount = _task.shgCount;
//                                                    task.clfCount = _task.clfCount;
//                                                    task.egCount = _task.egCount;
//                                                    task.pgCount = _task.pgCount;
//                                                    task.month = _task.month;
//                                                    task.year = _task.year;
//                                                    task.schemeId = _task.schemeId;
//                                                    task.activityId = _task.activityId;
//                                                    task.taskStatus = "PENDING";
//                                                    task.schemeName = _task.schemeName;
//                                                    task.activityName = _task.activityName;
//                                                    task.monthCode = _task.monthCode;
//                                                    task.hashu = _task.hashu;
//
//                                                    realm1.insertOrUpdate(task);
//
//                                                    RealmResults<Task> taskRealmResults = realm.where(Task.class)
//                                                            .equalTo("clfCount", 0)
//                                                            .equalTo("shgCount", 0)
//                                                            .equalTo("pgCount", 0)
//                                                            .equalTo("egCount", 0)
//                                                            .equalTo("householdCount", 0)
//                                                            .findAll();
//
//                                                    taskRealmResults.deleteAllFromRealm();

                                                }

//                                                RealmResults<GoatryReportLine> goatryReportLines = realm.where(GoatryReportLine.class)
//                                                        .equalTo("unique", uniqueId).findAll();
//                                                goatryReportLines.deleteAllFromRealm();
//
//                                                RealmResults<TaskListItem> taskListItems = realm.where(TaskListItem.class)
//                                                        .equalTo("unique", uniqueId)
//                                                        .findAll();
//
//                                                taskListItems.deleteAllFromRealm();
//                                                pendingOfflineItemsAdapter.notifyItemRemoved(position);
//
//                                                progressDialogue.dismiss();

                                            }
                                        });
                                    } finally {
                                        if (realm != null) {
                                            realm.close();
                                        }
                                    }

                                } else {
                                    Toast.makeText(PendingActivity.this, resObj.optString("message"), Toast.LENGTH_SHORT).show();
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

    private void sendForApproval(String url, int monthId, int year, Long schemeId, Long activityId,
                                 Long entityId, String entityCode, String uniqueId, String taskUnique,
                                 Task _task, int position) {
        binding.progress.setVisibility(View.VISIBLE);
        JSONObject object = new JSONObject();
        try {
            object.put("month", monthId);
            object.put("year", year);
            object.put("schemeId", schemeId);
            object.put("activityId", activityId);
            object.put("entityId", entityId);
            object.put("entityTypeCode", entityCode);
            object.put("newStatus", "SUBMITTED_TO_MBK");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + url)
                .addHeaders("Authorization", "Bearer " + token)
                .setTag("Approval")
                .addJSONObjectBody(object)
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        binding.progress.setVisibility(View.GONE);
                        if (Utility.isStringValid(response)) {
                            try {
                                JSONObject resObj = new JSONObject(response);
                                if (resObj.optBoolean("outcome")) {
                                    removeFromLocalDb(uniqueId, taskUnique, _task, position, url);
                                    showDialog(resObj.optString("message"));
//
                                } else {
                                    showDialog(resObj.optString("message"));
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        binding.progress.setVisibility(View.GONE);
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

    private void removeFromLocalDb(String uniqueId, String taskUnique, Task _task, int position, String url) {


        try {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm1) {

                    if (url.startsWith("goatry")) {
                        GoatryReportLine reportLine = realm.where(GoatryReportLine.class)
                                .equalTo("unique", uniqueId)
                                .findFirst();

                        if (reportLine != null) {

                            if (reportLine.entityTypeCode.equals("SHG")) {
                                _task.shgCount = _task.shgCount - 1;
                            } else if (reportLine.entityTypeCode.equals("CLF")) {
                                _task.clfCount = _task.clfCount - 1;
                            } else if (reportLine.entityTypeCode.equals("PG")) {
                                _task.pgCount = _task.pgCount - 1;
                            } else if (reportLine.entityTypeCode.equals("EG")) {
                                _task.egCount = _task.egCount - 1;
                            } else if (reportLine.entityTypeCode.equals("HH")) {
                                _task.householdCount = _task.householdCount - 1;
                            }

                            Task task = new Task();
                            task.unique = taskUnique;
                            task.householdCount = _task.householdCount;
                            task.shgCount = _task.shgCount;
                            task.clfCount = _task.clfCount;
                            task.egCount = _task.egCount;
                            task.pgCount = _task.pgCount;
                            task.month = _task.month;
                            task.year = _task.year;
                            task.schemeId = _task.schemeId;
                            task.activityId = _task.activityId;
                            task.taskStatus = "PENDING";
                            task.schemeName = _task.schemeName;
                            task.activityName = _task.activityName;
                            task.monthCode = _task.monthCode;
                            task.hashu = _task.hashu;

                            realm1.insertOrUpdate(task);

                            RealmResults<Task> taskRealmResults = realm.where(Task.class)
                                    .equalTo("clfCount", 0)
                                    .equalTo("shgCount", 0)
                                    .equalTo("pgCount", 0)
                                    .equalTo("egCount", 0)
                                    .equalTo("householdCount", 0)
                                    .findAll();

                            taskRealmResults.deleteAllFromRealm();

                        }

                        RealmResults<GoatryReportLine> goatryReportLines = realm.where(GoatryReportLine.class)
                                .equalTo("unique", uniqueId).findAll();
                        goatryReportLines.deleteAllFromRealm();

                        RealmResults<TaskListItem> taskListItems = realm.where(TaskListItem.class)
                                .equalTo("unique", uniqueId)
                                .findAll();

                        taskListItems.deleteAllFromRealm();
                        pendingOfflineItemsAdapter.notifyItemRemoved(position);

                        progressDialogue.dismiss();
                    } else if (url.startsWith("poultry")) {

                        PoultryReportLine reportLine = realm.where(PoultryReportLine.class)
                                .equalTo("unique", uniqueId)
                                .findFirst();
//
                        if (reportLine != null) {


                            if (reportLine.entityTypeCode.equals("SHG")) {
                                _task.shgCount = _task.shgCount - 1;
                            } else if (reportLine.entityTypeCode.equals("CLF")) {
                                _task.clfCount = _task.clfCount - 1;
                            } else if (reportLine.entityTypeCode.equals("PG")) {
                                _task.pgCount = _task.pgCount - 1;
                            } else if (reportLine.entityTypeCode.equals("EG")) {
                                _task.egCount = _task.egCount - 1;
                            } else if (reportLine.entityTypeCode.equals("HH")) {
                                _task.householdCount = _task.householdCount - 1;
                            }
////
                            Task task = new Task();
                            task.unique = taskUnique;
                            task.householdCount = _task.householdCount;
                            task.shgCount = _task.shgCount;
                            task.clfCount = _task.clfCount;
                            task.egCount = _task.egCount;
                            task.pgCount = _task.pgCount;
                            task.month = _task.month;
                            task.year = _task.year;
                            task.schemeId = _task.schemeId;
                            task.activityId = _task.activityId;
                            task.taskStatus = "PENDING";
                            task.schemeName = _task.schemeName;
                            task.activityName = _task.activityName;
                            task.monthCode = _task.monthCode;
                            task.hashu = _task.hashu;
////
                            realm1.insertOrUpdate(task);
////
                            RealmResults<Task> taskRealmResults = realm.where(Task.class)
                                    .equalTo("clfCount", 0)
                                    .equalTo("shgCount", 0)
                                    .equalTo("pgCount", 0)
                                    .equalTo("egCount", 0)
                                    .equalTo("householdCount", 0)
                                    .findAll();

                            taskRealmResults.deleteAllFromRealm();

                        }

                        RealmResults<PoultryReportLine> goatryReportLines = realm.where(PoultryReportLine.class)
                                .equalTo("unique", uniqueId).findAll();
                        goatryReportLines.deleteAllFromRealm();

                        RealmResults<TaskListItem> taskListItems = realm.where(TaskListItem.class)
                                .equalTo("unique", uniqueId)
                                .findAll();

                        taskListItems.deleteAllFromRealm();

                        pendingOfflineItemsAdapter.notifyItemRemoved(position);

                        progressDialogue.dismiss();

                    } else if (url.startsWith("dairy")) {
                        DairyReportLine reportLine = realm.where(DairyReportLine.class)
                                .equalTo("unique", uniqueId)
                                .findFirst();
//
                        if (reportLine != null) {


                            if (reportLine.entityTypeCode.equals("SHG")) {
                                _task.shgCount = _task.shgCount - 1;
                            } else if (reportLine.entityTypeCode.equals("CLF")) {
                                _task.clfCount = _task.clfCount - 1;
                            } else if (reportLine.entityTypeCode.equals("PG")) {
                                _task.pgCount = _task.pgCount - 1;
                            } else if (reportLine.entityTypeCode.equals("EG")) {
                                _task.egCount = _task.egCount - 1;
                            } else if (reportLine.entityTypeCode.equals("HH")) {
                                _task.householdCount = _task.householdCount - 1;
                            }
//
                            Task task = new Task();
                            task.unique = taskUnique;
                            task.householdCount = _task.householdCount;
                            task.shgCount = _task.shgCount;
                            task.clfCount = _task.clfCount;
                            task.egCount = _task.egCount;
                            task.pgCount = _task.pgCount;
                            task.month = _task.month;
                            task.year = _task.year;
                            task.schemeId = _task.schemeId;
                            task.activityId = _task.activityId;
                            task.taskStatus = "PENDING";
                            task.schemeName = _task.schemeName;
                            task.activityName = _task.activityName;
                            task.monthCode = _task.monthCode;
                            task.hashu = _task.hashu;
//
                            realm1.insertOrUpdate(task);
//
                            RealmResults<Task> taskRealmResults = realm.where(Task.class)
                                    .equalTo("clfCount", 0)
                                    .equalTo("shgCount", 0)
                                    .equalTo("pgCount", 0)
                                    .equalTo("egCount", 0)
                                    .equalTo("householdCount", 0)
                                    .findAll();

                            taskRealmResults.deleteAllFromRealm();


                            RealmResults<DairyReportLine> goatryReportLines = realm.where(DairyReportLine.class)
                                    .equalTo("unique", uniqueId).findAll();
                            goatryReportLines.deleteAllFromRealm();

                            RealmResults<TaskListItem> taskListItems = realm.where(TaskListItem.class)
                                    .equalTo("unique", uniqueId)
                                    .findAll();

                            taskListItems.deleteAllFromRealm();

                            pendingOfflineItemsAdapter.notifyItemRemoved(position);

                            progressDialogue.dismiss();
                        }
                    } else if (url.startsWith("fishery")) {
                        FisheryReportLine reportLine = realm.where(FisheryReportLine.class)
                                .equalTo("unique", uniqueId)
                                .findFirst();
//
                        if (reportLine != null) {

                            if (reportLine.entityTypeCode.equals("SHG")) {
                                _task.shgCount = _task.shgCount - 1;
                            } else if (reportLine.entityTypeCode.equals("CLF")) {
                                _task.clfCount = _task.clfCount - 1;
                            } else if (reportLine.entityTypeCode.equals("PG")) {
                                _task.pgCount = _task.pgCount - 1;
                            } else if (reportLine.entityTypeCode.equals("EG")) {
                                _task.egCount = _task.egCount - 1;
                            } else if (reportLine.entityTypeCode.equals("HH")) {
                                _task.householdCount = _task.householdCount - 1;
                            }
//
                            Task task = new Task();
                            task.unique = taskUnique;
                            task.householdCount = _task.householdCount;
                            task.shgCount = _task.shgCount;
                            task.clfCount = _task.clfCount;
                            task.egCount = _task.egCount;
                            task.pgCount = _task.pgCount;
                            task.month = _task.month;
                            task.year = _task.year;
                            task.schemeId = _task.schemeId;
                            task.activityId = _task.activityId;
                            task.taskStatus = "PENDING";
                            task.schemeName = _task.schemeName;
                            task.activityName = _task.activityName;
                            task.monthCode = _task.monthCode;
                            task.hashu = _task.hashu;
//
                            realm1.insertOrUpdate(task);
//
                            RealmResults<Task> taskRealmResults = realm.where(Task.class)
                                    .equalTo("clfCount", 0)
                                    .equalTo("shgCount", 0)
                                    .equalTo("pgCount", 0)
                                    .equalTo("egCount", 0)
                                    .equalTo("householdCount", 0)
                                    .findAll();

                            taskRealmResults.deleteAllFromRealm();


                            RealmResults<FisheryReportLine> goatryReportLines = realm.where(FisheryReportLine.class)
                                    .equalTo("unique", uniqueId).findAll();
                            goatryReportLines.deleteAllFromRealm();

                            RealmResults<TaskListItem> taskListItems = realm.where(TaskListItem.class)
                                    .equalTo("unique", uniqueId)
                                    .findAll();

                            taskListItems.deleteAllFromRealm();

                            pendingOfflineItemsAdapter.notifyItemRemoved(position);

                            progressDialogue.dismiss();

                        }
                    }else if (url.startsWith("farm")){
                        FarmReportLine reportLine = realm.where(FarmReportLine.class)
                                .equalTo("unique", uniqueId)
                                .findFirst();

                        if (reportLine != null) {

                            if (reportLine.entityTypeCode.equals("SHG")) {
                                _task.shgCount = _task.shgCount - 1;
                            } else if (reportLine.entityTypeCode.equals("CLF")) {
                                _task.clfCount = _task.clfCount - 1;
                            } else if (reportLine.entityTypeCode.equals("PG")) {
                                _task.pgCount = _task.pgCount - 1;
                            } else if (reportLine.entityTypeCode.equals("EG")) {
                                _task.egCount = _task.egCount - 1;
                            } else if (reportLine.entityTypeCode.equals("HH")) {
                                _task.householdCount = _task.householdCount - 1;
                            }
//
                            Task task = new Task();
                            task.unique = taskUnique;
                            task.householdCount = _task.householdCount;
                            task.shgCount = _task.shgCount;
                            task.clfCount = _task.clfCount;
                            task.egCount = _task.egCount;
                            task.pgCount = _task.pgCount;
                            task.month = _task.month;
                            task.year = _task.year;
                            task.schemeId = _task.schemeId;
                            task.activityId = _task.activityId;
                            task.taskStatus = "PENDING";
                            task.schemeName = _task.schemeName;
                            task.activityName = _task.activityName;
                            task.monthCode = _task.monthCode;
                            task.hashu = _task.hashu;
//
                            realm1.insertOrUpdate(task);
//
                            RealmResults<Task> taskRealmResults = realm.where(Task.class)
                                    .equalTo("clfCount", 0)
                                    .equalTo("shgCount", 0)
                                    .equalTo("pgCount", 0)
                                    .equalTo("egCount", 0)
                                    .equalTo("householdCount", 0)
                                    .findAll();

                            taskRealmResults.deleteAllFromRealm();


                            RealmResults<FarmReportLine> goatryReportLines = realm.where(FarmReportLine.class)
                                    .equalTo("unique", uniqueId).findAll();
                            goatryReportLines.deleteAllFromRealm();

                            RealmResults<TaskListItem> taskListItems = realm.where(TaskListItem.class)
                                    .equalTo("unique", uniqueId)
                                    .findAll();

                            taskListItems.deleteAllFromRealm();

                            pendingOfflineItemsAdapter.notifyItemRemoved(position);

                            progressDialogue.dismiss();

                        }
                    }


                }
            });
        } finally {
            if (realm != null) {
                realm.close();
            }
        }


    }

    private void showDialog(String message) {
        if (!PendingActivity.this.isFinishing()) {
            new AlertDialog.Builder(this)
                    .setTitle("")
                    .setMessage(message)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Continue with delete operation

                        }
                    }).show();
        }

    }
}