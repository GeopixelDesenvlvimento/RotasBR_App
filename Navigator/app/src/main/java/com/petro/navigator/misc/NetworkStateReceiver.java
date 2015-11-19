package com.petro.navigator.misc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.petro.navigator.AppManager;
import com.petro.navigator.R;

/**
 *
 * Classe responsável por atualiza a visibilidade do botão de sincronismo de acordo com o status da internet
 */
public class NetworkStateReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {

        if(intent.getExtras()!=null) {
            NetworkInfo ni=(NetworkInfo) intent.getExtras().get(ConnectivityManager.EXTRA_NETWORK_INFO);

            if(ni!=null && ni.getState()==NetworkInfo.State.CONNECTED)
                AppManager.showView(R.id.action_sync);
        }
        if(intent.getExtras().getBoolean(ConnectivityManager.EXTRA_NO_CONNECTIVITY,Boolean.FALSE))
            AppManager.hideView(R.id.action_sync);

    }
}