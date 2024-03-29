package ee.app.conversamanager.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.facebook.drawee.view.SimpleDraweeView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import ee.app.conversamanager.ConversaApp;
import ee.app.conversamanager.R;
import ee.app.conversamanager.extendables.ConversaActivity;
import ee.app.conversamanager.jobs.SettingsRedirectJob;
import ee.app.conversamanager.utils.AppActions;
import ee.app.conversamanager.utils.Utils;
import ee.app.conversamanager.view.LightTextView;
import ee.app.conversamanager.view.RegularTextView;

/**
 * Created by edgargomez on 9/9/16.
 */
public class ActivitySettingsAccount extends ConversaActivity implements View.OnClickListener,
        SwitchCompat.OnCheckedChangeListener, SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_account);
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

        getSupportActionBar().setTitle(R.string.preferences__account);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Uri uri = Utils.getUriFromString(
                ConversaApp.getInstance(this).getPreferences().getAccountAvatar()
        );

        if (uri == null) {
            uri = Utils.getDefaultImage(this, R.drawable.ic_business_default);
        }

        ((SimpleDraweeView)findViewById(R.id.sdvAvatar)).setImageURI(uri);

        SwitchCompat mScRedirectConversa = (SwitchCompat) findViewById(R.id.scRedirect);
        mScRedirectConversa.setChecked(false);

        mScRedirectConversa.setOnCheckedChangeListener(this);

        RegularTextView mLtvEmail = (RegularTextView) findViewById(R.id.rtvStatus);

        switch (ConversaApp.getInstance(this).getPreferences().getAccountStatus()) {
            case 0:
                mLtvEmail.setText(getString(R.string.profile_status_online));
                break;
            case 1:
                mLtvEmail.setText(getString(R.string.profile_status_away));
                break;
            case 2:
                mLtvEmail.setText(getString(R.string.profile_status_offline));
                break;
            default:
                mLtvEmail.setText(getString(R.string.profile_status_conversa));
                break;
        }

        ((RegularTextView)findViewById(R.id.rtvDisplayName)).setText(
                ConversaApp.getInstance(this).getPreferences().getAccountDisplayName()
        );

        ((LightTextView)findViewById(R.id.ltvConversaId)).setText(
                "@" + ConversaApp.getInstance(this).getPreferences().getAccountConversaId()
        );

        findViewById(R.id.rlProfile).setOnClickListener(this);
        findViewById(R.id.rlStatus).setOnClickListener(this);
        findViewById(R.id.rlLogOut).setOnClickListener(this);

        ConversaApp.getInstance(this)
                .getPreferences()
                .getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rlProfile: {
                Intent intent = new Intent(this, ActivitySettingsDetailAccount.class);
                startActivity(intent);
                break;
            }
            case R.id.rlStatus: {
                Intent intent = new Intent(this, ActivitySettingsStatus.class);
                startActivity(intent);
                break;
            }
            case R.id.rlLogOut: {
                new MaterialDialog.Builder(this)
                        .content(getString(R.string.logout_message))
                        .positiveText(getString(R.string.logout_ok))
                        .negativeText(getString(android.R.string.no))
                        .positiveColorRes(R.color.red)
                        .negativeColorRes(R.color.black)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                                AppActions.appLogout(getApplicationContext(), false);
                            }
                        })
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
                break;
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
        if (isChecked) {
            // Enable redirect
            new MaterialDialog.Builder(this)
                    .content(getString(R.string.sett_account_redirect_content))
                    .positiveColorRes(R.color.purple)
                    .positiveText(android.R.string.ok)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            ((SwitchCompat) findViewById(R.id.scRedirect)).setChecked(false);
                            dialog.dismiss();
                        }
                    })
                    .show();
        } else {
            // Disable redirect
            if (ConversaApp.getInstance(getApplicationContext())
                    .getPreferences()
                    .getAccountRedirect()) {
                ConversaApp.getInstance(getApplicationContext())
                        .getJobManager()
                        .addJobInBackground(new SettingsRedirectJob(
                                        ConversaApp.getInstance(getApplicationContext())
                                                .getPreferences()
                                                .getAccountBusinessId(),
                                        false
                                )
                        );
            }
        }
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(PreferencesKeys.ACCOUNT_STATUS_KEY)) {
            switch (ConversaApp.getInstance(this).getPreferences().getAccountStatus()) {
                case 0:
                    ((RegularTextView) findViewById(R.id.rtvStatus))
                            .setText(getString(R.string.profile_status_online));
                    break;
                case 1:
                    ((RegularTextView) findViewById(R.id.rtvStatus))
                            .setText(getString(R.string.profile_status_away));
                    break;
                case 2:
                    ((RegularTextView) findViewById(R.id.rtvStatus))
                            .setText(getString(R.string.profile_status_offline));
                    break;
                default:
                    ((RegularTextView) findViewById(R.id.rtvStatus))
                            .setText(getString(R.string.profile_status_conversa));
                    break;
            }
        } else if (key.equals(PreferencesKeys.ACCOUNT_CONVERSA_ID_KEY)) {
            ((LightTextView)findViewById(R.id.ltvConversaId)).setText(
                    ConversaApp.getInstance(this).getPreferences().getAccountDisplayName()
            );
        } else if (key.equals(PreferencesKeys.ACCOUNT_DISPLAY_NAME_KEY)) {
            ((RegularTextView)findViewById(R.id.rtvDisplayName)).setText(
                    ConversaApp.getInstance(this).getPreferences().getAccountDisplayName()
            );
        } else if (key.equals(PreferencesKeys.ACCOUNT_AVATAR_KEY)) {
            Uri uri = Utils.getUriFromString(
                    ConversaApp.getInstance(this).getPreferences().getAccountAvatar()
            );

            if (uri == null) {
                uri = Utils.getDefaultImage(this, R.drawable.ic_business_default);
            }

            ((SimpleDraweeView)findViewById(R.id.sdvAvatar)).setImageURI(uri);
        }
    }
}