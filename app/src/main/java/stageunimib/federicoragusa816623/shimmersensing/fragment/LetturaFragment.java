package stageunimib.federicoragusa816623.shimmersensing.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.ToneGenerator;
import android.os.Bundle;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.fragment.app.Fragment;

import com.example.shimmersensing.R;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import stageunimib.federicoragusa816623.shimmersensing.interfaccia.Shimmer_interface;
import stageunimib.federicoragusa816623.shimmersensing.utilities.ShimmerTrialLettura;
import stageunimib.federicoragusa816623.shimmersensing.utilities.ShimmerTrialMusic;

import static androidx.constraintlayout.widget.Constraints.TAG;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LetturaFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LetturaFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LetturaFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private ShimmerTrialLettura shimmer_lettura;
    private TextView trialNameView;
    private Button fButton;
    private ImageView trialImage;
    private boolean isRunning = false;
    private MediaPlayer mediaPlayer;
    private int actual_pos_media;
    private int bufferingLevel;
    private TextView text_lettura;

    private Runnable updateSeekBar = new Runnable() {
        @SuppressLint("SetTextI18n")
        public void run() {
//            if (mediaPlayer != null) {
//                long totalDuration = mediaPlayer.getDuration();
//                long currentDuration = mediaPlayer.getCurrentPosition();
//                // Displaying Total Duration time
//                remaining.setText("" + milliSecondsToTimer(totalDuration - currentDuration));
//                // Displaying time completed playing
//                elapsed.setText("" + milliSecondsToTimer(currentDuration));
//
//                // Updating progress bar
//                seekbar.setProgress((int) currentDuration);
//                if (totalDuration == currentDuration) {
//                    mediaPlayer.stop();
//
//                } else {
//                    // Call this thread again after 15 milliseconds => ~ 1000/60fps
//                    seekHandler.postDelayed(this, 1000);
//                }
//            }


        }
    };
    private StringBuilder content;
    private MotionLayout motion_layout;

    public void updateText() {

    }


    public LetturaFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param shimmer_lettura Parameter 1.
     * @return A new instance of fragment AudioFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LetturaFragment newInstance(ShimmerTrialLettura shimmer_lettura) {
        LetturaFragment fragment = new LetturaFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, shimmer_lettura);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            shimmer_lettura = (ShimmerTrialLettura) getArguments().getSerializable(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_lettura, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        fButton = getView().findViewById(R.id.buttonSendForm);
        text_lettura = getView().findViewById(R.id.textLettura);
        trialNameView = getView().findViewById(R.id.deviceName);
        trialImage = getView().findViewById(R.id.deviceImage);
        trialNameView.setText(shimmer_lettura.getTrialName());
        motion_layout = ((MotionLayout)getView().findViewById(R.id.motionlayout_fragment));
        Picasso.get()
                .load(Shimmer_interface.URL_FILE.concat(shimmer_lettura.getUrl_icon()))
                .into(trialImage);
        fButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onFragmentInteractionLettura(3);
//                if (mediaPlayer.isPlaying()) {
//
//                    actual_pos_media = mediaPlayer.getCurrentPosition();
//                    mediaPlayer.pause();
//                    Log.i(TAG, "onClick: " + actual_pos_media);
//                    if (mListener != null) {
//                        mListener.onFragmentInteractionAudio(2);
//                    }
//                } else {
//                    mediaPlayer.seekTo(actual_pos_media);
//                    if (mListener != null) {
//                        mListener.onFragmentInteractionAudio(1);
//                    }
//                }
            }
        });

        new Thread(new Runnable(){
            @Override
            public void run() {
                content = new StringBuilder();

                // many of these calls can throw exceptions, so i've just
                // wrapped them all in one try/catch statement.
                try
                {
                    // create a url object
                    URL url = new URL(Shimmer_interface.URL_FILE.concat(shimmer_lettura.getUrl_file_text()));

                    // create a urlconnection object
                    URLConnection urlConnection = url.openConnection();

                    // wrap the urlconnection in a bufferedreader
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                    String line;

                    // read from the urlconnection via the bufferedreader
                    while ((line = bufferedReader.readLine()) != null)
                    {
                        content.append(line + "\n");
                    }
                    bufferedReader.close();
                    Log.i(TAG, "run: "+content.toString());
                    updateText();
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        }).start();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                Log.i(TAG, "run: "+Shimmer_interface.URL_FILE.concat(shimmer_lettura.getUrl_file_text()));
                text_lettura.setText(content.toString());

            }
        }, 300);

        new CountDownTimer(4000, 1000) {

            public void onTick(long millisUntilFinished) {
                if(millisUntilFinished <= 3001) {
                    motion_layout.transitionToEnd();
                }
            }

            public void onFinish() {
                if (mListener != null) {
                    mListener.onFragmentInteractionLettura(69);
                }
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
        void onFragmentInteractionLettura(int uri);
    }
}
