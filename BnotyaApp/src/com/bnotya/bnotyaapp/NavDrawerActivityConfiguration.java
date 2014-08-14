package com.bnotya.bnotyaapp;

import android.widget.BaseAdapter;
import com.bnotya.bnotyaapp.models.INavDrawerItem;

public class NavDrawerActivityConfiguration
{
    private int _mainLayout;
    private int _drawerShadow;
    private int _drawerLayoutId;
    private int _leftDrawerId;
    private int[] _actionMenuItemsToHideWhenDrawerOpen;
    private INavDrawerItem[] _navItems;
    private int _drawerOpenDesc;
    private int _drawerCloseDesc;
    private BaseAdapter _baseAdapter;

    public int getMainLayout()
    {
        return _mainLayout;
    }

    public void setMainLayout(int mainLayout)
    {
        _mainLayout = mainLayout;
    }

    public int getDrawerShadow()
    {
        return _drawerShadow;
    }

    public void setDrawerShadow(int drawerShadow)
    {
        _drawerShadow = drawerShadow;
    }

    public int getDrawerLayoutId()
    {
        return _drawerLayoutId;
    }

    public void setDrawerLayoutId(int drawerLayoutId)
    {
        _drawerLayoutId = drawerLayoutId;
    }

    public int getLeftDrawerId()
    {
        return _leftDrawerId;
    }

    public void setLeftDrawerId(int leftDrawerId)
    {
        _leftDrawerId = leftDrawerId;
    }

    public int[] getActionMenuItemsToHideWhenDrawerOpen()
    {
        return _actionMenuItemsToHideWhenDrawerOpen;
    }

    public void setActionMenuItemsToHideWhenDrawerOpen(
            int[] actionMenuItemsToHideWhenDrawerOpen)
    {
        _actionMenuItemsToHideWhenDrawerOpen = actionMenuItemsToHideWhenDrawerOpen;
    }

    public INavDrawerItem[] getNavItems()
    {
        return _navItems;
    }

    public void setNavItems(INavDrawerItem[] navItems)
    {
        _navItems = navItems;
    }

    public int getDrawerOpenDesc()
    {
        return _drawerOpenDesc;
    }

    public void setDrawerOpenDesc(int drawerOpenDesc)
    {
        _drawerOpenDesc = drawerOpenDesc;
    }

    public int getDrawerCloseDesc()
    {
        return _drawerCloseDesc;
    }

    public void setDrawerCloseDesc(int drawerCloseDesc)
    {
        _drawerCloseDesc = drawerCloseDesc;
    }

    public BaseAdapter getBaseAdapter()
    {
        return _baseAdapter;
    }

    public void setBaseAdapter(BaseAdapter baseAdapter)
    {
        _baseAdapter = baseAdapter;
    }
}
