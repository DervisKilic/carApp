package com.example.dervis.autonomous;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;


public class CarRest {
    private Bitmap newImage;
    private String currentSpeed;
    private String param;
    private double speed;
    private double turn;
    private boolean lock;
    private String battery;
    int responseCode;

    String getSpeed(){
        return currentSpeed;
    }

    String getBattery(){
        return battery;
    }

    Bitmap getImage(){
        return newImage;
    }
    void setImage(Bitmap image){
        newImage = image;
    }

    public void carDataService(final String param) {
        this.param = param;
    }
    public void setData(double speed, double turn, boolean lock){
        this.speed = speed;
        this.turn = turn;
        this.lock = lock;

    }

    Runnable postService = new Runnable() {
        private HttpURLConnection connectionPost;
        @Override
        public void run() {
            String url = "http://192.168.150.155:5000" + param;
            String urlParameters = "turn="+turn+"&speed="+speed+"&lock="+lock;
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
                        is.close();
                        connection.disconnect();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
        }
    };

    private boolean canUseForInBitmap(
            Bitmap candidate, BitmapFactory.Options targetOptions) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int width = targetOptions.outWidth / targetOptions.inSampleSize;
            int height = targetOptions.outHeight / targetOptions.inSampleSize;
            int byteCount = width * height * getBytesPerPixel(candidate.getConfig());

            try {
                return byteCount <= candidate.getAllocationByteCount();
            } catch (NullPointerException e) {
                return byteCount <= candidate.getHeight() * candidate.getRowBytes();
            }
        }
        return candidate.getWidth() == targetOptions.outWidth
                && candidate.getHeight() == targetOptions.outHeight
                && targetOptions.inSampleSize == 1;
    }
    private int getBytesPerPixel(Bitmap.Config config) {
        if (config == null) {
            config = Bitmap.Config.ARGB_8888;
        }

        int bytesPerPixel;
        switch (config) {
            case ALPHA_8:
                bytesPerPixel = 1;
                break;
            case RGB_565:
            case ARGB_4444:
                bytesPerPixel = 2;
                break;
            case ARGB_8888:
            default:
                bytesPerPixel = 4;
                break;
        }
        return bytesPerPixel;
    }
    private void readData(InputStream iStream) throws Exception {
        StringBuilder totalLines = new StringBuilder(iStream.available());
        BufferedReader reader = new BufferedReader(new InputStreamReader(iStream));
        String singleLine = "";
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
        setImage(bitmapOne);
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        if (canUseForInBitmap(bitmapOne, options)) {
            options.inMutable = true;
            options.inBitmap = bitmapOne;
        }
        options.inJustDecodeBounds = false;
        Bitmap bitmapTwo = BitmapFactory.decodeStream(iStream, null, options);
        setImage(bitmapTwo);
    }
}