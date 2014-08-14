package com.bnotya.bnotyaapp.models;

public class ListItem
{
	private String _title;
	private int _icon;

	public ListItem()
	{
	}

	public ListItem(String title, int icon)
	{
		_title = title;
		_icon = icon;
	}

	public String getTitle()
	{
		return _title;
	}

	public int getIcon()
	{
		return _icon;
	}

	public void setTitle(String title)
	{
		_title = title;
	}

	public void setIcon(int icon)
	{
		_icon = icon;
	}
}
