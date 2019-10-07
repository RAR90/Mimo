package br.com.mimoapp.mimo.fragment;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import br.com.mimoapp.mimo.R;

/**
 * Created by rafael on 27/10/17.
 */

public class WorkersFilterDialogFragment extends DialogFragment {

    int mNum;

    public static WorkersFilterDialogFragment newInstance() {

        WorkersFilterDialogFragment f = new WorkersFilterDialogFragment();
        Bundle args = new Bundle();
        //args.putInt("num", num);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.MyDialog);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View thisView = inflater.inflate(R.layout.fragment_workers_filter_dialog, container, false);

        // Get here components of dialog fragment

        return thisView;
    }
}