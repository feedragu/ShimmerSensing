package com.example.shimmersensing.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.transition.Explode;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.transition.TransitionManager;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shimmersensing.R;
import com.example.shimmersensing.adapter.RecyclerAdapter;
import com.example.shimmersensing.adapter.TrialRecyclerAdapter;
import com.example.shimmersensing.global.GlobalValues;
import com.example.shimmersensing.utilities.ShimmerData;
import com.example.shimmersensing.utilities.ShimmerSensorDevice;
import com.example.shimmersensing.utilities.ShimmerTrial;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.shimmerresearch.algorithms.Filter;
import com.shimmerresearch.android.Shimmer;
import com.shimmerresearch.android.manager.ShimmerBluetoothManagerAndroid;
import com.shimmerresearch.biophysicalprocessing.PPGtoHRAlgorithm;
import com.shimmerresearch.biophysicalprocessing.PPGtoHRwithHRV;
import com.shimmerresearch.bluetooth.ShimmerBluetooth;
import com.shimmerresearch.driver.CallbackObject;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
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
    private List<ShimmerTrial> shimmerTrial;
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
//        Log.i("onCreate", "onCreate: " + audioArray);
//        byte[] array = Base64.decode(audioArray, Base64.DEFAULT);
//
        mediaPlayer = new MediaPlayer();
//
//        playMp3(array);




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
                    Log.i("tarantella", "onBackPressed: isplaying");
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = null;
                } else {
                    Log.i("ciumbia", "onBackPressed: isnotplaying");
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
            Log.i("tarantella", "onBackPressed: isplaying");
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        } else {
            Log.i("ciumbia", "onBackPressed: isnotplaying");
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        String prefTag = "shimmeraudio";
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//        prefs.edit().remove(prefTag).commit();
    }

    private void setDialog(boolean show) {
        AlertDialog.Builder builder = new AlertDialog.Builder(TrialRecapActivity.this);
        builder.setView(R.layout.progress_dialog);
        Dialog dialog = builder.create();
        if (show) dialog.show();
        else dialog.dismiss();
    }

    private void playMp3(byte[] mp3SoundByteArray) {
        try {
            // create temp file that will hold byte array
            File tempMp3 = File.createTempFile("kurchina", "mp3", getCacheDir());
            tempMp3.deleteOnExit();
            FileOutputStream fos = new FileOutputStream(tempMp3);
            fos.write(mp3SoundByteArray);
            fos.close();

            // resetting mediaplayer instance to evade problems
            mediaPlayer.reset();

            // In case you run into issues with threading consider new instance like:
            // MediaPlayer mediaPlayer = new MediaPlayer();

            // Tried passing path directly, but kept getting
            // "Prepare failed.: status=0x1"
            // so using file descriptor instead
            FileInputStream fis = new FileInputStream(tempMp3);
            mediaPlayer.setDataSource(fis.getFD());

            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException ex) {
            String s = ex.toString();
            ex.printStackTrace();
        }
    }

    Handler mHandler = new Handler(new Handler.Callback() {


        @Override
        public boolean handleMessage(Message msg) {

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


                        mSeries2.appendData(new DataPoint(timestamp / 1000, dataPPG), true, 10000);


                        double acceleration = Math.sqrt(Math.pow(accel_x, 2) + Math.pow(accel_z, 2) + Math.pow(accel_y, 2));
                        mSeriesX.appendData(new DataPoint(timestamp / 1000, acceleration), true, 10000);
                        mSeriesGsr.appendData(new DataPoint(timestamp / 1000, gsrConductance), true, 10000);
                        mSeriesGsrResistance.appendData(new DataPoint(timestamp / 1000, gsrResistance), true, 10000);
                        double tsLong = System.currentTimeMillis() / 1000;
                        ShimmerData s = new ShimmerData(dataPPG, gsrConductance, accel_x,
                                accel_y, accel_z, gyroscope_x, gyroscope_y, gyroscope_z,
                                magnetometer_x, magnetometer_y, magnetometer_z, tsLong);
                        shimmerData.add(s);

                    }
                    break;
                case Shimmer.MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(Shimmer.TOAST), Toast.LENGTH_SHORT).show();
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
                            ShimmerDevice shimmerDevice = btManager.getShimmerDeviceBtConnectedFromMac(shimmerBtAdd);
                            if (shimmerDevice != null) {
                                Log.i(LOG_TAG, "Got the ShimmerDevice!");
                                if (not_config_yet) {
                                    sampleRate = shimmerDevice.getSamplingRateShimmer();
                                    connected = true;
                                    mCountPPGInitial = 0;
                                    mHeartRateCalculation = new PPGtoHRAlgorithm(sampleRate, mNumberOfBeatsToAverage, 10); //10 second training period
//                                    handler.postDelayed(runnable = new Runnable() {
//                                        public void run() {
//                                            SharedPreferences.Editor editor = pref.edit();
//                                            Gson gson = new Gson();
//                                            JsonArray jsonElements = (JsonArray) new Gson().toJsonTree(shimmerData);
//                                            Log.i("prova_json", "run: " + jsonElements);
//                                            editor.putString("shimmerdata", String.valueOf(jsonElements));
//                                            editor.apply();
////                                            try {
////                                                new SendDeviceDetails().execute("http://192.168.1.16:5000/api/v1/resources/shimmersensing/sensordata", String.valueOf(jsonElements));
////
////                                                Log.i("im sending 2", "run: send " + shimmerData.size());
////                                                list.clear();
////                                                Log.i("im sending 3", "run: send " + shimmerData.size());
////                                            } catch (Exception e) {
////                                                e.printStackTrace();
////                                            }
//
//                                            handler.postDelayed(runnable, delay);
//                                        }
//                                    }, delay);
                                    not_config_yet = false;

                                    try {
                                        mLPFilter = new Filter(Filter.LOW_PASS, sampleRate, mLPFc);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                }
                                connect_start.setText("Start streaming");
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
                            if (nDialog.isShowing()) {
                                nDialog.dismiss();
                                new AlertDialog.Builder(TrialRecapActivity.this)
                                        .setTitle("Error")
                                        .setMessage("Shimmer sensor is not connected")
                                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Log.d("MainActivity", "Done");
                                            }
                                        })
                                        .show();
                            }
                            Log.i(LOG_TAG, "Shimmer [" + macAddress + "] has been DISCONNECTED");
                            break;
                    }
                    break;
            }

            return true;
        }
    });

    @Override
    public void onShimmerClick(int position) {

    }
}
