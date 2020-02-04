package com.example.shimmersensing.utilities;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

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
            httpURLConnection.connect();
            // getting file length
            int lenghtOfFile = httpURLConnection.getContentLength();
            // input stream to read file - with 8k buffer
            InputStream input = new BufferedInputStream(httpURLConnection.getInputStream(),
                    8192);
//            File download = new File(Environment.getExternalStorageDirectory()
//                    + "/download/");
//            if (!download.exists()) {
//                download.mkdir();
//            }
//            String strDownloaDuRL = download + "/" + "test";
//            Log.v("log_tag", " down url   " + strDownloaDuRL + " lenght: " + lenghtOfFile);
//            FileOutputStream output = new FileOutputStream(strDownloaDuRL);
//
//            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            int next = input.read();
            while (next > -1) {
                bos.write(next);
                next = input.read();
            }

            bos.flush();
            byte data_byte[] = bos.toByteArray();
            bos.close();

            Log.v("log_tag", "byte array " + data_byte.length);

            String saveThis = Base64.encodeToString(data_byte, Base64.DEFAULT);

            long total = 0;


//            // flushing output
//            output.flush();
//
//            // closing streams
//            output.close();
            input.close();


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