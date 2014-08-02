package com.bnotya.bnotyaapp.fragments;

import java.util.ArrayList;
import java.util.List;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import com.bnotya.bnotyaapp.CardFlipActivity;
import com.bnotya.bnotyaapp.R;
import com.bnotya.bnotyaapp.adapters.CustomArrayAdapter;
import com.bnotya.bnotyaapp.models.ListItem;

public class WomenListFragment extends Fragment
{
	public static final String ARG_VIEW_NUMBER = "view_number";
	private ListView _listView;
	private EditText _inputSearch;
	private CustomArrayAdapter _adapter;

	public WomenListFragment()
	{
		// Empty constructor required for fragment subclasses
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		View rootView = inflater.inflate(R.layout.women_list_view, container,
				false);

		getActivity().setTitle(R.string.women_list_title);
		
		initWomenList(rootView);			
		initSearch(rootView);

		return rootView;
	}

	private void initWomenList(View rootView)
	{
		// Get ListView object from xml
		_listView = (ListView) rootView.findViewById(R.id.womenlist);

		List<ListItem> listDataHeaders = fillData(R.array.women_names_array,
				R.array.women_list_icons);		

		// Define a new Adapter		
		_adapter = new CustomArrayAdapter(getActivity(),
				R.layout.women_list_item, listDataHeaders);

		// Assign adapter to ListView
		_listView.setAdapter(_adapter);

		// ListView Item Click Listener
		_listView.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				Intent intent = new Intent(getActivity(),
						CardFlipActivity.class);
				intent.putExtra("EXTRA_SESSION_ID", position);
				startActivity(intent);
			}
		});
	}		
	
	private List<ListItem> fillData(int titlesID, int iconsID)
	{		
		// Load item values
		String[] titles = getResources().getStringArray(titlesID);
		TypedArray icons = getResources().obtainTypedArray(iconsID);
		
		List<ListItem> result = new ArrayList<ListItem>();

		// Adding items to array

		for (int i = 0; i < titles.length; i++)
		{
			result.add(new ListItem(titles[i], icons
					.getResourceId(i, -1)));
		}

		// Recycle the typed array
		new ArrayRecycleTask().execute(icons);		

		return result;
	}
	
	private class ArrayRecycleTask extends AsyncTask<TypedArray, Void, Void>
	{
		protected Void doInBackground(TypedArray... arrays)
		{
			arrays[0].recycle();

			return null;
		}
	}

	private void initSearch(View rootView)
	{
		_inputSearch = (EditText) rootView.findViewById(R.id.inputSearch);

		_inputSearch.addTextChangedListener(new TextWatcher()
		{
			@Override
			public void onTextChanged(CharSequence cs, int arg1, int arg2,
					int arg3)
			{				
				// When user changed the Text
				_adapter.getFilter().filter(cs.toString());
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3)
			{
				
			}

			@Override
			public void afterTextChanged(Editable arg0)
			{
				
			}
		});
	}
}
