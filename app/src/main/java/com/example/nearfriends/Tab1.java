package com.example.nearfriends;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Optional;
import java.util.OptionalDouble;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Tab1#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Tab1 extends Fragment {

    //To pass in arguments into this fragment: create a new instance, create an argument Bundle, add arguments to the bundle

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    RecyclerView recyclerView;
    RecyclerAdapter recyclerAdapter;
    //List of all phone contacts as Contacts object
    private ArrayList<Contact> contactsArrayList = new ArrayList<>();
    //list of phone contacts that don't have addresses
    private ArrayList<Contact> emptyAddressContacts = new ArrayList<>();

    private Handler mainHandler = new Handler();

    public Tab1() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Tab1.
     */
    public static Tab1 newInstance(String param1, String param2) {
        Tab1 fragment = new Tab1();
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
        return inflater.inflate(R.layout.fragment_tab1, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        //Create a recycler view (in fragment_tab1.xml) that recursively replicates a UI object
        recyclerView = getView().findViewById(R.id.recyclerView_id);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        //Get list of all phone contacts
        if (contactsArrayList.isEmpty()) {
            new Thread(() -> {
                fetchContacts();
                mainHandler.post(() -> {
                    //Initialize adapter, pass in this context and the Contacts arraylist
                    recyclerAdapter = new RecyclerAdapter(getContext(), contactsArrayList);
                    recyclerView.setAdapter(recyclerAdapter);
                    recyclerAdapter.notifyDataSetChanged();
                });
            }).start();
        } else {
            //Initialize adapter, pass in this context and the Contacts arraylist
            recyclerAdapter = new RecyclerAdapter(getContext(), contactsArrayList);
            recyclerView.setAdapter(recyclerAdapter);
            recyclerAdapter.notifyDataSetChanged();
        }

        SearchView searchView = getView().findViewById(R.id.action_search);
        searchView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                searchView.setIconified(false);
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                recyclerAdapter.getFilter().filter(newText);
                return false;
            }
        });
    }

    public interface OnFragmentInteractionListener {
    }

    /**
     * Get list of contacts from phone and initialize recycler view adapter
     */
    private void fetchContacts() {
        System.out.println("FETCHING CONTACTS");
        Uri uri = ContactsContract.Contacts.CONTENT_URI;
        Cursor cursor = getContext().getContentResolver().query(uri, null, null, null, null);
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String address = "";
                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                Cursor addCursor = getContext().getContentResolver().query(ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI, null, ContactsContract.CommonDataKinds.StructuredPostal.CONTACT_ID + " = ?", new String[]{id}, null);
                while (addCursor.moveToNext()) {
                    address = addCursor.getString(addCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS));
                }
                addCursor.close();
                //if address for a contact is empty, add it to list, ask user if they want to add addresses
                if (address.isEmpty()) {
                    Contact missingAddressContact = new Contact(name, OptionalDouble.empty(), OptionalDouble.empty(), OptionalDouble.empty(), Optional.empty(), Optional.<String>empty());
                    emptyAddressContacts.add(missingAddressContact);
                }

                Contact singleContact = new Contact(name, OptionalDouble.empty(), OptionalDouble.empty(), OptionalDouble.empty(), Optional.ofNullable(address), Optional.<String>empty());
                contactsArrayList.add(singleContact);
            }
        }
        cursor.close();
    }
}