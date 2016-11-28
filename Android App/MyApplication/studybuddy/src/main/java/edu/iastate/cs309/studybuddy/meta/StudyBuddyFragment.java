package edu.iastate.cs309.studybuddy.meta;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Tyler on 2/8/2015.
 */
public class StudyBuddyFragment extends Fragment {
    //No methods here yet, but every Fragment should extend this class
    //and call super._____(); for every method.


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //container.removeAllViews();
        return super.onCreateView(inflater, container, savedInstanceState);
    }
}

