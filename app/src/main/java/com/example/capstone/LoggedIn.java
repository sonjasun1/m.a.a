package com.example.capstone;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;

import com.parse.ParseUser;

import java.util.Calendar;
import java.util.Date;

public class LoggedIn extends AppCompatActivity {

    boolean isMotivating;

    // logs the user out of the database.
    public void logOut(View view){
        ParseUser.logOut();
        finish();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void scheduleNotification() {
        // Gives access to the spinner and picker on the screen
        Spinner spinner = (Spinner) findViewById(R.id.categorySelection);
        TimePicker picker = (TimePicker) findViewById(R.id.timePicker);

        // Generates the notification's intent and ties it to the alarm receiver
        Intent notificationIntent = new Intent(getApplicationContext(), AlarmReceiver.class);
        notificationIntent.putExtra(AlarmReceiver.NOTIFICATION_ID, 1);
        notificationIntent.putExtra(AlarmReceiver.CATEGORY, spinner.getSelectedItem().toString());
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);

        // Creates the pending intent
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        // Creates a calendar that we place the time the user wants the alarm to fire at in
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, picker.getHour());
        calendar.set(Calendar.MINUTE, picker.getMinute());

        // Creates the repeating alarm that when triggered, files the pending intent.
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmManager.INTERVAL_DAY, pendingIntent);
    }

    private void cancelNotification() {
        Spinner spinner = (Spinner) findViewById(R.id.categorySelection);

        // Recreates the pending intent so that it's alarm can be cancelled
        Intent notificationIntent = new Intent(getApplicationContext(), AlarmReceiver.class);
        notificationIntent.putExtra(AlarmReceiver.NOTIFICATION_ID, 1);
        notificationIntent.putExtra(AlarmReceiver.CATEGORY, spinner.getSelectedItem().toString());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();
    }

    // What happens when the toggleMotivation button is clicked
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void toggleMotivation(View view) {
        Button giveMotivationButtons = (Button) findViewById(R.id.giveMotivationButtons);
        // If motivation is not currently being given, it start to
        if (isMotivating == false){
            scheduleNotification();
            giveMotivationButtons.setText("Stop Motivation");
            isMotivating = true;
        }
        // If the app is giving motivation, it stops doing so
        else if (isMotivating == true) {
            cancelNotification();
            giveMotivationButtons.setText("Give Motivation");
            isMotivating = false;
        }
    }

    // When the page is first created it does all this
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().hide();

        setContentView(R.layout.activity_logged_in);

        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        Button giveMotivationButtons = (Button) findViewById(R.id.giveMotivationButtons);
        Spinner spinner = (Spinner) findViewById(R.id.categorySelection);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getApplicationContext(),
                R.array.category_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        Intent notificationIntent = new Intent(getApplicationContext(), AlarmReceiver.class);
        notificationIntent.putExtra(AlarmReceiver.NOTIFICATION_ID, 1);

        isMotivating = false;

        // Checks if the pending intent is currently created
        if (PendingIntent.getBroadcast(getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_NO_CREATE) == null) {
            isMotivating = false;
            giveMotivationButtons.setText("Give Motivation");
        } else {
            isMotivating = true;
            giveMotivationButtons.setText("Stop Motivation");
        }

    }
}
