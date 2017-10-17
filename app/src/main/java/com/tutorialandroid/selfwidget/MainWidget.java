package com.tutorialandroid.selfwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * Created by VenkatPc on 8/19/2017.
 */
public class MainWidget extends AppWidgetProvider {


    private static final String START_CLICK = "startOnClick";
    private static final String STOP_CLICK = "stoptOnClick";
    private Context mContext;
    private MediaPlayer mediaPlayer;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        mContext = context;
        ComponentName thisWidget = new ComponentName(mContext, MainWidget.class);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        for (int widgetId : allWidgetIds) {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.activity_main);
            remoteViews.setOnClickPendingIntent(R.id.start, getPendingSelfIntent(context, START_CLICK));
            remoteViews.setOnClickPendingIntent(R.id.stop, getPendingSelfIntent(context, STOP_CLICK));
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
    }

    protected PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        super.onReceive(context, intent);//add this line
        mContext = context;
        Intent services=new Intent(context,BackgroundService.class);
        if (START_CLICK.equals(intent.getAction())) {
            Log.w("Widget", "Clicked button1");
            if (!((SecurityApplication) mContext.getApplicationContext()).getTimerStatus()) {
                mediaPlayer = MediaPlayer.create(mContext, R.raw.alarm);
                mediaPlayer.start();
                ((SecurityApplication) mContext.getApplicationContext()).startTimer();
                context.startService(services);
            } else {
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                }
            }
        } else if (STOP_CLICK.equals(intent.getAction())) {
            Log.w("Widget", "Clicked button2");
            if (mediaPlayer != null) {
                mediaPlayer.stop();
            }
            ((SecurityApplication) mContext.getApplicationContext()).stopTimer();
            context.stopService(services);
        }
    };
}

