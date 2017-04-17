package com.example.shalaka.automaticattendancesystem;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

public class BackgroundService extends Service {
    public BackgroundService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Let it continue running until it is stopped.
        Log.i(getClass().getSimpleName(), "onStartCommand() called");
        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
        new HttpRequestTask().execute("a");
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        Log.i(getClass().getSimpleName(), "Created");
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(getClass().getSimpleName(), "onDestroy() called");
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();

    }

    private class HttpRequestTask extends AsyncTask<String, Object, Course[]> {
        @Override
        protected Course[] doInBackground(String... params) {
            Course[] response = null;
            String url = null;
            try {
                url = "http://" + Util.getProperty("Server_IP", getApplicationContext()) + ":8080/access/studentCourses";
            } catch (IOException e) {
                e.printStackTrace();
            }

            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("ID", params[0]);
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

            try {
                response = restTemplate.getForObject(
                        builder.build().encode().toUri(),
                        Course[].class);
            } catch (RestClientException e) {
                Log.e("LoginActivity", e.getMessage(), e);
            }
            return response;
        }

        @Override
        protected void onPostExecute(Course[] response) {
            for (Course c : response)
                Log.i("BackgroundService", " " + c.getId() + c.getDay() + c.getCourse_ID() + c.getCourse_name());

            ///calling service between start and end times

            //    for(int i = 0;i<response.length;i++){
            //get time
            //    }
        }

    }
}
