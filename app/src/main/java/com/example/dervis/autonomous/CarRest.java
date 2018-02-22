package com.example.dervis.autonomous;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static java.lang.System.out;


class CarRest {
    private Bitmap newImage;
    private String currentSpeed;
    private String param;
    private double speed;
    private double turn;
    private boolean lock;
    private String battery;

    String getSpeed(){
        return currentSpeed;
    }

    String getBattery(){
        return battery;
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
                    wr.close();
                }
                InputStream is = connectionPost.getInputStream();
                connectionPost.connect();
                readData(is);

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
                InputStream is = connection.getInputStream();
                connection.setRequestMethod("GET");
                // responseCode = connection.getResponseCode();
                connection.connect();
                // Log.i("code", "" + responseCode);
                if (is != null) {
                    switch (param) {
                        case "/speed":
                            readData(is);
                            break;
                        case "/battery":
                            readData(is);
                            break;
                        case "/image":
                            readImage(is);
                            break;
                    }
                } else {
                    connection.disconnect();
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
        if(param.equals("/speed")) {
            currentSpeed = totalLines.toString();
        }else if(param.equals("/battery")){
            battery = totalLines.toString();
        }
    }
    private void readImage(InputStream iStream) throws Exception{
        Bitmap bitmapOne = BitmapFactory.decodeStream(iStream);
        System.gc();
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmapOne, 200, 200, true);
        setImage(scaledBitmap);
    }
}