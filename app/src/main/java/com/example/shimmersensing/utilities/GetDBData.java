package com.example.shimmersensing.utilities;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GetDBData extends AsyncTask<String, Void, String> {
    @Override
    protected String doInBackground(String... params) {
        StringBuilder data = new StringBuilder();
        Log.i("Prova", "cachi");
        HttpURLConnection httpURLConnection = null;
        try {

            httpURLConnection = (HttpURLConnection) new URL(params[0]).openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setDoOutput(true);
            try {
                InputStream in = new BufferedInputStream(httpURLConnection.getInputStream());
                InputStreamReader inputStreamReader = new InputStreamReader(in);
//
                int inputStreamData = inputStreamReader.read();
                while (inputStreamData != -1) {
                    char current = (char) inputStreamData;
                    inputStreamData = inputStreamReader.read();
                    data.append(current);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            Log.i("mannag", "doInBackground: " + data.toString());
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