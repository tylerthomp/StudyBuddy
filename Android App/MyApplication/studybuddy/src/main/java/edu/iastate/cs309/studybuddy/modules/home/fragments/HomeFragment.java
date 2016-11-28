package edu.iastate.cs309.studybuddy.modules.home.fragments;

import android.app.ActionBar;
import android.app.DownloadManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.support.v7.widget.CardView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import edu.iastate.cs309.studybuddy.R;
import edu.iastate.cs309.studybuddy.meta.LaunchActivity;
import edu.iastate.cs309.studybuddy.meta.MainActivity;
import edu.iastate.cs309.studybuddy.meta.StudyBuddyFragment;
import edu.iastate.cs309.studybuddy.meta.Util.DataUtil;
import edu.iastate.cs309.studybuddy.meta.Util.SharedPrefsUtil;
import edu.iastate.cs309.studybuddy.meta.Util.VolleyUtil;
import edu.iastate.cs309.studybuddy.meta.model.StudySession;
import edu.iastate.cs309.studybuddy.modules.createsession.fragments.CreateSessionFragment;

/**
 * Created by Tyler on 2/9/2015.
 */
@SuppressWarnings("deprecated")
public class HomeFragment extends StudyBuddyFragment {

    private String staticMapBaseUrl = "https://maps.googleapis.com/maps/api/staticmap?&zoom=15&size=600x400";

    View rootView;
    ListView homeListView;
    ArrayList<StudySession> recommendedSessions = new ArrayList<>();
    ArrayList<StudySession> mySessions = new ArrayList<>();
    ArrayList<StudySession> upcomingSessions = new ArrayList<>();
    HomeAdapter adapter;

