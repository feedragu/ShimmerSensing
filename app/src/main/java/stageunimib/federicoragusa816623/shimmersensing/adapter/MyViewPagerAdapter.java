package stageunimib.federicoragusa816623.shimmersensing.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shimmersensing.R;
import stageunimib.federicoragusa816623.shimmersensing.activities.TrialRecapActivity;
import stageunimib.federicoragusa816623.shimmersensing.global.GlobalValues;
import stageunimib.federicoragusa816623.shimmersensing.utilities.ShimmerSensorDevice;

import com.shimmerresearch.android.manager.ShimmerBluetoothManagerAndroid;
import com.shimmerresearch.driver.ShimmerDevice;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MyViewPagerAdapter extends RecyclerView.Adapter<MyHolder> {

    private Context context;
    private ArrayList<ShimmerSensorDevice> shimmerSensor;
    private SharedPreferences pref;
    private GlobalValues gv;
    private ShimmerBluetoothManagerAndroid btManager;
    private ShimmerDevice shimmerDevice = null;
    private String shimmerBtAdd = "00:00:00:00:00:00";
    private String LOG_TAG = "pageadpater_connection";

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
    public void onBindViewHolder(final MyHolder holder, final int position) {
        holder.sensorname.setText(shimmerSensor.get(position).getDeviceName());
        holder.macAddress.setText(shimmerSensor.get(position).getMacAddress());
        holder.sampleRate.setText(shimmerSensor.get(position).getSampleRate() + " Hz");
        holder.lastUse.setText(shimmerSensor.get(position).getLastUse().toString());

        Picasso.get()
                .load(R.drawable.shimmer_sensor)
                .placeholder(R.drawable.shimmer_sensor)
                .into(holder.sensorImageCard);

        holder.sensorCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.sensorCard.setTransitionName("cardView");
                holder.sensorImageCard.setTransitionName("sensorImageCard");
                Pair<View, String> pair1 = Pair.create((View) holder.sensorCard, holder.sensorCard.getTransitionName());
                Pair<View, String> pair2 = Pair.create((View) holder.sensorImageCard, holder.sensorImageCard.getTransitionName());
                ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) context, pair1);
                Intent intent = new Intent(context, TrialRecapActivity.class);
                gv = (GlobalValues) context.getApplicationContext();
                gv.setSsd(shimmerSensor.get(position));


                context.startActivity(intent, optionsCompat.toBundle());
            }
        });

    }

    @Override
    public int getItemCount() {
        return shimmerSensor.size();
    }


}

