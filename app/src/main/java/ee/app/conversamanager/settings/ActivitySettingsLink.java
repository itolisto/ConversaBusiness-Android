package ee.app.conversamanager.settings;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ee.app.conversamanager.ConversaApp;
import ee.app.conversamanager.R;
import ee.app.conversamanager.extendables.ConversaActivity;
import ee.app.conversamanager.view.MediumTextView;

/**
 * Created by edgargomez on 1/15/17.
 */

public class ActivitySettingsLink extends ConversaActivity implements View.OnClickListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_link);
        initialization();
    }

    @Override
    protected void initialization() {
        super.initialization();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle(R.string.preferences__link);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ((MediumTextView)findViewById(R.id.mtvConversaLink)).setText(
                "conversa.link/".concat(
                        ConversaApp.getInstance(this).getPreferences().getAccountConversaId()
                )
        );

        findViewById(R.id.btnShareConversa).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        final Intent intent_one = new Intent(android.content.Intent.ACTION_SEND);
        intent_one.setType("text/plain");
        // Add data to the intent, the receiving app will decide what to do with it.
        intent_one.putExtra(Intent.EXTRA_SUBJECT,
                getString(R.string.settings_using_conversa));
        intent_one.putExtra(Intent.EXTRA_TEXT,
                getString(R.string.settings_body_conversa));

        final List<ResolveInfo> activities = getPackageManager().queryIntentActivities(intent_one, 0);

        List<String> appNames = new ArrayList<>(2);
        List<Drawable> appIcons = new ArrayList<>(2);

        for (ResolveInfo info : activities) {
            appNames.add(info.loadLabel(getPackageManager()).toString());
            String packageName = info.activityInfo.packageName;

            try {
                Drawable icon = getPackageManager().getApplicationIcon(packageName);
                appIcons.add(icon);
            } catch (PackageManager.NameNotFoundException e) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    appIcons.add(getResources().getDrawable(R.drawable.ic_business_default, null));
                } else {
                    appIcons.add(getResources().getDrawable(R.drawable.ic_business_default));
                }
            }
        }

        ListAdapter adapter = new ArrayAdapterWithIcon(this, appNames, appIcons);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.settings_share_conversa));
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
    }

    private class ArrayAdapterWithIcon extends ArrayAdapter<String> {

        private List<Drawable> images;

        ArrayAdapterWithIcon(Context context, List<String> items, List<Drawable> images) {
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