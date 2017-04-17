package com.example.shalaka.automaticattendancesystem;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;

/**
 * Created by Shalaka on 4/15/2017.
 */

public class Scheduler extends BroadcastReceiver {
    public static final int REQUEST_CODE = 12345;
    public static final String ACTION = "com.codepath.example.servicesdemo.alarm";
    static boolean isScheduled = false;

    public static void scheduleAttendanceService(Context context, Intent intent) {

        if (isScheduled) {
            return;
        }
        ;
        isScheduled = true;
        Log.i("LoginActivity:", "scheduleAttendanceService() called");

        // Construct an intent that will execute the AlarmReceiver
        Intent i = new Intent(context, MarkAttendanceListener.class);

        final PendingIntent pIntent = PendingIntent.getBroadcast(context, MarkAttendanceListener.REQUEST_CODE,
                i, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.HOUR_OF_DAY, 0);

        calendar.set(Calendar.MINUTE, 2);

        calendar.set(Calendar.SECOND, 0);

        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);

        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pIntent);
        // ((AlarmManager) getSystemService(ALARM_SERVICE)).set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }

    // Triggered by the Alarm periodically (starts the service to run task)
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("LoginActivity:", "onReceive() called");

        boolean result = false;
        result = isMyServiceRunning(BackgroundService.class, context);
        Log.i("LoginActivity:", "" + result);
        if (!result) {
            Intent i = new Intent(context, BackgroundService.class);
            context.startService(i);
        }


        scheduleAttendanceService(context, intent);


    }

    public boolean isMyServiceRunning(Class<?> serviceClass, Context context) {
        Log.i("LoginActivity:", "isMyServiceRunning called");
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}