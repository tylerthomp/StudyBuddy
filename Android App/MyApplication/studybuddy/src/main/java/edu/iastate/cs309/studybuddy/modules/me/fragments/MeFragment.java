package edu.iastate.cs309.studybuddy.modules.me.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import edu.iastate.cs309.studybuddy.R;
import edu.iastate.cs309.studybuddy.meta.LaunchActivity;
import edu.iastate.cs309.studybuddy.meta.MainActivity;
import edu.iastate.cs309.studybuddy.meta.StudyBuddyFragment;
import edu.iastate.cs309.studybuddy.meta.Util.SharedPrefsUtil;
import edu.iastate.cs309.studybuddy.meta.Util.VolleyUtil;
import edu.iastate.cs309.studybuddy.modules.me.model.Classes;
import edu.iastate.cs309.studybuddy.modules.me.model.OptimalTime;

/**
 * Created by Tyler on 4/22/2015.
 */
public class MeFragment extends StudyBuddyFragment {

    View rootView;
    ListView myList;
    MeAdapter adapter;
    ArrayList<OptimalTime> availabilities = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_me, container, false);
        myList = (ListView) rootView.findViewById(R.id.view_list_me);
        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_availabilities, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.menu_availabilities_add_availability) {
            ((MainActivity) getActivity()).setContent(new AddAvailabilityFragment());
            return true;
        }

        return false;
    }

    @Override
    public void onStart() {
        super.onStart();
        VolleyUtil.getInstance(getActivity()).addToRequestQueue(getUsersAvailabilities());
        myList.setAdapter(adapter = new MeAdapter());
    }

    private StringRequest getUsersAvailabilities()
    {
        return new StringRequest(Request.Method.POST, getActivity().getString(R.string.base_api_url) + "/api/Availability/current",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        try {
                            availabilities.clear();
                            JSONArray array = new JSONArray(s);
                            for (int i = 0; i < array.length(); i++)
                                availabilities.add(new OptimalTime(array.getJSONObject(i)));
                            adapter.notifyDataSetChanged();
                        } catch (JSONException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("access_token", new SharedPrefsUtil(getActivity()).getSharedPrefs().getString(LaunchActivity.SHARED_PREF_LOG_IN_INFO_KEY, ""));
                return params;
            }
        };
    }

    private class MeAdapter extends BaseAdapter
    {

        @Override
        public int getCount() {
            return availabilities.size();
        }

        @Override
        public Object getItem(int position) {
            return availabilities.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if(view == null)
            {
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.view_availability_list_view_cell, parent, false);
            }

            TextView weekDay = (TextView) view.findViewById(R.id.text_view_day_of_week);
            TextView fromTime = (TextView) view.findViewById(R.id.text_view_me_from_time);
            TextView toTime = (TextView) view.findViewById(R.id.text_view_me_to_time);

            weekDay.setText(availabilities.get(position).getDayOfWeek());
            fromTime.setText(getTextTimeFromInt(availabilities.get(position).getFromHour(), availabilities.get(position).getFromMinute()));
            toTime.setText(getTextTimeFromInt(availabilities.get(position).getToHour(), availabilities.get(position).getToMinute()));
            view.setOnClickListener(getRemoveDialog(availabilities.get(position)));

            return view;
        }

        private String getTextTimeFromInt(int hour, int minute)
        {
            String amOrPm = "AM";
            int newHour = hour;
            if(newHour > 12){
                newHour -= 12;
                amOrPm = "PM";
            }

            if(minute < 10)
                return newHour + ":0" + minute + " " + amOrPm;
            else
                return newHour + ":" + minute + " " + amOrPm;
        }

        private View.OnClickListener getRemoveDialog(final OptimalTime time)
        {

            return new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog removeDialog = new AlertDialog.Builder(getActivity())
                            .setTitle("Remove Time")
                            .setCancelable(true)
                            .setMessage("Would you like to remove this time from your availability list?")
                            .setNeutralButton("Remove", getRemoveAvailabilityClickListener(time))
                            .show();
                }
            };
        }

        private AlertDialog.OnClickListener getRemoveAvailabilityClickListener(final OptimalTime time)
        {
            return new AlertDialog.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(which == AlertDialog.BUTTON_NEUTRAL)
                    {
                        availabilities.remove(time);
                        VolleyUtil.getInstance(getActivity()).addToRequestQueue(getRemoveAvailabilitysRequest(time));
                        dialog.dismiss();
                    }

                }
            };
        }

        private StringRequest getRemoveAvailabilitysRequest(final OptimalTime time)
        {
            availabilities.remove(time);
            return new StringRequest(Request.Method.POST, getString(R.string.base_api_url) + "/api/Availability/set",
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String s) {
                            Toast.makeText(getActivity(), "Time Successfully Removed",Toast.LENGTH_LONG).show();
                            VolleyUtil.getInstance(getActivity()).addToRequestQueue(getUsersAvailabilities());
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            Toast.makeText(getActivity(), "Error Removing Time", Toast.LENGTH_LONG).show();
                        }
                    }){
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String,String> params = new HashMap<>();
                    params.put("access_token", new SharedPrefsUtil(getActivity()).getSharedPrefs().getString(LaunchActivity.SHARED_PREF_LOG_IN_INFO_KEY, ""));
                    params.put("payload", buildJSONFromAvailabilities());
                    return params;
                }
            };
        }

        private String buildJSONFromAvailabilities()
        {
            String json = "[";
            for(int i = 0; i < availabilities.size(); i++)
            {
                OptimalTime time = availabilities.get(i);
                json += "{\"day\": " + getIntForDayOfWeek(time) + ",";
                json += "\"start\": \"" + time.getFromHour() + ":" + time.getFromMinute() + "\", ";
                json += "\"end\": \"" + time.getToHour() + ":" + time.getToMinute() + "\"}";
                if(i < availabilities.size()-1)
                    json += ",";

            }
            json += "]";
            return json;
        }

        private int getIntForDayOfWeek(OptimalTime time)
        {
            if(time.getDayOfWeek().equalsIgnoreCase("monday"))
                return 0;
            else if(time.getDayOfWeek().equalsIgnoreCase("tuesday"))
                return 1;
            else if(time.getDayOfWeek().equalsIgnoreCase("wednesday"))
                return 2;
            else if(time.getDayOfWeek().equalsIgnoreCase("thursday"))
                return 3;
            else if(time.getDayOfWeek().equalsIgnoreCase("friday"))
                return 4;
            else if(time.getDayOfWeek().equalsIgnoreCase("saturday"))
                return 5;
            else
                return 6;
        }
    }
}
