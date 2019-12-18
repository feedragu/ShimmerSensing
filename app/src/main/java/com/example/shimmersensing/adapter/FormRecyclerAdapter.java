package com.example.shimmersensing.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shimmersensing.R;
import com.example.shimmersensing.utilities.QuestionTrial;
import com.example.shimmersensing.utilities.ShimmerTrial;
import com.squareup.picasso.Picasso;

import java.util.List;

public class FormRecyclerAdapter extends RecyclerView.Adapter<FormRecyclerAdapter.ImageViewHolder> {

    private Context mContext;
    private List<QuestionTrial> formTrial;
    private RadioButton radioButton;

    public FormRecyclerAdapter(Context mContext, List<QuestionTrial> formTrial) {
        this.mContext = mContext;
        this.formTrial = formTrial;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.form_row_item, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        for (int i = 0; i < formTrial.get(position).getRange(); i++) {
            radioButton = new RadioButton(mContext);
            radioButton.setText("");
            radioButton.setId(i);
            holder.radioGroup.addView(radioButton);

            int keyI = i;
            radioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(mContext, "click " + radioButton.getId(), Toast.LENGTH_SHORT).show();

                }
            });
        }
        holder.rangeMin.setText("1");
        holder.rangeMax.setText(""+(formTrial.get(position).getRange()));
        holder.nomeDomanda.setText(formTrial.get(position).getNome_domanda());
        holder.numeroDomanda.setText(""+(position+1));

    }

    @Override
    public int getItemCount() {
        return formTrial.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        RadioGroup radioGroup;
        TextView nomeDomanda, numeroDomanda, rangeMin, rangeMax;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            radioGroup = itemView.findViewById(R.id.formRadio);
            nomeDomanda = itemView.findViewById(R.id.nomeDomanda);
            numeroDomanda = itemView.findViewById(R.id.numeroDomanda);
            rangeMin = itemView.findViewById(R.id.rangeMin);
            rangeMax = itemView.findViewById(R.id.rangeMax);

        }

        @Override
        public void onClick(View v) {
        }
    }


}
