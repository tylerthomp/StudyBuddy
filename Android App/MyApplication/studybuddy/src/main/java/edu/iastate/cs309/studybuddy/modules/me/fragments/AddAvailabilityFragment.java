package edu.iastate.cs309.studybuddy.modules.me.fragments;

import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import edu.iastate.cs309.studybuddy.R;
import edu.iastate.cs309.studybuddy.meta.LaunchActivity;
import edu.iastate.cs309.studybuddy.meta.MainActivity;
import edu.iastate.cs309.studybuddy.meta.StudyBuddyFragment;
import edu.iastate.cs309.studybuddy.meta.Util.SharedPrefsUtil;
import edu.iastate.cs309.studybuddy.meta.Util.VolleyUtil;

/**
 * Created by Tyler on 4/25/2015.
 */
public class AddAvailabilityFragment extends StudyBuddyFragment {

    View rootView;
    Spinner weekdaySpinner;
    TextView submit;
    String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

    int dayOfWeek;

    int startHour;
    int startMinute;
    TextView timePickerStart;

    int endHour;
    int endMinute;
    TextView timePickerEnd;

    SpinnerAdapter spinnerAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_add_availability, container, false);
        weekdaySpinner = (Spinner) rootView.findViewById(R.id.add_availability_weekday_spinner);
        timePickerStart = (TextView) rootView.findViewById(R.id.add_availability_start_time_picker);
        timePickerEnd = (TextView) rootView.findViewById(R.id.add_availability_end_time_picker);
        submit = (TextView) rootView.findViewById(R.id.add_availability_submit);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        timePickerStart.setOnClickListener(getTimePickerClickListenerStart());
        timePickerEnd.setOnClickListener(getTimePickerClickListenerEnd());
        submit.setOnClickListener(getSubmitListener());
        weekdaySpinner.setAdapter(spinnerAdapter = new SpinnerAdapter());
    }

    private View.OnClickListener getSubmitListener()
    {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VolleyUtil.getInstance(getActivity()).addToRequestQueue(getCurrentAvailabilitiesAndSendNewOne());
                ((MainActivity) getActivity()).setContent(new MeFragment());
            }
        };
    }

    private StringRequest getCurrentAvailabilitiesAndSendNewOne()
    {
        return new StringRequest(Request.Method.POST, getActivity().getString(R.string.base_api_url) + "/api/Availability/current",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        VolleyUtil.getInstance(getActivity()).addToRequestQueue(sendNewAvailability(s));
                        Log.e("Avail. old JSON", s);
                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("access_token", new SharedPrefsUtil(getActivity()).getSharedPrefs().getString(LaunchActivity.SHARED_PREF_LOG_IN_INFO_KEY, ""));
                return params;
            }
        };
    }

    private StringRequest sendNewAvailability(final String oldJson)
    {
        String string;
        return new StringRequest(Request.Method.POST, getActivity().getString(R.string.base_api_url) + "/api/Availability/set",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        Toast.makeText(getActivity(), "New Availability Added", Toast.LENGTH_LONG).show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Toast.makeText(getActivity(), "Failed to Add Availability", Toast.LENGTH_LONG).show();
                Log.e("Avail. New Json", appendNewAvailabilityToJson(oldJson));
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("access_token", new SharedPrefsUtil(getActivity()).getSharedPrefs().getString(LaunchActivity.SHARED_PREF_LOG_IN_INFO_KEY, ""));
                params.put("payload", appendNewAvailabilityToJson(oldJson));
                return params;
            }
        };
    }

    private String replaceLast(String string, String from, String to) {
        int lastIndex = string.lastIndexOf(from);
        if (lastIndex < 0) return string;
        String tail = string.substring(lastIndex).replaceFirst(from, to);
        return string.substring(0, lastIndex) + tail;
    }

    private String appendNewAvailabilityToJson(String json)
    {
        dayOfWeek = weekdaySpinner.getSelectedItemPosition();

        if(json.length() < 3)
            return json.replace("]", "{" + "\"day\":" + dayOfWeek +
                    ",\"start\":\"" + startHour + ":" + appendZeroIfNecessary(startMinute) +
                    "\",\"end\":\"" + endHour + ":" + appendZeroIfNecessary(endMinute) +"\"}]");
        else
            return json.replace("]", ",{" + "\"day\":" + dayOfWeek +
                    ",\"start\":\"" + startHour + ":" + appendZeroIfNecessary(startMinute) +
                    "\",\"end\":\"" + endHour + ":" + appendZeroIfNecessary(endMinute) +"\"}]");
    }

    private String appendZeroIfNecessary(int minute)
    {
        if(minute < 10)
            return "0"+minute;
        else
            return String.valueOf(minute);
    }

    private View.OnClickListener getTimePickerClickListenerStart()
    {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupStartTimePickerDialog();
            }
        };
    }

    private View.OnClickListener getTimePickerClickListenerEnd()
    {
        return new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                popupEndTimePickerDialog();
            }
        };
    }

    private void popupStartTimePickerDialog()
    {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog startPicker = new TimePickerDialog(getActivity(),
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        startHour = hourOfDay;
                        startMinute = minute;
                        timePickerStart.setText(getAmPmFor24HourTime(startHour, startMinute));
                    }
                }, calendar.get(Calendar.HOUR), calendar.get(Calendar.MINUTE), false);
        startPicker.show();
    }

    private void popupEndTimePickerDialog()
    {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog endPicker = new TimePickerDialog(getActivity(),
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        endHour = hourOfDay;
                        endMinute = minute;
                        timePickerEnd.setText(getAmPmFor24HourTime(endHour,endMinute));
                    }
                }, calendar.get(Calendar.HOUR), calendar.get(Calendar.MINUTE), false);
        endPicker.show();
    }

    private String getAmPmFor24HourTime(int hour, int minute)
    {
        String amOrPm = "AM";
        int tempHour = hour;
        if(tempHour > 11)
        {
            amOrPm = "PM";
            if(tempHour > 12)
                tempHour -= 12;
        }
        String minutes = "";
        if(minute < 10)
            minutes = "0" + minute;
        else
            minutes = minute+"";
        return tempHour + ":" + minutes + amOrPm;
    }

    class SpinnerAdapter extends BaseAdapter
    {

        @Override
        public int getCount() {
            return days.length;
        }

        @Override
        public Object getItem(int position) {
            return days[position];
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
            View view;
            if(convertView == null)
            {
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.view_spinner_item, parent, false);
            }
            else view = convertView;

            ((TextView) view.findViewById(R.id.spinner_item_text)).setText(days[position]);
            return view;
        }
    }
}
