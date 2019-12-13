package com.example.shimmersensing.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.transition.Scene;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;

import com.example.shimmersensing.R;
import com.example.shimmersensing.utilities.RecyclerAdapter;
import com.example.shimmersensing.utilities.row;

import java.util.ArrayList;
import java.util.List;

public class TrialPresentationActivity extends AppCompatActivity implements RecyclerAdapter.OnShimmerListener {

    ViewGroup rootContainer;
    Scene scene1, scene2;
    private Transition transitionMgr;
    private RecyclerView shimmerList;
    private RecyclerAdapter rAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trial_presentation);

        shimmerList = findViewById(R.id.bottomOptions);


        List<row> list = new ArrayList<>();
        list.add(new row(R.drawable.settings, "Impostazioni"));
        list.add(new row(R.drawable.walking, "Trial description"));
        rAdapter = new RecyclerAdapter(this, list, this);
        DividerItemDecoration itemDecor = new DividerItemDecoration(shimmerList.getContext(), DividerItemDecoration.VERTICAL);
        shimmerList.addItemDecoration(itemDecor);

        shimmerList.setAdapter(rAdapter);


//        DividerItemDecoration horizontalDecoration = new DividerItemDecoration(shimmerList.getContext(),
//                DividerItemDecoration.HORIZONTAL);
//        Drawable horizontalDivider = ContextCompat.getDrawable(this, R.drawable.horizontal_divider);
//        horizontalDecoration.setDrawable(horizontalDivider);
//        shimmerList.addItemDecoration(horizontalDecoration);
//        rootContainer =
//                (ViewGroup) findViewById(R.id.rootContainer);
//
//        transitionMgr = TransitionInflater.from(this)
//                .inflateTransition(R.transition.trial_presentation_transition);
//
//        scene1 = Scene.getSceneForLayout(rootContainer,
//                R.layout.trial_scene_1, this);
//
//        scene2 = Scene.getSceneForLayout(rootContainer,
//                R.layout.trial_scene_2, this);
//
//        scene1.enter();

    }

    public void switchScene(View view) {
        TransitionManager.go(scene2, transitionMgr);
    }

    @Override
    public void onShimmerClick(int position) {

    }


}
