package br.com.mimoapp.mimo.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import br.com.mimoapp.mimo.R;
import br.com.mimoapp.mimo.model.Contact;

/**
 * Created by rafael on 21/10/17.
 */

public class ContactAdapter extends ArrayAdapter<Contact> {

    private ArrayList<Contact> contacts;
    private Context context;

    public ContactAdapter(@NonNull Context c, @NonNull ArrayList<Contact> objects) {
        super(c, 0, objects);
        this.contacts = objects;
        this.context = c;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View view = null;

        if ( contacts != null ) {

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.contact_list_item, parent, false);

            TextView contactName = view.findViewById(R.id.contact_name);
            TextView contactPhone = view.findViewById(R.id.contact_phone);

            Contact contact = contacts.get( position );
            contactName.setText(contact.getName());
            contactPhone.setText(contact.getPhone());
        }

        return view;
    }
}
