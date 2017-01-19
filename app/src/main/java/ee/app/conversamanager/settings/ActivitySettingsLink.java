package ee.app.conversamanager.settings;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.View;

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
                ""
        );

        findViewById(R.id.btnShareConversa).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

    }

}