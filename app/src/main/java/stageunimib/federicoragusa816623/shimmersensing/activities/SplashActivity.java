package stageunimib.federicoragusa816623.shimmersensing.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shimmersensing.R;

import stageunimib.federicoragusa816623.shimmersensing.global.GlobalValues;
import stageunimib.federicoragusa816623.shimmersensing.interfaccia.Shimmer_interface;
import stageunimib.federicoragusa816623.shimmersensing.utilities.QuestionTrial;
import stageunimib.federicoragusa816623.shimmersensing.utilities.ShimmerData;
import stageunimib.federicoragusa816623.shimmersensing.utilities.ShimmerTrial;
import stageunimib.federicoragusa816623.shimmersensing.utilities.ShimmerTrialLettura;
import stageunimib.federicoragusa816623.shimmersensing.utilities.ShimmerTrialMusic;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;

public class SplashActivity extends AppCompatActivity implements Shimmer_interface {

    private ArrayList<ShimmerTrial> shimmertrial;
    private SharedPreferences pref;
    private GlobalValues globalValues;
    private AlertDialog.Builder alertDialogBuilder;
    private AlertDialog alertDialog;
    private boolean server_timeout = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("creating mate", "onCreate: imcreating u bitch");
        setContentView(R.layout.activity_splash);
        globalValues = (GlobalValues) getApplicationContext();
        ImageView shimm_logo = findViewById(R.id.shimmerlogo);
        shimm_logo.setAnimation(AnimationUtils.loadAnimation(this, R.anim.zoom_in));

        pref = getApplicationContext().getSharedPreferences("ShimmerSensingSamplingConfig", 0);

        try {
            if (!DEBUG_SHIMMER)
                new GetDBData().execute(URL_SERVER + "trialdetails");
            else
                new GetDBData().execute(URL_SERVER + "get");
        } catch (Exception e) {
            Log.i("pippobaudoerror", "onCreate: ");
        }

        alertDialogBuilder = new AlertDialog.Builder(SplashActivity.this);

        // set title
        alertDialogBuilder.setTitle("Server timeout");

