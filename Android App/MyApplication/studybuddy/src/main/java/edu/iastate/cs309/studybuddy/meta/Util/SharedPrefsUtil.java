package edu.iastate.cs309.studybuddy.meta.Util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Tyler on 2/18/2015.
 */
public class SharedPrefsUtil
{

    SharedPreferences sharedPrefs;

    private String sharedPrefsKey = "get shared prefs";

    public SharedPrefsUtil(Context context)
    {
        if (sharedPrefs == null)
            sharedPrefs = context.getSharedPreferences(sharedPrefsKey, Context.MODE_PRIVATE);
    }

    public SharedPreferences getSharedPrefs()
    {
        return sharedPrefs;
    }

}
