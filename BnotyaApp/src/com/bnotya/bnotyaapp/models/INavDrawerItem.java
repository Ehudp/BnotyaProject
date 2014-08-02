package com.bnotya.bnotyaapp.models;


public interface INavDrawerItem
{
    public int getId();
    public String getLabel();
    public int getType();
    public boolean isEnabled();
    public boolean updateActionBarTitle();
}
