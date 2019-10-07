package br.com.mimoapp.mimo.persistence;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import br.com.mimoapp.mimo.config.FireBaseConfig;
import br.com.mimoapp.mimo.model.App;
import br.com.mimoapp.mimo.model.User;
import br.com.mimoapp.mimo.model.Worker;

/**
 * Created by rafael on 09/10/17.
 */

public class AppPersist {

    private App app;
    private Context appContext;
    private SharedPreferences Preferences;
    private SharedPreferences.Editor editor;
    private DatabaseReference fireBaseReference;

    private final String KEY_APP_NODE = "app";
    private final String KEY_PRICE = "price";
    private final String KEY_PRICE_UNITS = "units";
    private final String KEY_PRICE_CENTS = "cents";
    private final String KEY_ABOUT = "about";
    private final String KEY_ABOUT_TITLE = "title";
    private final String KEY_ABOUT_TEXT = "text";

    public AppPersist( Context myContext ) {
        appContext = myContext;
        Preferences = appContext.getSharedPreferences( KEY_APP_NODE, 0 );
        fireBaseReference = FireBaseConfig.getFirebase().child( KEY_APP_NODE );
    }

    public void saveInSharedPreferences( App app ) {
        editor = Preferences.edit();
        editor.putString( KEY_PRICE_UNITS, app.getPriceUnits() );
        editor.putString( KEY_PRICE_CENTS, app.getPriceCents() );
        editor.putString( KEY_ABOUT_TITLE, app.getAboutTitle() );
        editor.putString( KEY_ABOUT_TEXT, app.getAboutText() );
        editor.apply();
    }

    public App getFromSharedPreferences() {
        app = new App();
        app.setAboutTitle( Preferences.getString( KEY_ABOUT_TITLE, null) );
        app.setAboutText( Preferences.getString( KEY_ABOUT_TEXT, null) );
        app.setPriceUnits( Preferences.getString( KEY_PRICE_UNITS, null) );
        app.setPriceCents( Preferences.getString( KEY_PRICE_CENTS, null) );
        return app;
    }
}
