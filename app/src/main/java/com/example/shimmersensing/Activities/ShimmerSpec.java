package com.example.shimmersensing.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import com.shimmerresearch.android.Shimmer;
import com.shimmerresearch.android.guiUtilities.ShimmerBluetoothDialog;
import com.shimmerresearch.android.guiUtilities.ShimmerDialogConfigurations;
import com.shimmerresearch.android.manager.ShimmerBluetoothManagerAndroid;
import com.shimmerresearch.bluetooth.ShimmerBluetooth;
import com.shimmerresearch.driver.CallbackObject;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;

import com.example.shimmersensing.R;

import java.util.Collection;

public class ShimmerSpec extends AppCompatActivity {

    ShimmerBluetoothManagerAndroid btManager;
    ShimmerDevice shimmerDevice;
    String shimmerBtAdd = "00:00:00:00:00:00";  //Put the address of the Shimmer device you want to connect here

    final static String LOG_TAG = "BluetoothManagerExample";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shimmer_spec);

        Toolbar mToolbar = findViewById(R.id.toolbar_shimmer);
        mToolbar.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        ColorDrawable colorDrawable = new ColorDrawable( Color.WHITE );
        getWindow().setBackgroundDrawable( colorDrawable );
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        View layout=findViewById(R.id.view_layout);
        final Rect epicenter=new Rect(layout.getLeft(), layout.getTop(), layout.getRight(), layout.getBottom());
        Explode explode = new Explode() {
            {
                super.setEpicenterCallback(new Transition.EpicenterCallback() {
                    @Override
                    public Rect onGetEpicenter(Transition transition) {
                        Log.i("prova", "funziona");
                        return epicenter;
                    };

            });
            }
        };
        Slide slide =new Slide();
        slide.setSlideEdge(Gravity.BOTTOM);
        slide.setDuration(250);
        CircularPropagation c=new CircularPropagation();
        c.setPropagationSpeed(3f);
        slide.setPropagation(c);

        slide.setInterpolator(new LinearInterpolator());
        Fade fade =new Fade();
        fade.setDuration(200);
        fade.setMode(Fade.MODE_IN);
        Transition transition = new TransitionSet()
                .addTransition(slide)
                .addTransition(fade);

        getWindow().setEnterTransition(transition);

        Transition transition2 = new TransitionSet()
        .addTransition(explode);
        getWindow().setExitTransition(transition2);

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

    Handler mHandler;

    {
        mHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {

                switch (msg.what) {
                    case ShimmerBluetooth.MSG_IDENTIFIER_DATA_PACKET:
                        if ((msg.obj instanceof ObjectCluster)) {

                            ObjectCluster objectCluster = (ObjectCluster) msg.obj;

                            //Retrieve all possible formats for the current sensor device:
                            Collection<FormatCluster> allFormats = objectCluster.getCollectionOfFormatClusters(Configuration.Shimmer3.ObjectClusterSensorName.TIMESTAMP);
                            FormatCluster timeStampCluster = ((FormatCluster) ObjectCluster.returnFormatCluster(allFormats, "CAL"));
                            double timeStampData = timeStampCluster.mData;
                            Log.i(LOG_TAG, "Time Stamp: " + timeStampData);
                            allFormats = objectCluster.getCollectionOfFormatClusters(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_X);
                            FormatCluster accelXCluster = ((FormatCluster) ObjectCluster.returnFormatCluster(allFormats, "CAL"));
                            if (accelXCluster != null) {
                                double accelXData = accelXCluster.mData;
                                Log.i(LOG_TAG, "Accel LN X: " + accelXData);
                            }
                        }
                        break;
                    case Shimmer.MESSAGE_TOAST:
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
                                Log.i(LOG_TAG, "Shimmer [" + macAddress + "] has been DISCONNECTED");
                                break;
                        }
                        break;
                }

                super.handleMessage(msg);
            }
        };
    }
}
