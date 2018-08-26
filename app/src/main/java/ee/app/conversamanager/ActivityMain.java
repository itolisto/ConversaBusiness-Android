package ee.app.conversamanager;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import ee.app.conversamanager.extendables.ConversaActivity;
import ee.app.conversamanager.jobs.BusinessInfoJob;
import ee.app.conversamanager.management.AblyConnection;
import ee.app.conversamanager.networking.FirebaseCustomException;
import ee.app.conversamanager.networking.NetworkingManager;
import ee.app.conversamanager.utils.AppActions;
import ee.app.conversamanager.utils.Foreground;
import ee.app.conversamanager.utils.Logger;
import ee.app.conversamanager.utils.PagerAdapter;
import ee.app.conversamanager.view.MediumTextView;
import io.fabric.sdk.android.Fabric;

public class ActivityMain extends ConversaActivity implements Foreground.Listener {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private final String TAG = ActivityMain.class.getSimpleName();
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
    private ViewPager mViewPager;
    private Timer timer;
    private boolean resetNotifications;
    private ImageView mIvConversa;
    private MediumTextView mRtvTitle;

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

        // Load new token for current session
        if (ConversaApp.getInstance(this).getPreferences().getFirebaseLoadToken()) {
            FirebaseUser current = FirebaseAuth.getInstance().getCurrentUser();

            if (current != null) {
                current.getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                    @Override
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        // TODO: Should evaluate the necessity of refreshing token after certain time and not onlly after Splash Screen is launched
                        if (task.isSuccessful()) {
                            ConversaApp.getInstance(getApplicationContext()).getPreferences().setFirebaseToken(task.getResult().getToken());
                        } else {
                            Logger.error(TAG, "Shouldn't reach this piece of code. Probably logout user automatically");
                        }

                        ConversaApp.getInstance(getApplicationContext()).getPreferences().setFirebaseLoadToken(false);
                    }
                });
            }
        }

        AblyConnection.getInstance().subscribeToChannels();

        initialization();
	}

    @Override
    protected void initialization() {
        super.initialization();
        startTimer();
        Foreground.get(this).addListener(this);

        FirebaseUser current = FirebaseAuth.getInstance().getCurrentUser();

        if (current != null) {
            current.getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                @Override
                public void onComplete(@NonNull Task<GetTokenResult> task) {
                    ConversaApp.getInstance(getApplicationContext()).getPreferences().setFirebaseToken(task.getResult().getToken());
                }
            });
        }
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
                        NetworkingManager.getInstance().postSync(getApplicationContext(),"business/updateBusinessLastConnection", params);
                    } catch (FirebaseCustomException e) {
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