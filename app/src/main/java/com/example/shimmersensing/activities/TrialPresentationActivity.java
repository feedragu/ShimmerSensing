package com.example.shimmersensing.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.transition.Explode;
import android.transition.Scene;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.shimmersensing.R;
import com.example.shimmersensing.graphic.ZoomOutPageTransformer;
import com.example.shimmersensing.utilities.RecyclerAdapter;
import com.example.shimmersensing.utilities.ShimmerSensorDevice;
import com.example.shimmersensing.utilities.row;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.Gson;
import com.shimmerresearch.android.manager.ShimmerBluetoothManagerAndroid;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

import static com.example.shimmersensing.activities.ShimmerSpec.LOG_TAG;
import static com.shimmerresearch.android.guiUtilities.ShimmerBluetoothDialog.EXTRA_DEVICE_ADDRESS;

public class TrialPresentationActivity extends AppCompatActivity implements RecyclerAdapter.OnShimmerListener {

    ViewGroup rootContainer;
    Scene scene1, scene2;
    private Transition transitionMgr;
    private RecyclerView shimmerList;
    private RecyclerAdapter rAdapter;
    private ViewPager2 mPager;
    private int sceneOn;
    private SharedPreferences pref;
    private ArrayList<ShimmerSensorDevice> shimmerSensor;
    private int currentPage;
    private ShimmerBluetoothManagerAndroid btManager;
    private Scene scene3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
// set an enter transition
        getWindow().setEnterTransition(new Explode());
// set an exit transition
        getWindow().setExitTransition(new Explode());

        setContentView(R.layout.activity_trial_presentation);
        pref = getApplicationContext().getSharedPreferences("ShimmerSensingSamplingConfig", 0); // 0 - for private mode


        rootContainer = findViewById(R.id.rootContainer);

        transitionMgr = TransitionInflater.from(this)
                .inflateTransition(R.transition.trial_presentation_transition);

        scene1 = Scene.getSceneForLayout(rootContainer,
                R.layout.trial_pres_1, this);

        scene2 = Scene.getSceneForLayout(rootContainer,
                R.layout.trial_pres_2, this);
        scene1.enter();


        sceneOn = pref.getInt("scene_on", 1);
        Log.i("onCreate", "onCreate: " + sceneOn);
        sceneOn = 1;
        storedScene(sceneOn);
        attachAdapter();

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        shimmerSensor = new ArrayList<ShimmerSensorDevice>();


        Calendar c = Calendar.getInstance();


        for (BluetoothDevice bt : pairedDevices) {
            if (bt.getName().contains("Shimmer")) {
                shimmerSensor.add(new ShimmerSensorDevice(bt.getName(), bt.getAddress(), 128.5, c.getTime()));
            }
        }


    }

    public void switchScene(View view) {
        TransitionManager.go(scene2, transitionMgr);
        sceneOn = 2;
        storedScene(sceneOn);
        attachAdapter();

    }


    @Override
    public void onShimmerClick(int position) {

    }

    public void addDevice(View view) {
        TransitionManager.go(scene1, transitionMgr);
        sceneOn = 1;
        attachAdapter();
        storedScene(sceneOn);

    }


    public class MyViewPagerAdapter extends RecyclerView.Adapter<MyHolder> {

        private Context context;
        private ArrayList<ShimmerSensorDevice> shimmerSensor;

        public MyViewPagerAdapter(Context context, ArrayList<ShimmerSensorDevice> shimmerSensor) {
            this.context = context;
            this.shimmerSensor = shimmerSensor;
        }

        @NonNull
        @Override
        public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new MyHolder(LayoutInflater.from(context).inflate(R.layout.fragment_screen_slide_page, parent, false));
        }

        @Override
        public void onBindViewHolder(final MyHolder holder, int position) {
            holder.sensorname.setText(shimmerSensor.get(position).getDeviceName());
            holder.macAddress.setText(shimmerSensor.get(position).getMacAddress());
            holder.sampleRate.setText(shimmerSensor.get(position).getSampleRate() + " Hz");
            holder.lastUse.setText(shimmerSensor.get(position).getLastUse().toString());
            holder.sensorCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    holder.sensorCard.setTransitionName("cardView");
                    holder.sensorImageCard.setTransitionName("sensorImageCard");
                    Pair<View, String> pair1 = Pair.create((View) holder.sensorCard, holder.sensorCard.getTransitionName());
                    Pair<View, String> pair2 = Pair.create((View) holder.sensorImageCard, holder.sensorImageCard.getTransitionName());
                    ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) context, pair1);
                    Intent intent = new Intent(context, TrialActivity.class);
                    Gson gson = new Gson();
                    String json = gson.toJson(shimmerSensor.get(currentPage));
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString("shimmersensor_selected", json);
                    editor.commit();
                    context.startActivity(intent, optionsCompat.toBundle());
                }
            });

        }

        @Override
        public int getItemCount() {
            return shimmerSensor.size();
        }


    }

    class MyHolder extends RecyclerView.ViewHolder {

        private CardView sensorCard;
        private ImageView sensorImageCard;
        private TextView sensorname;
        private TextView macAddress;
        private TextView sampleRate;
        private TextView lastUse;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            sensorCard = itemView.findViewById(R.id.sensorCard);
            sensorImageCard = itemView.findViewById(R.id.sensorImageCard);
            sensorname = itemView.findViewById(R.id.sensorName);
            macAddress = itemView.findViewById(R.id.macaddress);
            sampleRate = itemView.findViewById(R.id.sampleRate);
            lastUse = itemView.findViewById(R.id.dateUsage);
        }
    }

