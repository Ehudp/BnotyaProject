package com.bnotya.bnotyaapp.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.bnotya.bnotyaapp.CardFlipActivity;
import com.bnotya.bnotyaapp.CardFlipActivity.CardPart;
import com.bnotya.bnotyaapp.R;

public class CardFragment extends Fragment
{
	public CardFragment()
	{
		// Empty constructor required for fragment subclasses
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_card, container,
				false);
		ImageView cardView = (ImageView) view.findViewById(R.id.card);
		
		if (((CardFlipActivity) getActivity()).visibleSide == CardPart.BACK)
			cardView.setImageResource(((CardFlipActivity) getActivity()).card.getBackId());
		else if (((CardFlipActivity) getActivity()).visibleSide == CardPart.FRONT)
			cardView.setImageResource(((CardFlipActivity) getActivity()).card.getFrontId());
		else if (((CardFlipActivity) getActivity()).visibleSide == CardPart.INSIGHT)
			cardView.setImageResource(((CardFlipActivity) getActivity()).card.getInsightId());
		
		return view;
	}
}
