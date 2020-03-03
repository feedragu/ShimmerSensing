package stageunimib.federicoragusa816623.shimmersensing.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shimmersensing.R;

public class MyHolder extends RecyclerView.ViewHolder {

    public CardView sensorCard;
    public ImageView sensorImageCard;
    public TextView sensorname;
    public TextView macAddress;
    public TextView sampleRate;
    public TextView lastUse;

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