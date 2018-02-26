package com.example.dervis.autonomous;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import static java.lang.System.in;
import static java.lang.System.out;


class CarRest {
    private Bitmap newImage;
    private String currentSpeed;
    private String param;
    private double speed;
    private double turn;
    private boolean lock;
    private String battery;
    private Double lat;
    private Double lng;

    String getSpeed(){
        return currentSpeed;
    }

    String getBattery(){
        return battery;
    }

    Double[] getLoacation() {
        return new Double[]{lat, lng};
    }

    Bitmap getImage(){
        return newImage;
    }

    private void setImage(Bitmap image) {
        newImage = image;
    }

    void carDataService(final String param) {
        this.param = param;
    }

    void setData(double speed, double turn, boolean lock) {
        this.speed = speed;
        this.turn = turn;
        this.lock = lock;

    }

    Runnable postService = new Runnable() {
        private HttpURLConnection connectionPost;
        @Override
        public void run() {
            String url = "http://192.168.150.155:5000" + param;
            String urlParameters = "speed=" + speed + "&turn=" + turn + "&lock=" + lock;
            Log.i("speed", "" + speed);
            Log.i("turn", "" + turn);
            byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);

            try {
                URL myurl = new URL(url);
                connectionPost = (HttpURLConnection) myurl.openConnection();
                connectionPost.setDoOutput(true);
                connectionPost.setRequestMethod("POST");
                connectionPost.setRequestProperty("User-Agent", "Java client");
                connectionPost.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                try (DataOutputStream wr = new DataOutputStream(connectionPost.getOutputStream())) {
                    wr.write(postData);
                }
                InputStream is;
                if (connectionPost.getInputStream() != null) {
                    is = connectionPost.getInputStream();
                    readData(is);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                connectionPost.disconnect();
            }
        }
    };

    Runnable getService = new Runnable() {
        private HttpURLConnection connection;
        @Override
        public void run() {
            try {
                URL carUrl = new URL("http://192.168.150.155:5000" + param);
                connection = (HttpURLConnection) carUrl.openConnection();
                // responseCode = connection.getResponseCode();
                connection.setAllowUserInteraction(false);
                connection.setInstanceFollowRedirects(true);
                connection.setRequestMethod("GET");
                connection.connect();
                // Log.i("code", "" + responseCode);


                InputStream input;
                    switch (param) {
                        case "/speed":
                            input = connection.getInputStream();
                            readData(input);
                            connection.disconnect();
                            break;
                        case "/battery":
                            input = connection.getInputStream();
                            readData(input);
                            connection.disconnect();
                            break;
                        case "/location":
                            input = connection.getInputStream();
                            readData(input);
                            connection.disconnect();
                            break;
                        case "/image":
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inSampleSize = 1;
                            input = connection.getInputStream();
                            setImage(BitmapFactory.decodeStream(input, null, options));
                            break;

                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private void readData(InputStream iStream) throws Exception {
        StringBuilder totalLines = new StringBuilder(iStream.available());
        BufferedReader reader = new BufferedReader(new InputStreamReader(iStream));
        String singleLine;
        while ((singleLine = reader.readLine()) != null) {
            totalLines.append(singleLine);
        }
        switch (param) {
            case "/speed":
                currentSpeed = totalLines.toString();
                iStream.close();
                break;
            case "/battery":
                battery = totalLines.toString();
                iStream.close();
                break;
            case "/location":
                iStream.close();
                JSONArray jsonArray = new JSONArray(totalLines.toString());
                JSONObject json = null;
                for (int i = 0; i < jsonArray.length(); i++) {
                    json = jsonArray.getJSONObject(i);
                    Iterator<String> keys = json.keys();
                }
                assert json != null;
                lng = (Double) json.get("lng");
                lat = (Double) json.get("lat");
                break;
        }
    }
}