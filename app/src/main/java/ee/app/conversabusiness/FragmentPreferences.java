package ee.app.conversabusiness;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;
import com.facebook.drawee.view.SimpleDraweeView;

import ee.app.conversabusiness.extendables.ConversaFragment;
import ee.app.conversabusiness.settings.ActivitySettingsAccount;
import ee.app.conversabusiness.settings.ActivitySettingsChat;
import ee.app.conversabusiness.settings.ActivitySettingsHelp;
import ee.app.conversabusiness.settings.ActivitySettingsNotifications;
import ee.app.conversabusiness.utils.Utils;
import ee.app.conversabusiness.view.ImageBottomSheetDialogFragment;
import ee.app.conversabusiness.view.LightTextView;
import ee.app.conversabusiness.view.RegularTextView;

/**
 * Created by edgargomez on 9/14/16.
 */
public class FragmentPreferences extends ConversaFragment implements
        View.OnClickListener {

    private ImageBottomSheetDialogFragment myBottomSheet;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_settings_main, container, false);
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

        myBottomSheet = ImageBottomSheetDialogFragment.newInstance((AppCompatActivity)getActivity());

        SimpleDraweeView mSdvBusinessImage = (SimpleDraweeView) v.findViewById(R.id.sdvBusinessImage);
        RegularTextView mRtvHeaderName = (RegularTextView) v.findViewById(R.id.rtvHeaderName);
        LightTextView mLtvHeaderDate = (LightTextView) v.findViewById(R.id.ltvHeaderDate);

        mSdvBusinessImage.setOnClickListener(this);
        mSdvBusinessImage.setImageURI(Utils.getUriFromString(
                ConversaApp.getInstance(getContext()).getPreferences().getAvatarUrl()
        ));

        mRtvHeaderName.setText(
                ConversaApp.getInstance(getContext()).getPreferences().getDisplayName()
        );

        mLtvHeaderDate.setText(
                ConversaApp.getInstance(getContext()).getPreferences().getConversaId()
        );
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;

        switch (v.getId()) {
            case R.id.sdvBusinessImage:
                myBottomSheet.show(getActivity().getSupportFragmentManager(), myBottomSheet.getTag());
                break;
            case R.id.rlAccountContainer:
                intent = new Intent(getContext(), ActivitySettingsAccount.class);
                break;
            case R.id.rlChatContainer:
                intent = new Intent(getContext(), ActivitySettingsChat.class);
                break;
            case R.id.rlNotificationContainer:
                intent = new Intent(getContext(), ActivitySettingsNotifications.class);
                break;
            case R.id.rlLanguageContainer:
                new MaterialDialog.Builder(getContext())
                        .title(R.string.preferences__language)
                        .items(R.array.language_entries)
                        .itemsCallbackSingleChoice(
                                ConversaApp.getInstance(getContext()).getPreferences().getLanguagePosition(),
                                new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                if (ConversaApp.getInstance(getContext()).getPreferences().getLanguagePosition()
                                        != which)
                                {
                                    ConversaApp.getInstance(getContext()).getPreferences().setLanguage(
                                            getResources().getStringArray(R.array.language_values)[which]
                                    );
                                    getActivity().recreate();
                                }
                                return true;
                            }
                        })
                        .show();
                break;
            case R.id.rlShareContainer:
                intent = new Intent(android.content.Intent.ACTION_SEND);
                intent.setType("text/plain");
                // Add data to the intent, the receiving app will decide what to do with it.
                String subject = getString(R.string.settings_using_conversa);
                intent.putExtra(Intent.EXTRA_SUBJECT, subject);
                String body = getString(R.string.settings_body_conversa);
                intent.putExtra(Intent.EXTRA_TEXT, body);
                startActivity(Intent.createChooser(intent, getString(R.string.settings_share_conversa)));
                return;
            case R.id.rlHelpContainer:
                intent = new Intent(getContext(), ActivitySettingsHelp.class);
                break;
        }

        if (intent != null) {
            startActivity(intent);
        }
    }
}
