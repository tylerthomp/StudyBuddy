package edu.iastate.cs309.studybuddy.modules.search.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

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
 * Created by Tyler on 4/28/2015.
 */
public class SearchFragment extends StudyBuddyFragment{


    View rootView;
    Spinner prefixSpinner;
    EditText classNum;

    ArrayList<String> usableClassPrefixes = new ArrayList<>();
    PrefixSpinnerAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_search, container,false);
        prefixSpinner = (Spinner) rootView.findViewById(R.id.search_class_prefix_spinner);
        classNum = (EditText) rootView.findViewById(R.id.search_edit_text_class_number);
        rootView.findViewById(R.id.search_submit).setOnClickListener(goToResults());
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        VolleyUtil.getInstance(getActivity()).addToRequestQueue(getPrefixes());
    }

    private View.OnClickListener goToResults()
    {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(classNum.getText().toString().isEmpty())
                    return;

                SearchResultsFragment.classPrefix = prefixSpinner.getSelectedItem().toString();
                SearchResultsFragment.classNum = classNum.getText().toString();
                ((MainActivity) getActivity()).setContent(new SearchResultsFragment());
            }
        };
    }

    private StringRequest getPrefixes()
    {
        String url = getActivity().getString(R.string.base_api_url) + "/api/Class/prefixes";

        return new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {

                    @Override
                    public void onResponse(String string) {
                        usableClassPrefixes.clear();
                        try {
                            JSONArray jsonArray = new JSONArray(string);
                            for(int i = 0; i < jsonArray.length(); i++)
                                usableClassPrefixes.add(jsonArray.getString(i));
                            if(adapter == null) {
                                adapter = new PrefixSpinnerAdapter(getActivity());
                                prefixSpinner.setAdapter(adapter);
                            }
                            else adapter.notifyDataSetChanged();
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



    private class PrefixSpinnerAdapter extends BaseAdapter
    {
        Context context;

        public PrefixSpinnerAdapter(Context currentContext)
        {
            context = currentContext;
        }


        @Override
        public int getCount() {
            return usableClassPrefixes.size();
        }

        @Override
        public Object getItem(int position) {
            return usableClassPrefixes.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if(view == null)
                view = new TextView(context);

            ((TextView) view).setText(usableClassPrefixes.get(position));

            return view;
        }


    }
}
