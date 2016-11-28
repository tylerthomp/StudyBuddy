package edu.iastate.cs309.studybuddy.modules.review_session.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import edu.iastate.cs309.studybuddy.R;
import edu.iastate.cs309.studybuddy.meta.MainActivity;
import edu.iastate.cs309.studybuddy.meta.StudyBuddyFragment;
import edu.iastate.cs309.studybuddy.modules.me.fragments.ClassesFragment;
import edu.iastate.cs309.studybuddy.modules.me.fragments.MeFragment;

/**
 * Created by Sonic on 4/28/2015.
 */
public class ReviewModuleFragment extends StudyBuddyFragment {

    View rootView;
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_me_list, container, false);
        rootView.findViewById(R.id.classes).setOnClickListener(getGoToReviewSessionListener());
        ((TextView)rootView.findViewById(R.id.classes)).setText("Review Past Session");
        ((TextView)rootView.findViewById(R.id.availabilities)).setText("See Your Reviews");
        rootView.findViewById(R.id.availabilities).setOnClickListener(getGotoReviewsListener());
        return rootView;
    }

    private View.OnClickListener getGoToReviewSessionListener()
    {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).setContent(new PastSessionListFragment());
            }
        };
    }

    private View.OnClickListener getGotoReviewsListener()
    {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).setContent(new ReviewListFragment());
            }
        };
    }
}
