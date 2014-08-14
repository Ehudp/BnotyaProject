package com.bnotya.bnotyaapp.models;


public class NavMenuSection implements INavDrawerItem
{
    public static final int SECTION_TYPE = 0;
    private int _id;
    private String _label;

    private NavMenuSection() {
    }

    public static NavMenuSection create( int id, String label ) {
        NavMenuSection section = new NavMenuSection();
        section.setLabel(label);
        return section;
    }

    @Override
    public int getType() {
        return SECTION_TYPE;
    }

    public String getLabel() {
        return _label;
    }

    public void setLabel(String label) {
        _label = label;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    public int getId() {
        return _id;
    }

    public void setId(int id) {
        _id = id;
    }

    @Override
    public boolean updateActionBarTitle() {
        return false;
    }
}
