package edu.iastate.cs309.studybuddy.modules.createsession.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import edu.iastate.cs309.studybuddy.R;
import edu.iastate.cs309.studybuddy.meta.LaunchActivity;
import edu.iastate.cs309.studybuddy.meta.MainActivity;
import edu.iastate.cs309.studybuddy.meta.StudyBuddyFragment;
import edu.iastate.cs309.studybuddy.meta.Util.SharedPrefsUtil;
import edu.iastate.cs309.studybuddy.meta.Util.VolleyUtil;
import edu.iastate.cs309.studybuddy.modules.home.fragments.HomeFragment;

public class CreateSessionFragment extends StudyBuddyFragment implements DatePickerDialog.OnDateSetListener {

    ArrayList<String> usableClassPrefixes = new ArrayList<>();

    View rootView;

    Spinner classPrefixSpinner;
    PrefixSpinnerAdapter adapter;

    EditText editTitle;
    EditText editClassName;
    EditText editLocation;
    EditText editMaxMember;
    EditText editClassNumber;

    TextView startDate;
    TextView endDate;

    Button submit;
    CardView card;

    SharedPrefsUtil sharedPrefs;

    int startYear;
    int startDay;
    int startMonth;
    int startHour;
    int startMinute;

    int endYear;
    int endDay;
    int endMonth;
    int endHour;
    int endMinute;

    private TextView timePicker1;
    private TextView timePicker2;

    private ImageView staticMap;
    private static LatLng center = new LatLng(42.026618,-93.646466);
    private float mapZoom = 15.0f;
    private String staticMapBaseUrl = "https://maps.googleapis.com/maps/api/staticmap?&zoom=15&size=600x400";

