package com.bnotya.bnotyaapp.adapters;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import com.bnotya.bnotyaapp.R;
import com.bnotya.bnotyaapp.models.ListItem;

public class CustomArrayAdapter extends ArrayAdapter<ListItem> implements
		Filterable
{
	Context _context;
	int _layoutResourceId;
	List<ListItem> _data;
	List<ListItem> _itemsSource;
	private CustomFilter _filter;

	public CustomArrayAdapter(Context context, int layoutResourceId,
			List<ListItem> data)
	{
		super(context, layoutResourceId, data);
		_layoutResourceId = layoutResourceId;
		_context = context;
		_data = data;
		_itemsSource = data;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		ItemHolder holder;

		if (convertView == null)
		{
			convertView = LayoutInflater.from(getContext()).inflate(
					_layoutResourceId, null);

			holder = new ItemHolder();
			holder.imgIcon = (ImageView) convertView.findViewById(R.id.icon);
			holder.txtTitle = (TextView) convertView.findViewById(R.id.title);

			convertView.setTag(holder);
		}
		else
		{
			holder = (ItemHolder) convertView.getTag();
		}

		ListItem itemData = _data.get(position);
		holder.imgIcon.setImageResource(itemData.getIcon());
		holder.txtTitle.setText(itemData.getTitle());

		return convertView;
	}

	static class ItemHolder
	{
		ImageView imgIcon;
		TextView txtTitle;
	}
	
	@Override
	public int getCount () 
	{
	    return _data.size();
	}

	@Override
	public Filter getFilter()
	{
		if (_filter == null) 
			_filter = new CustomFilter();
		return _filter;
	}

	private class CustomFilter extends Filter
	{
		@Override
		protected FilterResults performFiltering(CharSequence constraint)
		{
			FilterResults results = new FilterResults();
			// We implement here the filter logic
			if (constraint == null || constraint.length() == 0)
			{
				// No filter implemented we return all the list
				results.values = _itemsSource;
				results.count = _itemsSource.size();
			}
			else
			{
				// We perform filtering operation
				List<ListItem> itemList = new ArrayList<ListItem>();
				for (ListItem p : _itemsSource)
				{
					if (p.getTitle().toUpperCase()
							.contains(constraint.toString().toUpperCase()))
						itemList.add(p);
				}

				results.values = itemList;
				results.count = itemList.size();
			}
			return results;
		}

		@Override
		protected void publishResults(CharSequence constraint,
				FilterResults results)
		{
			// Now we have to inform the adapter about the new list filtered
			if (results.count == 0)
				notifyDataSetInvalidated();
			else
			{				
				_data = (List<ListItem>)results.values; 
				notifyDataSetChanged();
			}
		}
	}
}