    SharedPrefsUtil sharedPrefs;


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_home, container, false);
        homeListView = (ListView) rootView.findViewById(R.id.home_list_view);
        adapter = new HomeAdapter(getActivity());
        homeListView.setAdapter(adapter);
        sharedPrefs = new SharedPrefsUtil(getActivity());
        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        populateWithUpcomingSessions();
        populateWithSuggestedSessions();
        populateWithMySessions();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_home, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.menu_home_add_session) {
            ((MainActivity) getActivity()).setContent(new CreateSessionFragment());
            return true;
        }
        return false;
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    private void populateWithUpcomingSessions()
    {
        // Get study sessions in JSON request and create card with study session.
        try{
            VolleyUtil.getInstance(this.getActivity()).addToRequestQueue(getFutureAttendence());
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void populateWithMySessions()
    {
        // Get study sessions in JSON request and create card with study session.
        try{
            VolleyUtil.getInstance(this.getActivity()).addToRequestQueue(getMyStudySessions());
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void populateWithSuggestedSessions()
    {
        // Get study sessions in JSON request and create card with study session.
        try{
            VolleyUtil.getInstance(this.getActivity()).addToRequestQueue(getSuggestedSessions());
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private class HomeAdapter extends BaseAdapter
    {
        Context currentContext;
        public HomeAdapter(Context context)
        {
            super();
            currentContext = context;
        }

        @Override
        public int getCount() {
            return recommendedSessions.size() + upcomingSessions.size() + mySessions.size() + 3;
        }

        @Override
        public Object getItem(int position) {
            return new Object();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            int i = position;
            boolean isHeader = false;

            if(i == 0)
            {
                //First Header
                return buildTextView("Suggested");
            }
            else if (i < recommendedSessions.size()+1)
            {
                i -= 1;
                //Recommended Sessions
                LayoutInflater inflater = (LayoutInflater) currentContext.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View cardView = inflater.inflate(R.layout.home_card_layout, parent, false);
                TextView sessionTitle = (TextView) cardView.findViewById(R.id.study_session_name);
                sessionTitle.setText(recommendedSessions.get(i).getTitle());
                Log.e("Session Title", recommendedSessions.get(i).getTitle() + "");
                Log.e("Session Location", recommendedSessions.get(i).getLocation() + "");
                TextView sessionLocation = (TextView) cardView.findViewById(R.id.study_session_location);
                sessionLocation.setText(recommendedSessions.get(i).getLocation());
                TextView sessionDescription = (TextView) cardView.findViewById(R.id.study_session_description);
                sessionDescription.setText(recommendedSessions.get(i).getDescription());
                ImageView map = (ImageView) cardView.findViewById(R.id.home_card_static_map);
                map.setScaleType(ImageView.ScaleType.CENTER_CROP);
                VolleyUtil.getInstance(getActivity()).addToRequestQueue(getStaticMap(recommendedSessions.get(i).getLocationLatLng().latitude, recommendedSessions.get(i).getLocationLatLng().longitude, map));
                TextView accept = (TextView) cardView.findViewById(R.id.study_session_accept);
                TextView ignore = (TextView) cardView.findViewById(R.id.study_session_ignore);

                accept.setOnClickListener(getAcceptListener(recommendedSessions.get(i).getID()));
                ignore.setOnClickListener(getIgnoreListener(recommendedSessions.get(i).getID()));

                return cardView;
            }
            else if (i == recommendedSessions.size()+1)
            {
                //Second Header
                return buildTextView("Upcoming");
            }
            else if (i < recommendedSessions.size() + upcomingSessions.size() +2)
            {
                i -= (recommendedSessions.size() + 2);
                //Upcoming sessions.
                LayoutInflater inflater = (LayoutInflater) currentContext.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View cardView = inflater.inflate(R.layout.home_card_layout, parent, false);
                TextView sessionTitle = (TextView) cardView.findViewById(R.id.study_session_name);
                sessionTitle.setText(upcomingSessions.get(i).getTitle());
                TextView sessionLocation = (TextView) cardView.findViewById(R.id.study_session_location);
                sessionLocation.setText(upcomingSessions.get(i).getLocation());
                TextView sessionDescription = (TextView) cardView.findViewById(R.id.study_session_description);
                sessionDescription.setText(upcomingSessions.get(i).getDescription());
                ImageView map = (ImageView) cardView.findViewById(R.id.home_card_static_map);
                map.setScaleType(ImageView.ScaleType.CENTER_CROP);
                VolleyUtil.getInstance(getActivity()).addToRequestQueue(getStaticMap(upcomingSessions.get(i).getLocationLatLng().latitude, upcomingSessions.get(i).getLocationLatLng().longitude, map));
                TextView accept = (TextView) cardView.findViewById(R.id.study_session_accept);
                TextView ignore = (TextView) cardView.findViewById(R.id.study_session_ignore);

                accept.setVisibility(View.GONE);
                ignore.setText("Cancel");
                ignore.setOnClickListener(getCancelClickListener(upcomingSessions.get(i)));

                return cardView;
            }
            else if (i == recommendedSessions.size() + upcomingSessions.size() +2)
            {
                //Third header
                return buildTextView("Mine");
            }
            else if (i < recommendedSessions.size() + upcomingSessions.size() + mySessions.size() + 3)
            {
                i -= (recommendedSessions.size() + upcomingSessions.size() + 3);
                //My Sessions
                LayoutInflater inflater = (LayoutInflater) currentContext.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View cardView = inflater.inflate(R.layout.home_card_layout, parent, false);
                TextView sessionTitle = (TextView) cardView.findViewById(R.id.study_session_name);
                sessionTitle.setText(mySessions.get(i).getTitle());
//                Log.e("Session Title", mySessions.get(i).getTitle() + "");
//                Log.e("Session Location", mySessions.get(i).getLocation() + "");
                TextView sessionLocation = (TextView) cardView.findViewById(R.id.study_session_location);
                sessionLocation.setText(mySessions.get(i).getLocation());
                TextView sessionDescription = (TextView) cardView.findViewById(R.id.study_session_description);
                sessionDescription.setText(mySessions.get(i).getDescription());
                ImageView map = (ImageView) cardView.findViewById(R.id.home_card_static_map);
                map.setScaleType(ImageView.ScaleType.CENTER_CROP);
                VolleyUtil.getInstance(getActivity()).addToRequestQueue(getStaticMap(mySessions.get(i).getLocationLatLng().latitude, mySessions.get(i).getLocationLatLng().longitude, map));
                TextView accept = (TextView) cardView.findViewById(R.id.study_session_accept);
                TextView ignore = (TextView) cardView.findViewById(R.id.study_session_ignore);

                accept.setVisibility(View.GONE);
                ignore.setText("Cancel");
                ignore.setOnClickListener(getRemoveSessionClickListener(mySessions.get(i).getID()));

                return cardView;
            }
            return new View(getActivity());
        }

        private ImageRequest getStaticMap(double lat, double lng, final ImageView map)
        {
            return new ImageRequest(staticMapBaseUrl+"&center="+lat+","+lng+"&markers=color:red%7C"+lat+","+lng,
                    new Response.Listener<Bitmap>() {
                        @Override
                        public void onResponse(Bitmap bitmap) {
                            map.setImageBitmap(bitmap);
                        }
                    }, 640, 640, null,
                    new Response.ErrorListener()
                    {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {

                        }
                    });
        }

        private View.OnClickListener getCancelClickListener(final StudySession session)
        {
            return new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    VolleyUtil.getInstance(getActivity()).addToRequestQueue(rsvpForSession(session.getID(), 0));
                }
            };
        }

        private View buildCardView(StudySession session, ViewGroup parent)
        {
            LayoutInflater inflater = (LayoutInflater) currentContext.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View cardView = inflater.inflate(R.layout.home_card_layout, parent, false);
            TextView sessionTitle = (TextView) cardView.findViewById(R.id.study_session_name);
            sessionTitle.setText(session.getTitle());
            TextView sessionLocation = (TextView) cardView.findViewById(R.id.study_session_location);
            sessionLocation.setText(session.getLocation());
            TextView accept = (TextView) cardView.findViewById(R.id.study_session_accept);
            TextView ignore = (TextView) cardView.findViewById(R.id.study_session_ignore);

//            accept.setOnClickListener(getAcceptListener(index));
//            ignore.setOnClickListener(getIgnoreListener(index));

            return cardView;
        }

        private View buildTextView(String s)
        {
            LayoutInflater inflater = (LayoutInflater) currentContext.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View cardView = inflater.inflate(R.layout.view_home_header, null);
            TextView headerText = (TextView) cardView.findViewById(R.id.text_view_home_header);
            headerText.setText(s);
            return cardView;
        }


        private View.OnClickListener getIgnoreListener(final int index)
        {
            return new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getActivity(), "Session Ignored", Toast.LENGTH_SHORT).show();
                    VolleyUtil.getInstance(getActivity()).addToRequestQueue(rsvpForSession(index, 2));
                    recommendedSessions.remove(index);
                    adapter.notifyDataSetChanged();
                }
            };
        }

        private View.OnClickListener getAcceptListener(final int index)
        {
            return new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getActivity(), "Spot Reserved", Toast.LENGTH_SHORT).show();
                    adapter.notifyDataSetChanged();
                    VolleyUtil.getInstance(getActivity()).addToRequestQueue(rsvpForSession(index, 1));
                }
            };
        }

        private StringRequest rsvpForSession(final int sessionID, final int rsvpStatus)
        {
            return new StringRequest(Request.Method.POST, getActivity().getResources().getString(R.string.base_api_url)+"/api/Attendance/rsvp",
            new Response.Listener<String>() {
                @Override
                public void onResponse(String s) {
                    Toast.makeText(getActivity(), "Successful", Toast.LENGTH_LONG).show();
                    VolleyUtil.getInstance(getActivity()).addToRequestQueue(getSuggestedSessions());
                    VolleyUtil.getInstance(getActivity()).addToRequestQueue(getFutureAttendence());
                }
            },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            Toast.makeText(getActivity(), "Unfortunately, there was an error.", Toast.LENGTH_LONG).show();
                        }
                    }){
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("access_token", sharedPrefs.getSharedPrefs().getString(LaunchActivity.SHARED_PREF_LOG_IN_INFO_KEY, ""));
                    params.put("payload", buildRSVPJson(sessionID, rsvpStatus));
                    return params;
                }
            };
        }

        private String buildRSVPJson(int sessionID, int rsvpStatus)
        {
            return "{ \"id\":" + sessionID + ", \"rsvp\":" + rsvpStatus + "}";
        }

        private View.OnClickListener getRemoveSessionClickListener(final int sessionId)
        {
            return new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    VolleyUtil.getInstance(getActivity()).addToRequestQueue(getRemoveSessionRequest(sessionId));
                }
            };
        }

        private StringRequest getRemoveSessionRequest(final int ID)
        {
            final int sessionId = ID;
            return new StringRequest(Request.Method.POST, getString(R.string.base_api_url) + "/api/StudySession/remove",
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String s) {
                            Toast.makeText(getActivity(), "Session Cancelled", Toast.LENGTH_LONG).show();
                            VolleyUtil.getInstance(getActivity()).addToRequestQueue(getMyStudySessions());
                            Log.e("Remove id", ID+"");
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            Toast.makeText(getActivity(), "Error Cancelling Session", Toast.LENGTH_LONG).show();
                            volleyError.printStackTrace();
                        }
                    }){
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String,String> params = new HashMap<>();
                    params.put("access_token", sharedPrefs.getSharedPrefs().getString(LaunchActivity.SHARED_PREF_LOG_IN_INFO_KEY, ""));
                    params.put("payload", "{\"study_session_id\": " + sessionId + "}");
                    return params;
                }
            };
        }

    }

    private StringRequest getSuggestedSessions()
    {
        return new StringRequest(Request.Method.POST, getActivity().getString(R.string.base_api_url) + "/api/Attendance/future", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                recommendedSessions.clear();
                JSONArray array = null;
                try {
                    array = new JSONArray(response);
                    for(int i = 0; i < array.length(); i++)
                        recommendedSessions.add(new StudySession(array.getJSONObject(i), "Rec"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                adapter.notifyDataSetChanged();
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error){
                error.printStackTrace();
                Log.e("Home Frag Volley Error", "Error");
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("access_token", sharedPrefs.getSharedPrefs().getString(LaunchActivity.SHARED_PREF_LOG_IN_INFO_KEY, ""));
                return params;
            }
        };
    }

    private StringRequest getMyStudySessions()
    {
        return new StringRequest(Request.Method.POST, getActivity().getString(R.string.base_api_url) + "/api/Attendance/my_children",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        mySessions.clear();
                        JSONArray array = null;
                        try {
                            array = new JSONArray(s);
                            for(int i = 0; i < array.length(); i++)
                                mySessions.add(new StudySession(array.getJSONObject(i), "My"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        adapter.notifyDataSetChanged();
                        Log.e("My sessions", s);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("access_token", sharedPrefs.getSharedPrefs().getString(LaunchActivity.SHARED_PREF_LOG_IN_INFO_KEY, ""));
                return params;
            }
        };
    }

    private StringRequest getFutureAttendence()
    {
        return new StringRequest(Request.Method.POST, getActivity().getString(R.string.base_api_url) + "/api/Attendance/accepted",
                        new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        upcomingSessions.clear();
                        JSONArray array = null;
                        try {
                            array = new JSONArray(s);
                            for(int i = 0; i < array.length(); i++)
                                upcomingSessions.add(new StudySession(array.getJSONObject(i), "Up"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        adapter.notifyDataSetChanged();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("access_token", sharedPrefs.getSharedPrefs().getString(LaunchActivity.SHARED_PREF_LOG_IN_INFO_KEY, ""));
                return params;
            }
        };
    }
}
