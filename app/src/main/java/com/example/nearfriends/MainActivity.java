package com.example.nearfriends;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_CONTACTS;

public class MainActivity extends AppCompatActivity implements Tab1.OnFragmentInteractionListener, Tab2.OnFragmentInteractionListener, Tab3.OnFragementInteractionListener, Tab4.OnFragmentInteractionListener {

    private static final int PERMISSIONS_REQUEST_CODE = 10001;
    private ArrayList<Contact> contactsArrayList = new ArrayList<>();

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
        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        PagerAdapter pagerAdapter = new PagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tablayout);
        tabLayout.setupWithViewPager(viewPager);
    }
}
