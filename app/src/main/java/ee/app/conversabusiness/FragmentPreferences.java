package ee.app.conversabusiness;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ee.app.conversabusiness.extendables.ConversaFragment;
import ee.app.conversabusiness.settings.ActivitySettingsAccount;
import ee.app.conversabusiness.settings.ActivitySettingsChat;
import ee.app.conversabusiness.settings.ActivitySettingsHelp;
import ee.app.conversabusiness.settings.ActivitySettingsNotifications;
import ee.app.conversabusiness.view.LightTextView;

/**
 * Created by edgargomez on 9/14/16.
 */
public class FragmentPreferences extends ConversaFragment implements View.OnClickListener {

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_preferences, container, false);
        initialization(rootView);
        return rootView;
    }

    public void initialization(View v) {
        v.findViewById(R.id.rlAccountContainer).setOnClickListener(this);
        v.findViewById(R.id.rlChatContainer).setOnClickListener(this);
        v.findViewById(R.id.rlNotificationContainer).setOnClickListener(this);
        v.findViewById(R.id.rlLanguageContainer).setOnClickListener(this);
        v.findViewById(R.id.rlShareContainer).setOnClickListener(this);
        v.findViewById(R.id.rlHelpContainer).setOnClickListener(this);

        ImageView mIvProfile = (ImageView) v.findViewById(R.id.ivProfile);
        LightTextView mLtvWelcomeMessage = (LightTextView) v.findViewById(R.id.ltvWelcomeMessage);

        if (ConversaApp.getInstance(getContext()).getPreferences().getAccountGender() == 0) {
            mIvProfile.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_person_female));
        } else {
            mIvProfile.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_person));
        }

        String mfasld = ConversaApp.getInstance(getActivity()).getPreferences().getAccountDisplayName();
        mLtvWelcomeMessage.setText(mfasld);
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;

        switch (v.getId()) {
            case R.id.rlAccountContainer: {
                intent = new Intent(getContext(), ActivitySettingsAccount.class);
                break;
            }
            case R.id.rlChatContainer: {
                intent = new Intent(getContext(), ActivitySettingsChat.class);
                break;
            }
            case R.id.rlNotificationContainer: {
                intent = new Intent(getContext(), ActivitySettingsNotifications.class);
                break;
            }
            case R.id.rlLanguageContainer: {
                final int index;

                switch(ConversaApp.getInstance(getActivity()).getPreferences().getLanguage()) {
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

                AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
                b.setTitle(R.string.language_spinner_title);
                b.setSingleChoiceItems(R.array.language_entries, index, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (which != index) {
                            ConversaApp.getInstance(getActivity())
                                    .getPreferences()
                                    .setLanguage(getResources()
                                            .getStringArray(R.array.language_values)[which]
                                    );
                            getActivity().recreate();
                        }
                    }
                });
                b.show();
                return;
            }
            case R.id.rlShareContainer: {
                final Intent intent_one = new Intent(android.content.Intent.ACTION_SEND);
                intent_one.setType("text/plain");
                // Add data to the intent, the receiving app will decide what to do with it.
                intent_one.putExtra(Intent.EXTRA_SUBJECT,
                        getActivity().getString(R.string.settings_using_conversa));
                intent_one.putExtra(Intent.EXTRA_TEXT,
                        getActivity().getString(R.string.settings_body_conversa));

                final List<ResolveInfo> activities = getActivity()
                        .getPackageManager().queryIntentActivities(intent_one, 0);

                List<String> appNames = new ArrayList<>(2);
                List<Drawable> appIcons = new ArrayList<>(2);

                for (ResolveInfo info : activities) {
                    appNames.add(info.loadLabel(getActivity().getPackageManager()).toString());
                    String packageName = info.activityInfo.packageName;

                    try {
                        Drawable icon = getActivity().getPackageManager().getApplicationIcon(packageName);
                        appIcons.add(icon);
                    } catch (PackageManager.NameNotFoundException e) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            appIcons.add(getResources().getDrawable(R.drawable.ic_business_default, null));
                        } else {
                            appIcons.add(getResources().getDrawable(R.drawable.ic_business_default));
                        }
                    }
                }

                ListAdapter adapter = new ArrayAdapterWithIcon(getActivity(), appNames, appIcons);

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getActivity().getString(R.string.settings_share_conversa));
                builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ResolveInfo info = activities.get(which);
                        //if (info.activityInfo.packageName.equals("com.facebook.katana")) {
                        // Facebook was chosen
                        //}
                        // Start the selected activity
                        intent_one.setPackage(info.activityInfo.packageName);
                        startActivity(intent_one);
                    }
                });

                AlertDialog share = builder.create();
                share.show();
                return;
            }
            case R.id.rlHelpContainer: {
                intent = new Intent(getContext(), ActivitySettingsHelp.class);
                break;
            }
        }

        startActivity(intent);
    }

    public class ArrayAdapterWithIcon extends ArrayAdapter<String> {

        private List<Drawable> images;

        public ArrayAdapterWithIcon(Context context, List<String> items, List<Drawable> images) {
            super(context, android.R.layout.select_dialog_item, items);
            this.images = images;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            TextView textView = (TextView) view.findViewById(android.R.id.text1);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                textView.setCompoundDrawablesRelativeWithIntrinsicBounds(images.get(position), null, null, null);
            } else {
                textView.setCompoundDrawablesWithIntrinsicBounds(images.get(position), null, null, null);
            }
            textView.setCompoundDrawablePadding(
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getContext().getResources().getDisplayMetrics()));
            return view;
        }
    }

}
