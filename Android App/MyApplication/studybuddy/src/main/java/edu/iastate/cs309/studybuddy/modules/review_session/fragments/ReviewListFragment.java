package edu.iastate.cs309.studybuddy.modules.review_session.fragments;

import android.content.Context;
import android.os.Bundle;
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
public class ReviewListFragment extends StudyBuddyFragment{

        View rootView;
        ListView pastSessions;
        PastSessionsListAdapter adapter = new PastSessionsListAdapter();
        ArrayList<StudySession> sessions = new ArrayList<>();

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.fragment_session_list_review, container, false);
            pastSessions = (ListView) rootView.findViewById(R.id.past_sessions_list);
            return rootView;
        }

        @Override
        public void onStart() {
            super.onStart();
            VolleyUtil.getInstance(getActivity()).addToRequestQueue(getPastAttendence());
            pastSessions.setAdapter(adapter);
        }

        class PastSessionsListAdapter extends BaseAdapter
        {

            @Override
            public int getCount() {
                return sessions.size();
            }

            @Override
            public Object getItem(int position) {
                return sessions.get(position);
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

                view.findViewById(R.id.home_card_static_map).setVisibility(View.GONE);

                accept.setVisibility(View.GONE);
                ignore.setVisibility(View.GONE);

                name.setText(sessions.get(position).getTitle());
                description.setText(sessions.get(position).getDescription());
                location.setText(sessions.get(position).getLocation());

                view.setOnClickListener(sendToReviewFragment(((MainActivity) getActivity()), sessions.get(position)));

                return view;
            }

            private View.OnClickListener sendToReviewFragment(final MainActivity activity, final StudySession session)
            {
                return new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        GetReviewListForSession.sessionId = session.getID();
                        activity.setContent(new GetReviewListForSession());
                    }
                };
            }
        }

        private StringRequest getPastAttendence()
        {
            return new StringRequest(Request.Method.POST, getActivity().getString(R.string.base_api_url) + "/api/StudySession/past_children",
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String s) {
                            sessions.clear();
                            try {
                                JSONArray array = new JSONArray(s);
                                for (int i = 0; i < array.length(); i++) {
                                    StudySession temp = new StudySession();
                                    temp.parseNonNestedJson(array.getJSONObject(i), "reviewp");
                                    sessions.add(temp);
                                }
                            } catch (JSONException e)
                            {
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
                    Map<String, String> params =  new HashMap<>();
                    params.put("access_token", new SharedPrefsUtil(getActivity()).getSharedPrefs().getString(LaunchActivity.SHARED_PREF_LOG_IN_INFO_KEY, ""));
                    return params;
                }
            };
        }



    }
