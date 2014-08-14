package com.bnotya.bnotyaapp.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bnotya.bnotyaapp.R;

public class CardBackFragment extends CardFragment
{
    public CardBackFragment()
    {
        // Empty constructor required for fragment subclasses
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        _cardView.setImageResource(((CardManagerFragment)getParentFragment()).CurrentCard
                .getBackId());

        return view;
    }
}
