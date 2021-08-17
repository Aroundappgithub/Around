package com.example.nearfriends;

import android.annotation.SuppressLint;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;

import static java.lang.Math.PI;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Tab2#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Tab2 extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private RecyclerView recyclerView;

    private Context thisContext;

    private TextView rangeText;
    private SeekBar rangeBar;
    private TextView currentCity;

    private String currentCityString;
    private Geocoder geocoder;

    //Contacts list with available contact address
    private ArrayList<Contact> userContactsList = new ArrayList<>();
    private ArrayList<Contact> nearbyContactsList = new ArrayList<>();

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;

    double myLastLat, myLastLong;
    //user must change location by this many miles for distance comparison in locationCallback
    double locationChangeThreshold = 0.1;

    LocationResult globalLocationResult;

    private Handler mainHandler = new Handler();

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            globalLocationResult = locationResult;
            //check if user location changed more than the locationChangeThreshold, otherwise nearbyContactsList does not need to be updated
            if ((haversineFormula(myLastLat, myLastLong, locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude()) >= locationChangeThreshold)) {
                if (locationResult == null) {
                    return;
                }

                //Set up device user's current city to display at the top of tab 2
                try {
                    List<Address> myCity = geocoder.getFromLocation(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude(), 1);
                    if (myCity.isEmpty()) {
                        currentCityString = "Waiting for Location";
                    } else {
                        currentCityString = myCity.get(0).getLocality();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                currentCity.setText(currentCityString);

                //Compare contact locations to my location
                if (!userContactsList.isEmpty()) {
                    double range = rangeBar.getProgress();
                    //Check within range contacts
                    ArrayList<Contact> currentNearbyContactsList = nearbyContactsList;
                    ArrayList<Contact> locationResultNearbyContactsList = compareMyLocation(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude(), range);
                    RecyclerAdapter recyclerAdapter = new RecyclerAdapter(thisContext, locationResultNearbyContactsList);
                    recyclerView.setAdapter(recyclerAdapter);
                    recyclerAdapter.notifyDataSetChanged();
                    if (!currentNearbyContactsList.equals(locationResultNearbyContactsList)) {
                        systemNotifyUser(currentNearbyContactsList);
                    }
                } else {
                    Toast.makeText(getContext(), "Waiting for contacts list to populate...", Toast.LENGTH_LONG).show();
                }
                myLastLat = locationResult.getLastLocation().getLatitude();
                myLastLong = locationResult.getLastLocation().getLongitude();
            }
        }
    };

    public Tab2() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Tab2.
     */
    // TODO: Rename and change types and number of parameters
    public static Tab2 newInstance(String param1, String param2) {
        Tab2 fragment = new Tab2();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tab2, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        recyclerView = getView().findViewById(R.id.recyclerViewTab2_id);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        geocoder = new Geocoder(getContext());
        thisContext = getContext();

        //Create FusedLocationProviderClient to get current location
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(thisContext);
        //Create a LocationRequest, set update interval and fastest update interval, set priority to high
        locationRequest = LocationRequest.create();
        //set to 1 min (60000ms)
        locationRequest.setInterval(10000);
        //set to 10 sec (10000ms)
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        startLocationUpdates();

        //Set up range seek bar
        currentCity = getView().findViewById(R.id.currentCityText);
        rangeText = getView().findViewById(R.id.rangeText);
        rangeBar = getView().findViewById(R.id.rangeBar);
        rangeText.setText(rangeBar.getProgress() + " mi");

        rangeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                rangeText.setText(progress + " mi");
                //update nearby contact list
                if (!userContactsList.isEmpty() && globalLocationResult != null) {
                    ArrayList<Contact> currentNearbyContactsList = nearbyContactsList;
                    ArrayList<Contact> onSeekBarChangeNearbyContactList = compareMyLocation(globalLocationResult.getLastLocation().getLatitude(), globalLocationResult.getLastLocation().getLongitude(), rangeBar.getProgress());
                    RecyclerAdapter onSeekBarChangeRecyclerAdapter = new RecyclerAdapter(thisContext, onSeekBarChangeNearbyContactList);
                    recyclerView.setAdapter(onSeekBarChangeRecyclerAdapter);
                    onSeekBarChangeRecyclerAdapter.notifyDataSetChanged();
                    if (!currentNearbyContactsList.equals(onSeekBarChangeNearbyContactList)) {
                        systemNotifyUser(currentNearbyContactsList);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void startLocationUpdates() {
        //Specifies type of location service
        LocationSettingsRequest locationSettingsRequest = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest).build();
        //Entry point for interacting with location settings-enabler APIs
        SettingsClient settingsClient = LocationServices.getSettingsClient(thisContext);
        //Response of checking location settings
        Task<LocationSettingsResponse> locationSettingsResponseTask = settingsClient.checkLocationSettings(locationSettingsRequest);
        locationSettingsResponseTask.addOnSuccessListener(locationSettingsResponse -> {
            requestLocationUpdates();
            if (userContactsList.isEmpty()) {
                new Thread(() -> {
                    fetchContacts();
                    mainHandler.post(() -> {
                        if (globalLocationResult != null) {
                            ArrayList<Contact> currentNearbyContactsList = nearbyContactsList;
                            ArrayList<Contact> onThreadCompleteNearbyContactsList = compareMyLocation(globalLocationResult.getLastLocation().getLatitude(), globalLocationResult.getLastLocation().getLongitude(), rangeBar.getProgress());
                            RecyclerAdapter onThreadCompleteRecyclerAdapter = new RecyclerAdapter(thisContext, onThreadCompleteNearbyContactsList);
                            recyclerView.setAdapter(onThreadCompleteRecyclerAdapter);
                            if (!currentNearbyContactsList.equals(onThreadCompleteNearbyContactsList)) {
                                systemNotifyUser(currentNearbyContactsList);
                            }
                        }
                    });
                }).start();
            }
        });

        //in case the location settings request is not satisfied, should not reach this point
        locationSettingsResponseTask.addOnFailureListener(e -> {
            //ask user to turn on location permissions
            if (e instanceof ResolvableApiException) {
                ResolvableApiException apiException = (ResolvableApiException) e;
                //Show dialog by calling startResolutionForResult()
                try {
                    apiException.startResolutionForResult(getActivity(), 1001);
                } catch (IntentSender.SendIntentException sendIntentException) {
                    sendIntentException.printStackTrace();
                }
            }
        });
    }

    /**
     * Parse user contacts api, add contacts with an address to a contacts list
     */
    private void fetchContacts() {
        Uri uri = ContactsContract.Contacts.CONTENT_URI;
        Cursor cursor = thisContext.getContentResolver().query(uri, null, null, null, null);

        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String address = "";
                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                Cursor addCursor = thisContext.getContentResolver().query(ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI, null, ContactsContract.CommonDataKinds.StructuredPostal.CONTACT_ID + " = ?", new String[]{id}, null);
                while (addCursor.moveToNext()) {
                    address = addCursor.getString(addCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS));
                }
                addCursor.close();
                //if address is empty, we can't do distance comparison
                if (!address.isEmpty()) {
                    try {
                        List<Address> wholeAddress = geocoder.getFromLocationName(address, 1);
                        Address location = wholeAddress.get(0);
                        double lat = location.getLatitude();
                        double longitude = location.getLongitude();
                        Contact contactInfo = new Contact(name, OptionalDouble.of(lat), OptionalDouble.of(longitude), OptionalDouble.empty(), Optional.ofNullable(address), Optional.empty());
                        userContactsList.add(contactInfo);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            cursor.close();
        }
    }

    private ArrayList<Contact> compareMyLocation(double myLat, double myLong, double range) {
        ArrayList<Contact> inRangeContacts = new ArrayList<>();
        for (Contact contact : userContactsList) {
            double distance = haversineFormula(myLat, myLong, contact.getLatitude().getAsDouble(), contact.getLongitude().getAsDouble());
            if (distance <= range) {
                contact.setDistance(OptionalDouble.of(distance));
                boolean contactAddedFlag = false;
                for (int i = 0; i < inRangeContacts.size(); i++) {
                    if (contact.getDistance().getAsDouble() <= inRangeContacts.get(i).getDistance().getAsDouble()) {
                        inRangeContacts.add(i, contact);
                        contactAddedFlag = true;
                        break;
                    }
                }
                if (!contactAddedFlag || inRangeContacts.size() == 0) {
                    inRangeContacts.add(contact);
                }
            }
        }
        //Update my location and nearby contacts list in main activity for tab 3 to retrieve
        nearbyContactsList = inRangeContacts;
        updateMainActivityLocationResult();
        return inRangeContacts;
    }

    /**
     * This method literally does this calculation: android.location.Location.distanceBetween()
     *
     * @param myLat
     * @param myLong
     * @param theirLat
     * @param theirLong
     * @return
     */
    private double haversineFormula(double myLat, double myLong, double theirLat, double theirLong) {
        double toRadian = PI / 180;
        double theirLatInRadians = theirLat * toRadian;
        double theirLongInRadians = theirLong * toRadian;
        double myLatInRadians = myLat * toRadian;
        double myLongInRadians = myLong * toRadian;

        double R = 6371000; //in meters
        double meterToMileConversion = 0.000621371;
        double a = pow(sin((theirLatInRadians - myLatInRadians) / 2), 2) + cos(myLatInRadians) * cos(theirLatInRadians) * pow(sin((theirLongInRadians - myLongInRadians) / 2), 2);
        double c = 2 * atan2(sqrt(a), sqrt(1 - a));
        double d = R * c * meterToMileConversion;
        return d;
    }

    @SuppressLint("MissingPermission")
    private void requestLocationUpdates() {
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    //update my location and nearby contacts list in the MainActivity.java for tab 3 to retrieve
    public void updateMainActivityLocationResult() {
        if (getActivity() instanceof MainActivity) {
//            ((MainActivity) getActivity()).clearCurrentLocationResult();
            ((MainActivity) getActivity()).setTabTwoData(globalLocationResult, nearbyContactsList);
        }
    }

    /**
     * Compare current nearby contacts list to new nearby contacts list and send a system
     * notification with new nearby contacts.
     */
    private void systemNotifyUser(ArrayList<Contact> currentNearbyContacts) {
        ArrayList<Contact> newNearbyContactsList = new ArrayList<>();
        for (Contact a : nearbyContactsList) {
            newNearbyContactsList.add(a);
        }
        //Prune newNearbyContactsList for unique contacts
        for (Contact con : currentNearbyContacts) {
            if (newNearbyContactsList.contains(con)) {
                newNearbyContactsList.remove(newNearbyContactsList.indexOf(con));
            }
        }

        //Android version greater than Oreo must create a notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel("channel1", "Nearby contact(s)", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = getActivity().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        //Send notification for only unique contacts
        if (newNearbyContactsList.size() == 1) {
//            Intent textIntent = new Intent()

            Notification notification = new NotificationCompat.Builder(thisContext, "channel1")
                    .setSmallIcon(R.drawable.ic_baseline_group_add_24)
                    .setContentTitle("Around")
                    .setContentText(newNearbyContactsList.get(0).getName() + " is nearby!")
                    .build();
            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getActivity());
            notificationManagerCompat.notify(1, notification);
        } else if (newNearbyContactsList.size() > 1) {
            Notification notification = new NotificationCompat.Builder(thisContext, "channel1")
                    .setSmallIcon(R.drawable.ic_baseline_group_add_24)
                    .setContentTitle("Around")
                    .setContentText("New contacts nearby!")
                    .build();
            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getActivity());
            notificationManagerCompat.notify(1, notification);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        stopLocationUpdates();
    }

    private void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    public interface OnFragmentInteractionListener {
    }
}