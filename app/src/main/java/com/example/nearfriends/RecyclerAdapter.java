package com.example.nearfriends;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyAdapterViewHolder> implements Filterable {

    Context context;
    ArrayList<Contact> contactArrayList;
    ArrayList<Contact> preFilterList;

    /**
     * @param context          the context making this call
     * @param contactArrayList the Contact arraylist
     */
    public RecyclerAdapter(Context context, ArrayList<Contact> contactArrayList) {
        this.context = context;
        this.contactArrayList = contactArrayList;
        preFilterList = new ArrayList<>(contactArrayList);
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
        holder.contactAddress.setText(contact.getAddress().orElse(""));
        holder.contactGroup.setText(contact.getGroup().orElse(""));
    }


    @Override
    public int getItemCount() {
        return contactArrayList.size();
    }

    @Override
    public Filter getFilter() {
        return listFilter;
    }

    private Filter listFilter = new Filter() {
        @Override
        //Works on a background thread
        protected FilterResults performFiltering(CharSequence constraint) {
            ArrayList<Contact> filteredList = new ArrayList<>();

            if(constraint == null || constraint.length()==0){
                filteredList.addAll(preFilterList);
            }else{
                String searchPattern = constraint.toString().toLowerCase().trim();

                //Compare each contact to the prefilterlist
                for(Contact contact: preFilterList){
                    if(contact.getName().toLowerCase().contains(searchPattern)){
                        filteredList.add(contact);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            contactArrayList.clear();
            contactArrayList.addAll((ArrayList)results.values);
            notifyDataSetChanged();
        }
    };

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
