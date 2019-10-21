package com.example.shimmersensing.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.transition.ChangeImageTransform;
import android.transition.Explode;
import android.transition.Fade;
import android.transition.Slide;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

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

        shimmerList = findViewById(R.id.shimmer_sensors);
        manager = new GridLayoutManager(MainActivity.this, 2);
        shimmerList.setLayoutManager(manager);

        List<row> list= new ArrayList<>();
        list.add(new row(R.drawable.shimmer_sensor));

        rAdapter = new RecyclerAdapter(this, list, this);
        shimmerList.setAdapter(rAdapter);
        //supportPostponeEnterTransition();
//        Fade fade = new Fade();
//        fade.setStartDelay(0);
//        fade.setMode(Fade.MODE_IN);
//        fade.setDuration(0);
        Slide slide =new Slide();
        slide.setSlideEdge(Gravity.TOP);
        slide.setDuration(270);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        getWindow().setSharedElementExitTransition(new ChangeImageTransform());
        getWindow().setExitTransition(slide);
        getWindow().setSharedElementEnterTransition(new ChangeImageTransform());
    }

    @Override
    public void onShimmerClick(int position) {
        Intent intent = new Intent(this, ShimmerSpec.class);
// Pass data object in the bundle and populate details activity.
        Pair<View, String> p1 = Pair.create(findViewById(R.id.ivImage), "shimmer_sensor");
        Pair<View, String> p2 = Pair.create(findViewById(R.id.tvTitle), "shimmer_text");
        ActivityOptionsCompat options = ActivityOptionsCompat.
                makeSceneTransitionAnimation(this, p1, p2);
        startActivity(intent, options.toBundle());


        //getWindow().setExitTransition(null);

    }
}
