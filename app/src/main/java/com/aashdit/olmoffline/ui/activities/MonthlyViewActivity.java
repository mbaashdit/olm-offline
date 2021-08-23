package com.aashdit.olmoffline.ui.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.aashdit.olmoffline.databinding.ActivityMonthlyViewBinding;


public class MonthlyViewActivity extends AppCompatActivity {

    private static final String TAG = "MonthlyViewActivity";
    private ActivityMonthlyViewBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMonthlyViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}