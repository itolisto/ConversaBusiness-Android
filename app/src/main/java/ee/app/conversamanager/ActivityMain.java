package ee.app.conversamanager;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;

import com.crashlytics.android.Crashlytics;
import com.parse.ParseCloud;
import com.parse.ParseException;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import ee.app.conversamanager.extendables.ConversaActivity;
import ee.app.conversamanager.jobs.BusinessInfoJob;
import ee.app.conversamanager.management.AblyConnection;
import ee.app.conversamanager.model.parse.Account;
import ee.app.conversamanager.utils.AppActions;
import ee.app.conversamanager.utils.Foreground;
import ee.app.conversamanager.utils.Logger;
import ee.app.conversamanager.utils.PagerAdapter;
import ee.app.conversamanager.view.MediumTextView;
import io.fabric.sdk.android.Fabric;

public class ActivityMain extends ConversaActivity implements Foreground.Listener {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private final String TAG = ActivityMain.class.getSimpleName();
    private ViewPager mViewPager;
    private Timer timer;
    private boolean resetNotifications;

    private ImageView mIvConversa;
    private MediumTextView mRtvTitle;

    private final int[] tabIcons = {
            R.drawable.tab_chat_inactive,
            R.drawable.tab_trending_inactive,
            R.drawable.tab_store_inactive
    };

    private final int[] tabSelectedIcons = {
            R.drawable.tab_chat_active,
            R.drawable.tab_trending_active,
            R.drawable.tab_store_active
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Fabric.with(this, new Crashlytics());
        AblyConnection.getInstance().initAbly();

        // Remove internet connection check
        checkInternetConnection = false;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        mIvConversa = (ImageView) toolbar.findViewById(R.id.ivConversa);
        mRtvTitle = (MediumTextView) toolbar.findViewById(R.id.rtvTitle);
        setSupportActionBar(toolbar);

        mViewPager = (ViewPager) findViewById(R.id.pager);
        final PagerAdapter mPagerAdapter = new PagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOffscreenPageLimit(2);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.getTabAt(0).setIcon(tabSelectedIcons[0]);
        tabLayout.getTabAt(1).setIcon(tabIcons[1]);
        tabLayout.getTabAt(2).setIcon(tabIcons[2]);

        resetNotifications = true;

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                final int p = tab.getPosition();

                if (p == 2) {
                    getSupportActionBar().hide();
                }

                mViewPager.setCurrentItem(p);
                supportInvalidateOptionsMenu();
                tab.setIcon(tabSelectedIcons[p]);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                int position = tab.getPosition();

                if (position == 0) {
                    if (mPagerAdapter.getRegisteredFragment(0) != null)
                        ((FragmentUsersChat)mPagerAdapter.getRegisteredFragment(0)).finishActionMode();
                } else if (position == 2) {
                    getSupportActionBar().show();
                }

                tab.setIcon(tabIcons[position]);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });

        if (ConversaApp.getInstance(this).getPreferences().getAccountBusinessId().isEmpty()) {
            // 1. Get Customer Id
            ConversaApp.getInstance(this)
                    .getJobManager()
                    .addJobInBackground(new BusinessInfoJob(Account.getCurrentUser().getObjectId()));
        } else {
            AblyConnection.getInstance().subscribeToChannels();
        }

        initialization();
	}

    @Override
    protected void initialization() {
        super.initialization();
        startTimer();
        Foreground.get(this).addListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Foreground.get(this).removeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Clear notifications
        if (resetNotifications) {
            resetNotifications = false;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Logger.error(TAG, "Resetting counts and clear all notifications");
                    ConversaApp.getInstance(getApplicationContext())
                            .getDB()
                            .resetAllCounts();
                    NotificationManager notificationManager =
                            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.cancelAll();
                }
            }).start();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        int p = mViewPager.getCurrentItem();
        switch (p) {
            case 0: {
                mIvConversa.setVisibility(View.VISIBLE);
                mRtvTitle.setVisibility(View.GONE);
                break;
            }
            case 1: {
                mIvConversa.setVisibility(View.GONE);
                mRtvTitle.setVisibility(View.VISIBLE);
                mRtvTitle.setText(getString(R.string.stats));
                break;
            }
        }

        return true;
    }

    public void startTimer() {
        if (timer != null) {
            return;
        }

        // Set a new Timer
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                String id = ConversaApp
                        .getInstance(getApplicationContext())
                        .getPreferences()
                        .getAccountBusinessId();

                if (!id.isEmpty()) {
                    HashMap<String, String> params = new HashMap<>(1);
                    params.put("businessId", id);

                    try {
                        ParseCloud.callFunction("updateBusinessLastConnection", params);
                    } catch (ParseException e) {
                        if (AppActions.validateParseException(e)) {
                            AppActions.appLogout(getApplicationContext(), true);
                        } else {
                            Logger.error("ActivityMain", "Status update error: " + e.getMessage());
                        }
                    }
                }
            }
        }, 0, 210000);
        // 210,000 = milliseconds passed in 3.5 minutes
    }

    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    public void onBecameForeground() {
        startTimer();
    }

    @Override
    public void onBecameBackground() {
        stoptimertask();
    }

}