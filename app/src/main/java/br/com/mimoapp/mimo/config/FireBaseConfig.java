package br.com.mimoapp.mimo.config;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by rafael on 12/10/17.
 */

public final class FireBaseConfig {

    private static DatabaseReference fireBaseReference;

    public static DatabaseReference getFirebase() {

        if (fireBaseReference == null) {
            fireBaseReference = FirebaseDatabase.getInstance().getReference();
        }

        return fireBaseReference;
    }

}
