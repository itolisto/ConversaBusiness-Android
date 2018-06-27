package ee.app.conversamanager.login;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.HashMap;

import ee.app.conversamanager.ActivitySignIn;
import ee.app.conversamanager.ConversaApp;
import ee.app.conversamanager.R;
import ee.app.conversamanager.extendables.BaseActivity;
import ee.app.conversamanager.interfaces.FunctionCallback;
import ee.app.conversamanager.model.nBusiness;
import ee.app.conversamanager.networking.FirebaseCustomException;
import ee.app.conversamanager.networking.NetworkingManager;
import ee.app.conversamanager.utils.Const;
import ee.app.conversamanager.utils.Utils;

/**
 * Created by edgargomez on 1/3/17.
 */

public class ActivityContact extends BaseActivity implements View.OnClickListener {

    private EditText mEtName;
    private EditText mEtEmail;
    private EditText mEtJob;
    private EditText mEtNumber;
    private nBusiness mBusiness;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        initialization();
    }

    @Override
    protected void initialization() {
        super.initialization();
        mEtName = (EditText) findViewById(R.id.etName);
        mEtEmail = (EditText) findViewById(R.id.etEmail);
        mEtJob = (EditText) findViewById(R.id.etJob);
        mEtNumber = (EditText) findViewById(R.id.etNumber);

        findViewById(R.id.tilName).setOnClickListener(this);
        findViewById(R.id.tilEmail).setOnClickListener(this);
        findViewById(R.id.tilJob).setOnClickListener(this);
        findViewById(R.id.tilNumber).setOnClickListener(this);

        Button mBtnReclaim = (Button) findViewById(R.id.btnReclaim);
        mBtnReclaim.setOnClickListener(this);
        mBtnReclaim.setTypeface(ConversaApp.getInstance(this).getTfRalewayMedium());

        mBusiness = getIntent().getExtras().getParcelable(Const.iExtraBusinessReclaimed);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tilName: {
                mEtName.requestFocus();
                break;
            }
            case R.id.tilEmail: {
                mEtEmail.requestFocus();
                break;
            }
            case R.id.tilJob: {
                mEtJob.requestFocus();
                break;
            }
            case R.id.tilNumber: {
                mEtNumber.requestFocus();
                break;
            }
            case R.id.btnReclaim: {
                if (validateForm()) {
                    HashMap<String, Object> params = new HashMap<>(1);
                    params.put("businessId", mBusiness.getBusinessId());
                    params.put("name", mEtName.getText().toString());
                    params.put("email", mEtEmail.getText().toString());
                    params.put("position", mEtJob.getText().toString());
                    params.put("contact", mEtNumber.getText().toString());
                    NetworkingManager.getInstance().post("business/businessClaimRequest", params, new FunctionCallback<Integer>() {
                        @Override
                        public void done(Integer object, FirebaseCustomException e) {
                            String title;

                            if (e == null) {
                                title = getString(R.string.signup_contact_success);
                            } else {
                                title = getString(R.string.signup_contact_error);
                            }

                            new AlertDialog.Builder(ActivityContact.this)
                                    .setTitle(title)
                                    .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            Intent intent = new Intent(ActivityContact.this, ActivitySignIn.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            startActivity(intent);
                                        }
                                    })
                                    .show();
                        }
                    });
                }
                break;
            }
        }
    }

    private boolean validateForm() {
        String title = null;
        EditText select = null;

        if (mEtName.getText().toString().isEmpty()) {
            title = getString(R.string.common_field_required);
            select = mEtName;
        } else if (mEtEmail.getText().toString().isEmpty()) {
            title = getString(R.string.common_field_required);
            select = mEtEmail;
        } else if (!Utils.checkEmail(mEtEmail.getText().toString())) {
            title = getString(R.string.common_field_invalid);
            select = mEtEmail;
        } else if (mEtJob.getText().toString().isEmpty()) {
            title = getString(R.string.common_field_required);
            select = mEtJob;
        } else if (mEtNumber.getText().toString().isEmpty()) {
            title = getString(R.string.common_field_required);
            select = mEtNumber;
        }

        if (title != null) {
            final EditText finalSelect = select;
            new AlertDialog.Builder(this)
                    .setTitle(title)
                    .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finalSelect.requestFocus();
                        }
                    })
                    .show();
            return false;
        }

        return true;
    }

}