package com.example.nearfriends;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.location.LocationResult;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_CONTACTS;

public class MainActivity extends AppCompatActivity implements Tab1.OnFragmentInteractionListener, Tab2.OnFragmentInteractionListener, Tab3.OnFragmentInteractionListener, Tab4.OnFragmentInteractionListener {

    private static final int PERMISSIONS_REQUEST_CODE = 10001;
    private LocationResult currentLocationResult;
    private ArrayList<Contact> nearbyContacts;

    /**
     * Initialize home screen activity
     *
     * @param savedInstanceState
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Check permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermission();
        } else {
            //handle older version of android
        }
    }


    /**
     * Request Contacts and Location permissions. Required for app to function
     */
    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) + ContextCompat.checkSelfPermission(this, READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, ACCESS_FINE_LOCATION) || ActivityCompat.shouldShowRequestPermissionRationale(this, READ_CONTACTS)) {
                Snackbar.make(this.findViewById(android.R.id.content),
                        "This app requires contacts and location permission to function",
                        Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
                        v -> requestPermissions(
                                new String[]{ACCESS_FINE_LOCATION, READ_CONTACTS},
                                PERMISSIONS_REQUEST_CODE)).show();
            } else {
                requestPermissions(new String[]{ACCESS_FINE_LOCATION, READ_CONTACTS}, PERMISSIONS_REQUEST_CODE);
            }
        } else {
            startFragments();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    //check location permission
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(getApplicationContext(), "All permissions granted", Toast.LENGTH_SHORT).show();
                        startFragments();
                    } else {
                        Toast.makeText(getApplicationContext(), "Location and Contacts permissions must be granted for app to function", Toast.LENGTH_LONG).show();
                        checkPermission();
                    }
                }
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    /**
     * Start the tabbed and swipeable fragment view
     */
    public void startFragments() {
        ViewPager viewPager = findViewById(R.id.pager);
        PagerAdapter pagerAdapter = new PagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);
        TabLayout tabLayout = findViewById(R.id.tablayout);
        tabLayout.setupWithViewPager(viewPager);
    }

    /**
     * Set to update every time location updates in tab 2
     *
     * @param locationResult
     */
    public void setTabTwoData(LocationResult locationResult, ArrayList<Contact> nearbyContactsList) {
//        nearbyContacts = new ArrayList<>();
        currentLocationResult = locationResult;
        nearbyContacts = nearbyContactsList;
    }

    public LocationResult getCurrentLocationResult() {
        return currentLocationResult;
    }

    public ArrayList<Contact> getNearbyContacts() {
        return nearbyContacts;
    }

    /*public void clearCurrentLocationResult() {
        if (nearbyContacts != null || currentLocationResult != null) {
            currentLocationResult = null;
            nearbyContacts.clear();
        }
    }*/
}
