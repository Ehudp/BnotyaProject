package com.bnotya.bnotyaapp.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.bnotya.bnotyaapp.R;

public class MainTehilotFragment extends Fragment
{
	public static final String ARG_VIEW_NUMBER = "view_number";

	public MainTehilotFragment()
	{
		// Empty constructor required for fragment subclasses
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		View rootView = inflater.inflate(R.layout.fragment_main_tehilot,
				container, false);

		int i = getArguments().getInt(ARG_VIEW_NUMBER);
		String viewTitle = getResources().getStringArray(R.array.views_array)[i];
		getActivity().setTitle(viewTitle);
		return rootView;
	}
}
