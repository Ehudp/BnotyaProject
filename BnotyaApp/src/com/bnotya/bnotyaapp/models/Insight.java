package com.bnotya.bnotyaapp.models;

import android.content.Intent;
import android.content.res.Resources;
import com.bnotya.bnotyaapp.R;

public class Insight
{
	private final int _insightId;
	private final int _id;
	private boolean _isFavorite;
	private final String _name;

	public Insight(Intent intent, Resources resources, String packageName)
	{
		int id = 1;

		id += intent.getIntExtra("EXTRA_SESSION_ID", 0);

		_insightId = resources.getIdentifier(
				String.format("card%sb", id), "drawable", packageName);

		_id = id;
		
		_isFavorite = false;
		
		String[] names = resources.getStringArray(R.array.women_names_array);
		_name = names[id - 1];
	}
	
	public Insight(int id, boolean isFavorite, String name, Resources resources, String packageName)
	{
		_insightId = resources.getIdentifier(
				String.format("card%sb", id), "drawable", packageName);

		_id = id;
		
		_isFavorite = isFavorite;
		
		_name = name;
	}

	public int getInsightId()
	{
		return _insightId;
	}

	public int getId()
	{
		return _id;
	}
	
	public boolean getIsFavorite()
	{
		return _isFavorite;
	}
	
	public void setIsFavorite(boolean value)
	{
		_isFavorite = value;
	}
	
	public String getName()
	{
		return _name;
	}
}
