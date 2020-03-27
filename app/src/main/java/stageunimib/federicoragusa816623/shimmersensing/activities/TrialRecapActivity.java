package stageunimib.federicoragusa816623.shimmersensing.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shimmersensing.R;
import stageunimib.federicoragusa816623.shimmersensing.adapter.TrialRecyclerAdapter;
import stageunimib.federicoragusa816623.shimmersensing.global.GlobalValues;
import stageunimib.federicoragusa816623.shimmersensing.utilities.ShimmerData;
import stageunimib.federicoragusa816623.shimmersensing.utilities.ShimmerSensorDevice;
import stageunimib.federicoragusa816623.shimmersensing.utilities.ShimmerTrial;

import com.google.gson.Gson;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.shimmerresearch.algorithms.Filter;
import com.shimmerresearch.android.manager.ShimmerBluetoothManagerAndroid;
import com.shimmerresearch.biophysicalprocessing.PPGtoHRAlgorithm;
import com.shimmerresearch.biophysicalprocessing.PPGtoHRwithHRV;

import java.util.ArrayList;
import java.util.List;

public class TrialRecapActivity extends AppCompatActivity implements TrialRecyclerAdapter.OnShimmerListener {

    private ShimmerBluetoothManagerAndroid btManager;
    private Filter mLPFilter;
    boolean connected = false;
    private String shimmerBtAdd;
    Button connect_start;
    ProgressDialog nDialog;
    PPGtoHRwithHRV mPPGtoHR;
    double[] mLPFc = {5};


    private static Integer INVALID_OUTPUT = -1;
    private static int mNumberOfBeatsToAverage = 5;
    private static PPGtoHRAlgorithm mHeartRateCalculation;
    private static int mCountPPGInitial = 0; //skip first 100 samples


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
    private SharedPreferences pref;
    private int sampling_time_progress, overlap_time_progress;
    private ArrayList<ShimmerData> shimmerData;
    private List<ShimmerTrial> shimmerTrial = null;
    private CardView sensorCard;
    private ImageView sensorImageCard;
    private TextView sensorname;
    private TextView macAddress;
    private TextView sampleRateSensor;
    private TextView lastUse;
    private ShimmerSensorDevice shimmerSensor;
    private TrialRecyclerAdapter rAdapter;
    private RecyclerView shimmerTrialRecycler;
    private MediaPlayer mediaPlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);

        setContentView(R.layout.activity_trial_recap);

        Transition enter = TransitionInflater.from(this).inflateTransition(R.transition.explode_trial);
        Transition exit = TransitionInflater.from(this).inflateTransition(R.transition.explode_trial_exit);

        getWindow().setEnterTransition(enter);

        getWindow().setExitTransition(new Fade());


        //ShimmerSensorDevice shimmerSensor = (ShimmerSensorDevice) getIntent().getExtras().getSerializable("shimmersensor_selected");
//        pref = getApplicationContext().getSharedPreferences("ShimmerSensingSamplingConfig", 0);

        Gson gson = new Gson();
//        String response = pref.getString("shimmertrial", "");
//        shimmerTrial = gson.fromJson(response,
//                new TypeToken<List<ShimmerTrial>>() {
//                }.getType());

        GlobalValues gv = (GlobalValues) getApplicationContext();

        shimmerTrial = gv.getShimmerTrialArrayList();
//
//        String json = pref.getString("shimmersensor_selected", "");
        shimmerSensor = gv.getSsd();

//        String audioArray= pref.getString("shimmeraudio", "");
//        byte[] array = Base64.decode(audioArray, Base64.DEFAULT);
//
        mediaPlayer = new MediaPlayer();
//
//        playMp3(array);


        if (shimmerSensor != null & shimmerTrial != null) {
            if(shimmerTrial.size() > 0 ) {
                shimmerTrialRecycler = findViewById(R.id.recyclerTrial);

                sensorname = findViewById(R.id.sensorName);
                macAddress = findViewById(R.id.macaddress);
                sampleRateSensor = findViewById(R.id.sampleRate);
                lastUse = findViewById(R.id.dateUsage);
                sensorname.setText(shimmerSensor.getDeviceName());
                macAddress.setText(shimmerSensor.getMacAddress());
                sampleRateSensor.setText(shimmerSensor.getSampleRate() + " Hz");
                lastUse.setText(shimmerSensor.getLastUse().toString());

                RecyclerView.LayoutManager recyce = new
                        LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

                shimmerTrialRecycler.setLayoutManager(recyce);
                shimmerTrialRecycler.setNestedScrollingEnabled(false);

                rAdapter = new TrialRecyclerAdapter(TrialRecapActivity.this, shimmerTrial, this);
                DividerItemDecoration itemDecor = new DividerItemDecoration(shimmerTrialRecycler.getContext(), DividerItemDecoration.VERTICAL);
                shimmerTrialRecycler.addItemDecoration(itemDecor);
                shimmerTrialRecycler.setAdapter(rAdapter);
            }else {
                finishAndRemoveTask();
            }

        }else {
            finishAndRemoveTask();
        }
        Toolbar mToolbar = findViewById(R.id.toolbar_shimmer);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Riepilogo Trial");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

//
//        String macAdd = shimmerSensor.getMacAddress();
//
//        setDialog(true);
//        try {
//            btManager = new ShimmerBluetoothManagerAndroid(this, mHandler);
//        } catch (Exception e) {
//            Log.e(LOG_TAG, "Couldn't create ShimmerBluetoothManagerAndroid. Error: " + e);
//        }
//        btManager.connectShimmerThroughBTAddress(macAdd);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getSupportActionBar().setTitle("");
                finishAfterTransition();
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void startTrial(View view) {
        Intent intent = new Intent(this, ShimmerTrialActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        getSupportActionBar().setTitle("");
        finishAfterTransition();
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void setDialog(boolean show) {
        AlertDialog.Builder builder = new AlertDialog.Builder(TrialRecapActivity.this);
        builder.setView(R.layout.progress_dialog);
        Dialog dialog = builder.create();
        if (show) dialog.show();
        else dialog.dismiss();
    }



    @Override
    public void onShimmerClick(int position) {

    }
}
