package ee.app.conversamanager.login;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ProgressCallback;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import ee.app.conversamanager.ConversaApp;
import ee.app.conversamanager.R;
import ee.app.conversamanager.extendables.BaseActivity;
import ee.app.conversamanager.model.nCountry;
import ee.app.conversamanager.model.parse.Account;
import ee.app.conversamanager.utils.Const;
import ee.app.conversamanager.utils.Utils;

import static ee.app.conversamanager.R.id.btnSignUpUp;
import static ee.app.conversamanager.utils.Const.kUserAvatarKey;
import static ee.app.conversamanager.utils.Const.kUserCategoryKey;
import static ee.app.conversamanager.utils.Const.kUserCountryKey;
import static ee.app.conversamanager.utils.Const.kUserTypeKey;

/**
 * Created by edgargomez on 1/3/17.
 */

public class ActivityRegisterComplete extends BaseActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private EditText mEtSignUpEmail;
    private EditText mEtSignUpPassword;
    private EditText mEtSignUpCountry;
    private Spinner mSpCountry;

    private nCountry selectedCountry;
    private List<nCountry> countries;

    CustomAdapter dataAdapter;

    private String categoryId;
    private String path;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_complete);
        initialization();
    }

    @Override
    protected void initialization() {
        super.initialization();

        categoryId = getIntent().getExtras().getString(Const.iExtraSignUpCategory, null);
        path = getIntent().getExtras().getString(Const.iExtraSignUpAvatar, null);

        mEtSignUpEmail = (EditText) findViewById(R.id.etSignUpEmail);
        mEtSignUpPassword = (EditText) findViewById(R.id.etSignUpPassword);
        mSpCountry = (Spinner) findViewById(R.id.spCountry);

        findViewById(R.id.tilEmailSignUp).setOnClickListener(this);
        findViewById(R.id.tilPasswordSignUp).setOnClickListener(this);

        Button mBtnSignUpUp = (Button) findViewById(btnSignUpUp);
        mBtnSignUpUp.setOnClickListener(this);
        mBtnSignUpUp.setTypeface(ConversaApp.getInstance(this).getTfRalewayMedium());

        countries = new ArrayList<>(1);
        dataAdapter = new CustomAdapter(this, android.R.layout.simple_spinner_item, countries);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpCountry.setAdapter(dataAdapter);
        mSpCountry.setOnItemSelectedListener(this);

        HashMap<String, Object> params = new HashMap<>(1);
        ParseCloud.callFunctionInBackground("getCountries", params, new FunctionCallback<String>() {
            @Override
            public void done(String jsonCountries, ParseException e) {
                if (e != null) {
                    showErrorMessage(getString(R.string.sign_up_register_countries_error));
                } else {
                    try {
                        JSONArray countries = new JSONArray(jsonCountries);

                        int size = countries.length();
                        List<nCountry> countryList = new ArrayList<>(size);

                        for (int i = 0; i < size; i++) {
                            JSONObject jsonCategory = countries.getJSONObject(i);

                            nCountry country = new nCountry(
                                    jsonCategory.optString("id", ""),
                                    jsonCategory.optString("na", ""));

                            countryList.add(country);
                        }

                        Collections.sort(countryList, new Comparator<nCountry>() {
                            @Override
                            public int compare(final nCountry object1, final nCountry object2) {
                                return object1.getName().compareTo(object2.getName());
                            }
                        });

                        dataAdapter.setItems(countryList);
                    } catch (JSONException ignored) {
                        showErrorMessage(getString(R.string.sign_up_register_countries_error));
                    }
                }
            }
        });
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
                if (validateForm()) {
                    uploadAvatar();
                }
                break;
            }
        }
    }

    private void uploadAvatar() {
        if (path == null) {
            completeSignup(null);
        } else {
            byte[] data = "Working at Parse is great!".getBytes();
            final ParseFile file = new ParseFile("avatar.jpg", data);
            file.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        completeSignup(file);
                    } else {
                        // Show alert
                    }
                }
            }, new ProgressCallback() {
                @Override
                public void done(Integer percentDone) {

                }
            });
        }
    }

    private void completeSignup(ParseFile avatar) {
        Account user = new Account();

        String username = TextUtils.split(mEtSignUpEmail.getText().toString(), "@")[0];

        user.setEmail(mEtSignUpEmail.getText().toString());
        user.setUsername(username);
        user.setPassword(mEtSignUpPassword.getText().toString());

        user.put(kUserTypeKey, 2);
        user.put(kUserCategoryKey, categoryId);
        user.put(kUserCountryKey, selectedCountry.getId());

        if (avatar != null) {
            user.put(kUserAvatarKey, avatar);
        }

        final ProgressDialog progress = new ProgressDialog(this);
        progress.show();

        user.signUpInBackground(new SignUpCallback() {
            public void done(ParseException e) {
                progress.dismiss();
                if (e == null) {
                    // Hooray! Let them use the app now.

                } else {
                    // Sign up didn't succeed. Look at the ParseException
                    // to figure out what went wrong

                }
            }
        });
    }

    private boolean validateForm() {
        String title = null;

        if (mEtSignUpEmail.getText().toString().isEmpty()) {
            title = getString(R.string.common_field_required);
        } else if (!Utils.checkEmail(mEtSignUpEmail.getText().toString())) {
            title = getString(R.string.common_field_invalid);
        } else if (mEtSignUpPassword.getText().toString().isEmpty()) {
            title = getString(R.string.common_field_required);
        } else if (selectedCountry == null) {
            title = getString(R.string.common_field_required);
        }

        if (title != null) {
            new AlertDialog.Builder(this)
                    .setTitle(title)
                    .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
            return false;
        }

        return true;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (countries.size() > 0) {
            selectedCountry = countries.get(position);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

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

    private class CustomAdapter extends ArrayAdapter {

        private Context context;
        private List<nCountry> itemList;

        CustomAdapter(Context context, int textViewResourceId, List<nCountry> itemList) {
            super(context, textViewResourceId, itemList);
            this.context=context;
            this.itemList=itemList;
        }

        void setItems(List<nCountry> itemList) {
            this.itemList.addAll(itemList);
            notifyDataSetChanged();
        }

        private View getCustomView(int position, View convertView, ViewGroup parent) {
            TextView v = (TextView) super.getView(position, convertView, parent);
            v.setTypeface(ConversaApp.getInstance(context).getTfRalewayRegular());
            v.setText(itemList.get(position).getName());
            return v;
        }

        // It gets a View that displays in the drop down popup the data at the specified position
        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        // It gets a View that displays the data at the specified position
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

    }

//    - (void)doRegister {
//        [MBProgressHUD showHUDAddedTo:self.view animated:YES];
//
//        if (self.avatar) {
//            PFFile *filePicture = [PFFile fileWithName:@"avatar.jpg" data:UIImageJPEGRepresentation(self.avatar, 1)];
//
//            [filePicture saveInBackgroundWithBlock:^(BOOL succeeded, NSError * _Nullable error) {
//                if (error) {
//                    [self showErrorMessage:NSLocalizedString(@"signup_complete_error", nil)];
//                } else {
//                    [self completeRegister:filePicture];
//                }
//            }];
//        } else {
//            [self completeRegister:nil];
//        }
//    }
//
//    - (void)doRegister {
//        [MBProgressHUD showHUDAddedTo:self.view animated:YES];
//
//        if (self.avatar) {
//            PFFile *filePicture = [PFFile fileWithName:@"avatar.jpg" data:UIImageJPEGRepresentation(self.avatar, 1)];
//
//            [filePicture saveInBackgroundWithBlock:^(BOOL succeeded, NSError * _Nullable error) {
//                if (error) {
//                    [self showErrorMessage:NSLocalizedString(@"signup_complete_error", nil)];
//                } else {
//                    [self completeRegister:filePicture];
//                }
//            }];
//        } else {
//            [self completeRegister:nil];
//        }
//    }
//
//    - (void)completeRegister:(PFFile*)file {
//        Account *user = [Account object];
//        NSArray *emailPieces = [self.emailTextField.text componentsSeparatedByString: @"@"];
//        user.username = [emailPieces objectAtIndex: 0];
//        user.email = self.emailTextField.text;
//        user.password = self.passwordTextField.text;
//        // Extra fields
//        user[kUserTypeKey] = @(2);
//        user[kUserTypeBusinessName] = self.businessName;
//        user[kUserTypeBusinessConversaId] = self.conversaId;
//        user[kUserTypeBusinessCategory] = self.categoryId;
//        user[kUserTypeBusinessCountry] = [self.countryPicked getObjectId];
//
//        if (file) {
//            user[kUserTypeBusinessAvatar] = file;
//        }
//
//        [user signUpInBackgroundWithBlock:^(BOOL succeeded, NSError * _Nullable error) {
//            [MBProgressHUD hideHUDForView:self.view animated:YES];
//            if (error) {
//                if (error.code == kPFErrorUserEmailTaken) {
//                    [self showErrorMessage:NSLocalizedString(@"signup_email_error", nil)];
//                } else {
//                    [self showErrorMessage:NSLocalizedString(@"signup_complete_error", nil)];
//                }
//            } else {
//                [LoginHandler proccessLoginForAccount:[Account currentUser] fromViewController:self];
//            }
//        }];
//    }

}