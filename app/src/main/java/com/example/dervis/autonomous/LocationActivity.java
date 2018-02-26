package com.example.dervis.autonomous;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LocationActivity extends FragmentActivity implements OnMapReadyCallback {
    private final static int INTERVAL = 1000;
    CarRest car = new CarRest();
    ExecutorService pool = Executors.newCachedThreadPool();
    Handler restLooper;
    GoogleMap mMap;
    Marker carLocationMarker;
    LatLng carLocation;
    MarkerOptions carOptions;
    Boolean addMark = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        restLooper = new Handler();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Double lat;
        Double lng;
        if (mMap != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            lat = car.getLoacation()[0];
            lng = car.getLoacation()[1];
            if (lat != null) {
                carLocation = new LatLng(lat, lng);
                carOptions = new MarkerOptions().position(carLocation).title("Car #1");
                if (addMark) {
                    carLocationMarker = mMap.addMarker(carOptions);
                    addMark = false;
                }
                carLocationMarker.setPosition(carLocation);

                mMap.moveCamera(CameraUpdateFactory.newLatLng(carLocation));
                CameraUpdate center = CameraUpdateFactory.newLatLng(carLocation);
                CameraUpdate zoom = CameraUpdateFactory.zoomTo(18);
                mMap.moveCamera(center);
                mMap.animateCamera(zoom);
            }
        }
    }


    Runnable handlerTask = new Runnable() {
        @Override
        public void run() {

            car.carDataService("/location");
            pool.execute(car.getService);


            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onMapReady(mMap);
                }
            });
            restLooper.postDelayed(handlerTask, INTERVAL);
        }
    };

    void startRepeatingTask() {
        handlerTask.run();
    }

    void stopRepeatingTask() {
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

    public void backArrowClicked(View view) {
        finish();
        overridePendingTransition(R.anim.enter_back_anim, R.anim.exit_back_anim);
    }
}