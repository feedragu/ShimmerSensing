package com.example.shimmersensing.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.transition.Fade;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.transition.TransitionSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.shimmersensing.R;
import com.example.shimmersensing.fragment.AudioFragment;
import com.example.shimmersensing.fragment.CountDownFragment;
import com.example.shimmersensing.fragment.FormFragment;
import com.example.shimmersensing.fragment.IntroductionFragment;
import com.example.shimmersensing.global.GlobalValues;
import com.example.shimmersensing.interfaccia.Shimmer_interface;
import com.example.shimmersensing.utilities.ShimmerData;
import com.example.shimmersensing.utilities.ShimmerTrial;
import com.example.shimmersensing.utilities.ShimmerTrialMusic;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.shimmerresearch.algorithms.Filter;
import com.shimmerresearch.android.Shimmer;
import com.shimmerresearch.android.manager.ShimmerBluetoothManagerAndroid;
import com.shimmerresearch.biophysicalprocessing.ECGtoHRAdaptive;
import com.shimmerresearch.biophysicalprocessing.PPGtoHRAlgorithm;
import com.shimmerresearch.biophysicalprocessing.PPGtoHRwithHRV;
import com.shimmerresearch.bluetooth.ShimmerBluetooth;
import com.shimmerresearch.driver.CallbackObject;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.exceptions.ShimmerException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ShimmerTrialActivity extends AppCompatActivity implements CountDownFragment.OnFragmentInteractionListener,
        IntroductionFragment.OnFragmentInteractionListener, FormFragment.OnFragmentInteractionListener, AudioFragment.OnFragmentInteractionListener, Shimmer_interface {


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

    ShimmerBluetoothManagerAndroid btManager;
    ShimmerDevice shimmerDevice = null;
    String shimmerBtAdd = "00:00:00:00:00:00";
    TextView macAddr;
    boolean connected = false;
    Button connect_start;
    ProgressDialog nDialog;
    PPGtoHRwithHRV mPPGtoHR;
    Filter mLPFilter;
    double[] mLPFc = {5};


    private static Integer INVALID_OUTPUT = -1;
    private static int mNumberOfBeatsToAverage = 5;
    private static Shimmer mShimmerHeartRate;
    private static PPGtoHRAlgorithm mHeartRateCalculation;
    private static ECGtoHRAdaptive mHeartRateCalculationECG;
    static Filter mHPFilter;
    public static String mBluetoothAddressToHeartRate;
    static private boolean mEnableHeartRate = false;
    static private boolean mEnableHeartRateECG = false;
    static private boolean mNewPPGSignalProcessing = true;
    static private boolean mNewECGSignalProcessing = true;
    static private double[] mHPFc = {0.5};
    static private double[] mLPFcECG = {51.2};
    static private double[] mHPFcECG = {0.5};
    private static int mCount = 0;
    private static int mCountPPGInitial = 0; //skip first 100 samples
    private static int mRefreshLimit = 10;
    private static double mCurrentHR = -1;
    private static String mSensortoHR = "";


    final static String LOG_TAG = "ShimmerProject";
    private double sampleRate;
    private boolean not_config_yet = true;
    private GraphView graph;
    private LineGraphSeries<DataPointInterface> mSeries2;
    private GraphView graph2, graph_gsr, graph_gsr_res;
    private LineGraphSeries<DataPointInterface> mSeriesX, mSeriesY, mSeriesZ, mSeriesGsr, mSeriesGsrResistance;
    private Bitmap graph_view_PPG, graph_view_Acc;
    private boolean onpause = false;
    private Dialog myDialog;
    private Button btnConfirm;
    private SeekBar sampling_bar, overlap_bar;
    private TextView sampling_text, overlap_text;
    private int sampling_time_progress, overlap_time_progress;
    private ArrayList<ShimmerData> list;


    Handler handler = new Handler();
    Handler handlerTest = new Handler();
    Runnable runnable;
    int delay = 300 * 1000; //Delay for 15 seconds.  One second = 1000 milliseconds.
    int hztoms = 7; //Delay for 15 seconds.  One second = 1000 milliseconds.
    int counterCsv = 0;
    private String jsonDB;
    private AlertDialog.Builder alertD;
    private AlertDialog alertdialog;

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


        if (!DEBUG_SHIMMER) {
            try {
                btManager = new ShimmerBluetoothManagerAndroid(ShimmerTrialActivity.this, mHandler);
                shimmerBtAdd = gv.getSsd().getMacAddress();
                btManager.connectShimmerThroughBTAddress(gv.getSsd().getMacAddress());
                alertD = new AlertDialog.Builder(ShimmerTrialActivity.this);
                alertD.setView(R.layout.progress_dialog);

                alertdialog = alertD.show();
                alertdialog.setCanceledOnTouchOutside(false);
                Log.i("testconnection", "onCreate: " + gv.getSsd().getMacAddress());
            } catch (Exception e) {
                Log.e("test_conn", "Couldn't create ShimmerBluetoothManagerAndroid. Error: " + e);
            }
        }

        mFragmentManager = getSupportFragmentManager();


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
//        if (!DEBUG_SHIMMER)
//            shimmerDevice.startStreaming();

        Fragment nextFragment = null;
        Log.i("test_mode", "performTransition: " + shimmerTrialProgress.get(0).getMode());
        Fragment previousFragment = mFragmentManager.findFragmentById(R.id.fragmentContainer);
        switch (shimmerTrialProgress.get(0).getMode()) {
            case "Musica": {
                nextFragment = AudioFragment.newInstance((ShimmerTrialMusic) shimmerTrialProgress.get(0));

                Log.i("Musica", "performTransition: ");
                break;
            }
            case "Lettura": {
                long countdown_timer = (long) ((Float.parseFloat(shimmerTrialProgress.get(0).getTrialDuration()) * 1000));
                msuntilfinish = countdown_timer;
                nextFragment = CountDownFragment.newInstance(shimmerTrialProgress.get(0).getTrialName(), shimmerTrialProgress.get(0).getUrl_icon(), countdown_timer);

                Log.i("Lettura", "performTransition: ");
                break;
            }
            case "Countdown": {
                long countdown_timer = (long) ((Float.parseFloat(shimmerTrialProgress.get(0).getTrialDuration()) * 1000));
                msuntilfinish = countdown_timer;
                nextFragment = CountDownFragment.newInstance(shimmerTrialProgress.get(0).getTrialName(), shimmerTrialProgress.get(0).getUrl_icon(), countdown_timer);
                Log.i("CountDown", "performTransition: ");
                break;
            }
            case "Prompt": {
                long countdown_timer = (long) ((Float.parseFloat(shimmerTrialProgress.get(0).getTrialDuration()) * 1000));
                msuntilfinish = countdown_timer;
                nextFragment = CountDownFragment.newInstance(shimmerTrialProgress.get(0).getTrialName(), shimmerTrialProgress.get(0).getUrl_icon(), countdown_timer);
                Log.i("Prompt", "performTransition: ");
                break;
            }
            default: {
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

        if ((shimmerTrial.size() - shimmerTrialProgress.size()) == shimmerTrial.size()) {
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
        msuntilfinish = 10000;
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
            new SendDeviceDetails().execute(URL_SERVER + "insertdatashimmer", String.valueOf(obj));

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
                jobjInnerDate.put("trialnumber", "Trial" + shimmerProgress);
                jobjInnerDate.put("Trial" + shimmerProgress, jobjInner);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            JsonArray jArray = new JsonArray();
            jArray.add(String.valueOf(obj));
            new SendDeviceDetails().execute(URL_SERVER + "update", String.valueOf(jobjInnerDate));

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
        if (shimmerProgress == 1) {
            sendShimmerDataInitial();
        } else {
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
                if (DEBUG_SHIMMER) {
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
                } else {
                    shimmerDevice.startStreaming();
                }
                break;
            }
            case 2: {
                Log.i("stop", "onFragmentInteraction: ");
                if (DEBUG_SHIMMER) {
                    cTimer.cancel();
                } else {
                    shimmerDevice.stopStreaming();
                }

                break;
            }
            case 3: {
                if (DEBUG_SHIMMER) {
                    cTimer.cancel();
                } else {
                    shimmerDevice.stopStreaming();
                }
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
                if (DEBUG_SHIMMER) {
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
                } else {
                    shimmerDevice.startStreaming();
                }

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
                String s = params[1];
                Log.i("mannag", "doInBackground: " + s);
                wr.writeBytes("postdata=" + s);
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
            if (shimmerProgress == 1) {
                GlobalValues gv = (GlobalValues) getApplicationContext();
                gv.set_id(result);
            }
            shimmerProgress++;
            Log.i("TAG", result); // this is expecting a response code to be sent from your server upon receiving the POST data
        }
    }

    private void showDialogShimmer() {
        alertdialog.dismiss();
        alertdialog = new AlertDialog.Builder(this)
                .setTitle("Non connesso")
                .setMessage("Non è stato possibile connettersi al dispositivo shimmer")
                .setIcon(R.drawable.s_logo_shimmer)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ShimmerTrialActivity.this.finish();
                    }
                })
                .show();
    }

    private void showDialogShimmerDisconnetted() {
        alertdialog.dismiss();
        alertdialog = new AlertDialog.Builder(this)
                .setTitle("Shimmer disconnesso")
                .setMessage("Il dispositivo è stasto disconnesso, prego riprovare")
                .setIcon(R.drawable.s_logo_shimmer)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ShimmerTrialActivity.this.finish();
                    }
                })
                .show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.i("stop", "onFragmentInteraction: ");
        if (DEBUG_SHIMMER) {
            cTimer.cancel();
        } else {
            if (shimmerDevice.isStreaming())
                shimmerDevice.stopStreaming();
        }
    }

    @Override
    protected void onDestroy() {
        if (shimmerDevice != null) {
            if (shimmerDevice.isStreaming() | shimmerDevice.isSDLogging()) {
                shimmerDevice.stopStreaming();
                try {
                    shimmerDevice.disconnect();
                } catch (ShimmerException e) {
                    e.printStackTrace();
                }

            }


        }
        handler.removeCallbacks(runnable); //stop handler when activity not visible
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        //Disconnect the Shimmer device when app is stopped
        if (shimmerDevice != null) {
            if (shimmerDevice.isSDLogging()) {
                shimmerDevice.stopSDLogging();
                Log.d(LOG_TAG, "Stopped Shimmer Logging");
            } else if (shimmerDevice.isStreaming()) {
                shimmerDevice.stopStreaming();
                Log.d(LOG_TAG, "Stopped Shimmer Streaming");
            } else {
                shimmerDevice.stopStreamingAndLogging();
                Log.d(LOG_TAG, "Stopped Shimmer Streaming and Logging");
            }
        }
        btManager.disconnectAllDevices();
        Log.i(LOG_TAG, "Shimmer DISCONNECTED");
        super.onStop();
    }

    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case ShimmerBluetooth.MSG_IDENTIFIER_DATA_PACKET:
                    if ((msg.obj instanceof ObjectCluster)) {

                        ObjectCluster objectCluster = (ObjectCluster) msg.obj;
                        double gsrConductance = 0;
                        double gsrResistance = 0;
                        double ppg = 0;
                        double temperature = 0;
                        double accel_x = 6;
                        double accel_z = 6;
                        double accel_y = 6;
                        double magnetometer_x = 0, magnetometer_y = 0, magnetometer_z = 0;
                        double gyroscope_x = 0, gyroscope_y = 0, gyroscope_z = 0;
                        double timestamp = 0;


                        Collection<FormatCluster> allFormats = objectCluster.getCollectionOfFormatClusters(Configuration.Shimmer3.ObjectClusterSensorName.TIMESTAMP);
                        FormatCluster formatCluster = ((FormatCluster) ObjectCluster.returnFormatCluster(allFormats, "CAL"));
//                        double timeStampData = formatCluster.mData;
//                        Log.i(LOG_TAG, "Time Stamp: " + timeStampData);
                        timestamp = formatCluster.mData;
                        double dataPPG = 0;
                        allFormats = objectCluster.getCollectionOfFormatClusters("PPG_A13");
                        formatCluster = (ObjectCluster.returnFormatCluster(allFormats, "CAL"));
                        if (formatCluster != null) {
                            dataPPG = formatCluster.mData;
                        }

                        double timeStampPPG = 0;
//                        Collection<FormatCluster> formatClusterTimeStamp = objectCluster.mPropertyCluster.get("Timestamp");
//                        FormatCluster timeStampCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(formatClusterTimeStamp,"CAL"));
//                        if (timeStampCluster!=null) {
//                            timeStampPPG = ((FormatCluster)ObjectCluster.returnFormatCluster(formatClusterTimeStamp,"CAL")).mData;
//                        }

                        double lpFilteredDataPPG = 0;
//		            		double hpFilteredDataPPG = 0;
                        try {
                            lpFilteredDataPPG = mLPFilter.filterData(dataPPG);
//								hpFilteredDataPPG = lpFilteredDataPPG;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        double heartRate = Double.NaN;
                        if (mCountPPGInitial < sampleRate * 2) { //skip first 2 seconds
                            mCountPPGInitial++;
                        } else {
                            heartRate = mHeartRateCalculation.ppgToHrConversion(lpFilteredDataPPG, timestamp);

                            if (heartRate == INVALID_OUTPUT) {
                                heartRate = Double.NaN;
                            }


                        }
                        //objectCluster.mPropertyCluster.put("Heart Rate", new FormatCluster("CAL", "bpm", heartRate));
                        Log.d("shimmerSensingHeartRate", "HR: " + heartRate);

                        if (formatCluster != null) {
                            gsrConductance = formatCluster.mData;
                        }
                        allFormats = objectCluster.getCollectionOfFormatClusters(Configuration.Shimmer3.ObjectClusterSensorName.GSR_RESISTANCE);
                        formatCluster = (ObjectCluster.returnFormatCluster(allFormats, "CAL"));
                        if (formatCluster != null) {
                            gsrResistance = formatCluster.mData;
                        }
                        allFormats = objectCluster.getCollectionOfFormatClusters("PPG_A13");
                        formatCluster = (ObjectCluster.returnFormatCluster(allFormats, "CAL"));
                        if (formatCluster != null) {
                            ppg = formatCluster.mData;
                        }
//                        allFormats = objectCluster.getCollectionOfFormatClusters("Temperature_BMP280");
//                        formatCluster = (ObjectCluster.returnFormatCluster(allFormats, "CAL"));
//                        if (formatCluster != null) {
//                            temperature = formatCluster.mData;
//                        }

                        allFormats = objectCluster.getCollectionOfFormatClusters(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_X);
                        formatCluster = (ObjectCluster.returnFormatCluster(allFormats, "CAL"));
                        if (formatCluster != null) {
                            accel_x = formatCluster.mData;
                        }

                        allFormats = objectCluster.getCollectionOfFormatClusters(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_Z);
                        formatCluster = (ObjectCluster.returnFormatCluster(allFormats, "CAL"));
                        if (formatCluster != null) {
                            accel_z = formatCluster.mData;
                        }

                        allFormats = objectCluster.getCollectionOfFormatClusters(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_Y);
                        formatCluster = (ObjectCluster.returnFormatCluster(allFormats, "CAL"));
                        if (formatCluster != null) {
                            accel_y = formatCluster.mData;
                        }

                        allFormats = objectCluster.getCollectionOfFormatClusters(Configuration.Shimmer3.ObjectClusterSensorName.GYRO_X);
                        formatCluster = (ObjectCluster.returnFormatCluster(allFormats, "CAL"));
                        if (formatCluster != null) {
                            gyroscope_x = formatCluster.mData;
                        }

                        allFormats = objectCluster.getCollectionOfFormatClusters(Configuration.Shimmer3.ObjectClusterSensorName.GYRO_Y);
                        formatCluster = (ObjectCluster.returnFormatCluster(allFormats, "CAL"));
                        if (formatCluster != null) {
                            gyroscope_y = formatCluster.mData;
                        }

                        allFormats = objectCluster.getCollectionOfFormatClusters(Configuration.Shimmer3.ObjectClusterSensorName.GYRO_Z);
                        formatCluster = (ObjectCluster.returnFormatCluster(allFormats, "CAL"));
                        if (formatCluster != null) {
                            gyroscope_z = formatCluster.mData;
                        }

                        allFormats = objectCluster.getCollectionOfFormatClusters(Configuration.Shimmer3.ObjectClusterSensorName.MAG_X);
                        formatCluster = (ObjectCluster.returnFormatCluster(allFormats, "CAL"));
                        if (formatCluster != null) {
                            magnetometer_x = formatCluster.mData;
                        }

                        allFormats = objectCluster.getCollectionOfFormatClusters(Configuration.Shimmer3.ObjectClusterSensorName.MAG_Y);
                        formatCluster = (ObjectCluster.returnFormatCluster(allFormats, "CAL"));
                        if (formatCluster != null) {
                            magnetometer_y = formatCluster.mData;
                        }

                        allFormats = objectCluster.getCollectionOfFormatClusters(Configuration.Shimmer3.ObjectClusterSensorName.MAG_Z);
                        formatCluster = (ObjectCluster.returnFormatCluster(allFormats, "CAL"));
                        if (formatCluster != null) {
                            magnetometer_z = formatCluster.mData;
                        }


                        Log.d(LOG_TAG, "DATA_PACKET: " +
                                "\n GSR CONDUCTANCE: " + gsrConductance +
                                "\n PPG: " + ppg +
                                "\n ACCELERATION_X: " + accel_x +
                                "\n ACCELERATION_Z: " + accel_z +
                                "\n ACCELERATION_Y: " + accel_y +
                                "\n ACCELERATION_X: " + accel_x +
                                "\n ACCELERATION_Z: " + accel_z +
                                "\n gyroscope_x: " + gyroscope_x +
                                "\n gyroscope_y: " + gyroscope_y +
                                "\n gyroscope_z: " + gyroscope_z +
                                "\n magnetometer_x: " + magnetometer_x +
                                "\n magnetometer_y: " + magnetometer_y +
                                "\n magnetometer_z: " + magnetometer_z);

                        double tsLong = System.currentTimeMillis() / 1000;
                        ShimmerData s = new ShimmerData(dataPPG, gsrConductance, accel_x,
                                accel_y, accel_z, gyroscope_x, gyroscope_y, gyroscope_z,
                                magnetometer_x, magnetometer_y, magnetometer_z, tsLong);
                        shimmerDataProgress.add(s);

                    }
                    break;

                case ShimmerBluetooth.MSG_IDENTIFIER_STATE_CHANGE:
                    ShimmerBluetooth.BT_STATE state = null;
                    String macAddress = "";

                    if (msg.obj instanceof ObjectCluster) {
                        state = ((ObjectCluster) msg.obj).mState;
                        macAddress = ((ObjectCluster) msg.obj).getMacAddress();
                    } else if (msg.obj instanceof CallbackObject) {
                        state = ((CallbackObject) msg.obj).mState;
                        macAddress = ((CallbackObject) msg.obj).mBluetoothAddress;
                    }

                    Log.d(LOG_TAG, "Shimmer state changed! Shimmer = " + macAddress + ", new state = " + state);

                    switch (state) {
                        case CONNECTED:
                            Log.i(LOG_TAG, "Shimmer [" + macAddress + "] is now CONNECTED");
                            shimmerDevice = btManager.getShimmerDeviceBtConnectedFromMac(shimmerBtAdd);
                            if (shimmerDevice != null) {

                                Log.i(LOG_TAG, "Got the ShimmerDevice!");
                                if (not_config_yet) {
                                    alertdialog.dismiss();
                                    loadInitialFragment();
                                    sampleRate = shimmerDevice.getSamplingRateShimmer();
                                    connected = true;
                                    mNewPPGSignalProcessing = true;
                                    mCountPPGInitial = 0;
                                    mHeartRateCalculation = new PPGtoHRAlgorithm(sampleRate, mNumberOfBeatsToAverage, 10); //10 second training period

                                    not_config_yet = false;

                                    try {
                                        mLPFilter = new Filter(Filter.LOW_PASS, sampleRate, mLPFc);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                }
                            } else {
                                Log.i(LOG_TAG, "ShimmerDevice returned is NULL!");
                            }
                            break;
                        case CONNECTING:
                            Log.i(LOG_TAG, "Shimmer [" + macAddress + "] is CONNECTING");
                            break;
                        case STREAMING:
                            Log.i(LOG_TAG, "Shimmer [" + macAddress + "] is now STREAMING");
                            break;
                        case STREAMING_AND_SDLOGGING:
                            Log.i(LOG_TAG, "Shimmer [" + macAddress + "] is now STREAMING AND LOGGING");
                            break;
                        case SDLOGGING:
                            Log.i(LOG_TAG, "Shimmer [" + macAddress + "] is now SDLOGGING");
                            break;
                        case DISCONNECTED:
                            if (not_config_yet) {
                                showDialogShimmer();

                            }else {
                                showDialogShimmerDisconnetted();
                            }
                                Log.i(LOG_TAG, "Shimmer [" + macAddress + "] has been DISCONNECTED");
                            break;
                    }
                    break;
            }

            super.handleMessage(msg);
        }
    };




}
