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
 * the main activity that pops up when starting the application
 */
public class MainActivity extends AppCompatActivity  {

    /**
     * max width of the battery status
     */
    int maxWidthBattery;

    /**
     * current width of the battery status
     */
    int currentWidthBattery;

    /**
     * current battery in percent from the car
     */
    public static Double currentBattery;

    /**
     * current voltage on battery
     */
    private Double currentVoltage;

    /**
     * current speed of the car displayed
     */
    TextView currentSpeed;

    /**
     * battery status image
     */
    ImageView batteryStatus;

    /**
     * lock image
     */
    ImageView lock;

    /**
     * locked true/false
     */
    Boolean locked;

    /**
     * stop image
     */
    ImageView stopImg;

    /**
     * creates an instance of CarRest class
     */
    CarRest car = new CarRest();

    /**
     * creates a Executable thread pool
     */
    ExecutorService pool = Executors.newCachedThreadPool();

    /**
     * repeat handler for speed
     */
    Handler speedHandler;

    /**
     * repeat handler for battery
     */
    Handler batteryHandler;

    /**
     * animation for navigation
     */
    Animation animAlpha;

    /**
     * text for voltage
     */
    TextView voltage;

    /**
     * gets and sets resources
     *
     * @param savedInstanceState .
     */
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
     * start a new thread and calls car.getService.
     * sets the route to /speed for the server.
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
     * opens diagnostics activity
     * animates the transition
     * stops repeating tasks
     * @param view view
     *
     */
    public void diagActivity(View view) {
        startActivity(new Intent(MainActivity.this, DiagnosticsActivity.class));
        overridePendingTransition(R.anim.enter_anim, R.anim.exit_anim);
        stopRepeatingTask();
    }

    /**
     * opens Video activity
     * animates the transition
     * stops repeating tasks
     * @param view view
     *
     */
    public void remoteControlClick(View view) {
        startActivity(new Intent(MainActivity.this, VideoActivity.class));
        overridePendingTransition(R.anim.enter_anim, R.anim.exit_anim);
        stopRepeatingTask();
    }

    /**
     * trigger when changing activity or shutting down activity
     * stops repeating task
     */
    protected void onDestroy() {
        super.onDestroy();
        stopRepeatingTask();
    }

    /**
     * trigger when changing to this activity
     * starts repeating task
     */
    protected void onStart() {
        super.onStart();
        startRepeatingTask();
    }

    /**
     * animates lock icon change.
     * checks if lock is locked, if locked
     * sets the route to /lock for the server.
     * sets data for the server for lock to false
     * start a new thread and calls car.postService.
     * changes lock icon to unlocked.
     * changes locked to false.
     * reverse all if lock is unlocked.
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
     * opens location activity
     * animates the transition
     * stops repeating tasks
     * @param view view
     *
     */
    public void LocationClicked(View view) {
        startActivity(new Intent(MainActivity.this, LocationActivity.class));
        overridePendingTransition(R.anim.enter_anim, R.anim.exit_anim);
        stopRepeatingTask();
    }

    /**
     * animates lock icon change
     * checks if lock is locked, if locked
     * sets the route to /lock for the server.
     * sets data for the server for lock to true
     * start a new thread and calls car.postService.
     * changes lock icon to locked
     * changes locked to true
     * @param view view
     */
    public void emergencyStopClick(View view) {
        stopImg.startAnimation(animAlpha);
        car.setDataLock(true);
        pool.execute(car.postServiceLock);
        lock.setImageResource(R.drawable.locked);
        locked = true;
    }

    /**
     * sets the route to /lock for the server.
     * start a new thread and calls car.getService.
     * set this current battery in percent to current battery from server
     * sets the width of the batter status in based on how much percent left
     * changes the color of battery status if below 30% and to red if bellow 20%
     */
    public void getBatteryStatus() {
        Double maxBattery = 16800.0;
        pool.execute(car.getServiceBattery);
        currentBattery = car.getBattery();
        currentVoltage = car.getBattery() / 1000;
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