package com.example.dervis.autonomous;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class CarDataActivity extends AppCompatActivity {
    TextView batteryPercentText;
    int batteryPercent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_data);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        batteryPercentText = (findViewById(R.id.batteryValueText));
    }

    /**
     * takes user back to previous activity and closes this activity
     *
     * @param view view
     */
    public void backArrow(View view) {
        finish();
        overridePendingTransition(R.anim.enter_back_anim, R.anim.exit_back_anim);
    }

    protected void onStart() {
        super.onStart();
        batteryPercent = (int) Math.floor(MainActivity.currentBattery * 100);
        batteryPercentText.setText(batteryPercent + "%");
    }
}
