package me.ceo.bitch.appathon;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;

import android.app.Activity;
import android.app.Dialog;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;

import org.apache.http.cookie.Cookie;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * A login screen that offers login via email/password and via Google+ sign in.
 * <p/>
 * ************ IMPORTANT SETUP NOTES: ************
 * In order for Google+ sign in to work with your app, you must first go to:
 * https://developers.google.com/+/mobile/android/getting-started#step_1_enable_the_google_api
 * and follow the steps in "Step 1" to create an OAuth 2.0 client for your package.
 */
public class LoginActivity extends PlusBaseActivity implements LoaderCallbacks<Cursor> {

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;
    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mEmailLoginFormView;
    private SignInButton mPlusSignInButton;
    private View mSignOutButtons;
    private View mLoginFormView;
    public static CookieManager cookieMan;
    SharedPreferences settings;
    SharedPreferences prefs;
    public static String PREFS_NAME="UserAccount";
    public static String pref_key="Appathon";
    public static String pref_value="IsitThere";
    public static String ACC_NAME="accountName";
    public static String api_key ="";
    public static String ServerURL;
    private ProgressBar progressBar, progressBar2;
    public static final String URL = "http://appathon.erf.asia";
    String output = "";
    public static String UserData;
    public String token;
    String accountName;
    static final String scope = "oauth2:https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/userinfo.profile";
    static final int PICK_ACCOUNT_REQUEST = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        progressBar = (ProgressBar) findViewById(R.id.login_progress);
        progressBar2 = (ProgressBar) findViewById(R.id.login_progress2);

        settings = getSharedPreferences(PREFS_NAME, 0);

        cookieMan = CookieManager.getInstance();
        CookieSyncManager.createInstance(getApplicationContext());


        SharedPreferences.Editor editor = settings.edit();
        editor.putString("ServerURL", URL);
        editor.commit();

