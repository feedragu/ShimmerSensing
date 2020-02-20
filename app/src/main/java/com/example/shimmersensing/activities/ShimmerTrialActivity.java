package com.example.shimmersensing.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionInflater;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.transition.Fade;
import android.transition.TransitionSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.shimmersensing.fragment.AudioFragment;
import com.example.shimmersensing.fragment.CountDownFragment;
import com.example.shimmersensing.R;
import com.example.shimmersensing.fragment.FormFragment;
import com.example.shimmersensing.fragment.IntroductionFragment;
import com.example.shimmersensing.global.GlobalValues;
import com.example.shimmersensing.utilities.ShimmerData;
import com.example.shimmersensing.utilities.ShimmerTrial;
import com.example.shimmersensing.utilities.ShimmerTrialMusic;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ShimmerTrialActivity extends AppCompatActivity implements CountDownFragment.OnFragmentInteractionListener,
        IntroductionFragment.OnFragmentInteractionListener, FormFragment.OnFragmentInteractionListener, AudioFragment.OnFragmentInteractionListener {


    private FragmentManager mFragmentManager;
    private IntroductionFragment initialFragment;
    private SharedPreferences pref;
    private List<ShimmerTrial> shimmerTrial;
    private List<ShimmerTrial> shimmerTrialProgress;
    private List<ShimmerData> shimmerData;
    private List<ShimmerData> shimmerDataProgress = new ArrayList<>();
    private ProgressBar trialProgress;
    private int counterShimmerDEBUG = 0;
    private static final long MOVE_DEFAULT_TIME = 200;
    private static final long FADE_DEFAULT_TIME = 250;
    private long msuntilfinish = 10000;
    private CountDownTimer cTimer;
    private GlobalValues gv;
    private int[] values;
    private long countdowninterval = 3;
    private int shimmerProgress = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        setContentView(R.layout.shimmer_trial);

        Transition enter = TransitionInflater.from(this).inflateTransition(R.transition.explode_trial);
        Transition exit = TransitionInflater.from(this).inflateTransition(R.transition.explode_trial_exit);

        getWindow().setEnterTransition(new Slide());

        getWindow().setExitTransition(new Slide());

        Toolbar mToolbar = findViewById(R.id.toolbar_shimmer);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        trialProgress = findViewById(R.id.trialProgress);

        gv = (GlobalValues) getApplicationContext();
        pref = getApplicationContext().getSharedPreferences("ShimmerSensingSamplingConfig", 0);


        shimmerTrial = gv.getShimmerTrialArrayList();
        try {
            Gson gson = new Gson();
            String response = pref.getString("shimmerdata", "");
            shimmerData = gson.fromJson(response,
                    new TypeToken<List<ShimmerData>>() {
                    }.getType());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (shimmerTrialProgress == null) {
            try {
                shimmerTrialProgress = cloneList(shimmerTrial);
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
            trialProgress.setMax(shimmerTrial.size());
            Log.i("shimmerprogress", "onCreate: " + shimmerTrialProgress.size());
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

    @SuppressLint("ShowToast")
    private void performTransition() {
        if (isDestroyed()) {
            return;
        }

        Fragment nextFragment = null;
        Log.i("test_mode", "performTransition: "+shimmerTrialProgress.get(0).getMode());
        Fragment previousFragment = mFragmentManager.findFragmentById(R.id.fragmentContainer);
        switch (shimmerTrialProgress.get(0).getMode()) {
            case "Musica" : {
                nextFragment = AudioFragment.newInstance((ShimmerTrialMusic) shimmerTrialProgress.get(0));

                Log.i("Musica", "performTransition: ");
                break;
            }
            case "Lettura" : {
                nextFragment = AudioFragment.newInstance((ShimmerTrialMusic) shimmerTrialProgress.get(0));
                Log.i("Lettura", "performTransition: ");
                break;
            }
            case "Countdown" : {
                long countdown_timer = Integer.parseInt(shimmerTrialProgress.get(0).getTrialDuration()) * 1000;
                msuntilfinish = countdown_timer;
                nextFragment = CountDownFragment.newInstance(shimmerTrialProgress.get(0).getTrialName(), shimmerTrialProgress.get(0).getUrl_icon(), countdown_timer);
                Log.i("CountDown", "performTransition: ");
                break;
            }
            case "Prompt" : {
                long countdown_timer = Integer.parseInt(shimmerTrialProgress.get(0).getTrialDuration()) * 1000;
                msuntilfinish = countdown_timer;
                nextFragment = CountDownFragment.newInstance(shimmerTrialProgress.get(0).getTrialName(), shimmerTrialProgress.get(0).getUrl_icon(), countdown_timer);
                Log.i("Prompt", "performTransition: ");
                break;
            }
            default : {
                Toast.makeText(this, "Errore", Toast.LENGTH_LONG);
            }
        }

        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();

        // 1. Exit for Previous Fragment
        Slide exitFade = new Slide(Gravity.LEFT);
        exitFade.setDuration(FADE_DEFAULT_TIME);
        assert previousFragment != null;
        previousFragment.setExitTransition(exitFade);

        // 2. Shared Elements Transition
//        TransitionSet enterTransitionSet = new TransitionSet();
//        enterTransitionSet.addTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.move));
//        enterTransitionSet.setDuration(MOVE_DEFAULT_TIME);
//        enterTransitionSet.setStartDelay(FADE_DEFAULT_TIME);
//        nextFragment.setSharedElementEnterTransition(enterTransitionSet);

        // 3. Enter Transition for New Fragment
        Slide enterFade = new Slide(Gravity.RIGHT);
        enterFade.setStartDelay(MOVE_DEFAULT_TIME);
        enterFade.setDuration(FADE_DEFAULT_TIME);
        nextFragment.setEnterTransition(enterFade);

        //fragmentTransaction.addSharedElement(logo, logo.getTransitionName());
        fragmentTransaction.replace(R.id.fragmentContainer, nextFragment);
        fragmentTransaction.commit();
    }

    private void updateProgressTrial() {
        shimmerTrialProgress.remove(0);

        if((shimmerTrial.size() - shimmerTrialProgress.size()) == shimmerTrial.size()) {
            finishAffinity();
            startActivity(new Intent(this, EndTrialActivity.class));
        }


        trialProgress.setProgress((shimmerTrial.size() - shimmerTrialProgress.size()), true);

        Log.i("shimmerprogress_orig", "onCreate: " + shimmerTrial.size());
        Log.i("shimmerprogress", "onCreate: " + shimmerTrialProgress.size());
        Fragment previousFragment = mFragmentManager.findFragmentById(R.id.fragmentContainer);
        IntroductionFragment nextFragment = IntroductionFragment.newInstance();

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
        msuntilfinish=10000;
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

    public static List<ShimmerTrial> cloneList(List<ShimmerTrial> list) throws CloneNotSupportedException {
        List<ShimmerTrial> clone = new ArrayList<ShimmerTrial>(list.size());
        for (ShimmerTrial item : list) clone.add((ShimmerTrial) item.clone());
        return clone;
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
    public void onFragmentInteraction(int code) {
        fragment_handler(code);

    }



    public void sendShimmerDataInitial() {
        try {
            Gson gson = new Gson();
            String listString = gson.toJson(
                    shimmerDataProgress,
                    new TypeToken<ArrayList<ShimmerData>>() {
                    }.getType());
            JSONArray jsonArray = new JSONArray(listString);
            JSONObject obj = new JSONObject();
            try {
                obj.put("Nome", gv.getName());
                obj.put("Cognome", gv.getSurname());

                JSONObject jobjInner = new JSONObject();
                jobjInner.put("trial_name", shimmerTrialProgress.get(0).getTrialName());
                jobjInner.put("trial_duration", shimmerTrialProgress.get(0).getTrialDuration());
                jobjInner.put("trial_mode", shimmerTrialProgress.get(0).getMode());
                jobjInner.put("shimmerdata", jsonArray);
                for (int i = 0; i < values.length; i++) {
                    jobjInner.put(shimmerTrialProgress.get(0).getN_domande().get(i).getNome_domanda(), values[i]);
                }
                JSONObject jobjInnerDate = new JSONObject();
                jobjInnerDate.put("Trial1", jobjInner);
                obj.put(gv.getDate(), jobjInnerDate);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            JsonArray jArray = new JsonArray();
            jArray.add(String.valueOf(obj));
            new SendDeviceDetails().execute("http://192.168.1.16:5000/api/v1/resources/shimmersensing/sensordata", String.valueOf(obj));

            Log.i("im sending 2", "run: send " + shimmerDataProgress.size());
            shimmerDataProgress.clear();
            Log.i("im sending 3", "run: send " + shimmerDataProgress.size());
        } catch (Exception e) {
            e.printStackTrace();
        }

        shimmerDataProgress.clear();
    }

    public void sendShimmerDataUpdate() {
        try {
            Gson gson = new Gson();
            String listString = gson.toJson(
                    shimmerDataProgress,
                    new TypeToken<ArrayList<ShimmerData>>() {
                    }.getType());
            JSONArray jsonArray = new JSONArray(listString);
            JSONObject obj = new JSONObject();
            JSONObject jobjInnerDate = new JSONObject();
            try {
                obj.put("Nome", gv.getName());
                obj.put("Cognome", gv.getSurname());

                JSONObject jobjInner = new JSONObject();
                jobjInner.put("trial_name", shimmerTrialProgress.get(0).getTrialName());
                jobjInner.put("trial_duration", shimmerTrialProgress.get(0).getTrialDuration());
                jobjInner.put("trial_mode", shimmerTrialProgress.get(0).getMode());
                jobjInner.put("shimmerdata", jsonArray);
                for (int i = 0; i < values.length; i++) {
                    jobjInner.put(shimmerTrial.get(shimmerTrial.size() - shimmerTrialProgress.size()).getN_domande().get(i).getNome_domanda(), values[i]);
                }

                jobjInnerDate.put("_id", gv.get_id());
                jobjInnerDate.put("date_trial", gv.getDate());
                jobjInnerDate.put("trialnumber", "Trial"+shimmerProgress);
                jobjInnerDate.put("Trial"+shimmerProgress, jobjInner);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            JsonArray jArray = new JsonArray();
            jArray.add(String.valueOf(obj));
            new SendDeviceDetails().execute("http://192.168.1.16:5000/api/v1/resources/shimmersensing/sensordata/update", String.valueOf(jobjInnerDate));

            Log.i("im sending 2", "run: send " + shimmerDataProgress.size());
            shimmerDataProgress.clear();
            Log.i("im sending 3", "run: send " + shimmerDataProgress.size());
        } catch (Exception e) {
            e.printStackTrace();
        }

        shimmerDataProgress.clear();
    }

    @Override
    public void onFragmentInteractionIntroduction() {
        performTransition();
    }

    @Override
    public void onFragmentInteractionForm(int[] voti) {
        values = voti;
        for (int i : voti) {
            Log.i("caac", "onFragmentInteractionForm: " + i);
        }
        if(shimmerProgress == 1) {
            sendShimmerDataInitial();
        }else {
            sendShimmerDataUpdate();
        }
        updateProgressTrial();

    }


    @Override
    public void onFragmentInteractionAudio(int code) {
        fragment_handler(code);
    }

    private void fragment_handler(int event) {
        switch (event) {
            case 1: {
                Log.i("play", "onFragmentInteraction: ");
                cTimer = new CountDownTimer(msuntilfinish, countdowninterval) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        msuntilfinish = millisUntilFinished;
                        double tsLong = System.currentTimeMillis() / 1000;
                        shimmerData.get(counterShimmerDEBUG).setTimestamp_shimmer(tsLong);
                        shimmerDataProgress.add(shimmerData.get(counterShimmerDEBUG));
                        Log.i("shimmerdatadebug", shimmerDataProgress.get(counterShimmerDEBUG).toString());
                        counterShimmerDEBUG++;
                        if (counterShimmerDEBUG == shimmerData.size() - 1)
                            counterShimmerDEBUG = 0;

                    }

                    @Override
                    public void onFinish() {
                        counterShimmerDEBUG = 0;
                    }
                }.start();
                break;
            }
            case 2: {
                Log.i("stop", "onFragmentInteraction: ");
                cTimer.cancel();
                break;
            }
            case 3: {
                Fragment previousFragment = mFragmentManager.findFragmentById(R.id.fragmentContainer);
                Log.i("cachi", "onFragmentInteraction: " + shimmerTrial.get(0).getN_domande().size());
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
            case 69: {
                cTimer = new CountDownTimer(msuntilfinish, countdowninterval) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        msuntilfinish = millisUntilFinished;
                        double tsLong = System.currentTimeMillis() / 1000;
                        shimmerData.get(counterShimmerDEBUG).setTimestamp_shimmer(tsLong);
                        shimmerDataProgress.add(shimmerData.get(counterShimmerDEBUG));
                        Log.i("shimmerdatadebug", shimmerDataProgress.get(counterShimmerDEBUG).toString());
                        counterShimmerDEBUG++;
                        if (counterShimmerDEBUG == shimmerData.size() - 1)
                            counterShimmerDEBUG = 0;

                    }

                    @Override
                    public void onFinish() {
                        counterShimmerDEBUG = 0;
                    }
                }.start();
            }
            default:
                break;
        }
    }

    public class SendDeviceDetails extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String data = "";
            Log.i("Prova", "cachi");
            HttpURLConnection httpURLConnection = null;
            try {

                httpURLConnection = (HttpURLConnection) new URL(params[0]).openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);

                DataOutputStream wr = new DataOutputStream(httpURLConnection.getOutputStream());
                String s=params[1];
                Log.i("mannag", "doInBackground: "+s);
                wr.writeBytes("postdata="+ s);
                wr.flush();
                wr.close();

                InputStream in = httpURLConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(in);

                int inputStreamData = inputStreamReader.read();
                while (inputStreamData != -1) {
                    char current = (char) inputStreamData;
                    inputStreamData = inputStreamReader.read();
                    data += current;
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
            }

            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if(shimmerProgress == 1) {
                GlobalValues gv = (GlobalValues) getApplicationContext();
                gv.set_id(result);
            }
            shimmerProgress++;
            Log.i("TAG", result); // this is expecting a response code to be sent from your server upon receiving the POST data
        }
    }


}
