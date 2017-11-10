package ee.app.conversamanager.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.View;

import ee.app.conversamanager.ConversaApp;
import ee.app.conversamanager.R;
import ee.app.conversamanager.extendables.ConversaActivity;
import ee.app.conversamanager.jobs.SettingsUpdateStatusJob;

/**
 * Created by edgargomez on 1/15/17.
 */

public class ActivitySettingsStatus extends ConversaActivity implements View.OnClickListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private int originalStatus;
    private int currentStatus;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_status);
        initialization();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ConversaApp.getInstance(this)
                .getPreferences()
                .getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void initialization() {
        super.initialization();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle(R.string.preferences__status);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        findViewById(R.id.rlOnline).setOnClickListener(this);
        findViewById(R.id.rlAway).setOnClickListener(this);
        findViewById(R.id.rlOffline).setOnClickListener(this);
        findViewById(R.id.rlUpdateStatus).setOnClickListener(this);

        originalStatus = ConversaApp.getInstance(this).getPreferences().getAccountStatus();
        currentStatus = originalStatus;

        switch (originalStatus) {
            case 0:
                findViewById(R.id.ivOnlineCheck).setVisibility(View.VISIBLE);
                break;
            case 1:
                findViewById(R.id.ivAwayCheck).setVisibility(View.VISIBLE);
                break;
            case 2:
                findViewById(R.id.ivOfflineCheck).setVisibility(View.VISIBLE);
                break;
            default:
                finish();
                break;
        }

        ConversaApp.getInstance(this)
                .getPreferences()
                .getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rlOnline:
                findViewById(R.id.ivOnlineCheck).setVisibility(View.VISIBLE);
                findViewById(R.id.ivAwayCheck).setVisibility(View.GONE);
                findViewById(R.id.ivOfflineCheck).setVisibility(View.GONE);
                currentStatus = 0;
                break;
            case R.id.rlAway:
                findViewById(R.id.ivOnlineCheck).setVisibility(View.GONE);
                findViewById(R.id.ivAwayCheck).setVisibility(View.VISIBLE);
                findViewById(R.id.ivOfflineCheck).setVisibility(View.GONE);
                currentStatus = 1;
                break;
            case R.id.rlOffline:
                findViewById(R.id.ivOnlineCheck).setVisibility(View.GONE);
                findViewById(R.id.ivAwayCheck).setVisibility(View.GONE);
                findViewById(R.id.ivOfflineCheck).setVisibility(View.VISIBLE);
                currentStatus = 2;
                break;
            default:
                ConversaApp.getInstance(getApplicationContext())
                        .getJobManager()
                        .addJobInBackground(new SettingsUpdateStatusJob(
                                        ConversaApp.getInstance(getApplicationContext())
                                                .getPreferences()
                                                .getAccountBusinessId(),
                                originalStatus,
                                currentStatus
                            )
                        );
                originalStatus = currentStatus;
                break;
        }

        updateViews(currentStatus, originalStatus);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(PreferencesKeys.ACCOUNT_STATUS_KEY)) {
            originalStatus = ConversaApp.getInstance(this).getPreferences().getAccountStatus();
            updateViews(currentStatus, originalStatus);
        }
    }

    private void updateViews(int currentStatus, int originalStatus) {
        switch (currentStatus) {
            case 0:
                findViewById(R.id.ivOnlineCheck).setVisibility(View.VISIBLE);
                findViewById(R.id.ivAwayCheck).setVisibility(View.GONE);
                findViewById(R.id.ivOfflineCheck).setVisibility(View.GONE);
                break;
            case 1:
                findViewById(R.id.ivOnlineCheck).setVisibility(View.GONE);
                findViewById(R.id.ivAwayCheck).setVisibility(View.VISIBLE);
                findViewById(R.id.ivOfflineCheck).setVisibility(View.GONE);
                break;
            case 2:
                findViewById(R.id.ivOnlineCheck).setVisibility(View.GONE);
                findViewById(R.id.ivAwayCheck).setVisibility(View.GONE);
                findViewById(R.id.ivOfflineCheck).setVisibility(View.VISIBLE);
                break;
            default:
                finish();
                break;
        }

        if (currentStatus == originalStatus) {
            findViewById(R.id.rlUpdateStatus).setVisibility(View.GONE);
        } else {
            findViewById(R.id.rlUpdateStatus).setVisibility(View.VISIBLE);
        }
    }
}