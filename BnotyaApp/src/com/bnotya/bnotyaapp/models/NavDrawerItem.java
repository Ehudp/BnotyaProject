package com.bnotya.bnotyaapp.models;

public class NavDrawerItem
{
    private String _title;
    private int _icon;
    private String _count = "0";
    // boolean to set visibility of the counter
    private boolean _isCounterVisible = false;

    public NavDrawerItem(){}

    public NavDrawerItem(String title, int icon){
        _title = title;
        _icon = icon;
    }

    public NavDrawerItem(String title, int icon, boolean isCounterVisible, String count){
        _title = title;
        _icon = icon;
        _isCounterVisible = isCounterVisible;
        _count = count;
    }

    public String getTitle(){
        return _title;
    }

    public int getIcon(){
        return _icon;
    }

    public String getCount(){
        return _count;
    }

    public boolean getCounterVisibility(){
        return _isCounterVisible;
    }

    public void setTitle(String title){
        _title = title;
    }

    public void setIcon(int icon){
        _icon = icon;
    }

    public void setCount(String count){
        _count = count;
    }

    public void setCounterVisibility(boolean isCounterVisible){
        _isCounterVisible = isCounterVisible;
    }
}
