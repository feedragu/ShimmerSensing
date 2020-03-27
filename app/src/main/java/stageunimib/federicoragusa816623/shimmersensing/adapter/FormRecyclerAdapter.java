package stageunimib.federicoragusa816623.shimmersensing.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shimmersensing.R;
import stageunimib.federicoragusa816623.shimmersensing.utilities.QuestionTrial;

import java.util.List;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class FormRecyclerAdapter extends RecyclerView.Adapter<FormRecyclerAdapter.ImageViewHolder> {

    private Context mContext;
    private List<QuestionTrial> formTrial;
    private RadioButton radioButton;
    private OnFormValueClickListener onFormValueClickListener;

    public FormRecyclerAdapter(Context mContext, List<QuestionTrial> formTrial, OnFormValueClickListener onFormValueClickListener) {
        this.mContext = mContext;
        this.formTrial = formTrial;
        this.onFormValueClickListener=onFormValueClickListener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.form_row_item, parent, false);
        return new ImageViewHolder(view, onFormValueClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, final int position) {
        for (int i = 0; i < formTrial.get(position).getRange(); i++) {
            final RadioButton radioButton = new RadioButton(mContext);
            radioButton.setText("");
            radioButton.setId(i);
            holder.radioGroup.addView(radioButton);

            int keyI = i;
            radioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onFormValueClickListener.OnFormValueClickListener(position, radioButton.getId());
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
        OnFormValueClickListener onFormValueClickListener;

        public ImageViewHolder(@NonNull View itemView, OnFormValueClickListener onFormValueClickListener) {
            super(itemView);
            itemView.setOnClickListener(this);
            this.onFormValueClickListener=onFormValueClickListener;
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

    public interface OnFormValueClickListener {
        void OnFormValueClickListener(int position, int value);
    }


}
