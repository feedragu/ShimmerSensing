package stageunimib.federicoragusa816623.shimmersensing.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.transition.Scene;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.shimmersensing.R;
import stageunimib.federicoragusa816623.shimmersensing.adapter.BtShimmerRecyclerAdapter;
import stageunimib.federicoragusa816623.shimmersensing.adapter.MyViewPagerAdapter;
import stageunimib.federicoragusa816623.shimmersensing.adapter.RecyclerAdapter;
import stageunimib.federicoragusa816623.shimmersensing.global.GlobalValues;
import stageunimib.federicoragusa816623.shimmersensing.graphic.ZoomOutPageTransformer;
import stageunimib.federicoragusa816623.shimmersensing.utilities.ShimmerSensorDevice;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.shimmerresearch.android.manager.ShimmerBluetoothManagerAndroid;
import com.shimmerresearch.driver.ShimmerDevice;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements BtShimmerRecyclerAdapter.OnBtClickListener {

    ViewGroup rootContainer;
    Scene scene1, scene2, scene3;
    private Transition transitionMgr;
    private RecyclerView shimmerList;
    private RecyclerAdapter rAdapter;
    private ViewPager2 mPager;
    private int sceneOn;
    private SharedPreferences pref;
    private ArrayList<ShimmerSensorDevice> shimmerSensor;
    private int currentPage;
    private ShimmerBluetoothManagerAndroid btManager;
    final BluetoothAdapter myBlueToothAdapter = BluetoothAdapter.getDefaultAdapter();
    private RecyclerView btList;
    private ArrayList<BluetoothDevice> btListDev = new ArrayList<>();
    private BtShimmerRecyclerAdapter btAdapter;
    private ArrayList<ShimmerSensorDevice> shimmerTrialSaved;
    private Gson gson = new Gson();
    private boolean noShimmerSelected = true;
    private GlobalValues gv;
    private ShimmerDevice shimmerDevice = null;
    private String shimmerBtAdd = "00:00:00:00:00:00";
    private String LOG_TAG = "pageadpater_connection";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        Transition enter = TransitionInflater.from(this).inflateTransition(R.transition.explode_trial);
        Transition exit = TransitionInflater.from(this).inflateTransition(R.transition.fade_trial);
// set an enter transition
        getWindow().setEnterTransition(enter);
// set an exit transition
        getWindow().setExitTransition(exit);

        setContentView(R.layout.activity_trial_presentation);
        pref = getApplicationContext().getSharedPreferences("ShimmerSensingSamplingConfig", 0); // 0 - for private mode


        rootContainer = findViewById(R.id.rootContainer);

        transitionMgr = TransitionInflater.from(this)
                .inflateTransition(R.transition.trial_presentation_transition);

        scene1 = Scene.getSceneForLayout(rootContainer,
                R.layout.trial_pres_1, this);
        scene2 = Scene.getSceneForLayout(rootContainer,
                R.layout.trial_pres_2, this);
        scene3 = Scene.getSceneForLayout(rootContainer,
                R.layout.trial_pres_3, this);
        scene1.enter();

        gv = (GlobalValues) getApplicationContext();


        sceneOn = pref.getInt("scene_on", 1);

        sceneOn = 1;
        storedScene(sceneOn);
        attachAdapter();


        shimmerSensor = new ArrayList<ShimmerSensorDevice>();
//
//        try {
//            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
//            for (BluetoothDevice bt : pairedDevices) {
//                if (bt.getName().contains("Shimmer")) {
//                    shimmerSensor.add(new ShimmerSensorDevice(bt.getName(), bt.getAddress(), 128.5, c.getTime()));
//                }
//            }
//        } catch (Exception e) {
//            shimmerSensor.add(new ShimmerSensorDevice("shimmersensor", "06:07:MC:09:55", 128.5, c.getTime()));
//            shimmerSensor.add(new ShimmerSensorDevice("shimmersensor", "06:07:MC:09:55", 128.5, c.getTime()));
//            e.printStackTrace();
//        }


    }

    private final BroadcastReceiver FoundReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            // When discovery finds a new device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getName() != null) {
                    btListDev.add(device);
                    btAdapter.notifyDataSetChanged();
                }

            }

            // When discovery cycle finished
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {

            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                btListDev.clear();
            }

        }
    };

    public void switchScene(View view) {
        TransitionManager.go(scene2, transitionMgr);
        String response = pref.getString("shimmertrialsensorsaved", "");
        if (response.equals("")) {
            sceneOn = 3;
            noShimmerSelected = true;
        } else {
            noShimmerSelected = false;
            shimmerSensor = gson.fromJson(response,
                    new TypeToken<List<ShimmerSensorDevice>>() {
                    }.getType());
            sceneOn = 2;
        }
        storedScene(sceneOn);
        attachAdapter();

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1001) {

            if (myBlueToothAdapter != null) {
                myBlueToothAdapter.startDiscovery();
            }

            try {

                IntentFilter filter = new IntentFilter();
                filter.addAction(BluetoothDevice.ACTION_FOUND);
                filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
                this.registerReceiver(FoundReceiver, filter);

            } catch (IllegalArgumentException e) {

                e.printStackTrace();
            }

        }
    }


    public void addDevice(View view) {
        myBlueToothAdapter.cancelDiscovery();
        btListDev.clear();
        TransitionManager.go(scene3, transitionMgr);
        sceneOn = 3;
        attachAdapter();
        storedScene(sceneOn);

    }


    private void setDialog(boolean show) {
        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
        builder.setView(R.layout.progress_dialog);
        Dialog dialog = builder.create();
        if (show) dialog.show();
        else dialog.dismiss();
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onRestart() {
        super.onRestart();

    }


    private void storedScene(int scene_on) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("scene_on", scene_on);
        editor.apply();
    }

    private void attachAdapter() {
        switch (sceneOn) {
            case 1:
                scene1.enter();

                break;
            case 2:
                scene2.enter();
                mPager = findViewById(R.id.pager);
                mPager.setAdapter(new MyViewPagerAdapter(this, shimmerSensor));
                mPager.setPageTransformer(new ZoomOutPageTransformer());
                mPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {

                    @Override
                    public void onPageSelected(int position) {
                        super.onPageSelected(position);
                        currentPage = position;
                    }
                });
                TabLayout tabLayout = findViewById(R.id.tabDots);
                new TabLayoutMediator(tabLayout, mPager,
                        new TabLayoutMediator.TabConfigurationStrategy() {
                            @Override
                            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                            }
                        }).attach();

                Toolbar mToolbar = findViewById(R.id.toolbar_shimmer);
                setSupportActionBar(mToolbar);
                getSupportActionBar().setTitle("");
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);


                break;
            case 3:
                scene3.enter();
                //Turn on Bluetooth
                if (myBlueToothAdapter == null)
                    Toast.makeText(HomeActivity.this, "Your device doesnot support Bluetooth", Toast.LENGTH_LONG).show();
                else if (!myBlueToothAdapter.isEnabled()) {
                    Intent BtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(BtIntent, 0);
                    Toast.makeText(HomeActivity.this, "Turning on Bluetooth", Toast.LENGTH_LONG).show();
                }


                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                    int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
                    permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
                    if (permissionCheck != 0) {

                        this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
                    }
                } else {
                    Log.d("cazzi", "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");


                    if (myBlueToothAdapter != null) {
                        myBlueToothAdapter.startDiscovery();
                    }
                    Toast.makeText(HomeActivity.this, "Scanning Devices", Toast.LENGTH_LONG).show();

                    try {

                        IntentFilter filter = new IntentFilter();
                        filter.addAction(BluetoothDevice.ACTION_FOUND);
                        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
                        this.registerReceiver(FoundReceiver, filter);

                    } catch (IllegalArgumentException e) {

                        e.printStackTrace();
                    }
                }


                btList = findViewById(R.id.btShimmerList);

                RecyclerView.LayoutManager recyce = new
                        LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

                btList.setLayoutManager(recyce);
                btList.setNestedScrollingEnabled(false);

                btAdapter = new BtShimmerRecyclerAdapter(HomeActivity.this, btListDev, HomeActivity.this);
                DividerItemDecoration itemDecor = new DividerItemDecoration(btList.getContext(), DividerItemDecoration.VERTICAL);
                btList.addItemDecoration(itemDecor);
                btList.setAdapter(btAdapter);
                Toolbar mToolbar2 = findViewById(R.id.toolbar_shimmer);
                setSupportActionBar(mToolbar2);
                getSupportActionBar().setTitle("");
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);


                break;
            default:
                break;

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:

                myBlueToothAdapter.cancelDiscovery();
                switch (sceneOn) {
                    case 2:
                        TransitionManager.go(scene1, transitionMgr);
                        sceneOn = 1;
                        storedScene(sceneOn);
                        attachAdapter();
                        break;
                    case 3:
                        if (noShimmerSelected) {
                            TransitionManager.go(scene1, transitionMgr);
                            sceneOn = 1;
                            storedScene(sceneOn);
                            attachAdapter();
                        } else {
                            TransitionManager.go(scene2, transitionMgr);
                            sceneOn = 2;
                            storedScene(sceneOn);
                            attachAdapter();
                        }

                        break;
                }

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        myBlueToothAdapter.cancelDiscovery();
        sceneOn = pref.getInt("scene_on", 1);
        switch (sceneOn) {
            case 1:
                finish();
                sceneOn = 1;
                storedScene(sceneOn);
                break;
            case 2:
                TransitionManager.go(scene1, transitionMgr);
                sceneOn = 1;
                storedScene(sceneOn);
                attachAdapter();
                break;
            case 3:
                if (noShimmerSelected) {
                    TransitionManager.go(scene1, transitionMgr);
                    sceneOn = 1;
                    storedScene(sceneOn);
                    attachAdapter();
                } else {
                    TransitionManager.go(scene2, transitionMgr);
                    sceneOn = 2;
                    storedScene(sceneOn);
                    attachAdapter();
                }
                break;
            default:
                break;

        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        sceneOn = 1;
        storedScene(sceneOn);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("scene_on", sceneOn);
        editor.apply();
        try {

            unregisterReceiver(FoundReceiver);

        } catch (IllegalArgumentException e) {

            e.printStackTrace();
        }

    }

    @Override
    public void OnBtClickListener(int position) {
        Intent intent = new Intent(this, TrialRecapActivity.class);

        String response = pref.getString("shimmertrialsensorsaved", "");
        Calendar c = Calendar.getInstance();
        ShimmerSensorDevice s = new ShimmerSensorDevice(btListDev.get(position).getName(), btListDev.get(position).getAddress(), 128.5, c.getTime());
        if (!response.equals("")) {
            shimmerTrialSaved = gson.fromJson(response,
                    new TypeToken<List<ShimmerSensorDevice>>() {
                    }.getType());
            checkifissaved(s);

        } else {
            shimmerTrialSaved = new ArrayList<>();
            shimmerTrialSaved.add(s);
            SharedPreferences.Editor editor = pref.edit();
            JsonArray jsonElements = (JsonArray) new Gson().toJsonTree(shimmerTrialSaved);
            editor.putString("shimmertrialsensorsaved", String.valueOf(jsonElements));
            editor.apply();
        }


        String json = gson.toJson(s);

        gv.setSsd(s);

        startActivity(intent);
    }

    private void checkifissaved(ShimmerSensorDevice s) {
        for (ShimmerSensorDevice s_saved : shimmerTrialSaved) {
            if (s_saved.equals(s)) {
                return;
            }
        }
        shimmerTrialSaved.add(s);
        SharedPreferences.Editor editor = pref.edit();
        JsonArray jsonElements = (JsonArray) new Gson().toJsonTree(shimmerTrialSaved);
        editor.putString("shimmertrialsensorsaved", String.valueOf(jsonElements));
        editor.apply();
    }



}
