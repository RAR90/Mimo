package br.com.mimoapp.mimo.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import br.com.mimoapp.mimo.R;
import br.com.mimoapp.mimo.config.FireBaseConfig;
import br.com.mimoapp.mimo.model.App;
import br.com.mimoapp.mimo.persistence.AppPersist;

public class AboutCompanyActivity extends AppCompatActivity {

    private TextView tvAboutTitle;
    private TextView tvAboutText;

    private App app;
    private AppPersist appPersist;

    private final String KEY_APP_NODE = "app";
    private final String KEY_ABOUT = "about";
    private final String KEY_ABOUT_TITLE = "title";
    private final String KEY_ABOUT_TEXT = "text";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_about_company);

        // Get textViews
        tvAboutTitle = findViewById( R.id.tv_about_title );
        tvAboutText = findViewById( R.id.tv_about_text );

        // Get app persistence object
        appPersist = new AppPersist( getApplicationContext() );

        // Get app data from shared preferences
        app = appPersist.getFromSharedPreferences();

        // Bind text view with data
        tvAboutTitle.setText( app.getAboutTitle() );
        tvAboutText.setText( app.getAboutText() );

        FireBaseConfig.getFirebase().child( KEY_APP_NODE ).addListenerForSingleValueEvent( new ValueEventListener() {
            @Override
            public void onDataChange( DataSnapshot dataSnapshot ) {

                app.setAboutTitle( dataSnapshot.child( KEY_ABOUT ).child( KEY_ABOUT_TITLE ).getValue().toString() );
                app.setAboutText( (String) dataSnapshot.child( KEY_ABOUT ).child( KEY_ABOUT_TEXT ).getValue() );

                // Set text views with online content
                tvAboutTitle.setText( app.getAboutTitle() );
                tvAboutText.setText( app.getAboutText() );

                // Save online data in Shared Preferences
                appPersist.saveInSharedPreferences( app );
            }
            @Override
            public void onCancelled( DatabaseError databaseError ) {}
        });

        // Set default toolbar
        Toolbar toolbar = findViewById(R.id.main_toolbar);
        toolbar.setTitle( app.getAboutTitle() );
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        setSupportActionBar( toolbar );



    }
}
