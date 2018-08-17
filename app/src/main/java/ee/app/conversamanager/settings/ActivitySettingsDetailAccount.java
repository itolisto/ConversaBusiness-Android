package ee.app.conversamanager.settings;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;

import ee.app.conversamanager.ConversaApp;
import ee.app.conversamanager.R;
import ee.app.conversamanager.extendables.ConversaActivity;
import ee.app.conversamanager.interfaces.FunctionCallback;
import ee.app.conversamanager.networking.FirebaseCustomException;
import ee.app.conversamanager.networking.NetworkingManager;
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
                FirebaseAuth.getInstance().getCurrentUser().getEmail()
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
                    // Create FirebaseStorage
                    FirebaseStorage storage = FirebaseStorage.getInstance("gs://luminous-inferno-3905-business");
                    // Create a storage reference from our app
                    StorageReference storageRef = storage.getReference();
                    // Reference to messages images
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                    if (user != null) {
                        final String path = ImageFilePath.getPath(this, Uri.parse(data.getStringExtra("imageUri")));
                        final Uri file = Uri.fromFile(new File(path));
                        // Schema is of type file://......
                        File files = new File(file.getPath());

                        final StorageReference riversRef = storageRef.child(ConversaApp.getInstance(this).getPreferences().getAccountBusinessId() + "/" + files.getName());
                        // Create upload task
                        UploadTask uploadTask = riversRef.putFile(file);

                        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                if (!task.isSuccessful()) {
                                    throw task.getException();
                                }

                                // Continue with the task to get the download URL
                                return riversRef.getDownloadUrl();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()) {
                                    Uri downloadUri = task.getResult();
                                    saveAvatar(downloadUri.toString(), path);
                                } else {
                                    showErrorMessage(getString(R.string.settings_avatar_error));
                                }
                            }
                        });
                    }
                    break;
                }
            }
        } else {
            Logger.error("onActivityResult", "Error");
        }
    }

    private void saveAvatar(String file, final String path) {
        final HashMap<String, Object> params = new HashMap<>(9);
        params.put("avatar", file);
        params.put("businessId", ConversaApp.getInstance(this).getPreferences().getAccountBusinessId());
        NetworkingManager.getInstance().post("business/updateBusinessAvatar", params, new FunctionCallback<Object>() {
            @Override
            public void done(Object json, FirebaseCustomException exception) {
                if (exception == null) {
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
                NetworkingManager.getInstance().post("business/updateBusinessName", params, new FunctionCallback<JSONObject>() {
                    @Override
                    public void done(JSONObject object, FirebaseCustomException e) {
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
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if (user != null) {
                    user.updatePassword(newValue)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    showSuccessMessage(getString(R.string.settings_password_succesful));
                                } else {
                                    showErrorMessage(getString(R.string.settings_password_error));
                                }
                            }
                        });
                }
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
                NetworkingManager.getInstance().post("business/updateBusinessId", params, new FunctionCallback<JSONObject>() {
                    @Override
                    public void done(JSONObject object, FirebaseCustomException e) {
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