package edu.iastate.cs309.studybuddy.meta;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.plus.Plus;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import edu.iastate.cs309.studybuddy.R;
import edu.iastate.cs309.studybuddy.meta.Util.SharedPrefsUtil;
import edu.iastate.cs309.studybuddy.meta.Util.VolleyUtil;
import edu.iastate.cs309.studybuddy.modules.createsession.fragments.CreateSessionFragment;
import edu.iastate.cs309.studybuddy.modules.home.fragments.HomeFragment;
import edu.iastate.cs309.studybuddy.modules.me.fragments.MeFragment;
import edu.iastate.cs309.studybuddy.modules.me.fragments.MeListFragment;
import edu.iastate.cs309.studybuddy.modules.review_session.fragments.PastSessionListFragment;
import edu.iastate.cs309.studybuddy.modules.review_session.fragments.ReviewModuleFragment;
import edu.iastate.cs309.studybuddy.modules.search.fragments.SearchFragment;


public class MainActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{
    /*******************************************
     * NOTE: COMMENTS ON OVERRIDDEN METHODS ARE
     * USUALLY CONSIDERED UNNECESSARY IN ANDROID
     * DEVELOPMENT, HOWEVER I WILL COMMENT ALL
     * METHODS IN THIS CLASS IN ORDER TO HELP
     * DEVELOPERS LOOKING AT THIS CODE WHO MAY
     * NOT HAVE ANY ANDROID EXPERIENCE
     *******************************************/

    private String[] navDrawerItems;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggleListener;
    private ListView drawerListView;

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
    String token = " ";


    /*******************************************
     * LIFECYCLE METHODS
     * THESE METHODS MUST BE OVERRIDDEN IN ORDER
     * FOR OUR CONTROLLER TO WORK PROPERLY
     *******************************************/

    /**
     * This method is executed prior to the UI being
     * drawn. It is good practice to ONLY get Views
     * from the XML and attach them to Java code
     * in this method. Launching Network operations
     * (even ones done in a background thread) may
     * cause a noticeable lag when the user tries
     * to view this Activity. However, since we are
     * using Fragments, we shouldn't have to worry
     * about that in this Activity.
     * @param savedInstanceState - The state the OS saves when suspending the app.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startDefaultFragment();
        checkLogin();
    }

    /**
     * This method is called immediately after
     * onCreate(). This is typically where we
     * start network operations required to
     * populate the UI. If the operations take
     * a while, we put a progress circle on
     * the screen here, and make is disappear
     * when we get a network response.
     */
    @Override
    protected void onStart() {
        super.onStart();
        setupNavigationDrawer();

    }

    /**
     * This is where we inflate the menu defined in the xml file.
     * @param menu - The Menu Object Android creates for the Activity
     * @return - If the Menu inflates correctly.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * This is the callback from the built-in click event
     * observer. This is where we attach the logic for
     * our menu clicks.
     * @param item - The MenuItem that was clicked by th user.
     * @return - If the options task was completed successfully.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        //However, it will not automatically handle hamburger button
        //events.
        int id = item.getItemId();

        //Hamburger button events
        if(id == android.R.id.home)
        {
            if(drawerLayout.isDrawerOpen(Gravity.LEFT))
                drawerLayout.closeDrawer(Gravity.LEFT);
            else
                drawerLayout.openDrawer(Gravity.LEFT);
        }
        return super.onOptionsItemSelected(item);
    }

    /*****************************************************
                       HELPER METHODS
     *****************************************************/

    /**
     * Helper method that does basic setup for the
     * navigation drawer. This grabs the elements
     * from the xml, sets the adapter, and sets
     * the hamburger button.
     */
    private void setupNavigationDrawer()
    {
        drawerLayout = (DrawerLayout) findViewById(R.id.nav_drawer_layout);
        navDrawerItems = getResources().getStringArray(R.array.nav_drawer_items_array);
        drawerLayout = (DrawerLayout) findViewById(R.id.nav_drawer_layout);
        drawerListView = (ListView) findViewById(R.id.left_nav_drawer_list);

        // Set the adapter for the list view
        drawerListView.setAdapter(new NavigationDrawerAdapter(this));
        drawerToggleListener = getDrawerToggleLogic();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        drawerLayout.setDrawerListener(drawerToggleListener);
        drawerToggleListener.syncState();
    }

