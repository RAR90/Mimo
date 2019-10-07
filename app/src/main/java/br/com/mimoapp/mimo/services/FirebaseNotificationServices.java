package br.com.mimoapp.mimo.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.Html;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import br.com.mimoapp.mimo.R;
import br.com.mimoapp.mimo.activity.ChatActivity;
import br.com.mimoapp.mimo.activity.MainActivity;
import br.com.mimoapp.mimo.model.Notify;
import br.com.mimoapp.mimo.model.User;


/**
 * Created by rafael on 09/11/17.
 */

public class FirebaseNotificationServices extends Service {

    private static final String CHANNEL_ID = "media_playback_channel";
    private static final String TAG = "FirebaseService";
    private static final String KEY_STATUS = "status";
    private static final String KEY_NOTIFICATIONS_NODE = "notifications";
    private static final String KEY_EXTRA_CHAT_FLAG = "chat_flag";

    private FirebaseDatabase mDatabase;
    private FirebaseAuth firebaseAuth;
    private Context context;
    private DatabaseReference fireBaseReference;


    @Override
    public void onCreate() {

        super.onCreate();

        context = this;
        mDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        setupNotificationListener();
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void createChannel() {
        // Get notification manager
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // The id of the channel.
        String id = CHANNEL_ID;
        // The user-visible name of the channel.
        CharSequence name = "Services notifications";
        // The user-visible description of the channel.
        String description = "Services notifications controls";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel mChannel = new NotificationChannel(id, name, importance);
        // Configure the notification channel.
        mChannel.setDescription(description);
        mChannel.setShowBadge(false);
        mChannel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        assert mNotificationManager != null;
        mNotificationManager.createNotificationChannel(mChannel);
    }

    private void setupNotificationListener() {

        // Get current user id

        if ( firebaseAuth.getCurrentUser() != null ) {

            String currentUserId = firebaseAuth.getCurrentUser().getUid();

            // Get notifications node of this user
            fireBaseReference = mDatabase.getReference().child(KEY_NOTIFICATIONS_NODE).child(currentUserId);

            // Create fireBase query to be listen
            Query fireBaseQuery = fireBaseReference.orderByChild(KEY_STATUS).equalTo(0);

            // Add listener to fireBase query
            fireBaseQuery.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    if (dataSnapshot != null) {
                        Notify notify = dataSnapshot.getValue(Notify.class);
                        showNotification(context, notify, dataSnapshot.getKey());
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void showNotification( Context context, Notify notify, String notification_key ){

        // Set vibrate pattern
        long[] v = {500, 1000};

        // Set sound uri
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // You only need to create the channel on API 26+ devices
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel();
        }

        // Create notification builder
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID);

        // Configure notification
        notificationBuilder.setStyle(
            new android.support.v4.media.app.NotificationCompat
            .MediaStyle()
            .setShowCancelButton(true)
            .setCancelButtonIntent(MediaButtonReceiver.buildMediaButtonPendingIntent( context, PlaybackStateCompat.ACTION_STOP ) )
        )
        .setLargeIcon( BitmapFactory.decodeResource( getResources(), R.drawable.mimologotipo ) )
        .setSmallIcon( R.drawable.ic_stat_name )
        .setVisibility( NotificationCompat.VISIBILITY_PUBLIC )
        .setOnlyAlertOnce( true )
        .setContentTitle( notify.getDescription() )
        .setContentText( notify.getMessage() )
        .setVibrate( v )
        .setSubText( notify.getServiceName() )
        .setSound( uri );

        Intent intent;

        /*  Use the notify type to switch activity to stack on the main activity */
        if( notify.getType().equals("chat_activity") ){

            intent = new Intent(context, MainActivity.class);
            intent.putExtra(KEY_EXTRA_CHAT_FLAG, true);

            final PendingIntent pendingIntent = PendingIntent.getActivities(context, 900, new Intent[] {intent}, PendingIntent.FLAG_ONE_SHOT);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addParentStack(MainActivity.class);

            notificationBuilder.setContentIntent(pendingIntent);


            NotificationManager mNotificationManager =  (NotificationManager)context. getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(1, notificationBuilder.build());

            /* Update firebase set notifcation with this key to 1 so it doesnt get pulled by our notify listener*/
            flagNotificationAsSent(notification_key);
        }
    }

    private void flagNotificationAsSent(String notification_key) {
        mDatabase.getReference().child( KEY_NOTIFICATIONS_NODE )
        .child(firebaseAuth.getCurrentUser().getUid())
        .child(notification_key)
        .child( KEY_STATUS )
        .setValue(1);
    }


}
