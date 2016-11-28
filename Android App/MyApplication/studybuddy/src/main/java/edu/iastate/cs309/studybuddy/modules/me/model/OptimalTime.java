package edu.iastate.cs309.studybuddy.modules.me.model;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Sonic on 4/22/2015.
 */
public class OptimalTime {

    private String dayOfWeek;
    private int fromHour;
    private int fromMinute;
    private int fromSecond;
    private int toHour;
    private int toMinute;
    private int toSecond;

    public OptimalTime()
    {

    }

    public OptimalTime(JSONObject object)
    {
        try {
            Log.e("Time Json", object.toString());
            dayOfWeek = getDayOfWeekFromInt(object.getInt("day"));
            fromHour = Integer.parseInt(object.getString("start").substring(0, 2));
            fromMinute = Integer.parseInt(object.getString("start").substring(3));
            toHour = Integer.parseInt(object.getString("end").substring(0, 2));
            toMinute = Integer.parseInt(object.getString("start").substring(3));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String getDayOfWeekFromInt(int day)
    {
        if(day == 0)
            return "Monday";
        if(day == 1)
            return "Tuesday";
        if(day == 2)
            return "Wednesday";
        if(day == 3)
            return "Thursday";
        if(day == 4)
            return "Friday";
        if(day == 5)
            return "Saturday";
        if(day == 6)
            return "Sunday";

        return "Error";
    }

    public int getFromHour() {
        return fromHour;
    }

    public void setFromHour(int fromHour) {
        this.fromHour = fromHour;
    }

    public int getFromMinute() {
        return fromMinute;
    }

    public void setFromMinute(int fromMinute) {
        this.fromMinute = fromMinute;
    }

    public int getFromSecond() {
        return fromSecond;
    }

    public void setFromSecond(int fromSecond) {
        this.fromSecond = fromSecond;
    }

    public int getToHour() {
        return toHour;
    }

    public void setToHour(int toHour) {
        this.toHour = toHour;
    }

    public int getToMinute() {
        return toMinute;
    }

    public void setToMinute(int toMinute) {
        this.toMinute = toMinute;
    }

    public int getToSecond() {
        return toSecond;
    }

    public void setToSecond(int toSecond) {
        this.toSecond = toSecond;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }
}
