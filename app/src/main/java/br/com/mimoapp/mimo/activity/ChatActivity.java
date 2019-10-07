package br.com.mimoapp.mimo.activity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.Toast;

import com.codemybrainsout.ratingdialog.RatingDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import br.com.mimoapp.mimo.R;
import br.com.mimoapp.mimo.adapter.MessageAdapter;
import br.com.mimoapp.mimo.config.FireBaseConfig;
import br.com.mimoapp.mimo.model.Conversation;
import br.com.mimoapp.mimo.model.Message;
import br.com.mimoapp.mimo.model.Notify;
import br.com.mimoapp.mimo.model.User;
import br.com.mimoapp.mimo.persistence.UserPersist;

/**
 * <h1> Content description </h1>
 * <ul>
    * <li>FireBase settings</li>
    * <li>Visual components of this view</li>
    * <li>Recipient information</li>
    * <li>Sender information</li>
    * <li>Default key of values</li>
    * <li>Assign inputs</li>
    * <li>Assign list view for messages</li>
    * <li>Get user sender data</li>
    * <li>Get bundle of recipient information</li>
    * <li>Assign recipient information if bundle is not null</li>
    * <li>Configure toolbars</li>
    * <li>Set toolbar (with support layer)</li>
    * <li>Set title to the action bar using recipient name</li>
    * <li>Setup listView and adapter</li>
    * <li>Instantiate custom adapter of messages</li>
    * <li>Get fireBase messages node for this user</li>
    * <li>Get fireBase conversations node for this user</li>
    * <li>Get current time and date</li>
    * <li>Set new conversation</li>
    * <li>Set information about this conversation</li>
    * <li>Scroll to end os list when keyboard is open</li>
    * <li>Setup fireBase listener of data changes in fireBase</li>
    * <li>Clear messages array to prevent duplicate messages</li>
    * <li>Send message event</li>
    * <li>Get message string of EditText</li>
    * <li>Send message if is not empty</li>
    * <li>Prepare message</li>
    * <li>Send message</li>
    * <li>Notification Sender</li>
    * <li>Get FireBase Notification node</li>
    * <li>Create notification object</li>
    * <li>Get pool of notifications</li>
    * <li>Scroll to end of list view chat</li>
    * <li>Select the last row so it will scroll into view</li>
    * <li>Save message on fireBase</li>
    * <li>Get current data and time</li>
    * <li>Select or create "message" node on fireBase</li>
    * <li>Select or create node of sender on fireBase</li>
    * <li>Save message to firebase</li>
 * </ul>
 */

public class ChatActivity extends AppCompatActivity {

    // FireBase settings
    private DatabaseReference fireBaseConversationTo;
    private DatabaseReference fireBaseConversationFor;
    private DatabaseReference fireBase;
    private ValueEventListener valueEventListenerMessage;

    // Visual components of this view
    private Toolbar toolbar;
    private EditText editMessage;
    private ImageButton btSendMessage;
    private ListView listView;
    private ListView myListView;
    private ArrayList<Message> messages;

    private ArrayAdapter<Message> adapter;
    // Recipient information
    private String toId;
    private String toToken;
    private String toName;

    private String toPhone;
    // Sender information
    private String forId;
    private String forName;
    private String serviceName;


    private String forPhone;
    // Default key of values
    private String KEY_ID = "id";
    private String KEY_NAME = "name";
    private String KEY_PHONE = "phone";
    private String KEY_STATUS = "readed";
    private String KEY_DATATIME = "data";
    private String KEY_NODE_MESSAGE = "messages";
    private String KEY_CONVERSATION_MESSAGE = "conversation";
    private String KEY_NOTIFICATION = "chat_activity";

    private String NODE_USER_RATING = "user_rating";

    private RatingBar ratingBar;


    private String KEY_SERVICE_ID = "service_id";
    private String EXTRA_SERVICE_NAME = "service_name";
    private final String EXTRA_WORKER_ID = "worker_id";
    private final String KEY_RATE = "rate";
    private final String NODE_WORKER = "workers";
    private String serviceId;

