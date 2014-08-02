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
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;

import android.view.Menu;
import android.view.MenuItem;

import android.view.View;
import com.bnotya.bnotyaapp.adapters.NavDrawerAdapter;
import com.bnotya.bnotyaapp.controls.PageActionProvider;
import com.bnotya.bnotyaapp.fragments.MainDefaultFragment;
import com.bnotya.bnotyaapp.fragments.MainTehilotFragment;
import com.bnotya.bnotyaapp.fragments.WomenListFragment;
import com.bnotya.bnotyaapp.helpers.About;
import com.bnotya.bnotyaapp.models.INavDrawerItem;
import com.bnotya.bnotyaapp.models.NavMenuItem;
import com.bnotya.bnotyaapp.models.NavMenuSection;
import com.bnotya.bnotyaapp.services.DataBaseService;

public class MainActivity extends AbstractNavDrawerActivity
{
    /* For Menu Overflow in API < 11 */
    private Handler handler = new Handler(Looper.getMainLooper());

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
            replaceFragment(new MainDefaultFragment(), 0);
        }
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
                replaceFragment(new MainDefaultFragment(), 1);
                break;
            case 3:
                replaceFragment(new MainTehilotFragment(), 1);
                break;
            case 5:
                replaceFragment(new WomenListFragment(), 0);
                break;
            case 6:
                openTriviaPage(null);
                break;
            case 7:
                openInsightList(null);
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

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        // For Menu Overflow in API < 11
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        {
            menu.removeItem(R.id.action_overflow);
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
        new OpenWomenListTask().execute();
    }

    private void openWomenList()
    {
        replaceFragment(new WomenListFragment(), 0);
    }

    public void openTehilot(View view)
    {
        new OpenTehilotTask().execute();
    }

    private void openTehilot()
    {
        replaceFragment(new MainTehilotFragment(), 1);
    }

    public void openTriviaPage(View view)
    {
        new OpenTriviaPageTask().execute();
    }

    private void openTriviaPage()
    {
        Intent intent = new Intent(getBaseContext(), TriviaActivity.class);
        startActivity(intent);
    }

    public void openInsightList(View view)
    {
        new OpenInsightListTask().execute();
    }

    public void openInsightList()
    {
        Intent intent = new Intent(getBaseContext(), InsightListActivity.class);
        startActivity(intent);
    }

    public void openMailPage(View view)
    {
        new OpenMailPageTask().execute();
    }

    public void openMailPage()
    {
        Intent emailIntent = getMailIntent();
        startActivity(Intent.createChooser(emailIntent,
                getString(R.string.chooser_title)));
    }

    public void replaceFragment(Fragment fragment, int position)
    {
        Bundle args = new Bundle();

        args.putInt(MainDefaultFragment.ARG_VIEW_NUMBER, position);
        fragment.setArguments(args);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(null).commit();

        // call onPrepareOptionsMenu
        supportInvalidateOptionsMenu();
    }

    private void initMusic()
    {
        music = MediaPlayer.create(this, R.raw.backgroundmusic);
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());
        boolean hasMusic = prefs.getBoolean(
                getString(R.string.music_on_preference), true);
        if (hasMusic)
        {
            music.setOnPreparedListener(new MediaPlayer.OnPreparedListener()
            {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer)
                {
                    if (music == mediaPlayer)
                    {
                        music.start();
                    }
                }
            });
        }
        //music.start();
        else
            music.release();
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

    private class OpenTriviaPageTask extends AsyncTask<Void, Void, Void>
    {
        protected Void doInBackground(Void... voids)
        {
            openTriviaPage();
            return null;
        }
    }

    private class OpenInsightListTask extends AsyncTask<Void, Void, Void>
    {
        protected Void doInBackground(Void... voids)
        {
            openInsightList();
            return null;
        }
    }

    private class OpenMailPageTask extends AsyncTask<Void, Void, Void>
    {
        protected Void doInBackground(Void... voids)
        {
            openMailPage();
            return null;
        }
    }

    private class OpenWomenListTask extends AsyncTask<Void, Void, Void>
    {
        protected Void doInBackground(Void... voids)
        {
            openWomenList();
            return null;
        }
    }

    private class OpenTehilotTask extends AsyncTask<Void, Void, Void>
    {
        protected Void doInBackground(Void... voids)
        {
            openTehilot();
            return null;
        }
    }
}
