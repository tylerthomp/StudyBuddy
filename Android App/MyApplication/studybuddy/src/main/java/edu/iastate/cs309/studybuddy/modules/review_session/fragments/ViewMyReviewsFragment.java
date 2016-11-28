package edu.iastate.cs309.studybuddy.modules.review_session.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

import edu.iastate.cs309.studybuddy.R;
import edu.iastate.cs309.studybuddy.meta.LaunchActivity;
import edu.iastate.cs309.studybuddy.meta.StudyBuddyFragment;
import edu.iastate.cs309.studybuddy.meta.Util.SharedPrefsUtil;
import edu.iastate.cs309.studybuddy.meta.Util.VolleyUtil;
import edu.iastate.cs309.studybuddy.meta.model.StudySession;

/**
 * Created by Sonic on 4/28/2015.
 */
public class ViewMyReviewsFragment extends StudyBuddyFragment{

    View rootView;
    public static StudySession currentSession;
    RatingBar rating;
    EditText details;
    TextView submit;

    public static int ratingNum;
    public static String text = "";

    SharedPrefsUtil sharedPrefs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        container.removeAllViews();
        rootView = inflater.inflate(R.layout.fragment_session_review, container, false);
        rating = (RatingBar) rootView.findViewById(R.id.session_review_ratings_bar);
        rating.setClickable(false);
        details = (EditText) rootView.findViewById(R.id.session_review_comments);
        details.setClickable(false);
        submit = (TextView) rootView.findViewById(R.id.session_review_submit);
        submit.setVisibility(View.GONE);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        sharedPrefs = new SharedPrefsUtil(getActivity());
        rating.setNumStars(ratingNum);
        details.setText(text);
    }


}
