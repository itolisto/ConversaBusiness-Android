package ee.app.conversabusiness;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;

import com.onesignal.OneSignal;
import com.parse.ParseCloud;
import com.parse.ParseException;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import ee.app.conversabusiness.extendables.ConversaActivity;
import ee.app.conversabusiness.jobs.BusinessInfoJob;
import ee.app.conversabusiness.management.AblyConnection;
import ee.app.conversabusiness.model.parse.Account;
import ee.app.conversabusiness.utils.AppActions;
import ee.app.conversabusiness.utils.Foreground;
import ee.app.conversabusiness.utils.Logger;
import ee.app.conversabusiness.utils.PagerAdapter;
import ee.app.conversabusiness.utils.Utils;

public class ActivityMain extends ConversaActivity implements Foreground.Listener {

    private final String TAG = ActivityMain.class.getSimpleName();
    private ViewPager mViewPager;
    private TabLayout tabLayout;
    private String titles[];
    private Timer timer;
    private boolean resetNotifications;

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

        AblyConnection.getInstance().initAbly();

        // Remove internet connection check
        checkInternetConnection = false;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mViewPager = (ViewPager) findViewById(R.id.pager);
        titles = getResources().getStringArray(R.array.categories_titles);
        final PagerAdapter mPagerAdapter = new PagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOffscreenPageLimit(2);

        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        if (tabLayout != null) {
            tabLayout.setupWithViewPager(mViewPager);
        }

        // Initial state of tabs and titles
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(titles[0]);
        }

        resetNotifications = true;

        tabLayout.getTabAt(0).setIcon(tabSelectedIcons[0]);
        tabLayout.getTabAt(1).setIcon(tabIcons[1]);
        tabLayout.getTabAt(2).setIcon(tabIcons[2]);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int p = tab.getPosition();
                mViewPager.setCurrentItem(p);
                tab.setIcon(tabSelectedIcons[p]);

                getSupportActionBar().setTitle(titles[p]);
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                supportInvalidateOptionsMenu();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                int position = tab.getPosition();

                if (position == 0) {
                    if (mPagerAdapter.getRegisteredFragment(0) != null) {
                        ((FragmentUsersChat)mPagerAdapter.getRegisteredFragment(0)).finishActionMode();
                    }
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
            OneSignal.getTags(new OneSignal.GetTagsHandler() {
                @Override
                public void tagsAvailable(JSONObject tags) {
                    if (tags == null || tags.length() == 0) {
                        OneSignal.setSubscription(true);
                        Utils.subscribeToTags(ConversaApp.getInstance(getApplicationContext())
                                .getPreferences().getAccountBusinessId());
                    }
                }
            });
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

    public void selectViewPagerTab(int tab) {
        if (tab > 2 || tab < 0) {
            return;
        }

        mViewPager.setCurrentItem(tab);
    }

    public void startTimer() {
        if (timer != null) {
            return;
        }

        // Set a new Timer
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                Logger.error("ActivityMain", "Status update begin");
                HashMap<String, Object> params = new HashMap<>(5);
                params.put("bId", ConversaApp
                            .getInstance(getApplicationContext())
                            .getPreferences()
                            .getAccountBusinessId()
                );

                params.put("status", (Foreground.get().isBackground()) ? Integer.valueOf(1) : Integer.valueOf(0));

                try {
                    ParseCloud.callFunction("updateStatus", params);
                } catch (ParseException e) {
                    AppActions.validateParseException(getApplicationContext(), e);
                    Logger.error("ActivityMain", "Status update error: " + e.getMessage());
                }
            }
        }, 0, 60000);
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
        AblyConnection.getInstance().connectAbly();
    }

    @Override
    public void onBecameBackground() {
        stoptimertask();
        AblyConnection.getInstance().disconnectAbly();
    }

}