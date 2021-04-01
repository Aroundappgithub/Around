package com.example.nearfriends;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.OptionalDouble;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_CONTACTS;
import static java.lang.Math.PI;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

public class MainActivity2 extends AppCompatActivity {
    private static final String TAG = "MainActivity2";
    private static final int PERMISSIONS_REQUEST_CODE = 10001;

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
                System.out.println("Setting myRange: " + myRange);
                ArrayList<Contact> withinMyRange = compareMyLocation(location.getLatitude(), location.getLongitude(), myRange);
                TextView distance = findViewById(R.id.distance);
                StringBuilder distanceBuilder = new StringBuilder("Contacts within range: \n");
                for (Contact contact : withinMyRange) {
                    //syntax is not efficient with large lists, use .append().append()...
                    distanceBuilder.append(contact.getName() + "-distance: " + contact.getDistance().getAsDouble() + "\n");
                }
                System.out.println(distanceBuilder);
                distance.setText(distanceBuilder);

                Log.d(TAG, "OnLocationResult: " + location.toString());
                TextView textView = findViewById(R.id.location);
                textView.setText("My Location\n" + "Latitude: " + location.getLatitude() + "\nLongitude: " + location.getLongitude() + "\nUpdate Counter = " + updateCounter + "\nmy range: " + myRange);
                updateCounter++;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) + ContextCompat.checkSelfPermission(this, READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, ACCESS_FINE_LOCATION) || ActivityCompat.shouldShowRequestPermissionRationale(this, READ_CONTACTS)) {
                Snackbar.make(this.findViewById(android.R.id.content),
                        "Please grant permissions to compare current location to contact locations",
                        Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
                        v -> requestPermissions(
                                new String[]{ACCESS_FINE_LOCATION, READ_CONTACTS},
                                PERMISSIONS_REQUEST_CODE)).show();
            } else {
                requestPermissions(new String[]{ACCESS_FINE_LOCATION, READ_CONTACTS}, PERMISSIONS_REQUEST_CODE);
            }
        } else {
            //permissions already granted, continue functionality
            checkSettingsAndStartLocationUpdates();
            fetchContacts();
        }
    }

    /**
     * Called after onCreate, draw visual elements here
     */
    @Override
    protected void onStart() {
        super.onStart();
        //Create a FusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        //Create a LocationRequest, set update interval to 10 secs (minimum update of 5 secs), set priority to high
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(2000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermission();
        } else {
            //handle older version of android
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
        System.out.println("STARTING LOCATION UPDATES");
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    //check location permission
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(getApplicationContext(), "All permissions granted", Toast.LENGTH_SHORT).show();
                        checkSettingsAndStartLocationUpdates();
                        fetchContacts();
                    } else {
                        Toast.makeText(getApplicationContext(), "Location and Contacts permissions must be granted to compare current location to contact locations", Toast.LENGTH_LONG).show();
                        checkPermission();
                    }
                }
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    /**
     * Compare user's most recent location coordinates to contacts.
     *
     * @param myLat  user's latest latitude coordinate
     * @param myLong user's latest longitude coordinate
     * @return contacts that are within user's range
     */
    private ArrayList<Contact> compareMyLocation(double myLat, double myLong, double range) {
        ArrayList<Contact> inRangeContacts = new ArrayList<Contact>();
        ArrayList<Contact> contactArrayList = getContactList();
        for (Contact contact : contactArrayList) {
            double distance = haversineFormula(myLat, myLong, contact.getLatitude(), contact.getLongitude());
            if (distance <= range) {
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
     *
     * @return ArrayList of contact information
     */
    private ArrayList<Contact> getContactList() {
        //Create fake map with locations
        ArrayList<Contact> contactArrayList = new ArrayList<Contact>();
        contactArrayList.add(new Contact("Isabella Murmann", "Richmond", "Virginia", 37.562457, -77.473087, null));
        contactArrayList.add(new Contact("Bobby Shmurda", "Reston", "Virginia", 38.9586, -77.3570, null));
        //add contact without specified address, use city center as coordinates
        return contactArrayList;
    }

    private double haversineFormula(double myLat, double myLong, double theirLat,
                                    double theirLong) {
        double toRadian = PI / 180;
        double theirLatitudeInRadians = theirLat * toRadian;
        double theirLongitudeInRadians = theirLong * toRadian;
        double myLatitudeInRadians = myLat * toRadian;
        double myLongitudeInRadians = myLong * toRadian;

        double R = 6371000; //in meters
        double meterToMile = 0.000621371;
        double a = pow(sin((theirLatitudeInRadians - myLatitudeInRadians) / 2), 2) + cos(myLatitudeInRadians) * cos(theirLatitudeInRadians) * pow(sin((theirLongitudeInRadians - theirLongitudeInRadians) / 2), 2);
        double c = 2 * atan2(sqrt(a), sqrt((1 - a)));
        double d = R * c * meterToMile;
        return d;
    }

    private void fetchContacts() {
        System.out.println("FETCHING CONTACTS");
/*//        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        Uri uri = ContactsContract.Data.CONTENT_URI;
        String[] projection = {ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.SipAddress.SIP_ADDRESS};
        String selection = null;
        String[] selectionArgs = null;
        String sortOrder = null;

        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(uri, null, selection, selectionArgs, sortOrder);

        while (cursor.moveToNext()) {
//            if(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))=="Isabell"){
            String addy = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.SipAddress.SIP_ADDRESS));
//                System.out.println("addy: "+addy);
//            }
            String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
//            String num = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            System.out.println("contact name: " + name + " addy: " + addy);
        }*/

        Uri postal_uri = ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI;
        Cursor cursor = getContentResolver().query(postal_uri, null, null, null, null);
        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.DISPLAY_NAME));
            String street = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET));
            String city = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY));
            String country = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY));
            String fulladdy = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS));

            System.out.println("name: " + name + " full addy: " + fulladdy);
        }
    }
}