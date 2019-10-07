package br.com.mimoapp.mimo.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import br.com.mimoapp.mimo.R;
import br.com.mimoapp.mimo.model.Message;
import br.com.mimoapp.mimo.model.User;
import br.com.mimoapp.mimo.persistence.UserPersist;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

/**
 * Created by rafael on 21/10/17.
 */

public class MessageAdapter extends ArrayAdapter<Message> {

    private ArrayList<Message> messages;
    private Context context;

    public MessageAdapter(@NonNull Context c, @NonNull ArrayList<Message> objects) {
        super(c, 0, objects);
        this.messages = objects;
        this.context = c;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = null;

        if ( messages != null ) {

            UserPersist userPersist = new UserPersist( context );
            User user = userPersist.getUser();

            Message message = messages.get( position );

            LayoutInflater inflater = ( LayoutInflater ) context.getSystemService( LAYOUT_INFLATER_SERVICE );

            TextView messageText;

            if ( !user.getPhone().equals( message.getPhone() ) ) {

                view = inflater.inflate( R.layout.item_message_left, parent, false );
                messageText = view.findViewById( R.id.message_left_tv );

            } else {

                view = inflater.inflate(R.layout.item_message_right, parent, false);
                messageText = view.findViewById(R.id.message_right_tv);

            }

            messageText.setText( message.getMessage() );



        }

        return view;
    }
}
