package br.com.mimoapp.mimo.persistence;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import br.com.mimoapp.mimo.config.FireBaseConfig;
import br.com.mimoapp.mimo.model.User;
import br.com.mimoapp.mimo.model.Worker;

/**
 * Created by rafael on 09/10/17.
 */

public class WorkerPersist {

    private Context appContext;
    private SharedPreferences Preferences;
    private SharedPreferences.Editor editor;

    private Worker worker;

    private int MOD = 0;

    private final String WORKER_PREFERENCES = "user_preferences";

    private final String KEY_WORKERS_NODE = "workers";
    private final String KEY_ID = "id";
    private final String KEY_NAME = "name";
    private final String KEY_LASTNAME = "lastName";
    private final String KEY_BIRTHDATE = "birthDate";
    private final String KEY_EMAIL = "email";
    private final String KEY_GENDER = "gender";
    private final String KEY_STATUS = "status";

    private DatabaseReference fireBaseReference;

    public WorkerPersist(Context myContext) {

        UserPersist userPersist = new UserPersist( myContext );
        User user = userPersist.getUser();

        appContext = myContext;
        Preferences = appContext.getSharedPreferences(KEY_WORKERS_NODE, MOD);
        fireBaseReference = FireBaseConfig.getFirebase().child(KEY_WORKERS_NODE).child( user.getId() );
    }

    public void saveInSharedPreferences( Worker worker ) {

        editor = Preferences.edit();

        editor.putString(KEY_ID, worker.getId());
        editor.putString(KEY_NAME, worker.getName());
        editor.putString(KEY_LASTNAME, worker.getLastName());
        editor.putString(KEY_BIRTHDATE, worker.getBirthDate());
        editor.putString(KEY_EMAIL, worker.getEmail());
        editor.putString(KEY_GENDER, worker.getGender());
        editor.putString(KEY_STATUS, worker.getStatus());

        editor.apply();
    }

    public void saveInFireBase( Worker worker ) {

        fireBaseReference.setValue( worker );

    }

    public Worker getFromFireBase() {


        UserPersist userPersist = new UserPersist( appContext );
        User user = userPersist.getUser();

        fireBaseReference = FireBaseConfig.getFirebase().child(KEY_WORKERS_NODE).child( user.getId() );

        worker = new Worker();

        // Set listener of data change in FireBase
        ValueEventListener valueEventListener = new ValueEventListener() {

            /**
             * Get result of search in FireBase then export to
             * array ArrayList<Service> ( services ) to serve the adapter to compose the list
             * @param dataSnapshot FireBase retrieved data
             */
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                worker = dataSnapshot.getValue( Worker.class );

                // Iterate retrieved data
                //for (DataSnapshot data: dataSnapshot.getChildren() ) {
                    //worker = data.getValue( Worker.class );
                //}

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        fireBaseReference.addValueEventListener( valueEventListener );

        return worker;

    }

    public Worker getFromSharedPreferences() {

        Worker worker = new Worker();
        worker.setId(Preferences.getString(KEY_ID, null));
        worker.setName(Preferences.getString(KEY_NAME, null));
        worker.setLastName(Preferences.getString(KEY_LASTNAME, null));
        worker.setBirthDate(Preferences.getString(KEY_BIRTHDATE, null));
        worker.setEmail(Preferences.getString(KEY_EMAIL, null));
        worker.setGender(Preferences.getString(KEY_GENDER, null));
        worker.setStatus(Preferences.getString(KEY_STATUS, null));

        return worker;
    }

}
