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

/**
 * this class shows the main menu for the app.
 * has battery, speed and a navigation menu.
 */
public class MainActivity extends AppCompatActivity  {
    /**
     * the current battery in percent
     */
    public static Double currentBattery;

    int maxWidthBattery;
    int currentWidthBattery;
    TextView currentSpeed;
    ImageView batteryStatus;
    ImageView lock;
    Boolean locked;
    ImageView stopImg;
    CarRest car = new CarRest();
    ExecutorService pool = Executors.newCachedThreadPool();
    Handler speedHandler;
    Handler batteryHandler;
    Animation animAlpha;
    TextView voltage;

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
        voltage = findViewById(R.id.voltageText);
        lock = findViewById(R.id.lockedImg);
        stopImg = findViewById(R.id.stopImg);
        locked = true;
        speedHandler = new Handler();
        batteryHandler = new Handler();
    }

    /**
     * calls on run().
     * start a new thread and calls car.getService.
     * gets the speed from server and sets it to the current speed.
     * repeats this task every 1000 milliseconds.
     */
    Runnable handlerTask = new Runnable() {
        @Override
        public void run() {
            pool.execute(car.getServiceSpeed);
            currentSpeed.setText(car.getSpeed());
            speedHandler.postDelayed(handlerTask, 1000);
        }
    };

    /**
     * calls getBatteryStatus method.
     * repeats this task ever 4500 milliseconds.
     */
    Runnable handlerTask2 = new Runnable() {
        @Override
        public void run() {
            getBatteryStatus();
            batteryHandler.postDelayed(handlerTask2, 4500);
        }
    };

    /**
     * starts handlerTask and handlerTask2
     */
    void startRepeatingTask() {
        handlerTask.run();
        handlerTask2.run();
    }

    /**
     * stops handlerTask and handlerTask2
     */
    void stopRepeatingTask() {
        speedHandler.removeCallbacks(handlerTask);
        batteryHandler.removeCallbacks(handlerTask2);

    }

    /**
     * opens diagnostics activity and calls on stopsRepeatingTask
     * @param view view
     *
     */
    public void diagActivity(View view) {
        startActivity(new Intent(MainActivity.this, DiagnosticsActivity.class));
        overridePendingTransition(R.anim.enter_anim, R.anim.exit_anim);
        stopRepeatingTask();
    }

    /**
     * opens Video activity and cals on stopsRepeatingTask
     * @param view view
     *
     */
    public void remoteControlClick(View view) {
        startActivity(new Intent(MainActivity.this, VideoActivity.class));
        overridePendingTransition(R.anim.enter_anim, R.anim.exit_anim);
        stopRepeatingTask();
    }

    /**
     * stops everything in this class
     */
    protected void onDestroy() {
        super.onDestroy();
        stopRepeatingTask();
    }

    /**
     * called on when changed to this activity, calls on startRepeatingTask
     */
    protected void onStart() {
        super.onStart();
        startRepeatingTask();
    }

    /**
     * changes the lock icon on clicked
     * @param view view
     */
    public void lockClicked(View view) {

        lock.startAnimation(animAlpha);

        if(locked) {
            car.setDataLock(false);
            pool.execute(car.postServiceLock);
            lock.setImageResource(R.drawable.unlocked);
            locked = false;
        } else {
            car.setDataLock(true);
            pool.execute(car.postServiceLock);
            lock.setImageResource(R.drawable.locked);
            locked = true;
        }
    }

    /**
     * opens location activity and calls on stopRepeatingTask
     * @param view view
     */
    public void LocationClicked(View view) {
        startActivity(new Intent(MainActivity.this, LocationActivity.class));
        overridePendingTransition(R.anim.enter_anim, R.anim.exit_anim);
        stopRepeatingTask();
    }

    /**
     * stops the "engine" to the car, and changes the lock icon
     */
    public void emergencyStopClick(View view) {
        stopImg.startAnimation(animAlpha);
        car.setDataLock(true);
        pool.execute(car.postServiceLock);
        lock.setImageResource(R.drawable.locked);
        locked = true;
    }

    /**
     * changes battery icon based on how much voltage is left
     */
    public void getBatteryStatus() {
        Double maxBattery = 16800.0;
        Double currentVoltage = car.getBattery() / 1000;
        pool.execute(car.getServiceBattery);
        currentBattery = car.getBattery();
        currentBattery = currentBattery / maxBattery;
        currentWidthBattery = (int) (maxWidthBattery * currentBattery);
        batteryStatus.getLayoutParams().width = currentWidthBattery;
        voltage.setText(currentVoltage + "V");

        if (currentBattery < 0.3 && currentBattery > 0.2) {
            batteryStatus.setBackgroundColor((Color.parseColor("#fffb1e")));
        } else if (currentBattery < 0.2) {
            batteryStatus.setBackgroundColor((Color.parseColor("#ed3636")));

        } else {
            batteryStatus.setBackgroundColor((Color.parseColor("#90CC42")));
        }
    }
}