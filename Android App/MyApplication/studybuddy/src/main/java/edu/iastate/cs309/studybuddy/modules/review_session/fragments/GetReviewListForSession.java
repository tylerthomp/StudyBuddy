package edu.iastate.cs309.studybuddy.modules.review_session.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

/**
 * Created by Tyler on 4/28/2015.
 */
public class GetReviewListForSession extends StudyBuddyFragment{

    View rootView;
    ListView pastSessions;
    PastSessionsListAdapter adapter = new PastSessionsListAdapter();
    ArrayList<Review> reviews = new ArrayList<>();
    public static int sessionId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_session_list_review, container, false);
        pastSessions = (ListView) rootView.findViewById(R.id.past_sessions_list);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        VolleyUtil.getInstance(getActivity()).addToRequestQueue(getReviews(sessionId));
        pastSessions.setAdapter(adapter);
    }

    class PastSessionsListAdapter extends BaseAdapter
    {

        @Override
        public int getCount() {
            return reviews.size();
        }

        @Override
        public Object getItem(int position) {
            return reviews.get(position);
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
            TextView description = (TextView) view.findViewById(R.id.study_session_name);
            TextView location = (TextView) view.findViewById(R.id.study_session_name);
            TextView accept = (TextView) view.findViewById(R.id.study_session_name);
            TextView ignore = (TextView) view.findViewById(R.id.study_session_name);

            accept.setVisibility(View.INVISIBLE);
            ignore.setVisibility(View.INVISIBLE);

            name.setText(reviews.get(position).getRating() + "");
            description.setText(reviews.get(position).getText());
            location.setVisibility(View.GONE);

            view.setOnClickListener(sendToReviewFragment(((MainActivity) getActivity()), reviews.get(position)));

            return view;
        }

        private View.OnClickListener sendToReviewFragment(final MainActivity activity, final Review session)
        {
            return new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ViewMyReviewsFragment.ratingNum = session.getRating();
                    ViewMyReviewsFragment.text = session.getText();
                    activity.setContent(new ViewMyReviewsFragment());
                }
            };
        }
    }

    private StringRequest getReviews(final int sessionID)
    {
        return new StringRequest(Request.Method.POST, getActivity().getString(R.string.base_api_url) + "/api/Review/get",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        reviews.clear();
                        try {
                            Log.e("Get Reviews ID", sessionID+"");
                            Log.e("Get Reviews JSON", s);
                            JSONArray array = new JSONArray(s);
                            for (int i = 0; i < array.length(); i++)
                                reviews.add(new Review(array.getJSONObject(i)));
                        } catch (JSONException e)
                        {
                            Log.e("Review JSON", s);
                            e.printStackTrace();
                        }
                        adapter.notifyDataSetChanged();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Log.e("Get Reviews ID", sessionID+"");
                        volleyError.printStackTrace();
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params =  new HashMap<>();
                params.put("access_token", new SharedPrefsUtil(getActivity()).getSharedPrefs().getString(LaunchActivity.SHARED_PREF_LOG_IN_INFO_KEY, ""));
                params.put("payload", "{\"study_session_id\": "+sessionID +"}");
                return params;
            }
        };
    }

    private class Review
    {
        private int rating;
        private String text;

        public Review(JSONObject object)
        {
            try {
                rating = object.getInt("rating");
                text = object.getString("text");
            }catch (JSONException e)
            {
                e.printStackTrace();
            }
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public int getRating() {
            return rating;
        }

        public void setRating(int rating) {
            this.rating = rating;
        }
    }

}
