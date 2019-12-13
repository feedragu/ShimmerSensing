package com.example.shimmersensing.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.transition.Scene;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;

import com.example.shimmersensing.R;

public class TrialPresentationActivity extends AppCompatActivity {

    ViewGroup rootContainer;
    Scene scene1, scene2;
    private Transition transitionMgr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trial_presentation);

        rootContainer =
                (ViewGroup) findViewById(R.id.rootContainer);

        transitionMgr = TransitionInflater.from(this)
                .inflateTransition(R.transition.trial_presentation_transition);

        scene1 = Scene.getSceneForLayout(rootContainer,
                R.layout.trial_scene_1, this);

        scene2 = Scene.getSceneForLayout(rootContainer,
                R.layout.trial_scene_2, this);

        scene1.enter();

    }

    public void switchScene(View view) {
        TransitionManager.go(scene2, transitionMgr);
    }

}
