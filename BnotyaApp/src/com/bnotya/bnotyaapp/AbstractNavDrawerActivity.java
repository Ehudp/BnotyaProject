package com.bnotya.bnotyaapp;

import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.support.v7.app.ActionBarActivity;

import com.bnotya.bnotyaapp.models.INavDrawerItem;

public abstract class AbstractNavDrawerActivity extends ActionBarActivity
{
    private DrawerLayout _drawerLayout;
    protected ActionBarDrawerToggle _drawerToggle;
    protected ListView _drawerList;
    private CharSequence _drawerTitle;
    private CharSequence _title;
    protected NavDrawerActivityConfiguration _navConf;

    protected abstract NavDrawerActivityConfiguration getNavDrawerConfiguration();

    protected abstract void onNavItemSelected( int id );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _navConf = getNavDrawerConfiguration();

        setContentView(_navConf.getMainLayout());

        _title = _drawerTitle = getTitle();

        _drawerLayout = (DrawerLayout) findViewById(_navConf.getDrawerLayoutId());
        _drawerList = (ListView) findViewById(_navConf.getLeftDrawerId());
        _drawerList.setAdapter(_navConf.getBaseAdapter());
        _drawerList.setOnItemClickListener(new DrawerItemClickListener());

        this.initDrawerShadow();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setHomeButtonEnabled(true);
        }
        else
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        _drawerToggle = new ActionBarDrawerToggle(
                this,
                _drawerLayout,
                getDrawerIcon(),
                _navConf.getDrawerOpenDesc(),
                _navConf.getDrawerCloseDesc()
        ) {
            public void onDrawerClosed(View view)
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                {
                    getActionBar().setTitle(_title);
                    invalidateOptionsMenu();
                }
                else
                {
                    getSupportActionBar().setTitle(_title);
                    supportInvalidateOptionsMenu();
                }
            }

            public void onDrawerOpened(View drawerView)
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                {
                    getActionBar().setTitle(_drawerTitle);
                    invalidateOptionsMenu();
                }
                else
                {
                    getSupportActionBar().setTitle(_drawerTitle);
                    supportInvalidateOptionsMenu();
                }
            }
        };
        _drawerLayout.setDrawerListener(_drawerToggle);
    }

    protected void initDrawerShadow() {
        _drawerLayout.setDrawerShadow(_navConf.getDrawerShadow(), GravityCompat.START);
    }

    protected int getDrawerIcon() {
        return R.drawable.ic_drawer;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        _drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        _drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        if ( _navConf.getActionMenuItemsToHideWhenDrawerOpen() != null )
        {
            boolean drawerOpen = _drawerLayout.isDrawerOpen(_drawerList);
            for( int iItem : _navConf.getActionMenuItemsToHideWhenDrawerOpen())
            {
                menu.findItem(iItem).setVisible(!drawerOpen);
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        return _drawerToggle.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ( keyCode == KeyEvent.KEYCODE_MENU ) {
            if ( this._drawerLayout.isDrawerOpen(this._drawerList)) {
                this._drawerLayout.closeDrawer(this._drawerList);
            }
            else {
                this._drawerLayout.openDrawer(this._drawerList);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    protected DrawerLayout getDrawerLayout() {
        return _drawerLayout;
    }

    protected ActionBarDrawerToggle getDrawerToggle() {
        return _drawerToggle;
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    public void selectItem(int position) {
        INavDrawerItem selectedItem = _navConf.getNavItems()[position];

        this.onNavItemSelected(selectedItem.getId());
        _drawerList.setItemChecked(position, true);

        if ( selectedItem.updateActionBarTitle()) {
            setTitle(selectedItem.getLabel());
        }

        if ( this._drawerLayout.isDrawerOpen(this._drawerList)) {
            _drawerLayout.closeDrawer(_drawerList);
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        _title = title;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        {
            getActionBar().setTitle(_title);
        }
        else
        {
            getSupportActionBar().setTitle(_title);
        }
    }
}
