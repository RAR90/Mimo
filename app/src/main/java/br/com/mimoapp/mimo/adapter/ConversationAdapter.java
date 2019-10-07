package br.com.mimoapp.mimo.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.target.Target;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import javax.sql.DataSource;

import br.com.mimoapp.mimo.R;
import br.com.mimoapp.mimo.model.Conversation;
import br.com.mimoapp.mimo.vendor.CircularTransform;

/**
 * Created by rafael on 21/10/17.
 */

public class ConversationAdapter extends ArrayAdapter<Conversation> {

    private ArrayList<Conversation> conversations;
    public Context context;
    private ImageView userImage;


    //Firebase
    StorageReference storageReference;

    public ConversationAdapter(@NonNull Context c, @NonNull ArrayList<Conversation> objects) {
        super(c, 0, objects);
        this.conversations = objects;
        this.context = c;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View view = null;

        if ( conversations != null ) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.mydeals_list_item, parent, false);
            userImage = view.findViewById(R.id.iv_user_image_conversation);
            TextView dealerNewMessage = view.findViewById(R.id.newMessageFlag);
            TextView dealerName = view.findViewById(R.id.dealer_name);
            TextView dealerService = view.findViewById(R.id.dealer_phone);
            TextView dealerText = view.findViewById(R.id.dealer_text);
            dealerNewMessage.setVisibility(LinearLayout.GONE);
            final Conversation conversation = conversations.get( position );
            if (!conversation.getReaded()) {
                dealerText.setTypeface(null, Typeface.BOLD);
                dealerNewMessage.setVisibility(LinearLayout.VISIBLE);
            }
            dealerName.setText(conversation.getServiceName());
            dealerService.setText(conversation.getName());
            dealerText.setText(conversation.getText());
            storageReference = FirebaseStorage.getInstance().getReference().child("images/").child( conversation.getIdRecipient() );

            // Load the image using Glide
            Glide.with(context)
            .using(new FirebaseImageLoader())
            .load(storageReference)
            .placeholder(R.drawable.circle_placeholder)
            .error(R.drawable.circle_placeholder)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .listener(new RequestListener<StorageReference, GlideDrawable>() {
                @Override
                public boolean onException(Exception e, StorageReference model, Target<GlideDrawable> target, boolean isFirstResource) {
                    userImage.setImageResource(R.drawable.circle_placeholder);
                    return false;
                }
                @Override
                public boolean onResourceReady(GlideDrawable resource, StorageReference model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                    return false;
                }
            })
            .transform(new CircularTransform(context))
            .into(userImage);

        }

        return view;
    }
}