//    public void pageClicked(View view) {
//        Log.i("pageclicked", "pageClicked: " + shimmerSensor.get(currentPage).toString());
//
//        Intent intent = new Intent(this, TrialActivity.class);
////        Pair<View, String> p1 = Pair.create((View)sensorname, "sensorname");
////        Pair<View, String> p2 = Pair.create((View)macAddress, "macAddress");
////        Pair<View, String> p3 = Pair.create((View)sampleRate, "sampleRate");
////        ActivityOptionsCompat options = ActivityOptionsCompat.
////                makeSceneTransitionAnimation(this, p1, p2, p3);
//
//        Bundle bundle = new Bundle();
//        bundle.putSerializable("shimmersensor_selected", shimmerSensor.get(currentPage));
////        bundle.putAll(options.toBundle());
//        intent.putExtras(bundle);
//        startActivity(intent);
//    }


    private void setDialog(boolean show) {
        AlertDialog.Builder builder = new AlertDialog.Builder(TrialPresentationActivity.this);
        builder.setView(R.layout.progress_dialog);
        Dialog dialog = builder.create();
        if (show) dialog.show();
        else dialog.dismiss();
    }


    @Override
    protected void onResume() {
        super.onResume();
//        sceneOn = pref.getInt("scene_on", 1);
//        Log.i("onresume", "onResume: " + sceneOn);
//        switch (sceneOn) {
//            case 1:
//                scene1.enter();
//                attachAdapter();
//                break;
//            case 2:
//                scene2.enter();
//                attachAdapter();
//                break;
//            default:
//                break;
//
//        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
//        sceneOn = pref.getInt("scene_on", 1);
//        Log.i("onresume", "onResume: " + sceneOn);
//        switch (sceneOn) {
//            case 1:
//                scene1.enter();
//                attachAdapter();
//                break;
//            case 2:
//                scene2.enter();
//                attachAdapter();
//                break;
//            default:
//                break;
//
//        }
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
                shimmerList = findViewById(R.id.bottomOptions);


                List<row> list = new ArrayList<>();
                list.add(new row(R.drawable.settings, "Impostazioni"));
                list.add(new row(R.drawable.walking, "Trial description"));
                rAdapter = new RecyclerAdapter(this, list, this);
                DividerItemDecoration itemDecor = new DividerItemDecoration(shimmerList.getContext(), DividerItemDecoration.VERTICAL);
                shimmerList.addItemDecoration(itemDecor);

                try {
                    shimmerList.setAdapter(rAdapter);
                } catch (Exception e) {
                    e.printStackTrace();
                }
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

                shimmerList = findViewById(R.id.bottomOptions);
                List<row> list2 = new ArrayList<>();
                list2.add(new row(R.drawable.settings, "Impostazioni"));
                rAdapter = new RecyclerAdapter(this, list2, this);
                DividerItemDecoration itemDecor2 = new DividerItemDecoration(shimmerList.getContext(), DividerItemDecoration.VERTICAL);
                shimmerList.addItemDecoration(itemDecor2);

                shimmerList.setAdapter(rAdapter);

//                Toolbar mToolbar = findViewById(R.id.toolbar_shimmer);
////                setSupportActionBar(mToolbar);
////                getSupportActionBar().setTitle("");
////                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
////                getSupportActionBar().setDisplayShowHomeEnabled(true);



                break;
            default:
                break;

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                TransitionManager.go(scene1, transitionMgr);
                sceneOn = 1;
                storedScene(sceneOn);
                attachAdapter();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        sceneOn = pref.getInt("scene_on", 1);
        Log.i("onBackPressed", "onBackPressed: " + sceneOn);
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
        Log.i("onDestroy", "onDestroy: " + sceneOn);
    }
}
