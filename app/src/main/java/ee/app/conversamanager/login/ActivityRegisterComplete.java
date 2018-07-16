package ee.app.conversamanager.login;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import ee.app.conversamanager.ConversaApp;
import ee.app.conversamanager.R;
import ee.app.conversamanager.extendables.BaseActivity;
import ee.app.conversamanager.interfaces.FunctionCallback;
import ee.app.conversamanager.model.nCountry;
import ee.app.conversamanager.model.parse.Account;
import ee.app.conversamanager.networking.FirebaseCustomException;
import ee.app.conversamanager.networking.NetworkingManager;
import ee.app.conversamanager.utils.AppActions;
import ee.app.conversamanager.utils.Const;
import ee.app.conversamanager.utils.Utils;
import ee.app.conversamanager.view.LightTextView;
import ee.app.conversamanager.view.URLSpanNoUnderline;

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

    private String displayName;
    private String conversaId;
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

        displayName = getIntent().getExtras().getString(Const.iExtraSignUpDisplayName, null);
        conversaId = getIntent().getExtras().getString(Const.iExtraSignUpConversaId, null);
        categoryId = getIntent().getExtras().getString(Const.iExtraSignUpCategory, null);
        path = getIntent().getExtras().getString(Const.iExtraSignUpAvatar, null);

        mEtSignUpEmail = (EditText) findViewById(R.id.etSignUpEmail);
        mEtSignUpPassword = (EditText) findViewById(R.id.etSignUpPassword);
        mSpCountry = (Spinner) findViewById(R.id.spCountry);

        findViewById(R.id.tilEmailSignUp).setOnClickListener(this);
        findViewById(R.id.tilPasswordSignUp).setOnClickListener(this);

        Button mBtnSignUpUp = (Button) findViewById(R.id.btnSignUpUp);
        mBtnSignUpUp.setOnClickListener(this);
        mBtnSignUpUp.setTypeface(ConversaApp.getInstance(this).getTfRalewayMedium());

        countries = new ArrayList<>(1);
        dataAdapter = new CustomAdapter(this, android.R.layout.simple_spinner_item, countries);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpCountry.setAdapter(dataAdapter);
        mSpCountry.setOnItemSelectedListener(this);

        LightTextView mLtvTermsPrivacy = (LightTextView) findViewById(R.id.ltvTermsPrivacy);
        String text = mLtvTermsPrivacy.getText().toString();

        String language = ConversaApp.getInstance(this).getPreferences().getLanguage();

        if (language.equals("zz")) {
            if (Locale.getDefault().getLanguage().startsWith("es")) {
                language = "es";
            } else {
                language = "en";
            }
        }

        int indexTerms;
        int indexPrivacy;

        if (language.equals("es")) {
            indexTerms = TextUtils.indexOf(text, "términos");
            indexPrivacy = TextUtils.indexOf(text, "políticas");
        } else {
            indexTerms = TextUtils.indexOf(text, "terms");
            indexPrivacy = TextUtils.indexOf(text, "privacy");
        }

        Spannable styledString = new SpannableString(text);
        // url
        styledString.setSpan(new URLSpanNoUnderline("http://manager.conversachat.com/terms"), (indexTerms==-1 ? 0 : indexTerms), indexTerms + (language.equals("es") ? 8 : 5), 0);
        styledString.setSpan(new URLSpanNoUnderline("http://manager.conversachat.com/privacy"), (indexPrivacy==-1 ? 0 : indexPrivacy), text.length(), 0);
        // change text color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            styledString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.purple, null)),
                    indexTerms, indexTerms + (language.equals("es") ? 8 : 5), 0);
            styledString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.purple, null)),
                    indexPrivacy, text.length(), 0);
        } else {
            styledString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.purple)),
                    (indexTerms==-1 ? 0 : indexTerms), (indexTerms==-1 ? 0 : indexTerms) + (language.equals("es") ? 8 : 5), 0);
            styledString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.purple)),
                    (indexPrivacy==-1 ? 0 : indexPrivacy), text.length(), 0);
        }
        // this step is mandated for the url and clickable styles.
        mLtvTermsPrivacy.setMovementMethod(LinkMovementMethod.getInstance());
        mLtvTermsPrivacy.setText(styledString);

        HashMap<String, Object> params = new HashMap<>(1);
        NetworkingManager.getInstance().post("public/getCountries", params, new FunctionCallback<Object>() {
            @Override
            public void done(Object json, FirebaseCustomException exception) {
                if (exception != null) {
                    showErrorMessage(getString(R.string.sign_up_register_countries_error));
                } else {
                    try {
                        JSONArray countries = new JSONArray(json.toString());

                        int size = countries.length();
                        final List<nCountry> countryList = new ArrayList<>(size);

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

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dataAdapter.setItems(countryList);
                            }
                        });
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
            case R.id.btnSignUpUp: {
                if (validateForm()) {
                    uploadAvatar();
                }
                break;
            }
        }
    }

    private void uploadAvatar() {
//        if (path == null) {
            completeSignup("");
//        } else {
//            FirebaseStorage storage = FirebaseStorage.getInstance("gs://luminous-inferno-3905-business");
//
//            FirebaseAuth mAuth = FirebaseAuth.getInstance();
//            FirebaseUser currentUser = mAuth.getCurrentUser();
//
//            Uri file = Uri.fromFile(new File(path));
//
////            StorageReference storageRef = storage.getReference();
////            StorageReference avatarRef = storageRef.child("avatar/" + currentUser.getUid() + "/" + file.getLastPathSegment());
//            StorageReference storageRef = storage.getReference("avatar/" + currentUser.getUid() + "/" + file.getLastPathSegment());
//
//            UploadTask uploadTask = storageRef.putFile(file);
//            // Register observers to listen for when the download is done or if it fails
//            uploadTask.addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception exception) {
//                    showErrorMessage(getString(R.string.sign_up_error));
//                }
//            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                @Override
//                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
//                    // TODO: review this method
//                    Uri downloadUrl = taskSnapshot.getUploadSessionUri();
//                    if (downloadUrl != null)
//                        completeSignup(downloadUrl.toString());
//                    else
//                        showErrorMessage(getString(R.string.settings_avatar_error));
//
//                }
//            });
//        }
    }

    private void completeSignup(String avatar) {
//        user.setUserType(2);
        final ProgressDialog progress = ProgressDialog.show(this, null, null, true, false);
        progress.setContentView(R.layout.progress_layout);

        final String email = mEtSignUpEmail.getText().toString();
        final String password = mEtSignUpPassword.getText().toString();
        String parts[] = TextUtils.split(email, "@");
        String username = parts[0];

        HashMap<String, Object> params = new HashMap<>(5);

        params.put("email", email);
        params.put("username", username);
        params.put("password", password);
        params.put("avatar", avatar);
        params.put("countryId", selectedCountry.getId());
        params.put("displayName", displayName);
        params.put("conversaID", conversaId);
        params.put("categoryId", categoryId);
        NetworkingManager.getInstance().post("users", params, new FunctionCallback<JSONObject>() {
            @Override
            public void done(JSONObject json, FirebaseCustomException exception) {
                progress.dismiss();
                if (exception == null) {
                    SignListener(true, null);
                } else {
                    SignListener(false, exception);
                }
            }
        });
    }

    private boolean validateForm() {
        String title = null;

        if (mEtSignUpEmail.getText().toString().isEmpty()) {
            title = getString(R.string.common_field_required);
        } else if (!Utils.checkEmail(mEtSignUpEmail.getText().toString())) {
            title = getString(R.string.common_field_invalid_email);
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

    public void SignListener(boolean result, Exception error) {
        if (result) {
            String email = mEtSignUpEmail.getText().toString();
            String password = mEtSignUpPassword.getText().toString();

            FirebaseAuth mAuth = FirebaseAuth.getInstance();

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                AuthListener(true, null);
                            } else {
                                AuthListener(false, task.getException());
                            }
                        }
                    });
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

    public void AuthListener(boolean result, Exception error) {
        if (result) {
            AppActions.initSession(this);
        } else {
            showErrorMessage(getString(R.string.sign_up_error));
        }
    }

}
