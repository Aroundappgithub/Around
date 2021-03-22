package com.example.nearfriends;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.EditText;
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
import java.util.OptionalDouble;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static java.lang.Math.PI;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

public class MainActivity2 extends AppCompatActivity {
    private static final String TAG = "MainActivity2";
    int LOCATION_REQUEST_CODE = 10001;

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;

    int updateCounter = 0;
    //need to set myRange upon initialization of app, update when changed
    double myRange = 0;
    //Locations stored in a location callback
    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationRequest == null) {
                return;
            }
            //Loop through locationResult list of locations
            for (Location location : locationResult.getLocations()) {
                //list of contacts within my range
                myRange = Double.valueOf(getIntent().getStringExtra("range value"));
                System.out.println("Setting myRange: "+myRange);
                ArrayList<Contact> withinMyRange = compareMyLocation(location.getLatitude(),location.getLongitude(), myRange);
                TextView distance = findViewById(R.id.distance);
                StringBuilder distanceBuilder = new StringBuilder("Contacts within range: \n");
                for (Contact contact:withinMyRange) {
                    //syntax is not efficient with large lists, use .append().append()...
                    distanceBuilder.append(contact.getName()+ "-distance: "+contact.getDistance().getAsDouble()+"\n");
                }
                System.out.println(distanceBuilder);
                distance.setText(distanceBuilder);

                Log.d(TAG, "OnLocationResult: " + location.toString());
                TextView textView = findViewById(R.id.location);
                textView.setText("My Location\n" + "Latitude: " + location.getLatitude() + "\nLongitude: " + location.getLongitude()+"\nUpdate Counter = "+updateCounter+"\nmy range: "+myRange);
                updateCounter++;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
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
                        apiException.startResolutionForResult(MainActivity2.this, 1001);
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
    private ArrayList<Contact> compareMyLocation(double myLat, double myLong, double range){
        ArrayList<Contact> inRangeContacts = new ArrayList<Contact>();
        ArrayList<Contact> contactArrayList = getContactList();
        for (Contact contact : contactArrayList) {
            double distance = haversineFormula(myLat,myLong,contact.getLatitude(),contact.getLongitude());
            if(distance<=range){
                contact.setDistance(OptionalDouble.of(distance));
                inRangeContacts.add(contact);
            }
        }
        return inRangeContacts;
    }

    /**
     * This method shall stream Contact objects into an ArrayList
     * that will be used by the caller to store in a hashmap...
     * Do i even need a hashmap? Can I reference the first index of the ArrayList?
     * @return ArrayList of contact information
     */
    private ArrayList<Contact> getContactList(){
        //Create fake map with locations
        ArrayList<Contact> contactArrayList = new ArrayList<Contact>();
        contactArrayList.add(new Contact("Isabella Murmann","Richmond", "Virginia", 37.562457, -77.473087, null));
        contactArrayList.add(new Contact("Bobby Shmurda", "Reston", "Virginia", 38.9586, -77.3570,null));
        //add contact without specified address, use city center as coordinates
        return contactArrayList;
    }

    private double haversineFormula(double myLat, double myLong, double theirLat, double theirLong){
        double toRadian = PI/180;
        double theirLatitudeInRadians = theirLat*toRadian;
        double theirLongitudeInRadians = theirLong*toRadian;
        double myLatitudeInRadians = myLat*toRadian;
        double myLongitudeInRadians = myLong*toRadian;

        double R = 6371000; //in meters
        double meterToMile = 0.000621371;
        double a = pow(sin((theirLatitudeInRadians-myLatitudeInRadians)/2),2)+cos(myLatitudeInRadians)*cos(theirLatitudeInRadians)*pow(sin((theirLongitudeInRadians-theirLongitudeInRadians)/2),2);
        double c = 2*atan2(sqrt(a),sqrt((1-a)));
        double d = R*c*meterToMile;
        return d;
    }
}