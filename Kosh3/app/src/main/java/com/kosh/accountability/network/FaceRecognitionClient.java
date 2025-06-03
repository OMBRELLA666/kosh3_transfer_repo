package com.kosh.accountability.network;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Scanner;

public class FaceRecognitionClient {

    public interface Callback {
        void onSuccess(boolean match);
        void onError(String error);
    }

    public static void sendImages(List<String> base64Images, String mode, Callback callback) {
        new AsyncTask<Void, Void, Boolean>() {
            String errorMessage = null;

            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    URL url = new URL("http://10.0.2.2:5000/" + mode); // 10.0.2.2 = Android emulator to host
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setDoOutput(true);

                    JSONObject body = new JSONObject();
                    JSONArray images = new JSONArray(base64Images);
                    body.put("images", images);

                    OutputStream os = conn.getOutputStream();
                    os.write(body.toString().getBytes());
                    os.flush();
                    os.close();

                    int responseCode = conn.getResponseCode();
                    if (responseCode == 200) {
                        Scanner scanner = new Scanner(conn.getInputStream()).useDelimiter("\\A");
                        String result = scanner.hasNext() ? scanner.next() : "";
                        JSONObject json = new JSONObject(result);
                        return json.optBoolean("match", false); // for validate, default false
                    } else {
                        Scanner scanner = new Scanner(conn.getErrorStream()).useDelimiter("\\A");
                        errorMessage = scanner.hasNext() ? scanner.next() : "Error: " + responseCode;
                        return false;
                    }

                } catch (Exception e) {
                    errorMessage = e.toString();
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (errorMessage != null) {
                    callback.onError(errorMessage);
                } else {
                    callback.onSuccess(result);
                }
            }
        }.execute();
    }
}
