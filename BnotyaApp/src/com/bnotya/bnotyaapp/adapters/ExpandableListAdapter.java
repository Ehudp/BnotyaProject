package com.bnotya.bnotyaapp.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.bnotya.bnotyaapp.R;
import com.bnotya.bnotyaapp.models.NavDrawerItem;

import java.util.HashMap;
import java.util.List;

public class ExpandableListAdapter extends BaseExpandableListAdapter
{
	private Context _context;
	private List<NavDrawerItem> _listDataHeaders; // header titles
	// child data in format of header title, child title
	private HashMap<String, List<NavDrawerItem>> _listDataChildren;

	public ExpandableListAdapter(Context context,
			List<NavDrawerItem> listDataHeader,
			HashMap<String, List<NavDrawerItem>> listChildData)
	{
		this._context = context;
		this._listDataHeaders = listDataHeader;
		this._listDataChildren = listChildData;
	}

	@Override
	public Object getChild(int groupPosition, int childPosititon)
	{
		return this._listDataChildren.get(
				this._listDataHeaders.get(groupPosition).getTitle()).get(childPosititon);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition)
	{
		return childPosition;
	}

	@Override
	public View getChildView(int groupPosition, final int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent)
	{
		final NavDrawerItem child = (NavDrawerItem) getChild(groupPosition, childPosition);

		if (convertView == null)
		{
			LayoutInflater infalInflater = (LayoutInflater) this._context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = infalInflater
					.inflate(R.layout.drawer_list_item, null);
		}
		
		ImageView imgIcon = (ImageView) convertView.findViewById(R.id.iconChild);
		TextView txtTitle = (TextView) convertView.findViewById(R.id.titleChild);
		TextView txtCount = (TextView) convertView.findViewById(R.id.counterChild);

		imgIcon.setImageResource(child.getIcon());
		txtTitle.setText(child.getTitle());		

		// displaying count
		// check whether it set visible or not
		if (child.getCounterVisibility())
		{
			txtCount.setText(child.getCount());
		}
		else
		{
			// hide the counter view
			txtCount.setVisibility(View.GONE);
		}
		
		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition)
	{
		return this._listDataChildren.get(
				this._listDataHeaders.get(groupPosition).getTitle()).size();
	}

	@Override
	public Object getGroup(int groupPosition)
	{
		return this._listDataHeaders.get(groupPosition);
	}

	@Override
	public int getGroupCount()
	{
		return this._listDataHeaders.size();
	}

	@Override
	public long getGroupId(int groupPosition)
	{
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent)
	{
		final NavDrawerItem group = (NavDrawerItem) _listDataHeaders.get(groupPosition);
		
		if (convertView == null)
		{
			LayoutInflater infalInflater = (LayoutInflater) this._context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = infalInflater.inflate(R.layout.drawer_list_group,
					null);
		}

		ImageView imgIcon = (ImageView) convertView.findViewById(R.id.icon);
		TextView txtTitle = (TextView) convertView.findViewById(R.id.title);
		TextView txtCount = (TextView) convertView.findViewById(R.id.counter);

		imgIcon.setImageResource(group.getIcon());
		txtTitle.setText(group.getTitle());
		txtTitle.setTypeface(null, Typeface.BOLD);

		// displaying count
		// check whether it set visible or not
		if (group.getCounterVisibility())
		{
			txtCount.setText(group.getCount());
		}
		else
		{
			// hide the counter view
			txtCount.setVisibility(View.GONE);
		}

		return convertView;
	}

	@Override
	public boolean hasStableIds()
	{
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition)
	{
		return true;
	}

}