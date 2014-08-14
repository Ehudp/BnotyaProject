package com.bnotya.bnotyaapp.models;

public class NavMenuItem implements INavDrawerItem
{
    public static final int ITEM_TYPE = 1;

    private int _id;
    private String _label;
    private int _icon;
    private boolean _updateActionBarTitle;

    private NavMenuItem() {
    }

    public static NavMenuItem create( int id, String label, int icon, boolean updateActionBarTitle)
    {
        NavMenuItem item = new NavMenuItem();
        item.setId(id);
        item.setLabel(label);
        item.setIcon(icon);
        item.setUpdateActionBarTitle(updateActionBarTitle);
        return item;
    }

    @Override
    public int getType() {
        return ITEM_TYPE;
    }

    public int getId() {
        return _id;
    }

    public void setId(int id) {
        _id = id;
    }

    public String getLabel() {
        return _label;
    }

    public void setLabel(String label) {
        _label = label;
    }

    public int getIcon() {
        return _icon;
    }

    public void setIcon(int icon) {
        _icon = icon;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean updateActionBarTitle() {
        return _updateActionBarTitle;
    }

    public void setUpdateActionBarTitle(boolean updateActionBarTitle) {
        _updateActionBarTitle = updateActionBarTitle;
    }
}
