package com.example.shimmersensing.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.transition.Scene;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.shimmersensing.R;
import com.example.shimmersensing.graphic.ZoomOutPageTransformer;
import com.example.shimmersensing.utilities.RecyclerAdapter;
import com.example.shimmersensing.utilities.row;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

public class TrialPresentationActivity extends AppCompatActivity implements RecyclerAdapter.OnShimmerListener {

    ViewGroup rootContainer;
    Scene scene1, scene2;
    private Transition transitionMgr;
    private RecyclerView shimmerList;
    private RecyclerAdapter rAdapter;
    private ViewPager2 mPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trial_presentation);




//        DividerItemDecoration horizontalDecoration = new DividerItemDecoration(shimmerList.getContext(),
//                DividerItemDecoration.HORIZONTAL);
//        Drawable horizontalDivider = ContextCompat.getDrawable(this, R.drawable.horizontal_divider);
//        horizontalDecoration.setDrawable(horizontalDivider);
//        shimmerList.addItemDecoration(horizontalDecoration);
        rootContainer =
                (ViewGroup) findViewById(R.id.rootContainer);

        transitionMgr = TransitionInflater.from(this)
                .inflateTransition(R.transition.trial_presentation_transition);

        scene1 = Scene.getSceneForLayout(rootContainer,
                R.layout.trial_pres_1, this);

        scene2 = Scene.getSceneForLayout(rootContainer,
                R.layout.trial_pres_2, this);

        scene1.enter();

        shimmerList = findViewById(R.id.bottomOptions);


        List<row> list = new ArrayList<>();
        list.add(new row(R.drawable.settings, "Impostazioni"));
        list.add(new row(R.drawable.walking, "Trial description"));
        rAdapter = new RecyclerAdapter(this, list, this);
        DividerItemDecoration itemDecor = new DividerItemDecoration(shimmerList.getContext(), DividerItemDecoration.VERTICAL);
        shimmerList.addItemDecoration(itemDecor);

        shimmerList.setAdapter(rAdapter);



    }

    public void switchScene(View view) {
        TransitionManager.go(scene2, transitionMgr);
        shimmerList = findViewById(R.id.bottomOptions);
        List<row> list = new ArrayList<>();
        list.add(new row(R.drawable.settings, "Impostazioni"));
        rAdapter = new RecyclerAdapter(this, list, this);
        DividerItemDecoration itemDecor = new DividerItemDecoration(shimmerList.getContext(), DividerItemDecoration.VERTICAL);
        shimmerList.addItemDecoration(itemDecor);

        shimmerList.setAdapter(rAdapter);
        mPager = findViewById(R.id.pager);
        mPager.setAdapter(new MyViewPagerAdapter(this));
        mPager.setPageTransformer(new ZoomOutPageTransformer());
        mPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                Log.i("TAG", "onPageSelected: "+position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabDots);
        new TabLayoutMediator(tabLayout, mPager,
                new TabLayoutMediator.TabConfigurationStrategy() {
                    @Override public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                    }
                }).attach();
    }


    @Override
    public void onShimmerClick(int position) {

    }

    public void addDevice(View view) {
        TransitionManager.go(scene1, transitionMgr);
        shimmerList = findViewById(R.id.bottomOptions);
        List<row> list = new ArrayList<>();
        list.add(new row(R.drawable.settings, "Impostazioni"));
        list.add(new row(R.drawable.walking, "Trial description"));
        rAdapter = new RecyclerAdapter(this, list, this);
        DividerItemDecoration itemDecor = new DividerItemDecoration(shimmerList.getContext(), DividerItemDecoration.VERTICAL);
        shimmerList.addItemDecoration(itemDecor);

        shimmerList.setAdapter(rAdapter);
    }


    public class MyViewPagerAdapter extends RecyclerView.Adapter<MyHolder> {

        private Context context;

        public MyViewPagerAdapter(Context context) {
            this.context=context;
        }

        @NonNull
        @Override
        public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new MyHolder(LayoutInflater.from(context).inflate(R.layout.fragment_screen_slide_page, parent, false));
        }

        @Override
        public void onBindViewHolder( MyHolder holder, int position) {
            holder.mText.setText("Page "+(position+1));
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("onresume", "onResume: ");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i("onRestart", "onRestart: ");
    }

    class MyHolder extends RecyclerView.ViewHolder {

        public TextView mText;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            mText = itemView.findViewById(R.id.sensorName);
        }
    }


}
