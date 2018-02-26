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

public class VideoActivity extends AppCompatActivity {
    private final static int INTERVAL = 20;
    CarRest car = new CarRest();
    ExecutorService pool = Executors.newCachedThreadPool();
    Handler restLooper;
    ImageView carStream;
    Bitmap newImage;
    public double turnRate;
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

                car.setData(speed, turnRate, false);
                car.carDataService("/steer");
                pool.execute(car.postService);

            }
        });

        carStream = findViewById(R.id.carGoggle);
        restLooper = new Handler();
    }

    Runnable handlerTask = new Runnable()
    {
        @Override
        public void run() {
            car.carDataService("/image");
            pool.execute(car.getService);
            newImage = car.getImage();
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    carStream.setImageBitmap(newImage);
                }
            });
            restLooper.postDelayed(handlerTask, INTERVAL);
        }
    };

    void startRepeatingTask()
    {
        handlerTask.run();
    }

    void stopRepeatingTask()
    {
        restLooper.removeCallbacks(handlerTask);
    }

    public void backOnclick(View view) {
        finish();
        stopRepeatingTask();
        overridePendingTransition(R.anim.enter_back_anim, R.anim.exit_back_anim);
    }

    protected void onDestroy() {
        super.onDestroy();
        stopRepeatingTask();
    }
    protected void onStart() {
        super.onStart();
        startRepeatingTask();
    }
}