package br.com.mimoapp.mimo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import br.com.mimoapp.mimo.R;
import br.com.mimoapp.mimo.config.FireBaseConfig;
import br.com.mimoapp.mimo.model.User;
import br.com.mimoapp.mimo.model.Worker;
import br.com.mimoapp.mimo.vendor.CircularTransform;

public class MyAccountActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ActionBar actionbar;
    private StorageReference storageReference;
    private DatabaseReference fireBase;

    private ImageView ivProfileImage;
    private TextView tvMyName;
    private TextView tvMyBirth;
    private TextView tvMyEmail;
    private TextView tvMyStreet;
    private TextView tvMyCity;
    private TextView tvMyPlan;

    private static final String KEY_USERS_NODE = "users";
    private static final String KEY_WORKERS_NODE = "workers";
    private static final String KEY_ISWORKER = "is_worker";
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_FCMTOKEN = "fcmToken";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_account);

        tvMyName = findViewById(R.id.tvMyName);
        tvMyBirth = findViewById(R.id.tvMyBirth);
        tvMyEmail = findViewById(R.id.tvMyEmail);
        tvMyStreet = findViewById(R.id.tvMyStreet);
        tvMyCity = findViewById(R.id.tvMyCity);
        tvMyPlan = findViewById(R.id.tvMyPlan);

        Button btProfileImage = findViewById(R.id.btProfileImage);
        Button btMyData = findViewById(R.id.btMyData);
        Button btMyPlan = findViewById(R.id.btMyPlan);

        // Configure toolbars
        toolbar = findViewById(R.id.main_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);

        // Set toolbar (with support layer)
        setSupportActionBar(toolbar);
        actionbar = getSupportActionBar();
        actionbar.setTitle("Meus dados");

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if ( currentUser == null ) {
            Intent i = new Intent(MyAccountActivity.this, LoginActivity.class);
            startActivity(i);
            finish();
        }

        final String uId = currentUser.getUid();


        ivProfileImage = findViewById(R.id.ivProfileImage);
        storageReference = FirebaseStorage.getInstance().getReference().child("images/").child( uId );

        // Load the image using Glide
        Glide.with(getApplicationContext())
        .using(new FirebaseImageLoader())
        .load(storageReference)
        .diskCacheStrategy(DiskCacheStrategy.NONE)
        .listener(new RequestListener<StorageReference, GlideDrawable>() {
            @Override
            public boolean onException(Exception e, StorageReference model, Target<GlideDrawable> target, boolean isFirstResource) {
                ivProfileImage.setImageResource(R.drawable.circle_placeholder);
                return false;
            }

            @Override
            public boolean onResourceReady(GlideDrawable resource, StorageReference model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                return false;
            }
        })
        .transform(new CircularTransform(getApplicationContext()))
        .into(ivProfileImage);

        btProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent iUserImageUploadActivity = new Intent(MyAccountActivity.this, UserImageUploadActivity.class);
                iUserImageUploadActivity.putExtra(KEY_ISWORKER, true);
                startActivity(iUserImageUploadActivity);
            }
        });

        btMyData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent MyAccountActivity = new Intent(MyAccountActivity.this, SignInActivity.class);
                MyAccountActivity.putExtra(KEY_ISWORKER, true);
                startActivity(MyAccountActivity);
            }
        });

        DatabaseReference WorkerFbRef = FireBaseConfig.getFirebase().child(KEY_WORKERS_NODE).child( uId );

        WorkerFbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    Worker myWorker = dataSnapshot.getValue( Worker.class );
                    tvMyName.setText( myWorker.getName() );
                    tvMyBirth.setText( myWorker.getBirthDate() );
                    tvMyEmail.setText( myWorker.getEmail() );
                    tvMyStreet.setText( "Rua nome exemplo, 000" );
                    tvMyCity.setText( "Ribeirão Preto - SP" );
                    tvMyPlan.setText( "PLANO ELITE" );

                } else {

                    DatabaseReference UserFbRef = FireBaseConfig.getFirebase().child(KEY_USERS_NODE).child( uId );

                    // Set listener of data change in FireBase
                    UserFbRef.addListenerForSingleValueEvent( new ValueEventListener() {

                        /**
                         * Get result of search in FireBase then export to
                         * array ArrayList<Service> ( services ) to serve the adapter to compose the list
                         * @param dataSnapshot FireBase retrieved data
                         */
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            User user = dataSnapshot.getValue( User.class );

                            //TODO: Incluir na classe User todos os dados do Worker
                            tvMyName.setText( user.getName() );
//                            tvMyBirth.setText();
//                            tvMyEmail.setText();
//                            tvMyStreet.setText();
//                            tvMyCity.setText();
//                            tvMyPlan.setText();

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

}
