package com.example.shimmersensing.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.shimmersensing.R;
import com.example.shimmersensing.utilities.ShimmerTrialMusic;
import com.squareup.picasso.Picasso;

import static androidx.constraintlayout.widget.Constraints.TAG;
import static com.example.shimmersensing.interfaccia.Shimmer_interface.URL_FILE;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AudioFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AudioFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AudioFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private ShimmerTrialMusic shimmer_audio;
    private TextView trialNameView;
    private Button fButton;
    private ImageView trialImage;
    private boolean isRunning = false;
    private MediaPlayer mediaPlayer;
    private int actual_pos_media;
    private int bufferingLevel;
    private ProgressBar seekbar;
    private Handler seekHandler;
    private TextView remaining;
    private TextView elapsed;

    private Runnable updateSeekBar = new Runnable() {
        @SuppressLint("SetTextI18n")
        public void run() {
            if (mediaPlayer != null) {
                long totalDuration = mediaPlayer.getDuration();
                long currentDuration = mediaPlayer.getCurrentPosition();
                // Displaying Total Duration time
                remaining.setText("" + milliSecondsToTimer(totalDuration - currentDuration));
                // Displaying time completed playing
                elapsed.setText("" + milliSecondsToTimer(currentDuration));

                // Updating progress bar
                seekbar.setProgress((int) currentDuration);
                if (totalDuration == currentDuration) {
                    mediaPlayer.stop();

                } else {
                    // Call this thread again after 15 milliseconds => ~ 1000/60fps
                    seekHandler.postDelayed(this, 15);
                }
            }


        }
    };


    public AudioFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param shimmer_audio Parameter 1.
     * @return A new instance of fragment AudioFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AudioFragment newInstance(ShimmerTrialMusic shimmer_audio) {
        AudioFragment fragment = new AudioFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, shimmer_audio);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            shimmer_audio = (ShimmerTrialMusic) getArguments().getSerializable(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_audio, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        fButton = getView().findViewById(R.id.buttonSendForm);
        trialNameView = getView().findViewById(R.id.deviceName);
        trialImage = getView().findViewById(R.id.deviceImage);
        trialNameView.setText(shimmer_audio.getTrialName());
        remaining = getView().findViewById(R.id.totalProgress);
        elapsed = getView().findViewById(R.id.minuteProgress);
        seekbar = getView().findViewById(R.id.progressPlayer);
        Picasso.get()
                .load(URL_FILE.concat(shimmer_audio.getUrl_icon()))
                .into(trialImage);
        fButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mediaPlayer.isPlaying()) {

                    actual_pos_media = mediaPlayer.getCurrentPosition();
                    mediaPlayer.pause();
                    Log.i(TAG, "onClick: " + actual_pos_media);
                    if (mListener != null) {
                        mListener.onFragmentInteractionAudio(2);
                    }
                } else {
                    mediaPlayer.seekTo(actual_pos_media);
                    if (mListener != null) {
                        mListener.onFragmentInteractionAudio(1);
                    }
                }
            }
        });

        seekHandler = new Handler();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    String url = URL_FILE.concat(shimmer_audio.getUrl_file_audio());
                    Log.i(TAG, "url : " + url);
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mediaPlayer.setDataSource(url);
                    mediaPlayer.prepare(); // might take long! (for buffering, etc)
                    mediaPlayer.start();
                    seekbar.setProgress(0);
                    seekbar.setMax(mediaPlayer.getDuration());

                    // Updating progress bar
                    seekHandler.postDelayed(updateSeekBar, 15);
                    if (mListener != null) {
                        mListener.onFragmentInteractionAudio(69);
                    }
                    mediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
                        @Override
                        public void onSeekComplete(MediaPlayer arg0) {
                            Log.d(TAG, "onSeekComplete() current pos : " + arg0.getCurrentPosition());
                            SystemClock.sleep(200);
                            mediaPlayer.start();
                        }
                    });
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            Log.i(TAG, "onCompletion: ");
                            if (mListener != null) {
                                mListener.onFragmentInteractionAudio(3);
                            }
                            seekHandler.removeCallbacks(updateSeekBar);
                        }
                    });
                    Log.i(TAG, "onViewCreated: " + mediaPlayer.getDuration());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 300);

    }


    public String milliSecondsToTimer(long milliseconds) {
        String finalTimerString = "";
        String secondsString = "";

        // Convert total duration into time
        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
        // Add hours if there
        if (hours > 0) {
            finalTimerString = hours + ":";
        }

        // Prepending 0 to seconds if it is one digit
        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        // return timer string
        return finalTimerString;
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
        mediaPlayer.stop();
        mediaPlayer.release();
        Log.i(TAG, "onDetach: ");
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
        void onFragmentInteractionAudio(int uri);
    }
}
