package com.example.shimmersensing.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.example.shimmersensing.R;
import com.example.shimmersensing.utilities.ShimmerData;
import com.example.shimmersensing.utilities.ShimmerTrial;
import com.google.gson.Gson;
import com.google.gson.JsonArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class SplashActivity extends AppCompatActivity {

    private ArrayList<ShimmerTrial> shimmertrial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView shimm_logo = findViewById(R.id.shimmerlogo);
        shimm_logo.setAnimation(AnimationUtils.loadAnimation(this, R.anim.zoom_in));
        try {
            new GetDBData().execute("http://192.168.1.16:5000/api/v1/resources/shimmersensing/sensordata/get");
            Log.i("pippobaudo", "onCreate: ");
        } catch (Exception e) {
            Log.i("pippobaudoerror", "onCreate: ");
        }

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
                    if(httpURLConnection.getInputStream() != null) {
                        InputStream in = new BufferedInputStream(httpURLConnection.getInputStream());

                        InputStreamReader inputStreamReader = new InputStreamReader(in);

                        int inputStreamData = inputStreamReader.read();
                        while (inputStreamData != -1) {
                            char current = (char) inputStreamData;
                            inputStreamData = inputStreamReader.read();
                            data.append(current);
                        }
                    }else {

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
            Log.i("pippobaudoerrorOnPost", "onCreate: ");
            ArrayList<ShimmerData> list = new ArrayList<>();
            String jsonDB = result;
            Log.i(" ca", "onPostExecute: " + jsonDB);
            if (jsonDB.equals("")) {
                goAhead();
            }
            if (jsonDB.contains("trial")) {

                shimmertrial = new ArrayList<>();
                SharedPreferences pref = getApplicationContext().getSharedPreferences("ShimmerSensingSamplingConfig", 0);

                String shimmer_name;
                String shimmer_mode;
                String mode;
                String domande;
                try {
                    JSONArray jarray = new JSONArray(jsonDB);
                    for (int j = 0; j < jarray.length(); j++) {
                        JSONObject curr = jarray.getJSONObject(j);
                        shimmer_name = curr.getString("trial_name");
                        shimmer_mode = curr.getString("trial_duration");
                        mode = curr.getString("mode");
                        domande = curr.getString("n_domande");
                        ShimmerTrial s = new ShimmerTrial(shimmer_name, shimmer_mode, mode, domande);
                        shimmertrial.add(s);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                try {
                    SharedPreferences.Editor editor = pref.edit();
                    Gson gson = new Gson();
                    JsonArray jsonElements = (JsonArray) new Gson().toJsonTree(shimmertrial);
                    Log.i("shimmertrial", "run: " + jsonElements);
                    editor.putString("shimmertrial", String.valueOf(jsonElements));
                    editor.apply();

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
                } catch (JSONException e) {
                    e.printStackTrace();
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

            }


        }
    }
}
