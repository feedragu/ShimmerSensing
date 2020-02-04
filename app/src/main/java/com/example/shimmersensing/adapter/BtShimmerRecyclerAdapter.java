package com.example.shimmersensing.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shimmersensing.R;
import com.example.shimmersensing.utilities.ShimmerTrial;
import com.squareup.picasso.Picasso;

import java.util.List;

public class BtShimmerRecyclerAdapter extends RecyclerView.Adapter<BtShimmerRecyclerAdapter.ImageViewHolder> {

    private Context mContext;
    private List<BluetoothDevice> btShimmer;
    private OnBtClickListener OnBtClickListener;

    public BtShimmerRecyclerAdapter(Context mContext, List<BluetoothDevice> btShimmer, OnBtClickListener OnBtClickListener) {
        this.mContext = mContext;
        this.btShimmer = btShimmer;
        this.OnBtClickListener=OnBtClickListener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(mContext).inflate(R.layout.bluetooth_shimmer_item, parent, false);
        return new ImageViewHolder(view, OnBtClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Picasso.get()
                .load(R.drawable.bt_icon)
                .into(holder.btImage);
        if(btShimmer.get(position).getName() != null) {
            holder.btName.setText(btShimmer.get(position).getName());
        }else {
            holder.btName.setText(btShimmer.get(position).getAddress());
        }


    }

    @Override
    public int getItemCount() {
        return btShimmer.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView btImage;
        TextView btName;
        OnBtClickListener onShimmerListener;

        public ImageViewHolder(@NonNull View itemView, OnBtClickListener onShimmerListener) {
            super(itemView);
            this.onShimmerListener=onShimmerListener;
            itemView.setOnClickListener(this);
            btImage = itemView.findViewById(R.id.deviceImage);
            btName= itemView.findViewById(R.id.deviceName);
        }

        @Override
        public void onClick(View v) {
            onShimmerListener.OnBtClickListener(getAdapterPosition());
        }
    }

    public interface OnBtClickListener {
        void OnBtClickListener(int position);
    }

}
