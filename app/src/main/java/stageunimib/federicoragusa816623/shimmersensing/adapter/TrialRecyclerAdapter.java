package stageunimib.federicoragusa816623.shimmersensing.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shimmersensing.R;
import stageunimib.federicoragusa816623.shimmersensing.interfaccia.Shimmer_interface;
import stageunimib.federicoragusa816623.shimmersensing.utilities.ShimmerTrial;
import com.squareup.picasso.Picasso;

import java.util.List;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class TrialRecyclerAdapter extends RecyclerView.Adapter<TrialRecyclerAdapter.ImageViewHolder> implements Shimmer_interface {

    private Context mContext;
    private List<ShimmerTrial> shimmerTrial;
    private OnShimmerListener mOnShimmerListener;

    public TrialRecyclerAdapter(Context mContext, List<ShimmerTrial> shimmerTrial, OnShimmerListener mOnShimmerListener) {
        this.mContext = mContext;
        this.shimmerTrial = shimmerTrial;
        this.mOnShimmerListener=mOnShimmerListener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(mContext).inflate(R.layout.trial_row_item, parent, false);
        return new ImageViewHolder(view, mOnShimmerListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        int resourceImage = mContext.getResources().getIdentifier(shimmerTrial.get(position).getTrialName().toLowerCase(), "drawable", mContext.getPackageName());
        Picasso.get()
                .load(URL_FILE.concat(shimmerTrial.get(position).getUrl_icon()))
                .into(holder.trialImage);
        holder.trialName.setText(shimmerTrial.get(position).getTrialName());
        holder.trialDuration.setText(shimmerTrial.get(position).getTrialDuration());
        holder.nDomande.setText(""+shimmerTrial.get(position).getN_domande().size());

    }

    @Override
    public int getItemCount() {
        return shimmerTrial.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView trialImage;
        TextView trialName, trialDuration, nDomande;
        OnShimmerListener onShimmerListener;

        public ImageViewHolder(@NonNull View itemView, OnShimmerListener onShimmerListener) {
            super(itemView);
            this.onShimmerListener=onShimmerListener;
            itemView.setOnClickListener(this);
            trialImage = itemView.findViewById(R.id.deviceImage);
            trialName= itemView.findViewById(R.id.deviceName);
            trialDuration= itemView.findViewById(R.id.trialDuration);
            nDomande= itemView.findViewById(R.id.nDomande);
        }

        @Override
        public void onClick(View v) {
            onShimmerListener.onShimmerClick(getAdapterPosition());
        }
    }

    public interface OnShimmerListener {
        void onShimmerClick(int position);
    }

}