    private Boolean ret;

    // Inflate menu of toolbar
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {

        // Get fireBase messages node for this user
        FireBaseConfig.getFirebase().child( NODE_WORKER ).child( forId ).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if ( dataSnapshot.getValue() != null ) {
                    menu.removeItem( R.id.action_worker_info );
                    menu.removeItem( R.id.action_worker_qualify );
                } else {
                    final MenuInflater inflater = getMenuInflater();
                    inflater.inflate(R.menu.menu_chat, menu);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);



        setContentView(R.layout.activity_chat);

        // Assign inputs
        btSendMessage = findViewById( R.id.chat_send_btn );
        editMessage = findViewById( R.id.chat_message );

        // Assign list view for messages
        listView = findViewById(R.id.chat_lv);

        // Get user sender data
        UserPersist userPersist = new UserPersist( getApplicationContext() );
        User mySelf = userPersist.getUser();

        forId = mySelf.getId();
        forName = mySelf.getName();
        forPhone = mySelf.getPhone();

        // Get bundle of recipient information
        Bundle extra = getIntent().getExtras();

        // Assign recipient information if bundle is not null
        if ( extra != null ) {
            toId = (String) extra.get(KEY_ID);
            toName = (String) extra.get(KEY_NAME);
            toPhone = (String) extra.get(KEY_PHONE);
            serviceId = (String) extra.get(KEY_SERVICE_ID);
            serviceName = (String) extra.get(EXTRA_SERVICE_NAME);
        }

        // Configure toolbars
        toolbar = findViewById( R.id.chat_toolbar );
        toolbar.setNavigationIcon( R.drawable.ic_arrow_back );

        // Set toolbar (with support layer)
        setSupportActionBar( toolbar );
        ActionBar actionbar = getSupportActionBar();

        // Set title to the action bar using recipient name
        if (actionbar != null) {
            actionbar.setTitle( serviceName );
            actionbar.setSubtitle( toName );
        }

        // Setup listView and adapter
        messages = new ArrayList<>();

        // Instantiate custom adapter of messages
        adapter = new MessageAdapter( getApplicationContext(), messages );
        listView.setAdapter( adapter );

        // Get fireBase messages node for this user
        fireBase = FireBaseConfig.getFirebase().child(KEY_NODE_MESSAGE).child(forPhone).child(serviceId).child(toPhone);

        // Get fireBase conversations node of this user
        fireBaseConversationFor = FireBaseConfig.getFirebase().child(KEY_CONVERSATION_MESSAGE).child(forPhone).child(serviceId).child(toPhone);

        // Get fireBase conversations node of recipient
        fireBaseConversationTo = FireBaseConfig.getFirebase().child(KEY_CONVERSATION_MESSAGE).child(toPhone).child(serviceId).child(forPhone);

        // Set Status readed
        DatabaseReference db = FireBaseConfig.getFirebase().child(KEY_CONVERSATION_MESSAGE).child(forPhone).child(serviceId).child(toPhone).child(KEY_STATUS);
        db.setValue( true );

        // Scroll to end os list when keyboard is open
        KeyboardVisibilityEvent.setEventListener(
                ChatActivity.this,
                new KeyboardVisibilityEventListener() {
                    @Override
                    public void onVisibilityChanged(boolean isOpen) {
                        scrollMyListViewToBottom();
                    }
                });

        // Setup fireBase listener of data changes in fireBase
        valueEventListenerMessage = new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // Clear messages array to prevent duplicate messages
                messages.clear();

                for ( DataSnapshot myData: dataSnapshot.getChildren() ) {
                    Message message = myData.getValue( Message.class );
                    if ( message != null ) {
                        messages.add( message );
                        scrollMyListViewToBottom();
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        fireBase.addValueEventListener( valueEventListenerMessage );

        // Send message event
        btSendMessage.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick( View v ) {

                // Get message string of EditText
                String message_str = editMessage.getText().toString();

                // Send message if is not empty
                if ( !message_str.isEmpty() ) {

                    // Prepare message
                    Message message = new Message();
                    message.setMessage( message_str );
                    message.setPhone( forPhone );

                    //String notificationMessage = message_str.substring(0, 100);
                    //String conversationMessage = message_str.substring(0, 120);

                    String notificationMessage = message_str;
                    String conversationMessage = message_str;

                    // Send message
                    Boolean messageIsSentToSender = sendMessage( forPhone, toPhone, message );
                    if ( messageIsSentToSender ) {
                        Boolean messageIsSentToRecipient = sendMessage( toPhone, forPhone, message );
                        if ( messageIsSentToRecipient ) {

                            editMessage.setText("");

                            // TODO: cortar mensagem antes de inserir na notificação

                            // Send notification to recipient
                            sendNotification(
                                toId,
                                notificationMessage,
                                forName,
                                KEY_NOTIFICATION
                            );

                            // Get current time and date
                            Date currentTime = Calendar.getInstance().getTime();
                            long millis = System.currentTimeMillis() % 1000;

                            // Set new conversation for sender
                            Conversation conversation = new Conversation();

                            conversation.setIdSender( forId );
                            conversation.setIdRecipient( toId );
                            conversation.setIdService( serviceId );
                            conversation.setServiceName( serviceName );
                            conversation.setName( toName );
                            conversation.setPhone( toPhone );

                            // TODO: adicionar trecho da mensagem na classe Conversation para ser usada de thumbnail na listagem
                            conversation.setText( "You : "+ conversationMessage );
                            conversation.setData( currentTime.toString() );
                            conversation.setSort( millis );
                            conversation.setReaded(true);

                            // Set information about this conversation
                            fireBaseConversationFor.setValue( conversation );

                            // Set new conversation for recipient (to appear in deals list)
                            conversation.setIdSender( toId );
                            conversation.setIdRecipient( forId );
                            conversation.setIdService( serviceId );
                            conversation.setServiceName( serviceName );
                            conversation.setName( forName );
                            conversation.setPhone( forPhone );
                            conversation.setText( conversationMessage );
                            conversation.setData( currentTime.toString() );
                            conversation.setSort( millis );
                            conversation.setReaded(false);

                            fireBaseConversationTo.setValue( conversation );

                        } else {
                            Toast.makeText( ChatActivity.this, R.string.message_is_not_sent, Toast.LENGTH_SHORT ).show();
                        }
                    } else {
                        Toast.makeText( ChatActivity.this, R.string.message_is_not_sent, Toast.LENGTH_SHORT ).show();
                    }

                }

            }
        });
    }

