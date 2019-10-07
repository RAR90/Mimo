package br.com.mimoapp.mimo.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import br.com.mimoapp.mimo.R;
import br.com.mimoapp.mimo.model.User;
import br.com.mimoapp.mimo.vendor.CircularTransform;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

/**
 * Created by rafael on 31/10/17.
 */

public class ServiceUsersAdapter extends ArrayAdapter<User> {

    private ArrayList<User> users;
    private Context context;
    private ImageView userImage;

    //Firebase
    StorageReference storageReference;

    /**
     * Default constructor
     * @param c Context of Activity
     * @param objects Array of "Service" objects
     */
    public ServiceUsersAdapter(@NonNull Context c, @NonNull ArrayList<User> objects) {
        super(c, 0, objects);
        this.users = objects;
        this.context = c;
    }

    /**
     * Assign content of Service array to outputs (TextViews or image views)
     * @param position position of current service object from array of services
     * @return view object
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View view = null;

        if ( users != null ) {

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.service_user_list_item, parent, false);

            // TODO: Configurar corretamente o nome dos textviews
            TextView userName = view.findViewById(R.id.tv_user_name);
            TextView userPhone = view.findViewById(R.id.tv_user_phone);
            userImage = view.findViewById(R.id.iv_user_image);

            User user = users.get(position);
            userName.setText(user.getName());
            userPhone.setText(user.getPhone());

            userImage.setScaleType( ImageView.ScaleType.FIT_CENTER );

            storageReference = FirebaseStorage.getInstance().getReference().child("images/").child( user.getId() );

            // Load the image using Glide
            Glide.with(context)
            .using(new FirebaseImageLoader())
            .load(storageReference)
            .placeholder( R.drawable.circle_placeholder )
            .diskCacheStrategy( DiskCacheStrategy.NONE )
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

            userImage.setImageResource(R.drawable.circle_placeholder);
        }

        return view;
    }
}
