package edu.iastate.cs309.studybuddy.modules.createsession.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;

import java.util.Calendar;

/**
 * Created by Tyler on 4/13/2015.
 */
public class DatePickerDialogFragment extends android.support.v4.app.DialogFragment {
    private android.support.v4.app.Fragment mFragment;
    public static boolean isStartDate;

    public DatePickerDialogFragment()
    {

    }

    public DatePickerDialogFragment(android.support.v4.app.Fragment callback, boolean isStart)
    {
        super();
        isStartDate = isStart;
        mFragment = callback;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Calendar cal = Calendar.getInstance();
        return new DatePickerDialog(getActivity(), (DatePickerDialog.OnDateSetListener) mFragment, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
    }
}