    /**
     * Creates the logic that fires when the navigation
     * drawer opens or closes.
     */
    private ActionBarDrawerToggle getDrawerToggleLogic()
    {
        return new ActionBarDrawerToggle(this, drawerLayout, R.string.open_drawer, R.string.close_drawer)
        {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                invalidateOptionsMenu();
            }
        };
    }

    /**
     * This helper method allows a developer to switch
     * the Fragment in the MainActivity.
     * @param fragment - Fragment to display to the user.
     */
    public void setContent(Fragment fragment)
    {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.addToBackStack(null);
        transaction.replace(R.id.content_frame, fragment).commit();
    }

    /*
     This is the "Adapter" class for the Navigation Drawer.
     This class is constructed and attached to the drawer.
     It reads the string array that holds all of the
     drawer options, displays the options, and handles
     user click events.
     */
    private class NavigationDrawerAdapter extends BaseAdapter
    {
        private Context currentContext;
        public NavigationDrawerAdapter(Context context)
        {
            currentContext = context;
        }
         /**
         * This is how the ListView knows how many
         * cells there are in the ListView.
         * @return - int representing all items in the ListView
         */
        @Override
        public int getCount() {
            return navDrawerItems.length;
        }

        /**
         * This method is supposed to return the object
         * that the cell represents. Since our cells represent
         * entire modules, we just return the strings.
         * @param position
         * @return String name of Study Buddy module
         */
        @Override
        public Object getItem(int position) {
            return navDrawerItems[position];
        }

        /**
         * Below method tells the OS that the IDs don't really
         * mean anything. Allows us to do fun stuff with the
         * navigation drawer later. This is technically more
         * expensive than providing our own IDs, but not
         * expensive enough to effect performance on even
         * low-end devices.
         * @return If item IDs are stable or not. false = unstable
         */
        @Override
        public boolean hasStableIds() {
            return false;
        }

        /**
         * Returns a unique ID for an
         * @param position - provided by BaseAdapter superclass
         * @return unique ID
         */
        @Override
        public long getItemId(int position) {
            return position;
        }

        /**
         * This method recycles cell views and populates each
         * cell with data.
         * @param position
         * @param convertView
         * @param parent
         * @return - The view to use in the list
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View root = convertView;
            if(root == null)
            {
                LayoutInflater inflater = getLayoutInflater();
                root = inflater.inflate(R.layout.layout_drawer_list_item, parent, false);
            }
            ImageView navDrawerItemImage = (ImageView) root.findViewById(R.id.nav_drawer_item_image);
            TextView navDrawerItemText = (TextView) root.findViewById(R.id.nav_drawer_item_name);
            navDrawerItemText.setText(navDrawerItems[position]);
            navDrawerItemText.setTextColor(Color.WHITE);
            navDrawerItemImage.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.nav_dawer_home_icon));
            root.setOnClickListener(getNewNavDrawerItemClickListener(currentContext, navDrawerItems[position]));
            return root;
        }

        private View.OnClickListener getNewNavDrawerItemClickListener(final Context context, final String navDrawerItemName)
        {
            return new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Temporarily will only toast user with item name.
                    final Fragment newFragment;
                    if (navDrawerItemName.equalsIgnoreCase("home"))
                        newFragment = new HomeFragment();
                    else if (navDrawerItemName.equalsIgnoreCase("review session"))
                    {
                        newFragment = new ReviewModuleFragment();
                    }
                    else if (navDrawerItemName.equalsIgnoreCase("me"))
                    {
                        newFragment = new MeListFragment();
                    }
                    else if (navDrawerItemName.equalsIgnoreCase("search"))
                    {
                        newFragment = new SearchFragment();
                    }
                    else {
                        Toast.makeText(context, navDrawerItemName, Toast.LENGTH_LONG).show();
                        drawerLayout.closeDrawer(Gravity.LEFT);
                        return;
                    }
                    drawerLayout.closeDrawer(Gravity.LEFT);
                    setContent(newFragment);
                }
            };
        }
    }

    private void startDefaultFragment()
    {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content_frame, new HomeFragment()).commit();
    }

    private void checkLogin()
    {
        if(googleSignInClient == null)
            setupGoogleApiClient();
        if(!googleSignInClient.isConnected())
            pickUserAccount();

    }

    private void setupGoogleApiClient()
    {
        googleSignInClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .build();
    }

    /********************************************
     * LOG IN CODE
     */

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
        Toast.makeText(this, "Please select a Google Account to log in with", Toast.LENGTH_LONG).show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
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
        } catch (Exception e)
        {
            e.printStackTrace();
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
                Log.e("Token Fetching error:", e.toString());
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //Toast.makeText(mActivity, token, Toast.LENGTH_LONG).show();
            if(token != null)
                Log.e("Access Token:", token);
            else
                Log.e("Access Token:", "Is null");
            Log.i("Log in:", "Logging user in now.");

            if(sharedPrefsUtil == null)
                sharedPrefsUtil = new SharedPrefsUtil(mActivity);
            sharedPrefsUtil.getSharedPrefs().edit().putString(LaunchActivity.SHARED_PREF_LOG_IN_INFO_KEY, token).apply();
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
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        VolleyLog.e("Volley Error:", volleyError.getMessage());
                    }
                });
    }
}
