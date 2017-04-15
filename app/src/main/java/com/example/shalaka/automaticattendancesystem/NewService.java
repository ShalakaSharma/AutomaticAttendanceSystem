package com.example.shalaka.automaticattendancesystem;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Pratik on 04/07/2017.
 */

public class NewService extends Service{
private static Timer timer = new Timer();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        // TODO Auto-generated method stub
        Log.i(getClass().getSimpleName(), "StartOfMethod");
        Intent restartService = new Intent(getApplicationContext(), this.getClass());
        restartService.setPackage(getPackageName());
        PendingIntent restartServicePI = PendingIntent.getService(
                getApplicationContext(), 0, restartService,
                PendingIntent.FLAG_ONE_SHOT);

        //Restart the service once it has been killed android
        Log.i(getClass().getSimpleName(), "onTaskRemoved");
        _startService();
        //sendBroadcast(new Intent("YouWillNeverKillMe"));
        //startService(new Intent(this, NewService.class));
        AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 100, restartServicePI);

    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        Log.i(getClass().getSimpleName(),"Created");
        super.onCreate();
        _startService();
        //start a separate thread and start listening to your network object
    }

    private void _startService() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                long UPDATE_INTERVAL = new Random().nextInt(20) * 1000;
                timer.scheduleAtFixedRate(
                        new TimerTask() {
                            public void run() {
                                doServiceWork();
                            }
                        }, 1000, UPDATE_INTERVAL);
                Log.i(getClass().getSimpleName(), "NewService Timer started....");
            }
        };
        new Thread(r).start();
    }

    private void doServiceWork() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo;
        wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo.getSupplicantState() == SupplicantState.COMPLETED) {
            String bssid = wifiInfo.getBSSID();
            int frequency = 0;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                frequency = wifiInfo.getFrequency();
            }
            Log.i(getClass().getSimpleName(), bssid + " " + frequency);
        }
    }

    @Override
    public void onDestroy() {
        Log.i("Background Service","onDestroy Called");
        sendBroadcast(new Intent("YouWillNeverKillMe"));
        super.onDestroy();
    }
}
