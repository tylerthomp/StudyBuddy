package edu.iastate.cs309.studybuddy.meta.model;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Tyler on 2/9/2015.
 */
public class StudySession {


    /*****************************************************
     * This class will be filled out as requirements become clearer.
     *****************************************************/

    private String title;
    private String description;
    private String location;
    private int ID;
    private int attendanceId;
    private boolean isReviewed = false;
    private LatLng locationLatLng = new LatLng(42.026618,-93.646466);

    public StudySession()
    {

    }

    public StudySession(JSONObject session, String errorTag)
    {
        try {
            Log.e("Session Json" + errorTag, session.toString());
            ID = session.getJSONObject("study_session").getInt("id");
            title = session.getJSONObject("study_session").getJSONObject("parent_class").getString("class_prefix") + " " + session.getJSONObject("study_session").getJSONObject("parent_class").getInt("class_number");
            description = session.getJSONObject("study_session").getString("description");
            location = session.getJSONObject("study_session").getString("location");
            Log.e("Study lat", session.getJSONObject("study_session").getDouble("latitude")+"");
            Log.e("Study Long", session.getJSONObject("study_session").getDouble("longitude")+"");
            locationLatLng = new LatLng(session.getJSONObject("study_session").getDouble("latitude"), session.getJSONObject("study_session").getDouble("longitude"));
            if(session.has("has_review"))
                isReviewed = session.getBoolean("has_review");


        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void parseNonNestedJson(JSONObject session, String errorTag)
    {
        try {
            Log.e("Session Json" + errorTag, session.toString());
            ID = session.getInt("id");
            Log.e("Session Json id"+errorTag, ID+"");
            title = session.getJSONObject("parent_class").getString("class_prefix") + " " + session.getJSONObject("parent_class").getInt("class_number");
            description = session.getString("description");
            location = session.getString("location");
            Log.e("Study lat", session.getDouble("latitude")+"");
            Log.e("Study Long", session.getDouble("longitude")+"");
            locationLatLng = new LatLng(session.getDouble("latitude"), session.getDouble("longitude"));
            if(session.has("has_review"))
                isReviewed = session.getBoolean("has_review");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public StudySession(String newTitle)
    {
        title = newTitle;
    }

    public StudySession(String newTitle, String newDescription)
    {
        title = newTitle;
        description = newDescription;
    }

    public StudySession(String newTitle, String newDescription, String newLocation)
    {
        title = newTitle;
        description = newDescription;
        location = newLocation;
    }

    public StudySession(String newTitle, String newDescription, String newLocation, int id)
    {
        title = newTitle;
        description = newDescription;
        location = newLocation;
        ID = id;
    }

    public String getTitle()
    {
        return title;
    }

    public String getDescription()
    {
        return description;
    }

    public String getLocation()
    {
        return location;
    }

    public int getID() { return ID;}

    public void setID(int id) {ID=id;}

    public void setIsReviewed(boolean hasBeenReviewed) {isReviewed = hasBeenReviewed;}

    public boolean hasBeenReviewed() {return isReviewed;}

    public int getAttendanceId() {
        return attendanceId;
    }

    public void setAttendanceId(int attendanceId) {
        this.attendanceId = attendanceId;
    }

    public LatLng getLocationLatLng() {
        return locationLatLng;
    }

    public void setLocationLatLng(LatLng locationLatLng) {
        this.locationLatLng = locationLatLng;
    }
}
