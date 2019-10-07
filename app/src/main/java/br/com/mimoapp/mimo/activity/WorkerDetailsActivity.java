package br.com.mimoapp.mimo.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import br.com.mimoapp.mimo.R;
import br.com.mimoapp.mimo.config.FireBaseConfig;
import br.com.mimoapp.mimo.model.Worker;
import br.com.mimoapp.mimo.vendor.CircularTransform;

public class WorkerDetailsActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ActionBar actionbar;

    private String workerId;
    private String serviceName;

    private Worker worker;

    private final String EXTRA_WORKER_ID = "worker_id";
    private final String EXTRA_SERVICE_NAME = "service_name";
    private final String WORKER_NODE = "workers";

    private StorageReference storageReference;
    private DatabaseReference fireBase;
    private DatabaseReference fireBaseLocation;

    private ImageView ivWorkerImage;

    private LatLng latLng;
    private LatLngBounds latLngBounds;

    private GoogleMap map;
    private SupportMapFragment mapFragment;

    private TextView tvWorkerName;

    private String NODE_USER_RATING = "user_rating";

    private int rateCount = 0;
    private int rateSum = 0;
    private int rateMed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_details);

        // Configure toolbars
        toolbar = findViewById(R.id.chat_toolbar);

        // Set toolbar (with support layer)
        setSupportActionBar(toolbar);
        actionbar = getSupportActionBar();

        // Get worker id of previous activity
        Bundle extra = getIntent().getExtras();
        if (extra != null) {
            workerId = (String) extra.get(EXTRA_WORKER_ID);
            serviceName = (String) extra.get(EXTRA_SERVICE_NAME);
        }

        // Get outputs
        tvWorkerName = findViewById( R.id.tv_worker_name );

        // Get worker data
        fireBase = FireBaseConfig.getFirebase().child(WORKER_NODE).child(workerId);
        fireBase.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                worker = dataSnapshot.getValue(Worker.class);

                // Set title to the action bar using recipient name
                if (actionbar != null) {
                    actionbar.setTitle(worker.getName());
                    actionbar.setSubtitle(serviceName);
                }

                storageReference = FirebaseStorage.getInstance().getReference().child("images/").child(worker.getId());

                ivWorkerImage = findViewById(R.id.iv_worker_image);

                // Load the image using Glide
                Glide.with(getApplicationContext())
                    .using(new FirebaseImageLoader())
                    .load(storageReference)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .listener(new RequestListener<StorageReference, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, StorageReference model, Target<GlideDrawable> target, boolean isFirstResource) {
                            ivWorkerImage.setImageResource(R.drawable.circle_placeholder);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, StorageReference model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            return false;
                        }
                    })
                    .transform(new CircularTransform(getApplicationContext()))
                    .into(ivWorkerImage);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        fireBase = FireBaseConfig.getFirebase().child( "user_location" ).child( workerId );
        fireBase.addListenerForSingleValueEvent( new ValueEventListener() {
            @Override
            public void onDataChange( DataSnapshot dataSnapshot ) {

                double lat = dataSnapshot.child("latitude").getValue() != null ? (double) dataSnapshot.child("latitude").getValue() : 0;
                double lon = dataSnapshot.child("longitude").getValue() != null ? (double) dataSnapshot.child("longitude").getValue() : 0;

                latLng = new LatLng( lat, lon );

                latLngBounds = new LatLngBounds( latLng, latLng );

                // Map marker
                mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mf_worker_location);
                mapFragment.getMapAsync(new OnMapReadyCallback() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onMapReady(GoogleMap googleMap) {
                        googleMap.setMyLocationEnabled(true);
                        googleMap.addMarker( new MarkerOptions()
                                .position( latLng )
                                .title( worker.getName()) );
                        googleMap.setLatLngBoundsForCameraTarget( latLngBounds );
                        googleMap.setMinZoomPreference( 14 );
                        mapFragment.onResume();
                    }
                });
                //String workerFullName = worker.getName() + " " + worker.getLastName();
                String workerFullName = worker.getName() + " " + worker.getLastName();
                // TODO: Puxar rating aqui
                tvWorkerName.setText( workerFullName );
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        fireBase = FireBaseConfig.getFirebase().child( NODE_USER_RATING ).child( workerId );
        fireBase.addListenerForSingleValueEvent( new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {



                for ( DataSnapshot mData: dataSnapshot.getChildren() ) {
                    rateCount++;
                    String rate = mData.child("rate").getValue().toString();
                    rateSum += Integer.parseInt(rate);

                    rateMed = rateSum / rateCount;

                    RatingBar ratingBar = findViewById( R.id.rb_worker );
                    ratingBar.setRating( rateMed );
                    ratingBar.setNumStars( 5 );
                    ratingBar.setIsIndicator( true );

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



    }
}
