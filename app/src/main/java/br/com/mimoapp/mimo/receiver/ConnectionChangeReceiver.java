package br.com.mimoapp.mimo.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.Display;
import android.widget.Toast;

import br.com.mimoapp.mimo.R;

/**
 * Created by rafael on 17/12/17.
 */

public class ConnectionChangeReceiver extends BroadcastReceiver {

    private boolean isConnected;
    private Toast mToast;

    @Override
    public void onReceive(Context context, Intent intent ) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }
        if ( !isConnected ) {
            Toast.makeText(context, R.string.no_internet, Toast.LENGTH_LONG).show();
        }

    }
}
