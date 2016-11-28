package edu.iastate.cs309.studybuddy.meta.Util;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import edu.iastate.cs309.studybuddy.meta.model.StudySession;

/**
 * Created by Tyler on 4/19/2015.
 */
public class DataUtil {

    private DataUtil()
    {

    }

    public static void parseSessionsFromString(String response, ArrayList<StudySession> collectionOfSessions)
    {
        try{
            // Get array of future attendances and parse to add to card view
            JSONObject json = new JSONObject(response);
            JSONArray sessions = new JSONArray();
            json.toJSONArray(sessions);
            String className, description, location;
            for( int i = 0; i < sessions.length(); i++ )
            {
                JSONObject session = sessions.getJSONObject(i);
                className = session.getString("class_name");
                description = session.getString("description");
                location = session.getString("location");
                StudySession tempSession = new StudySession(className, description, location, session.getInt("id"));
                tempSession.setIsReviewed(session.getBoolean("has_review"));
                collectionOfSessions.add(tempSession);
            }
            Log.e("Home Fragment", "Response returned");
        } catch(JSONException e) {
            e.printStackTrace();
        }
    }

}
