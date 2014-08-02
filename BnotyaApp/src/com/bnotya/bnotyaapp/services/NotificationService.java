package com.bnotya.bnotyaapp.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import java.util.Calendar;

public class NotificationService
{
	private static PendingIntent _pendingAlarmIntent;
	
	public static void cancelNotification(Context context)
	{
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(_pendingAlarmIntent);
		
		disableAlarm(context);
	}
	
	public static void handleNotification(Context context, int hour, int minute) 
	{		
		enableAlarm(context);
		
	    Intent alarmIntent = new Intent(context, AlarmReceiver.class);
	    _pendingAlarmIntent = PendingIntent.
	    		getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
	    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);	    
	    
	    // Set the alarm
	    Calendar calendar = Calendar.getInstance();
	    calendar.setTimeInMillis(System.currentTimeMillis());
	    
	    calendar.set(Calendar.HOUR_OF_DAY, hour);
	    calendar.set(Calendar.MINUTE, minute);
	    
	    alarmManager.setInexactRepeating(AlarmManager.RTC, calendar.getTimeInMillis(),
	            AlarmManager.INTERVAL_DAY, _pendingAlarmIntent);
	}
	
	private static void enableAlarm(Context context)
	{
		ComponentName receiver = new ComponentName(context, AlarmReceiver.class);
		PackageManager pm = context.getPackageManager();

		pm.setComponentEnabledSetting(receiver,
		        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
		        PackageManager.DONT_KILL_APP);
	}
	
	private static void disableAlarm(Context context)
	{
		ComponentName receiver = new ComponentName(context, AlarmReceiver.class);
		PackageManager pm = context.getPackageManager();

		pm.setComponentEnabledSetting(receiver,
		        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
		        PackageManager.DONT_KILL_APP);
	}
}
