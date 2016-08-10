package ee.app.conversabusiness;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import ee.app.conversabusiness.extendables.ConversaActivity;
import ee.app.conversabusiness.notifications.RegistrationIntentService;
import ee.app.conversabusiness.utils.Logger;
import ee.app.conversabusiness.utils.PagerAdapter;

public class ActivityMain extends ConversaActivity {

    private ViewPager mViewPager;
    private TabLayout tabLayout;
    private String titles[];
    private int[] tabIcons = {
            R.drawable.chats,
            R.drawable.basket,
            R.drawable.settings
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* QUITAR CON EMULADOR DE ECLIPSE*/
        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        } else {
            Logger.error(TAG_GCM, "No valid Google Play Services APK found.");
        }

        // Remove internet connection check
        checkInternetConnection = false;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mViewPager = (ViewPager) findViewById(R.id.pager);
        titles = getResources().getStringArray(R.array.categories_titles);
        PagerAdapter mPagerAdapter = new PagerAdapter(getSupportFragmentManager());
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

        tabLayout.getTabAt(0).setIcon(tabIcons[0]);
        tabLayout.getTabAt(1).setIcon(tabIcons[1]);
        tabLayout.getTabAt(2).setIcon(tabIcons[2]);

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
                int p = tab.getPosition();

                try {
                    getSupportActionBar().setTitle(titles[p]);
                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                    supportInvalidateOptionsMenu();
                } catch (NullPointerException e) {
                    Logger.error(this.toString(), e.getMessage());
                }

                switch (p) {
                    case 2:
                        tab.setIcon(R.drawable.settings_active);

                        if (Build.VERSION.SDK_INT >= 23) {
                            getSupportActionBar().setBackgroundDrawable(
                                    new ColorDrawable(getResources().getColor(R.color.settings_tab, null)));
                            tabLayout.setBackground(new ColorDrawable(getResources().getColor(R.color.settings_tab, null)));
                            mViewPager.setBackground(new ColorDrawable(getResources().getColor(R.color.settings_background, null)));
                        } else {
                            getSupportActionBar().setBackgroundDrawable(
                                    new ColorDrawable(getResources().getColor(R.color.settings_tab)));
                            tabLayout.setBackgroundColor(getResources().getColor(R.color.settings_tab));
                            mViewPager.setBackgroundColor(getResources().getColor(R.color.settings_background));
                        }
                        break;
                    default:
                        if (p == 1) {
                            tab.setIcon(R.drawable.actuales_active);
                        } else {
                            tab.setIcon(R.drawable.chats_active);
                        }

                        if (Build.VERSION.SDK_INT >= 23) {
                            getSupportActionBar().setBackgroundDrawable(
                                    new ColorDrawable(getResources().getColor(R.color.regular_tabs, null)));
                            tabLayout.setBackground(new ColorDrawable(getResources().getColor(R.color.regular_tabs, null)));
                            mViewPager.setBackground(new ColorDrawable(getResources().getColor(R.color.normal_background, null)));
                        } else {
                            getSupportActionBar().setBackgroundDrawable(
                                    new ColorDrawable(getResources().getColor(R.color.regular_tabs))
                            );
                            tabLayout.setBackgroundColor(getResources().getColor(R.color.regular_tabs));
                            mViewPager.setBackgroundColor(getResources().getColor(R.color.normal_background));
                        }
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                switch (position) {
                    case 0:
                        tab.setIcon(R.drawable.chats_inactive);
                        break;
                    case 1:
                        tab.setIcon(R.drawable.actuales_inactive);
                        break;
                    case 2:
                        if (Build.VERSION.SDK_INT >= 23) {
                            tabLayout.setBackground(new ColorDrawable(getResources().getColor(R.color.regular_tabs, null)));
                            mViewPager.setBackground(new ColorDrawable(getResources().getColor(R.color.normal_background, null)));
                        } else {
                            tabLayout.setBackgroundColor(getResources().getColor(R.color.regular_tabs));
                            mViewPager.setBackgroundColor(getResources().getColor(R.color.normal_background));
                        }
                        tab.setIcon(R.drawable.settings_inactive);

                        break;
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });

        super.initialization();
	}

    @Override
    protected void openFromNotification(Bundle extras) {

    }

    /*********************************************************************************************/
    /***********************************GOOGLE CLOUD MESSAGING************************************/
    /********************************************* GCM *******************************************/
    /*********************************************************************************************/
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG_GCM = "GCM Conversa";

    /**
     * Revisa el dispositivo para asegurarse que tiene la APK de Google Play Services.
     * Si no lo tiene, despliega un dialogo que permite al usuario descargar la APK
     * desde la Google Play Store o activarlo en los ajustes del sistema del dispositivo.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Logger.error(TAG_GCM, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

}
