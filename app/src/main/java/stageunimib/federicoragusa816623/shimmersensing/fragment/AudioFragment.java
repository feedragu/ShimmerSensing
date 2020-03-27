package stageunimib.federicoragusa816623.shimmersensing.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.fragment.app.Fragment;

import android.os.CountDownTimer;
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
import stageunimib.federicoragusa816623.shimmersensing.utilities.ShimmerTrialMusic;
import com.squareup.picasso.Picasso;

import stageunimib.federicoragusa816623.shimmersensing.interfaccia.Shimmer_interface;

import static androidx.constraintlayout.widget.Constraints.TAG;


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
    private MotionLayout motion_layout;
    private TextView audio_name;


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

        motion_layout = ((MotionLayout)getView().findViewById(R.id.motionlayout_fragment));
        fButton = getView().findViewById(R.id.buttonSendForm);
        trialNameView = getView().findViewById(R.id.deviceName);
        trialImage = getView().findViewById(R.id.deviceImage);
        trialNameView.setText(shimmer_audio.getTrialName());
        audio_name = getView().findViewById(R.id.audioName);
        remaining = getView().findViewById(R.id.totalProgress);
        elapsed = getView().findViewById(R.id.minuteProgress);
        seekbar = getView().findViewById(R.id.progressPlayer);
        int endIndex = shimmer_audio.getUrl_file_audio().lastIndexOf("/");
        if (endIndex != -1)
        {
            String audioname = shimmer_audio.getUrl_file_audio().substring(endIndex+1);
            audio_name.setText(audioname);
        }

        Picasso.get()
                .load(Shimmer_interface.URL_FILE.concat(shimmer_audio.getUrl_icon()))
                .into(trialImage);
        fButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mediaPlayer.isPlaying()) {

                    actual_pos_media = mediaPlayer.getCurrentPosition();
                    mediaPlayer.pause();
                    if (mListener != null) {
                        mListener.onFragmentInteractionAudio(2);
                    }
                    seekHandler.removeCallbacks(updateSeekBar);
                } else {
                    mediaPlayer.seekTo(actual_pos_media);
                    seekHandler.postDelayed(updateSeekBar, 15);
                    if (mListener != null) {
                        mListener.onFragmentInteractionAudio(1);
                    }
                }
            }
        });

        seekHandler = new Handler();

        final Handler handler = new Handler();


        new CountDownTimer(2500, 1000) {

            public void onTick(long millisUntilFinished) {
                if(millisUntilFinished <= 1501) {
                    motion_layout.transitionToEnd();
                }
            }

            public void onFinish() {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String url = Shimmer_interface.URL_FILE.concat(shimmer_audio.getUrl_file_audio());
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
                                    if (mListener != null) {
                                        mListener.onFragmentInteractionAudio(3);
                                    }
                                    seekHandler.removeCallbacks(updateSeekBar);
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, 300);
            }

        }.start();

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
        if(mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            seekHandler.removeCallbacks(updateSeekBar);
        }
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
