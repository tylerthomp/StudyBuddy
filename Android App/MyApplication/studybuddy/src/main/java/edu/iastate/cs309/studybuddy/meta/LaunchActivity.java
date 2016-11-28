package edu.iastate.cs309.studybuddy.meta;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import edu.iastate.cs309.studybuddy.R;
import edu.iastate.cs309.studybuddy.meta.Util.SharedPrefsUtil;

/**
 * Created by Tyler on 2/15/2015.
 */
public class LaunchActivity extends ActionBarActivity {

    Context context;
    SharedPrefsUtil sharedPrefs;
    public static String SHARED_PREF_LOG_IN_INFO_KEY = "get login info";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        context = this;
    }

    @Override
    protected void onStart() {
        super.onStart();
        getPauseAsyncTask().execute(new Object());
    }

    private Intent getNextActivity()
    {
        Intent nextActivity;
//        if(!isSignedIn())
//            nextActivity = new Intent(this, SignInActivity.class);
//        else
            nextActivity = new Intent(this, MainActivity.class);

        return nextActivity;
    }

    private boolean isSignedIn()
    {
        if(sharedPrefs == null)
            sharedPrefs = new SharedPrefsUtil(this);
        String signedIn = sharedPrefs.getSharedPrefs().getString(SHARED_PREF_LOG_IN_INFO_KEY, "");

        if(signedIn.isEmpty())
            return false;

        return true;
    }

    private AsyncTask<Object,Object,Object> getPauseAsyncTask() {
        return new AsyncTask() {

            @Override
            protected Object doInBackground(Object[] params) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
                return new Object();
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                startActivity(getNextActivity());
            }
        };
    }
}
