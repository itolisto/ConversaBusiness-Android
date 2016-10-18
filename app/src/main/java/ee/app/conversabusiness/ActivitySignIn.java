package ee.app.conversabusiness;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import ee.app.conversabusiness.extendables.BaseActivity;
import ee.app.conversabusiness.utils.Utils;

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
        setContentView(R.layout.activity_sign_in);
        initialization();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Utils.hideKeyboard(this);
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
                Intent intent = new Intent(getApplicationContext(), ActivitySignUp.class);
                startActivity(intent);
                break;
            }
            case R.id.ivLanguage: {
                int index;

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
                        ConversaApp.getInstance(getBaseContext()).getPreferences()
                                .setLanguage(getResources().getStringArray(R.array.language_values)[which]);
                        recreate();
                        dialog.dismiss();
                    }
                });
                b.show();
                break;
            }
        }
    }

}