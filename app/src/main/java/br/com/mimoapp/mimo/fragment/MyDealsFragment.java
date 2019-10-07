package br.com.mimoapp.mimo.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import br.com.mimoapp.mimo.R;
import br.com.mimoapp.mimo.activity.ChatActivity;
import br.com.mimoapp.mimo.activity.ServiceActivity;
import br.com.mimoapp.mimo.adapter.ContactAdapter;
import br.com.mimoapp.mimo.adapter.ConversationAdapter;
import br.com.mimoapp.mimo.config.FireBaseConfig;
import br.com.mimoapp.mimo.model.Contact;
import br.com.mimoapp.mimo.model.Conversation;
import br.com.mimoapp.mimo.model.ConversationComparator;
import br.com.mimoapp.mimo.model.User;
import br.com.mimoapp.mimo.persistence.UserPersist;

/**
 * A simple {@link Fragment} subclass.
 */

public class MyDealsFragment extends Fragment {

    private ListView listView;
    private ArrayAdapter<Conversation> adapter;
    private ArrayList<Conversation> conversations;
    //private Query fireBase;

    private String KEY_ID = "id";
    private String KEY_NAME = "name";
    private String KEY_PHONE = "phone";
    private String KEY_DATATIME = "data";
    private String KEY_CONVERSATION_MESSAGE = "conversation";

    private String EXTRA_SERVICE_NAME = "service_name";

    private LinearLayout placeholder;

    private ValueEventListener fireBaseListener;
    private ValueEventListener valueEventListenerContacts;

    private View view;


    private DatabaseReference serviceIdDbReference;
    private String KEY_SERVICE_ID = "service_id";
    private String serviceId;


    public MyDealsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();
        serviceIdDbReference.addValueEventListener( fireBaseListener );
        //fireBase.addValueEventListener( valueEventListenerContacts );
    }

    @Override
    public void onStop() {
        super.onStop();
        serviceIdDbReference.removeEventListener( fireBaseListener );
        //fireBase.removeEventListener( valueEventListenerContacts );
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_mydeals, container, false);
        listView = view.findViewById(R.id.lv_my_deals);

        // Configure placeholder that appear when usr has no messages yet
        placeholder = view.findViewById( R.id.ll_deals_empty );

        // Get my data
        UserPersist userPersist = new UserPersist( getActivity() );
        User myself = userPersist.getUser();

        conversations = new ArrayList<>();
        adapter = new ConversationAdapter( getActivity().getApplicationContext(), conversations );

        // Set reference of main node of messages (child nodes are serviceId)
        serviceIdDbReference = FireBaseConfig.getFirebase()
        .child( KEY_CONVERSATION_MESSAGE )
        .child( myself.getPhone() );

        fireBaseListener = serviceIdDbReference.addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange( DataSnapshot dataSnapshot ) {
                conversations.clear();
                for ( DataSnapshot mDataSnapshot : dataSnapshot.getChildren() ) {
                    for ( DataSnapshot mData : mDataSnapshot.getChildren() ) {
                        Conversation conversation = mData.getValue( Conversation.class );
                        conversations.add( conversation );
                        if ( conversations.size() == 0 ) {
                            placeholder.setVisibility( View.VISIBLE );
                        }else {
                            placeholder.setVisibility( View.GONE );
                        }
                    }
                }
                Collections.sort( conversations, new ConversationComparator() );
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        if ( conversations.size() == 0 ) {
            placeholder.setVisibility( View.VISIBLE );
        }else {
            placeholder.setVisibility( View.GONE );
        }

        // Adapter
        listView.setAdapter( adapter );
        listView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // Create new intent of activity
                Intent i = new Intent( getActivity(), ChatActivity.class );
                Conversation conversation = conversations.get( position );

                // Pass service data to new intent of activity
                i.putExtra( KEY_ID, conversation.getIdRecipient() );
                i.putExtra( KEY_NAME, conversation.getName() );
                i.putExtra( KEY_PHONE, conversation.getPhone() );
                i.putExtra( KEY_SERVICE_ID, conversation.getIdService() );
                i.putExtra( EXTRA_SERVICE_NAME, conversation.getServiceName() );

                // Start activity "service"
                startActivity(i);
            }
        });


        return view;
    }

}
