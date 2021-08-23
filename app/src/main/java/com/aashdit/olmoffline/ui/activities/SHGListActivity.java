package com.aashdit.olmoffline.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.aashdit.olmoffline.BuildConfig;
import com.aashdit.olmoffline.adapters.ShgListAdapter;
import com.aashdit.olmoffline.databinding.ActivitySHGListBinding;
import com.aashdit.olmoffline.models.SHG;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

import static android.content.pm.PackageManager.GET_META_DATA;

public class SHGListActivity extends AppCompatActivity implements ShgListAdapter.ShgDetailsListener {

    private static final String TAG = "SHGListActivity";
    private Toolbar mToolbarShgList;

    private ActivitySHGListBinding shgListBinding;
    private List<SHG> shgList;
    private ShgListAdapter shgListAdapter;
    private SharedPrefManager sp;
    private String token;
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
        shgListBinding = ActivitySHGListBinding.inflate(getLayoutInflater());
        setContentView(shgListBinding.getRoot());
        resetTitles();
        sp = SharedPrefManager.getInstance(this);
        token = sp.getStringData(Constant.APP_TOKEN);
        setSupportActionBar(shgListBinding.toolbarShgList);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.MINUTES)
                .readTimeout(10, TimeUnit.MINUTES)
                .writeTimeout(10, TimeUnit.MINUTES)
                .build();
        AndroidNetworking.initialize(this, okHttpClient);

        shgListBinding.rvShgList.setLayoutManager(new LinearLayoutManager(this));
        shgList = new ArrayList<>();

//        SHG shg = new SHG();
//        shg.setShgName("SHG One");
//        shg.setShgDetailsId(12342435L);
//        shg.setShgRegNumber("1235456");
//        shgList.add(shg);
//        shgList.add(shg);
//        shgList.add(shg);
//        shgList.add(shg);

//        for (int i = 0; i < 5; i++) {
//            shgList.add(new SHG("Name "+i,i+""));
//        }
        shgListAdapter = new ShgListAdapter(this,shgList);
        shgListAdapter.setShgDetailsListener(this);
        shgListBinding.rvShgList.setAdapter(shgListAdapter);

        shgListBinding.progress.setVisibility(View.GONE);
        shgListBinding.ivAddShg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent createShgIntent = new Intent(SHGListActivity.this, CreateShgProfileActivity.class);
                createShgIntent.putExtra("INTENT_TYPE","ADD");
                startActivityForResult(createShgIntent,55);
            }
        });

        shgListBinding.swiperefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getAllShgList();
                shgListBinding.swiperefresh.setRefreshing(false);
            }
        });

        getAllShgList();

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 55 && resultCode == RESULT_OK && data != null) {
            if (data.getBooleanExtra("res", false)){
                getAllShgList();
            }
        }
    }

    private void getAllShgList() {
        shgListBinding.progress.setVisibility(View.VISIBLE);
        AndroidNetworking.get(BuildConfig.BASE_URL + Constant.API_PATH + "shg/list")
                .addHeaders("Authorization", "Bearer " + token)
                .setTag("GetAllShg")
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        if (Utility.isStringValid(response)) {
                            shgListBinding.progress.setVisibility(View.GONE);
                            try {
                                JSONObject resObj = new JSONObject(response);
                                if (resObj.optBoolean("outcome")){
                                   JSONArray array = resObj.optJSONArray("data");
                                   if (array != null && array.length() > 0){
                                       shgList.clear();
                                       for (int i = 0; i < array.length(); i++) {
                                           SHG shg  = SHG.parseShg(array.optJSONObject(i));
                                           shgList.add(shg);
                                       }
                                       shgListAdapter.notifyDataSetChanged();
                                   }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        shgListBinding.progress.setVisibility(View.GONE);
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void showShgDetails(int position) {
        Intent createShgIntent = new Intent(SHGListActivity.this, CreateShgProfileActivity.class);
        SHG shg = shgList.get(position);
        createShgIntent.putExtra("INTENT_TYPE","UPDATE");
//        createShgIntent.putExtra("DIST_ID",shgList.);
//        createShgIntent.putExtra("BLOCK_ID",shgList.get);
        createShgIntent.putExtra("SHG_DETAILS",shg);
        createShgIntent.putExtra("SHG_ID",shg.getShgId());
        createShgIntent.putExtra("VILLAGE_ID",shg.getVillageId());
        createShgIntent.putExtra("VILLAGE_NAME",shg.getVillageName());
        createShgIntent.putExtra("SHG_NAME",shg.getShgName());
        createShgIntent.putExtra("SHG_DATE_OF_FORMATION",shg.getDateOfFormation());

        startActivityForResult(createShgIntent,55);
    }
}