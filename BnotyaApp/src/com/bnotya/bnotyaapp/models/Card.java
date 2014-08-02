package com.bnotya.bnotyaapp.models;

import android.content.Intent;
import android.content.res.Resources;

public class Card
{
	private final int _backId;
	private final int _frontId;
	private final int _insightId;
    private final int _id;
    
    public Card(Intent intent, Resources resources, String packageName)
    {	
    	int cardId = 1;		
		
		cardId += intent.getIntExtra("EXTRA_SESSION_ID", 0);		

		_frontId = resources.getIdentifier(String.format("card%s", cardId),
				"drawable", packageName);
		_backId = resources.getIdentifier(
				String.format("card%sa", cardId), "drawable",
				packageName);
		_insightId = resources.getIdentifier(
				String.format("card%sb", cardId), "drawable",
				packageName);
		
		_id = cardId;
    }
    
    public int getInsightId()
    {
    	return _insightId;
    }
    
    public int getBackId()
    {
    	return _backId;
    }
    
    public int getFrontId()
    {
    	return _frontId;
    }
    
    public int getId()
    {
    	return _id;
    }
}
