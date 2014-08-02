package com.bnotya.bnotyaapp.services;

import android.content.Context;
import com.bnotya.bnotyaapp.helpers.DatabaseHelper;

public class DataBaseService
{
	// Database Helper
		public static DatabaseHelper dbHelper;
		
		public static void initDatabaseHelper(Context context)
		{
			dbHelper = DatabaseHelper.getInstance(context);
			
			dbHelper.clearDb();

			dbHelper.createDb();
		}
}
