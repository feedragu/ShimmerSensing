package com.example.shimmersensing.Activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.transition.AutoTransition;
import android.transition.ChangeBounds;
import android.transition.ChangeImageTransform;
import android.transition.Explode;
import android.transition.Fade;
import android.transition.Slide;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.Toolbar;

import com.example.shimmersensing.R;

public class ShimmerSpec extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shimmer_spec);
//        ActionBar actionBar = getSupportActionBar();
//        actionBar.setTitle("Shimmer specification");
//        Fade fade = new Fade();
//        fade.setStartDelay(0);
//        fade.setMode(Fade.MODE_OUT);
//        fade.setDuration(0);
        Fade fade =new Fade();
        fade.setDuration(115);
        getWindow().setSharedElementExitTransition(new ChangeImageTransform());
        getWindow().setEnterTransition(fade);

        Slide slide =new Slide();
        slide.setSlideEdge(Gravity.BOTTOM);
        getWindow().setSharedElementExitTransition(new ChangeImageTransform());
        getWindow().setExitTransition(slide);

    }

    @Override
    public void onBackPressed() {
        //To support reverse transitions when user clicks the device back button
        supportFinishAfterTransition();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                supportFinishAfterTransition();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
