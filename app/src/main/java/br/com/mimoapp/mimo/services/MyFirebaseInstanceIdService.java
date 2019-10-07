package br.com.mimoapp.mimo.services;

import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by rafael on 06/11/17.
 */

public class MyFirebaseInstanceIdService extends FirebaseInstanceIdService {


    private SharedPreferences Preferences;
    private String USER_PREFERENCES;
    private String KEY_TOKEN;
    private SharedPreferences.Editor editor;

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        sendRegistrationToServer(refreshedToken);
    }

    public void sendRegistrationToServer(String refreshedToken) {
        USER_PREFERENCES = "user_preferences";
        KEY_TOKEN = "fcm_token";
        Preferences = getApplicationContext().getSharedPreferences(USER_PREFERENCES, 0);
        editor.putString(KEY_TOKEN, refreshedToken);
    }
}
