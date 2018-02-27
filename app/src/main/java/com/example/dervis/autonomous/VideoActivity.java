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

    /**
     * creates an instance of CarRest
     */
    CarRest car = new CarRest();

    /**
     * creates a Executable thread pool
     */
    ExecutorService pool = Executors.newCachedThreadPool();

    /**
     * repeat handler for REST calls
     */
    Handler restLooper;

    /**
     * display screen for live view
     */
    ImageView carStream;

    /**
     * Bitmap image
     */
    Bitmap newImage;

    /**
     * turn rate double
     */
    public double turnRate;

    /**
     * speed double
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

    Runnable handlerTask = new Runnable()
    {
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
            restLooper.postDelayed(handlerTask, 20);
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