        // set dialog message
        alertDialogBuilder
                .setMessage("Impossibile connettersi al server")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finishAffinity();
                    }
                });

        // create alert dialog
        alertDialog = alertDialogBuilder.create();

    }

    public void server_timeout() {
        MaterialAlertDialogBuilder dialogBuilderMaterial = new MaterialAlertDialogBuilder(SplashActivity.this,R.style.DialogServerTheme_MaterialComponents_MaterialAlertDialog)
                .setTitle("Server timeout")
                .setMessage("Impossibile connettersi al server")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finishAffinity();
                    }
                });
        androidx.appcompat.app.AlertDialog dialog = dialogBuilderMaterial.create();


        Objects.requireNonNull(dialog.getWindow()).getAttributes().windowAnimations = R.style.popup_alert;
        dialog.show();
        dialog.setCancelable(false);
        dialog.getButton(Dialog.BUTTON_POSITIVE).setTextSize(16);

        TextView textView = dialog.findViewById(android.R.id.message);
        if (textView != null) {
            textView.setTextSize(16);
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
                } catch (IOException e) {


                }

                Log.i("shimmer", "doInBackground: " + data.toString());
            } catch (IOException e) {

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
            if (jsonDB.contains("trial") | !DEBUG_SHIMMER) {

                shimmertrial = new ArrayList<>();
                String shimmer_name;
                String shimmer_mode;
                String mode;
                String url_icon;
                String duration_trial;
                String description;
                JSONArray domande;
                try {
                    JSONObject jObj = new JSONObject(jsonDB);
                    globalValues.setName(jObj.getString("name"));
                    globalValues.setSurname(jObj.getString("surname"));
                    globalValues.setDate(jObj.getString("date"));
                    jObj.get("trial");
                    JSONArray jarray = jObj.getJSONArray("trial");
                    for (int j = 0; j < jarray.length(); j++) {
                        ArrayList<QuestionTrial> aQuest = new ArrayList<>();
                        JSONObject curr = jarray.getJSONObject(j);
                        shimmer_name = curr.getString("trial_name");
                        shimmer_mode = curr.getString("mode");
                        url_icon = curr.getString("url_img_cat");
                        description = curr.getString("categoria_descr");
                        switch (shimmer_mode) {
                            case "Musica" :  {
                                String shimmer_audio = curr.getString("mod_file_url");
                                duration_trial = curr.getString("trial_duration");
                                domande = curr.getJSONArray("questionario_trial");
                                for (int k = 0; k < domande.length(); k++) {
                                    JSONObject question = domande.getJSONObject(k);
                                    aQuest.add(new QuestionTrial(question.getString("domanda_questionario"), question.getInt("range_domanda")));
                                }
                                ShimmerTrialMusic s = new ShimmerTrialMusic(shimmer_name, duration_trial, shimmer_mode, url_icon, aQuest, description, shimmer_audio);
                                shimmertrial.add(s);
                                break;
                            }
                            case "Lettura" :  {
                                String shimmer_text = curr.getString("mod_file_url");
                                duration_trial = curr.getString("trial_duration");
                                domande = curr.getJSONArray("questionario_trial");
                                for (int k = 0; k < domande.length(); k++) {
                                    JSONObject question = domande.getJSONObject(k);
                                    aQuest.add(new QuestionTrial(question.getString("domanda_questionario"), question.getInt("range_domanda")));
                                }
                                ShimmerTrialLettura s = new ShimmerTrialLettura(shimmer_name, duration_trial, shimmer_mode, url_icon, aQuest, description, shimmer_text);
                                shimmertrial.add(s);
                                break;
                            }
                            case "Prompt" :  {
                                duration_trial = "0";
                                domande = curr.getJSONArray("questionario_trial");
                                for (int k = 0; k < domande.length(); k++) {
                                    JSONObject question = domande.getJSONObject(k);
                                    aQuest.add(new QuestionTrial(question.getString("domanda_questionario"), question.getInt("range_domanda")));
                                }
                                ShimmerTrial s = new ShimmerTrial(shimmer_name, duration_trial, shimmer_mode, url_icon, aQuest, description);
                                shimmertrial.add(s);
                                break;
                            }
                            case "Countdown" :  {
                                duration_trial = curr.getString("trial_duration");
                                domande = curr.getJSONArray("questionario_trial");
                                for (int k = 0; k < domande.length(); k++) {
                                    JSONObject question = domande.getJSONObject(k);
                                    aQuest.add(new QuestionTrial(question.getString("domanda_questionario"), question.getInt("range_domanda")));
                                }
                                ShimmerTrial s = new ShimmerTrial(shimmer_name, duration_trial, shimmer_mode, url_icon, aQuest, description);
                                shimmertrial.add(s);
                                break;
                            }
                            default: {
                                break;
                            }
                        }
//                        if (shimmer_mode.equals("Musica")) {
//                            String shimmer_audio = curr.getString("mod_file_url");
//                            duration_trial = curr.getString("trial_duration");
//                            domande = curr.getJSONArray("questionario_trial");
//                            for (int k = 0; k < domande.length(); k++) {
//                                JSONObject question = domande.getJSONObject(k);
//                                aQuest.add(new QuestionTrial(question.getString("domanda_questionario"), question.getInt("range_domanda")));
//                            }
//                            ShimmerTrialMusic s = new ShimmerTrialMusic(shimmer_name, duration_trial, shimmer_mode, url_icon, aQuest, description, shimmer_audio);
//                            shimmertrial.add(s);
//                        }
//                        else if (shimmer_mode.equals("Lettura")) {
//                            String shimmer_audio = curr.getString("mod_file_url");
//                            duration_trial = curr.getString("trial_duration");
//                            domande = curr.getJSONArray("questionario_trial");
//                            for (int k = 0; k < domande.length(); k++) {
//                                JSONObject question = domande.getJSONObject(k);
//                                aQuest.add(new QuestionTrial(question.getString("domanda_questionario"), question.getInt("range_domanda")));
//                            }
//                            ShimmerTrialMusic s = new ShimmerTrialMusic(shimmer_name, duration_trial, shimmer_mode, url_icon, aQuest, description, shimmer_audio);
//                            shimmertrial.add(s);
//                        }
//                        else {
//                            duration_trial = curr.getString("trial_duration");
//                            domande = curr.getJSONArray("questionario_trial");
//                            for (int k = 0; k < domande.length(); k++) {
//                                JSONObject question = domande.getJSONObject(k);
//                                aQuest.add(new QuestionTrial(question.getString("domanda_questionario"), question.getInt("range_domanda")));
//                            }
//                            ShimmerTrial s = new ShimmerTrial(shimmer_name, duration_trial, shimmer_mode, url_icon, aQuest, description);
//                            shimmertrial.add(s);
//                        }


                    }
                } catch (JSONException e) {
                    Log.i("pippobaudoerrorOnPost", "onCreate: ");
                    server_timeout();
                    server_timeout = true;
                } finally {
                    try {
                        SharedPreferences.Editor editor = pref.edit();
                        Gson gson = new Gson();
                        JsonArray jsonElements = (JsonArray) new Gson().toJsonTree(shimmertrial);
                        Log.i("shimmertrial", "run: " + jsonElements);
                        editor.putString("shimmertrial", String.valueOf(jsonElements));
                        editor.apply();
                        globalValues.setShimmerTrialArrayList(shimmertrial);

                        Log.i("TAG", String.valueOf(shimmertrial.size()));
                        if (!server_timeout)
                            goAhead();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                }


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


                } catch (JSONException e) {
                    Log.i("pippobaudoerrorOnPost", "onCreate: ");
                    server_timeout = true;

                } finally {
                    if (!server_timeout)
                        new GetDBData().execute(URL_SERVER + "trialdetails");
                    else
                        server_timeout();
                }


            }


        }
    }

}
