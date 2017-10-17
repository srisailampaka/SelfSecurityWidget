package com.tutorialandroid.selfwidget;

import android.app.Application;
import android.util.Log;

/**
 * Created by srisailampaka on 24/08/17.
 */

public class SecurityApplication extends Application {
    public static SecurityApplication appInstance;
    boolean starttimer = false;


    @Override
    public void onCreate() {
        super.onCreate();
        appInstance = this;

    }

    public void startTimer() {
        if (!starttimer) {
            starttimer = true;
        }
        Log.w("startTimer", "startTimer");
    }

    public void stopTimer() {
        Log.w("stopTime00 ", "stoptimer");
        starttimer = false;
    }


    public boolean getTimerStatus() {
        return starttimer;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.w("onlowmemory", "onLowmemory");
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.w("onterminate", "onTerminate");
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
    }
}