package com.example.shimmersensing.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.transition.ChangeImageTransform;
import android.transition.Explode;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionSet;
import android.view.Gravity;
import android.view.MenuItem;

import com.example.shimmersensing.R;

public class ShimmerSpec extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shimmer_spec);

        Toolbar mToolbar = findViewById(R.id.toolbar_shimmer);
        mToolbar.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        ColorDrawable colorDrawable = new ColorDrawable( Color.TRANSPARENT );
        getWindow().setBackgroundDrawable( colorDrawable );
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

//        Fade fade = new Fade();
//        fade.setStartDelay(0);
//        fade.setMode(Fade.MODE_OUT);
//        fade.setDuration(0);

//        Fade fade =new Fade();
//        //fade.setDuration(115);
//        Slide slide =new Slide();
//        slide.setSlideEdge(Gravity.BOTTOM);
        final Rect epicenter=new Rect(getCurrentFocus().getLeft(), getCurrentFocus().getTop(), getCurrentFocus().getRight(), getCurrentFocus().getBottom());;
        Explode explode = new Explode() {
            {
                super.setEpicenterCallback(new Transition.EpicenterCallback() {
                    @Override
                    public Rect onGetEpicenter(Transition transition) {
                        return epicenter;
                    };

            });
            }
        };
        Transition transition = new TransitionSet()
                .addTransition(explode);
        getWindow().setSharedElementExitTransition(new ChangeImageTransform());
        getWindow().setEnterTransition(transition);

        getWindow().setSharedElementExitTransition(new ChangeImageTransform());
        Transition transition2 = new TransitionSet()
        .addTransition(explode);
        getWindow().setExitTransition(transition2);

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
}
