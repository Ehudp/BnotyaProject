package com.bnotya.bnotyaapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.media.MediaPlayer;
import android.os.*;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;

import android.view.Menu;
import android.view.MenuItem;

import android.view.View;
import android.view.ViewConfiguration;

import com.bnotya.bnotyaapp.adapters.NavDrawerAdapter;
import com.bnotya.bnotyaapp.controls.PageActionProvider;
import com.bnotya.bnotyaapp.fragments.CardManagerFragment;
import com.bnotya.bnotyaapp.fragments.MainDefaultFragment;
import com.bnotya.bnotyaapp.fragments.MainTehilotFragment;
import com.bnotya.bnotyaapp.fragments.TriviaFragment;
import com.bnotya.bnotyaapp.fragments.WomenListFragment;
import com.bnotya.bnotyaapp.helpers.About;
import com.bnotya.bnotyaapp.models.INavDrawerItem;
import com.bnotya.bnotyaapp.models.NavMenuItem;
import com.bnotya.bnotyaapp.models.NavMenuSection;
import com.bnotya.bnotyaapp.services.DataBaseService;

import java.lang.reflect.Field;

public class MainActivity extends AbstractNavDrawerActivity
        implements WomenListFragment.ICommunicator
{
    /* For Menu Overflow in API < 11 */
    private Handler handler = new Handler(Looper.getMainLooper());
    /* Fragments */
    private MainDefaultFragment _mainDefaultFragment;
    private MainTehilotFragment _mainTehilotFragment;
    private WomenListFragment _womenListFragment;

    private boolean _musicPaused;

    public static MediaPlayer music;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // If EXIT is True exit application
        if (getIntent().getBooleanExtra("EXIT", false))
        {
            finish();
            return;
        }

        _musicPaused = false;

        // To run multiple background threads
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        {
            new InitMusicTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            new InitDatabaseTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, this);
        }
        else
        {
            new InitMusicTask().execute();
            new InitDatabaseTask().execute(this);
        }

        if (savedInstanceState == null)
        {
            // If there is no saved instance state, add a fragment to this activity.
            openMainPage();
        }

        initOnBackStackChangedListener();
    }

    @Override
    protected NavDrawerActivityConfiguration getNavDrawerConfiguration()
    {
        INavDrawerItem[] menu = fillNavigationData(R.array.views_array,
                R.array.nav_drawer_icons);

        NavDrawerActivityConfiguration navConf = new NavDrawerActivityConfiguration();
        navConf.setMainLayout(R.layout.activity_main);
        navConf.setDrawerLayoutId(R.id.drawer_layout);
        navConf.setLeftDrawerId(R.id.left_drawer);
        navConf.setNavItems(menu);
        navConf.setDrawerShadow(R.drawable.drawer_shadow);
        navConf.setDrawerOpenDesc(R.string.drawer_open);
        navConf.setDrawerCloseDesc(R.string.drawer_close);
        navConf.setBaseAdapter(
                new NavDrawerAdapter(this, R.layout.navdrawer_item, menu));
        navConf.setActionMenuItemsToHideWhenDrawerOpen(new int[]
                {R.id.action_settings, R.id.action_about});
        return navConf;
    }

    @Override
    protected void onNavItemSelected(int id)
    {
        switch (id)
        {
            case 1:
                openMainPage();
                break;
            case 3:
                openTehilot();
                break;
            case 5:
                openWomenList();
                break;
            case 6:
                openTriviaPage();
                break;
            case 7:
                openInsightList();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        // Set up PageActionProvider's intent
        MenuItem mailItem = menu.findItem(R.id.action_mail);
        PageActionProvider actionProvider = (PageActionProvider)
                MenuItemCompat.getActionProvider(mailItem);
        actionProvider.setIntent(getMailIntent());
        actionProvider.setButtonSource(R.drawable.ic_action_mail);

        return super.onCreateOptionsMenu(menu);
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        // For Menu Overflow in API < 11
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        {
           // TODO: Return this after bug Ehud fix
           // menu.removeItem(R.id.action_overflow);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (_drawerToggle.onOptionsItemSelected(item))
        {
            return true;
        }

        switch (item.getItemId())
        {
            // For Menu Overflow in API < 11
            case R.id.action_overflow:
                openOptionsMenuDeferred();
                return true;
            case R.id.action_settings:
                startActivity(new Intent(this, Preferences.class));
                return true;
            case R.id.action_about:
                About.showAboutDialog(this);
                return true;
            case R.id.action_exit:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDestroy()
    {
        if (music != null)
        {
            if (music.isPlaying())
            {
                music.stop();
            }
            music.release();
            music = null;
        }

        super.onDestroy();
    }

    @Override
    protected void onStop()
    {
        if (music != null && music.isPlaying())
        {
            music.pause();
            music.seekTo(0);
            _musicPaused = true;
        }
        super.onStop();
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        if(_musicPaused)
        {
            startMusic();
            _musicPaused = false;
        }
    }

    private void initOnBackStackChangedListener()
    {
        getSupportFragmentManager().addOnBackStackChangedListener(
                new FragmentManager.OnBackStackChangedListener()
                {
                    @Override
                    public void onBackStackChanged()
                    {
                        int count = getSupportFragmentManager().getBackStackEntryCount();

                        if (count != 0)
                        {
                            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id
                                    .content_frame);
                            if (fragment != null)
                            {
                                updateTitleAndDrawer(fragment);
                            }
                        }
                        else
                        {
                            finish();
                        }
                    }
                }
        );
    }

    private void updateTitleAndDrawer(Fragment fragment)
    {
        if (fragment instanceof MainDefaultFragment)
        {
            setTitle(getResources().getStringArray(R.array.views_array)[0]);
            _drawerList.setItemChecked(1, true);
        }
        else if (fragment instanceof MainTehilotFragment)
        {
            setTitle(getResources().getStringArray(R.array.views_array)[2]);
            _drawerList.setItemChecked(3, true);
        }
        else if (fragment instanceof WomenListFragment)
        {
            setTitle(getResources().getStringArray(R.array.views_array)[4]);
            _drawerList.setItemChecked(5, true);
        }
    }

    // For Menu Overflow in API < 11
    public void openOptionsMenuDeferred()
    {
        handler.post(new Runnable()
        {
            @Override
            public void run()
            {
                openOptionsMenu();
            }
        });
    }

    private Intent getMailIntent()
    {
        String[] address =
                {
                        "a@a.a"
                }; // TODO: Replace with Bnotya's address
        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
        emailIntent.setType("plain/text");
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, address);
        return emailIntent;
    }

    private INavDrawerItem[] fillNavigationData(int titlesID, int iconsID)
    {
        // Load nav item values
        String[] navMenuTitles = getResources().getStringArray(titlesID);
        TypedArray navMenuIcons = getResources().obtainTypedArray(iconsID);
        INavDrawerItem[] result = new INavDrawerItem[navMenuTitles.length];

        // Adding nav drawer items to array
        for (int i = 0; i < navMenuTitles.length; i++)
        {
            int iconID = navMenuIcons.getResourceId(i, -1);
            if (iconID == -1)
            {
                result[i] = NavMenuSection.create(i, navMenuTitles[i]);
            }
            else
            {
                result[i] = NavMenuItem.create(i, navMenuTitles[i], iconID, false);
            }
        }

        // Recycle the typed array
        navMenuIcons.recycle();

        return result;
    }

    public void openWomenList(View view)
    {
        openWomenList();
    }

    public void openTehilot(View view)
    {
        openTehilot();
    }

    public void openTriviaPage(View view)
    {
        openTriviaPage();
    }

    public void openInsightList(View view)
    {
        openInsightList();
    }

    public void openMailPage(View view)
    {
        openMailPage();
    }

    private void replaceFragment(Fragment fragment, int position)
    {
        initFragmentBundle(fragment, position);

        String backStateName = fragment.getClass().getName();

        FragmentManager manager = getSupportFragmentManager();
        boolean fragmentPopped = manager.popBackStackImmediate(backStateName, 0);

        // If fragment is not in back stack, create it.
        if (!fragmentPopped && manager.findFragmentByTag(backStateName) == null)
        {
            manager.beginTransaction()
                    .replace(R.id.content_frame, fragment, backStateName)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .addToBackStack(backStateName).commit();
        }

        // call onPrepareOptionsMenu
        supportInvalidateOptionsMenu();
    }

    private void initFragmentBundle(Fragment fragment, int position)
    {
        Bundle args = fragment.getArguments();

        if (args == null)
        {
            args = new Bundle();
            fragment.setArguments(args);
        }

        if (fragment instanceof CardManagerFragment)
            args.putInt(CardManagerFragment.ARG_VIEW_NUMBER, position);
    }

    @Override
    public void respondToWomenListSelect(int index)
    {
        replaceFragment(new CardManagerFragment(), index);
    }

    private void openTriviaPage()
    {
        Intent intent = new Intent(getBaseContext(), TriviaActivity.class);
        startActivity(intent);
    }

    public void openInsightList()
    {
        Intent intent = new Intent(getBaseContext(), InsightListActivity.class);
        startActivity(intent);
    }

    public void openMailPage()
    {
        Intent emailIntent = getMailIntent();
        startActivity(Intent.createChooser(emailIntent,
                getString(R.string.chooser_title)));
    }

    private void openWomenList()
    {
        if (_womenListFragment == null)
            _womenListFragment = new WomenListFragment();
        _womenListFragment.setCommunicator(this);
        replaceFragment(_womenListFragment, 0);
    }

    private void openTehilot()
    {
        if (_mainTehilotFragment == null)
            _mainTehilotFragment = new MainTehilotFragment();
        replaceFragment(_mainTehilotFragment, 0);
    }

    private void openMainPage()
    {
        if (_mainDefaultFragment == null)
            _mainDefaultFragment = new MainDefaultFragment();
        replaceFragment(_mainDefaultFragment, 0);
    }

	/* Tasks */

    private class InitDatabaseTask extends AsyncTask<Context, Void, Void>
    {
        protected Void doInBackground(Context... contexts)
        {
            DataBaseService.initDatabaseHelper(contexts[0]);
            return null;
        }
    }

    private class InitMusicTask extends AsyncTask<Void, Void, Void>
    {
        protected Void doInBackground(Void... voids)
        {
            initMusic();
            return null;
        }
    }

    private void initMusic()
    {
        music = MediaPlayer.create(this, R.raw.backgroundmusic);
        startMusic();
    }

    private void startMusic()
    {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());
        boolean hasMusic = prefs.getBoolean(
                getString(R.string.music_on_preference), true);
        if (hasMusic)
        {
            music.start();
        }
    }
}
