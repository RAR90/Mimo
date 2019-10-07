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
import br.com.mimoapp.mimo.model.Contact;
import br.com.mimoapp.mimo.model.Service;
import br.com.mimoapp.mimo.vendor.CircularTransform;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

/**
 * List adapter for Service model class
 */
public class ServiceAdapter extends ArrayAdapter<Service> {

    private ArrayList<Service> services;
    private Context context;

    /**
     * Default constructor
     * @param c Context of Activity
     * @param objects Array of "Service" objects
     */
    public ServiceAdapter(@NonNull Context c, @NonNull ArrayList<Service> objects) {
        super(c, 0, objects);
        this.services = objects;
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

        if ( services != null ) {

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.service_list_item, parent, false);

            // TODO: Configurar corretamente o nome dos textviews
            final TextView serviceName = view.findViewById(R.id.service_name);
            final TextView servicePhone = view.findViewById(R.id.service_description);
            final ImageView serviceImage = view.findViewById(R.id.service_image);

            Service service = services.get( position );
            serviceName.setText(service.getName());
            servicePhone.setText(service.getDescription());

            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("services/").child(service.getId()+".png");

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
                    serviceImage.setImageResource(R.drawable.circle_placeholder);
                    return false;
                }

                @Override
                public boolean onResourceReady(GlideDrawable resource, StorageReference model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                    return false;
                }
            })
            .transform(new CircularTransform(context))
            .into(serviceImage);
        }

        return view;
    }
}
