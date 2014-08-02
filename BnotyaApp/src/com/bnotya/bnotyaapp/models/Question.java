package com.bnotya.bnotyaapp.models;

public class Question
{
	int _id;
	String _content;

	public Question()
	{
		
	}
	
	public Question(String content)
	{		
		_content = content;
	}
	
	public void setId(int id)
	{
		_id = id;
	}
	
	public void setContent(String content)
	{
		_content = content;
	}
	
	public long getId() 
	{
        return _id;
    }
 
    public String getContent() 
    {
        return _content;
    }
}
