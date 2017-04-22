package com.example.shalaka.automaticattendancesystem;

import android.app.AlarmManager;
import android.app.PendingIntent;
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
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by Shalaka on 4/15/2017.
 */

public class MarkAttendanceListener extends BroadcastReceiver {
    public static final int REQUEST_CODE = 12345;
    public static int successfulPings = 0;
    private Context myContext = null;
    public static Course course = new Course();
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

    private class HttpRequestPing extends AsyncTask<String, Object, Course> {
        @Override
        protected Course doInBackground(String... params) {
            Course response = new Course();
            String url = null;
            try {
                url = "http://" + Util.getProperty("Server_IP", myContext) + ":8080/access/newPing";
            } catch (IOException e) {
                e.printStackTrace();
            }


         //  Student student = MyApplication.student;
         //   Log.i("LoginActivity:", "" + student);
         //   Log.i("LoginActivity", student.getFirst_name() + " " + student.getEmail() + " " + student.getCourse().getCourse_name());

            TelephonyManager tm = (TelephonyManager) myContext.getSystemService(Context.TELEPHONY_SERVICE);
            String IMEINumber = tm.getDeviceId();
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("IMEI", IMEINumber)
                    /*.queryParam("student_ID", student.getStudent_ID())
                    .queryParam("email_id", student.getEmail())
                    .queryParam("android_ID", student.getAndroid_ID())
                    .queryParam("course_ID", student.getCourse().getCourse_ID())*/;
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

            try {
                response = restTemplate.getForObject(
                        builder.build().encode().toUri(),
                        Course.class);
            } catch (Exception e) {
                Log.e("LoginActivity", e.getMessage(), e);
            }
            return response;
        }

        @Override
        protected void onPostExecute(Course response) {


            Log.i("MarkAttendanceListener", "onPostExecute() for successful ping");
            Log.i("MarkAttendanceListener", "Course Details received");
            Log.i("MarkAttendanceListener", "" + response);
            Log.i("MarkAttendanceListener", "" + response.getCourse_ID());
            if(response.getCourse_ID() != null) {
                successfulPings++;
            }


           // Course course = MyApplication.course;



            Date sDate = new Date();
            Date eDate = new Date();
            sDate.setHours(response.getStart_time().getHours());
            sDate.setMinutes(response.getStart_time().getMinutes());
            eDate.setHours(response.getStart_time().getHours());
            eDate.setMinutes(response.getStart_time().getMinutes());

            long diff = eDate.getTime() - sDate.getTime();

            long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);

            Calendar sCalendar = dateToCalendar(sDate);

           // sCalendar.add(Calendar.MINUTE, (int)minutes/3);
            Log.i("LoginActivity:", "Hours on post: " + sCalendar.get(Calendar.HOUR_OF_DAY));
            Log.i("LoginActivity:", "Minutes on post" + sCalendar.get(Calendar.MINUTE));
            Log.i("LoginActivity:", "Day of Week on post" + sCalendar.get(Calendar.DAY_OF_WEEK));
            //sCalendar.add(Calendar.MINUTE, (int)minutes/3);
            sCalendar.add(Calendar.MINUTE, 2);
            Log.i("LoginActivity:", "Hours on post after: " + sCalendar.get(Calendar.HOUR_OF_DAY));
            Log.i("LoginActivity:", "Minutes on post after" + sCalendar.get(Calendar.MINUTE));
            Log.i("LoginActivity:", "Day of Week on post after" + sCalendar.get(Calendar.DAY_OF_WEEK));
            Log.i("MarkAttendanceListener", "Minutes to add after:" + (int)(minutes/3));
            int k=0;
            while(k < 3) {
                schedulePing(myContext, sCalendar);
                //sCalendar.add(Calendar.MINUTE, (int)minutes/3);
                sCalendar.add(Calendar.MINUTE, 2);
                k++;
            }
        }

    }

    //Convert Date to Calendar
    private Calendar dateToCalendar(Date date) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;

    }

    //Convert Calendar to Date
    private Date calendarToDate(Calendar calendar) {
        return calendar.getTime();
    }

    public void schedulePing(Context context, Calendar calendar) {

        Log.i("LoginActivity:", "schedulePing() called");

        Log.i("LoginActivity:", "Hours: " + calendar.get(Calendar.HOUR_OF_DAY));
        Log.i("LoginActivity:", "Minutes" + calendar.get(Calendar.MINUTE));
        Log.i("LoginActivity:", "Day of Week" + calendar.get(Calendar.DAY_OF_WEEK));
        // Construct an intent that will execute the AlarmReceiver
        Intent i = new Intent(context, PingInitiator.class);

        final PendingIntent pIntent = PendingIntent.getBroadcast(context, PingInitiator.REQUEST_CODE,
                i, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pIntent);

    }

    private static int getDay(String day) {
        switch(day) {
            case "Sunday": {
                return Calendar.SUNDAY;
            } case "Monday": {
                return Calendar.MONDAY;
            } case "Tuesday": {
                return Calendar.TUESDAY;
            } case "Wednesday": {
                return Calendar.WEDNESDAY;
            } case "Thursday": {
                return Calendar.THURSDAY;
            } case "Friday": {
                return Calendar.FRIDAY;
            } case "Saturday": {
                return Calendar.SATURDAY;
            } default: {
                return Calendar.MONDAY;
            }
        }
    }
}