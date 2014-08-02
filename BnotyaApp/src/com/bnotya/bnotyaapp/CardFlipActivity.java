package com.bnotya.bnotyaapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBarActivity;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import com.bnotya.bnotyaapp.fragments.CardFragment;
import com.bnotya.bnotyaapp.helpers.About;
import com.bnotya.bnotyaapp.models.Card;

public class CardFlipActivity extends ActionBarActivity implements
		OnGestureListener, OnDoubleTapListener
{	
	public enum CardPart {
		   FRONT,
		   BACK,
		   INSIGHT
		}
	public CardPart visibleSide;
	private GestureDetectorCompat _detector;
	public Card card;
	private ActionBar _actionBar;
	private ActionBar.TabListener _tabListener;
	private Tab _cardFrontTab;
	private Tab _cardBackTab;
	private Tab _cardInsightTab;	

	@SuppressLint("NewApi") 
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_card_flip);
		
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
		
		// Setup card
		card = new Card(getIntent(), getResources(), getPackageName());	

		visibleSide = CardPart.FRONT;
		InitTabs();		

		if (savedInstanceState == null)
		{
			// If there is no saved instance state, add a fragment representing
			// the front of the card to this activity.
			// If there is saved instance state,
			// this fragment will have already been added to the activity.
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new CardFragment()).commit();
		}

		// Detect touched area
		_detector = new GestureDetectorCompat(this, this);
		_detector.setOnDoubleTapListener(this);	
	}	

	private void InitTabs()
	{
		_actionBar = getSupportActionBar();
		_actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Create a tab listener that is called when the user changes tabs.
		_tabListener = new ActionBar.TabListener()
		{
			@Override
			public void onTabReselected(Tab tab, FragmentTransaction arg1)
			{

			}

			@Override
			public void onTabSelected(Tab tab, FragmentTransaction arg1)
			{
				if (_actionBar.getSelectedTab() == _cardFrontTab && visibleSide != CardPart.FRONT)
				{
					visibleSide = CardPart.FRONT;
					flipCard();
				}
				else if(_actionBar.getSelectedTab() == _cardBackTab	&& visibleSide != CardPart.BACK)
				{
					visibleSide = CardPart.BACK;
					flipCard();
				}					
				else if( _actionBar.getSelectedTab() == _cardInsightTab	&& visibleSide != CardPart.INSIGHT)
				{
					visibleSide = CardPart.INSIGHT;
					flipCard();
				}
			}

			@Override
			public void onTabUnselected(Tab tab, FragmentTransaction arg1)
			{

			}
		};

		// Add tabs, specifying the tab's text and TabListener
		_cardFrontTab = _actionBar.newTab().setText(R.string.card_front)
				.setTabListener(_tabListener);
		_cardBackTab = _actionBar.newTab().setText(R.string.card_back)
				.setTabListener(_tabListener);
		_cardInsightTab = _actionBar.newTab().setText(R.string.card_insight)
				.setTabListener(_tabListener);

		_actionBar.addTab(_cardFrontTab);
		_actionBar.addTab(_cardBackTab);
		_actionBar.addTab(_cardInsightTab);
	}

	private void SelectTab()
	{
		if (_actionBar.getSelectedTab() == _cardFrontTab)
		{
			_actionBar.selectTab(_cardBackTab);
			visibleSide = CardPart.BACK;
		}
		else if(_actionBar.getSelectedTab() == _cardBackTab)
		{
			_actionBar.selectTab(_cardInsightTab);
			visibleSide = CardPart.INSIGHT;
		}
		else if(_actionBar.getSelectedTab() == _cardInsightTab)
		{
			_actionBar.selectTab(_cardFrontTab);
			visibleSide = CardPart.FRONT;
		}
	}
	
	private void SelectBackwardsTab()
	{
		if (_actionBar.getSelectedTab() == _cardFrontTab)
		{
			_actionBar.selectTab(_cardInsightTab);
			visibleSide = CardPart.INSIGHT;
		}
		else if(_actionBar.getSelectedTab() == _cardBackTab)
		{
			_actionBar.selectTab(_cardFrontTab);
			visibleSide = CardPart.FRONT;
		}
		else if(_actionBar.getSelectedTab() == _cardInsightTab)
		{
			_actionBar.selectTab(_cardBackTab);
			visibleSide = CardPart.BACK;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.page_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case android.R.id.home:
				NavUtils.navigateUpFromSameTask(this);
				//navigateToParent();
				return true;
			case R.id.action_home:
				NavUtils.navigateUpTo(this, new Intent(this, MainActivity.class));				
				return true;
			case R.id.action_settings:
				startActivity(new Intent(this, Preferences.class));
				return true;
			case R.id.action_about:
				About.showAboutDialog(this);
				return true;
			case R.id.action_exit:
				exitApplication();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	// Do not remove!
	private void navigateToParent()
	{
		Intent upIntent = NavUtils.getParentActivityIntent(this);
        /*if (NavUtils.shouldUpRecreateTask(this, upIntent)) 
        {*/
            // This activity is NOT part of this app's task, so create a new task
            // when navigating up, with a synthesized back stack.
            TaskStackBuilder.create(this)
                    // Add all of this activity's parents to the back stack
                    .addNextIntentWithParentStack(upIntent)
                    // Navigate up to the closest parent
                    .startActivities();
        /*} 
        else 
        {
            // This activity is part of this app's task, so simply
            // navigate up to the logical parent activity.
            NavUtils.navigateUpTo(this, upIntent);
        }*/
	}
	
	private void exitApplication()
	{
		Intent intent = new Intent(this, MainActivity.class);
	    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	    intent.putExtra("EXIT", true);
	    startActivity(intent);
	    finish();
	}

	private void flipCard()
	{
		// Create and commit a new fragment transaction that adds the fragment
		// for the card and uses custom animations.
		getSupportFragmentManager().beginTransaction()
		// Replace the default fragment animations with animator resources
		// representing rotations when switching to the back of the card,
		// as well as animator resources representing rotations when flipping
		// back to the front.
				.setCustomAnimations(R.anim.card_flip_right_in,
						R.anim.card_flip_right_out, R.anim.card_flip_left_in,
						R.anim.card_flip_left_out)
				// Replace any fragments currently in the container view with a
				// fragment representing the next page
				// (indicated by the just-incremented
				// currentPage variable).
				.replace(R.id.container, new CardFragment())
				// Commit the transaction.
				.commit();
	}

	/* Touch Implementation */

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		this._detector.onTouchEvent(event);
		// Be sure to call the superclass implementation
		return super.onTouchEvent(event);
	}

	@Override
	public boolean onDown(MotionEvent event)
	{
		/*
		 * Toast.makeText(getApplicationContext(), "onDown: " +
		 * event.toString(), Toast.LENGTH_SHORT).show();
		 */
		return true;
	}

	@Override
	public boolean onFling(MotionEvent event1, MotionEvent event2,
			float velocityX, float velocityY)
	{
		/*
		 * Toast.makeText(getApplicationContext(), "onFling: " +
		 * event1.toString()+event2.toString(), Toast.LENGTH_SHORT).show();
		 */
		// Check if movement is left or right
		float x1 = event1.getX();
		float x2 = event2.getX();
		
		if(x1 - x2 > 0)			
			SelectTab();
		else			
			SelectBackwardsTab();
			
		return true;
	}

	@Override
	public void onLongPress(MotionEvent event)
	{
		/*
		 * Toast.makeText(getApplicationContext(), "onLongPress: " +
		 * event.toString(), Toast.LENGTH_SHORT).show();
		 */
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY)
	{
		/*
		 * Toast.makeText(getApplicationContext(), "onScroll: " +
		 * e1.toString()+e2.toString(), Toast.LENGTH_SHORT).show();
		 */
		// SelectTab();
		return true;
	}

	@Override
	public void onShowPress(MotionEvent event)
	{
		/*
		 * Toast.makeText(getApplicationContext(), "onShowPress: " +
		 * event.toString(), Toast.LENGTH_SHORT).show();
		 */
	}

	@Override
	public boolean onSingleTapUp(MotionEvent event)
	{
		/*
		 * Toast.makeText(getApplicationContext(), "onSingleTapUp: " +
		 * event.toString(), Toast.LENGTH_SHORT).show();
		 */
		return true;
	}

	@Override
	public boolean onDoubleTap(MotionEvent event)
	{
		/*
		 * Toast.makeText(getApplicationContext(), "onDoubleTap: " +
		 * event.toString(), Toast.LENGTH_SHORT).show();
		 */
		SelectTab();
		return true;
	}

	@Override
	public boolean onDoubleTapEvent(MotionEvent event)
	{
		/*
		 * Toast.makeText(getApplicationContext(), "onDoubleTapEvent: " +
		 * event.toString(), Toast.LENGTH_SHORT).show();
		 */
		return true;
	}

	@Override
	public boolean onSingleTapConfirmed(MotionEvent event)
	{
		/*
		 * Toast.makeText(getApplicationContext(), "onSingleTapConfirmed: " +
		 * event.toString(), Toast.LENGTH_SHORT).show();
		 */
		return true;
	}

	/* End of Touch Implementation */

	public void flipCard(View view)
	{
		SelectTab();
	}
}
