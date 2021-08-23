package com.aashdit.olmoffline.ui.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.aashdit.olmoffline.databinding.ActivityOFViewMainBinding;

public class OFViewMainActivity extends AppCompatActivity {

    private static final String TAG = "OFViewMainActivity";
    private ActivityOFViewMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOFViewMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}