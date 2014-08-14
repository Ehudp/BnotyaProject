package com.bnotya.bnotyaapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class SplashActivity extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.activity_splash);
		
		Thread logoTimer = new Thread()
		{
			public void run()
			{
				try
				{
					sleep(1);
					startActivity(new Intent("com.bnotya.bnotyaapp.MainActivity"));
				}
				catch(InterruptedException e)
				{
					e.printStackTrace();
				}
				finally
				{
					finish();
				}
			}
		};
		
		logoTimer.start();
	}
}
