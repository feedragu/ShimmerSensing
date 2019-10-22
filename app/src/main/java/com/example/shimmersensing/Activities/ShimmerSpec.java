package com.example.shimmersensing.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
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
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import com.shimmerresearch.android.Shimmer;
import com.shimmerresearch.android.guiUtilities.ShimmerBluetoothDialog;
import com.shimmerresearch.android.manager.ShimmerBluetoothManagerAndroid;
import com.shimmerresearch.bluetooth.ShimmerBluetooth;
import com.shimmerresearch.driver.CallbackObject;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;

import com.example.shimmersensing.R;
import com.shimmerresearch.driverUtilities.SensorDetails;

import java.io.IOException;
import java.util.Collection;

import static com.shimmerresearch.android.guiUtilities.ShimmerBluetoothDialog.EXTRA_DEVICE_ADDRESS;

public class ShimmerSpec extends AppCompatActivity {

    ShimmerBluetoothManagerAndroid btManager;
    Shimmer shimmerDevice;
    ShimmerDevice shimmerDev;
    String shimmerBtAdd = "00:00:00:00:00:00";  //Put the address of the Shimmer device you want to connect here

    final static String LOG_TAG = "BluetoothManagerExample";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shimmer_spec);

        Toolbar mToolbar = findViewById(R.id.toolbar_shimmer);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        View layout=findViewById(R.id.view_layout);

        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        }, 0);

        Slide slide =new Slide();
        slide.setSlideEdge(Gravity.BOTTOM);
        slide.setDuration(400);
        CircularPropagation c=new CircularPropagation();
        c.setPropagationSpeed(5f);
        slide.setPropagation(c);

        slide.setInterpolator(new DecelerateInterpolator());
        Fade fade =new Fade();
        fade.setDuration(100);
        fade.setMode(Fade.MODE_OUT);
        Transition transition = new TransitionSet()
                .addTransition(slide)
                .addTransition(fade);

        getWindow().setEnterTransition(transition);

        Slide slide2 =new Slide();
        slide2.setSlideEdge(Gravity.BOTTOM);
        slide2.setDuration(250);
        slide.setPropagation(c);

        slide2.setInterpolator(new LinearOutSlowInInterpolator());
        getWindow().setExitTransition(slide2);

        try {
            btManager = new ShimmerBluetoothManagerAndroid(this, mHandler);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Couldn't create ShimmerBluetoothManagerAndroid. Error: " + e);
        }
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
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            Log.e(LOG_TAG, "Error. This device does not support Bluetooth");
            Toast.makeText(this, "Error. This device does not support Bluetooth", Toast.LENGTH_LONG).show();
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                // Bluetooth is not enabled
                Log.e(LOG_TAG, "Error. Shimmer device not paired or Bluetooth is not enabled");
                Toast.makeText(this, "Error. Shimmer device not paired or Bluetooth is not enabled. " +
                        "Please close the app and pair or enable Bluetooth", Toast.LENGTH_LONG).show();
            } else {
                Intent intent = new Intent(getApplicationContext(), ShimmerBluetoothDialog.class);
                startActivityForResult(intent, ShimmerBluetoothDialog.REQUEST_CONNECT_SHIMMER);
            }
        }
    }

    /**
     * Get the result from the paired devices dialog
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 2) {
            if (resultCode == Activity.RESULT_OK) {
                //Get the Bluetooth mac address of the selected device:
                String macAdd = data.getStringExtra(EXTRA_DEVICE_ADDRESS);
                Log.i("shimmer_sensing", "connected right now");
                try {
                    btManager.connectShimmerThroughBTAddress(macAdd);
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Error. Shimmer device not paired or Bluetooth is not enabled");
                    Toast.makeText(this, "Error. Shimmer device not paired or Bluetooth is not enabled. " +
                            "Please close the app and pair or enable Bluetooth", Toast.LENGTH_LONG).show();
                }
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }



    public void startStreaming() throws InterruptedException, IOException{
        shimmerDev.startStreaming();
    }

    public void stopStreaming(View v) throws IOException {
        shimmerDev.stopStreaming();
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

                        Collection<FormatCluster> allFormats = objectCluster.getCollectionOfFormatClusters(Configuration.Shimmer3.ObjectClusterSensorName.GSR_CONDUCTANCE);
                        FormatCluster formatCluster = ((FormatCluster) ObjectCluster.returnFormatCluster(allFormats, "CAL"));
                        if (formatCluster != null) {
                            gsrConductance = formatCluster.mData;
                        }
                        allFormats = objectCluster.getCollectionOfFormatClusters(Configuration.Shimmer3.ObjectClusterSensorName.GSR_RESISTANCE);
                        formatCluster = ((FormatCluster) ObjectCluster.returnFormatCluster(allFormats, "CAL"));
                        if (formatCluster != null) {
                            gsrResistance = formatCluster.mData;
                        }
                        allFormats = objectCluster.getCollectionOfFormatClusters("PPG_A13");
                        formatCluster = ((FormatCluster) ObjectCluster.returnFormatCluster(allFormats, "CAL"));
                        if (formatCluster != null) {
                            ppg = formatCluster.mData;
                        }
                        allFormats = objectCluster.getCollectionOfFormatClusters("Temperature_BMP280");
                        formatCluster = ((FormatCluster) ObjectCluster.returnFormatCluster(allFormats, "CAL"));
                        if (formatCluster != null) {
                            temperature = formatCluster.mData;
                        }

                        Log.d(LOG_TAG, "DATA_PACKET: " +
                                "\n GSR CONDUCTANCE: " + gsrConductance +
                                "\n GSR RESISTANCE: " + gsrResistance +
                                "\n PPG: " + ppg +
                                "\n TEMPERATURE: " + temperature);
                    }
                    break;
                case Shimmer.MESSAGE_TOAST:
                    Log.i("handler_test", ""+msg.what);
                    /** Toast messages sent from {@link Shimmer} are received here. E.g. device xxxx now streaming.
                     *  Note that display of these Toast messages is done automatically in the Handler in {@link com.shimmerresearch.android.shimmerService.ShimmerService} */
                    Toast.makeText(getApplicationContext(), msg.getData().getString(Shimmer.TOAST), Toast.LENGTH_SHORT).show();
                    break;
                case ShimmerBluetooth.MSG_IDENTIFIER_STATE_CHANGE:

                    ShimmerBluetooth.BT_STATE state = null;
                    String macAddress = "";

                    if (msg.obj instanceof ObjectCluster) {

                        state = ((ObjectCluster) msg.obj).mState;
                        macAddress = ((ObjectCluster) msg.obj).getMacAddress();
                        Log.i("get_mac", ""+macAddress);
                        shimmerBtAdd=macAddress;
                    } else if (msg.obj instanceof CallbackObject) {
                        Log.i("get_bluetooth", ""+msg.what);
                        state = ((CallbackObject) msg.obj).mState;
                        macAddress = ((CallbackObject) msg.obj).mBluetoothAddress;
                    }

                    switch (state) {
                        case CONNECTED:
                            Log.i(LOG_TAG, "Shimmer [" + shimmerBtAdd + "] is now CONNECTED");

                            if(shimmerDev != null) {
                                Log.i(LOG_TAG, "Got the ShimmerDevice!");
                                shimmerDev.startStreaming();

                                for (SensorDetails sensorsDetsils : shimmerDevice.getListOfEnabledSensors()) {
                                    for (String sensors : sensorsDetsils.mSensorDetailsRef.mListOfChannelsRef) {
                                        Log.d("Enabled Sensors", sensors);
                                    }
                                }

                            }
                            else { Log.i(LOG_TAG, "ShimmerDevice returned is NULL!"); }
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
                            Log.i(LOG_TAG, "Shimmer [" + macAddress + "] has been DISCONNECTED");
                            break;
                    }
                    break;
                    default:

                        break;
            }
            super.handleMessage(msg);
        }
    };

}
