package com.bnotya.bnotyaapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import com.bnotya.bnotyaapp.adapters.CustomArrayAdapter;
import com.bnotya.bnotyaapp.helpers.About;
import com.bnotya.bnotyaapp.models.Insight;
import com.bnotya.bnotyaapp.models.ListItem;
import com.bnotya.bnotyaapp.services.DataBaseService;
import com.bnotya.bnotyaapp.services.RecentInsightsContainer;
import com.bnotya.bnotyaapp.controls.DragSortController;
import com.bnotya.bnotyaapp.controls.DragSortListView;
import java.util.ArrayList;
import java.util.List;

public class InsightListActivity extends ActionBarActivity
{
	private DragSortListView _listView;
	private CustomArrayAdapter _adapter;
	private Insight _removedInsight;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		initView();
	}	

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		initView();
	}	

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.page_menu, menu);	
		
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle presses on the action bar items
		switch (item.getItemId())
		{
			case android.R.id.home:
				_removedInsight = null;
				NavUtils.navigateUpTo(this,
						new Intent(this, MainActivity.class));				
	            return true;
			case R.id.action_home:
				_removedInsight = null;
				NavUtils.navigateUpTo(this,
						new Intent(this, MainActivity.class));					
	            return true;				
			case R.id.action_settings:
				startActivity(new Intent(this, Preferences.class));
				return true;
			case R.id.action_about:
				About.showAboutDialog(this);
				return true;			
			case R.id.action_exit:
				_removedInsight = null;
				exitApplication();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private DragSortListView.RemoveListener onRemove = new DragSortListView.RemoveListener()
	{
	    @Override
	    public void remove(int which)
	    {	    	
	    	List<Insight> insights = DataBaseService.dbHelper.getFavoriteInsights(getBaseContext());
	    	_removedInsight = insights.get(which);
	    	_removedInsight.setIsFavorite(false);
	    	DataBaseService.dbHelper.updateInsight(_removedInsight);	    	
	    }
	};
	
	public void undoInsightRemoval(View view)
	{
		if(_removedInsight != null)
		{
			_removedInsight.setIsFavorite(true);
	    	DataBaseService.dbHelper.updateInsight(_removedInsight);
	    	initListView();
		}		
	}
	
	public void openRandomInsight(View view)
	{
		Intent intent = new Intent(this, InsightActivity.class);

		int numberOfCards = getResources()
				.getInteger(R.integer.number_of_cards) - 1;

		int id = 0;

		do
		{
			id = RecentInsightsContainer.getRandomId(numberOfCards);
		} while (RecentInsightsContainer.recentInsights.contains(id));

		intent.putExtra("EXTRA_SESSION_ID", id);
		RecentInsightsContainer.recentInsights.add(id);

		if (RecentInsightsContainer.recentInsights.size() > 10) 
			RecentInsightsContainer.recentInsights.poll();

		startActivity(intent);
	}
	
	private void exitApplication()
	{
		Intent intent = new Intent(this, MainActivity.class);
	    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	    intent.putExtra("EXIT", true);
	    startActivity(intent);
	    finish();
	}

	@SuppressLint("NewApi") 
	private void initView()
	{
		setContentView(R.layout.insight_list_view);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		{
			getActionBar().setDisplayHomeAsUpEnabled(true);
			getActionBar().setHomeButtonEnabled(true);
		}
		else
		{
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setHomeButtonEnabled(true);
		}

		initListView();
	}
	
	private void initListView()
	{
		// Get ListView object from xml
				_listView = (DragSortListView) findViewById(R.id.insightlist);
				
				final List<Insight> insights = DataBaseService.dbHelper.getFavoriteInsights(this);
				List<ListItem> listDataHeaders = fillData(R.array.women_list_icons, insights);

				// Define a new Adapter				
				_adapter = new CustomArrayAdapter(this,
						R.layout.insight_list_item, listDataHeaders);		

				// Assign adapter to ListView
				_listView.setAdapter(_adapter);		
				_listView.setRemoveListener(onRemove);		
				
				DragSortController controller = new DragSortController(_listView);
			    controller.setDragHandleId(R.id.title);
			            
			    controller.setRemoveEnabled(true);
			    controller.setSortEnabled(false);
			    controller.setDragInitMode(1);
			    controller.setRemoveMode(DragSortController.FLING_REMOVE | DragSortController.ON_DRAG);

			    _listView.setFloatViewManager(controller);
			    _listView.setOnTouchListener(controller);
			    _listView.setDragEnabled(true);

				// ListView Item Click Listener
				_listView.setOnItemClickListener(new OnItemClickListener()
				{
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id)
					{
						Intent intent = new Intent(getBaseContext(),
								InsightActivity.class);
						intent.putExtra("EXTRA_SESSION_ID", insights.get(position).getId() - 1);
						intent.putExtra("EXTRA_IS_FROM_LIST", true);
						startActivity(intent);
					}
				});
	}
	
	private List<ListItem> fillData(int iconsID, List<Insight> insights)
	{	
		// set insight names as titles						
		int size = insights.size();
		// Load item values		
		TypedArray icons = getResources().obtainTypedArray(iconsID);		
		List<ListItem> result = new ArrayList<ListItem>();

		// Adding items to array
		for (int i = 0; i < size; i++)
		{
			result.add(new ListItem(insights.get(i).getName(), 
					icons.getResourceId(i, -1)));
		}

		// Recycle the typed array
		icons.recycle();

		return result;
	}
}

