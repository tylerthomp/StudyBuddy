package edu.iastate.cs309.studybuddy.modules.me.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
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

/**
 * Created by Sonic on 4/25/2015.
 */
public class AddClassFragment extends StudyBuddyFragment{

    View rootView;
    Spinner classPrefixSpinner;
    EditText classNum;
    ArrayList<String> classPrefixes = new ArrayList<>();
    ClassSpinnerAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_add_class, container, false);
        classNum = (EditText) rootView.findViewById(R.id.add_class_number_edit);
        classPrefixSpinner = (Spinner) rootView.findViewById(R.id.add_class_prefix_spinner);
        rootView.findViewById(R.id.add_class_submit).setOnClickListener(getSubmitListener());
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        VolleyUtil.getInstance(getActivity()).addToRequestQueue(getPrefixes());
        classPrefixSpinner.setAdapter(adapter = new ClassSpinnerAdapter());
    }

    class ClassSpinnerAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return classPrefixes.size();
        }

        @Override
        public Object getItem(int position) {
            return classPrefixes.get(position);
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
                view = inflater.inflate(R.layout.view_spinner_item, parent, false);
            } else view = convertView;

            ((TextView) view.findViewById(R.id.spinner_item_text)).setText(classPrefixes.get(position));

            return view;
        }
    }

    private StringRequest getPrefixes()
    {
        String url = getActivity().getString(R.string.base_api_url) + "/api/Class/prefixes";

        return new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {

                    @Override
                    public void onResponse(String string) {
                        classPrefixes.clear();
                        try {
                            JSONArray jsonArray = new JSONArray(string);
                            for(int i = 0; i < jsonArray.length(); i++)
                                classPrefixes.add(jsonArray.getString(i));
                            adapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        adapter.notifyDataSetChanged();


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        VolleyLog.e("Error getting class prefixes:", volleyError.getMessage());
                    }
                })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("access_token", new SharedPrefsUtil(getActivity()).getSharedPrefs().getString(LaunchActivity.SHARED_PREF_LOG_IN_INFO_KEY, ""));
                return params;
            }
        };
    }

    private View.OnClickListener getSubmitListener()
    {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VolleyUtil.getInstance(getActivity()).addToRequestQueue(addClassToUser((MainActivity) getActivity()));
            }
        };
    }

    private StringRequest addClassToUser(final MainActivity activity)
    {
        return new StringRequest(Request.Method.POST, getString(R.string.base_api_url) + "/api/StudentToClass/add_class",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        Toast.makeText(getActivity(), "Class Added Successfully", Toast.LENGTH_LONG).show();
                        activity.setContent(new ClassesFragment());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Toast.makeText(getActivity(), "Error Adding Class, Please Try Again", Toast.LENGTH_LONG).show();
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("access_token", new SharedPrefsUtil(getActivity()).getSharedPrefs().getString(LaunchActivity.SHARED_PREF_LOG_IN_INFO_KEY, ""));
                params.put("payload", "{\"class_prefix\":\""+ classPrefixSpinner.getSelectedItem().toString() + "\",\"class_number\":" + classNum.getText().toString() + "}");
                return params;
            }
        };
    }


}
