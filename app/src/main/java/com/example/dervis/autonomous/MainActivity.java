package com.example.dervis.autonomous;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MainActivity extends AppCompatActivity  {
    private final static int INTERVAL = 1000;
    TextView currentSpeed;
    ImageView batteryStatus;
    ImageView lock;
    Boolean locked;
    CarRest car = new CarRest();
    ExecutorService pool = Executors.newCachedThreadPool();
    Handler handler;
    Animation animAlpha;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        animAlpha = AnimationUtils.loadAnimation(this, R.anim.alpha);

        lock = findViewById(R.id.lockedImg);
        locked = true;
        handler = new Handler();
    }


    Runnable handlerTask = new Runnable()
    {
        @Override
        public void run() {
            car.carDataService("/speed");
            pool.execute(car.getService);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    currentSpeed.setText(car.getSpeed());
                }
            });
            handler.postDelayed(handlerTask, INTERVAL);
        }
    };

    void startRepeatingTask()
    {
        handlerTask.run();
    }

    void stopRepeatingTask()
    {
        handler.removeCallbacks(handlerTask);
    }

    public void diagActivity(View view) {
        startActivity(new Intent(MainActivity.this, DiagnosticsActivity.class));
        overridePendingTransition(R.anim.enter_anim, R.anim.exit_anim);
        stopRepeatingTask();
    }

    public void remoteControlClick(View view) {
        startActivity(new Intent(MainActivity.this, VideoActivity.class));
        overridePendingTransition(R.anim.enter_anim, R.anim.exit_anim);
        stopRepeatingTask();
    }

    protected void onDestroy() {
        super.onDestroy();
        stopRepeatingTask();
    }

    protected void onStart() {
        super.onStart();
        currentSpeed = findViewById(R.id.currentSpeedText);
        batteryStatus = findViewById(R.id.batteryStatus);
        startRepeatingTask();
    }

    public void lockClicked(View view) {

        lock.startAnimation(animAlpha);

        if(locked){
            car.carDataService("/lock");
            car.setData(0, 0, false);
            pool.execute(car.postService);
            lock.setImageResource(R.drawable.unlocked);
            locked = false;
        }else{
            car.carDataService("/lock");
            car.setData(0, 0, true);
            pool.execute(car.postService);
            lock.setImageResource(R.drawable.locked);
            locked = true;
        }
    }

    public void LocationClicked(View view) {
        startActivity(new Intent(MainActivity.this, LocationActivity.class));
        overridePendingTransition(R.anim.enter_anim, R.anim.exit_anim);
        stopRepeatingTask();
    }

    public void emergencyStopClick(View view) {
        ImageView stopImg = findViewById(R.id.stopImg);
        stopImg.startAnimation(animAlpha);
        car.carDataService("/motorStop");
        car.setData(0, 0, true);
        pool.execute(car.postService);
        lock.setImageResource(R.drawable.locked);
        int currentBattery = (int) (batteryStatus.getLayoutParams().width*0.2);
        batteryStatus.getLayoutParams().width = currentBattery;
        if(batteryStatus.getLayoutParams().width < currentBattery) {
            batteryStatus.setColorFilter(Color.parseColor("#f25037"));
        }
        locked = true;
    }
}