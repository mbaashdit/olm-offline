package com.aashdit.olmoffline.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.aashdit.olmoffline.BuildConfig;
import com.aashdit.olmoffline.adapters.ShgMembersListAdapter;
import com.aashdit.olmoffline.databinding.ActivityAddShgMemberBinding;
import com.aashdit.olmoffline.models.ShgMember;
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

import static android.content.pm.PackageManager.GET_META_DATA;

public class ShgMembersActivity extends AppCompatActivity implements ShgMembersListAdapter.ShgMemberDetailsListener {

    private static final String TAG = "AddShgMemberActivity";

    private ActivityAddShgMemberBinding binding;
    private Long shgId = 0L;
    private SharedPrefManager sp;
    private String token;

    private ShgMembersListAdapter membersListAdapter;

    private final ArrayList<ShgMember> members = new ArrayList<>();
    private String intentType;

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
        binding = ActivityAddShgMemberBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        resetTitles();
        sp = SharedPrefManager.getInstance(this);
        token = sp.getStringData(Constant.APP_TOKEN);

        shgId = getIntent().getLongExtra("SHG_ID", 0L);

        setSupportActionBar(binding.toolbarShgMemberList);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        binding.progress.setVisibility(View.GONE);
        binding.rvShgMembers.setLayoutManager(new LinearLayoutManager(this));

        
        binding.ivAddShgMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addShgMemberIntent = new Intent(ShgMembersActivity.this, AddShgMemberActivity.class);
                addShgMemberIntent.putExtra("SHG_ID",shgId);
                addShgMemberIntent.putExtra("INTENT_TYPE","ADD");
                startActivityForResult(addShgMemberIntent,12);
            }
        });

        binding.swiperefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getShgMemberList();
                binding.swiperefresh.setRefreshing(false);
            }
        });


        membersListAdapter = new ShgMembersListAdapter(this,members);
        membersListAdapter.setShgMemberDetailsListener(this);
        binding.rvShgMembers.setAdapter(membersListAdapter);
        getShgMemberList();
    }

    private void getShgMemberList() {
        binding.progress.setVisibility(View.VISIBLE);
        AndroidNetworking.get(BuildConfig.BASE_URL + Constant.API_PATH + "shg/member/list/"+shgId)
                .addHeaders("Authorization", "Bearer " + token)
                .setTag("GetAllShg")
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
                                    JSONArray array = resObj.optJSONArray("data");
                                    if (array != null && array.length() > 0) {
                                        binding.tvNoItem.setVisibility(View.GONE);
                                        members.clear();
                                        for (int i = 0; i < array.length(); i++) {
                                            ShgMember shg = ShgMember.parseShgMember(array.optJSONObject(i));
                                            members.add(shg);
                                        }
                                        membersListAdapter.notifyDataSetChanged();
                                    }
                                } else {
                                    binding.tvNoItem.setVisibility(View.VISIBLE);
                                    members.clear();
                                    membersListAdapter.notifyDataSetChanged();
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void showShgMemberDetails(int position) {
        ShgMember member = members.get(position);
        Intent memberIntent = new Intent(this, AddShgMemberActivity.class);
        memberIntent.putExtra("INTENT_TYPE", "UPDATE");
        memberIntent.putExtra("SHG_MEMBER", member);
        startActivityForResult(memberIntent, 12);
    }

    @Override
    public void deleteShgMember(int position) {
        ShgMember member = members.get(position);
        binding.progress.setVisibility(View.VISIBLE);
        if (member.memberId != null || member.memberId.equals(0L)) {
            AndroidNetworking.post(BuildConfig.BASE_URL + Constant.API_PATH + "shg/deleteShgMemberDetails/" + member.memberId)
                    .addHeaders("Authorization", "Bearer " + token)
                    .setTag("DeleteShgMemberDetails")
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
                                        Toast.makeText(ShgMembersActivity.this, resObj.optString("message"), Toast.LENGTH_SHORT).show();
                                        getShgMemberList();
                                    } else {
                                        Toast.makeText(ShgMembersActivity.this, resObj.optString("message"), Toast.LENGTH_SHORT).show();
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 12 && resultCode == RESULT_OK && data != null) {
            if (data.getBooleanExtra("res", false)) {
                getShgMemberList();
            }
        }
    }
}