        ServerURL = getSharedPreferences(PREFS_NAME, 0).getString(pref_key, pref_value);
        setContentView(R.layout.activity_login);
        if(isNetworkAvailable(this)) {
            if (settings.contains(ACC_NAME)) {
                settings.getString(ACC_NAME, "");

                if (cookieMan.hasCookies()) {
                    cookieMan.getCookie(ServerURL);
                    String url = LoginActivity.ServerURL + "/api/currentuser";
                    getDataInAsyncTask(url, null, "getUserData");
                } else {
                    getAndUseAuthTokenInAsyncTask();
                }
            } else {
                // Find the Google+ sign in button.
                mPlusSignInButton = (SignInButton) findViewById(R.id.plus_sign_in_button);
                if (supportsGooglePlayServices()) {
                    // Set a listener to connect the user when the G+ button is clicked.
                    mPlusSignInButton.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            signIn();
                        }
                    });
                } else {
                    // Don't offer G+ sign in if the app's version is too low to support Google Play
                    // Services.
                    mPlusSignInButton.setVisibility(View.GONE);
                    return;
                }

                // Set up the login form.
                mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
                populateAutoComplete();

                mPasswordView = (EditText) findViewById(R.id.password);
                mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                        if (id == R.id.login || id == EditorInfo.IME_NULL) {
                            attemptLogin();
                            return true;
                        }
                        return false;
                    }
                });

                Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
                mEmailSignInButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        attemptLogin();
                    }
                });

                mLoginFormView = findViewById(R.id.login_form);
                mProgressView = findViewById(R.id.login_progress);
                mEmailLoginFormView = findViewById(R.id.email_login_form);
            }
        } else {
            Toast.makeText(
                    getApplicationContext(),
                    "No Network!!",
                    Toast.LENGTH_LONG).show();
        }
        //mSignOutButtons = findViewById(R.id.plus_sign_out_buttons);
    }

    private void populateAutoComplete() {
        getLoaderManager().initLoader(0, null, this);
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;


        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
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
    public void showProgress(final boolean show) {
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
    protected void onPlusClientSignIn() {

        Intent mainActivityIntent = new Intent(this, MainActivity.class);
        startActivity(mainActivityIntent);

        //Set up sign out and disconnect buttons.
        /*Button signOutButton = (Button) findViewById(R.id.plus_sign_out_button);
        signOutButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
            }
        });
        Button disconnectButton = (Button) findViewById(R.id.plus_disconnect_button);
        disconnectButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                revokeAccess();
            }
        });*/
    }



    @Override
    protected void onPlusClientBlockingUI(boolean show) {
        showProgress(show);
    }

    @Override
    protected void updateConnectButtonState() {
        //TODO: Update this logic to also handle the user logged in by email.
        boolean connected = getPlusClient().isConnected();

        //mSignOutButtons.setVisibility(connected ? View.VISIBLE : View.GONE);
        mPlusSignInButton.setVisibility(connected ? View.GONE : View.VISIBLE);
        mEmailLoginFormView.setVisibility(connected ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onPlusClientRevokeAccess() {
        // TODO: Access to the user's G+ account has been revoked.  Per the developer terms, delete
        // any stored user data here.
    }

    @Override
    protected void onPlusClientSignOut() {

    }

    /**
     * Check if the device supports Google Play Services.  It's best
     * practice to check first rather than handling this as an error case.
     *
     * @return whether the device supports Google Play Services
     */
    private boolean supportsGooglePlayServices() {
        return GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) ==
                ConnectionResult.SUCCESS;
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
        List<String> emails = new ArrayList<String>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
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


    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }

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

            for (String credential : DUMMY_CREDENTIALS) {
                String[] pieces = credential.split(":");
                if (pieces[0].equals(mEmail)) {
                    // Account exists, return true if the password matches.
                    return pieces[1].equals(mPassword);
                }
            }

            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    public static boolean isNetworkAvailable(Context context) {
        boolean outcome = false;
        if (context != null) {
            ConnectivityManager cm = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo[] networkInfos = cm.getAllNetworkInfo();

            for (NetworkInfo tempNetworkInfo : networkInfos) {
                /**
                 * Can also check if the user is in roaming
                 */
                if (tempNetworkInfo.isConnected()) {
                    outcome = true;
                    break;
                }
            }
        }

        return outcome;
    }


    void getDataInAsyncTask(final String url, final String postData,
                            final String callback) {

        AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {

            @Override
            protected void onPreExecute() {
                try {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar2.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            protected String doInBackground(Void... params) {
                return getData(url, postData);
            }
            @Override
            protected void onPostExecute(String v) {
                if(callback=="getUserData"){
                    getUserData();
                }
            }
        };
        task.execute();
    }

    protected String getData(String url, String postData) {
        String outputData = null;
        try {
            String cookie = CookieManager.getInstance().getCookie(LoginActivity.ServerURL);
            URL urlObject;
            HttpURLConnection urlConn = null;

            try {
                urlObject = new URL(url);
                urlConn = (HttpURLConnection) urlObject.openConnection();
                urlConn.addRequestProperty("Authorization",
                        "Token token=" + api_key);

                urlConn.addRequestProperty("Cookie", cookie);
                urlConn.setDoOutput(true);

                if (postData != null) {
                    OutputStreamWriter wr = new OutputStreamWriter(
                            urlConn.getOutputStream());
                    wr.write(postData);
                    wr.flush();
                }

                InputStream in = new BufferedInputStream(urlConn.getInputStream());
                BufferedReader buffin = new BufferedReader(
                        new InputStreamReader(in));
                StringBuilder responseStrBuilder = new StringBuilder();
                while ((outputData = buffin.readLine()) != null) {
                    responseStrBuilder.append(outputData);
                }
                outputData = responseStrBuilder.toString();
                Log.d("Response code", urlConn.getResponseCode() + "    " + url);
            } catch (MalformedURLException e1) {
                e1.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConn != null)
                    urlConn.disconnect();
            }
        }catch(Exception e){
            //when server is down
            e.printStackTrace();
        }
        if(outputData != null) {
            return outputData;
        } else{
            throw new NullPointerException();
        }
    }

    void getAndUseAuthTokenInAsyncTask() {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                progressBar.setVisibility(View.VISIBLE);
                progressBar2.setVisibility(View.VISIBLE);
                mLoginFormView.setVisibility(View.INVISIBLE);
            }
            @Override
            protected Void doInBackground(Void... params) {
                getAndUseAuthTokenBlocking();
                return null;
            }
            @Override
            protected void onPostExecute(Void v) {
            }
        };
        task.execute((Void) null);
    }

    public void getUserData(){
        UserData = output;
        Log.d("gotUserData",  "++++++++++++++++++++");
    }

    void getAndUseAuthTokenBlocking() {
        try {
            token = GoogleAuthUtil.getToken(getApplicationContext(),
                    accountName, scope);
            GoogleAuthUtil.invalidateToken(this, token);

// Log.d("token", token);
            getCookieFromBackpackInAsyncTask(token);
            //return;
        } catch (GooglePlayServicesAvailabilityException playEx) {
            //TODO this block gives an error as it is running on a thread
            //Not on UI thus cannot produce an Dialog Alert..
            //FIXME run this dialog in runOnUiThread()
            try {
                Dialog alert = GooglePlayServicesUtil.getErrorDialog(
                        playEx.getConnectionStatusCode(), this,
                        PICK_ACCOUNT_REQUEST);
                alert.show();
            } catch(RuntimeException runtimeException){
                /*Toast.makeText(
                        getApplicationContext(),
                        "Please update your Google Play services App.",
                        Toast.LENGTH_LONG).show();*/
                runtimeException.printStackTrace();
                //Close the app after giving the notice to update play services.
                finish();
            }
        } catch (UserRecoverableAuthException userAuthEx) {
            startActivityForResult(userAuthEx.getIntent(), PICK_ACCOUNT_REQUEST);
            return;
        } catch (IOException transientEx) {
            return;
        } catch (GoogleAuthException authEx) {
            return;
        }
    }

    private void getCookieFromBackpackInAsyncTask(final String token2) {
        AsyncTask<String, Void, Integer> task = new AsyncTask<String, Void, Integer>() {
            @Override
            protected void onPreExecute() {
                progressBar.setVisibility(View.VISIBLE);
                progressBar2.setVisibility(View.VISIBLE);
            }

            @Override
            protected Integer doInBackground(String... params) {
                return getBackpackCookie(token2);
            }

            @Override
            protected void onPostExecute(Integer v) {
                if (v == -2) {
                    Toast.makeText(
                            getApplicationContext(),
                            "Couldn't Connect to Server. Make sure you are using an already registered email.",
                            Toast.LENGTH_LONG).show();
                }
            }
        };
        task.execute();
    }

    protected int getBackpackCookie(String token2) {
        String data = "token=" + token2;
        Log.d("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%", token2);
        String cookie;
        URL urlObject;
        try {
            urlObject = new URL(LoginActivity.ServerURL + "/api/signin");
        } catch (MalformedURLException e1) {
            // //Log.d("URL", "Bad url");
            return -1;
        }
        HttpURLConnection urlConn;
        try {
            urlConn = (HttpURLConnection) urlObject.openConnection();
        } catch (IOException e) {
            // //Log.d("connection", "main ni karna connect");
            return -1;
        }
        urlConn.addRequestProperty("Authorization",
                "Token token=" + api_key);
        urlConn.setDoOutput(true);
        urlConn.setDoInput(true);
        try {
            OutputStreamWriter wr = new OutputStreamWriter(
                    urlConn.getOutputStream());
            wr.write(data);
            wr.flush();
        } catch (IOException e) {
            // //Log.d("outputstream", "IOException in outputstream");
            return -1;
        }
        try {
            if (urlConn.getResponseCode() == 200) {
                cookie = urlConn.getHeaderField("set-cookie");
                if (cookie != null) {
                    // Log.d("Cookie", cookie);
                    CookieManager.getInstance().setCookie(
                            LoginActivity.ServerURL, cookie);
                }
            } else {

                return -2;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            urlConn.disconnect();
        }

        String url = LoginActivity.ServerURL + "/api/currentuser";
        try {
            getDataInAsyncTask(url, null, "getUserId");
        }catch(NullPointerException nullpointerexception){
            //if the server if down then this block should throw and error
            Toast.makeText(
                    getApplicationContext(),
                    "Our Server is under maintenance, We are sorry for the inconvenience ;( ",
                    Toast.LENGTH_LONG).show();
        }
        return 1;

    }



}




