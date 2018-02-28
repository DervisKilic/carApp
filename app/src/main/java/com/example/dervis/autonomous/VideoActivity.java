package com.example.dervis.autonomous;

import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import io.github.controlwear.virtual.joystick.android.JoystickView;

/**
 * this class handles the camera display settings and steering
 */
public class VideoActivity extends AppCompatActivity {
    CarRest car = new CarRest();
    ExecutorService pool = Executors.newCachedThreadPool();
    Handler imageHandler;
    Handler speedHandler;
    ImageView carStream;
    Bitmap newImage;
    TextView speedText;

    /**
     * turn rate
     */
    public double turnRate;

    /**
     * speed
     */
    public double speed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        JoystickView joystick = findViewById(R.id.joyStick);
        joystick.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                int str = strength * 5;

                turnRate = str / 5 * 3 * Math.cos(Math.toRadians(angle));
                speed = str * Math.sin(Math.toRadians(angle));
                car.setDataSteer(speed, turnRate);
                pool.execute(car.postServiceSteer);
            }
        });

        speedText = findViewById(R.id.speedText);
        carStream = findViewById(R.id.carGoggle);
        imageHandler = new Handler();
        speedHandler = new Handler();
    }

    /**
     * get a new image from server and displays the image every 10 milliseconds
     */
    Runnable imageHandlerTask = new Runnable() {
        @Override
        public void run() {
            pool.execute(car.getServiceImage);
            newImage = car.getImage();
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    carStream.setImageBitmap(newImage);
                }
            });
            imageHandler.postDelayed(imageHandlerTask, 10);
        }
    };

    /**
     * get speed from MainActivity and displays it on this activity every 1000 milliseconds
     */
    Runnable speedHandlerTask = new Runnable() {
        @Override
        public void run() {
            speedText.setText(MainActivity.speed);
            speedHandler.postDelayed(speedHandlerTask, 1000);
        }
    };

    /**
     * starts speedHandlerTask
     */
    void startRepeatingTask() {
        imageHandlerTask.run();
        speedHandlerTask.run();
    }

    /**
     * stops speedHandlerTask
     */
    void stopRepeatingTask() {
        imageHandler.removeCallbacks(imageHandlerTask);
        speedHandler.removeCallbacks(speedHandlerTask);
    }

    /**
     * goes back to previous activity and closes this activity.
     *
     * @param view this view
     */
    public void backOnclick(View view) {
        finish();
        stopRepeatingTask();
        overridePendingTransition(R.anim.enter_back_anim, R.anim.exit_back_anim);
    }

    /**
     * stops everything in this class, calls on stopRepeatingTask.
     */
    protected void onDestroy() {
        super.onDestroy();
        stopRepeatingTask();
    }

    /**
     * called on when entered this activity, calls on startRepeatingTask.
     */
    protected void onStart() {
        super.onStart();
        startRepeatingTask();
    }
}