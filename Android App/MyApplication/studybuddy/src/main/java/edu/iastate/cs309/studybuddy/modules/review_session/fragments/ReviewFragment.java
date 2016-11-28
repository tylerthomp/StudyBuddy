package edu.iastate.cs309.studybuddy.modules.review_session.fragments;

import android.media.Rating;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

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
 * Created by Tyler on 4/19/2015.
 */
public class ReviewFragment extends StudyBuddyFragment {

    View rootView;
    public static StudySession currentSession;
    RatingBar rating;
    EditText details;
    TextView submit;

    SharedPrefsUtil sharedPrefs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        container.removeAllViews();
        rootView = inflater.inflate(R.layout.fragment_session_review, container, false);
        rating = (RatingBar) rootView.findViewById(R.id.session_review_ratings_bar);
        details = (EditText) rootView.findViewById(R.id.session_review_comments);
        submit = (TextView) rootView.findViewById(R.id.session_review_submit);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        sharedPrefs = new SharedPrefsUtil(getActivity());
        submit.setOnClickListener(sendReview());
    }

    private View.OnClickListener sendReview()
    {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VolleyUtil.getInstance(getActivity()).addToRequestQueue(sentRatingAndComments());
            }
        };
    }

    private StringRequest sentRatingAndComments()
    {
        return new StringRequest(Request.Method.POST, getActivity().getString(R.string.base_api_url)+"/api/Review/create", new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                Toast.makeText(getActivity(), "Successfully Reviewed", Toast.LENGTH_LONG).show();
                ((MainActivity) getActivity()).setContent(new PastSessionListFragment());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("access_token", sharedPrefs.getSharedPrefs().getString(LaunchActivity.SHARED_PREF_LOG_IN_INFO_KEY, ""));
                params.put("payload", "{\"text\": \"" + details.getText().toString() + "\", \"rating\": " + rating.getRating() + ", \"attendance_id\": " + currentSession.getID() + "}");
                return params;
            }
        };
    }
}
