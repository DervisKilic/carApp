package com.example.dervis.autonomous;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;


class CarRest {
    private Bitmap newImage;
    private String currentSpeed;
    private double speed;
    private double turn;
    private boolean lock;
    private Double battery = 0.0;
    private Double lat;
    private Double lng;

    String getSpeed(){
        return currentSpeed;
    }

    Double getBattery() {
        return battery;
    }

    Double[] getLoacation() {
        return new Double[]{lat, lng};
    }

    Bitmap getImage(){
        return newImage;
    }

    void setDataSteer(double speed, double turn) {
        this.speed = speed;
        this.turn = turn;
    }

    void setDataLock(Boolean lock) {
        this.lock = lock;

    }

    Runnable postServiceSteer = new Runnable() {
        private HttpURLConnection connectionPost;
        @Override
        public void run() {
            String url = "http://192.168.150.155:5000/steer";
            String urlParameters = "speed=" + speed + "&turn=" + turn;
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
                if (connectionPost.getInputStream() != null) {
                    InputStream input = connectionPost.getInputStream();
                    StringBuilder totalLines = new StringBuilder(input.available());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    String singleLine;
                    while ((singleLine = reader.readLine()) != null) {
                        totalLines.append(singleLine);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    Runnable postServiceLock = new Runnable() {
        private HttpURLConnection connectionPost;

        @Override
        public void run() {
            String url = "http://192.168.150.155:5000/lock";
            String urlParameters = "&lock=" + lock;
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
                if (connectionPost.getInputStream() != null) {
                    InputStream input = connectionPost.getInputStream();
                    StringBuilder totalLines = new StringBuilder(input.available());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    String singleLine;
                    while ((singleLine = reader.readLine()) != null) {
                        totalLines.append(singleLine);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    Runnable getServiceImage = new Runnable() {
        private HttpURLConnection connection;
        @Override
        public void run() {
            try {
                URL carUrl = new URL("http://192.168.150.155:5000/image");
                connection = (HttpURLConnection) carUrl.openConnection();
                connection.setRequestMethod("GET");

                InputStream input = connection.getInputStream();
                newImage = BitmapFactory.decodeStream(input);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    Runnable getServiceSpeed = new Runnable() {
        private HttpURLConnection connection;

        @Override
        public void run() {
            try {
                URL carUrl = new URL("http://192.168.150.155:5000/speed");
                connection = (HttpURLConnection) carUrl.openConnection();
                connection.setAllowUserInteraction(false);
                connection.setInstanceFollowRedirects(true);
                connection.setRequestMethod("GET");

                InputStream input = connection.getInputStream();
                StringBuilder totalLines = new StringBuilder(input.available());
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                String singleLine;
                while ((singleLine = reader.readLine()) != null) {
                    totalLines.append(singleLine);
                }
                currentSpeed = totalLines.toString();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    Runnable getServiceLocation = new Runnable() {
        private HttpURLConnection connection;

        @Override
        public void run() {
            try {
                URL carUrl = new URL("http://192.168.150.155:5000/location");
                connection = (HttpURLConnection) carUrl.openConnection();
                connection.setAllowUserInteraction(false);
                connection.setInstanceFollowRedirects(true);
                connection.setRequestMethod("GET");

                InputStream input = connection.getInputStream();
                StringBuilder totalLines = new StringBuilder(input.available());
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                String singleLine;
                while ((singleLine = reader.readLine()) != null) {
                    totalLines.append(singleLine);
                }
                JSONArray jsonArray = new JSONArray(totalLines.toString());
                JSONObject json = null;
                for (int i = 0; i < jsonArray.length(); i++) {
                    json = jsonArray.getJSONObject(i);
                    Iterator<String> keys = json.keys();
                }
                if (json != null) {
                    lng = (Double) json.get("lng");
                    lat = (Double) json.get("lat");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    Runnable getServiceBattery = new Runnable() {
        private HttpURLConnection connection;

        @Override
        public void run() {
            try {
                URL carUrl = new URL("http://192.168.150.155:5000/battery");
                connection = (HttpURLConnection) carUrl.openConnection();
                connection.setAllowUserInteraction(false);
                connection.setInstanceFollowRedirects(true);
                connection.setRequestMethod("GET");

                InputStream input = connection.getInputStream();
                StringBuilder totalLines = new StringBuilder(input.available());
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                String singleLine;
                while ((singleLine = reader.readLine()) != null) {
                    totalLines.append(singleLine);
                }
                battery = Double.parseDouble(totalLines.toString());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
}