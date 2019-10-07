package br.com.mimoapp.mimo.activity;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import br.com.mimoapp.mimo.R;
import br.com.mimoapp.mimo.adapter.ServiceUsersAdapter;
import br.com.mimoapp.mimo.config.FireBaseConfig;
import br.com.mimoapp.mimo.fragment.WorkersFilterDialogFragment;
import br.com.mimoapp.mimo.model.User;
import br.com.mimoapp.mimo.persistence.UserPersist;

public class ServiceActivity extends AppCompatActivity {

    private ProgressBar serviceUsersProgress;

    private ListView listView;
    private ArrayAdapter<User> adapter;
    private ArrayList<User> users;

    private FloatingActionButton fab;

    private FirebaseUser currentUser;
    private UserPersist userPersist;

    private TextView FerasCountTv;
    private User mySelf;

    private DatabaseReference fireBase;

    private String KEY_SERVICE_USERS_NODE = "service_users";
    private String KEY_USERS_NODE = "users";


    private String KEY_SERVICE_ID = "service_id";
    private String KEY_SERVICE_NAME = "name";
    private String KEY_SERVICE_DESCRIPTION = "description";
    private String serviceName;
    private String EXTRA_SERVICE_NAME = "service_name";

    private final String NODE_WORKERS = "workers";

    private ValueEventListener valueEventListenerServices;

    private String KEY_ID = "id";
    private String KEY_NAME = "name";
    private String KEY_PHONE = "phone";

    private String serviceId;

    private String idOfUserInDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setTheme(R.style.AppTheme);


        super.onCreate(savedInstanceState);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        setContentView(R.layout.activity_service);

        // Get progress spinner
        serviceUsersProgress = findViewById(R.id.service_progress_spinner);

        // Get FAB
        fab = findViewById( R.id.fab_service );

        // FireBase Authentication
        FirebaseAuth mAuth;

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Get user sender data
        userPersist = new UserPersist( getApplicationContext() );
        mySelf = userPersist.getUser();

        if ( currentUser == null ) {
            Intent iLogin = new Intent(ServiceActivity.this, LoginActivity.class);
            startActivity(iLogin);
            finish();
        }

        // Set default toolbar
        Toolbar toolbar = findViewById(R.id.main_toolbar);
        toolbar.setTitle(R.string.page_name_service);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        setSupportActionBar( toolbar );

        // Get created toolbar
        ActionBar actionBar = getSupportActionBar();

        // Get data of previus intent
        Intent intent = getIntent();
        serviceId = intent.getStringExtra(KEY_SERVICE_ID);
        serviceName = intent.getStringExtra(KEY_SERVICE_NAME);
        String serviceDescription = intent.getStringExtra(KEY_SERVICE_DESCRIPTION);

        // Get layout output components
        ConstraintLayout ServiceCardCl = findViewById( R.id.cl_service_card );
        TextView ServiceNameTv = findViewById( R.id.tv_service_name );
        TextView ServiceDescriptionTv = findViewById( R.id.tv_service_description );
        TextView FerasLabelTv = findViewById( R.id.tv_feras_label );

        // Get list view
        listView = findViewById(R.id.lv_service_users);

        // Setup service card
        ServiceNameTv.setText( serviceName );
        ServiceDescriptionTv.setText( serviceDescription );

        // Configure services listView adapter
        users = new ArrayList<>();
        adapter = new ServiceUsersAdapter( getApplicationContext(), users );
        listView.setAdapter( adapter );

        // Get node "service_users : users " from FireBase
        fireBase = FireBaseConfig.getFirebase()
                .child( KEY_SERVICE_USERS_NODE )
                .child( serviceId );

        // Set listener of data change in FireBase
        valueEventListenerServices = new ValueEventListener() {

            /**
             * Get result of search in FireBase then export to
             * array ArrayList<User> ( users ) to serve the adapter to compose the list
             * @param dataSnapshot FireBase retrieved data
             */
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                int intUsersCount;
                String usersCount;

                // Reset services array to comport the new search
                users.clear();

                // Get count of user of this service then set the output
                FerasCountTv = findViewById( R.id.tv_feras_count );
                User user = new User();


                // Iterate retrieved data
                for (DataSnapshot data: dataSnapshot.getChildren() ) {

                    if ( data.child("id").getValue() != null ) {
                        String myId = mySelf.getId();
                        idOfUserInDb = data.child("id").getValue().toString();
                        if ( !idOfUserInDb.equalsIgnoreCase(myId) ) {
                            user.setId( (String) data.child("id").getValue() );
                            user.setName( (String) data.child("name").getValue() );
                            user.setPhone( (String) data.child("phone").getValue() );
                            users.add( user );
                        }
                    }
                }

                intUsersCount = users.size();
                usersCount = String.valueOf( intUsersCount );
                FerasCountTv.setText( usersCount );

                // Notify adapter (update listView)
                adapter.notifyDataSetChanged();

                // Hide progress spinner
                serviceUsersProgress.setVisibility(View.GONE);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };

        // On click in list item
        listView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            /**
             * On item click, start a intent of service
             * @param position current item
             */
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // Create new intent of activity
                Intent i = new Intent(ServiceActivity.this, ChatActivity.class);
                User user = users.get( position );

                // Pass service data to new intent of activity
                i.putExtra(KEY_ID, user.getId());
                i.putExtra(KEY_NAME, user.getName());
                i.putExtra(KEY_PHONE, user.getPhone());
                i.putExtra(KEY_SERVICE_ID, serviceId);
                i.putExtra(EXTRA_SERVICE_NAME, serviceName);

                // Start activity "service"
                startActivity(i);

            }
        } );

        // FAB event listener
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showWorkersFilterDialog();
            }
        });
    }


    void showWorkersFilterDialog() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        DialogFragment newFragment = WorkersFilterDialogFragment.newInstance();
        newFragment.show( getSupportFragmentManager(), "dialog" );
    }

    /**
     * Open fireBase listener when app is open and this fragment on screen
     */
    @Override
    public void onStart() {
        super.onStart();
        fireBase.addValueEventListener( valueEventListenerServices );
    }

    /**
     * Close fireBase listener when fragment change or app lose focus (minimized)
     */
    @Override
    public void onStop() {
        super.onStop();
        fireBase.removeEventListener( valueEventListenerServices );
    }

}
