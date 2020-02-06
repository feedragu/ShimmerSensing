package com.example.shimmersensing.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.view.View;

import com.example.shimmersensing.R;

public class EndTrialActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end_trial);
    }

    public void endApp(View view) {
        this.finishAffinity();
    }
}
