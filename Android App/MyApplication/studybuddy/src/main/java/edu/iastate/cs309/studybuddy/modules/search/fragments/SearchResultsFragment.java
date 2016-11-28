package edu.iastate.cs309.studybuddy.modules.search.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import edu.iastate.cs309.studybuddy.R;
import edu.iastate.cs309.studybuddy.meta.LaunchActivity;
import edu.iastate.cs309.studybuddy.meta.MainActivity;
import edu.iastate.cs309.studybuddy.meta.StudyBuddyFragment;
import edu.iastate.cs309.studybuddy.meta.Util.SharedPrefsUtil;
import edu.iastate.cs309.studybuddy.meta.Util.VolleyUtil;
import edu.iastate.cs309.studybuddy.meta.model.StudySession;
import edu.iastate.cs309.studybuddy.modules.review_session.fragments.GetReviewListForSession;

/**
 * Created by Tyler on 4/29/2015.
 */
public class SearchResultsFragment extends StudyBuddyFragment{

    View rootView;
    ListView searchResults;
    ArrayList<StudySession> session = new ArrayList<>();
    ResultAdapter adapter;

    public static String classPrefix;
    public static String classNum;

    private String staticMapBaseUrl = "https://maps.googleapis.com/maps/api/staticmap?&zoom=15&size=600x400";

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_home, container,false);
        searchResults = (ListView) rootView.findViewById(R.id.home_list_view);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        VolleyUtil.getInstance(getActivity()).addToRequestQueue(sendQuery());
        adapter = new ResultAdapter();
        searchResults.setAdapter(adapter);
    }

    private StringRequest sendQuery()
    {
        return new StringRequest(Request.Method.POST, getString(R.string.base_api_url) + "/api/StudySession/search",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        Log.e("Search Json", s);
                        session.clear();
                        try {
                            JSONArray array = new JSONArray(s);
                            for(int i = 0; i < array.length(); i++)
                            {
                                StudySession temp = new StudySession();
                                temp.parseNonNestedJson(array.getJSONObject(i), "sresult");
                                session.add(temp);
                            }
                            adapter.notifyDataSetChanged();
                            Log.e("Result size", session.size()+"");
                        }catch (JSONException e)
                        {
                            e.printStackTrace();
                        }
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
                params.put("access_token", new SharedPrefsUtil(getActivity()).getSharedPrefs().getString(LaunchActivity.SHARED_PREF_LOG_IN_INFO_KEY, ""));
                params.put("payload", buildJson(classPrefix, classNum));
                return params;
            }
        };
    }

    private String buildJson(String prefix, String number)
    {
        return "{\"class_prefix\": \"" + prefix + "\", \"class_number\": " + number + "}";
    }

    class ResultAdapter extends BaseAdapter
    {
        @Override
        public int getCount() {
            return session.size();
        }

        @Override
        public Object getItem(int position) {
            return session.get(position);
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
                view = inflater.inflate(R.layout.home_card_layout, parent, false);
            }
            else
                view = convertView;

            TextView name = (TextView) view.findViewById(R.id.study_session_name);
            TextView description = (TextView) view.findViewById(R.id.study_session_description);
            TextView location = (TextView) view.findViewById(R.id.study_session_location);
            TextView accept = (TextView) view.findViewById(R.id.study_session_accept);
            TextView ignore = (TextView) view.findViewById(R.id.study_session_ignore);
            ignore.setVisibility(View.INVISIBLE);

            accept.setOnClickListener(getRsvpListener(session.get(position).getID()));

            getStaticMap(session.get(position).getLocationLatLng().latitude, session.get(position).getLocationLatLng().longitude, ((ImageView) view.findViewById(R.id.home_card_static_map)));
            view.findViewById(R.id.home_card_static_map).setVisibility(View.GONE);

            name.setText(session.get(position).getTitle());
            description.setText(session.get(position).getDescription());
            location.setText(session.get(position).getLocation());
            Log.e("Result Adapter", "getView");
            return view;
        }

        private View.OnClickListener getRsvpListener(final int id)
        {
            return new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    VolleyUtil.getInstance(getActivity()).addToRequestQueue(rsvpForSession(id));
                }
            };
        }

        private StringRequest rsvpForSession(final int id)
        {
            return new StringRequest(Request.Method.POST, getString(R.string.base_api_url) + "/api/StudySession/rsvp",
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String s) {
                            Toast.makeText(getActivity(), "RSVP Successful", Toast.LENGTH_LONG).show();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            Toast.makeText(getActivity(), "Error, Please Try Again", Toast.LENGTH_LONG).show();
                        }
                    }){
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("access_token", new SharedPrefsUtil(getActivity()).getSharedPrefs().getString(LaunchActivity.SHARED_PREF_LOG_IN_INFO_KEY, ""));
                    params.put("payload", "{\"study_session_id\": " + id + "}");
                    return params;
                }
            };
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

    }

}
