package com.example.nearfriends;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    int LOCATION_REQUEST_CODE = 10001;

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;

    int updateCounter = 0;
    //Locations stored in a location callback
    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationRequest == null) {
                return;
            }
            //Loop through locationResult list of locations
            for (Location location : locationResult.getLocations()) {
                boolean withinMyRange = compareMyLocation(location.getLatitude(),location.getLongitude());


                Log.d(TAG, "OnLocationResult: " + location.toString());
                TextView textView = findViewById(R.id.location);
                textView.setText("My Location\n" + "Latitude: " + location.getLatitude() + "\nLongitude: " + location.getLongitude()+"\nUpdate Counter = "+updateCounter);
                updateCounter++;
            }
        }
    };

    /**
     * Initialize home screen activity
     * @param savedInstanceState
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Create a FusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        //Create a LocationRequest, set update interval to 10 secs (minimum update of 5 secs), set priority to high
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(2000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Called after onCreate, draw visual elements here
     */
    @Override
    protected void onStart() {
        super.onStart();
        //Check if user has granted app location permission yet, else ask for location permission
        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//            getLastLocation();
            checkSettingsAndStartLocationUpdates();
        } else {
            askLocationPermission();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopLocationUpdates();
    }

    /**
     *
     */
    private void checkSettingsAndStartLocationUpdates() {
        //Specifies type of location services
        LocationSettingsRequest locationSettingsRequest = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest).build();
        //Main entry point for interacting with location settings-enabler APIs
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        //Response of checking location settings
        Task<LocationSettingsResponse> locationSettingsResponseTask = settingsClient.checkLocationSettings(locationSettingsRequest);
        locationSettingsResponseTask.addOnSuccessListener(locationSettingsResponse -> {
            //settings of device are satisfied and we can start location updates
            startLocationUpdates();
        });
        //Location settings are not satisfied, show the user a dialog
        locationSettingsResponseTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //ask user to turn on location permissions
                if (e instanceof ResolvableApiException) {
                    ResolvableApiException apiException = (ResolvableApiException) e;
                    //Show dialog by calling startResolutionForResult()
                    try {
                        apiException.startResolutionForResult(MainActivity.this, 1001);
                    } catch (IntentSender.SendIntentException sendIntentException) {
                        sendIntentException.printStackTrace();
                    }

                }
            }
        });
    }

    /**
     * Start location updates. Results are delivered in a list of Location objects passed by
     * LocationCallback.onLocationResult()
     */
    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    private void getLastLocation() {
        @SuppressLint("MissingPermission") Task<Location> locationTask = fusedLocationProviderClient.getLastLocation();

        locationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    //we have location
                    Log.d(TAG, "onSuccess: " + location.toString());
                    Log.d(TAG, "onSuccess: " + location.getLatitude());
                    Log.d(TAG, "onSuccess: " + location.getLongitude());
                    TextView textView = findViewById(R.id.location);
                    textView.setText("My Location\n" + "Latitude: " + location.getLatitude() + "\nLongitude: " + location.getLongitude());
                } else {
                    Log.d(TAG, "onSuccess: Location was null...");
                    //location from getLastLocation from the fusedLocationProviderClient is a cached location from other applications
                }
            }
        });

        locationTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "onFailure: " + e.getLocalizedMessage());
            }
        });
    }

    /**
     * Asks user for location permission.
     *
     * Needs further development
     */
    private void askLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, ACCESS_FINE_LOCATION)) {
                Log.d(TAG, "askLocationPermission: you should show and alert dialog..."); //implement a button to request location
                ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            }
        }
    }

    public void onRequestPermissionResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //permission granted
//                getLastLocation();  //call last location since permission is granted
                checkSettingsAndStartLocationUpdates(); //start location updates
            } else {
                //permission not granted - add logic to handle location not granted
            }
        }
    }

    /**
     * Compare user's most recent location coordinates to contacts.
     * @param myLat user's latest latitude coordinate
     * @param myLong user's latest longitude coordinate
     * @return contacts that are within user's range
     */
    private boolean compareMyLocation(double myLat, double myLong){
        boolean result = true;
        //call to contacts list, for now call fake list
        ArrayList<Contact> contactArrayList = new ArrayList<Contact>();
        return result;
    }

    private double[][] getContactLocations(){
        double[][] locations = new double[0][0];
        return locations;
    }
}
