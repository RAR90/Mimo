package br.com.mimoapp.mimo.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import br.com.mimoapp.mimo.R;
import br.com.mimoapp.mimo.activity.ChatActivity;
import br.com.mimoapp.mimo.activity.ServiceActivity;
import br.com.mimoapp.mimo.activity.SignInActivity;
import br.com.mimoapp.mimo.adapter.ChooseServiceAdapter;
import br.com.mimoapp.mimo.adapter.ContactAdapter;
import br.com.mimoapp.mimo.adapter.ServiceAdapter;
import br.com.mimoapp.mimo.config.FireBaseConfig;
import br.com.mimoapp.mimo.model.App;
import br.com.mimoapp.mimo.model.Contact;
import br.com.mimoapp.mimo.model.Service;
import br.com.mimoapp.mimo.model.User;
import br.com.mimoapp.mimo.model.Worker;
import br.com.mimoapp.mimo.persistence.UserPersist;
import br.com.mimoapp.mimo.persistence.WorkerPersist;

/**
 * <h1> Fragment Description <h1/>
 * <ul>
     * <li>Required empty public constructor<li/>
     * <li>Get current user information<li/>
     * <li>Get current user in "worker" list<li/>
     * <li>If current user is not in "worker" list, call view of sign in<li/>
     * <li>Assign button SignIn<li/>
     * <li>Bind click event if button exists<li/>
     * <li>Call intent Sign In Activity<li/>
     * <li>If current user in "worker" list, call services select fragment<li/>
 * <ul/>
 */

public class MyServicesFragment extends Fragment {

    private final String KEY_STATUS = "status";
    private final String KEY_USER_SERVICES_NODE = "user_services";
    private final String KEY_SERVICES_NODE = "services";
    private String status;
    private Worker worker;
    private WorkerPersist workerPersist;
    private ProgressBar userServicesProgress;
    private DatabaseReference fireBaseServices;
    private ValueEventListener valueEventListenerServices;

    // Services
    private ArrayList<Service> services;
    private ChooseServiceAdapter adapter;

    private TextView tvPrice;

    private final String KEY_APP_NODE = "app";
    private final String KEY_PRICE = "price";
    private final String KEY_PRICE_UNITS = "units";
    private final String KEY_PRICE_CENTS = "cents";

    public MyServicesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {

        View view;

        // Get worker information
        workerPersist = new WorkerPersist( getActivity().getApplicationContext() );
        worker = workerPersist.getFromSharedPreferences();
        status = worker.getStatus();

        // Get current user information
        UserPersist userPersist = new UserPersist( getActivity().getApplicationContext() );
        final User mySelf = userPersist.getUser();

        // Get current user in "worker" list
        DatabaseReference fireBase = FireBaseConfig.getFirebase().child( "worker" ).child( mySelf.getId() ).child( KEY_STATUS );

        // Update local worker status
        fireBase.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange( DataSnapshot dataSnapshot ) {
                if ( dataSnapshot.getValue() != null ) {
                    status = dataSnapshot.getValue().toString();
                    worker.setStatus(status);
                    workerPersist.saveInSharedPreferences( worker );
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });

        // If current user is not in "worker" list, call view of sign in
        if ( status != null && status.equals("1") ) {

            // If current user in "worker" list, call services select fragment
            view = inflater.inflate(R.layout.fragment_myservices, container, false);

            // Configure user services listView adapter
            services = new ArrayList<>();
            adapter = new ChooseServiceAdapter( getActivity(), services );

            ListView listView = view.findViewById( R.id.lv_choose_service );
            listView.setAdapter( adapter );

            // Get node "services" from FireBase
            fireBaseServices = FireBaseConfig.getFirebase().child( KEY_SERVICES_NODE );

            // Get progress spinner
            userServicesProgress = view.findViewById( R.id.progress_user_services );

            // Set listener of data change in FireBase
            valueEventListenerServices = new ValueEventListener() {

                /**
                 * Get result of search in FireBase then export to
                 * array ArrayList<Service> ( services ) to serve the adapter to compose the list
                 *
                 * @param dataSnapshot FireBase retrieved data
                 */
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    // Reset services array to comport the new search
                    services.clear();

                    // Iterate retrieved data
                    for (DataSnapshot data : dataSnapshot.getChildren()) {

                        // Get data from FireBase then assign to services objects array
                        Service service = data.getValue( Service.class );
                        services.add( service );

                    }

                    // Notify adapter (update listView)
                    adapter.notifyDataSetChanged();

                    // Hide progress spinner

                    // Get progress
                    userServicesProgress.setVisibility( View.GONE );
                }

                @Override
                public void onCancelled( DatabaseError databaseError ) {}
            };

            // Set action of item list click
            listView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
                /**
                 * On item click, start a intent of service
                 * @param position current item
                 */
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                   // Do something

                }
            } );

        } else {

            view = inflater.inflate(R.layout.fragment_bepartner, container, false);

            // Assign button SignIn
            Button btnSignIn = view.findViewById(R.id.btnSignIn);
            tvPrice = view.findViewById(R.id.tv_price);

            FireBaseConfig.getFirebase().child( KEY_APP_NODE ).addListenerForSingleValueEvent( new ValueEventListener() {
                @Override
                public void onDataChange( DataSnapshot dataSnapshot ) {
                    // Set text views with online content
                    tvPrice.setText( (String) dataSnapshot.child( KEY_PRICE ).child( KEY_PRICE_UNITS ).getValue() );
                }
                @Override
                public void onCancelled( DatabaseError databaseError ) {}
            });

            // Bind click event if button exists
            if ( btnSignIn != null ) {
                btnSignIn.setOnClickListener(new View.OnClickListener() {
                    /**
                     * <h2>Sign In button click event<h2/>
                     * <p>Start Sign In activity then finalize preview activity</p>
                     * @param v: view of button
                     */
                    @Override
                    public void onClick(View v) {
                        // Call intent Sign In Activity
                        Intent i = new Intent( getActivity().getApplicationContext(), SignInActivity.class );
                        startActivity( i );
                    }
                });
            }

        }

        return view;

    }


    /**
     * Open fireBase listener when app is open and this fragment on screen
     */
    @Override
    public void onStart() {
        super.onStart();
        if (fireBaseServices != null && valueEventListenerServices != null) {
            fireBaseServices.addValueEventListener( valueEventListenerServices );
        }
    }

    /**
     * Close fireBase listener when fragment change or app lose focus (minimized)
     */
    @Override
    public void onStop() {
        super.onStop();
        if (fireBaseServices != null && valueEventListenerServices != null) {
            fireBaseServices.removeEventListener( valueEventListenerServices );
        }
    }

}
