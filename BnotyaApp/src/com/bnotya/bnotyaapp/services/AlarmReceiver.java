package com.bnotya.bnotyaapp.services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import com.bnotya.bnotyaapp.InsightActivity;
import com.bnotya.bnotyaapp.MainActivity;
import com.bnotya.bnotyaapp.R;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class AlarmReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) 
		{
			//oldNotificationBuilder(context, intent);
			initNotification(context);
        }		
	}
	
	private void oldNotificationBuilder(Context context, Intent intent)
	{
		Calendar now = GregorianCalendar.getInstance();
		int dayOfWeek = now.get(Calendar.DATE);
		if (dayOfWeek != 1 && dayOfWeek != 7)
		{
			NotificationCompat.Builder builder = new NotificationCompat.Builder(
					context)
					.setSmallIcon(R.drawable.ic_launcher)
					.setContentTitle(
							context.getResources().getString(
									R.string.app_name))
					.setContentText(
							context.getResources().getString(
									R.string.app_name));
			Intent resultIntent = new Intent(context, MainActivity.class);
			TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
			stackBuilder.addParentStack(MainActivity.class);
			stackBuilder.addNextIntent(resultIntent);
			PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
					0, PendingIntent.FLAG_UPDATE_CURRENT);
			builder.setContentIntent(resultPendingIntent);
			NotificationManager notificationManager = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.notify(1, builder.build());
		}
	}
	
	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	private void initNotification(Context context)
	{
		Intent intent = new Intent(context, InsightActivity.class);
		
		int numberOfCards = context.getResources().getInteger(
				R.integer.number_of_cards) - 1;
		// Random from 1 to number of cards
		int id = 0;
		id += (Math.random() * numberOfCards);
		
		intent.putExtra("EXTRA_SESSION_ID", id);
		
		// Sets the Activity to start in a new, empty task
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_CLEAR_TASK);

		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);

		Notification notification;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		{
			notification = new NotificationCompat.Builder(context)
					.setContentTitle("Localize").setContentText("Localize")
					.setSmallIcon(R.drawable.ic_launcher)
					.setContentIntent(pendingIntent).build();
		}
		else
		{
			notification = new Notification(R.drawable.ic_launcher, "Localize",
					System.currentTimeMillis());
			notification.setLatestEventInfo(context, "Localize", "Localize",
					pendingIntent);
			notification.defaults = Notification.DEFAULT_ALL;
		}

		NotificationManager notificationManager = (NotificationManager)context.
				getSystemService(Context.NOTIFICATION_SERVICE);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notificationManager.notify(1234567, notification);
	}
}
