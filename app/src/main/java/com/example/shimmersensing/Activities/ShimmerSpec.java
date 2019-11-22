package com.example.shimmersensing.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.transition.CircularPropagation;
import android.transition.Explode;
import android.transition.Fade;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shimmersensing.Utilities.CSVFile;
import com.example.shimmersensing.Utilities.SendDeviceDetails;
import com.example.shimmersensing.Utilities.ShimmerData;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.shimmerresearch.algorithms.Filter;
import com.shimmerresearch.android.Shimmer;
import com.shimmerresearch.android.guiUtilities.ShimmerDialogConfigurations;
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

import com.example.shimmersensing.R;
import com.shimmerresearch.exceptions.ShimmerException;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.shimmerresearch.android.guiUtilities.ShimmerBluetoothDialog.EXTRA_DEVICE_ADDRESS;

public class ShimmerSpec extends AppCompatActivity {

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
    private SharedPreferences pref;
    private int sampling_time_progress, overlap_time_progress;
    private ArrayList<ShimmerData> list;

    Handler handler = new Handler();
    Runnable runnable;
    int delay = 20 * 1000; //Delay for 15 seconds.  One second = 1000 milliseconds.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shimmer_spec);

        Toolbar mToolbar = findViewById(R.id.toolbar_shimmer);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);

        View layout = findViewById(R.id.view_layout);
        final Rect epicenter = new Rect(layout.getLeft(), layout.getTop(), layout.getRight(), layout.getBottom());
        Explode explode = new Explode() {
            {
                super.setEpicenterCallback(new Transition.EpicenterCallback() {
                    @Override
                    public Rect onGetEpicenter(Transition transition) {
                        Log.i("prova", "funziona");
                        return epicenter;
                    }

                    ;

                });
            }
        };


        graph = findViewById(R.id.graph);

        mSeries2 = new LineGraphSeries<>();
        graph.addSeries(mSeries2);


        graph.getViewport().setScalable(true);
        graph.getViewport().setScalableY(false);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(10);
        graph.getViewport().setMinY(1000);
        graph.getViewport().setMaxY(1900);
        graph.getViewport().setMaxYAxisSize(2000);

        graph.setTitle("PPG Data");
        graph.setTitleTextSize(55f);


        graph2 = findViewById(R.id.graph2);

        mSeriesX = new LineGraphSeries<>();
        mSeriesZ = new LineGraphSeries<>();
        mSeriesY = new LineGraphSeries<>();


        mSeriesX.setColor(R.color.red);
        mSeriesZ.setColor(R.color.blue);
        mSeriesY.setColor(R.color.green);

        graph2.addSeries(mSeriesX);
        graph2.addSeries(mSeriesZ);
        graph2.addSeries(mSeriesY);


        graph2.getViewport().setScalable(true);
        graph2.getViewport().setScalableY(false);
        graph2.getViewport().setXAxisBoundsManual(true);
        graph2.getViewport().setYAxisBoundsManual(true);
        graph2.getViewport().setMinX(0);
        graph2.getViewport().setMaxX(20);
        graph2.getViewport().setMinY(0);
        graph2.getViewport().setMaxY(25);
        graph2.getViewport().setMaxYAxisSize(20);

        graph2.setTitle("Accelerometer Data");
        graph2.setTitleTextSize(55f);

        graph_gsr = findViewById(R.id.graph_gsr);

        mSeriesGsr = new LineGraphSeries<>();


        mSeriesGsr.setColor(R.color.red);

        graph_gsr.addSeries(mSeriesGsr);


        graph_gsr.getViewport().setScalable(true);
        graph_gsr.getViewport().setScalableY(false);
        graph_gsr.getViewport().setXAxisBoundsManual(true);
        graph_gsr.getViewport().setYAxisBoundsManual(true);
        graph_gsr.getViewport().setMinX(0);
        graph_gsr.getViewport().setMaxX(10);
        graph_gsr.getViewport().setMinY(1200);
        graph_gsr.getViewport().setMaxY(1700);
        graph_gsr.getViewport().setMaxYAxisSize(20);

        graph_gsr.setTitle("GSR Data");
        graph_gsr.setTitleTextSize(55f);

        graph_gsr_res = findViewById(R.id.graph_gsr_resistance);

        mSeriesGsrResistance = new LineGraphSeries<>();


        mSeriesGsrResistance.setColor(R.color.red);

        graph_gsr_res.addSeries(mSeriesGsrResistance);


        graph_gsr_res.getViewport().setScalable(true);
        graph_gsr_res.getViewport().setScalableY(false);
        graph_gsr_res.getViewport().setXAxisBoundsManual(true);
        graph_gsr_res.getViewport().setYAxisBoundsManual(true);
        graph_gsr_res.getViewport().setMinX(0);
        graph_gsr_res.getViewport().setMaxX(10);
        graph_gsr_res.getViewport().setMinY(600);
        graph_gsr_res.getViewport().setMaxY(1200);
        graph_gsr_res.getViewport().setMaxYAxisSize(20);

        graph_gsr_res.setTitle("GSR Resistance Data");
        graph_gsr_res.setTitleTextSize(55f);

        Slide slide = new Slide();
        slide.setSlideEdge(Gravity.BOTTOM);
        slide.setDuration(400);
        CircularPropagation c = new CircularPropagation();
        c.setPropagationSpeed(5f);
        slide.setPropagation(c);

        slide.setInterpolator(new DecelerateInterpolator());
        Fade fade = new Fade();
        fade.setDuration(100);
        fade.setMode(Fade.MODE_OUT);
        Transition transition = new TransitionSet()
                .addTransition(slide)
                .addTransition(fade);

        getWindow().setEnterTransition(transition);

        Slide slide2 = new Slide();
        slide2.setSlideEdge(Gravity.BOTTOM);
        slide2.setDuration(250);
        slide.setPropagation(c);

        slide2.setInterpolator(new LinearOutSlowInInterpolator());
        getWindow().setExitTransition(slide2);

        myDialog = new Dialog(this);
