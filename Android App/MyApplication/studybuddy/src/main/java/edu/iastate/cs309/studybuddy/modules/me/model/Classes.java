package edu.iastate.cs309.studybuddy.modules.me.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Sonic on 4/25/2015.
 */
public class Classes {

    private String prefix;
    private int number;

    public Classes()
    {

    }

    public Classes(JSONObject object)
    {
        try {
            prefix = object.getString("class_prefix");
            number = object.getInt("class_number");
        } catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    public Classes(String newPrefix, int classNumber)
    {
        prefix = newPrefix;
        number = classNumber;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}
