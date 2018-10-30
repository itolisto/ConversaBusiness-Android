package ee.app.conversamanager.settings;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.facebook.drawee.view.SimpleDraweeView;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.SaveCallback;

import java.io.File;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import ee.app.conversamanager.ConversaApp;
import ee.app.conversamanager.R;
import ee.app.conversamanager.extendables.ConversaActivity;
import ee.app.conversamanager.model.parse.Account;
import ee.app.conversamanager.utils.Const;
import ee.app.conversamanager.utils.ImageFilePath;
import ee.app.conversamanager.utils.Logger;
import ee.app.conversamanager.utils.Utils;
import ee.app.conversamanager.view.AvatarSheetDialog;
import ee.app.conversamanager.view.LightTextView;

/**
 * Created by edgargomez on 9/9/16.
 */
public class ActivitySettingsDetailAccount extends ConversaActivity implements View.OnClickListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private AvatarSheetDialog myBottomSheet;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_detail_account);
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

        getSupportActionBar().setTitle(R.string.preferences__profile);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        myBottomSheet = AvatarSheetDialog.newInstance(this, false);

        Uri uri = Utils.getUriFromString(
                ConversaApp.getInstance(this).getPreferences().getAccountAvatar()
        );

        if (uri == null) {
            uri = Utils.getDefaultImage(this, R.drawable.ic_business_default);
        }

        ((SimpleDraweeView)findViewById(R.id.sdvAvatar)).setImageURI(uri);

        findViewById(R.id.llDisplayName).setOnClickListener(this);
        findViewById(R.id.llConversaId).setOnClickListener(this);
        findViewById(R.id.llPassword).setOnClickListener(this);
        findViewById(R.id.rlCategories).setOnClickListener(this);
        findViewById(R.id.btnEdit).setOnClickListener(this);

        ((LightTextView)findViewById(R.id.ltvDisplayName)).setText(
                ConversaApp.getInstance(this).getPreferences().getAccountDisplayName()
        );
        ((LightTextView)findViewById(R.id.ltvConversaId)).setText(
                ConversaApp.getInstance(this).getPreferences().getAccountConversaId()
        );
        ((LightTextView)findViewById(R.id.ltvEmail)).setText(
                Account.getCurrentUser().getEmail()
        );


        ConversaApp.getInstance(this)
                .getPreferences()
                .getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.llDisplayName: {
                new MaterialDialog.Builder(this)
                        .title(R.string.sett_account_name_alert_title)
                        .positiveText(getString(R.string.action_change))
                        .positiveColorRes(R.color.purple)
                        .negativeText(getString(android.R.string.cancel))
                        .negativeColorRes(R.color.black)
                        .inputType(InputType.TYPE_CLASS_TEXT)
                        .input(getString(R.string.business_name), ConversaApp.getInstance(this)
                                .getPreferences().getAccountDisplayName(), new MaterialDialog.InputCallback()
                        {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {
                                dialog.dismiss();
                                onPreferenceChange(PreferencesKeys.ACCOUNT_NAME_KEY, input.toString());
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
            case R.id.llConversaId: {
                new MaterialDialog.Builder(this)
                        .title(R.string.sett_account_conversa_id_alert_title)
                        .positiveText(getString(R.string.action_change))
                        .positiveColorRes(R.color.purple)
                        .negativeText(getString(android.R.string.cancel))
                        .negativeColorRes(R.color.black)
                        .inputType(InputType.TYPE_CLASS_TEXT)
                        .input(getString(R.string.conversa_id), ConversaApp.getInstance(this)
                                .getPreferences().getAccountConversaId(), new MaterialDialog.InputCallback()
                        {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {
                                dialog.dismiss();
                                onPreferenceChange(PreferencesKeys.ACCOUNT_CONVERSA_ID_KEY, input.toString());
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
            case R.id.llPassword: {
                new MaterialDialog.Builder(this)
                        .title(R.string.sett_account_password_alert_title)
                        .positiveText(getString(R.string.action_change))
                        .positiveColorRes(R.color.purple)
                        .negativeText(getString(android.R.string.cancel))
                        .negativeColorRes(R.color.black)
                        .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)
                        .input(getString(R.string.password), "", new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {
                                dialog.dismiss();
                                onPreferenceChange(PreferencesKeys.ACCOUNT_PASSWORD_KEY, input.toString());
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
            case R.id.rlCategories: {
                Intent intent = new Intent(this, ActivitySettingsCategory.class);
                startActivity(intent);
                break;
            }
            case R.id.btnEdit: {
                myBottomSheet.show(getSupportFragmentManager(), myBottomSheet.getTag());
                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            switch (requestCode) {
                case Const.CAPTURE_MEDIA: {
                    final String path = ImageFilePath.getPath(this, Uri.parse(data.getStringExtra("imageUri")));
                    final ParseFile file = new ParseFile(new File(path));
                    file.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                saveAvatar(file, path);
                            } else {
                                showErrorMessage(getString(R.string.settings_avatar_error));
                            }
                        }
                    });
                    break;
                }
            }
        } else {
            Logger.error("onActivityResult", "Error");
        }
    }

    private void saveAvatar(ParseFile file, final String path) {
        final HashMap<String, Object> params = new HashMap<>(9);
        params.put("avatar", file);
        params.put("businessId", ConversaApp.getInstance(this).getPreferences().getAccountBusinessId());
        ParseCloud.callFunctionInBackground("updateBusinessAvatar", params, new FunctionCallback<Object>() {
            @Override
            public void done(Object object, ParseException e) {
                if (e == null) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                new File(ConversaApp.getInstance(getApplicationContext())
                                        .getPreferences()
                                        .getAccountAvatar()
                                ).delete();

                                ConversaApp.getInstance(getApplicationContext())
                                        .getPreferences()
                                        .setAccountAvatar(path);
                            } catch (Exception ignored) {
                                ConversaApp.getInstance(getApplicationContext())
                                        .getPreferences()
                                        .setAccountAvatar(path);
                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showSuccessMessage(getString(R.string.settings_avatar_succesful));
                                }
                            });
                        }
                    }).start();
                } else {
                    showErrorMessage(getString(R.string.settings_avatar_error));
                }
            }
        });
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(PreferencesKeys.ACCOUNT_CONVERSA_ID_KEY)) {
            ((LightTextView)findViewById(R.id.ltvConversaId)).setText(
                    ConversaApp.getInstance(this).getPreferences().getAccountConversaId()
            );
        } else if (key.equals(PreferencesKeys.ACCOUNT_DISPLAY_NAME_KEY)) {
            ((LightTextView)findViewById(R.id.ltvDisplayName)).setText(
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

    public void onPreferenceChange(String key, final String newValue) {
        if (TextUtils.isEmpty(newValue)) {
            showErrorMessage(getString(R.string.common_field_required));
        }

        switch (key) {
            case PreferencesKeys.ACCOUNT_NAME_KEY: {
                String name = newValue;
                name = name.replaceAll("\\t", "");
                name = name.replaceAll("\\n", "");
                final String newName = name;

                if (newName.equals(ConversaApp.getInstance(this).getPreferences().getAccountDisplayName()))
                    break;

                HashMap<String, String> params = new HashMap<>(2);
                params.put("displayName", newName);
                params.put("businessId", ConversaApp.getInstance(this).getPreferences().getAccountBusinessId());
                ParseCloud.callFunctionInBackground("updateBusinessName", params, new FunctionCallback<Integer>() {
                    @Override
                    public void done(Integer object, ParseException e) {
                        if (e == null) {
                            ConversaApp.getInstance(getApplicationContext())
                                    .getPreferences()
                                    .setAccountDisplayName(newName, true);
                            showSuccessMessage(getString(R.string.settings_name_succesful));
                        } else {
                            showErrorMessage(getString(R.string.settings_name_error));
                        }
                    }
                });
                break;
            }
            case PreferencesKeys.ACCOUNT_PASSWORD_KEY: {
                Account.getCurrentUser().setPassword(newValue);
                Account.getCurrentUser().saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            showSuccessMessage(getString(R.string.settings_password_succesful));
                        } else {
                            showErrorMessage(getString(R.string.settings_password_error));
                        }
                    }
                });

                break;
            }
            default:
                String name = newValue;
                name = name.replaceAll("\\t", "");
                name = name.replaceAll("\\n", "");
                final String newName = name;

                if (newName.equals(ConversaApp.getInstance(this).getPreferences().getAccountConversaId()))
                    break;

                HashMap<String, String> params = new HashMap<>(2);
                params.put("conversaId", newName);
                params.put("businessId", ConversaApp.getInstance(this).getPreferences().getAccountBusinessId());
                ParseCloud.callFunctionInBackground("updateBusinessId", params, new FunctionCallback<Integer>() {
                    @Override
                    public void done(Integer object, ParseException e) {
                        if (e == null) {
                            ConversaApp.getInstance(getApplicationContext())
                                    .getPreferences()
                                    .setAccountConversaId(newName);
                            showSuccessMessage(getString(R.string.settings_conversa_id_succesful));
                        } else {
                            showErrorMessage(getString(R.string.settings_conversa_id_error));
                        }
                    }
                });
                break;
        }
    }

    private void showSuccessMessage(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);

        String positiveText = getString(android.R.string.ok);
        builder.setPositiveButton(positiveText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showErrorMessage(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);

        String positiveText = getString(android.R.string.ok);
        builder.setPositiveButton(positiveText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

}