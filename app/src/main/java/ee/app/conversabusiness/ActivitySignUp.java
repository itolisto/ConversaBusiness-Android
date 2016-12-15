package ee.app.conversabusiness;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.parse.ParseException;
import com.parse.SignUpCallback;

import ee.app.conversabusiness.extendables.BaseActivity;
import ee.app.conversabusiness.model.parse.Account;
import ee.app.conversabusiness.utils.Const;
import ee.app.conversabusiness.utils.Utils;

import static ee.app.conversabusiness.R.id.btnSignUpUp;

/**
 * Created by edgargomez on 8/12/16.
 */
public class ActivitySignUp extends BaseActivity implements View.OnClickListener {

    private Button mBtnSignUpUp;
    private EditText mEtSignUpEmail;
    private EditText mEtSignUpPassword;
    private TextInputLayout mTilSignUpEmail;
    private TextInputLayout mTilSignUpPassword;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        initialization();
    }

    @Override
    protected void initialization() {
        super.initialization();
        mEtSignUpEmail = (EditText) findViewById(R.id.etSignUpEmail);
        mEtSignUpPassword = (EditText) findViewById(R.id.etSignUpPassword);

        mTilSignUpEmail = (TextInputLayout) findViewById(R.id.tilEmailSignUp);
        mTilSignUpPassword = (TextInputLayout) findViewById(R.id.tilPasswordSignUp);

        mBtnSignUpUp = (Button) findViewById(btnSignUpUp);

        mEtSignUpEmail.addTextChangedListener(new MyTextWatcher(mEtSignUpEmail));
        mEtSignUpPassword.addTextChangedListener(new MyTextWatcher(mEtSignUpPassword));

        mTilSignUpEmail.setOnClickListener(this);
        mTilSignUpPassword.setOnClickListener(this);

        mBtnSignUpUp.setOnClickListener(this);
        mBtnSignUpUp.setTypeface(ConversaApp.getInstance(this).getTfRalewayMedium());
    }

    @Override
    public void yesInternetConnection() {
        super.yesInternetConnection();
        if (validateForm()) {
            mBtnSignUpUp.setEnabled(true);
        }
    }

    @Override
    public void noInternetConnection() {
        super.noInternetConnection();
        mBtnSignUpUp.setEnabled(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tilEmailSignUp: {
                mEtSignUpEmail.requestFocus();
                break;
            }
            case R.id.tilPasswordSignUp: {
                mEtSignUpPassword.requestFocus();
                break;
            }
            case btnSignUpUp: {
                Account user = new Account();

                String username = TextUtils.split(mEtSignUpEmail.getText().toString(), "@")[0];

                user.setEmail(mEtSignUpEmail.getText().toString());
                user.setUsername(username);
                user.setPassword(mEtSignUpPassword.getText().toString());
                user.put(Const.kUserTypeKey, 2);

                final ProgressDialog progress = new ProgressDialog(this);
                progress.show();

                user.signUpInBackground(new SignUpCallback() {
                    public void done(ParseException e) {
                        progress.dismiss();
                        if (e == null) {
                            // Hooray! Let them use the app now.
                            AuthListener(true, null);
                        } else {
                            // Sign up didn't succeed. Look at the ParseException
                            // to figure out what went wrong
                            AuthListener(false, e);
                        }
                    }
                });
                break;
            }
        }
    }

    private boolean validateForm() {
        if (mEtSignUpEmail.getText().toString().isEmpty() || mEtSignUpPassword.getText().toString().isEmpty()) {
            mBtnSignUpUp.setEnabled(false);
        } else if (mTilSignUpEmail.isErrorEnabled() || mTilSignUpPassword.isErrorEnabled()) {
            mBtnSignUpUp.setEnabled(false);
        } else {
            mBtnSignUpUp.setEnabled(true);
            return true;
        }

        return false;
    }

    private void isEmailValid(String email) {
        TextInputLayout layout = mTilSignUpEmail;

        if (email.isEmpty()) {
            layout.setErrorEnabled(true);
            layout.setError(getString(R.string.sign_email_length_error));
        } else {
            if (Utils.checkEmail(email)) {
                layout.setErrorEnabled(false);
                layout.setError("");
            } else {
                layout.setErrorEnabled(true);
                layout.setError(getString(R.string.sign_email_not_valid_error));
            }
        }
    }

    private void isPasswordValid(String password) {
        TextInputLayout layout = mTilSignUpPassword;

        if (password.isEmpty()) {
            layout.setErrorEnabled(true);
            layout.setError(getString(R.string.signup_password_empty_error));
        } else {
            if (password.length() < 6) {
                layout.setErrorEnabled(true);
                layout.setError(getString(R.string.signup_password_length_error));
            } else {
                if (Utils.checkPassword(password)) {
                    layout.setErrorEnabled(false);
                    layout.setError("");
                } else {
                    layout.setErrorEnabled(true);
                    layout.setError(getString(R.string.signup_password_regex_error));
                }
            }
        }
    }

    public void AuthListener(boolean result, ParseException error) {
        if(result) {
            Intent intent = new Intent(this, ActivityMain.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.signup_register_error));

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

    private class MyTextWatcher implements TextWatcher {

        private View view;

        private MyTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

        public void afterTextChanged(Editable editable) {
            switch (view.getId()) {
                case R.id.etSignUpEmail:
                    isEmailValid(editable.toString());
                    break;
                case R.id.etSignUpPassword:
                    isPasswordValid(editable.toString());
                    break;
            }

            validateForm();
        }
    }
}