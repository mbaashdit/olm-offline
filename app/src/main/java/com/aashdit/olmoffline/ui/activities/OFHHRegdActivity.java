package com.aashdit.olmoffline.ui.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.aashdit.olmoffline.databinding.ActivityOFHHRegdBinding;

public class OFHHRegdActivity extends AppCompatActivity {

    private static final String TAG = "OFHHRegdActivity";
    private ActivityOFHHRegdBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOFHHRegdBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}