// Request permissions
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        }, 0);

        list = new ArrayList<>();


        try {
            btManager = new ShimmerBluetoothManagerAndroid(this, mHandler);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Couldn't create ShimmerBluetoothManagerAndroid. Error: " + e);
        }

        InputStream inputStream = getResources().openRawResource(R.raw.sampledata_gsr_ppg);
        CSVFile csvFile = new CSVFile(inputStream);
        List scoreList = csvFile.read();
        ArrayList<String[]> secondList = new ArrayList();
        for (int i = 3; i < scoreList.size(); i++) {
            secondList.add((String[]) scoreList.get(i));
        }
        for (String[] array : secondList) {
            if(array.length > 1) {
                String test = array[1];
                char[] s = test.toCharArray();
                List<String> listCsv = new ArrayList<String>(Arrays.asList(test.split(String.valueOf(s[12]))));
                Long tsLong = System.currentTimeMillis() / 1000;
                double acceleration = Math.sqrt(Math.pow(Double.parseDouble(listCsv.get(1)), 2) +
                        Math.pow(Double.parseDouble(listCsv.get(2)), 2) +
                        Math.pow(Double.parseDouble(listCsv.get(3)), 2));
                ShimmerData sData = new ShimmerData(Double.parseDouble(listCsv.get(5)),
                        Double.parseDouble(listCsv.get(4)),
                        0, acceleration,
                        tsLong);
                list.add(sData);
            }
        }
        Log.i("prova", "onCreate: " + list.size());

    }


    @Override
    protected void onStart() {
        pref = getApplicationContext().getSharedPreferences("ShimmerSensingSamplingConfig", 0); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();
        if (!pref.contains("sampling_time") & !pref.contains("overlap_time")) {
            editor.putInt("sampling_time", 20);
            editor.putInt("overlap_time", 2);
            editor.apply();
        } else {
            sampling_time_progress = pref.getInt("sampling_time", 20);
            overlap_time_progress = pref.getInt("overlap_time", 2);

        }

        Gson gson = new Gson();
        String response = pref.getString("shimmerdata", "");
        ArrayList<ShimmerData> prova = gson.fromJson(response,
                new TypeToken<List<ShimmerData>>() {
                }.getType());
//        Log.d("prova", "onStart: "+prova.size());
        super.onStart();
    }


    @Override
    public void onBackPressed() {
        //To support reverse transitions when user clicks the device back button
        finishAfterTransition();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finishAfterTransition();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void connectDevice(View view) {
        for (int j = 0; j < 5000; j++) {
            Long tsLong = System.currentTimeMillis();
            ShimmerData s = new ShimmerData(j, j, j, j, tsLong);
            list.add(s);
        }

        JsonArray jsonElements = (JsonArray) new Gson().toJsonTree(list);
        Log.i("prova_json", "run: " + jsonElements);
        list.clear();

        try {


            new SendDeviceDetails().execute("http://192.168.1.16:5000/api/v1/resources/shimmersensing/sensordata", String.valueOf(jsonElements));
        } catch (Exception e) {
            e.printStackTrace();
        }
//        if (!connected) {
//            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//            if (mBluetoothAdapter == null) {
//                // Device does not support Bluetooth
//                Log.e(LOG_TAG, "Error. This device does not support Bluetooth");
//                Toast.makeText(this, "Error. This device does not support Bluetooth", Toast.LENGTH_LONG).show();
//            } else {
//                if (!mBluetoothAdapter.isEnabled()) {
//                    // Bluetooth is not enabled
//                    Log.e(LOG_TAG, "Error. Shimmer device not paired or Bluetooth is not enabled");
//                    Toast.makeText(this, "Error. Shimmer device not paired or Bluetooth is not enabled. " +
//                            "Please close the app and pair or enable Bluetooth", Toast.LENGTH_LONG).show();
//                } else {
//                    Intent intent = new Intent(getApplicationContext(), ShimmerBluetoothDialog.class);
//                    startActivityForResult(intent, ShimmerBluetoothDialog.REQUEST_CONNECT_SHIMMER);
//                }
//            }
//        } else {
//            if (shimmerDevice != null & !onpause) {
//                shimmerDevice.startStreaming();
//                onpause = true;
//            }
//        }
    }

    public void sampleWindow(View view) {
        myDialog.setContentView(R.layout.sampling_window_popup);
        sampling_bar = myDialog.findViewById(R.id.sampling_bar);
        overlap_bar = myDialog.findViewById(R.id.overlap_bar);
        sampling_text = myDialog.findViewById(R.id.samplingwindow_sec);
        overlap_text = myDialog.findViewById(R.id.overlap_sec);
        sampling_bar.setMax(10);
        overlap_bar.setMax(5);
        sampling_time_progress = pref.getInt("sampling_time", -1);
        overlap_time_progress = pref.getInt("overlap_time", -1);
        sampling_bar.setProgress(sampling_time_progress - 20);
        overlap_bar.setProgress(overlap_time_progress - 1);
        sampling_text.setText(sampling_time_progress + " s");
        overlap_text.setText(overlap_time_progress + " s");
        btnConfirm = myDialog.findViewById(R.id.btnConfirm);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDialog.dismiss();
                SharedPreferences.Editor editor = pref.edit();
                editor.putInt("sampling_time", sampling_bar.getProgress() + 20);
                editor.putInt("overlap_time", overlap_bar.getProgress() + 1);
                editor.apply();
            }
        });

        sampling_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    if (progress >= 0 && progress <= sampling_bar.getMax()) {

                        String progressString = String.valueOf(progress + 20);
                        sampling_text.setText(progressString + " s");
                        seekBar.setSecondaryProgress(progress);
                    }
                }

            }
        });

        overlap_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    if (progress >= 0 && progress <= overlap_bar.getMax()) {

                        String progressString = String.valueOf(progress + 1);
                        overlap_text.setText(progressString + " s");
                        seekBar.setSecondaryProgress(progress);
                    }
                }

            }
        });
        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        myDialog.show();
    }

    /**
     * Get the result from the paired devices dialog
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 2) {
            if (resultCode == Activity.RESULT_OK) {
                btManager.disconnectAllDevices();   //Disconnect all devices first
                //Get the Bluetooth mac address of the selected device:
                String macAdd = data.getStringExtra(EXTRA_DEVICE_ADDRESS);

                nDialog = new ProgressDialog(ShimmerSpec.this);
                nDialog.setMessage("Connecting..");
                nDialog.setIndeterminate(false);
                nDialog.setCancelable(true);
                nDialog.show();
                btManager.connectShimmerThroughBTAddress(macAdd);   //Connect to the selected device
                shimmerBtAdd = macAdd;
                macAddr = findViewById(R.id.mac_address);

                connect_start = findViewById(R.id.connect_start);

            }

        }
        super.onActivityResult(requestCode, resultCode, data);
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

    public void openMenuSensor(View view) {


        if (shimmerDevice != null) {
            if (!shimmerDevice.isStreaming() && !shimmerDevice.isSDLogging()) {
                //ShimmerDialogConfigurations.buildShimmerSensorEnableDetails(shimmerDevice, MainActivity.this);
                ShimmerDialogConfigurations.buildShimmerSensorEnableDetails(shimmerDevice, ShimmerSpec.this, btManager);
                Log.i("the end", "done");
            } else {
                shimmerDevice.stopStreaming();
                onpause = true;
                ShimmerDialogConfigurations.buildShimmerSensorEnableDetails(shimmerDevice, ShimmerSpec.this, btManager);
                Log.i("the end", "done");

            }
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("No shimmer device connected")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.d("MainActivity", "Done");
                        }
                    })
                    .show();
        }
    }

    /**
     * Messages from the Shimmer device including sensor data are received here
     */


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


                        Log.d(LOG_TAG, "DATA_PACKET: " +
                                "\n GSR CONDUCTANCE: " + gsrConductance +
                                "\n GSR RESISTANCE: " + gsrResistance +
                                "\n PPG: " + ppg +
                                "\n ACCELERATION_X: " + accel_x +
                                "\n ACCELERATION_Z: " + accel_z +
                                "\n ACCELERATION_Y: " + accel_y);


                        mSeries2.appendData(new DataPoint(timestamp / 1000, dataPPG), true, 10000);


                        double acceleration = Math.sqrt(Math.pow(accel_x, 2) + Math.pow(accel_z, 2) + Math.pow(accel_y, 2));
                        mSeriesX.appendData(new DataPoint(timestamp / 1000, acceleration), true, 10000);
                        mSeriesGsr.appendData(new DataPoint(timestamp / 1000, gsrConductance), true, 10000);
                        mSeriesGsrResistance.appendData(new DataPoint(timestamp / 1000, gsrResistance), true, 10000);
                        Long tsLong = System.currentTimeMillis() / 1000;
                        ShimmerData s = new ShimmerData(dataPPG, gsrResistance, gsrConductance, acceleration, tsLong);
                        list.add(s);
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
                            shimmerDevice = btManager.getShimmerDeviceBtConnectedFromMac(shimmerBtAdd);
                            if (shimmerDevice != null) {
                                Log.i(LOG_TAG, "Got the ShimmerDevice!");
                                if (not_config_yet) {
                                    sampleRate = shimmerDevice.getSamplingRateShimmer();
                                    connected = true;
                                    mNewPPGSignalProcessing = true;
                                    mCountPPGInitial = 0;
                                    mHeartRateCalculation = new PPGtoHRAlgorithm(sampleRate, mNumberOfBeatsToAverage, 10); //10 second training period
                                    handler.postDelayed(runnable = new Runnable() {
                                        public void run() {
                                            SharedPreferences.Editor editor = pref.edit();
                                            Gson gson = new Gson();
                                            JsonArray jsonElements = (JsonArray) new Gson().toJsonTree(list);
                                            Log.i("prova_json", "run: " + jsonElements);
                                            editor.putString("shimmerdata", String.valueOf(jsonElements));
                                            editor.apply();

                                            handler.postDelayed(runnable, delay);
                                        }
                                    }, delay);
                                    not_config_yet = false;

                                    try {
                                        mLPFilter = new Filter(Filter.LOW_PASS, sampleRate, mLPFc);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                }
                                nDialog.dismiss();
                                macAddr.setText(shimmerBtAdd);
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
                                new AlertDialog.Builder(ShimmerSpec.this)
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

            super.handleMessage(msg);
        }
    };


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
}
