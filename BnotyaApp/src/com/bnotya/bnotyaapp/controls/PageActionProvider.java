package com.bnotya.bnotyaapp.controls;

import com.bnotya.bnotyaapp.R;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ActionProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;

public class PageActionProvider extends ActionProvider
{
	private Context _context;
	private Intent _intent;
	private ImageButton _button;
	private int _buttonSourceId;

	public PageActionProvider(Context context)
	{
		super(context);
		_context = context;
	}

	@Override
	public View onCreateActionView()
	{
		// Inflate the action view to be shown on the action bar.
		LayoutInflater layoutInflater = LayoutInflater.from(_context);
		View view = layoutInflater.inflate(R.layout.action_provider, null);
		_button = (ImageButton) view.findViewById(R.id.btnPageAction);
		_button.setImageResource(_buttonSourceId);
		_button.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Bundle extras = _intent.getExtras();
				if (extras.containsKey(android.content.Intent.EXTRA_EMAIL))
				{
					_context.startActivity(Intent.createChooser(_intent,
							_context.getString(R.string.chooser_title)));
				}
			}
		});
		return view;
	}

	public void setIntent(Intent intent)
	{
		_intent = intent;
	}

	public void setButtonSource(int resourceId)
	{
		_buttonSourceId = resourceId;
	}
}
