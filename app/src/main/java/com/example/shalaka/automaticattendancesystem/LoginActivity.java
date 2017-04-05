package com.example.shalaka.automaticattendancesystem;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    // UI references.
    private EditText mEmailView;
    private EditText mFirstNameView;
    private EditText mLastNameView;
    private EditText mstudentIDView;
    private EditText mCourseIDView;
    private EditText mDayOfTheWeekView;
    private TimePicker startTimePicker;
    private TimePicker endTimePicker;
    private View mProgressView;
    private View mLoginFormView;

    private String IMEINumber;
    private String android_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(getClass().getSimpleName(), "onCreate() called");
        setContentView(R.layout.activity_login);

        mEmailView = (EditText) findViewById(R.id.email);
        mFirstNameView = (EditText) findViewById(R.id.studentfirstname);
        mLastNameView = (EditText) findViewById(R.id.studentlastname);
        mstudentIDView = (EditText) findViewById(R.id.studentid);
        mCourseIDView = (EditText) findViewById(R.id.courseid);
        startTimePicker = (TimePicker) findViewById(R.id.start_time_picker);
        endTimePicker = (TimePicker) findViewById(R.id.end_time_picker);

        attemptPermission();

        Button submit = (Button) findViewById(R.id.register_button);
        submit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegister();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    private void attemptPermission() {
        Log.i(getClass().getSimpleName(), "Requesting permission to access phone state");
        ActivityCompat.requestPermissions(LoginActivity.this,
                new String[]{Manifest.permission.READ_PHONE_STATE},
                1);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        Log.i(getClass().getSimpleName(), "Result for permission received");
        switch (requestCode) {
            case 1: {
                Log.i(getClass().getSimpleName(), "Request code was 1");
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(getClass().getSimpleName(), "PERMISSION_GRANTED");
                    Toast.makeText(LoginActivity.this, "", Toast.LENGTH_SHORT).makeText(LoginActivity.this, "Permission granted to read your phone state", Toast.LENGTH_SHORT).show();
                    TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                    IMEINumber = tm.getDeviceId();
                    Log.i(getClass().getSimpleName(), "IMEINumber" + IMEINumber);
                    String phone1 = tm.getLine1Number();
                    Log.i(getClass().getSimpleName(), "Phone Number" + phone1);
                    String subscriberId = tm.getSubscriberId();
                    Log.i(getClass().getSimpleName(), "IMSI" + subscriberId);
                    Toast.makeText(LoginActivity.this, "", Toast.LENGTH_SHORT).makeText(LoginActivity.this, "IMEI" + IMEINumber, Toast.LENGTH_SHORT).show();
                    android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
                    Log.i(getClass().getSimpleName(), "unique_id" + android_id);

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    Log.i(getClass().getSimpleName(), "PERMISSION_DENIED");
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(LoginActivity.this, "Permission denied to read your phone state", Toast.LENGTH_LONG).show();
                   // Toast.makeText(LoginActivity.this, "Permission to read your phone state is required. Please accept", Toast.LENGTH_LONG).show();
                    //reattemptPermission();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private boolean reattemptPermission() {
        Log.i(getClass().getSimpleName(), "Reattempting to get permission to access phone state");

        if (ContextCompat.checkSelfPermission(LoginActivity.this,
                Manifest.permission.READ_PHONE_STATE)!= PackageManager.PERMISSION_GRANTED) {
            Log.i(getClass().getSimpleName(), "Showing explanation for requested permission");
            if (ActivityCompat.shouldShowRequestPermissionRationale(LoginActivity.this,
                    Manifest.permission.READ_PHONE_STATE)) {
                Log.i(getClass().getSimpleName(), "Entered shouldShowRequestPermissionRationale");
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            }

        } else {
            Log.e(getClass().getSimpleName(), "Error occured while requesting permissions. Critical");
            Toast.makeText(LoginActivity.this, "Error occured while requesting permissions. Critical", Toast.LENGTH_LONG).show();
        }


        ActivityCompat.requestPermissions(LoginActivity.this,
                new String[]{Manifest.permission.READ_PHONE_STATE},
                1);

        return false;
    }

    private String getAndroidID() {

        return "";
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptRegister() {
        Log.i(getClass().getSimpleName(), "attemptRegister() called");

    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }


   /* private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }*/

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

           /* for (String credential : DUMMY_CREDENTIALS) {
                String[] pieces = credential.split(":");
                if (pieces[0].equals(mEmail)) {
                    // Account exists, return true if the password matches.
                    return pieces[1].equals(mPassword);
                }
            }*/

            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
          /*  mAuthTask = null;
            showProgress(false);

            if (success) {
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }*/
        }

        @Override
        protected void onCancelled() {
           /* mAuthTask = null;
            showProgress(false);*/
        }


    }

}