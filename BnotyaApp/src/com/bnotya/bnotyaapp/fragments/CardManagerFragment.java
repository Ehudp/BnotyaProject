package com.bnotya.bnotyaapp.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TextView;

import com.bnotya.bnotyaapp.R;
import com.bnotya.bnotyaapp.models.Card;

public class CardManagerFragment extends Fragment
{
    private static final int THREE_FRAGMENTS = 3;
    private ViewPager _viewPager;
    private LayoutInflater _layoutInflater;
    public Card CurrentCard;
    private static String _frontKey;
    private static String _backKey;
    private static String _insightKey;

    public static final String ARG_VIEW_NUMBER = "view_number";

    private TabHost.TabContentFactory mFactory = new TabHost.TabContentFactory()
    {
        @Override
        public View createTabContent(String tag)
        {
            View v = new View(getActivity());
            v.setMinimumHeight(0);
            return v;
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View fragContent = inflater.inflate(R.layout.fragment_card_manager,
                container, false);
        int id = getArguments().getInt(ARG_VIEW_NUMBER);
        // Setup CurrentCard
        CurrentCard = new Card(id, getResources(), getActivity().getPackageName());

        _frontKey = getString(R.string.card_front);
        _backKey = getString(R.string.card_back);
        _insightKey = getString(R.string.card_insight);

        _layoutInflater = inflater;
        _viewPager = (ViewPager) fragContent.findViewById(R.id.pager);
        _viewPager.setAdapter(new TransactionInnerPagerAdapter(getChildFragmentManager()));

        final TabHost tabHost = getTabHost(fragContent);
        initOnPageChangeListener(tabHost);
        initOnTabChangedListener(tabHost);

        return fragContent;
    }

    private TabHost getTabHost(View fragContent)
    {
        final TabHost tabHost = (TabHost) fragContent
                .findViewById(android.R.id.tabhost);
        tabHost.setup();
        tabHost.addTab(tabHost.newTabSpec(_frontKey)
                .setIndicator(createTabView(_frontKey))
                .setContent(mFactory));
        tabHost.addTab(tabHost.newTabSpec(_backKey)
                .setIndicator(_backKey)
                .setContent(mFactory));
        tabHost.addTab(tabHost.newTabSpec(_insightKey)
                .setIndicator(_insightKey)
                .setContent(mFactory));
        return tabHost;
    }

    private void initOnPageChangeListener(final TabHost tabHost)
    {
        _viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener()
        {

            @Override
            public void onPageSelected(int position)
            {
                tabHost.setCurrentTab(position);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2)
            {
                // TODO Auto-generated method stub

            }

            @Override
            public void onPageScrollStateChanged(int arg0)
            {
                // TODO Auto-generated method stub

            }
        });
    }

    private void initOnTabChangedListener(TabHost tabHost)
    {
        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {

            @Override
            public void onTabChanged(String tabId) {
                if (tabId.equals(_frontKey)) {
                    _viewPager.setCurrentItem(0);
                } else if (tabId.equals(_backKey)) {
                    _viewPager.setCurrentItem(1);
                } else if (tabId.equals(_insightKey)) {
                    _viewPager.setCurrentItem(2);
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private View createTabView(String tabText)
    {
        View view = _layoutInflater.inflate(R.layout.tab_indicator, null);
        TextView tv = (TextView)view.findViewById(R.id.tabTitleText);
        tv.setText(tabText);
        return view;
    }

    private class TransactionInnerPagerAdapter extends FragmentPagerAdapter
    {

        public TransactionInnerPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return new CardFrontFragment();
            } else if (position == 1) {
                return new CardBackFragment();
            } else if (position == 2) {
                return new CardInsightFragment();
            }
            return null;
        }

        @Override
        public int getCount() {
            return THREE_FRAGMENTS;
        }

    }
}
