package com.example.shimmersensing.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.MenuItem;

import com.example.shimmersensing.fragment.CountDownFragment;
import com.example.shimmersensing.R;
import com.example.shimmersensing.utilities.ShimmerTrial;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hookedonplay.decoviewlib.events.DecoEvent;

import java.util.List;

public class ShimmerTrialActivity extends AppCompatActivity implements CountDownFragment.OnFragmentInteractionListener {


    private FragmentManager mFragmentManager;
    private CountDownFragment initialFragment;
    private SharedPreferences pref;
    private List<ShimmerTrial> shimmerTrial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shimmer_trial);

        Toolbar mToolbar = findViewById(R.id.toolbar_shimmer);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        pref = getApplicationContext().getSharedPreferences("ShimmerSensingSamplingConfig", 0);

        try {
            Gson gson = new Gson();
            String response = pref.getString("shimmertrial", "");
            shimmerTrial = gson.fromJson(response,
                    new TypeToken<List<ShimmerTrial>>() {
                    }.getType());
        }catch (Exception e) {
            e.printStackTrace();
        }


        mFragmentManager = getSupportFragmentManager();

        loadInitialFragment();



    }

    private void loadInitialFragment()
    {
        CountDownTimer countDown = new CountDownTimer(2000, 1000) {

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                initialFragment = CountDownFragment.newInstance();
                FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragmentContainer, initialFragment);
                fragmentTransaction.commit();
            }

        }.start();

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("provaResume", "onResume: ");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i("provaRestart", "onRestart: ");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getSupportActionBar().setTitle("");
                finishAfterTransition();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
        savedInstanceState.putBoolean("MyBoolean", true);
        savedInstanceState.putDouble("myDouble", 1.9);
        savedInstanceState.putInt("MyInt", 1);
        savedInstanceState.putString("MyString", "Welcome back to Android");
        // etc.
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore UI state from the savedInstanceState.
        // This bundle has also been passed to onCreate.
        boolean myBoolean = savedInstanceState.getBoolean("MyBoolean");
        double myDouble = savedInstanceState.getDouble("myDouble");
        int myInt = savedInstanceState.getInt("MyInt");
        String myString = savedInstanceState.getString("MyString");
    }

    @Override
    public void onFragmentInteraction() {
    }
}
