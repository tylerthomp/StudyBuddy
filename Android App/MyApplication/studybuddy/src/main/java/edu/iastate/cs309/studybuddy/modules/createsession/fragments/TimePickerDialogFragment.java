package edu.iastate.cs309.studybuddy.modules.createsession.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;

/**
 * Created by Tyler on 4/15/2015.
 */
public class TimePickerDialogFragment extends android.support.v4.app.DialogFragment {

    public static boolean isStartTime;

    public TimePickerDialogFragment()
    {

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }
}
