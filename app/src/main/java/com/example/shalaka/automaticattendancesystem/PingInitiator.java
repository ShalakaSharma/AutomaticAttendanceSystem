package com.example.shalaka.automaticattendancesystem;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

/**
 * Created by Shalaka on 4/21/2017.
 */

public class PingInitiator extends BroadcastReceiver {
    public static final int REQUEST_CODE = 12345;
    private static int count = 0;
    private Context myContext = null;
    private int pingCount = 0;
    private Course course;

    // Triggered by the Alarm periodically (starts the service to run task)
    @Override
    public void onReceive(Context context, Intent intent) {
        myContext = context;
        Log.i("LoginActivity:", "onReceive() for MarkAttendanceListener called");
        if (isWifiConnected(context) && isLocationCorrect(context)) {
            new HttpRequestPing().execute();
        }

    }

    boolean isWifiConnected(Context context) {

        Log.i("LoginActivity", "isWifiConnected() called");
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo;
        //while(true){
        wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo.getSupplicantState() == SupplicantState.COMPLETED) {
            String bssid = wifiInfo.getBSSID();
            int frequency = 0;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                frequency = wifiInfo.getFrequency();
            }
            //Toast.makeText(getApplicationContext(), bssid + " " + frequency, Toast.LENGTH_SHORT).show();
            Log.i("MarkAttendanceListener", bssid + " " + frequency);

        }
        return true;
    }

    boolean isLocationCorrect(Context context) {
        Log.i("LoginActivity", "isLocationCorrect() called");
        return true;
    }

    void attendanceComplete(Context context) {
        Log.i("LoginActivity", "attendanceComplete() called");

    }

    private class HttpRequestPing extends AsyncTask<String, Object, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            boolean response = false;
            String url = null;
            try {
                url = "http://" + Util.getProperty("Server_IP", myContext) + ":8080/access/newPing";
            } catch (IOException e) {
                e.printStackTrace();
            }

           // Student student = MyApplication.student;

            TelephonyManager tm = (TelephonyManager) myContext.getSystemService(Context.TELEPHONY_SERVICE);
            String IMEINumber = tm.getDeviceId();
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("IMEI", IMEINumber);
                    /*.queryParam("student_ID", student.getStudent_ID())
                    .queryParam("email_id", student.getEmail())
                    .queryParam("android_ID", student.getAndroid_ID())
                    .queryParam("course_ID", student.getCourse().getCourse_ID());*/
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

            try {
                response = restTemplate.getForObject(
                        builder.build().encode().toUri(),
                        boolean.class);
            } catch (Exception e) {
                Log.e("LoginActivity", e.getMessage(), e);
            }
            return response;
        }

        @Override
        protected void onPostExecute(Boolean response) {
            Log.i("BackgroundService", "onPostExecute() for fetching course details");
            Log.i("BackgroundService", "Course Details received");
            Log.i("MarkAttendanceListener1", "" + response);

            MarkAttendanceListener.successfulPings++;

            if (MarkAttendanceListener.successfulPings == 4) {
                attendanceComplete(myContext);
            }
        }

    }

}