package com.example.shimmersensing.Activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.interpolator.view.animation.FastOutLinearInInterpolator;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.transition.ChangeImageTransform;
import android.transition.CircularPropagation;
import android.transition.Explode;
import android.transition.Fade;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionSet;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import com.example.shimmersensing.R;
import com.example.shimmersensing.Utilities.RecyclerAdapter;
import com.example.shimmersensing.Utilities.row;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements RecyclerAdapter.OnShimmerListener {

    private RecyclerView shimmerList;
    private RecyclerAdapter rAdapter;
    private GridLayoutManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar mToolbar = findViewById(R.id.toolbar_shimmer);
        //ColorDrawable colorDrawable = new ColorDrawable( Color.TRANSPARENT );
        //getWindow().setBackgroundDrawable( colorDrawable );


        shimmerList = findViewById(R.id.shimmer_sensors);
        manager = new GridLayoutManager(MainActivity.this, 2);
        shimmerList.setLayoutManager(manager);

        List<row> list= new ArrayList<>();
        list.add(new row(R.drawable.shimmer_sensor));
        list.add(new row(R.drawable.shimmer_sensor));
        list.add(new row(R.drawable.shimmer_sensor));

        rAdapter = new RecyclerAdapter(this, list, this);
        shimmerList.setAdapter(rAdapter);

        Slide slide =new Slide();
        slide.setSlideEdge(Gravity.TOP);
        slide.setDuration(300);
        slide.setPropagation(new CircularPropagation());

        Fade fade =new Fade();
        fade.setDuration(350);

        slide.setInterpolator(new LinearInterpolator());
        Transition transition = new TransitionSet()
                .addTransition(fade);
                //.addTransition(slide)

        getWindow().setExitTransition(transition);
    }

    @Override
    public void onShimmerClick(int position) {
        Intent intent = new Intent(this, ShimmerSpec.class);
// Pass data object in the bundle and populate details activity.
        Pair<View, String> p1 = Pair.create(findViewById(R.id.ivImage), "shimmer_sensor");
        Pair<View, String> p2 = Pair.create(findViewById(R.id.tvTitle), "shimmer_text");
        Pair<View, String> p3 = Pair.create(findViewById(R.id.toolbar_shimmer), "toolbar_shimmer");
        ActivityOptionsCompat options = ActivityOptionsCompat.
                makeSceneTransitionAnimation(this, p3, p2, p1);
        startActivity(intent, options.toBundle());


        //getWindow().setExitTransition(null);

    }
}
