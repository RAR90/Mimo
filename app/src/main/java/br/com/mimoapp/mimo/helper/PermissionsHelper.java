package br.com.mimoapp.mimo.helper;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rafael on 12/10/17.
 */

public class PermissionsHelper {

    public static boolean validatePermissions( int requestCode, Activity activity, String[] permissions ){

        if ( Build.VERSION.SDK_INT >= 23 ) {

            List<String> permissionsList = new ArrayList<String>();

            // Check if necessary permissions has been granted
            for ( String permission : permissions ) {
                Boolean isGranted = ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED;
                if ( !isGranted ) {
                    permissionsList.add(permission);
                }
            }

            // If permissions list is empty, call permissions is unnecessary
            if ( permissionsList.isEmpty() ) {
                return true;
            }

            String[] newPermissions = new String[ permissionsList.size() ];
            permissionsList.toArray( newPermissions );

            // Call permissions
            ActivityCompat.requestPermissions( activity, newPermissions, requestCode );
        }

        return true;

    }

}
