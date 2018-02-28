package com.example.dervis.autonomous;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

/**
 * this class shows diagnostics for the car
 */
public class DiagnosticsActivity extends AppCompatActivity {
    TextView odometer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diagnostics);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        odometer = findViewById(R.id.odometerValueText);
    }

    /**
     * takes user back to previous activity and closes this activity
     * @param view view
     */
    public void backArrow(View view) {
        finish();
        overridePendingTransition(R.anim.enter_back_anim, R.anim.exit_back_anim);
    }

    /**
     * takes user to car data activity and closes this activity
     * @param view view
     */
    public void carDataActivity(View view) {
        startActivity(new Intent(DiagnosticsActivity.this, CarDataActivity.class));
        overridePendingTransition(R.anim.enter_anim, R.anim.exit_anim);
    }

    /**
     * called on when entered this activity and sets the text for odometer
     */
    protected void onStart() {
        super.onStart();
        odometer.setText(MainActivity.currentOdometer + "m");
    }
}
