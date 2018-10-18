package ee.app.conversamanager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.List;

import ee.app.conversamanager.extendables.ConversaFragment;
import ee.app.conversamanager.settings.ActivitySettingsAccount;
import ee.app.conversamanager.settings.ActivitySettingsChat;
import ee.app.conversamanager.settings.ActivitySettingsHelp;
import ee.app.conversamanager.settings.ActivitySettingsLink;
import ee.app.conversamanager.settings.ActivitySettingsNotifications;
import ee.app.conversamanager.settings.ActivitySettingsProfile;
import ee.app.conversamanager.settings.PreferencesKeys;
import ee.app.conversamanager.utils.Utils;
import ee.app.conversamanager.view.LightTextView;

/**
 * Created by edgargomez on 9/14/16.
 */
public class FragmentPreferences extends ConversaFragment implements View.OnClickListener, SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_preferences, container, false);
        ConversaApp.getInstance(getActivity())
                .getPreferences()
                .getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
        initialization(rootView);
        return rootView;
    }

    public void initialization(View v) {
        v.findViewById(R.id.btnViewProfile).setOnClickListener(this);
        v.findViewById(R.id.rlAccountContainer).setOnClickListener(this);
        v.findViewById(R.id.rlChatContainer).setOnClickListener(this);
        v.findViewById(R.id.rlNotificationContainer).setOnClickListener(this);
        v.findViewById(R.id.rlLanguageContainer).setOnClickListener(this);
        v.findViewById(R.id.rlShareContainer).setOnClickListener(this);
        v.findViewById(R.id.rlHelpContainer).setOnClickListener(this);

        SimpleDraweeView mIvProfile = (SimpleDraweeView) v.findViewById(R.id.sdvProfile);
        LightTextView mLtvWelcomeMessage = (LightTextView) v.findViewById(R.id.ltvWelcomeMessage);

        Uri uri = Utils.getUriFromString(
                ConversaApp.getInstance(getActivity()).getPreferences().getAccountAvatar()
        );

        if (uri == null) {
            uri = Utils.getDefaultImage(getActivity(), R.drawable.ic_business_default);
        }

        mIvProfile.setImageURI(uri);
        mLtvWelcomeMessage.setText(getString(R.string.preferences__header_hi,
                ConversaApp.getInstance(getActivity()).getPreferences().getAccountDisplayName()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ConversaApp.getInstance(getActivity())
                .getPreferences()
                .getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(PreferencesKeys.ACCOUNT_DISPLAY_NAME_KEY)) {
            ((LightTextView)getView().findViewById(R.id.ltvWelcomeMessage)).setText(
                    getString(R.string.preferences__header_hi,
                            ConversaApp.getInstance(getActivity()).getPreferences().getAccountDisplayName())
            );
        } else if (key.equals(PreferencesKeys.ACCOUNT_AVATAR_KEY)) {
            Uri uri = Utils.getUriFromString(
                    ConversaApp.getInstance(getActivity()).getPreferences().getAccountAvatar()
            );

            if (uri == null) {
                uri = Utils.getDefaultImage(getActivity(), R.drawable.ic_business_default);
            }

            ((SimpleDraweeView) getView().findViewById(R.id.sdvProfile)).setImageURI(uri);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnViewProfile: {
                Intent intent = new Intent(getContext(), ActivitySettingsProfile.class);
                startActivity(intent);
                getActivity().overridePendingTransition(0, 0);
                break;
            }
            case R.id.rlAccountContainer: {
                Intent intent = new Intent(getContext(), ActivitySettingsAccount.class);
                startActivity(intent);
                break;
            }
            case R.id.rlChatContainer: {
                Intent intent = new Intent(getContext(), ActivitySettingsChat.class);
                startActivity(intent);
                break;
            }
            case R.id.rlNotificationContainer: {
                Intent intent = new Intent(getContext(), ActivitySettingsNotifications.class);
                startActivity(intent);
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
                Intent intent = new Intent(getActivity(), ActivitySettingsLink.class);
                startActivity(intent);
                return;
            }
            case R.id.rlHelpContainer: {
                Intent intent = new Intent(getContext(), ActivitySettingsHelp.class);
                startActivity(intent);
                break;
            }
        }
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
