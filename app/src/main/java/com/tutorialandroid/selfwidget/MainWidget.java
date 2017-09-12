package com.tutorialandroid.selfwidget;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.UriMatcher;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.CursorAdapter;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by VenkatPc on 8/19/2017.
 */
public class MainWidget extends AppWidgetProvider implements LocationListener {


    private LocationManager locationManager;
    private String locationProvider;
    private static final String AUTHORITY = "com.tutorialandroid.selfsecurity";
    private static final String BASE_PATH = "contacts";
    private static final String MESSAGE_PATH = "message";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);
    public static final Uri MESSAGE_URI = Uri.parse("content://" + AUTHORITY + "/" + MESSAGE_PATH);

    // Constant to identify the requested operation
    private static final int CONTACTS = 1;
    private static final int CONTACT_ID = 2;
    private static final int MESSAGE = 3;
    private static final int MESSAGE_ID = 4;
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private boolean status =false;
    static {
        uriMatcher.addURI(AUTHORITY, BASE_PATH, CONTACTS);
        uriMatcher.addURI(AUTHORITY, BASE_PATH + "/#", CONTACT_ID);
        uriMatcher.addURI(AUTHORITY, MESSAGE_PATH, MESSAGE);
        uriMatcher.addURI(AUTHORITY, MESSAGE_PATH + "/#", MESSAGE_ID);
    }

    private List<ContactDetails> list = new ArrayList<ContactDetails>();


    private static final String START_CLICK = "startOnClick";
    private static final String STOP_CLICK = "stoptOnClick";
    private static int time = 0;
    private static String message = "";
    private android.os.Handler handler = null;
    private Runnable runnable = null;
    private Context mContext;
    private String address;
    private static final int PERMISSION_REQUEST_CODE = 1;


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        mContext = context;
        ComponentName thisWidget = new ComponentName(context, MainWidget.class);
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
        int lastItem = 0;
        final ArrayList<Message> list = getMessage(context);
        //final ArrayList<ContactDetails> details = getContacts(context);
        if (list.size() != 0) {
            lastItem = getMessage(context).size() - 1;
            time = Integer.parseInt(list.get(lastItem).getTime());
            message = getMessage(context).get(lastItem).getMessage();
            // Toast.makeText(context, ""+time+message, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Please set the alert Message and time", Toast.LENGTH_SHORT).show();
        }
        handler = new android.os.Handler();

        runnable = new Runnable() {
            @Override
            public void run() {
                int lastItemNo = 0;
//                    ArrayList<Message> list = getMessage(context);
                Log.w("Widget", "Clicked button11");
                if (list.size() != 0) {
                    Log.w("Widget", "Clicked button111");
                    lastItemNo = list.size() - 1;
                    time = Integer.parseInt(list.get(lastItemNo).getTime());
                    message = getMessage(context).get(lastItemNo).getMessage();
                    Log.d("log....", time + message + " , address=" + address);
                    getContacts(context);
//                        for (int i = 0; i < details.size(); i++) {
//                            Log.d("log....",details.get(i).getNumber()+message + " , address=" + address);
//                            sendSMS(context, details.get(i).getNumber(), message + " , address=" + address);
//                        }


                }
                handler.postDelayed(runnable, time * 60000);
            }
        };

        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        //define the location manager criteria
        Criteria criteria = new Criteria();

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
        } else {
            locationProvider = locationManager.getBestProvider(criteria, false);

            Location location = locationManager.getLastKnownLocation(locationProvider);
            //initialize the location
            if (location != null) {

                onLocationChanged(location);
            }
        }

        if (START_CLICK.equals(intent.getAction())) {
            Log.w("Widget", "Clicked button1" + time);
            runnable.run();
            handler.postDelayed(runnable, time * 60000);
            locationManager.requestLocationUpdates(locationProvider, 400, 1, this);
            status=true;
        } else if (STOP_CLICK.equals(intent.getAction())) {
            Log.w("Widget", "Clicked button2");
            status=false;
            locationManager.removeUpdates(this);
            handler.removeCallbacks(runnable);
        }
    }

    ;

    public ArrayList<ContactDetails> getContacts(Context context) {
        ArrayList<ContactDetails> detailses = new ArrayList<>();
        // String URL = CONTENT_URI;
        //Uri students = Uri.parse(CONTENT_URI);
        Cursor cursor = context.getContentResolver().query(CONTENT_URI, null, null, null, "name");
        if (cursor.moveToFirst()) {
            do {
                ContactDetails contactDetails = new ContactDetails();
                contactDetails.setId(Integer.parseInt(cursor.getString(0)));
                contactDetails.setName(cursor.getString(1));
                contactDetails.setNumber(cursor.getString(2));
                //Log.d("log....", cursor.getString(1)+cursor.getString(2));
                if (!message.isEmpty()) {
                    if(status) {
                        sendSMS(context, cursor.getString(2), message);
                    }else{}
                }
            } while (cursor.moveToNext());
        }
        cursor.close();

        return detailses;


    }


    public ArrayList<Message> getMessage(Context context) {
        ArrayList<Message> messagesList = new ArrayList<>();
        Cursor cursor = context.getContentResolver().query(MESSAGE_URI, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                Message message = new Message();
                message.setTime(cursor.getString(1));
                message.setMessage(cursor.getString(2));
                messagesList.add(message);


            } while (cursor.moveToNext());
        }
        cursor.close();

        return messagesList;


    }

    private void sendSMS(final Context context, String phoneNumber, String message) {
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";
        PendingIntent sentPI = PendingIntent.getBroadcast(context, 0,
                new Intent(SENT), 0);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(context, 0,
                new Intent(DELIVERED), 0);
        //---when the SMS has been sent---
        context.getApplicationContext().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(context, "SMS sent",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(context, "Generic failure",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(context, "No service",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(context, "Null PDU",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(context, "Radio off",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(SENT));
        //---when the SMS has been delivered---
        context.getApplicationContext().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(context, "SMS delivered",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(context, "SMS not delivered",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(DELIVERED));
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message+"address=" + address, sentPI, deliveredPI);
    }


    public void multipleSMS(final Context context, String phoneNumber, String message) {
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        PendingIntent sentPI = PendingIntent.getBroadcast(context, 0, new Intent(
                SENT), 0);

        PendingIntent deliveredPI = PendingIntent.getBroadcast(context, 0,
                new Intent(DELIVERED), 0);

        // ---when the SMS has been sent---
        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        ContentValues values = new ContentValues();
                        for (int i = 0; i < list.size() - 1; i++) {
                            values.put("address", list.get(i).getNumber());
                            // txtPhoneNo.getText().toString());
                            values.put("body", "Say Hi");
                        }
                        context.getContentResolver().insert(
                                Uri.parse("content://sms/sent"), values);
                        Toast.makeText(context, "SMS sent",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(context, "Generic failure",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(context, "No service",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(context, "Null PDU",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(context, "Radio off",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(SENT));

        // ---when the SMS has been delivered---
        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(context, "SMS delivered",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(context, "SMS not delivered",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(DELIVERED));

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
    }

    @Override
    public void onLocationChanged(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        String msg = "New Latitude: " + latitude + "New Longitude: " + longitude;
        if (Connectivity.isNetworkAvailable(mContext)) {
            address = getAddress(latitude, longitude);
        } else {
            address = latitude + "," + longitude;
        }
       // Toast.makeText(mContext, address, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    private String getAddress(double latitude, double longitude) {
        Geocoder geocoder;
        List<Address> addresses;
        String address = "";
        String city = "";
        String state = "";
        String country = "";
        try {
            geocoder = new Geocoder(mContext, Locale.getDefault());

            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

            address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            city = addresses.get(0).getLocality();
            state = addresses.get(0).getAdminArea();
            country = addresses.get(0).getCountryName();
            String postalCode = addresses.get(0).getPostalCode();
            String knownName = addresses.get(0).getFeatureName();
        } catch (Exception e) {

        }
        return address + "," + city + "," + state + "," + country;
    }


}

