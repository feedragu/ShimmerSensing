package com.example.shimmersensing.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.transition.TransitionInflater;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.transition.Fade;
import android.transition.TransitionSet;
import android.util.Log;
import android.view.MenuItem;

import com.example.shimmersensing.fragment.CountDownFragment;
import com.example.shimmersensing.R;
import com.example.shimmersensing.fragment.FormFragment;
import com.example.shimmersensing.fragment.IntroductionFragment;
import com.example.shimmersensing.utilities.ShimmerTrial;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

public class ShimmerTrialActivity extends AppCompatActivity implements CountDownFragment.OnFragmentInteractionListener,
        IntroductionFragment.OnFragmentInteractionListener, FormFragment.OnFragmentInteractionListener {


    private FragmentManager mFragmentManager;
    private IntroductionFragment initialFragment;
    private SharedPreferences pref;
    private List<ShimmerTrial> shimmerTrial;
    private static final long MOVE_DEFAULT_TIME = 0;
    private static final long FADE_DEFAULT_TIME = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shimmer_trial);

        Toolbar mToolbar = findViewById(R.id.toolbar_shimmer);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        pref = getApplicationContext().getSharedPreferences("ShimmerSensingSamplingConfig", 0);

        try {
            Gson gson = new Gson();
            String response = pref.getString("shimmertrial", "");
            shimmerTrial = gson.fromJson(response,
                    new TypeToken<List<ShimmerTrial>>() {
                    }.getType());
        } catch (Exception e) {
            e.printStackTrace();
        }


        mFragmentManager = getSupportFragmentManager();

        loadInitialFragment();


    }

    private void loadInitialFragment() {
        initialFragment = IntroductionFragment.newInstance();
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainer, initialFragment);
        fragmentTransaction.commit();

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

    private void performTransition() {
        if (isDestroyed()) {
            return;
        }
        Fragment previousFragment = mFragmentManager.findFragmentById(R.id.fragmentContainer);
        CountDownFragment nextFragment = CountDownFragment.newInstance();

        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();

        // 1. Exit for Previous Fragment
        Fade exitFade = new Fade();
        exitFade.setDuration(FADE_DEFAULT_TIME);
        previousFragment.setExitTransition(exitFade);

        // 2. Shared Elements Transition
        TransitionSet enterTransitionSet = new TransitionSet();
        enterTransitionSet.addTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.move));
        enterTransitionSet.setDuration(MOVE_DEFAULT_TIME);
        enterTransitionSet.setStartDelay(FADE_DEFAULT_TIME);
        nextFragment.setSharedElementEnterTransition(enterTransitionSet);

        // 3. Enter Transition for New Fragment
        Fade enterFade = new Fade();
        enterFade.setStartDelay(MOVE_DEFAULT_TIME + FADE_DEFAULT_TIME);
        enterFade.setDuration(FADE_DEFAULT_TIME);
        nextFragment.setEnterTransition(enterFade);

        //fragmentTransaction.addSharedElement(logo, logo.getTransitionName());
        fragmentTransaction.replace(R.id.fragmentContainer, nextFragment);
        fragmentTransaction.commitAllowingStateLoss();
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
    public void onFragmentInteraction(int event) {
        switch (event) {
            case 1: {
                Log.i("play", "onFragmentInteraction: ");
                break;
            }
            case 2: {
                Log.i("stop", "onFragmentInteraction: ");
                break;
            }
            case 3: {
                Fragment previousFragment = mFragmentManager.findFragmentById(R.id.fragmentContainer);
                Log.i("cachi", "onFragmentInteraction: "+shimmerTrial.get(0).getN_domande().size());
                FormFragment nextFragment = FormFragment.newInstance(shimmerTrial.get(0).getN_domande());

                FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();

                // 1. Exit for Previous Fragment
                Fade exitFade = new Fade();
                exitFade.setDuration(FADE_DEFAULT_TIME);
                previousFragment.setExitTransition(exitFade);

                // 2. Shared Elements Transition
                TransitionSet enterTransitionSet = new TransitionSet();
                enterTransitionSet.addTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.move));
                enterTransitionSet.setDuration(MOVE_DEFAULT_TIME);
                enterTransitionSet.setStartDelay(FADE_DEFAULT_TIME);
                nextFragment.setSharedElementEnterTransition(enterTransitionSet);

                // 3. Enter Transition for New Fragment
                Fade enterFade = new Fade();
                enterFade.setStartDelay(MOVE_DEFAULT_TIME + FADE_DEFAULT_TIME);
                enterFade.setDuration(FADE_DEFAULT_TIME);
                nextFragment.setEnterTransition(enterFade);

                //fragmentTransaction.addSharedElement(logo, logo.getTransitionName());
                fragmentTransaction.replace(R.id.fragmentContainer, nextFragment);
                fragmentTransaction.commitAllowingStateLoss();
                break;
            }
            default:
                break;
        }
    }

    @Override
    public void onFragmentInteractionIntroduction() {
        performTransition();
    }

    @Override
    public void onFragmentInteractionForm() {

    }
}
