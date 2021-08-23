package com.aashdit.olmoffline.ui.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.aashdit.olmoffline.databinding.ActivityPGRegistraionBinding;

public class PGRegistrationActivity extends AppCompatActivity {

    private static final String TAG = "PGRegistrationActivity";

    private ActivityPGRegistraionBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPGRegistraionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


    }
}