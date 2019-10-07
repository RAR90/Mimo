package br.com.mimoapp.mimo.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import br.com.mimoapp.mimo.R;
import br.com.mimoapp.mimo.adapter.TabAdapter;
import br.com.mimoapp.mimo.config.FireBaseConfig;
import br.com.mimoapp.mimo.config.MyApplication;
import br.com.mimoapp.mimo.helper.PermissionsHelper;
import br.com.mimoapp.mimo.model.User;
import br.com.mimoapp.mimo.persistence.UserPersist;
import br.com.mimoapp.mimo.services.FirebaseNotificationServices;
import br.com.mimoapp.mimo.services.MyLocationService;
import br.com.mimoapp.mimo.vendor.SlidingTabLayout;

/**
 * <h1> Content description </h1>
 * <ul>5516991641626
     * <li> FireBase Authentication </li>
     * <li> Start Services </li>
     * <li> Toolbar configuration </li>
     * <li> Get float action button </li>
     * <li> Assign tab vars </li>
     * <li> Set default tab adapter </li>
     * <li> Configure tabs </li>
     * <li> Detect page to show fab only on first fragment </li>
     * <li> Hide keyboard </li>
     * <li> Inflate menu of toolbar </li>
     * <li> Handle menu navigation </li>
 * </ul>
 */

public class MainActivity extends AppCompatActivity {

    private FloatingActionButton fab;
    private SlidingTabLayout slidingTabLayout;

    private String KEY_FCMTOKEN;
    private SharedPreferences.Editor editor;

    private static final String KEY_EXTRA_CHAT_FLAG = "chat_flag";
    private static final String KEY_EXTRA_SERVICES_FLAG = "services_flag";
    private static final String KEY_ISWORKER = "is_worker";


    private boolean connected;
    private Snackbar snackbar;
    private CoordinatorLayout layout;
    private int connectionStatus;
    private ConnectivityManager connectivityManager;


    private LinearLayout connectionBar;
    private TextView textViewConnection;


    private Intent notificationIntent;
    private Intent locationServiceIntent;

    private final String NODE_WORKER = "workers";

    // Permissions
    private String[] appPermissions = new String[] {
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.ACCESS_NETWORK_STATE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        setTheme( R.style.AppTheme );

        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        // Local declarations
        Toolbar mainToolbar;
        ViewPager viewPager;
        FirebaseUser currentUser;
        FirebaseAuth mAuth;
        TabAdapter tabAdapter;

        UserPersist userPersist = new UserPersist( getApplicationContext() );
        User mySelf = userPersist.getUser();

        // PermissionsHelper
        PermissionsHelper.validatePermissions( 1, this, appPermissions );

        // FireBase Authentication
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if ( currentUser == null ) {
            Intent i = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(i);
            finish();
        }

        // Start Services
        notificationIntent = new Intent( getApplicationContext(), FirebaseNotificationServices.class );
        locationServiceIntent = new Intent( getApplicationContext(), MyLocationService.class );

        startService( notificationIntent );
        startService( locationServiceIntent );
        // startService(new Intent(getApplicationContext(), MyFirebaseMessagingService.class));

        // Toolbar configuration
        mainToolbar = findViewById( R.id.main_toolbar );
        mainToolbar.setTitleTextColor(getResources().getColor(R.color.black));
        mainToolbar.setTitle( "Olá, " + mySelf.getName() );
        mainToolbar.setLogo(R.drawable.mimologo);
        setSupportActionBar( mainToolbar );

        // Get float action button
        fab = findViewById( R.id.voiceSearchFab );

        // Assign tab vars
        slidingTabLayout = findViewById( R.id.main_tabs );
        viewPager = findViewById( R.id.vp_pagina );

        // Set default tab adapter
        tabAdapter = new TabAdapter( getSupportFragmentManager() );
        viewPager.setAdapter( tabAdapter );

        // Configure tabs
        slidingTabLayout.setViewPager( viewPager );
        slidingTabLayout.setSelectedIndicatorColors( getResources().getColor(R.color.primaryColor) );

        // Detect page to show fab only on first fragment
        viewPager.addOnPageChangeListener( new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                if ( position == 0 ) {
                    fab.show();
                } else {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    if ( imm != null && getCurrentFocus() != null ) {
                        // Hide keyboard
                        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                    }
                    fab.hide();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        // If this intent are called from notification, open in Mydeals fragment
        // Get bundle of recipient information
        Bundle extra = getIntent().getExtras();

        // Assign recipient information if bundle is not null
        if ( extra != null ) {
            Boolean fromNotification = (Boolean) extra.get(KEY_EXTRA_CHAT_FLAG);
            Boolean fromSignInActivity = (Boolean) extra.get(KEY_EXTRA_SERVICES_FLAG);
            if ( fromNotification != null && fromNotification ) {
                viewPager.setCurrentItem(2);
            }
            if ( fromSignInActivity != null && fromSignInActivity ) {
                viewPager.setCurrentItem(1);
            }
        }
        invalidateOptionsMenu();
    }

    // Inflate menu of toolbar
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {

        UserPersist userPersist = new UserPersist( getApplicationContext() );
        User mySelf = userPersist.getUser();

        if ( mySelf.getId() != null ) {

            // Get fireBase messages node for this user
            FireBaseConfig.getFirebase().child( NODE_WORKER ).child( mySelf.getId() ).addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if ( dataSnapshot.getValue() == null ) {
                        menu.removeItem( R.id.action_mydata );
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            final MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu_main, menu);

            return true;
        } else {
            return false;
        }

    }

    // Handle menu navigation
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch ( item.getItemId() ) {

            case R.id.action_mydata:

                Intent MyAccountActivity = new Intent(MainActivity.this, MyAccountActivity.class);
                MyAccountActivity.putExtra(KEY_ISWORKER, true);
                startActivity(MyAccountActivity);

            return true;

            case R.id.action_about:

                Intent iAboutCompanyActivity = new Intent(MainActivity.this, AboutCompanyActivity.class);
                startActivity(iAboutCompanyActivity);

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // [ start PermissionsHelper treatment ]

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int result : grantResults) {
            if (result == PackageManager.PERMISSION_DENIED) {
                permissionsAlert();
            }
        }
    }

    private void permissionsAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder( this );
        builder.setTitle(R.string.no_permission);
        builder.setMessage(R.string.no_permission_text);
        builder.setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // [ end PermissionsHelper treatment ]

    @Override
    protected void onPause() {
        super.onPause();
        MyApplication.activityPaused();// On Pause notify the Application
    }

    @Override
    protected void onResume() {
        super.onResume();
        MyApplication.activityResumed();// On Resume notify the Application
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}