package com.bnotya.bnotyaapp.controls;

/*
 * The following code was written by Matthew Wiggins and is released under the
 * APACHE 2.0 license
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Improvements : - save the value on positive button click, not on seekbar
 * change - handle @string/... values in xml file
 */

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class SeekBarPreference extends DialogPreference implements
		SeekBar.OnSeekBarChangeListener, OnClickListener
{
	// ------------------------------------------------------------------------------------------
	// Private attributes :
	private static final String _androidNS = "http://schemas.android.com/apk/res/android";

	private SeekBar _seekBar;
	private TextView _splashText;
    private TextView _valueText;
	private Context _context;

	private String _dialogMessage;
    private String _suffix;
	private int _default = 0;
    private int _max = 0;
    private int _value = 0;

	// ------------------------------------------------------------------------------------------

	// ------------------------------------------------------------------------------------------
	// Constructor :
	public SeekBarPreference(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		_context = context;

		// Get string value for dialogMessage :
		int mDialogMessageId = attrs.getAttributeResourceValue(_androidNS,
				"dialogMessage", 0);
		if (mDialogMessageId == 0)
			_dialogMessage = attrs
					.getAttributeValue(_androidNS, "dialogMessage");
		else
			_dialogMessage = _context.getString(mDialogMessageId);

		// Get string value for suffix (text attribute in xml file) :
		int mSuffixId = attrs.getAttributeResourceValue(_androidNS, "text", 0);
		if (mSuffixId == 0)
			_suffix = attrs.getAttributeValue(_androidNS, "text");
		else
			_suffix = _context.getString(mSuffixId);

		// Get default and max seekbar values :
		_default = attrs.getAttributeIntValue(_androidNS, "defaultValue", 0);
		_max = attrs.getAttributeIntValue(_androidNS, "max", 100);
	}

	// ------------------------------------------------------------------------------------------

	// ------------------------------------------------------------------------------------------
	// DialogPreference methods :
	@Override
	protected View onCreateDialogView()
	{
		LinearLayout.LayoutParams params;
		LinearLayout layout = new LinearLayout(_context);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setPadding(6, 6, 6, 6);

		_splashText = new TextView(_context);
		_splashText.setPadding(30, 10, 30, 10);
		if (_dialogMessage != null) _splashText.setText(_dialogMessage);
		layout.addView(_splashText);

		_valueText = new TextView(_context);
		_valueText.setGravity(Gravity.CENTER_HORIZONTAL);
		_valueText.setTextSize(32);
		params = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		layout.addView(_valueText, params);

		_seekBar = new SeekBar(_context);
		_seekBar.setOnSeekBarChangeListener(this);
		layout.addView(_seekBar, new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT));

		if (shouldPersist()) _value = getPersistedInt(_default);

		_seekBar.setMax(_max);
		_seekBar.setProgress(_value);

		return layout;
	}

	@Override
	protected void onBindDialogView(View v)
	{
		super.onBindDialogView(v);
		_seekBar.setMax(_max);
		_seekBar.setProgress(_value);
	}

	@Override
	protected void onSetInitialValue(boolean restore, Object defaultValue)
	{
		super.onSetInitialValue(restore, defaultValue);
		if (restore)
			_value = shouldPersist() ? getPersistedInt(_default) : 0;
		else
			_value = (Integer) defaultValue;
	}

	// ------------------------------------------------------------------------------------------

	// ------------------------------------------------------------------------------------------
	// OnSeekBarChangeListener methods :
	@Override
	public void onProgressChanged(SeekBar seek, int value, boolean fromTouch)
	{
		String t = String.valueOf(value);
		_valueText.setText(_suffix == null ? t : t.concat(" " + _suffix));
	}

	@Override
	public void onStartTrackingTouch(SeekBar seek)
	{
	}

	@Override
	public void onStopTrackingTouch(SeekBar seek)
	{
	}

	public void setMax(int max)
	{
		_max = max;
	}

	public int getMax()
	{
		return _max;
	}

	public void setProgress(int progress)
	{
		_value = progress;
		if (_seekBar != null) _seekBar.setProgress(progress);
	}

	public int getProgress()
	{
		return _value;
	}

	// ------------------------------------------------------------------------------------------

	// ------------------------------------------------------------------------------------------
	// Set the positive button listener and onClick action :
	@Override
	public void showDialog(Bundle state)
	{

		super.showDialog(state);

		Button positiveButton = ((AlertDialog) getDialog())
				.getButton(AlertDialog.BUTTON_POSITIVE);
		positiveButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View v)
	{

		if (shouldPersist())
		{

			_value = _seekBar.getProgress();
			persistInt(_seekBar.getProgress());
			callChangeListener(Integer.valueOf(_seekBar.getProgress()));
		}

		getDialog().dismiss();
	}
	// ------------------------------------------------------------------------------------------
}