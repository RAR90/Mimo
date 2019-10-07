package br.com.mimoapp.mimo.persistence;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import br.com.mimoapp.mimo.config.FireBaseConfig;
import br.com.mimoapp.mimo.model.Contact;
import br.com.mimoapp.mimo.model.User;

/**
 * Created by rafael on 21/10/17.
 */

public class ContactPersist {

    private Contact contact;

    private Context appContext;
    private SharedPreferences Preferences;
    private SharedPreferences.Editor editor;

    private int MOD = 0;

    private String USER_PREFERENCES = "user_preferences";

    private String KEY_USERS_NODE = "users";
    private String KEY_CONTACT_NODE = "contact";
    private String KEY_ID = "id";
    private String KEY_NAME = "name";
    private String KEY_PHONE = "phone";

    private DatabaseReference fireBaseReference;

    private UserPersist userPersist;


    public ContactPersist(Context myContext) {

        appContext = myContext;
        Preferences = appContext.getSharedPreferences(USER_PREFERENCES, MOD);
        fireBaseReference = FireBaseConfig.getFirebase();
    }

    public Contact saveContact(String phone) {

        Contact contact = this.contact;
        final String STR_PHONE = phone;

        userPersist = new UserPersist( appContext );

        fireBaseReference.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Contact contact = new Contact();
                User mySelf = userPersist.getUser();

                if ( dataSnapshot.child( KEY_USERS_NODE ).child( STR_PHONE ).getValue() != null ) {

                    User user = dataSnapshot.child( KEY_USERS_NODE ).child( STR_PHONE ).getValue( User.class );

                    contact.setId( user.getId() );
                    contact.setName( user.getName() );
                    contact.setPhone( user.getPhone() );

                    fireBaseReference.child( KEY_CONTACT_NODE ).child( mySelf.getPhone() ).child( user.getPhone() ).setValue( contact );

                } else {

                    Toast.makeText( appContext, "This is not a user number", Toast.LENGTH_SHORT ).show();
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });


        return contact;
    }



}
