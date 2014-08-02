package com.bnotya.bnotyaapp.helpers;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class About
{
	public static void showAboutDialog(Context context)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage("Look at this dialog!")
		       .setCancelable(false)
		       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                //do things
		           }
		       });
		AlertDialog alert = builder.create();
		alert.show();
	}
}
