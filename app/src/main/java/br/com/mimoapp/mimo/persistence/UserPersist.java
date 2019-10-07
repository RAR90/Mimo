package br.com.mimoapp.mimo.persistence;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.database.DatabaseReference;

import br.com.mimoapp.mimo.config.FireBaseConfig;
import br.com.mimoapp.mimo.model.User;

/**
 * Created by rafael on 09/10/17.
 */

public class UserPersist {

    private Context appContext;
    private SharedPreferences Preferences;
    private SharedPreferences.Editor editor;

    private int MOD = 0;

    private static final String USER_PREFERENCES = "user_preferences";
    private static final String KEY_USERS_NODE = "users";
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_FCMTOKEN = "fcmToken";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";

    private DatabaseReference fireBaseReference;

    public UserPersist(Context myContext) {

        appContext = myContext;
        Preferences = appContext.getSharedPreferences(USER_PREFERENCES, MOD);
        fireBaseReference = FireBaseConfig.getFirebase();
    }

    public void saveInSharedPreferences( User user ) {

        editor = Preferences.edit();

        editor.putString(KEY_ID, user.getId());
        editor.putString(KEY_NAME, user.getName());
        editor.putString(KEY_PHONE, user.getPhone());

        editor.apply();
    }

    public void saveInFireBase( User user ) {

        fireBaseReference.child(KEY_USERS_NODE).child(user.getPhone()).child(KEY_ID).setValue(user.getId());
        fireBaseReference.child(KEY_USERS_NODE).child(user.getPhone()).child(KEY_NAME).setValue(user.getName());
        fireBaseReference.child(KEY_USERS_NODE).child(user.getPhone()).child(KEY_PHONE).setValue(user.getPhone());
    }

    public User getUser() {

        User user = new User();
        user.setId(Preferences.getString(KEY_ID, null));
        user.setFcmToken(Preferences.getString(KEY_FCMTOKEN, null));
        user.setName(Preferences.getString(KEY_NAME, null));
        user.setPhone(Preferences.getString(KEY_PHONE, null));
        user.setLatitude(Preferences.getString(KEY_LATITUDE, null));
        user.setLongitude(Preferences.getString(KEY_LONGITUDE, null));

        return user;
    }

}
