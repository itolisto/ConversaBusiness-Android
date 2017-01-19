package ee.app.conversamanager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import ee.app.conversamanager.extendables.BaseActivity;
import ee.app.conversamanager.login.ActivityCheck;
import ee.app.conversamanager.login.ActivityLogIn;
import ee.app.conversamanager.utils.Const;
import ee.app.conversamanager.utils.Logger;

/**
 * ActivitySignIn
 * 
 * Allows user to sign in, sign up or receive an email with password if user
 * is already registered with a valid email.
 * 
 */
public class ActivitySignIn extends BaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        checkInternetConnection = false;
        initialization();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null) {
            if (intent.getExtras().getInt(Const.ACTION, -1) == -1) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Logger.error("ActivitySignIn", "Refreshing database");
                        ConversaApp.getInstance(getApplicationContext())
                                .getDB()
                                .refreshDbHelper();
                    }
                }).start();

                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
                dialogBuilder.setTitle(getString(R.string.sett_account_logout_title));
                dialogBuilder.setMessage(getString(R.string.parse_logout_reason));
                dialogBuilder.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                });
                AlertDialog b = dialogBuilder.create();
                b.show();
                intent.removeExtra(Const.ACTION);
            }
        }
    }

    protected void initialization() {
        super.initialization();
        Button mBtnSignIn = (Button) findViewById(R.id.btnSignIn);
        Button mBtnSignUp = (Button) findViewById(R.id.btnSignUp);
        ImageView mivLanguage = (ImageView) findViewById(R.id.ivLanguage);

        if(mBtnSignIn != null) {
            mBtnSignIn.setOnClickListener(this);
            mBtnSignIn.setTypeface(ConversaApp.getInstance(this).getTfRalewayMedium());
        }

        if(mBtnSignUp != null) {
            mBtnSignUp.setOnClickListener(this);
            mBtnSignUp.setTypeface(ConversaApp.getInstance(this).getTfRalewayMedium());
        }

        mivLanguage.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSignIn: {
                Intent intent = new Intent(getApplicationContext(), ActivityLogIn.class);
                startActivity(intent);
                break;
            }
            case R.id.btnSignUp: {
                Intent intent = new Intent(getApplicationContext(), ActivityCheck.class);
                startActivity(intent);
                break;
            }
            case R.id.ivLanguage: {
                final int index;

                switch(ConversaApp.getInstance(getBaseContext()).getPreferences().getLanguage()) {
                    case "en":
                        index = 1;
                        break;
                    case "es":
                        index = 2;
                        break;
                    default:
                        index = 0;
                        break;
                }

                AlertDialog.Builder b = new AlertDialog.Builder(this);
                b.setTitle(R.string.language_spinner_title);
                b.setSingleChoiceItems(R.array.language_entries, index, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (which != index) {
                            ConversaApp.getInstance(getBaseContext()).getPreferences()
                                    .setLanguage(getResources().getStringArray(R.array.language_values)[which]);
                            recreate();
                        }
                    }
                });
                b.show();
                break;
            }
        }
    }

}