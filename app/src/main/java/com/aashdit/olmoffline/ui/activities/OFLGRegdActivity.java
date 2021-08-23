package com.aashdit.olmoffline.ui.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.aashdit.olmoffline.databinding.ActivityOFLGRegdBinding;

public class OFLGRegdActivity extends AppCompatActivity {

    private static final String TAG = "OFLGRegdActivity";
    private ActivityOFLGRegdBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOFLGRegdBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());



    }
}