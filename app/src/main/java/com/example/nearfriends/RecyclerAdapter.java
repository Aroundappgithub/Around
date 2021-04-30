package com.example.nearfriends;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyAdapterViewHolder> {

    Context context;
    ArrayList<Contact> contactArrayList;

    /**
     * @param context          the context making this call
     * @param contactArrayList the Contact arraylist
     */
    public RecyclerAdapter(Context context, ArrayList<Contact> contactArrayList) {
        this.context = context;
        this.contactArrayList = contactArrayList;
    }

    @NonNull
    @Override
    public MyAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_list, parent, false);
        return new MyAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyAdapterViewHolder holder, int position) {
        //Set each UI component to relative Contact element
        Contact contact = contactArrayList.get(position);
        holder.contactName.setText(contact.getName());
        holder.contactAddress.setText(contact.getAddress());
        holder.contactGroup.setText(contact.getGroup().orElse(""));
    }


    @Override
    public int getItemCount() {
        return contactArrayList.size();
    }

    public class MyAdapterViewHolder extends RecyclerView.ViewHolder {

        TextView contactName, contactAddress, contactGroup;
        CheckBox favoriteCheckbox;

        public MyAdapterViewHolder(View itemView) {
            super(itemView);
            //Link each variable with respective UI component
            contactName = (TextView) itemView.findViewById(R.id.contactName);
            contactAddress = (TextView) itemView.findViewById(R.id.contactAddress);
            contactGroup = (TextView) itemView.findViewById(R.id.contactGroup);
            //add starred listener functionality here?
            favoriteCheckbox = itemView.findViewById(R.id.favorite);
        }
    }
}
