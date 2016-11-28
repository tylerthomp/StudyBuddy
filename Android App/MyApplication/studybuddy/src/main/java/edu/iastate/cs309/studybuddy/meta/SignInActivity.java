package edu.iastate.cs309.studybuddy.meta;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import edu.iastate.cs309.studybuddy.R;
import edu.iastate.cs309.studybuddy.meta.Util.SharedPrefsUtil;
import edu.iastate.cs309.studybuddy.meta.Util.VolleyUtil;

/**
 * Created by Tyler on 2/18/2015.
 */
public class SignInActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    SharedPrefsUtil sharedPrefsUtil;
    CardView container;

    View signInGoogle;
    GoogleApiClient googleSignInClient;
    boolean mSignInClicked = false;
    boolean mIntentInProgress = false;
    ConnectionResult mConnectionResult;
    private static final int RC_SIGN_IN = 0;


    static final int REQUEST_CODE_PICK_ACCOUNT = 1000;
    String mEmail; // Received from newChooseAccountIntent(); passed to getToken()

    private final static String GPLUS_SCOPE
            = "https://www.googleapis.com/auth/plus.login";
    private final static String mScopes
            = "oauth2:" + " " + GPLUS_SCOPE;
    String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        getSupportActionBar().setTitle("Sign-In");
        sharedPrefsUtil = new SharedPrefsUtil(this);
        container = (CardView) findViewById(R.id.sign_in_container_card);
        signInGoogle = findViewById(R.id.sign_in_button);
        googleSignInClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        container.setCardElevation(100.0f);
        container.setPreventCornerOverlap(true);
        signInGoogle.setOnClickListener(signUserInGoogle());
    }

    private View.OnClickListener signUserInGoogle()
    {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickUserAccount();

            }
        };
    }


    @Override
    public void onConnected(Bundle arg0) {
        mSignInClicked = false;
        Toast.makeText(this, "User is connected!", Toast.LENGTH_LONG).show();

        // Get user's information
        //getProfileInformation();

        // Update the UI after signin
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

    }

    @Override
    public void onConnectionSuspended(int arg0) {
        googleSignInClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (!result.hasResolution()) {
            GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this,
                    0).show();
            return;
        }

        if (!mIntentInProgress) {
            // Store the ConnectionResult for later usage
            mConnectionResult = result;

            if (mSignInClicked) {
                // The user has already clicked 'sign-in' so we attempt to
                // resolve all
                // errors until the user is signed in, or they cancel.
                resolveSignInError();
            }
        }

    }


    /**
     * Method to resolve any signin errors
     * */
    private void resolveSignInError() {
        if (mConnectionResult.hasResolution()) {
            try {
                mIntentInProgress = true;
                mConnectionResult.startResolutionForResult(this, RC_SIGN_IN);
            } catch (IntentSender.SendIntentException e) {
                mIntentInProgress = false;
                googleSignInClient.connect();
            }
        }
    }

    private void pickUserAccount() {
        String[] accountTypes = new String[]{"com.google"};
        Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                accountTypes, false, null, null, null, null);
        startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PICK_ACCOUNT) {
            // Receiving a result from the AccountPicker
            if (resultCode == RESULT_OK) {
                mEmail = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                // With the account name acquired, go get the auth token
                new GetUsernameTask(this, mEmail, mScopes).execute();

            } else if (resultCode == RESULT_CANCELED) {
                // The account picker dialog closed without selecting an account.
                // Notify users that they must pick an account to proceed.

                Toast.makeText(this, "A Google Account must be chosen to use Study Buddy.", Toast.LENGTH_SHORT).show();
            }
        }
        // Later, more code will go here to handle the result from some exceptions...
    }

        public class GetUsernameTask extends AsyncTask<Void,Void,Void> {
            Activity mActivity;
            String mScope;
            String mEmail;

            GetUsernameTask(Activity activity, String name, String scope) {
                this.mActivity = activity;
                this.mScope = scope;
                this.mEmail = name;
            }

            /**
             * Executes the asynchronous job. This runs when you call execute()
             * on the AsyncTask instance.
             */
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    token = fetchToken();
                } catch (IOException e) {
                    // The fetchToken() method handles Google-specific exceptions,
                    // so this indicates something went wrong at a higher level.
                    // TIP: Check for network connectivity before starting the AsyncTask.

                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                //Toast.makeText(mActivity, token, Toast.LENGTH_LONG).show();
                Log.e("Access Token:", token);
                Log.i("Log in:", "Logging user in now.");
                sharedPrefsUtil.getSharedPrefs().edit().putString(LaunchActivity.SHARED_PREF_LOG_IN_INFO_KEY, token).commit();
                VolleyUtil.getInstance(mActivity).addToRequestQueue(logUserIn("http://williamnorton.me:8000/api/manual_login/google-oauth2/?access_token=",token, mActivity));
            }

            /**
             * Gets an authentication token from Google and handles any
             * GoogleAuthException that may occur.
             */
            protected String fetchToken() throws IOException {
                try {
                    return GoogleAuthUtil.getToken(mActivity, mEmail, mScope);
                } catch (GoogleAuthException fatalException) {
                    // Some other type of unrecoverable exception has occurred.
                    // Report and log the error as appropriate for your app.
                    fatalException.printStackTrace();
                }
                return null;
            }
        }

    private JsonObjectRequest logUserIn(String baseUrl, String accessToken, final Context context)
    {
        return new JsonObjectRequest(Request.Method.GET, baseUrl+accessToken, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.e("API Response", response.getString("logged_in"));


                if(response.getString("logged_in").equalsIgnoreCase("true"))
                {
                    Intent goToStudyBuddy = new Intent(context, MainActivity.class);
                    startActivity(goToStudyBuddy);
                }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },

        new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError volleyError) {

        }
    });
    }

}
