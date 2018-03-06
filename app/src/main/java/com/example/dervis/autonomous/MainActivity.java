package com.example.dervis.autonomous;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
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

    /**
     * the current odometer in meters
     */
    public static String currentOdometer;

    /**
     * the current speed in meters per second
     */
    public static String speed;

    /**
     * connection open or not checker
     */
    public static Boolean openConnection = false;


    int maxWidthBattery;
    int currentWidthBattery;
    TextView currentSpeed;
    ImageView batteryStatus;
    ImageView lockImg;
    Boolean locked;
    Boolean lightsOn = false;
    ImageView stopImg;
    ImageView lightsImg;
    Button connectButton;
    CarRest car;
    ExecutorService pool;
    Handler speedHandler;
    Handler batteryHandler;
    Handler odometerHandler;
    Animation animAlpha;
    TextView voltage;
    String ip;
    EditText ipNr;
    View connectionView;

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
        voltage = findViewById(R.id.voltageText);
        lockImg = findViewById(R.id.lockedImg);
        stopImg = findViewById(R.id.stopImg);
        lightsImg = findViewById(R.id.lightsImg);
        connectButton = findViewById(R.id.connectButton);
        locked = true;
        car = new CarRest();
        pool = Executors.newSingleThreadExecutor();
        speedHandler = new Handler();
        batteryHandler = new Handler();
        odometerHandler = new Handler();
    }

    /**
     * start a new thread and calls car.getServiceSpeed.
     * gets the speed from server and sets it to the current speed.
     * repeats this task every 1000 milliseconds.
     */
    Runnable speedHandlerTask = new Runnable() {
        @Override
        public void run() {
            pool.execute(car.getServiceSpeed);
            speed = (car.getSpeed());
            currentSpeed.setText(speed);
            speedHandler.postDelayed(speedHandlerTask, 1000);
        }
    };

    /**
     * calls getBatteryStatus method.
     * repeats this task ever 4500 milliseconds.
     */
    Runnable batteryHandlerTask = new Runnable() {
        @Override
        public void run() {
            getBatteryStatus();
            batteryHandler.postDelayed(batteryHandlerTask, 15000);
        }
    };

    /**
     * start a new thread and calls car.getServiceOdometer.
     * gets the odometer from server and sets it to the current odometer.
     * repeats this task every 1000 milliseconds.
     */
    Runnable odometerHandlerTask = new Runnable() {
        @Override
        public void run() {
            pool.execute(car.getServiceOdometer);
            currentOdometer = car.getOdometer();
            odometerHandler.postDelayed(odometerHandlerTask, 15400);
        }
    };

    /**
     * starts speedHandlerTask, odometerHandlerTask and batteryHandlerTask
     */
    void startRepeatingTask() {
        speedHandlerTask.run();
        batteryHandlerTask.run();
        odometerHandlerTask.run();
    }

    /**
     * stops batteryHandlerTask, odometerHandlerTask and - speedHandlerTask if Boolean is true
     */
    void stopRepeatingTask() {
        speedHandler.removeCallbacks(speedHandlerTask);
        batteryHandler.removeCallbacks(batteryHandlerTask);
        odometerHandler.removeCallbacks(odometerHandlerTask);
    }

    /**
     * opens diagnostics activity and calls on stopsRepeatingTask
     * @param view this view
     *
     */
    public void diagActivity(View view) {
        startActivity(new Intent(MainActivity.this, DiagnosticsActivity.class));
        overridePendingTransition(R.anim.enter_anim, R.anim.exit_anim);
        stopRepeatingTask();
    }

    /**
     * opens Video activity and calls on stopsRepeatingTask
     * @param view this view
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
     * called on when entered this activity, calls on startRepeatingTask
     */
    protected void onStart() {
        super.onStart();
        if(openConnection) {
            startRepeatingTask();
        }
    }

    protected void onPause(){
        stopRepeatingTask();
        super.onPause();
    }

    /**
     * changes the lock icon on clicked and calls server to change status of lock
     * @param view view
     */
    public void lockClicked(View view) {

        lockImg.startAnimation(animAlpha);

        if(locked) {
            car.setDataLock(false);
            pool.execute(car.postServiceLock);
            lockImg.setImageResource(R.drawable.unlocked);
            locked = false;
        } else {
            car.setDataLock(true);
            pool.execute(car.postServiceLock);
            lockImg.setImageResource(R.drawable.locked);
            locked = true;
        }
    }

    /**
     * opens location activity and calls on stopRepeatingTask
     * @param view view
     */
    public void locationClicked(View view) {
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
        lockImg.setImageResource(R.drawable.locked);
        locked = true;
    }

    /**
     * calls the server and changes battery icon based on how much voltage is left
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

    /**
     * calls server to change status of lights
     * @param view view
     */
    public void lightsClicked(View view) {
        lightsImg.startAnimation(animAlpha);

        if (lightsOn) {
            lightsImg.setAlpha(0.1f);
            car.setDataLights(false);
            pool.execute(car.postServiceLights);
            lightsOn = false;
        } else {
            lightsImg.setAlpha(1f);
            car.setDataLights(true);
            pool.execute(car.postServiceLights);
            lightsOn = true;
        }
    }

    /**
     * Opens a dialog view and ask to enter ip address if you get a response code 200 connection is
     * established else it fails.
     * @param view connect
     */
    public void connectOnClicked(View view) {
        connectionView = getLayoutInflater().inflate(R.layout.server_connect, null);
        AlertDialog.Builder connection = new AlertDialog.Builder(this);
        Button connectNowButton = connectionView.findViewById(R.id.connectNowButton);
        connection.setView(connectionView);
        final AlertDialog dialog = connection.create();
        ipNr = connectionView.findViewById(R.id.ipAddressEditText);

        if(!openConnection) {
            connectNowButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    stopRepeatingTask();
                    ip = ipNr.getText().toString();
                    CarRest.ip = ip;
                    pool.execute(car.getServiceSpeed);

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            if(CarRest.responseCode == 200) {
                                startRepeatingTask();
                                openConnection = true;
                                dialog.cancel();
                                connectButton.setText("Disconnect");
                                Toast.makeText(MainActivity.this,"Connection Success!", Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(MainActivity.this,"Connection failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, 1000);
                }
            });
            dialog.show();
        }else {
            stopRepeatingTask();
            openConnection = false;
            connectButton.setText("Connect");
            currentSpeed.setText("0");
        }
    }
}
