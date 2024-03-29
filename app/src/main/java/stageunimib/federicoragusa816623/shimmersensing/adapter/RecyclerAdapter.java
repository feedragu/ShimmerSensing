package stageunimib.federicoragusa816623.shimmersensing.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shimmersensing.R;
import stageunimib.federicoragusa816623.shimmersensing.utilities.row;
import com.squareup.picasso.Picasso;

import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ImageViewHolder> {

    private Context mContext;
    private List<row> mdata;
    private OnShimmerListener mOnShimmerListener;

    public RecyclerAdapter(Context mContext, List<row> mdata, OnShimmerListener mOnShimmerListener) {
        this.mContext = mContext;
        this.mdata = mdata;
        this.mOnShimmerListener = mOnShimmerListener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_item_shimmer, parent, false);
        return new ImageViewHolder(view, mOnShimmerListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Picasso.get()
                .load(mdata.get(position).getImg())
                .into(holder.img);
        holder.text.setText(mdata.get(position).getRow_name());

    }

    @Override
    public int getItemCount() {
        return mdata.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView img;
        TextView text;
        OnShimmerListener onShimmerListener;

        public ImageViewHolder(@NonNull View itemView, OnShimmerListener onShimmerListener) {
            super(itemView);
            this.onShimmerListener = onShimmerListener;
            itemView.setOnClickListener(this);
            img = itemView.findViewById(R.id.infoDrawable);
            text = itemView.findViewById(R.id.infoBottom);
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