    public static final String LAT_KEY = "CREATE SESSION LAT KEY";
    public static final String LONG_KEY = "CREATE SESSION LONG KEY";

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_create_session, container, false);
        staticMap = (ImageView) rootView.findViewById(R.id.create_session_text_view_map_location_get_map);
        editTitle = (EditText) rootView.findViewById(R.id.create_session_title_edit);
        classPrefixSpinner = (Spinner) rootView.findViewById(R.id.create_session_class_prefix_spinner);
        editClassName = (EditText) rootView.findViewById(R.id.create_session_edit_text_class_name);
        editLocation = (EditText) rootView.findViewById(R.id.create_session_edit_text_location);
        editMaxMember = (EditText) rootView.findViewById(R.id.create_session_edit_text_max_attendees);
        submit = (Button) rootView.findViewById(R.id.create_session_submit);
        card = (CardView) rootView.findViewById(R.id.create_session_container_card);
        editClassNumber = (EditText) rootView.findViewById(R.id.create_session_edit_text_class_number);
        sharedPrefs = new SharedPrefsUtil(getActivity());
        startDate = (TextView) rootView.findViewById(R.id.create_session_start_date_picker);
        endDate = (TextView) rootView.findViewById(R.id.create_session_end_date_picker);
        timePicker1 = (TextView) rootView.findViewById(R.id.timePicker1);
        timePicker2 = (TextView) rootView.findViewById(R.id.timePicker2);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        submit.setOnClickListener(getSubmitButtonListener());
        card.setPreventCornerOverlap(true);
        card.setCardElevation(20);
        startDate.setOnClickListener(getDatePickerClickListener(this, true));
        endDate.setOnClickListener(getDatePickerClickListener(this, false));
        timePicker1.setOnClickListener(getTimePickerClickListenerStart());
        timePicker2.setOnClickListener(getTimePickerClickListenerEnd());
        staticMap.setScaleType(ImageView.ScaleType.CENTER_CROP);
        VolleyUtil.getInstance(getActivity()).addToRequestQueue(getPrefixes());
        setupClassPrefixSpinner();
        VolleyUtil.getInstance(getActivity()).addToRequestQueue(getStaticMap());
        staticMap.setOnClickListener(getMapFragment());

    }

    private void setupClassPrefixSpinner() {
        adapter = new PrefixSpinnerAdapter(usableClassPrefixes, getActivity());
        classPrefixSpinner.setAdapter(adapter);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        if (DatePickerDialogFragment.isStartDate) {
            startYear = year;
            startDay = dayOfMonth;
            startMonth = monthOfYear + 1;
            Log.e("Month int start", startMonth+"");

            startDate.setText(convertIntToMonthOfYearString(endMonth) + " " + dayOfMonth + ", " + year);
        } else if (!DatePickerDialogFragment.isStartDate) {
            endYear = year;
            endDay = dayOfMonth;
            endMonth = monthOfYear+1;
            Log.e("Month int end", endMonth+"");
            endDate.setText(convertIntToMonthOfYearString(endMonth) + " " + dayOfMonth + ", " + year);
        }
    }

    private String convertIntToMonthOfYearString(int monthOfYear) {
        String month = "";
        if (monthOfYear == 1)
            month = "January";
        else if (monthOfYear == 2)
            month = "February";
        else if (monthOfYear == 3)
            month = "March";
        else if (monthOfYear == 4)
            month = "April";
        else if (monthOfYear == 5)
            month = "May";
        else if (monthOfYear == 6)
            month = "June";
        else if (monthOfYear == 7)
            month = "July";
        else if (monthOfYear == 8)
            month = "August";
        else if (monthOfYear == 9)
            month = "September";
        else if (monthOfYear == 10)
            month = "October";
        else if (monthOfYear == 11)
            month = "November";
        else if (monthOfYear == 12)
            month = "December";
        return month;
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

    private class PrefixSpinnerAdapter extends BaseAdapter
    {
        ArrayList<String> prefixList;
        Context context;

        public PrefixSpinnerAdapter(ArrayList<String> availablePrefixes, Context currentContext)
        {
            prefixList = availablePrefixes;
            context = currentContext;
        }


        @Override
        public int getCount() {
            return prefixList.size();
        }

        @Override
        public Object getItem(int position) {
            return prefixList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if(view == null)
                view = new TextView(context);

            ((TextView) view).setText(prefixList.get(position));

            return view;
        }
    }

    private View.OnClickListener getSubmitButtonListener(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editClassName.getText().toString().isEmpty()) {
                    Toast.makeText(getActivity(), "Please enter a class name.", Toast.LENGTH_LONG).show();
                    return;
                }
                if(editLocation.getText().toString().isEmpty()) {
                    Toast.makeText(getActivity(), "Please enter a location.", Toast.LENGTH_LONG).show();
                    return;
                }

                if(editMaxMember.getText().toString().isEmpty()) {
                    Toast.makeText(getActivity(), "Please enter a maximum member requirement.", Toast.LENGTH_LONG).show();
                    return;
                }

                if(editTitle.getText().toString().isEmpty())
                {
                    Toast.makeText(getActivity(), "Please enter a study session title.", Toast.LENGTH_LONG).show();
                    return;
                }

                VolleyUtil.getInstance(getActivity()).addToRequestQueue(sendJSON(buildJSON(editTitle.getText().toString(),
                        classPrefixSpinner.getSelectedItem().toString(),
                        editClassNumber.getText().toString(),
                        editLocation.getText().toString(),
                        editMaxMember.getText().toString())));


            }
        };

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
                        timePicker1.setText(getAmPmFor24HourTime(startHour, startMinute));
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
                        timePicker2.setText(getAmPmFor24HourTime(endHour,endMinute));
                    }
                }, calendar.get(Calendar.HOUR), calendar.get(Calendar.MINUTE), false);
        endPicker.show();
    }

    private View.OnClickListener getDatePickerClickListener(final Fragment frag, final boolean isStart)
    {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                android.support.v4.app.DialogFragment newFragment = new DatePickerDialogFragment(frag, isStart);
                newFragment.show(ft, "Choose day of session");
            }
        };
    }

    private String buildJSON(String studySessionTitle, String studySessionClassPrefix, String studySessionClassNumber, String studySessionLocationString, String maxAttendees)
    {
            return "{ \"class\": { \"class_prefix\" : \"" + studySessionClassPrefix + "\", \"class_number\": \"" + studySessionClassNumber + "\"},\n" +
                    "  \"description\": \"" + studySessionTitle + "\",\n" +
                    "  \"location\": \"" + studySessionLocationString + "\",\n" +
                    "  \"latitude\": \""+center.latitude+"\"," +
                    "  \"longitude\": \""+center.longitude+"\"," +
                    "  \"start_time\": \"" + startYear + "-" + startMonth + "-"+ startDay +"T" + String.valueOf(startHour)+ ":" + String.valueOf(startMinute) + ":" + "00" + "\",\n" +
                    "  \"end_time\": \"" + endYear+"-"+endMonth+"-"+endDay+"T"+ String.valueOf(endHour) + ":" + String.valueOf(endMinute) + ":" + "00" + "\"\n" +
                    "}";
    }

    private StringRequest sendJSON(final String json)
    {
        String url = getActivity().getString(R.string.base_api_url) + "/api/StudySession/create";

        return new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                Toast.makeText(getActivity(), "Session Created", Toast.LENGTH_LONG).show();
                ((MainActivity) getActivity()).setContent(new HomeFragment());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Toast.makeText(getActivity(), "Unfortunately, there was an error. Please try again later", Toast.LENGTH_LONG).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("access_token", sharedPrefs.getSharedPrefs().getString(LaunchActivity.SHARED_PREF_LOG_IN_INFO_KEY, ""));
                params.put("payload", json);
                return params;
            }
        };

    }

    private StringRequest getPrefixes()
    {
        String url = getActivity().getString(R.string.base_api_url) + "/api/Class/prefixes";

        return new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {

                    @Override
                    public void onResponse(String string) {

                        try {
                            JSONArray jsonArray = new JSONArray(string);
                            for(int i = 0; i < jsonArray.length(); i++)
                                    usableClassPrefixes.add(jsonArray.getString(i));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        adapter.notifyDataSetChanged();


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        VolleyLog.e("Error getting class prefixes:", volleyError.getMessage());
                    }
                })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("access_token", sharedPrefs.getSharedPrefs().getString(LaunchActivity.SHARED_PREF_LOG_IN_INFO_KEY, ""));
                return params;
            }
        };
    }

    private Fragment buildGoogleMap() {
        final Bundle bundle = new Bundle();
        GoogleMapOptions options = new GoogleMapOptions();
        options.camera(new CameraPosition(new LatLng(42.026618, -93.646466), 15.0f, 0, 0));
        options.mapToolbarEnabled(true);
        options.compassEnabled(true);
        final SupportMapFragment fragment = SupportMapFragment.newInstance(options);
        ((SupportMapFragment) fragment).getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {

                googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(final Marker marker) {
                        final AlertDialog alert = new AlertDialog.Builder(fragment.getActivity())
                                .setTitle("Use Location?")
                                .setMessage("Would you like to use this location for the Study Session?")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (which == AlertDialog.BUTTON_POSITIVE)
                                        {
                                            Toast.makeText(getActivity(), marker.getPosition().latitude + " " + marker.getPosition().longitude, Toast.LENGTH_LONG).show();
                                            center = new LatLng(marker.getPosition().latitude, marker.getPosition().longitude);
                                            ((MainActivity) getActivity()).setContent(new CreateSessionFragment());
                                        }
                                        else if (which == AlertDialog.BUTTON_NEUTRAL)
                                            dialog.dismiss();
                                        else if (which == AlertDialog.BUTTON_NEGATIVE)
                                            marker.remove();
                                    }
                                })
                                .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (which == AlertDialog.BUTTON_POSITIVE) {
                                            Toast.makeText(getActivity(), marker.getPosition().latitude + marker.getPosition().longitude + "", Toast.LENGTH_LONG).show();
                                            center = new LatLng(marker.getPosition().latitude, marker.getPosition().longitude);
                                            ((MainActivity) getActivity()).setContent(new CreateSessionFragment());
                                        }
                                        else if (which == AlertDialog.BUTTON_NEUTRAL)
                                            ;
                                        else if (which == AlertDialog.BUTTON_NEGATIVE)
                                            marker.remove();
                                    }
                                })
                                .setNegativeButton("Remove", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (which == AlertDialog.BUTTON_POSITIVE)
                                        {
                                            Toast.makeText(getActivity(), marker.getPosition().latitude + marker.getPosition().longitude + "", Toast.LENGTH_LONG).show();
                                            center = new LatLng(marker.getPosition().latitude, marker.getPosition().longitude);
                                            ((MainActivity) getActivity()).setContent(new CreateSessionFragment());
                                        }
                                        else if (which == AlertDialog.BUTTON_NEUTRAL)
                                            ;
                                        else if (which == AlertDialog.BUTTON_NEGATIVE)
                                            marker.remove();
                                    }
                                }).show();

                        return true;
                    }
                });
                ((SupportMapFragment) fragment).getMap().setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        ((SupportMapFragment) fragment).getMap().addMarker(new MarkerOptions().draggable(false).position(latLng));
                    }
                });
            }
        });

        return fragment;
    }

    private ImageRequest getStaticMap()
    {
        return new ImageRequest(staticMapBaseUrl+"&center="+center.latitude+","+center.longitude+"&markers=color:red%7C"+center.latitude+","+center.longitude,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap bitmap) {
                        staticMap.setImageBitmap(bitmap);
                    }
                }, 640, 640, null,
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                    }
                });
    }

    private View.OnClickListener getMapFragment()
    {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).setContent(buildGoogleMap());
            }
        };
    }


}
