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
    private final Double MAXBATTERY = 16800.0;
    int maxWidthBattery;
    int currentWidthBattery;
    Double currentBattery;
    TextView currentSpeed;
    ImageView batteryStatus;
    ImageView lock;
    Boolean locked;
    CarRest car = new CarRest();
    ExecutorService pool = Executors.newCachedThreadPool();
    Handler handler;
    Handler handler2;
    Animation animAlpha;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        animAlpha = AnimationUtils.loadAnimation(this, R.anim.alpha);
        batteryStatus = findViewById(R.id.batteryStatus);
        maxWidthBattery = batteryStatus.getLayoutParams().width;
        currentWidthBattery = maxWidthBattery;
        currentSpeed = findViewById(R.id.currentSpeedText);
        batteryStatus = findViewById(R.id.batteryStatus);
        lock = findViewById(R.id.lockedImg);
        locked = true;
        handler = new Handler();
        handler2 = new Handler();
    }


    Runnable handlerTask = new Runnable()
    {
        @Override
        public void run() {
            pool.execute(car.getService);
            car.carDataService("/speed");
            currentSpeed.setText(car.getSpeed());
            handler.postDelayed(handlerTask, 1000);
        }
    };

    Runnable handlerTask2 = new Runnable() {
        @Override
        public void run() {
            getBatteryStatus();
            handler2.postDelayed(handlerTask2, 4500);
        }
    };

    void startRepeatingTask()
    {
        handlerTask.run();
        handlerTask2.run();
    }

    void stopRepeatingTask()
    {
        handler.removeCallbacks(handlerTask);
        handler2.removeCallbacks(handlerTask2);

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
        locked = true;
    }

    public void getBatteryStatus() {
        pool.execute(car.getService);
        car.carDataService("/battery");
        currentBattery = car.getBattery();
        currentBattery = currentBattery / MAXBATTERY;
        currentWidthBattery = (int) (maxWidthBattery * currentBattery);
        batteryStatus.getLayoutParams().width = currentWidthBattery;

        if (currentBattery < 0.3 && currentBattery > 0.2) {
            batteryStatus.setBackgroundColor((Color.parseColor("#fffb1e")));
        } else if (currentBattery < 0.2) {
            batteryStatus.setBackgroundColor((Color.parseColor("#ed3636")));

        } else {
            batteryStatus.setBackgroundColor((Color.parseColor("#90CC42")));
        }

    }
}