package com.example.dervis.autonomous;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;


/**
 * this class handles http calls to server
 */
public class CarRest {
    private Bitmap image;
    public static String ip;
    private String currentSpeed;
    private double speed;
    private String currentOdometer;
    private double turn;
    private boolean lock;
    private boolean lights;
    private Double battery = 0.0;
    private Double lat;
    private Double lng;
    public static int responseCode;

    /**
     * gets the current speed of the car
     * @return this currentSpeed
     */
    String getSpeed(){
        return currentSpeed;
    }

    /**
     * gets the current voltage of the car
     * @return this battery voltage
     */
    Double getBattery() {
        return battery;
    }

    /**
     * gets the location of the car
     * @return array with lat and lng
     */
    Double[] getLoacation() {
        return new Double[]{lat, lng};
    }

    /**
     * gets the image from car
     * @return this image
     */
    Bitmap getImage() {
        return image;
    }

    /**
     * gets the current odometer of the car
     * @return this odometer
     */
    String getOdometer() {
        return currentOdometer;
    }

    /**
     * sets the speed and turn rate for the car
     * @param speed speed of the car
     * @param turn turn rate of the car
     */
    void setDataSteer(double speed, double turn) {
        this.speed = speed;
        this.turn = turn;
    }

    /**
     * sets the lock to false or true
     * @param lock lock status
     */
    void setDataLock(Boolean lock) {
        this.lock = lock;
    }
    void setIp(String ip) {
        this.ip = ip;
    }
    String getIp() {
        return ip;
    }

    /**
     * sets the lock to false or true
     * @param lights lock status
     */
    void setDataLights(Boolean lights) {
        this.lights = lights;
    }

    /**
     * connects to server and sets data for steering
     */
    Runnable postServiceSteer = new Runnable() {
        private HttpURLConnection connectionPost;

        @Override
        public void run() {
            String url = "http://"+ip+":5000/steer";
            String urlParameters = "speed=" + speed + "&turn=" + turn;
            byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);

            try {
                URL localUrl = new URL(url);
                connectionPost = (HttpURLConnection) localUrl.openConnection();
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

    /**
     * connects to server and sets data for lock
     */
    Runnable postServiceLock = new Runnable() {
        private HttpURLConnection connectionPost;

        @Override
        public void run() {
            String url = "http://"+ip+":5000/lock";
            String urlParameters = "&lock=" + lock;
            byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);

            try {
                URL localUrl = new URL(url);
                connectionPost = (HttpURLConnection) localUrl.openConnection();
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

    /**
     * connects to server and sets data for lights
     */
    Runnable postServiceLights = new Runnable() {
        private HttpURLConnection connectionPost;

        @Override
        public void run() {
            String url = "http://"+ip+":5000/lights";
            String urlParameters = "&lights=" + lights;
            byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);

            try {
                URL localUrl = new URL(url);
                connectionPost = (HttpURLConnection) localUrl.openConnection();
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

    /**
     * connects to server and gets data for image and sets variable to frontend to get
     */
    Runnable getServiceImage = new Runnable() {
        private HttpURLConnection connection;

        @Override
        public void run() {
            try {
                URL localUrl = new URL("http://"+ip+":5000/image");
                connection = (HttpURLConnection) localUrl.openConnection();
                connection.setRequestMethod("GET");

                InputStream input = connection.getInputStream();
                image = BitmapFactory.decodeStream(input);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * connects to server and gets data for speed and sets variable to frontend to get
     */
    Runnable getServiceSpeed = new Runnable() {
        private HttpURLConnection connection;
        @Override
        public void run() {
            try {
                responseCode = 0;
                URL localUrl = new URL("http://"+ip+":5000/speed");
                connection = (HttpURLConnection) localUrl.openConnection();
                connection.setAllowUserInteraction(false);
                connection.setInstanceFollowRedirects(true);
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(1000);
                responseCode = connection.getResponseCode();

                InputStream input = connection.getInputStream();
                StringBuilder totalLines = new StringBuilder(input.available());
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                String singleLine;
                while ((singleLine = reader.readLine()) != null) {
                    totalLines.append(singleLine);
                }
                currentSpeed = totalLines.toString();


            }  catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * connects to server and gets data for location and sets variables to frontend to get
     */
    Runnable getServiceLocation = new Runnable() {
        private HttpURLConnection connection;

        @Override
        public void run() {
            try {
                URL localUrl = new URL("http://"+ip+":5000/location");
                connection = (HttpURLConnection) localUrl.openConnection();
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

    /**
     * connects to server and gets data for battery and sets variable to frontend to get
     */
    Runnable getServiceBattery = new Runnable() {
        private HttpURLConnection connection;

        @Override
        public void run() {
            try {
                URL localUrl = new URL("http://"+ip+":5000/battery");
                connection = (HttpURLConnection) localUrl.openConnection();
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

    /**
     * connects to server and gets data for odometer and sets variable to frontend to get
     */
    Runnable getServiceOdometer = new Runnable() {
        private HttpURLConnection connection;

        @Override
        public void run() {
            try {
                URL localUrl = new URL("http://"+ip+":5000/odometer");
                connection = (HttpURLConnection) localUrl.openConnection();
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
                currentOdometer = totalLines.toString();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
}