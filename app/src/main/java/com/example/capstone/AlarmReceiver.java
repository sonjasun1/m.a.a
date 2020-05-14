package com.example.capstone;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class AlarmReceiver extends BroadcastReceiver {

    //variables accessible from other classes
    public static String NOTIFICATION_ID = "notification-id";
    public static String CHANNEL_ID = "CHANNEL";
    public static String CATEGORY = "everything";
    //accessible throughout the whole class
    static String quoteString;

    //what is triggered when the alarm fires
    @Override
    public void onReceive(final Context context, Intent intent) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // constructs base of the notification
        Notification.Builder builder = new Notification.Builder(context)
                .setContentTitle("Scheduled Notification")
                .setSmallIcon(R.drawable.logo);

        // If the OS is Oreo or higher then a channel is created (new requirement for Oreo and higher).
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "init", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("This is the one and only channel for this program.");
            channel.enableLights(true);
            channel.setLightColor(Color.MAGENTA);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            channel.setLockscreenVisibility(1);
            notificationManager.createNotificationChannel(channel);
            builder.setChannelId(CHANNEL_ID);
        }

        // fetches information from the intent
        int id = intent.getIntExtra(NOTIFICATION_ID, 0);
        // quote category
        String category = intent.getStringExtra(CATEGORY);

        // begins contacting the parse server
        Parse.initialize(new Parse.Configuration.Builder(context.getApplicationContext())
                .applicationId("8b84a08f75e0b47cf96fc7adf14489088dd3b1cb")
                .clientKey("4b3c301e3594424a0ab7c4c89fe66e34e9815060")
                .server("http://3.17.14.88:80/parse")
                .build()
        );

        ParseACL defaultACL = new ParseACL();
        defaultACL.setPublicReadAccess(true);
        defaultACL.setPublicWriteAccess(true);
        ParseACL.setDefaultACL(defaultACL, true);

        Log.i("MainFunction", category);

        // begins the query to the Motivation table
        final ParseQuery<ParseObject> query = ParseQuery.getQuery("Motivation");
        // filters to quotes of a specific category
        query.whereEqualTo("Category", category);
        // only the actual text quotes
        query.selectKeys(Arrays.asList("Motivation"));

        // substantiates the array list
        List<ParseObject> results = null;

        // tries to get results from the server and store them in results
        try {
            results = query.find();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // gets size of the array so a random one can be requested
        int size = results.size();
        Log.i("MainFunction", Integer.toString(size));
        // checks that results has entries
        if (size > 0) {
            Random rand = new Random();
            int i = rand.nextInt(size);
            ParseObject please = results.get(i);
            quoteString = please.getString("Motivation");
        } else
            Log.i("size > 0", "size is 0");
        // if no entries logs an error message


        Log.i("findInBackground", "onReceive: ");

        if (quoteString != null) {
            Log.i("Pre-Notification", quoteString);
            builder.setContentText(quoteString);
        }
        else {
            // for if there was an error in fetching a quote
            Log.i("Pre-Notification", "Mission Failed");
            builder.setContentText("Mission Failed");
        }

        // fires the actual notification
        notificationManager.notify(id, builder.build());


    }
}