    /**
     * Notification Sender
     * @param user_id : user identifier
     * @param message : message to send
     * @param description : description of notification
     * @param type : type of notification
     */
    public void sendNotification(String user_id,String message,String description,String type){

        // Get FireBase Notification node
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("notifications").child(user_id);
        String pushKey = databaseReference.push().getKey();

        // Create notification object
        Notify notify = new Notify();
        notify.setDescription(description);
        notify.setMessage(message);
        notify.setServiceName(serviceName);
        notify.setUser_id(user_id);
        notify.setType(type);

        // Get pool of notifications
        Map<String, Object> forumValues = notify.toMap();
        Map<String, Object> childUpdates = new HashMap<>();

        childUpdates.put(pushKey, forumValues);
        databaseReference.setPriority(ServerValue.TIMESTAMP);
        databaseReference.updateChildren(childUpdates, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if(databaseError == null){

                }
            }
        });
    }

    /**
     * Scroll to end of list view chat
     */
    protected void scrollMyListViewToBottom() {
        myListView = findViewById(R.id.chat_lv);
        myListView.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view
                myListView.setSelection(adapter.getCount() - 1);
            }
        });
    }

    /**
     * Save message on fireBase (send message method)
     *
     * @param sender : phone number of sender device
     * @param recipient : phone number of recipient (value of EditText)
     *
     * TODO: send message to Teclabs server
     */
    private boolean sendMessage(String sender, String recipient, Message message) {
        try {

            // Get current data and time
            String mydate = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());

            // Select or create "message" node on fireBase
            fireBase = FireBaseConfig.getFirebase().child( KEY_NODE_MESSAGE );

            // Select or create messages structure of sender on fireBase
            fireBase = fireBase.child( sender ).child( serviceId ).child( recipient );

            // Create multiples nodes for messages
            fireBase = fireBase.push();

            // Save message to firebase
            fireBase.setValue( message );

            return true;

        } catch ( Exception e ) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        //fireBase.removeEventListener( valueEventListenerMessage );
    }

    private void rating() {

        AppCompatDelegate.setCompatVectorFromResourcesEnabled( true );

        Drawable upArrow = ContextCompat.getDrawable(this, R.drawable.ic_thumbs_up_down);



        final RatingDialog ratingDialog = new RatingDialog.Builder(this)

            .icon( upArrow )

            .formSubmitText(getString(R.string.rate))
            .formCancelText(getString(R.string.cancel))
            .positiveButtonText(getString(R.string.done))

            .feedbackTextColor( R.color.primaryColor )
            .title("How was your experience with "+toName+" ?")
            .negativeButtonBackgroundColor( R.color.primaryLighterColor )
            .negativeButtonTextColor( R.color.primaryColor )

            .positiveButtonTextColor( R.color.primaryLighterColor )
            .positiveButtonBackgroundColor( R.color.primaryColor )
            .titleTextColor( R.color.primaryColor )

            .ratingBarColor( R.color.primaryColor )
            .ratingBarBackgroundColor( R.color.primaryLighterColor )

            .onThresholdCleared(new RatingDialog.Builder.RatingThresholdClearedListener() {
                @Override
                public void onThresholdCleared(RatingDialog ratingDialog, float rating, boolean thresholdCleared) {}
            })
                .onThresholdFailed(new RatingDialog.Builder.RatingThresholdFailedListener() {
                    @Override
                    public void onThresholdFailed(RatingDialog ratingDialog, float rating, boolean thresholdCleared) {}
                })
                .onRatingChanged(new RatingDialog.Builder.RatingDialogListener() {
                    @Override
                    public void onRatingSelected(float rating, boolean thresholdCleared) {

                        // Select or create "message" node on fireBase
                        fireBase = FireBaseConfig.getFirebase().child( NODE_USER_RATING ).child( toId ).child( forId ).child( KEY_RATE );
                        fireBase.setValue( rating );

                    }
                })
                .onRatingBarFormSumbit(new RatingDialog.Builder.RatingDialogFormListener() {
                    @Override
                    public void onFormSubmitted(String feedback) {
                        Toast.makeText(ChatActivity.this, "Você avaliou "+toName+" !", Toast.LENGTH_SHORT).show();
                    }
                }).build();



        ratingDialog.show();

        ratingBar = ratingDialog.getRatingBarView();

        fireBase = FireBaseConfig.getFirebase().child( NODE_USER_RATING ).child( toId ).child( forId ).child( KEY_RATE );
        fireBase.addListenerForSingleValueEvent( new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if ( dataSnapshot.getValue() != null ) {
                    String str_rate = dataSnapshot.getValue().toString();
                    float flt_rate = Float.parseFloat( str_rate );
                    if ( ratingBar != null ) {
                        ratingBar.setRating( flt_rate );
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        ratingDialog.getPositiveButtonTextView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ChatActivity.this, "Você avaliou " + toName + "com " + (int) ratingBar.getRating() + " estrelas!", Toast.LENGTH_SHORT).show();
                ratingDialog.dismiss();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch ( item.getItemId() ) {
            case R.id.action_worker_info :
                Intent intent = new Intent( ChatActivity.this, WorkerDetailsActivity.class );
                intent.putExtra( EXTRA_WORKER_ID, toId );
                intent.putExtra( EXTRA_SERVICE_NAME, serviceName );
                startActivity( intent );
            return true;
            case R.id.action_worker_qualify :

                rating();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
