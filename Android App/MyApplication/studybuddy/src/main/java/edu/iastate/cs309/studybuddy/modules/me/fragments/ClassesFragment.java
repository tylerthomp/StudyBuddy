package edu.iastate.cs309.studybuddy.modules.me.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import edu.iastate.cs309.studybuddy.R;
import edu.iastate.cs309.studybuddy.meta.LaunchActivity;
import edu.iastate.cs309.studybuddy.meta.MainActivity;
import edu.iastate.cs309.studybuddy.meta.StudyBuddyFragment;
import edu.iastate.cs309.studybuddy.meta.Util.SharedPrefsUtil;
import edu.iastate.cs309.studybuddy.meta.Util.VolleyUtil;
import edu.iastate.cs309.studybuddy.modules.me.model.Classes;

/**
 * Created by Tyler on 4/25/2015.
 */
public class ClassesFragment extends StudyBuddyFragment{

    View rootView;
    ListView classList;
    ClassesAdapter adapter;
    ArrayList<Classes> classCollection = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_classes, container, false);
        classList = (ListView) rootView.findViewById(R.id.classes_list);
        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        VolleyUtil.getInstance(getActivity()).addToRequestQueue(getCurrentClasses());
        classList.setAdapter(adapter = new ClassesAdapter());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_classes, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.menu_classes_add_class) {
            ((MainActivity) getActivity()).setContent(new AddClassFragment());
            return true;
        }

        return false;
    }

    private StringRequest getCurrentClasses()
    {
        return new StringRequest(Request.Method.POST, getString(R.string.base_api_url) + "/api/Class/my_classes",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        try {
                            JSONArray array = new JSONArray(s);
                            for (int i = 0; i < array.length(); i++)
                                classCollection.add(new Classes(array.getJSONObject(i)));
                            adapter.notifyDataSetChanged();
                        } catch (JSONException e)
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
                Map<String,String> params = new HashMap<>();
                params.put("access_token", new SharedPrefsUtil(getActivity()).getSharedPrefs().getString(LaunchActivity.SHARED_PREF_LOG_IN_INFO_KEY, ""));
                return params;
            }
        };
    }

    class ClassesAdapter extends BaseAdapter
    {

        @Override
        public int getCount() {
            return classCollection.size();
        }

        @Override
        public Object getItem(int position) {
            return classCollection.get(position);
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
                view = inflater.inflate(R.layout.view_classes_list_view_cell, parent, false);
            }
            else view = convertView;

            TextView text = (TextView) view.findViewById(R.id.classes_list_view_cell_text);
            text.setText(classCollection.get(position).getPrefix() + " " + classCollection.get(position).getNumber());
            view.setOnClickListener(getRemoveDialog(classCollection.get(position)));
            return view;
        }

        private View.OnClickListener getRemoveDialog(final Classes classes)
        {

            return new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog removeDialog = new AlertDialog.Builder(getActivity())
                            .setTitle("Remove Class")
                            .setCancelable(true)
                            .setMessage("Would you like to remove this class from your class list?")
                            .setNeutralButton("Remove", getRemoveClassClickListener(classes))
                            .show();
                }
            };


        }

        private AlertDialog.OnClickListener getRemoveClassClickListener(final Classes classes)
        {
            return new AlertDialog.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(which == AlertDialog.BUTTON_NEUTRAL)
                    {
                        VolleyUtil.getInstance(getActivity()).addToRequestQueue(getRemoveClassRequest(classes));
                        dialog.dismiss();
                    }

                }
            };
        }

        private StringRequest getRemoveClassRequest(final Classes classes)
        {
            return new StringRequest(Request.Method.POST, getString(R.string.base_api_url) + "/api/StudentToClass/remove_class",
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String s) {
                            Toast.makeText(getActivity(), "Class Removed Successfully", Toast.LENGTH_LONG).show();
                            classCollection.clear();
                            VolleyUtil.getInstance(getActivity()).addToRequestQueue(getCurrentClasses());
                            adapter.notifyDataSetChanged();
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            Toast.makeText(getActivity(), "Error Removing Class", Toast.LENGTH_LONG).show();
                        }
            }){
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("access_token", new SharedPrefsUtil(getActivity()).getSharedPrefs().getString(LaunchActivity.SHARED_PREF_LOG_IN_INFO_KEY, ""));
                    params.put("payload", "{\"class_prefix\": \"" + classes.getPrefix() + "\", \"class_number\": " + classes.getNumber()  + "}");
                    return params;
                }
            };
        }

    }
}
