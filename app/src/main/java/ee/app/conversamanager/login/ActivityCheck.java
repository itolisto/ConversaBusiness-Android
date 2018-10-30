package ee.app.conversamanager.login;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import ee.app.conversamanager.R;
import ee.app.conversamanager.extendables.BaseActivity;
import ee.app.conversamanager.model.nBusiness;
import ee.app.conversamanager.utils.Const;

/**
 * Created by edgargomez on 1/3/17.
 */

public class ActivityCheck extends BaseActivity implements View.OnClickListener {

    private EditText mEtCheckName;
    private ArrayList<nBusiness> list;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check);
        initialization();
    }

    @Override
    protected void initialization() {
        super.initialization();
        list = new ArrayList<>(1);
        mEtCheckName = (EditText) findViewById(R.id.etName);
        findViewById(R.id.tilName).setOnClickListener(this);
        findViewById(R.id.btnCheck).setOnClickListener(this);
        findViewById(R.id.btnSkip).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tilName: {
                mEtCheckName.requestFocus();
                break;
            }
            case R.id.btnCheck: {
                if (validateForm()) {
                    HashMap<String, Object> params = new HashMap<>(1);
                    params.put("search", mEtCheckName.getText().toString());

                    ParseCloud.callFunctionInBackground("businessClaimSearch", params, new FunctionCallback<String>() {
                        @Override
                        public void done(String object, ParseException e) {
                            if (e == null) {
                                try {
                                    JSONArray results = new JSONArray(object);

                                    int size = results.length();

                                    for (int i = 0; i < size; i++) {
                                        JSONObject jsonCategory = results.getJSONObject(i);
                                        nBusiness business = new nBusiness(
                                                jsonCategory.optString("oj", ""),
                                                jsonCategory.optString("dn", ""),
                                                jsonCategory.optString("id", ""),
                                                jsonCategory.optString("av", ""));
                                        list.add(business);
                                    }
                                } catch (JSONException ignored) {}
                            }

                            goToActivity();
                        }
                    });
                }
                break;
            }
            case R.id.btnSkip: {
                Intent intent = new Intent(this, ActivityRegister.class);
                startActivity(intent);
                break;
            }
        }
    }

    private boolean validateForm() {
        if (mEtCheckName.getText().toString().isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.common_field_required))
                    .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            mEtCheckName.requestFocus();
                        }
                    })
                    .show();
            return false;
        }

        return true;
    }

    private void goToActivity() {
        if (list.size() > 0) {
            Intent intent = new Intent(this, ActivityBusinessList.class);
            intent.putParcelableArrayListExtra(Const.iExtraList, list);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, ActivityEmptyList.class);
            startActivity(intent);
        }
    }

}