package br.com.mimoapp.mimo.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import br.com.mimoapp.mimo.R;
import br.com.mimoapp.mimo.config.FireBaseConfig;
import br.com.mimoapp.mimo.model.Service;
import br.com.mimoapp.mimo.model.User;
import br.com.mimoapp.mimo.persistence.UserPersist;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

/**
 * List adapter for Service model class
 */
public class ChooseServiceAdapter extends ArrayAdapter<Service> {

    private ArrayList<Service> services;
    private Context context;
    private CheckBox check;
    private Service service;
    private DatabaseReference fireBase;
    private User user;

    private ValueEventListener valueEventListener;
    private DatabaseReference UserServicesDbReference;

    private final String KEY_SERVICE_USERS = "service_users";
    private final String KEY_USER_SERVICES = "user_services";
    private final String KEY_SERVICES = "services";

    /**
     * Default constructor
     * @param c Context of Activity
     * @param objects Array of "Service" objects
     */
    public ChooseServiceAdapter(@NonNull Context c, @NonNull ArrayList<Service> objects) {
        super(c, 0, objects);
        this.services = objects;
        this.context = c;
    }

    /**
     * Assign content of Service array to outputs (TextViews or image views)
     * @param position position of current service object from array of services
     * @return view object
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View view = null;
        UserPersist userPersist = new UserPersist( context );
        user = userPersist.getUser();

        if ( services != null ) {

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.choose_service_list_item, parent, false);

            markChecks(view);

            final TextView serviceName = view.findViewById(R.id.choose_service_name);
            check = view.findViewById(R.id.cb_user_service);

            service = services.get( position );

            serviceName.setText( service.getName() );
            check.setText( service.getId() );

            // Set Click Event (Insert or remove node user_services and service_users)
            check.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    CheckBox checkBox = (CheckBox) v;

                    if ( checkBox.isChecked() ) {

                        // Update service_users node
                        fireBase = FireBaseConfig.getFirebase()
                                .child(KEY_SERVICE_USERS)
                                .child( checkBox.getText().toString() )
                                .child( user.getId() );

                        fireBase.child("id").setValue( user.getId() );
                        fireBase.child("name").setValue( user.getName() );
                        fireBase.child("phone").setValue( user.getPhone() );
                        fireBase.child("latitude").setValue( user.getLatitude() );
                        fireBase.child("longitude").setValue( user.getLongitude() );


                        // Update user_service
                        fireBase = FireBaseConfig.getFirebase()
                                .child( KEY_USER_SERVICES )
                                .child( user.getId() ).child( KEY_SERVICES )
                                .child( checkBox.getText().toString() );

                        fireBase.child("id").setValue( checkBox.getText().toString() );
                        fireBase.child("name").setValue( serviceName.getText() );

                        Toast.makeText(context, "You offer: " + serviceName.getText(), Toast.LENGTH_SHORT).show();

                    }else {

                        fireBase = FireBaseConfig.getFirebase()
                                .child(KEY_SERVICE_USERS)
                                .child( checkBox.getText().toString() )
                                .child( user.getId() );
                        fireBase.removeValue();
                        fireBase = FireBaseConfig.getFirebase()
                                .child( KEY_USER_SERVICES )
                                .child( user.getId() ).child( KEY_SERVICES )
                                .child( checkBox.getText().toString() );
                        fireBase.removeValue();
                    }
                }
            });
        }
        return view;
    }

    public void markChecks(View view) {
        UserPersist userPersist = new UserPersist( context );
        User user = userPersist.getUser();
        final CheckBox check = view.findViewById(R.id.cb_user_service);
        UserServicesDbReference = FireBaseConfig.getFirebase().child( KEY_USER_SERVICES ).child( user.getId() ).child( KEY_SERVICES );
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    if ( data.child("id").getValue() != null && data.child("id").getValue().equals( check.getText() ) ) {
                        check.setChecked( true );
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };

        UserServicesDbReference.addValueEventListener( valueEventListener );

    }
}

