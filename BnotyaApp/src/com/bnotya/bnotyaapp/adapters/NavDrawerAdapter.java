package com.bnotya.bnotyaapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.bnotya.bnotyaapp.R;
import com.bnotya.bnotyaapp.models.INavDrawerItem;
import com.bnotya.bnotyaapp.models.NavMenuItem;
import com.bnotya.bnotyaapp.models.NavMenuSection;

public class NavDrawerAdapter extends ArrayAdapter<INavDrawerItem>
{
    private LayoutInflater _inflater;

    public NavDrawerAdapter(Context context, int textViewResourceId, INavDrawerItem[] objects)
    {
        super(context, textViewResourceId, objects);
        _inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View view;
        INavDrawerItem menuItem = this.getItem(position);
        if (menuItem.getType() == NavMenuItem.ITEM_TYPE)
        {
            view = getItemView(convertView, parent, menuItem);
        }
        else
        {
            view = getSectionView(convertView, parent, menuItem);
        }
        return view;
    }

    public View getItemView(View convertView, ViewGroup parentView, INavDrawerItem navDrawerItem)
    {
        NavMenuItem menuItem = (NavMenuItem) navDrawerItem;
        NavMenuItemHolder navMenuItemHolder = null;

        if (convertView == null)
        {
            convertView = _inflater.inflate(R.layout.navdrawer_item, parentView, false);
            TextView labelView = (TextView) convertView
                    .findViewById(R.id.navmenuitem_label);
            ImageView iconView = (ImageView) convertView
                    .findViewById(R.id.navmenuitem_icon);

            navMenuItemHolder = new NavMenuItemHolder();
            navMenuItemHolder._labelView = labelView;
            navMenuItemHolder._iconView = iconView;

            convertView.setTag(navMenuItemHolder);
        }

        if (navMenuItemHolder == null)
        {
            navMenuItemHolder = (NavMenuItemHolder) convertView.getTag();
        }

        navMenuItemHolder._labelView.setText(menuItem.getLabel());
        navMenuItemHolder._iconView.setImageResource(menuItem.getIcon());

        return convertView;
    }

    public View getSectionView(View convertView, ViewGroup parentView,
                               INavDrawerItem navDrawerItem)
    {
        NavMenuSection menuSection = (NavMenuSection) navDrawerItem;
        NavMenuSectionHolder navMenuItemHolder = null;

        if (convertView == null)
        {
            convertView = _inflater.inflate(R.layout.navdrawer_section, parentView, false);
            TextView labelView = (TextView) convertView
                    .findViewById(R.id.navmenusection_label);

            navMenuItemHolder = new NavMenuSectionHolder();
            navMenuItemHolder._labelView = labelView;
            convertView.setTag(navMenuItemHolder);
        }

        if (navMenuItemHolder == null)
        {
            navMenuItemHolder = (NavMenuSectionHolder) convertView.getTag();
        }

        navMenuItemHolder._labelView.setText(menuSection.getLabel());

        return convertView;
    }

    @Override
    public int getViewTypeCount()
    {
        return 2;
    }

    @Override
    public int getItemViewType(int position)
    {
        return this.getItem(position).getType();
    }

    @Override
    public boolean isEnabled(int position)
    {
        return getItem(position).isEnabled();
    }

    private static class NavMenuItemHolder
    {
        private TextView _labelView;
        private ImageView _iconView;
    }

    private class NavMenuSectionHolder
    {
        private TextView _labelView;
    }
}
