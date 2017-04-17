package com.example.shalaka.automaticattendancesystem;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;

/**
 * Created by Shalaka on 4/15/2017.
 */

public class MarkAttendanceListener extends BroadcastReceiver {
    public static final int REQUEST_CODE = 12345;
    public static final String ACTION = "com.codepath.example.servicesdemo.alarm";

    // Triggered by the Alarm periodically (starts the service to run task)
    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i("LoginActivity:", "onReceive() for MarkAttendanceListener called");

    }
}