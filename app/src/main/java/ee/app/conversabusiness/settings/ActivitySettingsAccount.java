package ee.app.conversabusiness.settings;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;

import ee.app.conversabusiness.R;
import ee.app.conversabusiness.extendables.ConversaActivity;

/**
 * Created by edgargomez on 9/9/16.
 */
public class ActivitySettingsAccount extends ConversaActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_account);
        initialization();
    }

    @Override
    protected void initialization() {
        super.initialization();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle(R.string.preferences__account);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        //((FragmentPreferences)getActivity()).getSupportActionBar().setTitle(R.string.preferences__account);
    }

//    @Override
//    public boolean onPreferenceClick(Preference preference) {
//        switch (preference.getKey()) {
//            case PreferencesKeys.ACCOUNT_LOGOUT_KEY: {
//                int colorNegative, colorPositive;
//
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                    colorPositive = getActivity().getResources().getColor(android.R.color.holo_red_light, null);
//                    colorNegative = getActivity().getResources().getColor(R.color.default_black, null);
//                } else {
//                    colorPositive = getActivity().getResources().getColor(android.R.color.holo_red_light);
//                    colorNegative = getActivity().getResources().getColor(R.color.default_black);
//                }
//
//                final CustomDialog dialog = new CustomDialog(getActivity());
//                dialog.setTitle(getString(R.string.logout_message))
//                        .setMessage(null)
//                        .setupNegativeButton(getString(R.string.cancel), new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                dialog.dismiss();
//                            }
//                        })
//                        .setNegativeColor(colorNegative)
//                        .setupPositiveButton(getString(R.string.logout_ok), new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                dialog.dismiss();
//                                appLogout();
//                            }
//                        })
//                        .setPositiveColor(colorPositive);
//                dialog.show();
//                break;
//            }
//            default:
//                return false;
//        }
//
//        return true;
//    }

//    @Override
//    public boolean onPreferenceChange(Preference preference, Object newValue) {
//        switch (preference.getKey()) {
//            case PreferencesKeys.ACCOUNT_EMAIL_KEY: {
//                final String oldEmail = Account.getCurrentUser().getEmail();
//                String email = (String) newValue;
//                email = email.replaceAll("\\t", "");
//                email = email.replaceAll("\\n", "");
//                email = email.replaceAll(" ", "");
//                final String newEmail = email;
//
//                if (newEmail.isEmpty()) {
//                    showErrorMessage(getString(R.string.sign_email_length_error));
//                } else {
//                    if (Utils.checkEmail(newEmail)) {
//                        Account.getCurrentUser().setEmail(newEmail);
//                        Account.getCurrentUser().saveInBackground(new SaveCallback() {
//                            @Override
//                            public void done(ParseException e) {
//                                if (e == null) {
//                                    findPreference(PreferencesKeys.ACCOUNT_EMAIL_KEY).setSummary(newEmail);
//                                    showSuccessMessage(getString(R.string.settings_email_succesful));
//                                } else {
//                                    Account.getCurrentUser().setEmail(oldEmail);
//                                    showErrorMessage(getString(R.string.settings_email_error));
//                                }
//                            }
//                        });
//                    } else {
//                        showErrorMessage(getString(R.string.sign_email_not_valid_error));
//                    }
//                }
//                // Return false as we don't wanna save/update this preference
//                return false;
//            }
//            case PreferencesKeys.ACCOUNT_PASSWORD_KEY: {
//                String newPassword = (String) newValue;
//
//                if (newPassword.isEmpty()) {
//                    showErrorMessage(getString(R.string.signup_password_empty_error));
//                } else {
//                    if (Utils.checkPassword(newPassword)) {
//                        Account.getCurrentUser().setPassword(newPassword);
//                        Account.getCurrentUser().saveInBackground(new SaveCallback() {
//                            @Override
//                            public void done(ParseException e) {
//                                if (e == null) {
//                                    showSuccessMessage(getString(R.string.settings_password_succesful));
//                                } else {
//                                    showErrorMessage(getString(R.string.settings_password_error));
//                                }
//                            }
//                        });
//                    } else {
//                        showErrorMessage(getString(R.string.signup_password_regex_error));
//                    }
//                }
//
//                return false;
//            }
//            default:
//                return false;
//        }
//    }

//    private void showSuccessMessage(String message) {
//        final CustomDialog dialog = new CustomDialog(getActivity());
//        dialog.setTitle(null)
//                .setMessage(message)
//                .setupNegativeButton(null, null)
//                .setupPositiveButton(getString(android.R.string.ok), new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        dialog.dismiss();
//                    }
//                });
//        dialog.show();
//    }
//
//    private void showErrorMessage(String message) {
//        final CustomDialog dialog = new CustomDialog(getActivity());
//        dialog.setTitle(null)
//                .setMessage(message)
//                .setupNegativeButton(null, null)
//                .setupPositiveButton(getString(android.R.string.ok), new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        dialog.dismiss();
//                    }
//                });
//        dialog.show();
//    }
//
//    private void appLogout() {
//        boolean result = ConversaApp.getInstance(getActivity()).getDB().deleteDatabase();
//        if(result)
//            Logger.error("Logout", "Database removed");
//        else
//            Logger.error("Logout", "An error has occurred while removing databased. Database not removed");
//
//        Collection<String> tempList = new ArrayList<>(2);
//        tempList.add("upbc");
//        tempList.add("upvt");
//        OneSignal.deleteTags(tempList);
//        OneSignal.clearOneSignalNotifications();
//        OneSignal.setSubscription(false);
//        AblyConnection.getInstance().disconnectAbly();
//
//        Account.logOut();
//        AppCompatActivity fromActivity = (AppCompatActivity) getActivity();
//        Intent goToSignIn = new Intent(fromActivity, ActivitySignIn.class);
//        goToSignIn.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        goToSignIn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        ConversaApp.getInstance(getActivity()).getPreferences().cleanSharedPreferences();
//        fromActivity.startActivity(goToSignIn);
//        fromActivity.finish();
//    }

}