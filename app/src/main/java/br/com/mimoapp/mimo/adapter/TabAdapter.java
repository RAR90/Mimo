package br.com.mimoapp.mimo.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import br.com.mimoapp.mimo.R;
import br.com.mimoapp.mimo.fragment.MyDealsFragment;
import br.com.mimoapp.mimo.fragment.MyServicesFragment;
import br.com.mimoapp.mimo.fragment.ServicesFragment;

/**
 * Created by rafael on 17/10/17.
 */

public class TabAdapter extends FragmentStatePagerAdapter {

    public TabAdapter(FragmentManager fm) {
        super(fm);
    }

    private String[] tabTitle = {"Buscar Serviço", "Meus Negócios", "Oferecer Serviços" };

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;

        switch ( position ) {
            case 0 :
                fragment = new ServicesFragment();
                break;
            case 1 :
                fragment = new MyDealsFragment();
                break;
            case 2 :
                fragment = new MyServicesFragment();
                break;
        }

        return fragment;
    }

    @Override
    public int getCount() {
        return tabTitle.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {

        return tabTitle[ position ];

    }
}
