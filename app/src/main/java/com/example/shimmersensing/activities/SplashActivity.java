package com.example.shimmersensing.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Base64;
import android.util.Log;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.example.shimmersensing.R;
import com.example.shimmersensing.global.GlobalValues;
import com.example.shimmersensing.utilities.QuestionTrial;
import com.example.shimmersensing.utilities.ShimmerData;
import com.example.shimmersensing.utilities.ShimmerTrial;
import com.example.shimmersensing.utilities.ShimmerTrialMusic;
import com.google.gson.Gson;
import com.google.gson.JsonArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class SplashActivity extends AppCompatActivity {

    private ArrayList<ShimmerTrial> shimmertrial;
    private SharedPreferences pref;
    private GlobalValues globalValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        globalValues = (GlobalValues) getApplicationContext();
        ImageView shimm_logo = findViewById(R.id.shimmerlogo);
        shimm_logo.setAnimation(AnimationUtils.loadAnimation(this, R.anim.zoom_in));

        pref = getApplicationContext().getSharedPreferences("ShimmerSensingSamplingConfig", 0);

        try {
            new GetDBData().execute("http://192.168.1.16:5000/api/v1/resources/shimmersensing/sensordata/get");
            Log.i("pippobaudo", "onCreate: ");
        } catch (Exception e) {
            Log.i("pippobaudoerror", "onCreate: ");
        }
//
//        new GetAudioTrial().execute("http://192.168.1.16:5000/api/v1/resources/shimmersensing/sensordata/audiotrial");

    }

    public void goAhead() {

        Intent intent = new Intent(SplashActivity.this, TrialPresentationActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

    }

    @SuppressLint("StaticFieldLeak")
    public class GetDBData extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            StringBuilder data = new StringBuilder();
            data.append("");
            HttpURLConnection httpURLConnection = null;
            try {

                httpURLConnection = (HttpURLConnection) new URL(params[0]).openConnection();

                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setConnectTimeout(2000);

                try {
                    Log.e("trycatch", "onCreate: ");
                    if (httpURLConnection.getInputStream() != null) {
                        InputStream in = new BufferedInputStream(httpURLConnection.getInputStream());

                        InputStreamReader inputStreamReader = new InputStreamReader(in);

                        int inputStreamData = inputStreamReader.read();
                        while (inputStreamData != -1) {
                            char current = (char) inputStreamData;
                            inputStreamData = inputStreamReader.read();
                            data.append(current);
                        }
                    } else {

                    }
                } catch (Exception e) {

                    e.printStackTrace();

                }

                Log.i("shimmer", "doInBackground: " + data.toString());
            } catch (Exception e) {
                e.printStackTrace();
                Log.i("caccapupu", "onCreate: ");

            } finally {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }

            }

            return data.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ArrayList<ShimmerData> list = new ArrayList<>();
            String jsonDB = result;
            if (jsonDB.equals("")) {
                goAhead();
            }
            if (jsonDB.contains("trial")) {

                shimmertrial = new ArrayList<>();
                String shimmer_name;
                String shimmer_mode;
                String mode;
                JSONArray domande;
                try {
                    JSONObject jObj = new JSONObject(jsonDB);
                    globalValues.setName(jObj.getString("name"));
                    globalValues.setSurname(jObj.getString("surname"));
                    globalValues.setDate(jObj.getString("date"));
                    jObj.get("shimmerdata");
                    JSONArray jarray = jObj.getJSONArray("shimmerdata");
                    for (int j = 0; j < jarray.length(); j++) {
                        ArrayList<QuestionTrial> aQuest = new ArrayList<>();
                        JSONObject curr = jarray.getJSONObject(j);
                        shimmer_name = curr.getString("trial_name");
                        if (shimmer_name.equals("musica")) {
                            String shimmer_audio = curr.getString("track");
//                            SharedPreferences.Editor editor = pref.edit();
//                            editor.putString("shimmeraudio", shimmer_audio);
//                            editor.apply();
                            shimmer_mode = curr.getString("trial_duration");
                            mode = curr.getString("mode");
                            domande = curr.getJSONArray("n_domande");
                            for (int k = 0; k < domande.length(); k++) {
                                JSONObject question = domande.getJSONObject(k);
                                aQuest.add(new QuestionTrial(question.getString("sheet_name"), question.getString("domanda"), question.getInt("range")));
                            }
                            ShimmerTrialMusic s = new ShimmerTrialMusic(shimmer_name, shimmer_mode, mode, aQuest, shimmer_audio);
                            shimmertrial.add(s);
                        } else {
                            shimmer_mode = curr.getString("trial_duration");
                            mode = curr.getString("mode");
                            domande = curr.getJSONArray("n_domande");
                            for (int k = 0; k < domande.length(); k++) {
                                JSONObject question = domande.getJSONObject(k);
                                aQuest.add(new QuestionTrial(question.getString("sheet_name"), question.getString("domanda"), question.getInt("range")));
                            }
                            ShimmerTrial s = new ShimmerTrial(shimmer_name, shimmer_mode, mode, aQuest);
                            shimmertrial.add(s);
                        }


                    }
                } catch (JSONException e) {
                    Log.i("pippobaudoerrorOnPost", "onCreate: ");
                    e.printStackTrace();
                }

                try {
                    SharedPreferences.Editor editor = pref.edit();
                    Gson gson = new Gson();
                    JsonArray jsonElements = (JsonArray) new Gson().toJsonTree(shimmertrial);
                    Log.i("shimmertrial", "run: " + jsonElements);
                    editor.putString("shimmertrial", String.valueOf(jsonElements));
                    editor.apply();
                    globalValues.setShimmerTrialArrayList(shimmertrial);

                    Log.i("TAG", String.valueOf(shimmertrial.size()));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                goAhead();
            } else {
                SharedPreferences pref = getApplicationContext().getSharedPreferences("ShimmerSensingSamplingConfig", 0);

                double ppg = 0;
                double gsrConductance = 0;
                double accel_x = 0;
                double accel_z = 0;
                double accel_y = 0;
                double magnetometer_x = 0, magnetometer_y = 0, magnetometer_z = 0;
                double gyroscope_x = 0, gyroscope_y = 0, gyroscope_z = 0;
                double timestamp = 0;
                try {
                    JSONArray jarray = new JSONArray(jsonDB);
                    for (int j = 0; j < jarray.length(); j++) {
                        JSONObject curr = jarray.getJSONObject(j);
                        ppg = curr.getDouble("PPG");
                        gsrConductance = curr.getDouble("gsrConductance");
                        accel_x = curr.getDouble("accelerometer_x");
                        accel_z = curr.getDouble("accelerometer_y");
                        accel_y = curr.getDouble("accelerometer_z");
                        magnetometer_x = curr.getDouble("gyroscope_x");
                        magnetometer_y = curr.getDouble("gyroscope_y");
                        magnetometer_z = curr.getDouble("gyroscope_z");
                        gyroscope_x = curr.getDouble("magnetometer_x");
                        gyroscope_y = curr.getDouble("magnetometer_y");
                        gyroscope_z = curr.getDouble("magnetometer_z");
                        timestamp = curr.getDouble("timestamp_shimmer");
                        ShimmerData s = new ShimmerData(ppg, gsrConductance, accel_x,
                                accel_y, accel_z, gyroscope_x, gyroscope_y, gyroscope_z,
                                magnetometer_x, magnetometer_y, magnetometer_z, timestamp);
                        list.add(s);

                    }

                    try {
                        SharedPreferences.Editor editor = pref.edit();
                        Gson gson = new Gson();
                        JsonArray jsonElements = (JsonArray) new Gson().toJsonTree(list);
                        Log.i("prova_json", "run: " + jsonElements);
                        editor.putString("shimmerdata", String.valueOf(jsonElements));
                        editor.apply();

                        Log.i("TAG", String.valueOf(list.size()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                    new GetDBData().execute("http://192.168.1.16:5000/api/v1/resources/shimmersensing/sensordata/trialdetails");

                } catch (JSONException e) {
                    Log.i("pippobaudoerrorOnPost", "onCreate: ");
                    e.printStackTrace();
                }


            }


        }
    }

    public class GetAudioTrial extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            StringBuilder data = new StringBuilder();
            Log.i("Prova", "cachi");
            HttpURLConnection httpURLConnection = null;
            try {

                httpURLConnection = (HttpURLConnection) new URL(params[0]).openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setDoOutput(true);
//                httpURLConnection.connect();
//                // getting file length
//                int lenghtOfFile = httpURLConnection.getContentLength();
//                // input stream to read file - with 8k buffer
//                InputStream input = new BufferedInputStream(httpURLConnection.getInputStream(),
//                        8192);
////            File download = new File(Environment.getExternalStorageDirectory()
////                    + "/download/");
////            if (!download.exists()) {
////                download.mkdir();
////            }
////            String strDownloaDuRL = download + "/" + "test";
////            Log.v("log_tag", " down url   " + strDownloaDuRL + " lenght: " + lenghtOfFile);
////            FileOutputStream output = new FileOutputStream(strDownloaDuRL);
////
////            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
//
//                ByteArrayOutputStream bos = new ByteArrayOutputStream();
//                int next = input.read();
//                while (next > -1) {
//                    bos.write(next);
//                    next = input.read();
//                }
//
//                bos.flush();
//                byte data_byte[] = bos.toByteArray();
//                bos.close();
//
//                Log.v("log_tag", "byte array " + data_byte.length);
                try {
                    Log.e("trycatch", "onCreate: ");
                    if (httpURLConnection.getInputStream() != null) {
                        InputStream in = new BufferedInputStream(httpURLConnection.getInputStream());

                        InputStreamReader inputStreamReader = new InputStreamReader(in);

                        int inputStreamData = inputStreamReader.read();
                        while (inputStreamData != -1) {
                            char current = (char) inputStreamData;
                            inputStreamData = inputStreamReader.read();
                            data.append(current);
                        }
                    } else {

                    }
                } catch (Exception e) {

                    e.printStackTrace();

                }

//
//                String saveThis = Base64.encodeToString(data_byte, Base64.DEFAULT);

                SharedPreferences.Editor editor = pref.edit();
                editor.putString("shimmeraudio", data.toString());
                editor.apply();


                long total = 0;


//            // flushing output
//            output.flush();
//
//            // closing streams
//            output.close();
//                input.close();


//            try {
//                InputStream in = new BufferedInputStream(httpURLConnection.getInputStream());
//                InputStreamReader inputStreamReader = new InputStreamReader(in);
////
//                int inputStreamData = inputStreamReader.read();
//                while (inputStreamData != -1) {
//                    char current = (char) inputStreamData;
//                    inputStreamData = inputStreamReader.read();
//                    data.append(current);
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
            }

            return data.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.i("TAG", result); // this is expecting a response code to be sent from your server upon receiving the POST data
        }
    }
}
