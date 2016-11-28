package edu.iastate.cs309.studybuddy.modules.me.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.iastate.cs309.studybuddy.R;
import edu.iastate.cs309.studybuddy.meta.MainActivity;
import edu.iastate.cs309.studybuddy.meta.StudyBuddyFragment;

/**
 * Created by Tyler on 4/25/2015.
 */
public class MeListFragment extends StudyBuddyFragment {

    View rootView;
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_me_list, container, false);
        rootView.findViewById(R.id.classes).setOnClickListener(getGoToClassesListener());
        rootView.findViewById(R.id.availabilities).setOnClickListener(getGotoAvailabilityListener());
        return rootView;
    }

    private View.OnClickListener getGoToClassesListener()
    {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).setContent(new ClassesFragment());
            }
        };
    }

    private View.OnClickListener getGotoAvailabilityListener()
    {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).setContent(new MeFragment());
            }
        };
    }
}
