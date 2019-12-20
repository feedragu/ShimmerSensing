package com.example.shimmersensing.fragment;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.shimmersensing.R;
import com.example.shimmersensing.utilities.QuestionTrial;
import com.google.android.material.button.MaterialButton;
import com.hookedonplay.decoviewlib.DecoView;
import com.hookedonplay.decoviewlib.charts.SeriesItem;
import com.hookedonplay.decoviewlib.events.DecoEvent;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.ArrayList;

import static androidx.constraintlayout.widget.Constraints.TAG;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CountDownFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CountDownFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CountDownFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private MaterialButton fButton;
    private AnimationDrawable pausePlayAnim;
    private Handler timeHandler = new Handler();


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private int secondsCountDown;
    private CountDownTimer countDown;
    private TextView textPercentage;
    private DecoView arcView;
    private int series1Index;
    private long lastSeconds;
    private boolean isRunning;
    private String trialName;
    private TextView trialNameView;
    private ImageView trialImage;

    public CountDownFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment questionaryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CountDownFragment newInstance(String trialName) {
        CountDownFragment fragment = new CountDownFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, trialName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            trialName = getArguments().getString(ARG_PARAM1);
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_countdown, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        fButton = getView().findViewById(R.id.buttonSendForm);
        trialNameView = getView().findViewById(R.id.trialName);
        trialImage = getView().findViewById(R.id.trialImage);
        trialNameView.setText(trialName);
        int resourceImage = getView().getContext().getResources().getIdentifier(trialName, "drawable", getView().getContext().getPackageName());
        Picasso.get()
                .load(resourceImage)
                .into(trialImage);
        final AnimationDrawable ad = (AnimationDrawable) fButton.getIcon();
        fButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isRunning) {
                    ad.start();
                    isRunning = false;
                    Log.i(TAG, "stopPlay: ");
                    if (mListener != null) {
                        mListener.onFragmentInteraction(2);
                    }
                    timerPause();
                } else {
                    Log.i(TAG, "resumePlay: ");
                    timerResume();
                    if (mListener != null) {
                        mListener.onFragmentInteraction(1);
                    }
                }
            }
        });

        arcView = (DecoView) getView().findViewById(R.id.progressBar);

        arcView.addSeries(new SeriesItem.Builder(Color.LTGRAY)
                .setRange(0, 10, 10)
                .setInitialVisibility(true)
                .setLineWidth(20f)
                .build());

        SeriesItem seriesItem1 = new SeriesItem.Builder(getThemeAccentColor(getView().getContext()))
                .setRange(0, 10, 10)
                .setLineWidth(20f)
                .build();
        series1Index = arcView.addSeries(seriesItem1);

        textPercentage = getView().findViewById(R.id.timerCountdown);
        final SeriesItem finalSeriesItem = seriesItem1;

        timerStart(10000);


    }

    public static int getThemeAccentColor(final Context context) {
        final TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorAccent, value, true);
        return value.data;
    }


    public void timerPause() {
        countDown.cancel();
    }

    private void timerResume() {
        Log.i("Sec", Long.toString(lastSeconds));
        timerStart(lastSeconds);
    }


    public void timerStart(long timeLengthMilli) {
        textPercentage.setText("" + timeLengthMilli / 1000);
        countDown = new CountDownTimer(timeLengthMilli, 1000) {

            public void onTick(long millisUntilFinished) {
                lastSeconds = millisUntilFinished;
                isRunning = true;
                arcView.addEvent(new DecoEvent.Builder(millisUntilFinished / 1000)
                        .setIndex(series1Index)
                        .setDuration(200)
                        .build());
                textPercentage.setText("" + millisUntilFinished / 1000);
            }

            public void onFinish() {
                textPercentage.setText("done!");
                new CountDownTimer(2000, 1000) {

                    public void onTick(long millisUntilFinished) {
                    }

                    public void onFinish() {
                        if (mListener != null) {
                            mListener.onFragmentInteraction(3);
                        }
                    }

                }.start();
                isRunning = false;
            }

        }.start();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(int event);
    }
}
