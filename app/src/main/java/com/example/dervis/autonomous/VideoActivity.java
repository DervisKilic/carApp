package com.example.dervis.autonomous;

import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.github.controlwear.virtual.joystick.android.JoystickView;

/**
 * this class shows handles the camera display settings and steering
 */
public class VideoActivity extends AppCompatActivity {
    CarRest car = new CarRest();
    ExecutorService pool = Executors.newCachedThreadPool();
    Handler restLooper;
    ImageView carStream;
    Bitmap newImage;

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

        carStream = findViewById(R.id.carGoggle);
        restLooper = new Handler();
    }

    /**
     * displays a new image every 20 millisecond
     */
    Runnable handlerTask = new Runnable() {
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
            restLooper.postDelayed(handlerTask, 10);
        }
    };

    /**
     * starts speedHandlerTask
     */
    void startRepeatingTask() {
        handlerTask.run();
    }

    /**
     * stops speedHandlerTask
     */
    void stopRepeatingTask() {
        restLooper.removeCallbacks(handlerTask);
    }

    /**
     * goes back to previous activity.
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
     * called on when changed to this activity, calls on startRepeatingTask.
     */
    protected void onStart() {
        super.onStart();
        startRepeatingTask();
    }
}