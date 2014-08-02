package com.bnotya.bnotyaapp;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {		
		case R.id.action_open_tehilot:
			openTehilotPage(null);
			return true;
		case R.id.action_open_women_menu:
			openWomenPage(null);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void openTehilotPage(View view) {
		startActivity(new Intent(this, MainTehilotActivity.class));
	}

	public void openWomenPage(View view) {
		startActivity(new Intent(this, MainWomenActivity.class));
	}
}
