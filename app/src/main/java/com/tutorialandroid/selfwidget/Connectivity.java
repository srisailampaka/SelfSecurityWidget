package com.tutorialandroid.selfwidget;

import android.content.Context;
import android.net.ConnectivityManager;

/**
 * Created by srisailampaka on 26/08/17.
 */

public class Connectivity {
    public static boolean isNetworkAvailable(Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }
}
