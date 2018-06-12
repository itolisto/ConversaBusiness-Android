package ee.app.conversamanager.login;

import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
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

import com.afollestad.materialdialogs.MaterialDialog;
import com.facebook.drawee.view.SimpleDraweeView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import ee.app.conversamanager.ConversaApp;
import ee.app.conversamanager.R;
import ee.app.conversamanager.extendables.BaseActivity;
import ee.app.conversamanager.interfaces.FunctionCallback;
import ee.app.conversamanager.model.nCategory;
import ee.app.conversamanager.networking.FirebaseCustomException;
import ee.app.conversamanager.networking.NetworkingManager;
import ee.app.conversamanager.utils.Const;
import ee.app.conversamanager.utils.ImageFilePath;
import ee.app.conversamanager.utils.Logger;
import ee.app.conversamanager.view.AvatarSheetDialog;

/**
 * Created by edgargomez on 8/12/16.
 */
public class ActivityRegister extends BaseActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private EditText mEtName;
    private EditText mEtConversaId;
    private nCategory selectedCategory;
    private SimpleDraweeView mIvAvatar;
    private String selectedImage;
    private List<nCategory> categories;
    private AvatarSheetDialog myBottomSheet;

    CustomAdapter dataAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        initialization();
    }

    @Override
    protected void initialization() {
        super.initialization();

        mEtName = (EditText) findViewById(R.id.etName);
        mEtConversaId = (EditText) findViewById(R.id.etConversaId);
        mIvAvatar = (SimpleDraweeView) findViewById(R.id.ivAvatar);

        Spinner mSpCategory = (Spinner) findViewById(R.id.spCategory);

        findViewById(R.id.tilName).setOnClickListener(this);
        findViewById(R.id.tilConversaId).setOnClickListener(this);
        findViewById(R.id.ivAdd).setOnClickListener(this);

        Button mBtnSignUpContinue = (Button) findViewById(R.id.btnSignUpContinue);
        mBtnSignUpContinue.setOnClickListener(this);
        mBtnSignUpContinue.setTypeface(ConversaApp.getInstance(this).getTfRalewayMedium());

        myBottomSheet = AvatarSheetDialog.newInstance(this, true);

        categories = new ArrayList<>(1);
        dataAdapter = new CustomAdapter(this, android.R.layout.simple_spinner_item, categories);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpCategory.setAdapter(dataAdapter);
        mSpCategory.setOnItemSelectedListener(this);

        String language = ConversaApp.getInstance(this).getPreferences().getLanguage();

        if (language.equals("zz")) {
            if (Locale.getDefault().getLanguage().startsWith("es")) {
                language = "es";
            } else {
                language = "en";
            }
        }

        HashMap<String, Object> params = new HashMap<>(1);
        params.put("language", language);

        NetworkingManager.getInstance().post("getOnlyCategories", params, new FunctionCallback<Object>() {
            @Override
            public void done(Object json, FirebaseCustomException exception) {
                if (exception != null) {
                    showErrorMessage(getString(R.string.sign_up_register_categories_error));
                } else {
                    try {
                        JSONArray categories = new JSONArray(json.toString());

                        int size = categories.length();
                        List<nCategory> categoriesList = new ArrayList<>(size);

                        for (int i = 0; i < size; i++) {
                            JSONObject jsonCategory = categories.getJSONObject(i);

                            nCategory category = new nCategory(
                                    jsonCategory.optString("id", ""),
                                    jsonCategory.optString("na", ""),
                                    "");

                            categoriesList.add(category);
                        }

                        Collections.sort(categoriesList, new Comparator<nCategory>() {
                            @Override
                            public int compare(final nCategory object1, final nCategory object2) {
                                return object1.getCategoryName(getApplicationContext())
                                        .compareTo(object2.getCategoryName(getApplicationContext()));
                            }
                        });

                        final List<nCategory> catlist = categoriesList;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dataAdapter.setItems(catlist);
                            }
                        });
                    } catch (JSONException ignored) {
                        showErrorMessage(getString(R.string.sign_up_register_categories_error));
                    }
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent upIntent = NavUtils.getParentActivityIntent(this);
        // Delete image
        if (!TextUtils.isEmpty(selectedImage)) {
            try {
                boolean result = new File(selectedImage).delete();
                Logger.error("onBackPressed", (result) ? "Image deleted" : "Image not deleted, be careful");
            } catch (Exception ignored) {}
        }

        if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
            // This activity is NOT part of this app's task, so create a new task
            // when navigating up, with a synthesized back stack.
            TaskStackBuilder.create(this)
                    // Add all of this activity's parents to the back stack
                    .addNextIntentWithParentStack(upIntent)
                    // Navigate up to the closest parent
                    .startActivities();
        } else {
            // This activity is part of this app's task, so simply
            // navigate up to the logical parent activity.
            NavUtils.navigateUpTo(this, upIntent);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivAdd: {
                myBottomSheet.show(getSupportFragmentManager(), myBottomSheet.getTag());
                break;
            }
            case R.id.tilName: {
                mEtName.requestFocus();
                break;
            }
            case R.id.tilConversaId: {
                mEtConversaId.requestFocus();
                break;
            }
            case R.id.btnSignUpContinue: {
                if (validateForm()) {
                    final MaterialDialog dialog = new MaterialDialog.Builder(this)
                            .content(R.string.sign_up_checking_conversa_id)
                            .progress(true, 0)
                            .show();

                    HashMap<String, Object> params = new HashMap<>(1);
                    params.put("conversaID", mEtConversaId.getText().toString());

                    NetworkingManager.getInstance().post("businessValidateId", params, new FunctionCallback<Object>() {
                        @Override
                        public void done(Object object, FirebaseCustomException e) {
                            dialog.dismiss();

                            if (e == null) {
                                Intent intent = new Intent(getApplicationContext(), ActivityRegisterComplete.class);
                                intent.putExtra(Const.iExtraSignUpDisplayName, mEtName.getText().toString());
                                intent.putExtra(Const.iExtraSignUpConversaId, mEtConversaId.getText().toString());
                                intent.putExtra(Const.iExtraSignUpCategory, selectedCategory.getObjectId());
                                if (!TextUtils.isEmpty(selectedImage))
                                    intent.putExtra(Const.iExtraSignUpAvatar, selectedImage);
                                startActivity(intent);
                            } else {
                                showErrorMessage(getString(R.string.conversa_id_already_taken));
                            }
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
        } else if (mEtConversaId.getText().toString().isEmpty()) {
            title = getString(R.string.common_field_required);
            select = mEtConversaId;
        } else if (selectedCategory == null) {
            title = getString(R.string.common_field_required);
        }

        if (title != null) {
            final EditText finalSelect = select;
            new AlertDialog.Builder(this)
                    .setTitle(title)
                    .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            if (finalSelect != null)
                                finalSelect.requestFocus();
                        }
                    })
                    .show();
            return false;
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            switch (requestCode) {
                case Const.CAPTURE_MEDIA: {
                    String path = ImageFilePath.getPath(this, Uri.parse(data.getStringExtra("imageUri")));
                    selectedImage = path;
                    mIvAvatar.setImageURI(Uri.fromFile(new File(path)));
                    break;
                }
            }
        } else {
            Logger.error("onActivityResult", "Error");
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (categories.size() > 0) {
            selectedCategory = categories.get(position);
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

    private class CustomAdapter extends ArrayAdapter<nCategory> {

        private Context context;
        private List<nCategory> itemList;

        CustomAdapter(Context context, int textViewResourceId, List<nCategory> itemList) {
            super(context, textViewResourceId, itemList);
            this.context=context;
            this.itemList=itemList;
        }

        void setItems(List<nCategory> itemList) {
            this.itemList.addAll(itemList);
            notifyDataSetChanged();
        }

        private @NonNull View getCustomView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            TextView v = (TextView) super.getView(position, convertView, parent);
            v.setTypeface(ConversaApp.getInstance(context).getTfRalewayRegular());
            v.setText(itemList.get(position).getCategoryName(context));
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

}