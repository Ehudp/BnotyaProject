package com.bnotya.bnotyaapp;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class WomenListActivity extends ListActivity 
{
	String[] womenNames = { "First", "Second", "Third" };

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);		
		setListAdapter(new ArrayAdapter<String>(
				this, android.R.layout.simple_list_item_1, womenNames));
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	@Override
	protected void onListItemClick(
			ListView listView, View view, int position, long id)
	{
		super.onListItemClick(listView, view, position, id);
		String selectedWoman = womenNames[position];
		/* TODO: create activities for women
		try
		{
			Class selected = Class.forName("com.bnotya.bnotyaapp." + selectedWoman);
			startActivity(new Intent(this, selected));
		}
		catch(ClassNotFoundException e)
		{
			e.printStackTrace();
		}	
		*/	
	}
	
	@Override
	protected void onPause()
	{
		super.onPause();
		finish();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.women_list_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {		
		case R.id.action_open_search:
			openSearch(null);
			return true;		
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void openSearch(View view) 
	{		
		
	}
}
