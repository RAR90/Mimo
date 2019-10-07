package br.com.mimoapp.mimo.services;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import br.com.mimoapp.mimo.config.FireBaseConfig;
import br.com.mimoapp.mimo.model.User;
import br.com.mimoapp.mimo.persistence.UserPersist;

/**
 * Created by rafael on 28/11/17.
 */

public class MyLocationService extends Service {

    private static final String TAG = "MyLocationService";
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 10f;
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";

    private LocationManager mLocationManager = null;
    private DatabaseReference fireBase;
    private User mySelf;

    private Context appContext;
    private SharedPreferences Preferences;
    private SharedPreferences.Editor editor;

    private int MOD = 0;
    private String USER_PREFERENCES = "user_preferences";

    private class LocationListener implements android.location.LocationListener {

        Location mLastLocation;

        public LocationListener(String provider) {
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(final Location location) {
            UpdateLocationInFireBase(location);
            UpdateLocationInSharedPreferences(location);
            mLastLocation.set(location);
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }

    public void UpdateLocationInSharedPreferences( final Location location ) {

        appContext = getApplicationContext();
        Preferences = appContext.getSharedPreferences(USER_PREFERENCES, MOD);

        editor = Preferences.edit();

        editor.putString( KEY_LATITUDE, String.valueOf(location.getLatitude()) );
        editor.putString( KEY_LONGITUDE, String.valueOf(location.getLongitude()) );

        editor.apply();
    }

    public void UpdateLocationInFireBase( final Location location ) {

        // Get user sender data
        UserPersist userPersist = new UserPersist( getApplicationContext() );
        mySelf = userPersist.getUser();

        // Select or create "message" node on fireBase
        fireBase = FireBaseConfig.getFirebase().child( "service_users" );
        fireBase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange( DataSnapshot dataSnapshot ) {
                for ( DataSnapshot data : dataSnapshot.getChildren() ) {
                    fireBase = fireBase.child( data.getKey() ).child( mySelf.getId() );
                    if ( fireBase.getKey() != null ) {

                        //fireBase.child("latitude").setValue( location.getLatitude() );
                        //fireBase.child("longitude").setValue( location.getLongitude() );

                        fireBase = FireBaseConfig.getFirebase().child( "user_location" ).child( mySelf.getId() );
                        fireBase.child("latitude").setValue( location.getLatitude() );
                        fireBase.child("longitude").setValue( location.getLongitude() );

                        fireBase = FireBaseConfig.getFirebase().child( "service_users" );
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MyLocationService.this, "Location cannot be uploaded", Toast.LENGTH_SHORT).show();
            }
        });
    }

    LocationListener[] mLocationListeners = new LocationListener[]{
        new LocationListener(LocationManager.GPS_PROVIDER),
        new LocationListener(LocationManager.NETWORK_PROVIDER),
        new LocationListener(LocationManager.PASSIVE_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.PASSIVE_PROVIDER,
                    LOCATION_INTERVAL,
                    LOCATION_DISTANCE,
                    mLocationListeners[0]
            );
        } catch (SecurityException | IllegalArgumentException ignored) {}
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mLocationManager != null) {
            for (LocationListener mLocationListener : mLocationListeners) {
                try {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    mLocationManager.removeUpdates(mLocationListener);
                } catch (Exception ignored) {}
            }
        }
    }

    private void initializeLocationManager() {
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }
}